# Dados de entidades com OpenMobster

Componente de dados de entidades baseado no [OpenMobster](https://github.com/ZalemSoftware/OpenMobster).<br>
Provê sincronização com a nuvem, persistência local para acesso offline e API simplificada de manipulação dos dados.

<br>
## Configurações

Cada entidade precisa ter seus campos e relacionamentos definidos em um arquivo JSON. Por enquanto, a estrutura completa desta configuração pode ser vista a partir de sua [representação POJO](https://zalemsoftware.github.io/Ymir/ymir.client-android.entity.data-openmobster/br/com/zalem/ymir/client/android/entity/data/openmobster/metadata/EntityMetadataConfig.html).<br>
Recomenda-se que os arquivos JSON fiquem na pasta `raw` de recursos do Android e que a nomenclatura seja `<nome da entidade>_metadata.json`.<br>
<br>
Além disso, o OpenMobster precisa que os canias de dados sejam definidos em um arquivo chamado `openmobster-app.xml`, que deve estar na raiz do classpath. Para isto, recomenda-se criar uma pasta `resources`.<br>
Os canais representam entidades de dados para o OpenMobster, sendo utilizados também para a comunicação com a nuvem.

#### openmobster-app.xml
```xml
<app-conf>
    <channels>
        <channel name='canal_da_entidade'/>
    </channels>
</app-conf>
```

<br>
> Por enquanto, documentações mais completas sobre como utilizar este componente podem ser vistas no [aplicativo de exemplo](../ymir.sample-offline#data).

<br>
## Dependências

Este componente depende dos seguintes módulos:
* [Comuns](../ymir.client-android.commons)
