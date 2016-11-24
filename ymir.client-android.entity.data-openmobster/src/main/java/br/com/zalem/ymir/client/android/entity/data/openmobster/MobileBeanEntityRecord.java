package br.com.zalem.ymir.client.android.entity.data.openmobster;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.support.v4.app.AndroidBugsUtils;
import android.util.Log;

import org.openmobster.android.api.sync.BeanList;
import org.openmobster.android.api.sync.BeanListEntry;
import org.openmobster.android.api.sync.MobileBean;
import org.openmobster.core.mobileCloud.android.service.Registry;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.cache.IEntityRecordImageCache;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.TypeUtils;
import br.com.zalem.ymir.client.android.entity.data.query.select.ITerminalStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.NonUniqueResultException;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;

/**
 * Registro de entidade de dados baseada no {@link org.openmobster.android.api.sync.MobileBean} do OpenMobster.
 *
 * @author Thiago Gesser
 */
public class MobileBeanEntityRecord implements IEntityRecord {
	
	public static final String TAGS_PROPERTY_NAME = "*tags";
	public static final byte DESYNCHRONIZED_TAG = 1;
	public static final byte SYNCHRONIZING_TAG = 2;
	public static final byte LOCAL_TAG = 4;
	
	private final MobileBean bean;
	private final EntityMetadata metadata;
	private final MobileBeanEntityDataManager entityManager;
	
	private byte tags;
	//Buffer de registros. Armazena os registros definidos nos relacionamentos deste registro até que ele seja salvo.
	private Map<String, RelationshipBufferEntry> relationshipsBuffer;
	//Marcadores de alterações nos campos. Só são criados quando necessários, assim só haverá gasto de memória com os sets quando um registro for alterado.
	private Set<String> dirtyFields;
	
	MobileBeanEntityRecord(MobileBean bean, EntityMetadata metadata, MobileBeanEntityDataManager entityManager) {
		this.bean = bean;
		this.metadata = metadata;
		this.entityManager = entityManager;
		this.tags = -1;
    }
	
	@Override
	public final Serializable getId() {
		return getBeanId();
	}
	
	@Override
	public final EntityMetadata getEntityMetadata() {
		return metadata;
	}
	
	@Override
	public final SyncStatus getSyncStatus() {
		if (hasTag(DESYNCHRONIZED_TAG)) {
			return SyncStatus.DESYNCHRONIZED;
		}

		if (hasTag(SYNCHRONIZING_TAG)) {
			return SyncStatus.SYNCHRONIZING;
		}

		return SyncStatus.SYNCHRONIZED;
	}
	
	@Override
	public final boolean isLocal() {
		return hasTag(LOCAL_TAG);
	}
	
	@Override
	public final boolean isNew() {
		return bean.isNew();
	}
	
	@Override
	public final boolean isDeleted() {
		return bean.isDeleted();
	}
	
	@Override
	public final boolean isDirty() {
		return relationshipsBuffer != null || hasDirtyFields();
	}

	@Override
	public final boolean isDirty(String fieldName) {
		metadata.checkFieldName(fieldName);
		
		return (dirtyFields != null && dirtyFields.contains(fieldName)) ||
			   (relationshipsBuffer != null && relationshipsBuffer.containsKey(fieldName));
	}
	
	@Override
	public final boolean isNull(String fieldName) {
		metadata.checkFieldName(fieldName);
		
		return bean.isNull(fieldName);
	}
	
	/**
	 * Obtém o {@link org.openmobster.android.api.sync.MobileBean} do registro.
	 * 
	 * @return o MobileBean obtido.
	 */
	public final MobileBean getBean() {
		return bean;
	}

	/**
	 * Obtém o identificador diretamente do {@link org.openmobster.android.api.sync.MobileBean} do registro, retornando-o como <code>String</code>.
	 * 
	 * @return o identificador obtido.
	 */
	public final String getBeanId() {
		return bean.getId();
	}
	
	
	/*
	 * Métodos de obtanção de valores do registro
	 */

    @Override
    public Object getAttributeValue(String attributeName) {
        IEntityAttribute attribute = getAndCheckAttribute(attributeName, false);

        //Precisa de tratamento diferenciado por causa do cache de imagens.
        if (attribute.getType() == EntityAttributeType.IMAGE) {
            return getImageValue(attributeName);
        }

        return convertTo(bean.getValue(attributeName), attribute);
    }

    @Override
	public Integer getIntegerValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.INTEGER);
		
		return convertToInteger(bean.getValue(attribute), attribute);
	}

	@Override
	public Double getDecimalValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.DECIMAL);
		
		return convertToDouble(bean.getValue(attribute), attribute);
	}

	@Override
	public String getTextValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.TEXT);
		
		return bean.getValue(attribute);
	}

	@Override
	public Boolean getBooleanValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.BOOLEAN);
		
		return convertToBoolean(bean.getValue(attribute), attribute);
	}

	@Override
	public Date getDateValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.DATE);
		
		return convertToDate(bean.getValue(attribute), attribute);
	}

	@Override
	public Time getTimeValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.TIME);
		
		return convertToTime(bean.getValue(attribute), attribute);
	}

    @Override
    public Timestamp getDatetimeValue(String attribute) {
        checkAttribute(attribute, EntityAttributeType.DATETIME);

        return convertToDatetime(bean.getValue(attribute), attribute);
    }

    @Override
	public Character getCharacterValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.CHARACTER);
		
		return convertToCharacter(bean.getValue(attribute), attribute);
	}

	@Override
	public Bitmap getImageValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.IMAGE);
		
		IEntityRecordImageCache imageCache = entityManager.getImageCache();
		//Registros novos não usam o cache devido à falta de ID.
		boolean canUseCache = imageCache != null && !isNew();
		
		//Se a imagem estiver no cache, retorna-a. Se não, obtém ela do registro.
		if (canUseCache) {
			Bitmap cacheImage = imageCache.getImage(metadata.getChannel(), getBeanId(), attribute);
			if (cacheImage != null) {
				return cacheImage;
			}
		}
		
		//Obtém a imagem do registro e coloca-a no cache se possível.
		Bitmap image = convertToBitmap(bean.getValue(attribute), attribute);
		if (image != null && canUseCache) {
			imageCache.addImage(metadata.getChannel(), getBeanId(), attribute, image);
		}
		return image;
	}
	
	@Override
	public Bitmap getImageValue(String attribute, boolean fromCache) {
		checkAttribute(attribute, EntityAttributeType.IMAGE);
		
		IEntityRecordImageCache imageCache = entityManager.getImageCache();
		boolean canUseCache = imageCache != null && !isNew();
		if (fromCache) {
			if (!canUseCache) {
				return null;
			}
			
			return imageCache.getImage(metadata.getChannel(), getBeanId(), attribute);
		}
		
		//Obtém a imagem do registro e coloca-a no cache se possível.
		Bitmap image = convertToBitmap(bean.getValue(attribute), attribute);
		if (image != null && canUseCache) {
			imageCache.addImage(metadata.getChannel(), getBeanId(), attribute, image);
		}
		return image;
	}
	
	@Override
	public MobileBeanEntityRecord getRelationshipValue(String relationshipName) {
		EntityRelationship entityRelationship = getAndCheckRelationship(relationshipName, false);
		
		RelationshipBufferEntry bufferEntry = getRelationshipBufferEntry(relationshipName);
		if (bufferEntry != null) {
			return bufferEntry.getRelationshipAsSingle();
		}
		
		if (entityRelationship.isRelatedBySource()) {
			return getSourceRelatedRecord(entityRelationship);
		}
		return getTargetRelatedRecord(entityRelationship);
	}


    @Override
    public Object[] getAttributeArrayValue(String attributeName) {
        IEntityAttribute attribute = getAndCheckAttribute(attributeName, true);

        //Precisa de tratamento diferenciado por causa do cache de imagens.
        if (attribute.getType() == EntityAttributeType.IMAGE_ARRAY) {
            return getImageArrayValue(attributeName);
        }

        return getAttributeArray(attribute);
    }

    @Override
	public Integer[] getIntegerArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.INTEGER_ARRAY);
	}
	
	@Override
	public Double[] getDecimalArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.DECIMAL_ARRAY);
	}

	@Override
	public String[] getTextArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.TEXT_ARRAY);
	}

	@Override
	public Boolean[] getBooleanArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.BOOLEAN_ARRAY);
	}

	@Override
	public Date[] getDateArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.DATE_ARRAY);
	}

	@Override
	public Time[] getTimeArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.TIME_ARRAY);
	}

    @Override
    public Timestamp[] getDatetimeArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.DATETIME_ARRAY);
    }

	@Override
	public Character[] getCharacterArrayValue(String attribute) {
        return getAttributeArray(attribute, EntityAttributeType.CHARACTER_ARRAY);
	}

	@Override
	public Bitmap[] getImageArrayValue(String attribute) {
		checkAttribute(attribute, EntityAttributeType.IMAGE_ARRAY);
		
		IEntityRecordImageCache imageCache = entityManager.getImageCache();
		//Registros novos não usam o cache devido à falta de ID.
		boolean canUseCache = imageCache != null && !isNew();
		
		//Se estiver a imagem estiver no cache, retorna-a. Se não, obtém ela do registro.
		if (canUseCache) {
			Bitmap[] cacheImageArray = imageCache.getImageArray(metadata.getChannel(), getBeanId(), attribute);
			if (cacheImageArray != null) {
				return cacheImageArray;
			}
		}
		
		//Obtém o array de imagens do registro e coloca-o no cache se possível.
		Bitmap[] imageArray = getBitmapArray(attribute);
		if (imageArray != null && canUseCache) {
			imageCache.addImageArray(metadata.getChannel(), getBeanId(), attribute, imageArray);
		}
		return imageArray;
	}
	
	@Override
	public Bitmap[] getImageArrayValue(String attribute, boolean fromCache) {
		checkAttribute(attribute, EntityAttributeType.IMAGE_ARRAY);
		
		IEntityRecordImageCache imageCache = entityManager.getImageCache();
		boolean canUseCache = imageCache != null && !isNew();
		if (fromCache) {
			if (!canUseCache) {
				return null;
			}
			
			return imageCache.getImageArray(metadata.getChannel(), getBeanId(), attribute);
		}
		
		Bitmap[] imageArray = getBitmapArray(attribute);
		if (imageArray != null && canUseCache) {
			imageCache.addImageArray(metadata.getChannel(), getBeanId(), attribute, imageArray);
		}
		return imageArray;
	}
	
	@Override
	public MobileBeanEntityRecord[] getRelationshipArrayValue(String relationshipName) {
		EntityRelationship entityRelationship = getAndCheckRelationship(relationshipName, true);
		
		RelationshipBufferEntry bufferEntry = getRelationshipBufferEntry(relationshipName);
		if (bufferEntry != null) {
			List<MobileBeanEntityRecord> relationships = bufferEntry.getRelationships();
			if (relationships.isEmpty()) {
				return null;
			}
			return relationships.toArray(new MobileBeanEntityRecord[relationships.size()]);
		}
		
		if (entityRelationship.isRelatedBySource()) {
			return getSourceRelatedRecords(entityRelationship);
		}
		return getTargetRelatedRecords(entityRelationship);
	}
	
	/**
	 * Obtém um valor diretamente do {@link org.openmobster.android.api.sync.MobileBean} do registro.
	 * 
	 * @param field campo cujo o valor será obtido. 
	 * @return o valor obtido ou <code>null</code> se não havia valor definido.
	 */
	public String getBeanValue(String field) {
		return bean.getValue(field);
	}
	
	/**
	 * Obtém um valor de lista diretamente do {@link org.openmobster.android.api.sync.MobileBean} do registro.
	 * 
	 * @param field campo cujo o valor será obtido. 
	 * @return a lista obtida ou <code>null</code> se não havia valor definido.
	 */
	public BeanList getBeanListValue(String field) {
		BeanList beanList = bean.readList(field);
		//Atualmente a API do openmobster nunca retorna null neste método, mas sim uma lista vazia.
		if (beanList.size() == 0) {
			return null;
		}
		
		return beanList;
	}
	
	
	/*
	 * Métodos de definição de valores do registro
	 */

    @Override
    public void setAttributeValue(String attributeName, Object value) {
        IEntityAttribute attribute = getAndCheckAttribute(attributeName, false);
        checkAttributeValue(attribute, value);

        if (attribute.getType() == EntityAttributeType.IMAGE) {
            setImageValue(attributeName, (Bitmap) value);
            return;
        }

        setToStringValue(attributeName, value);
    }

    @Override
	public void setIntegerValue(String attribute, Integer value) {
		setToStringValue(attribute, EntityAttributeType.INTEGER, value);
	}
	
	@Override
	public void setDecimalValue(String attribute, Double value) {
		setToStringValue(attribute, EntityAttributeType.DECIMAL, value);
	}
	
	@Override
	public void setTextValue(String attribute, String value) {
        checkAttribute(attribute, EntityAttributeType.TEXT);
		setDirtyField(attribute);
		
		bean.setValue(attribute, value);
	}

	@Override
	public void setBooleanValue(String attribute, Boolean value) {
		setToStringValue(attribute, EntityAttributeType.BOOLEAN, value);
	}

	@Override
	public void setDateValue(String attribute, Date value) {
		setToStringValue(attribute, EntityAttributeType.DATE, value);
	}

	@Override
	public void setTimeValue(String attribute, Time value) {
		setToStringValue(attribute, EntityAttributeType.TIME, value);
	}

    @Override
    public void setDatetimeValue(String attribute, Timestamp value) {
		setToStringValue(attribute, EntityAttributeType.DATETIME, value);
    }

    @Override
	public void setCharacterValue(String attribute, Character value) {
		setToStringValue(attribute, EntityAttributeType.CHARACTER, value);
	}
	
	@Override
	public void setImageValue(String attribute, Bitmap value) {
        checkAttribute(attribute, EntityAttributeType.IMAGE);
		setDirtyField(attribute);
		
		byte[] imageBytes = convertToByteArray(value);
		bean.setBinaryValue(attribute, imageBytes);
	}

	@Override
	public void setRelationshipValue(String relationshipName, IEntityRecord value) {
		EntityRelationship entityRelationship = getAndCheckRelationship(relationshipName, false);
		checkRelationshipValue(entityRelationship, value);
		
		setRelationshipValue(entityRelationship, value, true);
	}


    @Override
    public void setAttributeArrayValue(String attributeName, Object[] value) {
        IEntityAttribute attribute = getAndCheckAttribute(attributeName, true);
        checkAttributeArrayValue(attribute, value);

        if (attribute.getType() == EntityAttributeType.IMAGE_ARRAY) {
            setImageArrayValue(attributeName, (Bitmap[]) value);
            return;
        }

        setToStringArrayValue(attributeName, value);
    }

    @Override
	public void setIntegerArrayValue(String attribute, Integer[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.INTEGER_ARRAY, value);
	}
	
	@Override
	public void setDecimalArrayValue(String attribute, Double[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.DECIMAL_ARRAY, value);
	}
	
	@Override
	public void setTextArrayValue(String attribute, String[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.TEXT_ARRAY, value);
	}

	@Override
	public void setBooleanArrayValue(String attribute, Boolean[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.BOOLEAN_ARRAY, value);
	}

	@Override
	public void setDateArrayValue(String attribute, Date[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.DATE_ARRAY, value);
	}

	@Override
	public void setTimeArrayValue(String attribute, Time[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.TIME_ARRAY, value);
	}

    @Override
    public void setDatetimeArrayValue(String attribute, Timestamp[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.DATETIME_ARRAY, value);
    }

    @Override
	public void setCharacterArrayValue(String attribute, Character[] value) {
		setToStringArrayValue(attribute, EntityAttributeType.CHARACTER_ARRAY, value);
	}

	@Override
	public void setImageArrayValue(String attribute, Bitmap[] values) {
        checkAttribute(attribute, EntityAttributeType.IMAGE_ARRAY);
        setDirtyField(attribute);

		BeanList beanList = new BeanList(attribute);
		//Se o array for nulo, é obrigado a setar um BeanList vazio ao invés de null.
		if (values != null) {
			for (Bitmap value : values) {
				if (value == null) {
					continue;
				}

				BeanListEntry entry = new BeanListEntry();
				byte[] imageBytes = convertToByteArray(value);
				entry.setBinaryProperty(attribute, imageBytes);
				beanList.addEntry(entry);
			}
		}
		setBeanListValue(beanList);
	}

	@Override
	public void setRelationshipArrayValue(String relationshipName, IEntityRecord[] values) {
		EntityRelationship entityRelationship = getAndCheckRelationship(relationshipName, true);
		checkRelationshipArrayValue(entityRelationship, values);

		setRelationshipArrayValue(entityRelationship, values, true);
	}

	@Override
	public void addRelationshipValue(String relationshipName, IEntityRecord value) {
		if (value == null) {
			throw new NullPointerException("value == null");
		}
		EntityRelationship entityRelationship = getAndCheckRelationship(relationshipName, true);
		checkRelationshipValue(entityRelationship, value);

		RelationshipBufferEntry bufferEntry = getMultipleRelationshipBufferEntry(entityRelationship);
		List<MobileBeanEntityRecord> relationships = bufferEntry.getRelationships();
		relationships.add((MobileBeanEntityRecord) value);
	}

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
	public void removeRelationshipValue(String relationshipName, IEntityRecord value) {
		if (value == null) {
			throw new NullPointerException("value == null");
		}
		EntityRelationship entityRelationship = getAndCheckRelationship(relationshipName, true);
		checkRelationshipValue(entityRelationship, value);

		RelationshipBufferEntry bufferEntry = getMultipleRelationshipBufferEntry(entityRelationship);
		List<MobileBeanEntityRecord> relationships = bufferEntry.getRelationships();
        if (!relationships.remove(value)) {
			throw new IllegalArgumentException(String.format("Entity record with id \"%s\" not found in the relationship array \"%s\" of the entity \"%s\".", value.getId(), relationshipName, metadata.getName()));
		}
	}

	/**
	 * Define um valor diretamente do {@link org.openmobster.android.api.sync.MobileBean} do registro.
	 *
	 * @param field campo cujo o valor será definido.
	 * @param value novo valor do campo.
	 */
	public void setBeanValue(String field, String value) {
		setDirtyField(field);

		bean.setValue(field, value);
	}

	/**
	 * Define um valor de lista diretamente do {@link org.openmobster.android.api.sync.MobileBean} do registro.
	 * 
	 * @param list nova lista do campo.
	 */
	public void setBeanListValue(BeanList list) {
		setDirtyField(list.getListProperty());
		
		bean.saveList(list);
	}


	/*
	 * Métodos protegidos que podem ser sobrescritos pelas subclasses
	 */
	
	/**
	 * Obtém o registro referente ao relacionamento singular, sendo que a referência está armazenada neste registro (fonte).
	 * 
	 * @param relationship relacionamento singular
	 * @return o registro obtido ou <code>null</code> se não havia registro referenciado.
	 */
	protected MobileBeanEntityRecord getSourceRelatedRecord(EntityRelationship relationship) {
		EntityMetadata entity = relationship.getTarget();
		if (entity.isInternal()) {
			InternalMobileBeanEntityRecordSerializer serializer = new InternalMobileBeanEntityRecordSerializer(entityManager, this);
			return serializer.deserializeRelationshipValue(relationship);
		}
		
		String relName = relationship.getName();
		String recordId = bean.getValue(relName);
		return findEntityRecord(recordId, entity.getName(), relName);
	}
	
	/**
	 * Obtém os registros referentes ao relacionamento múltiplo, sendo que a referência está armazenada neste registro (fonte).
	 * 
	 * @param relationship relacionamento singular
	 * @return os registros obtidos ou <code>null</code> se não haviam registros referenciados.
	 */
	protected MobileBeanEntityRecord[] getSourceRelatedRecords(EntityRelationship relationship) {
		EntityMetadata entity = relationship.getTarget();
		if (entity.isInternal()) {
			InternalMobileBeanEntityRecordSerializer serializer = new InternalMobileBeanEntityRecordSerializer(entityManager, this);
			return serializer.deserializeRelationshipArrayValue(relationship);
		}
		
		String relName = relationship.getName();
		BeanList beanList = getBeanListValue(relName);
		if (beanList == null) {
			return null;
		}
		int arraySize = beanList.size();
		MobileBeanEntityRecord[] resultArray = new MobileBeanEntityRecord[arraySize];
		for (int i = 0; i < arraySize; i++) {
			BeanListEntry beanEntry = beanList.getEntryAt(i);
			String recordId = beanEntry.getValue();
			resultArray[i] = findEntityRecord(recordId, entity.getName(), relName);
		}
		return resultArray;
	}
	
	/**
	 * Obtém o registro referente ao relacionamento singular, sendo que a referência está armazenada nele (alvo).
	 * 
	 * @param relationship relacionamento singular.
	 * @return o registro obtido ou <code>null</code> se não havia registro referenciado.
	 */
	protected MobileBeanEntityRecord getTargetRelatedRecord(EntityRelationship relationship) {
		EntityMetadata entity = relationship.getTarget();
		//Relacionamentos para entidades internas não podem ser referenciadas pelo alvo. 
		if (BuildConfig.DEBUG && entity.isInternal()) {
			throw new AssertionError();
		}
		
		//Se é novo, não haverá nenhum registro apontando para este, pois ele ainda n possui id.
		if (isNew()) {
			return null;
		}
		
		//Busca o registro que aponta para este.
		String entityName = entity.getName();
		try {
			return createTargetRelatedSearchQuery(entityName, relationship.getMappedBy()).uniqueResult();
		} catch (NonUniqueResultException e) {
			throw new IllegalStateException(String.format("The entity \"%s\" has more than one record pointing to the record with the id \"%s\". Source entity = \"%s\", relationship = \"%s\".", entityName, getBeanId(), metadata.getName(), relationship.getName()));
		}
	}
	
	/**
	 * Obtém os registros referentes ao relacionamento múltiplo, sendo que as referências estão armazenadas neles (alvos).
	 * 
	 * @param relationship relacionamento singular
	 * @return os registros obtidos ou <code>null</code> se não haviam registros referenciados.
	 */
	protected MobileBeanEntityRecord[] getTargetRelatedRecords(EntityRelationship relationship) {
		EntityMetadata entity = relationship.getTarget();
		//Relacionamentos para entidades internas não podem ser referenciadas pelo alvo. 
		if (BuildConfig.DEBUG && entity.isInternal()) {
			throw new AssertionError();
		}
		
		//Se é novo, não haverá nenhum registro apontando para este, pois ele ainda n possui id.
		if (isNew()) {
			return null;
		}
		
		//Busca os registros que aponta para este.
		String entityName = entity.getName();
		List<MobileBeanEntityRecord> records = createTargetRelatedSearchQuery(entityName, relationship.getMappedBy()).listResult();
		return records.toArray(new MobileBeanEntityRecord[records.size()]);
	}
	
	
	/*
	 * Métodos internos para uso do próprio acessor de dados
	 */
	
	void setNew() {
        byte tags = getTags();
        tags = clearTagBit(tags, SYNCHRONIZING_TAG);
        tags = setTagBit(tags, (byte) (DESYNCHRONIZED_TAG | LOCAL_TAG));
        setTags(tags);
	}
	
	void setDesynchronized() {
        setTag(DESYNCHRONIZED_TAG);
	}
	
	void setSynchronizing() {
		byte tags = getTags();
		tags = clearTagBit(tags, (byte) (DESYNCHRONIZED_TAG | LOCAL_TAG));
		tags = setTagBit(tags, SYNCHRONIZING_TAG);
		setTags(tags);
	}
	
	Set<String> getDirtyFields() {
		return dirtyFields;
	}
	
	void clearDirtyFields() {
		dirtyFields = null;
	}
	
	boolean hasDirtyFields() {
		return dirtyFields != null;
	}
	
	Set<String> getRelationshipRecordsIds(String relationshipName) {
		IEntityRelationship relationship = metadata.getRelationship(relationshipName);
		Set<String> ret = new HashSet<>();
		switch (relationship.getType()) {
			case ASSOCIATION:
			case COMPOSITION:
				String recordId = bean.getValue(relationshipName);
				if (recordId != null) {
					ret.add(recordId);
				}
				break;
				
			case ASSOCIATION_ARRAY:
			case COMPOSITION_ARRAY:
				BeanList beanList = getBeanListValue(relationshipName);
				if (beanList != null) {
					int arraySize = beanList.size();
					for (int i = 0; i < arraySize; i++) {
						BeanListEntry beanEntry = beanList.getEntryAt(i);
						recordId = beanEntry.getValue();
						ret.add(recordId);
					}
				}
				break;
				
			default:
				throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationship.getType());
		}
		
		return ret;
	}
	
	void setRelationshipValue(EntityRelationship entityRelationship, IEntityRecord value, boolean toBuffer) {
		String relationshipName = entityRelationship.getName();
		if (toBuffer) {
			//Neste caso, pode apenas tentar remover a entrada anterior no buffer para este relacionamento.
			if (entityRelationship.isRelatedBySource() && value == null && bean.isNull(relationshipName)) {
				consumeRelationshipsBufferEntry(relationshipName);
				return;
			}
			
			RelationshipBufferEntry bufferEntry = new RelationshipBufferEntry(value);
			
			//Substitui qualquer entrada anterior no buffer.
			internalGetRelationshipsBuffer().put(relationshipName, bufferEntry);
			return;
		}
		
		//Se não vai para o buffer, aplica alterações somente para relacionamentos referenciados pelo registro fonte (sem mappedBy).
		if (BuildConfig.DEBUG && !entityRelationship.isRelatedBySource()) {
			throw new AssertionError("Relationships related by the target should be defined directly in the target record.");
		}
		
		setDirtyField(relationshipName);
		String recordId = value != null ? getEntityRecordId(value) : null; 
		bean.setValue(relationshipName, recordId);
	}
	
	void setRelationshipArrayValue(EntityRelationship entityRelationship, IEntityRecord[] values, boolean toBuffer) {
		String relationshipName = entityRelationship.getName();
		if (toBuffer) {
			//Neste caso, pode apenas tentar remover a entrada anterior no buffer para este relacionamento.
			if (entityRelationship.isRelatedBySource() && (values == null || values.length == 0) && bean.isNull(relationshipName)) {
				consumeRelationshipsBufferEntry(relationshipName);
				return;
			}
			
			RelationshipBufferEntry bufferEntry = new RelationshipBufferEntry(values);
			
			//Substitui qualquer entrada anterior no buffer.
			internalGetRelationshipsBuffer().put(relationshipName, bufferEntry);
			return;
		}
		
		//Se não vai para o buffer, aplica alterações somente para relacionamentos referenciados pelo registro fonte (sem mappedBy).
		if (BuildConfig.DEBUG && !entityRelationship.isRelatedBySource()) {
			throw new AssertionError("Relationships related by the target should be defined directly in the target record.");
		}
		
		//Se o array for nulo, é obrigado a setar um BeanList vazio ao invés de null.
		BeanList beanList = new BeanList(relationshipName);
		if (values != null) {
			for (IEntityRecord value : values) {
				if (value == null) {
					continue;
				}
				
				String entityId = getEntityRecordId(value);
				BeanListEntry entry = new BeanListEntry();
				entry.setValue(entityId);
				beanList.addEntry(entry);
			}
		}
		setBeanListValue(beanList);
	}
	
	Map<String, RelationshipBufferEntry> getRelationshipsBuffer() {
		return relationshipsBuffer;
	}

	void setRelationshipsBuffer(Map<String, RelationshipBufferEntry> relationshipsBuffer) {
		this.relationshipsBuffer = relationshipsBuffer;
	}
	
	RelationshipBufferEntry consumeRelationshipsBufferEntry(String relationshipName) {
		if (relationshipsBuffer == null) {
			return null;
		}
		
		RelationshipBufferEntry entry = relationshipsBuffer.remove(relationshipName);
		if (relationshipsBuffer.isEmpty()) {
			relationshipsBuffer = null;
		}
		return entry;
	}
	
	void clearRelationshipsBuffer() {
		relationshipsBuffer = null;
	}
	

	/*
	 * Métodos auxiliares de verificação.
	 */

	private void checkAttribute(String name, EntityAttributeType expectedType) {
        checkAttribute(metadata.getAttribute(name), expectedType);
	}

    private void checkAttribute(IEntityAttribute attribute, EntityAttributeType expectedType) {
        if (attribute.getType() != expectedType) {
            throw newPropertyTypeException(attribute.getName(), expectedType.toString());
        }
    }

    private EntityRelationship getAndCheckRelationship(String name, boolean checkIsArray) {
		EntityRelationship relationship = metadata.getRelationship(name);
		boolean isSingle = MetadataUtils.isSingleRelationship(relationship);
		if (checkIsArray) {
			if (isSingle) {
				throw newPropertyTypeException(name, "Multiple relationship (array)");
			}
		} else if (!isSingle){
			throw newPropertyTypeException(name, "Single relationship");
		}
		
		return relationship;
	}

    private IEntityAttribute getAndCheckAttribute(String name, boolean checkIsArray) {
        IEntityAttribute attribute = metadata.getAttribute(name);
        boolean isSingle = MetadataUtils.isSingleAttribute(attribute);
        if (checkIsArray) {
            if (isSingle) {
                throw newPropertyTypeException(name, "Multiple attribute (array)");
            }
        } else if (!isSingle){
            throw newPropertyTypeException(name, "Single attribute");
        }

        return attribute;
    }

    private IllegalArgumentException newPropertyTypeException(String name, String expectedType) {
        return new IllegalArgumentException(String.format("The property \"%s\" of the entity \"%s\" is not of the type \"%s\". Record id: \"%s\".", name, metadata.getName(), expectedType, getId()));
    }

    private void checkRelationshipValue(IEntityRelationship relationship, IEntityRecord value) {
		if (value == null) {
			return;
		}
		
		//Existe uma instância de metadata para cada entidade, mas usa o "equals" no lugar do "==" para evitar problemas caso esta característica seja alterada no futuro.
		if (!value.getEntityMetadata().equals(relationship.getTarget())) {
			throw new IllegalArgumentException(String.format("The record entity \"%s\" is not applicable for the relationship \"%s\" that points to the entity \"%s\". Wrong record id = %s, Target record id = %s.", value.getEntityMetadata().getName(), relationship.getName(), relationship.getTarget().getName(), value.getId(), getId()));
		}
	}
	
	private void checkRelationshipArrayValue(IEntityRelationship relationship, IEntityRecord[] values) {
		if (values == null) {
			return;
		}
		
		for (IEntityRecord value : values) {
			checkRelationshipValue(relationship, value);
		}
	}

    private void checkAttributeValue(IEntityAttribute attribute, Object value) {
        Class<?> type = getAttributeValueType(attribute.getType());
        checkAttributeValue(attribute.getName(), type, value);
    }

    private void checkAttributeArrayValue(IEntityAttribute attribute, Object[] values) {
        if (values == null || values.length == 0) {
            return;
        }

        Class<?> type = getAttributeValueType(attribute.getType());
        for (Object value : values) {
            checkAttributeValue(attribute.getName(), type, value);
        }
    }

    private void checkAttributeValue(String attributeName, Class<?> type, Object value) {
        if (value == null) {
            return;
        }

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(String.format("Incorrect value type: \"%s\". The attribute \"%s\" of the entity \"%s\" requires values of type \"%s\". Record id: \"%s\", wrong value: \"%s\".", value.getClass().getSimpleName(), attributeName, metadata.getName(), type.getSimpleName(), getId(), value));
        }
    }

    private static Class<?> getAttributeValueType(EntityAttributeType type) {
        switch (type) {
            case INTEGER:
            case INTEGER_ARRAY:
                return Integer.class;
            case DECIMAL:
            case DECIMAL_ARRAY:
                return Double.class;
            case TEXT:
            case TEXT_ARRAY:
                return String.class;
            case CHARACTER:
            case CHARACTER_ARRAY:
                return Character.class;
            case BOOLEAN:
            case BOOLEAN_ARRAY:
                return Boolean.class;
            case DATE:
            case DATE_ARRAY:
                return Date.class;
            case TIME:
            case TIME_ARRAY:
                return Time.class;
            case DATETIME:
            case DATETIME_ARRAY:
                return Timestamp.class;
            case IMAGE:
            case IMAGE_ARRAY:
                return Bitmap.class;

            default:
                throw new IllegalArgumentException("Unsupported EntityAttributeType: " + type);
        }
    }


	/*
	 * Métodos auxiliares de definição de valor.
	 */

    private void setToStringValue(String attributeName, EntityAttributeType expectedType, Object value) {
        checkAttribute(attributeName, expectedType);

        setToStringValue(attributeName, value);
    }

	private void setToStringValue(String attributeName, Object value) {
		setDirtyField(attributeName);
		
		String strValue = null;
		if (value != null) {
			strValue = value.toString();
		}
		
		bean.setValue(attributeName, strValue);
	}
	
	private void setToStringArrayValue(String attributeName, EntityAttributeType expectedType, Object[] values) {
        checkAttribute(attributeName, expectedType);

        setToStringArrayValue(attributeName, values);
    }

	private void setToStringArrayValue(String attributeName, Object[] values) {
        setDirtyField(attributeName);

		BeanList beanList = new BeanList(attributeName);
		//Se o array for nulo, é obrigado a setar um BeanList vazio ao invés de null.
		if (values != null) {
			for (Object value : values) {
				if (value == null) {
					continue;
				}

				BeanListEntry entry = new BeanListEntry();
				entry.setValue(value.toString());
				beanList.addEntry(entry);
			}
		}
		setBeanListValue(beanList);
	}
	
	private void setDirtyField(String field) {
		if (dirtyFields == null) {
			dirtyFields = new HashSet<>();
		}
		dirtyFields.add(field);
	}
	

	/*
	 * Métodos auxiliares de conversão de valores.
	 */

    private Object convertTo(String strValue, IEntityAttribute attribute) {
        switch (attribute.getType()) {
            case TEXT:
            case TEXT_ARRAY:
                return strValue;
            case CHARACTER:
            case CHARACTER_ARRAY:
                return convertToCharacter(strValue, attribute.getName());
            case INTEGER:
            case INTEGER_ARRAY:
                return convertToInteger(strValue, attribute.getName());
            case DECIMAL:
            case DECIMAL_ARRAY:
                return convertToDouble(strValue, attribute.getName());
            case BOOLEAN:
            case BOOLEAN_ARRAY:
                return convertToBoolean(strValue, attribute.getName());
            case DATE:
            case DATE_ARRAY:
                return convertToDate(strValue, attribute.getName());
            case TIME:
            case TIME_ARRAY:
                return convertToTime(strValue, attribute.getName());
            case DATETIME:
            case DATETIME_ARRAY:
                return convertToDatetime(strValue, attribute.getName());
            case IMAGE:
            case IMAGE_ARRAY:
                return convertToBitmap(strValue, attribute.getName());

            default:
                throw new IllegalArgumentException("Unsupported EntityAttributeType: " + attribute.getType());
        }
    }

	private Integer convertToInteger(String strValue, String attribute) {
		try {
			return TypeUtils.convertToInteger(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(attribute, "Integer", strValue);
		}
	}
	
	private Double convertToDouble(String strValue, String attribute) {
		try {
			return TypeUtils.convertToDecimal(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(attribute, "Double", strValue);
		}
	}
	
	private Boolean convertToBoolean(String strValue, String attribute) {
		try {
			return TypeUtils.convertToBoolean(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(attribute, "Boolean", strValue);
		}
	}
	
	private Date convertToDate(String strValue, String attribute) {
		try {
			return TypeUtils.convertToDate(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(attribute, "Date", strValue);
		}
	}
	
	private Time convertToTime(String strValue, String attribute) {
		try {
			return TypeUtils.convertToTime(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(attribute, "Time", strValue);
		}
	}

    private Timestamp convertToDatetime(String strValue, String attribute) {
		try {
			return TypeUtils.convertToDatetime(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(attribute, "Datetime", strValue);
		}
	}

	private Character convertToCharacter(String strValue, String attribute) {
		try {
			return TypeUtils.convertToCharacter(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(attribute, "Character", strValue);
		}
	}

    private Bitmap convertToBitmap(String strValue, String attribute) {
        try {
            return TypeUtils.convertToImage(strValue);
        } catch (ParseException e) {
            throw newAttributeValueFormatException(attribute, "Image", strValue);
        }
    }

    private IllegalArgumentException newAttributeValueFormatException(String attribute, String type, String strValue) {
        return new IllegalArgumentException(String.format("The attribute \"%s\" of the entity \"%s\" do not have a value of type \"%s\". Record id: \"%s\", attribute value: \"%s\".", attribute, metadata.getName(), type, getId(), strValue));
    }

    private <T> T[] getAttributeArray(String attributeName, EntityAttributeType expectedType) {
        IEntityAttribute attribute = metadata.getAttribute(attributeName);
        if (expectedType != null) {
            checkAttribute(attribute, expectedType);
        }

        return getAttributeArray(attribute);
    }

    @SuppressWarnings("unchecked")
    private <T> T[] getAttributeArray(IEntityAttribute attribute) {
        BeanList beanList = getBeanListValue(attribute.getName());
        if (beanList == null) {
            return null;
        }

        int arraySize = beanList.size();
        T[] resultArray = (T[]) new Object[arraySize];
        for (int i = 0; i < arraySize; i++) {
            BeanListEntry beanEntry = beanList.getEntryAt(i);
            resultArray[i] = (T) convertTo(beanEntry.getValue(), attribute);
        }
        return resultArray;
    }
	
	private Bitmap[] getBitmapArray(String attribute) {
		BeanList beanList = getBeanListValue(attribute);
		if (beanList == null) {
			return null;
		}
		
		int arraySize = beanList.size();
		Bitmap[] resultArray = new Bitmap[arraySize];
		for (int i = 0; i < arraySize; i++) {
			BeanListEntry beanEntry = beanList.getEntryAt(i);
			resultArray[i]  = convertToBitmap(beanEntry.getValue(), attribute);
		}
		return resultArray;
	}

    private byte[] convertToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //Não há como suportar alpha pois a imagem é convertida para JPG. Entretanto, troca o fundo preto que o JPG coloca nos pixels
        //transparentes para um fundo branco, o qual é mais aderente à maioria das interfaces.
        boolean hasAlpha = bitmap.hasAlpha();
        if (hasAlpha) {
            Bitmap imageWithBG = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            imageWithBG.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(imageWithBG);
            canvas.drawBitmap(bitmap, 0f, 0f, null);
            bitmap = imageWithBG;
        }

        bitmap.compress(CompressFormat.JPEG, 75, out);

        if (hasAlpha) {
            //Recicla o bitmap criado especialmente para colocar o fundo branco na transparência. O bitmap que veio de parâmetro não é alterado.
            bitmap.recycle();
        }

        return out.toByteArray();
    }


	/*
	 * Métodos auxiliares de entidades.
	 */
	
	private MobileBeanEntityRecord findEntityRecord(Serializable recordId, String entity, String relationship) {
		if (recordId == null) {
			return null;
		}
		
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(entity);
		MobileBeanEntityRecord entityRecord = dao.get(recordId);
		if (entityRecord == null) {
            //Se estiver em produção, apenas ignora para não interromper a aplicação como um todo.
            String errorMsg = String.format("The entity \"%s\" has no record with the id \"%s\". Source entity = \"%s\", relationship = \"%s\".", entity, recordId, metadata.getName(), relationship);
            if (AndroidBugsUtils.applyWorkaroundForBug52962(Registry.getActiveInstance().getContext())) {
                throw new IllegalStateException(errorMsg);
            }
            Log.e(MobileBeanEntityRecord.class.getSimpleName(), errorMsg);
        }
		return entityRecord;
	}
	
	private ITerminalStatement createTargetRelatedSearchQuery(String entity, String mappedBy) {
		//Cria a query para a busca do(s) registro(s) que aponta(m) para este.
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(entity);
		String recordId = getEntityRecordId(this);
		return dao.select().
					where().
						rEq(recordId, mappedBy);
	}
	
	private String getEntityRecordId(IEntityRecord record) {
		Serializable entityId = record.getId();
		if (entityId == null) {
			throw new IllegalArgumentException("The id of the entity record can't be null.");
		}
		return entityId.toString();
	}
	

	/*
	 * Métodos auxiliares do buffer de relacionamentos.
	 */
	
	private Map<String, RelationshipBufferEntry> internalGetRelationshipsBuffer() {
		if (relationshipsBuffer == null) {
			relationshipsBuffer = new HashMap<>();
		}
		return relationshipsBuffer;
	}
	
	private RelationshipBufferEntry getMultipleRelationshipBufferEntry(EntityRelationship entityRelationship) {
		Map<String, RelationshipBufferEntry> relationshipsBuffer = internalGetRelationshipsBuffer();
		
		//Se for null, cria um buffer entry com os dados atuais do registro.
		String relName = entityRelationship.getName();
		RelationshipBufferEntry bufferEntry = relationshipsBuffer.get(relName);
		if (bufferEntry == null) {
			IEntityRecord[] records;
			if (entityRelationship.isRelatedBySource()) {
				records = getSourceRelatedRecords(entityRelationship);
			} else {
				records = getTargetRelatedRecords(entityRelationship);
			}
			bufferEntry = new RelationshipBufferEntry(records);
			relationshipsBuffer.put(relName, bufferEntry);
		}
		
		return bufferEntry;
	}
	
	private RelationshipBufferEntry getRelationshipBufferEntry(String relationshipName) {
		if (relationshipsBuffer == null) {
			return null;
		}
		
		return relationshipsBuffer.get(relationshipName);
	}
	
	
	/*
	 * Métodos auxiliares de tags.
	 */
	
	byte getTags() {
		if (tags == -1) {
			String tagsString = bean.getValue(TAGS_PROPERTY_NAME);
			if (tagsString == null) {
				tags = 0;
			} else {
				tags = Byte.parseByte(tagsString);
			}
		}
		
		return tags;
	}
	
	void setTags(byte tags) {
		bean.setValue(TAGS_PROPERTY_NAME, String.valueOf(tags));
		this.tags = tags;
	}
	
	private boolean hasTag(byte tagBitIndex) {
		byte tags = getTags();
		return (tags & tagBitIndex) != 0;
	}

	private void setTag(byte tagBitIndex) {
		byte tags = getTags();
        tags = setTagBit(tags, tagBitIndex);
        setTags(tags);
    }

	private byte setTagBit(byte tags, byte tagBitIndex) {
		return (byte) (tags | tagBitIndex);
	}
	
	private byte clearTagBit(byte tags, byte tagBitIndex) {
		return (byte) (tags & ~tagBitIndex);
	}
	
	
	/*
	 * Classes auxiliares.
	 */
	
	/**
	 * Entrada no buffer de relacionamentos. É utilizado tanto por relacionamentos singulares quanto por múltiplos.
	 */
	static final class RelationshipBufferEntry {
		
		private final List<MobileBeanEntityRecord> relationships;

		public RelationshipBufferEntry(IEntityRecord singleRelationshipRecord) {
			if (singleRelationshipRecord == null) {
				relationships = Collections.emptyList();
			} else {
				relationships = Collections.singletonList((MobileBeanEntityRecord) singleRelationshipRecord);
			}
		}
		
		public RelationshipBufferEntry(IEntityRecord[] records) {
			List<MobileBeanEntityRecord> relationships = new ArrayList<>();
			if (records != null) {
				for (IEntityRecord record : records) {
					relationships.add((MobileBeanEntityRecord) record);
				}
			}
			this.relationships = relationships;
		}
		
		public RelationshipBufferEntry(List<MobileBeanEntityRecord> relationships) {
			this.relationships = relationships;
		}
		
		public MobileBeanEntityRecord getRelationshipAsSingle() {
			if (relationships.isEmpty()) {
				return null;
			}
			
			if (BuildConfig.DEBUG && relationships.size() != 1) {
				throw new AssertionError();
			}
			
			return relationships.get(0);
		}
		
		public List<MobileBeanEntityRecord> getRelationships() {
			return relationships;
		}
	}
}