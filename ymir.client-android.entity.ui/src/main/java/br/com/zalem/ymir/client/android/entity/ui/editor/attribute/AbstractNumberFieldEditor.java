package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.view.TouchDelegateComposite;

/**
 * Base para editores de campos numéricos.<br>
 * Se o <code>incremento</code> foi definido, exibe botões que incrementam e decrementam o valor nas laterais do {@link EditText}.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractNumberFieldEditor <T extends Number> extends AbstractTextFieldEditor<T> implements OnClickListener {

	private final T increment;
	private ImageButton decrementButton;
	private ImageButton incrementButton;

	public AbstractNumberFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
									 int inputType, T increment) {
		super(fieldName, label, editable, hidden, virtual, help, inputType);
		this.increment = increment;
	}

    @Override
    protected EditText createEditText(LayoutInflater inflater, ViewGroup rootView) {
        //Se não for incremental, usa o EditText padrão.
        if (increment == null) {
            return super.createEditText(inflater, rootView);
        }

        //Como é incremental, utiliza um layout com os botões de incrementar e decrementar juntamente com o EditText.
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.entity_field_editor_number, rootView, false);
        rootView.addView(layout, 1);

        decrementButton = (ImageButton) layout.findViewById(R.id.entity_field_editor_number_decrementbutton);
        decrementButton.setOnClickListener(this);
        incrementButton = (ImageButton) layout.findViewById(R.id.entity_field_editor_number_incrementbutton);
        incrementButton.setOnClickListener(this);

        return (EditText) layout.findViewById(R.id.entity_field_editor_number_edittext);
    }

	@Override
	protected void destroyView() {
		super.destroyView();

		decrementButton = null;
		incrementButton = null;
	}

    @Override
    protected void configureTouchDelegate(ViewGroup rootView) {
        //Se não for incremental, usa TouchDelegate padrão.
        if (increment == null) {
            super.configureTouchDelegate(rootView);
            return;
        }

        //Pode ter sido destruído neste meio tempo.
        TextView labelView = getLabelView();
        if (labelView == null) {
            return;
        }

        /*
         * Faz com que os componentes possam ser clicados em qualquer parte de cima ou de baixo do layout pai (incluindo o label).
         */
        //Utiliza o composite para suportar o uso de mais de um TouchDelegate.
        TouchDelegateComposite touchDelegateComposite = new TouchDelegateComposite(rootView.getContext());
        rootView.setTouchDelegate(touchDelegateComposite);

        //Tem que considerar o tamanho do label também.
        int bottom = rootView.getHeight() + labelView.getMeasuredHeight();

        //Utiliza como largura toda área da esquerda do pai até o início do EditText.
        EditText editText = getView();
        Rect r = new Rect();
        r.top = 0;
        r.left = 0;
        r.bottom = bottom;
        r.right = editText.getLeft();
        touchDelegateComposite.addDelegate(new TouchDelegate(r, decrementButton));

        //Utiliza apenas a largura do próprio EditText.
        r = new Rect();
        editText.getHitRect(r);
        r.top = 0;
        r.bottom = bottom;
        touchDelegateComposite.addDelegate(new TouchDelegate(r, editText));

        //Utiliza como largura toda área do fim do EditText até a direita do pai.
        r = new Rect();
        r.top = 0;
        r.left = editText.getRight();
        r.bottom = bottom;
        r.right = rootView.getWidth();
        touchDelegateComposite.addDelegate(new TouchDelegate(r, incrementButton));
    }

    @Override
	@SuppressLint("Assert")
	public void onClick(View v) {
		boolean increase;
		if (v == decrementButton) {
			increase = false;
		} else {
			assert v == incrementButton;
			increase = true;
		}

		onChangeValue(increment, increase);
	}

	/**
	 * Chamado quando um dos botões de incremento ou decremento foi pressionado, sendo necessário a alteração do valor.
	 *
	 * @param amount o tanto que deve ser alterado.
	 * @param increase <code>true</code> se deve aumentar o valor e <code>false</code> se deve diminuir.
	 */
	protected abstract void onChangeValue(T amount, boolean increase);
}
