package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonFormattableFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IFilterFieldMapping;

/**
 * Representação do mapeamento de campo de filtro em JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonFilterFieldMapping extends AbstractJsonFormattableFieldMapping implements IFilterFieldMapping {

    private String[] values;

    @Override
    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }
}
