package br.com.zalem.ymir.client.android.entity.ui.configuration.json.field;

import android.text.TextUtils;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.ILabelableFieldMapping;

/**
 * Generalização de um mapeamento de campo rotuável de entidade para JSON.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractJsonLabelableFieldMapping extends AbstractJsonFormattableFieldMapping implements ILabelableFieldMapping {

	private String label;

    @Override
    public String getLabel() {
        //Se não tem valor definido, retorna o padrão que é o nome do campo.
        if (TextUtils.isEmpty(label)) {
            String[] values = getAttribute();
            if (values == null || values.length == 0) {
                values = getRelationship();
                if (values == null  || values.length == 0) {
                    return null;
                }
            }
            return values[values.length-1];
        }
        return label;
    }

    /**
     * Retorna o valor defindio para o label, sem considerar o valor padrão.
     *
     * @return o label obtido.
     */
    public final String getLabelValue() {
        return label;
    }

    public final void setLabel(String label) {
        this.label = label;
    }
}
