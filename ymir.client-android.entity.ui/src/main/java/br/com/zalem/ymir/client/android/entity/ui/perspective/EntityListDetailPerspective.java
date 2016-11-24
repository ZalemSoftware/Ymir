package br.com.zalem.ymir.client.android.entity.ui.perspective;

import android.annotation.SuppressLint;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.RelationshipViolationException;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.ui.BuildConfig;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingPermissions;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListFilter;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListOrder;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.ITabbedListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityUIEventManager;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityListFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityListFragment.OnEntityRecordClickListener;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityListFragment.OnRecordsChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityDetailFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityDetailPagerFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityListFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityListPagerFragment;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.IEntityRecordListActionProvider;
import br.com.zalem.ymir.client.android.entity.ui.search.EntitySearchSuggestionsProvider;
import br.com.zalem.ymir.client.android.entity.ui.search.ISearchableManager;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment;
import br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment.IConfirmationDialogListener;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import br.com.zalem.ymir.client.android.perspective.IPerspectiveManager;

import static br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils.createEntitiesDisplayList;
import static br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils.createMessage;
import static br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils.getEntityDisplayName;

/**
 * Perspectiva que dispõe uma lista de registros de uma entidade e seus detalhes.<br>
 * Para a lista de registros é utilizado o fragmento {@link EntityListFragment} ou o {@link EntityListPagerFragment} e 
 * para os detalhes de um registro o fragmento {@link EntityDetailFragment} ou o {@link EntityDetailPagerFragment},
 * dependendo da configuração definida para a entidade.<br>
 * <br>
 * O EntityListDetailPerspective tem seu comportamento definido através da action e da categoria de seu {@link Intent}.
 * A categoria define o nome da entidade que a Perspectiva irá utilizar. Por este motivo, deve ser definida exatamente
 * uma categoria. A action pode ser uma das seguintes:
 * <ul>
 * 	<li>{@link #ENTITY_LIST_DETAIL_ACTION}: sem modo definido, incia com o último modo mostrado pela perspectiva. O modo padrão é o de lista;</li>
 * 	<li>{@link #ENTITY_LIST_ACTION}: inicia no modo de exibição da lista de registros da entidade;</li>
 * 	<li>{@link #ENTITY_DETAIL_ACTION}: inicia no modo de exibição dos detalhes de um registro da entidade. O id do
 * 		registro deve ser passado como um Extra com o nome {@link #RECORD_ID_EXTRA}.</li>
 * </ul>
 * <br>
 * A perspectiva pode habilitar a funcionalidade de pesquisa de registros do EntityListFragment.
 * Para isto, é necessário que o {@link ISearchableManager} seja definido e que a entidade possua <code>Searchable</code> disponível.<br>
 * <br>
 * O EntityListFragment suporta a sugestão de termos de pesquisas bem sucedidas recentes. Para habilitar esta
 * funcionalidade, a aplicação deve declarar um <code>Content Provider</code> do tipo {@link EntitySearchSuggestionsProvider} e o Searchable
 * deve conter o atributo <code>searchSuggestAuthority</code> com o {@link EntitySearchSuggestionsProvider#AUTHORITY} e o atributo
 * <code>searchSuggestSelection</code> com o nome da entidade que a Perspectiva representa (por exemplo, cliente).<br>
 * <br>
 * As pesquisas no EntityListFragment farão com que a Perspectiva habilite botão <code>Up</code>, provendo uma maneira de
 * desfazer a pesquisa e trazer novamente a listagem de todos os registros. O botão <code>Back</code> também desfaz a
 * pesquisa.
 * 
 * @see EntityListFragment
 * @see EntityDetailFragment
 * @see IPerspectiveManager
 * @see EntitySearchSuggestionsProvider
 * 
 * @author Thiago Gesser
 */
public class EntityListDetailPerspective extends EntityDetailPerspective implements IEntityRecordListActionProvider, OnEntityRecordClickListener, OnRecordsChangeListener {

	/**
	 * Ação de listagem ou detalhamento de registros de entidade, iniciando no último modo utilizado pela perspectiva ou
	 * no modo lista se for a primeira vez.
	 */
	public static final String ENTITY_LIST_DETAIL_ACTION = EntityListDetailPerspective.class.getPackage().getName() + ".LIST_DETAIL";
	/**
	 * Ação de listagem de registros de entidade.
	 */
	public static final String ENTITY_LIST_ACTION = EntityListDetailPerspective.class.getPackage().getName() + ".LIST";

    /**
     * Argumento que desabilita o comportamento de iniciar a edição ao clicar em um registro editável da lista de registros.
     * Com isto, o detalhamento será mostrado ao clicar em qualquer registro. Entretanto, registros editáveis ainda terão a ação de edição disponível.<br>
     * <br>
     * Não é necessário definir valor para o argumento, apenas sua presença já desabilita o comportamento.
     */
    public static final String DISABLE_EDIT_ON_CLICK_ARGUMENT = "DISABLE_EDIT_ON_CLICK";
    /**
     * Argumento que habilita o uso do FAB (Floating Action Button) de adição na perspectiva, fazendo com que a ação não seja mostrada na App Bar.<br>
     * <br>
     * Não é necessário definir valor para o argumento, apenas sua presença já habilita o comportamento.
     */
    public static final String ENABLE_FAB_ADD_ARGUMENT = "ENABLE_FAB_ADD";

	/**
	 * Extra do tipo {@link ISelectQuery} que define a origem dos dados a ser utilizada nesta perspectiva ao invés de todos
	 * os dados disponíveis para a entidade.
	 * A query deve selecionar registros completos (sem campos específicos) da mesma entidade da perspectiva, podendo utilizar filtros de qualquer tipo.
	 */
	public static final String SOURCE_QUERY_EXTRA = "SOURCE_QUERY_EXTRA";
	
	/**
	 * Ações suportadas pela perspectiva de lista/detalhes.
	 */
	public static final String[] SUPPORTED_ACTIONS = new String[] {ENTITY_LIST_DETAIL_ACTION, ENTITY_LIST_ACTION, ENTITY_DETAIL_ACTION};
	
	protected static final String SAVED_MODE = "SAVED_MODE";
	protected static final int MODE_LIST = 0;
	protected static final int MODE_DETAIL = 1;
	
	protected static final String LIST_FRAGMENT_TAG = "LIST_FRAGMENT_TAG";
	
	protected AbstractEntityListFragment listFragment;
	private int mode = -1;

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		int initialMode = getInitialMode(savedInstanceState);
		switch (initialMode) {
			case MODE_LIST:
				showList();
				break;
			case MODE_DETAIL:
				showDetail(null);
				break;

			default:
				throw new IllegalStateException("Unsupported mode: " + initialMode);
		}
	}

	@Override
	protected String[] getSupportedActions() {
		return SUPPORTED_ACTIONS;
	}
	
	@Override
	protected void createFragments(FragmentTransaction fragmentTransaction, int containerViewId) {
		super.createFragments(fragmentTransaction, containerViewId);
		
		if (useEntityListPager()) {
			listFragment = new EntityListPagerFragment();
		} else {
			listFragment = new EntityListFragment();
		}
		
		fragmentTransaction.add(containerViewId, listFragment, LIST_FRAGMENT_TAG);
	}
	
	@Override
	protected void configurePerspective(Bundle extras, IEntityDAO entityDAO) {
		ISelectQuery sourceQuery = null;
		if (extras != null) {
			sourceQuery = extras.getParcelable(SOURCE_QUERY_EXTRA);
		}
				
		//Se a não foi definido um Searchable para a entidade, não haverá o botão de pesquisa na ActionBar.
		SearchableInfo searchableInfo = null;
		ISearchableManager searchableManager = getSearchableManager();
		if (searchableManager != null) {
			searchableInfo = searchableManager.getSearchableInfo(entityDAO.getEntityMetadata().getName());
		}
		
		//Apenas define os argumentos no fragmento se necessário.
		if (sourceQuery != null || searchableInfo != null || isAddFABEnabled()) {
			Bundle listFragmentArgs = new Bundle();
			if (sourceQuery != null) {
				listFragmentArgs.putParcelable(EntityListFragment.SOURCE_QUERY_ARGUMENT, sourceQuery);
			}
			if (searchableInfo != null) {
				listFragmentArgs.putParcelable(EntityListFragment.SEARCHABLE_ARGUMENT, searchableInfo);
			}
            if (isAddFABEnabled() && canAddRecord()) {
                listFragmentArgs.putInt(EntityListFragment.THEME_ARGUMENT, R.style.ThemeOverlay_Ymir_EntityFragment_FAB);
            }
			listFragment.setArguments(listFragmentArgs);
		}
		
		
		//Só utiliza a definição do registro no fragmento de detalhe via extra do Intent se for action de detalhe e 
		//já não houver um registro sendo mostrado nos detalhes.
		if (detailFragment.getEntityRecord() == null && getIntent().getAction().equals(ENTITY_DETAIL_ACTION)) {
			super.configurePerspective(extras, entityDAO);
		}
	}
	
	@Override
	protected void checkExtras(Serializable recordId, Parcelable recordSavedState) {
		//Só suporta o uso do extra de id.
		if (recordSavedState != null) {
			throw new IllegalArgumentException("The extra RECORD_EXTRA is not supported by " + EntityListDetailPerspective.class.getSimpleName());
		}
		
		super.checkExtras(recordId, recordSavedState);
    }

    @Override
	protected void restoreFragments(FragmentManager fragmentManager) {
		super.restoreFragments(fragmentManager);
		
		listFragment = (AbstractEntityListFragment) getChildFragmentManager().findFragmentByTag(LIST_FRAGMENT_TAG);

        //Se o fragmento do dialogo de confirmação foi restaurado, define o listener nele novamente.
        ConfirmationDialogFragment dialogFragment = (ConfirmationDialogFragment) getChildFragmentManager().findFragmentByTag(ConfirmationDialogFragment.CONFIRMATION_DIALOG_FRAGMENT_TAG);
        if (dialogFragment != null) {
            dialogFragment.setListener(new RecordDeletionConfirmationDialogListener());
        }
    }
	
	@Override
	protected void initializeFragments(MaskManager maskManager) {
		super.initializeFragments(maskManager);

		//Inicialização do fragmento de lista.
		ITabbedListDisplayConfig listDisplayConfig = getListDisplayConfig();
		ILayoutConfig<ListLayoutType> listLayout = listDisplayConfig.getLayout();
		IListFilter listFilter = listDisplayConfig.getFilter();
		IListOrder listOrder = listDisplayConfig.getOrder();
		if (useEntityListPager(listDisplayConfig)) {
			EntityListPagerFragment frag = (EntityListPagerFragment) listFragment;
			IListTab[] listTabs = listDisplayConfig.getTabs();
			frag.initialize(entityDAO, listTabs, maskManager, listLayout, listFilter, listOrder);
		} else {
			EntityListFragment frag = (EntityListFragment) listFragment;
			frag.initialize(entityDAO, listLayout, listFilter, listOrder, maskManager);
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		//Faz isto no onViewStateRestored pq apenas a partir deste momento o listFragment poderá estar totalmente iniciado (views criadas).
		configureListFragmentEntityRecordClickListener();
		configureListFragmentSearchListener();
		configureListFragmentRecordActions();
		configureListFragmentRecordsChangeListener();
	}

    @Override
    protected IEntityConfig getEntityConfig(String entityName) {
        IEntityConfig entityConfig = super.getEntityConfig(entityName);
        if (entityConfig.getList() == null) {
            throw new IllegalArgumentException(String.format("Missing required configuration \"%s\" of entity %s.", "list", entityName));
        }
        return entityConfig;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable(SAVED_MODE, mode);
	}

    @Override
	public boolean isUpEnabled() {
        switch (mode) {
            case MODE_LIST:
				return isSearching(listFragment);

            case MODE_DETAIL:
				return true;

            default:
                throw new IllegalStateException("Unsupported mode: " + mode);
        }
    }

	@Override
	public void onUpPressed() {
		switch (mode) {
			case MODE_LIST:
				listFragment.undoSearch();
				break;
			case MODE_DETAIL:
				if (super.isUpEnabled()) {
					super.onUpPressed();
				} else {
					showList();
	            }
				break;

			default:
				throw new IllegalStateException("Unsupported mode: " + mode);
		}
	}

	@Override
	public boolean onBackPressed() {
		if (isUpEnabled()) {
			switch (mode) {
				case MODE_LIST:
					listFragment.undoSearch();
					break;
				case MODE_DETAIL:
					if (!super.onBackPressed()) {
                        showList();
					}
					break;

				default:
					throw new IllegalStateException("Unsupported mode: " + mode);
			}
            return true;
        }

		return false;
	}

    @Override
    protected String getSearchTitle() {
        switch (mode) {
            case MODE_DETAIL:
                //A prioridade de título de busca é da parte de detalhe.
                String searchTitle = super.getSearchTitle();
                if (searchTitle != null) {
                    return searchTitle;
                }
                //$FALL-THROUGH$
            case MODE_LIST:
                //Cai aqui se o modo de detalhe não estiver em busca ou se estiver no modo de lista.
                if (isSearching(listFragment)) {
                    return listFragment.getSimpleSearchQuery();
                }
                return null;
            default:
                throw new IllegalStateException("Unsupported mode: " + mode);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        //Como por enquanto todas as ações são de edição, só coloca elas se for necessário.
        if (isEditingEnabled()) {
            inflater.inflate(R.menu.entity_list_detail_perspective_actions, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!isEditingEnabled()) {
            return;
        }

        boolean enableAddAction =  !isAddFABEnabled() && isAddActionEnabled();
        MenuItem addMenuItem = menu.findItem(R.id.action_add);
        addMenuItem.setVisible(enableAddAction).setEnabled(enableAddAction);

        IEntityRecord record = detailFragment.getEntityRecord();
        boolean enableDeleteAction = mode == MODE_DETAIL && record != null;
        if (enableDeleteAction) {
            IEditingPermissions permissions = getRecordPermissions(record, entityConfig);
            enableDeleteAction = permissions != null && permissions.canDelete();
        }
        MenuItem deleteMenuItem = menu.findItem(R.id.action_delete);
        deleteMenuItem.setVisible(enableDeleteAction).setEnabled(enableDeleteAction);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            checkEditingEnabled();

            editRecord(entityDAO, null);
            return true;
        }
        if (item.getItemId() == R.id.action_delete) {
            checkEditingEnabled();

            deleteRecord(entityDAO, detailFragment.getEntityRecord());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean hasFABs() {
        return isAddFABEnabled();
    }

    @Override
    public void onCreateFABs(YmirMenu fabMenu, YmirMenuInflater menuInflater) {
        super.onCreateFABs(fabMenu, menuInflater);

        if (isEditingEnabled()) {
            menuInflater.inflate(R.xml.entity_list_detail_perspective_fabs, fabMenu);
        }
    }

    @Override
    public boolean isFABAvailable(YmirMenuItem fabItem) {
        if (fabItem.getId() == R.id.action_add) {
            return isAddFABEnabled() && isAddActionEnabled();
        }

        return super.isFABAvailable(fabItem);
    }

    @Override
    public void onFABClicked(YmirMenuItem fabItem) {
        if (fabItem.getId() == R.id.action_add) {
            checkEditingEnabled();

            editRecord(entityDAO, null);
            return;
        }

        super.onFABClicked(fabItem);
    }


    @Override
    public void onCreateRecordActionMenu(YmirMenu menu, YmirMenuInflater menuInflater) {
        //Como por enquanto todas as ações são de edição, só coloca elas se for necessário.
        if (!isEditingEnabled()) {
            return;
        }

        menuInflater.inflate(R.xml.entity_list_detail_perspective_record_actions, menu);
    }

    @Override
    public boolean isRecordActionItemAvailable(IEntityRecord record, YmirMenuItem item) {
        if (isEditingEnabled()) {
            if (item.getId() == R.id.record_action_delete) {
                IEditingPermissions permissions = getRecordPermissions(record, entityConfig);
                return permissions != null && permissions.canDelete();
            }
            if (item.getId() == R.id.record_action_edit) {
                //Se já abre a edição com um click, não preicsa colocar a ação de edição.
                return !isEditOnClickEnabled(record) && isEditable(record);
            }
            if (item.getId() == R.id.record_action_duplicate) {
                return entityConfig.getList().isEnableDuplicate();
            }
        }

        return false;
    }

    @Override
    public void onRecordActionItemSelected(IEntityRecord record, YmirMenuItem item) {
        checkEditingEnabled();

        if (item.getId() == R.id.record_action_delete) {
            deleteRecord(entityDAO, record);
        } else if (item.getId() == R.id.record_action_edit) {
            editRecord(entityDAO, record);
        } else if (item.getId() == R.id.record_action_duplicate) {
            Intent intent = onCreateRecordDuplicationIntent(record);
            startPerspective(intent);
        }
    }

    @Override
    public void onEntityRecordClick(IEntityRecord record) {
        if (isEditingEnabled() && isEditOnClickEnabled(record) && isEditable(record)) {
            //Se o registro pode ser editado, inicia a perspectiva de edição.
            editRecord(entityDAO, record);
        } else {
            //Mostra os detalhes do registro.
            showDetail(record);
        }
    }

	@Override
	public List<IEntityRecord> beforeRecordsChange(EntityListFragment listFragment, List<IEntityRecord> records) {
		IEntityUIEventManager eventManager = getEventManager();
		if (eventManager != null) {
			return eventManager.fireBeforeListRecordsEvent(entityDAO.getEntityMetadata().getName(), records);
		}

		return null;
	}

	@Override
	public void afterRecordsChange(EntityListFragment listFragment, List<IEntityRecord> records) {
		IEntityUIEventManager eventManager = getEventManager();
		if (eventManager != null) {
			eventManager.fireAfterListRecordsEvent(entityDAO.getEntityMetadata().getName(), records);
		}
	}


    /**
	 * Obtém o modo inicial da perspectiva, podendo ser {@link #MODE_LIST} ou {@link #MODE_DETAIL}.
	 *  
	 * @param savedInstanceState estado salvo da perspectiva se ela está sendo reiniciada ou <code>null</code> se está
	 * sendo iniciada pela primeira vez.
	 * @return o modo inicial da perspectiva.
	 */
	@SuppressLint("Assert")
	protected int getInitialMode(Bundle savedInstanceState) {
		String action = getIntent().getAction();
		if (action.equals(ENTITY_LIST_ACTION)) {
			return MODE_LIST;
		} else if (action.equals(ENTITY_DETAIL_ACTION)) {
			return MODE_DETAIL;
		} else {
			assert action.equals(ENTITY_LIST_DETAIL_ACTION);
			
			//Sem modo definido, irá utilizar o modo padrão ou o último modo utilizado.
			if (savedInstanceState == null) {
				return MODE_LIST;
			}
			
			return savedInstanceState.getInt(SAVED_MODE);
		}
	}

    /**
     * Obtém o modo atual da perspectiva, podendo ser {@link #MODE_LIST} ou {@link #MODE_DETAIL}.
     *
     * @return o modo obtido.
     */
    protected int getMode() {
        return mode;
    }

	/**
	 * Torna o fragmento de lista visível e esconde o de detalhes.
	 */
	protected void showList() {
        if (mode == MODE_LIST) {
            return;
        }

		mode = MODE_LIST;
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.show(listFragment);
		fragmentTransaction.hide(detailFragment);
        fragmentTransaction.commit();

		fragmentManager.executePendingTransactions();
		detailFragment.setEntityRecord(null);

        notifyFABsChanged();
	}

	/**
	 * Torna o fragmento de detalhes visível e esconde o de lista, mostrando os detalhes do registro.
     *
	 * @param record registro que será detalhado.
	 */
	protected void showDetail(IEntityRecord record) {
        if (mode == MODE_DETAIL) {
            return;
        }

		mode = MODE_DETAIL;
		if (record != null) {
			detailFragment.setEntityRecord(record);
		}

		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.show(detailFragment);
		fragmentTransaction.hide(listFragment);
		fragmentTransaction.commit();

        notifyFABsChanged();
	}


	/**
	 * Obtém a configuração de exibição lista que será utilizada na perspectiva.
	 *
	 * @return a configuração obtida.
	 */
	protected ITabbedListDisplayConfig getListDisplayConfig() {
		return entityConfig.getList();
	}

    /**
	 * Configura o listener de pesquisa no fragmento de lista de registros.
     */
    protected void configureListFragmentSearchListener() {
		listFragment.setOnSearchListener(new ActionBarNotifierSearchListener());
	}

	/**
	 * Configura o listener de alteração dos registros fragmento de lista.
	 */
	protected void configureListFragmentRecordsChangeListener() {
		listFragment.setOnRecordsChangeListener(this);
	}

    /**
     * Configura o listener de click no registro no fragmento de lista de registros.
     */
    protected void configureListFragmentEntityRecordClickListener() {
        listFragment.setOnEntityRecordClickListener(this);
    }

	/**
	 * Configura o menu de ações dos registros do fragmento de lista.
	 */
	protected void configureListFragmentRecordActions() {
		listFragment.setActionProvider(this);
	}


    /**
     * Chamado para excluir um registro após a confirmação do usuário que foi iniciado por {@link #deleteRecord(IEntityDAO, IEntityRecord)}.
     *
     * @param dao acessor de dados da entidade do registro que será excluído.
     * @param entityRecord registro que será excluído.
     */
    protected void onDeleteRecord(IEntityDAO dao, IEntityRecord entityRecord) {
        IEntityUIEventManager eventManager = getEventManager();
        Context context = getActivity();
        if (eventManager != null) {
            boolean cancel = eventManager.fireBeforeDeleteRecordEvent(entityRecord);
            if (cancel) {
                Toast.makeText(context, R.string.deletion_denied, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        IEntityUIConfigManager configManager = getConfigManager();
        String entityDisplayName = getEntityDisplayName(configManager, entityDAO.getEntityMetadata().getName(), false);
        try {
            dao.delete(entityRecord, true);

            String message = createMessage(context, R.string.record_deleted_format, entityDisplayName);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            afterDeleteRecord(entityRecord);
            if (eventManager != null) {
                eventManager.fireAfterDeleteRecordEvent(entityRecord);
            }
        } catch (RelationshipViolationException e) {
            String message = createMessage(context, R.string.record_deletion_relationship_violation_format,
                    createEntitiesDisplayList(context, configManager, true, e.getSourceEntities()), entityDisplayName);
            new AlertDialog.Builder(context).
                    setTitle(R.string.operation_denied).
                    setMessage(message).
                    setPositiveButton(android.R.string.ok, null).
                    show();
        }
    }

    /**
     * Chamado após a exclusão de um registro iniciada por {@link #deleteRecord(IEntityDAO, IEntityRecord)}.
     *
     * @param record registro excluído.
     */
    protected void afterDeleteRecord(IEntityRecord record) {
        listFragment.onRecordDeleted(record);

        if (mode == MODE_DETAIL) {
            showList();
        }
    }

    /**
     * Verifica se o comportamento de iniciar a edição ao clicar em um registro editável está habilitada.
     *
     * @param record registro que foi clicado.
     * @return <code>true</code> se a edição pode ser iniciada e <code>false</code> caso contrário.
     */
    protected boolean isEditOnClickEnabled(IEntityRecord record) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            return !arguments.containsKey(DISABLE_EDIT_ON_CLICK_ARGUMENT);
        }

        return true;
    }


	/*
	 * Métodos auxiliares
	 */

    /**
     * Exclui o registro, mas apenas após a confirmaçao do usuario. Depois da exclusão, o metodo {@link #afterDeleteRecord(IEntityRecord)}.
     *
     * @param dao acessor de dados da entidade cujo registro será excluído.
     * @param record registro que será excluído.
     */
    protected final void deleteRecord(IEntityDAO dao, IEntityRecord record) {
        //Pede a confirmação do usuário antes de excluir.
        Bundle arguments = new Bundle();
        String message = createMessage(getActivity(), R.string.record_deletion_confirmation, getEntityDisplayName(getConfigManager(), dao.getEntityMetadata().getName(), false));
        arguments.putString(ConfirmationDialogFragment.MESSAGE_STRING_ARGUMENT, message);
        arguments.putInt(ConfirmationDialogFragment.POSITIVE_BUTTON_ARGUMENT, R.string.record_deletion_confirmation_positive);
        Parcelable recordSavedState = dao.toSavedState(record);
        arguments.putParcelable(RecordDeletionConfirmationDialogListener.RECORD_ARGUMENT, recordSavedState);
        arguments.putString(RecordDeletionConfirmationDialogListener.ENTITY_NAME_ARGUMENT, dao.getEntityMetadata().getName());

        ConfirmationDialogFragment dialogFragment = new ConfirmationDialogFragment();
        dialogFragment.setArguments(arguments);
        dialogFragment.setListener(new RecordDeletionConfirmationDialogListener(dao));
        dialogFragment.show(getChildFragmentManager(), ConfirmationDialogFragment.CONFIRMATION_DIALOG_FRAGMENT_TAG);
    }

	private boolean useEntityListPager() {
		return useEntityListPager(getListDisplayConfig());
	}

	private boolean useEntityListPager(ITabbedListDisplayConfig listDisplayConfig) {
		IListTab[] listTabs = listDisplayConfig.getTabs();
		return listTabs != null && listTabs.length > 0;
	}

    private boolean isAddFABEnabled() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            return arguments.containsKey(ENABLE_FAB_ADD_ARGUMENT);
        }

        return false;
    }

    private boolean isAddActionEnabled() {
        return mode == MODE_LIST && canAddRecord();

    }

    private boolean canAddRecord() {
        IEditingPermissions localPermissions = null;
        IEditingPermissions dataSourcePermissions = null;
        IEditingConfig editingConfig = entityConfig.getEditing();
        if (editingConfig != null) {
            localPermissions = editingConfig.getDataSourcePermissions();
            dataSourcePermissions = editingConfig.getLocalPermissions();
        }

        //Só pode adicionar se houver pelo menos uma das permissões de criação liberadas.
        return (localPermissions != null && localPermissions.canCreate()) ||
               (dataSourcePermissions != null && dataSourcePermissions.canCreate());
    }
	

	/*
	 * Classes auxiliares.
	 */
	

	/**
	 * Listener do dialogo de confirmação, que exclui o registro de fato se o usuário assim decidiu.
	 */
	private final class RecordDeletionConfirmationDialogListener implements IConfirmationDialogListener {

		private static final String RECORD_ARGUMENT = "RECORD_ARGUMENT";
		private static final String ENTITY_NAME_ARGUMENT = "ENTITY_NAME_ARGUMENT";

        private final IEntityDAO dao;

        public RecordDeletionConfirmationDialogListener() {
            this(null);
        }

        public RecordDeletionConfirmationDialogListener(IEntityDAO dao) {
            this.dao = dao;
        }

		@Override
		public void onConfirm(ConfirmationDialogFragment fragment) {
            Bundle arguments = fragment.getArguments();
            if (BuildConfig.DEBUG && arguments == null) {
                throw new AssertionError();
            }

            IEntityDAO dao = this.dao;
            if (dao == null) {
                dao = getDataManager().getEntityDAO(arguments.getString(ENTITY_NAME_ARGUMENT));
            }
            Parcelable recordSavedState = arguments.getParcelable(RECORD_ARGUMENT);
			IEntityRecord entityRecord = dao.fromSavedState(recordSavedState);

            onDeleteRecord(dao, entityRecord);
		}

		@Override
		public void onCancel(ConfirmationDialogFragment fragment) {
		}
	}
}
