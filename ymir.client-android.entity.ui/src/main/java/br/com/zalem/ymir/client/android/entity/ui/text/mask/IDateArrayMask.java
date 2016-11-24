package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.sql.Date;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de datas</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface IDateArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de datas</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>array de datas</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatDateArray(Date[] value);
}
