package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailTab;
import br.com.zalem.ymir.client.android.entity.ui.search.ISearchableManager;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.widget.AbstractFragmentPagerAdapter;

/**
 * Paginador de fragmentos do tipo {@link EntityDetailFragment}. Mantém os fragmentos em um {@link ViewPager}.<br>
 * As páginas deste fragmento são configuradas através de um array de {@link IDetailTab} e demais configurações passadas
 * no método {@link #initialize(IEntityDataManager, IEntityUIConfigManager, ISearchableManager, IEntityDAO, IDetailTab[], MaskManager)}.
 * Cada configuração de aba resultará em um fragmento no paginador.<br>
 * <br> 
 * O EntityDetailPagerFragment altera os dados de todos os fragmentos de detalhe conforme requisitado através do método
 * {@link #setEntityRecord(IEntityRecord)}. Os argumentos definidos neste fragmento são repassados para os fragmentos de detalhes.
 *
 * @see EntityDetailFragment
 *
 * @author Thiago Gesser
 */
public final class EntityDetailPagerFragment extends AbstractEntityDetailFragment {
	
	private ViewPager pager;
	private EntityDetailPagerAdapter adapter;
	private DetailFragmentChangeManager detailFragmentManager;
	
	private IEntityDataManager entityManager;
	private IEntityUIConfigManager configManager;
	private ISearchableManager searchableManager;
	private MaskManager maskManager;
	private IDetailTab[] detailTabs;
	
	@Override
    protected View onCreateThemedView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.entity_detail_pager_fragment, container, false);
		pager = (ViewPager) view.findViewById(R.id.entity_detail_pager);
        int pagesMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.pager_fragment_pages_margin);
        pager.setPageMargin(pagesMargin);

		adapter = new EntityDetailPagerAdapter();
		detailFragmentManager = new DetailFragmentChangeManager();
		//Coloca o adapter ajeitado para contornar o bug de não chamar o listener na primeira vez.
		pager.setAdapter(AndroidBugsUtils.applyWorkaroundForBug27526(pager, adapter, detailFragmentManager));
        pager.addOnPageChangeListener(detailFragmentManager);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.entity_detail_pager_tabs);
        tabLayout.setupWithViewPager(pager);
		return view;
	}
	
	@Override
	protected void refreshContent() {
		//Atualiza todos os fragmentos criados.
		for (int i = 0; i < adapter.getCount(); i++) {
			EntityDetailFragment detailFragment = adapter.getFragment(i);
			if (detailFragment == null) {
				continue;
			}
			
			detailFragment.setEntityRecord(entityRecord);
		}
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		EntityDetailFragment detailFragment = getCurrentDetailFragment();
		if (detailFragment == null) {
			return;
		}
		
		if (hidden) {
			detailFragment.setMenuVisibility(false);
			detailFragment.setUserVisibleHint(false);
 		} else {
 			detailFragment.setMenuVisibility(true);
 			detailFragment.setUserVisibleHint(true);
 		}
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		EntityDetailFragment detailFragment = getCurrentDetailFragment();
		if (detailFragment == null || isHidden()) {
			return;
		}
		
		//Repassa para o fragmento filho, se houver, pq o Android não faz isto automaticamente.
		detailFragment.setMenuVisibility(menuVisible);
	}

	@Override
	public View getHeader() {
		EntityDetailFragment currentDetailFragment = getCurrentDetailFragment();
		if (currentDetailFragment == null) {
			return null;
		}
		
		return currentDetailFragment.getHeader();
	}

	@Override
	public ListView getFieldsList() {
		EntityDetailFragment currentDetailFragment = getCurrentDetailFragment();
		if (currentDetailFragment == null) {
			return null;
		}
		
		return currentDetailFragment.getFieldsList();
	}

	@Override
	public EntityListFragment getEntityList() {
		EntityDetailFragment currentDetailFragment = getCurrentDetailFragment();
		if (currentDetailFragment == null) {
			return null;
		}
		
		return currentDetailFragment.getEntityList();
	}
	
	@Override
	public void setOnContentChangeListener(OnContentChangeListener contentChangeListener) {
		super.setOnContentChangeListener(contentChangeListener);
		
		EntityDetailFragment currentDetailFragment = getCurrentDetailFragment();
		if (currentDetailFragment == null) {
			return;
		}
		currentDetailFragment.setOnContentChangeListener(contentChangeListener);
	}
	
	/**
	 * Inicializa o fragmento com os objetos necessários para o seu funcionamento.<br>
	 * 
	 * @param entityManager gerenciador de entidades da aplicação.
	 * @param configManager gerenacidor de configurações da aplicação.
	 * @param searchableManager gerenciador de Serachables da aplicação, utilizado na configuração da pesquisa no fragmento
	 * de lista de registros de relacionamento. <b>Parâmetro opcional.</b>
	 * @param entityDAO acessor dos dados da entidade.
	 * @param detailTabs configurações das abas de detalhes.
	 * @param maskManager gerenciador de máscaras utilizado na criação dos formatadores dos campos.
	 * @throws IllegalStateException se a inicialização for feita novamente.
	 */
	public void initialize(IEntityDataManager entityManager, IEntityUIConfigManager configManager, ISearchableManager searchableManager,
						   IEntityDAO entityDAO, IDetailTab[] detailTabs, MaskManager maskManager) {
		if (entityManager == null || configManager == null || entityDAO == null || detailTabs == null || detailTabs.length == 0 ||  maskManager == null) {
			throw new IllegalArgumentException("entityManager == null || configManager == null || entityDAO == null || detailTabs == null || detailTabs.length == 0 ||  maskManager == null");
		}
		initialize(entityDAO);
		
		this.entityManager = entityManager;
		this.configManager = configManager;
		this.searchableManager = searchableManager;
		this.detailTabs = detailTabs;
		this.maskManager = maskManager;
	}

    /**
     * Obtém o adapter utilizado na criação dos fragmentos das páginas.
     *
     * @return o adapter obtido.
     * @throws IllegalStateException se a View do fragmento (e consequentemente o adapter) não tiver sido criada ainda.
     */
    public EntityDetailPagerAdapter getPagerAdapter() {
        if (!isViewCreated()) {
            throw new IllegalStateException("View was not created yet.");
        }

        return adapter;
    }


    /*
	 * Métodos auxiliares
	 */
	
	private boolean isViewCreated() {
		return getView() != null;
	}
	
	private EntityDetailFragment getCurrentDetailFragment() {
		if (!isViewCreated()) {
			return null;
		}
		
		return detailFragmentManager.getCurrentDetailFragment();
	}
	

	/*
	 * Classes auxliares
	 */
	
	/**
	 * Gerenciador das trocas de fragmento no ViewPager. É responsável por mostrar as ações de ActionBar do fragmento atual, 
	 * esconder as do fragmento antigo e disponibilizar o fragmento atual correto.
	 */
	private class DetailFragmentChangeManager extends SimpleOnPageChangeListener {
		
		private int currentPosition = pager.getCurrentItem();
		
		@Override
		public void onPageSelected(int newPosition) {
			//Se ainda não foi startado, não há pq chamar o listener, já que o conteúdo só vai ser criado depois que o fragmento interno for startado.
			//Por isto, repassa o listener para ele, fazendo com que ele chame no momento correto. 
			if (isStarted()) {
				fireOnBeforeChangeContent();
			}
			
			if (currentPosition != newPosition) {
				//Remove as ações do fragmento antigo.
				EntityDetailFragment currentDetailFragment = getCurrentDetailFragment();
				if (currentDetailFragment != null) {
					currentDetailFragment.setMenuVisibility(false);
					currentDetailFragment.setUserVisibleHint(false);
					currentDetailFragment.setOnContentChangeListener(null);
				}
				currentPosition = newPosition;
			}
			
			//Executa as pendências para garantir que as views dos fragmentos estão criadas.
			getChildFragmentManager().executePendingTransactions();

			//Seta os listeners e as ações no fragmento novo.
			EntityDetailFragment newDetailFragment = getCurrentDetailFragment();
			newDetailFragment.setOnContentChangeListener(getOnContentChangeListener());
			if (!isHidden()) {
				//Só faz isto quando o fragmento não está escondido pq se não depois ele não atualiza as ações da ActionBar.
				newDetailFragment.setMenuVisibility(isMenuVisible());
				newDetailFragment.setUserVisibleHint(true);
			}
			
			if (isStarted()) {
				fireOnAfterChangeContent();
			}
		}
		
		EntityDetailFragment getCurrentDetailFragment() {
			return adapter.getFragment(currentPosition);
		}
	}
	
	/**
	 * Adapter de fragmentos do tipo {@link EntityDetailFragment}.<br>
	 */
	public final class EntityDetailPagerAdapter extends AbstractFragmentPagerAdapter<EntityDetailFragment> {

		public EntityDetailPagerAdapter() {
			super(getChildFragmentManager(), detailTabs.length);
		}
		
		@Override
		public int getCount() {
			return detailTabs.length;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return detailTabs[position].getTitle();
		}
		
		@Override
		protected EntityDetailFragment createFragment(int position) {
			EntityDetailFragment fragment = new EntityDetailFragment();
			//Repassa os argumentos para o fragmento interno.
			fragment.setArguments(getArguments());
			return fragment;
		}

		@Override
		protected void initializeFragment(EntityDetailFragment fragment, int position) {
			IDetailTab detailTab = detailTabs[position];
			
			//Inicialização do fragmento de detalhe.
			fragment.initialize(entityDAO, maskManager, getActivity(), detailTab.getHeader(), detailTab.getFields(), entityManager, configManager, searchableManager);
			fragment.setEntityRecord(entityRecord);
			//Os fragmentos filhos n precisam manter o registro pq este já manterá.
			fragment.setKeepRecord(false);
		}
	}
}
