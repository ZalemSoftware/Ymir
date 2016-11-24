package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;

/**
 * Configuração de ordenação de listagem de registros de entidade.
 *
 * @author Thiago Gesser
 */
public interface IListOrder {

	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém os campos que serão utilizados como critérios de ordenação na listagem de registros.<br>
	 * A ordem destes campos no array determina a prioridade dos critérios.
	 * 
	 * @return os campos obtidos.
	 */
	IOrderFieldMapping[] getFields();
}
