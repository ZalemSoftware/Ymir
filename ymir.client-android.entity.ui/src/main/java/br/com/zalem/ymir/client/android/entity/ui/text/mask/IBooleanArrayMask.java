package br.com.zalem.ymir.client.android.entity.ui.text.mask;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de booleanos</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface IBooleanArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de booleanos</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>array de booleanos</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatBooleanArray(Boolean[] value);
}
