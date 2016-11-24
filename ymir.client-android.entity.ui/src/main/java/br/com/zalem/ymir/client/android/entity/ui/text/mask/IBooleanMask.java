package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.text.ParseException;

/**
 * Representa uma máscara de formatação de valores do tipo <code>booleano</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface IBooleanMask extends IMask {
	
	/**
	 * Formata um valor <code>booleano</code> para um texto.<br>
	 * 
	 * @param value valor <code>booleano</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatBoolean(Boolean value);
	
	/**
	 * Parseia um texto formatado para um valor do tipo <code>booleano</code>.
	 * 
	 * @param text o texto formatado.
	 * @return o valor do tipo <code>booleano</code>.
	 * @throws ParseException se o texto não estiver no formatado correto.
	 */
	Boolean parseBoolean(CharSequence text) throws ParseException;
}
