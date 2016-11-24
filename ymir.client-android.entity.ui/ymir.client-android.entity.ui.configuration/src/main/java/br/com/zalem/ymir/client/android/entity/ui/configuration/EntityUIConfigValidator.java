package br.com.zalem.ymir.client.android.entity.ui.configuration;

import android.text.TextUtils;

import java.util.HashSet;

import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldEnum;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingPermissions;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IVirtualAttribute;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IVirtualRelationship;
import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.DetailLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldVisibility;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IFilterFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListFilter;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListOrder;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IOrderFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.ITabbedListDisplayConfig;

/**
 * TODO Colocar um link para as especificações das configurações.<br><br>
 * 
 * Validador de configurações de entidades de acordo com suas especificações.<br>
 * <br>
 * A validação é interrompida quando algum problema é encontrado, lançando um {@link EntityConfigException} com os detalhes 
 * do problema. Desta forma, a configuração é considerada válida somente quando o determinado método <code>validate</code>
 * consegue executar até o final sem nenhuma exceção.
 *
 * @author Thiago Gesser
 */
public final class EntityUIConfigValidator {
	
	//Não permite instanciação.
	private EntityUIConfigValidator() {}
	
	private static final String MISSING_FIELD_ERROR_FORMAT = "\"%s\" can't be null or empty. Entity = %s.";

	
	/**
	 * Valida as configuração das entidade como um todo, lançando um {@link EntityConfigException} se algum problema for identificado.
	 * 
	 * @param entityConfigs configuração de cliente que será validada.
	 * @throws EntityConfigException se houver algum problema com as configurações.
	 */
	public static void validate(IEntityConfig... entityConfigs) throws EntityConfigException {
		HashSet<String> entitiesNames = new HashSet<>(entityConfigs.length);
		for (IEntityConfig entityConfig : entityConfigs) {
			validate(entityConfig);
			
			if (!entitiesNames.add(entityConfig.getName())) {
				throw new EntityConfigException("An entity configuration must be unique. The following entity had two configurations declared: " + entityConfig.getName());
			}
		}
	}
	
	/**
	 * Valida a configuração da entidade, lançando um {@link EntityConfigException} se algum problema for identificado.
	 * 
	 * @param entityConfig configuração de cliente que será validada.
	 * @throws EntityConfigException se houver algum problema com a configuração.
	 */
	public static void validate(IEntityConfig entityConfig) throws EntityConfigException {
		String entityName = entityConfig.getName();
		if (TextUtils.isEmpty(entityName)) {
			throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, "name", ""));
		}
		
		validateListConfig(entityConfig.getList(), entityName);
		validateListConfig(entityConfig.getSelection(), entityName);
		
		validateDetailConfig(entityConfig.getDetail(), entityName);
		
		validateEditingConfig(entityConfig.getEditing(), entityName);
	}
	
	
	/*
	 * Métodos auxiliares 
	 */

	private static void validateListConfig(ITabbedListDisplayConfig listConfig, String entityName) throws EntityConfigException {
		if (listConfig == null) {
			return;
		}
		
		ILayoutConfig<ListLayoutType> listConfigLayout = listConfig.getLayout();
		IListTab[] tabs = listConfig.getTabs();
		validateListOrderConfig(listConfig.getOrder(), "list.order", entityName);
		validateLayoutConfig(listConfigLayout, "list.layout", entityName);
			
		if (tabs == null || tabs.length == 0) {
			if (listConfigLayout == null) {
				throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, "list.layout", entityName));
			}
		} else {
			for (int i = 0; i < tabs.length; i++) {
				IListTab tab = tabs[i];
				
				if (TextUtils.isEmpty(tab.getTitle())) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, "list.tabs[" + i + "].title", entityName));
				}
				validateListOrderConfig(tab.getOrder(), "list.tabs[" + i + "].order", entityName);
				validateListFilterConfig(tab.getFilter(), "list.tabs[" + i + "].filter", entityName);
				if (tab.getLayout() != null) {
					validateLayoutConfig(tab.getLayout(), "list.tabs[" + i + "].layout", entityName);
				} else {
					if (listConfigLayout == null) {
						throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, "list.tabs[" + i + "].layout", entityName));
					}
				}
					
			}
		}
	}
	
	private static void validateListOrderConfig(IListOrder order, String configFieldName, String entityName) throws EntityConfigException {
		if (order == null) {
			return;
		}
		IOrderFieldMapping[] fields = order.getFields();
		if (fields == null) {
			return;
		}
		
		for (int i = 0; i < fields.length; i++) {
            IOrderFieldMapping field = fields[i];
			validateFieldMapping(field, configFieldName + ".fields[" + i + "]", entityName);
		}
	}

    private static void validateListFilterConfig(IListFilter filter, String configFieldName, String entityName) throws EntityConfigException {
        if (filter == null) {
            return;
        }
        IFilterFieldMapping[] fields = filter.getFields();
        if (fields == null) {
            return;
        }

        for (int i = 0; i < fields.length; i++) {
            IFilterFieldMapping field = fields[i];
            validateFieldMapping(field, configFieldName + ".fields[" + i + "]", entityName);

            String[] values = field.getValues();
            if (values == null || values.length == 0) {
                throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configFieldName + ".fields[" + i + "].values", entityName));
            }
        }
    }

	private static void validateLayoutConfig(ILayoutConfig<? extends ILayoutType> layoutConfig, String configFieldName, String entityName) throws EntityConfigException {
		if (layoutConfig == null) {
			return;
		}
		
		ILayoutType layoutType = layoutConfig.getType();
		if (layoutType == null) {
			throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configFieldName + ".type", entityName));
		}
		
		ILayoutFieldMapping[] fieldMappings = layoutConfig.getFields();
		if (fieldMappings == null) {
			throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configFieldName + ".fields", entityName));
		}
		
		LayoutField[] layoutFields = layoutType.getFields();
		if (layoutFields.length != fieldMappings.length) {
			throw new EntityConfigException(String.format("The number of required fields from \"%s\" is different from the number of field mappings in \"%s\". Entity = %s.", layoutType, configFieldName, entityName));
		}
		
		//Valida o mapeamento dos campos (layoutField, entityField = obrigatórios; mask = opcional).
		for (int i = 0; i < fieldMappings.length; i++) {
			ILayoutFieldMapping fieldMapping = fieldMappings[i];

            if (fieldMapping.getVisibility() == LayoutFieldVisibility.VISIBLE) {
                validateFieldMapping(fieldMapping, configFieldName + ".fields[" + i + "]", entityName);
            }
			LayoutField layoutField = fieldMapping.getLayoutField();
			if (layoutField == null) {
				throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configFieldName + ".fields[" + i + "].layoutField", entityName));
			}
			
			//Verifica se layoutField mapeado é utilizado pelo layout. 
			layoutFieldValidation: {
				for (LayoutField ef : layoutFields) {
					if (ef == fieldMapping.getLayoutField()) {
						break layoutFieldValidation;
					}
				}
				throw new EntityConfigException(String.format("\"%s.fields[%d].layoutField\" is not a valid field of the layout \"%s\". Entity = %s.", configFieldName, i, layoutType, entityName));
			}
		}
	}
	
	private static void validateDetailConfig(IDetailConfig detailConfig, String entityName) throws EntityConfigException {
		if (detailConfig == null) {
			return;
		}
		
		IDetailTab[] tabs = detailConfig.getTabs();
		ILayoutConfig<DetailLayoutType> mainHeader = detailConfig.getHeader();
		IDetailFieldMapping[] mainFields = detailConfig.getFields();
		if (tabs == null) {
			validateDetails(mainHeader, mainFields, "detail", entityName);
		} else {
			if (mainHeader != null || mainFields != null) {
				throw new EntityConfigException("The detail tabs can only be defined if the main detail configuration is not. Entity = " + entityName);
			}
			
			if (tabs.length == 0) {
				throw new EntityConfigException("At least one tab must be declared in detail.tabs. Entity = " + entityName);
			}
			
			for (int i = 0; i < tabs.length; i++) {
				IDetailTab tab = tabs[i];
				
				if (TextUtils.isEmpty(tab.getTitle())) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, "details.tabs[" + i + "].title", entityName));
				}
				validateDetails(tab.getHeader(), tab.getFields(), "details.tabs[" + i + "]", entityName);
			}
		}
		
	}
	
	private static void validateDetails(ILayoutConfig<DetailLayoutType> header, IDetailFieldMapping[] fields, String configName, String entityName) throws EntityConfigException {
		if (header == null && (fields == null || fields.length == 0)) {
			throw new EntityConfigException(String.format("At least one of the following configuration must be defined: %1$s.header or %1$s.fields. Entity = %2$s.",  configName, entityName));
		}
		
		validateLayoutConfig(header, "detail.header", entityName);
		
		validateDetailFieldConfigs(fields,  "detail.fields", entityName);
	}
	
	private static void validateDetailFieldConfigs(IDetailFieldMapping[] fields, String configFieldName, String entityName) throws EntityConfigException {
		//A declaração do "additionalFields" é opcional.
		if (fields == null) {
			return;
		}
		
		//entityField = obrigatório; label, mask = opcionais.
		for (int i = 0; i < fields.length; i++) {
			IDetailFieldMapping fieldMapping = fields[i];
			String curConfigName = configFieldName + "[" + i + "]";
			validateFieldMapping(fieldMapping, curConfigName, entityName);
			
			//Se declarou o listConfig, precisa declarar pelo menos um campo, se não trata-se de uma declaração inútil.
			IListDisplayConfig listConfig = fieldMapping.getListConfig();
			if (listConfig != null && listConfig.getLayout() == null && listConfig.getFilter() == null && listConfig.getOrder() == null) {
				throw new EntityConfigException(String.format("At least one property of the following configuration must be defined: %s.listConfig. Entity = %s.", curConfigName, entityName));
			}
		}
	}
	
	private static void validateFieldMapping(IFieldMapping fieldMapping, String configFieldName, String entityName) throws EntityConfigException {
        String[] attributePath = fieldMapping.getAttribute();
        String[] relationshipPath = fieldMapping.getRelationship();
        boolean hasAttribute = attributePath != null;
        boolean hasRelationship = relationshipPath != null;

		if (hasAttribute) {
            if (attributePath.length == 0) {
                throw new EntityConfigException(String.format("Attribute path can't be empty: %s.attribute. Entity = %s.", configFieldName, entityName));
            }
            if (hasRelationship) {
                throw new EntityConfigException(String.format("Only one of the following configuration can be defined, not both: %1$s.attribute or %1$s.relationship. Entity = %2$s.", configFieldName, entityName));
            }
        } else if (hasRelationship) {
            if (relationshipPath.length == 0) {
                throw new EntityConfigException(String.format("Relationship path can't be empty: %s.relationship. Entity = %s.", configFieldName, entityName));
            }
        } else {
			throw new EntityConfigException(String.format("At least one of the following configuration must be defined: %1$s.attribute or %1$s.relationship. Entity = %2$s.", configFieldName, entityName));
		}
	}
	
	private static void validateEditingConfig(IEditingConfig editingConfig, String entityName) throws EntityConfigException {
		if (editingConfig == null) {
			return;
		}
		
		validateEditingPermissions(entityName, editingConfig.getLocalPermissions(), editingConfig.getDataSourcePermissions());
		
		IEditingTab[] tabs = editingConfig.getTabs();
		IEditingFieldMapping[] mainFields = editingConfig.getFields();
		if (tabs == null) {
			validateEditingFields(mainFields, "editing", entityName);
		} else {
			if (mainFields != null) {
				throw new EntityConfigException("The editing tabs can only be defined if the fields configuration is not. Entity = " + entityName);
			}

            if (!TextUtils.isEmpty(editingConfig.getLayout())) {
                throw new EntityConfigException("The editing layout configuration should be defined within the tabs when tabs are declared. Entity = " + entityName);
            }
			
			if (tabs.length == 0) {
				throw new EntityConfigException("At least one tab must be declared in editing.tabs. Entity = " + entityName);
			}
			
			for (int i = 0; i < tabs.length; i++) {
				IEditingTab tab = tabs[i];
				
				if (TextUtils.isEmpty(tab.getTitle())) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, "editing.tabs[" + i + "].title", entityName));
				}
				validateEditingFields(tab.getFields(), "editing.tabs[" + i + "]", entityName);
			}
		}
	}
	
	private static void validateEditingPermissions(String entityName, IEditingPermissions... permissions) throws EntityConfigException {
		//Deve haver pelo menos uma configuração de edição habilitada em alguma das permissões.
		for (IEditingPermissions permission : permissions) {
			if (permission == null) {
				continue;
			}
			
			if (permission.canCreate() || permission.canUpdate() || permission.canDelete()) {
				return;
			}
		}
		
		throw new EntityConfigException("At least one permission must be granted in the editing config. Entity = " + entityName);
	}
	
	private static void validateEditingFields(IEditingFieldMapping[] fields, String configName, String entityName) throws EntityConfigException {
		if (fields == null) {
			return;
		}

		for (int i = 0; i < fields.length; i++) {
			IEditingFieldMapping field = fields[i];

            boolean hasAttribute = field.getAttribute() != null;
            if (hasAttribute && field.getAttribute().length != 1) {
                throw new EntityConfigException(String.format("Only the attributes of the entity itself can be mapped to editing: %s.fields[%d].attribute. Entity = %s.", configName, i, entityName));
            }
            boolean hasRelationship = field.getRelationship() != null;
            if (hasRelationship && field.getRelationship().length != 1) {
                throw new EntityConfigException(String.format("Only the relationships of the entity itself can be mapped to editing: %s.fields[%d].relationship. Entity = %s.", configName, i, entityName));
            }
			boolean hasVirtualAttribute = field.getVirtualAttribute() != null;
			boolean hasVirtualRelationship = field.getVirtualRelationship() != null;

            //O if atua como um "ou exclusivo" na verificação dos campos declarados. Há o operador ^ para isto, mas ele é binário então para três ou mais valores não funciona adequadamente.
			if ((hasRelationship || hasVirtualAttribute || hasVirtualRelationship) && (hasAttribute || hasVirtualAttribute || hasVirtualRelationship) &&
				(hasAttribute || hasRelationship || hasVirtualRelationship) && (hasAttribute || hasRelationship || hasVirtualAttribute)) {
				throw new EntityConfigException(String.format("Only one of the following configuration can be defined in %s.fields[%d]: attribute, relationship or virtualAttribute. Entity = %s.", configName, i, entityName));
			}
			if (!hasAttribute && !hasRelationship && !hasVirtualAttribute && !hasVirtualRelationship) {
				throw new EntityConfigException(String.format("At least one of the following configuration must be defined in %s.fields[%d]:  attribute, relationship or virtualAttribute. Entity = %s.", configName, i, entityName));
			}

			//Validações dos campos virtuais.
            if (hasVirtualAttribute) {
				IVirtualAttribute virtualAttribute = field.getVirtualAttribute();
				if (TextUtils.isEmpty(virtualAttribute.getName())) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configName + ".fields[" + i + "].virtualAttribute.name", entityName));
				}
                if (virtualAttribute.getType() == null) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configName + ".fields[" + i + "].virtualAttribute.type", entityName));
				}
            } else if (hasVirtualRelationship) {
				IVirtualRelationship virtualRelationship = field.getVirtualRelationship();
				if (TextUtils.isEmpty(virtualRelationship.getName())) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configName + ".fields[" + i + "].virtualRelationship.name", entityName));
				}
				if (virtualRelationship.getType() == null) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configName + ".fields[" + i + "].virtualRelationship.type", entityName));
				}
				if (TextUtils.isEmpty(virtualRelationship.getEntity())) {
					throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configName + ".fields[" + i + "].virtualRelationship.entity", entityName));
				}
			}

			//Validações de enumeração.
            IEditingFieldEnum enumConfig = field.getEnum();
            if (enumConfig != null) {
                String[] enumAttr = enumConfig.getAttribute();
                String enumMask = enumConfig.getMask();
                if (hasAttribute || hasVirtualAttribute) {
                    //A configuração de atributo de exibição da enumeração só pode ser utilizada em campos de relacionamento.
                    if (enumAttr != null || enumMask != null) {
                        throw new EntityConfigException(String.format("Enumeration display attribute and mask can only be used by a relationship field mapping. Enum config = %s.fields[%d].enum, entity = %s.", configName, i, entityName));
                    }
                } else {
                    //Se é enumeração para relacionamento, precisa declarar um atributo de exibição.
                    if (enumAttr == null || enumAttr.length == 0) {
                        throw new EntityConfigException(String.format(MISSING_FIELD_ERROR_FORMAT, configName + ".fields[" + i + "].enum.attribute", entityName));
                    }
                }
                if (enumConfig.getRelationship() != null) {
                    throw new EntityConfigException(String.format("Enumerations can't point to relationships. Enum config = %s.fields[%d].enum, entity = %s.", configName, i, entityName));
                }
            }

			//Validações do inputType.
			if (field.getInputType() != null) {
				if (enumConfig != null || hasRelationship || hasVirtualRelationship)
				throw new EntityConfigException(String.format("The input type configuration can't be used in relationship or enumeration fields. Field = %s.fields[%d], entity = %s.", configName, i, entityName));
			}
		}
	}
}