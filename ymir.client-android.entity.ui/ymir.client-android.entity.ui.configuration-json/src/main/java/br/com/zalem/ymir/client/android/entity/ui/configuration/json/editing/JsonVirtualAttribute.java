package br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IVirtualAttribute;

/**
 * Representação da configuração de um atributo virtual para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonVirtualAttribute implements IVirtualAttribute {

	private String name;
	private EntityAttributeType type;
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public EntityAttributeType getType() {
		return type;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(EntityAttributeType type) {
		this.type = type;
	}
}
