package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDatetimeMask;

/**
 * Máscara que formata datas no padrão internacional e horas no padrão 12: "aaaa-mm-dd - hh:mm AM/PM".
 *
 * @author Thiago Gesser
 */
public final class InternationalDatetimeMask implements IDatetimeMask {

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd - hh:mm aa", Locale.US);

    @Override
    public CharSequence formatDatetime(Timestamp value) {
		return dateFormat.format(value);
    }

    @Override
    public Timestamp parseDatetime(CharSequence text) throws ParseException {
		java.util.Date date = dateFormat.parse(text.toString());
		return new Timestamp(date.getTime());
    }

    @Override
    public boolean is24Hour() {
        return false;
    }
}
