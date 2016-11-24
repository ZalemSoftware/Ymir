package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.detail;

import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonLabelableFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list.JsonListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailFieldMapping;

/**
 * Representação do mapeamento de campo em JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonDetailFieldMapping extends AbstractJsonLabelableFieldMapping implements IDetailFieldMapping {

	private JsonListDisplayConfig listConfig;
    private boolean labelHidden;

	@Override
	public JsonListDisplayConfig getListConfig() {
		return listConfig;
	}

    @Override
    public boolean isLabelHidden() {
        return labelHidden;
    }

    public void setListConfig(JsonListDisplayConfig listConfig) {
		this.listConfig = listConfig;
	}

    public void setLabelHidden(boolean labelHidden) {
        this.labelHidden = labelHidden;
    }
}