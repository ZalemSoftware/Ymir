package br.com.zalem.ymir.sample.offline;

import android.app.Application;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;

import java.util.HashMap;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.openmobster.cache.impl.LRUImageCache;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadataException;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.OpenMobsterActivator;
import br.com.zalem.ymir.client.android.entity.ui.configuration.EntityConfigException;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.json.JsonEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityUIEventManager;
import br.com.zalem.ymir.client.android.entity.ui.event.basic.BasicEntityUIEventManager;
import br.com.zalem.ymir.client.android.entity.ui.search.ISearchableManager;
import br.com.zalem.ymir.client.android.entity.ui.search.activitybased.ActivityBasedSearchableManager;
import br.com.zalem.ymir.sample.offline.event.ExpenseEventListener;
import br.com.zalem.ymir.sample.offline.event.PlaceEventListener;
import br.com.zalem.ymir.sample.offline.event.ProductEventListener;

/**
 * Módulo de configuração das dependências utilizadas pelo framework, como o gerenciador de dados e eventos.
 *
 * @author Thiago Gesser
 */
public final class SampleOfflineModule extends AbstractModule {

    private final Application application;

    public SampleOfflineModule(Application application) {
        this.application = application;
    }

	@Override
	protected void configure() {
        try {
            //Cria o mapper que será utilizado na conversão das configurações JSON para Objetos.
            ObjectMapper objectMapper = new ObjectMapper().
                    enable(Feature.ALLOW_COMMENTS).
                    enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES).
                    enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);


            /*
             * Vincula o gerenciador de dados das entidades da aplicação (baseado no OpenMobster).
             */
            MobileBeanEntityDataManager dataManager = MobileBeanEntityDataManager.fromJsonResources(objectMapper, application,
                    R.raw.expense_metadata,
                    R.raw.place_metadata,
                    R.raw.product_metadata
            );
            bind(IEntityDataManager.class).toInstance(dataManager);

            //Configura um cache de imagens com 1/8 da memória total.
            int imageCacheSize = (int) (Runtime.getRuntime().maxMemory() / 8);
            dataManager.setImageCache(new LRUImageCache(imageCacheSize));

            //Configura o ativador do OpenMobster na aplicação.
            application.registerActivityLifecycleCallbacks(OpenMobsterActivator.createOfflineActivator());


            /*
             * Vincula o gerenciador de configurações das entidades da aplicação
             */
            JsonEntityUIConfigManager configManager = JsonEntityUIConfigManager.fromJsonResources(objectMapper, application,
                    R.raw.expense_config,
                    R.raw.place_config,
                    R.raw.product_config
            );
            bind(IEntityUIConfigManager.class).toInstance(configManager);

            //Aplica os valores padrão em cada configuração (definidas nos "fieldsDefaults" de cada entidade).
            configManager.applyFieldsDefaults(dataManager);


            /*
             * Vincula o gerenciador de eventos da aplicação.
             */
            ExpenseEventListener expenseListener = new ExpenseEventListener();
            PlaceEventListener placeListener = new PlaceEventListener();
            ProductEventListener productListener = new ProductEventListener();
            bind(IEntityUIEventManager.class).toInstance(new BasicEntityUIEventManager(expenseListener, placeListener, productListener));

            //Os listeners também podem utilizar os componentes injetáveis.
            requestInjection(expenseListener);


            /*
             * Vincula o gerenciador de Searchables da aplicação.
             */
            Map<String, String> searchablesConfigs = new HashMap<>();
            //Configura de acordo com o nome da Activity declarada no manifest.
            searchablesConfigs.put(EntityConstants.EXPENSE_ENTITY, ".ExpenseSearchableActivity");
            searchablesConfigs.put(EntityConstants.PLACE_ENTITY, ".PlaceSearchableActivity");
            searchablesConfigs.put(EntityConstants.PRODUCT_ENTITY, ".ProductSearchableActivity");
            bind(ISearchableManager.class).toInstance(new ActivityBasedSearchableManager(application, searchablesConfigs));
        } catch (EntityConfigException | EntityMetadataException e) {
            throw new RuntimeException(e);
        }
	}
}
