package br.com.zalem.ymir.sample.offline.event;

import android.text.TextUtils;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;
import br.com.zalem.ymir.sample.offline.R;


/**
 * Disponibiliza métodos utilitários para o tratamento de eventos das entidades.
 *
 * @author Thiago Gesser
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * Valida se o relacionamento não é nulo.
     *
     * @param record registro da entidade
     * @param relationshipName nome do relacionamento.
     * @param errorHandler manipulador de erros da edição do registro.
     */
    public static void validateRelationshipNotNull(IEntityRecord record, String relationshipName, IEntityEditingErrorHandler errorHandler) {
        if (record.isNull(relationshipName)) {
            errorHandler.setRelationshipError(relationshipName, R.string.record_validation_not_null);
        } else {
            errorHandler.removeRelationshipError(relationshipName);
        }
    }

    /**
     * Valida se o atributo do tipo <code>decimal</code> não é nulo e é maior do que zero.
     *
     * @param record registro da entidade
     * @param attributeName nome do atributo
     * @param errorHandler manipulador de erros da edição do registro.
     */
    public static void validatePositive(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler) {
        Double currency = record.getDecimalValue(attributeName);
        if (currency == null) {
            errorHandler.setAttributeError(attributeName, R.string.record_validation_not_null);
        } else if (currency <= 0) {
            errorHandler.setAttributeError(attributeName, R.string.record_validation_positive);
        } else {
            errorHandler.removeAttributeError(attributeName);
        }
    }

    /**
     * Valida se o atributo do tipo <code>texto</code> não está vazio.
     *
     * @param record registro da entidade
     * @param attributeName nome do atributo
     * @param errorHandler manipulador de erros da edição do registro.
     */
    public static void validateNotEmpty(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler) {
        if (isEmpty(record, attributeName)) {
            errorHandler.setAttributeError(attributeName, R.string.record_validation_not_null);
        } else {
            errorHandler.removeAttributeError(attributeName);
        }
    }


    /*
     * Métodos auxiliares
     */

    private static boolean isEmpty(IEntityRecord record, String attributeName) {
        String text = record.getTextValue(attributeName);
        return text == null || text.trim().isEmpty();
    }
}
