package br.com.zalem.ymir.client.android.entity.data.openmobster.query;

import android.os.Parcel;

import br.com.zalem.ymir.client.android.entity.data.openmobster.util.InternalMobileBeanEntityRecordSerializer;


/**
 * Query de seleção de registros de um relacionamento interno a partir de um registro dono.
 *
 * @author Thiago Gesser
 */
public final class InternalMobileBeanSelectQuery extends MobileBeanSelectQuery {

	private final String ownerId;
	private final String ownerEntityName;
	private final String relFullname;

	/**
	 * Cria um InternalMobileBeanSelectQuery.
	 * 
	 * @param query query no formato SQL.
	 * @param parameters parâmetros utilizados na query.
	 * @param fields campos selecionados na query.
	 * @param entityName nome da entidade alvo da query.
	 * @param ownerId identificador do registro dono dos registros internos.
	 * @param ownerEntityName nome da entidade do registro dono.
	 * @param relFullname caminho completo até o array (relacionamento) que contém os registros internos, seguindo o {@link InternalMobileBeanEntityRecordSerializer formato do OpenMobster}.
	 */
	public InternalMobileBeanSelectQuery(String query, String[] parameters, SelectField[] fields, String entityName,
									 	 String ownerId, String ownerEntityName, String relFullname) {
		super(query, parameters, fields, entityName);
		this.ownerId = ownerId;
		this.ownerEntityName = ownerEntityName;
		this.relFullname = relFullname;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getOwnerEntityName() {
		return ownerEntityName;
	}
	
	public String getRelFullname() {
		return relFullname;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(ownerId);
		dest.writeString(ownerEntityName);
		dest.writeString(relFullname);
	}
	
	public static final Creator<InternalMobileBeanSelectQuery> CREATOR = new Creator<InternalMobileBeanSelectQuery>() {
		@Override
		public InternalMobileBeanSelectQuery createFromParcel(Parcel source) {
			return new InternalMobileBeanSelectQuery(source.readString(),
													 source.createStringArray(),
													 source.createTypedArray(SelectField.CREATOR),
													 source.readString(),
													 source.readString(),
													 source.readString(),
													 source.readString());
		}
		
		@Override
		public InternalMobileBeanSelectQuery[] newArray(int size) {
			return new InternalMobileBeanSelectQuery[size];
		}
	};
}
