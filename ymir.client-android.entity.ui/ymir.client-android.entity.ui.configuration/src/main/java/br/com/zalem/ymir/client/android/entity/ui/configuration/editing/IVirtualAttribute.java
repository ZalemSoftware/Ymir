package br.com.zalem.ymir.client.android.entity.ui.configuration.editing;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;

/**
 * Configuração de um atributo virtual. É utilizado como um campo de edição de entidade, possuindo todos os comportamentos
 * dos demais campos, mas não representa um campo existente da entidade.<br>
 * Como um atributo virtual não está ligado à fonte de dados, pode ser utilizado para apresentar / coletar dados que
 * não serão salvos no registro da entidade.
 *
 * @author Thiago Gesser
 */
public interface IVirtualAttribute {
	
	/**
	 * Obtém o nome do atributo virtual.
	 * 
	 * @return o nome obtido.
	 */
	String getName();
	
	/**
	 * Obtém o tipo do atributo virtual.
	 * 
	 * @return o tipo obtido.
	 */
	EntityAttributeType getType();
}
