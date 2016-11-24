package br.com.zalem.ymir.client.android.util;

/**
 * Exceção que sinaliza a tentativa de utilizar um recurso ou funcionalidade que ainda está pendente de implementação,
 * testes ou liberação.
 *
 * @author Thiago Gesser
 */
@SuppressWarnings("serial")
public final class PendingFeatureException extends RuntimeException {
	
	public PendingFeatureException(String featureName) {
		super(String.format("The feature \"%s\" is not ready for use yet. You can gently ask to a skilled developer implement this now or just wait, because one day, it will be implemented for sure.", featureName));
	}
}
