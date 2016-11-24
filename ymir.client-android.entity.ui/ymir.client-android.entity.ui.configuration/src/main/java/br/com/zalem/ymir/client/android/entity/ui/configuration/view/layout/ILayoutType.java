package br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout;

/**
 * Tipo de layout visual do Ymir.<br>
 * Especifica os métodos que os enums de tipos de layout devem implementar.<br>
 * Esta interface deve apenas ser implementada por enums.  
 *
 * @author Thiago Gesser
 */
public interface ILayoutType {
	
	/**
	 * Obtém o nome do layout. Este nome deve ser único em relação aos outros layouts.
	 * 
	 * @return o nome obtido.
	 */
	String getName();

	/**
	 * Obtém os campos que o layout utiliza.
	 * 
	 * @return os campos obtidos.
	 */
	LayoutField[] getFields();
}
