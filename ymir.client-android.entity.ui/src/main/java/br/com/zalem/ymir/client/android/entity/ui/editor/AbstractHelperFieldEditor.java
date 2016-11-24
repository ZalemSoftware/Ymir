package br.com.zalem.ymir.client.android.entity.ui.editor;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.zalem.ymir.client.android.entity.ui.R;


/**
 * Base para editores de campos que utilizam o auxílio de textos de ajuda/erro.<br>
 * Inicialmente o texto de ajuda é exibido junto ao editor, caso tenha sido definido. Quando uma mensagem de erro é definida, o texto
 * de ajuda é substituido por ela. A mensagem de erro pode ser definida através do método {@link #setError(String)}.<br>
 * Enquanto o editor estiver com erro, sua View pode exibir uma indicação que é pintada através do método {@link #tintError(boolean, boolean)}.
 * A cor de erro utilizada é definida pelo atributo do tema {@link R.attr#colorError}.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractHelperFieldEditor <T> extends AbstractFieldEditor<T> {

    private static final String SAVED_ERROR = ".error";

    private final String help;

    private TextView helperTextView;
    private ColorStateList helperTextColor;
    private int errorColor;
    private String error;

    public AbstractHelperFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help) {
		super(fieldName, label, editable, hidden, virtual);
        this.help = help;
    }

	@Override
	protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.entity_field_editor_helper, parent, false);

        helperTextView = createHelperTextView(inflater, parent);

        rootView.addView(helperTextView);
        return rootView;
	}

    @Override
    protected void onViewCreated() {
        TypedValue typedValue = new TypedValue();
        helperTextView.getContext().getTheme().resolveAttribute(R.attr.colorError, typedValue, true);
        errorColor = typedValue.data;

        helperTextColor = helperTextView.getTextColors();

        refreshHelperText(false);
    }

    @Override
	protected void destroyView() {
        helperTextView = null;
	}

    @Override
    protected void internalSaveState(Bundle bundle, String key, T value) {
        bundle.putString(key + SAVED_ERROR, error);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T internalRestoreState(Bundle bundle, String key) {
        error = bundle.getString(key + SAVED_ERROR);
        return null;
    }

    /**
     * Define a mensagem de erro do editor.<br>
     * O erro será removido se a mensagem for <code>null</code>.
     *
     * @param error a mensagem de erro.
     */
    public final void setError(String error) {
        boolean hadError = hasError();
        this.error = error;
        if (isViewCreated()) {
            refreshHelperText(hadError);
        }
    }

    /**
     * Obtém a mensagem de erro do editor.
     *
     * @return A mensagem obtida ou <code>null</code> se não há mensagem de erro no editor.
     */
    public final String getError() {
        return error;
    }

    /**
     * Indica se o editor possui mensagem de erro.
     *
     * @return <code>true</code> se o editor possui erro e <code>false</code> caso contrário.
     */
    public final boolean hasError() {
        return !TextUtils.isEmpty(error);
    }


    /**
     * Pinta ou remove a indicação de erro na View do editor.
     *
     * @param hasError indica se há erro atualmente no editor.
     * @param hadError indica se havia erro no editor.
     */
    protected void tintError(boolean hasError, boolean hadError) {
        View view = getView();
        if (hasError) {
            if (!hadError) {
                //Armazena o tint original na tag da View.
                ColorStateList originalTint = ViewCompat.getBackgroundTintList(view);
                view.setTag(originalTint);
            }

            ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(errorColor));
        } else if (hadError) {
            ColorStateList originalTint = (ColorStateList) view.getTag();
            ViewCompat.setBackgroundTintList(view, originalTint);
        }
    }

    /**
     * Cria o {@link TextView} de ajuda/erro do editor.
     *
     * @param inflater inflater de layout.
     * @param parent View pai.
     * @return o TextView criado.
     */
    protected TextView createHelperTextView(LayoutInflater inflater, ViewGroup parent) {
        return (TextView) inflater.inflate(R.layout.entity_field_editor_helper_text, parent, false);
    }


    /*
     * Metodos auxiliares
     */

    private void refreshHelperText(boolean hadError) {
        boolean hasError = hasError();
        if (hasError) {
            helperTextView.setText(error);
            if (!hadError) {
                helperTextView.setVisibility(View.VISIBLE);
                helperTextView.setTextColor(errorColor);
            }
        } else {
            helperTextView.setText(help);
            if (hadError) {
                helperTextView.setTextColor(helperTextColor);
            }
            helperTextView.setVisibility(TextUtils.isEmpty(help) ? View.GONE : View.VISIBLE);
        }

        tintError(hasError, hadError);
    }
}
