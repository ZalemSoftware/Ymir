package br.com.zalem.ymir.client.android.entity.ui.configuration.field;

/**
 * Mapeamento para um campo da entidade (attributo ou relacionamento) ou de um campo referenciado por um relacionamento da entidade.<br>
 * Deve haver apenas a configuração de um atributo ou um de relacionamento, nunca os dois juntos.
 *
 * @author Thiago Gesser
 */
public interface IFieldMapping {
	
	/**
	 * <b>Mutuamente exclusivo com o relacionamento.</b><br>
	 * Obtém o caminho para o atributo. Pode ser diretamente um atributo da entidade alvo da configuração ou um caminho que parte dela,
     * navega por relacionamentos singulares e chega no atributo desejado.
	 * 
	 * @return o atributo obtido.
	 */
	String[] getAttribute();
	
	/**
	 * <b>Mutuamente exclusivo com o atributo.</b><br>
	 * Obtém o caminho para o relacionamento. Pode ser diretamente um relacionamento da entidade alvo da configuração ou um caminho que parte dela,
     * navega por relacionamentos singulares e chega no relacionamento desejado.
	 * 
	 * @return o relacionamento obtido.
	 */
	String[] getRelationship();
}
