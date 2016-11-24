package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import java.io.Serializable;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractEnumFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter.TypedFormatter;

/**
 * Editor do tipo <code>enumeração</code> para atributos.<br>
 * Formata os valores de acordo com um {@link TypedFormatter}.
 * 
 * @author Thiago Gesser
 */
public final class EnumAttributeEditor extends AbstractEnumFieldEditor<Serializable> {

    private final TypedFormatter<?, Serializable> formatter;
    private final EntityAttributeType type;

    public EnumAttributeEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
                               FragmentManager fragmentManager, List<Serializable> defaultValues, TypedFormatter<?, Serializable> formatter, EntityAttributeType type) {
		super(fieldName, label, editable, hidden, virtual, help, fragmentManager, defaultValues);
        this.formatter = formatter;
        this.type = type;
	}

    @Override
    protected Serializable internalLoadValue(IEntityRecord record, String fieldName) {
        return (Serializable) record.getAttributeValue(fieldName);
    }

    @Override
    protected void internalStoreValue(IEntityRecord record, String fieldName, Serializable value) {
        record.setAttributeValue(fieldName, value);
    }

    @Override
    protected Serializable internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

        return (Serializable) bundle.get(key);
    }

    @Override
    protected void internalSaveState(Bundle bundle, String key, Serializable value) {
        super.internalSaveState(bundle, key, value);

        bundle.putSerializable(key, value);
    }

    @Override
    public boolean accept(IFieldEditorVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    protected CharSequence formatValue(Serializable value) {
        return formatter.formatValue(value).toString();
    }

    /**
     * Obtém o tipo de atributo do editor de enumeração.
     *
     * @return o tipo obtido.
     */
    public EntityAttributeType getType() {
        return type;
    }
}
