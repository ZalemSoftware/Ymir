package br.com.zalem.ymir.client.android.entity.data.openmobster.util;

import org.openmobster.android.api.sync.BeanList;
import org.openmobster.android.api.sync.BeanListEntry;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.data.openmobster.BuildConfig;
import br.com.zalem.ymir.client.android.entity.data.openmobster.InternalMobileBeanEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;

/**
 * Serializador/deserializador de registros internos baseados em {@link MobileBeanEntityRecord}.<br>
 * Os registros são manipulados através de um relacionamento para uma entidade interna (composição) que parte de um 
 * registro base. O registro base pode ser interno também, mas neste caso, o dono deste registro será utilizado como base.
 * O registro dono não pode ser interno.<br>
 * Os registros são armazenados/obtidos dos {@link org.openmobster.android.api.sync.MobileBean} utilizando o mecanismo de objetos internos.
 * Cada relacionamento interno é representado como uma propriedade que aponta para um objeto interno no MobileBean.<br>
 * <br>
 * O {@link EntityRelationship#getMappedBy() mappedBy} dos relacionamentos internos indica que os registros alvos
 * precisam de uma referência para seu registro mestre. Desta forma, quando registros internos pertencentes a um relacionamento
 * com tal configuração forem serializados/deserializados, receberão uma referência para seu mestre no relacionamento
 * indicado pelo <code>mappedBy</code>.
 * 
 * <h3>Formato de objetos internos do OpenMobster</h3>
 * Os objetos internos tem suas propriedades armazenadas como qualquer outra propriedade do MobileBean dono, diferenciando-se
 * apenas na URI que indica o caminho completo até ela. A URI é formado pela hierarquia de objetos internos (singulares ou arrays)
 * necessária para chegar à propriedade desejada. O caracter separador de hierarquia é o {@value #HIERARCHY_SEPARATOR}.
 * A forma como cada objeto é representado na URI depende de sua multiplicidade:<br>
 * <br>
 * <b>Objeto singular:</b> {@code /<nome da propriedade que aponta para o objeto>/<nome de uma propriedade do objeto interno>}<br>
 * Exemplo: /objInterno/propriedade<br>
 * <br>
 * <b>Objeto integrante de um array:</b> {@code /<nome da propriedade que aponta para o array>[<índice do objeto no array>]/<nome de uma propriedade do objeto interno>}<br>
 * Exemplo: /arrayObjInternos[1]/propriedade<br>
 * <br>
 * Objetos internos podem conter outros objetos internos, fazendo com que a URI indique o caminho que passa por todos estes objetos.<br>
 * Exemplo: /objInternoA/arrayObjInternosA[2]/objInternoB/arrayObjInternosB[0]/propriedade<br>
 * <br>
 * <h3>Exemplo de registros internos armazenados como objetos internos</h3>
 * Relacionamentos das entidades: cadastro (não interna) -> pessoas (interna) -> endereço (interna)<br>
 * <br>
 * Dados dos registros internos:<br>
 * <div>
 * <table border="1" style="float:left">
 *  <tr>
 *  	<th colspan="2">Pessoa</th>
 *  </tr>
 * 	<tr>
 * 		<td><b>Nome</b></td>
 * 		<td>Filipe</td>
 *	</tr>
 * 	<tr>
 * 		<td><b>Idade</b></td>
 * 		<td>24</td>
 *	</tr>
 * 	<tr>
 * 		<td><b>Gênero</b></td>
 * 		<td>Indefinido</td>
 *	</tr>
 * </table>
 * 	
 * <table border="1">
 *  <tr>
 *  	<th colspan="2">Endereço</th>
 *  </tr>
 * 	<tr>
 * 		<td><b>CEP</b></td>
 * 		<td>98754-123</td>
 *	</tr>
 * 	<tr>
 * 		<td><b>Nr</b></td>
 * 		<td>666</td>
 *	</tr>
 * </table>
 * </div>
 * <br><br><br>
 * Propriedades dos objetos internos armazenados:<br>
 * <table border="1">
 *  <tr>
 *  	<th>URI</th>
 *  	<th>Valor</th>
 *  </tr>
 * 	<tr>
 * 		<td>/pessoas[0]/nome</td>
 * 		<td>João</td>
 *	</tr>
 * 	<tr>
 * 		<td>/pessoas[0]/idade</td>
 * 		<td>24</td>
 *	</tr>
 * 	<tr>
 * 		<td>/pessoas[0]/genero</td>
 * 		<td>Indefinido</td>
 *	</tr>
 * 	<tr>
 * 		<td>/pessoas[0]/endereco/cep</td>
 * 		<td>98754-123</td>
 *	</tr>
 * 	<tr>
 * 		<td>/pessoas[0]/endereco/nr</td>
 * 		<td>666</td>
 *	</tr>
 * </table>
 * 
 * 
 * <br><br>
 *
 * @author Thiago Gesser
 */
public final class InternalMobileBeanEntityRecordSerializer {
	
	/**
	 * Separador de níveis de hierarquia entre os objetos internos.
	 */
	public static final String HIERARCHY_SEPARATOR = "/";
	
	/**
	 * Indicador de fim do índice de um objeto interno que faz parte de um array/lista de objetos internos.
	 */
	public static final String LIST_INDEX_START = "[";
	
	/**
	 * Indicador de início do índice de um objeto interno que faz parte de um array/lista de objetos internos.
	 */
	public static final String LIST_INDEX_END = "]";
	

	private final MobileBeanEntityDataManager entityManager;
	private final MobileBeanEntityRecord owner;
	private final String path;

	public InternalMobileBeanEntityRecordSerializer(MobileBeanEntityDataManager entityManager, MobileBeanEntityRecord baseRecord) {
		this.entityManager = entityManager;
		
		if (baseRecord.getEntityMetadata().isInternal()) {
			//Se for interno, aponta para seu owner.
			InternalMobileBeanEntityRecord inRecord = (InternalMobileBeanEntityRecord) baseRecord;
			this.owner = inRecord.getOwner();
			if (this.owner == null) {
				throw new IllegalArgumentException("The \"baseRecord\" is internal and has no owner.");
			}
			this.path = inRecord.getOwnerPath();
		} else {
			this.owner = baseRecord;
			this.path = HIERARCHY_SEPARATOR;
		}
	}
	
	/**
	 * Serializa o registro interno dentro do registro dono, de acordo com o nome do relacionamento.<br>
	 * O relacionamento deve ser do tipo {@link EntityRelationshipType#COMPOSITION}.
	 * 
	 * @param relationship relacionamento para a entidade interna.
	 * @param value registro interno.
	 */
	public void serializeRelationshipValue(EntityRelationship relationship, InternalMobileBeanEntityRecord value) {
		if (BuildConfig.DEBUG && (!relationship.getTarget().isInternal() || !isSameEntity(relationship, value) || relationship.getType() != EntityRelationshipType.COMPOSITION)) {
			throw new AssertionError();
		}
		
		setSingleRelationshipProperties(value, relationship, path, null, null);
	}
	
	/**
	 * Serializa o array de registros internos dentro do registro dono, de acordo com o nome do relacionamento.<br>
	 * O relacionamento deve ser do tipo {@link EntityRelationshipType#COMPOSITION_ARRAY}.
	 * 
	 * @param relationship relacionamento para a entidade interna.
	 * @param value array de registros internos.
	 */
	public void serializeRelationshipArrayValue(EntityRelationship relationship, InternalMobileBeanEntityRecord[] value) {
		if (BuildConfig.DEBUG && (!relationship.getTarget().isInternal() || !isSameEntity(relationship, value) || relationship.getType() != EntityRelationshipType.COMPOSITION_ARRAY)) {
			throw new AssertionError();
		}
		
		setMultipleRelationshipProperties(value, relationship, path);
	}
	
	/**
	 * Deserializa o registro interno de dentro do registro dono, de acordo com o nome do relacionamento.<br>
	 * O relacionamento deve ser do tipo {@link EntityRelationshipType#COMPOSITION}.
	 * 
	 * @param relationship relacionamento para a entidade interna.
	 * @return o registro interno deserializado ou <code>null</code> se não havia registro serializado.
	 */
	public InternalMobileBeanEntityRecord deserializeRelationshipValue(EntityRelationship relationship) {
		if (BuildConfig.DEBUG && (!relationship.getTarget().isInternal() || relationship.getType() != EntityRelationshipType.COMPOSITION)) {
			throw new AssertionError();
		}
	
		return getSingleRelationshipRecord(owner, relationship, path, null, null);
	}
	
	/**
	 * Deserializa o array de registros internos de dentro do registro dono, de acordo com o nome do relacionamento.<br>
	 * O relacionamento deve ser do tipo {@link EntityRelationshipType#COMPOSITION_ARRAY}.
	 * 
	 * @param relationship relacionamento para a entidade interna.
	 * @return o array de registros internos deserializado ou <code>null</code> se não haviam registros serializados.
	 */
	public InternalMobileBeanEntityRecord[] deserializeRelationshipArrayValue(EntityRelationship relationship) {
		if (BuildConfig.DEBUG && (!relationship.getTarget().isInternal() || relationship.getType() != EntityRelationshipType.COMPOSITION_ARRAY)) {
			throw new AssertionError();
		}

		return getMultipleRelationshipRecords(owner, relationship, path);
	}
	
	/**
	 * Deserializa um registro interno que faz parte de um array de dentro do registro dono, de acordo com o nome completo
	 * do relacionamento e o índice do registro.<br>
	 * 
	 * @param relFullName nome completo do relacionamento, partindo do registro dono.
	 * @param relEntityMetadata metadados da entidade alvo do relacionamento.
	 * @param entryIndex índice do registro no array.
	 * @return o registro interno deserializado ou <code>null</code> se não havia o registro serializado.
	 */
	public InternalMobileBeanEntityRecord deserializeRelationshipArrayValueEntry(String relFullName, EntityMetadata relEntityMetadata, int entryIndex) {
		if (BuildConfig.DEBUG && !relEntityMetadata.isInternal()) {
			throw new AssertionError();
		}
		
		BeanListEntry relEntry = owner.getBean().readListEntry(relFullName, entryIndex);
        return getRecord(relEntityMetadata, createListPrefix(relFullName, entryIndex), relEntry, HIERARCHY_SEPARATOR);
	}
	
	
	/*
	 * Métodos auxiliares que podem ser utilizados externamente
	 */
	
	/**
	 * Obtém o nome completo para uma propriedade.
	 *  
	 * @param prefix prefixo da propriedade
	 * @param propertyName o nome da propriedade
	 * @return o nome completo.
	 */
	public static String getPropertyFullname(String prefix, String propertyName) {
		return prefix + propertyName;
	}
	
	/**
	 * Cria um prefixo com a propriedade.
	 * 
	 * @param curPrefix prefixo atual.
	 * @param propertyName nome da propriedade.
	 * @return o prefixo criado.
	 */
	public static String createPropertyPrefix(String curPrefix, String propertyName) {
		return getPropertyFullname(curPrefix, propertyName) + HIERARCHY_SEPARATOR;
	}
	
	/**
	 * Chama o método {@link #createListPrefix(String, int)} passando o nome completo para o array/lista.
	 * 
	 * @param curPrefix prefixo do array/lista.
	 * @param listName nome do array/lista.
	 * @param index índice.
	 * @return o prefixo criado.
	 */
	public static String createListPrefix(String curPrefix, String listName, int index) {
		return createListPrefix(getPropertyFullname(curPrefix, listName), index);
	}
	
	/**
	 * Cria um prefixo para um índice específico do array/lista.
	 * 
	 * @param listFullname nome completo para o array/lista.
	 * @param index índice.
	 * @return o prefixo criado.
	 */
	public static String createListPrefix(String listFullname, int index) {
		return listFullname + LIST_INDEX_START + index + LIST_INDEX_END + HIERARCHY_SEPARATOR;
	}
	
	
	/*
	 * Métodos auxiliares de serialização
	 */
	
	private void setRelationshipProperties(InternalMobileBeanEntityRecord internal, String ownerPrefix, BeanListEntry entry, String entryPrefix, EntityRelationship relationship) {
		if (!MetadataUtils.isComposition(relationship)) {
			//Os relacionamentos para os donos não são serializados.
			return;
		}
		
		String relName = relationship.getName();
		if (MetadataUtils.isSingleRelationship(relationship)) {
			InternalMobileBeanEntityRecord value = (InternalMobileBeanEntityRecord) internal.getRelationshipValue(relName);
			setSingleRelationshipProperties(value, relationship, ownerPrefix, entry, entryPrefix);
		} else {
			MobileBeanEntityRecord[] values = internal.getRelationshipArrayValue(relName);
			setMultipleRelationshipProperties(values, relationship, ownerPrefix);
		}
	}

	private void setSingleRelationshipProperties(InternalMobileBeanEntityRecord value, EntityRelationship relationship, String ownerPrefix, BeanListEntry entry, String entryPrefix) {
		String relName = relationship.getName();
		if (value != null) {
			String entryRelPrefix = entry == null ? null : createPropertyPrefix(entryPrefix, relName);
			setRecordProperties(value, createPropertyPrefix(ownerPrefix, relName), entry, entryRelPrefix);
		} else {
			//Se os atributos do registro interno estão sendo mantidos em uma entrada de lista, não precisa apagar os atributos do owner.
			clearRecordProperties(relationship.getTarget(), createPropertyPrefix(ownerPrefix, relName), entry == null);
		}
	}
	
	private <T extends MobileBeanEntityRecord> void setMultipleRelationshipProperties(T[] values, EntityRelationship relationship, String ownerPrefix) {
		String relName = relationship.getName();
		BeanList ownerList = new BeanList(getPropertyFullname(ownerPrefix, relName));
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				InternalMobileBeanEntityRecord value = (InternalMobileBeanEntityRecord) values[i];
				BeanListEntry relEntry = new BeanListEntry();
				setRecordProperties(value, createListPrefix(ownerPrefix, relName, i), relEntry, HIERARCHY_SEPARATOR);
				ownerList.addEntry(relEntry);
			}
		}
		owner.setBeanListValue(ownerList);
	}
	
	private void setRecordProperties(InternalMobileBeanEntityRecord internalRecord, String ownerPrefix, BeanListEntry entry, String entryPrefix) {
		EntityMetadata internalEntity = internalRecord.getEntityMetadata();
		internalRecord.setOwner(owner);
		internalRecord.setOwnerPath(ownerPrefix);
		
		for (EntityAttribute attr : internalEntity.getAttributesMap().values()) { 
			String attrName = attr.getName();
			if (MetadataUtils.isSingleAttribute(attr)) {
				setPropertyValue(internalRecord, attrName, ownerPrefix, entry, entryPrefix);
			} else {
				setListValue(internalRecord, ownerPrefix, attrName);
			}
		}
		
		for (EntityRelationship rel : internalEntity.getRelationshipsMap().values()) {
			if (!rel.getTarget().isInternal()) {
				String relName = rel.getName();
				if (MetadataUtils.isSingleRelationship(rel)) {
					setPropertyValue(internalRecord, relName, ownerPrefix, entry, entryPrefix);
				} else {
					setListValue(internalRecord, ownerPrefix, relName);
				}
				continue;
			}
			
			setRelationshipProperties(internalRecord, ownerPrefix, entry, entryPrefix, rel);
		}
	}

	private void setPropertyValue(MobileBeanEntityRecord internal, String propertyName, String ownerPrefix, BeanListEntry entry, String entryPrefix) {
		String value = internal.getBeanValue(propertyName);
		
		//Se faz parte de uma lista, tem que definir o valor na entrada de lista, se não no registro dono. 
		if (entry != null) {
			if (value == null) {
				//Não há porque definir um valor nulo aqui pois todos os BeanListEntry são novos, ou seja, não há valor para limpar.
				return;
			}
			entry.setProperty(getPropertyFullname(entryPrefix, propertyName), value);
		} else {
			owner.setBeanValue(getPropertyFullname(ownerPrefix, propertyName), value);
		}
	}
	
	private void setListValue(MobileBeanEntityRecord internal, String ownerPrefix, String propertyName) {
		BeanList ownerList = new BeanList(getPropertyFullname(ownerPrefix, propertyName));
		BeanList internalList = internal.getBeanListValue(propertyName);
		if (internalList != null) {
			for (int i = 0; i < internalList.size(); i++) {
				ownerList.addEntry(internalList.getEntryAt(i));
			}
		}
		owner.setBeanListValue(ownerList);
	}
	
	private void clearRecordProperties(EntityMetadata recordEntity, String prefix, boolean clearAttributes) {
		if (clearAttributes) {
			for (EntityAttribute attr : recordEntity.getAttributesMap().values()) { 
				owner.setBeanValue(getPropertyFullname(prefix, attr.getName()), null);
			}
		}
		
		for (EntityRelationship rel : recordEntity.getRelationshipsMap().values()) {
			String relName = rel.getName();
			
			switch (rel.getType()) {
				case COMPOSITION:
					clearRecordProperties(rel.getTarget(), createPropertyPrefix(prefix, relName), clearAttributes);
					break;
				case COMPOSITION_ARRAY:
					BeanList emptyList = new BeanList(getPropertyFullname(prefix, relName));
					owner.setBeanListValue(emptyList);
					break;
					
				case ASSOCIATION:
				case ASSOCIATION_ARRAY:
					//Trata-se de relacionamentos para os donos (mappedBy) que ficam apenas em memória então não precisam ser limpos.
					break;
					
				default: 
					throw new IllegalArgumentException("Unsupported EntityRelationshipType " + rel.getType());
			}
		}
	}
	
	
	/*
	 * Métodos auxiliares de deserialização
	 */
	
	private InternalMobileBeanEntityRecord getSingleRelationshipRecord(MobileBeanEntityRecord relOwner, EntityRelationship relationship, String ownerPrefix, BeanListEntry entry, String entryPrefix) {
		String relName = relationship.getName();
		String entryRelPrefix = entry == null ? null : createPropertyPrefix(entryPrefix, relName);
		
		InternalMobileBeanEntityRecord record = getRecord(relationship.getTarget(), createPropertyPrefix(ownerPrefix, relName), entry, entryRelPrefix);
		trySetInternalRelationshipOwner(relOwner, relationship, record);
		return record;
	}
	
	private InternalMobileBeanEntityRecord[] getMultipleRelationshipRecords(MobileBeanEntityRecord relOwner, EntityRelationship relationship, String ownerPrefix) {
		String relName = relationship.getName();
		BeanList ownerList = owner.getBeanListValue(getPropertyFullname(ownerPrefix, relName));
		if (ownerList == null) {
			return null;
		}
		
		EntityMetadata entityMetadata = relationship.getTarget();
		InternalMobileBeanEntityRecord[] records = new InternalMobileBeanEntityRecord[ownerList.size()];
		for (int i = 0; i < records.length; i++) {
			BeanListEntry relEntry = ownerList.getEntryAt(i);
			records[i] = getRecord(entityMetadata, createListPrefix(ownerPrefix, relName, i), relEntry, HIERARCHY_SEPARATOR);
		}
		trySetInternalRelationshipOwner(relOwner, relationship, records);
		return records;
	}
	
	private InternalMobileBeanEntityRecord getRecord(EntityMetadata entityMetadata, String ownerPrefix, BeanListEntry entry, String entryPrefix) {
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(entityMetadata.getName());
		InternalMobileBeanEntityRecord internalRecord = (InternalMobileBeanEntityRecord) dao.create();
		
		for (EntityAttribute attr : entityMetadata.getAttributesMap().values()) { 
			String attrName = attr.getName();
			if (MetadataUtils.isSingleAttribute(attr)) {
				getPropertyValue(internalRecord, attrName, ownerPrefix, entry, entryPrefix);
			} else {
				getListValue(internalRecord, ownerPrefix, attrName);
			}
		}
		
		for (EntityRelationship rel : entityMetadata.getRelationshipsMap().values()) {
			String relName = rel.getName();
			if (!rel.getTarget().isInternal()) {
				if (MetadataUtils.isSingleRelationship(rel)) {
					getPropertyValue(internalRecord, relName, ownerPrefix, entry, entryPrefix);
				} else {
					getListValue(internalRecord, ownerPrefix, relName);
				}
				continue;
			}
			
			switch (rel.getType()) {
				case COMPOSITION:
					MobileBeanEntityRecord record = getSingleRelationshipRecord(internalRecord, rel, ownerPrefix, entry, entryPrefix);
					if (record != null) {
						internalRecord.setRelationshipValue(rel.getName(), record);
					}
					break;
				case COMPOSITION_ARRAY:
					MobileBeanEntityRecord[] records = getMultipleRelationshipRecords(internalRecord, rel, ownerPrefix);
					if (records != null) {
						internalRecord.setRelationshipArrayValue(rel.getName(), records);
					}
					break;
					
				case ASSOCIATION:
				case ASSOCIATION_ARRAY:
					//Trata-se de relacionamentos para os donos (mappedBy), que são setados no "trySetInternalRelationshipOwner".
					break;
					
				default: 
					throw new IllegalArgumentException("Unsupported EntityRelationshipType " + rel.getType());
			}
			
		}
		
		if (!internalRecord.isDirty()) {
			return null;
		}
		
		internalRecord.setOwner(owner);
		internalRecord.setOwnerPath(ownerPrefix);
		return internalRecord;
	}
	
	private void getPropertyValue(MobileBeanEntityRecord internal, String propertyName, String ownerPrefix, BeanListEntry entry, String entryPrefix) {
		//Se faz parte de uma lista, tem que obter o valorda entrada de lista, se não do registro dono.
		String value;
		if (entry != null) {
			value = entry.getProperty(getPropertyFullname(entryPrefix, propertyName));
		} else {
			value = owner.getBeanValue(getPropertyFullname(ownerPrefix, propertyName));
		}
		
		if (value == null) {
			return;
		}
		internal.setBeanValue(propertyName, value);
	}
	
	private void getListValue(MobileBeanEntityRecord internal, String ownerPrefix, String propertyName) {
		BeanList ownerList = owner.getBeanListValue(getPropertyFullname(ownerPrefix, propertyName));
		if (ownerList == null) {
			return;
		}
		
		BeanList internalList = new BeanList(propertyName);
		for (int i = 0; i < ownerList.size(); i++) {
			//Repassa a mesma instância de BeanListEntry pois ela não será modificada pelo MobileBeanEntityRecord. Sempre são criadas listas novas.
			ownerList.addEntry(ownerList.getEntryAt(i));
		}
		internal.setBeanListValue(internalList);
	}
	
	private void trySetInternalRelationshipOwner(MobileBeanEntityRecord relOwner, EntityRelationship relationship, MobileBeanEntityRecord... records) {
		if (records == null) {
			return;
		}
		String mappedBy = relationship.getMappedBy();
		if (mappedBy == null) {
			return;
		}
		
		for (MobileBeanEntityRecord record : records) {
			record.setRelationshipValue(mappedBy, relOwner);
		}
	}
	
	private boolean isSameEntity(EntityRelationship rel, InternalMobileBeanEntityRecord... records) {
		if (records != null) {
			EntityMetadata relEntity = rel.getTarget();
			for (InternalMobileBeanEntityRecord record : records) {
				//Existe uma instância de metadata para cada entidade, mas usa o "equals" no lugar do "==" para evitar problemas caso esta característica seja alterada no futuro.
				if (!relEntity.equals(record.getEntityMetadata())) {
					return false;
				}
			}
		}
		return true;
	}
}
