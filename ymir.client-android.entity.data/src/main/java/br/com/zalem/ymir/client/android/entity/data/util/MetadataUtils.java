package br.com.zalem.ymir.client.android.entity.data.util;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;

/**
 * Provê métodos utilitários para o uso de metadados de entidades, atributos e relacionamentos.
 *
 * @author Thiago Gesser
 */
public final class MetadataUtils {
	
	private MetadataUtils() {}
	
	/**
	 * Obtém os metadados do atributo referenciado pelo caminho.
	 * 
	 * @param entityMetadata metadados da entidade base do caminho.
	 * @param attributePath caminho para o atributo. Pode ser diretamente um atributo da entidade base ou um caminho 
	 * que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return os metadados do atributo.
	 * @throws IllegalArgumentException se o caminho não apontava para propriedades existentes ou se os relacionamentos do meio do caminho não eram singulares.
	 */
	public static IEntityAttribute getAttributeFromPath(IEntityMetadata entityMetadata, String... attributePath) {
		IEntityMetadata curMetadata = entityMetadata;
		for (int i = 0; i < attributePath.length; i++) {
			String path = attributePath[i];
			if (i == attributePath.length-1) {
				return curMetadata.getAttribute(path);
			}
			
			IEntityRelationship relationship = curMetadata.getRelationship(path);
			checkSingleRelationship(relationship);
			curMetadata = relationship.getTarget();
		}
		throw new IllegalArgumentException("\"attributePath\" can't be empty.");
	}
	
	/**
	 * Obtém os metadados do relacionamento referenciado pelo caminho.
	 * 
	 * @param entityMetadata metadados da entidade base do caminho.
	 * @param relationshipPath caminho para o relacionamento. Pode ser diretamente um relacionamento da entidade base ou
	 * um caminho que parte dela, navega por relacionamentos singulares e chega no relacionamento desejado.
	 * @return os metadados do relacionamento.
	 * @throws IllegalArgumentException se o caminho não apontava para relacionamentos existentes ou se os relacionamentos do meio do caminho não eram singulares.
	 */
	public static IEntityRelationship getRelationshipFromPath(IEntityMetadata entityMetadata, String... relationshipPath) {
		IEntityMetadata curMetadata = entityMetadata;
		for (int i = 0; i < relationshipPath.length; i++) {
			String path = relationshipPath[i];
			if (i == relationshipPath.length-1) {
				return curMetadata.getRelationship(path);
			}
			
			IEntityRelationship relationship = curMetadata.getRelationship(path);
			checkSingleRelationship(relationship);
			curMetadata = relationship.getTarget();
		}
		throw new IllegalArgumentException("\"relationshipPath\" can't be empty.");
	}

    /**
     * Obtém os metadados da entidade final referenciada pelo caminho para uma propriedade (atributo ou relacionamento).<br>
     * Por exemplo:<br>
     * Se o caminho for ["entidade1, entidade2, campo1"], será retornado os metadados da <code>entidade2</code><br>
     * Se o caminho for ["campo1"], será retornado os metadados da entidade base.<br>
     *
     * @param entityMetadata metadados da entidade base do caminho.
     * @param propertyPath caminho para a propriedade (atributo ou relacionamento). Pode ser diretamente uma propriedade da entidade base ou
     * um caminho que parte dela, navega por relacionamentos singulares e chega na propriedade desejado.
     * @return os metadados da entidade.
     * @throws IllegalArgumentException se o caminho não apontava para propriedades existentes ou se os relacionamentos do meio do caminho não eram singulares.
     */
    public static IEntityMetadata getEntityFromPath(IEntityMetadata entityMetadata, String... propertyPath) {
        for (int i = 0; i < propertyPath.length-1; i++) {
            IEntityRelationship relationship = entityMetadata.getRelationship(propertyPath[i]);
            MetadataUtils.checkSingleRelationship(relationship);
            entityMetadata = relationship.getTarget();
        }
        return entityMetadata;
    }
	
	/**
	 * Verifica se o atributo é do tipo singular, ou seja, que não é do tipo array.
	 * 
	 * @param attribute o atributo que será verificado.
	 * @return <code>true</code> se o atributo é singular e <code>false</code> caso contrário.
	 */
	public static boolean isSingleAttribute(IEntityAttribute attribute) {
		switch (attribute.getType()) {
			case TEXT:
			case CHARACTER:
			case INTEGER:
			case DECIMAL:
			case BOOLEAN:
			case DATE:
			case TIME:
			case DATETIME:
			case IMAGE:
				return true;
			
			case TEXT_ARRAY:
			case CHARACTER_ARRAY:
			case INTEGER_ARRAY:
			case DECIMAL_ARRAY:
			case BOOLEAN_ARRAY:
			case DATE_ARRAY:
			case TIME_ARRAY:
			case DATETIME_ARRAY:
			case IMAGE_ARRAY:
				return false;

			default:
				throw new IllegalArgumentException("Unsupported EntityAttributeType: " + attribute.getType());
	
		}
	}
	
	/**
	 * Verifica se o relacionamento é do tipo singular, ou seja, que não é do tipo array.
	 * 
	 * @param relationship o relacionamento que será verificado.
	 * @return <code>true</code> se o relacionamento é singular e <code>false</code> caso contrário.
	 */
	public static boolean isSingleRelationship(IEntityRelationship relationship) {
		switch (relationship.getType()) {
			case ASSOCIATION:
			case COMPOSITION:
				return true;
				
			case ASSOCIATION_ARRAY:
			case COMPOSITION_ARRAY:
				return false;
				
			default:
				throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationship.getType());
	
		}
	}
	
	/**
	 * Verifica se o relacionamento é uma composição.
	 * 
	 * @param relationship o relacionamento que será verificado.
	 * @return <code>true</code> se o relacionamento é uma composição e <code>false</code> caso contrário.
	 */
	public static boolean isComposition(IEntityRelationship relationship) {
		return isComposition(relationship.getType());
	}

	/**
	 * Verifica se o tipo de relacionamento é uma composição.
	 *
	 * @param relationshipType o tipo do relacionamento que será verificado.
	 * @return <code>true</code> se o tipo de relacionamento é uma composição e <code>false</code> caso contrário.
	 */
	public static boolean isComposition(EntityRelationshipType relationshipType) {
		switch (relationshipType) {
			case COMPOSITION_ARRAY:
			case COMPOSITION:
				return true;

			case ASSOCIATION:
			case ASSOCIATION_ARRAY:
				return false;

			default:
				throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationshipType);

		}
	}

	/**
	 * Valida se o relacionamento é do tipo singular através do método {@link #isSingleRelationship(IEntityRelationship)},
	 * lançando um {@link IllegalArgumentException} se não for.
	 * 
	 * @param relationship o relacionamento que será verificado.
	 * @throws IllegalArgumentException se o relacionamento não for singular.
	 */
	public static void checkSingleRelationship(IEntityRelationship relationship) {
		if (!isSingleRelationship(relationship)) {
			throw new IllegalArgumentException(String.format("The relationship \"%s\" is not singular.", relationship.getName()));
		}
	}
	
	/**
	 * Valida se o relacionamento é do tipo múltiplo através do método {@link #isSingleRelationship(IEntityRelationship)},
	 * lançando um {@link IllegalArgumentException} se não for.
	 * 
	 * @param relationship o relacionamento que será verificado.
	 * @throws IllegalArgumentException se o relacionamento não for múltiplo.
	 */
	public static void checkMultipleRelationship(IEntityRelationship relationship) {
		if (isSingleRelationship(relationship)) {
			throw new IllegalArgumentException(String.format("The relationship \"%s\" is not multiple.", relationship.getName()));
		}
	}
}
