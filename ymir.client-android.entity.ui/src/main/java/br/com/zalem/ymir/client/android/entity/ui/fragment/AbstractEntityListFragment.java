package br.com.zalem.ymir.client.android.entity.ui.fragment;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.layout.ListLayoutConfigAdapter.IEntityRecordListActionProvider;
import br.com.zalem.ymir.client.android.menu.YmirMenu;

/**
 * Define as responsabilidades que um fragmento de lista de registros de entidade deve suportar.<br>
 * Por padrão, os registros listados são atualizados a cada {@link #onStart()}, mas este comportamento
 * pode ser configurado através do argumento {@link #AUTO_REFRESH_ARGUMENT}.<br>
 * 
 * @author Thiago Gesser
 */
public abstract class AbstractEntityListFragment extends AbstractThemedFragment {

	/**
	 * Argumento do tipo <code>boolean</code> que define se a lista de registros será atualizada automaticamente
	 * na reinicialização do fragmento (sair e voltar da aplicação, por exemplo).<br>
	 * O valor padrão é <code>true</code>.
	 */
	public static final String AUTO_REFRESH_ARGUMENT = "AUTO_REFRESH_ARGUMENT";

	/**
	 * Argumento do tipo <code>boolean</code> que define se a lista de registros suportará a atualização através de um
	 * movimento de swipe para baixo quando ela está no topo.<br>
	 * O valor padrão é <code>true</code>.
	 */
	public static final String SWIPE_REFRESH_ARGUMENT = "SWIPE_REFRESH_ARGUMENT";


    protected IEntityDAO entityDAO;

    /**
     * Obtém o {@link IEntityDAO} utilizado por este fragmento.
     *
     * @return o IEntityDAO obtido.
     */
    public IEntityDAO getEntityDAO() {
        return entityDAO;
    }

    /**
     * Executa o método {@link #doSimpleSearch(String, Runnable)} passando <code>nulo</code> para o callback
     * de finalização.
     *
     * @param query termo de filtro.
     */
    public final void doSimpleSearch(String query) {
        doSimpleSearch(query, null);
    }

    /**
     * Executa o método {@link #undoSearch(Runnable)} passando <code>nulo</code> para o callback de finalização.
     */
    public final void undoSearch() {
        undoSearch(null);
    }

    /**
     * Executa o metodo {@link #refresh(boolean, Runnable)} passando <code>false</code> e <code>null</code>.
     */
    public final void refresh() {
        refresh(false, null);
    }


    /**
     * Executa a busca simples, que consiste em filtrar os registros da lista a partir dos campos utilizados no layout dos items da lista.<br>
     * Passarão pelo filtro todos os registros que contiverem o termo de filtro em pelo menos
     * um dos campos utilizados no layout dos items da lista.<br>
     * O processo de filtragem é feito em uma outra Thread, então este método retornará antes da finalização da filtragem.
     * Se houver a necessidade de executar alguma ação na finalização, pode ser passado um callback.
     * O callback será chamado logo após a finalização da filtragem, na Thread de UI.
     *
     * @param query termo de filtro.
     * @param completionCallback callback opcional que será chamado depois da finalização da filtragem.
     */
    public abstract void doSimpleSearch(String query, Runnable completionCallback);

    /**
     * Desfaz qualquer busca realizada anteriormente e volta a mostrar todos os registros.
     * O processo de carregamento dos dados é feito em uma outra Thread, então este método retornará antes da finalização do carregamento.
     * Se houver a necessidade de executar alguma ação na finalização, pode ser passado um callback.
     * O callback será chamado logo após a finalização do carregamento, na Thread de UI.
     *
     * @param completionCallback callback opcional que será chamado depois da finalização da filtragem.
     */
    public abstract void undoSearch(Runnable completionCallback);

    /**
     * Atualiza os registros da lista mantendo a busca atual, se houver.
     *
     * @param full <code>true</code> para indicar uma atualização por completo (possivelmente demorada, então esconde os registros) ou
     * <code>false</code> para indicar uma atualização rápida (continua mostrando os registros).
     * @param completionCallback callback opcional que será chamado depois da finalização da filtragem.
     */
    public abstract void refresh(boolean full, Runnable completionCallback);

	/**
	 * Registra um listener para clicks em registros da lista do fragmento.
	 *
	 * @param clickListener listener que será executado quando um registro for clicado.
	 */
	public abstract void setOnEntityRecordClickListener(OnEntityRecordClickListener clickListener);
	
	/**
	 * Registra um listener para as ações de pesquisa do fragmento.
	 * 
	 * @param searchListener listener que será executado nas ações de pesquisa do fragmento.
	 */
	public abstract void setOnSearchListener(OnSearchListener searchListener);
	
	/**
	 * Registra um listener para a alteração dos registros sendo exibidos no fragmento.
	 * 
	 * @param recordsListener listener que será executado depois da alteração dos registros do fragmento.
	 */
	public abstract void setOnRecordsChangeListener(OnRecordsChangeListener recordsListener);
	
	/**
     * Define um provedor de ações para os registros. As ações são representadas por itens de um {@link YmirMenu} e disponibilizadas para cada
     * registro exibido pela lista do fragmento.<br>
	 * Se houver apenas um item de ação, será exibido o ícone do item como uma ação única. Desta forma, o ícone do item é obrigatório, enquanto
     * o título é opcional (apenas usado como hint).<br>
	 * Se houver mais de um item, será exibido um ícone de <code>overflow</code> que ao ser clicado exibirá uma lista com os títulos dos itens
     * do menu como um menu de ações disponíveis. Desta forma, os ícones dos itens são desnecessários enquanto os títulos são obrigatórios.<br>
	 *
     * @param actionProvider provedor de ações.
	 */
	public abstract void setActionProvider(IEntityRecordListActionProvider actionProvider);

    /**
     * Obtem o provedor de ações de registros definido neste fragmento.
     *
     * @return o provedor obtido ou <code>null</code> se ele não foi definido.
     */
    public abstract IEntityRecordListActionProvider getActionProvider();

	/**
	 * Obtém o termo de filtro da pesquisa simples atual.
	 * 
	 * @return o termo de filtro obtido ou <code>nulo</code> caso não haja uma pesquisa simple atual. 
	 */
	public abstract String getSimpleSearchQuery();
	
	/**
	 * Avisa que um registro foi criado e fará parte dos registros sendo listados pelo fragmento.<br>
	 * Desta forma, o fragmento se antecipa e adiciona o registro sem atualizar toda a lista, tornando este
	 * método ideal para a exibição de registros recém-criados.<br>
	 * <br>
	 * Este método só deve ser utilizado se o registro for de fato compor a lista de registros, se não ele pode 
	 * desaparecer quando o próximo {@link #refresh()} acontecer.
	 * 
	 * @param record registro que será adicionado na lista.
	 * @param position posição em que o registro deve ser adicionado na lista. Também pode ser menor que zero para que o
	 * registro seja adicionado no início e maior que o tamanho da lista para que o registro seja adicionado no final.
	 * @throws IllegalArgumentException se o registro for novo ou deletado, pois assim ele não poderá compor a lista.   
	 */
	public abstract void onRecordCreated(IEntityRecord record, int position);

    /**
     * Avisa que um registro foi alterado. Desta forma, o fragmento se antecipa e atualiza o registro sem atualizar toda a lista, tornando
     * este método ideal para a atualização de registros recém-alterados.<br>
     * <br>
     * Este método só deve ser utilizado se o registro foi de fato alterado, pois isto pode acarretar em um processamento desnecessário.
     *
     * @param record registro que será removido da lista.
     */
    public abstract void onRecordChanged(IEntityRecord record);
	
	/**
	 * Avisa que um registro foi excluído e não fará mais parte dos registros sendo listados pelo fragmento.<br>
	 * Desta forma, o fragmento se antecipa e remove o registro sem atualizar toda a lista, tornando este
	 * método ideal para a remoção de registros recém-excluídos.<br>
	 * <br>
	 * Este método só deve ser utilizado se o registro for de fato não compor mais a lista de registros, se não ele pode 
	 * reaparecer quando o próximo {@link #refresh()} acontecer.
	 * 
	 * @param record registro que será removido da lista.
	 */
	public abstract void onRecordDeleted(IEntityRecord record);
	
	
	/*
	 * Interfaces auxilares
	 */
	
	/**
	 * Listener de clicks em registros da lista de entidades do {@link EntityListFragment}.
	 * Pode ser registrado no fragmento através do método {@link EntityListFragment#setOnEntityRecordClickListener(OnEntityRecordClickListener)}.
	 */
	public interface OnEntityRecordClickListener {
		
		/**
		 * Chamado quando um registro da lista é clicado.
		 * 
		 * @param entityRecordId registro da entidade clicado.
		 */
		void onEntityRecordClick(IEntityRecord entityRecordId);
	}
	
	/**
	 * Listener de pesquisas feitas com os registros da lista de entidades do {@link EntityListFragment}.
	 * Pode ser registrado no fragmento através do método {@link EntityListFragment#setOnSearchListener(OnSearchListener)}.
	 */
	public interface OnSearchListener {
		
		/**
		 * Chamado quando a pesquisa simples for executada.
		 * 
		 * @param query termo da pesquisa simples
		 */
		void onDoSimpleSearch(String query);
		
		/**
		 * Chamado quando a pesquisa atual for desfeita.
		 */
		void onUndoSearch();
	}
	
	/**
	 * Listener de alteração dos registros do fragmento de lista.
	 */
	public interface OnRecordsChangeListener {
		
		/**
		 * Chamado antes de uma alteração nos registros do fragmento.<br>
		 * É possível retornar uma lista de registros diferente para ser utilizada na listagem ao invés da lista passada de parâmetro. 
		 *
         * @param fragment fragmento que está alterando os registros.
		 * @param records lista com os novos registros.
		 * @return uma lista de registros diferente ou <code>null</code> se a mesma lista de registros deve ser utilizada.
		 */
		List<IEntityRecord> beforeRecordsChange(EntityListFragment fragment, List<IEntityRecord> records);
		
		/**
		 * Chamado depois de uma alteração nos registros do fragmento.
		 *
         * @param fragment fragmento que alterou os registros.
		 * @param records lista com os novos registros.
		 */
		void afterRecordsChange(EntityListFragment fragment, List<IEntityRecord> records);
	}
}
