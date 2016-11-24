package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import android.content.Context;

import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IBooleanMask;

/**
 * Máscara que formata booleanos em "Sim" ou "Não", dependendo do idioma do dispositivo.
 *
 * @author Thiago Gesser
 */
public final class YesNoBooleanMask implements IBooleanMask {

	private final String yesString;
	private final String noString;

	public YesNoBooleanMask(Context context) {
		yesString = context.getString(R.string.yes);
		noString = context.getString(R.string.no);
	}
	
	@Override
	public CharSequence formatBoolean(Boolean value) {
		return value ? yesString : noString;
	}
	
	@Override
	public Boolean parseBoolean(CharSequence text) throws ParseException {
		if (text.equals(yesString)) {
			return Boolean.TRUE;
		}
		if (text.equals(noString)) {
			return Boolean.FALSE;
		}
		
		throw new ParseException(text.toString(), -1);
	}
}
