package br.com.zalem.ymir.client.android.entity.data.query.select;

import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;

/**
 * Instrução de seleção de uma query de seleção de dados.<br>
 * Permite a definição dos campos selecionados (atributos ou relacionamentos) e a origem dos dados da query. 
 *
 * @author Thiago Gesser
 */
public interface ISelectStatement {

	/**
	 * Adiciona a seleção dos valores do atributo na query.<br>
	 * Se não for definido um alias através do {@link ISelectAsStatement#as(String)}, será utilizado o alias padrão.
	 * O alias padrão é o caminho do campo separado por - (hífen). Por exemplo:<br>
	 * caminho = ["atributo"], alias = "atributo";<br>
	 * caminho = ["relacionamento", "atributo"], alias = "relacionamento-atributo".
	 * 
	 * @param attributePath caminho para o atributo. Pode ser diretamente um atributo da entidade alvo da query ou
	 * um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link ISelectAsStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o tipo do atributo não é suportado.
	 */
	ISelectAsStatement attribute(String... attributePath);
	
	/**
	 * Adiciona a seleção dos valores do relacionamento na query.<br>
	 * Se não for definido um alias através do {@link ISelectAsStatement#as(String)}, será utilizado o alias padrão.
	 * O alias padrão é o caminho do campo separado por - (hífen). Por exemplo:<br>
	 * caminho = ["relacionamento"], alias = "relacionamento";<br>
	 * caminho = ["relacionamento1", "relacionamento2"], alias = "relacionamento1-relacionamento2".
	 * 
	 * @param relationshipPath caminho para o relacionamento. Pode ser diretamente um relacionamento da entidade alvo da query ou
	 * um caminho que parte dela, navega por relacionamentos singulares e chega no relacionamento desejado.
	 * @return o {@link ISelectAsStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento ou se o tipo do relacionamento não é suportado.
	 */
	ISelectAsStatement relationship(String... relationshipPath);
	
	/**
	 * Define a entidade como a origem de dados.
	 *  
	 * @param entityName o nome da entidade.
	 * @return o {@link IFromStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IFromStatement from(String entityName);
	
	/**
	 * Define uma query de seleção (subselect) como a origem dos dados.<br>
	 * Atualmente, apenas subselects que selecionam registros completos (sem campos específicos) são suportados.
	 *  
	 * @param subselect query de seleção que será utilizada como a origem dos dados.
	 * @return o {@link IFromStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IFromStatement from(ISelectQuery subselect);
	
	/**
	 * Define a visão como a origem de dados.
	 *  
	 * @param dataView visão de dados.
	 * @return o {@link IFromStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IFromStatement from(RelationshipArrayView dataView);
}
