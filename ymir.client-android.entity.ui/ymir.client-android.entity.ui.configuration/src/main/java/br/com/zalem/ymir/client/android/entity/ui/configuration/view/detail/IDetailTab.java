package br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.DetailLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;

/**
 * Configurações de uma aba de detalhes de um registro de entidade.
 *
 * @author Thiago Gesser
 */
public interface IDetailTab {
	
	/**
	 * Obtém o título desta aba.
	 * 
	 * @return o título obtido.
	 */
	String getTitle();
	
	/**
	 * <b>Configuração opcional se houver pelo menos um campo definido.</b><br>
	 * Obtém a configuração do cabeçalho da aba de detalhe.
	 * 
	 * @return a configuração do cabeçalho obtida.
	 */
	ILayoutConfig<DetailLayoutType> getHeader();
	
	/**
	 * <b>Configuração opcional se houver cabeçalho definido.</b><br>
	 * <br>
	 * Obtém os campos adicionais a serem mostrados no detalhamento.
	 * 
	 * @return os campos obtidos.
	 */
	IDetailFieldMapping[] getFields();
	
}
