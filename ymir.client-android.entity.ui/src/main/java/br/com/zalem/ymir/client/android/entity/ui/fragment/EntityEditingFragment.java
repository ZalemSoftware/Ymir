package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.Arrays;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor.OnValueChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.FieldEditorFactory;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.editor.adapter.AbstractFieldEditorAdapter;
import br.com.zalem.ymir.client.android.entity.ui.editor.adapter.AbstractFieldEditorAdapter.IFieldEditorAdapterListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.adapter.DefaultFieldEditorAdapter;
import br.com.zalem.ymir.client.android.entity.ui.editor.adapter.LayoutFieldEditorAdapter;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.MultipleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;

/**
 * O EntityEditingFragment provê a edição de determinados campos (atributos ou relacionamentos) de um registro de entidade
 * através de editores ({@link AbstractFieldEditor}) específicos para cada tipo de campo. O fragmento funciona a partir
 * de um {@link AbstractFieldEditorAdapter}, que dispõe os editores e é responsável pela criação das Views.<br>
 * <br>
 * A configuração dos campos de edição pode ser feita através de {@link IEditingFieldMapping}, que são utilizados na criação
 * dos editores pelo método de inicialização {@link #initialize(Context, IEntityMetadata, IEditingFieldMapping[], IEntityDataManager, IEntityUIConfigManager, MaskManager, String)}.
 * Também é possível inicializar o fragmento diretamente com as instâncias de editores através do método {@link #initialize(Context, AbstractFieldEditor[], String)}.
 * Estes dois métodos de inicialização utilizam o {@link DefaultFieldEditorAdapter} como adapter. Entretanto, é possível
 * inicializar o fragmento com um adapter próprio através do método {@link #initialize(AbstractFieldEditorAdapter)}.<br>
 * <br>   
 * O carregamento dos valores de um registro para os editores pode ser feito através do método {@link #loadValues(IEntityRecord, boolean)}.
 * Os valores dos editores podem ser armazenados em um registro através do método {@link #storeValues(IEntityRecord, boolean)}.<br>
 * <br>
 * É possível executar configurações ou qualquer outra operação nos editores através de um visitador {@link IFieldEditorVisitor}.
 * Visitadores podem ser aplicados nos editores através do método {@link #visitEditors(IFieldEditorVisitor)}.
 *
 * @author Thiago Gesser
 */
public final class EntityEditingFragment extends AbstractEntityEditingFragment {

	private MultipleRelationshipFieldEditor uniqueEditor;
	private boolean saveEditorsState = true;
	private Bundle savedEditorsState;
	
	private ViewGroup editorsContainer;
	private AbstractFieldEditorAdapter editorAdapter;
	
	@Override
    protected View onCreateThemedView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.entity_editing_fragment, container, false);
        editorsContainer = (ViewGroup) view.findViewById(R.id.entity_editing_editors_container);
        savedEditorsState = savedInstanceState;

		if (isInitialized()) {
			restoreEditorsState();
			createEditorsViews();
		}
		return view;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		if (isInitialized()) {
            clearEditorsViews();
		}
	}

    @Override
    public List<AbstractFieldEditor<?>> getEditors() {
        checkInitialized();

        return editorAdapter.getEditors();
    }

    @Override
	public boolean visitEditors(IFieldEditorVisitor visitor) {
		checkInitialized();

		for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
			if (editor.accept(visitor)) {
				return true;
			}
		}
		return false;
	}

    @Override
    @SuppressWarnings("unchecked")
    public <T extends AbstractFieldEditor<?>> T findEditor(String fieldName) {
        checkInitialized();

        for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
            if (editor.getFieldName().equals(fieldName)) {
                return (T) editor;
            }
        }
        return null;
    }

    @Override
    public void scrollToEditor(String fieldName) {
        checkInitialized();
        checkViewCreated();

        AbstractFieldEditor<?> editor = findEditor(fieldName);
        if (editor == null) {
            throw new IllegalArgumentException(String.format("Editor for the field \"%s\" not found.", fieldName));
        }

        scrollToEditor(editor);
    }

    @Override
	public void loadValues(IEntityRecord record, boolean clearDirt) {
		checkInitialized();
		
		for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
            editor.loadValue(record, clearDirt);
		}
	}

	@Override
	public void storeValues(IEntityRecord record, boolean clearDirt) {
		checkInitialized();
		
		for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
			editor.storeValue(record, clearDirt);
		}
	}
	
	@Override
	public boolean isDirty() {
		checkInitialized();
		
		for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
            if (editor.isDirty()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void setFieldEditorsValueChangeListener(OnValueChangeListener valueChangeListener) {
        checkInitialized();
		
		for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
			editor.setOnValueChangeListener(valueChangeListener);
		}
	}

	/**
	 * Inicializa o fragmento com os objetos necessários para a criação dos editores, que serão repassados para o método
	 * {@link #initialize(Context, AbstractFieldEditor[], String)}.
	 * 
	 * @param context contexto. É necessário neste momento porque o fragmento pode não estar ligado a Activity ainda, então
	 * o seu <code>getActivity</code> vai retornar <code>null</code>.
	 * @param entityMetadata metadados da entidade cujo os registros serão editados por este fragmento.
	 * @param fieldsMappings configuração dos campos de edição.
	 * @param entityManager gerenciador de entidades da aplicação.
	 * @param configManager gerenacidor de configurações da aplicação.
	 * @param maskManager gerenciador de máscaras utilizado na criação dos formatadores dos campos.
     * @param layoutName nome de recurso de layout que será utilizado para a configuração de layout customizado para os editores ou <code>null</code>
     *               se o layout padrão for utilizado.
	 * @throws IllegalStateException se a inicialização for feita novamente.
	 */
	public void initialize(Context context, IEntityMetadata entityMetadata,  IEditingFieldMapping[] fieldsMappings,
						   IEntityDataManager entityManager, IEntityUIConfigManager configManager, MaskManager maskManager, String layoutName) {
		if (context == null || entityMetadata == null || fieldsMappings == null || configManager == null || maskManager == null) {
			throw new NullPointerException("context == null || entityMetadata == null || fieldsMappings == null || configManager == null || maskManager == null");
		}
		
		//Utiliza um fragmentManager adaptado para contornar o bug de estado inválido quando o getChildFragmentManager é chamado antes do fragmento ser atachado à Activity.
		FragmentManager fragmentManager = AndroidBugsUtils.applyWorkaroundForInvalidFragmentManagerStateBug(this);
		
		FieldEditorFactory editorFactory = new FieldEditorFactory(context, entityMetadata, entityManager, fragmentManager, configManager, maskManager);
		initialize(context, editorFactory.createFieldsEditors(fieldsMappings), layoutName);
	}
	
	/**
	 * Inicializa o fragmento com os editores que serão utilizados na edição dos campos do registro. Para isto, é criado
	 * um {@link DefaultFieldEditorAdapter} e repassado ao método {@link #initialize(AbstractFieldEditorAdapter)}.
	 * O DefaultFieldEditorAdapter cria uma View para cada editor visível.
	 *
     * @param context contexto. É necessário neste momento porque o fragmento pode não estar ligado a Activity ainda, então
     * o seu <code>getActivity</code> vai retornar <code>null</code>.
	 * @param editors editores.
     * @param layoutName nome de recurso de layout que será utilizado para a configuração de layout customizado para os editores ou <code>null</code>
     *               se o layout padrão for utilizado.
	 */
	public void initialize(Context context, AbstractFieldEditor<?>[] editors, String layoutName) {
		if (editors == null || editors.length == 0) {
			throw new IllegalArgumentException("editors == null || editors.length == 0");
		}

        List<AbstractFieldEditor<?>> editorsList = Arrays.asList(editors);
        AbstractFieldEditorAdapter adapter;
        if (layoutName == null) {
            adapter = new DefaultFieldEditorAdapter(context, editorsList);
        } else {
            adapter = new LayoutFieldEditorAdapter(context, editorsList, layoutName);
        }
        initialize(adapter);
	}
	
	/**
	 * Inicializa o fragmento com o adapter de editores. O adapter dispõe os editores que serão utilizados na edição dos
	 * campos do registro e é responsável pela criação de suas Views.
	 * 
	 * @param editorAdapter adapter de editores.
	 */
	public void initialize(AbstractFieldEditorAdapter editorAdapter) {
		if (editorAdapter == null || editorAdapter.getCount() == 0) {
			throw new IllegalArgumentException("editorAdapter == null || editorAdapter.getCount() == 0");
		}
		if (isInitialized()) {
			throw new IllegalStateException("EntityEditingFragment is already initialized.");
		}
		this.editorAdapter = editorAdapter;
		
		editorAdapter.addListener(new FieldEditorAdapterObserver());
		onEditorsChanged(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		//Só libera a ação de adicionar se for editável.
		if (uniqueEditor != null && uniqueEditor.isEditable() && uniqueEditor.canCreate()) {
            inflater.inflate(R.menu.entity_editing_fragment_multiple_relationship_editor_actions, menu);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_add) {
			uniqueEditor.onAddRecord();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (!saveEditorsState) {
			return;
		}
		
		for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
			editor.saveState(outState);
		}
	}
	
	/**
	 * Obtem o adapter de editores do fragmento.
	 *
	 * @return o adapter obtido ou <code>null</code> se o fragmento não foi inicializado
	 */
	public AbstractFieldEditorAdapter getEditorAdapter() {
		return editorAdapter;
	}


    /**
     * Define se os estados dos editores devem ser salvos e recuperados por este fragmento.
     *
     * @param saveEditorsState <code>true</code> se os estados devem ser salvos e <code>false</code> caso contrário.
     */
    void setSaveEditorsState(boolean saveEditorsState) {
        this.saveEditorsState = saveEditorsState;
    }

    void scrollToEditor(AbstractFieldEditor<?> editor) {
        //Se é o editor único, ele já está sendo mostrado.
        if (uniqueEditor != null || editor.isHidden()) {
            return;
        }

        View rootView = editorsContainer.getChildAt(0);
        View editorView = editor.getView();
        //Se a View do editor já é a root, ela já sendo mostrada
        if (editorView == rootView) {
            return;
        }

        //Obtém a posiçao da View do editor em relação a rootView.
        int scrollPosition = -getResources().getDimensionPixelSize(R.dimen.entity_editing_scroll_to_margin);
        for (;;) {
            scrollPosition += editorView.getTop();

            ViewParent viewParent = editorView.getParent();
            if (viewParent == null) {
                return;
            }
            if (viewParent == rootView) {
                break;
            }

            editorView = (View) viewParent;
        }

        //Scrolla até a View do editor.
        rootView.scrollTo(0, scrollPosition);
    }

	/*
	 * Métodos auxiliares
	 */

	private boolean isInitialized() {
		return editorAdapter != null;
	}

	private void checkInitialized() {
		if (!isInitialized()) {
			throw new IllegalStateException("EntityEditingFragment is not initialized yet.");
		}
	}

	private void onEditorsChanged(boolean restoreEditorsState) {
		//Se for apenas um editor de relacionamento múltiplo, esconde seu header e coloca a ação de adicionar na ActionBar.
        uniqueEditor = null;
		if (editorAdapter.getCount() == 1) {
			AbstractFieldEditor<?> editor = editorAdapter.getEditor(0);
			if (editor instanceof MultipleRelationshipFieldEditor) {
				uniqueEditor = (MultipleRelationshipFieldEditor) editor;
				uniqueEditor.setHeaderHidden(true);
			}
		}
		setHasOptionsMenu(uniqueEditor != null && uniqueEditor.isEditable() && !uniqueEditor.isHidden() && uniqueEditor.canCreate());
		
		if (isViewCreated()) {
			if (restoreEditorsState) {
				restoreEditorsState();
			}
			createEditorsViews();
		}
	}
	
	private void createEditorsViews() {
		editorsContainer.removeAllViews();
        clearEditorsViews();
		LayoutInflater inflater = getThemedLayoutInflater();

		//Se possui um editor único de relacionamento múltiplo, usa a View proveniente dele diretamente.
		if (uniqueEditor != null) {
            editorsContainer.addView(uniqueEditor.onCreateView(inflater, editorsContainer));
			return;
		}

        //Cria e adiciona a views dos editores.
        View editorsView = editorAdapter.getView(inflater, editorsContainer);
        editorsContainer.addView(editorsView);
	}

    private void clearEditorsViews() {
        for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
            editor.onDestroyView();
        }
    }
	
	private void restoreEditorsState() {
		if (!saveEditorsState || savedEditorsState == null) {
			return;
		}
		
		for (AbstractFieldEditor<?> editor : editorAdapter.getEditors()) {
			editor.restoreState(savedEditorsState);
		}
		refreshValues();
	}
	
	
	/*
	 * Classes auxiliares 
	 */
	
	/**
	 * Listener de alterações nos editores do adapter que manda atualizar as Views sendo mostradas.
	 */
	private final class FieldEditorAdapterObserver implements IFieldEditorAdapterListener {
        @Override
        public void onEditorsChanged() {
            EntityEditingFragment.this.onEditorsChanged(false);
        }
    }

}