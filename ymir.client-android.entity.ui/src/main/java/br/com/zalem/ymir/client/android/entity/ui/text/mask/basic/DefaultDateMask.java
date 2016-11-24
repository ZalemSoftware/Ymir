package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import android.content.Context;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDateMask;

/**
 * Máscara que formata datas de acordo com o local e as configurações do usuário.
 *
 * @author Thiago Gesser
 */
public final class DefaultDateMask implements IDateMask {

    private final DateFormat dateFormat;

    public DefaultDateMask(Context context) {
        dateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

	@Override
	public CharSequence formatDate(Date value) {
		return dateFormat.format(value);
	}
	
	@Override
	public Date parseDate(CharSequence text) throws ParseException {
		java.util.Date date = dateFormat.parse(text.toString());
		return new Date(date.getTime());
	}
}
