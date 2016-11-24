package br.com.zalem.ymir.client.android.entity.ui.event.basic;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;


/**
 * Adaptador do listener de eventos de entidade que permite à subclasse implementar apenas os métodos que deseja.
 * 
 * @author Thiago Gesser
 */
public abstract class EntityUIEventListenerAdapter implements IEntityUIEventListener {

	@Override
	public void onStartEditRecord(IEntityRecord record, IEntityEditingErrorHandler errorHandler) {
	}

	@Override
	public void onEditRecordAttribute(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler) {
	}

	@Override
	public void onEditRecordRelationship(IEntityRecord record, String relationshipName,	IEntityEditingErrorHandler errorHandler) {
	}

	@Override
	public List<?> beforeListEnumValues(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler errorHandler) {
		return null;
	}
	
	@Override
	public void afterListEnumValues(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler errorHandler) {
	}
	
	@Override
	public boolean beforeSaveRecord(IEntityRecord record, boolean sync, IEntityEditingErrorHandler errorHandler) {
		return false;
	}

	@Override
	public void onSaveRecord(IEntityRecord record, boolean sync) {
	}
	
	@Override
	public void afterSaveRecord(IEntityRecord record, boolean sync) {
	}

	@Override
	public boolean beforeConfirmEditRecord(IEntityRecord record, IEntityEditingErrorHandler errorHandler) {
		return false;
	}
	
	@Override
	public void onConfirmEditRecord(IEntityRecord record) {
	}

	@Override
	public void afterConfirmEditRecord(IEntityRecord record) {
	}

	@Override
	public boolean beforeDeleteRecord(IEntityRecord record) {
		return false;
	}

	@Override
    public void afterDeleteRecord(IEntityRecord record) {
    }

    @Override
	public List<IEntityRecord> beforeListRecords(List<IEntityRecord> records) {
		return null;
	}
	
	@Override
	public void afterListRecords(List<IEntityRecord> records) {
	}

	@Override
	public void onDetailRecord(IEntityRecord record) {
	}
}
