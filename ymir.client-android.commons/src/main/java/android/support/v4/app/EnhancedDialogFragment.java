package android.support.v4.app;

import android.app.Dialog;
import android.os.Bundle;

/**
 * Versão aprimorada do {@link DialogFragment} que completa-o com partes que estão faltando, como os métodos para mostrar o fragmento com
 * perda de estado.
 *
 * @author Thiago Gesser
 */
public class EnhancedDialogFragment extends DialogFragment {

    private static final String SAVED_HIDDEN = "SAVED_HIDDEN";
    private boolean hidden;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SAVED_HIDDEN, hidden);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            hidden = savedInstanceState.getBoolean(SAVED_HIDDEN, false);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (hidden) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.hide();
            }
        }
    }

    /**
     * Esconde o Dialog deste fragmento, prevenindo que ele apareça caso ainda não tenha sido mostrado.
     */
    public final void hideDialog() {
        hidden = true;
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.hide();
        }
    }

    /**
     * Mostra o Dialog deste fragmento, garantido que ele apareça caso ainda nao tenha sido mostrado.
     */
    public final void showDialog() {
        hidden = false;
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.show();
        }
    }

    /**
     * Mostra o fragmento como feito em {@link #show(FragmentManager, String)}) mas faz o commit possibilitando a perda de estado.
     *
     * @param manager gerenciador de fragmentos.
     * @param tag tag do fragmento.
     */
    public void showAllowingStateLoss(FragmentManager manager, String tag) {
        mDismissed = false;
        mShownByMe = true;
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    /**
     * Mostra o fragmento como feito em {@link #show(FragmentTransaction, String)}) mas faz o commit possibilitando a perda de estado.
     *
     * @param transaction transaction de fragmentos.
     * @param tag tag do fragmento.
     * @return o identificador da transação commitada.
     */
    public int showAllowingStateLoss(FragmentTransaction transaction, String tag) {
        mDismissed = false;
        mShownByMe = true;
        transaction.add(this, tag);
        mViewDestroyed = false;
        mBackStackId = transaction.commitAllowingStateLoss();
        return mBackStackId;
    }

    @Override
    public void onDestroyView() {
        AndroidBugsUtils.applyWorkaroundForBug17423(this);
        super.onDestroyView();
    }
}
