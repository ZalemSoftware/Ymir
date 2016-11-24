package br.com.zalem.ymir.client.android.entity.ui.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.inject.Inject;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingPermissions;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListConfig;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityUIEventManager;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityDetailFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityDetailFragment.OnContentChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityDetailFragment.OnRecordChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityListFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityListFragment.OnEntityRecordClickListener;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityListFragment.OnRecordsChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityListFragment.OnSearchListener;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityDetailFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityDetailPagerFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityListFragment;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.IEntityRecordListActionProvider;
import br.com.zalem.ymir.client.android.entity.ui.search.ISearchableManager;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import br.com.zalem.ymir.client.android.perspective.Perspective;

/**
 * Perspectiva que dispõe o detalhamento de um registro de entidade.<br>
 * Utiliza o fragmento {@link EntityDetailFragment} ou o {@link EntityDetailPagerFragment} para mostrar os detalhes,
 * dependendo da configuração definida para a entidade.<br>
 * <br>
 * O EntityDetailPerspective deve ter sua entidade definida através de uma categoria de seu {@link Intent}. Por este motivo,
 * deve ser definida exatamente uma categoria contendo o nome da entidade.<br>
 * O registro que será mostrado pela perspectiva deve ser definido através de um dos seguintes extras do Intent:
 * <ul>
 * 	<li>{@link #RECORD_EXTRA}: o estado salvo de um registro da entidade, que pode ser obtido através do método {@link IEntityDAO#toSavedState(IEntityRecord)}.
 * 		Com este extra, o registro não será atualizado automaticamente na reinicialização ({@link #onStart()} posteriores) da perspectiva;</li>
 * 	<li>{@link #RECORD_ID_EXTRA}: o id do registro da entidade. Com este extra, o registro será atualizado automaticamente na reinicialização da perspectiva.</li>
 * </ul>
 * A única action suportada por esta perspectiva é a {@link #ENTITY_DETAIL_ACTION}.<br>
 * 
 * @see IEntityDAO
 * @see EntityDetailFragment
 * @see EntityDetailPagerFragment
 * 
 * @author Thiago Gesser
 */
public class EntityDetailPerspective extends Perspective implements OnContentChangeListener, OnRecordChangeListener {
	
	/**
	 * Ação de detalhamento de registro de entidade.
	 */
	public static final String ENTITY_DETAIL_ACTION = EntityListDetailPerspective.class.getPackage().getName() + ".DETAIL";
    /**
     * Ações suportadas pela perspectiva de detalhes.
     */
    public static final String[] SUPPORTED_ACTIONS = new String[] {ENTITY_DETAIL_ACTION};

    /**
	 * Extra do tipo {@link Parcelable} que é um estado salvo do registro de entidade que será detalhado na perspectiva.
	 * O estado salvo pode ser obtido através do método {@link IEntityDAO#toSavedState(IEntityRecord)}.<br>
	 * <br>
	 * Este extra ou o {@link #RECORD_ID_EXTRA} deve ser definido para utilizar esta perspectiva, mas não ambos.
	 */
	public static final String RECORD_EXTRA = "RECORD_EXTRA";
    /**
	 * Extra do tipo {@link Serializable} que é um identificador de registro de entidade que será detalhado na perspectiva.<br>
	 * <br>
	 * Este extra ou o {@link #RECORD_EXTRA} deve ser definido para utilizar esta perspectiva, mas não ambos.
	 */
	public static final String RECORD_ID_EXTRA = "RECORD_ID_EXTRA";
    /**
     * Extra do tipo <code>boolean</code> que determina se a perspectiva deve desabilitar todas as ações de edição na perspectiva,
     * como adicionar, editar, remover e duplicar registros.<br>
     * Por padrão, todas as ações de edição são <b>habilitadas</b> de acordo com as permissões da entidade. Desta forma, se o valor deste
     * extra for <code>false</code>, não haverá alteração no comportamento.
     */
    public static final String DISABLE_EDITING_EXTRA = "DISABLE_EDITING_EXTRA";

    protected static final String DETAIL_FRAGMENT_TAG = "DETAIL_FRAGMENT_TAG";
	
	@Inject
	private IEntityDataManager dataManager;
	
	@Inject
	private IEntityUIConfigManager configManager;
	
	@Inject(optional = true)
	private IEntityUIEventManager eventManager;
	
	@Inject(optional = true)
	private ISearchableManager searchableManager;
	
	protected AbstractEntityDetailFragment detailFragment;
    protected IEntityConfig entityConfig;
    protected IEntityDAO entityDAO;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.entity_detail_perspective, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//Utiliza o "onActivityCreated" pq só aqui os fragmentos filhos salvos terão sido restaurados.
		super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

		Intent intent = getIntent();
		//Verifica se é uma ação suportada.
		checkAction(intent.getAction());
		
		//Obtém o nome da entidade da categoria.
		Set<String> categories = intent.getCategories();
		if (categories == null) {
			throw new IllegalArgumentException("No category (referring to the entity name) was defined.");
		}
		if (categories.size() > 1) {
			throw new IllegalArgumentException("Only one category (referring to the entity name) is allowed.");
		}
		String entityName = categories.iterator().next();
		entityConfig = getEntityConfig(entityName);
		
		//Cria os fragmentos apenas na primeira vez pq depois o Android cria eles automaticamente (comportamento não documentado).
		FragmentManager fragmentManager = getChildFragmentManager();
		if (savedInstanceState == null) {
			//Adiciona os fragmentos previamente para evitar transições estranhas entre os fragmentos.
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			createFragments(fragmentTransaction, getFragmentsContainerViewId());
			
			fragmentTransaction.commit();
		} else {
			//Como o Android possui um comportamento não documentado de restaurar automaticamente os fragmentos, apenas
			//obtém as instâncias já criadas por ele.
			restoreFragments(fragmentManager);
		}
		
		entityDAO = dataManager.getEntityDAO(entityName);
		//Utiliza um MaskManager comum na criação dos formatadores, evitando assim a criação desnecessária de máscaras iguais.
		MaskManager maskManager = new MaskManager(getActivity());
		initializeFragments(maskManager);
        configureDetailFragmentContentChangeListener();
        configureDetailFragmentRecordChangeListener();
		
		//Só é necessário fazer esta configuração na primeira vez.
		if (savedInstanceState == null) {
			Bundle extras = getIntent().getExtras();
			configurePerspective(extras, entityDAO);
		}
    }

	@Override
	public String getTitle() {
		String searchTitle = getSearchTitle();
		if (searchTitle != null) {
			return searchTitle;
		}
		
		return super.getTitle();
	}
	
	@Override
	public boolean isUpEnabled() {
		EntityListFragment entityList = detailFragment.getEntityList();
		return entityList != null && isSearching(entityList);
	}

    @Override
	public boolean onBackPressed() {
		EntityListFragment entityList = detailFragment.getEntityList();
		if (entityList != null && isSearching(entityList)) {
			entityList.undoSearch();
			return true;
		}

		return false;
	}

    @Override
	public void onUpPressed() {
		EntityListFragment entityList = detailFragment.getEntityList();
		if (entityList != null) {
			entityList.undoSearch();
		}
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!isEditingEnabled()) {
            return;
        }

        inflater.inflate(R.menu.entity_detail_perspective_actions, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //Como por enquanto todas as ações são de edição, só coloca elas se for necessário.
        if (!isEditingEnabled()) {
            return;
        }

        IEntityRecord record = detailFragment.getEntityRecord();
        IDetailConfig detailConfig = entityConfig.getDetail();
        boolean enableDuplicateAction = record != null && detailConfig != null && detailConfig.isEnableDuplicate();
        MenuItem duplicateMenuItem = menu.findItem(R.id.action_duplicate);
        duplicateMenuItem.setVisible(enableDuplicateAction).setEnabled(enableDuplicateAction);

        boolean enableEditAction = record != null && isEditable(record);
        MenuItem editMenuItem = menu.findItem(R.id.action_edit);
        editMenuItem.setVisible(enableEditAction).setEnabled(enableEditAction);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        checkEditingEnabled();

        if (item.getItemId() == R.id.action_duplicate) {
            Intent intent = onCreateRecordDuplicationIntent(detailFragment.getEntityRecord());
            startPerspective(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_edit) {
            editRecord(entityDAO, detailFragment.getEntityRecord());
            return true;
        }

        return false;
    }


    /*
     * Método do OnRecordChangeListener do fragmento de detalhe que avisa o IEventManager.
     */

    @Override
    public void onRecordChanged(IEntityRecord record) {
        if (record != null && eventManager != null) {
            eventManager.fireDetailRecordEvent(record);
        }
    }


    /*
     * Métodos do OnContentChangeListener do fragmento de detalhe que ajustam o fragmento de lista interno.
     */

    @Override
    public void beforeContentChange() {
        //Remove qualquer listener que tenha colocado no EntityListFragment antigo.
        EntityListFragment entityList = detailFragment.getEntityList();
        if (entityList == null) {
            return;
        }

        entityList.setOnEntityRecordClickListener(null);
        entityList.setOnSearchListener(null);
        entityList.setOnRecordsChangeListener(null);
    }

    @Override
    public void afterContentChange() {
        notifyAppBarChanged();

        //Coloca listeners para agir de acordo com as alterações no fragmento de lista utilizado pelo de detalhe.
        EntityListFragment entityList = detailFragment.getEntityList();
        if (entityList == null) {
            return;
        }

        entityList.setOnSearchListener(new ActionBarNotifierSearchListener());
        entityList.setOnEntityRecordClickListener(new DetailListRecordClickListener());
        if (eventManager != null) {
            entityList.setOnRecordsChangeListener(new DetailRecordsListChangeListener(eventManager));
        }
        //Só seta se necessário pois esta ação desencadeia a recriação das Views da lista.
        if (entityList.getActionProvider() == null) {
            entityList.setActionProvider(new DetailRecordsListActionProvider());
        }
    }

	/**
	 * Obtém as ações suportadas por esta perspectiva.<br>
	 * 
	 * @return as ações suportadas.
	 */
	protected String[] getSupportedActions() {
		return SUPPORTED_ACTIONS;
	}
	
	/**
	 * Cria os fragmentos utilizados por esta perspectiva.
	 * 
	 * @param fragmentTransaction transação utilizado na criação dos fragmentos.
	 * @param containerViewId id do container dos fragmentos.
	 */
	protected void createFragments(FragmentTransaction fragmentTransaction, int containerViewId) {
		if (useEntityDetailPager(entityConfig)) {
			detailFragment = new EntityDetailPagerFragment();
		} else {
			detailFragment = new EntityDetailFragment();
		}

		fragmentTransaction.add(containerViewId, detailFragment, DETAIL_FRAGMENT_TAG);
	}
	
	/**
	 * Restaura os fragmentos criados anteriormente por esta perspectiva.
	 * 
	 * @param fragmentManager o gerenciador de fragmentos utilizado na criação dos fragmentos.
	 */
	protected void restoreFragments(FragmentManager fragmentManager) {
		detailFragment =  (AbstractEntityDetailFragment) fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
	}
	
	/**
	 * Inicializa os fragmentos criados por esta perspectiva.
	 * 
	 * @param maskManager gerenciador de máscaras.
	 */
	protected void initializeFragments(MaskManager maskManager) {
		IDetailConfig detailConfig = entityConfig.getDetail();
		if (useEntityDetailPager(entityConfig)) {
			IDetailTab[] detailTabs = detailConfig.getTabs(); 
			EntityDetailPagerFragment frag = (EntityDetailPagerFragment) detailFragment;
			frag.initialize(dataManager, configManager, searchableManager, entityDAO, detailTabs, maskManager);
		} else {
			EntityDetailFragment frag = (EntityDetailFragment) detailFragment;
			frag.initialize(entityDAO, maskManager, getActivity(), detailConfig.getHeader(), detailConfig.getFields(), dataManager, configManager, searchableManager);
		}
	}
	
	/**
	 * Executa as configurações da perspectiva em si, baseando-se nos extras passados para ela no {@link Intent}.<br>
	 * Esta configuração é chamada apenas na criação da perspectiva, nunca em sua restauração.
	 * 
	 * @param extras extras do Intent da perspectiva.
	 * @param entityDAO acessor de dados da entidade.
	 */
	protected void configurePerspective(Bundle extras, IEntityDAO entityDAO) {
		if (extras == null) {
			throw new IllegalArgumentException("Intent extras are missing.");
		}
		
		//Define o registro que será mostrado pelo fragmento de detalhe através dos extras no Intent.
		Serializable recordId = extras.getSerializable(RECORD_ID_EXTRA);
		Parcelable recordSavedState = extras.getParcelable(RECORD_EXTRA);
		
		//Verifica se nenhum ou amboas os extras foram definidos.
		checkExtras(recordId, recordSavedState);
		
		if (recordId != null) {
			IEntityRecord record = entityDAO.get(recordId);
			if (record == null) {
				//Se não possui mais o registro, apenas loga um aviso. O fragmento de detalhe já vai informar a inexistência dos dados.
				Log.w(getClass().getSimpleName(), String.format("The entity record referenced by the RECORD_ID_EXTRA no longer exists. Id = %s, entity = %s.", recordId, entityDAO.getEntityMetadata().getName()));
			} else {
				detailFragment.setEntityRecord(record);
			}
		} else {
			//Se vai mostrar um registro vindo do Extra, define que ele não poderá sofrer refresh automático para evitar a perda de qualquer dado.
			Bundle arguments = new Bundle();
			arguments.putBoolean(AbstractEntityDetailFragment.AUTO_REFRESH_ARGUMENT, false);
			detailFragment.setArguments(arguments);
			
			IEntityRecord record = entityDAO.fromSavedState(recordSavedState);
			detailFragment.setEntityRecord(record);
		}
	}
	
	/**
	 * Configura o listener de alteração de conteúdo do fragmento de detalhes.
	 */
	protected void configureDetailFragmentContentChangeListener() {
		detailFragment.setOnContentChangeListener(this);
	}
	
	/**
	 * Configura o listener de alteração de registro do fragmento de detalhes.
	 */
	protected void configureDetailFragmentRecordChangeListener() {
        detailFragment.setOnRecordChangeListener(this);
	}
	
	/**
	 * Verifica os extras passados para esta perspectiva.
	 * @throws IllegalArgumentException se os extras estavam errados.
	 */
	protected void checkExtras(Serializable recordId, Parcelable recordSavedState) {
		if (recordId == null && recordSavedState == null) {
			throw new IllegalArgumentException(String.format("One of the extras %s or %s is required.", RECORD_ID_EXTRA, RECORD_EXTRA));
		}
		if (recordId != null && recordSavedState != null) {
			throw new IllegalArgumentException(String.format("Both extras %s and %s were defined. Only one of them is allowed.", RECORD_ID_EXTRA, RECORD_EXTRA));
		}
	}

	/**
	 * Obtém o id do container de fragmentos utilizado por esta perspectiva.
	 * 
	 * @return o id do container obtido.
	 */
	protected int getFragmentsContainerViewId() {
		return R.id.entity_detail_perspective_fragments_container;
	}
	
	/**
	 * Obtém o {@link IEntityConfig} da entidade, verificando se as configurações necessárias estão presentes.
	 * 
	 * @param entityName o nome da entidade.
	 * @return o IEntityConfig obtido.
	 * @throws IllegalArgumentException se faltou alguma configuração necessária.
	 */
	protected IEntityConfig getEntityConfig(String entityName) {
		IEntityConfig entityConfig = configManager.getEntityConfig(entityName);
		if (entityConfig == null || entityConfig.getDetail() == null) {
			throw new IllegalArgumentException(String.format("Missing required configuration \"%s\" of entity %s.", "detail", entityName));
		}
		return entityConfig;
	}

    /**
	 * Obtém o título que retrata a pesquisa atual, se houver.
	 * 
	 * @return o título da pesquisa ou <code>null</code> se não houver uma pesquisa no momento.
	 */
	protected String getSearchTitle() {
		EntityListFragment entityList = detailFragment.getEntityList();
		if (entityList == null || !isSearching(entityList)) {
			return null;
		}
		return entityList.getSimpleSearchQuery();
	}

    /**
     * Verifica se as ações de edição devem estar habilitadas para esta perspectiva.
     *
     * @return <code>true</code> se a edição está habilitada e <code>false</code> caso contrário.
     */
    protected boolean isEditingEnabled() {
        return !getIntent().getBooleanExtra(DISABLE_EDITING_EXTRA, false);
    }

    /**
     * Verifica se o registro é editavel, com a finalidade de habilitar ações de edição.<br>
     * Por padrão, a editabilidade do registro é determinada pelas permissões definidas para sua entidade na configuração.
     *
     * @param record registro que sera verificado.
	 * @param config configuraçao da entidade.
     * @return <code>true</code> se o registro for editável e <code>false</code> caso contrário.
     */
    protected boolean isEditable(IEntityRecord record, IEntityConfig config) {
		if (record.isNew()) {
			return false;
		}
		IEditingPermissions permissions = getRecordPermissions(record, config);
		return permissions != null && permissions.canUpdate();
    }


    /**
     * Chamado a partir da seleção de um registro em uma lista do fragmento de detalhes para a criação do {@link Intent} que será utilizado para
     * lançar a perspectiva de detalhamento de registro.
     *
     * @param dao acessor de dados da entidade do registro.
     * @param record o registro que será detalhado.
     * @return a Intent criada.
     */
    protected Intent onCreateRecordDetailingIntent(IEntityDAO dao, IEntityRecord record) {
        //Se veio de um intent com o registro completo ou é novo ou possui alterações, manda o registro completo do detalhe tbm. Se não, só o id.
        Intent ownIntent = getIntent();

        //Cria o intent para exibir os detalhes deste registro.
        Intent perspectiveIntent = new Intent();
        perspectiveIntent.setAction(ENTITY_DETAIL_ACTION);
        perspectiveIntent.addCategory(dao.getEntityMetadata().getName());

        boolean useRecordInstance = ownIntent.hasExtra(RECORD_EXTRA) || record.isNew() || record.isDirty();
        if (useRecordInstance) {
            Parcelable recordSavedState = dao.toSavedState(record);
            perspectiveIntent.putExtra(RECORD_EXTRA, recordSavedState);
        } else {
            perspectiveIntent.putExtra(RECORD_ID_EXTRA, record.getId());
        }
        if (!isEditingEnabled()) {
            //Repassa a desabilitação da edição.
            perspectiveIntent.putExtra(DISABLE_EDITING_EXTRA, true);
        }

        return perspectiveIntent;
    }

    /**
     * Chamado a partir da ação de duplicaçar para a criação do {@link Intent} que será utilizado para lançar a perspectiva de edição do
     * registro duplicado.
     *
     * @param dao acessor de dados da entidade do registro.
     * @param record o registro que será duplicado.
     * @return a Intent criada.
     */
    protected Intent onCreateRecordDuplicationIntent(IEntityDAO dao, IEntityRecord record) {
        //Copia o registro atual.
        record = dao.copy(record, true);
        Parcelable recordSavedState = dao.toSavedState(record);

        //Cria o Intent para a edição do registro copiado.
        Intent intent = new Intent();
        intent.addCategory(dao.getEntityMetadata().getName());
        intent.setAction(EntityEditingPerspective.ENTITY_EDITING_ACTION);
        intent.putExtra(EntityEditingPerspective.EDITING_RECORD_EXTRA, recordSavedState);
        return intent;
    }

    /**
     * Chamado a partir da ação de editar para a criação do {@link Intent} que será utilizado para lançar a perspectiva de edição do registro.
     *
     * @param dao acessor de dados da entidade do registro.
     * @param record o registro que será editado.
     * @return a Intent criada.
     */
    protected Intent onCreateRecordEditingIntent(IEntityDAO dao, IEntityRecord record) {
        Intent intent = new Intent();
        intent.addCategory(dao.getEntityMetadata().getName());
        intent.setAction(EntityEditingPerspective.ENTITY_EDITING_ACTION);
        if (record != null) {
            intent.putExtra(EntityEditingPerspective.EDITING_RECORD_EXTRA, dao.toSavedState(record));
        }
        return intent;
    }


    /**
     * Chamado quando uma lista do fragmento de detalhes está criando as ações para seus registros.
     *
     * @param menu menu de ações do registro.
     * @param menuInflater inflater que pode ser utilizado para popular as ações no menu.
     * @see IEntityRecordListActionProvider#onCreateRecordActionMenu(YmirMenu, YmirMenuInflater)
     */
    protected void onCreateDetailListRecordActionMenu(YmirMenu menu, YmirMenuInflater menuInflater) {
        //Como por enquanto todas as ações são de edição, só coloca elas se for necessário.
        if (!isEditingEnabled()) {
            return;
        }

        menuInflater.inflate(R.xml.entity_detail_perspective_record_actions, menu);
    }

    /**
     * Chamado quando uma lista do fragmento de detalhes está filtrando as ações disponíveis para um registro.
     *
     * @param record registro cujo as ações estão sendo filtradas.
     * @param item item de ação que está sendo verificado.
     * @return <code>true</code> se a ação está disponível e <code>false</code> caso contrário.
     * @see IEntityRecordListActionProvider#isRecordActionItemAvailable(IEntityRecord, YmirMenuItem)
     */
    protected boolean isDetailListRecordActionItemAvailable(IEntityRecord record, YmirMenuItem item) {
        if (isEditingEnabled()) {
            if (item.getId() == R.id.record_action_duplicate) {
                IEntityConfig config = configManager.getEntityConfig(record.getEntityMetadata().getName());
                if (config != null) {
					IListConfig listConfig = config.getList();
                    if (listConfig != null) {
                        return listConfig.isEnableDuplicate();
                    }
                }
            } else if (item.getId() == R.id.record_action_edit) {
                IEntityConfig config = configManager.getEntityConfig(record.getEntityMetadata().getName());
                return isEditable(record, config);
            }
        }
        return false;
    }

    /**
     * Chamado quando uma ação de registro de uma lista do fragmento de detalhes foi selecionada.
     *
     * @param record registro cujo o item de ação foi selecionado.
     * @param item item de ação que foi selecionado.
     * @see IEntityRecordListActionProvider#onRecordActionItemSelected(IEntityRecord, YmirMenuItem)
     */
    protected void onDetailListRecordActionItemSelected(IEntityRecord record, YmirMenuItem item) {
        checkEditingEnabled();

        if (item.getId() == R.id.record_action_duplicate) {
            IEntityDAO dao = dataManager.getEntityDAO(record.getEntityMetadata().getName());
            Intent intent = onCreateRecordDuplicationIntent(dao, record);
            startPerspective(intent);
        } else if (item.getId() == R.id.record_action_edit) {
            IEntityDAO dao = dataManager.getEntityDAO(record.getEntityMetadata().getName());
            editRecord(dao, record);
        }
    }

	/**
	 * Chamado quando um registro de uma lista do fragmento de detalhes foi clicado.
	 *
	 * @param record o registro clicado.
	 */
	protected void onDetailListRecordClick(IEntityRecord record) {
		String recordEntityName = record.getEntityMetadata().getName();
		IEntityDAO recordEntityDAO = dataManager.getEntityDAO(recordEntityName);

		Intent perspectiveIntent = onCreateRecordDetailingIntent(recordEntityDAO, record);
		startPerspective(perspectiveIntent);
	}

	
	public final void setConfigManager(IEntityUIConfigManager configManager) {
		this.configManager = configManager;
	}
	
	public final void setDataManager(IEntityDataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	public void setEventManager(IEntityUIEventManager eventManager) {
		this.eventManager = eventManager;
	}
	
	public final void setSearchableManager(ISearchableManager searchableManager) {
		this.searchableManager = searchableManager;
	}
	
	public final IEntityUIConfigManager getConfigManager() {
		return configManager;
	}
	
	public final IEntityDataManager getDataManager() {
		return dataManager;
	}
	
	public IEntityUIEventManager getEventManager() {
		return eventManager;
	}
	
	public final ISearchableManager getSearchableManager() {
		return searchableManager;
	}
	
	
	/*
	 * Métodos auxiliares
	 */

    /**
     * Chama o método {@link #onCreateRecordDuplicationIntent(IEntityDAO, IEntityRecord)} passando o DAO da perspectiva.
     *
     * @param record o registro que será duplicado.
     * @return a Intent criada.
     */
    protected final Intent onCreateRecordDuplicationIntent(IEntityRecord record) {
        return onCreateRecordDuplicationIntent(entityDAO, record);
    }

    /**
     * Inicia a perspectiva de edição de registro. Por padrão, um novo registro será criado com esta ação, mas é possível passar um registro
     * já existente ao invés disso.
     *
     * @param dao acessor de dados da entidade cujo registro será editado.
     * @param record registro que será passado para a perspectiva de edição ou <code>null</code> se um novo deve ser criado.
     */
    protected final void editRecord(IEntityDAO dao, IEntityRecord record) {
        if (!dao.isReady()) {
            Toast.makeText(getActivity(), R.string.unavailable_action, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = onCreateRecordEditingIntent(dao, record);
        startPerspective(intent);
    }

    /**
     * Obtém as permissões de edição da entidade do registro.
     *
     * @param record o registro cujo as permissoes serão obtidas.
     * @return as permissões obtidas ou <code>null</code> se elas não foram declaradas.
     */
    protected final IEditingPermissions getRecordPermissions(IEntityRecord record, IEntityConfig config) {
        IEditingConfig editingConfig = config.getEditing();
        if (editingConfig == null) {
            return null;
        }

        if (record.isLocal()) {
            return editingConfig.getLocalPermissions();
        }

        return editingConfig.getDataSourcePermissions();
    }

	/**
	 * Chama o método {@link #isEditable(IEntityRecord, IEntityConfig)} passando a configuração da entidade da perspectiva.
	 *
	 * @param record registro que sera verificado.
	 * @return <code>true</code> se o registro for editável e <code>false</code> caso contrário.
	 */
	protected final boolean isEditable(IEntityRecord record) {
		return isEditable(record, entityConfig);
	}

    protected void checkEditingEnabled() {
        if (!isEditingEnabled()) {
            throw new IllegalStateException(String.format("The editing is not enabled for this perspective. Entity = %s, class = %s.", entityDAO.getEntityMetadata().getName(), getClass().getName()));
        }
    }

	private void checkAction(String action) {
		String[] supportedActions = getSupportedActions();
		if (supportedActions == null) {
			return;
		}
		
		for (String supportedAction : supportedActions) {
			if (action.equals(supportedAction)) {
				return;
			}
		}
		
		throw new IllegalArgumentException("Unsupported action: " + action);
	}

	private static boolean useEntityDetailPager(IEntityConfig entityConfig) {
		IDetailConfig detailConfig = entityConfig.getDetail();
		IDetailTab[] detailTabs = detailConfig.getTabs();
		return detailTabs != null && detailTabs.length > 0;
	}

	protected static boolean isSearching(AbstractEntityListFragment entityListFragment) {
		return entityListFragment.getSimpleSearchQuery() != null;
	}


	
	/*
	 * Classes auxiliares.
	 */

	/**
	 * Listener de pesquisa para fragmento de lista que notifica uma alteração na ActionBar.
	 */
	protected final class ActionBarNotifierSearchListener implements OnSearchListener {
		@Override
		public void onDoSimpleSearch(String query) {
			notifyAppBarChanged();
		}

		@Override
		public void onUndoSearch() {
			notifyAppBarChanged();
		}
	}

	/**
     * Provedor de ações para os registros do fragmento de lista utilizado pelo fragmento de detalhe.
     */
    private final class DetailRecordsListActionProvider implements IEntityRecordListActionProvider {

        @Override
        public void onCreateRecordActionMenu(YmirMenu menu, YmirMenuInflater menuInflater) {
            onCreateDetailListRecordActionMenu(menu, menuInflater);
        }

        @Override
        public void onRecordActionItemSelected(IEntityRecord record, YmirMenuItem item) {
            onDetailListRecordActionItemSelected(record, item);
        }

        @Override
        public boolean isRecordActionItemAvailable(IEntityRecord record, YmirMenuItem item) {
            return isDetailListRecordActionItemAvailable(record, item);
        }

	}

	/**
	 * Listener de click em registros do fragmento de lista que inicia a perspectiva de detalhamento.
	 */
	private final class DetailListRecordClickListener implements OnEntityRecordClickListener {

		@Override
		public void onEntityRecordClick(IEntityRecord record) {
			onDetailListRecordClick(record);
		}
	}

	/**
	 * Listener de alteração dos registros do fragmento de lista que avisa ao {@link IEntityUIEventManager} sobre as alterações.
	 */
	private static final class DetailRecordsListChangeListener implements OnRecordsChangeListener {

		private final IEntityUIEventManager eventManager;

		public DetailRecordsListChangeListener(IEntityUIEventManager eventManager) {
			this.eventManager = eventManager;
		}

		@Override
		public List<IEntityRecord> beforeRecordsChange(EntityListFragment listFragment, List<IEntityRecord> records) {
            String entityName = listFragment.getEntityDAO().getEntityMetadata().getName();
            return eventManager.fireBeforeListRecordsEvent(entityName, records);
		}

		@Override
		public void afterRecordsChange(EntityListFragment listFragment, List<IEntityRecord> records) {
            String entityName = listFragment.getEntityDAO().getEntityMetadata().getName();
			eventManager.fireAfterListRecordsEvent(entityName, records);
		}
	}
}