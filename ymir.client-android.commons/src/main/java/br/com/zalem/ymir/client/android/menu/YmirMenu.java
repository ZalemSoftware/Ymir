package br.com.zalem.ymir.client.android.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um menu utilizado pelo framework Ymir.
 */
public final class YmirMenu {

    private final List<YmirMenuItem> items;

    public YmirMenu() {
        this.items = new ArrayList<>();
    }

    void addItem(YmirMenuItem item) {
        items.add(item);
        item.setMenu(this);
    }

    /**
     * Obtém o número de itens do menu.
     *
     * @return o número de itens obtido.
     */
    public int size() {
        return items.size();
    }

    /**
     * Obtém o item de menu correspondente ao índice.
     *
     * @param index índice do item.
     * @return o item obtido.
     * @throws java.lang.IndexOutOfBoundsException se o índice não corresponder ao item do menu.
     */
    public YmirMenuItem getItem(int index) {
        return items.get(index);
    }
}
