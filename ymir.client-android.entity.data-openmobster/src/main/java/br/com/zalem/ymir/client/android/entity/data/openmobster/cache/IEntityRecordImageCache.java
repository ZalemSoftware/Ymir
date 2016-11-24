package br.com.zalem.ymir.client.android.entity.data.openmobster.cache;

import android.graphics.Bitmap;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;

/**
 * Cache de imagens de registros de entidades da fonte de dados.<br>
 * As imagens são mapeadas especificamente para os atributos dos registros, de forma que cada imagem possui uma chave
 * composta pelos seguintes dados: nome da entidade, id do registro e nome do atributo.<br>
 * As imagens devem ser adicionadas pelo método {@link #addImage(String, String, String, android.graphics.Bitmap)} ou {@link #addImageArray(String, String, String, android.graphics.Bitmap[])}
 * e obtidas pelo método {@link #getImage(String, String, String)} ou {@link #getImageArray(String, String, String)}.<br>
 *
 * @author Thiago Gesser
 */
public interface IEntityRecordImageCache {

	/**
	 * Adiciona uma imagem no cache para determinado atributo de um registro, de acordo com o nome da entidade,
	 * id do registro e nome do atributo.
	 *  
	 * @param entityName nome da entidade do registro.
	 * @param recordId id do registro.
	 * @param attribute nome do atributo.
	 * @param image a imagem que será adicionada no cache.
	 */
	void addImage(String entityName, String recordId, String attribute, Bitmap image);
	
	/**
	 * Adiciona um array de imagens no cache para determinado atributo de um registro, de acordo com o nome da entidade,
	 * id do registro e nome do atributo.
	 *  
	 * @param entityName nome da entidade do registro.
	 * @param recordId id do registro.
	 * @param attribute nome do atributo.
	 * @param imageArray o array de imagens que será adicionado no cache.
	 */
	void addImageArray(String entityName, String recordId, String attribute, Bitmap[] imageArray);
	
	/**
	 * Obtém uma imagem adicionada previamente no cache, de acordo com seu nome da entidade, id do registro e nome do atributo.
	 * 
	 * @param entityName nome da entidade do registro.
	 * @param recordId id do registro.
	 * @param attribute nome do atributo.
	 * @return a imagem contida no cache ou <code>null</code> se não há imagem no cache para o nome da entidade, id do registro e nome do atributo.
	 * @throws IllegalArgumentException se havia um array de imagens para este nome da entidade, id do registro e nome do atributo ao invés de uma imagem.
	 */
	Bitmap getImage(String entityName, String recordId, String attribute);
	
	/**
	 * Obtém um array de imagens adicionada previamente no cache, de acordo com seu nome da entidade, id do registro e nome do atributo.
	 * 
	 * @param entityName nome da entidade do registro.
	 * @param recordId id do registro.
	 * @param attribute nome do atributo.
	 * @return a imagem contida no cache ou <code>null</code> se não há imagem no cache para o nome da entidade, id do registro e nome do atributo.
	 * @throws IllegalArgumentException se havia uma imagem para este nome da entidade, id do registro e nome do atributo ao invés de um array de imagens.
	 */
	Bitmap[] getImageArray(String entityName, String recordId, String attribute);
	
	/**
	 * Chamado quando o cache é atrelado a um {@link MobileBeanEntityDataManager}.<br>
	 * Pode ser utilizado para fazer inicializações.
	 */
	void onAttach(MobileBeanEntityDataManager entityManager);
	
	/**
	 * Chamado quando o cache é desatrelado de um {@link MobileBeanEntityDataManager}.<br>
	 * Pode ser utilizado para fazer finalizações.
	 */
	void onDetach(MobileBeanEntityDataManager entityManager);
}
