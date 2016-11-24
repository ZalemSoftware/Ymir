package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.detail;

import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.layout.JsonLayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.DetailLayoutType;

/**
 * Representação da configuração de aba de detalhe de registros de entidade para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonDetailTab implements IDetailTab {
	
	private String title;
	private JsonLayoutConfig<DetailLayoutType> header;
	private JsonDetailFieldMapping[] fields;
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public JsonLayoutConfig<DetailLayoutType> getHeader() {
		return header;
	}
	
	@Override
	public JsonDetailFieldMapping[] getFields() {
		return fields;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setHeader(JsonLayoutConfig<DetailLayoutType> layout) {
		this.header = layout;
	}
	
	public void setFields(JsonDetailFieldMapping[] additionalFields) {
		this.fields = additionalFields;
	}
}
