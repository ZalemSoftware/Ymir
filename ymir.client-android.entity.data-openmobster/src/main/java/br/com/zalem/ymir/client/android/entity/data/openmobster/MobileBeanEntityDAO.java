package br.com.zalem.ymir.client.android.entity.data.openmobster;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.AndroidBugsUtils;
import android.text.TextUtils;
import android.util.Log;

import org.openmobster.android.api.sync.BeanList;
import org.openmobster.android.api.sync.CommitException;
import org.openmobster.android.api.sync.MobileBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.RelationshipViolationException;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityRecord.RelationshipBufferEntry;
import br.com.zalem.ymir.client.android.entity.data.openmobster.cursor.InternalMobileBeanEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.openmobster.cursor.MobileBeanEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.openmobster.cursor.MobileBeanSelectionCursor;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.InternalMobileBeanSelectQuery;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.MobileBeanQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.MobileBeanSelectQuery;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.SelectFromQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectFromStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery.ISelectField;
import br.com.zalem.ymir.client.android.entity.data.query.select.NonUniqueResultException;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * Provê a manipulação de registros de uma entidade de dados baseada no {@link org.openmobster.android.api.sync.MobileBean} do OpenMobster.
 *
 * @author Thiago Gesser
 */
public class MobileBeanEntityDAO implements IEntityDAO {
	
	private boolean isReady;
	protected final EntityMetadata metadata;
	protected final MobileBeanEntityDataManager entityManager;
	
	/**
	 * Cria um MobileBeanEntityDAO para uma entidade, de acordo com seus metadados.
	 * 
	 * @param metadata metadados da entidade.
	 */
	MobileBeanEntityDAO(EntityMetadata metadata, MobileBeanEntityDataManager entityManager) {
		this.metadata = metadata;
		this.entityManager = entityManager;
	}
	
	@Override
	public final MobileBeanEntityDataManager getEntityManager() {
		return entityManager;
	}
	
	@Override
	public final EntityMetadata getEntityMetadata() {
		return metadata;
	}
	
	@Override
	public boolean isReady() {
		return isReady(null);
	}

	@Override
	public MobileBeanEntityRecord create() {
        return create(MobileBean.newInstance(metadata.getChannel()));
	}

    @Override
    public MobileBeanEntityRecord create(Serializable id) {
        return create(MobileBean.newInstance(metadata.getChannel(), id.toString()));
    }

    @Override
	public boolean save(IEntityRecord record, boolean sync) {
        return save(record, sync, true);
    }

    public boolean save(IEntityRecord record, boolean sync, boolean scheduleSyncTask) {
        checkIsReady();
        checkNotInternal();
        checkNotDeleted(record);
        MobileBeanEntityRecord beanRecord = (MobileBeanEntityRecord) record;
        checkRecordMetadata(beanRecord);

        MobileBean.beginTransaction();
        try {
            internalSave(beanRecord, sync, scheduleSyncTask);

            MobileBean.setTransactionSuccessful();
            return true;
        } catch (CommitException e) {
            Log.e(MobileBeanEntityDAO.class.getSimpleName(), String.format("CommitException at save(). Record id = %s, Message = %s.", record.getId(), e.getMessage()));
            return false;
        } catch (RelationshipViolationException e) {
            Log.e(MobileBeanEntityDAO.class.getSimpleName(), String.format("RelationshipViolationException at save(). Record id = %s, Violated entities = %s, Message = %s.", record.getId(), Arrays.toString(e.getSourceEntities()), e.getMessage()));
            return false;
        } finally {
            MobileBean.endTransaction();
        }
    }

    @Override
	public boolean delete(IEntityRecord record, boolean sync) throws RelationshipViolationException {
		checkIsReady();
		checkNotInternal();
        checkNotDeleted(record);
		MobileBeanEntityRecord beanRecord = (MobileBeanEntityRecord) record;
		checkRecordMetadata(beanRecord);
        if (!sync) {
            //O "deleteWithoutSync" do OpenMobster na verdade só posterga a sincronização. Teria que ser feito algo similar ao "saveLocal" para suportar o delete sem sync.
            throw new PendingFeatureException("delete with sync == false");
        }

        MobileBean.beginTransaction();
        try {
            internalDelete(beanRecord);

            MobileBean.setTransactionSuccessful();
            return true;
        } catch (CommitException e) {
            Log.e(MobileBeanEntityDAO.class.getSimpleName(), String.format("CommitException at delete(). Record id = %s, Message = %s.", record.getId(), e.getMessage()));
            return false;
        } finally {
            MobileBean.endTransaction();
        }
	}

	@Override
	public boolean refresh(IEntityRecord record) {
		checkIsReady();
        checkNotDeleted(record);
		MobileBeanEntityRecord beanRecord = (MobileBeanEntityRecord) record;
		checkRecordMetadata(beanRecord);
		
		//Se for interno ou novo, não há o q fazer.
		if (metadata.isInternal() || beanRecord.isNew()) {
			return true;
		}
		
		MobileBean bean = beanRecord.getBean();
		bean.refresh();
		return !bean.isDeleted();
	}

    @Override
    public MobileBeanEntityRecord copy(IEntityRecord record) {
        return copy(record, false);
    }

	@Override
	public MobileBeanEntityRecord copy(IEntityRecord record, boolean fresh) {
		checkIsReady();
		MobileBeanEntityRecord beanRecord = (MobileBeanEntityRecord) record;
		checkRecordMetadata(beanRecord);

        if (fresh) {
            return copyFreshRecord(beanRecord, null);
        }

        checkNotDeleted(record);
        MobileBeanEntityRecord copy;
        if (record.isNew()) {
            copy = create();
        } else {
            copy = get(record.getId());
        }
        //As tags fazem parte de um campo especial, por isto n é tratado junto com os demais.
        copy.setTags(beanRecord.getTags());

        if (!record.isDirty()) {
            return copy;
        }

        //Copia os valores alterados dos campos.
        Set<String> dirtyFields = beanRecord.getDirtyFields();
        if (dirtyFields != null) {
            for (String dirtyField : dirtyFields) {
                copy.setBeanValue(dirtyField, beanRecord.getBeanValue(dirtyField));
            }
        }

        //Copia os relacionamentos presentes no buffer.
        Map<String, RelationshipBufferEntry> relationshipsBuffer = beanRecord.getRelationshipsBuffer();
        if (relationshipsBuffer != null) {
            Map<String, RelationshipBufferEntry> relationshipsBufferCopy = new HashMap<>();

            for (Entry<String, RelationshipBufferEntry> mapEntry : relationshipsBuffer.entrySet()) {
                String relationshipName = mapEntry.getKey();
                EntityRelationship relationship = metadata.getRelationship(relationshipName);
                MobileBeanEntityDAO relationshipDAO = entityManager.getEntityDAO(relationship.getTarget().getName());
                RelationshipBufferEntry bufferEntry = mapEntry.getValue();
                List<MobileBeanEntityRecord> relationshipRecords = bufferEntry.getRelationships();

                List<MobileBeanEntityRecord> relationshipsRecordsCopy = new ArrayList<>(relationshipRecords.size());
                for (MobileBeanEntityRecord relationshipRecord : relationshipRecords) {
                    MobileBeanEntityRecord relationshipRecordCopy;
                    if (MetadataUtils.isComposition(relationship) || relationshipRecord.isDirty()) {
                        relationshipRecordCopy = relationshipDAO.copy(relationshipRecord);
                    } else {
                        //Se for uma associação e n estiver sujo, pode usar diretamente o registro ao invés de copiá-lo.
                        relationshipRecordCopy = relationshipRecord;
                    }

                    relationshipsRecordsCopy.add(relationshipRecordCopy);
                }

                relationshipsBufferCopy.put(relationshipName, new RelationshipBufferEntry(relationshipsRecordsCopy));
            }

            copy.setRelationshipsBuffer(relationshipsBufferCopy);
        }

		return copy;
	}

    @Override
	public MobileBeanEntityRecord get(Serializable id) {
		checkIsReady();
		checkNotInternal();
		
		MobileBean bean = MobileBean.readById(metadata.getChannel(), id.toString());
		if (bean == null) {
			return null;
		}
		return new MobileBeanEntityRecord(bean, metadata, entityManager);
	}

	@Override
	public List<IEntityRecord> getAll() {
		checkIsReady();
		checkNotInternal();
		
		return adaptBeans(MobileBean.readAll(metadata.getChannel()));
	}

    @Override
    public boolean isEmpty() {
        return MobileBean.isEmpty(metadata.getChannel());
    }

    @Override
	public Parcelable toSavedState(IEntityRecord record) {
		MobileBeanEntityRecord beanRecord = (MobileBeanEntityRecord) record;
		checkRecordMetadata(beanRecord);
		
		return new RecordHolderSavedState(beanRecord);
	}

	@Override
	public IEntityRecord fromSavedState(Parcelable savedState) {
		RecordHolderSavedState recordHolder = (RecordHolderSavedState) savedState;
		return recordHolder.getRecord(this, entityManager);
	}
	

	@Override
	public ISelectFromStatement select() {
		return select(false);
	}

	@Override
	public ISelectFromStatement select(boolean distinct) {
		return newSelectBuilder(distinct);
	}

    @Override
    public ISelectBuilder newSelectBuilder(boolean distinct) {
        checkNotInternal();

        return new SelectFromQueryBuilder(entityManager, metadata.getName(), distinct);
    }

    @Override
	public <T> T executeUniqueSelect(ISelectQuery query) throws NonUniqueResultException {
		List<T> list = executeListSelect(query);
		if (list.isEmpty()) {
			return null;
		}
		if (list.size() > 1) {
			throw new NonUniqueResultException(query);
		}
		
		return list.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> executeListSelect(ISelectQuery query) {
		//Simplesmente itera pelo cursor e obtém os valores de acordo com a seleção.
		IEntityRecordCursor cursor = executeCursorSelect(query);
		try {
			List<T> result = new ArrayList<>();
			ISelectField[] fields = query.getFields();
			
			if (fields.length == 0) {
				//Se não possui campos, se trata de uma seleção do registro inteiro.
				while (cursor.moveToNext()) {
					IEntityRecord entityRecord = cursor.getEntityRecord();
					if (entityRecord != null) {
						result.add((T) entityRecord);
					}
				}
			} else if (fields.length == 1) {
				//Se possui apenas um campo, retorna uma lista diretamente do tipo campo.
				while (cursor.moveToNext()) {
					result.add((T) cursor.getValue(0));
				}
			} else {
				//Coloca os valores de cada linha em tuplas (array de objetos)
				int tupleLength = fields.length;
				while (cursor.moveToNext()) {
					Object[] tuple = new Object[tupleLength];
					for (int i = 0; i < tupleLength; i++) {
						tuple[i] = cursor.getValue(i);
					}
					result.add((T) tuple);
				}
			}
			
			return result;
		} finally {
			cursor.close();
		}
	}
	
	@Override
	@SuppressWarnings("resource")
	public IEntityRecordCursor executeCursorSelect(ISelectQuery query) {
		checkIsReady();
		
		MobileBeanSelectQuery mbQuery = (MobileBeanSelectQuery) query;
		if (!metadata.getName().equals(mbQuery.getEntityName())) {
			throw new IllegalArgumentException(String.format("The query source entity is not the same of this DAO. Query source entity = %s, DAO entity = %s.", mbQuery.getEntityName(), metadata.getName()));
		}

		Cursor cursor = MobileBean.rawQuery(mbQuery.getQuery(), mbQuery.getParameters());
		ISelectField[] selectFields = query.getFields();
		//Se não possui campos, se trata de uma seleção do registro inteiro.
		if (selectFields.length == 0) {
			//Registros internos são selecioandos de forma diferente.
			if (metadata.isInternal()) {
				InternalMobileBeanSelectQuery imbQuery = (InternalMobileBeanSelectQuery) mbQuery;
				return new InternalMobileBeanEntityRecordCursor(cursor, entityManager, metadata, imbQuery.getOwnerId(), imbQuery.getOwnerEntityName(), imbQuery.getRelFullname());
			}
			return new MobileBeanEntityRecordCursor(cursor, this);
		}
		
		return new MobileBeanSelectionCursor(cursor, selectFields, metadata, entityManager);
	}
	

	/*
	 * Métodos auxiliares
	 */

    public void resetReady() {
        isReady = false;
    }
	
	protected final void checkIsReady() {
		if (!isReady()) {
			throw new IllegalStateException("isReady() == false");
		}
	}
	
	protected final void checkRecordMetadata(MobileBeanEntityRecord record) {
		//Existe uma instância de metadata para cada entidade, mas usa o "equals" no lugar do "==" para evitar problemas caso esta característica seja alterada no futuro.
		if (!metadata.equals(record.getEntityMetadata())) {
			throw new IllegalArgumentException(String.format("The record metadata is not the same of this DAO. Record entity = %s, DAO entity = %s.", record.getEntityMetadata().getName(), metadata.getName()));
		}
	}
	
	protected final void checkNotInternal() {
		if (metadata.isInternal()) {
			throw new UnsupportedOperationException("For internal entity " + metadata.getName());
		}
	}

    protected final void checkNotDeleted(IEntityRecord record) {
        if (record.isDeleted()) {
            throw new IllegalStateException("Record is deleted:  " + record.getId());
        }
    }

    private MobileBeanEntityRecord create(MobileBean bean) {
        MobileBeanEntityRecord record;
        if (metadata.isInternal()) {
            record = new InternalMobileBeanEntityRecord(bean, metadata, entityManager);
        } else {
            record = new MobileBeanEntityRecord(bean, metadata, entityManager);
        }
        record.setNew();
        return record;
    }
	
	private boolean isReady(Set<String> currentEntities) {
		if (isReady) {
			return true;
		}
		
		//Entidades internas não possuem canal, então avalia apenas os canais relacioandos.
		if (!metadata.isInternal() && !MobileBean.isBooted(metadata.getChannel())) {
			return false;
		}
		
		//Se possui relacionamentos com outras entidades, elas devem estar prontas também.
		for (IEntityRelationship relationship : metadata.getRelationships()) {
			String targetEntityName = relationship.getTarget().getName();
			MobileBeanEntityDAO relationshipEntityDAO = entityManager.getEntityDAO(targetEntityName);

			//Verifica se a entidade alvo já não está sendo verificada (por uma chamada anterior) nesta execução.
			//Isto é necessário para evitar recursivadade infinita.
			if (currentEntities == null) {
				currentEntities = new HashSet<>();
			} else if (currentEntities.contains(targetEntityName)) {
				continue;
			}
			//Adiciona a entidade na lista de entidades corrents para evitar que ela seja verificada novamente nesta execução.
			currentEntities.add(metadata.getName());

			if (!relationshipEntityDAO.isReady(currentEntities)) {
				return false;
			}
		}

		return isReady = true;
	}
	
	private List<IEntityRecord> adaptBeans(MobileBean[] beans) {
		if (beans == null) {
			return Collections.emptyList();
		}
		
		List<IEntityRecord> ret = new ArrayList<>(beans.length);
		for (MobileBean bean : beans) {
			ret.add(new MobileBeanEntityRecord(bean, metadata, entityManager));
		}
		
		return ret;
	}


    private void internalSave(MobileBeanEntityRecord beanRecord, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
        //Salva os registros relacionados pela fonte antes pois eles devem possuir um id para serem salvos na fonte.
        EntityRelationship[] relationships = metadata.getRelationships();
        for (EntityRelationship relationship : relationships) {
            if (!relationship.isRelatedBySource()) {
                continue;
            }

            saveSourceRelatedRecords(beanRecord, relationship, sync, scheduleSyncTask);
        }

        //Só precisa salvar se for novo, se possuir campos sujos (as alterações do buffer de relacionamentos são consumidas
        //no "saveSourceRelatedRecords" e "saveTargetRelatedRecords") ou se for um registro local sendo enviado.
        if (beanRecord.isNew() || beanRecord.hasDirtyFields() ||  (beanRecord.isLocal() && sync)) {
            if (sync) {
                //O openmobster só sincroniza registros que são novos ou estão sujos, então se o registro é local e não está sujo, ele só vai ser enviado por causa do "setSynchronizing".
                //Se isto algum dia não for mais utilizado, terá que ser visto uma forma de fazer o OpenMobster sincronizar um registro que não está sujo.
                beanRecord.setSynchronizing();
                if (scheduleSyncTask) {
                    beanRecord.getBean().save();
                } else {
                    beanRecord.getBean().saveWithoutSync();
                }
            } else {
                beanRecord.setDesynchronized();
                beanRecord.getBean().saveLocal();
            }
            beanRecord.clearDirtyFields();
        }

        //Salva os registros relacionados pelo alvo depois pois o registro fonte já possui um id para ser salvo no alvo.
        for (EntityRelationship relationship : relationships) {
            if (relationship.isRelatedBySource()) {
                continue;
            }

            saveTargetRelatedRecords(beanRecord, relationship, sync, scheduleSyncTask);
        }
    }
	
	private void saveSourceRelatedRecords(MobileBeanEntityRecord sourceRecord, EntityRelationship relationship, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
		EntityMetadata relationshipEntity = relationship.getTarget();
		
		if (relationshipEntity.isInternal()) {
			serializeSourceRelatedInternalRecords(sourceRecord, relationship, sync, scheduleSyncTask);
		} else {
			saveSourceRelatedNormalRecords(sourceRecord, relationship, sync, scheduleSyncTask);
		}
	}

	private void saveSourceRelatedNormalRecords(MobileBeanEntityRecord sourceRecord, EntityRelationship relationship, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
		String compositionName = relationship.getName();
		boolean isComposition = MetadataUtils.isComposition(relationship);
		
		RelationshipBufferEntry bufferEntry = sourceRecord.consumeRelationshipsBufferEntry(compositionName);
		if (bufferEntry == null) {
			//Se não está no buffer, só é necessário salvar as alterações das composições.
			if (isComposition) {
				saveCompositionRecords(sourceRecord, relationship, sync, scheduleSyncTask);
			}
			return;
		}
		
		/*
		 * Se é uma composição que está no buffer, precisa salvar os novos registros e excluir os antigos.
		 */
		if (isComposition) {
			EntityMetadata relationshipEntity = relationship.getTarget();
			MobileBeanEntityDAO dao = entityManager.getEntityDAO(relationshipEntity.getName());
			
			//Obtém os registros antigos para poder remover os que não estão mais presentes.
			Set<String> oldRecordsIds = sourceRecord.getRelationshipRecordsIds(compositionName);
			
			//Salva os registros presentes no buffer.
			for (MobileBeanEntityRecord compositionRecord : bufferEntry.getRelationships()) {
				if (compositionRecord == null) {
					continue;
				}
				
				dao.internalSave(compositionRecord, sync, scheduleSyncTask);
                oldRecordsIds.remove(compositionRecord.getBeanId());
			}
			
			//Exclui os registros que não fazem mais parte da composição.
			for (String recordId : oldRecordsIds) {
				MobileBeanEntityRecord oldRecord = dao.get(recordId);
				internalDelete(oldRecord);
			}
		}
		
		//Seta a referência para os registros alvos no registro fonte.
		if (MetadataUtils.isSingleRelationship(relationship)) {
			sourceRecord.setRelationshipValue(relationship, bufferEntry.getRelationshipAsSingle(), false);
		} else {
			List<MobileBeanEntityRecord> records = bufferEntry.getRelationships();
			sourceRecord.setRelationshipArrayValue(relationship, records.toArray(new MobileBeanEntityRecord[records.size()]), false);
		}
	}
	
	@SuppressWarnings("SuspiciousToArrayCall")
    private void serializeSourceRelatedInternalRecords(MobileBeanEntityRecord sourceRecord, EntityRelationship relationship, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
		//Se não está no buffer, não há objetos internos para serializar.
		RelationshipBufferEntry bufferEntry = sourceRecord.consumeRelationshipsBufferEntry(relationship.getName());
		if (bufferEntry == null) {
			return;
		}
		
		//Salva todos os relacionamentos não internos do registro interno antes de serializar.
		List<MobileBeanEntityRecord> internalRecords = bufferEntry.getRelationships();
		for (MobileBeanEntityRecord internalRecord : internalRecords) {
			saveSourceRelatedInternalRecords(internalRecord, sync, scheduleSyncTask);
		}
		
		//Serializa os dados dos registros internos dentro do próprio MobileBean fonte.
		InternalMobileBeanEntityRecordSerializer serializer = new InternalMobileBeanEntityRecordSerializer(entityManager, sourceRecord);
		if (MetadataUtils.isSingleRelationship(relationship)) {
			serializer.serializeRelationshipValue(relationship, (InternalMobileBeanEntityRecord) bufferEntry.getRelationshipAsSingle());
		} else {
            serializer.serializeRelationshipArrayValue(relationship, internalRecords.toArray(new InternalMobileBeanEntityRecord[internalRecords.size()]));
		}
	}
	
	private void saveSourceRelatedInternalRecords(MobileBeanEntityRecord sourceRecord, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
		if (sourceRecord == null) {
			return;
		}
		
		EntityMetadata entityMetadata = sourceRecord.getEntityMetadata();
		for (EntityRelationship rel : entityMetadata.getRelationshipsMap().values()) {
			if (rel.getTarget().isInternal()) {
				//Associação para registro interno representa apenas uma referência para seu dono, então não precisam ser salvo.
				if (MetadataUtils.isComposition(rel)) {
					saveSourceRelatedInternalRecords(sourceRecord, rel, sync, scheduleSyncTask);
				}
				continue;
			}
			
			saveSourceRelatedNormalRecords(sourceRecord, rel, sync, scheduleSyncTask);
		}
	}

	private void saveSourceRelatedInternalRecords(MobileBeanEntityRecord sourceRecord, EntityRelationship relationship, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
		String relName = relationship.getName();
		if (MetadataUtils.isSingleRelationship(relationship)) {
			MobileBeanEntityRecord value = sourceRecord.getRelationshipValue(relName);
			saveSourceRelatedInternalRecords(value, sync, scheduleSyncTask);
		} else {
			MobileBeanEntityRecord[] values = sourceRecord.getRelationshipArrayValue(relName);
			if (values == null) {
				return;
			}
			
			for (MobileBeanEntityRecord value : values) {
				saveSourceRelatedInternalRecords(value, sync, scheduleSyncTask);
			}
		}
	}

	private void saveTargetRelatedRecords(MobileBeanEntityRecord sourceRecord, EntityRelationship relationship, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
		EntityMetadata relationshipEntity = relationship.getTarget();
		//Relacionamentos para entidades internas não podem ser referenciadas pelo alvo. 
		if (BuildConfig.DEBUG && relationshipEntity.isInternal()) {
			throw new AssertionError();
		}
		
		String relationshipName = relationship.getName();
		boolean isComposition = MetadataUtils.isComposition(relationship);
		RelationshipBufferEntry bufferEntry = sourceRecord.consumeRelationshipsBufferEntry(relationshipName);
		if (bufferEntry == null) {
			//Se não está no buffer, só é necessário salvar os registros se forem de uma composição.
			if (isComposition) {
				saveCompositionRecords(sourceRecord, relationship, sync, scheduleSyncTask);
			}
			return;
		}
		
		//Relacionamentos através do alvo sempre possuem o mappedBy.
		String mappedBy = relationship.getMappedBy();
		if (BuildConfig.DEBUG && TextUtils.isEmpty(mappedBy)) {
			throw new AssertionError();
		}
		
		/*
		 * Se está no buffer, significa que o relacionamento teve alterações, tendo que agir de acordo com o tipo de relacionamento:
		 * composições: salva os novos registros e exclui os antigos;
		 * associações: associa os novos registros alvo ao registro fonte através do "mappedBy" e desassocia os antigos.
		 */
		List<MobileBeanEntityRecord> targetRecords = bufferEntry.getRelationships();
		EntityRelationship mappedByRelationship = relationshipEntity.getRelationship(mappedBy);
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(relationshipEntity.getName());
		
		//Obtém os ids dos registros atuais para saber quais n devem ser excluídos.
		List<Serializable> existentIds = new ArrayList<>();
		for (MobileBeanEntityRecord targetRecord : targetRecords) {
			if (targetRecord == null || targetRecord.isNew()) {
				continue;
			}
			existentIds.add(targetRecord.getId());
		}
		Serializable[] ids = existentIds.toArray(new Serializable[existentIds.size()]);
		
		//Obtém os registros antigos para poder remover os que não estão mais presentes.
		List<MobileBeanEntityRecord> oldRecords = dao.
											 select().
											 where().
											 	rEq(sourceRecord.getId(), mappedBy).and().
												not().idIn(ids).
									     listResult();
		for (MobileBeanEntityRecord oldRecord : oldRecords) {
			if (isComposition) {
				//Exclui o registro que não faz mais parte da composição.
				internalDelete(oldRecord);
			} else {
				//Remove a referência do registro alvo para o fonte.
				oldRecord.setRelationshipValue(mappedByRelationship, null, false);
				dao.internalSave(oldRecord, sync, scheduleSyncTask);
			}
		}
		
		//Salva os registros presentes no buffer.
		for (MobileBeanEntityRecord targetRecord : targetRecords) {
			if (targetRecord == null) {
				continue;
			}
			
			if (!isComposition) {
				if (BuildConfig.DEBUG && targetRecord.isNew()) {
					throw new AssertionError("Only compositions should have new records, not associations.");
				}
				
				//Obtém uma versão limpa do registro alvo para apenas salvar a referência da fonte no alvo.
				dao.refresh(targetRecord);
			}
			
			String storedRecordId = targetRecord.getBeanValue(mappedBy);
			String sourceRecordId = sourceRecord.getBeanId();
			if (!TextUtils.equals(storedRecordId, sourceRecordId)) {
				//Seta a referência do registro fonte no registro alvo e salva-o.
				targetRecord.setRelationshipValue(mappedByRelationship, sourceRecord, false);
			}
			dao.internalSave(targetRecord, sync, scheduleSyncTask);
		}
	}
	
	private void saveCompositionRecords(MobileBeanEntityRecord record, IEntityRelationship composition, boolean sync, boolean scheduleSyncTask) throws CommitException, RelationshipViolationException {
		String compositionName = composition.getName();
		if (MetadataUtils.isSingleRelationship(composition)) {
            MobileBeanEntityRecord compositionRecord = record.getRelationshipValue(compositionName);
			saveCompositionRecords(composition, sync, scheduleSyncTask, compositionRecord);
		} else {
            MobileBeanEntityRecord[] compositionRecords = record.getRelationshipArrayValue(compositionName);
			saveCompositionRecords(composition, sync, scheduleSyncTask, compositionRecords);
		}
	}
	
	private void saveCompositionRecords(IEntityRelationship composition, boolean sync, boolean scheduleSyncTask, MobileBeanEntityRecord... records) throws CommitException, RelationshipViolationException {
		if (records == null) {
			return;
		}
		
		IEntityMetadata compositionEntity = composition.getTarget();
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(compositionEntity.getName());
		for (MobileBeanEntityRecord record : records) {
			if (record == null) {
				continue;
			}
			dao.internalSave(record, sync, scheduleSyncTask);
		}
	}


    private void internalDelete(MobileBeanEntityRecord record) throws RelationshipViolationException, CommitException {
        //Exclui os registros das composições salvas deste registro (os registros do buffer são ignorados).
        //Primeiro apenas os relacionados pelo alvo para que o registro n seja impedido de ser excluído por causa de uma composição apontando pra ele.
        deleteAllCompositionsRecords(record, false);

        if (!record.isNew()) {
            //Verifica se alguém está apontando para este registro.
            checkReferencesToRecord(record);

            //Exclui o registro do banco.
            record.getBean().delete();
        }

        //Por último, exclui as composições relacionadas pela fonte. Desta forma, nenhum deles será impedido de ser excluído pq o registro fonte não aponta mais para eles.
        deleteAllCompositionsRecords(record, true);
    }

    private void checkReferencesToRecord(MobileBeanEntityRecord record) throws RelationshipViolationException {
        EntityMetadata entityMetadata = record.getEntityMetadata();

        Set<String> violatedEntities = new HashSet<>();
        for (EntityRelationship referenceRel : entityMetadata.getReferencesToMe()) {
            //Se a entidade fonte do relacionamento possui referência para o registro, ela sera violada.
            if (hasReferenceToRecord(record, referenceRel)) {
                violatedEntities.add(referenceRel.getSource().getName());
            }
        }

        if (!violatedEntities.isEmpty()) {
            throw new RelationshipViolationException(record, violatedEntities.toArray(new String[violatedEntities.size()]));
        }
    }

    private boolean hasReferenceToRecord(MobileBeanEntityRecord record, EntityRelationship referenceRel) {
        if (!MetadataUtils.isSingleRelationship(referenceRel)) {
            throw new PendingFeatureException("Delete a record that is pointed by a multiple relationship hosted by the source, because of the lack of checking violation of relationship array");
        }

        EntityMetadata sourceEntity = referenceRel.getSource();
        if (sourceEntity.isInternal()) {
            throw new PendingFeatureException("Delete a record that is pointed by an internal entity, because of the lack of violation checking that needs a direct query to that type of entity, which is not supported for now.");
        }

        SQLiteQueryBuilder selectQuery = new MobileBeanQueryBuilder(entityManager).
                                            select(false).
                                            from(sourceEntity.getName()).
                                            where().
                                                rEq(record.getId(), referenceRel.getName()).
                                         toSQLiteQuery();

        SQLiteQueryBuilder existsQuery = new SQLiteQueryBuilder().
                                            select(false).
                                                exists().o().
                                                    append(selectQuery).
                                                    limit(1).
                                                c();

        Cursor cursor = MobileBean.rawQuery(existsQuery.toString(), existsQuery.getParameters());
        try {
            if (!cursor.moveToNext()) {
                return false;
            }
            return cursor.getInt(0) != 0;
        } finally {
            cursor.close();
        }
    }
	
	private void deleteAllCompositionsRecords(MobileBeanEntityRecord record, Boolean relatedBySource) throws RelationshipViolationException, CommitException {
		record.clearRelationshipsBuffer();
		
		EntityRelationship[] relationships = record.getEntityMetadata().getRelationships();
		for (EntityRelationship relationship : relationships) {
            if (relatedBySource != null && (relatedBySource ^ relationship.isRelatedBySource())) {
                continue;
            }


			EntityMetadata entity = relationship.getTarget();
			switch (relationship.getType()) { 
				case COMPOSITION:
					MobileBeanEntityRecord compositionRecord = record.getRelationshipValue(relationship.getName());
					if (entity.isInternal()) {
						deleteInternalCompositionRecords(compositionRecord);
					} else {
						deleteNormalCompositionRecords(compositionRecord);
					}
					break;
				case COMPOSITION_ARRAY:
					MobileBeanEntityRecord[] compositionRecords = record.getRelationshipArrayValue(relationship.getName());
					if (entity.isInternal()) {
						deleteInternalCompositionRecords(compositionRecords);
					} else {
						deleteNormalCompositionRecords(compositionRecords);
					}
					break;
					
				case ASSOCIATION:
				case ASSOCIATION_ARRAY:
					continue;
					
				default:
					throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationship.getType());
			}
		}
	}

	private void deleteNormalCompositionRecords(MobileBeanEntityRecord... records) throws RelationshipViolationException, CommitException {
		if (records == null) {
			return;
		}
		
		for (MobileBeanEntityRecord record : records) {
			if (record == null) {
				continue;
			}
			internalDelete(record);
		}
	}
	
	private void deleteInternalCompositionRecords(MobileBeanEntityRecord... records) throws RelationshipViolationException, CommitException {
		if (records == null) {
			return;
		}
		
		for (MobileBeanEntityRecord record : records) {
			if (record == null) {
				continue;
			}
			
			//Registros internos não precisam ser excluídos mas as composições deles sim.
			deleteAllCompositionsRecords(record, null);
		}
	}


    private MobileBeanEntityRecord copyFreshRecord(MobileBeanEntityRecord beanRecord, String ignoredMappedBy) {
        MobileBeanEntityRecord copy = create();
        for (EntityAttribute attr : metadata.getAttributesMap().values()) {
            String attrName = attr.getName();
            if (MetadataUtils.isSingleAttribute(attr)) {
                copy.setBeanValue(attrName, beanRecord.getBeanValue(attrName));
            } else {
                BeanList listValue = beanRecord.getBeanListValue(attrName);
                if (listValue != null) {
                    copy.setBeanListValue(listValue);
                }
            }
        }

        for (EntityRelationship rel : metadata.getRelationshipsMap().values()) {
            String relName = rel.getName();
            //Não copia a referência para o dono antigo.
            if (ignoredMappedBy != null && relName.equals(ignoredMappedBy)) {
                continue;
            }

            String mappedBy = rel.getMappedBy();
            boolean isComposition = MetadataUtils.isComposition(rel);
            MobileBeanEntityDAO relDAO = entityManager.getEntityDAO(rel.getTarget().getName());

            if (MetadataUtils.isSingleRelationship(rel)) {
                MobileBeanEntityRecord value = beanRecord.getRelationshipValue(relName);
                value = getFreshRelationshipRecord(value, isComposition, mappedBy, relDAO);
                copy.setRelationshipValue(relName, value);
            } else {
                MobileBeanEntityRecord[] values = beanRecord.getRelationshipArrayValue(relName);
                for (int i = 0; i < values.length; i++) {
                    values[i] = getFreshRelationshipRecord(values[i], isComposition, mappedBy, relDAO);
                }
                copy.setRelationshipArrayValue(relName, values);
            }
        }

        return copy;
    }

    private MobileBeanEntityRecord getFreshRelationshipRecord(MobileBeanEntityRecord value, boolean isComposition, String mappedBy, MobileBeanEntityDAO relDAO) {
        if (value == null) {
            return null;
        }

        if (isComposition) {
            //Passa o "mappedBy" para que se o registro dono for mapeado pelo alvo, ele não seja copiado junto.
            value = relDAO.copyFreshRecord(value, mappedBy);
        } else if (value.isDirty()) {
            value = relDAO.copy(value);
        }
        //Se for uma associação e n estiver sujo, pode usar diretamente o registro ao invés de copiá-lo.
        return value;
    }
	
	
	/*
	 * Classes auxiliares
	 */
	
	/**
	 * Define as responsabilidades que um estado salvo de registro deve atender.
	 */
	@SuppressLint("ParcelCreator")
	private interface IRecordSavedState extends Parcelable {
		
		MobileBeanEntityRecord getRecord(MobileBeanEntityDAO dao, MobileBeanEntityDataManager entityManager);
	}
	
	/**
	 * Estado salvo que mantém a referência para o {@link MobileBeanEntityRecord} enquanto for possível. Desta forma,
	 * o processo de salvamento e recuperação do estado de um registro fica praticamente com custo zero enquanto
	 * a aplicação ainda estiver na memória. A conversão do registro para um estado salvo em si só será feito quando 
	 * o {@link #writeToParcel(android.os.Parcel, int)} for chamado, ou seja, em momentos excepcionais.<br>
	 * É instanciado inicialmente utilizando um registro, mas depois que for parcelado e recuperado, será instanciado
	 * utilizando o estado salvo que será utilizado para recuperar o registro.
	 */
	private static final class RecordHolderSavedState implements IRecordSavedState {

		private MobileBeanEntityRecord record;
		private IRecordSavedState savedState;

		public RecordHolderSavedState(MobileBeanEntityRecord record) {
			assert record != null;
			this.record = record;
		}
		
		public RecordHolderSavedState(IRecordSavedState savedState) {
			assert savedState != null;
			this.savedState = savedState;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			//Gera o SavedState apenas quando necessário.
			if (savedState == null) {
				assert record != null;
				if (!record.isNew() && !record.isDirty()) {
					//Se não há alterações, apenas salva a referência para o registro.
					savedState = new RecordReferenceSavedState(record.getBeanId());
				} else {
					//Salva todas as alterações no registro e em suas composições.
                    savedState = new RecordBufferSavedState(record);
				}
				record = null;
			}
			
			dest.writeParcelable(savedState, flags);
		}
		
		@Override
		public MobileBeanEntityRecord getRecord(MobileBeanEntityDAO dao, MobileBeanEntityDataManager entityManager) {
			//Cria o registro apenas quando necessário.
			if (record == null) {
				assert savedState != null;
				record = savedState.getRecord(dao, entityManager);
				savedState = null;
			}
			
			return record;
		}
	
		public static final Creator<RecordHolderSavedState> CREATOR = new Creator<RecordHolderSavedState>() {
			@Override
			public RecordHolderSavedState createFromParcel(Parcel source) {
				IRecordSavedState savedState = AndroidBugsUtils.applyWorkaroundForParcelableDefaultClassloaderBug(source);
				return new RecordHolderSavedState(savedState);
			}
			
			@Override
			public RecordHolderSavedState[] newArray(int size) {
				return new RecordHolderSavedState[size];
			}
		};
	}
	
	/**
	 * Estado salvo de uma referência a um registro (através de seu id). Nenhuma alteração feita no registro ou em suas composições é mantida.
	 */
	private static class RecordReferenceSavedState implements IRecordSavedState {
		
		private final String recordId;

		public RecordReferenceSavedState(String recordId) {
			this.recordId = recordId;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(recordId);
		}
		
		@SuppressWarnings("unused")
		public static final Creator<RecordReferenceSavedState> CREATOR = new Creator<RecordReferenceSavedState>() {
			@Override
			public RecordReferenceSavedState createFromParcel(Parcel source) {
				return new RecordReferenceSavedState(source.readString());
			}
			
			@Override
			public RecordReferenceSavedState[] newArray(int size) {
				return new RecordReferenceSavedState[size];
			}
		};

		@Override
		public MobileBeanEntityRecord getRecord(MobileBeanEntityDAO dao, MobileBeanEntityDataManager entityManager) {
			if (recordId == null) {
				return null;
			}
			
			return dao.get(recordId);
		}
	}
	
	/**
	 * Estado salvo de um registro que mantém todas as suas alterações e de qualquer registro que faz parte de uma de suas composições.<br>
	 * As composições são salvas usando {@link br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDAO.RecordHolderSavedState}.
	 */
	private static final class RecordBufferSavedState implements IRecordSavedState {

		private final String recordId;
        private final boolean isNew;
        private final byte tags;
		private final StringValueEntry[] fields;
		private final RecordArrayValueEntry[] relationships;
		
		//Exclusivo de registros internos.
		private final InternalOwnerRecordInfo internalOwnerInfo;

		public RecordBufferSavedState(MobileBeanEntityRecord record) {
			//Salva todos os campos (atributos e relacionamentos) alterados.
			StringValueEntry[] fields = null;
			Set<String> dirtyFields = record.getDirtyFields();
			if (dirtyFields != null) {
				fields = new StringValueEntry[dirtyFields.size()];
				int i = 0;
				for (String dirtyField : dirtyFields) { 
					fields[i++] = new StringValueEntry(dirtyField, record.getBeanValue(dirtyField));
				}
			}
			
			//Salva as alterações (se houverem) de cada registro dos relacionamentos presentes no buffer.
			RecordArrayValueEntry[] relationships = null;
			Map<String, RelationshipBufferEntry> relationshipsBuffer = record.getRelationshipsBuffer();
			if (relationshipsBuffer != null) {
				relationships = new RecordArrayValueEntry[relationshipsBuffer.size()];
				int relIndex = 0;
				for (Entry<String, RelationshipBufferEntry> mapEntry : relationshipsBuffer.entrySet()) {
					String relationshipName = mapEntry.getKey();
					RelationshipBufferEntry bufferEntry = mapEntry.getValue();
					List<MobileBeanEntityRecord> relationshipRecords = bufferEntry.getRelationships();
					
					RecordHolderSavedState[] savedStates = new RecordHolderSavedState[relationshipRecords.size()];
					for (int i = 0; i < relationshipRecords.size(); i++) {
						MobileBeanEntityRecord relationshipRecord = relationshipRecords.get(i);
						savedStates[i] = new RecordHolderSavedState(relationshipRecord);
					}
					relationships[relIndex++] = new RecordArrayValueEntry(relationshipName, savedStates);
				}
			}

			this.recordId = record.getBeanId();
            this.isNew = record.isNew();
            this.tags = record.getTags();
			this.fields = fields;
			this.relationships = relationships;
			
			InternalOwnerRecordInfo internalOwnerInfo = null;
			if (record.getEntityMetadata().isInternal()) {
				InternalMobileBeanEntityRecord internal = (InternalMobileBeanEntityRecord) record;
				MobileBeanEntityRecord owner = internal.getOwner();
				if (owner != null) {
					internalOwnerInfo = new InternalOwnerRecordInfo(owner.getBeanId(), owner.getEntityMetadata().getName(), internal.getOwnerPath());
				}
			}
			this.internalOwnerInfo = internalOwnerInfo;
		}
		
		public RecordBufferSavedState(String recordId, boolean isNew, byte tags, StringValueEntry[] fields, RecordArrayValueEntry[] relationshipsBuffer, InternalOwnerRecordInfo internalOwnerInfo) {
			this.recordId = recordId;
            this.isNew = isNew;
            this.tags = tags;
            this.fields = fields;
			this.relationships = relationshipsBuffer;
			this.internalOwnerInfo = internalOwnerInfo;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(recordId);
            AndroidBugsUtils.applyWorkaroundForBug5973_write(dest, isNew);
			dest.writeByte(tags);
			dest.writeTypedArray(fields, flags);
			dest.writeTypedArray(relationships, flags);
			dest.writeParcelable(internalOwnerInfo, flags);
		}
	
		@SuppressWarnings("unused")
		public static final Creator<RecordBufferSavedState> CREATOR = new Creator<RecordBufferSavedState>() {
			@Override
			public RecordBufferSavedState createFromParcel(Parcel source) {
				return new RecordBufferSavedState(source.readString(),
                                                  AndroidBugsUtils.applyWorkaroundForBug5973_read(source),
                                                  source.readByte(),
												  source.createTypedArray(StringValueEntry.CREATOR),
												  source.createTypedArray(RecordArrayValueEntry.CREATOR),
												  (InternalOwnerRecordInfo) AndroidBugsUtils.applyWorkaroundForParcelableDefaultClassloaderBug(source));
			}
			
			@Override
			public RecordBufferSavedState[] newArray(int size) {
				return new RecordBufferSavedState[size];
			}
		};

		@Override
		public MobileBeanEntityRecord getRecord(MobileBeanEntityDAO dao, MobileBeanEntityDataManager entityManager) {
			//Obtém o registro da fonte de dados. Se o registo era novo, apenas cria um outro. Os dados alterados serão setados no registro novamente.
			MobileBeanEntityRecord record;
			if (isNew) {
                if (recordId == null) {
				    record = dao.create();
                } else {
				    record = dao.create(recordId);
                }
            } else {
				record = dao.get(recordId);
				//Se o registro deixou de existir durante o tempo que a aplicação estava destruída, simplesmente ignora-o.
				if (record == null) {
					return null;
				}
			}

			EntityMetadata entityMetadata = dao.getEntityMetadata();
			if (entityMetadata.isInternal() && internalOwnerInfo != null) {
				//Busca pelo registro dono. Se ele não existir mais, ignora o registro interno recuperado.
				MobileBeanEntityDAO ownerDAO = entityManager.getEntityDAO(internalOwnerInfo.getEntityName());
				MobileBeanEntityRecord owner = ownerDAO.get(internalOwnerInfo.getId());
				if (owner == null) {
					return null;
				}
				
				InternalMobileBeanEntityRecord internalRecord = (InternalMobileBeanEntityRecord) record;
				internalRecord.setOwner(owner);
				internalRecord.setOwnerPath(internalOwnerInfo.getPath());
			}

            //As tags fazem parte de um campo especial, por isto n é tratado junto com os demais.
            record.setTags(tags);
			
			if (fields != null) {
				for (StringValueEntry field : fields) {
					record.setBeanValue(field.getKey(), field.getValue());
				}
			}
			
			if (relationships != null) {
				Map<String, RelationshipBufferEntry> relationshipsBuffer = new HashMap<>();
				
				for (RecordArrayValueEntry relationshipEntry : relationships) {
					String relationshipName = relationshipEntry.getKey();
					RecordHolderSavedState[] savedStates = relationshipEntry.getValue();
				
					IEntityMetadata relationshipEntityMetadata = entityMetadata.getRelationship(relationshipName).getTarget();
					MobileBeanEntityDAO relationshipDAO = entityManager.getEntityDAO(relationshipEntityMetadata.getName());
					
					List<MobileBeanEntityRecord> relationshipRecords = new ArrayList<>(savedStates.length);
                    for (RecordHolderSavedState savedState : savedStates) {
                        relationshipRecords.add(savedState.getRecord(relationshipDAO, entityManager));
                    }
					
					RelationshipBufferEntry bufferEntry = new RelationshipBufferEntry(relationshipRecords);
					relationshipsBuffer.put(relationshipName, bufferEntry);
				}
				
				record.setRelationshipsBuffer(relationshipsBuffer);
			}
			
			return record;
		}
	}
	
	/**
	 * Armazena as informações específicas de um registro interno, podendo ser parcelado e recuperado posteriormente.
	 */
	private static final class InternalOwnerRecordInfo implements Parcelable { 
		private final String id;
		private final String entityName;
		private final String path;

		public InternalOwnerRecordInfo(String id, String entityName, String path) {
			this.id = id;
			this.entityName = entityName;
			this.path = path;
		}
		
		public String getId() {
			return id;
		}
		
		public String getEntityName() {
			return entityName;
		}
		
		public String getPath() {
			return path;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(id);
			dest.writeString(entityName);
			dest.writeString(path);
		}
		
		@SuppressWarnings("unused")
		public static final Creator<InternalOwnerRecordInfo> CREATOR = new Creator<InternalOwnerRecordInfo>() {
			@Override
			public InternalOwnerRecordInfo createFromParcel(Parcel source) {
				return new InternalOwnerRecordInfo(source.readString(), source.readString(), source.readString());
			}
			
			@Override
			public InternalOwnerRecordInfo[] newArray(int size) {
				return new InternalOwnerRecordInfo[size];
			}
		};
	}
	
	/**
	 * Entrada de valor string de um estado salvo de um registro.
	 */
	private static final class StringValueEntry implements Parcelable { 
		private final String key;
		private final String value;

		public StringValueEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		
		public String getValue() {
			return value;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(key);
			dest.writeString(value);
		}
		
		public static final Creator<StringValueEntry> CREATOR = new Creator<StringValueEntry>() {
			@Override
			public StringValueEntry createFromParcel(Parcel source) {
				return new StringValueEntry(source.readString(), source.readString());
			}
			
			@Override
			public StringValueEntry[] newArray(int size) {
				return new StringValueEntry[size];
			}
		};
	}
	
	/**
	 * Entrada de valor array de registros de um estado salvo de um registro.
	 */
	private static final class RecordArrayValueEntry implements Parcelable { 
		private final String key;
		private final RecordHolderSavedState[] value;

		public RecordArrayValueEntry(String key, RecordHolderSavedState[] value) {
			this.key = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}
		
		public RecordHolderSavedState[] getValue() {
			return value;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(key);
			dest.writeTypedArray(value, flags);
		}
		
		public static final Creator<RecordArrayValueEntry> CREATOR = new Creator<RecordArrayValueEntry>() {
			@Override
			public RecordArrayValueEntry createFromParcel(Parcel source) {
				return new RecordArrayValueEntry(source.readString(), source.createTypedArray(RecordHolderSavedState.CREATOR));
			}
			
			@Override
			public RecordArrayValueEntry[] newArray(int size) {
				return new RecordArrayValueEntry[size];
			}
		};
	}
}
