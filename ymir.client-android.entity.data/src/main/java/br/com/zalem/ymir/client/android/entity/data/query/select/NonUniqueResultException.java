package br.com.zalem.ymir.client.android.entity.data.query.select;

/**
 * Exceção que sinaliza um resultado não único de uma query que foi executada para ter um resultado único.
 *
 * @author Thiago Gesser
 */
@SuppressWarnings("serial")
public final class NonUniqueResultException extends Exception {

	private final ISelectQuery query;

	public NonUniqueResultException(ISelectQuery query) {
		this.query = query;
	}
	
	/**
	 * Obtém a query cujo o resultado não era único.
	 * 
	 * @return a query obtida.
	 */
	public ISelectQuery getQuery() {
		return query;
	}
}
