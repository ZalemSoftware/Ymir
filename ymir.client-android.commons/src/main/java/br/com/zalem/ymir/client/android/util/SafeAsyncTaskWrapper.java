package br.com.zalem.ymir.client.android.util;

/**
 * Engloba um {@link SafeAsyncTask} e chama seus respectivos métodos, permitindo adicionar um comportamento à uma task já existente.
 *
 * @author Thiago Gesser
 */
public abstract class SafeAsyncTaskWrapper <Params, Progress, Result> extends SafeAsyncTask<Params, Progress, Result> {

    private final SafeAsyncTask<Params, Progress, Result> task;

    public SafeAsyncTaskWrapper(SafeAsyncTask<Params, Progress, Result> task) {
        this.task = task;
    }

    @Override
    protected void onPreExecute() {
        task.onPreExecute();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onProgressUpdate(Progress... values) {
        task.onProgressUpdate(values);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Result safeDoInBackground(Params... params) throws Exception {
        return task.safeDoInBackground(params);
    }

    @Override
    protected void safeOnCancelled(Result result) {
        task.safeOnCancelled(result);
    }

    @Override
    protected void safeOnPostExecute(Result result) {
        task.safeOnPostExecute(result);
    }

    public SafeAsyncTask<Params, Progress, Result> getTask() {
        return task;
    }
}
