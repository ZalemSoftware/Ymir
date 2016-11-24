package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import android.content.Context;
import android.text.format.DateFormat;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDatetimeMask;

/**
 * Máscara que formata datas e horas de acordo com o local e as configurações do usuário.
 *
 * @author Thiago Gesser
 */
public final class DefaultDatetimeMask implements IDatetimeMask {

    public static final String DATE_TIME_SEPARATOR = " - ";
    private final IDatetimeMask datetimeMask;
    private final boolean is24Hour;

    public DefaultDatetimeMask(Context context) {
        IDatetimeMask dateMask;
        try {
            //Geralmente os formatadores retornados pelo DateFormat são SimpleDateFormat, o que permite juntá-los em um só, deixando
            //a formatação/parse mais rápidos. Entretanto, se algum dia isto não for mais verdade, conta com o formatador que
            //utiliza-os de forma separada, que é mais lento.
            SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateFormat(context);
            SimpleDateFormat tf = (SimpleDateFormat) DateFormat.getTimeFormat(context);
            dateMask = new SimpleDateFormatMask(df, tf);
        } catch (ClassCastException e) {
            dateMask = new DateFormatMask(context);
        }

        this.datetimeMask = dateMask;
        this.is24Hour = DateFormat.is24HourFormat(context);
    }

    @Override
    public CharSequence formatDatetime(Timestamp value) {
		return datetimeMask.formatDatetime(value);
    }

    @Override
    public Timestamp parseDatetime(CharSequence text) throws ParseException {
        return datetimeMask.parseDatetime(text);
    }

    @Override
    public boolean is24Hour() {
        return is24Hour;
    }


    /**
     * Máscara que assume que os {@link java.text.DateFormat} retornados pelo {@link DateFormat#getDateFormat(Context)} e {@link DateFormat#getTimeFormat(Context)}
     * são {@link SimpleDateFormat}. Desta forma, é possível criar e utilizar um formatador só, facilitando o processo de formatação / parse.
     */
    private static final class SimpleDateFormatMask implements IDatetimeMask {

        private final SimpleDateFormat dateFormat;

        public SimpleDateFormatMask(SimpleDateFormat df, SimpleDateFormat tf) {
            dateFormat = new SimpleDateFormat(df.toPattern() + DATE_TIME_SEPARATOR + tf.toPattern(), Locale.US);
        }

        @Override
        public CharSequence formatDatetime(Timestamp value) {
            return dateFormat.format(value);
        }

        @Override
        public Timestamp parseDatetime(CharSequence text) throws ParseException {
            Date date = dateFormat.parse(text.toString());
            return new Timestamp(date.getTime());
        }

        @Override
        public boolean is24Hour() {
            return false;
        }
    }

    /**
     * Máscara que utiliza os {@link java.text.DateFormat} retornados pelo {@link DateFormat#getDateFormat(Context)} e {@link DateFormat#getTimeFormat(Context)}
     * e formata e parseia as datas/tempos utilizando formatadores diferentes. Isto torna o processo mais complicado e demorado.
     */
    private static final class DateFormatMask implements IDatetimeMask {

        private final java.text.DateFormat dateFormat;
        private final java.text.DateFormat timeFormat;

        public DateFormatMask(Context context) {
            dateFormat = DateFormat.getDateFormat(context);
            timeFormat = DateFormat.getTimeFormat(context);
        }

        @Override
        public CharSequence formatDatetime(Timestamp value) {
            return dateFormat.format(value) + DATE_TIME_SEPARATOR + timeFormat.format(value);
        }

        @Override
        public Timestamp parseDatetime(CharSequence text) throws ParseException {
            String[] strings = text.toString().split(DATE_TIME_SEPARATOR);
            if (strings.length != 2) {
                throw new ParseException(String.format("text.toString().split(%s).length != 2", DATE_TIME_SEPARATOR), -1);
            }

            Date date = dateFormat.parse(strings[0]);
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);
            Date time = timeFormat.parse(strings[1]);
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(time);

            Calendar dateTimeCalendar = Calendar.getInstance();
            dateTimeCalendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));
            dateTimeCalendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
            dateTimeCalendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
            dateTimeCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            dateTimeCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            dateTimeCalendar.set(Calendar.SECOND, timeCalendar.get(Calendar.SECOND));
            return new Timestamp(dateTimeCalendar.getTimeInMillis());
        }

        @Override
        public boolean is24Hour() {
            return false;
        }
    }
}
