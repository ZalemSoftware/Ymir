package br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout;

import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.IMAGE1;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT1;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT2;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField.TEXT3;

/**
 * Tipos de layouts disponíveis para o detalhamento de um registro específico.<br>
 * Novos tipos de layouts podem ser adicionados neste enum conforme forem surgindo novas especificações de layouts.
 *
 * @author Thiago Gesser
 */
public enum DetailLayoutType implements ILayoutType {
	DETAIL_LAYOUT_1(IMAGE1, TEXT1, TEXT2, TEXT3),
	DETAIL_LAYOUT_2(IMAGE1, TEXT1, TEXT2),
	DETAIL_LAYOUT_3(TEXT1, TEXT2, TEXT3),
	DETAIL_LAYOUT_4(TEXT1),
	DETAIL_LAYOUT_5(TEXT1, TEXT2),
	DETAIL_LAYOUT_6(IMAGE1, TEXT1, TEXT2);

	private final LayoutField[] fields;
	
	DetailLayoutType(LayoutField... fields) {
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
