package br.com.zalem.ymir.client.android.entity.data.openmobster.metadata;

/**
 * Utilizada para informar sobre erros detectados em validações feitas pelo {@link EntityMetadataConfigValidator}.
 *
 * @author Thiago Gesser
 */
@SuppressWarnings("serial")
public final class EntityMetadataException extends Exception {

	public EntityMetadataException(String msg) {
		super(msg);
	}
}
