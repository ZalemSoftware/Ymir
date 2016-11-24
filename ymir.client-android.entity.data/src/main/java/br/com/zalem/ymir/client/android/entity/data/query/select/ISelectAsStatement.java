package br.com.zalem.ymir.client.android.entity.data.query.select;

/**
 * Instrução de seleção que permite a definição de um alias para um campo selecionado.
 *
 * @author Thiago Gesser
 */
public interface ISelectAsStatement extends ISelectStatement {

	/**
	 * Define o alias do campo de seleção adicionado anteriormente.
	 * 
	 * @param alias o alias.
	 * @return o {@link ISelectStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	ISelectStatement as(String alias);
}
