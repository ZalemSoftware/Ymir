package br.com.zalem.ymir.client.android.entity.data.query.select;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;

/**
 * Instrução terminal de uma query de seleção de dados.<br>
 * Permite a definição de critérios de ordenação e a finalização da query. A finalização pode ser feita através do 
 * {@link #toQuery()}, que irá gerar uma instância da query para uso futuro, ou a execução direta através de um dos
 * métodos {@link #uniqueResult()}, {@link #listResult()} ou {@link #cursorResult()}. 
 *
 * @see ISelectQuery
 * @see IFromStatement
 * @see IRestrictionStatement
 * 
 * @author Thiago Gesser
 */
public interface ITerminalStatement {

	/**
	 * Adiciona um atributo como um critério de ordenação para os dados da query.
	 * 
	 * @param asc <code>true</code> se o critério for ascendente e <code>false</code> caso contrário.
	 * @param attrPathOrAlias caminho para o atributo. Pode ser diretamente um atributo da entidade alvo da query ou
	 * um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o próprio ITerminalStatement, para adicionar mais critérios de ordenação ou finalizar a query.
	 */
	ITerminalStatement orderBy(boolean asc, String... attrPathOrAlias);

    /**
     * Adiciona um limitador do número de resultados da query.
     *
     * @param number o número limite de resultados da query.
     * @return o próprio ITerminalStatement, para adicionar mais critérios de ordenação ou finalizar a query.
     */
    ITerminalStatement limit(int number);
	
	/**
	 * Finaliza a construção da query através da geração de uma instância de {@link ISelectQuery}, sendo possível
	 * executá-la posteriormente. É indicado para situações em que é necessário armazenar ou transferir a query
	 * para outras partes da aplicação.
	 * 
	 * @return a instância da query.
	 */
	ISelectQuery toQuery();
	
	/**
	 * Método de conveniência que obtém o {@link IEntityDAO} da entidade alvo da query através
	 * do {@link IEntityDataManager#getEntityDAO(String)}, gera a query através do {@link #toQuery()} e executa-a através
	 * do {@link IEntityDAO#executeUniqueSelect(ISelectQuery)}.
	 *  
	 * @return o resultado único da query.
	 * @throws NonUniqueResultException se havia mais do que um resultado na query.
	 */
	<T> T uniqueResult() throws NonUniqueResultException;
	
	/**
	 * Método de conveniência que obtém o {@link IEntityDAO} da entidade alvo da query através
	 * do {@link IEntityDataManager#getEntityDAO(String)}, gera a query através do {@link #toQuery()} e executa-a através
	 * do {@link IEntityDAO#executeListSelect(ISelectQuery)}.
	 * 
	 * @return a lista com os resultados da query.
	 */
	<T> List<T> listResult();
	
	/**
	 * Método de conveniência que obtém o {@link IEntityDAO} da entidade alvo da query através
	 * do {@link IEntityDataManager#getEntityDAO(String)}, gera a query através do {@link #toQuery()} e executa-a através
	 * do {@link IEntityDAO#executeCursorSelect(ISelectQuery)}.
	 * 
	 * @return o cursor com os resultados da query.
	 */
	IEntityRecordCursor cursorResult();
}
