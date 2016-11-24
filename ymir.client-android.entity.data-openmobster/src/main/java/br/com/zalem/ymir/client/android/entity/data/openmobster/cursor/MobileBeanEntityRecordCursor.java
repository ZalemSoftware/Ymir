package br.com.zalem.ymir.client.android.entity.data.openmobster.cursor;

import android.database.Cursor;
import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDAO;

/**
 * Cursor baseado no OpenMobster especializado em obter registros completos de entidades não internas.<br>
 * Baseia-se em um cursor do banco que deve conter uma coluna do tipo <code>String</code> que retorna o identificador 
 * do registro a ser buscado em cada linha. Os registros são obtidos através do {@link IEntityDAO#get(java.io.Serializable)}
 * da entidade alvo.
 * 
 * @author Thiago Gesser
 */
public final class MobileBeanEntityRecordCursor extends AbstractMobileBeanEntityRecordCursor {

	private final int idColIndex;
	private final IEntityDAO dao;
	
	/**
	 * Invoca o construtor {@link br.com.zalem.ymir.client.android.entity.data.openmobster.cursor.MobileBeanEntityRecordCursor#MobileBeanEntityRecordCursor(android.database.Cursor, MobileBeanEntityDAO, int)}
	 * passando <code>0</code> como índice da coluna.
	 */
	public MobileBeanEntityRecordCursor(Cursor dbCursor, MobileBeanEntityDAO dao) {
		this(dbCursor, dao, 0);
	}
	
	/**
	 * Cria MobileBeanEntityRecordCursor utilizando o índice da coluna de id para obter os identificadores dos registros.
	 * 
	 * @param dbCursor cursor do banco.
	 * @param dao acessor de dados da entidade alvo.
	 * @param idColIndex índice da coluna de id.
	 */
	public MobileBeanEntityRecordCursor(Cursor dbCursor, MobileBeanEntityDAO dao, int idColIndex) {
		super(dbCursor);
		this.idColIndex = idColIndex;
		this.dao = dao;
	}

	@Override
	public IEntityRecord getEntityRecord() {
		String id = dbCursor.getString(idColIndex);
		return dao.get(id);
	}
}
