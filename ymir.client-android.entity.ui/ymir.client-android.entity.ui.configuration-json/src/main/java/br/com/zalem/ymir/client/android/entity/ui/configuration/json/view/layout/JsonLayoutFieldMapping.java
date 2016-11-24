package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.layout;

import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonFormattableFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldVisibility;

/**
 * Representação do mapeamento de campo de layout em JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonLayoutFieldMapping extends AbstractJsonFormattableFieldMapping implements ILayoutFieldMapping {

	private LayoutField layoutField;
	private LayoutFieldVisibility visibility = LayoutFieldVisibility.VISIBLE;
    private String surrogateAttribute;

	@Override
	public LayoutField getLayoutField() {
		return layoutField;
	}
	
	public void setLayoutField(LayoutField layoutField) {
		this.layoutField = layoutField;
	}

    @Override
    public LayoutFieldVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(LayoutFieldVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public String getSurrogateAttribute() {
        return surrogateAttribute;
    }

    public void setSurrogateAttribute(String surrogateAttribute) {
        this.surrogateAttribute = surrogateAttribute;
    }
}
