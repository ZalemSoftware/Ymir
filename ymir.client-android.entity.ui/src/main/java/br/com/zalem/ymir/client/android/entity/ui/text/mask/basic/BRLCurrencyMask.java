package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalMask;

/**
 * Máscara que formata números decimais em valores monetários, no padrão de Reais do Brasil (BRL): "R$ #.##0,00".
 *
 * @author Thiago Gesser
 */
public final class BRLCurrencyMask implements IDecimalMask {

	private final DecimalFormat currencyFormatter;
	private final CurrencyParser currentyParser;

	public BRLCurrencyMask() {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
		currencyFormatter = new DecimalFormat("R$ #,##0.00", symbols);
		
		currentyParser = new CurrencyParser(2); 
	}

	@Override
	public CharSequence formatDecimal(Double value) {
		return currencyFormatter.format(value);
	}

	@Override
	public Double parseDecimal(CharSequence text) throws ParseException {
		return currentyParser.parseCurrency(text);
	}
}
