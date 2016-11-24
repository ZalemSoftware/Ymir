package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.BasicMaskType;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.BRLCurrencyMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.BrazilianDateMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.BrazilianDatetimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.CircularImageMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.DefaultCurrencyMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.DefaultDateMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.DefaultDatetimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.DefaultTimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.InternationalDateMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.InternationalDatetimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.TwelveTimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.TwentyFourTimeMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.USDCurrencyMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.basic.YesNoBooleanMask;

/**
 * Gerenciador de {@link IMask máscaras}, responsável por provê-las para a aplicação.<br>
 * Permite a obtenção de máscaras pré-definidas, como as {@link #getBasicMask(BasicMaskType) básicas} e as {@link #getDefaultMask(EntityAttributeType) padrões},
 * além de {@link #getMask(String) máscaras customizadas} a partir do nome de sua classe.<br>
 * <br>
 * As máscaras são instanciadas sob demanda e mantidas no gerenciador, fazendo com que a mesma máscara não seja instanciada mais de uma vez.
 *
 * @author Thiago Gesser
 */
public final class MaskManager {
	
	private final Map<String, IMask> masks = new HashMap<>();
	private final Context context;
    private final IMask defaultMask;


    /**
     * Cria um gerenciador de mascaras utilizando como máscara padrão o {@link DefaultMask}.
     */
    public MaskManager(Context context) {
        this(context, DefaultMask.getInstance(context));
    }

    /**
     * Cria um gerenciador de mascaras utilizando a máscara padrão definida. A máscara padrão deve implementar todos os tipos de máscaras
     * que podem ser utilizados.
     *
     * @param defaultMask a máscara padrão.
     */
	public MaskManager(Context context, IMask defaultMask) {
		this.context = context;
        this.defaultMask = defaultMask;
	}

    /**
	 * Obtém uma instância de máscara através do nome de uma classe que estenda de {@link IMask}.
	 * 
	 * @param maskClassName nome completo da classe da máscara.
	 * @return a máscara obtida.
	 */
	@SuppressWarnings("TryWithIdenticalCatches")
    public IMask getMask(String maskClassName) {
		//Se já foi criada, retorna a instância já existente.
		IMask mask = masks.get(maskClassName);
		if (mask != null) {
			return mask;
		}

		try {
			Class<?> maskClass = Class.forName(maskClassName);
			//A máscara deve ser um IMask.
			if (!IMask.class.isAssignableFrom(maskClass)) {
				throw new IllegalArgumentException(String.format("The customized mask class \"%s\" is not an IMask.", maskClass));
			}

            //Se não possui um construtor com Context, instancia pelo construtor sem parâmetros.
            try {
                Constructor<?> constructor = maskClass.getConstructor(Context.class);
                mask = (IMask) constructor.newInstance(context);
            } catch (NoSuchMethodException e) {
			    mask = (IMask) maskClass.newInstance();
            }

            //Mantém a máscara criada no cache para evitar criá-la novamente.
            masks.put(maskClassName, mask);
            return mask;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }
	
	/**
	 * Obtém uma instância de máscara básica a partir do seu tipo.
	 * 
	 * @param maskType tipo de máscara básica.
	 * @return a máscara obtida.
	 */
	public IMask getBasicMask(BasicMaskType maskType) {
		//Se já foi criada, retorna a instância já existente.
		IMask mask = masks.get(maskType.toString());
		if (mask != null) {
			return mask;
		}
		
		//Cria a máscara básica de acordo com o seu tipo.
		switch (maskType) {
			case CURRENCY_DEFAULT:
                mask = new DefaultCurrencyMask();
                break;
			case CURRENCY_BRL:
				mask = new BRLCurrencyMask();
				break;
			case CURRENCY_USD:
				mask = new USDCurrencyMask();
				break;
			case DATE_DEFAULT:
				mask = new DefaultDateMask(context);
				break;
			case DATE_BRAZILIAN:
				mask = new BrazilianDateMask();
				break;
			case DATE_INTERNATIONAL:
				mask = new InternationalDateMask();
				break;
            case TIME_DEFAULT:
                mask = new DefaultTimeMask(context);
                break;
			case TIME_12H:
				mask = new TwelveTimeMask();
				break;
			case TIME_24H:
				mask = new TwentyFourTimeMask();
				break;
            case DATETIME_DEFAULT:
                mask = new DefaultDatetimeMask(context);
                break;
            case DATETIME_BRAZILIAN:
                mask = new BrazilianDatetimeMask();
				break;
            case DATETIME_INTERNATIONAL:
                mask = new InternationalDatetimeMask();
				break;
			case BOOLEAN_YES_NO:
				mask = new YesNoBooleanMask(context);
				break;
			case IMAGE_CIRCULAR:
                mask = new CircularImageMask(context);
                break;

			default:
				throw new IllegalArgumentException("Unsupported BasicMaskType: " + maskType);
		}

		//Mantém a máscara criada no cache para evitar criá-la novamente.
		masks.put(maskType.toString(), mask);
		return mask;
	}

    /**
     * Obtém a mascara padrão para o tipo de atributo.<br>
     * Se nao houver máscara padrão para o tipo em questão, retorna a máscara padrão do gerenciador.
     *
     * @param type tipo de atributo.
     * @return a máscara obtida.
     */
    public IMask getDefaultMask(EntityAttributeType type) {
        switch (type) {
            case DATE:
                return getBasicMask(BasicMaskType.DATE_DEFAULT);
            case TIME:
                return getBasicMask(BasicMaskType.TIME_DEFAULT);
            case DATETIME:
                return getBasicMask(BasicMaskType.DATETIME_DEFAULT);

            case INTEGER:
            case INTEGER_ARRAY:
            case DECIMAL:
            case DECIMAL_ARRAY:
            case TEXT:
            case TEXT_ARRAY:
            case BOOLEAN:
            case BOOLEAN_ARRAY:
            case DATE_ARRAY:
            case TIME_ARRAY:
            case DATETIME_ARRAY:
            case CHARACTER:
            case CHARACTER_ARRAY:
            case IMAGE:
            case IMAGE_ARRAY:
                return defaultMask;

            default:
                throw new IllegalArgumentException("Unsupported EntityAttributeType: " + type);
        }
    }

    /**
     * Obtém uma instância de máscara a partir de uma String de configuração, que pode ser um dos valores do {@link BasicMaskType} ou
     * o nome de uma classe que estenda de {@link IMask}. Se não houver a configuração, será retornada a máscara basica para o tipo do atributo.
     *
     * @param maskConfig String de definição de máscara.
     * @param entityAttributeType tipo do atributo.
     * @return a máscara obtida.
     */
    public IMask getMaskFromConfig(String maskConfig, EntityAttributeType entityAttributeType) {
        //Se não possui configuração, retorna a padrão para o tipo de atributo.
        if (maskConfig == null) {
            return getDefaultMask(entityAttributeType);
        }

        //Se for uma máscara básica, cria a partir do seu tipo.
        try {
            BasicMaskType maskType = BasicMaskType.valueOf(maskConfig);
            return getBasicMask(maskType);
        } catch (IllegalArgumentException e) {
            //IllegalArgumentException é a maneira da API de Enum dizer que não existe tal valor.
        }

        //Se não for uma máscara básica, só pode ser uma classe de máscara customizada.
        return getMask(maskConfig);
    }

    /**
     * Obtém o contexto utilizado no gerenciador de máscaras.
     *
     * @return o contexto obtido.
     */
    public Context getContext() {
        return context;
    }
}
