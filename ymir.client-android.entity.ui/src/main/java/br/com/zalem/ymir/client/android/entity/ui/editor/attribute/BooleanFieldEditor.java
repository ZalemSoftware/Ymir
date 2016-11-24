package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractHelperFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;

/**
 * Editor de campo referente a um atributo <code>booleano</code> da entidade.<br>
 * Como o campo não consegue representar <code>null</code>, ele é considerado como <code>false</code>.
 *
 * @author Thiago Gesser
 */
public final class BooleanFieldEditor extends AbstractHelperFieldEditor<Boolean> implements OnCheckedChangeListener {

	private CheckBox checkBox;

	public BooleanFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help) {
		super(fieldName, label, editable, hidden, virtual, help);
	}
	
	@Override
	protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = super.createView(inflater, label, editable, parent);

		checkBox = (CheckBox) inflater.inflate(R.layout.entity_field_editor_boolean, parent, false);
		checkBox.setHint(label);
		checkBox.setEnabled(editable);
		checkBox.setOnCheckedChangeListener(this);

        rootView.addView(checkBox, 0);
		return rootView;
	}
	
	@Override
	protected void destroyView() {
        super.destroyView();

		checkBox = null;
	}
	
	@Override
	protected void refreshView(Boolean value) {
		checkBox.setOnCheckedChangeListener(null);
		if (value == null) {
			checkBox.setChecked(false);
		} else {
			checkBox.setChecked(value);
		}
		checkBox.setOnCheckedChangeListener(this);
	}

    @Override
    public CheckBox getView() {
        return checkBox;
    }

    @Override
	protected boolean canStoreValue() {
		//Como o campo não permite valor "null", o "false" toma o seu lugar. E como o campo começa com "null", tem que ser
		//salvo mesmo sem ter sido alterado, pois ele já está representando o valor "false".
		return true;
	}
	
	@Override
	public Boolean internalLoadValue(IEntityRecord record, String fieldName) {
		return record.getBooleanValue(fieldName);
	}

	@Override
	public void internalStoreValue(IEntityRecord record, String fieldName, Boolean value) {
		if (value == null) {
			value = Boolean.FALSE;
		}
		record.setBooleanValue(fieldName, value);
	}
	
	@Override
	protected Boolean internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		return (Boolean) bundle.get(key);
	}
	
	@Override
	protected void internalSaveState(Bundle bundle, String key, Boolean value) {
        super.internalSaveState(bundle, key, value);

		bundle.putSerializable(key, value);
	}
	
	@Override
	public boolean accept(IFieldEditorVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		setValue(isChecked);
	}

    @Override
    protected void tintError(boolean hasError, boolean hadError) {
        //Não pinta o checkbox quando há erro.
    }
}
