package br.com.zalem.ymir.client.android.entity.ui.text.mask;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de caracteres</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface ICharacterArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de caracteres</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>array de caracteres</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatCharacterArray(Character[] value);
}
