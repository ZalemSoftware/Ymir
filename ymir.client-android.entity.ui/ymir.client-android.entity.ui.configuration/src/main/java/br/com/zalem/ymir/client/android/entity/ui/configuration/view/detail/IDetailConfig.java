package br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.DetailLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;


/**
 * Configurações que envolvem os detalhes de uma entidade.<br>
 * Deve ser definido pelo menos uma aba ou alguma configuração de detalhe no próprio IDetailConfig, mas não ambas.
 *
 * @author Thiago Gesser
 */
public interface IDetailConfig {

	/**
	 * <b>Configuração proibida se houver definição de cabeçalho ou campo no próprio IDetailConfig.</b><br> 
	 * <br>
	 * Obtém as abas nas quais os detalhes do registro devem estar divididas.<br>
	 * Se as abas não forem definidas, haverá apenas um detalhamento de acordo com as demais configurações do IDetailConfig.
	 * 
	 * @return as abas obtidas.
	 */
	IDetailTab[] getTabs();
	
	/**
	 * <b>Configuração proibida se houver abas definidas.</b><br> 
	 * <b>Se não estiver proibida, é opcional se houver pelo menos um campo definido.</b><br> 
	 * Obtém a configuração do cabeçalho de detalhe.
	 * 
	 * @return a configuração do cabeçalho obtida.
	 */
	ILayoutConfig<DetailLayoutType> getHeader();
	
	/**
	 * <b>Configuração proibida se houver abas definidas.</b><br> 
	 * <b>Se não estiver proibida, é opcional se houver cabeçalho definido.</b><br>
	 * <br>
	 * Obtém os campos adicionais a serem mostrados no detalhamento.
	 * 
	 * @return os campos obtidos.
	 */
	IDetailFieldMapping[] getFields();

    /**
     * <b>Configuração opcional.</b><br>
     * <br>
     * Determina se a ação de duplicar o registro através do detalhamento está habilitada. Por padrão está desabilitada.
     *
     * @return <code>true</code> se a duplicação está habilitada e <code>false</code> caso contrário.
     */
    boolean isEnableDuplicate();
}
