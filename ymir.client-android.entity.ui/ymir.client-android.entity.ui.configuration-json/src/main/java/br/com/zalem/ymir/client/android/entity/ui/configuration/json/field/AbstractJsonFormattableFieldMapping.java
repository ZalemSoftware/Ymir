package br.com.zalem.ymir.client.android.entity.ui.configuration.json.field;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFormattableFieldMapping;

/**
 * Generalização de um mapeamento de campo formatável de entidade para JSON.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractJsonFormattableFieldMapping extends AbstractJsonFieldMapping implements IFormattableFieldMapping {

	private String mask;

    @Override
    public final String getMask() {
        return mask;
    }

    public final void setMask(String mask) {
        this.mask = mask;
    }
}
