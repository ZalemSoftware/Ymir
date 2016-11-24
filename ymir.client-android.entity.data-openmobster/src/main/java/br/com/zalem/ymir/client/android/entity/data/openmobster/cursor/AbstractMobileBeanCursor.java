package br.com.zalem.ymir.client.android.entity.data.openmobster.cursor;

import android.database.Cursor;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;

/**
 * Base para cursores de dados baseados no OpenMobster.<br>
 * Apenas engloba um {@link android.database.Cursor} e repassa para ele as funções básicas de um cursor, como mover-se, obter a contagem,
 * fechar, etc. 
 * 
 * @see android.database.Cursor
 *
 * @author Thiago Gesser
 */
public abstract class AbstractMobileBeanCursor implements IEntityRecordCursor {
	
	protected final Cursor dbCursor;

	public AbstractMobileBeanCursor(Cursor dbCursor) {
		this.dbCursor = dbCursor;
	}
	
	@Override
	public int getCount() {
		return dbCursor.getCount();
	}

	@Override
	public int getPosition() {
		return dbCursor.getPosition();
	}

	@Override
	public boolean move(int offset) {
		return dbCursor.move(offset);
	}

	@Override
	public boolean moveToPosition(int position) {
		return dbCursor.moveToPosition(position);
	}

	@Override
	public boolean moveToFirst() {
		return dbCursor.moveToFirst();
	}

	@Override
	public boolean moveToLast() {
		return dbCursor.moveToLast();
	}

	@Override
	public boolean moveToNext() {
		return dbCursor.moveToNext();
	}

	@Override
	public boolean moveToPrevious() {
		return dbCursor.moveToPrevious();
	}

	@Override
	public boolean isFirst() {
		return dbCursor.isFirst();
	}

	@Override
	public boolean isLast() {
		return dbCursor.isLast();
	}

	@Override
	public boolean isBeforeFirst() {
		return dbCursor.isBeforeFirst();
	}

	@Override
	public boolean isAfterLast() {
		return dbCursor.isAfterLast();
	}
	
	@Override
	public void close() {
		dbCursor.close();
	}
	
	@Override
	public boolean isClosed() {
		return dbCursor.isClosed();
	}
}
