package br.com.zalem.ymir.client.android.entity.ui.configuration;


/**
 * Máscaras básicas disponíveis para a formatação dos valores dos campos das entidades.
 *
 * @author Thiago Gesser
 */
public enum BasicMaskType {
	
	DATE_DEFAULT,
	DATE_BRAZILIAN,
	DATE_INTERNATIONAL,

	TIME_DEFAULT,
	TIME_12H,
	TIME_24H,

	DATETIME_DEFAULT,
	DATETIME_BRAZILIAN,
	DATETIME_INTERNATIONAL,

	CURRENCY_DEFAULT,
	CURRENCY_BRL,
	CURRENCY_USD,
	
	BOOLEAN_YES_NO,

    IMAGE_CIRCULAR
}