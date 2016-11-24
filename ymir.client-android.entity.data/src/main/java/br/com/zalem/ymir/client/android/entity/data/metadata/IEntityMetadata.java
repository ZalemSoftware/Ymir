package br.com.zalem.ymir.client.android.entity.data.metadata;

/**
 * Metadados de uma entidade. Provê informações sobre a entidade, seus atributos e relacionamentos.
 *
 * @author Thiago Gesser
 */
public interface IEntityMetadata {

	/**
	 * Obtém o nome da entidade.
	 * 
	 * @return o nome obtido.
	 */
	String getName();
	
	/**
	 * Obtém o campo da atributo referenciado pelo nome.
	 * 
	 * @param name nome do atributo.
	 * @return o atributo obtido.
	 * @throws IllegalArgumentException se não havia um atributo referenciado pelo nome.
	 */
	IEntityAttribute getAttribute(String name);
	
	/**
	 * Obtém os atributos da entidade.
	 * 
	 * @return os atributos obtidos.
	 */
	IEntityAttribute[] getAttributes();

	/**
	 * Obtém um relacionamento da entidade referenciada pelo nome;
	 * 
	 * @param name nome do relacionamento.
	 * @return o relacionamento obtida.
	 * @throws IllegalArgumentException se não havia um relacionamento referenciada pelo nome.
	 */
	IEntityRelationship getRelationship(String name);
	
	/**
	 * Obtém os relacionamentos da entidade.
	 * 
	 * @return os relacionamentos obtidos.
	 */
	IEntityRelationship[] getRelationships();
}
