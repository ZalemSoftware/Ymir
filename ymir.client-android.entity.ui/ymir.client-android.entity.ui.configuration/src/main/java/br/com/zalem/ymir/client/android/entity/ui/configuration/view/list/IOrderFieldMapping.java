package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFieldMapping;

/**
 * Mapeamento de um campo de entidade para um campo de ordenação.
 *
 * @author Thiago Gesser
 */
public interface IOrderFieldMapping extends IFieldMapping {
	
	/**
	 * Obtém a configuração referente à forma de ordenação do campo (crescente ou decrescente).<br>
	 * O valor padrão desta configuração é <code>true</code>.
	 * 
	 * @return <code>true</code> se for crescente e <code>false</code> se for decrescente.
	 */
	boolean isAsc();
}
