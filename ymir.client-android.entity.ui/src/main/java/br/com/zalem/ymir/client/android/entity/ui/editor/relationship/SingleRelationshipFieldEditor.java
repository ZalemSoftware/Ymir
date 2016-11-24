package br.com.zalem.ymir.client.android.entity.ui.editor.relationship;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingPermissions;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.ITabbedListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractLabeledFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.layout.LayoutConfigAdapter;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * Editor de campo referente a um relacionamento singular da entidade.<br>
 * Os tipos de relacionamentos suportados são: {@link EntityRelationshipType#ASSOCIATION} e {@link EntityRelationshipType#COMPOSITION}.
 *
 * @author Thiago Gesser
 */
public final class SingleRelationshipFieldEditor extends AbstractLabeledFieldEditor<IEntityRecord> implements OnClickListener {

	private final LayoutConfigAdapter adapter;
	private final IEntityDAO entityDAO;
	private final EntityRelationshipType relationshipType;
	private ISingleRelationshipFieldEditorListener listener;
	
	private final IEditingPermissions localPermissions;
	private final IEditingPermissions dataSourcePermissions;
	private boolean canCreate;
	
	private View recordView;
	private View hintView;
	
	public SingleRelationshipFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
										 EntityRelationshipType relationshipType, IEntityMetadata relationshipEntity,
										 IEntityDataManager entityManager, IEntityUIConfigManager configManager, MaskManager maskManager) {
		super(fieldName, label, editable, hidden, virtual, help);
		this.relationshipType = relationshipType;
		
		String relationshipEntityName = relationshipEntity.getName();
		entityDAO = entityManager.getEntityDAO(relationshipEntityName);
		
		if (hidden) {
			//Estes atributos não são utilizados quando o editor está escondido (sem view).
			localPermissions = null;
			dataSourcePermissions = null;
			adapter = null;
		} else {
			IEntityConfig entityConfig = configManager.getEntityConfig(relationshipEntityName);
			//Utiliza a configuração de layout de lista/seleção da entidade alvo do relacionamento.
			if (entityConfig == null || (entityConfig.getList() == null && entityConfig.getSelection() == null)) {
				throw new IllegalArgumentException(String.format("Missing required configuration \"%s\" of entity %s.", "list or selection", relationshipEntityName));
			}
			//Prioriza a configuração de seleção pq ela tende a ser utilizada na busca do registro.
			ITabbedListDisplayConfig listDisplayConfig = entityConfig.getSelection() != null ? entityConfig.getSelection() : entityConfig.getList();
			ILayoutConfig<ListLayoutType> layoutConfig = listDisplayConfig.getLayout();
			if (layoutConfig == null) {
				//TODO por enquanto exige que a entidade referenciada possua a configuração do layout padrão de lista. No futuro poderá haver uma configuração específica para a representação da entidade em um campo de edição de relacionamento.
				throw new IllegalArgumentException(String.format("Missing required configuration \"%s\" of entity %s.", "list.layout", relationshipEntityName));
			}
			
			Context context = maskManager.getContext();
            EntityAttributeFormatter layoutFormatter = EntityAttributeFormatter.fromConfig(context, relationshipEntity, maskManager, layoutConfig.getFields());
			adapter = new LayoutConfigAdapter(context, layoutConfig, layoutFormatter, null);
			
			IEditingConfig editingConfig = entityConfig.getEditing();
			//Esta configuração n é necessária se o editor aponta para um relacionamento virtual, pois ele pode criar/editar à vontade.
			if (!virtual && editingConfig != null) {
				localPermissions = editingConfig.getLocalPermissions();
				dataSourcePermissions = editingConfig.getDataSourcePermissions();
			} else {
				localPermissions = null;
				dataSourcePermissions = null;
			}
		}
	}

    @Override
    protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = super.createView(inflater, label, editable, parent);

		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.entity_field_editor_single_relationship, parent, false);
		layout.setOnClickListener(this);

		hintView = layout.findViewById(R.id.entity_field_editor_single_relationship_hint_container);
		TextView textView = (TextView) hintView.findViewById(R.id.entity_field_editor_single_relationship_hint);
		textView.setHint(label);

        //Define um inflater baseado no contexto da View pq ela pode estar influenciada por outro tema.
        adapter.setInflater(LayoutInflater.from(layout.getContext()));
		recordView = adapter.createAndHoldView(layout);
		layout.addView(recordView);

        rootView.addView(layout, 1);
        return rootView;
	}

    @Override
    protected void destroyView() {
        super.destroyView();

		hintView = null;
		recordView = null;
	}

    @Override
    protected void refreshView(IEntityRecord newValue) {
        super.refreshView(newValue);

        if (newValue == null) {
            recordView.setVisibility(View.INVISIBLE);
            hintView.setVisibility(View.VISIBLE);
        } else {
            adapter.setHeldViewValues(newValue, recordView);
            hintView.setVisibility(View.GONE);
            recordView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View getView() {
        return recordView;
    }

    @Override
	protected IEntityRecord internalLoadValue(IEntityRecord record, String fieldName) {
		if (!isHidden()) {
			//Define se pode criar baseado nas permissões da entidade alvo do relacionamento, dependendo se o registro fonte é local ou da fonte dados.
			IEditingPermissions permissions = getRecordPermissions(record);
			canCreate = permissions != null && permissions.canCreate();
		}

		return record.getRelationshipValue(fieldName);
	}

	@Override
	protected void internalStoreValue(IEntityRecord record, String fieldName, IEntityRecord value) {
		record.setRelationshipValue(fieldName, value);
	}

	@Override
	protected IEntityRecord internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		Parcelable recordSavedState = bundle.getParcelable(key);
		if (recordSavedState == null) {
			return null;
		}
		return entityDAO.fromSavedState(recordSavedState);
	}

	@Override
	protected void internalSaveState(Bundle bundle, String key, IEntityRecord value) {
        super.internalSaveState(bundle, key, value);

		Parcelable recordSavedState = null;
		if (value != null) {
			recordSavedState = entityDAO.toSavedState(value);
		}
		bundle.putParcelable(key, recordSavedState);
	}

    @Override
    protected void tintError(boolean hasError, boolean hadError) {
        //Não pinta este editor quando houver erro.
    }

	@Override
	public boolean accept(IFieldEditorVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public void onClick(View v) {
		IEntityRecord record = getValue();
		if (!isEditable()) {
			if (record != null) {
				detailRecord();
			}
			return;
		}
		
		switch (relationshipType) {
			case ASSOCIATION:
				selectRecord();
				break;
				
			case COMPOSITION:
				//Se for virtual, pode criar / editor sem a necessidade de permissões. 
				if (record == null) {
					if (canCreate || isVirtual()) {
						createRecord();
					}
				} else {
					if (isVirtual()) {
						editRecord();
						return;
					}
					
					//Só permite a edição se a entidade tiver permissão para isto.
					IEditingPermissions permissions = getRecordPermissions(record);
					if (permissions != null && permissions.canUpdate()) {
						editRecord();
					} else {
						detailRecord();
					}
				}
				break;

            case ASSOCIATION_ARRAY:
            case COMPOSITION_ARRAY:
                break;
				
			default:
				throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationshipType);
		}
	}

	/**
	 * Atualiza o valor do editor com a fonte de dados.
	 */
	public void refreshValue() {
		IEntityRecord value = getValue();
		if (value != null) {
			if (entityDAO.refresh(value)) {
				tryRefreshView(value);
			} else {
				//Se foi excluído, seta null.
				setValue(null);
			}
		}
	}
	
	/**
	 * Define o listener de ações efetuadas pelo editor.
	 * 
	 * @param listener o listener.
	 */
	public void setEditorListener(ISingleRelationshipFieldEditorListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Obtém o listener definido para as ações efetuadas pelo editor.
	 * 
	 * @return o listener obtido ou <code>null</code> caso nenhum listener tenha sido definido.
	 */
	public ISingleRelationshipFieldEditorListener getEditorListener() {
		return listener;
	}

	/**
	 * Obtém os metadados da entidade referenciada pelo relacionamento que este editor representa.
	 * 
	 * @return os metadados da entidade obtidos.
	 */
	public IEntityMetadata getRelationshipEntity() {
		return entityDAO.getEntityMetadata();
	}
	
	/**
	 * Obtém o tipo do relacionamento que este editor representa.
	 *  
	 * @return o tipo de relacionamento obtido.
	 */
	public EntityRelationshipType getRelationshipType() {
		return relationshipType;
	}
	
	
	/*
	 * Métodos auxiliares
	 */
	
	private IEditingPermissions getRecordPermissions(IEntityRecord record) {
		if (record.isLocal()) {
			return localPermissions;
		}
		
		return dataSourcePermissions;
	}
	
	private void selectRecord() {
		//Se o listener retornou true, significa que ele mesmo tratou a criação.
		if (listener != null && listener.onRecordSelection(this)) { 
			return;
		}
		
		throw new PendingFeatureException("Default SingleRelationshipFieldEditor record selection");
	}
	
	private void createRecord() {
		//Se o listener retornou true, significa que ele mesmo tratou a criação.
		if (listener != null && listener.onRecordCreation(this)) { 
			return;
		}
		
		throw new PendingFeatureException("Default SingleRelationshipFieldEditor record creation");
	}
	
	private void editRecord() {
		//Se o listener retornou true, significa que ele mesmo tratou a edição.
		if (listener != null && listener.onRecordEditing(this)) { 
			return;
		}
		
		throw new PendingFeatureException("Default SingleRelationshipFieldEditor record editing");
	}
	
	private void detailRecord() {
		//Se o listener retornou true, significa que ele mesmo tratou o detalhamento.
		if (listener != null && listener.onRecordDetailing(this)) { 
			return;
		}
		
		throw new PendingFeatureException("Default SingleRelationshipFieldEditor record detailing");
	}
	
	/**
	 * Listener de ações efetuadas pelo editor de relacionamentos singulares.<br>
	 * Todos os métodos possuem um retorno booleano que indica se o próprio listener vai tratar a ação ou se vai
	 * deixar o tratamento para o editor.
	 * 
	 * @author Thiago Gesser
	 */
	public interface ISingleRelationshipFieldEditorListener {
		
		/**
		 * Chamado quando um registro precisa ser selecionado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordSelection(SingleRelationshipFieldEditor editor);
		
		/**
		 * Chamado quando um registro precisa ser criado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordCreation(SingleRelationshipFieldEditor editor);
		
		/**
		 * Chamado quando um registro precisa ser editado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordEditing(SingleRelationshipFieldEditor editor);
		
		/**
		 * Chamado quando um registro precisa ser detalhado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordDetailing(SingleRelationshipFieldEditor editor);
		
		/**
		 * Chamado quando um registro precisa ser removido.
		 *  
		 * @param editor o editor fonte da ação.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordRemoval(SingleRelationshipFieldEditor editor);
	}
}
