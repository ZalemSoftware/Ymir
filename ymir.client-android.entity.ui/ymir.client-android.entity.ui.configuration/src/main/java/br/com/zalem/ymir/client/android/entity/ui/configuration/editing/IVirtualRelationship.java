package br.com.zalem.ymir.client.android.entity.ui.configuration.editing;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;

/**
 * Configuração de um relacionamento virtual. É utilizado como um campo de edição de entidade, possuindo todos os comportamentos
 * dos demais campos, mas não representa um campo existente da entidade.<br>
 * Como um relacionamento virtual não está ligado à fonte de dados, pode ser utilizado para apresentar / coletar dados que
 * não serão salvos no registro da entidade.
 *
 * @author Thiago Gesser
 */
public interface IVirtualRelationship {
	
	/**
	 * Obtém o nome do relacionamento virtual.
	 * 
	 * @return o nome obtido.
	 */
	String getName();
	
	/**
	 * Obtém o tipo do relacionamento virtual.
	 * 
	 * @return o tipo obtido.
	 */
	EntityRelationshipType getType();
	
	/**
	 * Obtém o nome da entidade referenciada pelo relacionamento virutal.
	 * 
	 * @return o nome da entidade obtido.
	 */
	String getEntity();
}
