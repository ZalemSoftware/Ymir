package br.com.zalem.ymir.client.android.entity.ui.event.basic;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityEditingErrorHandler;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityUIEventManager;

/**
 * Gerenciador básico de eventos.<br>
 * Permite a definição de listeners de eventos da aplicação específicos para determinadas entidades.
 * Atualmente só é suportado um listener para cada entidade.<br>
 *
 * @see IEntityUIEventListener
 *
 * @author Thiago Gesser
 */
public final class BasicEntityUIEventManager implements IEntityUIEventManager {

	private final Map<String, IEntityUIEventListener> listenersMap;
	
	public BasicEntityUIEventManager(IEntityUIEventListener... listeners) {
		if (listeners.length == 0) {
			listenersMap = Collections.emptyMap();
		} else {
			listenersMap = new HashMap<>(listeners.length);
			
			for (IEntityUIEventListener listener : listeners) {
				boolean alreadyAdded = listenersMap.put(listener.getEntityName(), listener) != null;
				if (alreadyAdded) {
					throw new IllegalArgumentException("There are two or more listeners pointing to the same entity: " + listener.getEntityName());
				}
			}
		}
	}

	@Override
	public void fireStartEditRecordEvent(IEntityRecord record, IEntityEditingErrorHandler msgHandler) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.onStartEditRecord(record, msgHandler);
	}

	@Override
	public void fireEditRecordAttributeEvent(IEntityRecord record, String attributeName, IEntityEditingErrorHandler msgHandler) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.onEditRecordAttribute(record, attributeName, msgHandler);
	}

	@Override
	public void fireEditRecordRelationshipEvent(IEntityRecord record, String relationshipName, IEntityEditingErrorHandler msgHandler) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.onEditRecordRelationship(record, relationshipName, msgHandler);
	}
	
	@Override
	public List<?> fireBeforeListEnumValuesEvent(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler msgHandler) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return null;
		}
		return listener.beforeListEnumValues(record, fieldName, values, msgHandler);
	}

	@Override
	public void fireAfterListEnumValuesEvent(IEntityRecord record, String fieldName, List<?> values, IEntityEditingErrorHandler msgHandler) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.afterListEnumValues(record, fieldName, values, msgHandler);
	}

	@Override
	public boolean fireBeforeSaveRecordEvent(IEntityRecord record, boolean sync, IEntityEditingErrorHandler msgHandler) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return false;
		}
		return listener.beforeSaveRecord(record, sync, msgHandler);
	}
	
	@Override
	public void fireSaveRecordEvent(IEntityRecord record, boolean sync) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.onSaveRecord(record, sync);
	}

	@Override
	public void fireAfterSaveRecordEvent(IEntityRecord record, boolean sync) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.afterSaveRecord(record, sync);
	}
	
	@Override
	public boolean fireBeforeConfirmEditRecordEvent(IEntityRecord record, IEntityEditingErrorHandler msgHandler) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return false;
		}
		return listener.beforeConfirmEditRecord(record, msgHandler);
	}
	
	@Override
	public void fireConfirmEditRecordEvent(IEntityRecord record) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.onConfirmEditRecord(record);
	}
	
	@Override
	public void fireAfterConfirmEditRecordEvent(IEntityRecord record) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.afterConfirmEditRecord(record);
	}

	@Override
	public boolean fireBeforeDeleteRecordEvent(IEntityRecord record) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return false;
		}
		return listener.beforeDeleteRecord(record);
	}

	@Override
    public void fireAfterDeleteRecordEvent(IEntityRecord record) {
        IEntityUIEventListener listener = getListener(record);
        if (listener == null) {
            return;
        }
        listener.afterDeleteRecord(record);
    }

    @Override
	public List<IEntityRecord> fireBeforeListRecordsEvent(String entityName, List<IEntityRecord> records) {
		IEntityUIEventListener listener = listenersMap.get(entityName);
		if (listener == null) {
			return null;
		}
		return listener.beforeListRecords(records);
	}
	
	@Override
	public void fireAfterListRecordsEvent(String entityName, List<IEntityRecord> records) {
		IEntityUIEventListener listener = listenersMap.get(entityName);
		if (listener == null) {
			return;
		}
		listener.afterListRecords(records);
	}

	@Override
	public void fireDetailRecordEvent(IEntityRecord record) {
		IEntityUIEventListener listener = getListener(record);
		if (listener == null) {
			return;
		}
		listener.onDetailRecord(record);
	}
	
	
	/*
	 * Métodos auxiliares
	 */
	
	private IEntityUIEventListener getListener(IEntityRecord record) {
		String entityName = record.getEntityMetadata().getName();
		return listenersMap.get(entityName);
	}
}
