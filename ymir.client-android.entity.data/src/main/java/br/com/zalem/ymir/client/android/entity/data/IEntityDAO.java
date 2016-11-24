package br.com.zalem.ymir.client.android.entity.data;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectFromStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.data.query.select.NonUniqueResultException;

/**
 * Acessor de dados de uma entidade. Provê a obtenção e a manipulação de registros da entidade.
 *
 * @author Thiago Gesser
 */
public interface IEntityDAO {
	
	/**
	 * Obtém o <code>gerenciador de entidades</code> dono deste IEntityDAO.
	 * 
	 * @return o gerenciador de entidades obtido.
	 */
	IEntityDataManager getEntityManager();
	
	/**
	 * Obtém os metadados da entidade que o acessor de dados referencia.
	 * 
	 * @return os metadados da entidade.
	 */
	IEntityMetadata getEntityMetadata();
	
	/**
	 * Verifica se os registros da entidade de dados estão prontos para serem manipulados.
	 * 
	 * @return <code>true</code> se estiverem prontos ou <code>false</code> caso contrário.
	 */
	boolean isReady();
	
	/**
	 * Cria um novo registro da entidade de dados, utilizando um identificador gerado autoamticamente.
	 * 
	 * @return o registro criado.
	 * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
	 */
	IEntityRecord create();

    /**
     * Cria um novo registro da entidade de dados, utilizando o identificador definido.
     *
     * @param id o identificador do novo registro.
     * @return o registro criado.
     * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
     */
    IEntityRecord create(Serializable id);
	
	/**
	 * Salva o registro da entidade de dados, de forma que as alterações possam ser recuperadas posteriormente.<br>
	 * Se a sincronização foi ligada, o registro será sincronizado com a fonte de dados, ficando inicialmente
	 * com o {@link SyncStatus#SYNCHRONIZING} e depois que a sincronização tiver terminado com o {@link SyncStatus#SYNCHRONIZED}.<br>
	 * Se a sincronização não foi ligada, o registro ficará com as alterações armazenadas, mas ele não será 
	 * sincronizado com a fonte de dados, ficando com o {@link SyncStatus#DESYNCHRONIZED}.
	 * 
	 * @param record o registro que será salvo.
	 * @param sync define se a sincronização com a fonte de dados será ligada ou não.
	 * @return <code>true</code> se o registro foi salvo com sucesso e <code>false</code> caso contrário.
	 * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
	 */
	boolean save(IEntityRecord record, boolean sync);
	
	/**
	 * Exclui o registro da entidade de dados, de forma que ele não possa mais ser recuperado posteriormente.<br>
	 * Se a sincronização foi ligada, a exclusão do registro será sincronizada com a fonte de dados, caso contrário, a 
	 * exclusão não será sincronizada e o registro ficará com o status {@link SyncStatus#DESYNCHRONIZED}.<br>
	 * <br>
	 * Se a exclusão ocorrer com sucesso, o registro será marcado como <b>excluído</b> e não poderá mais ser utilizado nas operações do DAO,
     * permitindo apenas que seus dados sejam acessados.
	 * 
	 * @param record o registro que será excluído.
	 * @param sync define se a sincronização com a fonte de dados será ligada ou não.
	 * @return <code>true</code> se o registro foi excluído com sucesso e <code>false</code> caso contrário.
	 * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
     * @throws RelationshipViolationException se o registro não pode ser excluído porque há outros registros que apontam pra ele.
	 */
	boolean delete(IEntityRecord record, boolean sync) throws RelationshipViolationException;
	
	/**
	 * Atualiza os dados do registro de acordo com a sua versão salva mais recente.<br>
	 * Se não existir mais uma versão salva dos dados, o registro será marcado como <b>excluído</b> e este método
	 * retornará <code>false</code>. Registros excluídos não podem ser utilizado nas operações do DAO, permitindo apenas que seus dados sejam acessados.
	 * 
	 * @param record o registro que será atualizado.
	 * @return <code>true</code> se o registro foi atualizado com sucesso e <code>false</code> caso ele não exista mais.
	 * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
	 */
	boolean refresh(IEntityRecord record);
	
	/**
     * Chama o método {@link #copy(IEntityRecord, boolean)} passando <code>false</code> para o parâmetro <code>fresh</code>.
	 */
	IEntityRecord copy(IEntityRecord record);

    /**
     * Cria uma cópia do registro.<br>
     * Se o <code>fresh</code> for <code>false</code>, a cópia será exata, mantendo o id, o <code>status de sincronizaçao</code> e os campos alterados do registro.
     * Do contrário, a cópia será simplesmente um registro novo com todos os dados copiados do registro fonte.
     *
     * @param record registro que será copiado.
     * @param fresh se for <code>true</code>, a cópia do registro será apenas um registro novo com todos os dados do registro fonte.
     * @return a cópia do registro.
     */
    IEntityRecord copy(IEntityRecord record, boolean fresh);
	
	/**
	 * Obtém o registro da entidade de dados de acordo com o identificador único.
	 * O identificador não pode ser nulo.
	 * 
	 * @param id identificador único
	 * @return o registro obtido ou <code>null</code> caso não haja um registro correspondente ao identificador único.
	 * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
	 */
	IEntityRecord get(Serializable id);
	
	/**
	 * Obtém todos os registros da entidade de dados.
	 * 
	 * @return os registros obtidos.
	 * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
	 */
	List<IEntityRecord> getAll();

    /**
     * Verifica se existem registros da entidade de dados.
     *
     * @return <code>true</code> se existem registros e <code>false</code> caso contrário.
     * @throws IllegalStateException se os registros da entidade de dados ainda não estiverem prontos para serem manipulados.
     */
    boolean isEmpty();
	
	/**
	 * Chama o método {@link #select(boolean)} passando <code>false</code> como parâmetro.
	 * 
	 * @return o {@link ISelectFromStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	ISelectFromStatement select();
	
	/**
	 * Inicia a construção de uma query de seleção de dados, utilizando este DAO como a origem dos dados (FROM).<br>
	 * Como a origem dos dados já estará definida implicitamente, não será necessário defini-la durante a construção da query.
	 * Exemplo de utilização:
	 * <pre>
	 * {@code
	 * IEntityDAO dao = ...;
	 * List<String> result = dao.
	 *                           select(false).
	 *                               attribute("atributoX").
	 *                           where().
	 *                               eq("valor1", "atributo1").and().
	 *                               contains("valor2", "relacionamento", "atributoXpto").
	 *                       listResult();
	 * }<pre> 
	 * 
	 * @param distinct <code>true</code> se os resultados devem ser distintos e <code>false</code> caso contrário.
	 * @return o {@link ISelectFromStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	ISelectFromStatement select(boolean distinct);

    /**
     * Cria um montador de query de seleção que provê todos os métodos de montagem de uma vez, utilizando este DAO como a origem dos dados (FROM).<br>
     * Isto torna a montagem mais livre, mas sem nenhum auxílio contextual sobre o formato correto da query.<br>
     * <br>
     * Recomenda-se utilizar apenas para a montagem de seleções que exigem uma lógica diferenciada. Para as demais, o {@link #select()} é mais indicado.
     *
	 * @param distinct <code>true</code> se os resultados devem ser distintos e <code>false</code> caso contrário.
     * @return o montador de querie de seleção.
     */
    ISelectBuilder newSelectBuilder(boolean distinct);
	
	/**
	 * Executa a query de seleção de dados e assume que haverá apenas um resultado.<br>
	 * O resultado varia de acordo com os campos selecionados na query:
	 * <ul>
	 * 	<li>Nenhum campo selecionado: registro completo ({@link IEntityRecord});</li>
	 * 	<li>Um campo selecionado: valor do campo, de acordo com o seu tipo;</li>
	 * 	<li>Mais do que um campo selecionado: array com os valores dos campos, de acordo com seus tipos e obecedendo a ordem em que foram selecionados;</li>
	 * </ul>
	 * 
	 * @param query a query que será executada.
	 * @return o resultado da execução.
	 * @throws NonUniqueResultException se havia mais de um resultado.
	 */
	<T> T executeUniqueSelect(ISelectQuery query) throws NonUniqueResultException;
	
	/**
	 * Executa a query de seleção de dados e coloca os resultados em uma lista.<br>
	 * Os resultados variam de acordo com os campos selecionados na query:
	 * <ul>
	 * 	<li>Nenhum campo selecionado: registro completo ({@link IEntityRecord});</li>
	 * 	<li>Um campo selecionado: valor do campo, de acordo com o seu tipo;</li>
	 * 	<li>Mais do que um campo selecionado: array com os valores dos campos, de acordo com seus tipos e obecedendo a ordem em que foram selecionados;</li>
	 * </ul>
	 * 
	 * @param query a query que será executada.
	 * @return os resultados da execução em uma lista.
	 */
	<T> List<T> executeListSelect(ISelectQuery query);
	
	/**
	 * Executa a query de seleção de dados e retorna um cursor para a manipulação dos resultados.<br>
	 * 
	 * @param query a query que será executada.
	 * @return o cursor de manipulação dos resultados.
	 */
	IEntityRecordCursor executeCursorSelect(ISelectQuery query);
	
	/**
	 * Obtém o estado atual de um registro e salvo-o em um objeto {@link android.os.Parcelable}.<br>
	 * O registro depois pode ser recuperado através do método {@link #fromSavedState(android.os.Parcelable)}.
	 * 
	 * @param record registro que terá o estado salvo.
	 * @return o estado salvo do registro.
	 */
	Parcelable toSavedState(IEntityRecord record);
	
	/**
	 * Restaura o registro através do seu estado salvo obtido com o método {@link #toSavedState(IEntityRecord)}.
	 * 
	 * @param savedState o estado salvo do registro.
	 * @return o registro restaurado ou <code>null</code> se o registro já existia antes de ter seu estado salvo mas foi excluído.
	 */
	IEntityRecord fromSavedState(Parcelable savedState);
}
