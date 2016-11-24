package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.sql.Time;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de horas</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface ITimeArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de horas</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>array de horas</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatTimeArray(Time[] value);
}
