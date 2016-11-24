package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListFilter;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListOrder;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListTab;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.IEntityRecordListActionProvider;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.widget.AbstractFragmentPagerAdapter;

/**
 * Paginador de fragmentos do tipo {@link EntityListFragment}. Mantém os fragmentos em um {@link ViewPager}. <br>
 * As páginas deste fragmento são configuradas através de um array de {@link IListTab} e demais configurações passadas
 * no método {@link #initialize(IEntityDAO, IListTab[], MaskManager, ILayoutConfig, IListFilter, IListOrder)}.
 * Cada configuração de aba resultará em um fragmento no paginador.<br>
 * <br> 
 * O EntityListPagerFragment assume o papel do fragmento corrente, repassando
 * todas as responsabilidades vindas do {@link AbstractEntityListFragment} para ele. Os argumentos definidos neste fragmento
 * também são repassados para os fragmentos filhos.
 *
 * @see EntityListFragment
 * 
 * @author Thiago Gesser
 */
public final class EntityListPagerFragment extends AbstractEntityListFragment {

	private ViewPager pager;
	private EntityListPagerAdapter adapter;
	
	private MaskManager maskManager;
	private IListTab[] listTabs;
	private ILayoutConfig<ListLayoutType> defaultLayout;
	private IListFilter defaultFilter;
	private IListOrder defaultOrder;
	
	private OnListFragmentChangeListener pageChangeListener;
	private OnEntityRecordClickListener entityRecordClickListener;
	private OnSearchListener searchListener;
	private IEntityRecordListActionProvider actionProvider;
	private OnRecordsChangeListener recordsListener;

	@Override
    protected View onCreateThemedView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.entity_list_pager_fragment, container, false);
		pager = (ViewPager) view.findViewById(R.id.entity_list_pager);
        int pagesMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.pager_fragment_pages_margin);
        pager.setPageMargin(pagesMargin);

		adapter = new EntityListPagerAdapter();
		pageChangeListener = new OnListFragmentChangeListener();
		//Coloca o adapter ajeitado para contornar o bug de não chamar o listener na primeira vez.
		pager.setAdapter(AndroidBugsUtils.applyWorkaroundForBug27526(pager, adapter, pageChangeListener));
        pager.addOnPageChangeListener(pageChangeListener);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.entity_list_pager_tabs);
        tabLayout.setupWithViewPager(pager);
		return view;
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		EntityListFragment listFragment = getCurrentEntityListFragment();
		if (listFragment == null) {
			return;
		}
		
		if (hidden) {
			listFragment.setMenuVisibility(false);
			listFragment.setUserVisibleHint(false);
 		} else {
 			listFragment.setMenuVisibility(true);
 			listFragment.setUserVisibleHint(true);
 		}
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		EntityListFragment listFragment = getCurrentEntityListFragment();
		if (listFragment == null || isHidden()) {
			return;
		}

		//Repassa para o fragmento filho, se houver, pq o Android não faz isto automaticamente.
		listFragment.setMenuVisibility(menuVisible);
	}

	/**
	 * Inicializa o fragmento com os objetos necessários para o seu funcionamento.<br>
	 * 
	 * @param entityDAO acessor dos dados da entidade.
	 * @param listTabs configurações das abas de lista de registros.
	 * @param maskManager gerenciador de máscaras utilizado na criação dos formatadores dos campos das listas.
	 * @param defaultLayout configuração de layout padrão para os itens das listas. <b>Parâmetro opcional</b>.
	 * @param defaultFilter configuração de filtro padrão para as listas. <b>Parâmetro opcional</b>.
	 * @param defaultOrder configuração de ordenação padrão para as listas. <b>Parâmetro opcional</b>.
	 * @throws IllegalStateException se a inicialização for feita novamente.
	 */
	public void initialize(IEntityDAO entityDAO, IListTab[] listTabs,  MaskManager maskManager,
						   ILayoutConfig<ListLayoutType> defaultLayout, IListFilter defaultFilter, IListOrder defaultOrder) {
		if (entityDAO == null || listTabs == null || listTabs.length == 0 || maskManager == null) {
			throw new IllegalArgumentException("entityDAO == null || listTabs == null || listTabs.length == 0 || maskManager == null");
		}
		
		if (this.entityDAO != null) {
			throw new IllegalStateException("EntityListPagerFragment is already initialized.");
		}
		
		this.entityDAO = entityDAO;
		this.listTabs = listTabs;
		this.maskManager = maskManager;
		this.defaultLayout = defaultLayout;
		this.defaultFilter = defaultFilter;
		this.defaultOrder = defaultOrder;
	}

	@Override
	public void setOnEntityRecordClickListener(OnEntityRecordClickListener entityRecordClickListener) {
		this.entityRecordClickListener = entityRecordClickListener;
		
		EntityListFragment currentListFragment = getCurrentEntityListFragment();
		if (currentListFragment != null) { 
			currentListFragment.setOnEntityRecordClickListener(entityRecordClickListener);
		}
	}


	@Override
	public void setOnSearchListener(OnSearchListener searchListener) {
		this.searchListener = searchListener;
		
		EntityListFragment currentListFragment = getCurrentEntityListFragment();
		if (currentListFragment != null) { 
			currentListFragment.setOnSearchListener(searchListener);
		}
	}
	
	@Override
	public void setOnRecordsChangeListener(OnRecordsChangeListener recordsListener) {
		this.recordsListener = recordsListener;
		//Se o adapter ainda não estiver criado, deixa para definir o listener nos fragmentos assim que eles forem criados.
		if (!isViewCreated()) {
			return;
		}
		
		//Seta o listener em todos os fragmentos criados.
		for (int i = 0; i < adapter.getCount(); i++) {
			EntityListFragment listFragment = adapter.getFragment(i);
			if (listFragment == null) {
				continue;
			}

			listFragment.setOnRecordsChangeListener(recordsListener);
		}
	}
	
	@Override
	public void setActionProvider(IEntityRecordListActionProvider actionProvider) {
		if (actionProvider == null) {
			throw new NullPointerException("actionProvider == null");
		}
		
		//Armazena para setar nos fragmentos que forem sendo criados.
		this.actionProvider = actionProvider;

		//Atualiza todos os fragmentos já criados.
		for (int i = 0; i < adapter.getCount(); i++) {
			EntityListFragment listFragment = adapter.getFragment(i);
			if (listFragment == null) {
				continue;
			}
			
			listFragment.setActionProvider(actionProvider);
		}
	}

    @Override
    public IEntityRecordListActionProvider getActionProvider() {
        return actionProvider;
    }

	@Override
	public void refresh(boolean full, Runnable completionCallback) {
		//Atualiza todos os fragmentos criados.
		for (int i = 0; i < adapter.getCount(); i++) {
			EntityListFragment listFragment = adapter.getFragment(i);
			if (listFragment == null) {
				continue;
			}

            if (i == pager.getCurrentItem()) {
                //Avisa apenas para o fragmento corrente.
			    listFragment.refresh(full, completionCallback);
            } else {
			    listFragment.refresh(full, null);
            }
		}
	}

    @Override
	public String getSimpleSearchQuery() {
		EntityListFragment currentListFragment = getCurrentEntityListFragment();
		if (currentListFragment == null) { 
			return null;
		}
			
		return currentListFragment.getSimpleSearchQuery();
	}


	@Override
	public void doSimpleSearch(String query, Runnable completionCallback) {
		getAndCheckCurrentEntityListFragment().doSimpleSearch(query, completionCallback);
	}

	@Override
	public void undoSearch(Runnable completionCallback) {
		getAndCheckCurrentEntityListFragment().undoSearch(completionCallback);
	}
	
	@Override
	public void onRecordCreated(IEntityRecord record, int position) {
		getAndCheckCurrentEntityListFragment().onRecordCreated(record, position);
	}

    @Override
    public void onRecordChanged(IEntityRecord record) {
		getAndCheckCurrentEntityListFragment().onRecordChanged(record);
    }

    @Override
	public void onRecordDeleted(IEntityRecord record) {
		getAndCheckCurrentEntityListFragment().onRecordDeleted(record);
	}


	/**
	 * Obtém o adapter utilizado na criação dos fragmentos das páginas.
	 *
	 * @return o adapter obtido.
	 * @throws IllegalStateException se a View do fragmento (e consequentemente o adapter) não tiver sido criada ainda.
	 */
	public EntityListPagerAdapter getPagerAdapter() {
        checkViewCreated();

        return adapter;
	}

    /**
     * Obtém o número da página corrente do fragmento.
     *
     * @return o número da pagina obtido.
     */
    public int getCurrentPage() {
        checkViewCreated();

        return pager.getCurrentItem();
    }

	/**
	 * Define um listener de alteração de páginas do fragmento.
	 *
	 * @param pageChangeListener o listener.
	 */
	public void setOnPageChangeListener(OnPageChangeListener pageChangeListener) {
        checkViewCreated();

        this.pageChangeListener.setInnerListener(pageChangeListener);
	}

    /**
	 * Obtém o listener de alteração de páginas do fragmento.
	 *
	 * @return o listener obtido ou <code>null</code> caso nenhum listener tenha sido definido.
	 */
	public OnPageChangeListener getOnPageChangeListener() {
		return pageChangeListener.getInnerListener();
	}

	
	/*
	 * Métodos auxiliares
	 */
	
	private boolean isViewCreated() {
		return getView() != null;
	}

    private void checkViewCreated() {
        if (!isViewCreated()) {
            throw new IllegalStateException("View was not created yet.");
        }
    }
	
	private EntityListFragment getCurrentEntityListFragment() {
		if (!isViewCreated()) {
			return null;
		}
		return adapter.getFragment(pager.getCurrentItem());
	}
	
	private EntityListFragment getAndCheckCurrentEntityListFragment() {
		EntityListFragment entityListFragment = getCurrentEntityListFragment();
		if (entityListFragment == null) {
			throw new IllegalArgumentException("The current EntityListFragment is not available yet.");
		}
		
		return entityListFragment;
	}
	
	
	/*
	 * Classes auxliares
	 */

	/**
	 * Listener de troca do fragmento no ViewPager. É responsável por setar os listeners e mostrar as ações de 
	 * ActionBar no fragmento atual e remove-los do fragmento antigo. 
	 */
	private class OnListFragmentChangeListener implements OnPageChangeListener {
		
		private int oldPosition = pager.getCurrentItem();
		private OnPageChangeListener innerListener;

		@Override
		public void onPageSelected(int position) {
			if (oldPosition != position) {
				//Remove os listeners e as ações do fragmento antigo.
				EntityListFragment oldListFragment = adapter.getFragment(oldPosition);
				if (oldListFragment != null) {
					oldListFragment.setOnEntityRecordClickListener(null);
					oldListFragment.setOnSearchListener(null);
					oldListFragment.setMenuVisibility(false);
					oldListFragment.setUserVisibleHint(false);
				}
				oldPosition = position;
			}

			//Executa as pendências para garantir que as views dos fragmentos estão criadas.
			getChildFragmentManager().executePendingTransactions();

			//Seta os listeners e as ações no fragmento corrente.
			EntityListFragment currentListFragment = adapter.getFragment(position);
			currentListFragment.setOnSearchListener(searchListener);
			currentListFragment.setOnEntityRecordClickListener(entityRecordClickListener);

			if (!isHidden()) {
				//Só faz isto quando o fragmento não está escondido pq se não depois ele não atualiza as ações da ActionBar.
				currentListFragment.setMenuVisibility(isMenuVisible());
				currentListFragment.setUserVisibleHint(true);

				//E só faz isto na mesma situação para evitar atualizações indevidas na tela.
				if (searchListener != null) {
					//Avisa ao listener a mudança no filtro atual.
					String searchQuery = currentListFragment.getSimpleSearchQuery();
					if (searchQuery != null) {
						searchListener.onDoSimpleSearch(searchQuery);
					} else {
						searchListener.onUndoSearch();
					}
				}
			}

			if (innerListener != null) {
				innerListener.onPageSelected(position);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (innerListener != null) {
				innerListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			if (innerListener != null) {
				innerListener.onPageScrollStateChanged(state);
			}
		}

		public OnPageChangeListener getInnerListener() {
			return innerListener;
		}

		public void setInnerListener(OnPageChangeListener innerListener) {
			this.innerListener = innerListener;
		}
	}
	
	/**
	 * Adapter de fragmentos do tipo {@link EntityListFragment}.
	 */
    public final class EntityListPagerAdapter extends AbstractFragmentPagerAdapter<EntityListFragment> {
		
		public EntityListPagerAdapter() {
			super(getChildFragmentManager(), listTabs.length);
		}
		
		@Override
		public int getCount() {
			return listTabs.length;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return listTabs[position].getTitle();
		}
		
		@Override
		protected EntityListFragment createFragment(int position) {
			EntityListFragment fragment = new EntityListFragment();
			//Repassa todos os argumentos do fragmento de pager para os fragmentos filhos.
            Bundle arguments = getArguments();
            if (getArguments() != null) {
                //Copia para evitar que os fragmentos compartilhem a mesma instância de Bundle.
			    fragment.setArguments(new Bundle(getArguments()));
            }
			return fragment;
		}
		
		@Override
		protected void initializeFragment(EntityListFragment fragment, int position) {
			IListTab listTab = listTabs[position];
			ILayoutConfig<ListLayoutType> layoutConfig = listTab.getLayout();
			if (layoutConfig == null) {
				if (defaultLayout == null) {
					throw new IllegalArgumentException("No list layout defined. Tab config: " + listTab);
				}
				layoutConfig = defaultLayout;
			}
			IListFilter filter = listTab.getFilter();
			if (filter == null) {
				filter = defaultFilter;
			}
			IListOrder order = listTab.getOrder();
			if (order == null) {
				order = defaultOrder;
			}

			fragment.initialize(entityDAO, layoutConfig, filter, order, maskManager);
			fragment.setOnRecordsChangeListener(recordsListener);
			
			if (actionProvider != null) {
				//A view do fragmento deve estar criada para setar o menu.
				getChildFragmentManager().executePendingTransactions();
				
				fragment.setActionProvider(actionProvider);
			}
		}
	}
}
