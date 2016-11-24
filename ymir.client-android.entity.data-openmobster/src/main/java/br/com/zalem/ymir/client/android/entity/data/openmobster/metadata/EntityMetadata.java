package br.com.zalem.ymir.client.android.entity.data.openmobster.metadata;

import android.text.TextUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;

/**
 * Representação dos metadados de uma entidade.
 *
 * @author Thiago Gesser
 */
public final class EntityMetadata implements IEntityMetadata {

	private final String name;
	private final String channel;
	private final boolean internal;
	private final Map<String, EntityAttribute> attributesMap;
	private final Map<String, EntityRelationship> relationshipsMap;
    private EntityRelationship[] referencesToMe;
	
	public EntityMetadata(EntityMetadataConfig config) {
		this.name = config.getName();
		//Se o canal não foi definido, assume que o nome da entidade é o nome do canal.
		String channel = config.getChannel();
		this.channel = channel != null ? channel : name;
		this.internal = config.isInternal();
		
		EntityAttribute[] attributes = config.getAttributes();
		if (attributes != null && attributes.length > 0) {
			attributesMap = new HashMap<>(attributes.length);
			for (EntityAttribute attribute : attributes) {
				attributesMap.put(attribute.getName(), attribute);
			}
		} else {
			attributesMap = Collections.emptyMap();
		}

		//Apenas cria o mapa de relacionamentos agora. Eles serão inicializadas depois, pois necessitam dos metadados
		//das outras entidades para isto.
		EntityRelationshipConfig[] relationshipsConfigs = config.getRelationships();
		if (relationshipsConfigs != null && relationshipsConfigs.length > 0) {
			relationshipsMap = new HashMap<>(relationshipsConfigs.length);
		} else {
			relationshipsMap = Collections.emptyMap();
		}
	}

    @Override
	public String getName() {
		return name;
	}

	@Override
	public EntityAttribute getAttribute(String name) {
		EntityAttribute attribute = attributesMap.get(name);
		if (attribute == null) {
			throw new IllegalArgumentException(String.format("No attribute found with the name \"%s\" for the entity %s.", name, this.name));
		}
		return attribute;
	}

	@Override
	public EntityAttribute[] getAttributes() {
		Collection<EntityAttribute> values = attributesMap.values();
		return values.toArray(new EntityAttribute[values.size()]);
	}

	@Override
	public EntityRelationship getRelationship(String name) {
		EntityRelationship relationship = relationshipsMap.get(name);
		if (relationship == null) {
			throw new IllegalArgumentException(String.format("No relationship found with the name \"%s\" for the entity %s.", name, this.name));
		}
		return relationship;
	}

	@Override
	public EntityRelationship[] getRelationships() {
		Collection<EntityRelationship> values = relationshipsMap.values();
		return values.toArray(new EntityRelationship[values.size()]);
	}
	
	/**
	 * Obtém o nome do canal do OpenMobster para esta entidade.
	 * 
	 * @return o nome do canal obtido.
	 */
	public String getChannel() {
		return channel;
	}
	
	/**
	 * Indica se a entidade é interna.<br>
	 * Uma entidade interna represeneta uma estrutura de dados auxiliar, utilizada apenas para compor outras entidades.
	 * Registros deste tipo de entidade ficam armazenados dentro dos registros das entidades donas. Desta forma,
	 * registros de entidades internas não possuem identificador e não podem ser acessados diretamente pelos
	 * DAOs. A única forma de obtê-los é através dos registros donos, utilizando os métodos normais de manipulação de relacionamentos.<br>
	 * Relacionamento para entidades internas são feitos da mesma forma como uma entidade normal, com a diferença que
	 * apenas composições são suportadas. Associações só podem ser feitas no caso de uma entidade intena detalhe estar
	 * referenciando uma entidade interna mestre.
	 * 
	 * @return <code>true</code> se a entidade for interna e <code>false</code> caso contrário.
	 */
	public boolean isInternal() {
		return internal;
	}
	
	
	/**
	 * Obtém o mapa de relacionamentos.
	 * 
	 * @return o mapa obtido.
	 */
	public Map<String, EntityRelationship> getRelationshipsMap() {
		return relationshipsMap;
	}
	
	/**
	 * Obtém o mapa de atributos.
	 * 
	 * @return o mapa obtido.
	 */
	public Map<String, EntityAttribute> getAttributesMap() {
		return attributesMap;
	}

    /**
     * Obtém as referências (relacionamentos mantidos pela fonte) que apontam para esta entidade.
     *
     * @return os relacionamentos obtidos.
     */
    public EntityRelationship[] getReferencesToMe() {
        return referencesToMe;
    }
	
	/**
	 * Verifica se o campo existe.
	 * 
	 * @param fieldName o nome do campo que será verificado.
	 * @throws IllegalArgumentException se o campo não existir.
	 */
	public void checkFieldName(String fieldName) {
		if (!attributesMap.containsKey(fieldName) && !relationshipsMap.containsKey(fieldName)) {
			throw new IllegalArgumentException(String.format("No field found with the name \"%s\".", name));
		}
	}


    /*
     * Métodos de inicialização utilizados pelo MobileBeanEntityDataManager.
     */

    /**
     * Inicializa os relacionamentos da entidade. Precisa ser feito em outro momento porque os metadados de todas as entidades
     * precisam estar criados para que os relacionamentos apontem pra eles.
     *
     * @param relationshipsConfigs configuraões de relacionamentos da entidade.
     * @param manager gerenciador de entidades.
     */
    public void initializeRelationships(EntityRelationshipConfig[] relationshipsConfigs, MobileBeanEntityDataManager manager) {
        if (!relationshipsMap.isEmpty()) {
            throw new IllegalStateException();
        }

        if (relationshipsConfigs == null || relationshipsConfigs.length == 0) {
            return;
        }

        for (EntityRelationshipConfig rc : relationshipsConfigs) {
            EntityMetadata targetEntityMetadata = manager.getEntityMetadata(rc.getEntity());
            String mappedBy = TextUtils.isEmpty(rc.getMappedBy()) ? null : rc.getMappedBy();
            EntityRelationship relationship = new EntityRelationship(rc.getName(), rc.getType(), this, targetEntityMetadata, mappedBy);

            relationshipsMap.put(rc.getName(), relationship);
        }
    }

    /**
     * Define os relacionamentos que apontam para esta entidade.
     *
     * @param relationshipsToMe relacionamentos para a entidade.
     */
    public void setReferencesToMe(EntityRelationship[] relationshipsToMe) {
        if (this.referencesToMe != null) {
            throw new IllegalStateException();
        }

        this.referencesToMe = relationshipsToMe;
    }
}
