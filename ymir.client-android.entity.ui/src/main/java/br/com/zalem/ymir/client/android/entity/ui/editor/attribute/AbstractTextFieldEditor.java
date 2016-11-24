package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;

import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractLabeledFieldEditor;

/**
 * Base para editores de campos baseados em texto.
 * 
 * @author Thiago Gesser
 */
public abstract class AbstractTextFieldEditor <T> extends AbstractLabeledFieldEditor<T> {

	private final int inputType;
	private EditText editText;
	private TextFieldChangeListener textChangeListener;
    private String hint;

	public AbstractTextFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
								   int inputType) {
		super(fieldName, label, editable, hidden, virtual, help);
        this.hint = label;
		this.inputType = inputType;
	}

    @Override
    protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = super.createView(inflater, label, editable, parent);

        editText = createEditText(inflater, rootView);
        editText.setHint(hint);

        editText.setEnabled(editable);
        //Utiliza o setFocusable também se não o campo ainda fica focável, mesmo se for desabilitado.
        editText.setFocusable(editable);

        editText.setInputType(inputType);
        //Wrapeia o KeyListener para evitar que o EditText use-o como um InputFilter também, o que pode after as máscaras.
        editText.setKeyListener(new KeyListenerWrapper(editText.getKeyListener(), inputType));

        textChangeListener = new TextFieldChangeListener();
        addTextChangedListener(editText, textChangeListener);

        return rootView;
    }

	@Override
	protected void destroyView() {
        super.destroyView();

		editText = null;
		textChangeListener = null;
	}
	
	@Override
	protected void refreshView(T value) {
		//Se foi disparado pela própria alteração no text change listener, não faz nada.
		if (!textChangeListener.isDisabled()) {
			if (value == null) {
				setText(null);
			} else {
				setText(formatValue(value));
			}
		}

		super.refreshView(value);
	}

    @Override
    public EditText getView() {
        return editText;
    }

	@Override
	protected boolean isAutomaticLabelVisible() {
		return editText.getText().length() > 0;
	}


    /**
     * Define o hint do editor de texto.
     *
     * @param hint o novo hint do editor.
     */
    public final void setHint(String hint) {
        this.hint = hint;

        if (isViewCreated()) {
            editText.setHint(hint);
        }
    }

	/**
	 * Obtém o hint do editor de texto.
	 *
	 * @return o hint obtido.
	 */
	public final String getHint() {
		return hint;
	}

	/**
	 * Obtem o valor do {@link InputType} do editor de texto.
	 *
	 * @return o valor obtido.
	 */
	public final int getInputType() {
		return inputType;
	}

    /**
     * Cria o {@link EditText} do editor e adiciona-o ao layout da View raiz.
     *
     * @param inflater inflater de layout
     * @param rootView a View raiz do editor, onde o EditText deve ser inserido.
     * @return o EditText criado.
     */
    protected EditText createEditText(LayoutInflater inflater, ViewGroup rootView) {
        EditText editText = (EditText) inflater.inflate(R.layout.entity_field_editor_text, rootView, false);
        rootView.addView(editText, 1);
        return editText;
    }

	/**
	 * Adiciona o listener de alterações de texto no editor.<br>
	 * Pode ser sobrescrito para customizar o listener ou adicionar outros listeners.
	 * 
	 * @param editText editor de texto.
	 * @param textChangeListener listener de alterações de texto.
	 */
	protected void addTextChangedListener(EditText editText, TextWatcher textChangeListener) {
		editText.addTextChangedListener(textChangeListener);
	}
	
	/**
	 * Trata a edição do texto, parseando o texto num valor e formatando o valor novamente em texto. Caso o texto
	 * não possa ser parseado, o texto antigo será recuperado.<br>
	 * Pode ser sobrescrito para customizar o comportamento de edição de texto.
	 * 
	 * @param newText editable contendo o texto novo, onde qualquer alteração deve ser feita.
	 * @param oldText texto antigo.
	 */
	protected void afterTextChanged(Editable newText, String oldText) {
		if (newText.length() == 0) {
			//Vazio significa "null".
			setValue(null);
			return;
		}

		CharSequence replaceText = null;
		try {
			T parsedValue = parseValue(newText);
			//Se retornou null, significa que o texto não representa um valor novo, mas sim um valor sendo composto.
			if (parsedValue != null) {
				setValue(parsedValue);
				replaceText = formatValue(parsedValue);
			} else {
				//Avisa sobre a alteração mesmo se não alterou o valor, pois o texto do edit foi alterado e isto pode afetar o label.
				refreshView(null);
			}
		} catch (ParseException e) {
			//Se o parse deu errado, volta o texto antigo  e mantém o valor corrente.
			replaceText = oldText;
		}

		if (replaceText != null) {
			newText.replace(0, newText.length(), replaceText);
		}
	}

	/**
	 * Parseia o valor a partir do texto.
	 * @param text texto.
	 * 
	 * @return o valor obtido do parse.
	 */
	protected abstract T parseValue(CharSequence text) throws ParseException;
	
	/**
	 * Formata o valor em um texto.
	 * 
	 * @param value o valor.
	 * @return o texto formatado.
	 */
	protected abstract CharSequence formatValue(T value);
	
	
	/*
	 * Métodos / classes auxiliares
	 */
	
	private void setText(CharSequence text) {
		textChangeListener.setDisabled(true);
		editText.setText(text);
		textChangeListener.setDisabled(false);
	}
	
	/**
	 *  Listener que controla as alterações nos textos através do método {@link AbstractTextFieldEditor#afterTextChanged(Editable, String)}.
	 */
	private final class TextFieldChangeListener implements TextWatcher {
		
		private boolean disabled = false;
		private String oldText;
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			if (disabled) {
				return;
			}
			
			//Tem que copiar pq o "s" será a instância que soferá a alteração.
			oldText = s.toString();
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if (disabled) {
				return;
			}
			disabled = true;
			
			AbstractTextFieldEditor.this.afterTextChanged(s, oldText);
			
			oldText = null;
			disabled = false;
		}
		
		public void setDisabled(boolean disabled) {
			this.disabled = disabled;
		}
		
		public boolean isDisabled() {
			return disabled;
		}
	}
}