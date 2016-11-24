package br.com.zalem.ymir.client.android.entity.data.metadata;

/**
 * Representação de um relacionamento de uma entidade com outra entidade.
 *
 * @author Thiago Gesser
 */
public interface IEntityRelationship {

	/**
	 * Obtém o nome do relacionamento.
	 * 
	 * @return o nome obtido.
	 */
	String getName();
	
	/**
	 * Obtém o tipo do relacionamento.
	 * 
	 * @return o tipo obtido.
	 */
	EntityRelationshipType getType();

    /**
     * Obtém os metadados da entidade fonte do relacionamento.
     *
     * @return os metadados da entidade obtido.
     */
    IEntityMetadata getSource();

	/**
	 * Obtém os metadados da entidade alvo do relacionamento.
	 *
	 * @return os metadados da entidade obtido.
	 */
	IEntityMetadata getTarget();
}
