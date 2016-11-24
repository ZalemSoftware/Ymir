package br.com.zalem.ymir.client.android.entity.ui.editor;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldEnum;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IVirtualAttribute;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IVirtualRelationship;
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
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter.TypedFormatter;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDateMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IImageMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IIntegerMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITextMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * Factory de editores de campos (subclasses de {@link AbstractFieldEditor}).<br>
 * Provê a criação dos editores através de configurações de campos de edição ({@link IEditingFieldMapping}).
 * Os tipos dos editores gerados variam de acordo com o tipo dos atributos / relacionamentos definidos. 
 *
 * @author Thiago Gesser
 */
public final class FieldEditorFactory {

	private final Context context;
	private final IEntityMetadata entityMetadata;
	private final IEntityDataManager entityManager;
	private final FragmentManager fragmentManager;
    private final IEntityUIConfigManager configManager;
    private final MaskManager maskManager;

    public FieldEditorFactory(Context context, IEntityMetadata entityMetadata, IEntityDataManager entityManager,
                              FragmentManager fragmentManager, IEntityUIConfigManager configManager, MaskManager maskManager) {
        this.context = context;
        this.entityMetadata = entityMetadata;
        this.entityManager = entityManager;
        this.fragmentManager = fragmentManager;
        this.configManager = configManager;
        this.maskManager = maskManager;
    }
	
	/**
	 * Cria um editor para cada configuração de campo (atributo ou relacionamento) de edição. O tipo de cada editor gerado varia de acordo com
     * o tipo do atributo / relacionamento.
	 * 
	 * @param fieldsMappings configurações de campos de edição.
	 * @return os editores criados.
	 */
	public AbstractFieldEditor<?>[] createFieldsEditors(IEditingFieldMapping... fieldsMappings) {
		AbstractFieldEditor<?>[] fieldsAdpters = new AbstractFieldEditor<?>[fieldsMappings.length];
		for (int i = 0; i < fieldsMappings.length; i++) {
			IEditingFieldMapping fieldMapping = fieldsMappings[i];

            boolean isAttribute = fieldMapping.getAttribute() != null;
            if (isAttribute && fieldMapping.getAttribute().length != 1) {
                throw new IllegalArgumentException(String.format("Only the attributes of the entity itself can be edited. Attribute = %s, entity = %s.", Arrays.toString(fieldMapping.getAttribute()), entityMetadata.getName()));
            }
            boolean isRelationship = fieldMapping.getRelationship() != null;
            if (isRelationship && fieldMapping.getRelationship().length != 1) {
                throw new IllegalArgumentException(String.format("Only the relationships of the entity itself can be edited. Relationship = %s, entity = %s.", Arrays.toString(fieldMapping.getRelationship()), entityMetadata.getName()));
            }
			boolean isVirtualAttribute = fieldMapping.getVirtualAttribute() != null;
			boolean isVirtualRelationship = fieldMapping.getVirtualRelationship() != null;
			
			//Só permite uma das quatro configurações.
			if ((isRelationship || isVirtualAttribute || isVirtualRelationship) && (isAttribute || isVirtualAttribute || isVirtualRelationship) && (isAttribute || isRelationship || isVirtualRelationship) && (isAttribute || isRelationship || isVirtualAttribute)) {
				throw new IllegalArgumentException("Invalid editing field mapping: only one attribute, relationship, virtual attribute or virtual relationship can be defined. Entity = " + entityMetadata.getName());
			}
			
			if (isAttribute || isVirtualAttribute) {
				String attributeName;
				EntityAttributeType attributeType;
				if (isVirtualAttribute) {
					IVirtualAttribute virtualAttribute = fieldMapping.getVirtualAttribute();
					attributeName = virtualAttribute.getName();
					attributeType = virtualAttribute.getType();
				} else {
					attributeName = fieldMapping.getAttribute()[0];
					IEntityAttribute entityAttribute = entityMetadata.getAttribute(attributeName);
					attributeType = entityAttribute.getType();
				}

                IMask attributeMask = maskManager.getMaskFromConfig(fieldMapping.getMask(), attributeType);
				if (fieldMapping.getEnum() == null) {
					fieldsAdpters[i] = createAttributeEditor(attributeName, attributeType, fieldMapping.getLabel(), fieldMapping.isEditable(),
                                                             fieldMapping.isHidden(), isVirtualAttribute, fieldMapping.getHelp(), fieldMapping.getInputType(), attributeMask, fieldMapping.getIncremental());
				} else {
					//Se for enum, cria um tipo de editor específico.
					fieldsAdpters[i] = createAttributeEnumEditor(attributeName, attributeType, fieldMapping.getLabel(), fieldMapping.isEditable(),
															 	 fieldMapping.isHidden(),  isVirtualAttribute, fieldMapping.getHelp(), fieldMapping.getEnum(), attributeMask);
				}
			} else if (isRelationship || isVirtualRelationship) {
				String relationshipName;
				EntityRelationshipType relationshipType;
				IEntityMetadata relationshipEntity;
				if (isVirtualRelationship) {
					IVirtualRelationship virtualRelationship = fieldMapping.getVirtualRelationship();
					relationshipName = virtualRelationship.getName();
					relationshipType = virtualRelationship.getType();
					relationshipEntity = entityManager.getEntityMetadata(virtualRelationship.getEntity());
				} else {
					relationshipName = fieldMapping.getRelationship()[0];
					IEntityRelationship relationship = entityMetadata.getRelationship(relationshipName);
					relationshipType = relationship.getType();
					relationshipEntity = relationship.getTarget();
				}

                if (fieldMapping.getEnum() == null) {
                    fieldsAdpters[i] = createRelationshipEditor(relationshipName, fieldMapping.getLabel(), fieldMapping.isEditable(), fieldMapping.isHidden(),
                                                                isVirtualRelationship, fieldMapping.getHelp(), relationshipType, relationshipEntity);

                } else {
                    fieldsAdpters[i] = createRelationshipEnumEditor(relationshipName, fieldMapping.getLabel(), fieldMapping.isEditable(), fieldMapping.isHidden(),
                                                                    isVirtualRelationship, fieldMapping.getHelp(), relationshipType, relationshipEntity, fieldMapping.getEnum());
                }
			} else {
				throw new IllegalArgumentException("Invalid editing field mapping: an attribute or a relationship must be defined. Entity = " + entityMetadata.getName());
			}
		}
		
		return fieldsAdpters;
	}


	/*
	 * Métodos auxiliares
	 */

	private AbstractFieldEditor<?> createAttributeEditor(String attributeName, EntityAttributeType attributeType, String label,
                                                         boolean isEditable, boolean isHidden, boolean isVirtual, String help, Integer inputTypeInteger, IMask mask, Double incremental) {
		try {
			int inputType = inputTypeInteger == null ? 0 : inputTypeInteger;
			switch (attributeType) {
				case INTEGER:
					Integer intIncrement = incremental == null ? null : incremental.intValue();
					return new IntegerFieldEditor(attributeName, label, isEditable, isHidden, isVirtual, help, inputType, (IIntegerMask) mask, intIncrement);
	
				case DECIMAL:
					return new DecimalFieldEditor(attributeName, label, isEditable, isHidden, isVirtual, help, inputType, (IDecimalMask) mask, incremental);
	
				case TEXT:
					return new TextFieldEditor(attributeName, label, isEditable, isHidden, isVirtual, help, inputType, (ITextMask) mask);

				case BOOLEAN:
					checkInputType(inputTypeInteger, attributeName, attributeType);
					return new BooleanFieldEditor(attributeName, label, isEditable, isHidden, isVirtual, help);

				case DATE:
					checkInputType(inputTypeInteger, attributeName, attributeType);
					return new DateFieldEditor(attributeName, label, isEditable, isHidden, isVirtual, help, (IDateMask) mask, fragmentManager);

				case TIME:
					checkInputType(inputTypeInteger, attributeName, attributeType);
					return new TimeFieldEditor(attributeName, label, isEditable, isHidden, isVirtual, help, (ITimeMask) mask, fragmentManager);
	
				case IMAGE:
                    checkInputType(inputTypeInteger, attributeName, attributeType);
                    return new ImageFieldEditor(attributeName, label, isEditable, isHidden, isVirtual, help, (IImageMask) mask);


				case DATETIME:
					throw new PendingFeatureException("DATETIME attribute editor");

				case CHARACTER:
					throw new PendingFeatureException("CHARACTER attribute editor");

				case INTEGER_ARRAY:
					throw new PendingFeatureException("INTEGER_ARRAY attribute editor");
	
				case DECIMAL_ARRAY:
					throw new PendingFeatureException("DECIMAL_ARRAY attribute editor");
	
				case TEXT_ARRAY:
					throw new PendingFeatureException("TEXT_ARRAY attribute editor");
	
				case BOOLEAN_ARRAY:
					throw new PendingFeatureException("BOOLEAN_ARRAY attribute editor");
	
				case DATE_ARRAY:
					throw new PendingFeatureException("DATE_ARRAY attribute editor");
	
				case TIME_ARRAY:
					throw new PendingFeatureException("TIME_ARRAY attribute editor");

				case DATETIME_ARRAY:
					throw new PendingFeatureException("DATETIME_ARRAY attribute editor");

				case CHARACTER_ARRAY:
					throw new PendingFeatureException("CHARACTER_ARRAY attribute editor");
	
				case IMAGE_ARRAY:
					throw new PendingFeatureException("IMAGE_ARRAY attribute editor");
					
				default:
					throw new IllegalArgumentException("Unsupported EntityAttributeType: " + attributeType);
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(String.format("The entity attribute or virtual attribute \"%s\" has the wrong mask type mapped to it. Detailed message: %s", attributeName, e.getMessage()));
		}
	}

	private void checkInputType(Integer inputType, String attributeName, EntityAttributeType attributeType) {
		if (inputType != null) {
			throw new IllegalArgumentException(String.format("Invalid editing attribute mapping: \"inputType\" is not allowed for the type %s. Attribute: %s, Entity: %s.", attributeType, attributeName, entityMetadata.getName()));
		}
	}

    @SuppressWarnings("unchecked")
	private AbstractFieldEditor<?> createAttributeEnumEditor(String attributeName, EntityAttributeType attributeType, String label,
                                                             boolean isEditable, boolean isHidden, boolean isVirtual, String help, IEditingFieldEnum enumConfig, IMask mask) {
		try {
			switch (attributeType) {
				case INTEGER:
				case DECIMAL:
				case TEXT:
				case BOOLEAN:
				case DATE:
				case TIME:
				case DATETIME:
                case CHARACTER:
                    TypedFormatter<?, Serializable> formatter = (TypedFormatter<?, Serializable>) EntityAttributeFormatter.createTypedFormatter(attributeType, mask);

                    //Utiliza os valores parseados (de acordo com a máscara) definidos na configuração como a lista de entradas disponíveis para o campo.
                    List<Serializable> values = getEnumEntries(enumConfig.getValues(), formatter);

                    return new EnumAttributeEditor(attributeName, label, isEditable, isHidden, isVirtual, help,
                                                   fragmentManager, values, formatter, attributeType);

				case IMAGE:
					throw new PendingFeatureException("IMAGE attribute editor (enum)");
	
				case INTEGER_ARRAY:
					throw new PendingFeatureException("INTEGER_ARRAY attribute editor (enum)");
	
				case DECIMAL_ARRAY:
					throw new PendingFeatureException("DECIMAL_ARRAY attribute editor (enum)");
	
				case TEXT_ARRAY:
					throw new PendingFeatureException("TEXT_ARRAY attribute editor (enum)");
	
				case BOOLEAN_ARRAY:
					throw new PendingFeatureException("BOOLEAN_ARRAY attribute editor (enum)");
	
				case DATE_ARRAY:
					throw new PendingFeatureException("DATE_ARRAY attribute editor (enum)");
	
				case TIME_ARRAY:
					throw new PendingFeatureException("TIME_ARRAY attribute editor (enum)");

				case DATETIME_ARRAY:
					throw new PendingFeatureException("TIME_ARRAY attribute editor (enum)");

				case CHARACTER_ARRAY:
					throw new PendingFeatureException("CHARACTER_ARRAY attribute editor (enum)");
	
				case IMAGE_ARRAY:
					throw new PendingFeatureException("IMAGE_ARRAY attribute editor (enum)");
	
				default:
					throw new IllegalArgumentException("Unsupported EntityAttributeType: " + attributeType);
			}
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<Serializable> getEnumEntries(String[] valuesConfigs, TypedFormatter<?, Serializable> formatter) throws ParseException {
		if (valuesConfigs == null || valuesConfigs.length == 0) {
			return null;
		}

        return Arrays.asList(formatter.parseValues(valuesConfigs));
	}
	
	private AbstractFieldEditor<?> createRelationshipEditor(String relationshipName, String label, boolean isEditable, boolean isHidden, boolean isVirtual, String help,
                                                            EntityRelationshipType relationshipType, IEntityMetadata relationshipEntity) {
		switch (relationshipType) {
			case ASSOCIATION:
			case COMPOSITION:
				return new SingleRelationshipFieldEditor(relationshipName, label, isEditable, isHidden, isVirtual, help, relationshipType, relationshipEntity, entityManager, configManager, maskManager);
				
			case ASSOCIATION_ARRAY:
			case COMPOSITION_ARRAY:
				return new MultipleRelationshipFieldEditor(relationshipName, label, isEditable, isHidden, isVirtual, help, relationshipType, relationshipEntity, entityManager, configManager, maskManager);
				
			default:
				throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationshipType);
		}
	}

	private AbstractFieldEditor<?> createRelationshipEnumEditor(String relationshipName, String label, boolean isEditable, boolean isHidden, boolean isVirtual, String help,
                                                                EntityRelationshipType relationshipType, IEntityMetadata relationshipEntity, IEditingFieldEnum enumConfig) {
		switch (relationshipType) {
			case ASSOCIATION:
                IEntityDAO entityDAO = entityManager.getEntityDAO(relationshipEntity.getName());
                List<IEntityRecord> values = getEnumRecords(enumConfig.getValues(), entityDAO, relationshipName);

                String[] displayAttribute = enumConfig.getAttribute();
                if (displayAttribute == null || displayAttribute.length == 0) {
                    throw new IllegalArgumentException(String.format("The enumeration config for the relationship \"%s\" does not declare a display attribute. Entity = %s.", relationshipName, entityMetadata.getName()));
                }
                EntityAttributeFormatter displayFormatter = EntityAttributeFormatter.fromConfig(context, relationshipEntity, maskManager, enumConfig);

                return new EnumRelationshipEditor(relationshipName, label, isEditable, isHidden, isVirtual, help,
                                                  fragmentManager, values, entityDAO, displayAttribute, displayFormatter);
			case COMPOSITION:
                throw new PendingFeatureException("COMPOSITION relationship editor (enum)");
			case ASSOCIATION_ARRAY:
                throw new PendingFeatureException("ASSOCIATION_ARRAY relationship editor (enum)");
			case COMPOSITION_ARRAY:
                throw new PendingFeatureException("COMPOSITION_ARRAY relationship editor (enum)");


			default:
				throw new IllegalArgumentException("Unsupported EntityRelationshipType: " + relationshipType);
		}
	}

    private List<IEntityRecord> getEnumRecords(String[] valuesIds, IEntityDAO entityDAO, String relationshipName) {
        if (valuesIds == null || valuesIds.length == 0) {
            return null;
        }

        List<IEntityRecord> records = new ArrayList<>(valuesIds.length);
        for (String valueId : valuesIds) {
            IEntityRecord record = entityDAO.get(valueId);
            if (record == null) {
                throw new IllegalArgumentException(String.format("The enumeration config for the relationship \"%s\" has a value declared that does not point to a record: \"%s\". Source entity = %s.", relationshipName, valueId, entityMetadata.getName()));
            }
            records.add(record);
        }
        return records;
    }
}
