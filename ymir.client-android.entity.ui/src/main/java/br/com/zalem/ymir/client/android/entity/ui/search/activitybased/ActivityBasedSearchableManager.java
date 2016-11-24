package br.com.zalem.ymir.client.android.entity.ui.search.activitybased;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import br.com.zalem.ymir.client.android.entity.ui.search.ISearchableManager;

/**
 * Gerenciador de <code>Searchables</code> baseado na declaração de <code>Searchables</code> em recursos xml 
 * associados à Activities.<br>
 * Cada recurso xml de <code>Searchable</code> deve ser associado a uma Activity (de preferência falsa) declarada no
 * AndroidManifest.xml. Activities falsas podem ter um nome qualquer e serem utilizadas apenas para isto. Este mecanismo
 * basedo em Activities é necessário porque o Android não disponibiliza nenhuma outra forma de obter o Searchable que não
 * seja através da declaração de uma Activity.<br>
 * <br>
 * Os <code>Searchables</code> são configurados neste gerenciador através de um mapa cujo a chave é o nome da entidade
 * e o valor o nome da Activity que contém o <code>Searchable</code> associado.<br>
 * <br>
 * Segue um exemplo da declaração de uma Activity com um <code>Searchable</code> associado no AndroidManifest.xml:
 * <pre>{@code
 *  <activity android:name=".FakeActivity">
 *    <meta-data
 *        android:name="android.app.searchable"
 *        android:resource="@xml/fakeactivity_searchable" />
 *    <intent-filter>
 *        <action android:name="android.intent.action.SEARCH" />
 *    </intent-filter>
 * </activity>
 * }</pre>
 *
 * @author Thiago Gesser
 */
public final class ActivityBasedSearchableManager implements ISearchableManager {
	
	private final Map<String, SearchableInfo> searchables;
	
	public ActivityBasedSearchableManager(Context context, Map<String, String> searchablesConfigs) {
		searchables = new HashMap<>(searchablesConfigs.size());
		
		SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
		for (Entry<String, String> entry : searchablesConfigs.entrySet()) {
			String entityName = entry.getKey();
			String activityName = entry.getValue();
			
			//Se não possui o pacote, tem que colocar o pacote da aplicação.
			if (activityName.startsWith(".")) {
				activityName = context.getApplicationInfo().packageName + activityName; 
			}
		
			ComponentName componentName = new ComponentName(context, activityName);
			SearchableInfo searchableInfo = searchManager.getSearchableInfo(componentName);
			if (searchableInfo == null) {
				throw new IllegalArgumentException("Searchable not found for " + activityName);
			}
			
			searchables.put(entityName, searchableInfo);
		}
	}

	@Override
	public SearchableInfo getSearchableInfo(String entityName) {
		return searchables.get(entityName);
	}

}
