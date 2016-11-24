package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingTab;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor.OnValueChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.FieldEditorFactory;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.util.Utils;
import br.com.zalem.ymir.client.android.widget.AbstractFragmentPagerAdapter;

/**
 * Paginador de fragmentos do tipo {@link EntityEditingFragment}. Mantém os fragmentos em um {@link ViewPager}.<br>
 * As páginas deste fragmento são configuradas através de um array de {@link IEditingTab} e demais configurações passadas
 * no método {@link #initialize(Context, IEntityMetadata, IEditingTab[], IEntityDataManager, IEntityUIConfigManager, MaskManager)}. Cada configuração
 * de aba resultará em um fragmento no paginador.<br>
 * <br>
 * O EntityEditingPagerFragment cria e mantém os editores de todos os fragmentos em memória, independente se os fragmentos
 * estão criados ou não. Desta forma, os valores dos editores são mantidos independente da paginação.<br>
 * Todos os métodos que envolvem os editores irão afetar todos eles, independente da página corrente.<br>
 *
 * @see EntityEditingFragment
 *
 * @author Thiago Gesser
 */
public final class EntityEditingPagerFragment extends AbstractEntityEditingFragment {
	
	private EntityEditingFragmentState[] fragmentsStates;
	
	private ViewPager pager;
	private EntityEditingPagerAdapter adapter;

	private Bundle savedEditorsState;
	
	@Override
    protected View onCreateThemedView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.entity_editing_pager_fragment, container, false);
		pager = (ViewPager) view.findViewById(R.id.entity_editing_pager);
        int pagesMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.pager_fragment_pages_margin);
        pager.setPageMargin(pagesMargin);

		adapter = new EntityEditingPagerAdapter();
		EditingFragmentChangeManager fragmentChangeManager = new EditingFragmentChangeManager();
		//Coloca o adapter ajeitado para contornar o bug de não chamar o listener na primeira vez.
		pager.setAdapter(AndroidBugsUtils.applyWorkaroundForBug27526(pager, adapter, fragmentChangeManager));
        //Contorno para o bug de trocar de página pelas abas.
        pager.addOnPageChangeListener(fragmentChangeManager);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.entity_editing_pager_tabs);
        tabLayout.setupWithViewPager(pager);
		
		savedEditorsState = savedInstanceState;
		if (isInitialized()) {
			restoreEditorsState();
		}

		return view;
	}

	@Override
	public void onStart() {
		//Este fragmento já atualiza os valores, retirando a necessidade dos fragmentos das páginas o fazerem.
		for (int i = 0; i < adapter.getCount(); i++) {
			EntityEditingFragment editingFragment = adapter.getFragment(i);
			if (editingFragment == null) {
				continue;
			}

			editingFragment.dontRefreshValuesOnStart();
		}

		super.onStart();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		EntityEditingFragment editingFragment = getCurrentEditingFragment();
		if (editingFragment == null) {
			return;
		}
		
		if (hidden) {
			editingFragment.setMenuVisibility(false);
			editingFragment.setUserVisibleHint(false);
 		} else {
 			editingFragment.setMenuVisibility(true);
 			editingFragment.setUserVisibleHint(true);
 		}
	}
	
	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		EntityEditingFragment editingFragment = getCurrentEditingFragment();
		if (editingFragment == null || isHidden()) {
			return;
		}
		
		//Repassa para o fragmento filho, se houver, pq o Android não faz isto automaticamente.
		editingFragment.setMenuVisibility(menuVisible);
	}

    @Override
    public List<AbstractFieldEditor<?>> getEditors() {
        checkInitialized();

        List<AbstractFieldEditor<?>> editors = new ArrayList<>();
        for (EntityEditingFragmentState fragmentState : fragmentsStates) {
            editors.addAll(Arrays.asList(fragmentState.getEditors()));
        }
        return editors;
    }

    @Override
	public boolean visitEditors(IFieldEditorVisitor visitor) {
		checkInitialized();

		for (EntityEditingFragmentState fragmentState : fragmentsStates) {
			AbstractFieldEditor<?>[] editors = fragmentState.getEditors();

			for (AbstractFieldEditor<?> editor : editors) {
				if (editor.accept(visitor)) {
					return true;
				}
			}
		}
		return false;
	}

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractFieldEditor<?>> T findEditor(String fieldName) {
        checkInitialized();

        for (EntityEditingFragmentState fragmentState : fragmentsStates) {
            AbstractFieldEditor<?>[] editors = fragmentState.getEditors();

            for (AbstractFieldEditor<?> editor : editors) {
                if (editor.getFieldName().equals(fieldName)) {
                    return (T) editor;
                }
            }
        }

        return null;
    }

    @Override
    public void scrollToEditor(String fieldName) {
        checkInitialized();

        for (int fragIndex = 0; fragIndex < fragmentsStates.length; fragIndex++) {
            EntityEditingFragmentState fragmentState = fragmentsStates[fragIndex];
            AbstractFieldEditor<?>[] editors = fragmentState.getEditors();

            for (AbstractFieldEditor<?> editor : editors) {
                if (editor.getFieldName().equals(fieldName)) {
                    //Vai até a página que possui o editor e depois scrolla até ele.
                    scrollToEditor(fragIndex, editor);
                    return;
                }
            }
        }

        throw new IllegalArgumentException(String.format("Editor for the field \"%s\" not found.", fieldName));
    }

    @Override
	public void loadValues(IEntityRecord record, boolean clearDirt) {
		checkInitialized();

        for (EntityEditingFragmentState fragmentState : fragmentsStates) {
			AbstractFieldEditor<?>[] editors = fragmentState.getEditors();
			
			for (AbstractFieldEditor<?> editor : editors) {
				editor.loadValue(record, clearDirt);
			}
		}
	}

	@Override
	public void storeValues(IEntityRecord record, boolean clearDirt) {
		checkInitialized();
		
		for (EntityEditingFragmentState fragmentState : fragmentsStates) {
			AbstractFieldEditor<?>[] editors = fragmentState.getEditors();
			
			for (AbstractFieldEditor<?> editor : editors) {
				editor.storeValue(record, clearDirt);
			}
		}
	}
	
	@Override
	public boolean isDirty() {
		checkInitialized();
		
		for (EntityEditingFragmentState fragmentState : fragmentsStates) {
			for (AbstractFieldEditor<?> editor : fragmentState.getEditors()) {
				if (editor.isDirty()) {
                    return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void setFieldEditorsValueChangeListener(OnValueChangeListener valueChangeListener) {
		checkInitialized();
		
		for (EntityEditingFragmentState fragmentState : fragmentsStates) {
			for (AbstractFieldEditor<?> editor : fragmentState.getEditors()) {
				editor.setOnValueChangeListener(valueChangeListener);
			}
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (!isInitialized()) {
			return;
		}
		
		for (EntityEditingFragmentState fragmentState : fragmentsStates) {
			AbstractFieldEditor<?>[] editors = fragmentState.getEditors();
			
			for (AbstractFieldEditor<?> editor : editors) {
				editor.saveState(outState);
			}
		}
	}
	
	/**
	 * Inicializa o fragmento com os objetos necessários para a criação dos editores, que serão repassados para os fragmentos
	 * de cada aba.
	 * 
	 * @param context contexto. É necessário neste momento porque o fragmento pode não estar ligado a Activity ainda, então
	 * o seu <code>getActivity</code> vai retornar <code>null</code>.
	 * @param entityMetadata metadados da entidade cujo os registros serão editados por este fragmento.
	 * @param editingTabs configuração das abas de edição
	 * @param entityManager gerenciador de entidades da aplicação.
	 * @param configManager gerenacidor de configurações da aplicação.
	 * @param maskManager gerenciador de máscaras utilizado na criação dos formatadores dos campos.
	 * @throws IllegalStateException se a inicialização for feita novamente.
	 */
	public void initialize(Context context, IEntityMetadata entityMetadata, IEditingTab[] editingTabs, IEntityDataManager entityManager, IEntityUIConfigManager configManager, MaskManager maskManager) {
		if (context == null || entityMetadata == null || editingTabs == null || editingTabs.length == 0 || configManager == null || maskManager == null) {
			throw new IllegalArgumentException("context == null || entityMetadata == null || editingTabs == null || editingTabs.length == 0 || configManager == null || maskManager == null");
		}
		if (isInitialized()) {
			throw new IllegalStateException("EntityEditingPagerFragment is already initialized.");
		}
		
		//Utiliza um fragmentManager adaptado para contornar o bug de estado inválido quando o getChildFragmentManager é chamado antes do fragmento ser atachado à Activity.
		FragmentManager fragmentManager = AndroidBugsUtils.applyWorkaroundForInvalidFragmentManagerStateBug(this);
		
		FieldEditorFactory editorFactory = new FieldEditorFactory(context, entityMetadata, entityManager, fragmentManager, configManager, maskManager);
		EntityEditingFragmentState[] fragmentsStates = new EntityEditingFragmentState[editingTabs.length];
		for (int i = 0; i < editingTabs.length; i++) {
			IEditingTab editingTab = editingTabs[i];
			IEditingFieldMapping[] fieldMappings = editingTab.getFields();
			AbstractFieldEditor<?>[] editors = editorFactory.createFieldsEditors(fieldMappings);
			fragmentsStates[i] = new EntityEditingFragmentState(editingTab.getTitle(), editors, editingTab.getLayout());
		}
		this.fragmentsStates = fragmentsStates;
		
		if (isViewCreated()) {
			restoreEditorsState();
		}
	}

    /**
     * Obtém o adapter utilizado na criação dos fragmentos das páginas.
     *
     * @return o adapter obtido.
     * @throws IllegalStateException se a View do fragmento (e consequentemente o adapter) não tiver sido criada ainda.
     */
    public EntityEditingPagerAdapter getPagerAdapter() {
        if (!isViewCreated()) {
            throw new IllegalStateException("View was not created yet.");
        }

        return adapter;
    }

	
	/*
	 * Métodos auxiliares
	 */
	
	private boolean isInitialized() {
		return fragmentsStates != null;
	}
	
	private void checkInitialized() {
		if (!isInitialized()) {
			throw new IllegalStateException("EntityEditingPagerFragment is not initialized yet.");
		}
	}

	private EntityEditingFragment getCurrentEditingFragment() {
		if (!isViewCreated()) {
			return null;
		}
		
		return adapter.getFragment(pager.getCurrentItem());
	}
	
	private void restoreEditorsState() {
		if (savedEditorsState == null) {
			return;
		}
		
		for (EntityEditingFragmentState fragmentState : fragmentsStates) {
			AbstractFieldEditor<?>[] editors = fragmentState.getEditors();
			
			for (AbstractFieldEditor<?> editor : editors) {
				editor.restoreState(savedEditorsState);
			}
		}
		refreshValues();
	}

    private void scrollToEditor(int fragIndex, final AbstractFieldEditor<?> editor) {
        if (pager.getCurrentItem() == fragIndex) {
            //Se já está na página correta, apenas scrolla até o editor.
            adapter.getFragment(fragIndex).scrollToEditor(editor);
            return;
        }

        //Desliga a animação de layout temporariamente para evitar conflito entre as animações.
        final ViewGroup rootView = (ViewGroup) getView();
        final LayoutTransition curLayoutTransition = rootView.getLayoutTransition();
        rootView.setLayoutTransition(null);

        //Scrolla até a página que contém o editor.
        pager.setCurrentItem(fragIndex);

        //Posta para garantir que a View do fragmento tenha sofrido layout e o scroll funcione corretamente.
        final EntityEditingFragment fragment = adapter.getFragment(fragIndex);
        pager.post(new Runnable() {
            @Override
            public void run() {
                fragment.scrollToEditor(editor);

                //Religa a animação de layout (se estava definida).
                rootView.setLayoutTransition(curLayoutTransition);
            }
        });
    }


	/*
	 * Classes auxliares
	 */
	
	/**
	 * Gerenciador das trocas de fragmento no ViewPager. É responsável por mostrar as ações de ActionBar do fragmento atual, 
	 * esconder as do fragmento antigo e disponibilizar o fragmento atual correto.
	 */
	private class EditingFragmentChangeManager extends SimpleOnPageChangeListener {
		
		private int currentPosition = pager.getCurrentItem();
		
		@Override
		public void onPageSelected(int newPosition) {
			if (currentPosition != newPosition) {
				Utils.hideSoftInput(getActivity());
				
				//Remove as ações do fragmento antigo.
				EntityEditingFragment currentDetailFragment = getCurrentEditingFragment();
				if (currentDetailFragment != null) {
					currentDetailFragment.setMenuVisibility(false);
					currentDetailFragment.setUserVisibleHint(false);
				}
				currentPosition = newPosition;
			}
			
			EntityEditingFragment newDetailFragment = getCurrentEditingFragment();
			if (!isHidden()) {
				//Só faz isto quando o fragmento não está escondido pq se não depois ele não atualiza as ações da ActionBar.
				newDetailFragment.setMenuVisibility(isMenuVisible());
				newDetailFragment.setUserVisibleHint(true);
			}
		}
		
		EntityEditingFragment getCurrentEditingFragment() {
			return adapter.getFragment(currentPosition);
		}
	}
	
	/**
	 * Adapter de fragmentos do tipo {@link EntityEditingFragment}.<br>
	 */
    public final class EntityEditingPagerAdapter extends AbstractFragmentPagerAdapter<EntityEditingFragment> {

		public EntityEditingPagerAdapter() {
			super(getChildFragmentManager(), fragmentsStates.length);
		}
		
		@Override
		public int getCount() {
			return fragmentsStates.length;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return fragmentsStates[position].getTitle();
		}
		
		@Override
		protected EntityEditingFragment createFragment(int position) {
			EntityEditingFragment fragment = new EntityEditingFragment();
			fragment.setArguments(getArguments());
			
			return fragment;
		}
		
		@Override
		protected void initializeFragment(EntityEditingFragment fragment, int position) {
			//Este fragmento irá salvar os estados de todos os editores, n sendo necessário que o fragmento filho faça isto.
			fragment.setSaveEditorsState(false);
			//Inicialização do fragmento de edição.
            EntityEditingFragmentState fragmentsState = fragmentsStates[position];
            fragment.initialize(getActivity(), fragmentsState.getEditors(), fragmentsState.getLayout());
		}
	}
	
	private static final class EntityEditingFragmentState { 
		
		private final String title;
		private final AbstractFieldEditor<?>[] editors;
        private final String layout;

        public EntityEditingFragmentState(String title, AbstractFieldEditor<?>[] editors, String layout) {
			this.title = title;
			this.editors = editors;
            this.layout = layout;
        }
		
		public AbstractFieldEditor<?>[] getEditors() {
			return editors;
		}
		
		public String getTitle() {
			return title;
		}

        public String getLayout() {
            return layout;
        }
    }
}
