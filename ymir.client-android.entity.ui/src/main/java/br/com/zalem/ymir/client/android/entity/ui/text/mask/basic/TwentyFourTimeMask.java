package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITimeMask;

/**
 * Máscara que formata horas no padrão de 24: "hh:mm".
 *
 * @author Thiago Gesser
 */
public final class TwentyFourTimeMask implements ITimeMask {
	
	private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

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
		return true;
	}
}
