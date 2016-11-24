package br.com.zalem.ymir.client.android.entity.ui.editor.relationship;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractEnumFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;

/**
 * Editor do tipo <code>enumeração</code> para relacionamentos singulares.<br>
 * Lista os registros da entidade alvo do relacionamento em um Dialog de lista. Cada registro é representado a partir do valor de um atributo da entidade.<br>
 * <br>
 * A listagem é feita a partir de todos os registros da entidade alvo, a não ser que tenha-se definido registros específicos através dos valores padrões.
 * Entretanto, o {@link OnValueChangeListener} pode definir registros diferentes para serem listados.<br>
 * <br>
 * Apenas associações singulares são suportadas.
 *
 * @author Thiago Gesser
 */
public final class EnumRelationshipEditor extends AbstractEnumFieldEditor<IEntityRecord> {

    private final IEntityDAO entityDAO;
    private final String[] displayAttribute;
    private final EntityAttributeFormatter displayFormatter;

    public EnumRelationshipEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help, FragmentManager fragmentManager, List<IEntityRecord> defaultValues,
                                  IEntityDAO entityDAO, String[] displayAttribute, EntityAttributeFormatter displayFormatter) {
		super(fieldName, label, editable, hidden, virtual, help, fragmentManager, defaultValues);
        this.entityDAO = entityDAO;
        this.displayAttribute = displayAttribute;
        this.displayFormatter = displayFormatter;
    }

    @Override
    protected IEntityRecord internalLoadValue(IEntityRecord record, String fieldName) {
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
    public boolean accept(IFieldEditorVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    protected CharSequence formatValue(IEntityRecord value) {
        return displayFormatter.formatAttributeValueToText(value, displayAttribute);
    }

    @Override
    protected boolean needBackgroundLoad() {
        return super.needBackgroundLoad() || getDefaultValues().isEmpty();
    }

    @Override
    protected List<IEntityRecord> loadValues() {
        OnListValuesListener<IEntityRecord> valuesListener = getOnListValuesListener();
        if (valuesListener != null) {
            List<IEntityRecord> newValues = valuesListener.beforeListValues(this);
            if (newValues != null) {
                return newValues;
            }
        }

        List<IEntityRecord> defaultValues = getDefaultValues();
        if (!defaultValues.isEmpty()) {
            return defaultValues;
        }

        //Obtém todos os registros da entidade alvo da enumeração como valores.
        return entityDAO.getAll();
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
     * Obtém o tipo de relacionamento do editor de enumeração.
     *
     * @return o tipo obtido.
     */
    public EntityRelationshipType getRelationshipType() {
        //Por enquanto suporta apenas associação singular.
        return EntityRelationshipType.ASSOCIATION;
    }

    /**
     * Obtém os metadados da entidade referenciada pelo relacionamento que este editor representa.
     *
     * @return os metadados da entidade obtidos.
     */
    public IEntityMetadata getRelationshipEntity() {
        return entityDAO.getEntityMetadata();
    }
}
