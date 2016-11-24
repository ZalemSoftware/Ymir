package br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout;

import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.IMAGE1;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT1;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT2;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT3;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT4;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT5;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT6;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT7;

/**
 * Tipos de layouts disponíveis para a representação de registros em uma lista específica.<br>
 * Novos tipos de layouts podem ser adicionados neste enum conforme forem surgindo novas especificações de layouts.
 *
 * @author Thiago Gesser
 */
public enum ListLayoutType implements ILayoutType {
	LIST_LAYOUT_1(IMAGE1, TEXT1),
	LIST_LAYOUT_2(IMAGE1, TEXT1, TEXT2),
	LIST_LAYOUT_3(IMAGE1, TEXT1, TEXT2, TEXT3),
	LIST_LAYOUT_4(TEXT1, TEXT2),
	LIST_LAYOUT_5(TEXT1, TEXT2, TEXT3),
	LIST_LAYOUT_6(TEXT1),
	LIST_LAYOUT_7(IMAGE1, TEXT1, TEXT2),
	LIST_LAYOUT_8(IMAGE1, TEXT1, TEXT2, TEXT3, TEXT4),
	LIST_LAYOUT_9(TEXT1, TEXT2, TEXT3, TEXT4, TEXT5, TEXT6, TEXT7),
	LIST_LAYOUT_10(TEXT1, TEXT2),
	LIST_LAYOUT_11(TEXT1),
	LIST_LAYOUT_12(TEXT1, TEXT2, TEXT3, TEXT4),
	LIST_LAYOUT_13(TEXT1, TEXT2, TEXT3, TEXT4),
	LIST_LAYOUT_14(TEXT1, TEXT2, TEXT3),
	LIST_LAYOUT_15(TEXT1, TEXT2, TEXT3, TEXT4),
	LIST_LAYOUT_16(IMAGE1, TEXT1, TEXT2, TEXT3, TEXT4),
	LIST_LAYOUT_17(IMAGE1, TEXT1, TEXT2, TEXT3);

	private final LayoutField[] fields;
	
	ListLayoutType(LayoutField... fields) {
		this.fields = fields;
	}

	@Override
	public LayoutField[] getFields() {
		return fields;
	}
	
	@Override
	public String getName() {
		return name();
	}
}
