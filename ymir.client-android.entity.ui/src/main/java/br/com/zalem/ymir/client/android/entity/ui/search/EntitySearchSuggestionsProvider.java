package br.com.zalem.ymir.client.android.entity.ui.search;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.net.Uri;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Provedor de sugestões de pesquisas recentes feitas nos registros das entidades.<br>
 * É baseada totalmente na lógica interna de sua classe pai {@link SearchRecentSuggestionsProvider}, então este provedor pode
 * parar de funcionar em versões futuras do Android.<br>
 * Este tipo de extensão errada foi necessário pois apesar do Android disponibilizar um mecanismo robusto para a definição de provedores de sugestão,
 * a classe disponibilizada para prover sugestões recentes ({@link SearchRecentSuggestionsProvider}) junta todas as sugestões
 * recebidas de pesquisas diferentes em uma única fonte de dados. Isto faz com que pesquisas digitadas em uma Activity acabem aparecendo em outras,
 * tornando o seu uso inviável.<br>
 * Esta classe tem o objetivo de atender temporariamente a demanda básica que a {@link SearchRecentSuggestionsProvider} não atende.
 * A meta é a implementação própria de um provedor de sugestões de pesquisas recentes.<br>
 * <br>
 * O funcionamento do mecanismo de diferenciação das sugestões de pesquisa se baseia no campo secundário (display2) declarado na tabela criada pela classe pai.
 * Este campo secundário normalmente é utilizado para mostar uma segunda linha nas sugestões de pesquisa.
 * O nome da entidade em que a pesquisa está sendo baseada será salvo no "display2" e depois no momento da consulta das sugestões este campo
 * será utilizado para a filtragem.<br>
 * Para isto, o nome da entidade deve ser declarado no campo "searchSuggestSelection" do <code>Searchable</code> utilizado pela Activity que possui pesquisa.
 * Desta forma, o nome da entidade será passado automaticamente para este provedor no momento da consulta das sugestões recentes.
 * Por fim, depois de cada pesquisa bem sucedida, o método {@link SearchRecentSuggestions#saveRecentQuery(String, String)} deve ser chamado,
 * utilizando o termo da pesquisa como primeiro parâmetro e o nome da entidade declarado no Searcable como segundo.
 *
 * @author Thiago Gesser
 */
public final class EntitySearchSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = EntitySearchSuggestionsProvider.class.getName();
    public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
    
    private static final String SUGGESTIONS_URI_PATH = "suggestions";
    private static final String DISPLAY_FIELD = "display1";
    private static final String ENTITY_FIELD = "display2";
    private static final String ENTITY_SELECTION = ENTITY_FIELD + " = ?";
    private static final String DISPLAY_ENTITY_SELECTION = DISPLAY_FIELD + " LIKE ? AND " + ENTITY_SELECTION;
    private static final String[] PROJECTIONS = new String [] {
										        "0 AS " + SearchManager.SUGGEST_COLUMN_FORMAT,
										        "'android.resource://system/" + android.R.drawable.ic_menu_recent_history + "' AS " + SearchManager.SUGGEST_COLUMN_ICON_1,
										        DISPLAY_FIELD + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
										        "query AS " + SearchManager.SUGGEST_COLUMN_QUERY,
										        "_id"};

    

    public EntitySearchSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
    
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selectionEntity, String[] selectionArgs, String sortOrder) {
    	//Precisa alterar o path se não o super faz a query de maneira totalmente específica para o SearchManager.
    	uri = uri.buildUpon().path(SUGGESTIONS_URI_PATH).build();
    
    	//Seleciona de acordo com o filtro (se houver) e o nome da entidade declarado no "searchSuggestSelection" do searchable.
    	String[] args;
    	String selection;
    	String filter = selectionArgs[0];
    	if (TextUtils.isEmpty(filter)) {
    		selection = ENTITY_SELECTION;
    		args = new String[] {selectionEntity};
    	} else {
    		selection = DISPLAY_ENTITY_SELECTION;
    		args = new String[] {'%' + filter + '%', selectionEntity};
    	}
    	
    	return super.query(uri, PROJECTIONS, selection, args, sortOrder);
    }
}