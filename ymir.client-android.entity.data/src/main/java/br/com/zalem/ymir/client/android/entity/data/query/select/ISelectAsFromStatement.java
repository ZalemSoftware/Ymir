package br.com.zalem.ymir.client.android.entity.data.query.select;

/**
 * Instrução de seleção de uma query cujo a origem dos dados já foi definida de forma implícita que permite a definição
 * de um alias para um campo selecionado.
 *
 * @author Thiago Gesser
 */
public interface ISelectAsFromStatement extends ISelectFromStatement {

	/**
	 * Define o alias do campo de seleção adicionado anteriormente.
	 * 
	 * @param alias o alias.
	 * @return o {@link ISelectStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	ISelectFromStatement as(String alias);
}
