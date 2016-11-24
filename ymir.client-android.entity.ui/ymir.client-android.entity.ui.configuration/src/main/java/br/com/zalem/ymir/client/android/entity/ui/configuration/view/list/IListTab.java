package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;


/**
 * Configurações de uma aba de listagem de registros de entidade.<br>
 * A configuração do <code>layout</code> é opcional se já houver um <code>layout</code> definido no {@link ITabbedListDisplayConfig}.<br>
 * Se alguma das configurações de <code>layout</code>, <code>filtro</code> e <code>ordenação</code> não tiverem sido definidas, serão utilizadas
 * as configurações correspondentes definidas no ITabbedListConfig.
 *
 * @author Thiago Gesser
 */
public interface IListTab extends IListDisplayConfig {

	/**
	 * Obtém o título desta aba.
	 * 
	 * @return o título obtido.
	 */
	String getTitle();
}
