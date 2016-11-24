package br.com.zalem.ymir.client.android.entity.ui.event;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;

/**
 * Gerenciador de eventos da aplicação.<br>
 * Define os eventos que a aplicação deve disparar e como cada um deles pode influenciar no comportamento de determinadas
 * funcionalidades.<br>
 * Cabe a implementação do gerenciador de eventos determinar como estes eventos podem ser observados e utilizados na 
 * customização das funcionalidades da aplicação.
 *
 * @author Thiago Gesser
 */
public interface IEntityUIEventManager {

	/**
	 * Dispara o evento de início de edição de um registro de entidade.<br>
	 * Os valores dos campos que estão sendo editados devem poder sofrer alterações. 
	 *  
	 * @param record registro que será editado.
	 * @param msgHandler manipulador de mensagens da edição do registro.
	 */
	void fireStartEditRecordEvent(IEntityRecord record, IEntityEditingErrorHandler msgHandler);
	
	/**
	 * Dispara o evento de edição de um atributo de um registro de entidade.<br>
	 * Os valores dos campos que estão sendo editados devem poder sofrer alterações. 
	 * 
	 * @param record registro que foi editado.
	 * @param attributeName nome do atributo que sofreu edição.
	 * @param msgHandler manipulador de mensagens da edição do registro.
	 */
	void fireEditRecordAttributeEvent(IEntityRecord record, String attributeName, IEntityEditingErrorHandler msgHandler);
	
	/**
	 * Dispara o evento de edição de um relacionamento de um registro de entidade.<br>
	 * Os valores dos campos que estão sendo editados devem poder sofrer alterações. 
	 * 
	 * @param record registro que foi editado.
	 * @param relationshipName nome do relacionamento que sofreu edição.
	 * @param msgHandler manipulador de mensagens da edição do registro.
	 */
	void fireEditRecordRelationshipEvent(IEntityRecord record, String relationshipName, IEntityEditingErrorHandler msgHandler);

	/**
	 * Dispara o evento que antecede a listagem de valores de uma enumeração.<br> 
	 * Os valores que serão listados devem ser alterados se o retorno deste método for uma lista de valores ao invés de <code>null</code>.<br>
	 * <br>
	 * Os valores dos campos do registro que estão sendo editados devem poder sofrer alterações.
	 * 
	 * @param record registro que está sendo editado.
	 * @param fieldName nome do campo que a enumeração representa.
	 * @param values valores da enumeração.
	 * @param msgHandler manipulador de mensagens da edição do registro.
     * @return uma lista de objetos diferente ou <code>null</code> se a mesma lista de objetos passada de parâmetro deve ser utilizada.
	 */
	List<?> fireBeforeListEnumValuesEvent(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler msgHandler);
	
	/**
	 * Dispara o evento que sucede a listagem de valores de uma enumeração.<br>
	 * Os valores dos campos do registro que estão sendo editados devem poder sofrer alterações.
	 * 
	 * @param record registro que está sendo editado.
	 * @param fieldName nome do campo que a enumeração representa.
	 * @param values valores da enumeração.
	 * @param msgHandler manipulador de mensagens da edição do registro.
	 */
	void fireAfterListEnumValuesEvent(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler msgHandler);
	
	/**
	 * Dispara o evento que antecede o salvamento de um registro de entidade.<br>
	 * O salvamento do registro deve ser cancelado se o retorno deste método for <code>true</code>.
	 * Os valores dos campos que estão sendo editados devem poder sofrer alterações. 
	 * 
	 * @param record registro que será salvo.
	 * @param sync indica se o registro será sincronizado com a fonte de dados.
	 * @param msgHandler manipulador de mensagens da edição do registro.
	 * @return <code>true</code> para cancelar o salvamento do registro ou <code>false</code> para deixá-lo ocorrer normalmente.
	 */
	boolean fireBeforeSaveRecordEvent(IEntityRecord record, boolean sync, IEntityEditingErrorHandler msgHandler);
	
	/**
	 * Dispara o evento que antecede o salvamento efetivo de um registro de entidade.<br>
	 * Neste momento, não deve ser mais possível cancelar o salvamento do registro e todos os seus campos devem poder
	 * ser alterados, não apenas os que estão sendo editados. 
	 * 
	 * @param record registro que será salvo.
	 * @param sync indica se o registro será sincronizado com a fonte de dados.
	 */
	void fireSaveRecordEvent(IEntityRecord record, boolean sync);
	
	/**
	 * Dispara o evento que sucede o salvamento de um registro de entidade.<br>
	 * A alteração dos valores do registro neste momento pode não ter nenhum efeito.
	 * 
	 * @param record registro que foi salvo.
	 * @param sync indica se o registro foi sincronizado com a fonte de dados.
	 */
	void fireAfterSaveRecordEvent(IEntityRecord record, boolean sync);
	
	/**
	 * Dispara o evento que antecede a confirmação da edição de um registro de entidade, geralmente utilizado na edição
	 * de composições.<br>
	 * A confirmação da edição do registro deve ser cancelada se o retorno deste método for <code>true</code>.
	 * Os valores dos campos que estão sendo editados devem poder sofrer alterações. 
	 * 
	 * @param record registro que terá a edição confirmada.
	 * @param msgHandler manipulador de mensagens da edição do registro.
	 * @return <code>true</code> para cancelar a confirmação da edição do registro ou <code>false</code> para deixá-la ocorrer normalmente.
	 */
	boolean fireBeforeConfirmEditRecordEvent(IEntityRecord record, IEntityEditingErrorHandler msgHandler);
	
	/**
	 * Dispara o evento que antecede a confirmação efetiva da edição de um registro de entidade.<br>
	 * Neste momento, não deve ser mais possível cancelar a confirmação do registro e todos os seus campos devem poder
	 * ser alterados, não apenas os que estão sendo editados. 
	 * 
	 * @param record registro que terá a edição confirmada.
	 */
	void fireConfirmEditRecordEvent(IEntityRecord record);
	
	/**
	 * Dispara o evento que sucede a confirmação da edição de um registro de entidade, geralmente utilizado na edição
	 * de composições.<br>
	 * A alteração dos valores do registro neste momento pode não ter nenhum efeito.
	 * 
	 * @param record registro que teve a edição confirmada.
	 */
	void fireAfterConfirmEditRecordEvent(IEntityRecord record);


	/**
	 * Dispara o evento que precede a exclusão de um registro de entidade.
	 *
	 * @param record registro que será excluído.
	 * @return <code>true</code> para cancelar a exclusão do registro ou <code>false</code> para deixá-la ocorrer normalmente.
	 */
	boolean fireBeforeDeleteRecordEvent(IEntityRecord record);

    /**
     * Dispara o evento que sucede a exclusão de um registro de entidade.
     *
     * @param record registro que foi excluído.
     */
    void fireAfterDeleteRecordEvent(IEntityRecord record);


	/**
	 * Dispara o evento que antecede a alteração da listagem de registros de uma entidade.<br> 
	 * Os registros que serão listados devem ser alterados se o retorno deste método for uma lista de registros ao
	 * invés de <code>null</code>.
	 * 
	 * @param records lista com os novos registros.
	 * @return uma lista de registros diferente ou <code>null</code> se a mesma lista de registros passada de parâmetro deve ser utilizada.
	 */
	List<IEntityRecord> fireBeforeListRecordsEvent(String entityName, List<IEntityRecord> records);
	
	/**
	 * Dispara o evento que sucede a alteração da listagem de registros de uma entidade.
	 * 
	 * @param records lista com os novos registros.
	 */
	void fireAfterListRecordsEvent(String entityName, List<IEntityRecord> records);
	
	/**
	 * Dispara o evento de detalhamento de um registro de entidade.<br>
	 * Os valores do registro devem poder sofrer alterações de forma que estas alterações reflitam no detalhamento.
	 * 
	 * @param record registro que será detalhado.
	 */
	void fireDetailRecordEvent(IEntityRecord record);
}
