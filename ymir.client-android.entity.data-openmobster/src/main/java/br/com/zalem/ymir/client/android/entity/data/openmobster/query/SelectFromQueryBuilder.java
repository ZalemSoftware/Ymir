package br.com.zalem.ymir.client.android.entity.data.openmobster.query;

import java.io.Serializable;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.data.query.select.ITerminalStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.NonUniqueResultException;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;

/**
 * Construtor de queries que dispõe apenas a parte do <code>select</code> e adiciona o <code>from</code> automaticamente.<br>
 * Engloba um {@link MobileBeanQueryBuilder}, o construtor real que recebe todas as informações colocadas neste construtor
 * e que ao fim é disponibilizado para dar continuidade a query. 
 *
 * @see MobileBeanQueryBuilder
 * 
 * @author Thiago Gesser
 */
public final class SelectFromQueryBuilder implements ISelectBuilder {
	
	private final MobileBeanQueryBuilder query;
	private final RelationshipArrayView dataView;
	private final String entityName;
	
	private boolean fromApplied;
	
	/**
	 * Cria um SelectFromQueryBuilder cujo o <code>from</code> será baseado no nome da entidade.
	 * 
	 * @param entityManager gerenciador de entidades.
	 * @param entityName nome da entidade.
	 * @param distinct <code>true</code> se o select deverá ser distinto ou <code>false</code> caso contrário.
	 */
	public SelectFromQueryBuilder(MobileBeanEntityDataManager entityManager, String entityName, boolean distinct) {
		query = new MobileBeanQueryBuilder(entityManager);
		query.select(distinct);
		this.entityName = entityName;
		dataView = null;
	}
	
	/**
	 * Cria um SelectFromQueryBuilder cujo o <code>from</code> será baseado na visão de dados.
	 * 
	 * @param entityManager gerenciador de entidades.
	 * @param dataView visão de dados baseado em um relacionamento do tipo array.
	 * @param distinct <code>true</code> se o select deverá ser distinto ou <code>false</code> caso contrário.
	 */
	public SelectFromQueryBuilder(MobileBeanEntityDataManager entityManager, RelationshipArrayView dataView, boolean distinct) {
		query = new MobileBeanQueryBuilder(entityManager);
		query.select(distinct);
		this.dataView = dataView;
		entityName = null;
	}

	@Override
	public ISelectBuilder attribute(String... attributePath) {
		query.attribute(attributePath);
		return this;
	}

	@Override
	public ISelectBuilder relationship(String... relationshipPath) {
		query.relationship(relationshipPath);
		return this;
	}
	
	@Override
	public ISelectBuilder as(String alias) {
		query.as(alias);
		return this;
	}
	
	@Override
	public ISelectBuilder where() {
		tryApplyFrom();
		
		return query.where();
	}

    @Override
    public ISelectBuilder condition() {
        tryApplyFrom();

        return query.condition();
    }


    @Override
    public ISelectBuilder eq(Object value, String... attrPathOrAlias) {
        return query.eq(value, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder lt(Object value, String... attrPathOrAlias) {
        return query.lt(value, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder gt(Object value, String... attrPathOrAlias) {
        return query.gt(value, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder le(Object value, String... attrPathOrAlias) {
        return query.le(value, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder ge(Object value, String... attrPathOrAlias) {
        return query.ge(value, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder in(Object[] values, String... attrPathOrAlias) {
        return query.in(values, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder between(Object value1, Object value2, String... attrPathOrAlias) {
        return query.between(value1, value2, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder contains(String text, String... attrPathOrAlias) {
        return query.contains(text, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder startsWith(String text, String... attrPathOrAlias) {
        return query.startsWith(text, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder endsWith(String text, String... attrPathOrAlias) {
        return query.endsWith(text, attrPathOrAlias);
    }

    @Override
    public ISelectBuilder isNull(String... attrPathOrAlias) {
        return query.isNull(attrPathOrAlias);
    }

    @Override
    public ISelectBuilder rEq(Serializable id, String... relPathOrAlias) {
        return query.rEq(id, relPathOrAlias);
    }

    @Override
    public ISelectBuilder rIn(Serializable[] ids, String... relPathOrAlias) {
        return query.rIn(ids, relPathOrAlias);
    }

    @Override
    public ISelectBuilder rIsNull(String... relPathOrAlias) {
        return query.rIsNull(relPathOrAlias);
    }

    @Override
    public ISelectBuilder ssEq(SyncStatus ss) {
        return query.ssEq(ss);
    }

    @Override
    public ISelectBuilder ssIn(SyncStatus... sss) {
        return query.ssIn(sss);
    }

    @Override
    public ISelectBuilder idEq(Serializable id) {
        return query.idEq(id);
    }

    @Override
    public ISelectBuilder idIn(Serializable... ids) {
        return query.idIn(ids);
    }

    @Override
    public ISelectBuilder o() {
        return query.o();
    }

    @Override
    public ISelectBuilder c() {
        return query.c();
    }

    @Override
    public ISelectBuilder not() {
        return query.not();
    }

    @Override
    public ISelectBuilder and() {
        return query.and();
    }

    @Override
    public ISelectBuilder or() {
        return query.or();
    }

	@Override
	public ISelectBuilder orderBy(boolean asc, String... attributePath) {
		tryApplyFrom();

		return query.orderBy(asc, attributePath);
	}

    @Override
    public ITerminalStatement limit(int number) {
		tryApplyFrom();

        return query.limit(number);
    }

    @Override
	public ISelectQuery toQuery() {
		tryApplyFrom();
		
		return query.toQuery();
	}

	@Override
	public <T> T uniqueResult() throws NonUniqueResultException {
		tryApplyFrom();
		
		return query.uniqueResult();
	}

	@Override
	public <T> List<T> listResult() {
		tryApplyFrom();
		
		return query.listResult();
	}

	@Override
	public IEntityRecordCursor cursorResult() {
		tryApplyFrom();
		
		return query.cursorResult();
	}

	
	/*
	 * Métodos auxiliares
	 */
	
	private void tryApplyFrom() {
		if (fromApplied) {
			return;
		}
		
		//Antes de continuar a query para além do FROM, aplica o FROM fixo determinado para este select (de acordo com a entidade ou dataView).
		if (dataView == null) {
			query.from(entityName);
		} else {
			query.from(dataView);
		}
		fromApplied = true;
	}
}
