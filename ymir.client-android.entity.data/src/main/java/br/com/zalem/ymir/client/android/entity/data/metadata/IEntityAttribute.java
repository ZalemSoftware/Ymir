package br.com.zalem.ymir.client.android.entity.data.metadata;

/**
 * Representação de um atributo de uma entidade.
 *
 * @author Thiago Gesser
 */
public interface IEntityAttribute {
	
	/**
	 * Obtém o nome do atributo.
	 * 
	 * @return o nome obtido.
	 */
	String getName();
	
	/**
	 * Obtém o tipo do atributo.
	 * 
	 * @return o tipo obtido.
	 */
	EntityAttributeType getType();
}
