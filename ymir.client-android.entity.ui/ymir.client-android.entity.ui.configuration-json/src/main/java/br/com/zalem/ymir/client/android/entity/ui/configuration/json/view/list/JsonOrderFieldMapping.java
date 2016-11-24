package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IOrderFieldMapping;

/**
 * Representação do mapeamento de campo um ordenação para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonOrderFieldMapping extends AbstractJsonFieldMapping implements IOrderFieldMapping {

	private boolean asc = true;
	
	@Override
	public boolean isAsc() {
		return asc;
	}

    public void setAsc(boolean asc) {
        this.asc = asc;
    }
}
