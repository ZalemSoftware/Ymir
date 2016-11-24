package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListTab;

/**
 * Representação da configuração de aba de listagem de registros de entidade para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonListTab extends JsonListDisplayConfig implements IListTab {

	private String title;
	
	@Override
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
}
