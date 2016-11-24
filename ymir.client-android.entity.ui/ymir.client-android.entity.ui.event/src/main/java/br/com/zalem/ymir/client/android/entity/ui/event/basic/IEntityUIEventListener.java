package br.com.zalem.ymir.client.android.entity.ui.event.basic;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;


/**
 * Listener de eventos da aplicação referentes a entidades específicas.<br>
 * Deve ser utilizado através do {@link BasicEntityUIEventManager}.
 * 
 * @see BasicEntityUIEventManager
 * @see IEntityEditingErrorHandler
 *
 * @author Thiago Gesser
 */
public interface IEntityUIEventListener {
	
	/**
	 * Obtém o nome da entidade referenciada por este listener.
	 * 
	 * @return o nome da entidade obtido.
	 */
	String getEntityName();

	/**
	 * Chamado quando um registro da entidade vai começar a ser editado.<br>
	 * Os valores dos campos que estão sendo editados podem ser alterados.
	 * 
	 * @param record registro que será editado.
	 * @param errorHandler manipulador de erros da edição do registro.
	 */
	void onStartEditRecord(IEntityRecord record, IEntityEditingErrorHandler errorHandler);
	
	/**
	 * Chamado quando um atributo de um registro da entidade foi editado.<br>
	 * Os valores dos campos que estão sendo editados podem ser alterados.
	 * 
	 * @param record registro que foi editado.
	 * @param attributeName nome do atributo que foi editado.
	 * @param errorHandler manipulador de erros da edição do registro. 
	 */
	void onEditRecordAttribute(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler);
	
	/**
	 * Chamado quando um relacionamento de um registro da entidade foi editado.<br>
	 * Os valores dos campos que estão sendo editados podem ser alterados.
	 * 
	 * @param record registro que foi editado.
	 * @param relationshipName nome do relacionamento que foi editado.
	 * @param errorHandler manipulador de erros da edição do registro.
	 */
	void onEditRecordRelationship(IEntityRecord record, String relationshipName, IEntityEditingErrorHandler errorHandler);
	
	/**
	 * Chamado antes da listagem dos valores de um campo de enumeração.<br>
	 * Os valores que serão listados podem ser alterados se o retorno deste método for uma lista de objetos ao invés de <code>null</code>.<br>
	 * <br>
	 * Os valores dos campos do registro que estão sendo editados podem ser alterados.
	 * 
	 * @param record registro que está sendo editado.
	 * @param fieldName nome do campo que a enumeração representa.
	 * @param values valores da enumeração.
	 * @param errorHandler manipulador de erros da edição do registro.
	 * @return uma lista de objetos diferente ou <code>null</code> se a mesma lista de objetos passada de parâmetro deve ser utilizada.
	 */
	List<?> beforeListEnumValues(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler errorHandler);
	
	/**
	 * Chamado depois da listagem dos valores de um campo de enumeração.<br>
	 * Os valores dos campos do registro que estão sendo editados podem ser alterados.
	 * 
	 * @param record registro que está sendo editado.
	 * @param fieldName nome do campo que a enumeração representa.
	 * @param values valores da enumeração.
	 * @param errorHandler manipulador de erros da edição do registro.
	 */
	void afterListEnumValues(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler errorHandler);
	
	/**
	 * Chamado antes do salvamento de um registro da entidade.<br>
	 * O salvamento do registro pode ser cancelado se o retorno deste método for <code>true</code>.
	 * Os valores dos campos que estão sendo editados podem ser alterados.<br>
	 * <br>
	 * Recomenda-se utilizar este método para a validação dos valores dos registros, cancelando o salvamento se houver algum erro.
	 * 
	 * @param record registro que será salvo.
	 * @param sync indica se o registro será sincronizado com a fonte de dados.
	 * @param errorHandler manipulador de erros da edição do registro.
	 * @return <code>true</code> para cancelar o salvamento do registro ou <code>false</code> para deixá-lo ocorrer normalmente.
	 */
	boolean beforeSaveRecord(IEntityRecord record, boolean sync, IEntityEditingErrorHandler errorHandler);
	
	/**
	 * Chamado antes do salvamento efetivo de um registro da entidade.<br>
	 * Neste momento, o salvamento do registro não pode mais ser cancelado e todos os seus campos podem ser alterados,
	 * não apenas os que estão sendo editados.<br>
	 * <br>
	 * Recomenda-se utilizar este método para a aplicação de valores antes do salvamento dos registros.
	 * 
	 * @param record registro que será salvo.
	 * @param sync indica se o registro será sincronizado com a fonte de dados.
	 */
	void onSaveRecord(IEntityRecord record, boolean sync);
	
	/**
	 * Chamado após o salvamento de um registro da entidade.<br>
	 * A alteração dos valores do registro neste momento não terá nenhum efeito.<br>
	 * <br>
	 * Recomenda-se utilizar este método para o log de informações.
	 * 
	 * @param record registro que foi salvo.
	 * @param sync indica se o registro foi sincronizado com a fonte de dados.
	 */
	void afterSaveRecord(IEntityRecord record, boolean sync);
	
	/**
	 * Chamado antes da confirmação da edição de um registro da entidade, geralmente utilizado se a entidade
	 * for uma composição de outra.<br>
	 * A confirmação da edição do registro pode ser cancelada se o retorno deste método for <code>true</code>.
	 * Os valores dos campos que estão sendo editados devem poder sofrer alterações.<br>
	 * <br>
	 * Recomenda-se utilizar este método para a validação dos valores dos registros, cancelando a confirmnação se houver algum erro.
	 * 
	 * @param record registro que terá a edição confirmada.
	 * @param errorHandler manipulador de erros da edição do registro.
	 * @return <code>true</code> para cancelar a confirmação da edição do registro ou <code>false</code> para deixá-la ocorrer normalmente.
	 */
	boolean beforeConfirmEditRecord(IEntityRecord record, IEntityEditingErrorHandler errorHandler);
	
	/**
	 * Chamado antes da confirmação efetiva da edição de um registro da entidade.<br>
	 * Neste momento, a confirmação do registro não pode mais ser cancelada e todos os seus campos podem ser alterados,
	 * não apenas os que estão sendo editados.<br>
	 * <br>
	 * Recomenda-se utilizar este método para a aplicação de valores antes da confirmação dos registros.
	 * 
	 * @param record registro que terá a edição confirmada.
	 */
	void onConfirmEditRecord(IEntityRecord record);
	
	/**
	 * Chamado após a confirmação da edição de um registro da entidade, geralmente utilizado se a entidade
	 * for uma composição de outra.<br>
	 * A alteração dos valores do registro neste momento não terá nenhum efeito.<br>
	 * <br>
	 * Recomenda-se utilizar este método para o log de informações.
	 * 
	 * @param record registro que teve a edição confirmada.
	 */
	void afterConfirmEditRecord(IEntityRecord record);


	/**
	 * Chamado antes da exclusão de um registro da entidade.
	 *
	 * @param record registro que será excluído.
	 * @return <code>true</code> para cancelar a exclusão do registro ou <code>false</code> para deixá-la ocorrer normalmente.
	 */
	boolean beforeDeleteRecord(IEntityRecord record);

    /**
     * Chamado após a exclusão de um registro da entidade.<br>
     *
     * @param record registro que foi excluído.
     */
    void afterDeleteRecord(IEntityRecord record);

	
	/**
	 * Chamado antes da alteração da listagem de registros da entidade.<br>
	 * Os registros que serão listados podem ser alterados se o retorno deste método for uma lista de registros ao
	 * invés de <code>null</code>.
	 * 
	 * @param records lista com os novos registros.
	 * @return uma lista de registros diferente ou <code>null</code> se a mesma lista de registros passada de parâmetro deve ser utilizada.
	 */
	List<IEntityRecord> beforeListRecords(List<IEntityRecord> records);
	
	/**
	 * Chamado após a alteração da listagem de registros da entidade.
	 * 
	 * @param records lista com os novos registros.
	 */
	void afterListRecords(List<IEntityRecord> records);
	
	/**
	 * Chamado quando um registro da entidade está sendo detalhado.<br>
	 * Os valores do registro podem ser alterados, influenciando o detalhamento.
	 * 
	 * @param record registro que está sendo detalhado.
	 */
	void onDetailRecord(IEntityRecord record);
}
