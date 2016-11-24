package br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing;

import android.text.InputType;

import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IVirtualAttribute;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonLabelableFieldMapping;

/**
 * Representação da configuração de um campo de edição de registro para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonEditingFieldMapping extends AbstractJsonLabelableFieldMapping implements IEditingFieldMapping {

    private String help;
	private boolean editable = true;
	private boolean hidden;
	private JsonVirtualAttribute virtualAttribute;
	private JsonVirtualRelationship virtualRelationship;
	private JsonEditingFieldEnum enumeration;
    private Integer inputType;
	private Double incremental;

    @Override
    public String getLabel() {
        String label = super.getLabel();
        if (label == null) {
            if (virtualAttribute != null) {
                return virtualAttribute.getName();
            }
            if (virtualRelationship != null) {
                return virtualRelationship.getName();
            }
        }
        return label;
    }

    @Override
    public String getHelp() {
        return help;
    }

    @Override
	public boolean isEditable() {
		return editable;
	}
	
	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public IVirtualAttribute getVirtualAttribute() {
		return virtualAttribute;
	}
	
	@Override
	public JsonVirtualRelationship getVirtualRelationship() {
		return virtualRelationship;
	}
	
	@Override
	public JsonEditingFieldEnum getEnum() {
		return enumeration;
	}

	@Override
	public Integer getInputType() {
		return inputType;
	}

	@Override
	public Double getIncremental() {
		return incremental;
	}

	public void setInputType(String[] inputTypeItems) {
		if (inputTypeItems == null || inputTypeItems.length == 0) {
			return;
		}

		int it = 0;
		for (String inputTypeItem : inputTypeItems) {
			try {
				it |= (int) InputType.class.getDeclaredField(inputTypeItem).get(null);
			} catch (NoSuchFieldException e) {
				throw new IllegalArgumentException("Unknown InputType: " + inputTypeItem);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Invalid InputType: " + inputTypeItem);
			}
		}
		inputType = it;
	}

    public void setHelp(String help) {
        this.help = help;
    }

    public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public void setVirtualAttribute(JsonVirtualAttribute virtualAttribute) {
		this.virtualAttribute = virtualAttribute;
	}
	
	public void setVirtualRelationship(JsonVirtualRelationship virtualRelationship) {
		this.virtualRelationship = virtualRelationship;
	}
	
	public void setEnum(JsonEditingFieldEnum enumeration) {
		this.enumeration = enumeration;
	}

	public void setIncremental(Double incremental) {
		this.incremental = Math.abs(incremental);
	}
}
