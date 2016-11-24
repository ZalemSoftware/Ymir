package br.com.zalem.ymir.client.android.entity.data.view;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;

/**
 * Configuração de uma visão limitada de dados de uma entidade proveniente de um relacionamento de tipo <code>array</code>
 * de um registro. 
 *
 * @author Thiago Gesser
 */
public final class RelationshipArrayView {
	
	private final IEntityRecord record;
	private final IEntityRelationship relationship;

	public RelationshipArrayView(IEntityRecord record, String relationshipName) {
		IEntityRelationship relationship = record.getEntityMetadata().getRelationship(relationshipName);
		
		//Verifica se é um relacionamento múltiplo.
		if (MetadataUtils.isSingleRelationship(relationship)) {
			throw new IllegalArgumentException(String.format("Only a multiple relationship is allowed to the RelationshipArrayViewDAO. Entity: %s, relationship: %s.", record.getEntityMetadata().getName(), relationship.getName()));
		}
		
		this.record = record;
		this.relationship = relationship;
	}
	
	public IEntityRecord getRecord() {
		return record;
	}
	
	public IEntityRelationship getRelationship() {
		return relationship;
	}
}
