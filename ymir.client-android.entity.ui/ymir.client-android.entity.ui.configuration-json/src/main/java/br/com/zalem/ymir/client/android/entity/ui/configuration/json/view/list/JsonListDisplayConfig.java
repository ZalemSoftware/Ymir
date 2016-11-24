package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.layout.JsonLayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListDisplayConfig;

/**
 * Representação da configuração básica de lista para JSON.
 *
 * @author Thiago Gesser
 */
public class JsonListDisplayConfig implements IListDisplayConfig {

	private JsonListFilter filter;
	private JsonListOrder order;
	private JsonLayoutConfig<ListLayoutType> layout;
	
	@Override
	public final JsonListFilter getFilter() {
		return filter;
	}
	
	@Override
	public final JsonListOrder getOrder() {
		return order;
	}

	@Override
	public final JsonLayoutConfig<ListLayoutType> getLayout() {
		return layout;
	}
	
	public final void setFilter(JsonListFilter filter) {
		this.filter = filter;
	}
	
	public final void setOrder(JsonListOrder order) {
		this.order = order;
	}
	
	public final void setLayout(JsonLayoutConfig<ListLayoutType> layout) {
		this.layout = layout;
	}
}
