package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDateMask;

/**
 * Máscara que formata datas no padrão internacional: "aaaa-mm-dd".
 *
 * @author Thiago Gesser
 */
public final class InternationalDateMask implements IDateMask {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

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
