package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;


/**
 * Configuração de exibição de listagem de registros que possibilita a separação em abas.<br>
 * A configuração do <code>layout</code> é opcional se houverem abas declaradas. Neste caso, as configurações de <code>layout</code>,
 * <code>filtro</code> e <code>ordenação</code> servirão apenas como valores padrão para o caso de alguma aba não tê-los declarado.
 *
 * @author Thiago Gesser
 */
public interface ITabbedListDisplayConfig extends IListDisplayConfig {

	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém as abas nas quais as listagens dos registros devem estar divididas.<br>
	 * Se as abas forem definidas, as configurações de <code>layout</code>, <code>filtro</code> e <code>ordem</code>
	 * servirão apenas como valores padrão para o caso de alguma aba não tê-los declarado. Caso contrário, haverá apenas
	 * uma listagem de acordo com as demais configurações.
	 * 
	 * @return as abas obtidas.
	 */
	IListTab[] getTabs();
}
