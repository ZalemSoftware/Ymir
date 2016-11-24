package br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing;

import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldEnum;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonFormattableFieldMapping;

/**
 * Representação da configuração de um campo do tipo enumeração para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonEditingFieldEnum extends AbstractJsonFormattableFieldMapping implements IEditingFieldEnum {

	private String[] values;

	@Override
	public String[] getValues() {
		return values;
	}

    public void setValues(String[] values) {
		this.values = values;
	}
}
