package br.com.zalem.ymir.client.android.entity.data.openmobster.cursor;

import android.database.Cursor;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;

/**
 * Base para cursores baseado no OpenMobster e especializados em obter registros completos.<br>
 * <br>
 * Qualquer tentativa de obter os dados de uma campo específco resultará em exceção.
 * 
 * @see MobileBeanSelectionCursor
 *
 * @author Thiago Gesser
 */
public abstract class AbstractMobileBeanEntityRecordCursor extends AbstractMobileBeanCursor {
	
	private static final String NO_FIELDS_ERROR_MSG = "This cursor has no fields.";

	public AbstractMobileBeanEntityRecordCursor(Cursor dbCursor) {
		super(dbCursor);
	}
	
	@Override
	public int getFieldIndex(String fieldName) {
		throw new IllegalArgumentException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public String getFieldName(int columnIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public String[] getFieldNames() {
		return new String[0];
	}

	@Override
	public boolean isNull(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Integer getIntegerValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Double getDecimalValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public String getTextValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Boolean getBooleanValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Date getDateValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Time getTimeValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Timestamp getDatetimeValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Character getCharacterValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public IEntityRecord getRelationshipValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}

	@Override
	public Object getValue(int fieldIndex) {
		throw new UnsupportedOperationException(NO_FIELDS_ERROR_MSG);
	}
}
