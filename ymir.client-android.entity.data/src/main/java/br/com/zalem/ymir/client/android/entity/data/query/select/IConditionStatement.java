package br.com.zalem.ymir.client.android.entity.data.query.select;

import java.io.Serializable;

import br.com.zalem.ymir.client.android.entity.data.SyncStatus;

/**
 * Instrução de condição de uma query de seleção de dados.<br>
 * Permite a aplicação de restrições para a filtragem dos dados.
 * 
 * @author Thiago Gesser
 */
public interface IConditionStatement {

	/**
	 * Nega a próxima restrição da query.
	 * 
	 * @return o IConditionStatement para definir a restrição que será negada.
	 */
	IConditionStatement not();

	/**
	 * Abre um escopo na query, fazendo com que as restrições que estejam dentro dele sejam consideradas de maneira isolada
	 * das demais restrições.<br>
	 * O escopo deve ser fechado através do método {@link IRestrictionStatement#c()}
	 * @return o IConditionStatement para definir as restrições do escopo.
	 */
	IConditionStatement o();
	
	
	/*
	 * Atributos
	 */
	
	/**
	 * Adiciona uma restrição de <i>atributo é igual ao valor</i> na query.
	 * 
	 * @param value valor de tipo compatível com o campo.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo, se o valor não era compatível com o campo
	 * ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement eq(Object value, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo é menor que o valor</i> na query.
	 * 
	 * @param value valor de tipo compatível com o campo.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo, se o valor não era compatível com o campo
	 * ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement lt(Object value, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo é maior que o valor</i> na query.
	 * 
	 * @param value valor de tipo compatível com o campo.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo, se o valor não era compatível com o campo
	 * ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement gt(Object value, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo é menor ou igual ao valor</i> na query.
	 * 
	 * @param value valor de tipo compatível com o campo.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo, se o valor não era compatível com o campo
	 * ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement le(Object value, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo é maior ou igual ao valor</i> na query.
	 * 
	 * @param value valor de tipo compatível com o campo.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo, se o valor não era compatível com o campo
	 * ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement ge(Object value, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo é igual a um dos valores</i> na query.
	 * 
	 * @param values valores de tipo compatível com o campo.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo, se o valor não era compatível com o campo
	 * ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement in(Object[] values, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo está entre o valor1 e o valor2</i> na query.
	 * 
	 * @param value1 primeiro valor de tipo compatível com o campo.
	 * @param value2 segundo valor de tipo compatível com o campo.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo, se o valor não era compatível com o campo
	 * ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement between(Object value1, Object value2, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo contém o texto</i> na query.
	 * 
	 * @param text texto da restrição.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement contains(String text, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo inicia com o texto</i> na query.
	 * 
	 * @param text texto da restrição.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement startsWith(String text, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo termina com o texto</i> na query.
	 * 
	 * @param text texto da restrição.
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement endsWith(String text, String... attrPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>atributo é nulo</i> na query.
	 * 
	 * @param attrPathOrAlias caminho para o atributo ou seu alias. O caminho pode ser diretamente um atributo da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no atributo desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o tipo do atributo não é suportado.
	 */
	IRestrictionStatement isNull(String... attrPathOrAlias);
	
	
	/*
	 * Relacionamentos 
	 */
	
	/**
	 * Adiciona uma restrição de <i>id do relacionamento é igual ao id da restrição</i> na query.
	 * 
	 * @param id identificador da restrição.
	 * @param relPathOrAlias caminho para o relacionamento ou seu alias. O caminho pode ser diretamente um relacionamento da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no relacionamento desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento ou se o tipo de relacionamento não é suportado.
	 */
	IRestrictionStatement rEq(Serializable id, String... relPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>id do relacionamento é igual a um dos ids da restrição</i> na query.
	 * 
	 * @param ids identificadores da restrição.
	 * @param relPathOrAlias caminho para o relacionamento ou seu alias. O caminho pode ser diretamente um relacionamento da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no relacionamento desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento ou se o tipo de relacionamento não é suportado.
	 */
	IRestrictionStatement rIn(Serializable[] ids, String... relPathOrAlias);
	
	/**
	 * Adiciona uma restrição de <i>relacionamento é nulo</i> na query.
	 * 
	 * @param relPathOrAlias caminho para o relacionamento ou seu alias. O caminho pode ser diretamente um relacionamento da entidade
	 * alvo da query ou um caminho que parte dela, navega por relacionamentos singulares e chega no relacionamento desejado.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento ou se o tipo de relacionamento não é suportado.
	 */
	IRestrictionStatement rIsNull(String... relPathOrAlias);

	
	/*
	 * SyncStatus
	 */
	
	/**
	 * Adiciona uma restrição de <i>status de sincronização do registro é igual ao status de sincronização da restrição</i> na query.
	 * 
	 * @param ss status de sincronização da restrição.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IRestrictionStatement ssEq(SyncStatus ss);
	
	/**
	 * Adiciona uma restrição de <i>status de sincronização do registro é igual a um dos status de sincronização da restrição</i> na query.
	 * 
	 * @param sss status de sincronização da restrição.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IRestrictionStatement ssIn(SyncStatus... sss);
	
	
	/*
	 * Id
	 */
	
	/**
	 * Adiciona uma restrição de <i>identificador do registro é igual ao identificador da restrição</i> na query.
	 * 
	 * @param id identificador da restrição.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IRestrictionStatement idEq(Serializable id);
	
	/**
	 * Adiciona uma restrição de <i>identificadore do registro é igual a um dos identificadores da restrição</i> na query.
	 * 
	 * @param ids identificadores da restrição.
	 * @return o {@link IRestrictionStatement} com os métodos disponíveis para continuar a construção da query.
	 */
	IRestrictionStatement idIn(Serializable... ids);
}
