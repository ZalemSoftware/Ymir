package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.ITabbedListDisplayConfig;

/**
 * Representação da configuração de exibição de lista com abas para JSON.
 *
 * @author Thiago Gesser
 */
public class JsonTabbedListDisplayConfig extends JsonListDisplayConfig implements ITabbedListDisplayConfig {

	private JsonListTab[] tabs;

	@Override
	public JsonListTab[] getTabs() {
		return tabs;
	}

	public void setTabs(JsonListTab[] tabs) {
		this.tabs = tabs;
	}
}
