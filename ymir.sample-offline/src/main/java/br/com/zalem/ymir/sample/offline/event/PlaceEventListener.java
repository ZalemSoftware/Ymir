package br.com.zalem.ymir.sample.offline.event;

import android.text.TextUtils;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;
import br.com.zalem.ymir.client.android.entity.ui.event.basic.EntityUIEventListenerAdapter;

import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_CITY;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_FULLADDRESS;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_NAME;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_NEIGHBORHOOD;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_POSTALCODE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_STATE;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_STREET;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ATTRIBUTE_STREETNUMBER;
import static br.com.zalem.ymir.sample.offline.EntityConstants.PLACE_ENTITY;

/**
 * Listener de eventos referentes à entidade <code>Local</code>.<br>
 * Executa as validações na edição e monta o endereço completo no detalhamento.
 *
 * @author Thiago Gesser
 */
public final class PlaceEventListener extends EntityUIEventListenerAdapter {

    public static final String STREET_NUMBER_SEPARATOR = ", ";
    public static final String CITY_SEPARATOR = " - ";
    public static final String ADRESS_SEPARATOR = "\n";

	@Override
	public String getEntityName() {
		return PLACE_ENTITY;
	}


    @Override
    public void onEditRecordAttribute(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler) {
        switch (attributeName) {
            case PLACE_ATTRIBUTE_NAME:
                ValidationUtils.validateNotEmpty(record, PLACE_ATTRIBUTE_NAME, errorHandler);
                break;
        }
    }

    @Override
	public boolean beforeSaveRecord(IEntityRecord record, boolean sync,	IEntityEditingErrorHandler errorHandler) {
        ValidationUtils.validateNotEmpty(record, PLACE_ATTRIBUTE_NAME, errorHandler);

        return !errorHandler.isEmpty();
	}


    @Override
    public void onDetailRecord(IEntityRecord record) {
        /*
         * Monta o endereço completo, conforme abaixo:
         *
         * Rua, número
         * Bairro
         * Cidade - estado
         * País
         * CEP
         */
        StringBuilder ab = new StringBuilder();

        String street = record.getTextValue(PLACE_ATTRIBUTE_STREET);
        if (!TextUtils.isEmpty(street)) {
            ab.append(street);
        }

        Integer streetNumber = record.getIntegerValue(PLACE_ATTRIBUTE_STREETNUMBER);
        if (streetNumber != null) {
            if (ab.length() > 0) {
                ab.append(STREET_NUMBER_SEPARATOR);
            }
            ab.append(streetNumber);
        }
        String neighborhood = record.getTextValue(PLACE_ATTRIBUTE_NEIGHBORHOOD);
        if (!TextUtils.isEmpty(neighborhood)) {
            if (ab.length() > 0) {
                ab.append(ADRESS_SEPARATOR);
            }
            ab.append(neighborhood);
        }

        StringBuilder csLine = new StringBuilder();
        String city = record.getTextValue(PLACE_ATTRIBUTE_CITY);
        if (!TextUtils.isEmpty(city)) {
            if (ab.length() > 0) {
                csLine.append(ADRESS_SEPARATOR);
            }
            csLine.append(city);
        }
        String state = record.getTextValue(PLACE_ATTRIBUTE_STATE);
        if (!TextUtils.isEmpty(state)) {
            if (csLine.length() > 0) {
                csLine.append(CITY_SEPARATOR);
            } else if (ab.length() > 0) {
                csLine.append(ADRESS_SEPARATOR);
            }
            csLine.append(state);
        }
        if (csLine.length() > 0) {
            ab.append(csLine);
        }

        String postalCode = record.getTextValue(PLACE_ATTRIBUTE_POSTALCODE);
        if (!TextUtils.isEmpty(postalCode)) {
            if (ab.length() > 0) {
                ab.append(ADRESS_SEPARATOR);
            }
            ab.append(postalCode);
        }

        //Seta o endereço completo no cliente sendo detalhado.
        record.setTextValue(PLACE_ATTRIBUTE_FULLADDRESS, ab.toString());
    }
}