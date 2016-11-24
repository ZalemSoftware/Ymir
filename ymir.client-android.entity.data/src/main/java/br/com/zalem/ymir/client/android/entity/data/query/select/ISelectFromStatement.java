package br.com.zalem.ymir.client.android.entity.data.query.select;


/**
 * Instrução de seleção de uma query de seleção de dados cujo a origem dos dados já foi definida de forma implícita.<br>
 * Permite a definição dos campos selecionados (atributos ou relacionamentos), a aplicação de condições para a
 * filtragem dos dados ou a finalização da query. 
 *
 * @author Thiago Gesser
 */
public interface ISelectFromStatement extends IFromStatement {

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
	ISelectAsFromStatement attribute(String... attributePath);
	
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
	ISelectAsFromStatement relationship(String... relationshipPath);
}
