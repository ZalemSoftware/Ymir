package br.com.zalem.ymir.client.android.entity.ui.configuration.editing;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFormattableFieldMapping;

/**
 * Configuração de um campo do tipo enumeração.<br>
 * Seu valor será definido através de uma escolha em uma lista de valores predeterminados. 
 *
 * @author Thiago Gesser
 */
public interface IEditingFieldEnum extends IFormattableFieldMapping {

	/**
	 * Valores da enumeração disponíveis para a definição no campo.<br>
	 * Se a enumeração for para um atributo, cada valor será parseado de acordo com a máscara definida para ele.<br>
     * Se a enumeração for para um relacionamento, cada valor deve ser uma chave para um registro da entidade alvo do relacionamento.
	 * 
	 * @return os valores da enumeração. 
	 */
	String[] getValues();

    /**
     * <b>Obrigatório se o a enumeração for para um relacionamento e proibido se for para um atributo.</b>
     * Obtém o caminho para o atributo que será utilizado para ilustrar os registros da entidade alvo do relacionamento. Pode ser diretamente
     * um atributo da entidade alvo da configuração ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
     *
     * @return o caminho para o atributo obtido.
     */
    String[] getAttribute();
}
