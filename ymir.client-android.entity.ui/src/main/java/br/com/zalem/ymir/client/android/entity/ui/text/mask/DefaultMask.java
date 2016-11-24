package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;

import java.sql.Date;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;

import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * Máscara padrão que implementa todos os tipos de máscaras.<br>
 * Em geral, esta máscara não aplica nenhuma formatação, apenas chama o {@link #toString()} dos valores a serem formatados e chama
 * os métodos de parse normal dos valores a serem parseados.
 * <br>
 * É uma Classe Singleton, então sua única instância pode ser obtida através do método {@link #getInstance(Context)}.
 *
 * @author Thiago Gesser
 */
public final class DefaultMask implements IIntegerMask, IDecimalMask, ITextMask, IBooleanMask, IDateMask, ITimeMask, ICharacterMask, IImageMask,
										  IIntegerArrayMask, IDecimalArrayMask, ITextArrayMask, IBooleanArrayMask, IDateArrayMask, ITimeArrayMask, ICharacterArrayMask, IImageArrayMask {

	private static DefaultMask singleton;

	public static DefaultMask getInstance(Context context) {
		if (singleton == null) {
			boolean is24Hour = DateFormat.is24HourFormat(context);
			singleton = new DefaultMask(context.getApplicationContext().getResources(), is24Hour);
		}
		
		return singleton;
	}


    private final Resources resources;
    private final boolean is24Hour;
	private final DecimalFormat decimalFormat;
	private final DecimalFormatSymbols decimalFormatSymbols;

	private DefaultMask(Resources resources, boolean is24Hour) {
        this.resources = resources;
        this.is24Hour = is24Hour;
		decimalFormat = new DecimalFormat("0");
		//Não deve haver limites para as casas decimais e para fazer isto com o formato String seria necessário definir sucessivos #...
		//Utiliza 340 pq segundo a documentação é o limite suportado. Entretanto, não  há nenhuma constante com este valor, então tem que ser hardcoded.
		decimalFormat.setMaximumFractionDigits(340);
		decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
	}
	
	@Override
	public String formatInteger(Integer value) {
		return value.toString();
	}
	
	@Override
	public String formatDecimal(Double value) {
		return decimalFormat.format(value);
	}
	
	@Override
	public String formatText(String value) {
		return value;
	}
	
	@Override
	public String formatBoolean(Boolean value) {
		return value.toString();
	}
	
	@Override
	public String formatDate(Date value) {
		return value.toString();
	}
	
	@Override
	public String formatTime(Time value) {
		return value.toString();
	}
	
	@Override
	public String formatCharacter(Character value) {
		return value.toString();
	}
	
	@Override
	public Drawable formatImage(Bitmap value) {
		return new BitmapDrawable(resources, value);
	}
	
	@Override
	public CharSequence formatIntegerArray(Integer[] value) {
		return Arrays.toString(value);
	}
	
	@Override
	public CharSequence formatDecimalArray(Double[] value) {
		return Arrays.toString(value);
	}
	
	@Override
	public CharSequence formatTextArray(String[] value) {
		return Arrays.toString(value);
	}
	
	@Override
	public CharSequence formatBooleanArray(Boolean[] value) {
		return Arrays.toString(value);
	}
	
	@Override
	public CharSequence formatDateArray(Date[] value) {
		return Arrays.toString(value);
	}
	
	@Override
	public CharSequence formatTimeArray(Time[] value) {
		return Arrays.toString(value);
	}
	
	@Override
	public CharSequence formatCharacterArray(Character[] value) {
		return Arrays.toString(value);
	}
	
	@Override
	public Drawable formatImageArray(Bitmap[] value) {
        throw new PendingFeatureException("DefaultMask.formatImageArray");
	}

	@Override
	public String parseText(CharSequence text) {
		return text.toString();
	}

	@Override
	public Double parseDecimal(CharSequence text) throws ParseException {
		//Se está compondo, retorna null para indicar que não há número para parsear.
		if (isComposingNumber(text)) {
			return null;
		}

		//É obrigado a parsear desta forma pq só assim é possivel identificar erros de forma garantida.
		//Normalmente o parse obtém o primeimro número que consegue da String, independente se depois do número existem caracteres inválidos.
		ParsePosition pos = new ParsePosition(0);
		Number number = decimalFormat.parse(text.toString(), pos);
		if (pos.getIndex() < text.length()) {
			throw new ParseException("Invalid decimal String", pos.getIndex());
		}

		//Se termina no separador decimal, significa que está compondo um número.
		if (text.charAt(text.length()-1) == decimalFormatSymbols.getDecimalSeparator()) {
			return null;
		}
		return number.doubleValue();
	}

	@Override
	public Integer parseInteger(CharSequence text) throws ParseException {
		//Se está compondo, retorna null para indicar que não há número para parsear.
		if (isComposingNumber(text)) {
			return null;
		}

		try {
			return Integer.valueOf(text.toString());
		} catch (NumberFormatException e) {
			throw new ParseException(text.toString(), -1);
		}
	}
	
	@Override
	public Boolean parseBoolean(CharSequence text) throws ParseException {
		if (text.equals("false")) {
			return Boolean.FALSE;
		}
		if (text.equals("true")) {
			return Boolean.TRUE;
		}
		
		throw new ParseException(text.toString(), -1);
	}
	
	@Override
	public Date parseDate(CharSequence text) throws ParseException {
		try {
			return Date.valueOf(text.toString());
		} catch (IllegalArgumentException e) {
			throw new ParseException(text.toString(), -1);
		}
	}
	
	@Override
	public Time parseTime(CharSequence text) throws ParseException {
		try {
			return Time.valueOf(text.toString());
		} catch (IllegalArgumentException e) {
			throw new ParseException(text.toString(), -1);
		}
	}

    @Override
    public Character parseCharacter(CharSequence text) throws ParseException {
        if (text.length() != 1) {
			throw new ParseException(text.toString(), text.length() == 0 ? 0 : 1);
        }

		return text.charAt(0);
    }

    @Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean is24Hour() {
		return is24Hour;
	}


	/*
	 * Métodos auxiliares
	 */

	private boolean isComposingNumber(CharSequence text) {
		//Pode ser "-" ou "-0".
		switch (text.length()) {
			case 2:
				if (text.charAt(1) != '0') {
					return false;
				}
			case 1:
				return text.charAt(0) == decimalFormatSymbols.getMinusSign();

			default:
				return false;
		}
	}
}
