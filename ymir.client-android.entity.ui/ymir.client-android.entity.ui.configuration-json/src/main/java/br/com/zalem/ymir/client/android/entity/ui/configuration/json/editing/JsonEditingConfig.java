package br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing;

import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingPermissions;

/**
 * Representação da configuração de edição de registro para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonEditingConfig implements IEditingConfig {

	private JsonEditingPermissions local;
	private JsonEditingPermissions dataSource;
    private String layout;
    private boolean enableSummarize;
	private JsonEditingTab[] tabs;
	private JsonEditingFieldMapping[] fields;

	@Override
	public IEditingPermissions getLocalPermissions() {
		return local;
	}

	@Override
	public IEditingPermissions getDataSourcePermissions() {
		return dataSource;
	}

    @Override
    public String getLayout() {
        return layout;
    }

    @Override
	public JsonEditingTab[] getTabs() {
		return tabs;
	}
	
	@Override
	public JsonEditingFieldMapping[] getFields() {
		return fields;
	}

    @Override
    public boolean isEnableSummarize() {
        return enableSummarize;
    }

    public void setFields(JsonEditingFieldMapping[] fields) {
		this.fields = fields;
	}
	
	public void setLocal(JsonEditingPermissions local) {
		this.local = local;
	}
	
	public void setDataSource(JsonEditingPermissions dataSource) {
		this.dataSource = dataSource;
	}

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public void setTabs(JsonEditingTab[] tabs) {
		this.tabs = tabs;
	}

    public void setEnableSummarize(boolean enableSummarize) {
        this.enableSummarize = enableSummarize;
    }
}
