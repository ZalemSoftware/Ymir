package br.com.zalem.ymir.client.android.entity.data.openmobster.metadata;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;

/**
 * Representação de um campo de entidade.
 *
 * @author Thiago Gesser
 */
public final class EntityAttribute implements IEntityAttribute {

	private final String name;
	private final EntityAttributeType type;
	
	@JsonCreator
	public EntityAttribute(@JsonProperty("name") String name, @JsonProperty("type") EntityAttributeType type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public EntityAttributeType getType() {
		return type;
	}
}
