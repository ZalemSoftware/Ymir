package br.com.zalem.ymir.client.android.entity.data.openmobster.metadata;

import android.text.TextUtils;
import java.util.HashMap;

/**
 * Validador de configurações de metadados das entidades.<br>
 * <br>
 * A validação é interrompida quando algum problema é encontrado, lançando um {@link EntityMetadataException} com os
 * detalhes do problema. Desta forma, os metadados são considerados válidos somente quando o método {@link #validate(EntityMetadataConfig...)}
 * consegue executar até o final sem nenhuma exceção.
 *
 * @author Thiago Gesser
 */
public final class EntityMetadataConfigValidator {

	private static final String MISSING_INDEXED_FIELD_ERROR_FORMAT = "%s[%d].%s can't be null or empty. Entity = %s.";
	
	//Não permite instanciação.
	private EntityMetadataConfigValidator() {}
	
	/**
	 * Valida as configurações de metadados das entidades, lançando um {@link EntityMetadataException} se algum problema for identificado.
	 * 
	 * @param metadatas configurações de metadados.
	 * @throws EntityMetadataException se houver algum problema com as configurações.
	 */
	public static void validate(EntityMetadataConfig... metadatas) throws EntityMetadataException {
		if (metadatas == null || metadatas.length == 0) {
			throw new EntityMetadataException("At least one EntityMetadata must be defined.");
		}
		
		HashMap<String, EntityMetadataConfig> entitiesMap = new HashMap<>(metadatas.length);
		for (EntityMetadataConfig metadata : metadatas) {
			//Validação das configurações da entidade.
			String entityName = metadata.getName();
			if (TextUtils.isEmpty(entityName)) {
				throw new EntityMetadataException("The entity name can't be null or empty.");
			}
			
			if (entitiesMap.put(entityName, metadata) != null) {
				throw new EntityMetadataException("The entity name must be unique. The following name was declared twice: " + entityName);
			}
			
			if (metadata.isInternal() && !TextUtils.isEmpty(metadata.getChannel())) {
				throw new EntityMetadataException("Internal entities cannot declare channels. Entity = " + entityName);
			}
			
			EntityAttribute[] fields = metadata.getAttributes();
			EntityRelationshipConfig[] relationships = metadata.getRelationships();
			boolean hasFields = fields != null && fields.length > 0;
			boolean hasRelationships = relationships != null && relationships.length > 0;
			if (!hasFields && !hasRelationships) {
				throw new EntityMetadataException("At least one field or relationship must be defined. Entity = " + entityName);
			}
			
			//Validação das configurações dos campos.
			if (hasFields) {
				for (int i = 0; i < fields.length; i++) {
					EntityAttribute field = fields[i];
					if (TextUtils.isEmpty(field.getName())) {
						throw new EntityMetadataException(String.format(MISSING_INDEXED_FIELD_ERROR_FORMAT, "fields", i, "name", entityName));
					}
					if (field.getType() == null) {
						throw new EntityMetadataException(String.format(MISSING_INDEXED_FIELD_ERROR_FORMAT, "fields", i, "type", entityName));
					}
				}
			}
			
			//Validação das configurações de relacionamentos.
			if (hasRelationships) {
				for (int i = 0; i < relationships.length; i++) {
					EntityRelationshipConfig relationship = relationships[i];
					if (TextUtils.isEmpty(relationship.getName())) {
						throw new EntityMetadataException(String.format(MISSING_INDEXED_FIELD_ERROR_FORMAT, "relationships", i, "name", entityName));
					}
					if (relationship.getType() == null) {
						throw new EntityMetadataException(String.format(MISSING_INDEXED_FIELD_ERROR_FORMAT, "relationships", i, "type", entityName));
					}
					if (TextUtils.isEmpty(relationship.getEntity())) {
						throw new EntityMetadataException(String.format(MISSING_INDEXED_FIELD_ERROR_FORMAT, "relationships", i, "entity", entityName));
					}
				}
			}
		}
		
		//Validação dos relacionamentos das entidades.
		for (EntityMetadataConfig metadata : metadatas) {
			EntityRelationshipConfig[] relationships = metadata.getRelationships();
			if (relationships == null || relationships.length == 0) {
				continue;
			}
			
			String entityName = metadata.getName();
			for (EntityRelationshipConfig relationship : relationships) {
				//Verifica se o relacionamento aponta para uma entidade existente.
				EntityMetadataConfig targetEntityMetadata = entitiesMap.get(relationship.getEntity());
				if (targetEntityMetadata == null) {
					throw new EntityMetadataException(String.format("Relationship's target entity not found: \"%s\". Source entity = %s, relationship = %s.", relationship.getEntity(), entityName, relationship.getName()));
				}
				
				//Se é um relacionamento para uma entidade interna, verifica se é uma composição ou se é uma referência para seu dono.
				inRelVal: if (targetEntityMetadata.isInternal() && !isComposition(relationship)) {
					for (EntityRelationshipConfig targetRel : targetEntityMetadata.getRelationships()) {
						if (targetRel.getEntity().equals(entityName) && isComposition(targetRel)) {
							break inRelVal;
						}
					}
					
					throw new EntityMetadataException(String.format("A relationship targeting an internal entity must be a composition. Source entity = %s, relationship = %s, Target entity = %s.", entityName, relationship.getName(), relationship.getEntity()));						
				}
				
				//Se possui mappedBy, o relacionamento mapeado deve estar declarado na entidade alvo, ser singular e não possuir mappedBy também.
				String mappedBy = relationship.getMappedBy();
				if (!TextUtils.isEmpty(mappedBy)) {
					//Relacionamentos que partem de uma entidade interna para uma normal não podem ser relacionados pelo alvo, pois o alvo não vai conseguir referenciar um registro interno (sem id).
					if (metadata.isInternal() && !targetEntityMetadata.isInternal()) {
						throw new EntityMetadataException(String.format("A relationship starting from an internal entity must be referenced by the source, thus cannot declare the property \"mappedBy\". Source entity = %s, relationship = %s, Target entity = %s.", entityName, relationship.getName(), relationship.getEntity()));
					}
					
					EntityRelationshipConfig mappedRelacionship = null;
					EntityRelationshipConfig[] targetRelationships = targetEntityMetadata.getRelationships();
					if (targetRelationships != null) {
						for (EntityRelationshipConfig rc : targetRelationships) {
							if (rc.getName().equals(mappedBy)) {
								mappedRelacionship = rc;
								break;
							}
						}
					}

					//Verifica a existência.
					if (mappedRelacionship == null) {
						throw new EntityMetadataException(String.format("The relationship's target entity does not declare the mappedBy relationship: \"%s\". Source entity = %s, relationship = %s, Target entity = %s.", mappedBy, entityName, relationship.getName(), relationship.getEntity()));
					}
					//Verifica se aponta para a entidade fonte.
					if (!entityName.equals(mappedRelacionship.getEntity())) {
						throw new EntityMetadataException(String.format("The relationship's mappedBy must point to a relationship that references the source entity: \"%s\". Source entity = %s, relationship = %s, Target entity = %s.", mappedBy, entityName, relationship.getName(), relationship.getEntity()));
					}
					//Verifica se é singular.
					switch (mappedRelacionship.getType()) {
						case ASSOCIATION_ARRAY:
						case COMPOSITION_ARRAY:
							throw new EntityMetadataException(String.format("The relationship's mappedBy must point to a single relationship: \"%s\". Source entity = %s, relationship = %s, Target entity = %s.", mappedBy, entityName, relationship.getName(), relationship.getEntity()));
						case ASSOCIATION:
						case COMPOSITION:
							break;
						
						default:
							throw new EntityMetadataException("Invalid EntityRelationshipType: " + mappedRelacionship.getType());
					}

					//Verifica se não possui mappedBy.
					if (!TextUtils.isEmpty(mappedRelacionship.getMappedBy())) {
						throw new EntityMetadataException(String.format("The relationship's mappedBy must point to a relationship that does not declare mappedBy too: \"%s\". Source entity = %s, relationship = %s, Target entity = %s.", mappedBy, entityName, relationship.getName(), relationship.getEntity()));
					}
				}
			}
		}
	}
	
	private static boolean isComposition(EntityRelationshipConfig relationship) throws EntityMetadataException {
		switch (relationship.getType()) {
			case ASSOCIATION:
			case ASSOCIATION_ARRAY:
				return false;
				
			case COMPOSITION:
			case COMPOSITION_ARRAY:
				return true;
			
			default:
				throw new EntityMetadataException("Invalid EntityRelationshipType: " + relationship.getType());
		}
	}
}
