package br.com.zalem.ymir.client.android.entity.data.query.select;

import java.io.Serializable;

import br.com.zalem.ymir.client.android.entity.data.SyncStatus;

/**
 * Montador de queries de seleção de dados.<br>
 * Provê todos os métodos de montagem de seleção disponíveis, cabendo ao utilizador construir a query de maneira correta.<br>
 * <br>
 * Recomenda-se utilizar apenas para seleções que exigem uma lógica diferenciada na montagem. Para as demais, o {@link ISelectStatement} é mais indicado.
 *
 * @author Thiago Gesser
 */
public interface ISelectBuilder extends ISelectAsFromStatement, IConditionStatement, IRestrictionStatement {

    /**
     * Inicia uma nova condiçao na query, sendo um {@link #where()} na primeira vez e um {@link #and()} nas demais.
     *
     * @return o construtor de queries.
     */
    ISelectBuilder condition();

    @Override
    ISelectBuilder not();

    @Override
    ISelectBuilder o();

    @Override
    ISelectBuilder eq(Object value, String... attrPathOrAlias);

    @Override
    ISelectBuilder lt(Object value, String... attrPathOrAlias);

    @Override
    ISelectBuilder gt(Object value, String... attrPathOrAlias);

    @Override
    ISelectBuilder le(Object value, String... attrPathOrAlias);

    @Override
    ISelectBuilder ge(Object value, String... attrPathOrAlias);

    @Override
    ISelectBuilder in(Object[] values, String... attrPathOrAlias);

    @Override
    ISelectBuilder between(Object value1, Object value2, String... attrPathOrAlias);

    @Override
    ISelectBuilder contains(String text, String... attrPathOrAlias);

    @Override
    ISelectBuilder startsWith(String text, String... attrPathOrAlias);

    @Override
    ISelectBuilder endsWith(String text, String... attrPathOrAlias);

    @Override
    ISelectBuilder isNull(String... attrPathOrAlias);

    @Override
    ISelectBuilder rEq(Serializable id, String... relPathOrAlias);

    @Override
    ISelectBuilder rIn(Serializable[] ids, String... relPathOrAlias);

    @Override
    ISelectBuilder rIsNull(String... relPathOrAlias);

    @Override
    ISelectBuilder ssEq(SyncStatus ss);

    @Override
    ISelectBuilder ssIn(SyncStatus... sss);

    @Override
    ISelectBuilder idEq(Serializable id);

    @Override
    ISelectBuilder idIn(Serializable... ids);

    @Override
    ISelectBuilder where();

    @Override
    ISelectBuilder and();

    @Override
    ISelectBuilder or();

    @Override
    ISelectBuilder c();

    @Override
    ISelectBuilder as(String alias);

    @Override
    ISelectBuilder attribute(String... attributePath);

    @Override
    ISelectBuilder relationship(String... relationshipPath);

    @Override
    ISelectBuilder orderBy(boolean asc, String... attrPathOrAlias);
}
