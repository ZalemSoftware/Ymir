package br.com.zalem.ymir.client.android.entity.ui.text.mask;

/**
 * Representa uma máscara de formatação de valores do tipo <code>array de decimais</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface IDecimalArrayMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>array de decimais</code> para um texto.<br>
	 * 
	 * @param value valor do tipo <code>array de decimais</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatDecimalArray(Double[] value);
}
