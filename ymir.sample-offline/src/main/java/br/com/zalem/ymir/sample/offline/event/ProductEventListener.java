package br.com.zalem.ymir.sample.offline.event;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;
import br.com.zalem.ymir.client.android.entity.ui.event.basic.EntityUIEventListenerAdapter;

import static br.com.zalem.ymir.sample.offline.EntityConstants.PRODUCT_ATTRIBUTE_NAME;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PRODUCT_ATTRIBUTE_PRICE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PRODUCT_ATTRIBUTE_TYPE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PRODUCT_ENTITY;

/**
 * Listener de eventos referentes à entidade <code>Produto</code>.
 * Apenas executa as validações na edição.
 *
 * @author Thiago Gesser
 */
public final class ProductEventListener extends EntityUIEventListenerAdapter {

	@Override
	public String getEntityName() {
		return PRODUCT_ENTITY;
	}


    @Override
    public void onEditRecordAttribute(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler) {
        switch (attributeName) {
            case PRODUCT_ATTRIBUTE_NAME:
                ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_NAME, errorHandler);
                break;

            case PRODUCT_ATTRIBUTE_PRICE:
                ValidationUtils.validatePositive(record, PRODUCT_ATTRIBUTE_PRICE, errorHandler);
                break;

            case PRODUCT_ATTRIBUTE_TYPE:
                ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_TYPE, errorHandler);
                break;
        }
    }

    @Override
	public boolean beforeSaveRecord(IEntityRecord record, boolean sync,	IEntityEditingErrorHandler errorHandler) {
        //Revalida todos os atributos.
        ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_NAME, errorHandler);
        ValidationUtils.validatePositive(record, PRODUCT_ATTRIBUTE_PRICE, errorHandler);
        ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_TYPE, errorHandler);

        return !errorHandler.isEmpty();
	}
}