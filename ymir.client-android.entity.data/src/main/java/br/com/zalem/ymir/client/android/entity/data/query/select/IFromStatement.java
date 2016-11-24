package br.com.zalem.ymir.client.android.entity.data.query.select;

/**
 * Instrução de origem dos dados de uma query de seleção.
 * Permite a aplicação de condições para a filtragem dos dados ou a finalização da query.
 *
 * @author Thiago Gesser
 */
public interface IFromStatement extends ITerminalStatement {

	/**
	 * Inicia a condição da query.
	 * 
	 * @return o {@link IConditionStatement} para definir a condição da query.
	 */
	IConditionStatement where();
}
