package br.com.zalem.ymir.client.android.entity.ui.configuration.json;

import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing.JsonEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.JsonFieldDefaults;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.detail.JsonDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list.JsonListConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list.JsonTabbedListDisplayConfig;

/**
 * Representação da configuração de uma entidade para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonEntityConfig implements IEntityConfig {
	
	private String name;
	private EntityDisplayName displayName;
	private JsonFieldDefaults[] fieldsDefaults;
	private JsonListConfig list;
	private JsonTabbedListDisplayConfig selection;
	private JsonDetailConfig detail;
	private JsonEditingConfig editing;
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public JsonListConfig getList() {
		return list;
	}

	@Override
	public JsonTabbedListDisplayConfig getSelection() {
		return selection;
	}

	@Override
	public JsonDetailConfig getDetail() {
		return detail;
	}

	@Override
	public JsonEditingConfig getEditing() {
		return editing;
	}

    @Override
    public String getDisplayName(boolean plural) {
        String name = null;
        if (displayName != null) {
            name = plural ? displayName.getPlural() : displayName.getSingular();
        }

        //Se não possui nome de exibição definido, usa o próprio nome da entidade.
        if (name == null) {
            name = this.name;
        }
        return name;
    }

    public JsonFieldDefaults[] getFieldsDefaults() {
        return fieldsDefaults;
    }

	public void setFieldsDefaults(JsonFieldDefaults[] fields) {
		this.fieldsDefaults = fields;
	}

	public void setList(JsonListConfig list) {
		this.list = list;
	}

	public void setSelection(JsonTabbedListDisplayConfig selection) {
		this.selection = selection;
	}

	public void setDetail(JsonDetailConfig detail) {
		this.detail = detail;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setEditing(JsonEditingConfig editing) {
		this.editing = editing;
	}

    public EntityDisplayName getDisplayName() {
        return displayName;
    }

    public void setDisplayName(EntityDisplayName displayName) {
        this.displayName = displayName;
    }

    public static final class EntityDisplayName {

        private String singular;
        private String plural;

        public String getPlural() {
            return plural;
        }

        public String getSingular() {
            return singular;
        }

        public void setPlural(String plural) {
            this.plural = plural;
        }

        public void setSingular(String singular) {
            this.singular = singular;
        }
    }
}
