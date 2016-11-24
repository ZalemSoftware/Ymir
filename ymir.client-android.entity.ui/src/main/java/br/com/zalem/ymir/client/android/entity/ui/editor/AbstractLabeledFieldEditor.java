package br.com.zalem.ymir.client.android.entity.ui.editor;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.zalem.ymir.client.android.entity.ui.R;


/**
 * Base para editores de campos rotulados.<br>
 * O rótulo fica logo acima da View normal do editor. Por padrão, o rótulo só é mostrado quando o valor for diferente de nulo,
 * mas este comportamento pode ser evitado ao sobrescrever o método {@link #refreshView(Object)} sem chamar o <code>super</code>.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractLabeledFieldEditor <T> extends AbstractHelperFieldEditor<T> implements OnFocusChangeListener {

    private ViewGroup rootView;
	private TextView labelView;

	private Boolean manualVisibility;

    public AbstractLabeledFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help) {
		super(fieldName, label, editable, hidden, virtual, help);
	}

    @Override
    protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        rootView = super.createView(inflater, label, editable, parent);

        labelView = (TextView) inflater.inflate(R.layout.entity_field_editor_label, rootView, false);
        labelView.setText(label);
        labelView.setEnabled(editable);

        rootView.addView(labelView, 0);
        return rootView;
    }

    @Override
    protected void onViewCreated() {
        super.onViewCreated();

        if (manualVisibility != null) {
            setLabelVisibility(manualVisibility);
        }

        View labeledView = getView();
        if (labeledView.isFocusable()) {
            labeledView.setOnFocusChangeListener(this);
        }

        configureTouchDelegate();
    }

    @Override
	protected void destroyView() {
        super.destroyView();

        rootView = null;
		labelView = null;
	}

	@Override
	//A subclasse pode escolher não chamar este método para evitar o comportamento de esconder automaticamente.
	protected void refreshView(T newValue) {
		//Só altera por aqui se estiver no modo automático.
		if (manualVisibility != null) {
			return;
		}
		
		//Por padrão, os editores dispõem o label como um "hint" quando seus valores são nulos, então só mostra o label
		//quando o valor for diferente de nulo.
		setLabelVisibility(isAutomaticLabelVisible());
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        //Faz com que o label seja ativado com o foco na View rotulada, permitindo alteração no estado do label como a cor do texto, por exemplo.
        if (isViewCreated()) {
            labelView.setActivated(hasFocus);
        }
    }


	/**
	 * Define a visibilidade do rótulo de forma manual, desligando a visibilidade automática.
	 * 
	 * @param visible <code>true</code> se o rótulo deve ficar visível e <code>false</code> caso contrário.
	 */
	public final void setManualLabelVisibility(boolean visible) {
		manualVisibility = visible;
		if (isViewCreated()) {
			setLabelVisibility(visible);
		}
	}
	
	/**
	 * Define que a visibilidade do rótulo deve ser controlada automaticamente, sendo mostrado quando houver um valor no editor
	 * e escondido caso contrário.
	 */
	public final void setAutomaticLabelVisibility() {
		manualVisibility = null;
        if (isViewCreated()) {
            setLabelVisibility(getValue() != null);
        }
	}

	/**
	 * Obtém a View utilizada como rótulo.
	 *
	 * @return a View do rótulo.
	 */
	public final TextView getLabelView() {
		return labelView;
	}

    /**
     * Configura o {@link TouchDelegate} do editor, visando facilitar os clicks nas Views.<br>
     * É chamado automaticamente após a criação das Views. Entretanto, podem ocorrer situações em que as Views ainda não possuem tamanho
     * definido (por estarem escondidas, por exemplo), resultando em um TouchDelegate não funcional. Por isto, este método pode ser chamado
     * para configurar o TouchDelegate quando se tem certeza que as Views estão com os tamanhos calculados.
     */
    public final void configureTouchDelegate() {
        rootView.post(new Runnable() {
            public void run() {
                configureTouchDelegate(rootView);
            }
        });
    }


    /**
     * Altera a visibilidade do rótulo.
     *
     * @param visible <code>true</code> se ele deve ficar visível e <code>false</code> caso contrário.
     */
    protected final void setLabelVisibility(boolean visible) {
        labelView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Configura o {@link TouchDelegate} do editor, visando facilitar os clicks que serão repassados para a View retornada no {@link #getView()}.<br>
     * <br>
     * Pode ser sobrescrito para alterar o delegate configurado.
     *
     * @param rootView View raiz do editor.
     */
    protected void configureTouchDelegate(ViewGroup rootView) {
        //Pode ter sido destruído no meio tempo.
        if (labelView == null) {
            return;
        }

        //Faz com que a View interna possa ser clicado em qualquer parte do label também.
        Rect r = new Rect();
        r.top = 0;
        r.left = 0;
        r.bottom = rootView.getHeight() + labelView.getMeasuredHeight();
        r.right = rootView.getWidth();
        rootView.setTouchDelegate(new TouchDelegate(r, getView()));
    }

	/**
	 * Indica se o rótulo deve ser mostrado utilizando o mecanismo de visibilidade automática.
	 *
	 * @return <code>true</code> se o rótulo deve ser mostrado e <code>false</code> caso contrário.
	 */
	protected boolean isAutomaticLabelVisible() {
		return getValue() != null;
	}
}
