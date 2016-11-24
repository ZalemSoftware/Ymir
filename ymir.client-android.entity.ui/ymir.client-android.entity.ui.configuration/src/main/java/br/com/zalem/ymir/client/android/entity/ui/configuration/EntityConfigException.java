package br.com.zalem.ymir.client.android.entity.ui.configuration;

/**
 * Utilizada para informar sobre erros detectados em validações feitas pelo {@link IEntityUIConfigManager}.
 *
 * @author Thiago Gesser
 */
@SuppressWarnings("serial")
public final class EntityConfigException extends Exception {

	public EntityConfigException(String msg) {
		super(msg);
	}

    public EntityConfigException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
