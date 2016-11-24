# É necessário manter as classes/interfaces referenciadas em atributos com @Inject para que a injeção funcione corretamente.
-keep class br.com.zalem.ymir.client.android.entity.data.IEntityDataManager
-keep class br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager
-keep class br.com.zalem.ymir.client.android.entity.ui.event.IEntityUIEventManager
-keep class br.com.zalem.ymir.client.android.entity.ui.search.ISearchableManager

# É necessário manter o SearchView para que a ação de pesquisa funcione corretamente.
-keep class android.support.v7.widget.SearchView { *; }

# Evita o warning de classes não utilizadas na lib: https://github.com/square/okio/issues/60
-dontwarn okio.**