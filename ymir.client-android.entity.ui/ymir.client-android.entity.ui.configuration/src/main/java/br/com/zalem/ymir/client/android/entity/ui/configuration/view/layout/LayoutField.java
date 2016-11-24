package br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout;

import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldType.IMAGE;
import static br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldType.TEXT;

/**
 * Campos disponíveis para serem utilizados nos layouts.<br>
 * Novos campos podem ser adicionados neste enum conforme novos layouts forem precisando deles.
 *
 * @author Thiago Gesser
 */
public enum LayoutField {

	IMAGE1(IMAGE),
	TEXT1(TEXT), TEXT2(TEXT), TEXT3(TEXT), TEXT4(TEXT), TEXT5(TEXT), TEXT6(TEXT), TEXT7(TEXT);
	
	private final LayoutFieldType type;

	private LayoutField(LayoutFieldType type) {
		this.type = type;
	}
	
	public LayoutFieldType getType() {
		return type;
	}
}
