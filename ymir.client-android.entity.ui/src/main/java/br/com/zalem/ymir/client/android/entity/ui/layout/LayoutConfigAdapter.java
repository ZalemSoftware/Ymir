package br.com.zalem.ymir.client.android.entity.ui.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutField;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldType;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.LayoutFieldVisibility;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;
import br.com.zalem.ymir.client.android.util.SimpleEnumMap;

/**
 * Adapter de {@link ILayoutConfig configurações de layouts} para {@link View Views} que representam os dados de {@link IEntityRecord IEntityRecords}.<br>
 * As Views são geradas a partir de um recurso de layout Android que possua exatamente o mesmo nome do tipo de layout definido na configuração.<br>
 * <br>
 * Cada recurso de layout Android (por exemplo, list_layout_4.xml) deve possuir as Views correspondentes aos campos do layout que está atendendo
 * (pode ser verificado através de {@link ILayoutType#getFields()}).<br>
 * O tipo de View que deve ser utilizado depende do tipo do campo ({@link LayoutField#getType()}).
 * Segue a relação:
 * <ul>
 * 	<li>{@link LayoutFieldType#TEXT} -> {@link TextView}</li>
 * 	<li>{@link LayoutFieldType#IMAGE} -> {@link ImageView}</li>
 * </ul>
 * 
 * O mapeamento entre a View declarada no recurso de layout e o campo do layout é feito através da definição do nome do campo no
 * atributo <code>android:tag</code> da View. Esta definição pode ser observada no exemplo de recurso de layout abaixo:
 * 
 * <pre>
 * 	{@code
 *  <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
 *	    android:layout_width="match_parent"
 *	    android:layout_height="match_parent" >
 *	
 *		    <TextView
 *		        android:layout_width="match_parent"
 *		        android:layout_height="wrap_content"
 *		        android:tag="text1" />
 *	</LinearLayout>
 *	}
 * </pre>
 *
 * @author Thiago Gesser
 */
public final class LayoutConfigAdapter {
	
	private final int layoutResId;
	private final ILayoutFieldMapping[] fieldMappings;
	private final EntityAttributeFormatter attributesFormatter;
	private final boolean useSingleViewHolder;
	private LayoutInflater inflater;

	public LayoutConfigAdapter(Context context, ILayoutConfig<?> layoutConfig, EntityAttributeFormatter fieldFormatter) {
		this(context, layoutConfig, fieldFormatter, LayoutInflater.from(context));
	}
	
	public LayoutConfigAdapter(Context context, ILayoutConfig<?> layoutConfig, EntityAttributeFormatter attributesFormatter, LayoutInflater layoutInflater) {
		this.fieldMappings = layoutConfig.getFields();
		validateFieldMappings(fieldMappings);
		this.attributesFormatter = attributesFormatter;
		this.inflater = layoutInflater;

		String layoutName = layoutConfig.getType().getName();
		//Os recursos do Android podem possuir apenas letras minúsculas.
		//Utiliza o Locale.US pq de acordo com o warning Lint, usar o Locale default as vezes pode não retornar a versão minúscula da letra.
		layoutName = layoutName.toLowerCase(Locale.US);

		//Procura o recurso de layout Android correspondente ao nome do layout.
		int layoutResId = context.getResources().getIdentifier(layoutName, "layout", context.getPackageName());
		if (layoutResId == 0) {
			throw new IllegalArgumentException("There is no layout resource with the name: " + layoutName);
		}
		this.layoutResId = layoutResId;
		this.useSingleViewHolder = layoutConfig.getFields().length == 1;
	}

	/**
	 * Cria uma View para mostrar os dados de um registro da entidade de acordo com o recurso de
	 * layout Android definido na configuração.<br>
	 * Os dados podem ser definidos posteriormente através do método {@link #setViewValues(IEntityRecord, View)}.
	 * 
	 * @param parent View pai utilizada na criação da View.
	 * @return a View criada.
	 */
	public View createView(ViewGroup parent) {
        if (inflater == null) {
            throw new IllegalStateException("LayoutInflater is not defined");
        }

        return inflater.inflate(layoutResId, parent, false);
	}
	
	/**
	 * Define os dados do registro da entidade nos campos de uma View criada previamente através do método
	 * {@link #createView(ViewGroup)}.<br>
	 * Os campos são buscados na View toda vez que este método é chamado.
	 * 
	 * @param entityRecord registro da entidade
	 * @param view View criada previamente.
	 */
	public void setViewValues(IEntityRecord entityRecord, View view) {
		for (ILayoutFieldMapping fieldMapping : fieldMappings) {
            if (fieldMapping.getVisibility() != LayoutFieldVisibility.VISIBLE) {
                continue;
            }

			View fieldView = findFieldView(view, fieldMapping.getLayoutField());
			setFieldValue(fieldView, fieldMapping, entityRecord);
		}
	}

	/**
	 * Cria uma View para mostrar os dados de um registro da entidade de acordo com o recurso de layout Android definido na configuração.<br>
	 * Os dados podem ser definidos posteriormente através do método {@link #setHeldViewValues(IEntityRecord, View)}.<br>
	 * <br>
	 * Utiliza o conceito de View Holder para armazenar referencias aos campos de dados da View criada em seu atributo <code>tag</code>.
     * Desta forma, não será necessário buscar os campos toda vez em que se deseja definir os dados.
	 *
	 * @param parent View pai utilizada na criação da View.
	 * @return a View criada.
	 */
	public View createAndHoldView(ViewGroup parent) {
    	/*
    	 * 1. Cria a View de acordo com o layout xml correspondente ao tipo de layout vindo da configuração.
    	 * 2. Busca os campos da View a partir das tags definidas neles. As tags devem ser correspondentes ao nomes
    	 * 	  do campos vindos da configuração.
    	 * 3. Utiliza o pattern View Holder para armazeá-los. Desta forma, se esta View for reutilizada, a busca pelos
    	 * 	  campos não precisará ser executada novamente.
    	 */

        View view = createView(parent);
        Object viewHolder = createViewHolder(view);
		view.setTag(viewHolder);
        return view;
	}

	/**
	 * Define os dados do registro da entidade nos campos de uma View criada previamente através do método
	 * {@link #createAndHoldView(ViewGroup)}.<br>
	 * Os campos são obtidos através do armazenamento das referências feita utilizando o conceito de View Holder.
	 * Desta forma, se este método for chamado com uma View que não possua este armazenamento de referências no atributo <code>tag</code>,
     * resultará no lançamento de um <code>IllegalArgumentException</code>.
	 *
	 * @param entityRecord registro da entidade
	 * @param view View criada previamente.
	 * @throws IllegalArgumentException se a View não foi criada através do método {@link #createAndHoldView(ViewGroup)}.
	 */
	public void setHeldViewValues(IEntityRecord entityRecord, View view) {
		Object viewHolder = view.getTag();
        bindViewHolder(entityRecord, viewHolder);
	}

    /**
     * Cria um View Holder para armazenar as referências aos campos de dados de uma View gerada através do método {@link #createView(ViewGroup)}.
     * Os dados podem ser definidos posteriormente através do método {@link #bindViewHolder(IEntityRecord, Object)}.
     *
     * @return o View Holder criado.
     */
    public Object createViewHolder(View view) {
        if (fieldMappings.length == 0) {
            return null;
        }

        if (useSingleViewHolder) {
            ILayoutFieldMapping fieldMapping = fieldMappings[0];
            View fieldView = findFieldView(view, fieldMapping.getLayoutField());
            //Se o campo de layout não está visível, ele nem precisa ser mapeado pois não terá valor definido.
            if (configureFieldViewVisibility(fieldView, fieldMapping)) {
                view.setTag(fieldView);
            }

            return view;
        }

        SimpleEnumMap<LayoutField, View> viewHolder = new SimpleEnumMap<>(LayoutField.class);
        for (ILayoutFieldMapping fieldMapping : fieldMappings) {
            LayoutField layoutField = fieldMapping.getLayoutField();
            View fieldView = findFieldView(view, layoutField);
            //Se o campo de layout não está visível, ele nem precisa ser mapeado pois não terá valor definido.
            if (configureFieldViewVisibility(fieldView, fieldMapping)) {
                viewHolder.put(layoutField, fieldView);
            }
        }
        return viewHolder;
    }

    /**
     * Define os dados do registro da entidade nos campos mantidos por um View Holder criado previamente através do método {@link #createViewHolder(View)}.<br>
     *
     * @param entityRecord registro de entidade.
     * @param viewHolder o Holder com as referências para as Views.
     */
    @SuppressWarnings("unchecked")
    public void bindViewHolder(IEntityRecord entityRecord, Object viewHolder) {
        if (viewHolder == null) {
            return;
        }

        if (useSingleViewHolder) {
            ILayoutFieldMapping fieldMapping = fieldMappings[0];
            if (fieldMapping.getVisibility() == LayoutFieldVisibility.VISIBLE) {
                View fieldView = (View) viewHolder;
                setFieldValue(fieldView, fieldMapping, entityRecord);
            }
            return;
        }

        SimpleEnumMap<LayoutField, View> viewsMap = (SimpleEnumMap<LayoutField, View>) viewHolder;
        for (ILayoutFieldMapping fieldMapping : fieldMappings) {
            if (fieldMapping.getVisibility() != LayoutFieldVisibility.VISIBLE) {
                continue;
            }

            View fieldView = viewsMap.get(fieldMapping.getLayoutField());
            setFieldValue(fieldView, fieldMapping, entityRecord);
        }
    }



    /**
     * Obtem o {@link LayoutInflater} utilizado pelo adapter.
     *
     * @return o inflater obtido.
     */
    public LayoutInflater getInflater() {
        return inflater;
    }

    /**
     * Define o {@link LayoutInflater} do adapter.
     *
     * @param inflater o inflater que será utilizado pelo adapter.
     */
    public void setInflater(LayoutInflater inflater) {
        this.inflater = inflater;
    }


    /*
	 * Métodos auxiliares
	 */
	
	private View findFieldView(View view, LayoutField layoutField) {
		View fieldView = view.findViewWithTag(layoutField.name());
    	if (fieldView == null) {
    		throw new RuntimeException("No View found with the tag: " + layoutField.name());
    	}
    	
    	return fieldView;
	}

    private boolean configureFieldViewVisibility(View fieldView, ILayoutFieldMapping fieldMapping) {
        switch (fieldMapping.getVisibility()) {
            case VISIBLE:
                fieldView.setVisibility(View.VISIBLE);
                return true;

            case INVISIBLE:
                fieldView.setVisibility(View.INVISIBLE);
                return false;
            case GONE:
                fieldView.setVisibility(View.GONE);
                return false;

            default:
                throw new RuntimeException("Unsupported LayoutFieldVisibility: " + fieldMapping.getVisibility());
        }
    }
	
	private void setFieldValue(View fieldView, ILayoutFieldMapping fieldMapping, IEntityRecord record) {
		//Seta o valor no campo de acordo com o seu tipo.
		LayoutField layoutField = fieldMapping.getLayoutField();
		switch (layoutField.getType()) {
			case TEXT:
				setStringFieldValue(fieldView, fieldMapping, record);
				break;
				
			case IMAGE:
				setImageFieldValue(fieldView, fieldMapping, record);
				break;
				
			default:
				throw new RuntimeException("Unsupported LayoutFieldType: " + layoutField.getType());
		}
	}

	private void setStringFieldValue(View fieldView, ILayoutFieldMapping fieldMapping, IEntityRecord record) {
		TextView textView;
		try {
			textView = (TextView) fieldView;
		} catch (ClassCastException e) {
			//Utiliza try catch de ClassCastException para evitar o instanceof (da mesma forma que o ArrayAdapter faz).
			throw new RuntimeException("Layout fields of the TEXT type should be mapped to TextViews. View id = " + fieldView.getId());
		}
		
		//Formata o valor utilizandos a máscara definida para o campo, de acordo com o seu tipo.
		CharSequence value = attributesFormatter.formatAttributeValueToText(record, fieldMapping.getAttribute());
		if (value == null) {
			//Se o valor do campo em determinado registro for nulo, atribui um valor padrão (de acordo com o tipo do campo).
			textView.setText(R.string.null_text_value);
			return;
		}
		
		textView.setText(value);
	}
	
	private void setImageFieldValue(View fieldView, ILayoutFieldMapping fieldMapping, IEntityRecord record) {
		ImageView imageView;
		try {
			imageView = (ImageView) fieldView;
		} catch (ClassCastException e) {
			//Utiliza try catch de ClassCastException para evitar o instanceof (da mesma forma que o ArrayAdapter faz).
			throw new RuntimeException("Layout fields of the IMAGE type should be mapped to ImageView. View id = " + fieldView.getId());
		}
		
		//Primeiro tenta setar a imagem diretamente do cache.
		if (setCachedImageValue(imageView, fieldMapping, record, attributesFormatter)) {
			return;
		}

		//Se não foi possível setar a imagem a partir do cache, obtém a imagem do campo do registro em background e seta apenas depois disto.
		//A task roda no AsyncTask.SERIAL_EXECUTOR, de forma que apenas uma imagem é carregada de cada vez.
		new ImageLoaderTask(imageView, fieldMapping, record, attributesFormatter).execute();
	}
	
	private static boolean setCachedImageValue(ImageView imageView, ILayoutFieldMapping fieldMapping, IEntityRecord record, EntityAttributeFormatter attributesFormatter) {
        String[] attributePath = fieldMapping.getAttribute();
		//Vai passando pelos relacionamentos até chegar no registro fonte do valor. Se algum relacionamento do caminho for nulo, seta a imagem de valor nulo.
        for (int i = 0; i < attributePath.length-1; i++) {
            String relationship = attributePath[i];
            record = record.getRelationshipValue(relationship);
            if (record == null) {
                setImageViewValue(imageView, null, fieldMapping, attributesFormatter, null);
                return true;
            }
        }

        String attribute = attributePath[attributePath.length -1];
        Bitmap cacheValue = record.getImageValue(attribute, true);
        //Se a imagem não está no cache, ela deve ser carregada diretamente do campo do registro em background.
        //Entretanto, se o valor do campo em si for nulo, pode setar diretamente a imagem de valor nulo, evitando a criação da Task.
        if (cacheValue == null && !record.isNull(attribute)) {
            return false;
        }

        setImageViewValue(imageView, cacheValue, fieldMapping, attributesFormatter, record);
        return true;
	}

	private static void setImageViewValue(ImageView imageView, Bitmap value, ILayoutFieldMapping fieldMapping, EntityAttributeFormatter attributesFormatter, IEntityRecord record) {
		if (value == null) {
            //Se o valor da imagem é nulo, tenta utilizar o valor do campo substituto (se definido) para obter a primeira letra e criar uma representação dela.
            String surrogateText = null;
            if (record != null) {
                String surrogateAttribute = fieldMapping.getSurrogateAttribute();
                if (!TextUtils.isEmpty(surrogateAttribute)) {
                    String surrogateValue = record.getTextValue(surrogateAttribute);
                    if (!surrogateValue.isEmpty()) {
                        surrogateText = surrogateValue.substring(0, 1);
                    }
                }
            }

            if (surrogateText == null) {
                //Se o valor do campo em determinado registro for nulo, atribui um valor padrão.
                imageView.setImageResource(R.drawable.null_image_value);
            } else {
                //Se possui texto substituto, utiliza-o para montar uma imagem que o representa.
                ColorGenerator generator = getColorGenerator(imageView.getContext());
                TextDrawable textDrawable = TextDrawable.builder().buildRound(surrogateText, generator.getColor(surrogateText));
                imageView.setImageDrawable(textDrawable);
            }
			return;
		}

        Drawable imageDrawable = attributesFormatter.formatValue(value, fieldMapping.getAttribute());
        imageView.setImageDrawable(imageDrawable);
	}

    private static ColorGenerator colorGenerator;
    private static ColorGenerator getColorGenerator(Context context) {
        if (colorGenerator == null) {
            int[] colors = context.getResources().getIntArray(R.array.config_layout_image_surrogate_background_colors);
            ArrayList<Integer> colorsList = new ArrayList<>(colors.length);
            for (int color : colors) {
                colorsList.add(color);
            }
            colorGenerator = ColorGenerator.create(colorsList);
        }
        return colorGenerator;
    }
	
	private static void validateFieldMappings(ILayoutFieldMapping[] fieldMappings) {
		for (ILayoutFieldMapping fieldMapping : fieldMappings) {
            if (fieldMapping.getVisibility() != LayoutFieldVisibility.VISIBLE) {
                continue;
            }

            //Por enquanto só suporta atributos.
            String[] attributePath = fieldMapping.getAttribute();
            if (attributePath == null || attributePath.length == 0) {
                throw new IllegalArgumentException(String.format("Invalid layout field mapping (%s): only mappings leading to an attribute are supported and not to a relationship: %s.", fieldMapping.getLayoutField(), Arrays.toString(fieldMapping.getRelationship())));
            }
        }
	}

	
	/**
	 * Carrega uma imagem diretamente do campo do registro, setando a imagem no {@link ImageView} após o carregamento.
	 */
	private static final class ImageLoaderTask extends AsyncTask<Void, Void, ImageLoadResult> {
		
		private final WeakReference<ImageView> imageViewRef;
		private final ILayoutFieldMapping fieldMapping;
		private final IEntityRecord record;
        private final EntityAttributeFormatter attributesFormatter;

        public ImageLoaderTask(ImageView imageView, ILayoutFieldMapping fieldMapping, IEntityRecord record, EntityAttributeFormatter attributesFormatter) {
            this.attributesFormatter = attributesFormatter;
            this.imageViewRef = new WeakReference<>(imageView);
			this.fieldMapping = fieldMapping;
			this.record = record; 
		}
		
		@Override
		protected void onPreExecute() {
			//Seta nulo para remover qualquer imagem da view.
			imageViewRef.get().setImageDrawable(null);
		}
		
		@Override
		protected ImageLoadResult doInBackground(Void... params) {
			return getImageValue(fieldMapping.getAttribute(), record);
		}
		
		@Override
		protected void onPostExecute(ImageLoadResult value) {
			ImageView imageView = imageViewRef.get();
			if (imageView == null) {
				return;
			}
			setImageViewValue(imageView, value.getImage(), fieldMapping, attributesFormatter, value.getRecord());
		}

        private static ImageLoadResult getImageValue(String[] attributePath, IEntityRecord record) {
            for (int i = 0; i < attributePath.length-1; i++) {
                String relationship = attributePath[i];
                record = record.getRelationshipValue(relationship);
                if (record == null) {
                    return new ImageLoadResult(null, null);
                }
            }

            String attribute = attributePath[attributePath.length -1];
            Bitmap imageValue = record.getImageValue(attribute, false);
            return new ImageLoadResult(imageValue, record);
        }
	}

    private static final class ImageLoadResult {
        private Bitmap image;
        private IEntityRecord record;

        public ImageLoadResult(Bitmap image, IEntityRecord record) {
            this.image = image;
            this.record = record;
        }

        public Bitmap getImage() {
            return image;
        }

        public IEntityRecord getRecord() {
            return record;
        }
    }
}
