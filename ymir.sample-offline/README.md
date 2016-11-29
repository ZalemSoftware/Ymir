# Como utilizar
> English docs in the near future.

Passo a passo de como criar uma nova aplicação utilizando o Ymir. Foram utilizadas partes simplificadas do `ymir.sample-offline`, um aplicativo de registro de gastos sem conexão com a nuvem. Para conhecer recursos mais avançados do Ymir, recomenda-se o estudo dos fontes/configurações deste aplicativo de exemplo .
>Em breve, um aplicativo exemplo com sincronização de dados com a nuvem.

## Índice
  0. [Gradle](#gradle)
  0. [Dados das entidades](#data)
  0. [Interfaces das entidades](#ui)
  0. [Perspectivas](#perspectives)
  0. [Módulo](#module)
  0. [Manifest](#manifest)


<br>
<a name="gradle"/>
## 1. Gradle

A configuração abaixo determina que a aplicação irá utilizar o [componente de interfaces de entidades](../ymir.client-android.entity.ui). Para isto, também é necessário definir como as interfaces serão configureadas (JSON) e qual será o provedor de dados (OpenMobster).<br>
O Ymir utiliza o [RoboGuice](https://github.com/roboguice/roboguice) para a injeção de dependências de seus componentes (@Inject). A aplicação também pode utilizar este mecanismo em suas próprias classes, sendo necessário apenas algumas configurações adicionais.

```gradle
dependencies {
    //Utiliza UIs prontas para a manipulação dos dados das entidades.
    compile 'br.com.zalem.ymir:ymir.client-android.entity.ui:1.0.0'
    //Configura as interfaces através de arquivos JSON.
    compile 'br.com.zalem.ymir:ymir.client-android.entity.ui.configuration-json:1.0.0'
    //Provê os dados para as interfaces com o OpenMobster.
    compile 'br.com.zalem.ymir:ymir.client-android.entity.data-openmobster:1.0.0'
    
    //Necessário apenas se a aplicação possuir classes que utilizam injeção de dependências (@Inject).
    provided 'org.roboguice:roboblender:3.0.1'
}

//Necessário apenas se a aplicação possuir classes que utilizam injeção de dependências (@Inject).
tasks.withType(JavaCompile) { task ->
    //Configura a geração do banco de anotações, identificado pelo pacote da aplicação.
    options.compilerArgs << "-AguiceAnnotationDatabasePackageName=br.com.zalem.ymir.sample.offline"
}

```


<br>
<a name="data"/>
## 2. Dados das entidades

O [componente de dados do OpenMobster](../ymir.client-android.entity.data-openmobster) necessita que cada entidade tenha seus campos e relacionamentos definidos em um arquivo JSON. Por enquanto, a estrutura completa desta configuração pode ser vista em sua [representação POJO](../ymir.client-android.entity.data-openmobster/src/main/java/br/com/zalem/ymir/client/android/entity/data/openmobster/metadata/EntityMetadataConfig.java).

Recomenda-se que os arquivos JSON fiquem na pasta `raw` de recursos do Android e que a nomenclatura seja `<nome da entidade>_metadata.json`, de acordo com o exemplo:

#### product_metadata.json

```json
{
	"name": "Product",
	"channel": "ymir_sample_Product",
	
	"attributes": [{
        "name": "name",
        "type": "TEXT"
    }, {
	  	"name": "price",
	  	"type": "DECIMAL"
	}, {
        "name": "picture",
        "type": "IMAGE"
    }],
	
	"relationships": [{
        "name": "expenses",
        "type": "ASSOCIATION_ARRAY",
        "entity": "Expense",
        "mappedBy": "product"
    }]
}
```

<br>
Para finalizar, o OpenMobster precisa que os canias de dados sejam definidos em um arquivo chamado `openmobster-app.xml`, que deve estar na raiz do classpath. Para isto, recomenda-se criar uma pasta de `resources`, conforme [aqui](src/main/resources).<br>
Os canais também são utilizados para a sincronização de dados com a nuvem, o que será demonstrado posteriormente em uma aplicação de exemplo online.

#### openmobster-app.xml
```xml
<app-conf>
    <channels>
        <channel name='ymir_sample_Product'/>
        <channel name='ymir_sample_Place'/>
        <channel name='ymir_sample_Expense'/>
    </channels>
</app-conf>
```


<br>
<a name="ui"/>
## 3. Interfaces das entidades
O [componente de interfaces de entidades](../ymir.client-android.entity.ui) necessita que cada entidade tenha suas telas configuradas. Esta aplicação utiliza a versão [JSON da configuração](../ymir.client-android.entity.ui.configuration-json), mas há a intenção de disponibilizar uma outra forma em XML (a fim de melhor utilizar os recursos string do Android). Por enquanto, a estrutura completa desta configuração pode ser vista em sua [representação POJO](ymir.client-android.entity.ui/ymir.client-android.entity.ui.configuration/src/main/java/br/com/zalem/ymir/client/android/entity/ui/configuration/IEntityConfig.java).

Recomenda-se que os arquivos JSON fiquem na pasta `raw` de recursos do Android e que a nomenclatura seja `<nome da entidade>_config.json`, de acordo com o exemplo:

#### product_config.json
```
{
	"name": "Product",
	"displayName": {
		"singular": "Produto",
		"plural": "Produtos"
	},
	
	//Define os valores padrão para os campos nas configurações, evitando repetições.
	"fieldsDefaults": [{
		"name": "name",
		"label": "Nome"
    	}, {
		"name": "price",
		"label": "Preço",
		"mask": "CURRENCY_DEFAULT"
	}],
	
	//Configurações de listagem da entidade, como o layout de cada linha da lista e sua ordenação.
	"list": {
		"layout": {
			"type": "LIST_LAYOUT_3",
			"fields": [{
				"attribute": "picture",
				"layoutField": "IMAGE1"
			}, {
				"attribute": "name",
				"layoutField": "TEXT1"
			}, {
				"attribute": "description",
				"layoutField": "TEXT2"
			}, {
				"attribute": "price",
				"layoutField": "TEXT3"
			}]
		},
		"order": {
			"fields": [{
				"attribute": "name"
			}, {
				"attribute": "price",
				"asc": false
			}]
		}
	},
	
	//Configurações do detalhamento da entidade, como o layout do cabeçalho e os demais campos.
	"detail": {
		"header": {
			"type": "DETAIL_LAYOUT_1",
			"fields": [{
			    "attribute": "picture",
			    "layoutField": "IMAGE1"
			}, {
			    "attribute": "name",
			    "layoutField": "TEXT1"
			}, {
			    "attribute": "type",
			    "layoutField": "TEXT2"
			}, {
			    "attribute": "price",
			    "layoutField": "TEXT3"
			}]
		},
		"fields": [{
			"attribute": "name"
		}]
	},

	//Configurações de edição da entidade, como as permissões de edição e os campos editáveis.
	"editing": {
		"local": {
			"canCreate": true,
			"canUpdate": true,
			"canDelete": true
		},
		"fields": [{
			"attribute": "name",
			"inputType": "TYPE_TEXT_FLAG_CAP_SENTENCES"
		}, {
			"attribute": "price"
		}, {
			"attribute": "picture"
		}]
	}
}
```

<br>
Além disso, é possível configurar `listeners de eventos` para adicionar regras de negócio, validações e cálculos nas interfaces das entidades. Para isto, basta declarar uma classe que implemente a interface `IEntityUIEventListener` e configurá-la no Módulo, de acordo com o exemplo:

#### ProductEventListener.java
```java
public final class ProductEventListener extends EntityUIEventListenerAdapter {

    @Override
    public String getEntityName() {
        return PRODUCT_ENTITY;
    }
    
    
    @Override
    public void onEditRecordAttribute(IEntityRecord record, String attributeName, IEntityEditingErrorHandler errorHandler) {
        switch (attributeName) {
            case PRODUCT_ATTRIBUTE_NAME:
                ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_NAME, errorHandler);
                break;
    
            case PRODUCT_ATTRIBUTE_PRICE:
                ValidationUtils.validatePositive(record, PRODUCT_ATTRIBUTE_PRICE, errorHandler);
                break;
        }
    }
    
    @Override
    public boolean beforeSaveRecord(IEntityRecord record, boolean sync,	IEntityEditingErrorHandler errorHandler) {
        //Revalida todos os atributos.
        ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_NAME, errorHandler);
        ValidationUtils.validatePositive(record, PRODUCT_ATTRIBUTE_PRICE, errorHandler);
    
        return !errorHandler.isEmpty();
    }
}
```


<br>
<a name="perspectives"/>
## 4. Perspectivas
Cada tela do [componente de interfaces de entidades](../ymir.client-android.entity.ui) é uma perspectiva, um tipo de fragmento que atua como uma Activity. O [componente de perspectivas](../ymir.client-android.perspective) dispõe uma forma robusta de configuração, possibilitando definir qual perspectiva será aberta para cada ação de cada entidade. Desta forma, é possível utilizar as perspectivas já existentes do componente de interfaces, definir versões customiadas das perspectivas já existentes ou até criar perspectivas totalmente novas.<br>
As perspectivas da aplicação devem ser definidas em um arquivo XML, dentro da pasta de recursos `xml` do Android, conforme o exemplo:

#### perspectives.xml
```xml
<perspectives xmlns:ymir="http://schemas.android.com/apk/res-auto">

	<!-- Utiliza a perspectiva padrão para a listagem/detalhamento da entidade Produto (de acordo com os actions e category) -->
	<perspective ymir:title="@string/product_list_perspective_title"
		ymir:className="br.com.zalem.ymir.client.android.entity.ui.perspective.EntityListDetailPerspective"
		ymir:launchMode="single">
		<intent-filter>
			<action ymir:name="br.com.zalem.ymir.client.android.entity.ui.perspective.LIST_DETAIL" />
			<action ymir:name="br.com.zalem.ymir.client.android.entity.ui.perspective.LIST" />
			<category ymir:name="Product" />
		</intent-filter>
		
		<argument ymir:key="ENABLE_FAB_ADD" />
	</perspective>
	
	<!-- Utiliza uma perspectiva customizada para a edição da entidade Produto, que estende a perspectiva padrão  "br.com.zalem.ymir.client.android.entity.ui.perspective.EntityEditingPerspective" -->
	<perspective ymir:title="@string/product_editing_perspective_title"
		ymir:className="br.com.zalem.ymir.sample.offline.perspective.ProductCustomEditingPerspective">
		<intent-filter>
		    <action ymir:name="br.com.zalem.ymir.client.android.entity.ui.perspective.EDITING" />
		    <category ymir:name="Product" />
		</intent-filter>
	</perspective>
	
	<!-- ... -->
</perspectives>
```

<br>
Além disso, o componente de perspectivas dispõe o menu de navegação lateral, que permite a troca entre as perpectivas iniciais. O menu deve ser configurado em um arquivo XML, dentro da pasta de recursos `xml` do Android, conforme exemplo:

#### navigation_menu.xml
```xml
<ymirMenu xmlns:ymir="http://schemas.android.com/apk/res-auto">
    <group ymir:id="@+id/navigation_menu_group_1">
        <item ymir:title="@string/product_list_perspective_title"
              ymir:icon="@drawable/ic_menu_product"
              ymir:color="@color/primary_product">
            <intent ymir:action="br.com.zalem.ymir.client.android.entity.ui.perspective.LIST_DETAIL"
                    ymir:category="Product"
                    ymir:flags="clear_backstack" />
        </item>
    </group>
    
    <!-- ... -->
</ymirMenu>
```


<br>
<a name="module"/>
## 5. Módulo

O Ymir utiliza o [RoboGuice](https://github.com/roboguice/roboguice) para controlar injeção de dependências entre os componentes (como o gerenciador de dados ou o  gerenciador de configurações). Desta forma, é possível trocar os componentes originais por mocks ou até por componentes próprios de forma robusta, sem a necessidade de alterar cada classe que os utilizam.<br>
Para isto, é necessário declarar uma classe que estenda o `AbstractModule` e vincular os componentes no método `configure`, conforme no exemplo:

#### SampleOfflineModule.java
```java
public final class SampleOfflineModule extends AbstractModule {

	private final Application application;

	public SampleOfflineModule(Application application) {
		this.application = application;
	}

	@Override
	protected void configure() {
		/*
		 * Vincula o gerenciador de dados das entidades (baseado no OpenMobster).
		 * Necessário para o componente de interfaces de entidades.
		 */
		MobileBeanEntityDataManager dataManager = MobileBeanEntityDataManager.fromJsonResources(new ObjectMapper(), application,
			//Utiliza os metadados declarados previamente nos arquivos json.
			R.raw.product_metadata,
			R.raw.place_metadata,
			R.raw.expense_metadata
		);
		bind(IEntityDataManager.class).toInstance(dataManager);

		//Configura o ativador do OpenMobster na aplicação.
		application.registerActivityLifecycleCallbacks(OpenMobsterActivator.createOfflineActivator());


		/*
		 * Vincula o gerenciador de configurações das interfaces.
		 * Necessário para o componente de interfaces de entidades.
		 */
		JsonEntityUIConfigManager configManager = JsonEntityUIConfigManager.fromJsonResources(objectMapper, application,
			//Utiliza as configurações declaradas previamente nos arquivos json.
			R.raw.product_config,
			R.raw.place_config,
			R.raw.expense_config
		);
		bind(IEntityUIConfigManager.class).toInstance(configManager);

		//Aplica os valores padrão em cada configuração (definidas nos "fieldsDefaults" de cada entidade).
		configManager.applyFieldsDefaults(dataManager);


		/*
		 * Vincula o gerenciador de eventos da aplicação.
		 * Necessário apenas se a aplicação utilizar listeners de eventos.
		 */
		ProductEventListener productListener = new ProductEventListener();
		BasicEntityUIEventManager eventManager = new BasicEntityUIEventManager(productListener)
		bind(IEntityUIEventManager.class).toInstance(eventManager);
	}
}
```


<br>
<a name="manifest"/>
## 6. Manifest

Por fim, algumas configurações são necessárias no manifest da aplicação Android. O RoboGuice precisa saber o caminho do Módulo da aplicação e o [componente de perspectivas](../ymir.client-android.perspective) precisa ter sua Activity configurada. Esta Activity geralmente é o ponto de entrada da aplicação, já que controla a execução de todas as perspectivas declaradas anteriormente. Entretanto, ainda é possível declarar e abrir outras Activities normalmente. Segue um exemplo de manifest:

#### AndroidManifest.xml
```xml
<manifest package="br.com.zalem.ymir.sample.offline"
  	  xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:theme="@style/Theme.Ymir.Light.Entity"
        android:label="@string/app_name"
        android:icon="@drawable/ic_launcher">

        <!-- Determina o Módulo que configura as dependências injetadas. -->
        <meta-data
            android:name="roboguice.modules"
            android:value="br.com.zalem.ymir.sample.offline.SampleOfflineModule" />
        <!-- Determina os bancos de anotações da aplicação, sendo identificados pelo pacote do módulo.
	     Por enquanto, apenas o "br.com.zalem.ymir.client.android.entity.ui" é necessário.
	     Se a aplicação utilizar injeção de dependências, também é necessário colocar seu pacote. -->
        <meta-data
            android:name="roboguice.annotations.packages"
            android:value="br.com.zalem.ymir.sample.offline,br.com.zalem.ymir.client.android.entity.ui"/>

        <!-- Declara a Activity que gerencia todas as perspectivas (telas) da aplicação. -->
        <activity android:name="br.com.zalem.ymir.client.android.perspective.PerspectiveActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <!-- Configura o xml com as perspectivas definidas anteriormente -->
            <meta-data
                android:name="br.com.zalem.ymir.client.android.perspective.perspectives"
                android:resource="@xml/perspectives" />
            <!-- Configura o xml com o menu lateral de navegação definido anteriormente -->
            <meta-data
                android:name="br.com.zalem.ymir.client.android.perspective.navigation-menu"
                android:resource="@xml/navigation_menu" />
        </activity>
    </application>
</manifest>
```

<br>
Pronto! Agora é só rodar a aplicação feita com o framework Ymir.
<br>Para conhecer mais recursos, confira as configurações da aplicação de exemplo `ymir.sample-offline`, que é uma versão avançada do que foi mostrado neste tutorial.
