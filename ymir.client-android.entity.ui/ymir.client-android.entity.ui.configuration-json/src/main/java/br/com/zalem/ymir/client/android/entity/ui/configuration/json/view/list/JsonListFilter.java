package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListFilter;

/**
 * Representa da configuração de filtro de lista para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonListFilter implements IListFilter {

	private SyncStatus[] syncStatus;
    private JsonFilterFieldMapping[] fields;
	
	@Override
	public SyncStatus[] getSyncStatus() {
		return syncStatus;
	}

    @Override
    public JsonFilterFieldMapping[] getFields() {
        return fields;
    }

    public void setSyncStatus(SyncStatus[] syncStatus) {
		this.syncStatus = syncStatus;
	}

    public void setFields(JsonFilterFieldMapping[] fields) {
        this.fields = fields;
    }
}
