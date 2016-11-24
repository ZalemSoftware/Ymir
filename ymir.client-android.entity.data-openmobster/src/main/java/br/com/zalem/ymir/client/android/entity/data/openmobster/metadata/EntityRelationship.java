package br.com.zalem.ymir.client.android.entity.data.openmobster.metadata;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityRelationshipType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;

/**
 * Representação de um relacionamento de entidade.
 *
 * @author Thiago Gesser
 */
public final class EntityRelationship implements IEntityRelationship {

	private final String name;
	private final EntityRelationshipType type;
    private final EntityMetadata source;
    private final EntityMetadata target;
	private final String mappedBy;
	
	public EntityRelationship(String name, EntityRelationshipType type, EntityMetadata source, EntityMetadata target, String mappedBy) {
		this.name = name;
		this.type = type;
        this.source = source;
        this.target = target;
		this.mappedBy = mappedBy;
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
    public EntityMetadata getSource() {
        return source;
    }

    @Override
	public EntityMetadata getTarget() {
		return target;
	}
	
	
	/**
	 * Obtém o nome do relacionamento que a entidade alvo utiliza para se referenciar à entidade fonte.<br>
	 * O <code>mappedBy</code> suporta apenas relacionamentos singulares e que não declaram o <code>mappedBy</code> também.<br>
	 * <br>
	 * <b>Entidade alvo normal (não interna):</b><br>
	 * Se esta propriedade estiver definida, a responsabilidade de armazenar as referências para o registro fonte é
	 * dos registros alvos. Caso contrário, a responsabilidade de armazenar as referências para os registros alvos é do
	 * registro fonte.<br>
	 * <br>
	 * <b>Entidade alvo interna:</b><br>
	 * Se esta propriedade estiver definida, indica que a entidade interna (alvo) precisa ter acesso à sua entidade mestre (fonte).
	 * O relacionamento especificado no <code>mappedBy</code> será utilizado para armazenar a referência do registro mestre
	 * em cada registro interno. Como registros internos sempre são armazenados no registro fonte, o <code>mappedBy</code>
	 * não influencia nesta fator.
	 * 
	 * @return o nome de um relacionamento singular da entidade alvo para a entidade fonte.
	 */
	public String getMappedBy() {
		return mappedBy;
	}
	
	/**
	 * Indica se é a fonte do relacionamento que armazena as relações para os alvos (sem mappedBy) ou se são os alvos do 
	 * relacionamento que armazenam a relação para a fonte (com mappedBy).<br>
	 * Relacionameantos para entidades internas sempre armazenam as relações na fonte.
	 * 
	 * @return <code>true</code> se a fonte armazena as relações e <code>false</code> false caso contrário.
	 */
	public boolean isRelatedBySource() {
		return mappedBy == null || target.isInternal();
	}
}
