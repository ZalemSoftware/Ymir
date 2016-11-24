package br.com.zalem.ymir.client.android.menu;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * Representa um item do menu utilizado pelo framework Ymir.
 */
public final class YmirMenuItem {

    private final int id;
    private final String title;
    private final int iconResourceId;
    private final int color;
    private final int groupId;
    private Intent intent;
    private YmirMenu menu;

    public YmirMenuItem(int id, String title, int iconResourceId, Intent intent, int color, int groupId) {
        this.id = id;
        this.title = title;
        this.iconResourceId = iconResourceId;
        this.intent = intent;
        this.color = color;
        this.groupId = groupId;
    }

    YmirMenuItem(Context context, AttributeSet attr, int groupId) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attr, R.styleable.YmirMenuItem, 0, 0);
        try {
            id = a.getResourceId(R.styleable.YmirMenuItem_id, 0);
            title = a.getString(R.styleable.YmirMenuItem_title);
            iconResourceId = a.getResourceId(R.styleable.YmirMenuItem_icon, 0);
            color = a.getColor(R.styleable.YmirMenuItem_color, -1);
            this.groupId = groupId;
        } finally {
            a.recycle();
        }
    }

    void setIntent(Intent intentIntent) {
        this.intent = intentIntent;
    }

    void setMenu(YmirMenu menu) {
        this.menu = menu;
    }


    /**
     * Obtém o id do item de menu.
     *
     * @return o id ou <code>zero</code> se não há id definido.
     */
    public int getId() {
        return id;
    }

    /**
     * Obtém o {@link Intent} definido para o item de menu.
     *
     * @return o Intent ou <code>null</code> se não há Intent definido.
     */
    public Intent getIntent() {
        return intent;
    }

    /**
     * Obtém o título do item de menu.
     *
     * @return o título ou <code>null</code> se não há título definido.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Obtém o ícone do item de menu.
     *
     * @return o id do recurso do ícone ou <code>null</code> se não há título definido.
     */
    public int getIconResourceId() {
        return iconResourceId;
    }

    /**
     * Obtém a cor do item de menu.
     *
     * @return a cor obtida ou <code>-1</code> se nenhuma cor foi definida.
     */
    public int getColor() {
        return color;
    }

    /**
     * Obtém o id do grupo que o item pertence.
     *
     * @return o id obtido ou <code>0</code> se ele não pertence a nenhum grupo.
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Obtém o menu ao qual este item pertence.
     *
     * @return o menu deste item.
     */
    public YmirMenu getMenu() {
        return menu;
    }

    @Override
    public String toString() {
        return title;
    }
}
