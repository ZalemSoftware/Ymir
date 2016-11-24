package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.layout;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutType;

/**
 * Representação da configuração de layout para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonLayoutConfig <T extends ILayoutType> implements ILayoutConfig<T> {

	private T type;
	private JsonLayoutFieldMapping[] fields;
	
	@Override
	public T getType() {
		return type;
	}

	@Override
	public JsonLayoutFieldMapping[] getFields() {
		return fields;
	}
	
	public void setFields(JsonLayoutFieldMapping[] fields) {
		this.fields = fields;
	}
	
	public void setType(T type) {
		this.type = type;
	}
}
