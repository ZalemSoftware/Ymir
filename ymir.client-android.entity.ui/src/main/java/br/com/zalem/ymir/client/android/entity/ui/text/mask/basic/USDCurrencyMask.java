package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalMask;

/**
 * Máscara que formata números decimais em valores monetários, no padrão de Dólares dos Estados Unidos (USD): "US$ #,##0.00".
 * 
 * @author Thiago Gesser
 */
public final class USDCurrencyMask implements IDecimalMask {

	private final DecimalFormat decimalFormat;
	private final CurrencyParser currentyParser;

	public USDCurrencyMask() {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		symbols.setGroupingSeparator(',');
		decimalFormat = new DecimalFormat("US$ #,##0.00", symbols);
		
		currentyParser = new CurrencyParser(2);
	}

	@Override
	public CharSequence formatDecimal(Double value) {
		return decimalFormat.format(value);
	}
	
	@Override
	public Double parseDecimal(CharSequence text) throws ParseException {
		return currentyParser.parseCurrency(text);
	}
}
