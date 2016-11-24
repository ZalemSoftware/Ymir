package br.com.zalem.ymir.client.android.entity.ui.configuration.view.list;


/**
 * Configurações que envolvem a listagem de registros de uma entidade, possibilitando sua separação em abas.<br>
 * A configuração do <code>layout</code> é opcional se houverem abas declaradas. Neste caso, as configurações de <code>layout</code>,
 * <code>filtro</code> e <code>ordenação</code> servirão apenas como valores padrão para o caso de alguma aba não tê-los declarado.
 *
 * @author Thiago Gesser
 */
public interface IListConfig extends ITabbedListDisplayConfig {

    /**
     * <b>Configuração opcional.</b><br>
     * <br>
     * Determina se a ação de duplicar os registros através da lista está habilitada. Por padrão está desabilitada.
     *
     * @return <code>true</code> se a duplicação está habilitada e <code>false</code> caso contrário.
     */
    boolean isEnableDuplicate();
}
