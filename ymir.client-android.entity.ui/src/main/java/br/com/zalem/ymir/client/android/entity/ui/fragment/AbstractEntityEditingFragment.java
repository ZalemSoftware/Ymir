package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor.OnValueChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor.FieldEditorVisitorAdapter;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.BooleanFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DateFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DecimalFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.EnumAttributeEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.ImageFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.IntegerFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TextFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TimeFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.EnumRelationshipEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.MultipleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.SingleRelationshipFieldEditor;

/**
 * Define as responsabilidades que um fragmento de edição de registro de entidade deve suportar. 
 *
 * @author Thiago Gesser
 */
public abstract class AbstractEntityEditingFragment extends AbstractThemedFragment {

    private static final String SAVED_ERROR = "SAVED_ERROR";

    private boolean refreshValues;
    private TextView errorTextView;

    private String errorText;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        errorTextView = (TextView) view.findViewById(R.id.entity_editing_error);

        if (savedInstanceState != null) {
            setError(savedInstanceState.getString(SAVED_ERROR));
        }

        if (errorText != null) {
            setError(errorText);
            errorText = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (refreshValues) {
            refreshValues();
            refreshValues = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        refreshValuesOnStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SAVED_ERROR, getError());
    }

    /**
     * Busca o editor responsável pelo atributo neste fragmento.
     *
     * @param attributeName nome do atributo.
     * @throws IllegalArgumentException se não houver um editor para o atributo neste fragmento.
     */
    @SuppressWarnings("unchecked")
    public final <T extends AbstractFieldEditor<?>> T findAttributeEditor(String attributeName) {
        return (T) findExistingEditor(attributeName, false);
    }

    /**
     * Busca o editor responsável pelo relacionamento neste fragmento.
     *
     * @param relationshipName nome do relacionamento.
     * @return o editor encontrado.
     * @throws IllegalArgumentException se não houver um editor para o relacionamento neste fragmento.
     */
    @SuppressWarnings("unchecked")
    public final <T extends AbstractFieldEditor<?>> T findRelationshipEditor(String relationshipName) {
        return (T) findExistingEditor(relationshipName, true);
    }

    /**
     * Verifica se a View do fragmento foi criada.
     *
     * @return <code>true</code> se a View foi criada e <code>false</code> caso contrário.
     */
    public final boolean isViewCreated() {
        return getView() != null;
    }


    /**
     * Define uma mensagem de erro referente à edição atual do fragmento. A mensagem é exibida de forma global, acima de todos os
     * editores.
     *
     * @param error mensagem de erro.
     */
    public final void setError(String error) {
        if (errorTextView == null) {
            //Se a View ainda não existe, armazena o texto para ser atribuído quando isto acontecer.
            errorText = error;
            return;
        }

        errorTextView.setText(error);
        errorTextView.setVisibility(TextUtils.isEmpty(error) ? View.GONE : View.VISIBLE);
    }

    /**
     * Obtém a mensagem de erro global do fragmento.
     *
     * @return a mensagem de erro obtida ou <code>null</code> se não há mensagem definida.
     */
    public final String getError() {
        CharSequence errorText = errorTextView.getText();
        if (TextUtils.isEmpty(errorText)) {
            return null;
        }

        return errorText.toString();
    }


    /**
     * Obtém todos os editors deste fragmento.
     *
     * @return os editores obtidos.
     */
    public abstract List<AbstractFieldEditor<?>> getEditors();

	/**
	 * Visita todos os editores deste fragmento utilizando o visitador definido.<br>
	 * Se qualquer visita a um editor retornar <code>true</code>, o processo será parado e os demais editores
	 * não serão visitados.
	 *
	 * @param visitor o visitador que será utilizado.
	 * @return <code>true</code> se alguma visita retornou <code>true</code> e <code>false</code> caso contrário.
	 */
	public abstract boolean visitEditors(IFieldEditorVisitor visitor);

    /**
     * Busca o editor responsável pelo campo (atributo ou relacionamento) neste fragmento.<br>
     * É mais indicado utilizar os métodos {@link #findAttributeEditor(String)} e {@link #findRelationshipEditor(String)}, pois eles
     * validam a existência e o tipo do campo.
     *
     * @param fieldName nome do campo.
     * @return o editor encontrado ou <code>null</code> se não houver um editor para o campo.
     */
    public abstract <T extends AbstractFieldEditor<?>> T findEditor(String fieldName);

    /**
     * Posiciona o fragmento de forma que o editor do campo seja mostrado.
     *
     * @param fieldName o nome do campo cujo o editor deve ser mostrado.
     */
    public abstract void scrollToEditor(String fieldName);

	/**
	 * Carrega os valores dos campos que os editores deste fragmento representam do registro, passando a mostrar os valores carregados
	 * nas Views dos editores.<br>
	 * 
	 * @param record o registro que terá os valores dos campos carregados.
	 * @param clearDirt indica se a sujeira dos editores deve ser limpa.
	 */
	public abstract void loadValues(IEntityRecord record, boolean clearDirt);
	
	/**
	 * Armazena os valores atuais dos editores deste fragmento nos campos que eles representam do registro.<br>
	 * 
	 * @param record o registro que terá os valores dos campos armazenados.
	 * @param clearDirt indica se a sujeira dos editores deve ser limpa.
	 */
	public abstract void storeValues(IEntityRecord record, boolean clearDirt);
	
	/**
	 * Indica se pelo menos um dos editores deste fragmento está sujo.
	 * 
	 * @return <code>true</code> se algum editor está sujo e <code>false</code> caso contrário.
	 */
	public abstract boolean isDirty();
	
	/**
	 * Define o listener de alteração de valor dos editores deste fragmento.
	 * 
	 * @param valueChangeListener listener de alteração de valor.
	 */
	public abstract void setFieldEditorsValueChangeListener(OnValueChangeListener valueChangeListener);


    /**
     * Indica que os valores dos editores do fragmento devem ser atualizados no {@link #onStart()}.
     */
    protected final void refreshValuesOnStart() {
        refreshValues = true;
    }

    /**
     * Indica que os valores dos editores do fragmento não devem ser atualizados no {@link #onStart()}.
     */
    protected final void dontRefreshValuesOnStart() {
        refreshValues = false;
    }

    /**
     * Atualiza os valores dos editores do fragmento.
     */
    protected final void refreshValues() {
        visitEditors(new RefreshEditorValueVisitor());
    }

    /**
     * Verifica se a View do fragmento já foi criada.
     *
     * @throws IllegalStateException se a View ainda não foi criada.
     */
    protected final void checkViewCreated() {
        if (!isViewCreated()) {
            throw new IllegalStateException(getClass().getSimpleName() +  "'s View is not created yet.");
        }
    }


    /*
     * Métodos/classes auxiliares
     */

    private AbstractFieldEditor findExistingEditor(String fieldName, boolean isRelationship) {
        FindFieldEditorVisitor visitor = new FindFieldEditorVisitor(fieldName, isRelationship);
        visitEditors(visitor);

        AbstractFieldEditor editor = visitor.getResult();
        if (editor == null) {
            throw new IllegalArgumentException(String.format("Editor for the %s \"%s\" not found.", isRelationship ? "relationship" : "attribute", fieldName));
        }
        return editor;
    }


    /**
     * Visitador utilizado para encontrar um editor através do nome de seu campo.
     */
    private static final class FindFieldEditorVisitor implements IFieldEditorVisitor {

        private final String fieldName;
        private final boolean isRelationship;
        private AbstractFieldEditor<?> result;

        private FindFieldEditorVisitor(String fieldName, boolean isRelationship) {
            this.fieldName = fieldName;
            this.isRelationship = isRelationship;
        }

        public AbstractFieldEditor<?> getResult() {
            return result;
        }

        @Override
        public boolean visit(IntegerFieldEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(DecimalFieldEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(TextFieldEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(BooleanFieldEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(DateFieldEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(TimeFieldEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(ImageFieldEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(EnumAttributeEditor editor) {
            return checkFieldEditor(editor, false);
        }

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
            return checkFieldEditor(editor, true);
        }

        @Override
        public boolean visit(SingleRelationshipFieldEditor editor) {
            return checkFieldEditor(editor, true);
        }

        @Override
        public boolean visit(MultipleRelationshipFieldEditor editor) {
            return checkFieldEditor(editor, true);
        }

        private boolean checkFieldEditor(AbstractFieldEditor<?> editor, boolean isRelationship) {
            if (this.isRelationship != isRelationship || !editor.getFieldName().equals(fieldName)) {
                return false;
            }

            result = editor;
            return true;
        }
    }

    /**
     * Visitador que atualiza os valores de editores que podem ter sido alterados externamente.
     * Atualmente, atualiza apenas os valores de editores de relacionamentos que apontam para associações, removendo registros que já foram excluidos.
     */
    private static class RefreshEditorValueVisitor extends FieldEditorVisitorAdapter {

        @Override
        public boolean visit(MultipleRelationshipFieldEditor editor) {
            if (MetadataUtils.isComposition(editor.getRelationshipType())) {
                return false;
            }

            editor.refreshValues();
            return false;
        }

        @Override
        public boolean visit(SingleRelationshipFieldEditor editor) {
            if (MetadataUtils.isComposition(editor.getRelationshipType())) {
                return false;
            }

            editor.refreshValue();
            return false;
        }

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
            if (MetadataUtils.isComposition(editor.getRelationshipType())) {
                return false;
            }

            editor.refreshValue();
            return false;
        }
    }
}
