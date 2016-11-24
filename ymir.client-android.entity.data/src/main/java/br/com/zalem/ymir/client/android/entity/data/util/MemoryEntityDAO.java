package br.com.zalem.ymir.client.android.entity.data.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.BuildConfig;
import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectFromStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.data.query.select.ITerminalStatement;
import br.com.zalem.ymir.client.android.entity.data.query.select.NonUniqueResultException;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;

/**
 * Acessor de dados de entidades a partir da memória. Funciona a partir de uma lista fixa de {@link IEntityRecord}<br>
 * Utilizado em situações onde a aplicação precisa trabalhar com um {@link IEntityDAO}, mas os dados não estão salvos ou 
 * estão diferentes em relação à fonte de dados.<br>
 * Permite apenas as operações de leitura de dados. Queries de seleção não suportam seleção de campos específicos, filtros
 * e ordenação, ou seja, apenas queries que selecionam todos os registros completos são suportadas.
 * Ao invocar qualquer outra operação, será lançado um {@link UnsupportedOperationException}.
 *
 * @author Thiago Gesser
 */
public final class MemoryEntityDAO implements IEntityDAO {
	
	private final IEntityDataManager entityManager;
	private final IEntityMetadata entityMetadata;
	private final RelationshipArrayView dataView;
	private List<IEntityRecord> records;

	/**
	 * Cria um MemoryEntityDAO vazio.
	 * 
	 * @param entityManager gerenciador de entidades da aplicação.
	 * @param entityMetadata metadados da entidade que este DAO referencia.
	 */
	public MemoryEntityDAO(IEntityDataManager entityManager, IEntityMetadata entityMetadata) {
		this(entityManager, entityMetadata, Collections.<IEntityRecord>emptyList());
	}
	
	/**
	 * Cria um MemoryEntityDAO específico para a lista de registros.
	 * 
	 * @param entityManager gerenciador de entidades da aplicação.
	 * @param entityMetadata metadados da entidade que este DAO referencia.
	 * @param records lista de registros que integrarão o DAO.
	 */
	public MemoryEntityDAO(IEntityDataManager entityManager, IEntityMetadata entityMetadata, List<IEntityRecord> records) {
		if (entityManager == null || entityMetadata == null || records == null) {
			throw new NullPointerException("entityManager == null || entityMetadata == null || records == null");
		}
		
		this.entityManager = entityManager;
		this.entityMetadata = entityMetadata;
		this.records = records;
		this.dataView = null;
	}
	
	/**
	 * Cria um MemoryEntityDAO específico para os registros provenientes da visão de dados baseada num relacionamento
	 * do tipo array.
	 * 
	 * @param entityManager gerenciador de entidades da aplicação.
	 * @param dataView visão de dados.
	 */
	public MemoryEntityDAO(IEntityDataManager entityManager, RelationshipArrayView dataView) {
		if (entityManager == null || dataView == null) {
			throw new NullPointerException("entityManager == null || dataView == null");
		}
		
		this.entityManager = entityManager;
		this.entityMetadata = dataView.getRelationship().getTarget();
		this.dataView = dataView;
	}
	
	@Override
	public IEntityDataManager getEntityManager() {
		return entityManager;
	}

	@Override
	public IEntityMetadata getEntityMetadata() {
		return entityMetadata;
	}

	@Override
	public boolean isReady() {
		return true;
	}
	

	@Override
	public IEntityRecord get(Serializable id) {
		for (IEntityRecord record : getRecords()) {
			if (id.equals(record.getId())) {
				return record;
			}
		}
		return null;
	}
	
	@Override
	public List<IEntityRecord> getAll() {
		return getRecords();
	}

    @Override
    public boolean isEmpty() {
        return getRecords().isEmpty();
    }

    @Override
	public ISelectFromStatement select() {
		return new MemorySelectStament();
	}
	
	@Override
	public ISelectFromStatement select(boolean distinct) {
		return newSelectBuilder(distinct);
	}

    @Override
    public ISelectBuilder newSelectBuilder(boolean distinct) {
        return new MemorySelectStament();
    }

    @Override
	@SuppressWarnings("unchecked")
	public IEntityRecord executeUniqueSelect(ISelectQuery query) throws NonUniqueResultException {
		if (BuildConfig.DEBUG && !(query instanceof MemorySelectQuery)) {
			throw new AssertionError();
		}
		List<IEntityRecord> records = getRecords();
		if (records.size() > 1) {
			throw new NonUniqueResultException(query);
		}
		
		if (records.isEmpty()) {
			return null;
		}
		return records.get(0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IEntityRecord> executeListSelect(ISelectQuery query) {
		if (BuildConfig.DEBUG && !(query instanceof MemorySelectQuery)) {
			throw new AssertionError();
		}
		return getRecords();
	}

	@Override
	public IEntityRecordCursor executeCursorSelect(ISelectQuery query) {
		return new MemoryEntityRecordCursor(getRecords());
	}
	
	
	@Override
	public IEntityRecord create() {
		throw new UnsupportedOperationException();
	}

    @Override
    public IEntityRecord create(Serializable id) {
        throw new UnsupportedOperationException();
    }

    @Override
	public boolean save(IEntityRecord record, boolean sync) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean delete(IEntityRecord record, boolean sync) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean refresh(IEntityRecord record) {
		throw new UnsupportedOperationException();
	}
	
	@Override
         public IEntityRecord copy(IEntityRecord record) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IEntityRecord copy(IEntityRecord record, boolean fresh) {
        throw new UnsupportedOperationException();
    }

	@Override
	public Parcelable toSavedState(IEntityRecord record) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IEntityRecord fromSavedState(Parcelable savedState) {
		throw new UnsupportedOperationException();
	}
	
	private List<IEntityRecord> getRecords() {
		if (records == null) {
			assert dataView != null;
			
			//Obtém os registros da associação apenas na primeira vez que eles forem usados.
			IEntityRecord record = dataView.getRecord();
			String relName = dataView.getRelationship().getName();
			IEntityRecord[] values = record.getRelationshipArrayValue(relName);
			if (values != null) {
				records = Arrays.asList(values);
			} else {
				records = Collections.emptyList();
			}
		}
		
		return records;
	}

	
	/*
	 * Classes auxiliares
	 */
	
	/**
	 * Utilizado pelo {@link br.com.zalem.ymir.client.android.entity.data.util.MemoryEntityDAO} para prover o recurso de criação de query de seleção. Não suporta a seleção de
	 * campos específicos, filtros e ordenação.
	 */
	private class MemorySelectStament implements ISelectBuilder {

		@Override
		public ISelectQuery toQuery() {
			return new MemorySelectQuery();
		}

		@Override
		@SuppressWarnings("unchecked")
		public IEntityRecord uniqueResult() throws NonUniqueResultException {
			return executeUniqueSelect(toQuery());
		}

		@Override
		@SuppressWarnings("unchecked")
		public List<IEntityRecord> listResult() {
			return executeListSelect(toQuery());
		}

		@Override
		public IEntityRecordCursor cursorResult() {
			return executeCursorSelect(toQuery());
		}
		
		
		/*
		 * Métodos não suportados
		 */
		
		@Override
		public ISelectBuilder attribute(String... attributePath) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder relationship(String... relationshipPath) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public ISelectBuilder as(String alias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder where() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder orderBy(boolean asc, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder and() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder or() {
			throw new UnsupportedOperationException();
		}

        @Override
        public ISelectBuilder condition() {
			throw new UnsupportedOperationException();
        }

        @Override
		public ISelectBuilder c() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder not() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder o() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder eq(Object value, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder lt(Object value, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder gt(Object value, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder le(Object value, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder ge(Object value, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder in(Object[] values, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder between(Object value1, Object value2, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder contains(String text, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder startsWith(String text, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder endsWith(String text, String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder isNull(String... attrPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder rEq(Serializable id, String... relPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder rIn(Serializable[] ids, String... relPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder rIsNull(String... relPathOrAlias) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder ssEq(SyncStatus ss) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ISelectBuilder ssIn(SyncStatus... sss) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public ISelectBuilder idEq(Serializable id) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public ISelectBuilder idIn(Serializable... ids) {
			throw new UnsupportedOperationException();
		}

        @Override
        public ITerminalStatement limit(int number) {
			throw new UnsupportedOperationException();
        }
    }
	
	/**
	 * Cursor que provê a nevegação pelos registros do {@link br.com.zalem.ymir.client.android.entity.data.util.MemoryEntityDAO}.
	 */
	private static final class MemoryEntityRecordCursor implements IEntityRecordCursor {
		
		private final List<IEntityRecord> records;
		private int position = -1;
		
		public MemoryEntityRecordCursor(List<IEntityRecord> records) {
			this.records = records;
		}
		
		@Override
		public int getCount() {
			return records.size();
		}

		@Override
		public int getPosition() {
			return position;
		}
		

		@Override
		public boolean moveToPosition(int position) {
			if (position < 0) {
                this.position = -1;
				return false;
			}
			
			int size = records.size();
			if (position >= size) {
                this.position = size;
				return false;
			}
			
			this.position = position;
			return true;
		}

		@Override
		public boolean move(int offset) {
			return moveToPosition(position + offset);
		}

		@Override
		public boolean moveToFirst() {
			return moveToPosition(0);
		}

		@Override
		public boolean moveToLast() {
			return moveToPosition(records.size() - 1);
		}

		@Override
		public boolean moveToNext() {
			return moveToPosition(position + 1);
		}

		@Override
		public boolean moveToPrevious() {
			return moveToPosition(position - 1);
		}

		@Override
		public boolean isFirst() {
			return !records.isEmpty() && position == 0;
		}

		@Override
		public boolean isLast() {
			return !records.isEmpty() && position == (records.size() - 1);
		}

		@Override
		public boolean isBeforeFirst() {
			return records.isEmpty() || position == -1;
		}

		@Override
		public boolean isAfterLast() {
			return records.isEmpty() || position == records.size();
		}
		
		@Override
		public IEntityRecord getEntityRecord() {
			if (position < 0 || position >= records.size()) {
				throw new IllegalStateException("Cursor is beforeFirst or afterLast");
			}
			return records.get(position);
		}
		
		@Override
		public void close() {
		}

		@Override
		public boolean isClosed() {
			return false;
		}

		@Override
		public String[] getFieldNames() {
			return new String[0];
		}
		
		
		@Override
		public int getFieldIndex(String fieldName) {
			throw new IllegalArgumentException();
		}

		@Override
		public String getFieldName(int fieldIndex) {
			throw new IllegalArgumentException();
		}

		@Override
		public boolean isNull(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Integer getIntegerValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Double getDecimalValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getTextValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Boolean getBooleanValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Date getDateValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Time getTimeValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

        @Override
        public Timestamp getDatetimeValue(int fieldIndex) {
			throw new UnsupportedOperationException();
        }

        @Override
		public Character getCharacterValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public IEntityRecord getRelationshipValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getValue(int fieldIndex) {
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Query de seleção específica do {@link br.com.zalem.ymir.client.android.entity.data.util.MemoryEntityDAO}.<br>
	 * Seleciona todos os registros, de forma completa.
	 */
	private static final class MemorySelectQuery implements ISelectQuery {
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}

		@Override
		public ISelectField[] getFields() {
			return new ISelectField[0];
		}

		@Override
		public <T> T uniqueResult(IEntityDAO dao) throws NonUniqueResultException {
			return dao.executeUniqueSelect(this);
		}

		@Override
		public <T> List<T> listResult(IEntityDAO dao) {
			return dao.executeListSelect(this);
		}

		@Override
		public IEntityRecordCursor cursorResult(IEntityDAO dao) {
			return dao.executeCursorSelect(this);
		}
		
		@SuppressWarnings("unused")
		public static final Creator<MemorySelectQuery> CREATOR = new Creator<MemorySelectQuery>() {
			@Override
			public MemorySelectQuery createFromParcel(Parcel source) {
				return new MemorySelectQuery();
			}
			
			@Override
			public MemorySelectQuery[] newArray(int size) {
				return new MemorySelectQuery[size];
			}
		};
	}
}
