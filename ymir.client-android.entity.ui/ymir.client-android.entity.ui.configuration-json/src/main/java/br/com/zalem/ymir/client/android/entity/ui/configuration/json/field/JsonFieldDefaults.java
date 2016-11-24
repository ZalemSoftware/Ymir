package br.com.zalem.ymir.client.android.entity.ui.configuration.json.field;

/**
 * Configuração de valores padrão de um campo de uma entidade.
 *
 * @author Thiago Gesser
 */
public final class JsonFieldDefaults {

	private String name;
	private String label;
	private String mask;
    private String surrogateAttribute;

    /**
     * Obtém o nome do campo da entidade.
     *
     * @return o nome obtido.
     */
	public String getName() {
		return name;
	}

    /**
     * <b>Configuração opcional.</b><br>
     * <br>
     * Obtém um rótulo padrão para o campo da entidade.
     *
     * @return o rótulo obtido.
     */
	public String getLabel() {
		return label;
	}

    /**
     * <b>Configuração opcional.</b><br>
     * <br>
     * Obtém a máscra padrao para o campo da entidade.
     *
     * @return a configuração de máscara obtida.
     */
	public String getMask() {
		return mask;
	}

    /**
     * <b>Configuração opcional.</b><br>
     * <br>
     * Obtém o substituto de representação para o campo de imagem da entidade.
     *
     * @return a configuração de campo obtida.
     */
    public String getSurrogateAttribute() {
        return surrogateAttribute;
    }


    public void setName(String name) {
		this.name = name;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setMask(String mask) {
		this.mask = mask;
	}

    public void setSurrogateAttribute(String surrogateAttribute) {
        this.surrogateAttribute = surrogateAttribute;
    }
}