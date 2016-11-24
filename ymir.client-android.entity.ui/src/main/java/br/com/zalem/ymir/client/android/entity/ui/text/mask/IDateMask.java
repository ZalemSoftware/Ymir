package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.sql.Date;
import java.text.ParseException;

/**
 * Representa uma máscara de formatação de valores do tipo <code>data</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface IDateMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>data</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>data</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatDate(Date value);
	
	/**
	 * Parseia um texto formatado para um valor do tipo <code>data</code>.
	 * 
	 * @param text o texto formatado.
	 * @return o valor do tipo <code>data</code>.
	 * @throws ParseException se o texto não estiver no formatado correto.
	 */
	Date parseDate(CharSequence text) throws ParseException;
}
