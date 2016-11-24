package br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing;

import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingTab;

/**
 * Representação da configuração de aba de edição de registro para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonEditingTab implements IEditingTab {

	private String title;
    private String layout;
	private JsonEditingFieldMapping[] fields;
	
	@Override
	public String getTitle() {
		return title;
	}

    @Override
    public String getLayout() {
        return layout;
    }

    @Override
	public JsonEditingFieldMapping[] getFields() {
		return fields;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setFields(JsonEditingFieldMapping[] fields) {
		this.fields = fields;
	}

    public void setLayout(String layout) {
        this.layout = layout;
    }
}
