package br.com.zalem.ymir.client.android.entity.ui.configuration.json.field;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFieldMapping;

/**
 * Generalização de um mapeamento de campo (atributo ou relacionamento) de entidade para JSON.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractJsonFieldMapping implements IFieldMapping {

	private String[] attribute;
	private String[] relationship;
	
	@Override
	public final String[] getAttribute() {
		return attribute;
	}

	@Override
	public final String[] getRelationship() {
		return relationship;
	}

    public final void setAttribute(String[] attribute) {
        this.attribute = attribute;
    }

    public final void setRelationship(String[] relationship) {
        this.relationship = relationship;
    }
}
