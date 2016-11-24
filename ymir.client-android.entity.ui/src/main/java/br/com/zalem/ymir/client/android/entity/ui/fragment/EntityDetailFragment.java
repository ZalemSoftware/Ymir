package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.app.SearchableInfo;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.util.MemoryEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.DetailLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListFilter;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListOrder;
import br.com.zalem.ymir.client.android.entity.ui.layout.LayoutConfigAdapter;
import br.com.zalem.ymir.client.android.entity.ui.search.ISearchableManager;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * O EntityDetailFragment mostra os detalhes de um registro de uma entidade a partir de um cabeçalho contendo as
 * principais informações e um lista de campos e valores adicionais.<br>
 * O fragmento requer, entre outros, o acessor de dados da entidade ({@link IEntityDAO}) e a configuração do cabeçalho ({@link ILayoutConfig})
 * e campos de detalhes ({@link IDetailFieldMapping}). A definição destes objetos deve ser feita através do método
 * {@link #initialize(IEntityDAO, MaskManager, Context, ILayoutConfig, IDetailFieldMapping[], IEntityDataManager, IEntityUIConfigManager, ISearchableManager)}
 * ou {@link #setHeaderConfig(ILayoutConfig)} e {@link #setFieldsListConfig(IDetailFieldMapping[])}.<br>
 * A criação do cabeçalho é feita utilizando um {@link LayoutConfigAdapter}.<br>
 * <br>
 * Para mostrar os detalhes de um registro, deve-se chamar o método {@link #setEntityRecord(IEntityRecord)}.
 * 
 * @see IEntityDAO
 * @see IDetailConfig
 * @see LayoutConfigAdapter
 *
 * @author Thiago Gesser
 */
public final class EntityDetailFragment extends AbstractEntityDetailFragment {

    private MaskManager maskManager;
	private ViewGroup contentView;
	private TextView messageView;
	private AbstractEntityDetailComponent headerComponent;
	private AbstractEntityDetailComponent fieldsComponent;

    @Override
    protected View onCreateThemedView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup fragmentView = (ViewGroup) inflater.inflate(R.layout.entity_detail_fragment, container, false);
		
		//Utilizado para fazer o controle de quando mostrar o texto explicativo referente ao registro ter sido excluído. 
		contentView = (ViewGroup) fragmentView.findViewById(R.id.entity_detail_content);
		messageView = (TextView) fragmentView.findViewById(R.id.entity_detail_empty_view);

		//Só cria as views dos componentes aqui se eles foram previamente definidos na inicialização.
		if (headerComponent != null) {
            createComponentView(fragmentView, R.id.entity_detail_header_container, headerComponent);
		}
		if (fieldsComponent != null) {
            createComponentView(fragmentView, R.id.entity_detail_fields_container, fieldsComponent);
		}
		
		return fragmentView;
	}
	
	@Override
	protected void refreshContent() {
		fireOnBeforeChangeContent();

		if (entityRecord == null) {
			//Se não tem registro setado, mostra apenas uma mensagem.
			showEmptyView();
		} else {
			//Garante que as views de conteúdo estão sendo mostradas.
			showContent();
		}

		if (headerComponent != null) {
			headerComponent.refreshData(entityRecord);
		}

		if (fieldsComponent != null) {
			fieldsComponent.refreshData(entityRecord);
			
			//Repassa as configurações de visibilidade para o fragmento filho, se houver, pq o Android não faz isto automaticamente.
			EntityListFragment entityList = getEntityList();
			if (entityList != null) {
				entityList.setMenuVisibility(isMenuVisible());
				entityList.setUserVisibleHint(getUserVisibleHint());
			}
		}
		
		fireOnAfterChangeContent();
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		//Limpa estas referências para evitar que fique apontando pra uma View antiga.
		headerComponent = null;
		fieldsComponent = null;
	}
	
	@Override
	public View getHeader() {
		if (headerComponent == null) {
			return null;
		}
		return headerComponent.getView();
	}

	@Override
	public ListView getFieldsList() {
		if (fieldsComponent == null) {
			return null;
		}
		return (ListView) fieldsComponent.getView();
	}

	@Override
	public EntityListFragment getEntityList() {
		if (fieldsComponent == null) {
			return null;
		}
		return (EntityListFragment) fieldsComponent.getFragment();
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (!isViewCreated()) {
			return;
		}

		//Repassa para o fragmento filho, se houver, pq o Android não faz isto automaticamente.
		EntityListFragment entityList = getEntityList();
		if (entityList != null) {
			entityList.setMenuVisibility(menuVisible);
		}
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (!isViewCreated()) {
			return;
		}

		//Repassa para o fragmento filho, se houver, pq o Android não faz isto automaticamente.
		EntityListFragment entityList = getEntityList();
		if (entityList != null) {
			entityList.setUserVisibleHint(isVisibleToUser);
		}
	}

    /**
     * Inicializa o fragmento com os objetos necessários para o seu funcionamento.
     *
     * @param entityDAO acessor dos dados da entidade.
     * @param maskManager gerenciador de máscaras utilizado na criação dos formatadores dos campos.
     */
    public void initialize(IEntityDAO entityDAO, MaskManager maskManager) {
        if (entityDAO == null || maskManager == null) {
            throw new NullPointerException("entityDAO == null || maskManager == null");
        }

        initialize(entityDAO);
        this.maskManager = maskManager;
    }

	
	/**
	 * Inicializa o fragmento com os objetos necessários para o seu funcionamento e aplica as configurações de cabeçalho e campos do detalhe.
	 * 
	 * @param entityDAO acessor dos dados da entidade.
	 * @param maskManager gerenciador de máscaras utilizado na criação dos formatadores dos campos.
	 * @param context contexto. É necessário neste momento porque o fragmento pode não estar ligado a Activity ainda, então
	 * o seu <code>getActivity</code> vai retornar <code>null</code>.
	 * @param headerConfig configuração do cabeçalho do detalhe. <b>Parâmetro opcional se o fieldsMappings não for nulo.</b>
	 * @param fieldsMappings configuração dos campos do detalhe. <b>Parâmetro opcional se o headerConfig não for nulo.</b>
	 * @param dataManager gerenciador de dados da aplicação.
	 * @param configManager gerenacidor de configurações da aplicação.
	 * @param searchableManager gerenciador de Serachables da aplicação, utilizado na configuração da pesquisa no fragmento
	 * de lista de registros de relacionamento. <b>Parâmetro opcional.</b>
	 * @throws IllegalStateException se a inicialização for feita novamente.
	 */
	public void initialize(IEntityDAO entityDAO, MaskManager maskManager, Context context, ILayoutConfig<DetailLayoutType> headerConfig,  IDetailFieldMapping[] fieldsMappings,
                           IEntityDataManager dataManager, IEntityUIConfigManager configManager, ISearchableManager searchableManager) {
        initialize(entityDAO, maskManager);

        if (dataManager == null || configManager == null) {
            throw new NullPointerException("dataManager == null || configManager == null");
        }
        if (headerConfig == null && (fieldsMappings == null || fieldsMappings.length == 0)) {
            throw new IllegalArgumentException("headerConfig == null && fieldsConfigs == null");
        }

		if (headerConfig != null) {
            setHeaderConfig(context, headerConfig);
		}
		
		if (fieldsMappings != null && fieldsMappings.length > 0) {
            setFieldsListConfig(context, dataManager, configManager, searchableManager, fieldsMappings);
        }
	}

    /**
     * Define a configuração de layout que será utilizada pelo fragmento para exibir cabeçalho do detalhe.
     *
     * @param headerConfig a configuração do cabeçalho.
     */
    public void setHeaderConfig(ILayoutConfig<DetailLayoutType> headerConfig) {
        if (isViewCreated()) {
            destroyComponentView(getView(), R.id.entity_detail_header_container, headerComponent);
        }

        setHeaderConfig(getActivity(), headerConfig);
    }

    /**
     * Define as configurações que serão utilizadas pelo fragmento para exibir os campos do detalhe.
     *
     * @param fieldsMappings configurações dos campos.
     * @param dataManager gerenciador de dados da aplicação.
     * @param configManager gerenacidor de configurações da aplicação.
     * @param searchableManager gerenciador de Serachables da aplicação, utilizado na configuração da pesquisa no fragmento
     * de lista de registros de relacionamento. <b>Parâmetro opcional.</b>
     */
    public void setFieldsListConfig(IDetailFieldMapping[] fieldsMappings, IEntityDataManager dataManager, IEntityUIConfigManager configManager, ISearchableManager searchableManager) {
        if (isViewCreated()) {
            destroyComponentView(getView(), R.id.entity_detail_fields_container, fieldsComponent);
        }

        setFieldsListConfig(getActivity(), dataManager, configManager, searchableManager, fieldsMappings);
    }

    /**
     * Define as configurações que serão utilizadas pelo fragmento para exibir os campos do detalhe, sendo que as configurações não podem
     * apontar para um único campo de relacionamento múltiplo. Isto porque neste caso informações adicionais são necessárias, então o método
     * {@link #setFieldsListConfig(IDetailFieldMapping[], IEntityDataManager, IEntityUIConfigManager, ISearchableManager)} deve ser utilizado.
     *
     * @param fieldsMappings configurações dos campos.
     */
    public void setFieldsListConfig(IDetailFieldMapping[] fieldsMappings) {
        IEntityMetadata entityMetadata = entityDAO.getEntityMetadata();
        if (isSingleMappingToRelationshipArray(fieldsMappings, entityMetadata)) {
            throw new IllegalArgumentException("The fields list configuration points to one relationship array. In this case, additional information is necessary. Please call setFieldsListConfig(IDetailFieldMapping[], IEntityDataManager, IEntityUIConfigManager, ISearchableManager).");
        }

        boolean viewCreated = isViewCreated();
        if (viewCreated) {
            destroyComponentView(getView(), R.id.entity_detail_fields_container, fieldsComponent);
        }

        createDetailFieldsList(getActivity(), fieldsMappings, entityMetadata);

        if (viewCreated) {
            createComponentView(getView(), R.id.entity_detail_fields_container, fieldsComponent);
        }
    }

	/*
	 * Métodos auxiliares
	 */
	
	private boolean isViewCreated() {
		return getView() != null;
	}
	
	private void showContent() {
		contentView.setVisibility(View.VISIBLE);
		messageView.setVisibility(View.GONE);
	}
	
	private void showEmptyView() {
		contentView.setVisibility(View.GONE);
		messageView.setVisibility(View.VISIBLE);
	}

    private void setHeaderConfig(Context context, ILayoutConfig<DetailLayoutType> headerConfig) {
        checkInitialized();

        EntityAttributeFormatter headerLayoutFormatter = EntityAttributeFormatter.fromConfig(context, entityDAO.getEntityMetadata(), maskManager, headerConfig.getFields());
        LayoutConfigAdapter headerLayoutAdapter = new LayoutConfigAdapter(context, headerConfig, headerLayoutFormatter, null);
        headerComponent = new EntityDetailHeader(headerLayoutAdapter);

        //Se o fragmento foi inicializado apenas depois de sua View ser criada, cria a View do componente agora.
        if (isViewCreated()) {
            createComponentView(getView(), R.id.entity_detail_header_container, headerComponent);
        }
    }

    private void setFieldsListConfig(Context context, IEntityDataManager dataManager, IEntityUIConfigManager configManager, ISearchableManager searchableManager, IDetailFieldMapping[] fieldsMappings) {
        IEntityMetadata entityMetadata = entityDAO.getEntityMetadata();
        //Se possui exatamente um campo e este campo for do tipo array de relacionamentos, coloca um EntityListFragment para mostrar estes dados.
        if (isSingleMappingToRelationshipArray(fieldsMappings, entityMetadata)) {
            IDetailFieldMapping fieldMapping = fieldsMappings[0];
            String[] relationshipPath = fieldMapping.getRelationship();
            IListDisplayConfig listConfig = fieldMapping.getListConfig();

            //Utiliza um fragmentManager adaptado para contornar o bug de estado inválido quando o getChildFragmentManager é chamado antes do fragmento ser atachado à Activity.
            FragmentManager fragmentManager = AndroidBugsUtils.applyWorkaroundForInvalidFragmentManagerStateBug(this);
            fieldsComponent = new EntityDetailRelationshipArrayFragment(relationshipPath, listConfig, isAutoRefresh(), getTheme(), fragmentManager,
                                                                        dataManager, searchableManager, entityMetadata, configManager, maskManager);
        } else {
            createDetailFieldsList(context, fieldsMappings, entityMetadata);
        }

        if (isViewCreated()) {
            createComponentView(getView(), R.id.entity_detail_fields_container, fieldsComponent);
        }
    }

    private void createDetailFieldsList(Context context, IDetailFieldMapping[] fieldsMappings, IEntityMetadata entityMetadata) {
        EntityAttributeFormatter attributesFormatter = EntityAttributeFormatter.fromConfig(context, entityMetadata, maskManager, fieldsMappings);

        //Se o número de máscaras é diferente do número de fieldsMappings, significa que há algum fieldMapping apontando para um relacionamento, o que não é suportado por enquanto.
        if (fieldsMappings.length != attributesFormatter.getMasksCount()) {
            throw new PendingFeatureException("Multiple detail field mappings with a mapping to a relationship");
        }

        //Calcula previamente quais labels serão utilizados pelos campos.
        fieldsComponent = new EntityDetailFieldsList(context, fieldsMappings, attributesFormatter);
    }


	/*
	 * Método auxliares estáticos
	 */

	private static boolean isSingleMappingToRelationshipArray(IDetailFieldMapping[] fieldsMappings, IEntityMetadata entityMetadata) {
		if (fieldsMappings.length > 1) {
			return false;
		}
		
		String[] relationshipPath = fieldsMappings[0].getRelationship();
        if (relationshipPath == null) {
            return false;
        }

        IEntityRelationship relationship = MetadataUtils.getRelationshipFromPath(entityMetadata, relationshipPath);
        return !MetadataUtils.isSingleRelationship(relationship);
	}

    private static void createComponentView(View fragmentView, int containerId, AbstractEntityDetailComponent component) {
        ViewGroup container = (ViewGroup) fragmentView.findViewById(containerId);
        //Utiliza um inflater baseado no contexto da View pq ela pode estar influenciada por outro tema.
        LayoutInflater layoutInflater = LayoutInflater.from(container.getContext());
        component.onCreateView(layoutInflater, container);
    }

    private static void destroyComponentView(View fragmentView, int containerId, AbstractEntityDetailComponent component) {
        if (component == null) {
            return;
        }

        ViewGroup container = (ViewGroup) fragmentView.findViewById(containerId);
        component.onDestroyView(container);
    }

	
	/*
	 * Estrutura de componentes do fragmento de detalhe.
	 */
	
	/**
	 * Componente visual do fragmento de detalhes.
	 */
	private static abstract class AbstractEntityDetailComponent {
		
		/**
		 * Invocado quando o fragmento está criando sua View, pode ser utilizado para criar as Views do componente
		 * ou apenas fazer inicializações.
		 * 
		 * @param inflater inflater de layouts.
		 * @param container container do conteúdo visual do componente. Qualquer View do componente deve ser colocada
		 * como filha do container.
		 */
		abstract void onCreateView(LayoutInflater inflater, ViewGroup container);

		/**
		 * Invocado quando o componente está sendo destruído, deve ser utilizado para remover as Views criadas pelo componente (se elas
         * chegaram a ser criadas).
		 *
		 * @param container container do conteúdo visual do componente.
		 */
		abstract void onDestroyView(ViewGroup container);

		/**
		 * Invocado quando o registro de dados do fragmento foi alterado. Deve ser utilizado para atualizar os 
		 * dados do componente que se referem ao registro de dados.
		 * 
		 * @param entityRecord registro de dados.
		 */
		abstract void refreshData(IEntityRecord entityRecord);
		
		/**
		 * Obtém a View utilizada por este componente, se houver.
		 *  
		 * @return a View obtida ou <code>null</code> se não houver.
		 */
		View getView() {
			return null;
		}
		
		/**
		 * Obtém o Fragmento utilizado por este componente, se houver.
		 *  
		 * @return o Fragmento obtido ou <code>null</code> se não houver.
		 */
		Fragment getFragment() {
			return null;
		}
	}
	
	/**
	 * Componente de cabeçalho do fragmento de detalhe.<br>
	 * Utiliza um {@link LayoutConfigAdapter} configurado com as definições de cabeçalho de detalhe da entidade para criar a View.
	 */
	private static final class EntityDetailHeader extends AbstractEntityDetailComponent {
		
		private final LayoutConfigAdapter layoutAdapter;
		private View headerView;
		
		public EntityDetailHeader(LayoutConfigAdapter layoutAdapter) {
			this.layoutAdapter = layoutAdapter;
		}
		
		@Override
		void onCreateView(LayoutInflater inflater, ViewGroup container) {
            layoutAdapter.setInflater(inflater);
			headerView = layoutAdapter.createAndHoldView(container);
			container.addView(headerView);
		}

        @Override
        void onDestroyView(ViewGroup container) {
            if (headerView != null) {
                container.removeView(headerView);
                headerView = null;
            }
        }

        @Override
		void refreshData(IEntityRecord entityRecord) {
			if (entityRecord == null) {
				return;
			}
			
			layoutAdapter.setHeldViewValues(entityRecord, headerView);
		}

		@Override
		View getView() {
			return headerView;
		}
	}
	
	/**
	 * Componente de lista de campos do fragmento de detalhe.<br>
	 * Utiliza as configurações dos campos de detalhe da entidade para mostrá-los em um {@link ListView}.
	 */
	private static final class EntityDetailFieldsList extends AbstractEntityDetailComponent {

		private final Context context;
		private final IDetailFieldMapping[] fieldsMappings;
		private final EntityAttributeFormatter attributesFormatter;
		private ListView fieldsList;
		
		public EntityDetailFieldsList(Context context, IDetailFieldMapping[] fieldsMappings, EntityAttributeFormatter attributesFormatter) {
			this.context = context;
			this.fieldsMappings = fieldsMappings;
			this.attributesFormatter = attributesFormatter;
		}
		
		@Override
		void onCreateView(LayoutInflater inflater, ViewGroup container) {
			fieldsList = (ListView) inflater.inflate(R.layout.entity_detail_field_list, container, false);
			container.addView(fieldsList);
		}

        @Override
        void onDestroyView(ViewGroup container) {
            if (fieldsList != null) {
                container.removeView(fieldsList);
                fieldsList = null;
            }
        }

        @Override
		void refreshData(IEntityRecord entityRecord) {
			if (entityRecord == null) {
				return;
			}
			
			//Popula os campos adicionais.
			List<Map<String, CharSequence>> data = new ArrayList<>();
			for (IDetailFieldMapping fieldMapping : fieldsMappings) {
				String label = fieldMapping.getLabel();
				//Formata o valor utilizandos a máscara definida para o campo, de acordo com o seu tipo.
				CharSequence value = attributesFormatter.formatAttributeValueToText(entityRecord, fieldMapping.getAttribute());

				Map<String, CharSequence> dataItem = new HashMap<>();
				dataItem.put(DetailFieldAdapter.LABEL_COLUMN, label);
				dataItem.put(DetailFieldAdapter.VALUE_COLUMN, value);
				data.add(dataItem);
			}
			
			SimpleAdapter adapter = new DetailFieldAdapter(context, data, fieldsMappings);
			fieldsList.setAdapter(adapter);
		}
		
		@Override
		View getView() {
			return fieldsList;
		}

        /**
         * Adaptador simples dos campos de detalhes.<br>
         * Utiliza toda a estrutura do {@link SimpleAdapter}, apenas alterando a visibilidade do label de acordo com a configuração do {@link IDetailFieldMapping#isLabelHidden()}.
         */
        //TODO verificar sobre a possibilidade de implementar um adapter próprio. SimpleAdapter não oferece a possibilidade de atualizar os dados... então sempre é necessário criar tudo novamente.
        private static final class DetailFieldAdapter extends SimpleAdapter {
            static final String LABEL_COLUMN = "label";
            static final String VALUE_COLUMN = "value";
            private static final String[] FROM = {LABEL_COLUMN, VALUE_COLUMN};
            private static final int[] TO = {R.id.entity_detail_field_list_item_label, R.id.entity_detail_field_list_item_value};

            private final IDetailFieldMapping[] fieldsMappings;

            public DetailFieldAdapter(Context context, List<Map<String, CharSequence>> data, IDetailFieldMapping[] fieldsMappings) {
                super(context, data, R.layout.entity_detail_field_list_item, FROM, TO);
                this.fieldsMappings = fieldsMappings;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                boolean labelHidden = fieldsMappings[position].isLabelHidden();
                View view = super.getView(position, convertView, parent);
                view.findViewById(R.id.entity_detail_field_list_item_label).setVisibility(labelHidden ? View.GONE : View.VISIBLE);
                return view;
            }
        }
    }
	
	/**
	 * Componente de lista de registros do fragmento de detalhe.<br>
	 * Utiliza um mapeamento único para um relacionamento do tipo array a fim de mostrar os registros provenientes
	 * deste relacionamento através de um {@link EntityListFragment}.
	 */
	private static final class EntityDetailRelationshipArrayFragment extends AbstractEntityDetailComponent {

		private final String[] relationshipPath;
		private final ILayoutConfig<ListLayoutType> listLayout;
		private final IListFilter listFilter;
		private final IListOrder listOrder;
		private final boolean autoRefresh;
        private final int theme;

        private final FragmentManager fragmentManager;
		private final IEntityDataManager dataManager;
        private final MaskManager maskManager;
		private final IEntityMetadata targetMetadata;

		private EntityListFragment currentFragment;
		private boolean restoring;
		private int containerId;

		private final ISearchableManager searchableManager;


        public EntityDetailRelationshipArrayFragment(String[] relationshipPath, IListDisplayConfig listConfig, boolean autoRefresh, int theme,
													 FragmentManager fragmentManager, IEntityDataManager dataManager, ISearchableManager searchableManager,
													 IEntityMetadata entityMetadata, IEntityUIConfigManager configManager, MaskManager maskManager) {
			this.relationshipPath = relationshipPath;
			this.autoRefresh = autoRefresh;
            this.theme = theme;
            this.fragmentManager = fragmentManager;
			this.dataManager = dataManager;
			this.searchableManager = searchableManager;
            this.maskManager = maskManager;
            this.targetMetadata = MetadataUtils.getRelationshipFromPath(entityMetadata, relationshipPath).getTarget();
			
			/*
			 * Obtém as configuraões que serão utilizadas no fragmento de lista. Apenas o layout é obrigatório.
			 */
			ILayoutConfig<ListLayoutType> listLayout = null;
			IListFilter listFilter = null;
			IListOrder listOrder = null;
			
			//Inicialmente, tenta obter as configurações de listagem do mapeamento.
			if (listConfig != null) {
				listLayout = listConfig.getLayout();
				listFilter = listConfig.getFilter();
				listOrder = listConfig.getOrder();
			}
			
			//Se alguma das configurações não foi definida, tenta obter da configuração de lista da entidade alvo (ignorando as abas).
			IEntityConfig targetConfig = configManager.getEntityConfig(targetMetadata.getName());
			if (targetConfig != null && targetConfig.getList() != null) {
				IListConfig targetListConfig = targetConfig.getList();
				if (listLayout == null) { 
					listLayout = targetListConfig.getLayout();
				}
				if (listFilter == null) {
					listFilter = targetListConfig.getFilter();
				}
				if (listOrder == null) {
					listOrder = targetListConfig.getOrder();
				}
			}
			
			if (listLayout == null) {
				throw new IllegalArgumentException(String.format("Missing required configuration \"layout\" for the list of the relationship \"%s\" from the entity \"%s\". This configuration can be defined in the detail field mapping (listConfig) or in the target entity config (list.layout)", Arrays.toString(relationshipPath), entityMetadata.getName()));
			}
			this.listLayout = listLayout;
			this.listFilter = listFilter;
			this.listOrder = listOrder;
		}
		
		@Override
		void onCreateView(LayoutInflater inflater, ViewGroup container) {
			currentFragment = (EntityListFragment) fragmentManager.findFragmentByTag(getFragmentTag());
			restoring = currentFragment != null;
			containerId = container.getId();
		}

        @Override
        void onDestroyView(ViewGroup container) {
            if (currentFragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.remove(currentFragment);
                transaction.commit();

                restoring = false;
                containerId = 0;
            }
        }

        @Override
		void refreshData(IEntityRecord entityRecord) {
			boolean isRestoring = consumeRestoring();
			if (entityRecord == null) {
				if (currentFragment != null) {
					FragmentTransaction transaction = fragmentManager.beginTransaction();
					transaction.remove(currentFragment);
					transaction.commit();
					currentFragment = null;
					fragmentManager.executePendingTransactions();
				}
				return;
			}
			
			//Se foi recuperado de um estado salvo, não precisa criar o fragmento novamente.
			if (!isRestoring) {
				EntityListFragment newFragment = new EntityListFragment();
				
				//Define o estado do fragmento corrente para manter qualquer busca que ele tiver feito.
				if (currentFragment != null) {
					SavedState instanceState = fragmentManager.saveFragmentInstanceState(currentFragment);
					newFragment.setInitialSavedState(instanceState);
				}
				
				Bundle listFragmentArgs = new Bundle();
				//Desabilita o refresh no fragmento de lista, já que sempre será criado um novo.
				listFragmentArgs.putBoolean(EntityListFragment.AUTO_REFRESH_ARGUMENT, false);
				listFragmentArgs.putBoolean(EntityListFragment.SWIPE_REFRESH_ARGUMENT, false);
                //Repassa o tema do fragmento de detalhes, se houver.
                if (theme > 0) {
                    listFragmentArgs.putInt(EntityListFragment.THEME_ARGUMENT, theme);
                }
				newFragment.setArguments(listFragmentArgs);
				
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				transaction.replace(containerId, newFragment, getFragmentTag());
				transaction.commit();
				currentFragment = newFragment;
			}
			
			initializeFragment(entityRecord, relationshipPath);
			
			//Garente que quando o contentChangeListener avisar sobre as alterações, o fragmento já estará com a View criada.
			fragmentManager.executePendingTransactions();
		}
		
		@Override
		Fragment getFragment() {
			return currentFragment;
		}
		
		
		/*
		 * Métodos auxiliares
		 */
		
		private boolean consumeRestoring() {
			boolean ret = restoring;
			restoring = false;
			return ret;
		}

        private String getFragmentTag() {
            return "EntityDetailRelationshipArrayFragment_" + TextUtils.join("-", relationshipPath);
        }

		private void initializeFragment(IEntityRecord entityRecord, String[] relationshipPath) {
			//Se o relationamento referencia outro relacionamento, navega pelos relacionamentos até chegar no final da cadeia.
            for (int i = 0; i < relationshipPath.length-1; i++) {
                String relationship = relationshipPath[i];
                entityRecord = entityRecord.getRelationshipValue(relationship);
                if (entityRecord == null) {
                    //Se não tiver um registro necessário para fazer a negavação até o relacionamento final, inicializa com um DAO vazio.
                    initializeFragment(currentFragment, new MemoryEntityDAO(dataManager, targetMetadata), listLayout, null, null, maskManager, null);
                    return;
                }
            }

			//Cria um DAO limitado aos registros do array de relacionamentos.
            String relationship = relationshipPath[relationshipPath.length -1];
			RelationshipArrayView dataView = new RelationshipArrayView(entityRecord, relationship);
			
			//Se a atualização automática estiver ligada, verifica se é possível utilizar o DAO que busca-os da fonte de dados.
			//Se não, utiliza o DAO que usa os registros a partir de seu estado atual (memória).
			if (autoRefresh && !entityRecord.isNew() && !entityRecord.isDirty(relationship)) {
				//Define o Serachable da entidade para habilitar a pesquisa.
				SearchableInfo searchableInfo = null;
				if (searchableManager != null) {
					searchableInfo = searchableManager.getSearchableInfo(targetMetadata.getName());
				}

				IEntityDAO entityDAO = dataManager.getEntityDAO(dataView);
				initializeFragment(currentFragment, entityDAO, listLayout, listFilter, listOrder, maskManager, searchableInfo);
			} else {
				MemoryEntityDAO entityDAO = new MemoryEntityDAO(dataManager, dataView);
				//O MemoryEntityDAO não suporta ordenação e filtro. Por isto, além dos valores nulos na inicialização, a pesquisa não estará habilitada.
				initializeFragment(currentFragment, entityDAO, listLayout, null, null, maskManager, null);
			}
		}
		
		private static void initializeFragment(EntityListFragment fragment, IEntityDAO entityDAO, ILayoutConfig<ListLayoutType> layoutConfig, IListFilter filter, IListOrder order, MaskManager maskManager, SearchableInfo searchableInfo) {
			fragment.initialize(entityDAO, layoutConfig, filter, order, maskManager);
			fragment.getArguments().putParcelable(EntityListFragment.SEARCHABLE_ARGUMENT, searchableInfo);
		}
	}
}
