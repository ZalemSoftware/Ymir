package br.com.zalem.ymir.client.android.entity.data.openmobster.cursor;

import android.database.Cursor;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer;

/**
 * Cursor baseado no OpenMobster especializado em obter registros completos de relacionamentos para entidades internas
 * a partir de um registro dono.<br>
 * Baseia-se em um cursor do banco que deve conter uma coluna do tipo <code>int</code> que retorna o índice do registro 
 * interno a ser obtido em cada linha. Este índice é referente a posição do registro dentro do array de registros do relacionamento.
 * Os registros são obtidos através de um {@link InternalMobileBeanEntityRecordSerializer} criado a partir do registro dono.<br>
 * <br>
 * Atualmente, este cursor não respeita a configuração de <code>mappedBy</code> das entidades internas ({@link EntityRelationship#getMappedBy()}),
 * não colocando os registros mestres onde deveriam. Isto é considerado uma limitação e, dependendo da priorização,
 * pode ser implementado (ToDo.txt - 17).
 * 
 * @see InternalMobileBeanEntityRecordSerializer
 *
 * @author Thiago Gesser
 */
public final class InternalMobileBeanEntityRecordCursor extends AbstractMobileBeanEntityRecordCursor {
	
	private final EntityMetadata entityMetadata;
	private final MobileBeanEntityDataManager entityManager;
	private final String ownerId;
	private final String ownerEntityName;
	private final String relFullname;
	private final int iriColIndex;
	
	private InternalMobileBeanEntityRecordSerializer internalSerializer;
	private boolean ownerIsMissing;
	
	/**
	 * Invoca o construtor {@link br.com.zalem.ymir.client.android.entity.data.openmobster.cursor.InternalMobileBeanEntityRecordCursor#InternalMobileBeanEntityRecordCursor(android.database.Cursor, MobileBeanEntityDataManager, EntityMetadata, String, String, String, int)}
	 * passando <code>0</code> como índice da coluna.
	 */
	public InternalMobileBeanEntityRecordCursor(Cursor dbCursor, MobileBeanEntityDataManager entityManager, EntityMetadata entityMetadata,
												String ownerId, String ownerEntityName, String relFullname) {
		this(dbCursor, entityManager, entityMetadata, ownerId, ownerEntityName, relFullname, 0);
	}
	
	/**
	 * Cria um InternalMobileBeanEntityRecordCursor utilizando o índice da coluna de índices para obter os registros do relacionamento interno.
	 * 
	 * @param dbCursor o cursor do banco.
	 * @param entityManager o gerenciador de entidades.
	 * @param entityMetadata os metadados da entidade interna.
	 * @param ownerId identificador do registro dono dos registros internos.
	 * @param ownerEntityName nome da entidade do registro dono.
	 * @param relFullname caminho completo até o array (relacionamento) que contém os registros internos, seguindo o {@link InternalMobileBeanEntityRecordSerializer formato do OpenMobster}.
	 * @param iriColIndex índice da coluna de índices do relacionamento interno.
	 */
	public InternalMobileBeanEntityRecordCursor(Cursor dbCursor, MobileBeanEntityDataManager entityManager, EntityMetadata entityMetadata,
												String ownerId, String ownerEntityName, String relFullname, int iriColIndex) {
		super(dbCursor);
		this.entityManager = entityManager;
		this.entityMetadata = entityMetadata;
		this.ownerId = ownerId;
		this.ownerEntityName = ownerEntityName;
		this.relFullname = relFullname;
		this.iriColIndex = iriColIndex;
	}

	@Override
	public IEntityRecord getEntityRecord() {
		//Se o serializaer não pode ser criado, não há registro para retornar.
		InternalMobileBeanEntityRecordSerializer is = getInternalSerializer();
		if (is == null) {
			return null;
		}
		
		int entryIndex = dbCursor.getInt(iriColIndex);
		return is.deserializeRelationshipArrayValueEntry(relFullname, entityMetadata, entryIndex);
	}

	
	/*
	 * Métodos auxiliares
	 */
	
	private InternalMobileBeanEntityRecordSerializer getInternalSerializer() {
		//Se o owner não está mais presente na base, não há como criar o serializer para obter os dados dos registros internos.
		if (ownerIsMissing) {
			return null;
		}
		
		if (internalSerializer == null) {
			MobileBeanEntityDAO ownerDAO = entityManager.getEntityDAO(ownerEntityName);
			MobileBeanEntityRecord owner = ownerDAO.get(ownerId);
			if (owner == null) {
				ownerIsMissing = true;
				return null;
			}
			
			internalSerializer = new InternalMobileBeanEntityRecordSerializer(entityManager, owner);
		}
		return internalSerializer;
	}
}
