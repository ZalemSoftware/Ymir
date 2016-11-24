package br.com.zalem.ymir.client.android.entity.ui.text.mask;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de textos</code> para outros textos.
 *
 * @author Thiago Gesser
 */
public interface ITextArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de textos</code> para outro texto.<br>
	 * 
	 * @param value valor do tipo <code>array de textos</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatTextArray(String[] value);
}
