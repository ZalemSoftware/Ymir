package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.os.Bundle;
import android.support.v4.app.AndroidBugsUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITextMask;

/**
 * Editor de campo referente a um atributo do tipo <code>texto</code> da entidade.
 *
 * @author Thiago Gesser
 */
public final class TextFieldEditor extends AbstractTextFieldEditor<String> {

    private final ITextMask mask;

	public TextFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
						   int inputType, ITextMask mask) {
		super(fieldName, label, editable, hidden, virtual, help, getInputType(inputType, mask));
        this.mask = mask;
	}

	@Override
	protected void addTextChangedListener(EditText editText, TextWatcher textChangeListener) {
		//Se a máscara for aplicada na edição, não tem que corrigir o problema com os Spans de edição.
		if (mask.isEditable()) {
			editText.addTextChangedListener(AndroidBugsUtils.applyWorkaroundForSwipeSpanBug(editText.getContext(), editText, textChangeListener));
		} else {
			super.addTextChangedListener(editText, textChangeListener);
		}
	}

	@Override
	protected void afterTextChanged(Editable newText, String oldText) {
		if (mask.isEditable()) {
			super.afterTextChanged(newText, oldText);
		} else {
			//Se a máscara não for aplicada na edição, apenas seta o valor normalmente.
			if (newText.length() == 0) {
				setValue(null);
			} else {
				setValue(newText.toString());
			}
		}
	}

	@Override
	protected String parseValue(CharSequence text) throws ParseException {
		return mask.parseText(text);
	}

	@Override
	protected CharSequence formatValue(String value) {
		return mask.formatText(value);
	}
	
	@Override
	protected String internalLoadValue(IEntityRecord record, String key) {
		return record.getTextValue(key);
	}
	
	@Override
	protected void internalStoreValue(IEntityRecord record, String key, String value) {
		record.setTextValue(key, value);		
	}
	
	@Override
	protected String internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		return bundle.getString(key);
	}
	
	@Override
	protected void internalSaveState(Bundle bundle, String key, String value) {
        super.internalSaveState(bundle, key, value);

		bundle.putString(key, value);
	}
	
	@Override
	public boolean accept(IFieldEditorVisitor visitor) {
		return visitor.visit(this);
	}


	/*
	 * Métodos auxiliares
	 */

	private static int getInputType(int inputType, ITextMask mask) {
		//Se a máscara for aplicada na edição, não permite sugestões.
		if (mask.isEditable()) {
			inputType |= InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
		}

		//Se já especificou uma classe de input, mantém ela.
		if ((inputType & InputType.TYPE_MASK_CLASS) > 0) {
			return inputType;
		}

		//Utiliza a classe padrão de texto.
		return inputType | InputType.TYPE_CLASS_TEXT;
	}
}
