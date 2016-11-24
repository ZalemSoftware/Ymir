package br.com.zalem.ymir.client.android.entity.ui.editor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;

/**
 * Base para adapters de editores de campos. Mantém uma lista de editores a fim de adaptá-los para uma View que os contém.<br>
 * Os editores podem definidos no construtor ou pelo método {@link #setEditors}. A View pode ser gerada através do método {@link #getView(LayoutInflater, ViewGroup)}.
 * É possível adicionar um listener no adapter atraves do método {@link #addListener(IFieldEditorAdapterListener)} para ser avisado da alteração
 * dos editores. Se isto acontecer, a View gerada anteriormente pode se tornar inválida então é recomendado gerá-la novamente.<br>
 * <br>
 * As subclasses precisam implementar o método {@link #getView(LayoutInflater, ViewGroup)} para gerar a View adaptada. Cabe a implementação definir o layout das Views e quais editores estarão presentes.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractFieldEditorAdapter {

    private final List<IFieldEditorAdapterListener> listeners;
    private final Context context;
    private List<AbstractFieldEditor<?>> editors;

    public AbstractFieldEditorAdapter(Context context) {
        this(context, Collections.<AbstractFieldEditor<?>>emptyList());
    }

    public AbstractFieldEditorAdapter(Context context, List<AbstractFieldEditor<?>> editors) {
        this.context = context;
        this.editors = editors;
        this.listeners = new ArrayList<>();
    }

    /**
     * Define os editores do adapter, notificando a alteração para que os listeners possam recriar as Views de edição.
     *
     * @param editors novos editores do adapter.
     */
    public final void setEditors(List<AbstractFieldEditor<?>> editors) {
        if (editors == null) {
            throw new IllegalArgumentException("editors == null");
        }

        this.editors = editors;
        notifyEditorsChanged();
    }

    /**
     * Notifica que os editores foram alterados, possibilitando aos listeners recriar as Views de edição.
     */
    public void notifyEditorsChanged() {
        //Avisa à subclasse da alteração.
        onEditorsChanged();

        //Avisa aos listeners da alteração.
        for (IFieldEditorAdapterListener listener : listeners) {
            listener.onEditorsChanged();
        }
    }

    /**
     * Cria a View correspondente aos editores deste adapter.<br>
     * A View não deve ser adicionada no ViewGroup pai.
     *
     * @param inflater inflater de layout.
     * @param parent o ViewGroup que será o pai da View.
     * @return a View obtida.
     */
    public abstract View getView(LayoutInflater inflater, ViewGroup parent);

    /**
     * Chamado após a alteração dos editores. Pode ser sobrescrito para tratar esta situação.
     */
    protected void onEditorsChanged() {
    }

    /**
     * Obtém os editores disponíveis neste adapter.
     *
     * @return os editores obtidos.
     */
    public final List<AbstractFieldEditor<?>> getEditors() {
        return editors;
    }

    /**
     * Obtém o número de editores disponiveis neste adapter.
     *
     * @return o número de editores obtido.
     */
    public final int getCount() {
        return editors.size();
    }

    /**
     * Obtém o editor referente ao índice.
     *
     * @param index índice do editor.
     * @return o editor obtido.
     * @throws IndexOutOfBoundsException se o índice for < 0 ou >= {@link #getCount}
     */
    public final AbstractFieldEditor<?> getEditor(int index) {
        return editors.get(index);
    }

    /**
     * Adiciona o listener no adapter.
     *
     * @param listener listener do adapter
     */
    public final void addListener(IFieldEditorAdapterListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove o listener no adapter.
     *
     * @param listener listener do apater
     */
    public final void removeListener(IFieldEditorAdapterListener listener) {
        listeners.remove(listener);
    }

    /**
     * Obtém o contexto do adapter.
     * @return o contexto obtido.
     */
    public final Context getContext() {
        return context;
    }


    /**
     * Listener das ações ocorridas no adapter de editores de campos.
     */
    public interface IFieldEditorAdapterListener {

        /**
         * Chamado quando os editores foram alterados, fazendo com que a View gerada anteriormente fique inválida.
         */
        void onEditorsChanged();
    }
}
