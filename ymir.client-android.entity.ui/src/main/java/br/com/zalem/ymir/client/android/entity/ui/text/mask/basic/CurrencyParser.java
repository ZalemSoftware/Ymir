package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.text.ParseException;

/**
 * Parser genérico de números decimais que representam valores de moedas.
 *
 * @author Thiago Gesser
 */
public final class CurrencyParser {
	
	private final int decimals;

	public CurrencyParser(int decimals) {
		this.decimals = decimals;
	}
	
	/**
	 * Parseia o texto para um número decimal que representa um valor de moeda.<br>
	 * Considera que o número começa a partir das casas decimais, então se ele tiver menos dígitos que o número de casas
	 * decimais configuradas no parser, será preenchido com '0'.<br>
	 * Durante o parse, obtém apenas os dígitos, ignorando caracteres como '.', ',' e '$', por exemplo.
	 * 
	 * @param text o texto
	 * @return o número decimal parseado.
	 * @throws ParseException se houve um {@link NumberFormatException} no {@link Double#parseDouble(String)}.
	 */
	public double parseCurrency(CharSequence text) throws ParseException {
		StringBuilder numberBuilder = new StringBuilder();

		//Coloca apenas os números, desconsiderando qualquer outro caractere.
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isDigit(c)) {
				numberBuilder.append(c);
			}
		}

        if (numberBuilder.length() == 0) {
            return 0;
        }

        if (decimals > 0) {
            //Tem que possuir no mínimo as casas decimais, então completa com 0 os que faltam.
            for (int i = numberBuilder.length(); i < decimals; i++) {
                numberBuilder.insert(0, '0');
            }

            //Insere o ponto no início das casa decimais.
            numberBuilder.insert(numberBuilder.length() - decimals, '.');
        }

		try {
			return Double.parseDouble(numberBuilder.toString());
		} catch (NumberFormatException e) {
			throw new ParseException(text.toString(), -1);
		}
	}
}
