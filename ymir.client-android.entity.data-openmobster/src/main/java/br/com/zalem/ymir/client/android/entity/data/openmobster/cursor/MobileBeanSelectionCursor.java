package br.com.zalem.ymir.client.android.entity.data.openmobster.cursor;

import android.database.Cursor;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.TypeUtils;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery.ISelectField;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;

/**
 * Cursor baseado no OpenMobster especializado em obter dados dos campos.<br>
 * Os dados obtidos são convertidos para os tipos corretos utilizando os métodos do {@link TypeUtils}. Uma exceção
 * será lançada ao tentar obter um dado de um tipo diferente de seu campo.<br>
 * Não suporta a obtenção de registros completos.
 * 
 * @see MobileBeanEntityRecordCursor
 * @see TypeUtils
 *
 * @author Thiago Gesser
 */
public final class MobileBeanSelectionCursor extends AbstractMobileBeanCursor {

	private final SelectFieldInfo[] fields;

	public MobileBeanSelectionCursor(Cursor dbCursor, ISelectField[] selectFields, IEntityMetadata entityMetadata, IEntityDataManager entityManager) {
		super(dbCursor);
		
		fields = new SelectFieldInfo[selectFields.length];
		for (int i = 0; i < selectFields.length; i++) {
			ISelectField field = selectFields[i];
			
			int colIndex = dbCursor.getColumnIndexOrThrow(field.getAlias());
			if (field.isRelationship()) {
				IEntityRelationship relationship = MetadataUtils.getRelationshipFromPath(entityMetadata, field.getPath());
				IEntityDAO relationshipDAO = entityManager.getEntityDAO(relationship.getTarget().getName());
				fields[i] = new SelectFieldInfo(colIndex, field.getAlias(), relationshipDAO);
			} else {
				IEntityAttribute attribute = MetadataUtils.getAttributeFromPath(entityMetadata, field.getPath());
				fields[i] = new SelectFieldInfo(colIndex, field.getAlias(), attribute);
			}
		}
	}

	@Override
	public int getFieldIndex(String fieldName) {
		for (SelectFieldInfo field : fields) {
			if (field.getName().equals(fieldName)) {
				return field.getColIndex();
			}
		}
		
		throw new IllegalArgumentException("Field not found: " + fieldName);
	}

	@Override
	public String getFieldName(int fieldIndex) {
		return fields[fieldIndex].getName();
	}

	@Override
	public String[] getFieldNames() {
		String[] names = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			names[i] = fields[i].getName();
		}
		return names;
	}

	@Override
	public boolean isNull(int fieldIndex) {
		return dbCursor.isNull(fields[fieldIndex].getColIndex());
	}

	@Override
	public Integer getIntegerValue(int fieldIndex) {
		String strValue = getValue(fieldIndex, EntityAttributeType.INTEGER);
		try {
			return TypeUtils.convertToInteger(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(fieldIndex, EntityAttributeType.INTEGER, strValue);
		}
	}

	@Override
	public Double getDecimalValue(int fieldIndex) {
		String strValue = getValue(fieldIndex, EntityAttributeType.DECIMAL);
		try {
			return TypeUtils.convertToDecimal(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(fieldIndex, EntityAttributeType.DECIMAL, strValue);
		}
	}

	@Override
	public String getTextValue(int fieldIndex) {
		return getValue(fieldIndex, EntityAttributeType.TEXT);
	}

	@Override
	public Boolean getBooleanValue(int fieldIndex) {
		String strValue = getValue(fieldIndex, EntityAttributeType.BOOLEAN);
		try {
			return TypeUtils.convertToBoolean(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(fieldIndex, EntityAttributeType.BOOLEAN, strValue);
		}
	}

	@Override
	public Date getDateValue(int fieldIndex) {
		String strValue = getValue(fieldIndex, EntityAttributeType.DATE);
		try {
			return TypeUtils.convertToDate(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(fieldIndex, EntityAttributeType.DATE, strValue);
		}
	}

	@Override
	public Time getTimeValue(int fieldIndex) {
		String strValue = getValue(fieldIndex, EntityAttributeType.TIME);
		try {
			return TypeUtils.convertToTime(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(fieldIndex, EntityAttributeType.TIME, strValue);
		}
	}

    @Override
    public Timestamp getDatetimeValue(int fieldIndex) {
        String strValue = getValue(fieldIndex, EntityAttributeType.DATETIME);
        try {
            return TypeUtils.convertToDatetime(strValue);
        } catch (ParseException e) {
            throw newAttributeValueFormatException(fieldIndex, EntityAttributeType.DATETIME, strValue);
        }
    }

    @Override
	public Character getCharacterValue(int fieldIndex) {
		String strValue = getValue(fieldIndex, EntityAttributeType.CHARACTER);
		try {
			return TypeUtils.convertToCharacter(strValue);
		} catch (ParseException e) {
			throw newAttributeValueFormatException(fieldIndex, EntityAttributeType.CHARACTER, strValue);
		}
	}

    @Override
	public Object getValue(int fieldIndex) {
		SelectFieldInfo field = fields[fieldIndex];
		String strValue = dbCursor.getString(field.getColIndex());
		
		IEntityAttribute attribute = field.getAttribute();
		if (attribute == null) {
			return field.getRelationshipDAO().get(strValue);
		}
		
		try {
			return TypeUtils.convert(strValue, attribute.getType());
		} catch (ParseException e) {
			throw newAttributeValueFormatException(fieldIndex, attribute.getType(), strValue);
		}
	}

	@Override
	public IEntityRecord getRelationshipValue(int fieldIndex) {
		SelectFieldInfo field = fields[fieldIndex];
		IEntityDAO relationshipDAO = field.getRelationshipDAO();
		if (relationshipDAO == null) {
			throw new IllegalArgumentException(String.format("The field at the index %d is not a relationship.", fieldIndex));
		}
		
		String strValue = dbCursor.getString(field.getColIndex());
		return relationshipDAO.get(strValue);
	}

	@Override
	public IEntityRecord getEntityRecord() {
		throw new UnsupportedOperationException("Only cursors without specific selections can get entire records");
	}
	
	
	/*
	 * Métodos/classes auxiliares
	 */
	
	private String getValue(int fieldIndex, EntityAttributeType expectedType) {
		SelectFieldInfo field = fields[fieldIndex];
		checkAttributeType(field, expectedType, fieldIndex);
		
		return dbCursor.getString(field.getColIndex());
	}
	
	private static void checkAttributeType(SelectFieldInfo field, EntityAttributeType expectedType, int fieldIndex) {
		IEntityAttribute attribute = field.getAttribute();
		if (attribute == null) {
			throw new IllegalArgumentException(String.format("The field at the index %d is not an attribute.", fieldIndex));
		}
		if (attribute.getType() != expectedType) {
			throw new IllegalArgumentException(String.format("The field at the index %d is not of the %s type.", fieldIndex, expectedType));
		}
	}

    private IllegalArgumentException newAttributeValueFormatException(int fieldIndex, EntityAttributeType type, String strValue) {
        return new IllegalArgumentException(String.format("The field at the index %d do not have a value of type \"%s\". Field value = \"%s\".", fieldIndex, type, strValue));
    }
	
	/**
	 * Armazena as informações necessárias para a obtenção dos dados de um campo do cursor, que pode ser um atributo 
	 * (attribute != null) ou um relacionamento (relationshipDAO != null).
	 */
	private static final class SelectFieldInfo {
		private final int colIndex;
		private final String name;
		private IEntityAttribute attribute;
		private IEntityDAO relationshipDAO;
		
		private SelectFieldInfo(int colIndex, String name) {
			this.colIndex = colIndex;
			this.name = name;
		}
		
		public SelectFieldInfo(int colIndex, String name, IEntityAttribute attribute) {
			this(colIndex, name);
			this.attribute = attribute;
		}
		
		public SelectFieldInfo(int colIndex, String name, IEntityDAO relationshipDAO) {
			this(colIndex, name);
			this.relationshipDAO = relationshipDAO;
		}
		
		public int getColIndex() {
			return colIndex;
		}
		
		public String getName() {
			return name;
		}
		
		public IEntityAttribute getAttribute() {
			return attribute;
		}
		
		public IEntityDAO getRelationshipDAO() {
			return relationshipDAO;
		}
	}
}
