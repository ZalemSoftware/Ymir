package br.com.zalem.ymir.client.android.entity.ui.editor.relationship;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.EnhancedRecyclerView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractHelperFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.IEntityRecordListActionProvider;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.ListLayoutConfigViewHolder;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * Editor de campo referente a um relacionamento múltiplo da entidade.<br>
 * Os tipos de relacionamentos suportados são: {@link EntityRelationshipType#ASSOCIATION_ARRAY} e {@link EntityRelationshipType#COMPOSITION_ARRAY}.
 *
 * @author Thiago Gesser
 */
public final class MultipleRelationshipFieldEditor extends AbstractHelperFieldEditor<List<IEntityRecord>> implements IEntityRecordListActionProvider {

	private static final String SAVED_CAN_CREATE = ".canCreate";
	private static final String SAVED_RECORDS_ERRORS = ".recordsErrors";

	private final ILayoutConfig<ListLayoutType> layoutConfig;
	private final EntityAttributeFormatter layoutFormatter;
	private final EntityRelationshipType relationshipType;
	private final IEntityDAO entityDAO;
    private final Map<IEntityRecord, String> recordsErrors;

	private final IEditingPermissions localPermissions;
	private final IEditingPermissions dataSourcePermissions;
	private boolean canCreate = true;

	private boolean headerHidden;
	private EnhancedRecyclerView entityList;
	private MultipleRelationshipListAdapter adapter;
	private IMultipleRelationshipFieldEditorListener listener;

	public MultipleRelationshipFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
										  EntityRelationshipType relationshipType, IEntityMetadata relationshipEntity,
										  IEntityDataManager entityManager, IEntityUIConfigManager configManager, MaskManager maskManager) {
		super(fieldName, label, editable, hidden, virtual, help);
        recordsErrors = new HashMap<>();

		this.relationshipType = relationshipType;
		String relationshipEntityName = relationshipEntity.getName();
		entityDAO = entityManager.getEntityDAO(relationshipEntityName);
		
		if (hidden) {
			//Estes atributos não são utilizados quando o editor está escondido (sem view).
			localPermissions = null;
			dataSourcePermissions = null;
			layoutConfig = null;
			layoutFormatter = null;
		} else {
			IEntityConfig entityConfig = configManager.getEntityConfig(relationshipEntityName);
			//Utiliza a configuração de layout de lista/seleção da entidade alvo do relacionamento.
			if (entityConfig == null || (entityConfig.getList() == null && entityConfig.getSelection() == null)) {
				throw new IllegalArgumentException(String.format("Missing required configuration \"%s\" of entity %s.", "list or selection", relationshipEntityName));
			}
			//Prioriza a configuração de seleção pq ela tende a ser utilizada na busca do registro.
			ITabbedListDisplayConfig listDisplayConfig = entityConfig.getSelection() != null ? entityConfig.getSelection() : entityConfig.getList();
			layoutConfig = listDisplayConfig.getLayout();
			if (layoutConfig == null) {
				//TODO por enquanto exige que a entidade referenciada possua a configuração do layout padrão de lista. No futuro poderá haver uma configuração específica para a representação da entidade em um campo de edição de relacionamento.
				throw new IllegalArgumentException(String.format("Missing required configuration \"%s\" of entity %s.", "list.layout", relationshipEntityName));
			}
			
			//Monta o formatador de atributos para o layout da entidade alvo.
			layoutFormatter = EntityAttributeFormatter.fromConfig(maskManager.getContext(), relationshipEntity, maskManager, layoutConfig.getFields());

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
        rootView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		if (!headerHidden) {
			throw new PendingFeatureException("MultipleRelationshipFieldEditor's header");
		}
		
		View layout = inflater.inflate(R.layout.entity_field_editor_multiple_relationship, parent, false);
		entityList = (EnhancedRecyclerView) layout.findViewById(R.id.entity_field_editor_multiple_relationship_list);
		entityList.setEmptyView(layout.findViewById(R.id.entity_field_editor_multiple_relationship_empty_view));

        //Passa o contexto do entityList pois ele pode estar influenciado por outro tema.
        ListLayoutConfigAdapter layoutConfigAdapter = new ListLayoutConfigAdapter(entityList.getContext(), layoutConfig, layoutFormatter);
		//Configura a ação de remoção nos registros.
		if (editable) {
            layoutConfigAdapter.setActionProvider(this);
		}
        //Define o adapter de layout configurado, englobado por um adapter do próprio editor que controla a mensagem de erro dos registros.
        adapter = new MultipleRelationshipListAdapter(layoutConfigAdapter);
		entityList.setAdapter(adapter);

        rootView.addView(layout);
        return rootView;
	}

    @Override
    protected TextView createHelperTextView(LayoutInflater inflater, ViewGroup parent) {
        //Utiliza um Helper Text com mais margens, posicionado acima da lista.
        return (TextView) inflater.inflate(R.layout.entity_field_editor_multiple_relationship_helper_text, parent, false);
    }

    @Override
    protected void tintError(boolean hasError, boolean hadError) {
        //Não pinta este editor quando houver erro.
    }

    @Override
    protected void destroyView() {
        super.destroyView();

        entityList = null;
        adapter = null;
    }

	@Override
	protected void refreshView(List<IEntityRecord> newValue) {
        adapter.setRecords(newValue);
	}

    @Override
    public EnhancedRecyclerView getView() {
        return entityList;
    }

    @Override
	protected List<IEntityRecord> internalLoadValue(IEntityRecord record, String fieldName) {
		List<IEntityRecord> ret = null;
		IEntityRecord[] relRecords = record.getRelationshipArrayValue(fieldName);
		if (relRecords != null && relRecords.length > 0) {
			ret = new ArrayList<>();
            Collections.addAll(ret, relRecords);
		}

		if (!isHidden()) {
			//Define se pode criar baseado nas permissões da entidade alvo do relacionamento, dependendo se o registro fonte é local ou da fonte dados.
			IEditingPermissions permissions = getRecordPermissions(record);
			canCreate = permissions != null && permissions.canCreate();
		}

		return ret;
	}

	@Override
	protected void internalStoreValue(IEntityRecord record, String fieldName, List<IEntityRecord> value) {
		IEntityRecord[] records = null;
		if (value != null) {
			records = value.toArray(new IEntityRecord[value.size()]);
		}

		record.setRelationshipArrayValue(fieldName, records);
	}

	@Override
	protected List<IEntityRecord> internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		canCreate = bundle.getBoolean(key + SAVED_CAN_CREATE);

        clearRecordsErrors(true);
		Parcelable[] recordStates = bundle.getParcelableArray(key);
		if (recordStates == null) {
			return null;
		}

		List<IEntityRecord> records = new ArrayList<>(recordStates.length);
        String[] errors = bundle.getStringArray(key + SAVED_RECORDS_ERRORS);
        for (int i = 0; i < recordStates.length; i++)  {
		    Parcelable recordState = recordStates[i];
			IEntityRecord record = entityDAO.fromSavedState(recordState);
			records.add(record);

            if (errors != null) {
                String error = errors[i];
                if (error != null) {
                    recordsErrors.put(record, error);
                }
            }
		}

		return records;
	}

	@Override
	protected void internalSaveState(Bundle bundle, String key, List<IEntityRecord> value) {
        super.internalSaveState(bundle, key, value);

		Parcelable[] recordsStates = null;
        String[] errors = null;
		if (value != null) {
            int numberOfRecords = value.size();
            recordsStates = new Parcelable[numberOfRecords];
            if (!recordsErrors.isEmpty()) {
                errors = new String[numberOfRecords];
            }
			for (int i = 0; i < recordsStates.length; i++)  {
                IEntityRecord record = value.get(i);
                recordsStates[i] = entityDAO.toSavedState(record);

                if (errors != null) {
                    String error = recordsErrors.get(record);
                    if (error != null) {
                        errors[i] = error;
                    }
                }
			}
		}
		bundle.putParcelableArray(key, recordsStates);
		bundle.putBoolean(key + SAVED_CAN_CREATE, canCreate);
        bundle.putStringArray(key + SAVED_RECORDS_ERRORS, errors);
	}

    @Override
    public boolean accept(IFieldEditorVisitor visitor) {
        return visitor.visit(this);
    }

	@Override
	public List<IEntityRecord> getValue() {
		List<IEntityRecord> value = super.getValue();
		if (value == null) {
			return null;
		}
		return Collections.unmodifiableList(value);
    }

    @Override
    public void setValue(List<IEntityRecord> value) {
        super.setValue(value);

        clearRecordsErrors(true);
    }

	
	/*
	 * Métodos de tratamento de ações do usuário nos registros. 
	 */

    @Override
    public void onCreateRecordActionMenu(YmirMenu menu, YmirMenuInflater menuInflater) {
        menuInflater.inflate(R.xml.entity_field_editor_multiple_relationship_record_actions, menu);
    }

    @Override
    public void onRecordActionItemSelected(IEntityRecord record, YmirMenuItem item) {
        if (item.getId() == R.id.record_action_remove) {
            internalRemoveRecord(record);
        }
    }

    @Override
    public boolean isRecordActionItemAvailable(IEntityRecord record, YmirMenuItem item) {
        if (item.getId() != R.id.record_action_remove) {
            return false;
        }

        if (isVirtual()) {
            return true;
        }

        IEditingPermissions permissions = getRecordPermissions(record);
        return permissions != null && permissions.canDelete();
    }


	/*
	 * Métodos de manipulação dos registros
	 */

    /**
     * Chamado quando o usuário disparou a ação de adicionar um registro neste editor.
     *
     * @throws IllegalStateException se o editor não é editável.
     */
    public void onAddRecord() {
        if (!isEditable()) {
            throw new IllegalStateException("This field is not editable");
        }

        //Se for apenas associação, seleciona um registro já existente. Se for composição, cria um novo registro.
        switch (relationshipType) {
            case ASSOCIATION_ARRAY:
                selectRecord();
                break;

            case COMPOSITION_ARRAY:
                createRecord();
                break;

            case COMPOSITION:
            case ASSOCIATION:
                break;

            default:
                throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationshipType);
        }
    }

	/**
	 * Adiciona um registro como último na lista de registros do editor.
	 * 
	 * @param record o registro que será adicionado.
	 */
	public void addRecord(IEntityRecord record) {
		if (record == null) {
			throw new NullPointerException("record == null");
		}

		List<IEntityRecord> recordsList = super.getValue();
		if (recordsList == null) {
			recordsList = new ArrayList<>();
			recordsList.add(record);
			setValue(recordsList);
			return;
		}
		
		recordsList.add(record);
		notifyValueChanged();

        if (isViewCreated()) {
            int position = recordsList.size() - 1;
            adapter.notifyItemInserted(position);

			//Move o scroll para o registro adicionado.
            entityList.scrollToPosition(position);
		}
	}
	
	/**
	 * Remove um registro presente na lista de registros do editor.
	 * 
	 * @param record o registro que será removido.
	 * @throws IllegalArgumentException se o registro não estava presente.
	 */
	public void removeRecord(IEntityRecord record) {
        removeRecord(getRecordPosition(record));
    }

    /**
	 * Remove um registro presente na lista de registros do editor.
	 * 
	 * @param position a posição do registro que será removido.
	 * @throws IndexOutOfBoundsException se a posição não corresponde a uma posição existente da lista de registros.
	 */
	public void removeRecord(int position) {
		List<IEntityRecord> recordsList = getRecordsList();
        IEntityRecord record = recordsList.remove(position);

		notifyRecordRemoval(recordsList, record, position);
    }
	
	/**
	 * Substitui o registro contido na posição da lista de registros do editor por um outro. 
	 * 
	 * @param position a posição cujo o registro será substituído.
	 * @param record o novo registro que ocupará a posição.
	 * @throws IndexOutOfBoundsException se a posição não corresponde a uma posição existente da lista de registros.
	 */
	public void replaceRecord(int position, IEntityRecord record) {
		List<IEntityRecord> recordsList = getRecordsList();

        recordsList.set(position, record);
        recordsErrors.remove(record);
		notifyValueChanged();

		if (isViewCreated()) {
			adapter.notifyItemChanged(position);
			//Move o scroll para o registro alterado.
			entityList.scrollToPosition(position);
		}
	}
	
	/**
	 * Obtém o registro contido na posição da lista de registros do editor.
	 * 
	 * @param position a posição cujo o registro será obtido.
	 * @throws IndexOutOfBoundsException se a posição não corresponde a uma posição existente da lista de registros.
	 */
	public IEntityRecord getRecord(int position) {
		List<IEntityRecord> recordsList = getRecordsList();

        return recordsList.get(position);
    }
	
	/**
	 * Obtém o número de registros contidos na lista de registros do editor.
	 * 
	 * @return o número de registros obtido.
	 */
	public int getSize() {
		List<IEntityRecord> recordsList = super.getValue();
		if (recordsList == null) {
			return 0;
		}
		return recordsList.size();
	}

	/**
	 * Atualiza os valores do editor com a fonte de dados.
	 */
	public void refreshValues() {
		List<IEntityRecord> values = super.getValue();
		if (values != null && !values.isEmpty()) {
			int originalSize = values.size();
			for (Iterator<IEntityRecord> iter = values.iterator(); iter.hasNext();) {
				IEntityRecord value = iter.next();
				//Se foi excluído, remove da lista.
				if (!entityDAO.refresh(value)) {
					iter.remove();
                    recordsErrors.remove(value);
				}
			}

			if (values.size() == originalSize) {
				tryRefreshView(values);
			} else {
				if (values.isEmpty()) {
					values = null;
				}
				setValue(values);
			}
		}
	}

    /**
     * Posiciona a lista de forma que o registro seja mostrado.
     *
     * @param record o registro que deve ser mostrado.
     */
    public void scrollToRecord(IEntityRecord record) {
        scrollToRecord(getRecordPosition(record));
    }

    /**
     * Posiciona a lista de forma que o registro seja mostrado.
     *
     * @param position posição do registro.
     */
    public void scrollToRecord(int position) {
        entityList.smoothScrollToPosition(position);
    }


    /*
	 * Métodos de manipulação das mensagens de erro
	 */

    /**
     * Define uma mensagem de erro para um registro do editor.<br>
     * A mensagem será removida se for <code>null</code>.
     *
     * @param record registro.
     * @param error mensagem de erro.
     */
    public void setRecordError(IEntityRecord record, String error) {
        int position = getRecordPosition(record);
        if (error == null) {
            recordsErrors.remove(record);
        } else {
            recordsErrors.put(record, error);
        }
        adapter.notifyItemChanged(position);
    }

    /**
     * Obtém a mensagem de erro definida para o registro do editor.
     *
     * @param record registro.
     * @return a mensagem de erro obtida ou <code>null</code> se não ha erro definido.
     */
    public String getRecordError(IEntityRecord record) {
        checkRecord(record);

        return internalGetRecordError(record);
    }

    /**
     * Indica se o registro possui uma mensagem de erro definida.
     *
     * @param record registro.
     * @return <code>true</code> se o registro possui mensagem de erro definida e <code>false</code> caso contrário.
     */
    public boolean hasRecordError(IEntityRecord record) {
        checkRecord(record);

        return recordsErrors.containsKey(record);
    }

    /**
     * Indica se algum registro do editor possui mensagem de erro definida.
     *
     * @return <code>true</code> se algum registro possui erro e <code>false</code> caso contrário.
     */
    public boolean hasRecordErrors() {
        return !recordsErrors.isEmpty();
    }

    /**
     * Remove todas as mensagens de erro dos registros do editor.
     */
    public void clearRecordsErrors() {
        clearRecordsErrors(false);
    }


	/*
	 * Métodos de configurações do editor. 
	 */
	
	/**
	 * Indica se este editor pode criar registros baseado nas permissões de edição da entidade alvo do relacionamento.<br>
	 * Se o editor for virtual, a criação é permitida sem nenhuma permissão.
	 * 
	 * @return <code>true</code> se puder criar registros e <code>false</code> caso contrário.
	 */
	public boolean canCreate() {
		return canCreate || isVirtual();
	}
	
	/**
	 * Define se o cabeçalho do editor deve ser escondido.
	 * 
	 * @param headerHidden <code>true</code> para esconder o cabeçalho e <code>false</code> caso contrário.
	 */
	public void setHeaderHidden(boolean headerHidden) {
		if (!headerHidden) {
			throw new PendingFeatureException("MultipleRelationshipFieldEditor's header");
		}
		this.headerHidden = headerHidden;
	}
	
	/**
	 * Indica se o cabeçalho do editor está escondido.
	 * 
	 * @return <code>true</code> se o cabeçalho do editor está escondido e <code>false</code> caso contrário.
	 */
	public boolean isHeaderHidden() {
		return headerHidden;
	}
	
	/**
	 * Define o listener de ações efetuadas pelo editor.
	 * 
	 * @param listener o listener.
	 */
	public void setEditorListener(IMultipleRelationshipFieldEditorListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Obtém o listener definido para as ações efetuadas pelo editor.
	 * 
	 * @return o listener obtido ou <code>null</code> caso nenhum listener tenha sido definido.
	 */
	public IMultipleRelationshipFieldEditorListener getEditorListener() {
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

    private void onRecordClick(int position) {
        //Se for apenas associação, lista os detalhes. Se for composição, edita o registro.
        switch (relationshipType) {
            case ASSOCIATION_ARRAY:
                detailRecord(position);
                break;

            case COMPOSITION_ARRAY:
                IEntityRecord record = getRecord(position);
                //Só permite a edição se a entidade tiver permissão para isto.
                IEditingPermissions permissions = getRecordPermissions(record);
                if (isEditable() && (isVirtual() || (permissions != null && permissions.canUpdate()))) {
                    editRecord(position);
                } else {
                    detailRecord(position);
                }
                break;

            case COMPOSITION:
            case ASSOCIATION:
                break;

            default:
                throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationshipType);
        }
    }
	
	private List<IEntityRecord> getRecordsList() {
		List<IEntityRecord> recordsList = super.getValue();
		if (recordsList == null) {
            throw new IllegalStateException(String.format("The value is null in the %s for the field %s", getClass().getSimpleName(), getFieldName()));
		}
		
		return recordsList;
	}
	
	private void selectRecord() {
		//Se o listener retornou true, significa que ele mesmo tratou a criação.
		if (listener != null && listener.onRecordSelection(this)) { 
			return;
		}
		
		throw new PendingFeatureException("Default MultipleRelationshipFieldEditor record selection");
	}
	
	private void createRecord() {
		//Se o listener retornou true, significa que ele mesmo tratou a criação.
		if (listener != null && listener.onRecordCreation(this)) { 
			return;
		}
		
		throw new PendingFeatureException("Default MultipleRelationshipFieldEditor record creation");
	}

    private void checkRecord(IEntityRecord record) {
        //Garante que o registro esta contido no editor.
        getRecordPosition(record);
    }

    private int getRecordPosition(IEntityRecord record) {
        return getRecordPosition(getRecordsList(), record);
    }

    private int getRecordPosition(List<IEntityRecord> recordsList, IEntityRecord record) {
        int position = recordsList.indexOf(record);
        if (position == -1) {
            throw new IllegalArgumentException(String.format("Record not found in the %s for the field %s", getClass().getSimpleName(), getFieldName()));
        }
        return position;
    }
	
	@SuppressLint("Assert")
	private void internalRemoveRecord(IEntityRecord record) {
		List<IEntityRecord> recordsList = getRecordsList();
		int position = getRecordPosition(recordsList, record);

		//Se o listener retornou true, significa que ele mesmo tratou a remoção.
		if (listener != null && listener.onRecordRemoval(this, position)) {
			return;
		}
		
		recordsList.remove(position);
		notifyRecordRemoval(recordsList, record, position);
	}
	
	private void notifyRecordRemoval(List<IEntityRecord> recordsList, IEntityRecord record, int position) {
        recordsErrors.remove(record);

		//Se a lista ficou vazia, seta null diretamente. Se não, apenas avisa da alteração.
		if (recordsList.isEmpty()) {
			setValue(null);
		} else {
			notifyValueChanged();
			if (isViewCreated()) {
				adapter.notifyItemRemoved(position);
			}
		}
	}
	
	private void editRecord(int position) {
		//Se o listener retornou true, significa que ele mesmo tratou a edição.
		if (listener != null && listener.onRecordEditing(this, position)) { 
			return;
		}
		
		throw new PendingFeatureException("Default MultipleRelationshipFieldEditor record editing");
	}
	
	private void detailRecord(int position) {
		//Se o listener retornou true, significa que ele mesmo tratou o detalhamento.
		if (listener != null && listener.onRecordDetailing(this, position)) { 
			return;
		}
		
		throw new PendingFeatureException("Default MultipleRelationshipFieldEditor record detailing");
	}
	
	IEditingPermissions getRecordPermissions(IEntityRecord record) {
		if (record.isLocal()) {
			return localPermissions;
		}
		
		return dataSourcePermissions;
	}

    private String internalGetRecordError(IEntityRecord record) {
        return recordsErrors.get(record);
    }

    private void clearRecordsErrors(boolean silent) {
        recordsErrors.clear();
        if (!silent) {
            adapter.notifyDataSetChanged();
        }
    }


	/*
	 * Classes auxiliares
	 */

	/**
	 * Listener de ações efetuadas pelo editor de relacionamentos múltiplos.<br>
	 * Todos os métodos possuem um retorno booleano que indica se o próprio listener vai tratar a ação ou se vai
	 * deixar o tratamento para o editor.
	 * 
	 * @author Thiago Gesser
	 */
	public interface IMultipleRelationshipFieldEditorListener {
		
		/**
		 * Chamado quando um registro precisa ser selecionado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordSelection(MultipleRelationshipFieldEditor editor);
		
		/**
		 * Chamado quando um registro precisa ser criado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordCreation(MultipleRelationshipFieldEditor editor);

		/**
		 * Chamado quando um registro precisa ser editado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @param position posição do registro que precisa ser editado.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordEditing(MultipleRelationshipFieldEditor editor, int position);
		
		/**
		 * Chamado quando um registro precisa ser detalhado.
		 *  
		 * @param editor o editor fonte da ação.
		 * @param position posição do registro que precisa ser detalhado.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordDetailing(MultipleRelationshipFieldEditor editor, int position);
		
		/**
		 * Chamado quando um registro precisa ser removido.
		 *  
		 * @param editor o editor fonte da ação.
		 * @param position posição do registro que precisa ser removido.
		 * @return <code>true</code> se a ação foi tratada pelo listener ou <code>false</code> caso contrário.
		 */
		boolean onRecordRemoval(MultipleRelationshipFieldEditor editor, int position);
	}


    /**
     * Adapter que engloba um {@link ListLayoutConfigAdapter} e adiciona um {@link TextView} referente à mensagem de erro nas Views dos registros.
     */
    private final class MultipleRelationshipListAdapter extends RecyclerView.Adapter<MultipleRelationshipItemViewHolder> {

        private final ListLayoutConfigAdapter layoutConfigAdapter;

        public MultipleRelationshipListAdapter(ListLayoutConfigAdapter layoutConfigAdapter) {
            this.layoutConfigAdapter = layoutConfigAdapter;
        }

        @Override
        public MultipleRelationshipItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ViewGroup rootView = (ViewGroup) layoutConfigAdapter.getInflater().inflate(R.layout.entity_field_editor_multiple_relationship_item, parent, false);
            View layoutConfigView = layoutConfigAdapter.createView(rootView);
            rootView.addView(layoutConfigView, 0);

            ListLayoutConfigViewHolder layoutConfigViewHolder = layoutConfigAdapter.createViewHolder(rootView);
            return new MultipleRelationshipItemViewHolder(rootView, layoutConfigViewHolder);
        }

        @Override
        public void onBindViewHolder(MultipleRelationshipItemViewHolder holder, int position) {
            IEntityRecord record = layoutConfigAdapter.getRecord(position);
            layoutConfigAdapter.bindViewHolder(holder.getLayoutConfigViewHolder(), record);

            TextView errorMsgView = holder.getErrorMsgView();
            String error = internalGetRecordError(record);
            errorMsgView.setText(error);
            errorMsgView.setVisibility(TextUtils.isEmpty(error) ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return layoutConfigAdapter.getItemCount();
        }

        public void setRecords(List<IEntityRecord> records) {
            if (records == null) {
                layoutConfigAdapter.clearRecords();
            } else {
                layoutConfigAdapter.setRecords(records);
            }
            notifyDataSetChanged();
        }
    }

    /**
     * Engloba o {@link ListLayoutConfigViewHolder} e armazena a referência para o {@link TextView} de erro.
     */
    private final class MultipleRelationshipItemViewHolder extends ViewHolder implements OnClickListener {

        private final TextView errorMsgView;
        private final ListLayoutConfigViewHolder layoutConfigViewHolder;

        public MultipleRelationshipItemViewHolder(View itemView, ListLayoutConfigViewHolder layoutConfigViewHolder) {
            super(itemView);
            this.errorMsgView = (TextView) itemView.findViewById(R.id.entity_field_editor_multiple_relationship_item_error);
            this.layoutConfigViewHolder = layoutConfigViewHolder;
            itemView.setOnClickListener(this);
        }

        public ListLayoutConfigViewHolder getLayoutConfigViewHolder() {
            return layoutConfigViewHolder;
        }

        public TextView getErrorMsgView() {
            return errorMsgView;
        }

        @Override
        public void onClick(View v) {
            onRecordClick(getAdapterPosition());
        }
    }
}
