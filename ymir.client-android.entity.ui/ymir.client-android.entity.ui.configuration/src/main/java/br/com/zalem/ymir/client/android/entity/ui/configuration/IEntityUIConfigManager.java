package br.com.zalem.ymir.client.android.entity.ui.configuration;


/**
 * Gerenciador de configurações da aplicação.<br>
 * Possui o registro das configurações da aplicação referentes às entidades, sendo possível obtê-las através do método
 * {@link #getEntityConfig(String)}. Não é obrigatório que toda entidade possua uma configuração definida. 
 *
 * @author Thiago Gesser
 */
public interface IEntityUIConfigManager {

	/**
	 * Obtém a configuração da aplicação referente a uma entidade, de acordo com o seu nome.
	 * 
	 * @param entityName nome da entidade.
	 * @return a configuração obtida ou <code>null</code> se não houver configuração definida para a entidade.
	 */
	IEntityConfig getEntityConfig(String entityName);
}
