package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalMask;

/**
 * Máscara que formata números decimais em valores monetários de acordo com o localidade do usuário.
 *
 * @author Thiago Gesser
 */
public final class DefaultCurrencyMask implements IDecimalMask {

	private final DecimalFormat currencyFormatter;
	private final CurrencyParser currentyParser;

	public DefaultCurrencyMask() {
		currencyFormatter = (DecimalFormat) NumberFormat.getCurrencyInstance();
        adjustCurrencySymbol(currencyFormatter);
        currentyParser = new CurrencyParser(currencyFormatter.getCurrency().getDefaultFractionDigits());
	}


    @Override
	public CharSequence formatDecimal(Double value) {
		return currencyFormatter.format(value);
	}

	@Override
	public Double parseDecimal(CharSequence text) throws ParseException {
        double number = currentyParser.parseCurrency(text);
        return tryApplyCursorProblemWorkaround(text, number);
	}


    /*
     * Métodos auxiliares
     */

    //Adiciona um espaço entre o símbolo da moeda e o valor, caso ainda não houver.
    private void adjustCurrencySymbol(DecimalFormat currencyFormatter) {
        String pattern = currencyFormatter.toPattern();
        DecimalFormatSymbols symbols = currencyFormatter.getDecimalFormatSymbols();
        String currencySymbol = symbols.getCurrencySymbol();

        String modifiedSymbol = getSpacedCurrencySymbol(pattern, currencySymbol);
        if (modifiedSymbol != null) {
            symbols.setCurrencySymbol(modifiedSymbol);
            currencyFormatter.setDecimalFormatSymbols(symbols);
        }
    }

    private String getSpacedCurrencySymbol(String pattern, String currencySymbol) {
        //Busca pelo símbolo diretamente no formato.
        int symbolIndex = pattern.indexOf(currencySymbol);
        int symbolLength = currencySymbol.length();
        //Se o símbolo não está diretamente no formato, então deve estar representado pelo caractere ¤.
        if (symbolIndex == -1) {
            symbolIndex = pattern.indexOf("¤");
            symbolLength = 1;
        }

        if (symbolIndex > -1) {
            //Se o símbolo da moeda aparece no início, tenta adicionar um espaço depois dele, se não antes.
            if (symbolIndex == 0) {
                char nextChar = pattern.charAt(symbolIndex + symbolLength);
                //Só adiciona o espaço se ainda não possui.
                if (!Character.isSpaceChar(nextChar)) {
                    return currencySymbol + " ";
                }
            } else {
                applyCursorProblemWorkaround = true;

                char prevChar = pattern.charAt(symbolIndex - symbolLength);
                if (!Character.isSpaceChar(prevChar)) {
                    return " " + currencySymbol;
                }
            }
        }

        //Não foi necessário o espaço, então não altera o símbolo.
        return null;
    }


    /*
     * TODO
     * Contorno para o problema do "backspace" não funcionar quando o cursor está no último caractere em campos de moeda cujo o símbolo aparece no final.
     * Isto pode ser removido quando o esquema de máscara for refeito para permitir maior conrole do texto (incluindo posição do cursor e etc, através do android.text.Editable)
     * ou quando os campos tiverem o botão X no canto para anular o valor.
     *
     * O contorno identifica quando um "backspace" foi pressionado e retira um dígito do valor. Isto é feito através dos caracteres atuais
     * do texto, que estarão sem os últimos caso o "backspace" tenha sido pressioando quando o cursor estava no final.
     */
    private boolean applyCursorProblemWorkaround;
    private double tryApplyCursorProblemWorkaround(CharSequence text, double number) throws ParseException {
        if (!applyCursorProblemWorkaround) {
            return number;
        }

        String formattedText = currencyFormatter.format(number);
        for (int ti = text.length() -1, fi = formattedText.length() -1; ti >= 0 && fi >= 0; ti--, fi--) {
            char tc = text.charAt(ti);
            char fc = formattedText.charAt(fi);

            if (Character.isDigit(tc) || Character.isDigit(fc)) {
                break;
            }

            if (tc != fc) {
                String numberString = String.valueOf(number);
                numberString = numberString.substring(0, numberString.length() -1);
                return currentyParser.parseCurrency(numberString);
            }
        }

        return number;
    }
}
