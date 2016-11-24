package br.com.zalem.ymir.client.android.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.EnhancedDialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;

/**
 * Diálogo de confirmação padrão do Ymir.<br>
 * Permite a definição do título e da mensagem do diálogo com ids para recursos String a partir dos argumentos {@link #TITLE_ARGUMENT}
 * e {@link #MESSAGE_ARGUMENT}.<br>
 * O resultado do diálogo é informado a um listener que pode ser definido através do método {@link #setListener(br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment.IConfirmationDialogListener)}.<br>
 * <br> 
 * IMPORTANTE: Se este fragmento for destruído e restaurado juntamente com o seu dono (outro fragmento ou uma activity),
 * o listener deverá ser definido novamente. É recomendado utilizar a {@link #CONFIRMATION_DIALOG_FRAGMENT_TAG} para 
 * criar / recuperar o fragmento. 
 * 
 * @see br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment.IConfirmationDialogListener
 *
 * @author Thiago Gesser
 */
public class ConfirmationDialogFragment extends EnhancedDialogFragment implements OnClickListener {

	public static final String CONFIRMATION_DIALOG_FRAGMENT_TAG = "CONFIRMATION_DIALOG_FRAGMENT_TAG";
	
	/**
	 * Id para um recurso String que será utilizado como o título.
	 */
	public static final String TITLE_ARGUMENT = "TITLE_ARGUMENT";
	/**
	 * String que será utilizado como o título.
	 */
	public static final String TITLE_STRING_ARGUMENT = "TITLE_STRING_ARGUMENT";

    /**
     * Id para um recurso de layout que será utilizado como a View do Dialog. Isto faz com que a mensagem não seja utilizada.
     */
    public static final String VIEW_LAYOUT_ARGUMENT = "VIEW_LAYOUT_ARGUMENT";

    /**
	 * Id para um recurso String que será utilizado como a mensagem.
	 */
	public static final String MESSAGE_ARGUMENT = "MESSAGE_ARGUMENT";
	/**
	 * String que será utilizado como a mensagem.
	 */
	public static final String MESSAGE_STRING_ARGUMENT = "MESSAGE_STRING_ARGUMENT";

    /**
     * Id para um recurso String que será utilizado como o texto do botão da ação positiva.
     */
    public static final String POSITIVE_BUTTON_ARGUMENT = "POSITIVE_BUTTON_ARGUMENT";
    /**
     * Id para um recurso String que será utilizado como o texto do botão da ação negativa.
     */
    public static final String NEGATIVE_BUTTON_ARGUMENT = "NEGATIVE_BUTTON_ARGUMENT";

    /**
     * Boolean que define se o botão de ação nevativa deve ser mostrado.
     */
    public static final String NEGATIVE_BUTTON_VISIBLE_ARGUMENT = "NEGATIVE_BUTTON_VISIBLE_ARGUMENT";
    /**
     * Boolean que define se o Dialog deve ser cancelado (retirado) quando o usuário efetuar um toque fora. O padrão é <code>true</code>.
     */
    public static final String CANCELED_ON_TOUCH_OUTSIDE_ARGUMENT = "CANCELED_ON_TOUCH_OUTSIDE_ARGUMENT";

    private static final String SAVED_DIALOG_VIEW_STATE = "SAVED_DIALOG_VIEW_STATE";


	private IConfirmationDialogListener listener;
    private View dialogView;

    @Override
    @NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle arguments = getArguments();
        if (arguments == null) {
            throw new IllegalStateException("Arguments are missing");
        }
        Context context = getActivity();
        Builder builder = new Builder(context);

        int titleResId = arguments.getInt(TITLE_ARGUMENT);
        if (titleResId > 0) {
            builder.setTitle(titleResId);
        } else {
            String title = arguments.getString(TITLE_STRING_ARGUMENT);
            if (title != null) {
                builder.setTitle(title);
            }
        }

        int viewLayoutId = arguments.getInt(VIEW_LAYOUT_ARGUMENT);
        if (viewLayoutId > 0) {
            dialogView = LayoutInflater.from(context).inflate(viewLayoutId, null);
            if (savedInstanceState != null) {
                SparseArray<Parcelable> states = savedInstanceState.getSparseParcelableArray(SAVED_DIALOG_VIEW_STATE);
                dialogView.restoreHierarchyState(states);
            }
            builder.setView(dialogView);
        } else {
            int msgResId = arguments.getInt(MESSAGE_ARGUMENT);
            if (msgResId > 0) {
                builder.setMessage(msgResId);
            } else {
                String message = arguments.getString(MESSAGE_STRING_ARGUMENT);
                if (message != null) {
                    builder.setMessage(message);
                }
            }
        }

		builder.setPositiveButton(arguments.getInt(POSITIVE_BUTTON_ARGUMENT, android.R.string.ok), this);
        if (arguments.getBoolean(NEGATIVE_BUTTON_VISIBLE_ARGUMENT, true)) {
		    builder.setNegativeButton(arguments.getInt(NEGATIVE_BUTTON_ARGUMENT, android.R.string.cancel), this);
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(arguments.getBoolean(CANCELED_ON_TOUCH_OUTSIDE_ARGUMENT, true));

        if (dialogView != null) {
            //Tem que definir isto para que o teclado abra na View focada.
            alertDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return alertDialog;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
                onConfirm();
                break;
				
			case AlertDialog.BUTTON_NEGATIVE:
				onCancel();
				break;
				
			default:
				throw new IllegalArgumentException("Unsupported ConfirmationDialogFragment button: " + which);
		}
	}

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        dialogView = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (dialogView != null) {
            SparseArray<Parcelable> states = new SparseArray<>();
            dialogView.saveHierarchyState(states);
            outState.putSparseParcelableArray(SAVED_DIALOG_VIEW_STATE, states);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener = null;
    }


    /**
     * Chamado se o usuário confirmou o diálogo.
     */
    protected void onConfirm() {
        if (listener != null) {
            listener.onConfirm(this);
        }
    }

    /**
     * Chamado se o usuário cancelou o diálogo.
     */
    protected void onCancel() {
        if (listener != null) {
            listener.onCancel(this);
        }
    }


    /**
	 * Define o listener que será avisado do resultado do diálogo.
	 * 		
	 * @param listener listener do resultado do diálogo.
	 */
	public final void setListener(IConfirmationDialogListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Obtém o listener do resultado do diálogo.
	 * 
	 * @return o listener obtido ou <code>null</code> se nenhum listener foi definido.
	 */
	public final IConfirmationDialogListener getListener() {
		return listener;
	}

    /**
     * Obtém a View criada a partir do layout definido no argumento {@link #VIEW_LAYOUT_ARGUMENT}.
     *
     * @return a View obtida ou <code>null</code> se o argumento {@link #VIEW_LAYOUT_ARGUMENT} não foi definido ou se o Dialog ainda não foi criado.
     */
    public final View getDialogView() {
        return dialogView;
    }

    /**
	 * Listener do resultado do diálogo de confirmação.
	 *
	 * @author Thiago Gesser
	 */
	public interface IConfirmationDialogListener {
		
		/**
		 * Chamado se o usuário confirmou o diálogo.
		 * 
		 * @param fragment o fragmento cujo o diálogo foi confirmado.
		 */
		void onConfirm(ConfirmationDialogFragment fragment);
		
		/**
		 * Chamado se o usuário cancelou o diálogo.
		 * 
		 * @param fragment o fragmento cujo o diálogo foi cancelado.
		 */
		void onCancel(ConfirmationDialogFragment fragment);
	}
}
