package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.os.Bundle;
import android.text.InputType;

import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalMask;

/**
 * Editor de campo referente a um atributo <code>decimal</code> da entidade.
 *
 * @author Thiago Gesser
 */
public final class DecimalFieldEditor extends AbstractNumberFieldEditor<Double> {

	private final IDecimalMask mask;

	public DecimalFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
							  int inputType, IDecimalMask mask, Double increment) {
		super(fieldName, label, editable, hidden, virtual, help, inputType | InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, increment);
		this.mask = mask;
	}

	@Override
	protected Double parseValue(CharSequence text) throws ParseException {
		return mask.parseDecimal(text);
	}

	@Override
	protected CharSequence formatValue(Double value) {
		return mask.formatDecimal(value);
	}
	
	@Override
	protected Double internalLoadValue(IEntityRecord record, String fieldName) {
		return record.getDecimalValue(fieldName);
	}
	
	@Override
	protected void internalStoreValue(IEntityRecord record, String fieldName, Double value) {
		record.setDecimalValue(fieldName, value);
	}
	
	@Override
	protected Double internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		return (Double) bundle.get(key);
	}
	
	@Override
	protected void internalSaveState(Bundle bundle, String key, Double value) {
        super.internalSaveState(bundle, key, value);

		bundle.putSerializable(key, value);
	}

	@Override
	public boolean accept(IFieldEditorVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	protected void onChangeValue(Double amount, boolean increase) {
		Double rawValue = getValue();
		double value = rawValue == null ? 0 : rawValue;
		setValue(increase ? value + amount : value - amount);
	}
}
