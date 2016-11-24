package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;

/**
 * Configurações básicas que envolvem a exibição da lista de registros de uma entidade.
 *
 * @author Thiago Gesser
 */
public interface IListDisplayConfig {
	
	/**
	 * Obtém a configuração de layout de lista que será utilizado para representar os registros.<br>
	 * 
	 * @return a configuração de layout de lista obtida.
	 */
	ILayoutConfig<ListLayoutType> getLayout();
	
	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém o filtro que será utilizado na listagem dos registros.<br>
	 * 
	 * @return o filtro obtido.
	 */
	IListFilter getFilter();
	
	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém a ordenação que será utilizado na listagem dos registros.<br>
	 * 
	 * @return a ordenação obtida.
	 */
	IListOrder getOrder();
}
