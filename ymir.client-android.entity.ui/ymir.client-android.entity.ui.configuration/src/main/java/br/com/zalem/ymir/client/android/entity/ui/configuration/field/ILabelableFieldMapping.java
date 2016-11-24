package br.com.zalem.ymir.client.android.entity.ui.configuration.field;

/**
 * Generalização de um mapeamento de campo de entidade que pode ser rotuável.
 * Não é uma configuração em si, devendo ser apenas para ser estendida por outras interfaces de configuração.
 *
 * @author Thiago Gesser
 */
public interface ILabelableFieldMapping extends IFormattableFieldMapping {

    /**
     * <b>Configuração opcional, sendo que o padrão é o próprio nome do campo.</b><br>
     * <br>
     * Obtém o rótulo para ser utilizado nas exibições do campo.
     *
     * @return o rótulo obtido.
     */
    String getLabel();
}
