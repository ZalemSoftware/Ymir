package br.com.zalem.ymir.client.android.entity.data.query.select;

/**
 * Instrução de restrição de uma query de seleção de dados.
 * Permite a continuidade da condição através de operadores lógicos ou a finalização da query.
 *
 * @author Thiago Gesser
 */
public interface IRestrictionStatement extends ITerminalStatement {
	
	/**
	 * Aplica o operador lógico <code>e</code> entre a restrição atual e a próxima da query.
	 * 
	 * @return o {@link IConditionStatement} para definir a próxima restrição da query.
	 */
	IConditionStatement and();

	/**
	 * Aplica o operador lógico <code>ou</code> entre a restrição atual e a próxima da query.
	 * 
	 * @return o {@link IConditionStatement} para definir a próxima restrição da query.
	 */
	IConditionStatement or();

	/**
	 * Fecha um escopo na query, fazendo com que as restrições que estejam dentro dele sejam consideradas de maneira isolada
	 * das demais restrições.<br>
	 * @return o IRestrictionStatement com os métodos disponíveis para continuar a construção da query.
	 */
	IRestrictionStatement c();
}
