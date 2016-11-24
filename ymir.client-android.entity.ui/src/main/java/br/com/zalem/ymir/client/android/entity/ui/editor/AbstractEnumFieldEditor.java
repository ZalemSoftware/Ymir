package br.com.zalem.ymir.client.android.entity.ui.editor;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.ui.R;

/**
 * Base para editores do tipo <code>enumeração</code>.<br>
 * Dispõe os valores da enumeração em um Dialog de lista, onde o usuário pode clicar para selecionar o valor desejado.<br>
 * Por padrão, os valores definidos serão listados. Entretanto, o {@link OnValueChangeListener} pode definir valores diferentes.
 * 
 * @author Thiago Gesser
 */
public abstract class AbstractEnumFieldEditor<T> extends AbstractLabeledFieldEditor<T> implements OnClickListener {

    private static final String ENUM_PICKER_FRAGMENT_TAG_PREFIX = "ENUM_PICKER_FRAGMENT_TAG_";

	private final FragmentManager fragmentManager;

    private List<T> defaultValues;
	private TextView textView;
    private String hint;

	private OnListValuesListener<T> valuesListener;

    public AbstractEnumFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
                                   FragmentManager fragmentManager, List<T> defaultValues) {
		super(fieldName, label, editable, hidden, virtual, help);
		this.fragmentManager = fragmentManager;
        this.hint = label;
        if (defaultValues == null) {
            setDefaultValues(Collections.<T>emptyList());
        } else {
            //Não modificável pq esta lista será passada para o listener.
		    setDefaultValues(Collections.unmodifiableList(defaultValues));
        }
	}

	@Override
	@SuppressWarnings("unchecked")
    protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = super.createView(inflater, label, editable, parent);

		//Se o fragmento foi restaurado devido a uma mudança  de orientação, por exemplo, seta o editor novamente nele. Feito aqui ao invés de no construtor por causa do bug descrito em AndroidBugsUtils.applyWorkaroundForInvalidFragmentManagerStateBug.
		EnumPickerFragment enumPickerFragment = (EnumPickerFragment) fragmentManager.findFragmentByTag(getEnumPickerFragmentTag());
		if (enumPickerFragment != null) {
			enumPickerFragment.setEditor(this);
		}
		textView = (TextView) inflater.inflate(R.layout.entity_field_editor_enum, parent, false);
		textView.setHint(hint);
		textView.setEnabled(editable);
		textView.setOnClickListener(this);

        rootView.addView(textView, 1);
        return rootView;
	}

    @Override
    protected void destroyView() {
        super.destroyView();

		textView = null;
	}
	
	@Override
	protected void refreshView(T newValue) {
		super.refreshView(newValue);
		
		if (newValue == null) {
			textView.setText(null);
		} else {
			textView.setText(formatValue(newValue));
		}
	}

    @Override
    public TextView getView() {
        return textView;
    }

    @Override
	public final void onClick(View v) {
        EnumPickerFragment<T> newFragment = new EnumPickerFragment<>();
		newFragment.setEditor(this);
	    newFragment.show(fragmentManager, getEnumPickerFragmentTag());
	}

    /**
	 * Define o listener de listagem dos valores da enumeração.
	 * 
	 * @param valuesListener o novo listener.
	 */
	public final void setOnListValuesListener(OnListValuesListener<T> valuesListener) {
		this.valuesListener = valuesListener;
	}
	
	/**
	 * Obtém o listener de listagem dos valores da enumeração.
	 * 
	 * @return o listener obtido ou <code>null</code> se nenhum listener tiver sido definido ainda.
	 */
	public final OnListValuesListener<T> getOnListValuesListener() {
		return valuesListener;
	}

	/**
	 * Define os valores da enumerarção deste editor.
	 * 
	 * @param defaultValues os novos valores.
	 */
	public final void setDefaultValues(List<T> defaultValues) {
		this.defaultValues = defaultValues;
	}
	
	/**
	 * Obtém os valores da enumeração deste editor.
	 * 
	 * @return os valores obtidos.
	 */
	public final List<T> getDefaultValues() {
		return defaultValues;
	}

    /**
     * Define o hint do editor da enumeração.
     *
     * @param hint o novo hint do editor.
     */
    public void setHint(String hint) {
        this.hint = hint;

        if (isViewCreated()) {
            textView.setHint(hint);
        }
    }


    /**
     * Indica se o editor precisa carregar os valores da enumeração em background, devido a um tempo de carregamento elevado ou incerto.
     *
     * @return <code>true</code> se o editor deve carregar os valores em background e <code>false</code> caso contrário.
     */
    protected boolean needBackgroundLoad() {
        return valuesListener != null;
    }

    /**
     * Executa o carregamento dos valores da enumeração.
     *
     * @return os valores carregados.
     */
    protected List<T> loadValues() {
        if (valuesListener != null) {
            //Se o listener retornou novas entradas, usa elas. Se não, usa as entradas normais.
            List<T> newValues = valuesListener.beforeListValues(this);
            if (newValues != null) {
                return newValues;
            }
        }

        return defaultValues;
    }

    /**
     * Chamado após o carregamento dos valores da enumeração.
     *
     * @param values os valores carregados.
     */
    protected void afterLoadValues(List<T> values) {
        if (valuesListener != null) {
            valuesListener.afterListValues(this, values);
        }
    }


    /**
     * Formata o valor em um texto para ser mostrado no campo de enumeração.
     *
     * @param value o valor a ser formatado
     * @return o texto que representa o valor.
     */
    protected abstract CharSequence formatValue(T value);


    /*
	 * Classes/interfaces/métodos auxiliares
	 */

    private String getEnumPickerFragmentTag() {
        return ENUM_PICKER_FRAGMENT_TAG_PREFIX + getFieldName();
    }

	/**
	 * Listener da listagem dos valores disponíveis no editor do tipo enumeração.
	 */
	public interface OnListValuesListener <T> {
		
		/**
		 * Chamado antes da listagem dos valores da enumeração do editor.<br>
		 * Os valores que serão listados podem ser alterados se o retorno deste método for uma lista de objetos ao invés de <code>null</code>.<br>
		 * 
		 * @param editor editor cujo os valores serão listados.
		 * @return uma lista de valores diferente ou <code>null</code> se a lista de valores do editor deve ser usada.
		 */
		List<T> beforeListValues(AbstractEnumFieldEditor editor);
		
		/**
		 * Chamado depois da listagem dos valores da enumeração do editor.<br>
		 * 
		 * @param values valores que foram listados.
		 */
		void afterListValues(AbstractEnumFieldEditor editor, List<T> values);
	}
	
	/**
	 * Fragmento que exibe um Dialog para a seleção do valor da enumeração.<br> 
	 * Precisa ser público e estático para poder ser instânciado por fora desta classe (quando o fragmento estiver sendo
	 * recuperado, por exemplo).
	 */
	public static class EnumPickerFragment <T> extends DialogFragment implements OnItemClickListener {
		
		private AbstractEnumFieldEditor<T> editor;
		
		private ListView listView;
		private ProgressBar progressBar;
		private TextView emptyView;
		private LoadEntriesTask loadTask;

		void setEditor(AbstractEnumFieldEditor<T> editor) {
			this.editor = editor;

            tryLoadValues();

            //Se o fragmento foi restaurado, o editor pode ter sido setado apenas depois da criação do Dialog, então seta o título agora.
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.setTitle(editor.getLabel());
            }
        }
		
		@Override
        @NonNull
        @SuppressLint("InflateParams")
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Context context = getActivity();
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			View view = layoutInflater.inflate(R.layout.entity_field_editor_enum_dialog, null);
			listView = (ListView) view.findViewById(R.id.entity_field_editor_enum_dialog_list);
			emptyView = (TextView) view.findViewById(R.id.entity_field_editor_enum_dialog_empty_view);
			progressBar = (ProgressBar) view.findViewById(R.id.entity_field_editor_enum_dialog_progress_bar);
			listView.setOnItemClickListener(this);

            tryLoadValues();
			
			Builder builder = new Builder(context);
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.setView(view);

            //Se o fragmento foi restaurado, o editor pode n ter sido setado ainda.
            if (editor != null) {
			    builder.setTitle(editor.getLabel());
            } else {
                AndroidBugsUtils.applyWorkaroundForAlertDialogWithInvisibleTitleBug(builder);
            }

			return builder.create();
		}
		
		@Override
		public void onDestroy() {
			super.onDestroy();
			editor = null;
			
			if (loadTask != null) {
				loadTask.cancel(true);
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//Seta o valor da entrada no campo.
			EnumEntryAdapter adapter = (EnumEntryAdapter) parent.getAdapter();
            T value = adapter.getItem(position);
			editor.setValue(value);
			dismiss();
		}
		
		
		/*
		 * Métodos/classes auxiliares
		 */

        private void tryLoadValues() {
            //Só carrega quando for possivel.
            if (editor == null || listView == null) {
                return;
            }

            //Se possui um listener, roda a obtenção das entradas em background pois não se sabe o tempo que isto pode levar.
            if (editor.needBackgroundLoad()) {
                loadTask = new LoadEntriesTask();
                loadTask.execute();
            } else {
                setValues(editor.loadValues());
            }
        }
		
		private void setValues(List<T> values) {
			if (values.isEmpty()) {
				emptyView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.INVISIBLE);
				progressBar.setVisibility(View.GONE);
				return;
			}
			
			//Coloca os nomes das entradas na lista.
			listView.setAdapter(new EnumEntryAdapter(getActivity(), values));
			
			listView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
			progressBar.setVisibility(View.GONE);
		}
		
		/**
		 * Carrega os valores de enumeração e chama o {@link OnListValuesListener#beforeListValues(AbstractEnumFieldEditor)}
		 * em background. A definição dos valores na lista e a chamada ao {@link OnListValuesListener#afterListValues(AbstractEnumFieldEditor, List)}
		 * é feita na Thread de UI.
		 */
		private final class LoadEntriesTask extends AsyncTask<Void, Void, List<T>> {

			@Override
			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
				listView.setVisibility(View.INVISIBLE);
			}
			
			@Override
			protected List<T> doInBackground(Void... params) {
                return editor.loadValues();
			}
			
			@Override
			protected void onPostExecute(List<T> valeus) {
				setValues(valeus);
				
                editor.afterLoadValues(valeus);
			}
		}

		/**
		 * Adapter dos valores de enumeração do editor para {@link TextView} com o valor formatado de acordo com o método {@link AbstractEnumFieldEditor#formatValue(Object)}.
		 */
		private final class EnumEntryAdapter extends BaseAdapter {
	
			private final LayoutInflater inflater;
			private final List<T> values;
	
			public EnumEntryAdapter(Context context, List<T> values) {
				this.inflater = LayoutInflater.from(context);
				this.values = values;
			}
			
			@Override
			public int getCount() {
				return values.size();
			}
	
			@Override
			public T getItem(int position) {
				return values.get(position);
			}
	
			@Override
			public long getItemId(int position) {
				return position;
			}
	
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
		        TextView textView;
	
		        if (convertView == null) {
		        	textView = (TextView) inflater.inflate(R.layout.entity_field_editor_enum_dialog_item, parent, false);
		        } else {
		        	textView = (TextView) convertView;
		        }

                T value = values.get(position);
				CharSequence text = null;
				if (value != null) {
					text = editor.formatValue(value);
				}
		        textView.setText(text);
				return textView;
			}
			
		}
	}
}
