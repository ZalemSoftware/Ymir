package br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout;


/**
 * Tipos de visibilidade de campos de layout.
 *
 * @author Thiago Gesser
 */
public enum LayoutFieldVisibility {

    /**
     * Indica que o campo do layout deve ser visível.
     */
    VISIBLE,

    /**
     * Indica que o campo do layout não deve ser visível, mas ainda deve ocupar espaço.
     */
    INVISIBLE,

    /**
     * Indica que o campo do layout não deve ser visível e não deve ocupar espaço.
     */
    GONE
}
