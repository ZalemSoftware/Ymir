package br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.ILabelableFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListDisplayConfig;

/**
 * Mapeamento entre um campo da entidade de dados para um campo adicional de detalhe.
 *
 * @author Thiago Gesser
 */
public interface IDetailFieldMapping extends ILabelableFieldMapping {

	/**
	 * <b>Configuração opcional.</b><br>
	 * <b>Só pode ser utilizada se este mapeamento referenciar um relacionamento múltiplo.</b><br> 
	 * <br>
	 * Obtém a configuração de lista que será utilizado na listagem dos registros do relacionamento múltiplo.<br>
	 * Se alguma propriedade desta configuração não for definida (ou toda ela), serão utilizadas as propriedades
	 * da configuração de lista da própria entidade alvo do relacionamento (ignorando as abas).
	 * 
	 * @return a configuração de lista obtida ou <code>null</code> se ela não foi definida.
	 */
	IListDisplayConfig getListConfig();

    /**
     * <b>Configuração opcional, sendo que o padrão é <code>false</code>.</b><br>
     * Determina se o rótulo do campo deverá ser escondido.
     *
     * @return <code>true</code> se o rótulo do campo deve ser escondido e <code>false</code> caso contrário.
     */
    boolean isLabelHidden();
}
