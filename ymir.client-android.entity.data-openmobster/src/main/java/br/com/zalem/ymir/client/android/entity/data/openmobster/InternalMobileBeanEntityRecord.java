package br.com.zalem.ymir.client.android.entity.data.openmobster;

import org.openmobster.android.api.sync.MobileBean;

import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer;

/**
 * Registro de uma entidade de dados interna baseada no {@link org.openmobster.android.api.sync.MobileBean} do OpenMobster.<br>
 * Armazena o registro dono e o caminho que parte dele até o registro interno, possibilitando o controle correto deste
 * tipo de registro. Estas informações só são definidas após a serialização/deserialização do registro.
 * 
 * @see InternalMobileBeanEntityRecordSerializer
 *
 * @author Thiago Gesser
 */
public final class InternalMobileBeanEntityRecord extends MobileBeanEntityRecord {
	
	private MobileBeanEntityRecord owner;
	private String ownerPath;
		
	InternalMobileBeanEntityRecord(MobileBean bean, EntityMetadata metadata, MobileBeanEntityDataManager entityManager) {
		super(bean, metadata, entityManager);
	}
	
	@Override
	protected MobileBeanEntityRecord getSourceRelatedRecord(EntityRelationship relationship) {
		//Se não possui owner definido, não não há valores para buscar do relacionamento interno. 
		if (relationship.getTarget().isInternal() && owner == null) {
			return null;
		}
		
		return super.getSourceRelatedRecord(relationship);
	}
	
	@Override
	protected MobileBeanEntityRecord[] getSourceRelatedRecords(EntityRelationship relationship) {
		//Se não possui owner definido, não não há valores para buscar do relacionamento interno. 
		if (relationship.getTarget().isInternal() && owner == null) {
			return null;
		}
		
		return super.getSourceRelatedRecords(relationship);
	}
	

	public MobileBeanEntityRecord getOwner() {
		return owner;
	}
	
	public void setOwner(MobileBeanEntityRecord owner) {
		this.owner = owner;
	}
	
	public String getOwnerPath() {
		return ownerPath;
	}
	
	public void setOwnerPath(String ownerPath) {
		this.ownerPath = ownerPath;
	}
}
