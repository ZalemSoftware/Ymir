package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.text.ParseException;

/**
 * Representa uma máscara de formatação de valores do tipo <code>inteiro</code> para textos e o parse de textos para
 * valores do tipo <code>inteiro</code>.
 *
 * @author Thiago Gesser
 */
public interface IIntegerMask extends IMask {
	
	/**
	 * Formata um valor <code>inteiro</code> para um texto.<br>
	 * 
	 * @param value valor <code>inteiro</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatInteger(Integer value);
	
	/**
	 * Parseia um texto formatado para um valor <code>inteiro</code>.<br>
	 * 
	 * @param text o texto formatado.
	 * @return o valor <code>inteiro</code> ou <code>null</code> se o texto representar um valor sendo composto.
	 * @throws ParseException se o texto não estiver no formatado correto.
	 */
	Integer parseInteger(CharSequence text) throws ParseException;
}
