package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;

import br.com.zalem.ymir.client.android.entity.data.SyncStatus;

/**
 * Configuração de filtro de listagem de registros de entidade.<br>
 *
 * @author Thiago Gesser
 */
public interface IListFilter {

	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém os status de sincronização que devem ser utilizados na filtragem dos registros.<br>
     * Os registros devem conter ao menos um dos status para passarem pelo filtro.
	 *  
	 * @return os status obtidos.
	 */
	SyncStatus[] getSyncStatus();

    /**
     * <b>Configuração opcional.</b><br>
     * <br>
     * Obtém as restrições de campos/valores que devem ser utilizados na filtragem dos registros.<br>
     * Os registros devem atender a todas as restrições de campos/valores para passarem pelo filtro.<br>
     *
     * @return as restrições obtidas.
     */
    IFilterFieldMapping[] getFields();
}
