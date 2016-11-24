package br.com.zalem.ymir.client.android.entity.data.openmobster.util;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;

import org.openmobster.core.mobileCloud.android.util.Base64;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;

/**
 * Utilitário para a conversão de valores entre tipos de dados.<br>
 * O OpenMobster armazena todos os valores em Strings, então é necessário converter os valores para seus tipos originais
 * antes de retorná-los.
 *
 * @author Thiago Gesser
 */
public final class TypeUtils {

	private TypeUtils() {}
	
	/**
	 * Converte um valor String em um valor de acordo com o seu tipo de campo.
     *
	 * @param strValue valor String.
	 * @param type tipo do valor.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor de acordo com o seu tipo de campo.
	 */
	public static Object convert(String strValue, EntityAttributeType type) throws ParseException {
		switch (type) {
			case INTEGER:
				return convertToInteger(strValue);
			case DECIMAL:
				return convertToDecimal(strValue);
			case BOOLEAN:
				return convertToBoolean(strValue);
			case TEXT:
				return strValue;
			case CHARACTER:
				return convertToCharacter(strValue);
			case DATE:
				return convertToDate(strValue);
			case TIME:
				return convertToTime(strValue);
			case DATETIME:
				return convertToDatetime(strValue);
			case IMAGE:
				return convertToImage(strValue);

			case INTEGER_ARRAY:
			case DECIMAL_ARRAY:
			case BOOLEAN_ARRAY:
			case TEXT_ARRAY:
			case CHARACTER_ARRAY:
			case DATE_ARRAY:
			case TIME_ARRAY:
			case DATETIME_ARRAY:
			case IMAGE_ARRAY:
				throw new IllegalArgumentException("Invalid EntityAttributeType: " + type);
				
			default:
				throw new IllegalArgumentException("Unsupported EntityAttributeType: " + type);
		}
	}
	
	/**
	 * Converte um valor String em um valor <code>inteiro</code>.
	 * 
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor <code>inteiro</code>.
	 */
	public static Integer convertToInteger(String strValue) throws ParseException {
		if (strValue== null) {
			return null;
		}
		
		try {
			return Integer.parseInt(strValue);
		} catch (NumberFormatException e) {
			throw new ParseException(strValue, -1);
		}
	}
	
	/**
	 * Converte um valor String em um valor <code>decimal</code>.
	 * 
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor <code>decimal</code>.
	 */
	public static Double convertToDecimal(String strValue) throws ParseException {
		if (strValue == null) {
			return null;
		}
		
		try {
			return Double.parseDouble(strValue);
		} catch (NumberFormatException e) {
			throw new ParseException(strValue, -1);
		}
	}
	
	/**
	 * Converte um valor String em um valor <code>booleano</code>.
	 * 
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor <code>booleano</code>.
	 */
	public static Boolean convertToBoolean(String strValue) throws ParseException {
		if (strValue == null) {
			return null;
		}
		
		if (strValue.equals("false")) {
			return Boolean.FALSE;
		}
		if (strValue.equals("true")) {
			return Boolean.TRUE;
		}
		
		throw new ParseException(strValue, -1);
	}
	
	/**
	 * Converte um valor String em um valor do tipo <code>data</code>.
	 * 
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor do tipo <code>data</code>.
	 */
	public static Date convertToDate(String strValue) throws ParseException {
		if (strValue == null) {
			return null;
		}
		
		try {
			return Date.valueOf(strValue);
		} catch (IllegalArgumentException e) {
			throw new ParseException(strValue, -1);
		}
	}
	
	/**
	 * Converte um valor String em um valor do tipo <code>hora</code>.
	 * 
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor do tipo <code>hora</code>.
	 */
	public static Time convertToTime(String strValue) throws ParseException {
		if (strValue == null) {
			return null;
		}
		
		try {
			return Time.valueOf(strValue);
		} catch (IllegalArgumentException e) {
			throw new ParseException(strValue, -1);
		}
	}

	/**
	 * Converte um valor String em um valor do tipo <code>data e hora</code>.
	 *
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor do tipo <code>data e hora</code>.
	 */
	public static Timestamp convertToDatetime(String strValue) throws ParseException {
		if (strValue == null) {
			return null;
		}

		try {
            return Timestamp.valueOf(strValue);
		} catch (IllegalArgumentException e) {
			throw new ParseException(strValue, -1);
		}
	}

	/**
	 * Converte um valor String em um valor do tipo <code>caractere</code>.
	 * 
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor do tipo <code>caractere</code>.
	 */
	public static Character convertToCharacter(String strValue) throws ParseException {
		if (strValue == null) {
			return null;
		}

		if (strValue.length() != 1) {
			throw new ParseException(strValue, -1);
		}
		
		return strValue.charAt(0);
	}
	
	/**
	 * Converte um valor String em um valor do tipo <code>imagem</code>.
	 * 
	 * @param strValue valor string.
	 * @return o valor convertido.
	 * @throws java.text.ParseException se a String não representava um valor do tipo <code>imagem</code>.
	 */
	public static Bitmap convertToImage(String strValue) throws ParseException {
		//Esta conversão de texto para byte[] é feita de forma praticamente igual ao MobileBean.getBinaryValue().
		//A única diferença é que este método não ignora Strings vazias (deixa dar exceção).
		if (strValue == null) {
			return null;
		}
		
		try {
			byte[] imageBytes = Base64.decode(strValue);
			Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
			if (bitmap == null) {
				//Se é null, significa que os bytes não representam uma imagem válida.
				 throw new ParseException(strValue, -1); 
			}
			
			return bitmap;
		} catch (IOException e) {
			throw new ParseException(strValue, -1);
		}
	}
}
