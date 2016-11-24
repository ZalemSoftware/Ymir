package br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout;


/**
 * Configuração de layout.<br>
 * O tipo genérico <code>T</code> deve ser um enum que representa o tipo de layout utilizado na configuração.
 *
 * @author Thiago Gesser
 */
public interface ILayoutConfig <T extends ILayoutType> {

	/**
	 * Obtém o tipo do layout da configuração. O tipo de layout definirá, através do {@link ILayoutType#getFields()},
	 * os mapeamentos que precisam ser retornados pelo {@link #getFields()}.
	 * 
	 * @return o tipo de layout obtido.
	 */
	T getType();
	
	/**
	 * Obtém o mapeamento dos campos. Deve haver um {@link ILayoutFieldMapping} para cada campo requerido pelo tipo de layout
	 * (definidos pelo {@link ILayoutType#getFields()}).
	 * 
	 * @return os mapeamentos obtidos.
	 */
	ILayoutFieldMapping[] getFields();
}
