package br.com.zalem.ymir.client.android.entity.data.query.select;

import java.util.List;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.query.IQueryStatement;

/**
 * Representação de uma query de seleção de dados.<br>
 * É gerada através da construção de query, que pode ser iniciada por {@link IQueryStatement#select(boolean)} ou
 * {@link IEntityDAO#select(boolean)} e finalizada por uma das instruções terminais através do método {@link ITerminalStatement#toQuery()}.<br>
 * Os resultados da query de seleção podem ser obtidos através de um dos métodos {@link #uniqueResult(IEntityDAO)},
 * {@link #listResult(IEntityDAO)} ou {@link #uniqueResult(IEntityDAO)}.<br>
 * <br>
 * A classe de query de seleção deve ser <code>stateless</code>, então os resultados podem ser apurados quantas vezes
 * for necessário a partir da mesma query. Também é necessário implementar corretamente a interface {@link android.os.Parcelable}.
 *
 * @author Thiago Gesser
 */
@SuppressLint("ParcelCreator")
public interface ISelectQuery extends Parcelable {
	
	/**
	 * Obtém os campos de seleção da query. A ausência de campos significa a seleção de registros completos.
	 * 
	 * @return os campos obtidos.
	 */
	ISelectField[] getFields();
	
	/**
	 * Método de conveniência que chama o {@link IEntityDAO#executeUniqueSelect(br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery)} passando esta query.
	 * 
	 * @param dao o dao que executará a query.
	 * @return o resultado único da query.
	 * @throws NonUniqueResultException se havia mais do que um resultado na query.
	 */
	<T> T uniqueResult(IEntityDAO dao) throws NonUniqueResultException;
	
	/**
	 * Método de conveniência que chama o {@link IEntityDAO#executeListSelect(br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery)} passando esta query.
	 * 
	 * @param dao o dao que executará a query.
	 * @return a lista com os resultados da query.
	 */
	<T> List<T> listResult(IEntityDAO dao);
	
	/**
	 * Método de conveniência que chama o {@link IEntityDAO#executeCursorSelect(br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery)} passando esta query.
	 * 
	 * @param dao o dao que executará a query.
	 * @return o cursor com os resultados da query.
	 */
	IEntityRecordCursor cursorResult(IEntityDAO dao);
	
	/**
	 * Representação de um campo de uma query de seleção de dados.
	 */
	public interface ISelectField {

		/**
		 * Obtém o caminho do campo de seleção.
		 * 
		 * @return o caminho obtido.
		 */
		String[] getPath();

		/**
		 * Obtém o alias do campo de seleção.
		 * 
		 * @return o alias obtido.
		 */
		String getAlias();
		
		/**
		 * Verifica se o campo é um relacionamento.
		 * 
		 * @return <code>true</code> se o campo for um relacionamento e <code>false</code> caso contrário.
		 */
		boolean isRelationship();
	}
}
