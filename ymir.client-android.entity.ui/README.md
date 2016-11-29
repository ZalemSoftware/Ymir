# Interface de entidades

Componente que disponibiliza telas (perspectivas) prontas de listagem, detalhamento e edição de registros de entidades.

<br>
## Configurações

Cada entidade tem suas telas e layouts definidos através de configurações. Por enquanto, a estrutura completa desta configuração pode ser vista a partir de sua [representação POJO](ymir.client-android.entity.ui.configuration/src/main/java/br/com/zalem/ymir/client/android/entity/ui/configuration/IEntityConfig.java).
Os layouts disponíveis para os registros de lista e os cabeçalhos do detalhamento são:

> Posteriormente, será possível definir layouts de lista/detalhe customizados.

![ymir.client-android.entity.ui - layouts.png](https://s22.postimg.org/6l98bwncx/ymir_client_android_entity_ui_layouts.png)

<br>
Além disso, é possível configurar `listeners de eventos` para adicionar regras de negócio, validações e cálculos nas interfaces das entidades. Para isto, basta declarar uma classe que implemente a interface `IEntityUIEventListener` e configurá-la no Módulo.

> Por enquanto, documentações mais completas sobre como utilizar este componente podem ser vistas no [aplicativo de exemplo](../ymir.sample-offline#ui).

<br>
## Dependências

Este componente depende dos seguintes módulos:
* [Configurações de interface](ymir.client-android.entity.ui.configuration)
* [Dados de entidades](../ymir.client-android.entity.data)
* [Perspectivas](../ymir.client-android.perspective)
* [Comuns](../ymir.client-android.commons)
