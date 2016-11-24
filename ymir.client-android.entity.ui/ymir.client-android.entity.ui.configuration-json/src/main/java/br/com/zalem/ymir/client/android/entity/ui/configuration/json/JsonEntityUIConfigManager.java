package br.com.zalem.ymir.client.android.entity.ui.configuration.json;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.entity.ui.configuration.EntityConfigException;
import br.com.zalem.ymir.client.android.entity.ui.configuration.EntityUIConfigValidator;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing.JsonEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing.JsonEditingFieldEnum;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing.JsonEditingFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.editing.JsonEditingTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonFormattableFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.AbstractJsonLabelableFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.field.JsonFieldDefaults;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.detail.JsonDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.detail.JsonDetailFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.detail.JsonDetailTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.layout.JsonLayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.layout.JsonLayoutFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list.JsonListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list.JsonListFilter;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list.JsonListTab;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list.JsonTabbedListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.detail.IDetailConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListTab;

/**
 * Gerenciador de configurações baseado em JSON.<br>
 * Além das configurações das entidades, permite obter configurações extras através do metodo {@link #getExtraConfig(int)}. Estas configurações
 * podem ser adicionadas através dos métodos <code>addExtra*Config</code>.
 *
 * @author Thiago Gesser
 */
public final class JsonEntityUIConfigManager implements IEntityUIConfigManager {
	
	private final Map<String, JsonEntityConfig> entityConfigs;
	private final Map<Integer, Object> extraConfigs;

	public JsonEntityUIConfigManager(JsonEntityConfig... entityConfigs) throws EntityConfigException {
		EntityUIConfigValidator.validate(entityConfigs);

		this.entityConfigs = new HashMap<>();
		for (JsonEntityConfig entityConfig : entityConfigs) {
			this.entityConfigs.put(entityConfig.getName(), entityConfig);
		}
		this.extraConfigs = new HashMap<>();
	}

	@Override
	public JsonEntityConfig getEntityConfig(String entityName) {
		return entityConfigs.get(entityName);
	}

    /**
     * Aplica os {@link JsonFieldDefaults valores padrão dos campos} na configuração. Estes valores são definidos na configuração de cada entidade,
     * através do {@link JsonEntityConfig#getFieldsDefaults()}.
     *
     * @param dataManager gerenciador de dados utilizado para obtenção dos metadados das entidades devido aos campos declarados através de relacionamentos.
     */
    public void applyFieldsDefaults(IEntityDataManager dataManager) {
        for (JsonEntityConfig config : entityConfigs.values()) {
            JsonFieldDefaults[] fieldsDefaults = config.getFieldsDefaults();
            IEntityMetadata entityMetadata = dataManager.getEntityMetadata(config.getName());

            applyFieldsDefaults(config.getList(), fieldsDefaults, entityMetadata);
            applyFieldsDefaults(config.getSelection(), fieldsDefaults, entityMetadata);
            applyFieldsDefaults(config.getDetail(), fieldsDefaults, entityMetadata);
            applyFieldsDefaults(config.getEditing(), fieldsDefaults, entityMetadata);
        }
    }


    /**
     * Adiciona configurações extras de detalhes de entidade: {@link IDetailConfig}.
     *
     * @param objectMapper mapeador que será utilizado na deserialização para as instâncias de EntityMetadata.
     * @param context contexto.
     * @param dataManager gerenciador de dados utilizado para obtenção dos metadados das entidades devido aos campos declarados através de relacionamentos.
     * @param entityName nome da entidade do detalhe.
     * @param jsonResIds ids dos recursos json contendo as configurações.
     * @throws EntityConfigException se há algum problema nas configurações.
     */
    public void addExtraDetailConfig(ObjectMapper objectMapper, Context context, IEntityDataManager dataManager, String entityName, int... jsonResIds) throws EntityConfigException {
        Resources resources = context.getResources();
        for (int jsonResId : jsonResIds) {
            JsonDetailConfig detailConfig;
            try {
                InputStream configIn = resources.openRawResource(jsonResId);
                try {
                    detailConfig = objectMapper.readValue(configIn, JsonDetailConfig.class);
                } finally {
                    configIn.close();
                }
            } catch (IOException e) {
                throw newConfigParseException(resources, jsonResId, e);
            }

            JsonEntityConfig entityConfig = getEntityConfig(entityName);
            JsonFieldDefaults[] fieldsDefaults = entityConfig.getFieldsDefaults();
            IEntityMetadata entityMetadata = dataManager.getEntityMetadata(entityName);

            //Aplica os valores padrão da configuração de detalhe.
            applyFieldsDefaults(detailConfig, fieldsDefaults, entityMetadata);

            //Adiciona nas configurações extras.
            extraConfigs.put(jsonResId, detailConfig);
        }
    }

    /**
     * Adiciona configurações extras de abas de lista de uma entidade: {@link IListTab}[].
     *
     * @param objectMapper mapeador que será utilizado na deserialização para as instâncias de EntityMetadata.
     * @param context contexto.
     * @param dataManager gerenciador de dados utilizado para obtenção dos metadados das entidades devido aos campos declarados através de relacionamentos.
     * @param entityName nome da entidade da lista.
     * @param jsonResIds ids dos recursos json contendo as configurações.
     * @throws EntityConfigException se há algum problema nas configurações.
     */
    public void addExtraListTabsConfig(ObjectMapper objectMapper, Context context, IEntityDataManager dataManager, String entityName, int... jsonResIds) throws EntityConfigException {
        Resources resources = context.getResources();
        for (int jsonResId : jsonResIds) {
            JsonListTab[] listTabsConfigs;
            try {
                InputStream configIn = resources.openRawResource(jsonResId);
                try {
                    listTabsConfigs = objectMapper.readValue(configIn, JsonListTab[].class);
                } finally {
                    configIn.close();
                }
            } catch (IOException e) {
                throw newConfigParseException(resources, jsonResId, e);
            }

            JsonEntityConfig entityConfig = getEntityConfig(entityName);
            JsonFieldDefaults[] fieldsDefaults = entityConfig.getFieldsDefaults();
            IEntityMetadata entityMetadata = dataManager.getEntityMetadata(entityName);

            //Aplica os valores padrão da configuração de detalhe.
            for (JsonListTab tab : listTabsConfigs) {
                applyFieldsDefaults(tab, fieldsDefaults, entityMetadata);
            }

            //Adiciona nas configurações extras.
            extraConfigs.put(jsonResId, listTabsConfigs);
        }
    }

    /**
     * Obtém uma configuração extra previamente adicionada através dos métodos <code>addExtra*Config</code>.
     *
     * @param jsonResId o id do recurso json da configuração extra.
     * @param <T> Tipo da configuração extra.
     * @return a configuração extra obtida ou <code>null</code> se esta configuração não foi definida.
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtraConfig(int jsonResId) {
        return (T) extraConfigs.get(jsonResId);
    }


	/**
	 * Cria um JsonEntityConfigManager a partir de configurações de entidades obtidos através de recursos no formato json.
	 *
	 * @param objectMapper mapeador que será utilizado na deserialização para as instâncias de EntityMetadata.
	 * @param context contexto Android.
	 * @param jsonResIds ids dos recursos json.
	 * @return o JsonEntityConfigManager criado.
	 * @throws EntityConfigException se há algum problema nas configurações.
	 */
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static JsonEntityUIConfigManager fromJsonResources(ObjectMapper objectMapper, Context context, int... jsonResIds) throws EntityConfigException {
		JsonEntityConfig[] entityConfigs = new JsonEntityConfig[jsonResIds.length];
		Resources resources = context.getResources();
		for (int i = 0; i < jsonResIds.length; i++) {
			int jsonResId = jsonResIds[i];
			try {
				InputStream configIn = resources.openRawResource(jsonResId);
				try {
					entityConfigs[i] = objectMapper.readValue(configIn, JsonEntityConfig.class);
				} finally {
					configIn.close();
				}
			} catch (IOException e) {
                throw newConfigParseException(resources, jsonResId, e);
			}
		}

		return new JsonEntityUIConfigManager(entityConfigs);
	}


	/*
	 * Métodos auxiliares
	 */

    private void applyFieldsDefaults(JsonTabbedListDisplayConfig listConfig, JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata) {
        if (listConfig == null) {
            return;
        }

        applyFieldsDefaults((JsonListDisplayConfig) listConfig, baseEntityDefaults, entityMetadata);
        JsonListTab[] tabs = listConfig.getTabs();
        if (tabs != null) {
            for (JsonListTab tab : tabs) {
                applyFieldsDefaults(tab, baseEntityDefaults, entityMetadata);
            }
        }
    }

    private void applyFieldsDefaults(JsonListDisplayConfig listConfig, JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata) {
        applyLayoutFieldsDefaults(listConfig.getLayout(), baseEntityDefaults, entityMetadata);

        JsonListFilter filter = listConfig.getFilter();
        if (filter != null) {
            applyDefaultMask(baseEntityDefaults, entityMetadata, filter.getFields());
        }
    }

    private void applyFieldsDefaults(JsonDetailConfig detailConfig, JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata) {
        if (detailConfig == null) {
            return;
        }

        applyLayoutFieldsDefaults(detailConfig.getHeader(), baseEntityDefaults, entityMetadata);
        applyDetailFieldsDefaults(baseEntityDefaults, entityMetadata, detailConfig.getFields());

        JsonDetailTab[] tabs = detailConfig.getTabs();
        if (tabs != null) {
            for (JsonDetailTab tab : tabs) {
                applyLayoutFieldsDefaults(tab.getHeader(), baseEntityDefaults, entityMetadata);
                applyDetailFieldsDefaults(baseEntityDefaults, entityMetadata, tab.getFields());
            }
        }
    }

    private void applyFieldsDefaults(JsonEditingConfig editingConfig, JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata) {
        if (editingConfig == null) {
            return;
        }

        applyEditingFieldsDefaults(baseEntityDefaults, entityMetadata, editingConfig.getFields());
        JsonEditingTab[] tabs = editingConfig.getTabs();
        if (tabs != null) {
            for (JsonEditingTab tab : tabs) {
                applyEditingFieldsDefaults(baseEntityDefaults, entityMetadata, tab.getFields());
            }
        }
    }

    private void applyLayoutFieldsDefaults(JsonLayoutConfig<?> layoutConfig, JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata) {
        if (layoutConfig == null) {
            return;
        }

        JsonLayoutFieldMapping[] fields = layoutConfig.getFields();
        if (fields == null) {
            return;
        }


        for (JsonLayoutFieldMapping field : fields) {
            JsonFieldDefaults fieldDefaults = null;

            //Máscara
            if (TextUtils.isEmpty(field.getMask())) {
                fieldDefaults = findFieldDefaults(field, baseEntityDefaults, entityMetadata);
                if (fieldDefaults == null) {
                    continue;
                }
                field.setMask(fieldDefaults.getMask());
            }

            //Atributo de substituição para imagens.
            if (field.getSurrogateAttribute() == null) {
                if (fieldDefaults == null) {
                    fieldDefaults = findFieldDefaults(field, baseEntityDefaults, entityMetadata);
                }
                if (fieldDefaults == null) {
                    continue;
                }
                field.setSurrogateAttribute(fieldDefaults.getSurrogateAttribute());
            }
        }
    }

    private void applyDefaultMask(JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata, AbstractJsonFormattableFieldMapping... fields) {
        if (fields == null) {
            return;
        }
        for (AbstractJsonFormattableFieldMapping field : fields) {
            if (TextUtils.isEmpty(field.getMask())) {
                JsonFieldDefaults fieldDefaults = findFieldDefaults(field, baseEntityDefaults, entityMetadata);
                if (fieldDefaults != null) {
                    field.setMask(fieldDefaults.getMask());
                }
            }
        }
    }

    private void applyDetailFieldsDefaults(JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata, JsonDetailFieldMapping... fields) {
        if (fields == null) {
            return;
        }
        for (JsonDetailFieldMapping field : fields) {
            applyDefaultMaskAndLabel(baseEntityDefaults, entityMetadata, field);

            //Se declarou uma configuração de lista diferente, deve aplicar os defaults de acordo com a entidade alvo.
            JsonListDisplayConfig listConfig = field.getListConfig();
            if (listConfig != null && field.getRelationship() != null) {
                IEntityRelationship relationship = MetadataUtils.getRelationshipFromPath(entityMetadata, field.getRelationship());
                IEntityMetadata targetEntity = relationship.getTarget();
                applyFieldsDefaults(listConfig, null, targetEntity);
            }
        }
    }

    private void applyEditingFieldsDefaults(JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata, JsonEditingFieldMapping... fields) {
        if (fields == null) {
            return;
        }
        for (JsonEditingFieldMapping field : fields) {
            applyDefaultMaskAndLabel(baseEntityDefaults, entityMetadata, field);

            //Se for uma enumeração para um relacionamento, o atributo de exibição é da entidade alvo do relacionamento, então precisa dos defaults default dela.
            JsonEditingFieldEnum enumConfig = field.getEnum();
            if (enumConfig != null && field.getRelationship() != null) {
                IEntityRelationship relationship = MetadataUtils.getRelationshipFromPath(entityMetadata, field.getRelationship());
                IEntityMetadata targetEntity = relationship.getTarget();
                applyDefaultMask(null, targetEntity, enumConfig);
            }
        }
    }

    private void applyDefaultMaskAndLabel(JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata, AbstractJsonLabelableFieldMapping field) {
        JsonFieldDefaults fieldDefaults = findFieldDefaults(field, baseEntityDefaults, entityMetadata);
        if (fieldDefaults == null) {
            return;
        }

        if (TextUtils.isEmpty(field.getMask())) {
            field.setMask(fieldDefaults.getMask());
        }

        if (TextUtils.isEmpty(field.getLabelValue())) {
            field.setLabel(fieldDefaults.getLabel());
        }
    }

    private JsonFieldDefaults findFieldDefaults(IFieldMapping field, JsonFieldDefaults[] baseEntityDefaults, IEntityMetadata entityMetadata) {
        String[] fieldPath = field.getAttribute();
        if (fieldPath == null || fieldPath.length == 0) {
            fieldPath = field.getRelationship();
            if (fieldPath == null || fieldPath.length == 0) {
                return null;
            }
        }

        JsonFieldDefaults[] fieldsDefaults;
        if (fieldPath.length == 1 && baseEntityDefaults != null) {
            //Se referencia um campo da própria entidade, já usa os seus padrões diretamente.
            fieldsDefaults = baseEntityDefaults;
        } else {
            //Se referencia um campo de outra entidade, obtém os padroes dela.
            IEntityMetadata targetEntityMetadata = MetadataUtils.getEntityFromPath(entityMetadata, fieldPath);
            JsonEntityConfig targetEntityConfig = entityConfigs.get(targetEntityMetadata.getName());
            if (targetEntityConfig == null) {
                return null;
            }
            fieldsDefaults = targetEntityConfig.getFieldsDefaults();
        }

        if (fieldsDefaults != null) {
            String fieldName = fieldPath[fieldPath.length-1];
            for (JsonFieldDefaults fieldDefaults : fieldsDefaults) {
                if (fieldDefaults.getName().equals(fieldName)) {
                    return fieldDefaults;
                }
            }
        }
        return null;
    }

	private static EntityConfigException newConfigParseException(Resources resources, int jsonResId, Exception e) {
		String jsonResName = resources.getResourceEntryName(jsonResId);
		return new EntityConfigException("An error occurred during the parse of the following config resource: " + jsonResName, e);
	}
}
