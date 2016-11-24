package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.text.ParseException;

/**
 * Representa uma máscara de formatação de valores do tipo <code>decimal</code> para textos e o parse de textos para
 * valores do tipo <code>decimal</code>.
 *
 * @author Thiago Gesser
 */
public interface IDecimalMask extends IMask {
	
	/**
	 * Formata um valor <code>decimal</code> para um texto.<br>
	 * 
	 * @param value valor <code>decimal</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatDecimal(Double value);
	
	/**
	 * Parseia um texto formatado para um valor <code>decimal</code>.<br>
	 * 
	 * @param text o texto formatado.
	 * @return o valor <code>decimal</code> ou <code>null</code> se o texto representar um valor sendo composto.
	 * @throws ParseException se o texto não estiver no formatado correto. 
	 */
	Double parseDecimal(CharSequence text) throws ParseException;
}
