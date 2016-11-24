package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFormattableFieldMapping;

/**
 * Mapeamento de um campo de entidade para ser utilizado como filtro.
 *
 * @author Thiago Gesser
 */
public interface IFilterFieldMapping extends IFormattableFieldMapping {

    /**
     * Valores aceitos pelo filtro. Os registros devem conter ao menos um destes valores no campo designado para passarem pelo filtro.<br>
     * Se o campo for um atributo, cada valor será parseado de acordo com a máscara definida para o campo.
     * Se for um relacionamento, os valores deverão representar os identificadores dos registros que se deseja referenciar.
     *
     * @return os valores do filtro.
     */
    String[] getValues();
}
