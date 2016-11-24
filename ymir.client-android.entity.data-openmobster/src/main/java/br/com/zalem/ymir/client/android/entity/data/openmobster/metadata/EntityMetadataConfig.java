package br.com.zalem.ymir.client.android.entity.data.openmobster.metadata;

import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuração de metadados de uma entidade.<br>
 * É utilizada na criação do {@link MobileBeanEntityDataManager}, servindo como base para as instâncias de {@link EntityMetadata}.
 * As descrições das configurações estão presentes na classe EntityMetadata.
 *
 * @author Thiago Gesser
 */
public final class EntityMetadataConfig {

	private final String name;
	private final String channel;
	private final boolean internal;
	private final EntityAttribute[] attributes;
	private final EntityRelationshipConfig[] relationships;

	@JsonCreator
	public EntityMetadataConfig(@JsonProperty("name") String name, 
								@JsonProperty(value = "channel", required = false) String channel,
								@JsonProperty(value = "internal", required = false) boolean internal,
								@JsonProperty("attributes") EntityAttribute[] attributes,
								@JsonProperty("relationships") EntityRelationshipConfig[] relationships) {
		this.name = name;
		this.channel = channel;
		this.internal = internal;
		this.attributes = attributes;
		this.relationships = relationships;
	}
	
	public String getName() {
		return name;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public boolean isInternal() {
		return internal;
	}
	
	public EntityAttribute[] getAttributes() {
		return attributes;
	}
	
	public EntityRelationshipConfig[] getRelationships() {
		return relationships;
	}
}
