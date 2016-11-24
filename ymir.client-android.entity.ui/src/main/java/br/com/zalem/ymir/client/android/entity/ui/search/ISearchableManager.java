package br.com.zalem.ymir.client.android.entity.ui.search;

import android.app.SearchableInfo;

/**
 * Gerenciador de <code>Searchables</code> da aplicação.<br>
 * <code>Searchable</code> é um recurso do Android para definir o comportamento da pesquisa através da ActionBar.
 * As informações de um <code>Searchable</code> são disponibilizadas através de um {@link SearchableInfo}.   
 *
 * @see SearchableInfo
 * 
 * @author Thiago Gesser
 */
public interface ISearchableManager {
	
	/**
	 * Obtém o {@link SearchableInfo} definido para a Entidade.<br>
	 * 
	 * @param entityName nome da entidade.
	 * @return o SearchableInfo obtido ou <code>null</code> caso não haja um SearchableInfo definido para a entidade.
	 */
	SearchableInfo getSearchableInfo(String entityName);
}
