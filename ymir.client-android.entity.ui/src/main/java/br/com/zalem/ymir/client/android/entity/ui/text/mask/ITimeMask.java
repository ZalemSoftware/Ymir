package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.sql.Time;
import java.text.ParseException;

/**
 * Representa uma máscara de formatação de valores do tipo <code>hora</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface ITimeMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>hora</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>hora</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatTime(Time value);
	
	/**
	 * Parseia um texto formatado para um valor do tipo <code>hora</code>.
	 * 
	 * @param text o texto formatado.
	 * @return o valor do tipo <code>hora</code>.
	 * @throws ParseException se o texto não estiver no formatado correto.
	 */
	Time parseTime(CharSequence text) throws ParseException;
	
	/**
	 * Indica se a máscara considera as horas no formato 24.
	 * 
	 * @return <code>true</code> se considera as horas no formato 24 e <code>false</code> caso contrário.
	 */
	boolean is24Hour();
}
