package br.com.zalem.ymir.client.android.entity.data.query;

import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectStatement;

/**
 * Instruçao inicial de queries de manipulação de dados baseada no conceito de <code>Fluent Interface</code>.<br>
 * Provê a montagem simplificada e intuitiva de queries a partir de interfaces que limitam as expressões disponíveis a cada passo.
 *
 * @author Thiago Gesser
 */
public interface IQueryStatement {

	/**
	 * Inicia a construção de uma query de seleção de dados.<br>
	 * Exemplo de utilização:
	 * <pre>
	 * {@code
	 * IQueryBuilder builder = ...;
	 * List<String> result = builder.
	 *                           select(false).
	 *                               attribute("atributoX").
	 *                           from("Entidade").
	 *                           where().
	 *                               eq("valor1", "atributo1").and().
	 *                               contains("valor2", "relacionamento", "atributoXpto").
	 *                       listResult();
	 * }<pre> 
	 * 
	 * @param distinct <code>true</code> se os resultados devem ser distintos e <code>false</code> caso contrário.
	 * @return o {@link ISelectStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	ISelectStatement select(boolean distinct);
}
