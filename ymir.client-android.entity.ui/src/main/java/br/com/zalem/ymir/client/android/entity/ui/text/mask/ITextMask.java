package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import java.text.ParseException;

/**
 * Representa uma máscara de formatação de valores do tipo <code>texto</code> para outros textos e o parse de outros 
 * textos para valores do tipo <code>texto</code>.
 *
 * @author Thiago Gesser
 */
public interface ITextMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>texto</code> para outro texto.<br>
	 * 
	 * @param value valor do tipo <code>texto</code>.
	 * @return o texto formatado.
	 */
	CharSequence formatText(String value);
	
	/**
	 * Parseia um texto formatado para um valor do tipo <code>texto</code>.
	 * 
	 * @param text o texto formatado.
	 * @return o valor do tipo <code>texto</code> ou <code>null</code> se o texto representar um valor sendo composto.
	 * @throws ParseException se o texto não estiver no formatado correto.
	 */
	String parseText(CharSequence text) throws ParseException;

	/**
	 * Indica se a máscara pode ser aplicada na edição de campos do tipo <code>texto</code>.<br>
	 * Se for <code>true</code>, o texto será formatado conforme for sendo editado e em consequência disto, qualquer auxílio
	 * de escrita será desabilitado (sugestões, por exemplo). Caso contrário, o texto editado não sofrerá nenhuma alteração.
	 *
	 * @return <code>true</code> se o texto editado deve ser formatado e <code>false</code> caso contrário.
	 */
	boolean isEditable();
}
