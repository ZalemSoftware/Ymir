package android.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import net.xpece.android.support.preference.EditTextPreference;
import net.xpece.android.support.preference.ListPreference;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * Versão aprimorada do {@link EditTextPreference}.<br>
 * Formata o <code>summary</code> com o valor definido no campo, assim como o {@link ListPreference}. Também é possível definir o <code>summaryEmptyText</code>
 * que é utilizado caso a preferência ainda não tenha valor definido ou se texto definido pelo usuario seja vazio.
 *
 * @author Thiago Gesser
 */
public final class EnhancedEditTextPreference extends EditTextPreference {

    private String summaryEmptyText;

    public EnhancedEditTextPreference(Context context) {
        this(context, null);
    }

    public EnhancedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EnhancedEditTextPreference, 0, 0);
        summaryEmptyText = a.getString(R.styleable.EnhancedEditTextPreference_summaryEmptyText);
        a.recycle();
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        super.onAddEditTextToDialogView(dialogView, editText);

        //Faz com que o cursor comece no final.
        editText.setSelection(editText.length());
    }

    @Override
    public void setText(String text) {
        boolean changed = !TextUtils.equals(getText(), text);

        super.setText(text);

        if (changed) {
            notifyChanged();
        }
    }

    @Override
    public CharSequence getSummary() {
        String text = getText();
        if (TextUtils.isEmpty(text)) {
            return summaryEmptyText;
        }

        CharSequence summary = super.getSummary();
        if (TextUtils.isEmpty(summary)) {
            return summary;
        }

        return String.format(summary.toString(), text);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        AlertDialog dialog = (AlertDialog) getDialog();
        getEditText().setOnEditorActionListener(new PreferenceEditorActionListener(dialog));
    }

    /**
     * Obtém o resumo que será utilizado caso a preferência não tenha valor definido ou o valor seja vazio.
     *
     * @return o resumo obtido.
     */
    public String getSummaryEmptyText() {
        return summaryEmptyText;
    }

    /**
     * Define o resumo que será utilizado caso a preferência não tenha valor definido ou o valor seja vazio.
     *
     * @param summaryEmptyText resumo.
     */
    public void setSummaryEmptyText(String summaryEmptyText) {
        this.summaryEmptyText = summaryEmptyText;
    }


    /**
     * Listener das ações de teclado do {@link EditText} da preferência. Faz com que o click na ação "Done" execute a mesma ação do
     * botão "Ok" do Dialog.
     */
    private static class PreferenceEditorActionListener implements OnEditorActionListener {
        private final AlertDialog dialog;

        public PreferenceEditorActionListener(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                return true;
            }
            return false;
        }
    }
}
