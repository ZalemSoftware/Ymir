package br.com.zalem.ymir.client.android.entity.data.openmobster.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;

/**
 * Configuração de um relacionamento de uma entidade.<br>
 * É utilizada na criação do {@link EntityMetadata}, servindo como base para as instâncias de {@link EntityRelationship}.
 * As descrições das configurações estão presentes na classe EntityRelationship.
 *
 * @author Thiago Gesser
 */
public final class EntityRelationshipConfig {

	private final String name;
	private final EntityRelationshipType type;
	private final String entity;
	private final String mappedBy;
	
	public EntityRelationshipConfig(@JsonProperty("name") String name, 
								    @JsonProperty("type") EntityRelationshipType type,
								    @JsonProperty("entity") String entity,
								    @JsonProperty("mappedBy") String mappedBy) {
		this.name = name;
		this.type = type;
		this.entity = entity;
		this.mappedBy = mappedBy;
	}

	public String getName() {
		return name;
	}

	public EntityRelationshipType getType() {
		return type;
	}

	public String getEntity() {
		return entity;
	}
	
	public String getMappedBy() {
		return mappedBy;
	}
}
