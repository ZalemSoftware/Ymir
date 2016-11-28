# Como utilizar
> English docs in the near future.

Segue o passo-a-passo de como criar uma nova aplicação utilizando o Ymir. Os passos a seguir utilizam como exemplo este aplicativo, um simples registrador de gastos sem conexão com a nuvem.

<br>
## 1. Gradle

A configuração abaixo determina que a aplicação irá utilizar o componente de Interfaces de Entidades. Para isto, também é necessário definir como as interfaces serão configureadas (JSON) e qual será o provedor de dados (OpenMobster).<br>
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
## 3. Interfaces das entidades
O [componente de intefaces das entidades](../ymir.client-android.entity.ui) necessita que cada entidade tenha suas telas configuradas. Esta aplicação utiliza a versão [JSON da configuração](../ymir.client-android.entity.ui.configuration-json), mas há a intenção de disponibilizar uma outra forma em XML (a fim de melhor utilizar os recursos string do Android). Por enquanto, a estrutura completa desta configuração pode ser vista em sua [representação POJO](ymir.client-android.entity.ui/ymir.client-android.entity.ui.configuration/src/main/java/br/com/zalem/ymir/client/android/entity/ui/configuration/IEntityConfig.java).

Recomenda-se que os arquivos JSON fiquem na pasta `raw` de recursos do Android e que a nomenclatura seja `<nome da entidade>_config.json`, de acordo com o exemplo:

#### product_config.json
```json
{
	"name": "Product",
	"displayName": {
		"singular": "Produto",
		"plural": "Produtos"
	},

	"fieldsDefaults": [{
		"name": "name",
		"label": "Nome"
    	}, {
		"name": "price",
		"label": "Preço",
		"mask": "CURRENCY_DEFAULT"
	}],
	
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
    
            case PRODUCT_ATTRIBUTE_TYPE:
                ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_TYPE, errorHandler);
                break;
        }
    }
    
    @Override
    public boolean beforeSaveRecord(IEntityRecord record, boolean sync,	IEntityEditingErrorHandler errorHandler) {
        //Revalida todos os atributos.
        ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_NAME, errorHandler);
        ValidationUtils.validatePositive(record, PRODUCT_ATTRIBUTE_PRICE, errorHandler);
        ValidationUtils.validateNotEmpty(record, PRODUCT_ATTRIBUTE_TYPE, errorHandler);
    
        return !errorHandler.isEmpty();
    }
}
```


<br>
## 4. Perspectivas
Cada tela do [componente de intefaces das entidades](../ymir.client-android.entity.ui) é uma perspectiva, um tipo de fragmento que atua como uma Activity. O [componente de perspectivas](../ymir.client-android.perspective) dispõe uma forma robusta de configuração, possibilitando definir qual perspectiva será aberta para cada ação de cada entidade. Desta forma, é possível utilizar as perspectivas já existentes do componente de interfaces, definir versões customiadas das perspectivas já existentes ou até criar perspectivas totalmente novas.<br>
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
## 5. Módulo




//TODO o resto do tutorial
