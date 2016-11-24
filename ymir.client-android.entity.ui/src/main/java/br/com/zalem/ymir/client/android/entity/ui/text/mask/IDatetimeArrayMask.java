package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.sql.Timestamp;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de horas</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface IDatetimeArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de datas e horas</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>array de datas e horas</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatDatetimeArray(Timestamp[] value);
}
