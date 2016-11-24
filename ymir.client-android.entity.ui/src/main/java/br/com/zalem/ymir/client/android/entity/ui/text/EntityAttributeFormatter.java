package br.com.zalem.ymir.client.android.entity.ui.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFormattableFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IBooleanArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IBooleanMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ICharacterArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ICharacterMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDateArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDateMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDatetimeArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDatetimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IImageArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IImageMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IIntegerArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IIntegerMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITextArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITextMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITimeArrayMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.ITimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * Formatador de valores de attributos de entidade.<br>
 * A formatação se baseia em máscaras que podem ser inseridas/removidas do formatador para formar uma base de formatação através dos métodos
 * {@link #putMask(IMask, String...)} e {@link #removeMask(String...)}.<br>
 * Para executar a formatação, basta chamar o método {@link #formatAttributeValueToText(IEntityRecord, String...)} ou {@link #formatValue(Object, String...)}.
 * Também é possível parsear textos formatados previamente em valores através do metodo {@link #parseValue(CharSequence, String...)}.<br>
 * <br>
 * O EntityAttributeFormatter é composto por {@link TypedFormatter formatadores tipados}, que englobam uma {@link IMask máscara} e dispõem
 * métodos genéricos para a formatação/parse de valores. Estes formatadores tipados também podem ser obtidos isoladamente através do metodo
 * {@link #createTypedFormatter(EntityAttributeType, IMask)}.
 *
 * @author Thiago Gesser
 */
public final class EntityAttributeFormatter {

	private final Map<String, TypedFormatter<?, ?>> formatters;
	private final String nullTextValue;
	private final IEntityMetadata entityMetadata;
	
	/**
	 * Cria um formatador vazio.
	 * 
	 * @param context contexto.
	 */
	public EntityAttributeFormatter(Context context, IEntityMetadata entityMetadata) {
		this.entityMetadata = entityMetadata;
		this.formatters = new HashMap<>();
		this.nullTextValue = context.getString(R.string.null_text_value);
	}

	/**
	 * Define uma máscara para um attributo no formatador.
	 *  
	 * @param mask máscara
	 * @param attributePath array de strings indicando o caminho para o atributo, iniciando nos relacionamentos
	 * e terminando no nome do atributo. Se não houver relacionamentos, deve ser apenas o nome do atributo.
	 */
	public void putMask(IMask mask, String... attributePath) {
        checkAttributePath(null, attributePath);

		IEntityAttribute attribute = MetadataUtils.getAttributeFromPath(entityMetadata, attributePath);
		TypedFormatter<?, ?> formatter = createTypedFormatter(attribute.getType(), mask);
        putTypedFormatter(formatter, attributePath);
    }

	/**
	 * Remove uma máscara definida para um atributo no formatador.
	 *  
	 * @param attributePath array de strings indicando o caminho para o atributo, iniciando nos relacionamentos
	 * e terminando no nome do atributo. Se não houver relacionamentos, deve ser apenas o nome do atributo.
	 */
	public void removeMask(String... attributePath) {
        checkAttributePath(null, attributePath);

		formatters.remove(makeAttributePathKey(attributePath));
	}
	
	/**
	 * Remove todos as máscaras definidas no formatador.
	 */
	public void clearMasks() {
		formatters.clear();
	}

    /**
     * Obtém o número de mascaras no formatador.
     *
     * @return o número obtido.
     */
    public int getMasksCount() {
        return formatters.size();
    }


	/**
     * Formata o valor de um attributo a partir da máscara definida previamente para ele.
     *
     * @param entityRecord registro que terá o valor do attributo obtido.
     * @param attributePath array de strings indicando o caminho para o atributo, iniciando nos relacionamentos
     * e terminando no nome do atributo. Se não houver relacionamentos, deve ser apenas o nome do atributo.
     * @return o texto do valor do attributo formatado.
     */
    public CharSequence formatAttributeValueToText(IEntityRecord entityRecord, String... attributePath) {
        checkAttributePath(entityRecord, attributePath);
        checkEntity(entityRecord);

        TypedFormatter<?, ?> formatter = getTypedFormatter(makeAttributePathKey(attributePath));
        checkTextFormatter(formatter, attributePath);
        IEntityRecord targetRecord = getTargetRecord(entityRecord, attributePath);

        return formatAttributeValueToText(targetRecord, attributePath, formatter);
    }

    /**
     * Formata o valor a partir da máscara definida previamente para o seu atributo.
     *
     * @param value valor que será formatado.
     * @param attributePath array de strings indicando o caminho para o atributo, iniciando nos relacionamentos
     * e terminando no nome do atributo. Se não houver relacionamentos, deve ser apenas o nome do atributo.
     * @return o texto do valor formatado.
     */
    @SuppressWarnings("unchecked")
    public CharSequence formatValueToText(Object value, String... attributePath) {
        checkAttributePath(null, attributePath);

        TypedFormatter formatter = getTypedFormatter(makeAttributePathKey(attributePath));
        checkTextFormatter(formatter, attributePath);
        CharSequence text = (CharSequence) formatter.formatValue(value);
        if (text == null) {
            return nullTextValue;
        }
        return text;
    }

    /**
     * Formata o valor de um attributo a partir da máscara definida previamente para ele.
     *
     * @param entityRecord registro que terá o valor do attributo obtido.
     * @param attributePath array de strings indicando o caminho para o atributo, iniciando nos relacionamentos
     * e terminando no nome do atributo. Se não houver relacionamentos, deve ser apenas o nome do atributo.
     * @return o objeto resultante da formatação do valor do attributo.
     */
    @SuppressWarnings("unchecked")
    public <T> T formatAttributeValue(IEntityRecord entityRecord, String... attributePath) {
        checkAttributePath(entityRecord, attributePath);
        checkEntity(entityRecord);

        TypedFormatter<?, ?> formatter = getTypedFormatter(makeAttributePathKey(attributePath));
        IEntityRecord targetRecord = getTargetRecord(entityRecord, attributePath);
        if (targetRecord == null) {
            return null;
        }

        String attributeName = attributePath[attributePath.length-1];
        return (T) formatter.formatAttribute(targetRecord, attributeName);
    }

    /**
     * Formata o valor a partir da máscara definida previamente para o seu atributo.
     *
     * @param value valor que será formatado.
     * @param attributePath array de strings indicando o caminho para o atributo, iniciando nos relacionamentos
     * e terminando no nome do atributo. Se não houver relacionamentos, deve ser apenas o nome do atributo.
     * @return o objeto resultante da formatação do valor do attributo.
     */
    @SuppressWarnings("unchecked")
    public  <T> T formatValue(Object value, String... attributePath) {
        checkAttributePath(null, attributePath);

        TypedFormatter formatter = getTypedFormatter(makeAttributePathKey(attributePath));
        return (T) formatter.formatValue(value);
    }

    /**
     * Parseia o texto a partir da máscara definida previamente para o seu atributo
     *
     * @param text texto que será parseado.
     * @param attributePath array de strings indicando o caminho para o atributo, iniciando nos relacionamentos
     * e terminando no nome do atributo. Se não houver relacionamentos, deve ser apenas o nome do atributo.
     * @return o valor parseado do texto.
     */
    @SuppressWarnings("unchecked")
    public <T> T parseValue(CharSequence text, String... attributePath) throws ParseException {
        checkAttributePath(null, attributePath);

        TypedFormatter<?, ?> formatter = getTypedFormatter(makeAttributePathKey(attributePath));
        return (T) formatter.parseValue(text);
    }


    /**
     * Cria um formatador a partir da configuração de mapeamentos de atributos.<br>
     * As máscaras são colocadas no formatador de acordo com os mapeamentos, através do gerenciador de máscaras. Se um mapeamento não possuir
     * máscara definida, será utilizada a máscara padrão para o tipo do atributo.<br>
     * <br>
     * Mapeamentos para relacionamentos são ignorados.
     *
     * @param maskManager gerenciador de máscaras.
     * @param fieldMappings mapeamentos de atributos.
     */
    public static EntityAttributeFormatter fromConfig(Context context, IEntityMetadata entityMetadata, MaskManager maskManager, IFormattableFieldMapping... fieldMappings) {
        EntityAttributeFormatter formatter = new EntityAttributeFormatter(context, entityMetadata);
        for (IFormattableFieldMapping fieldMapping : fieldMappings) {
            //Considera apenas mapeamentos para atributos.
            String[] attributePath = fieldMapping.getAttribute();
            if (attributePath == null) {
                continue;
            }

            TypedFormatter<?, ?> typedFormatter = createTypedFormatter(maskManager, entityMetadata, fieldMapping.getMask(), attributePath);
            formatter.putTypedFormatter(typedFormatter, attributePath);
        }
        return formatter;
    }

    /**
     * Cria um formatador de acordo com a configuração de mapeamento para um atributo.
     *
     * @param maskManager gerenciador de máscaras.
     * @param entityMetadata metadados da entidade base.
     * @param fieldMapping configuraçao de mapeamento para um atributo.
     * @throws IllegalArgumentException se o mapeamento não for para um atributo ou se a máscara configurada não for do tipo adequado para o atributo.
     * @return o formatador criado.
     */
    public static TypedFormatter<?, ?> createTypedFormatter(MaskManager maskManager, IEntityMetadata entityMetadata, IFormattableFieldMapping fieldMapping) {
        String[] attributePath = fieldMapping.getAttribute();
        checkAttributePath(null, attributePath);

        return createTypedFormatter(maskManager, entityMetadata, fieldMapping.getMask(), attributePath);
    }

    /**
     * Cria um formatador com a máscara de acordo com o tipo do atributo.
     *
     * @param type tipo do atributo.
     * @param mask máscara.
     * @throws IllegalArgumentException se a máscara não for do tipo adequado para o atributo.
     * @return o formatador criado.
     */
    public static TypedFormatter<?, ?> createTypedFormatter(EntityAttributeType type, IMask mask) {
        try {
            switch (type) {
                case INTEGER:
                    return new IntegerFormatter((IIntegerMask) mask);

                case DECIMAL:
                    return new DecimalFormatter((IDecimalMask) mask);

                case TEXT:
                    return new TextFormatter((ITextMask) mask);

                case BOOLEAN:
                    return new BooleanFormatter((IBooleanMask) mask);

                case DATE:
                    return new DateFormatter((IDateMask) mask);

                case TIME:
                    return new TimeFormatter((ITimeMask) mask);

                case DATETIME:
                    return new DatetimeFormatter((IDatetimeMask) mask);

                case CHARACTER:
                    return new CharacterFormatter((ICharacterMask) mask);

                case IMAGE:
                    return new ImageFormatter((IImageMask) mask);

                case INTEGER_ARRAY:
                    return new IntegerArrayFormatter((IIntegerArrayMask) mask);

                case DECIMAL_ARRAY:
                    return new DecimalArrayFormatter((IDecimalArrayMask) mask);

                case TEXT_ARRAY:
                    return new TextArrayFormatter((ITextArrayMask) mask);

                case BOOLEAN_ARRAY:
                    return new BooleanArrayFormatter((IBooleanArrayMask) mask);

                case DATE_ARRAY:
                    return new DateArrayFormatter((IDateArrayMask) mask);

                case TIME_ARRAY:
                    return new TimeArrayFormatter((ITimeArrayMask) mask);

                case DATETIME_ARRAY:
                    return new DatetimeArrayFormatter((IDatetimeArrayMask) mask);

                case CHARACTER_ARRAY:
                    return new CharacterArrayFormatter((ICharacterArrayMask) mask);

                case IMAGE_ARRAY:
                    return new ImageArrayFormatter((IImageArrayMask) mask);

                default:
                    throw new IllegalArgumentException("Unsupported EntityAttributeType: " + type);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("The mask \"%s\" is not compatible with the attribute type %s", mask.getClass().getSimpleName(), type));
        }
    }


	/*
	 * Métodos auxiliares
	 */

    private static void checkAttributePath(IEntityRecord entityRecord, String[] attributePath) {
        if (attributePath == null || attributePath.length == 0) {
            String exceptionText = "The attribute path is null or empty.";
            if (entityRecord != null) {
                exceptionText += String.format(" Entity = %s, record id = %s.", entityRecord.getEntityMetadata().getName(), entityRecord.getId());
            }
            throw new IllegalArgumentException(exceptionText);
        }
    }

	private void checkEntity(IEntityRecord record) {
		if (!entityMetadata.getName().equals(record.getEntityMetadata().getName())) {
			throw new IllegalArgumentException("The record belongs to another entity: " + record.getEntityMetadata().getName());
		}
	}

    private void checkTextFormatter(TypedFormatter<?, ?> formatter, String[] attributePath) {
        if (!formatter.formatToText()) {
            throw new IllegalArgumentException(String.format("The attribute can't be formatted to a text.  Attribute = %s ", Arrays.toString(attributePath)));
        }
    }


    private void putTypedFormatter(TypedFormatter<?, ?> formatter, String[] attributePath) {
        formatters.put(makeAttributePathKey(attributePath), formatter);
    }

	private TypedFormatter<?, ?> getTypedFormatter(String key) {
		TypedFormatter<?, ?> formatter = formatters.get(key);
		if (formatter == null) {
			throw new IllegalArgumentException("There is no mask configured for " + key);
		}
		return formatter;
	}

	private IEntityRecord getTargetRecord(IEntityRecord entityRecord, String[] attributePath) {
		IEntityRecord curRecord = entityRecord;
		for (int i = 0; i < attributePath.length; i++) {
			if (i == attributePath.length-1) {
				return curRecord;
			}

			String relationshipName = attributePath[i];
			curRecord = curRecord.getRelationshipValue(relationshipName);
			if (curRecord == null) {
				return null;
			}
		}
		
		throw new IllegalArgumentException("Invalid attribute path: " + Arrays.toString(attributePath));
	}

	private CharSequence formatAttributeValueToText(IEntityRecord entityRecord, String[] attributePath, TypedFormatter<?, ?> formatter) {
		if (entityRecord == null) {
			return nullTextValue;
		}

        String attributeName = attributePath[attributePath.length-1];
		CharSequence text = (CharSequence) formatter.formatAttribute(entityRecord, attributeName);
		if (text == null) {
			return nullTextValue;
		}
		return text;
	}

	
	private static String makeAttributePathKey(String... attributePath) {
        if (attributePath.length == 1) {
            return attributePath[0];
        }

        return TextUtils.join(".", attributePath);
	}

    private static TypedFormatter<?, ?> createTypedFormatter(MaskManager maskManager, IEntityMetadata entityMetadata, String maskString, String[] attributePath) {
        IEntityAttribute attribute = MetadataUtils.getAttributeFromPath(entityMetadata, attributePath);

        IMask mask = maskManager.getMaskFromConfig(maskString, attribute.getType());
        return createTypedFormatter(attribute.getType(), mask);
    }
	

	/*
	 * Classes auxiliares
	 */
	
	/**
	 * Formatador específico para um tipo de atributo.<br>
     * Provê formas simplificadas para a {@link #formatValue(IMask, Object) formatação} e o parseamento de valores a partir de uma mascara.
	 *
	 * @param <M> o tipo da máscara do formatador
	 * @param <V> o tipo de valor do formatador
	 */
	public static abstract class TypedFormatter<M extends IMask, V> {
		
		private final M mask;
		
		TypedFormatter(M mask) {
			this.mask = mask;
		}

        /**
         * Formata o valor proveniente do atributo do registro em um texto.
         *
         * @param entityRecord registro.
         * @param attributeName nome do atributo.
         * @return o objeto formatado ou <code>null</code> se o valor era nulo.
         */
		public final Object formatAttribute(IEntityRecord entityRecord, String attributeName) {
			V value = getValue(entityRecord, attributeName);
			if (value == null) {
				return null;
			}
			return formatValue(mask, value);
		}

        /**
         * Formata o valor em um texto.
         *
         * @param value valor.
         * @return o objeto formatado.
         */
        public final Object formatValue(V value) {
            return formatValue(mask, value);
        }

        /**
         * Parseia o texto em um valor.
         *
         * @param text texto.
         * @return o valor parseado.
         */
        public final V parseValue(CharSequence text) throws ParseException {
            return parseAndCheckValue(mask, text);
        }

        /**
         * Parseia os textos em um array de valores.
         *
         * @param texts textos.
         * @return o array de valores parseado.
         */
        @SuppressWarnings("unchecked")
        public final V[] parseValues(CharSequence[] texts) throws ParseException {
            V[] values = (V[]) new Object[texts.length];
            for (int i = 0; i < texts.length; i++) {
                values[i] = parseAndCheckValue(mask, texts[i]);
            }
            return values;
        }

		protected abstract V getValue(IEntityRecord entityRecord, String attributeName);
        protected abstract Object formatValue(M mask, V value);

        protected abstract V parseValue(M mask, CharSequence text) throws ParseException;

        protected boolean formatToText() {
            return true;
        }


        private V parseAndCheckValue(M mask, CharSequence text) throws ParseException {
            V value = parseValue(mask, text);
            if (value == null) {
                throw new ParseException("Incomplete value", -1);
            }
            return value;
        }
	}

    /**
     * Formatador específico para o tipo <code>inteiro</code>.
     */
	public static final class IntegerFormatter extends TypedFormatter<IIntegerMask, Integer> {

        public IntegerFormatter(IIntegerMask mask) {
			super(mask);
		}

		@Override
		protected Integer getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getIntegerValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IIntegerMask mask, Integer value) {
			return mask.formatInteger(value);
		}

        @Override
        protected Integer parseValue(IIntegerMask mask, CharSequence text) throws ParseException {
            return mask.parseInteger(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>decimal</code>.
     */
    public static final class DecimalFormatter extends TypedFormatter<IDecimalMask, Double> {

        public DecimalFormatter(IDecimalMask mask) {
			super(mask);
		}

		@Override
        protected Double getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getDecimalValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IDecimalMask mask, Double value) {
			return mask.formatDecimal(value);
		}

        @Override
        protected Double parseValue(IDecimalMask mask, CharSequence text) throws ParseException {
            return mask.parseDecimal(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>texto</code>.
     */
    public static final class TextFormatter extends TypedFormatter<ITextMask, String> {

        public TextFormatter(ITextMask mask) {
			super(mask);
		}

		@Override
        protected String getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getTextValue(attribute);
		}

		@Override
        protected CharSequence formatValue(ITextMask mask, String value) {
			return mask.formatText(value);
		}

        @Override
        protected String parseValue(ITextMask mask, CharSequence text) throws ParseException {
            return mask.parseText(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>booleano</code>.
     */
    public static final class BooleanFormatter extends TypedFormatter<IBooleanMask, Boolean> {

        public BooleanFormatter(IBooleanMask mask) {
			super(mask);
		}

		@Override
        protected Boolean getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getBooleanValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IBooleanMask mask, Boolean value) {
			return mask.formatBoolean(value);
		}

        @Override
        protected Boolean parseValue(IBooleanMask mask, CharSequence text) throws ParseException {
            return mask.parseBoolean(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>data</code>.
     */
    public static final class DateFormatter extends TypedFormatter<IDateMask, Date> {

        public DateFormatter(IDateMask mask) {
			super(mask);
		}

		@Override
        protected Date getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getDateValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IDateMask mask, Date value) {
			return mask.formatDate(value);
		}

        @Override
        protected Date parseValue(IDateMask mask, CharSequence text) throws ParseException {
            return mask.parseDate(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>hora</code>.
     */
    public static final class TimeFormatter extends TypedFormatter<ITimeMask, Time> {

        public TimeFormatter(ITimeMask mask) {
            super(mask);
        }

        @Override
        protected Time getValue(IEntityRecord entityRecord, String attribute) {
            return entityRecord.getTimeValue(attribute);
        }

        @Override
        protected CharSequence formatValue(ITimeMask mask, Time value) {
            return mask.formatTime(value);
        }

        @Override
        protected Time parseValue(ITimeMask mask, CharSequence text) throws ParseException {
            return mask.parseTime(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>data e hora</code>.
     */
    public static final class DatetimeFormatter extends TypedFormatter<IDatetimeMask, Timestamp> {

        public DatetimeFormatter(IDatetimeMask mask) {
            super(mask);
        }

        @Override
        protected Timestamp getValue(IEntityRecord entityRecord, String attribute) {
            return entityRecord.getDatetimeValue(attribute);
        }

        @Override
        protected CharSequence formatValue(IDatetimeMask mask, Timestamp value) {
            return mask.formatDatetime(value);
        }

        @Override
        protected Timestamp parseValue(IDatetimeMask mask, CharSequence text) throws ParseException {
            return mask.parseDatetime(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>caractere</code>.
     */
    public static final class CharacterFormatter extends TypedFormatter<ICharacterMask, Character> {

        public CharacterFormatter(ICharacterMask mask) {
			super(mask);
		}

		@Override
        protected Character getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getCharacterValue(attribute);
		}

		@Override
        protected CharSequence formatValue(ICharacterMask mask, Character value) {
			return mask.formatCharacter(value);
		}

        @Override
        protected Character parseValue(ICharacterMask mask, CharSequence text) throws ParseException {
            return mask.parseCharacter(text);
        }
    }

    /**
     * Formatador específico para o tipo <code>imagem</code>.
     */
    public static final class ImageFormatter extends TypedFormatter<IImageMask, Bitmap> {

        public ImageFormatter(IImageMask mask) {
			super(mask);
		}

		@Override
        protected Bitmap getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getImageValue(attribute);
		}

		@Override
        protected Drawable formatValue(IImageMask mask, Bitmap value) {
			return mask.formatImage(value);
		}

        @Override
        protected Bitmap parseValue(IImageMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("IMAGE value parser");
        }

        @Override
        protected boolean formatToText() {
            return false;
        }
    }

    /**
     * Formatador específico para o tipo <code>array de inteiros</code>.
     */
    public static final class IntegerArrayFormatter extends TypedFormatter<IIntegerArrayMask, Integer[]> {

        public IntegerArrayFormatter(IIntegerArrayMask mask) {
			super(mask);
		}

		@Override
        protected Integer[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getIntegerArrayValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IIntegerArrayMask mask, Integer[] value) {
			return mask.formatIntegerArray(value);
		}

        @Override
        protected Integer[] parseValue(IIntegerArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("INTEGER_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de decimais</code>.
     */
    public static final class DecimalArrayFormatter extends TypedFormatter<IDecimalArrayMask, Double[]> {

        public DecimalArrayFormatter(IDecimalArrayMask mask) {
			super(mask);
		}

		@Override
        protected Double[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getDecimalArrayValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IDecimalArrayMask mask, Double[] value) {
			return mask.formatDecimalArray(value);
		}

        @Override
        protected Double[] parseValue(IDecimalArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("DECIMAL_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de textos</code>.
     */
    public static final class TextArrayFormatter extends TypedFormatter<ITextArrayMask, String[]> {

        public TextArrayFormatter(ITextArrayMask mask) {
			super(mask);
		}

		@Override
        protected String[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getTextArrayValue(attribute);
		}

		@Override
        protected CharSequence formatValue(ITextArrayMask mask, String[] value) {
			return mask.formatTextArray(value);
		}

        @Override
        protected String[] parseValue(ITextArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("TEXT_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de booleanos</code>.
     */
    public static final class BooleanArrayFormatter extends TypedFormatter<IBooleanArrayMask, Boolean[]> {

        public BooleanArrayFormatter(IBooleanArrayMask mask) {
			super(mask);
		}

		@Override
        protected Boolean[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getBooleanArrayValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IBooleanArrayMask mask, Boolean[] value) {
			return mask.formatBooleanArray(value);
		}

        @Override
        protected Boolean[] parseValue(IBooleanArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("BOOLEAN_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de datas</code>.
     */
    public static final class DateArrayFormatter extends TypedFormatter<IDateArrayMask, Date[]> {

        public DateArrayFormatter(IDateArrayMask mask) {
			super(mask);
		}

		@Override
        protected Date[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getDateArrayValue(attribute);
		}

		@Override
        protected CharSequence formatValue(IDateArrayMask mask, Date[] value) {
			return mask.formatDateArray(value);
		}

        @Override
        protected Date[] parseValue(IDateArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("DATE_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de horas</code>.
     */
    public static final class TimeArrayFormatter extends TypedFormatter<ITimeArrayMask, Time[]> {

        public TimeArrayFormatter(ITimeArrayMask mask) {
			super(mask);
		}

		@Override
        protected Time[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getTimeArrayValue(attribute);
		}

		@Override
        protected CharSequence formatValue(ITimeArrayMask mask, Time[] value) {
			return mask.formatTimeArray(value);
		}

        @Override
        protected Time[] parseValue(ITimeArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("TIME_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de datas e horas</code>.
     */
    public static final class DatetimeArrayFormatter extends TypedFormatter<IDatetimeArrayMask, Timestamp[]> {

        public DatetimeArrayFormatter(IDatetimeArrayMask mask) {
            super(mask);
        }

        @Override
        protected Timestamp[] getValue(IEntityRecord entityRecord, String attribute) {
            return entityRecord.getDatetimeArrayValue(attribute);
        }

        @Override
        protected CharSequence formatValue(IDatetimeArrayMask mask, Timestamp[] value) {
            return mask.formatDatetimeArray(value);
        }

        @Override
        protected Timestamp[] parseValue(IDatetimeArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("DATETIME_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de caracteres</code>.
     */
    public static final class CharacterArrayFormatter extends TypedFormatter<ICharacterArrayMask, Character[]> {

        public CharacterArrayFormatter(ICharacterArrayMask mask) {
			super(mask);
		}

		@Override
        protected Character[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getCharacterArrayValue(attribute);
		}

		@Override
        protected CharSequence formatValue(ICharacterArrayMask mask, Character[] value) {
			return mask.formatCharacterArray(value);
		}

        @Override
        protected Character[] parseValue(ICharacterArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("CHARACTER_ARRAY value parser");
        }
    }

    /**
     * Formatador específico para o tipo <code>array de imagens</code>.
     */
    public static final class ImageArrayFormatter extends TypedFormatter<IImageArrayMask, Bitmap[]> {

        public ImageArrayFormatter(IImageArrayMask mask) {
			super(mask);
		}

		@Override
        protected Bitmap[] getValue(IEntityRecord entityRecord, String attribute) {
			return entityRecord.getImageArrayValue(attribute);
		}

		@Override
        protected Drawable formatValue(IImageArrayMask mask, Bitmap[] value) {
			return mask.formatImageArray(value);
		}

        @Override
        protected Bitmap[] parseValue(IImageArrayMask mask, CharSequence text) throws ParseException {
            throw new PendingFeatureException("IMAGE_ARRAY value parser");
        }

        @Override
        protected boolean formatToText() {
            return false;
        }
    }
}
