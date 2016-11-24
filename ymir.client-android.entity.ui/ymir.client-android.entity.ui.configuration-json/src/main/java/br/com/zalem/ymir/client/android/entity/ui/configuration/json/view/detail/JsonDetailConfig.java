package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.detail;

import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.layout.JsonLayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.DetailLayoutType;

/**
 * Representação da configuração de detalhes para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonDetailConfig implements IDetailConfig {

	private JsonDetailTab[] tabs;
	private JsonLayoutConfig<DetailLayoutType> header;
	private JsonDetailFieldMapping[] fields;
    private boolean enableDuplicate;
	
	@Override
	public JsonDetailTab[] getTabs() {
		return tabs;
	}
	
	@Override
	public JsonLayoutConfig<DetailLayoutType> getHeader() {
		return header;
	}
	
	@Override
	public JsonDetailFieldMapping[] getFields() {
		return fields;
	}

    @Override
    public boolean isEnableDuplicate() {
        return enableDuplicate;
    }

    public void setTabs(JsonDetailTab[] tabs) {
		this.tabs = tabs;
	}
	
	public void setHeader(JsonLayoutConfig<DetailLayoutType> layout) {
		this.header = layout;
	}
	
	public void setFields(JsonDetailFieldMapping[] additionalFields) {
		this.fields = additionalFields;
	}

    public void setEnableDuplicate(boolean enableDuplicate) {
        this.enableDuplicate = enableDuplicate;
    }
}
