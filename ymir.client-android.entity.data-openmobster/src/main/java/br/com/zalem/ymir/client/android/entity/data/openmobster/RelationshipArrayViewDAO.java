package br.com.zalem.ymir.client.android.entity.data.openmobster;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.RelationshipViolationException;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.SelectFromQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.select.ISelectBuilder;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;

/**
 * Provê a manipulação de registros de uma <b>visão limitada</b> dos dados de uma entidade baseada no {@link org.openmobster.android.api.sync.MobileBean} do OpenMobster.<br>
 * A visão é limitada aos registros provenientes de um relacionamento de tipo <code>array</code> de uma entidade. Esta
 * visão é definida através da classe {@link RelationshipArrayView}.
 *
 * @author Thiago Gesser
 */
public final class RelationshipArrayViewDAO extends MobileBeanEntityDAO {
	
	private final RelationshipArrayView dataView;

	RelationshipArrayViewDAO(RelationshipArrayView dataView, MobileBeanEntityDataManager entityManager) {
		super((EntityMetadata) dataView.getRelationship().getTarget(), entityManager);
		
		this.dataView = dataView;
	}

	@Override
	public boolean save(IEntityRecord record, boolean sync) {
		//Verifica se é interno antes pois o "checkRecordOrigin" não funciona para este tipo de registro. Desta forma, o erro correto será lançado.
		checkNotInternal();
		checkRecordOrigin(record);
		
		return super.save(record, sync);
	}
	
	@Override
	public boolean delete(IEntityRecord record, boolean sync) throws RelationshipViolationException {
		checkNotInternal();
		checkRecordOrigin(record);
		
		return super.delete(record, sync);
	}
	
	@Override
	public boolean refresh(IEntityRecord record) {
		//O "checkRecordOrigin" não funciona para registros internos.
		if (!metadata.isInternal()) {
			checkRecordOrigin(record);
		}
		
		return super.refresh(record);
	}
	
	@Override
	public MobileBeanEntityRecord get(Serializable id) {
		checkIsReady();
		checkNotInternal();
		
		MobileBeanEntityRecord[] viewRecords = getViewRecords();
		if (viewRecords == null) {
			return null;
		}
		
		for (MobileBeanEntityRecord viewRecord : viewRecords) {
			if (id.equals(viewRecord.getId())) {
				return viewRecord;
			}
		}
		
		return null;
	}
	
	@Override
	public List<IEntityRecord> getAll() {
		checkIsReady();
		checkNotInternal();
		
		IEntityRecord[] viewRecords = getViewRecords();
		if (viewRecords == null) {
			return Collections.emptyList();
		}
		
		return Arrays.asList(viewRecords);
	}

    @Override
    public boolean isEmpty() {
        IEntityRecord[] viewRecords = getViewRecords();
        return viewRecords == null || viewRecords.length == 0;
    }

    @Override
    public ISelectBuilder newSelectBuilder(boolean distinct) {
        checkNotInternal();

        //Retorna um builder específico para a visão de dados.
        return new SelectFromQueryBuilder(entityManager, dataView, distinct);
    }


    /*
	 * Métodos auxiliares
	 */
	
	private MobileBeanEntityRecord[] getViewRecords() {
		MobileBeanEntityRecord record = (MobileBeanEntityRecord) dataView.getRecord();
		return record.getRelationshipArrayValue(dataView.getRelationship().getName());
	}
	
	private void checkRecordOrigin(IEntityRecord record) {
		//Verifica se o registro faz parte desta visão de dados, pois só permite utilizar estes registros.
		Serializable id = record.getId();
		IEntityRecord[] viewRecords = getViewRecords();
		if (viewRecords != null) {
			for (IEntityRecord viewRecord : viewRecords) {
				if ((id == null)) { 
					//Se o id é null, a mesma instância do registro deve estar presente nesta visão.
					if (record == viewRecord) {
						return; 
					}
				} else if (id.equals(viewRecord.getId())) {
					return;
				}
			}
		}
			
		throw new IllegalArgumentException("The IEntityRecord does not belong to this data view: " + id);
	}
}
