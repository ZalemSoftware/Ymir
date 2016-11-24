package br.com.zalem.ymir.client.android.entity.ui.configuration;

import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.ITabbedListDisplayConfig;

/**
 * Configurações de todas as funcionalidades visuais que envolvem uma entidade.
 *
 * @author Thiago Gesser
 */
public interface IEntityConfig {

	/**
	 * Obtém o nome da entidade que esta configuração referencia.
	 * 
	 * @return o nome obtido.
	 */
	String getName();

    /**
     * Obtém o nome de exibição da entidade.
     *
     * @param plural <code>true</code> para obter a versão em plural e <code>false</code> para a versão singular.
     * @return o nome obtido.
     */
    String getDisplayName(boolean plural);

	/**
	 * Obtém a configuração de lista para a entidade referenciada por esta configuração.
	 * 
	 * @return a configuração de lista obtida.
	 */
	IListConfig getList();

	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém a configuração para a seleção de registros da entidade referenciada por esta configuração.<br>
	 * Se não for definida, será utilizada a {@link #getList() configuração de lista} em seu lugar.
	 *
	 * @return a configuração de lista obtida.
	 */
	ITabbedListDisplayConfig getSelection();
	
	/**
	 * Obtém a configuração de detalhes para a entidade referenciada por esta configuração.
	 * 
	 * @return a configuração de detalhes obtida.
	 */
	IDetailConfig getDetail();
	
	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém a configuração de edição para a entidade referenciada por esta configuração.
	 * 
	 * @return a configuração de edição obtida.
	 */ 
	IEditingConfig getEditing();
}
