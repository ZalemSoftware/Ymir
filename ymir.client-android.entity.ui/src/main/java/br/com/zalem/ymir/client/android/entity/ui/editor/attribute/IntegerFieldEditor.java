package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.os.Bundle;
import android.text.InputType;

import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IIntegerMask;

/**
 * Editor de campo referente a um atributo <code>inteiro</code> da entidade.
 *
 * @author Thiago Gesser
 */
public final class IntegerFieldEditor extends AbstractNumberFieldEditor<Integer> {

	private final IIntegerMask mask;

	public IntegerFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
							  int inputType, IIntegerMask mask, Integer increment) {
		super(fieldName, label, editable, hidden, virtual, help, inputType | InputType.TYPE_CLASS_NUMBER, increment);
		this.mask = mask;
	}

	@Override
	protected Integer parseValue(CharSequence text) throws ParseException {
		return mask.parseInteger(text);
	}

	@Override
	protected CharSequence formatValue(Integer value) {
		return mask.formatInteger(value);
	}
	
	@Override
	protected Integer internalLoadValue(IEntityRecord record, String key) {
		return record.getIntegerValue(key);
	}
	
	@Override
	protected void internalStoreValue(IEntityRecord record, String key, Integer value) {
		record.setIntegerValue(key, value);		
	}
	
	@Override
	protected Integer internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		return (Integer) bundle.get(key);
	}
	
	@Override
	protected void internalSaveState(Bundle bundle, String key, Integer value) {
        super.internalSaveState(bundle, key, value);

		bundle.putSerializable(key, value);
	}
	
	@Override
	public boolean accept(IFieldEditorVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	protected void onChangeValue(Integer amount, boolean increase) {
		Integer rawValue = getValue();
		int value = rawValue == null ? 0 : rawValue;
		setValue(increase ? value + amount : value - amount);
	}
}
