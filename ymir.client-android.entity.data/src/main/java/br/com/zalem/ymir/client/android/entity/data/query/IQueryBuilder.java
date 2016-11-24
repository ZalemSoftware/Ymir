package br.com.zalem.ymir.client.android.entity.data.query;

import java.io.Serializable;

import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectAsStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;

/**
 * Montador de queries de manipulação de dados.<br>
 * Provê todos os métodos de montagem disponíveis, cabendo ao utilizador construir a query de maneira correta.<br>
 * <br>
 * Recomenda-se utilizar apenas para queries que exigem uma lógica diferenciada na montagem. Para as demais, o {@link IQueryStatement} é mais indicado.
 *
 * @author Thiago Gesser
 */
public interface IQueryBuilder extends IQueryStatement, ISelectAsStatement, ISelectBuilder {

    @Override
    IQueryBuilder select(boolean distinct);

    @Override
    IQueryBuilder as(String alias);

    @Override
    IQueryBuilder condition();

    @Override
    IQueryBuilder not();

    @Override
    IQueryBuilder o();

    @Override
    IQueryBuilder eq(Object value, String... attrPathOrAlias);

    @Override
    IQueryBuilder lt(Object value, String... attrPathOrAlias);

    @Override
    IQueryBuilder gt(Object value, String... attrPathOrAlias);

    @Override
    IQueryBuilder le(Object value, String... attrPathOrAlias);

    @Override
    IQueryBuilder ge(Object value, String... attrPathOrAlias);

    @Override
    IQueryBuilder in(Object[] values, String... attrPathOrAlias);

    @Override
    IQueryBuilder between(Object value1, Object value2, String... attrPathOrAlias);

    @Override
    IQueryBuilder contains(String text, String... attrPathOrAlias);

    @Override
    IQueryBuilder startsWith(String text, String... attrPathOrAlias);

    @Override
    IQueryBuilder endsWith(String text, String... attrPathOrAlias);

    @Override
    IQueryBuilder isNull(String... attrPathOrAlias);

    @Override
    IQueryBuilder rEq(Serializable id, String... relPathOrAlias);

    @Override
    IQueryBuilder rIn(Serializable[] ids, String... relPathOrAlias);

    @Override
    IQueryBuilder rIsNull(String... relPathOrAlias);

    @Override
    IQueryBuilder ssEq(SyncStatus ss);

    @Override
    IQueryBuilder ssIn(SyncStatus... sss);

    @Override
    IQueryBuilder idEq(Serializable id);

    @Override
    IQueryBuilder idIn(Serializable... ids);

    @Override
    IQueryBuilder where();

    @Override
    IQueryBuilder and();

    @Override
    IQueryBuilder or();

    @Override
    IQueryBuilder c();

    @Override
    IQueryBuilder orderBy(boolean asc, String... attrPathOrAlias);

    @Override
    IQueryBuilder attribute(String... attributePath);

    @Override
    IQueryBuilder relationship(String... relationshipPath);

    @Override
    IQueryBuilder from(String entityName);

    @Override
    IQueryBuilder from(ISelectQuery subselect);

    @Override
    IQueryBuilder from(RelationshipArrayView dataView);
}
