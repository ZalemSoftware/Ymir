package br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing;

import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingPermissions;

/**
 * Representação da configuração de permissões de edição para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonEditingPermissions implements IEditingPermissions {

	private boolean canCreate;
	private boolean canUpdate;
	private boolean canDelete;
	
	@Override
	public boolean canCreate() {
		return canCreate;
	}

	@Override
	public boolean canUpdate() {
		return canUpdate;
	}

	@Override
	public boolean canDelete() {
		return canDelete;
	}

	public void setCanCreate(boolean canCreate) {
		this.canCreate = canCreate;
	}
	
	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}
	
	public void setCanUpdate(boolean canUpdate) {
		this.canUpdate = canUpdate;
	}
}
