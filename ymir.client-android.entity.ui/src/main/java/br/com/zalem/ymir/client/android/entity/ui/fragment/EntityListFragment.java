package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.EnhancedSwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnSuggestionListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.ui.BuildConfig;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldVisibility;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IFilterFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListFilter;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListOrder;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IOrderFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.IEntityRecordListActionProvider;
import br.com.zalem.ymir.client.android.entity.ui.search.EntitySearchSuggestionsProvider;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter.TypedFormatter;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.util.SafeAsyncTask;

/**
 * O EntityListFragment provê uma lista de registros de uma entidade, de acordo com o acessor de dados da entidade
 * ({@link IEntityDAO}) e a configuração do layout dos items da lista ({@link ILayoutConfig}). A configuração destes
 * objetos deve ser feita através do método {@link #initialize(IEntityDAO, ILayoutConfig, IListFilter, IListOrder, MaskManager)}.<br>
 * A criação dos items da lista é feita utilizando um {@link ListLayoutConfigAdapter}.<br>
 * <br>
 * Inicia mostrando todos os registros da entidade. Entretanto, é possível executar pesquisas nos registros através
 * do método {@link #doSimpleSearch(String)} e posteriormente desfazer a pesquisa através do método
 * {@link #undoSearch()}.<br>
 * <br>
 * O fragmento provê uma a funcionalidade de pesquisa de registros para o usuário através de um botão na
 * <code>Action Bar</code>. Para que esta funcionalidade seja habilitada, o fragmento deve receber um
 * {@link SearchableInfo} no argumento {@link #SEARCHABLE_ARGUMENT}.<br>
 * A pesquisa só é realizada depois de um termo ser digitado e o <code>botão de execução</code> no teclado virtual ou o
 * <code>Enter</code> no teclado físico ser pressionado.<br>
 * A funcionalidade de pesquisa também está preparada para sugerir os termos de pesquisas bem sucedidas recentes. Uma
 * pesquisa bem sucedida é aquela que retornou pelo menos um registro. Para isto, o SearchableInfo deve estar
 * configurado com o <code>Suggest Authority</code> do tipo {@link EntitySearchSuggestionsProvider#AUTHORITY} e o
 * <code>Suggest Selection</code> com o nome da entidade cujo o fragmento está mostrando os registros.
 * 
 * @see ListLayoutConfigAdapter
 * @see IEntityDAO
 * @see ILayoutConfig
 * 
 * @author Thiago Gesser
 */
public final class EntityListFragment extends AbstractEntityListFragment {
	
	/**
	 * Argumento do tipo {@link SearchableInfo} que define o comportamento de busca do fragmento.
	 */
	public static final String SEARCHABLE_ARGUMENT = "SEARCHABLE_ARGUMENT";
	/**
	 * Argumento do tipo {@link ISelectQuery} que define a origem dos dados a ser utilizada no fragmento ao invés de todos
	 * os dados disponíveis no {@link IEntityDAO}.
	 * A query deve selecionar registros completos (sem campos específicos) da mesma entidade do IEntityDAO do fragmento,
	 * podendo utilizar filtros de qualquer tipo.
	 */
	public static final String SOURCE_QUERY_ARGUMENT = "SOURCE_QUERY_ARGUMENT";
	
	private static final String SAVED_SIMPLE_SEARCH_QUERY = "SAVED_SIMPLE_SEARCH_QUERY";
	
	private ILayoutConfig<ListLayoutType> layoutConfig;
	private IListFilter filter;
	private IListOrder order;
    private MaskManager maskManager;
	private EntityAttributeFormatter layoutFormatter;

    private TextView emptyTextView;
	private RecyclerView entityList;
	private ListLayoutConfigAdapter entityListAdapter;
	private ListRefreshUIManager uiManager;

	//Armazena a task corrente para que ela possa ser cancelada no caso de uma nova task ser iniciada.
	private AsyncTask<?, ?, ?> currentTask;

	private OnSearchListener searchListener;
	private String simpleSearchQuery;
	private OnRecordsChangeListener recordsListener;
	private boolean recordsLoaded;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			simpleSearchQuery = savedInstanceState.getString(SAVED_SIMPLE_SEARCH_QUERY);
		}
		
		setHasOptionsMenu(true);
	}
	
	@Override
    protected View onCreateThemedView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup fragmentView = (ViewGroup) inflater.inflate(R.layout.entity_list_fragment, container, false);
		entityList = (RecyclerView) fragmentView.findViewById(R.id.entity_list_list);
		//Não utiliza o "setEmptyView" pq ele acaba mostrando a empty view durante o carregamento.
        View emptyView = fragmentView.findViewById(R.id.entity_list_empty_view);
        emptyTextView = (TextView) fragmentView.findViewById(R.id.entity_list_empty_view_text);

		EnhancedSwipeRefreshLayout swipeRefreshLayout = (EnhancedSwipeRefreshLayout) fragmentView.findViewById(R.id.entity_list_swipe_refresh_container);
		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshListener());
		//Só suporta o movimento de swipe para refresh se o comportamento foi habilitado.
		swipeRefreshLayout.setEnabled(isSwipeRefresh());
        //Define as Views que podem sofrer swipe, pois por padrão o SwipeRefreshLayout só suporta isto na View filha direta.
        swipeRefreshLayout.setSwipeableViews(entityList, emptyView);
		
		View progressBarContainer = fragmentView.findViewById(R.id.entity_list_progress_bar_container);
		TextView progressTextView = (TextView) progressBarContainer.findViewById(R.id.entity_list_progress_text);
		uiManager = new ListRefreshUIManager(entityList, emptyView, progressBarContainer, progressTextView, swipeRefreshLayout);
		
		//Se ainda não tiver sido inicializado neste ponto, deixa para criar o adapter no momento da inicialização.
		if (isInitialized()) {
            configureAdapter();
        }

		return fragmentView;
	}

    @Override
	public void onStart() {
		super.onStart();
		
		//Só precisa carregar os registros na primeira vez, depois depende do autoRefresh.
		if (!recordsLoaded) {
			//Se já há uma task carregando os registros, não é necessário interrompê-la e criar outra. 
			if (currentTask != null) {
				return;
			}
			
			refresh(true, new Runnable() {
                @Override
                public void run() {
                    recordsLoaded = true;
                }
            });
		} else if (isAutoRefresh()) {
			refresh(false, null);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString(SAVED_SIMPLE_SEARCH_QUERY, simpleSearchQuery);
	}
	
	@Override
	public void onStop() {
		super.onStop();

		//Se for autoRefresh, para a task atual porque no onStart() será iniciada outra.
		if (recordsLoaded && isAutoRefresh()) {
			updateCurrentTask(null);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		updateCurrentTask(null);
	}
	
	/**
	 * Inicializa o fragmento com os objetos necessários para o seu funcionamento.
	 * 
	 * @param entityDAO acessor dos dados da entidade.
	 * @param layoutConfig configuração de layout para os itens da lista.
	 * @param filter configuração de filtro dos registros da lista. <b>Parâmetro opcional.</b>
	 * @param order configuração de ordenação dos registros da lista. <b>Parâmetro opcional.</b>
     * @param maskManager gerenciador de máscaras utilizado na criação dos formatadores dos campos.
	 * @throws IllegalStateException se a inicialização for feita novamente.
	 */
	public void initialize(IEntityDAO entityDAO, ILayoutConfig<ListLayoutType> layoutConfig, IListFilter filter, IListOrder order, MaskManager maskManager) {
        if (entityDAO == null || layoutConfig == null || maskManager == null) {
			throw new NullPointerException("entityDAO == null || layoutConfig == null || maskManager == null");
		}
		if (isInitialized()) {
			throw new IllegalStateException("EntityListFragment is already initialized.");
		}

		this.entityDAO = entityDAO;
		this.layoutConfig = layoutConfig;
		this.filter = filter;
		this.order = order;
        this.maskManager = maskManager;
		layoutFormatter = EntityAttributeFormatter.fromConfig(maskManager.getContext(), entityDAO.getEntityMetadata(), maskManager, layoutConfig.getFields());

		//Se foi inicializado apenas depois da View ser criada, cria o adapter agora.
		if (isViewCreated()) {
            configureAdapter();
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		SearchableInfo searchableInfo = getSearchableArgument();
		//Se não foi passado um Searchable nos argumentos, nem coloca a ação de Search na ActionBar.
		if (searchableInfo == null) {
			return;
		}
		
		//Coloca a ação de Search baseado no SearchableInfo
		inflater.inflate(R.menu.entity_list_fragment_actions, menu);
	    MenuItem searchMenuItem = menu.findItem(R.id.action_search);
	    SearchView searchView = (SearchView) searchMenuItem.getActionView();
	    searchView.setSearchableInfo(searchableInfo);
	    
	    //O próprio fragmento controla as ações das pesquisas.
	    SearchQueryTextListener searchListener = new SearchQueryTextListener(searchMenuItem);
	    searchView.setOnQueryTextListener(searchListener);
	    
	    String suggestAuthority = searchableInfo.getSuggestAuthority();
	    //Se possui provedor de sugestões, adiciona ações para a seleção de sugestões e o cadastramento de termos de sugestões.
		if (!TextUtils.isEmpty(suggestAuthority)) {
			if (!suggestAuthority.equals(EntitySearchSuggestionsProvider.AUTHORITY)) {
				//Por enquanto só suporta o provedor de sugestões padrão pois é necessário salvar os termos das pesquisas bem sucedidas através de uma meneira específica.
				throw new UnsupportedOperationException("Only the EntitySearchSuggestionsProvider is supported");
		    }
			searchView.setOnSuggestionListener(new SearchSuggestionListener(searchMenuItem, searchView.getSuggestionsAdapter()));
			
			//Seta a informação necessária para salvar os termoas das pesquisas bem sucedidas.
			searchListener.setSearchEntity(searchableInfo.getSuggestSelection());
		}
		
	}
	
	@Override
	public void doSimpleSearch(String query, Runnable completionCallback) {
		doInternalSimpleSearch(query, true, completionCallback);
		simpleSearchQuery = query;
		
		if (searchListener != null) {
			searchListener.onDoSimpleSearch(query);
		}
	}

	@Override
	public void undoSearch(Runnable completionCallback) {
		showAll(true, completionCallback);
		simpleSearchQuery = null;
		
		if (searchListener != null) {
			searchListener.onUndoSearch();
		}
	}
	
	@Override
	public void setActionProvider(IEntityRecordListActionProvider actionProvider) {
		checkIsReady();
		if (actionProvider == null) {
			throw new NullPointerException("actionProvider == null");
		}
		
		entityListAdapter.setActionProvider(actionProvider);
	}

    @Override
    public IEntityRecordListActionProvider getActionProvider() {
        checkIsReady();

        return entityListAdapter.getActionProvider();
    }

    @Override
	public void setOnEntityRecordClickListener(OnEntityRecordClickListener clickListener) {
		if (clickListener == null) {
			if (isReady()) {
				entityListAdapter.setOnItemClickListener(null);
			}
			return;
		}

		checkIsReady();
		EntityListItemClickListener itemClickListener = new EntityListItemClickListener(clickListener);
        entityListAdapter.setOnItemClickListener(itemClickListener);
	}
	
	@Override
	public void setOnSearchListener(OnSearchListener searchListener) {
		this.searchListener = searchListener;
	}
	
	@Override
	public void setOnRecordsChangeListener(OnRecordsChangeListener recordsListener) {
		this.recordsListener = recordsListener;
	}
	
	@Override
	public String getSimpleSearchQuery() {
		return simpleSearchQuery;
	}
	
	@Override
	public void refresh(boolean full,  Runnable completionCallback) {
        if (simpleSearchQuery == null) {
            showAll(full, completionCallback);
        } else {
            doInternalSimpleSearch(simpleSearchQuery, full, completionCallback);
        }
	}
	
	@Override
	public void onRecordCreated(IEntityRecord record, int position) {
		checkEntity(record);
		if (record.isNew() || record.isDeleted()) {
			throw new IllegalArgumentException("Record is new or deleted.");
		}

		List<IEntityRecord> records = entityListAdapter.getRecords();
		if (position < 0) {
			position = 0;
		} else if (position > records.size()) {
			position = records.size();
		}

		records.add(position, record);
		entityListAdapter.notifyItemInserted(position);
        uiManager.onListChanged();
	}

    @Override
    public void onRecordChanged(IEntityRecord record) {
        checkEntity(record);

        int recordIndex = findRecordIndex(record);
        entityListAdapter.notifyItemChanged(recordIndex);
    }

    @Override
	public void onRecordDeleted(IEntityRecord record) {
		checkEntity(record);
		if (!record.isDeleted()) {
			throw new IllegalArgumentException("Record is not deleted.");
		}
		
		List<IEntityRecord> listRecords = entityListAdapter.getRecords();
        int recordIndex = findRecordIndex(record, listRecords);
        listRecords.remove(recordIndex);

		entityListAdapter.notifyItemRemoved(recordIndex);
		uiManager.onListChanged();
	}
	
	/**
	 * Obtém os registros listados atualmente no fragmento.
	 * 
	 * @return os registros obtidos.
	 */
	public List<IEntityRecord> getRecords() {
		checkIsReady();
		
		return entityListAdapter.getRecords();
	}

	/**
	 * Indica se o fragmento já teve seus registros completamente carregados.
	 *
	 * @return <code>true</code> se o fragmento está carregado e <code>false</code> caso contrário.
	 */
	public boolean isLoaded() {
		return recordsLoaded;
	}

    /**
     * Define o texto que será exibido quando o fragmento não tiver registros para mostrar.<br>
     * É possível voltar para o texto original {@link R.string#no_data} passando como parametro um valor <= 0.
     *
     * @param resId o id do recurso String contendo o novo texto ou um valor <= 0 se o texto original deve ser aplicado.
     */
    public void setEmptyText(int resId) {
        if (!isViewCreated()) {
            throw new IllegalStateException("View is not created yet.");
        }

        if (resId <= 0) {
            resId = R.string.no_data;
        }
        emptyTextView.setText(resId);
    }

    /**
     * Obtém o {@link RecyclerView} que exibe a lista de registros da entidade.
     *
     * @return o RecyclerView obtida ou <code>null</code> se ela não tiver sido criada ainda.
     */
    public RecyclerView getEntityList() {
        return entityList;
    }

    /*
	 * Métodos auxiliares
	 */
	
	private boolean isInitialized() {
		return entityDAO != null;
	}
	
	private boolean isViewCreated() {
		return getView() != null;
	}
	
	private void checkIsReady() {
		if (!isReady()) {
			throw new IllegalStateException("EntityListFragment is not ready yet. This method can only be called after the fragment's view is created and the initialization is done.");
		}
	}

    private boolean isReady() {
        return entityListAdapter != null;
    }

    private void checkEntity(IEntityRecord record) {
		if (!entityDAO.getEntityMetadata().getName().equals(record.getEntityMetadata().getName())) {
			throw new IllegalArgumentException(String.format("The record is from the wrong entity. Expected entity = %s, record entity = %s", entityDAO.getEntityMetadata().getName(), record.getEntityMetadata().getName()));
		}
	}

    private void configureAdapter() {
        //Passa o contexto da lista pois ela pode estar influenciado por outro tema.
        entityListAdapter = new ListLayoutConfigAdapter(entityList.getContext(), layoutConfig, layoutFormatter);
        entityList.setAdapter(entityListAdapter);
    }


    private SearchableInfo getSearchableArgument() {
		Bundle arguments = getArguments();
		if (arguments == null) {
			return null;
		}

		return arguments.getParcelable(SEARCHABLE_ARGUMENT);
	}
	
	private ISelectQuery getSourceQueryArgument() {
		Bundle arguments = getArguments();
		if (arguments == null) {
			return null;
		}
		
		return arguments.getParcelable(SOURCE_QUERY_ARGUMENT);
	}

	private boolean isAutoRefresh() {
		Bundle arguments = getArguments();
		if (arguments == null) {
			//O padrão do autoRefresh é true.
			return true;
		}
		
		return arguments.getBoolean(AUTO_REFRESH_ARGUMENT, true);
	}
	
	private boolean isSwipeRefresh() {
		Bundle arguments = getArguments();
		if (arguments == null) {
			//O padrão do swipeRefresh é true.
			return true;
		}
		
		return arguments.getBoolean(SWIPE_REFRESH_ARGUMENT, true);
	}
	
	private void doInternalSimpleSearch(String query, boolean isFull, Runnable completionCallback) {
        executeListLoadTask(isFull, completionCallback, query);
	}
	
	private void showAll(boolean isFull, Runnable completionCallback) {
		executeListLoadTask(isFull, completionCallback);
	}
	
	private void executeListLoadTask(boolean isFull, Runnable completionCallback, String... params) {
		checkIsReady();

        ListLoadTask listLoadTask = new ListLoadTask(getSourceQueryArgument(), isFull, completionCallback);
		//Se o DAO ainda não está pronto, coloca a task de atualização dentro de uma outra task que irá aguardar por isto e que pode ser cancelada.
		if (!entityDAO.isReady()) {
			executeTask(new WaitForDAOReadyTask(listLoadTask), params);
			return;
		}
		
		executeTask(listLoadTask, params);
	}
	
	private void executeTask(AsyncTask<String, Void, List<IEntityRecord>> task, String... params) {
        updateCurrentTask(task);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
	}
	
	private void updateCurrentTask(AsyncTask<?, ?, ?> task) {
		if (currentTask != null) {
			currentTask.cancel(true);
		}
		
		currentTask = task;
	}

    private int findRecordIndex(IEntityRecord record) {
        return findRecordIndex(record, entityListAdapter.getRecords());
    }

    private static int findRecordIndex(IEntityRecord record, List<IEntityRecord> listRecords) {
        Serializable recordId = record.getId();
        for (int i = 0; i < listRecords.size(); i++) {
            IEntityRecord listRecord = listRecords.get(i);
            if (listRecord == record || (recordId != null && recordId.equals(listRecord.getId()))) {
                return i;
            }
        }

        throw new IllegalArgumentException("Record not found in the EntityListFragment: " + recordId);
    }
	
	
	/*
	 * Classes auxiliares.
	 */
	
	/**
	 * Generaliza a funcionalidade de pesquisa feita pelos listeners de pesquisa.
	 */
	private abstract class AbstractSearchListener {
		private final MenuItem searchMenuItem;
		
		public AbstractSearchListener(MenuItem searchMenuItem) {
			this.searchMenuItem = searchMenuItem;
		}
		
		protected final void doSearch(String query, Runnable completionCallback) {
			doSimpleSearch(query, completionCallback);
			
			//Esconde tudo relacionado a pesquisa.
			searchMenuItem.collapseActionView();
		}
	}
	
	/**
	 * Responsável por executar a pesquisa ({@link EntityListFragment#doSimpleSearch(String, Runnable)} quando um termo de pesquisa é submetido.
	 */
	private final class SearchQueryTextListener extends AbstractSearchListener implements SearchView.OnQueryTextListener {
		
		private String searchEntity;
		
		public SearchQueryTextListener(MenuItem searchMenuItem) {
			super(searchMenuItem);
		}
		
		@Override
		public boolean onQueryTextSubmit(String query) {
			SaveSuggestionQueryCallback searchCallback = null;
			//Só cria o callback se foi configurado um provedor de sugestões.
			if (searchEntity != null) {
				searchCallback = new SaveSuggestionQueryCallback(query, searchEntity);
			}

			doSearch(query, searchCallback);
		    return true;
		}
		
		@Override
		public boolean onQueryTextChange(String newText) {
		    return true;
		}
		
		public void setSearchEntity(String searchEntity) {
			this.searchEntity = searchEntity;
		}
	}
	
	/**
	 * Responsável por executar a pesquisa ({@link EntityListFragment#doSimpleSearch(String, Runnable)} quando uma sugestão é selecionada.
	 */
	private final class SearchSuggestionListener extends AbstractSearchListener implements OnSuggestionListener {
		
		private final CursorAdapter suggestionsAdapter;
		
		public SearchSuggestionListener(MenuItem searchMenuItem, CursorAdapter suggestionsAdapter) {
			super(searchMenuItem);
			this.suggestionsAdapter = suggestionsAdapter;
		}
		
		@Override
		@SuppressWarnings("resource")
		public boolean onSuggestionClick(int position) {
			//É obrigado a implementar este listener e obter a query da sugestão desta forma (utilizando conhecimento interno da API Android)
			//porque esta informação não é passada diretamente para o listener.
			Cursor cursor = (Cursor) suggestionsAdapter.getItem(position);
			if (BuildConfig.DEBUG && cursor == null) {
				throw new AssertionError();
			}
			
			String query = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY));
			doSearch(query, null);
			
			return true;
		}
		
		@Override
		public boolean onSuggestionSelect(int position) {
			return true;
		}
	}

	/**
	 * Responsável por salvar os termos das pesquisas bem sucedidas (que retornaram alguma entidade) para servirem como sugestões de futuras pesquisas. 
	 */
	private final class SaveSuggestionQueryCallback implements Runnable {

		private final String query;
		private final String searchEntity;

		public SaveSuggestionQueryCallback(String query, String searchEntity) {
			this.query = query;
			this.searchEntity = searchEntity;
		}
		
		@Override
		public void run() {
			//Se a pesquisa não retornou nada, não salva a sugestão.
			//Esta verificação não conflitará com outras pesquisas pois está sendo executado na Thread de UI.
			if (entityListAdapter.getItemCount() == 0) {
				return;
			}
			
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
                                                                              EntitySearchSuggestionsProvider.AUTHORITY,
                                                                              EntitySearchSuggestionsProvider.MODE);
			suggestions.saveRecentQuery(query, searchEntity);
		}
	}

	/**
	 * Responsável por executar o refresh da lista quando a ação de swipe refresh for feita.
	 */
	private final class SwipeRefreshListener implements OnRefreshListener {
		
		@Override
		public void onRefresh() {
            refresh(false, null);
		}
	}
	
	
	/*
	 * Classes auxiliares estáticas (verificar sobre a possibilidade de externalizá-las). 
	 */
	
	/**
	 * Responsável por executar o listener de click de item setado no fragmento.
	 */
	private static final class EntityListItemClickListener implements ListLayoutConfigAdapter.OnItemClickListener {

		private final OnEntityRecordClickListener innerListener;

		public EntityListItemClickListener(OnEntityRecordClickListener innerListener) {
			this.innerListener = innerListener;
		}

		@Override
		public void onItemClick(ListLayoutConfigAdapter adapter, View view, int position) {
			IEntityRecord entityRecord = adapter.getRecord(position);
			innerListener.onEntityRecordClick(entityRecord);
		}
	}

    /**
     * Engloba um {@link ListLoadTask} e aguarda até o {@link IEntityDAO} do fragmento estar pronto (através do {@link IEntityDAO#isReady()} == <code>true</code>)
     * para executá-la. Os métodos de controle de UI da <code>AbstractListRefreshTask</code> englobada também são executados por esta classe.<br>
     * Como o tempo para o <code>IEntityDAO</code> estar pronto é indeterminado, o <code>WaitForDAOReadyTask</code> deve ser executada através
     * do {@link #executeOnExecutor(Executor, Object[])} e deve ser cancelada no <code>onStop</code> do {@link Fragment}.
     */
    private final class WaitForDAOReadyTask extends SafeAsyncTask<String, Void, List<IEntityRecord>> {
		
		private final ListLoadTask innerTask;

		public WaitForDAOReadyTask(ListLoadTask innerTask) {
			this.innerTask = innerTask;
		}
		
		@Override
		protected void onPreExecute() {
			innerTask.onPreExecute();
			
			//Como ainda não está "ready", coloca uma mensagem no Progress para informar ao usuário da sincornização inicial.
			uiManager.startRefresh(true, R.string.loading_essential_data);
		}
		
		@Override
		protected List<IEntityRecord> safeDoInBackground(String... params) throws ParseException {
			//Aguarda até o DAO estar pronto para ser acessado.
			while (!entityDAO.isReady()) {
				if (isCancelled()) {
					//Se foi cancelado, interrompe a espera. Pode retornar null mesmo pq o "onPostExecute" não será chamado.
					return null;
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					//Provavelmente causado pelo cancelamento da task, então apenas ignora.
					Log.w(ListLoadTask.class.getName(), e);
				}
			}
			
			return innerTask.safeDoInBackground(params);
		}
		
		@Override
		protected void safeOnPostExecute(List<IEntityRecord> result) {
			innerTask.safeOnPostExecute(result);

            uiManager.finishRefresh();
		}
	}

	/**
	 * Task que seleciona e atualiza os dados da lista.<br>
     * A query pode se basear no {@link IEntityDAO DAO} do fragmento ou em uma {@link #sourceQuery outra query}. Os filtros da query
     * podem ser os* difinidos na {@link IListFilter configuração} e o termo de pesquisa, que deve estar contido em pelo menos um campo
     * mostrado no layout do fragmento.<br>
     * Os registros resultantes serão ordenados de acordo com a {@link IListOrder configuração de ordenação}, se foi definida, ou na ordem
     * original caso contrário.
	 */
	private final class ListLoadTask extends SafeAsyncTask<String, Void, List<IEntityRecord>> {

        private final ISelectQuery sourceQuery;
		private final boolean isFull;
		private final Runnable completionCallback;

        /**
         * Cria uma nova task de seleção de dados do fragmento.
         *
         * @param sourceQuery query cujo a seleção pode se basear. <b>parâmetro opcional</b>.
         * @param isFull <code>true</code> se será uma seleção completa (demorada) ou <code>false</code> caso contrário.
         * @param completionCallback callback para ser chamado ao fim da seleção de dados. <b>parâmetro opcional</b>.
         */
		public ListLoadTask(ISelectQuery sourceQuery, boolean isFull, Runnable completionCallback) {
			this.sourceQuery = sourceQuery;
			this.isFull = isFull;
			this.completionCallback = completionCallback;
		}

		@Override
		protected void onPreExecute() {
			uiManager.startRefresh(isFull);
		}
		
		@Override
		protected final List<IEntityRecord> safeDoInBackground(String... params) throws ParseException {
            String filterText = params.length == 0 ? null : params[0];
			List<IEntityRecord> records = selectRecords(filterText);

			if (recordsListener != null) {
				List<IEntityRecord> newRecords = recordsListener.beforeRecordsChange(EntityListFragment.this, Collections.unmodifiableList(records));
				if (newRecords != null) {
					return newRecords;
				}
			}

			return records;
		}

        @Override
		protected void safeOnPostExecute(final List<IEntityRecord> result) {
            entityListAdapter.setRecords(result);
			
			if (completionCallback != null) {
				completionCallback.run();
			}
			if (recordsListener != null) {
				recordsListener.afterRecordsChange(EntityListFragment.this, result);
			}
			
			uiManager.finishRefresh();
		}


        /*
		 * Métodos auxiliares
		 */

        private List<IEntityRecord> selectRecords(String layoutFieldsFilter) throws ParseException {
            //Por padrão usa o entityDAO como fonte de dados, mas se foi passada uma query, baseia-se nela.
            ISelectBuilder q;
            if (sourceQuery == null) {
                q = entityDAO.newSelectBuilder(false);
            } else {
                q = entityDAO.getEntityManager().newQueryBuilder().
                        select(false).
                        from(sourceQuery);
            }

            //Adiciona os filtros pré-determinados na configuração, se houverem.
            if (filter != null) {
                SyncStatus[] syncStatus = filter.getSyncStatus();
                if (syncStatus != null && syncStatus.length > 0) {
                    q.condition().ssIn(syncStatus);
                }

                IFilterFieldMapping[] fields = filter.getFields();
                if (fields != null && fields.length > 0) {
                    q.condition().o();

                    IEntityMetadata entityMetadata = entityDAO.getEntityMetadata();
                    for (int i = 0; i < fields.length; i++) {
                        IFilterFieldMapping fieldMapping = fields[i];
                        String[] values = getFilterFieldValues(fieldMapping);

                        String[] relationship = fieldMapping.getRelationship();
                        if (relationship != null) {
                            if (values.length == 1) {
                                q.rEq(values[0], relationship);
                            } else {
                                q.rIn(values, relationship);
                            }
                        } else {
                            TypedFormatter<?, ?> formatter = EntityAttributeFormatter.createTypedFormatter(maskManager, entityMetadata, fieldMapping);
                            String[] attributePath = fieldMapping.getAttribute();
                            if (values.length == 1) {
                                Object obj = formatter.parseValue(values[0]);
                                q.eq(obj, attributePath);
                            } else {
                                Object[] objs = formatter.parseValues(values);
                                q.in(objs, attributePath);
                            }
                        }

                        if (i < fields.length-1) {
                            q.and();
                        }
                    }
                    q.c();
                }
            }

            //Adiciona os campos filtráveis do layout, se houverem.
            if (layoutFieldsFilter != null) {
                List<String[]> attrsPaths = getFilterableAttrsPaths();
                if (!attrsPaths.isEmpty()) {
                    q.condition().o();

                    //Filtra de forma que pelo menos um dos campos filtráveis do layout contenha o texto.
                    for (Iterator<String[]> iter = attrsPaths.iterator(); iter.hasNext();) {
                        String[] attrPath = iter.next();
                        q.contains(layoutFieldsFilter, attrPath);

                        if (iter.hasNext()) {
                            q.or();
                        }
                    }
                    q.c();
                }
            }

            //Aplica a configuração de ordenação dos registros, se houver.
            if (order != null) {
                IOrderFieldMapping[] orderFields = order.getFields();
                if (orderFields != null && orderFields.length > 0) {
                    for (IOrderFieldMapping orderField : orderFields) {
                        q.orderBy(orderField.isAsc(), getAttributePath(orderField));
                    }
                }
            }

            //Executa e retorna a lista de resultados.
            return q.listResult();
        }

        private List<String[]> getFilterableAttrsPaths() {
            List<String[]> filtersAttrPaths = new ArrayList<>();
            for (ILayoutFieldMapping fieldMapping : layoutConfig.getFields()) {
                //Os campos de imagem não são utilizados nos filtros.
                if (fieldMapping.getVisibility() != LayoutFieldVisibility.VISIBLE || fieldMapping.getLayoutField().getType() == LayoutFieldType.IMAGE) {
                    continue;
                }

                String[] filterAttrPath = getAttributePath(fieldMapping);
                filtersAttrPaths.add(filterAttrPath);
            }
            return filtersAttrPaths;
        }

        private String[] getAttributePath(IFieldMapping fieldMapping) {
            String[] attributePath = fieldMapping.getAttribute();
            if (attributePath == null || attributePath.length == 0) {
                throw new IllegalArgumentException("Invalid field mapping: only mappings leading to an attribute are supported and not to a relationship: " + Arrays.toString(fieldMapping.getRelationship()));
            }
            return attributePath;
        }

        private String[] getFilterFieldValues(IFilterFieldMapping fieldMapping) {
            String[] fieldPath = fieldMapping.getRelationship() != null ? fieldMapping.getRelationship() : fieldMapping.getAttribute();
            if (fieldPath == null) {
                throw new IllegalArgumentException("Invalid filter field mapping: the mapping does not point to an attribute or relationship.");
            }

            String[] values = fieldMapping.getValues();
            if (values == null || values.length == 0) {
                throw new IllegalArgumentException("There are no values in the filter field mapping: " + Arrays.toString(fieldPath));
            }
            return values;
        }
    }

	/**
	 * Responsável por controlar as características visuais deste fragmento durante o progresso das atualizações de dados.
	 * Não é Thread Safe, mas não apresentará problemas porque o fragmento garante que apenas uma task execute de cada vez (a antiga é cancelada antes de iniciar a nova).
	 */
	private static final class ListRefreshUIManager {
		
		private final RecyclerView entityList;
		private View emptyView;
		private final View progressBarContainer;
		private final TextView progressTextView;
		private final SwipeRefreshLayout swipeRefreshLayout;
		private final boolean swipeRefreshEnabled;

		public ListRefreshUIManager(RecyclerView entityList, View emptyView, View progressBarContainer, TextView progressTextView, SwipeRefreshLayout swipeRefreshLayout) {
			this.entityList = entityList;
			this.emptyView = emptyView;
			this.progressBarContainer = progressBarContainer;
			this.progressTextView = progressTextView;
			this.swipeRefreshLayout = swipeRefreshLayout;
			
			//Se o swipeRefresh está desativado, n precisa controlar a habilitaçao dele aqui dentro.
			swipeRefreshEnabled = swipeRefreshLayout.isEnabled();
		}
		
		public void startRefresh(boolean isFull) {
			startRefresh(isFull, 0);
		}
		
		public void startRefresh(boolean isFull, int msgResId) {
			if (isFull) {
				swipeRefreshLayout.setRefreshing(false);
				if (msgResId > 0) {
					progressTextView.setText(msgResId);
					progressTextView.setVisibility(View.VISIBLE);				
				} else {
					progressTextView.setVisibility(View.GONE);				
				}
				progressBarContainer.setVisibility(View.VISIBLE);
				entityList.setVisibility(View.INVISIBLE);
				emptyView.setVisibility(View.GONE);
			} else {
				swipeRefreshLayout.setRefreshing(true);
				progressBarContainer.setVisibility(View.GONE);
				showList();
			}
			
			if (swipeRefreshEnabled) {
				swipeRefreshLayout.setEnabled(false);
			}
		}
		
		public void finishRefresh() {
			boolean isFull = !swipeRefreshLayout.isRefreshing();
			
			if (swipeRefreshEnabled) {
				swipeRefreshLayout.setEnabled(true);
			}
			
			if (entityList.getAdapter().getItemCount() == 0) {
				if (isFull) {
					progressBarContainer.setVisibility(View.GONE);
				} else {
					swipeRefreshLayout.setRefreshing(false);
				}
				showList(true);
				return;
			}
			
			if (isFull) {
                entityList.scrollToPosition(0);
                progressBarContainer.setVisibility(View.GONE);
			} else {
				swipeRefreshLayout.setRefreshing(false);
			}
            showList(false);
		}
		
		public void onListChanged() {
			//Se já está fazendo o refresh completo, não há porque atualizar a lista agora, já que ela será mostrada no final do refresh.
			if (!swipeRefreshLayout.isRefreshing() && progressBarContainer.getVisibility() == View.VISIBLE) {
				return;
			}
			
			showList();
		}
		
		/*
		 * Métodos auxiliares
		 */
		
		private void showList() {
			showList(entityList.getAdapter().getItemCount() == 0);
		}
		
		private void showList(boolean isEmpty) {
			if (isEmpty) {
				emptyView.setVisibility(View.VISIBLE);
				entityList.setVisibility(View.INVISIBLE);
			} else {
				emptyView.setVisibility(View.GONE);
				entityList.setVisibility(View.VISIBLE);
			}
		}
	}
}