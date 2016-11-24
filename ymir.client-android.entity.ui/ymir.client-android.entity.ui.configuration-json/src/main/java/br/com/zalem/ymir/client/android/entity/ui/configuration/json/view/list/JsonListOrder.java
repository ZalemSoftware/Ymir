package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListOrder;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IOrderFieldMapping;

/**
 * Representação da configuração de ordenação de lista para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonListOrder implements IListOrder {

	private JsonOrderFieldMapping[] fields;
	
	@Override
	public IOrderFieldMapping[] getFields() {
		return fields;
	}
	
	public void setFields(JsonOrderFieldMapping[] fields) {
		this.fields = fields;
	}
}
