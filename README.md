# Ymir
>English docs in the near future.

Ymir é um framework nativo Android para a construção facilitada de aplicativos CRUD com Material Design. Suporta a utilização conjunta com outras bibliotecas e de toda API Android (4.0+). O framework é dividido nos seguintes componentes, o que permite o uso isolado de determinadas funcionalidades:

#### Entidades
* **Dados**: sincronização com a nuvem, persistência local para acesso offline e API de manipulação.
* **Interface**: telas prontas de listagem, detalhamento e edição de registros. Configuradas através de arquivos JSON, permitem a escolha de layouts, a disposição de campos e etc. Aplicação de regras de negócio através de listeners de eventos.

#### Utilitários
* **Perspectivas**: sistema de telas baseada em fragmentos com funcionamento similar à Activities (abertura por Intent, com suporte a category/action). Oferece transições de telas mais fluídas e um menu de navegação lateral configurável.
* **Comuns**: fragmentos e widgets genéricos, classes utilitárias, além de melhorias/correções em componentes do Android.

<br>
## [Aplicativo exemplo](https://github.com/ZalemSoftware/Ymir/tree/master/ymir.sample-offline)

Um simples aplicativo de Registro de Gastos que demonstra [**como utilizar**](https://github.com/ZalemSoftware/Ymir/tree/master/ymir.sample-offline) o Ymir. Teste diretamente com o [APK](https://drive.google.com/open?id=0B9jY7lzj877VNXhRT3NYMU15dHc).

![ymir.sampe-offline - screenshots](https://s11.postimg.org/dgnkzbvcj/ymir_sampe_offline_screenshots.png)


## Gradle

Em breve, o Ymir estará disponível no repositório JCenter. Seguem os cenários de utilização mais comuns dos componentes do framework.

#### Framework completo
> Os componentes de interfaces, perspectivas e comuns são adicionados de forma transitiva com esta configuração.

```gradle
compile 'br.com.zalem.ymir:ymir.client-android.entity.ui.configuration-json:1.0.0'
compile 'br.com.zalem.ymir:ymir.client-android.entity.data-openmobster:1.0.0'
```


<br>
#### Apenas componente de dados
```gradle
compile 'br.com.zalem.ymir:ymir.client-android.entity.data-openmobster:1.0.0'
```


#### Apenas componente de perspectivas
```gradle
compile 'br.com.zalem.ymir:ymir.client-android.perspective:1.0.0'
```


#### Apenas componente de comuns
```gradle
compile 'br.com.zalem.ymir:ymir.client-android.commons:1.0.0'
```
