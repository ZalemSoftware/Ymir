package br.com.zalem.ymir.client.android.entity.ui.configuration.json.view.list;

import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.IListConfig;

/**
 * Representação da configuração de lista para JSON.
 *
 * @author Thiago Gesser
 */
public final class JsonListConfig extends JsonTabbedListDisplayConfig implements IListConfig {

    private boolean enableDuplicate;

    @Override
    public boolean isEnableDuplicate() {
        return enableDuplicate;
    }

    public void setEnableDuplicate(boolean enableDuplicate) {
        this.enableDuplicate = enableDuplicate;
    }
}
