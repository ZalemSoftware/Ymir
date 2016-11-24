package br.com.zalem.ymir.client.android.entity.ui.layout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ILayoutConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.ListLayoutType;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.ListLayoutConfigViewHolder;
import br.com.zalem.ymir.client.android.entity.ui.text.EntityAttributeFormatter;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;

/**
 * Adapter de {@link ILayoutConfig configurações de layouts} do tipo {@link ListLayoutType lista} para {@link View Views} que representam os
 * dados de {@link IEntityRecord IEntityRecords}.<br>
 * Implementa o {@link RecyclerView.Adapter} para que possa ser utilizado com o {@link RecyclerView}. As views são criadas a partir de um
 * {@link LayoutConfigAdapter}.<br>
 * <br>
 * O ListLayoutConfigAdapter permite a configuração de ações para as Views geradas dos registros. Mais detalhes podem ser vistos diretamente em
 * {@link #setActionProvider(IEntityRecordListActionProvider)}.
 *
 * @see LayoutConfigAdapter
 * 
 * @author Thiago Gesser
 */
public final class ListLayoutConfigAdapter extends RecyclerView.Adapter<ListLayoutConfigViewHolder> {
	
	private final Context context;
	private final LayoutInflater inflater;
	private final LayoutConfigAdapter layoutAdapter;

	private List<IEntityRecord> records;
	private ActionMenuAdapter actionMenuAdapter;
    private OnItemClickListener itemClickListener;

	public ListLayoutConfigAdapter(Context context, ILayoutConfig<ListLayoutType> layoutConfig, EntityAttributeFormatter fieldFormatter) {
		this(context, layoutConfig, Collections.<IEntityRecord>emptyList(), fieldFormatter);
	}
	
	public ListLayoutConfigAdapter(Context context, ILayoutConfig<ListLayoutType> layoutConfig, List<IEntityRecord> records, EntityAttributeFormatter fieldFormatter) {
		this.context = context;
		this.records = records;
		this.inflater = LayoutInflater.from(context);
		this.layoutAdapter = new LayoutConfigAdapter(context, layoutConfig, fieldFormatter, inflater);
	}

    @Override
    public ListLayoutConfigViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = createView(viewGroup);
        return createViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ListLayoutConfigViewHolder viewHolder, int position) {
		IEntityRecord entityRecord = records.get(position);
        bindViewHolder(viewHolder, entityRecord);
	}

    @Override
    public int getItemCount() {
        return records.size();
    }


    /**
     * Cria a View espeficiada pela configuração de layout.
     *
     * @param parent View pai.
     * @return a View criada.
     */
    public View createView(ViewGroup parent) {
        return layoutAdapter.createView(parent);
    }

    /**
     * Cria o View Holder para a View gerada a partir da configuração de layout. Ele armazena referências diretas às Views que serão
     * utilizadas para a exibição de valores, evitando o tempo de buscá-las a cada vez.
     *
     * @param view View base do View Holder.
     * @return o View Holder criado.
     */
    @NonNull
    public ListLayoutConfigViewHolder createViewHolder(View view) {
        return new ListLayoutConfigViewHolder(view, layoutAdapter.createViewHolder(view));
    }

    /**
     * Vincula os valores do registro às Views referenciadas pelo View Holder.
     *
     * @param viewHolder View Holder que armazena referências diretas às Views que terão os valores vinculados.
     * @param entityRecord registro cujo os valores serão exibidos nas Views.
     */
    public void bindViewHolder(ListLayoutConfigViewHolder viewHolder, IEntityRecord entityRecord) {
        layoutAdapter.bindViewHolder(entityRecord, viewHolder.getLayoutHolder());

        if (actionMenuAdapter != null && !actionMenuAdapter.isEmpty()) {
            final ViewGroup actionMenuContainer = viewHolder.getActionMenuContainer();
            if (actionMenuContainer == null) {
                throw new IllegalArgumentException("Menu container not found with id \"list_layout_record_action_container\".");
            }

            final View actionView = actionMenuContainer.getChildAt(0);
            final View newActionView = actionMenuAdapter.getView(entityRecord, actionView, actionMenuContainer);

            //Se não tem View, o container não precisa estar visível.
            if (newActionView == null) {
                actionMenuContainer.setVisibility(View.GONE);
            } else {
                actionMenuContainer.setVisibility(View.VISIBLE);

                //Só adiciona no container se a View de menu ainda não está adicionada ou se ela mudou.
                if (actionView != newActionView) {
                    if (actionView != null) {
                        actionMenuContainer.removeView(actionView);
                    }
                    actionMenuContainer.addView(newActionView);

                    //Aplica um TouchDelegate para facilitar o click na ação.
                    actionMenuContainer.post(new Runnable() {
                        public void run() {
                            Rect r = new Rect();
                            newActionView.getHitRect(r);
                            r.top = 0;
                            r.left = 0;
                            r.bottom = actionMenuContainer.getHeight();
                            r.right = actionMenuContainer.getWidth();
                            actionMenuContainer.setTouchDelegate(new TouchDelegate(r, newActionView));
                        }
                    });
                }
            }
        }
    }

    /**
     * Obtém o {@link LayoutInflater} utilizado por este adapter.
     *
     * @return o inflater obtido.
     */
    public LayoutInflater getInflater() {
        return inflater;
    }


    /**
     * Obtém o registro presente na posição indicada.
     *
     * @param position posição do registro.
     * @return o registro obtido.
     * @throws IndexOutOfBoundsException se a posição for inválida.
     */
    public IEntityRecord getRecord(int position) {
        return records.get(position);
    }

	/**
	 * Obtém os registros que este adapter está utilizando.
	 * 
	 * @return os registros obtidos.
	 */
	public List<IEntityRecord> getRecords() {
		return records;
	}
	
	/**
	 * Atualiza os registros que este adapter utiliza para a criação de Views.
	 * 
	 * @param records novos registros
	 */
	public void setRecords(List<IEntityRecord> records) {
		setRecords(records, false);
	}
	
	/**
	 * Atualiza os registros que este adapter utiliza para a criação de Views.
	 * 
	 * @param records novos registros
	 * @param silent determina se a View que utiliza este adapter deve ser privada do aviso sobre a alteração.
	 * Só deve ser utilizado para evitar layouts desnecessários, no caso em que a View já vai sofrer um layout que irá atualizar os dados.
	 */
	public void setRecords(List<IEntityRecord> records, boolean silent) {
		this.records = records;
		
		if (!silent) {
			notifyDataSetChanged();
		}
	}
	
	/**
	 * Limpa os registros que este adapter utiliza para a criação de Views. 
	 */
	public void clearRecords() {
		setRecords(Collections.<IEntityRecord>emptyList());
	}
	
	/**
	 * Limpa os registros que este adapter utiliza para a criação de Views.
	 * 
	 * @param silent determina se a View que utiliza este adapter deve ser privada do aviso sobre a alteração.
	 * Só deve ser utilizado para evitar layouts desnecessários, no caso em que a View já vai sofrer um layout que irá atualizar os dados.
	 */
	public void clearRecords(boolean silent) {
		setRecords(Collections.<IEntityRecord>emptyList(), silent);
	}


	/**
	 * Define um provedor de ações para os registros. As ações são representadas por itens de um {@link YmirMenu} e serão disponibilizadas nas
     * {@link View Views} criadas por este adapter.<br>
	 * Para isto, o layout definido para o adapter deve possuir um {@link ViewGroup} com o id {@link R.id#list_layout_record_action_container}, que será
     * utilizado para armazenar a View com as ações definidas pelo provedor.<br>
	 * <br>
	 * Se houver apenas uma ação, será exibido o ícone do item como uma ação única. Desta forma, o ícone do item é obrigatório, enquanto o título
     * é opcional (apenas usado como hint).<br>
	 * Se houver mais de um item, será exibido um ícone de <code>overflow</code> que ao ser clicado abrirá um popup com os títulos dos itens do
     * menu como uma lista de ações disponíveis. Desta forma, os ícones dos itens são desnecessários enquanto os títulos são obrigatórios.<br>
	 * 
	 * @param actionProvider provedor de ações.
	 */
	public void setActionProvider(IEntityRecordListActionProvider actionProvider) {
        actionMenuAdapter = new ActionMenuAdapter(context, inflater, actionProvider);

        notifyDataSetChanged();
	}

    /**
     * Obtem o provedor de ações definido neste adapter.
     *
     * @return o provedor obtido ou <code>null</code> se ele não foi definido.
     */
    public IEntityRecordListActionProvider getActionProvider() {
        if (actionMenuAdapter == null) {
            return null;
        }
        return actionMenuAdapter.getProvider();
    }


    /**
     * Registra um listener para clicks nas Views.
     *
     * @param onEntityRecordClickListener listener que será executado quando um registro for clicado.
     */
    public void setOnItemClickListener(OnItemClickListener onEntityRecordClickListener) {
        this.itemClickListener = onEntityRecordClickListener;
    }

    /**
     * Obtém o listener registrado  para clicks nas Views.
     *
     * @return o listener obtido ou <code>null</code> se não havia listener registrado.
     */
    public OnItemClickListener getOnItemClickListener() {
        return itemClickListener;
    }


    /**
     * Provedor de ações para registros de entidades exibidos em lista.<br>
     * As ações devem ser definidas como itens em um menu através do método {@link #onCreateRecordActionMenu(YmirMenu, YmirMenuInflater)}.
     * Quando determinada ação for selecionada, o método {@link #onRecordActionItemSelected(IEntityRecord, YmirMenuItem)} será chamado.
     * Além disso, é possível filtras as ações que estão disponíveis para cada registro atraves do método {@link #isRecordActionItemAvailable(IEntityRecord, YmirMenuItem)}.
     */
    public interface IEntityRecordListActionProvider {

        /**
         * Chamado na criação do menu de ações do registro em lista.<br>
         * Deve ser utilizado para popular as ações desejadas.
         *
         * @param menu menu de ações do registro.
         * @param menuInflater inflater que pode ser utilizado para popular as ações no menu.
         */
        void onCreateRecordActionMenu(YmirMenu menu, YmirMenuInflater menuInflater);

        /**
         * Chamado quando um item de ação de um registro foi selecionado.<br>
         * Deve ser utilizado para executar a ação do item.
         *
         * @param record registro cujo o item de ação foi selecionado.
         * @param item item de ação que foi selecionado.
         */
        void onRecordActionItemSelected(IEntityRecord record, YmirMenuItem item);

        /**
         * Chamado durante a filtragem dos itens de ações disponíveis para um registro.<br>
         * Pode ser utilizado para determinar se uma ação está ou não disponível para determinado registro.
         *
         * @param record registro cujo as ações estão sendo filtradas.
         * @param item item de ação que está sendo verificado.
         * @return <code>true</code> se a ação está disponível e <code>false</code> caso contrário.
         */
        boolean isRecordActionItemAvailable(IEntityRecord record, YmirMenuItem item);
    }
	
	
	/*
	 * Classes auxiliares
	 */

    /**
     * Listener de clicks ocorridos nos items (Views) gerados pelo adapter.
     * Foi necessário implementar este mecanismo de listener porque o {@link RecyclerView} não possui algo do gênero.
     */
    public interface OnItemClickListener {

        /**
         * Chamado quando uma View que representa um item do adapter foi clicada.
         *
         * @param adapter adapter originador da View clicada.
         * @param view View clicada.
         * @param position posição do item clicado.
         */
        void onItemClick(ListLayoutConfigAdapter adapter, View view, int position);
    }

    /**
     * Holder que armazena referências para as Views de um item gerado pelo adapter.<br>
     * As referencias das Views do layout configurável são mantidas através do <code>layoutHolder</code>.
     */
    public final class ListLayoutConfigViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        private final Object layoutHolder;
        private final ViewGroup actionMenuContainer;

        public ListLayoutConfigViewHolder(View itemView, Object layoutHolder) {
            super(itemView);
            this.layoutHolder = layoutHolder;
            actionMenuContainer = (ViewGroup) itemView.findViewById(R.id.list_layout_record_action_container);
            itemView.setOnClickListener(this);
        }

        public Object getLayoutHolder() {
            return layoutHolder;
        }

        public ViewGroup getActionMenuContainer() {
            return actionMenuContainer;
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(ListLayoutConfigAdapter.this, itemView, getAdapterPosition());
            }
        }
    }
	
	/**
	 * Adaptador de itens de um {@link YmirMenu} para {@link ImageView}, representando ações para os registros da lista.
     * Se o menu contém apenas um item, exibe-o diretamente como a ação do registro.<br>
     * Se o menu contém mais de um item, será exibido um ícone de <code>overflow</code> que ao ser clicada abre um {@link PopupMenu} com os itens do menu.
	 */
	private static class ActionMenuAdapter implements OnClickListener, OnLongClickListener {
		
        private final Context context;
		private final LayoutInflater layoutInflater;
        private final IEntityRecordListActionProvider provider;
        private final YmirMenu menu;
        private final YmirMenuInflater menuInflater;

        private YmirMenuItem overflowMenuItem;

        ActionMenuAdapter(Context context, LayoutInflater layoutInflater, IEntityRecordListActionProvider provider) {
			this.context = context;
			this.layoutInflater = layoutInflater;
            this.provider = provider;

            this.menu = new YmirMenu();
            this.menuInflater = new YmirMenuInflater(context);

            //Popula as ações no menu.
            provider.onCreateRecordActionMenu(menu, menuInflater);
		}

		public View getView(IEntityRecord record, View convertView, ViewGroup parent) {
            List<YmirMenuItem> items = getAvailableMenuItems(record);
            if (items == null) {
                return null;
            }

            RecordMenuData menuData = createMenuViewData(record, items);
            YmirMenuItem actionItem = menuData.getActionItem();

            ImageView imageView = (ImageView) convertView;
            if (imageView == null) {
                imageView = (ImageView) layoutInflater.inflate(R.layout.list_layout_record_action_icon, parent, false);
                imageView.setImageResource(actionItem.getIconResourceId());
                imageView.setOnClickListener(this);
                if (!TextUtils.isEmpty(actionItem.getTitle())) {
                    imageView.setOnLongClickListener(this);
                }
            } else {
                //Se o imageView estiver referenciando outro item, é preciso alterar a imagem.
                RecordMenuData convertedMenuData = (RecordMenuData) convertView.getTag();
                if (actionItem != convertedMenuData.getActionItem()) {
                    //Altera para a imagem correta.
                    imageView.setImageResource(actionItem.getIconResourceId());
                }
            }

            imageView.setTag(menuData);
            return imageView;
		}

        @Override
		public void onClick(View v) {
            RecordMenuData menuData = (RecordMenuData) v.getTag();
            YmirMenuItem actionItem = menuData.getActionItem();
            IEntityRecord record = menuData.getRecord();

            if (actionItem == overflowMenuItem) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.setOnMenuItemClickListener(new PopupMenuItemClickListner(menuData, provider));
                Menu popupMenu = popup.getMenu();

                List<YmirMenuItem> items = menuData.getItems();
                for (int i = 0; i < items.size(); i++) {
                    YmirMenuItem menuItem = items.get(i);
                    popupMenu.add(Menu.NONE, i, Menu.NONE, menuItem.getTitle());
                }

                popup.show();
                return;
            }

            provider.onRecordActionItemSelected(record, actionItem);
		}

        @Override
        @SuppressLint("RtlHardcoded")
		public boolean onLongClick(View v) {
			//Mostra um tooltip com o título do item do menu.
            RecordMenuData menuData = (RecordMenuData) v.getTag();
            String tooltip = menuData.getActionItem().getTitle();
			Toast toast = Toast.makeText(context, tooltip, Toast.LENGTH_SHORT);

			int[] pos = new int[2];
			v.getLocationInWindow(pos);
			toast.setGravity(Gravity.TOP | Gravity.RIGHT, v.getWidth(), pos[1]);

			toast.show();
			return true;
		}

        public boolean isEmpty() {
            return menu.size() == 0;
        }

        public IEntityRecordListActionProvider getProvider() {
            return provider;
        }

        /*
         * Métodos auxiliares
         */

        private RecordMenuData createMenuViewData(IEntityRecord record, List<YmirMenuItem> items) {
            YmirMenuItem actionItem;
            if (items.size() == 1) {
                //Se tiver só um, usa o proprio item como a ação.
                actionItem = items.get(0);
                items = null;
            } else {
                //Se tiver mais do que um item, coloca uma ação de overflow que abrirá um menu em popup com os itens.
                if (overflowMenuItem == null) {
                    //Só cria se necessário.
                    overflowMenuItem = new YmirMenuInflater(context).inflate(R.xml.list_layout_config_adapter_record_action_overflow).getItem(0);
                }
                actionItem = overflowMenuItem;
            }

            return new RecordMenuData(record, actionItem, items);
        }

        private List<YmirMenuItem> getAvailableMenuItems(IEntityRecord record) {
            List<YmirMenuItem> items = null;
            for (int i = 0; i < menu.size(); i++) {
                YmirMenuItem item = menu.getItem(i);
                if (!provider.isRecordActionItemAvailable(record, item)) {
                    continue;
                }

                if (items == null) {
                    items = new ArrayList<>();
                }
                items.add(item);
            }
            return items;
        }


        /**
         * Armazena os dados pertinentes ao menu de um registro.
         */
        private static final class RecordMenuData {
            private final IEntityRecord record;
            private final YmirMenuItem actionItem;
            private final List<YmirMenuItem> items;

            RecordMenuData(IEntityRecord record, YmirMenuItem actionItem, List<YmirMenuItem> items) {
                this.record = record;
                this.actionItem = actionItem;
                this.items = items;
            }

            public IEntityRecord getRecord() {
                return record;
            }

            public List<YmirMenuItem> getItems() {
                return items;
            }

            public YmirMenuItem getActionItem() {
                return actionItem;
            }
        }

        /**
         * Listener de click nos itens do {@link PopupMenu}, utilizado para invocar o {@link IEntityRecordListActionProvider#onRecordActionItemSelected(IEntityRecord, YmirMenuItem)}
         * passando o {@link YmirMenuItem} correto.
         */
        private static final class PopupMenuItemClickListner implements OnMenuItemClickListener {

            private final RecordMenuData menuData;
            private final IEntityRecordListActionProvider provider;

            PopupMenuItemClickListner(RecordMenuData menuData, IEntityRecordListActionProvider provider) {
                this.menuData = menuData;
                this.provider = provider;
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int index = item.getItemId();
                YmirMenuItem ymirMenuItem = menuData.getItems().get(index);
                provider.onRecordActionItemSelected(menuData.getRecord(), ymirMenuItem);
                return true;
            }
        }
	}
}
