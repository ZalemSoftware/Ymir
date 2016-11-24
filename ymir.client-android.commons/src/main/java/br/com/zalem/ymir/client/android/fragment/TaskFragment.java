package br.com.zalem.ymir.client.android.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.EnhancedDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Window;
import android.view.WindowManager;

import br.com.zalem.ymir.client.android.util.SafeAsyncTask;

/**
 * Fragmento especializado para a execução de um {@link FragmentAsyncTask}.<br>
 * A referência para a task é mantida pelo fragmento, independente da destruição/reinicialização da Activity, tirando esta responsabilidade
 * de quem executa a task. Para isto, a instância do fragmento é retida em memória.<br>
 * Além disso, faz com que o método de encerramento da task ({@link FragmentAsyncTask#saferOnPostExecute(Object)} ou {@link FragmentAsyncTask#onCancelled(Object)})
 * só seja chamado quando não for haver perda de estado nas Views. Para isto, caso a task termine após o {@link Fragment#onSaveInstanceState(Bundle)},
 * o encerramento só será chamado depois do {@link #onStart()}.<br>
 * O fragmento é destruído automaticamente após a finalização da task.<br>
 * <br>
 * Um {@link ProgressDialog} é exibido durante a execução da task, cuja mensagem pode ser definida através de {@link #setProgressMessage(String)}.<br>
 * Para evitar que o ProgressDialog apareça e suma rapidamente em tasks muito rápidas, ele só é exibido após o tempo definido em {@link #setProgressWaitTime(int)},
 * sendo que o padrão é {@value #DEFAULT_PROGRESS_WAIT_TIME} milisegundos.<br>
 * <br>
 * <br>
 * <b>IMPORTANTE:</b> Atualmente o comportamento de reter em memória um fragmento filho de outro fragmento não está funcionando devido a
 * um <a href="https://code.google.com/p/android/issues/detail?id=74222">bug no android</a>. Desta forma, para utilizar este fragmento neste
 * cenário, é necessário utilizar o contorno para o bug chamando o método {@link AndroidBugsUtils#applyWorkaroundForBug74222_onSaveInstanceState(Fragment)}
 * no {@link #onCreate(Bundle)} do fragmento pai e o {@link AndroidBugsUtils#applyWorkaroundForBug74222_onSaveInstanceState(Fragment)} no
 * {@link #onSaveInstanceState(Bundle)} do fragmento pai.
 *
 * @see FragmentAsyncTask
 *
 * @author Thiago Gesser
 */
public class TaskFragment <Params, Progress, Result> extends EnhancedDialogFragment {

    private static final int DEFAULT_PROGRESS_WAIT_TIME = 500;
    private int progressWaitTime = DEFAULT_PROGRESS_WAIT_TIME;
    private String progressMessage;

    private FragmentAsyncTask task;
    private boolean stateSaved;


    @Override
    public void onStart() {
        stateSaved = false;
        if (isTaskCompleted()) {
            finishTask();
        }

        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        stateSaved = true;
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        if (task == null) {
            //Se o task é null, o dialog já será removido.
            return super.onCreateDialog(savedInstanceState);
        }

        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setIndeterminate(true);

        if (progressMessage != null) {
            dialog.setMessage(progressMessage);
        }
        return dialog;
    }

    @Override
    public final void onCancel(DialogInterface dialog) {
        //Apesar de improvável, garante que se o Dialog foi cancelado de alguma forma, cancela a task para evitar problemas quando ela terminar e o fragmento tiver sido destruído.
        if (task != null) {
            task.cancel(false);
        }
    }


    /**
     * Chama o método {@link #isExecutingTask(FragmentManager, String)} passando o nome da classe do TaskFragment como <code>fragmentTag</code>.<br>
     * Esta forma de verificação não deve ser utilizada se o fragmento for uma subclasse do TaskFragment.
     *
     * @param fragmentManager gerenciador de fragmentos que foi utilizado para iniciar o fragmento.
     * @return <code>true</code> se o TaskFragment está em execução e <code>false</code> caso contrário.
     */
    public static boolean isExecutingTask(FragmentManager fragmentManager) {
        return isExecutingTask(fragmentManager, TaskFragment.class.getSimpleName());
    }

    /**
     * Verifica se o TaskFragmento está em execução através do <code>fragmentTag</code>.
     *
     * @param fragmentManager gerenciador de fragmentos que foi utilizado para iniciar o fragmento.
     * @param fragmentTag tag do fragmento.
     * @return <code>true</code> se o TaskFragment está em execução e <code>false</code> caso contrário.
     */
    public static boolean isExecutingTask(FragmentManager fragmentManager, String fragmentTag) {
        return fragmentManager.findFragmentByTag(fragmentTag) != null;
    }


    /**
     * Chama o método {@link #startTask(FragmentAsyncTask, Activity, FragmentManager, String, Object[])} passando o nome da classe deste
     * fragmento como <code>fragmentTag</code>.
     *
     * @param task task que sera executada.
     * @param activity Activity onde os toques serão desabilitados.
     * @param fragmentManager gerenciador de fragmentos que será utilizado para iniciar o fragmento.
     * @param taskParams parâmetros da task.
     */
    @SuppressWarnings("unchecked")
    public final void startTask(FragmentAsyncTask<Params, Progress, Result> task, Activity activity, FragmentManager fragmentManager, Params... taskParams) {
        startTask(task, activity, fragmentManager, getClass().getSimpleName(), taskParams);
    }

    /**
     * Inicia a task e o fragmento, sendo que o fragmento é mantido em memoria durante a execução da task.<br>
     * Se a task não for completada até o tempo definido em {@link #getProgressWaitTime()}, um {@link ProgressDialog} será exibido até sua
     * finalização. Para evitar interações indevidas durante este tempo de espera, os toques na tela são desabilitados.<br>
     * Após a finalziação da task, o fragmento é destruído.
     *
     * @param task task que sera executada.
     * @param activity Activity onde os toques serão desabilitados.
     * @param fragmentManager gerenciador de fragmentos que será utilizado para iniciar o fragmento.
     * @param fragmentTag tag do fragmento.
     * @param taskParams parâmetros da task.
     */
    @SuppressWarnings("unchecked")
    public final void startTask(FragmentAsyncTask<Params, Progress, Result> task, Activity activity, FragmentManager fragmentManager, String fragmentTag, Params... taskParams) {
        if (this.task != null) {
            throw new IllegalStateException("There is already a task running.");
        }

        //Previne as ações de toque neste momento pq o Dialog só vai ser mostrado depois de um tempo.
        setScreenTouchEnabled(activity, false);

        //Configura e inicia a task real e a que mostra o Progress.
        this.task = task;
        task.setFragment(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, taskParams);
        new ShowProgressDialogTask(progressWaitTime).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        /*
         * Configura e inicializa o fragmento, garantindo que ele será adicionado antes da Task chamar o onPostExecute ou onCancelled.
         */
        //Retém a instância para manter a task e para que ela também referencie o fragmento correto.
        setRetainInstance(true);
        //Garante que o Dialog não pode ser cancelado.
        setCancelable(false);
        //Só mostra o progresso depois do tempo mínimo passar.
        hideDialog();

        show(fragmentManager, fragmentTag);
        fragmentManager.executePendingTransactions();
    }

    /**
     * Define o tempo de espera até a exibição do {@link ProgressDialog}.<br>
     * Desta forma, se a task terminar antes deste tempo, o ProgressDialog nem será exibido.
     *
     * @param progressWaitTime tempo de espera em milisegundos.
     */
    public final void setProgressWaitTime(int progressWaitTime) {
        if (progressWaitTime < 0) {
            throw new IllegalArgumentException("progressWaitTime < 0");
        }

        this.progressWaitTime = progressWaitTime;
    }

    /**
     * Obtém o tempo de espera até a exibição do {@link ProgressDialog}.
     *
     * @return o tempo obtido em milisegundos.
     */
    public final int getProgressWaitTime() {
        return progressWaitTime;
    }

    /**
     * Define a mensagem que é exibida no {@link ProgressDialog}.
     *
     * @param progressMessage mensagem.
     */
    public void setProgressMessage(String progressMessage) {
        this.progressMessage = progressMessage;
    }

    /**
     * Obtém a mensagem que é exibida no {@link ProgressDialog}.
     *
     * @return a mensagem obtida ou <code>null</code> se não havia mensagem definida.
     */
    public String getProgressMessage() {
        return progressMessage;
    }


    /*
     * Métodos auxiliares.
     */

    private void tryFinishTask() {
        //Se o estado do fragmento atual já foi salvo, significa que qualquer alteração na UI acarretará em perda de estado,
        //então aguarda o fragmento ser iniciado novamente para finalizar a task.
        if (stateSaved) {
            return;
        }

        finishTask();
    }

    @SuppressWarnings("unchecked")
    private void finishTask() {
        dismiss();
        setScreenTouchEnabled(getParentFragment().getActivity(), true);

        task.finish();
        task.setFragment(null);
        task = null;
    }

    private boolean isTaskCompleted() {
        return task.getStatus() == Status.FINISHED;
    }

    private static void setScreenTouchEnabled(Activity activity, boolean enabled) {
        if (activity == null) {
            return;
        }

        Window window = activity.getWindow();
        if (enabled) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    /*
     * Classes auxiliares
     */

    /**
     * {@link AsyncTask} especializada para a execução a partir do {@link TaskFragment}.<br>
     * Faz com que o método de finalização ({@link #saferOnPostExecute(Object)} ou {@link #saferOnCancelled(Object)}) só seja chamado quando
     * não for haver perda de estado nas Views. Para isto, caso a task termine após o {@link Fragment#onSaveInstanceState(Bundle)},
     * a finalização só será chamada depois do {@link Fragment#onStart()}.
     */
    public static abstract class FragmentAsyncTask <Params, Progress, Result> extends SafeAsyncTask<Params, Progress, Result> {

        private TaskFragment<Params, Progress, Result> fragment;
        private Result result;

        public final TaskFragment<Params, Progress, Result> getFragment() {
            return fragment;
        }

        @Override
        protected void onPreExecute() {
            if (fragment == null) {
                throw new IllegalStateException("FragmentAsyncTask can only be used through a TaskFragment.");
            }
        }

        final void setFragment(TaskFragment<Params, Progress, Result> fragment) {
            this.fragment = fragment;
        }

        final void finish() {
            if (isCancelled()) {
                saferOnCancelled(result);
            } else {
                saferOnPostExecute(result);
            }
            result = null;
        }


        @Override
        protected final void safeOnCancelled(Result result) {
            this.result = result;
            fragment.tryFinishTask();
        }

        @Override
        protected final void safeOnPostExecute(Result result) {
            this.result = result;
            fragment.tryFinishTask();
        }

        /**
         * Versão do {@link #onPostExecute(Object)} que só é chamado quando não for haver perda de estado nas Views e apenas se não houve
         * erro na execução em background.
         *
         * @param result resultado da execução.
         */
        protected void saferOnPostExecute(Result result) {
        }

        /**
         * Versão do {@link #onCancelled(Object)} que só é chamado quando não for haver perda de estado nas Views e apenas se não houve
         * erro na execução em background.
         *
         * @param result resultado da execução.
         */
        protected void saferOnCancelled(Result result) {
        }
    }


    /**
     * Task que aguarda um tempo para mostrar o Dialog de progresso.
     */
    private final class ShowProgressDialogTask extends AsyncTask<Void, Void, Void> {

        private final long waitTime;

        public ShowProgressDialogTask(long waitTime) {
            this.waitTime = waitTime;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                //Apenas ignora.
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (task != null && !isTaskCompleted()) {
                showDialog();
            }
        }
    }
}
