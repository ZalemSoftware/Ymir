package br.com.zalem.ymir.client.android.entity.data.openmobster.query;

import org.openmobster.android.api.sync.BeanList;
import org.openmobster.android.api.sync.BeanListEntry;
import org.openmobster.core.mobileCloud.android.storage.DefaultCRUD;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.openmobster.BuildConfig;
import br.com.zalem.ymir.client.android.entity.data.openmobster.InternalMobileBeanEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.MobileBeanSelectQuery.SelectField;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.CastType;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.LikeType;
import br.com.zalem.ymir.client.android.entity.data.query.IQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.data.query.select.ITerminalStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.NonUniqueResultException;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;

import static br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.fnLength;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.fnSubstr;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.getColFullname;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.globLiteral;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.globOneCharacterNotDigit;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.globZeroOrMoreCharacters;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.query.SQLiteQueryBuilder.opSubtract;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer.HIERARCHY_SEPARATOR;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer.LIST_INDEX_END;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer.LIST_INDEX_START;
import static br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer.getPropertyFullname;

/**
 * Construtor de queries baseado no OpenMobster.<br>
 * Utiliza o {@link SQLiteQueryBuilder} para montar as expressões SQL aptas a serem executadas no banco de dados do OpenMobster.<br>
 * Por enquanto, suporta apenas a montagem de queries de seleção de dados. A query deve ser montada de acordo as regras
 * do comando <code>SELECT</code> de SQL, então qualquer chamada que possa levar a formação de uma query inválida lançará
 * uma exceção. Depois de montada, a query pode ser gerada através do método {@link #toQuery()} ou executada diretamente
 * através de um dos métodos {@link #uniqueResult()}, {@link #listResult()} ou {@link #cursorResult()}.<br>
 * Nem todos os os tipos de campos e relacionamentos são suportados por este construtor. Os tipos suportados podem ser
 * verificados através dos métodos {@link #isSupportedAttribute(IEntityAttribute)} e {@link #isSupportedRelationship(EntityRelationship)}.<br>
 * <br>
 * O OpenMobster armazena os dados em tabelas com 3 colunas cada: id do registro, nome do campo e valor do campo. Desta forma,
 * os dados de um registro de entidade ficam distribuídos em várias linhas. Por exemplo, uma tabela de Pessoa:<br>
 * <br>
 * <table border="1">
 * 	<tr><th>id do registro</th><th>nome do campo</th><th>valor do campo</th></tr>
 * 	<tr><td>123</td><td>Nome</td><td>João da Silva</td></tr>
 * 	<tr><td>123</td><td>Idade</td><td>30</td></tr>
 * 	<tr><td>123</td><td>Endereço</td><td>Rua Alguma Coisa</td></tr>
 * 	<tr><td>456</td><td>Nome</td><td>Jonoaldison Pereira</td></tr>
 * 	<tr><td>456</td><td>Idade</td><td>55</td></tr>
 * 	<tr><td>456</td><td>Endereço</td><td>Rua Longe Pra Caraca</td></tr>
 * </table>
 * <br>
 * Para que os registros possam ser plenamente selecionados, filtrados e ordenados, os dados de cada um deles precisam
 * estar em uma linha do retorno da query. Para isto, o construtor adiciona um <code>JOIN</code> e uma condição 
 * no <code>WHERE</code> para cada campo adicional que está sendo utilizado, da seguinte forma:
 * <pre>
 * <code>SELECT '0'.value AS Nome, '1'.value AS Idade FROM Pessoa '0' JOIN Pessoa '1' ON '0'.recordid = '1'.recordid WHERE '0'.name = 'Nome' AND '1'.name = 'Idade'</code>
 * </pre>
 * Assim, a seleção é feita de forma que cada linha possua os dados de apenas um registro da entidade. Os dados de
 * relacionamentos (outras tabelas) também são selecionados utilizando o mesmo mecanismo.
 * 
 * @see SQLiteQueryBuilder
 * @see MobileBeanSelectQuery
 * 
 * @author Thiago Gesser
 */
public final class MobileBeanQueryBuilder implements IQueryBuilder {

	//Os nomes das colunas do OpenMobster não estão em nenhuma constante presente nele.
	private static final String RECORDID_COLUMN = "recordid";
	private static final String NAME_COLUMN = "name";
	private static final String VALUE_COLUMN = "value";
	//Expressão de filtro GLOB utilizada para entidades internas.
	private static final String INTERNAL_ENTITY_INDEX_GLOB_EXPR = globZeroOrMoreCharacters() + globOneCharacterNotDigit() + globZeroOrMoreCharacters();
	
	private final MobileBeanEntityDataManager entityManager;
	private final Map<String, String[]> fieldsByAlias;
	//Mantém os campos utilizados em uma árvore, sendo que os relacionamentos são os galhos e os atributos as folhas.
	private final Map<String, UsedField> rootFields;
	
	private boolean selectDistinct;
	private String[] curSelectedFieldPath;
	private boolean curSelectionIsRelationship;
	private List<SelectField> selectFields;
	
	private EntityMetadata sourceEntity;
	private MobileBeanSelectQuery subselect;
	private RelationshipArrayView dataView;
	private InternalRelationshipArrayView internalDataView;
	
	private SQLiteQueryBuilder orderBuilder;
	private SQLiteQueryBuilder cndBuilder;
    private Integer limit;
	private int openScopesCount;


    public MobileBeanQueryBuilder(MobileBeanEntityDataManager entityManager) {
		this.entityManager = entityManager;
		fieldsByAlias = new HashMap<>();
		rootFields = new HashMap<>();
	}
	
	@Override
	public MobileBeanQueryBuilder select(boolean distinct) {
		if (selectFields != null) {
			throw new IllegalStateException("\"select\" can be called only once per query.");
		}

		selectDistinct = distinct;
		selectFields = new ArrayList<>();
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder attribute(String... attributePath) {
		checkSelectState();
		checkSelectFieldPath(attributePath);
		
		addCurrentSelectField(null);
		curSelectedFieldPath = attributePath;
		curSelectionIsRelationship = false;
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder relationship(String... relationshipPath) {
		checkSelectState();
		checkSelectFieldPath(relationshipPath);
		
		addCurrentSelectField(null);
		curSelectedFieldPath = relationshipPath;
		curSelectionIsRelationship = true;
		return this;
	}

	@Override
	public MobileBeanQueryBuilder as(String alias) {
		if (curSelectedFieldPath == null) {
			throw new IllegalArgumentException("\"attribute\" or \"relationship\" must be called before.");
		}
		
		addCurrentSelectField(alias);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder from(String entityName) {
		checkFromState(false);
		addCurrentSelectField(null);
		
		EntityMetadata entityMetadata = entityManager.getEntityMetadata(entityName);
		checkIsNotInternalEntity(entityMetadata);
		configureSourceEntity(entityMetadata);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder from(ISelectQuery subselect) {
		checkFromState(false);
		if (subselect.getFields().length > 0) {
			throw new IllegalArgumentException("Only subselects for complete records (without specific fields) are supported.");
		}
		addCurrentSelectField(null);
		
		this.subselect = (MobileBeanSelectQuery) subselect;
		EntityMetadata entityMetadata = entityManager.getEntityMetadata(this.subselect.getEntityName());
		
		checkIsNotInternalEntity(entityMetadata);
		configureSourceEntity(entityMetadata);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder from(RelationshipArrayView dataView) {
		checkFromState(false);
		addCurrentSelectField(null);
		
		EntityRelationship relationship = (EntityRelationship) dataView.getRelationship();
		configureSourceEntity(relationship.getTarget());
		
		//Se for interno, armazena as informações necessárias para esta visão de dados específica.
		if (sourceEntity.isInternal()) {
			this.internalDataView = createInternalDataView(dataView);
			return this;
		}
		
		if (BuildConfig.DEBUG && dataView.getRecord().isNew()) {
			throw new AssertionError();
		}
		
		this.dataView = dataView;
		return this;
	}

	@Override
	public MobileBeanQueryBuilder where() {
		checkFromState(true);
		
		cndBuilder = new SQLiteQueryBuilder();
		return this;
	}

	@Override
	public MobileBeanQueryBuilder eq(Object value, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		IEntityAttribute attribute = getAttributeFromPath(attributePath);
		checkAttributeType(attribute);
		
		cndBuilder.eq(useField(attributePath), VALUE_COLUMN, toParameterValue(attribute.getType(), value));
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder lt(Object value, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		EntityAttributeType attributeType = getAttributeFromPath(attributePath).getType();
		
		cndBuilder.lt(useField(attributePath), VALUE_COLUMN, toParameterValue(attributeType, value), getCastType(attributeType));
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder gt(Object value, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		EntityAttributeType attributeType = getAttributeFromPath(attributePath).getType();
		
		cndBuilder.gt(useField(attributePath), VALUE_COLUMN, toParameterValue(attributeType, value), getCastType(attributeType));
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder le(Object value, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		EntityAttributeType attributeType = getAttributeFromPath(attributePath).getType();
		
		cndBuilder.le(useField(attributePath), VALUE_COLUMN, toParameterValue(attributeType, value), getCastType(attributeType));
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder ge(Object value, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		EntityAttributeType attributeType = getAttributeFromPath(attributePath).getType();
		
		cndBuilder.ge(useField(attributePath), VALUE_COLUMN, toParameterValue(attributeType, value), getCastType(attributeType));
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder in(Object[] values, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		EntityAttributeType attributeType = getAttributeFromPath(attributePath).getType();
		
		String[] valuesParams = new String[values.length];
		for (int i = 0; i < valuesParams.length; i++) { 
			valuesParams[i] = toParameterValue(attributeType, values[i]);
		}
		cndBuilder.in(useField(attributePath), VALUE_COLUMN, valuesParams, getCastType(attributeType));
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder between(Object value1, Object value2, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		EntityAttributeType attributeType = getAttributeFromPath(attributePath).getType();
		
		cndBuilder.between(useField(attributePath), VALUE_COLUMN, toParameterValue(attributeType, value1), toParameterValue(attributeType, value2), getCastType(attributeType));
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder contains(String text, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		checkAttributeType(getAttributeFromPath(attributePath));
		
		cndBuilder.like(useField(attributePath), VALUE_COLUMN, text, LikeType.CONTAINS);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder startsWith(String text, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		checkAttributeType(getAttributeFromPath(attributePath));
		
		cndBuilder.like(useField(attributePath), VALUE_COLUMN, text, LikeType.STARTS_WITH);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder endsWith(String text, String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		checkAttributeType(getAttributeFromPath(attributePath));

		cndBuilder.like(useField(attributePath), VALUE_COLUMN, text, LikeType.ENDS_WITH);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder isNull(String... attrPathOrAlias) {
		checkWhereState();
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		checkAttributeType(attributePath);
		
		cndBuilder.isNull(useField(attributePath), VALUE_COLUMN);
		return this;
	}
	
	
	@Override
	public MobileBeanQueryBuilder rEq(Serializable id, String... relPathOrAlias) {
		checkWhereState();
		String[] relationshipPath = resolveFieldPath(relPathOrAlias);
		checkRelationshipType(relationshipPath);
		
		cndBuilder.eq(useField(relationshipPath), VALUE_COLUMN, id.toString());
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder rIn(Serializable[] ids, String... relPathOrAlias) {
		checkWhereState();
		String[] relationshipPath = resolveFieldPath(relPathOrAlias);
		checkRelationshipType(relationshipPath);
		
		addIdInRestriction(ids, useField(relationshipPath), VALUE_COLUMN);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder rIsNull(String... relPathOrAlias) {
		checkWhereState();
		String[] relationshipPath = resolveFieldPath(relPathOrAlias);
		checkRelationshipType(relationshipPath);
		
		cndBuilder.isNull(useField(relationshipPath), VALUE_COLUMN);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder ssEq(SyncStatus ss) {
		checkWhereState();
		checkIsNotInternalEntity(sourceEntity);
		
		switch (ss) {
			case DESYNCHRONIZED:
				addTagsRestriction(MobileBeanEntityRecord.DESYNCHRONIZED_TAG, false, MobileBeanEntityRecord.DESYNCHRONIZED_TAG);
				break;
			case SYNCHRONIZING:
				addTagsRestriction(MobileBeanEntityRecord.SYNCHRONIZING_TAG, false, MobileBeanEntityRecord.SYNCHRONIZING_TAG);
				break;
			case SYNCHRONIZED:
				//Está sincronizado quando não possui as demais tags de sincronização.
				addTagsRestriction((byte) (MobileBeanEntityRecord.DESYNCHRONIZED_TAG | MobileBeanEntityRecord.SYNCHRONIZING_TAG), true, (byte) 0);
				break;
				
			default:
				throw new IllegalArgumentException("Unsupported SyncStatus: " + ss);
		}
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder ssIn(SyncStatus... sss) {
		checkWhereState();
		checkIsNotInternalEntity(sourceEntity);
		
		boolean orNull = false;
		byte[] results = new byte[sss.length];
		for (int i = 0; i < results.length; i++) {
			switch (sss[i]) {
				case DESYNCHRONIZED:
					results[i] = MobileBeanEntityRecord.DESYNCHRONIZED_TAG;
					break;
				case SYNCHRONIZING:
					results[i] = MobileBeanEntityRecord.SYNCHRONIZING_TAG;
					break;
				case SYNCHRONIZED:
					//Está sincronizado quando não possui as demais tags de sincronização.
					results[i] = 0;
					orNull = true;
					break;
					
				default:
					throw new IllegalArgumentException("Unsupported SyncStatus: " + sss[i]);
			}
		}
		
		addTagsRestriction((byte) (MobileBeanEntityRecord.DESYNCHRONIZED_TAG | MobileBeanEntityRecord.SYNCHRONIZING_TAG), orNull, results);
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder idEq(Serializable id) {
		checkWhereState();
		checkIsNotInternalEntity(sourceEntity);
		
		cndBuilder.eq(getDefaultOwnerAlias(), RECORDID_COLUMN, id.toString());
		return this;
	}
	
	@Override
	public MobileBeanQueryBuilder idIn(Serializable... ids) {
		checkWhereState();
		checkIsNotInternalEntity(sourceEntity);
		
		addIdInRestriction(ids, getDefaultOwnerAlias(), RECORDID_COLUMN);
		return this;
	}
	

	@Override
	public MobileBeanQueryBuilder not() {
		checkWhereState();
		
		cndBuilder.not();
		return this;
	}

	@Override
	public MobileBeanQueryBuilder and() {
		checkWhereState();
		
		cndBuilder.and();
		return this;
	}

	@Override
	public MobileBeanQueryBuilder or() {
		checkWhereState();

		cndBuilder.or();
		return this;
	}

	@Override
	public MobileBeanQueryBuilder o() {
		checkWhereState();
		
		openScopesCount++;
		cndBuilder.o();
		return this;
	}

	@Override
	public MobileBeanQueryBuilder c() {
		checkWhereState();

		if (--openScopesCount < 0) {
			throw new IllegalStateException("\"o\" must be called before.");
		}
		cndBuilder.c();
		return this;
	}

	@Override
	public MobileBeanQueryBuilder orderBy(boolean asc, String... attrPathOrAlias) {
		checkFromState(true);
		
		String[] attributePath = resolveFieldPath(attrPathOrAlias);
		IEntityAttribute attribute = getAttributeFromPath(attributePath);
		CastType castType = getOrderCastType(attribute.getType());

		if (orderBuilder == null) {
			orderBuilder = new SQLiteQueryBuilder();
			orderBuilder.orderBy();
		}
		orderBuilder.orderByColumn(useField(attributePath), VALUE_COLUMN, asc, castType);
		return this;
	}

    @Override
    public ITerminalStatement limit(int number) {
        checkFromState(true);
        if (number < 0) {
            throw new IllegalArgumentException("Limit number < 0");
        }

        limit = number;
        return this;
    }

    @Override
    public MobileBeanQueryBuilder condition() {
        if (cndBuilder == null) {
            where();
        } else {
            and();
        }
        return this;
    }


    @Override
	public ISelectQuery toQuery() {
        SQLiteQueryBuilder sqliteQuery = toSQLiteQuery();

		//Cria o objeto que representa a query, armazenando todos os dados necessários para que a query seja executada.
		String queryStr = sqliteQuery.toString();
		String[] parameters = sqliteQuery.getParameters();
		SelectField[] selectFieldsArray = selectFields.toArray(new SelectField[selectFields.size()]);
		String sourceEntityName = sourceEntity.getName();
		
		if (internalDataView != null) {
			MobileBeanEntityRecord owner = internalDataView.getOwner();
			return new InternalMobileBeanSelectQuery(queryStr, parameters, selectFieldsArray, sourceEntityName,
													 owner.getBeanId(), owner.getEntityMetadata().getName(), internalDataView.getRelFullname());
		}
		return new MobileBeanSelectQuery(queryStr, parameters, selectFieldsArray, sourceEntityName);
	}

	@Override
	public <T> T uniqueResult() throws NonUniqueResultException {
		ISelectQuery query = toQuery();
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(sourceEntity.getName());
		return dao.executeUniqueSelect(query);
	}

	@Override
	public <T> List<T> listResult() {
		ISelectQuery query = toQuery();
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(sourceEntity.getName());
		return dao.executeListSelect(query);
	}

	@Override
	public IEntityRecordCursor cursorResult() {
		ISelectQuery query = toQuery();
		MobileBeanEntityDAO dao = entityManager.getEntityDAO(sourceEntity.getName());
		return dao.executeCursorSelect(query);
	}

    /**
     * Cria a query para SQLite.
     *
     * @return a query criada.
     */
    public SQLiteQueryBuilder toSQLiteQuery() {
        checkSelectState();
        checkFromState(true);
        if (openScopesCount > 0) {
            throw new IllegalStateException("There are still open scopes. Call c() to close them.");
        }
        adjustInternalRecordsSelection();

        //Obtém a restrição do from antecipadamente pois ela pode afetar os campos utilizados.
        SQLiteQueryBuilder fromRestriction = getFromRestriction();

        //Obtém a lista com todos os campos utilizados na query (seleção, filtro, ordenação, etc.).
        List<UsedField> fields = getUsedFieldsList(rootFields);

		/*
		 * Select
		 */
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.select(selectDistinct);

        //Se não declarou campos de seleção, seleciona apenas os identificadores para posteriormente obter os IEntityRecord.
        if (selectFields.isEmpty()) {
            //Neste caso, o recordid precisa apontar para alguma das tabelas, se não vai dar erro de ambiguidade.
            //Ex: SELECT 'x'.recordid FROM Entidade 'x' ...
            String defaultAlias = getDefaultOwnerAlias();

            //Se for visão de dados de um relacionamento interno, seleciona os índices dos registros pois eles não possuem identificadores.
            if (internalDataView != null) {
				/*
				 * Os registros internos são representados como campos do registro dono, então é necessário obter o seu índice através
				 * do nome de um campo. Por exemplo: "/items[1]/descricao", onde "1" é o índice do registro e "descricao" é o nome do campo utilizado.
				 */
                //Utiliza o primeiro campo declarado na query (o adjustInternalRecordsSelection garante que sempre haverá um campo).
                UsedField field = fields.get(0);

                //Cria a expressão de seleção do índice e adiciona-a na query.
                String indexExpr = getInternalRecordIndexExpr(internalDataView.getRelFullname(), getFieldOwnerAlias(field), field.getName());
                qb.selectExpr(indexExpr, null);
            } else {
                //Seleciona apenas os identificadores.
                qb.selectColumn(defaultAlias, RECORDID_COLUMN, null);

                //Se não usou nenhum campo no final das contas (para filtro, ordenação, etc.), constrói uma query bem simplificada.
                if (fields.isEmpty()) {
                    qb.from(sourceEntity.getChannel(), defaultAlias);

                    qb.where();
                    if (fromRestriction != null) {
                        qb.append(fromRestriction).and();
                    }
                    //Cada registro real possui apenas uma linha com este campo especial.
                    qb.eq(null, NAME_COLUMN, "om:json");
                    appendConditions(qb);

                    appendOrderBy(qb);

                    if (limit != null) {
                        qb.limit(limit);
                    }
                    return qb;
                }
            }
        } else {
            //Seleciona os campos escolhidos utilizando seus nomes como aliases.
            //Ex: SELECT 'x'.value AS nomeDoCampo FROM Entidade 'x' ...
            for (int i = 0; i < selectFields.size(); i++) {
                SelectField selectField = selectFields.get(i);
                String[] fieldPath = selectField.getPath();

                UsedField usedField = getUsedField(fieldPath);
                qb.selectColumn(getFieldOwnerAlias(usedField), VALUE_COLUMN, selectField.getAlias());
            }
        }

		/*
		 * From / Joins
		 */
        //Faz com que os campos utilizados (em selects, where, order by, etc.) sejam buscados numa linha só através de JOINs na mesma tabela.
        //Ex: ... FROM Entidade 'x' JOIN OutraEntidade 'y' ON 'x'.recordid = 'y'.recordid WHERE 'x'.name = 'nomeDoCampo' AND 'y'.name = 'outroNome' ...
        SQLiteQueryBuilder fieldsCndBuilder = new SQLiteQueryBuilder();
        if (internalDataView != null) {
            appendInternalRelationshipFieldsSources(fields, qb, fieldsCndBuilder);
        } else {
            appendNormalFieldsSources(fields, qb, fieldsCndBuilder);
        }

		/*
		 * Where
		 */
        qb.where();
        if (fromRestriction != null) {
            qb.append(fromRestriction).and();
        }
        qb.append(fieldsCndBuilder);
        appendConditions(qb);

		/*
		 * Order by
		 */
        appendOrderBy(qb);

        if (limit != null) {
            qb.limit(limit);
        }
        return qb;
    }

	
	/**
	 * Verifica se o campo é suportado pelo construtor de queries.
	 * 
	 * @param attribute o atributo que será verificado.
	 * @return <code>true</code> se for suportado e <code>false</code> caso contrário.
	 */
	public static boolean isSupportedAttribute(IEntityAttribute attribute) {
		return getAttributeValueType(attribute.getType()) != null;
	}
	
	/**
	 * Verifica se o relacionamento é suportado pelo construtor de queries.
	 * 
	 * @param relationship o relacionamento que será verificado.
	 * @return <code>true</code> se for suportado e <code>false</code> caso contrário.
	 */
	public static boolean isSupportedRelationship(EntityRelationship relationship) {
		return MetadataUtils.isSingleRelationship(relationship) && !relationship.getTarget().isInternal();
	}
	

	/*
	 * Métodos auxiliares de construção da query.
	 */

	private void appendNormalFieldsSources(List<UsedField> fields, SQLiteQueryBuilder mainBuilder, SQLiteQueryBuilder fieldsCndBuilder) {
		for (int i = 0; i < fields.size(); i++) {
			UsedField field = fields.get(i);
			String fieldOwnerAlias = getFieldOwnerAlias(field);
			
			if (i == 0) {
				//O primeiro campo utiliza a tabela do próprio SELECT FROM, evitando um JOIN.
				mainBuilder.from(sourceEntity.getChannel(), fieldOwnerAlias);
			} else {
				//Usa o JOIN do campo anterior para fazer a ligação.
				String previousOwnerName = getFieldOwnerAlias(fields.get(i-1));
				mainBuilder.join(sourceEntity.getChannel(), fieldOwnerAlias, RECORDID_COLUMN, previousOwnerName, RECORDID_COLUMN);
				
				//Como não é o primeiro campo, tem que separar as condições com AND.
				fieldsCndBuilder.and();
			}
			
			//Monta a parte do WHERE para o campo.
			fieldsCndBuilder.eq(fieldOwnerAlias, NAME_COLUMN, field.getName());
			
			//Apenda os joins dos subFields, se houverem.
			appendSubFieldsSources(field, mainBuilder, fieldsCndBuilder);
		}
	}
	
	private void appendInternalRelationshipFieldsSources(List<UsedField> fields, SQLiteQueryBuilder mainBuilder, SQLiteQueryBuilder fieldsCndBuilder) {
		if (BuildConfig.DEBUG && internalDataView == null) {
			throw new AssertionError();
		}
		
		MobileBeanEntityRecord owner = internalDataView.getOwner();
		String ownerChannel = owner.getEntityMetadata().getChannel();
		String relFullname = internalDataView.getRelFullname();
		String relDBFullname = DefaultCRUD.convertToDatabaseFormat(relFullname);
		
		String previousIndexExpr = null;
		for (int i = 0; i < fields.size(); i++) {
			UsedField field = fields.get(i);
			String fieldName = field.getName();
			String fieldOwnerAlias = getFieldOwnerAlias(field);
			
			String internalIndexExpr = getInternalRecordDBIndexExpr(relDBFullname, fieldOwnerAlias, fieldName);
			if (i == 0) {
				//O primeiro campo utiliza a tabela do próprio SELECT FROM, evitando um JOIN.
				mainBuilder.from(ownerChannel, fieldOwnerAlias);
			} else {
				/*
				 * Faz o JOIN entre campos internos a partir do índice do registro. O índice de cada campo é obtido usando o substring em seu nome.
				 * Por exemplo, o primeiro registro do relacionamento "items" para os campos "nome" e "idade" são guardados na base da seguinte forma:
				 * 		"items[0].nome" e "items[0].idade".
				 * Neste caso, o ON precisa se basear no índice '0'. Para isto, o JOIN é construído da seguinte forma:
				 * 		... JOIN Entidade 'y' ON substr('x'.name, 7, length('x'.name) - 12) = substr('y'.name, 7, length('y'.name) - 13) ...
				 * 
				 * OBS: O formato como os campos são guardados no banco é diferente do formato normal, usado em memória pelo MobileBean.
				 * A diferença é que o do banco não possui "/" inicial e as demais "/" são substituídas por ".". Por exemplo:
				 * 		formato normal = "/items[0]/nome"
				 * 		formato banco = "items[0].nome"
				 * A conversão de formato deve ser feita utilizando o método "DefaultCRUD.convertToDatabaseFormat".
				 */
				mainBuilder.join(ownerChannel, fieldOwnerAlias, internalIndexExpr, previousIndexExpr);
				
				//Como não é o primeiro campo, tem que separar as condições com AND.
				fieldsCndBuilder.and();
			}
			previousIndexExpr = internalIndexExpr;
			
			/*
			 * Monta a parte do WHERE para o campo. Utiliza GLOB pq expressões regulares não são suportadas por padrão no SQLite. Por exemplo:
			 * 		'x'.name GLOB 'items[*].nome' AND substr('x'.name, 7, length('x'.name) - 12) NOT GLOB '*[^0-9]*'
			 * 
			 * O segundo GLOB é necessário para avaliar se o que á entre o [ e ] são apenas números, pois a condição GLOB
			 * não suporta uma condição de "zero ou mais de determinado caractere". Por exemplo, sem o segundo GLOB, os seguintes
			 * campos seriam confundidos:
			 * 		"items[0].nome" e "items[0].subitems[1].nome" 
			 */
			String globCondition = globLiteral(relFullname) + globLiteral(LIST_INDEX_START) + globZeroOrMoreCharacters() + globLiteral(LIST_INDEX_END) + globLiteral(HIERARCHY_SEPARATOR) + globLiteral(fieldName);
			globCondition = DefaultCRUD.convertToDatabaseFormat(globCondition);
			fieldsCndBuilder.glob(fieldOwnerAlias, NAME_COLUMN, globCondition).and().
							 not().glob0(internalIndexExpr, INTERNAL_ENTITY_INDEX_GLOB_EXPR);
			
			//Apenda os joins dos subFields, se houverem.
			appendSubFieldsSources(field, mainBuilder, fieldsCndBuilder);
		}
	}
	
	private static String getInternalRecordIndexExpr(String relFullname, String fieldOwnerAlias, String fieldName) {
		String relDBFullname = DefaultCRUD.convertToDatabaseFormat(relFullname);
		return getInternalRecordDBIndexExpr(relDBFullname, fieldOwnerAlias, fieldName);
	}
	
	private static String getInternalRecordDBIndexExpr(String relDBFullname, String fieldOwnerAlias, String fieldName) {
		//Ex: 8
		String startIndex = String.valueOf(relDBFullname.length() + 1 + LIST_INDEX_START.length()); //Soma pq índice começa em 1 e precisa passar pelo '['.
		
		//Ex: 'y'.name
		String nameCol = getColFullname(fieldOwnerAlias, NAME_COLUMN);
		//Ex: length('y'.name) - 14
		int subSize = relDBFullname.length() + fieldName.length() + LIST_INDEX_START.length() + LIST_INDEX_END.length() + HIERARCHY_SEPARATOR.length(); //Considera o '[' e ']/' na soma.
		String numberOfDigits = opSubtract(fnLength(nameCol), String.valueOf(subSize));
		//Ex: substr('y'.name, 8, length('y'.name) - 14)
		return fnSubstr(nameCol, startIndex, numberOfDigits);
	}
	
	private static void appendSubFieldsSources(UsedField field, SQLiteQueryBuilder mainBuilder, SQLiteQueryBuilder fieldsCndBuilder) {
		Map<String, UsedField> subFields = field.getSubFields();
		if (subFields == null) {
			return;
		}
		
		String fieldOwnerAlias = getFieldOwnerAlias(field);
		EntityMetadata fieldEntity = field.getEntity();
		List<UsedField> subFieldsList = getUsedFieldsList(subFields);
		
		//Faz o ON nos subFields a partir do "value" deste campo.
		//Ex: ... JOIN 'sub' ON 'super'.value = 'sub'.recordid ...
		for (UsedField subField : subFieldsList) {
			//Apenda o join do subField.
			String subFieldOwnerAlias = getFieldOwnerAlias(subField);
			mainBuilder.join(fieldEntity.getChannel(), subFieldOwnerAlias, RECORDID_COLUMN, fieldOwnerAlias, VALUE_COLUMN);
			
			//Apenda a parte do WHERE do subField.
			fieldsCndBuilder.and().eq(subFieldOwnerAlias, NAME_COLUMN, subField.getName());
			
			//Apenda os joins dos subFields do subField, se houverem.
			appendSubFieldsSources(subField, mainBuilder, fieldsCndBuilder);
		}
	}
	
	
	private void appendConditions(SQLiteQueryBuilder sb) {
		//Apenda as condições normais de maneira isolada das provenientes do JOIN e da visão de dados.
		if (cndBuilder != null) {
			sb.and().o().append(cndBuilder).c();
		}
	}
	
	private void appendOrderBy(SQLiteQueryBuilder sb) {
		if (orderBuilder == null) {
			return;
		}
		sb.append(orderBuilder);
	}
	
	private SQLiteQueryBuilder getFromRestriction() {
		//Se há uma visão de dados de um relacionamento do tipo array, obtém as restrições necessárias aqui para aplicá-las no WHERE. 
		SQLiteQueryBuilder relArrayRestriction = getRelationshipArrayViewRestriction();
		if (relArrayRestriction != null) {
			return relArrayRestriction;
		}
		
		//Se a origem dos dados é um subselect, obtém a restrição com o subselect aqui para aplicá-la no WHERE.
		return getSubselectRestriction();
	}
	
	private SQLiteQueryBuilder getRelationshipArrayViewRestriction() {
		//Se o FROM é de uma visão dos dados, aplica uma restrição para dispor apenas os registros presentes na visão.
		if (dataView != null) {
			if (BuildConfig.DEBUG && (internalDataView != null || subselect != null)) {
				throw new AssertionError();
			}
			
			MobileBeanEntityRecord record = (MobileBeanEntityRecord) dataView.getRecord();
			EntityRelationship relationship = (EntityRelationship) dataView.getRelationship();
			
			SQLiteQueryBuilder sb = new SQLiteQueryBuilder();
			if (relationship.isRelatedBySource()) {
				//Se é relacionado pelo registro fonte, restringe através dos ids presentes na fonte.
				BeanList beanList = record.getBean().readList(relationship.getName());
				
				String[] relationshipRecordIds = new String[beanList.size()];
				for (int i = 0; i < relationshipRecordIds.length; i++) {
					BeanListEntry beanEntry = beanList.getEntryAt(i);
					relationshipRecordIds[i] = beanEntry.getValue();
				}
				
				sb.in(getDefaultOwnerAlias(), RECORDID_COLUMN, relationshipRecordIds, null);
			} else {
				//Se é relacionado pelos registros alvos, restringe através do relacionamento do mappedBy, que deve apontar para a fonte.
				String fieldAlias = useField(relationship.getMappedBy());
				sb.eq(fieldAlias, VALUE_COLUMN, record.getBeanId());
			}
			return sb;
		}
		
		//Se a visão de dados for para uma entidade interna, os registros estarão dentro do registro base, então a query tem que ser restrita a ele apenas.		
		if (internalDataView != null) {
			if (BuildConfig.DEBUG && subselect != null) {
				throw new AssertionError();
			}
			
			MobileBeanEntityRecord owner = internalDataView.getOwner();
			SQLiteQueryBuilder sb = new SQLiteQueryBuilder();
			
			//É necessário restringir cada campo interno pois o JOIN é feito pelo índice do array (ao contrário dos
			//campos normais que o JOIN é feito pelo próprio ID do registro).
			String recordId = owner.getBeanId();
			for (Iterator<UsedField> iter = rootFields.values().iterator(); iter.hasNext();) {
				String fieldAlias = getFieldOwnerAlias(iter.next());
				sb.eq(fieldAlias, RECORDID_COLUMN, recordId);
				
				if (iter.hasNext()) {
					sb.and();
				}
			}
			return sb;
		}
		
		return null;
	}
	
	private SQLiteQueryBuilder getSubselectRestriction() {
		if (subselect == null) {
			return null;
		}
		
		if (BuildConfig.DEBUG && (dataView != null || internalDataView != null)) {
			throw new AssertionError();
		}
		
		return new SQLiteQueryBuilder().in(getDefaultOwnerAlias(), RECORDID_COLUMN, 
										   subselect.getQuery(), null, subselect.getParameters());
	}
	
	//Ajuste para a situação de seleção de registros internos inteiros onde não há nenhum campo sendo utilizado.
	//Isto é necessário pq o registro interno será obtido pelo índice contido no nome do campo, então é obrigado a selecionar no mínimo um campo.
	private void adjustInternalRecordsSelection() {
		if (internalDataView == null || !selectFields.isEmpty() || !rootFields.isEmpty()) {
			return;
		}
		
		//Se não há nenhum já utilizado na query, utiliza o primeiro campo declcarado na entidade.
		String firstField = getEntityFirstField(sourceEntity);
		addUsedField(firstField);
	}
	
	
	/*
	 * Métodos auxiliares das condições (where).
	 */
	
	private void addTagsRestriction(byte tagsMask, boolean orNull, byte... results) {
		if (BuildConfig.DEBUG && results.length == 0) {
			throw new AssertionError();
		}

		String tagOwnerAlias = useField(MobileBeanEntityRecord.TAGS_PROPERTY_NAME);
		
		//O campo tags com o valor nulo pode representar um resultado, então tem que ser considerado.
		if (orNull) {
			cndBuilder.o();
			cndBuilder.isNull(tagOwnerAlias, VALUE_COLUMN);
			cndBuilder.or();
		}
		
		//Se for apenas um resultado, usa o "=", se não usa o "IN".
		if (results.length == 1) {
			cndBuilder.bitwiseEq(tagOwnerAlias, VALUE_COLUMN, tagsMask, results[0]);
		} else {
			cndBuilder.bitwiseIn(tagOwnerAlias, VALUE_COLUMN, tagsMask, results);
		}
		
		//Fecha a verificação de nulo, se necessário.
		if (orNull) {
			cndBuilder.c();
		}
	}
	
	private void addIdInRestriction(Serializable[] ids, String tableAlias, String colName) {
		String[] valuesParams = new String[ids.length];
		for (int i = 0; i < valuesParams.length; i++) {
			valuesParams[i] = ids[i].toString();
		}
		cndBuilder.in(tableAlias, colName, valuesParams, null);
	}
	
	
	/*
	 * Métodos auxiliares de campos utilizados na query.
	 */
	
	private String useField(String... fieldPath) {
		UsedField usedField = addUsedField(fieldPath);
	
		//Retorna uma referência para o alias do dono do campo.
		return getFieldOwnerAlias(usedField);
	}
	
	private UsedField addUsedField(String... fieldPath) {
		if (BuildConfig.DEBUG && fieldPath.length == 0) {
			throw new AssertionError();
		}
		
		//Otimização para campos sem navegação, que representam a maioria dos casos.
		if (fieldPath.length == 1) {
			return tryAddUsedField(fieldPath[0], rootFields, null, null);
		}
		
		//Compactar o caminho e já obtém as entidades das navegações entre cada campo.
		ArrayList<EntityMetadata> navEntities = new ArrayList<>();
		fieldPath = compactFieldPath(fieldPath, navEntities);
		
		//Adiciona o campo na árvore de campos.
		UsedField curField = null;
		for (int i = 0; i < fieldPath.length; i++) {
			String fieldName = fieldPath[i];
			//Apenas as entidades dos campos que possuem navegação são utilizadas, por isto, a última pode ficar nula (até pq n se sabe se o último campo é um relacionamento ou atributo).
			EntityMetadata fieldEntity = i < fieldPath.length-1 ? navEntities.get(i) : null;
			
			//Adiciona o campo na hierarquia de campos usados (se ele ainda não estiver nela).
			if (curField == null) {
				curField = tryAddUsedField(fieldName, rootFields, null, fieldEntity);
			} else {
				Map<String, UsedField> subFields = curField.getSubFields();
				if (subFields == null) {
					subFields = new HashMap<>();
					curField.setSubFields(subFields);
				}
				curField = tryAddUsedField(fieldName, subFields, curField, fieldEntity);
			}
		}

		return curField;
	}
	
	//Compacta o caminho, concatenando as navegações para relacionamentos internos.
    @SuppressWarnings("ManualArrayToCollectionCopy")
	private String[] compactFieldPath(String[] fieldPath, List<EntityMetadata> navEntities) {
		ArrayList<String> compactedPath = null;
		EntityMetadata fieldEntity = sourceEntity;
		for (int fieldIndex = 0; fieldIndex < fieldPath.length; fieldIndex++) {
			String fieldName = fieldPath[fieldIndex];
			
			/*
			 * Navegações para entidades internas podem ser concatenadas num campo só, afinal, elas estão armazenadas no mesmo registro.
			 * 		Ex: A -> B -> C se transforma em "A/B/C"
			 */
			if (fieldIndex < fieldPath.length-1) {
				fieldEntity = fieldEntity.getRelationship(fieldName).getTarget();
			
				if (fieldEntity.isInternal()) {
					StringBuilder internalFieldBuilder = new StringBuilder(fieldName);
					
					for (int internalIndex = fieldIndex + 1; internalIndex < fieldPath.length; internalIndex = fieldIndex + 1) {
						//Será necessário otimizar o caminho.
						if (compactedPath == null) {
							compactedPath = new ArrayList<>();
							
							//Adiciona os campos anteriores.
                            for (int cpIndex = 0; cpIndex < fieldIndex; cpIndex++) {
								compactedPath.add(fieldPath[cpIndex]);
							}
						}
	
						String nextFieldName = fieldPath[internalIndex];
						internalFieldBuilder.append(HIERARCHY_SEPARATOR).append(nextFieldName);
						
						//Avança o indíce do "for" externo para pular os campos já concatenados.
						fieldIndex++;
						
						//Se a entidade que foi concatenada não é interna, para a concatenação por aqui. O último campo sempre é concatenado, independente da entidade.
						if (internalIndex < fieldPath.length-1) {
							fieldEntity = fieldEntity.getRelationship(nextFieldName).getTarget();
							if (!fieldEntity.isInternal()) {
								break;
							}
						}
					}
					
					//Converte o caminho pelos campos internos para o formato do banco e define-o.
					fieldName = DefaultCRUD.convertToDatabaseFormat(internalFieldBuilder.toString());
				}
				
				if (navEntities != null && fieldIndex < fieldPath.length-1) {
					navEntities.add(fieldEntity);
				}
			}
			
			if (compactedPath != null) {
				compactedPath.add(fieldName);
			}
		}
		
		//Só altera o caminho se for necessário.
		if (compactedPath == null) {
			return fieldPath;
		}
		
		return compactedPath.toArray(new String[compactedPath.size()]);
	}
	
	private UsedField getUsedField(String... fieldPath) {
		fieldPath = compactFieldPath(fieldPath, null);
		
		UsedField curField = null;
		for (int i = 0; i < fieldPath.length; i++) {
			String fieldName = fieldPath[i];
			if (i == 0) {
				curField = getUsedField(rootFields, fieldName);
			} else {
				curField = getUsedField(curField.getSubFields(), fieldName);
			}
		}
		return curField;
	}
	
	private static UsedField getUsedField(Map<String, UsedField> map, String fieldName) {
		if (map != null) {
			UsedField usedField = map.get(fieldName);
			if (usedField != null) {
				return usedField;
			}
		}
		
		throw new IllegalArgumentException("UsedField not found: " + fieldName);
	}
	
	private static UsedField tryAddUsedField(String fieldName, Map<String, UsedField> map, UsedField superField, EntityMetadata entity) {
		UsedField usedField = map.get(fieldName);
		if (usedField == null) {
			usedField = new UsedField(fieldName, map.size(), superField, entity);
			map.put(fieldName, usedField);
		}
		return usedField;
	}
	
	private static List<UsedField> getUsedFieldsList(Map<String, UsedField> map) {
		if (map.isEmpty()) {
			return Collections.emptyList();
		}
		
		//Ordena os campos pelos índices para que a query fique mais legível.
		List<UsedField> fields = new ArrayList<>(map.values());
		Collections.sort(fields);
		return fields;
	}
	
	private static String getFieldOwnerAlias(UsedField field) {
		return "'" + getFieldOwnerName(field) + "'";
	}
	
	private static String getFieldOwnerName(UsedField field) {
		UsedField superField = field.getSuperField();
		if (superField != null) {
			return getFieldOwnerName(superField) + "." + field.getIndex();
		}
		
		return String.valueOf(field.getIndex());
	}
	
	private static String getSelectFieldAlias(String[] fieldPath) {
		if (fieldPath.length == 1) {
			return fieldPath[0];
		}
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < fieldPath.length; i++) {
			if (i > 0) {
				builder.append("-");
			}
			builder.append(fieldPath[i]);
		}
		return builder.toString();
	}
	
	//Utilizado para evitar a ambiguidade de campos (por exemplo, o recordid, que é o mesmo em todos os JOIN).
	private String getDefaultOwnerAlias() {
		//O HashMap gera o Values apenas uma vez, então chamá-lo aqui apenas antecipará sua criação normal.
		Collection<UsedField> values = rootFields.values();
		if (values.isEmpty()) {
			return "'0'";
		}
		UsedField rootField = values.iterator().next();
		return getFieldOwnerAlias(rootField);
	}
	
	private void addCurrentSelectField(String alias) {
		if (curSelectedFieldPath == null) {
			return;
		}
		
		if (alias == null) {
			//Se não foi definido alias através do "as", usa o padrão.
			alias = getSelectFieldAlias(curSelectedFieldPath);
		}
		if (fieldsByAlias.put(alias, curSelectedFieldPath) != null) {
			throw new IllegalArgumentException(String.format("There is already a field with the alias \"%s\". Field path = %s.", alias, Arrays.toString(curSelectedFieldPath)));
		}
		
		SelectField selectField = new SelectField(curSelectedFieldPath, alias, curSelectionIsRelationship);
		selectFields.add(selectField);
		
		curSelectedFieldPath = null;
	}
	
	private void configureSourceEntity(EntityMetadata sourceEntity) {
		if (BuildConfig.DEBUG && this.sourceEntity != null) {
			throw new AssertionError();
		}
		
		this.sourceEntity = sourceEntity;
		configureSelectFields();
	}
	
	private void configureSelectFields() {
		for (SelectField field : selectFields) {
			//Verifica o tipo do SelectField aqui pq no momento em que ele foi inserido ainda n havia o sourceEntity. 
			String[] fieldPath = field.getPath();
			if (field.isRelationship()) {
				checkRelationshipType(fieldPath);
			} else {
				checkAttributeType(fieldPath);
			}
			
			//Adiciona à lista de campos utilizados.
			addUsedField(fieldPath);
		}
	}
	
	private String[] resolveFieldPath(String... fieldPathOrAlias) {
		//Se é referente a um alias, obtém o path do campo dele.
		if (fieldPathOrAlias.length == 1) {
			String[] path = fieldsByAlias.get(fieldPathOrAlias[0]);
			if (path != null) {
				return path;
			}
		}
		return fieldPathOrAlias;
	}
	
	
	/*
	 * Métodos auxiliares de campos da entidade e seus tipos/valores.
	 */
	
	private IEntityAttribute getAttributeFromPath(String... attributePath) {
		checkFromState(true);
		
		return MetadataUtils.getAttributeFromPath(sourceEntity, attributePath);
	}
	
	private EntityRelationship getRelationshipFromPath(String... relationshipPath) {
		checkFromState(true);
		
		return (EntityRelationship) MetadataUtils.getRelationshipFromPath(sourceEntity, relationshipPath);
	}
	
	private static Class<?> getAttributeValueType(EntityAttributeType type) {
		switch (type) {
			case INTEGER:
			case DECIMAL:
				return Number.class;
			case TEXT:
				return String.class;
			case CHARACTER:
				return Character.class;
			case BOOLEAN:
				return Boolean.class;
			case DATE:
				return Date.class;
			case TIME:
				return Time.class;
			case DATETIME:
				return Timestamp.class;

			case IMAGE:
			case BOOLEAN_ARRAY:
			case INTEGER_ARRAY:
			case DECIMAL_ARRAY:
			case TEXT_ARRAY:
			case CHARACTER_ARRAY:
			case DATE_ARRAY:
			case TIME_ARRAY:
			case DATETIME_ARRAY:
			case IMAGE_ARRAY:
				//Nem todos os tipos de atributos suportam serem utilizados nas queries por enquanto.
				return null;
				
			default: 
				throw new IllegalArgumentException("Unsupported EntityAttributeType: " + type);
		}
	}

	private static String toParameterValue(EntityAttributeType type, Object value) {
		Class<?> valueClass = getAttributeValueType(type);
		if (valueClass == null) {
			throw new IllegalArgumentException("Invalid attribute type: " + type);
		}
		checkValueType(value, valueClass);
		
		//Os tipos de valores suportados podem ser representados com um simples "toString()".
		return value.toString();
	}
	
	private CastType getOrderCastType(EntityAttributeType attrType) {
		//O order by também suporta booleano, ao contrário das demais operações tipadas.
		if (attrType == EntityAttributeType.BOOLEAN) {
			return null;
		}
		return getCastType(attrType);
	}
	
	//Obtém o CastType para ser utilizado em operações como >, <, order by, etc.
	private CastType getCastType(EntityAttributeType attrType) {
		switch (attrType) {
			case INTEGER:
				return CastType.INTEGER;
			case DECIMAL:
				return CastType.REAL;
			case TEXT:
			case CHARACTER:
			case DATE:
			case TIME:
			case DATETIME:
				return null;
				
			case BOOLEAN:
				//Boolean não pode ser utilizado em operações deste tipo.
			case IMAGE:
			case BOOLEAN_ARRAY:
			case INTEGER_ARRAY:
			case DECIMAL_ARRAY:
			case TEXT_ARRAY:
			case CHARACTER_ARRAY:
			case DATE_ARRAY:
			case TIME_ARRAY:
			case DATETIME_ARRAY:
			case IMAGE_ARRAY:
				//Nem todos os tipos de atributos suportam este tipo de operação.
				throw new IllegalArgumentException("Invalid attribute type: " + attrType);
				
			default: 
				throw new IllegalArgumentException("Unsupported EntityAttributeType: " + attrType);
		}
	}
	
	private InternalRelationshipArrayView createInternalDataView(RelationshipArrayView dataView) {
		MobileBeanEntityRecord owner = (MobileBeanEntityRecord) dataView.getRecord(); 
		String ownerPath = HIERARCHY_SEPARATOR;
		//Se o registro base da visão de dados (fonte do relacionamento) for interno, a query tem que se basear no dono deste registro.
		if (owner.getEntityMetadata().isInternal()) {
			InternalMobileBeanEntityRecord internalRecord = (InternalMobileBeanEntityRecord) owner;
			owner = internalRecord.getOwner();
			ownerPath = internalRecord.getOwnerPath();
		}

		if (BuildConfig.DEBUG && (owner == null || owner.isNew())) {
			throw new AssertionError();
		}
		
		//Cria o caminho completo do registro dono até o relacionamento. Ex: /items[0]/subitems
		String relFullname = getPropertyFullname(ownerPath, dataView.getRelationship().getName());
        return new InternalRelationshipArrayView(owner, relFullname);
	}
	
	private static String getEntityFirstField(EntityMetadata entity) {
		Map<String, EntityAttribute> attributesMap = entity.getAttributesMap();
		if (!attributesMap.isEmpty()) {
			return attributesMap.values().iterator().next().getName();
		}
		Map<String, EntityRelationship> relationshipsMap = entity.getRelationshipsMap();
		if (!relationshipsMap.isEmpty()) {
			return relationshipsMap.values().iterator().next().getName();
		}
		throw new IllegalStateException(String.format("The entity %s should have declared at least one attribute or relationship", entity.getName()));
	}

	
	/*
	 * Métodos auxiliares de verificação.
	 */
	
	private void checkSelectState() {
		if (selectFields == null) {
			throw new IllegalArgumentException("\"select\" must be called before.");
		}
	}
	
	private void checkFromState(boolean needed) {
		if (needed) {
			if (sourceEntity == null) {
				throw new IllegalStateException("\"from\" must be called before.");
			}
		} else {
			if (sourceEntity != null) {
				throw new IllegalStateException("\"from\" can be called only once per query.");
			}
		}
	}
	
	private void checkIsNotInternalEntity(EntityMetadata entityMetadata) {
		if (entityMetadata.isInternal()) {
			throw new UnsupportedOperationException("Unsupported operation for internal entity: " + entityMetadata.getName());
		}
	}
	
	private void checkWhereState() {
		if (cndBuilder == null) {
			throw new IllegalStateException("\"where\" must be called before.");
		}
	}
	
	private void checkSelectFieldPath(String[] fieldPath) {
		if (fieldPath == null || fieldPath.length == 0) {
			throw new IllegalArgumentException("Invalid field path, it cannot be null or empty: " + Arrays.toString(fieldPath));
		}
	}
	
	private void checkRelationshipType(String... relationshipPath) {
		EntityRelationship relationship = getRelationshipFromPath(relationshipPath);
		
		if (!isSupportedRelationship(relationship)) {
			throw new IllegalArgumentException(String.format("Invalid relationship type, only non-internal single relationships are allowed. Relationship name = %s.", relationship.getName()));	
		}
	}
	
	private void checkAttributeType(String... attributePath) {
		checkAttributeType(getAttributeFromPath(attributePath));
	}
	
	private static void checkAttributeType(IEntityAttribute attribute) {
		if (!isSupportedAttribute(attribute)) {
			throw new IllegalArgumentException(String.format("Invalid attribute type: %s.", attribute.getType()));	
		}
	}
	
	private static void checkValueType(Object value, Class<?> type) {
		if (!type.isInstance(value)) { 
			throw new IllegalArgumentException(String.format("Incorrect value type. %s was expected, but %s was received.", type.getSimpleName(), value.getClass().getSimpleName()));
		}
	}
	
	
	/*
	 * Classes auxiliares
	 */
	
	/**
	 * Representa um campo sendo utilizado na construção da query.
	 */
	private static final class UsedField implements Comparable<UsedField> {
		
		private final String name;
		private final int index;
		private final UsedField superField;
		private final EntityMetadata entity;
		private Map<String, UsedField> subFields;
		
		public UsedField(String name, int index, UsedField superField, EntityMetadata entity) {
			this.name = name;
			this.index = index;
			this.superField = superField;
			this.entity = entity;
		}
		
		public String getName() {
			return name;
		}
		
		public int getIndex() {
			return index;
		}
		
		public UsedField getSuperField() {
			return superField;
		}
		
		public EntityMetadata getEntity() {
			return entity;
		}

		public Map<String, UsedField> getSubFields() {
			return subFields;
		}
		
		public void setSubFields(Map<String, UsedField> subFields) {
			this.subFields = subFields;
		}

		@Override
        @SuppressWarnings("NullableProblems")
		public int compareTo(UsedField another) {
			return index - another.index;
		}
	}
	
	/**
	 * Representa uma visão de dados baseada em um relacionamento para uma entidade interna.
	 * É gerada a partir de um {@link RelationshipArrayView} no momento de sua configuração. 
	 */
	private static final class InternalRelationshipArrayView {
		
		private final MobileBeanEntityRecord owner;
		private final String relFullname;
		
		public InternalRelationshipArrayView(MobileBeanEntityRecord owner, String relPath) {
			this.owner = owner;
			this.relFullname = relPath;
		}

		public MobileBeanEntityRecord getOwner() {
			return owner;
		}

		public String getRelFullname() {
			return relFullname;
		}
	}
}
