package br.com.zalem.ymir.client.android.entity.ui.configuration.editing;

/**
 * Permissões de operações de edição para uma determinada entidade ou grupo de registros.<br>
 * Por padrão, todas são negadas.
 *
 * @author Thiago Gesser
 */
public interface IEditingPermissions {

	/**
	 * Obtém a permissão referente à criação de registros.
	 * 
	 * @return <code>true</code> se registros podem ser criados e <code>false</code> caso contrário.
	 */
	boolean canCreate();
	
	/**
	 * Obtém a permissão referente à alteração de registros.
	 * 
	 * @return <code>true</code> se registros podem ser alterados e <code>false</code> caso contrário.
	 */
	boolean canUpdate();
	
	/**
	 * Obtém a permissão referente à exclusão de registros.
	 * 
	 * @return <code>true</code> se registros podem ser excluídos e <code>false</code> caso contrário.
	 */
	boolean canDelete();
}
