package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;

/**
 * Base para fragmentos de detalhes.<br>
 * Define as responsabilidades e provê os comportamentos comuns de um fragmento de detalhes de registro de entidade.<br>
 * Por padrão, os dados do registro detalhado são atualizados a cada {@link #onStart()}, mas este comportamento
 * pode ser configurado através do argumento {@link #AUTO_REFRESH_ARGUMENT}.<br>
 * <br>
 * Permite a definição de listener de alteração de conteúdo através de {@link #setOnContentChangeListener(OnContentChangeListener)}
 * e listener da alteração/atualiação do registro através de {@link #setOnRecordChangeListener(OnRecordChangeListener)}.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractEntityDetailFragment extends AbstractThemedFragment {
	
	/**
	 * Argumento do tipo <code>boolean</code> que define se o registro será atualizado automaticamente na reinicialização
	 * do fragmento (sair e voltar da aplicação, por exemplo).<br>
	 * Se o registro estiver sujo no momento do detalhamento, recomenda-se colocar este argumento como <code>false</code>
	 * para evitar perda de dados.<br>
	 * O valor padrão é <code>true</code>.
	 */
	public static final String AUTO_REFRESH_ARGUMENT = "AUTO_REFRESH_ARGUMENT";
	
	private static final String SAVED_RECORD = "SAVED_RECORD"; 
	
	protected IEntityRecord entityRecord;
	protected IEntityDAO entityDAO;
	private OnContentChangeListener contentChangeListener;
	private OnRecordChangeListener recordChangeListener;
	
	private boolean started;
	private boolean refreshContent = true;
	private boolean refreshRecord = true;
	private boolean keepRecord = true;

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		if (savedInstanceState != null) {
			Parcelable recordSavedState = savedInstanceState.getParcelable(SAVED_RECORD);
			if (recordSavedState != null) {
				checkInitialized();
				entityRecord = entityDAO.fromSavedState(recordSavedState); 
			}
		}
	}

	@Override
	public void onStart() {
		checkInitialized();
		super.onStart();
		started = true;
		
		//Atualiza os dados do registro, se necessário. 
		if (keepRecord && consumeRefreshRecord()) {
            //Se o registro está excluído, nem precisa atualizar.
			if (entityRecord.isDeleted() || !entityDAO.refresh(entityRecord)) {
				//Se não possui mais o registro, loga um aviso e retira suas informações do fragmento.
				Log.w(getClass().getSimpleName(), String.format("The entity record no longer exists. Id = %s, entity = %s.", entityRecord.getId(), entityDAO.getEntityMetadata().getName()));
				entityRecord = null;
			}
			if (recordChangeListener != null) {
				recordChangeListener.onRecordChanged(entityRecord);
			}
			
			//Requsita a atualização logo abaixo.
			refreshContentOnStart();
		}
		
		//Atualiza o conteúdo do fragmento, se necessário.
		if (consumeRefreshContent()) { 
			refreshContent();
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		started = false;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		if (keepRecord && entityRecord != null) {
			Parcelable recordSavedState = entityDAO.toSavedState(entityRecord);
			outState.putParcelable(SAVED_RECORD, recordSavedState);
		}
	}
	
	
	/**
	 * Define o listener para a alteração do conteúdo do fragmento de detalhe.
	 * 
	 * @param contentChangeListener listener que será executado antes e depois da alteração do conteúdo.
	 */
	public void setOnContentChangeListener(OnContentChangeListener contentChangeListener) {
		this.contentChangeListener = contentChangeListener;
	}
	
	/**
	 * Obtém o listener de alteração de conteúdo definido.
	 * 
	 * @return o listener obtido ou <code>null</code> caso não tenha listener definido.
	 */
	public final OnContentChangeListener getOnContentChangeListener() {
		return contentChangeListener;
	}
	
	/**
	 * Define o listener para a alteração do registro do fragmento de detalhe.
	 * 
	 * @param recordChangeListener listener que será executado após a alteração do registro.
	 */
	public final void setOnRecordChangeListener(OnRecordChangeListener recordChangeListener) {
		this.recordChangeListener = recordChangeListener;
	}
	
	/**
	 * Obtém o listener de alteração de registro do fragmento de detalhe.
	 * 
	 * @return o listener obtido ou <code>null</code> caso não tenha um listener definido.
	 */
	public final OnRecordChangeListener getOnRecordChangeListener() {
		return recordChangeListener;
	}
	
	/**
	 * Define o registro que será mostrado por este fragmento.<br>
	 * Se o fragmento estiver startado (entre o <code>onStart</code> e o <code>onStop</code>), a atualização dos dados será feita imediatamente.
	 * Se não, será feita apenas durante o {@link #onStart()}.
	 * 
	 * @param entityRecord o registo.
	 */
	public final void setEntityRecord(IEntityRecord entityRecord) {
		if (this.entityRecord == null && entityRecord == null) {
			return;
		}
		this.entityRecord = entityRecord;
		
		if (recordChangeListener != null) {
			recordChangeListener.onRecordChanged(entityRecord);
		}
		
		if (started) {
			refreshContent();
		} else {
			//Indica que o refresh não será necessário no onStart pq o registro acabou de ser setado.
			dontRefreshRecordOnStart();
			
			//Indica que o conteúdo precisa ser atualizado pq um novo registro foi setado. 
			refreshContentOnStart();
		}
	}
	
	/**
	 * Obtém o registro que está sendo mostrado por este fragmento.
	 * 
	 * @return o registro ou <code>null</code> se nenhum registro estiver sendo mostrado no momento.
	 */
	public final IEntityRecord getEntityRecord() {
		return entityRecord;
	}
	
	
	/**
	 * Inicializa com os objetos necessários para as funcionalidades comuns de fragmentos de detalhes.<br>
	 * As classes filhas precisam chamar este método durante sua própria inicialização, se não um {@link IllegalStateException}
	 * será lançado quando o fragmento for utilizado.
	 * 
	 * @param entityDAO acessor dos dados da entidade.
	 * @throws IllegalStateException se a inicialização for feita novamente.
	 */
	protected final void initialize(IEntityDAO entityDAO) {
		if (isInitialized()) {
			throw new IllegalStateException(getClass().getSimpleName() + "  is already initialized.");
		}
		
		this.entityDAO = entityDAO;
	}
	
	/**
	 * Lança o evento de antes da alteração de conteúdo, o qual avisará o listener, se houver.
	 */
	protected final void fireOnBeforeChangeContent() {
		if (contentChangeListener != null) {
			contentChangeListener.beforeContentChange();
		}
	}
	
	/**
	 * Lança o evento de depois da alteração de conteúdo, o qual avisará o listener, se houver.
	 */
	protected final void fireOnAfterChangeContent() {
		if (contentChangeListener != null) {
			contentChangeListener.afterContentChange();
		}
	}
	
	/**
	 * Indica se o fragmento está com a atualização automática ligada.
	 * 
	 * @return <code>true</code> se a atualização automática estiver ligada e <code>false</code> caso contrário.
	 */
	protected final boolean isAutoRefresh() {
		Bundle arguments = getArguments();
		if (arguments == null) {
			//O padrão do autoRefresh é true.
			return true;
		}
		
		return arguments.getBoolean(AUTO_REFRESH_ARGUMENT, true);
	}
	
	
	/**
	 * Chamado quando o conteúdo do fragmento de detalhe precisa ser atualizado. Isto pode ocorrer devido a atualização do registro
	 * ou apenas para a colocação dos dados iniciais.
	 */
	protected abstract void refreshContent();
	
	/**
	 * Obtém a View utilizada como cabeçalho do fragmento de detalhe.
	 * 
	 * @return a View obtida ou <code>null</code> caso não haja View para o cabeçalho.
	 */
	public abstract View getHeader();
	
	/**
	 * Obtém o {@link ListView} responsável pela listagem de campos da entidade.<br>
	 * Será utilizado apenas quando o fragmento de detalhe possuir campos do registro da entidade para exibir. 
	 * 
	 * @return a View obtida ou <code>null</code> caso este ListView não esteja sendo utilizada.
	 */
	public abstract ListView getFieldsList();
	
	/**
	 * Obtém o {@link EntityListFragment} responsável pela listagem de registros provenientes de um relacionamento da entidade.
	 * Será utilizada apenas quando o fragmento de detalhe possuir um único campo do tipo array de relacionamentos
	 * da entidade para exibir.
	 * 
	 * @return o fragmento obtido ou <code>null</code> caso o fragmento não esteja sendo utilizado ou ainda não tenha sido criado.
	 */
	public abstract EntityListFragment getEntityList();
	
	
	/*
	 * Métodos internos para uso de outros fragmentos.
	 */
	
	final boolean isStarted() {
		return started;
	}
	
	final void setKeepRecord(boolean keepRecord) {
		this.keepRecord = keepRecord;
	}
	
	final boolean isKeepRecord() {
		return keepRecord;
	}
	
	
	/*
	 * Métodos auxiliares
	 */
	
	private boolean consumeRefreshRecord() {
		boolean ret = isAutoRefresh() && refreshRecord && entityRecord != null && !entityRecord.isNew();
		//Reabilita o refreshRecord para tentar atualizar novamente no próximo onStart().
		refreshRecord = true;
		return ret;
	}
	
	private boolean consumeRefreshContent() {
		boolean ret = refreshContent;
		//Desabilita o refreshContent para só atualizar o conteúdo no próximo onStart() se for requisitado.
		refreshContent = false;
		return ret;
	}
	
	//Requsita a atualização do conteúdo no próximo onStart().
	private void refreshContentOnStart() {
		refreshContent = true;
	}
	
	//Desabilita a atualização do registro no próximo onStart().
	private void dontRefreshRecordOnStart() {
		refreshRecord = false;
	}
	
	private boolean isInitialized() {
		return entityDAO != null;
	}
	
	protected final void checkInitialized() {
		if (!isInitialized()) {
			throw new IllegalStateException(getClass().getSimpleName() + " was not initialized.");
		}
	}
	
	
	/*
	 * Interfaces auxiliares
	 */
	
	/**
	 * Listener de alteração do conteúdo do fragmento de detalhe.
	 */
	public interface OnContentChangeListener {
		
		/**
		 * Chamado antes da alteração do conteúdo do fragmento de detalhe.
		 */
		void beforeContentChange();
		
		/**
		 * Chamado depois da alteração do conteúdo do fragmento de detalhe.
		 */
		void afterContentChange();
	}
	
	/**
	 * Listener de alteração/atualização do registro sendo mostrado no fragmento de detalhe.
	 */
	public interface OnRecordChangeListener {
		
		/**
		 * Chamado após a alteração do registro do fragmento de detalhe.
		 * 
		 * @param record o novo registro ou <code>null</code> se não há mais um registro para detalhar.
		 */
		void onRecordChanged(IEntityRecord record);
	}
}
