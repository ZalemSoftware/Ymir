package br.com.zalem.ymir.client.android.entity.ui.configuration.field;

import br.com.zalem.ymir.client.android.entity.ui.configuration.BasicMaskType;

/**
 * Generalização de um mapeamento de campo de entidade que pode ser formatado através de uma máscara.
 * Não é uma configuração em si, devendo ser apenas para ser estendida por outras interfaces de configuração.
 *
 * @author Thiago Gesser
 */
public interface IFormattableFieldMapping extends IFieldMapping {
	
	/**
	 * <b>Configuração opcional.</b><br>
	 * <b>Só pode ser utilizada se este mapeamento referenciar um atributo.</b><br> 
	 * <br>
	 * Obtém a máscra para a exibição do valor do campo mapeado.<br>
	 * Há duas maneiras de configurá-lo:
	 * <ul>
	 * 	<li>o nome de uma das máscaras básicas disponíveis para o tipo do campo da entidade mapeado, ou seja, todos os valores de 
	 * {@link BasicMaskType} que declaram suportar o tipo de campo da entidade;</li>
	 * 	<li>o nome de uma classe de máscara customizada. As máscaras customizadas disponíveis dependerão da implementação 
	 * de Client Android sendo utilizada.</li>
	 * </ul>  
	 * 
	 * @return a configuração de máscara obtida.
	 */
	String getMask();
}
