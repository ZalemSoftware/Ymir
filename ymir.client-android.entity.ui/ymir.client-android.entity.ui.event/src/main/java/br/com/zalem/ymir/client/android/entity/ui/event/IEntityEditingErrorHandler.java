package br.com.zalem.ymir.client.android.entity.ui.event;


import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;

/**
 * Manipulador de erros de edição de registro de entidade. É utilizado pelo {@link IEntityUIEventManager} para dispor uma
 * maneira de customizar a edição de registro através de mensagens de orientação ao usuário.<br>
 * Cabe ao componente que lança os eventos de edição implementar a estrutura de manipulação das mensagens de erro, além de
 * definir como, quando e onde as mensagens serão exibidas.<br>
 * <br>
 * É possível definir/remover mensagens de erro em atributos, relacionamentos e registros referenciados por relacionamentos múltiplos
 * do registro sendo editado. Além disso, também é permitido definir uma mensagem global de erro, que pode orientar o usuário sobre
 * situações que vão além de apenas um erro em um campo.<br>
 * Para facilitar a manipulação dos erros, é disponibilizado um método para saber se existem erros definidos em determinado momento ({@link #isEmpty()})
 * e um para remover todos os erros de todos os atributos e relacionamentos ({@link #clearAll()}).
 *
 * @see IEntityUIEventManager
  *
 * @author Thiago Gesser
 */
public interface IEntityEditingErrorHandler {

    /**
     * Chama o método {@link #setGlobalError(String)} passando a mensagem de erro contida no recurso String.
     *
     * @param errorMsgResId identificador do recurso String com a mensagem de erro.
     */
    void setGlobalError(int errorMsgResId);

    /**
     * Define a mensagem global de erro da edição.<br>
     * Pode ser utilizada para orientar o usuário sobre problemas que envolvem mais que um campo ou outras questões.
     *
     * @param errorMsg mensagem global de erro.
     */
    void setGlobalError(String errorMsg);

    /**
     * Remove a mensagem global de erro da edição.
     */
    void removeGlobalError();

    /**
     * Obtém a mensagem global de erro da edição.
     *
     * @return a mensagem global de erro obtida ou <code>null</code> se não há mensagem global de erro.
     */
    String getGlobalError();


    /**
     * Chama o método {@link #setAttributeError(String, String)} passando a mensagem de erro contida no recurso String.
     *
     * @param attributeName nome do atributo.
     * @param errorMsgResId identificador do recurso String com a mensagem de erro.
     */
    void setAttributeError(String attributeName, int errorMsgResId);

    /**
     * Define uma mensagem de erro para um atributo sendo editado.
     *
     * @param attributeName nome do atributo.
     * @param errorMsg mensagem de erro.
     */
    void setAttributeError(String attributeName, String errorMsg);

    /**
     * Obtém a mensagem de erro definida para o atributo sendo editado.
     *
     * @param attributeName nome do atributo.
     * @return a mensagem de erro obtida ou <code>null</code> se não há erro definido.
     */
    String getAttributeError(String attributeName);

    /**
     * Remove a mensagem de erro definida para o atributo sendo editado.
     *
     * @param attributeName nome do atributo.
     */
    void removeAttributeError(String attributeName);


    /**
     * Chama o método {@link #setRelationshipError(String, String)} passando a mensagem de erro contida no recurso String.
     *
     * @param relationshipName nome do relacionamento.
     * @param errorMsgResId identificador do recurso String com a mensagem de erro.
     */
    void setRelationshipError(String relationshipName, int errorMsgResId);

    /**
     * Define uma mensagem de erro para um relacionamento sendo editado.
     *
     * @param relationshipName nome do relacionamento.
     * @param errorMsg mensagem de erro.
     */
    void setRelationshipError(String relationshipName, String errorMsg);

    /**
     * Remove a mensagem de erro definida para o relacionamento sendo editado.
     *
     * @param relationshipName nome do relacionamento.
     */
    void removeRelationshipError(String relationshipName);

    /**
     * Obtém a mensagem de erro definida para o relacionamento sendo editado.
     *
     * @param relationshipName nome do relacionamento.
     * @return a mensagem de erro obtida ou <code>null</code> se não há erro definido.
     */
    String getRelationshipError(String relationshipName);


    /**
     * Chama o método {@link #setMultipleRelationshipRecordError(String, IEntityRecord, String)} passando a mensagem de erro contida no recurso String.
     *
     * @param relationshipName nome do relacionamento múltiplo.
     * @param record registro apontado pelo relacionamento.
     * @param errorMsgResId identificador do recurso String com a mensagem de erro.
     */
    void setMultipleRelationshipRecordError(String relationshipName, IEntityRecord record, int errorMsgResId);

    /**
     * Define uma mensagem de erro para um registro apontado por um relacionamento múltiplo sendo editado.
     *
     * @param relationshipName nome do relacionamento múltiplo.
     * @param record registro apontado pelo relacionamento.
     * @param errorMsg mensagem de erro.
     */
    void setMultipleRelationshipRecordError(String relationshipName, IEntityRecord record, String errorMsg);

    /**
     * Remove a mensagem de erro definida para o registro apontado pelo relacionamento múltiplo sendo editado.
     *
     * @param relationshipName nome do relacionamento múltiplo.
     * @param record registro apontado pelo relacionamento.
     */
    void removeMultipleRelationshipRecordError(String relationshipName, IEntityRecord record);

    /**
     * Remove todas as mensagem de erro definidas para os registros apontados pelo relacionamento múltiplo sendo editado.
     *
     * @param relationshipName nome do relacionamento múltiplo.
     */
    void clearMultipleRelationshipRecordsErrors(String relationshipName);

    /**
     * Obtém a mensagem de erro definida para o registro apontado pelo relacionamento múltiplo sendo editado.
     *
     * @param relationshipName nome do relacionamento múltiplo.
     * @param record registro apontado pelo relacionamento.
     * @return a mensagem de erro obtida ou <code>null</code> se não há erro definido.
     */
    String getMultipleRelationshipRecordError(String relationshipName, IEntityRecord record);


    /**
     * Indica se não há nenhum erro nos campos sendo editados (atributos e relacionamentos).
     *
     * @return <code>true</code> se não houver nenhuma erro e <code>false</code> caso contrário.
     */
    boolean isEmpty();

    /**
     * Limpa todos os erros dos campos sendo editados (atributos e relacionamentos).
     */
    void clearAll();
}
