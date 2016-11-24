package br.com.zalem.ymir.client.android.entity.ui.editor.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;

/**
 * Adapter padrão de editores de campos.<br>
 * A View gerada é composta apenas pelos editores considerados ativos. Por padrão, os editores visíveis são ativos, mas a subclasse pode alterar
 * este comportamento através do método {@link #isActive(AbstractFieldEditor)}.<br>
 * Os editores são dispostos em uma lista vertical, onde cada editor ocupa uma linha e entre cada linha há um separador.
 *
 * @author Thiago Gesser
 */
public class DefaultFieldEditorAdapter extends AbstractFieldEditorAdapter {

    private List<AbstractFieldEditor<?>> activeEditors;

    public DefaultFieldEditorAdapter(Context context) {
        super(context);
        activeEditors = Collections.emptyList();
    }

    public DefaultFieldEditorAdapter(Context context, List<AbstractFieldEditor<?>> editors) {
        super(context, editors);
        updateActiveEditors();
    }

    @Override
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        View rootView = inflater.inflate(R.layout.default_field_editor_list, parent, false);
        ViewGroup editorsContainer = (ViewGroup) rootView.findViewById(R.id.default_field_editor_list_editors_container);

        //Coloca as views dos editores ativos no container..
        for (AbstractFieldEditor<?> activeEditor : activeEditors) {
            View view = getEditorView(inflater, editorsContainer, activeEditor);
            if (view == null) {
                throw new NullPointerException("Field editor View can't be null: " + activeEditor.getFieldName());
            }

            editorsContainer.addView(view);
        }

        return rootView;
    }

    /**
     * Obtém a View do editor.<br>
     * Pode ser sobrescrito para modificar a View gerada.
     *
     * @param inflater inflater de layout.
     * @param container container das Views dos editores.
     * @param editor editor.
     * @return a View obtida.
     */
    protected View getEditorView(LayoutInflater inflater, ViewGroup container, AbstractFieldEditor<?> editor) {
        return editor.onCreateView(inflater, container);
    }

    /**
     * {@inheritDoc}
     * <br><br>
     * Pode ser sobrescrito para desativar a atualização dos editores ativos.
     */
    @Override
    protected void onEditorsChanged() {
        updateActiveEditors();
    }

    /**
     * Verifica se o editor está ativo. Apenas editores ativos são colocados na View gerada pelo adapter.<br>
     * Pode ser sobrescrito para alterar a regra de editores ativos.
     *
     * @param editor editor que será verificado.
     * @return <code>true</code> se o editor estiver ativo e <code>false</code> caso contrário.
     */
    protected boolean isActive(AbstractFieldEditor<?> editor) {
        return !editor.isHidden();
    }

    /**
     * Obtém os editores ativos.
     *
     * @return os editores obtidos.
     */
    protected final List<AbstractFieldEditor<?>> getActiveEditors() {
        return activeEditors;
    }


    /*
     * Métodos auxiliares
     */

    private void updateActiveEditors() {
        activeEditors = new ArrayList<>();
        for (AbstractFieldEditor<?> editor : getEditors()) {
            if (!isActive(editor)) {
                continue;
            }
            activeEditors.add(editor);
        }
    }
}
