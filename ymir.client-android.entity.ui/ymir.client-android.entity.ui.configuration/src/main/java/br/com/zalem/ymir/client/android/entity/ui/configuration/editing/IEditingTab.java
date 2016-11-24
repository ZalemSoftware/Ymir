package br.com.zalem.ymir.client.android.entity.ui.configuration.editing;

/**
 * Configurações de uma aba de edição de um registro de entidade.
 *
 * @author Thiago Gesser
 */
public interface IEditingTab {

	/**
	 * Obtém o título desta aba.
	 * 
	 * @return o título obtido.
	 */
	String getTitle();

    /**
     * <b>Configuração opcional</b><br>
     * <br>
     * Obtém o nome do recurso de layout que permite customizar a forma como os editores de campos desta aba são exibidos. Os editores substituirão
     * as Views que declaram a propriedade <code>tag</code> com o nome de seu campo. O nome do campo deve ser precedido por um dos seguintes prefixos:
     * "attribute_" para editores de atributos e "relationship_" para editores de relacionamentos. Os atributos de layout da View substituída
     * são repassados para a View do editor.
     *
     * @see IEditingConfig#getLayout() Exemplo de layout
     *
     * @return o nome do layout customizado.
     */
    String getLayout();
	
	/**
	 * Obtém os campos a serem disponibilizados para a edição do registro nesta aba.
	 * 
	 * @return os campos obtidos.
	 */
	IEditingFieldMapping[] getFields();
}
