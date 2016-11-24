package br.com.zalem.ymir.client.android.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.util.List;

import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import roboguice.fragment.RoboFragment;

/**
 * Perspective é um tipo de fragmento especial que, conceitualmente, possui autonomia para atuar como uma Activity.
 * A perspectiva ocupará toda a área disponibilizada para ela, então seu layout deve ser pensado de forma à disponibilizar
 * uma visão completa de determinada funcionalidade da aplicação. Assim como uma Activity, uma perspectiva pode:
 * <ul>
 * 	<li>manipular fragmentos para compor suas funcionalidades, através do {@link #getChildFragmentManager()};</li>
 * 	<li>definir seu título na ActionBar ou alterá-lo para outra coisa;</li>
 * 	<li>mostrar o indicador de Up e tratar o click nele;</li>
 * 	<li>tratar o botão Back;</li>
 * 	<li>obter a configuração de um Searchable.</li>
 * </ul>
 * Fragmentos normais também conseguem executar muitas das ações listadas acima, mas isto foge do seu conceito,
 * que é o de ser apenas parte (com responsabilidades bem definidas) de uma tela.<br>
 * <br>
 * O ciclo de vida de uma perspectiva é controlado por um {@link IPerspectiveManager}. Ele dispõe a área onde a perspectiva 
 * será exibida e garente que eventos como o do botão Back chegarão à ela. A perspectiva possui uma referência para o IPerspectiveManager que a criou, podendo assim acessar algumas das ações
 * exclusivas de perspectivas. Estas ações são disponibilizadas através de métodos desta classe. A inicialização de uma 
 * perspectiva é feita pelo IPerspectiveManager, através do método {@link #initialize(IPerspectiveManager, PerspectiveInfo, android.content.Intent, int, boolean)}.<br>
 * <br>
 * Toda perspectiva é iniciada a partir de um {@link android.content.Intent}, através do método {@link IPerspectiveManager#startPerspective(android.content.Intent)}.
 * O Intent pode definir desde a classe da perspectiva que se deseja abrir ou apenas uma ação e categorias necessárias,
 * cabendo ao IPerspectiveManager achar uma perspectiva que atenda às definições do Intent. Cada perspectiva mantém 
 * uma referência para o Intent que a iniciou, disponível através do método {@link #getIntent()}. Isto pode ser utilizada para 
 * obter a ação e categorias que a Intent definiu, bem como os dados extras, podendo modelar a perspectiva de acordo com
 * os desejos do Intent.<br>
 * <br>
 * Quando uma perspectiva é destruída, o IPerspectiveManager guarda o estado atual dela para que seja recuperado quando
 * ela for criada novamente, podendo assim iniciar de onde parou. Para isto, é necessário sobrescrever o método {@link #onSaveInstanceState(android.os.Bundle)}
 * e salvar os valores do estado atual. Quando a perspectiva for recriada, estes valores estarão no {@link android.os.Bundle}
 * que é passado em métodos como o {@link #onActivityCreated(android.os.Bundle)}.
 *
 * @see IPerspectiveManager
 * @see android.content.Intent
 * 
 * @author Thiago Gesser
 */
public class Perspective extends RoboFragment {
	
	/**
	 * Código padrão para resultado cancelado.
	 */
	public static final int RESULT_CODE_CANCELED = -1;
	/**
	 * Código padrão para resultado ok.
	 */
	public static final int RESULT_CODE_OK = -2;

	private IPerspectiveManager perspectiveManager;
    //Informações gerais da perspectiva.
	private int id;
	private Intent intent;
	private PerspectiveInfo info;

    //Informaçoes sobre a abertura para um resultado.
	private boolean forResult;
	private int resultCode = RESULT_CODE_CANCELED;
	private Object resultData;


    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (perspectiveManager == null) {
			throw new IllegalStateException("The perspective was not initialized.");
		}
	}
	
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);

		getChildFragmentManager().executePendingTransactions();
		perspectiveManager.notifyReady(this);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		
		perspectiveManager.notifyVisibilityChanged(this);
	}


	/**
	 * Chamado quando o usuário pressiona o botão Back. Por padrão, nenhuma ação é feita, mas pode ser sobrescrito. Se 
	 * alguma ação for executada, deve-se retornar <code>true</code>, porque assim nenhuma outra ação será feita.
	 * 
	 * @return <code>true</code> se executou alguma ação ou <code>false</code> caso contrário.
	 */
	public boolean onBackPressed() {
		return false;
	}
	
	/**
	 * Chamado quando o usuário pressiona o botão Up. Por padrão, nenhuma ação é feita, mas pode ser sobrescrito.<br>
	 * Só será executado se o botão Up estiver habilitado, ou esja, se o retorno do método {@link #isUpEnabled()} for
	 * <code>true</code>.
	 */
	public void onUpPressed() {
	}

	/**
	 * Obtém a indicação de se esta perspectiva necessita que o Up esteja habilitado.<br>
	 * Por padrão retorna <code>false</code>, mas pode ser sobrescrito caso seja necessário habilitar o Up em determinada
	 * situação. Para notificar alteração no valor retornado por este método, deve se chamar
	 * {@link #notifyAppBarChanged()}.
	 * 
	 * @return <code>true</code> se o indicador de Up deve ser habilitado ou <code>false</code> caso contrário.
	 */
	public boolean isUpEnabled() {
		return false;
	}
	
	/**
	 * Obtém a indicação de se esta perspectiva possui trabalhos inacabados, que serão perdidos na finalização da perspectiva.
	 * Esta indicação é utilizada pelo gerenciador de perspectivas antes de encerrar uma perspectiva, pedindo
	 * a confirmação do usuário caso ela possua trabalhos inacabados.
	 * 
	 * @return <code>true</code> se a perspectiva possui trabalhos inacabados ou <code>false</code> caso contrário.
	 */
	public boolean hasUnfinishedWork() {
		return false;
	}


	/**
	 * Chamado quando a visibilidade da App Bar da perspectiva foi alterada, significando que seu conteúdo não estará mais disponível para
	 * o usuário.
	 *
	 * @param visible <code>true</code> se a App Bar da perspectiva ficou visivel e <code>false</code> caso contrário.
	 */
	public void onAppBarVisibilityChanged(boolean visible) {
	}

	/**
	 * Indica se a App Bar da perspectiva está visível.
	 *
	 * @return <code>true</code> se o conteúdo da App Bar da perspectiva esta disponível e <code>false</code> caso contrário.
	 */
	public final boolean isAppBarVisible() {
		return isMenuVisible();
	}


	/**
	 * Chamado quando uma perspectiva aberta para um resultado foi finalizada.<br>
	 * Pode ser sobrescrito para tratar o resultado.
	 * 
	 * @param requestKey a chave de requisição passada no momento da abertura da perspectiva.
	 * @param resultCode o código de resultado retornado pela perspectiva.
	 * @param data o dado retornado pela perspectiva.
	 */
	public void onPerspectiveResult(Serializable requestKey, int resultCode, Object data) {
	}
	
	/**
	 * Define o resultado desta perspectiva, que será propagado à perspectiva que abriu esta.<br>
	 * Só pode ser utilizado se a perspectiva foi aberta para um resultado, o que pode ser verificado através do método
	 * {@link #isForResult()}.
	 * 
	 * @param resultCode código de resultado, que pode ser um dos valores padrão {@link #RESULT_CODE_CANCELED}, {@link #RESULT_CODE_OK} ou um valor próprio.
	 * @param resultData dado de resultado.
	 */
	public final void setResult(int resultCode, Object resultData) {
		this.resultCode = resultCode;
		this.resultData = resultData;
	}
	
	/**
	 * Obtém o código do resultado desta perspectiva.
	 * 
	 * @return o código obtido.
	 */
	public final int getResultCode() {
		return resultCode;
	}
	
	/**
	 * Obtém o dado do resultado desta perspectiva.
	 * 
	 * @return o dado obtido.
	 */
	public final Object getResultData() {
		return resultData;
	}
	
	/**
	 * Sinaliza se a perspectiva foi aberta para um resultado.
	 * 
	 * @return <code>true</code> se a perspectiva foi aberta para um resultado e <code>false</code> caso contrário.
	 */
	public final boolean isForResult() {
		return forResult;
	}


    /**
     * Indica se a perspectiva utiliza Floating Action Buttons.<br>
     * Em caso afirmativo, os métodos {@link #onCreateFABs(YmirMenu, YmirMenuInflater)} e {@link #isFABAvailable(YmirMenuItem)} serão
     * chamados para configurar os FABs. Quando um FAB é clicado, o método {@link #onFABClicked(YmirMenuItem)} é chamado. Também é possivel
     * invalidar os FABs atuais, fazendo com que sua disponibilidade seja reavalidada através do metodo {@link #notifyFABsChanged()}.<br>
     * <br>
     * Por padrao as perspectivas não possuem FABs.
     *
     * @return <code>true</code> se a perspectiva possui FABs e <code>false</code> caso contrário.
     */
    public boolean hasFABs() {
        return false;
    }

    /**
     * Chamado para criar os Floating Action Buttons da perspectiva.<br>
     * Cada FAB deve ser representado através de um {@link YmirMenuItem item de menu} no <code>fabMenu</code>. Os itens podem ser facilmente
     * inflados a partir de um xml com o <code>menuInflater</code>.
     *
     * @param fabMenu o menu com os itens que representam os FABs.
     * @param menuInflater o inflater de menu.
     */
    public void onCreateFABs(YmirMenu fabMenu, YmirMenuInflater menuInflater) {
    }

    /**
     * Indica se o Floating Action Button está disponivel para o uso no estado atual.<br>
     * <br>
     * Por padrão os FABs não estão disponíveis.
     *
     * @param fabItem o item de menu que representa o FAB.
     * @return <code>true</code> se o FAB está disponivel e <code>false</code> caso contrário.
     */
    public boolean isFABAvailable(YmirMenuItem fabItem) {
        return false;
    }

    /**
     * Chamado quando um Floating Action Button é clicado.
     *
     * @param fabItem o item de menu que representa o FAB.
     */
    public void onFABClicked(YmirMenuItem fabItem) {
    }

    /**
     * Notifica que a perspectiva teve seu estado atual alterado de forma que os FABs disponíveis podem ser afetados.<br>
     * Isto desencadeia novas chamadas para o {@link #isFABAvailable(YmirMenuItem)}, permitindo que os FABs disponíveis sejam alterados.
     */
    public final void notifyFABsChanged() {
        perspectiveManager.notifyFABsChanged(this);
    }


    /**
     * Obtém o título da perspectiva.<br>
     * Por padrão retorna o título original da perspectiva fornecido na sua declaração, mas pode ser sobrescrito caso seja necessário alterar o
     * título em determinada situação. Para notificar alteração no valor retornado por este método, deve se chamar {@link #notifyAppBarChanged()}.
     *
     * @return o título obtido.
     */
    public String getTitle() {
        return info.getTitle();
    }

    /**
     * Obtém as informações fornecidas na declaraçao da perspectiva.
     *
     * @return as informações obtidas.
     */
    public final PerspectiveInfo getInfo() {
        return info;
    }

    /**
     * Obtém o Intent utilizado para abrir esta perspectiva.
     *
     * @return o Intent obtido.
     */
    public final Intent getIntent() {
        return intent;
    }

    /**
     * Obtém o identificador desta instância de perspectiva fornecido pelo gerenciador de perspectivas.
     *
     * @return o identificador obtido.
     */
    public final int getPerspectiveId() {
        return id;
    }

	
	/*
	 * Métodos que intermedeiam o IPerspectiveManager para as classes filhas.
	 */
	
	/**
	 * Notifica que a perspectiva alterou elementos que impactam na App Bar, como o título e o indicador de Up.
	 */
	protected final void notifyAppBarChanged() {
		perspectiveManager.notifyAppBarChanged(this);
	}

	/**
	 * Inicia uma perspectiva que atenda aos requisitos definidos pelo Intent, que podem ser:
	 * <ul>
	 * 	<li>o nome da classe da persepctiva;</li>
	 * 	<li>uma ação que a perspectiva suporta;</li>
	 * 	<li>qualquer número de categorias que a persepctiva possui.</li>
	 * </ul>
	 * Também é possível definir flags no Intent, de acordo com as opções disponibilizadas pelo {@link IPerspectiveManager}.
	 * 
	 * @param perspectiveIntent Intent contendo os requisitos da perspectiva.
	 */
	protected final void startPerspective(Intent perspectiveIntent) {
		perspectiveManager.startPerspective(perspectiveIntent);
	}
	
	/**
	 * Inicia uma perspectiva para obter um resultado depois que ela for finalizada.
	 * Será utilizada uma perspectiva que atenda aos requisitos definidos pelo Intent, que podem ser:
	 * <ul>
	 * 	<li>o nome da classe da persepctiva;</li>
	 * 	<li>uma ação que a perspectiva suporta;</li>
	 * 	<li>qualquer número de categorias que a persepctiva possui.</li>
	 * </ul>
	 * O resultado será passado para esta perspectiva através da chamada do método {@link br.com.zalem.ymir.client.android.perspective.Perspective#onPerspectiveResult(java.io.Serializable, int, Object)}.<br>
	 * <br>
	 * Também é possível definir flags no Intent, de acordo com as opções disponibilizadas pelo {@link IPerspectiveManager}.
	 * 
	 * @param perspectiveIntent Intent contendo os requisitos da perspectiva.
	 * @param requestKey chave de requisição que será passada junto com o resultado.
	 */
	protected final void startPerspectiveForResult(Intent perspectiveIntent, Serializable requestKey) {
		perspectiveManager.startPerspectiveForResult(this, perspectiveIntent, requestKey);
	}
	
	/**
     * Sinaliza que a perspectiva terminou suas atividades e pode ser fechada.<br>
     * Se foi aberta para um resultado, ele será repassado a perspectiva que o abriu.
     */
    protected final void finish() {
        perspectiveManager.notifyFinished(this);
    }

    /**
     * Sinaliza que a perspectiva terminou suas atividades e pode ser fechada, permitindo que qualquer estado alterado depois do último
     * {@link #onSaveInstanceState(Bundle)} possa ser perdido.<br>
     * Isto só deve ser utilizado em casos especiais quando o estado atual da perspectiva pode ser descartado e substituído pelo último
     * salvo.<br>
     * <br>
     * Se foi aberta para um resultado, ele será repassado a perspectiva que o abriu.
     */
    protected final void finishAllowingStateLoss() {
        boolean oldValue = perspectiveManager.isCommitAllowingStateLoss();
        perspectiveManager.setCommitAllowingStateLoss(true);

        perspectiveManager.notifyFinished(this);

        perspectiveManager.setCommitAllowingStateLoss(oldValue);
    }


	/*
	 * Métodos utilizados pelo IPerspectiveManager. 
	 */
	
	/**
	 * Inicializa a perspectiva com os objetos necessários para o seu funcionamento.<br>
	 * A inicialização não pode ser feita mais de uma vez.
	 * 
	 * @param perspectiveManager o {@link IPerspectiveManager} que criou a perspectiva.
	 * @param info informações sobre a perspectiva, como título e argumentos.
	 * @param intent Intent responsável pela criação da perspectiva.
	 * @param id identificador único da perspectiva.
     * @param forResult indica se a perspectiva deve retornar um resultado ou não.
	 * @throws IllegalStateException se a perspectiva já tiver sido iniciada.
	 */
	public final void initialize(IPerspectiveManager perspectiveManager, PerspectiveInfo info, Intent intent, int id, boolean forResult) {
		if (perspectiveManager == null || info == null || intent == null) {
			throw new NullPointerException("perspectiveManager == null || info == null || intent == null");
		}
		if (this.perspectiveManager != null) {
			throw new IllegalStateException("Perspective was already initialized.");
		}
		
		this.perspectiveManager = perspectiveManager;
		this.intent = intent;
		this.id = id;
		this.info = info;
		this.forResult = forResult;
	}
	
	/**
	 * Define a visibilidade das ações da App Bar (Options Menu) desta perspectiva e de qualquer fragmento contido nela.<br>
	 * Como a definição de visibilidade em um fragmento que esteja visível acaba desencadeando a invalidação de todas
	 * as ações da App Bar, este método retorna se isto já foi feito. Desta forma, é possível saber se é necessário chamar alguma
	 * invalidação adicional.
	 * 
	 * @param visible <code>true</code> se as ações devem ser mostradas ou <code>false</code> se elas devem ser escondidas. 
	 * @return <code>true</code> se alguma invalidação foi desencadeada ou <code>false</code> caso contrário.
	 */
	public final boolean setOptionsMenuVisibility(boolean visible) {
		boolean menuInvalidated = setOptionsMenuVisibility(this, visible);

		List<Fragment> nestedFragments = getChildFragmentManager().getFragments();
		if (nestedFragments != null) {
			for (Fragment nestedFragment : nestedFragments) {
				if (nestedFragment == null) {
					continue;
				}
				menuInvalidated |= setOptionsMenuVisibility(nestedFragment, visible);
			}
		}

		onAppBarVisibilityChanged(visible);

		return menuInvalidated;
	}
	
	
	/*
	 * Métodos auxiliares
	 */
	
	private boolean setOptionsMenuVisibility(Fragment fragment, boolean visible) {
		fragment.setMenuVisibility(visible);
		
		//Retorna se o menu foi invalidado pelo fragment.
		return fragment.hasOptionsMenu() && fragment.isAdded() && !fragment.isHidden();
	}
}
