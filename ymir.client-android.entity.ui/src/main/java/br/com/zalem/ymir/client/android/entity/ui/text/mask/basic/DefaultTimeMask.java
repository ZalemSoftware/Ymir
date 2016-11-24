package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import android.content.Context;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITimeMask;

/**
 * Máscara que formata horas de acordo com o local e as configurações do usuário.
 *
 * @author Thiago Gesser
 */
public final class DefaultTimeMask implements ITimeMask {

    private final DateFormat timeFormat;
    private final boolean is24Hour;

    public DefaultTimeMask(Context context) {
        timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        is24Hour = android.text.format.DateFormat.is24HourFormat(context);
    }

	@Override
	public CharSequence formatTime(Time value) {
		return timeFormat.format(value);
	}
	
	@Override
	public Time parseTime(CharSequence text) throws ParseException {
		java.util.Date time = timeFormat.parse(text.toString());
		return new Time(time.getTime());
	}

	@Override
	public boolean is24Hour() {
		return is24Hour;
	}
}
