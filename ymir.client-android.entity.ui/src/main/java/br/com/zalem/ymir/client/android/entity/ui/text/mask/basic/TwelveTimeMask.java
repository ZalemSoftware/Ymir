package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITimeMask;

/**
 * Máscara que formata horas no padrão de 12: "hh:mm AM/PM".
 *
 * @author Thiago Gesser
 */
public final class TwelveTimeMask implements ITimeMask {
	
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa", Locale.US);

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
		return false;
	}
}
