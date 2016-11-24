package br.com.zalem.ymir.client.android.entity.ui.text.mask;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de inteiros</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface IIntegerArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de inteiros</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>array de inteiros</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatIntegerArray(Integer[] value);
}
