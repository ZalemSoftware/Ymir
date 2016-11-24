package br.com.zalem.ymir.client.android.entity.data.openmobster.query;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.AndroidBugsUtils;

import java.util.List;
import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.cursor.IEntityRecordCursor;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectQuery;
import br.com.zalem.ymir.client.android.entity.data.query.select.NonUniqueResultException;

/**
 * Query de seleção de dados baseado no OpenMobster.
 * 
 * @see MobileBeanQueryBuilder
 * 
 * @author Thiago Gesser
 */
public class MobileBeanSelectQuery implements ISelectQuery {

	private final String query;
	private final String[] parameters;
	private final SelectField[] fields;
	private final String entityName;

	public MobileBeanSelectQuery(String query, String[] parameters, SelectField[] fields, String entityName) {
		this.query = query;
		this.parameters = parameters;
		this.fields = fields;
		this.entityName = entityName; 
	}
	
	@Override
	public final ISelectField[] getFields() {
		return fields;
	}
	
	@Override
	public final <T> T uniqueResult(IEntityDAO dao) throws NonUniqueResultException {
		return dao.executeUniqueSelect(this);
	}

	@Override
	public final <T> List<T> listResult(IEntityDAO dao) {
		return dao.executeListSelect(this);
	}

	@Override
	public final IEntityRecordCursor cursorResult(IEntityDAO dao) {
		return dao.executeCursorSelect(this);
	}

	public final String getQuery() {
		return query;
	}
	
	public final String[] getParameters() {
		return parameters;
	}
	
	public final String getEntityName() {
		return entityName;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(query);
		dest.writeStringArray(parameters);
		dest.writeTypedArray(fields, flags);
		dest.writeString(entityName);
	}
	
	public static final Creator<MobileBeanSelectQuery> CREATOR = new Creator<MobileBeanSelectQuery>() {
		@Override
		public MobileBeanSelectQuery createFromParcel(Parcel source) {
			return new MobileBeanSelectQuery(source.readString(),
											 source.createStringArray(),
											 source.createTypedArray(SelectField.CREATOR),
											 source.readString());
		}
		
		@Override
		public MobileBeanSelectQuery[] newArray(int size) {
			return new MobileBeanSelectQuery[size];
		}
	};
	
	
	/*
	 * Classes auxiliares.
	 */
	
	/**
	 * Representa um campo selecionado da query.
	 */
	static final class SelectField implements ISelectField, Parcelable {
		
		private final String[] path;
		private final boolean isRelationship;
		private final String alias;
		
		SelectField(String[] path, String alias, boolean isRelationship) {
			this.path = path;
			this.alias = alias;
			this.isRelationship = isRelationship;
		}

		@Override
		public String[] getPath() {
			return path;
		}
		
		@Override
		public String getAlias() {
			return alias;
		}
		
		@Override
		public boolean isRelationship() {
			return isRelationship;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeStringArray(path);
			dest.writeString(alias);
			AndroidBugsUtils.applyWorkaroundForBug5973_write(dest, isRelationship);
		}
		
		public static final Creator<SelectField> CREATOR = new Creator<SelectField>() {
			@Override
			public SelectField createFromParcel(Parcel source) {
				return new SelectField(source.createStringArray(), source.readString(), AndroidBugsUtils.applyWorkaroundForBug5973_read(source));
			}
			
			@Override
			public SelectField[] newArray(int size) {
				return new SelectField[size];
			}
		};
	}
}
