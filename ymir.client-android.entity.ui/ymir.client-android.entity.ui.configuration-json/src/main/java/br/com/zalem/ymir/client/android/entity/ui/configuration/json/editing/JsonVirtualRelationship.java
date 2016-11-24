package br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IVirtualRelationship;

/**
 * Representação da configuração de um relacionamento virtual para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonVirtualRelationship implements IVirtualRelationship {

	private String name;
	private EntityRelationshipType type;
	private String entity;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public EntityRelationshipType getType() {
		return type;
	}
	
	@Override
	public String getEntity() {
		return entity;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(EntityRelationshipType type) {
		this.type = type;
	}
	
	public void setEntity(String entity) {
		this.entity = entity;
	}
}
