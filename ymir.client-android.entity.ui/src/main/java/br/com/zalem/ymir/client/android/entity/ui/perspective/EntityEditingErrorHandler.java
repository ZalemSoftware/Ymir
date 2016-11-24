package br.com.zalem.ymir.client.android.entity.ui.perspective;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractHelperFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
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
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityEditingFragment;
import br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils;

/**
 * Manipulador de mensagens erro de edição utilizado pelo {@link EntityEditingPerspective}.<br>
 * A perspectiva deve chamar o método {@link #onSaveState(Bundle)} do manipulador durante o salvamento de seu estado.<br>
 * <br>
 * Permite a utilização de uma mensagem global de erro automática através do método {@link #setAutomaticGlobalError()}.
 *
 * @author Thiago Gesser
 */
final class EntityEditingErrorHandler implements IEntityEditingErrorHandler {

    private static final String SAVED_ERROR_FIELDS = "SAVED_ERROR_FIELDS";
    private static final String SAVED_USING_AUTOMATIC_GLOBAL_ERROR = "SAVED_USING_AUTOMATIC_GLOBAL_ERROR";

    private final Context context;
    private final AbstractEntityEditingFragment editingFragment;
    private final TreeSet<String> errorFields;
    private boolean usingAutomaticGlobalError;

	EntityEditingErrorHandler(Context context, AbstractEntityEditingFragment editingFragment, Bundle savedState) {
        this.context = context;
        this.editingFragment = editingFragment;
        this.errorFields = new TreeSet<>(new FieldEditorOrderComparator(editingFragment.getEditors()));

        if (savedState != null) {
            errorFields.addAll(savedState.getStringArrayList(SAVED_ERROR_FIELDS));
            usingAutomaticGlobalError = savedState.getBoolean(SAVED_USING_AUTOMATIC_GLOBAL_ERROR);
        }
    }

    @Override
    public void setGlobalError(int errorMsgResId) {
        setGlobalError(getErrorString(errorMsgResId));
    }

    @Override
    public void setGlobalError(String errorMsg) {
        internalSetGlobalError(errorMsg);
        usingAutomaticGlobalError = false;
    }

    @Override
    public void removeGlobalError() {
        internalSetGlobalError(null);
        usingAutomaticGlobalError = false;
    }

    @Override
    public String getGlobalError() {
        return editingFragment.getError();
    }


    @Override
    public void setAttributeError(String attributeName, int errorMsgResId) {
        setAttributeError(attributeName, getErrorString(errorMsgResId));
    }

    @Override
    public void setAttributeError(String attributeName, String errorMsg) {
        setFieldError(attributeName, false, errorMsg);
    }

    @Override
    public void removeAttributeError(String attributeName) {
        setFieldError(attributeName, false, null);
    }

    @Override
    public String getAttributeError(String attributeName) {
        AbstractHelperFieldEditor editor = editingFragment.findAttributeEditor(attributeName);
        return editor.getError();
    }


    @Override
    public void setRelationshipError(String relationshipName, int errorMsgResId) {
        setRelationshipError(relationshipName, getErrorString(errorMsgResId));
    }

    @Override
    public void setRelationshipError(String relationshipName, String errorMsg) {
        setFieldError(relationshipName, true, errorMsg);
    }

    @Override
    public void removeRelationshipError(String relationshipName) {
        setFieldError(relationshipName, true, null);
    }

    @Override
    public String getRelationshipError(String relationshipName) {
        AbstractHelperFieldEditor editor = editingFragment.findRelationshipEditor(relationshipName);
        return editor.getError();
    }


    @Override
    public void setMultipleRelationshipRecordError(String relationshipName, IEntityRecord record, int errorMsgResId) {
        setMultipleRelationshipRecordError(relationshipName, record, getErrorString(errorMsgResId));
    }

    @Override
    public void setMultipleRelationshipRecordError(String relationshipName, IEntityRecord record, String errorMsg) {
        MultipleRelationshipFieldEditor editor = editingFragment.findRelationshipEditor(relationshipName);
        editor.setRecordError(record, errorMsg);

        addErrorField(relationshipName);
    }

    @Override
    public void removeMultipleRelationshipRecordError(String relationshipName, IEntityRecord record) {
        MultipleRelationshipFieldEditor editor = editingFragment.findRelationshipEditor(relationshipName);
        editor.setRecordError(record, null);

        tryRemoveErrorField(editor);
    }

    @Override
    public void clearMultipleRelationshipRecordsErrors(String relationshipName) {
        MultipleRelationshipFieldEditor editor = editingFragment.findRelationshipEditor(relationshipName);
        editor.clearRecordsErrors();

        tryRemoveErrorField(editor);
    }

    @Override
    public String getMultipleRelationshipRecordError(String relationshipName, IEntityRecord record) {
        MultipleRelationshipFieldEditor editor = editingFragment.findRelationshipEditor(relationshipName);
        return editor.getRecordError(record);
    }

    @Override
	public boolean isEmpty() {
        return errorFields.isEmpty();
    }

    @Override
    public void clearAll() {
        if (errorFields.isEmpty()) {
            return;
        }


        editingFragment.visitEditors(new ClearFieldErrorVisitor());
        errorFields.clear();
        if (usingAutomaticGlobalError) {
            removeGlobalError();
        }
    }


    /*
     * Métodos para uso da própria API.
     */

    /**
     * Define que a mensagem global de erro deve ser gerada automaticamente, baseado nos campos com erro atuais. A mensagem é
     * atualizada conforme os campos com erro são alterados.<br>
     * Para remover a mensagem automatica, basta definiru uma nova mensagem através do {@link #setGlobalError(String)} ou removê-la
     * através do {@link #removeGlobalError()}.
     */
    void setAutomaticGlobalError() {
        usingAutomaticGlobalError = true;
        if (errorFields.isEmpty()) {
            //Mensagem genérica, utilizada para ilustrar a estranha situação em que a mensagem automática foi ativada mas nao há campos com erro.
            internalSetGlobalError(context.getString(R.string.entity_editing_automatic_global_error_empty));
        } else {
            //Só mostra a mensagem se houver mais do que um campo com erro.
            updateAutomaticGlobalError();
        }
    }

    /**
     * Indica se há uma mensagem global de erro definida de forma manual, ou seja, não através do {@link #setAutomaticGlobalError()}.
     *
     * @return <code>true</code> se há uma mensagem de erro manual e <code>false</code> caso contrário.
     */
    boolean hasManualGlobalError() {
        return !usingAutomaticGlobalError && !TextUtils.isEmpty(getGlobalError());
    }

    /**
     * Obtém os campos que possuem erro atualmente.
     *
     * @return os nomes dos campos que possuem erro.
     */
    SortedSet<String> getErrorFields() {
        return errorFields;
    }

    /**
     * Deve ser chamado quando a Perspectiva/Activity está salvando seu estado para que o estado interno do manipulador possa
     * ser salvo também.
     *
     * @param outState bundle onde o estado será salvo.
     */
    void onSaveState(Bundle outState) {
        outState.putStringArrayList(SAVED_ERROR_FIELDS, new ArrayList<>(errorFields));
        outState.putBoolean(SAVED_USING_AUTOMATIC_GLOBAL_ERROR, usingAutomaticGlobalError);
    }

    /**
     * Notifica que houve uma alteração em um editor de relacionamento múltiplo, o que pode afetar a mensagem global de erro automatica.
     *
     * @param editor editor de relacionamento múltiplo alterado.
     */
    void notifyMultipleRelationshipEditorValueChanged(AbstractHelperFieldEditor<List<IEntityRecord>> editor) {
        tryRemoveErrorField(editor);
    }

	
	/*
	 * Métodos auxiliares.
	 */

    @SuppressWarnings("StringEquality")
    private void setFieldError(String fieldName, boolean isRelationship, String errorMsg) {
        AbstractHelperFieldEditor editor;
        if (isRelationship) {
            editor = editingFragment.findRelationshipEditor(fieldName);
        } else {
            editor = editingFragment.findAttributeEditor(fieldName);
        }

        //Otimização, utiliza o == justamente para ser rápido e identificar a situação mais comum de Strings nulas ou iguais, pois elas vem de recursos Android.
        if (editor.getError() == errorMsg) {
            return;
        }

        editor.setError(errorMsg);
        if (TextUtils.isEmpty(errorMsg)) {
            tryRemoveErrorField(editor);
        } else {
            addErrorField(fieldName);
        }
    }

    @Nullable
    private String getErrorString(int errorResId) {
        if (errorResId <= 0) {
            return null;
        }
        return context.getString(errorResId);
    }

    private void addErrorField(String fieldName) {
        if (errorFields.add(fieldName)) {
            tryUpdateDefaultGlobalError();
        }
    }

    private void tryRemoveErrorField(AbstractHelperFieldEditor<?> editor) {
        //Se ainda tem erro, não remove.
        if (editor.accept(HasErrorFieldVisitor.SINGLETON)) {
            return;
        }

        if (errorFields.remove(editor.getFieldName())) {
            tryUpdateDefaultGlobalError();
        }
    }

    private void internalSetGlobalError(String errorMsg) {
        editingFragment.setError(errorMsg);
    }

    private void tryUpdateDefaultGlobalError() {
        if (!usingAutomaticGlobalError) {
            return;
        }

        if (errorFields.isEmpty()) {
            removeGlobalError();
        } else {
            updateAutomaticGlobalError();
        }
    }

    private void updateAutomaticGlobalError() {
        int errorFieldsSize = errorFields.size();
        String[] fieldsLabels = new String[errorFieldsSize];
        int fieldIndex = 0;
        for (String fieldName : errorFields) {
            AbstractFieldEditor<?> editor = editingFragment.findEditor(fieldName);
            fieldsLabels[fieldIndex++] = editor.getLabel();
        }

        String fieldsList = MessageUtils.createWordsList(editingFragment.getActivity(), fieldsLabels);
        String message = MessageUtils.createMessage(context,
                                                    errorFieldsSize == 1 ? R.string.entity_editing_automatic_global_error_format_singular : R.string.entity_editing_automatic_global_error_format_plural,
                                                    fieldsList);
        internalSetGlobalError(message);
    }


    /*
	 * Classes auxiliares.
	 */

    /**
     * Visitador que limpa as mensagens de erro dos campos.
     */
    private static final class ClearFieldErrorVisitor implements IFieldEditorVisitor {

        @Override
        public boolean visit(IntegerFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(DecimalFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(TextFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(BooleanFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(DateFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(TimeFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(ImageFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(SingleRelationshipFieldEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(EnumAttributeEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
            editor.setError(null);
            return false;
        }

        @Override
        public boolean visit(MultipleRelationshipFieldEditor editor) {
            editor.setError(null);
            editor.clearRecordsErrors();
            return false;
        }
    }

    /**
     * Visitador que verifica se os campos possuem erro.
     */
    private static final class HasErrorFieldVisitor implements IFieldEditorVisitor {

        public static final HasErrorFieldVisitor SINGLETON = new HasErrorFieldVisitor();

        @Override
        public boolean visit(IntegerFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(DecimalFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(TextFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(BooleanFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(DateFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(TimeFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(ImageFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(SingleRelationshipFieldEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(EnumAttributeEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
            return editor.hasError();
        }

        @Override
        public boolean visit(MultipleRelationshipFieldEditor editor) {
            return editor.hasError() || editor.hasRecordErrors();
        }
    }

    /**
     * Comparador de campos de edição que ordena-os de acordo com sua posição.
     */
    private static final class FieldEditorOrderComparator implements Comparator<String> {

        private final Map<String, Integer> editorsIndexes;

        public FieldEditorOrderComparator(List<AbstractFieldEditor<?>> editors) {
            int editorsSize = editors.size();
            editorsIndexes = new HashMap<>(editorsSize);

            for (int i = 0; i < editorsSize; i++) {
                editorsIndexes.put(editors.get(i).getFieldName(), i);
            }
        }

        @Override
        public int compare(String lhs, String rhs) {
            return editorsIndexes.get(lhs) - editorsIndexes.get(rhs);
        }
    }
}
