package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.text.ParseException;

/**
 * Representa uma máscara de formatação de valores do tipo <code>caractere</code> para textos.
 *
 * @author Thiago Gesser
 */
public interface ICharacterMask extends IMask {
	
	/**
	 * Formata um valor <code>caractere</code> para um texto.<br>
	 * 
	 * @param value valor <code>caractere</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatCharacter(Character value);

    /**
     * Parseia um texto formatado para um valor do tipo <code>caractere</code>.
     *
     * @param text o texto formatado.
     * @return o valor do tipo <code>caractere</code>.
     * @throws ParseException se o texto não estiver no formatado correto.
     */
    Character parseCharacter(CharSequence text) throws ParseException;
}
