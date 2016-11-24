package br.com.zalem.ymir.client.android.entity.ui.perspective;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.SyncStatus;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.BooleanFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DateFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DecimalFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.EnumAttributeEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.ImageFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.IntegerFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TextFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TimeFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.EnumRelationshipEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.MultipleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.SingleRelationshipFieldEditor;

/**
 * Engloba um registro que está em edição e faz com que os valores dos campos editados sejam obtidos / definidos
 * diretamente nos editores.<br>
 * Apenas os campos que estão sendo editados podem ter seus valores alterados. Entretanto, estes valores ainda podem ser
 * obtidos, o que será feito diretamente do registro interno.<br>
 * <br>
 * O {@link IEntityMetadata} deste registro é o metadado da entidade do registro interno acrescido com os metadados
 * dos atributos virtuais de edição.
 *
 * @see AbstractFieldEditor
 *
 * @author Thiago Gesser
 */
final class EntityEditingRecord implements IEntityRecord {

	private final IEntityRecord innerRecord;
	private Map<String, AbstractFieldEditor<?>> attributesEditors;
	private Map<String, AbstractFieldEditor<?>> relationshipsEditors;
	private IEntityRecord masterRecord;
	private String masterRelationship;
	private IEntityMetadata entityMetadata;
	private boolean enableInnerChanges;
	
	
	EntityEditingRecord(IEntityRecord innerRecord) {
		this(innerRecord, null, null);
	}
	
	EntityEditingRecord(IEntityRecord innerRecord, Map<String, AbstractFieldEditor<?>> attributesEditors, Map<String, AbstractFieldEditor<?>> relationshipsEditors) {
		this.innerRecord = innerRecord;
		setEditors(attributesEditors, relationshipsEditors);
	}
	
	@Override
	public IEntityMetadata getEntityMetadata() {
		if (entityMetadata == null) {
			//Mapeia os atributos virtuais, pois eles deverão fazer parte do metadata também.
			Map<String, VirtualAttributeMetadata> virtualAttributes = new HashMap<>();
			Map<String, VirtualRelationshipMetadata> virtualRelationships= new HashMap<>();
			VirtualAttributesMappingVisitor mappingVisitor = new VirtualAttributesMappingVisitor(virtualAttributes, virtualRelationships);
			for (AbstractFieldEditor<?> fieldEditor : attributesEditors.values()) {
				fieldEditor.accept(mappingVisitor);
			}
			for (AbstractFieldEditor<?> fieldEditor : relationshipsEditors.values()) {
				fieldEditor.accept(mappingVisitor);
			}
			
			//Se não há campos virtuais, pode usar o metadata normal. Se não, usa um metadata com os campos virtuais juntos.
			if (virtualAttributes.isEmpty() && virtualRelationships.isEmpty()) {
				entityMetadata = innerRecord.getEntityMetadata();
			} else {
				entityMetadata = new EntityEditingMetadata(innerRecord.getEntityMetadata(), virtualAttributes, virtualRelationships);
			}
		}
		
		return entityMetadata;
	}

	@Override
	public Serializable getId() {
		return innerRecord.getId();
	}

	@Override
	public SyncStatus getSyncStatus() {
		return innerRecord.getSyncStatus();
	}

	@Override
	public boolean isLocal() {
		return innerRecord.isLocal();
	}

	@Override
	public boolean isNew() {
		return innerRecord.isNew();
	}
	
	@Override
	public boolean isDeleted() {
		return innerRecord.isDeleted();
	}

	@Override
	public boolean isDirty() {
		for (AbstractFieldEditor<?> editor : attributesEditors.values()) {
			if (editor.isDirty()) {
				return true;
			}
		}
		for (AbstractFieldEditor<?> editor : relationshipsEditors.values()) {
			if (editor.isDirty()) {
				return true;
			}
		}
		
		return innerRecord.isDirty();
	}

	@Override
	public boolean isDirty(String fieldName) {
		AbstractFieldEditor<?> editor = attributesEditors.get(fieldName);
		if (editor != null) {
			return editor.isDirty();
		}
		editor = relationshipsEditors.get(fieldName);
		if (editor != null) {
			return editor.isDirty();
		}
		
		return innerRecord.isDirty(fieldName);
	}
	
	@Override
	public boolean isNull(String fieldName) {
		AbstractFieldEditor<?> editor = attributesEditors.get(fieldName);
		if (editor != null) {
			return editor.getValue() == null;
		}
		editor = relationshipsEditors.get(fieldName);
		if (editor != null) {
			return editor.getValue() == null;
		}
		
		return innerRecord.isNull(fieldName);
	}
	
	
	/*
	 * Métodos de obtenção de valores
	 */

    @Override
    public Object getAttributeValue(String attribute) {
        AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
        if (editor != null) {
            return editor.getValue();
        }

        return innerRecord.getAttributeValue(attribute);
    }

    @Override
	public Integer getIntegerValue(String attribute) {
		AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
		if (editor != null) {
			return (Integer) editor.getValue();
		}
		
		return innerRecord.getIntegerValue(attribute);
	}

	@Override
	public Double getDecimalValue(String attribute) {
		AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
		if (editor != null) {
			return (Double) editor.getValue();
		}
		
		return innerRecord.getDecimalValue(attribute);
	}

	@Override
	public String getTextValue(String attribute) {
		AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
		if (editor != null) {
			return (String) editor.getValue();
		}
		
		return innerRecord.getTextValue(attribute);
	}
	
	@Override
	public Boolean getBooleanValue(String attribute) {
		AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
		if (editor != null) {
			return (Boolean) editor.getValue();
		}
		
		return innerRecord.getBooleanValue(attribute);
	}

	@Override
	public Date getDateValue(String attribute) {
		AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
		if (editor != null) {
			return (Date) editor.getValue();
		}
		
		return innerRecord.getDateValue(attribute);
	}

	@Override
	public Time getTimeValue(String attribute) {
		AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
		if (editor != null) {
			return (Time) editor.getValue();
		}
		
		return innerRecord.getTimeValue(attribute);
	}

    @Override
    public Timestamp getDatetimeValue(String attribute) {
        //Por enquanto só suporta editor enum de Datetime - TO DO #-6.
        AbstractFieldEditor<?> editor = attributesEditors.get(attribute);
        if (editor != null) {
            return (Timestamp) editor.getValue();
        }

		return innerRecord.getDatetimeValue(attribute);
    }

    @Override
	public Character getCharacterValue(String attribute) {
		return innerRecord.getCharacterValue(attribute);
	}

	@Override
	public Bitmap getImageValue(String attribute) {
		return innerRecord.getImageValue(attribute);
	}
	
	@Override
	public Bitmap getImageValue(String attribute, boolean fromCache) {
		return innerRecord.getImageValue(attribute, fromCache);
	}

	@Override
	public IEntityRecord getRelationshipValue(String relationship) {
		if (relationship.equals(masterRelationship)) {
			return masterRecord;
		}

        AbstractFieldEditor<?> editor = relationshipsEditors.get(relationship);
		if (editor != null) {
			return (IEntityRecord) editor.getValue();
		}
		
		return innerRecord.getRelationshipValue(relationship);
	}

    @Override
    public Object[] getAttributeArrayValue(String attribute) {
        return innerRecord.getAttributeArrayValue(attribute);
    }

	@Override
	public Integer[] getIntegerArrayValue(String attribute) {
		return innerRecord.getIntegerArrayValue(attribute);
	}

	@Override
	public Double[] getDecimalArrayValue(String attribute) {
		return innerRecord.getDecimalArrayValue(attribute);
	}

	@Override
	public String[] getTextArrayValue(String attribute) {
		return innerRecord.getTextArrayValue(attribute);
	}

	@Override
	public Boolean[] getBooleanArrayValue(String attribute) {
		return innerRecord.getBooleanArrayValue(attribute);
	}

	@Override
	public Date[] getDateArrayValue(String attribute) {
		return innerRecord.getDateArrayValue(attribute);
	}

	@Override
	public Time[] getTimeArrayValue(String attribute) {
		return innerRecord.getTimeArrayValue(attribute);
	}

    @Override
    public Timestamp[] getDatetimeArrayValue(String attribute) {
        return innerRecord.getDatetimeArrayValue(attribute);
    }

    @Override
	public Character[] getCharacterArrayValue(String attribute) {
		return innerRecord.getCharacterArrayValue(attribute);
	}

	@Override
	public Bitmap[] getImageArrayValue(String attribute) {
		return innerRecord.getImageArrayValue(attribute);
	}
	
	@Override
	public Bitmap[] getImageArrayValue(String attribute, boolean fromCache) {
		return innerRecord.getImageArrayValue(attribute, fromCache);
	}

	@Override
	public IEntityRecord[] getRelationshipArrayValue(String relationship) {
		MultipleRelationshipFieldEditor editor = (MultipleRelationshipFieldEditor) relationshipsEditors.get(relationship);
		if (editor != null) {
			List<IEntityRecord> records = editor.getValue();
			if (records == null) {
				return null;
			}
			
			return records.toArray(new IEntityRecord[records.size()]);
		}
		
		return innerRecord.getRelationshipArrayValue(relationship);
	}
	
	
	/*
	 * Métodos de definição de valores
	 */

    @Override
    @SuppressWarnings("unchecked")
    public void setAttributeValue(String attribute, Object value) {
        AbstractFieldEditor editor = getEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
        	editor.setValue(value);
            return;
		}

		innerRecord.setAttributeValue(attribute, value);
    }

    @Override
	public void setIntegerValue(String attribute, Integer value) {
		AbstractFieldEditor<Integer> editor = getGenericEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setIntegerValue(attribute, value);
	}

	@Override
	public void setDecimalValue(String attribute, Double value) {
		AbstractFieldEditor<Double> editor = getGenericEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setDecimalValue(attribute, value);
	}

	@Override
	public void setTextValue(String attribute, String value) {
		AbstractFieldEditor<String> editor = getGenericEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setTextValue(attribute, value);
	}

	@Override
	public void setBooleanValue(String attribute, Boolean value) {
		AbstractFieldEditor<Boolean> editor = getGenericEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setBooleanValue(attribute, value);
	}

	@Override
	public void setDateValue(String attribute, Date value) {
		AbstractFieldEditor<Date> editor = getGenericEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setDateValue(attribute, value);
	}

	@Override
	public void setTimeValue(String attribute, Time value) {
		AbstractFieldEditor<Time> editor = getGenericEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setTimeValue(attribute, value);
	}

    @Override
    public void setDatetimeValue(String attribute, Timestamp value) {
        AbstractFieldEditor<Timestamp> editor = getGenericEditorForChanges(attribute, attributesEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setDatetimeValue(attribute, value);
    }

    @Override
	public void setCharacterValue(String attribute, Character value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setCharacterValue(attribute, value);
	}

	@Override
	public void setImageValue(String attribute, Bitmap value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setImageValue(attribute, value);
	}

	@Override
	public void setRelationshipValue(String relationship, IEntityRecord value) {
		if (relationship.equals(masterRelationship)) {
			throw new IllegalArgumentException("The master record relationship cannot be changed: " + relationship);
		}

        AbstractFieldEditor<IEntityRecord> editor = getGenericEditorForChanges(relationship, relationshipsEditors);
		if (editor != null) {
			editor.setValue(value);
            return;
		}

		innerRecord.setRelationshipValue(relationship, value);
	}

    @Override
    public void setAttributeArrayValue(String attribute, Object[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setAttributeArrayValue(attribute, value);
    }

    @Override
	public void setIntegerArrayValue(String attribute, Integer[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setIntegerArrayValue(attribute, value);
	}

	@Override
	public void setDecimalArrayValue(String attribute, Double[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setDecimalArrayValue(attribute, value);
	}

	@Override
	public void setTextArrayValue(String attribute, String[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setTextArrayValue(attribute, value);
	}

	@Override
	public void setBooleanArrayValue(String attribute, Boolean[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setBooleanArrayValue(attribute, value);
	}

	@Override
	public void setDateArrayValue(String attribute, Date[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setDateArrayValue(attribute, value);
	}

	@Override
	public void setTimeArrayValue(String attribute, Time[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setTimeArrayValue(attribute, value);
	}

    @Override
    public void setDatetimeArrayValue(String attribute, Timestamp[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setDatetimeArrayValue(attribute, value);
    }

    @Override
	public void setCharacterArrayValue(String attribute, Character[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setCharacterArrayValue(attribute, value);
	}

	@Override
	public void setImageArrayValue(String attribute, Bitmap[] value) {
		checkUnsupportedEditor(attribute);

		innerRecord.setImageArrayValue(attribute, value);
	}

	@Override
	public void setRelationshipArrayValue(String relationship, IEntityRecord[] value) {
		MultipleRelationshipFieldEditor editor = (MultipleRelationshipFieldEditor) getEditorForChanges(relationship, relationshipsEditors);
		if (editor != null) {
			if (value == null || value.length == 0) {
				editor.setValue(null);
			} else {
				List<IEntityRecord> records = new ArrayList<>();
				Collections.addAll(records, value);
				editor.setValue(records);
			}
            return;
		}

		innerRecord.setRelationshipArrayValue(relationship, value);
	}

	@Override
	public void addRelationshipValue(String relationship, IEntityRecord value) {
		MultipleRelationshipFieldEditor editor = (MultipleRelationshipFieldEditor) getEditorForChanges(relationship, relationshipsEditors);
		if (editor != null) {
			editor.addRecord(value);
            return;
		}

		innerRecord.addRelationshipValue(relationship, value);
	}

	@Override
	public void removeRelationshipValue(String relationship, IEntityRecord value) {
		MultipleRelationshipFieldEditor editor = (MultipleRelationshipFieldEditor) getEditorForChanges(relationship, relationshipsEditors);
		if (editor != null) {
			editor.removeRecord(value);
            return;
		}

		innerRecord.removeRelationshipValue(relationship, value);
	}
	
	
	/*
	 * Métodos internos para uso da própria estrutura de edição. 
	 */
	
	/**
	 * Define o registro mestre deste registro em edição para que ele possa ser disponibilizado através de seu relacionamento.
	 * 
	 * @param masterRecord o registro mestre.
	 * @param masterRelationship o relacionamento que aponta para o registro mestre.
	 */
	void setMasterRecord(IEntityRecord masterRecord, String masterRelationship) {
		if (masterRecord == null || masterRelationship == null) {
			throw new IllegalArgumentException("masterRecord == null || masterRelationship == null");
		}
		
		//Garante que não há editor declarado para o relacionamento do mestre.
		if (relationshipsEditors.containsKey(masterRelationship)) {
			throw new IllegalArgumentException("The master record relationship cannot be declared as an editor: " + masterRelationship);
		}
		
		this.masterRecord = masterRecord;
		this.masterRelationship = masterRelationship;
	}
	
	/**
	 * Define os editores de atributos e relacionamentos que estão sendo utilizados na edição do registro.
	 * 
	 * @param attributesEditors editores de atributos.
	 * @param relationshipsEditors editores de relacionamentos.
	 */
	void setEditors(Map<String, AbstractFieldEditor<?>> attributesEditors, Map<String, AbstractFieldEditor<?>> relationshipsEditors) {
		this.attributesEditors = attributesEditors == null ? Collections.<String, AbstractFieldEditor<?>>emptyMap() : attributesEditors;
		this.relationshipsEditors = relationshipsEditors == null ? Collections.<String, AbstractFieldEditor<?>>emptyMap() : relationshipsEditors;
		entityMetadata = null;
	}

	/**
	 * Habilita alterações no registro interno.
	 */
	void enableInnerChanges() {
		enableInnerChanges = true;
	}

	/**
	 * Desabilita alterações no registro interno.
	 */
	void disableInnerChanges() {
		enableInnerChanges = false;
	}

	IEntityRecord getMasterRecord() {
		return masterRecord;
	}

	String getMasterRelationship() {
		return masterRelationship;
	}

	
	/*
	 * Métodos auxiliares 
	 */
	
	private AbstractFieldEditor<?> getEditorForChanges(String fieldName, Map<String, AbstractFieldEditor<?>> editors) {
		AbstractFieldEditor<?> editor = editors.get(fieldName);
		if (!enableInnerChanges && editor == null) {
			throw new IllegalArgumentException("Only fields that were explicitly declared for editing may be changed this way. Field name = " + fieldName);
		}
		return editor;
	}
	
	@SuppressWarnings("unchecked")
	private <T> AbstractFieldEditor<T> getGenericEditorForChanges(String fieldName, Map<String, AbstractFieldEditor<?>> editors) {
		return (AbstractFieldEditor<T>) getEditorForChanges(fieldName, editors);
	}

	private void checkUnsupportedEditor(String fieldName) {
		if (!enableInnerChanges) {
			throwUnsupportedEditorException(fieldName);
		}
	}

	private static void throwUnsupportedEditorException(String fieldName) {
		throw new UnsupportedOperationException("Only changes to editable fields are allowed and this field has no editor implemented yet. Field name = " + fieldName);
	}
	
	
	/*
	 * Classes auxiliares
	 */

	/**
	 * Engloba os metadados de uma entidade e acresce os metadados de atributos virtuais. 
	 */
	private static final class EntityEditingMetadata implements IEntityMetadata {

		private final IEntityMetadata innerMetadata;
		private final Map<String, VirtualAttributeMetadata> virtualAttributes;
		private final Map<String, VirtualRelationshipMetadata> virtualRelationships;

		public EntityEditingMetadata(IEntityMetadata innerMetadata, Map<String, VirtualAttributeMetadata> virtualAttributes, Map<String, VirtualRelationshipMetadata> virtualRelationships) {
			this.innerMetadata = innerMetadata;
			this.virtualAttributes = virtualAttributes;
			this.virtualRelationships = virtualRelationships;

            for (VirtualRelationshipMetadata vr : virtualRelationships.values()) {
                vr.setSource(this);
            }
		}
		
		@Override
		public String getName() {
			return innerMetadata.getName();
		}

		@Override
		public IEntityAttribute getAttribute(String name) {
			VirtualAttributeMetadata virtualAttribtue = virtualAttributes.get(name);
			if (virtualAttribtue != null) {
				return virtualAttribtue;
			}
			
			return innerMetadata.getAttribute(name);
		}

		@Override
		public IEntityAttribute[] getAttributes() {
			IEntityAttribute[] entityAttributes = innerMetadata.getAttributes();
			
			//Coloca os atributos normais da entidade.
			IEntityAttribute[] allAttributes = new IEntityAttribute[entityAttributes.length + virtualAttributes.size()];
			System.arraycopy(entityAttributes, 0, allAttributes, 0, entityAttributes.length);
			
			
			//Coloca os atributos virtuais.
			int i = entityAttributes.length;
			for (VirtualAttributeMetadata va : virtualAttributes.values()) {
				allAttributes[i++] = va;
			}

			return allAttributes;
		}

		@Override
		public IEntityRelationship getRelationship(String name) {
			VirtualRelationshipMetadata virtualRelationship = virtualRelationships.get(name);
			if (virtualRelationship != null) {
				return virtualRelationship;
			}
			
			return innerMetadata.getRelationship(name);
		}

		@Override
		public IEntityRelationship[] getRelationships() {
			IEntityRelationship[] entityRelationships = innerMetadata.getRelationships();
			
			//Coloca os atributos normais da entidade.
			IEntityRelationship[] allRelationships = new IEntityRelationship[entityRelationships.length + virtualRelationships.size()];
			System.arraycopy(entityRelationships, 0, allRelationships, 0, entityRelationships.length);
			
			//Coloca os atributos virtuais.
			int i = entityRelationships.length;
			for (VirtualRelationshipMetadata vr : virtualRelationships.values()) {
				allRelationships[i++] = vr;
			}
			
			return allRelationships;
		}
	}
	
	/**
	 * Visitador que mapeia os atributos virtuais.
	 */
	private static final class VirtualAttributesMappingVisitor implements IFieldEditorVisitor {

		private final Map<String, VirtualAttributeMetadata> virtualAttributes;
		private final Map<String, VirtualRelationshipMetadata> virtualRelationships;

		public VirtualAttributesMappingVisitor(Map<String, VirtualAttributeMetadata> virtualAttributes, Map<String, VirtualRelationshipMetadata> virtualRelationships) {
			this.virtualAttributes = virtualAttributes;
			this.virtualRelationships = virtualRelationships;
		}
		
		@Override
		public boolean visit(IntegerFieldEditor editor) {
			tryPutVirtualAttribute(editor, EntityAttributeType.INTEGER);
			return false;
		}

		@Override
		public boolean visit(DecimalFieldEditor editor) {
			tryPutVirtualAttribute(editor, EntityAttributeType.DECIMAL);
			return false;
		}

		@Override
		public boolean visit(TextFieldEditor editor) {
			tryPutVirtualAttribute(editor, EntityAttributeType.TEXT);
			return false;
		}

		@Override
		public boolean visit(BooleanFieldEditor editor) {
			tryPutVirtualAttribute(editor, EntityAttributeType.BOOLEAN);
			return false;
		}

		@Override
		public boolean visit(DateFieldEditor editor) {
			tryPutVirtualAttribute(editor, EntityAttributeType.DATE);
			return false;
		}

		@Override
		public boolean visit(TimeFieldEditor editor) {
			tryPutVirtualAttribute(editor, EntityAttributeType.TIME);
			return false;
		}

		@Override
		public boolean visit(ImageFieldEditor editor) {
			tryPutVirtualAttribute(editor, EntityAttributeType.IMAGE);
			return false;
		}

		@Override
		public boolean visit(EnumAttributeEditor editor) {
			tryPutVirtualAttribute(editor, editor.getType());
			return false;
		}

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
			tryPutVirtualRelationship(editor, editor.getRelationshipType(), editor.getRelationshipEntity());
            return false;
        }

        @Override
		public boolean visit(SingleRelationshipFieldEditor editor) {
			tryPutVirtualRelationship(editor, editor.getRelationshipType(), editor.getRelationshipEntity());
			return false;
		}

		@Override
		public boolean visit(MultipleRelationshipFieldEditor editor) {
			tryPutVirtualRelationship(editor, editor.getRelationshipType(), editor.getRelationshipEntity());
			return false;
		}

		private void tryPutVirtualAttribute(AbstractFieldEditor<?> editor, EntityAttributeType type) {
			if (editor.isVirtual()) {
				String fieldName = editor.getFieldName();
				virtualAttributes.put(fieldName, new VirtualAttributeMetadata(fieldName, type));
			}
		}
		
		private void tryPutVirtualRelationship(AbstractFieldEditor<?> editor, EntityRelationshipType type, IEntityMetadata entity) {
			if (editor.isVirtual()) {
				String fieldName = editor.getFieldName();
				virtualRelationships.put(fieldName, new VirtualRelationshipMetadata(fieldName, type, entity));
			}
		}
	}
	
	/**
	 * Representa os metadados de um atributo virtual.
	 */
	private static final class VirtualAttributeMetadata implements IEntityAttribute {
		private final String name;
		private final EntityAttributeType type;

		public VirtualAttributeMetadata(String name, EntityAttributeType type) {
			this.name = name;
			this.type = type; 
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public EntityAttributeType getType() {
			return type;
		} 
	}
	
	/**
	 * Representa os metadados de um relacionamento virtual.
	 */
	private static final class VirtualRelationshipMetadata implements IEntityRelationship {
		private final String name;
		private final EntityRelationshipType type;
		private final IEntityMetadata target;
        private IEntityMetadata source;

		public VirtualRelationshipMetadata(String name, EntityRelationshipType type, IEntityMetadata target) {
			this.name = name;
			this.type = type;
			this.target = target;
		}
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public EntityRelationshipType getType() {
			return type;
		}

        @Override
        public IEntityMetadata getSource() {
            return source;
        }

        @Override
		public IEntityMetadata getTarget() {
			return target;
		}

        void setSource(IEntityMetadata source) {
            this.source = source;
        }
    }
}
