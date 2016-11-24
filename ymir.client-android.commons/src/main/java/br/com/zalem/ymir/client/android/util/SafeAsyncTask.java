package br.com.zalem.ymir.client.android.util;

import android.os.AsyncTask;

/**
 * AsyncTask que captura qualquer erro ocorrido na execução em background para tratá-lo na Thread de UI.<br>
 * Por padrão, o erro é lançado para interromper a aplicação, mas o metodo {@link #onError(Exception)} pode ser sobrescrito para alterar este comportamento.
 *
 * @author Thiago Gesser
 */
public abstract class SafeAsyncTask <Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private Exception error;

    @Override
    @SuppressWarnings("unchecked")
    protected final Result doInBackground(Params... params) {
        try {
            return safeDoInBackground(params);
        } catch (Exception e) {
            error = e;
        }
        return null;
    }

    @Override
    protected final void onCancelled(Result result) {
        if (handleError()) {
            safeOnCancelled(result);
        }
    }

    @Override
    protected final void onPostExecute(Result result) {
        if (handleError()) {
            safeOnPostExecute(result);
        }
    }


    //Apenas expõe o método para o SafeAsyncTaskWrapper.
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    //Apenas expõe o método para o SafeAsyncTaskWrapper.
    @SuppressWarnings("unchecked")
    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
    }


    /**
     * Chamado quando ouve um erro na execução em background. Possibilita um tratamento diferente do padrão, que é lançar o erro na Thread de UI
     * e interromper a aplicação.
     *
     * @param exception erro ocorrido em background.
     * @return <code>true</code> se o erro foi tratado e <code>false</code> se ele deve seguir o tratamento padrão e ser lançado na Thread de UI.
     */
    @SuppressWarnings("UnusedParameters")
    protected boolean onError(Exception exception) {
        return false;
    }

    /**
     * Versão do {@link #doInBackground(Object[])} que captura qualquer erro ocorrido para tratá-lo posteriormente na Thread de UI. Por padrão,
     * o erro é lançado, interrompendo a aplicação. Entretanto, o metodo {@link #onError(Exception)} pode ser sobrescrito para alterar este comportamento.
     *
     * @param params parâmetros da execuçao.
     * @return resultado da execuçao.
     * @throws Exception se ouve algum erro na execução.
     */
    @SuppressWarnings("unchecked")
    protected abstract Result safeDoInBackground(Params... params) throws Exception;

    /**
     * Versão do {@link #onPostExecute(Object)} que só é chamado se não houve erro na execução em background.
     *
     * @param result resultado da execução.
     */
    @SuppressWarnings("UnusedParameters")
    protected void safeOnPostExecute(Result result) {
    }

    /**
     * Versão do {@link #onCancelled(Object)} que só é chamado se não houve erro na execução em background.
     *
     * @param result resultado da execução.
     */
    @SuppressWarnings("UnusedParameters")
    protected void safeOnCancelled(Result result) {
    }


    private boolean handleError() {
        if (error == null) {
            return true;
        }

        if (onError(error)) {
            return false;
        }
        throw new RuntimeException(error);
    }
}
