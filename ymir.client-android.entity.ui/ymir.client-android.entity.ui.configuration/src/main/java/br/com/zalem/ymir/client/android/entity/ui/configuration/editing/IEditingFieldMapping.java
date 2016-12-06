package br.com.zalem.ymir.client.android.entity.ui.configuration.editing;

import android.text.InputType;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.ILabelableFieldMapping;

/**
 * Mapeamento de um campo da entidade (attributo ou relacionamento) para a edição.<br>
 * Deve haver apenas uma configuração de atributo, atributo virtual ou relacionamento.
 *
 * @author Thiago Gesser
 */
public interface IEditingFieldMapping extends ILabelableFieldMapping {

    /**
     * <b>Configuração opcional.</b><br>
     * Obtém o texto de ajuda que é exibido juntamente ao campo.
     *
     * @return o texto de ajuda obtido ou <code>null</code> se não deve haver ajuda no campo.
     */
    String getHelp();

	/**
	 * <b>Configuração opcional, sendo que o padrão é <code>true</code>.</b><br>
	 * Determina se o campo poderá ser editado.
	 * 
	 * @return <code>true</code> se o campo puder ser editado e <code>false</code> caso contrário.
	 */
	boolean isEditable();
	
	/**
	 * <b>Configuração opcional, sendo que o padrão é <code>false</code>.</b><br>
	 * Determina se o campo deverá ser escondido.
	 * 
	 * @return <code>true</code> se o campo deve ser escondido e <code>false</code> caso contrário.
	 */
	boolean isHidden();

	/**
	 * <b>Mutuamente exclusivo com o relacionamento, o relacionamento virtual e o atributo virtual.</b><br>
	 * Obtém o nome do atributo da entidade referenciado por este mapeamento.
	 *
	 * @return o atributo obtido.
	 */
	String[] getAttribute();
	
	/**
	 * <b>Mutuamente exclusivo com o atributo, o atributo virtual, relacionamento virtual.</b><br>
	 * Obtém o nome do relacionamento da entidade referenciado por este mapeamento.
     *
	 * @return o relacionamento obtido.
	 */
	String[] getRelationship();
	
	/**
	 * <b>Mutuamente exclusivo com o atributo, o relacionamento e o relacionamento virtual.</b><br>
	 * Obtém o atributo virtual que este mapeamento representa.<br>
	 * Um atributo virtual não está ligado à fonte de dados, podendo ser utilizado para apresentar / coletar dados que
	 * não serão salvos no registro da entidade.
	 * 
	 * @return o atributo virtual obtido.
	 */
	IVirtualAttribute getVirtualAttribute();
	
	/**
	 * <b>Mutuamente exclusivo com o atributo e o relacionamento.</b><br>
	 * Obtém o relacionamento virtual que este mapeamento representa.<br>
	 * Um relacionamento virtual não está ligado à fonte de dados, podendo ser utilizado para apresentar / coletar dados que
	 * não serão salvos no registro da entidade.
	 * 
	 * @return o atributo virtual obtido.
	 */
	IVirtualRelationship getVirtualRelationship();
	
	/**
	 * <b>Configuração opcional.</b><br>
	 * <br>
	 * Obtém a configuração que torna o campo uma enumerarção. Isto significa que seu valor será definido através
	 * de uma escolha em uma lista de valores predeterminados.
	 * 
	 * @return a configuração de enumeração obtida.
	 */
	IEditingFieldEnum getEnum();

	/**
	 * <b>Configuração opcional, proibida se o campo for uma enumeração.</b><br>
	 * <br>
	 * Obtém um {@link InputType} customizado para o campo de edição. Só pode ser utilizado se o mapeamento referenciar um atributo dos seguintes tipos e de
	 * acordo com suas restrições:
	 * <ul>
	 * 	<li><b>texto</b>: qualquer tipo pode ser utilizado;</li>
	 * 	<li><b>inteiro</b>: apenas flags de números podem ser utilizadas, sendo que o {@link InputType#TYPE_CLASS_NUMBER} é aplicado automaticamente;</li>
	 * 	<li><b>decimal</b>: apenas flags de números podem ser utilizadas, sendo que o {@link InputType#TYPE_CLASS_NUMBER} e o
	 * 	{@link InputType#TYPE_NUMBER_FLAG_DECIMAL} são aplicados automaticamente.</li>
     * </ul>
	 *
	 * @return o inteiro que representando o InputType ou <code>null</code> se o campo não deve utilizar esta funcionalidade.
	 */
	Integer getInputType();

	/**
	 * <b>Configuração opcional, permitida apenas para campos do tipo <code>inteiro</code> ou <code>decimal</code>.</b><br>
	 * <br>
	 * Define o uso de botões para incrementar/decrementar o valor do campo, facilitando a edição. Só pode ser utilizado se o mapeamento
	 * referenciar um campo do tipo <code>inteiro</code> ou <code>decimal</code>.
	 *
	 * @return o número que deve ser incrementado/decrementado ou <code>null</code> se o campo não deve utilizar esta funcionalidade.
	 */
	Double getIncremental();
}
