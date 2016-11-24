package br.com.zalem.ymir.client.android.entity.data;

/**
 * Status de sincronização de um registro de uma entidade em relação à fonte de dados.
 *
 * @author Thiago Gesser
 */
public enum SyncStatus {
	
	/**
	 * Significa que o registro está desincronizado em relação à fonte de dados. Se trata de um registro que foi
	 * salvo sem a sincronização ligada.
	 */
	DESYNCHRONIZED,
	
	/**
	 * Significa que o registro está sendo sincronizado com a fonte de dados. Se trata de um registro que foi salvo
	 * com a sincronização ligada mas ainda não foi totalmente sincronizado. 
	 */
	SYNCHRONIZING,
	
	/**
	 * Significa que o registro está sincronizado em relação à fonte de dados. Se trata de um registro que foi salvo com 
	 * sincronização ligada e já foi totalmente sincronizado.
	 */
	SYNCHRONIZED
	
}
