# Como utilizar
> English docs in the near future.

Segue o passo-a-passo de como criar uma nova aplicação utilizando o Ymir. Os passos a seguir utilizam como exemplo este aplicativo, um simples registrador de gastos sem conexão com a nuvem.

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

## 2. Dados

O [componente de dados do OpenMobster](../ymir.client-android.entity.data-openmobster) precisa que cada entidade tenha seus campos e relacionamentos definidos em um arquivo JSON. A estrutura completa desta configuração pode ser vista em sua [representação POJO](../ymir.client-android.entity.data-openmobster/src/main/java/br/com/zalem/ymir/client/android/entity/data/openmobster/metadata/EntityMetadataConfig.java).

O recomendado é que os arquivos json fiquem na pasta `raw` de recursos do Android e que a nomenclatura seja `<nome da entidade>_metadata.json`, de acordo com o exemplo abaixo.

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



//TODO o resto do tutorial
