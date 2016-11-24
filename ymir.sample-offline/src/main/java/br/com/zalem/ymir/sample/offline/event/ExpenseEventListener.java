package br.com.zalem.ymir.sample.offline.event;

import com.google.inject.Inject;

import java.sql.Date;

import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;
import br.com.zalem.ymir.client.android.entity.ui.event.basic.EntityUIEventListenerAdapter;
import br.com.zalem.ymir.sample.offline.R;

import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_ATTRIBUTE_DATE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_ATTRIBUTE_VALUE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_ENTITY;
import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_RELATIONSHIP_PLACE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_RELATIONSHIP_PRODUCT;
import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_VATTRIBUTE_QUANTITY;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ENTITY;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PRODUCT_ATTRIBUTE_PRICE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PRODUCT_ENTITY;

/**
 * Listener de eventos referentes à entidade <code>Gasto</code>.
 * Apenas executa as validações na edição.
 *
 * @author Thiago Gesser
 */
public final class ExpenseEventListener extends EntityUIEventListenerAdapter {

    @Inject
    private IEntityDataManager dataManager;

	@Override
	public String getEntityName() {
		return EXPENSE_ENTITY;
	}


    @Override
    public void onStartEditRecord(IEntityRecord record, IEntityEditingErrorHandler errorHandler) {
        //Se é um novo registro, define o valor inicial do campo "date".
        if (record.isNew() && !record.isDirty(EXPENSE_ATTRIBUTE_DATE)) {
            Date today = new Date(new java.util.Date().getTime());
            record.setDateValue(EXPENSE_ATTRIBUTE_DATE, today);
        }

        //Apresenta uma mensagem caso ainda não existam produtos. Apenas para demonstrar o uso do data manager e da mensagem global.
        checkProductsPlacesExistence(errorHandler);
    }

    @Override
    public void onEditRecordAttribute(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler) {
        if (attributeName.equals(EXPENSE_VATTRIBUTE_QUANTITY)) {
            ValidationUtils.validatePositive(record, EXPENSE_VATTRIBUTE_QUANTITY, errorHandler);
            calculateValue(record);
        }
    }

    @Override
    public void onEditRecordRelationship(IEntityRecord record, String relationshipName, IEntityEditingErrorHandler errorHandler) {
        if (relationshipName.equals(EXPENSE_RELATIONSHIP_PRODUCT)) {
            ValidationUtils.validateRelationshipNotNull(record, EXPENSE_RELATIONSHIP_PRODUCT, errorHandler);
            calculateValue(record);
        }

        checkProductsPlacesExistence(errorHandler);
    }

    @Override
	public boolean beforeSaveRecord(IEntityRecord record, boolean sync,	IEntityEditingErrorHandler errorHandler) {
        //Revalida todos os campos.
        ValidationUtils.validateRelationshipNotNull(record, EXPENSE_RELATIONSHIP_PRODUCT, errorHandler);
        ValidationUtils.validatePositive(record, EXPENSE_VATTRIBUTE_QUANTITY, errorHandler);

        return !errorHandler.isEmpty();
	}


    /*
     * Métodos auxiliares
     */

    private void checkProductsPlacesExistence(IEntityEditingErrorHandler errorHandler) {
        if (dataManager.getEntityDAO(PRODUCT_ENTITY).isEmpty()) {
            errorHandler.setGlobalError(R.string.expense_editing_no_product_place);
        } else {
            errorHandler.setGlobalError(null);
        }
    }

    private static void calculateValue(IEntityRecord expense) {
        double value = 0;

        IEntityRecord product = expense.getRelationshipValue(EXPENSE_RELATIONSHIP_PRODUCT);
        Double quantity = expense.getDecimalValue(EXPENSE_VATTRIBUTE_QUANTITY);
        if (product != null && quantity != null && quantity > 0) {
            double price = product.getDecimalValue(PRODUCT_ATTRIBUTE_PRICE);
            value = quantity * price;
        }

        expense.setDecimalValue(EXPENSE_ATTRIBUTE_VALUE, value);
    }
}