package br.com.zalem.ymir.client.android.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import java.io.Serializable;

/**
 * O gerenciador de perspectivas é responsável por criar, trocar e restaurar as perspectivas, cabendo a ele definir a 
 * área de ocupação delas.<br>
 * Dispõe formas das perspectivas notificarem sobre alterações internas delas. As notificações de visibilidade ({@link #notifyVisibilityChanged(Perspective)})
 * e pronta ({@link #notifyReady(Perspective)}) já são feitas automaticamente pela classe {@link Perspective}, já a 
 * notificação de alteração ({@link #notifyAppBarChanged(Perspective)}) deve ser feita pelas perspectivas concretas
 * quando necessário.<br>
 * Permite que uma perspectiva inicie outras perspectivas através do método {@link #startPerspective(android.content.Intent)}.
 * 
 * @see Perspective
 *
 * @author Thiago Gesser
 */
public interface IPerspectiveManager {
	
	/**
	 * Flag que pode ser utilizada no Intent de perspectivas para determinar que a backstack de perspectivas deve ser
	 * limpa antes de abrir a nova perspectiva. Se alguma perspectiva aberta possuir trabalhos inacabados, o usuário deve consentir com a
     * abertura da nova perspectiva. Esta verificação pode ser ignorada se a flag {@link #INTENT_FLAG_IGNORE_UNFINISHED_WORK} for utilizada.
	 */
	int INTENT_FLAG_CLEAR_BACKSTACK = 1;

	/**
	 * Flag que pode ser utilizada no Intent de perspectivas junto com a {@link #INTENT_FLAG_CLEAR_BACKSTACK} para ignorar qualquer trabalho
     * inacabado das perspectivas atuais e abrir a nova sem o consentimento do usuário.
	 */
	int INTENT_FLAG_IGNORE_UNFINISHED_WORK = 2;

    /**
     * Flag que pode ser utilizada no Intent de perspectivas para determinar que deve ser criada uma nova instância da perspectiva, independente
     * do <code>launch mode</code> definido em sua configuração.
     */
    int INTENT_FLAG_NEW_INSTANCE = 4;


	/**
     * Notifica ao gerenciador que a perspectiva alterou elementos que impactam na App Bar, como o título e o
     * indicador de Up.
     *
     * @param perspective perspectiva que foi alterada.
     */
    void notifyAppBarChanged(Perspective perspective);

    /**
     * Notifica ao gerenciador que a perspectiva alterou características dos FABs (Floating Action Buttons). Geralmente é apenas a disponibilidade.
     *
     * @param perspective perspectiva que foi alterada.
     */
    void notifyFABsChanged(Perspective perspective);
	
	/**
	 * Notifica ao gerenciador que a perspectiva teve sua visibilidade alterada.
	 * 
	 * @param perspective perspectiva que foi alterada.
	 */
	void notifyVisibilityChanged(Perspective perspective);
	
	/**
	 * Notifica ao gerenciador que a perspectiva está pronta para ser manipulada.
	 * 
	 * @param perspective perspectiva que está pronta.
	 */	
	void notifyReady(Perspective perspective);
	
	/**
	 * Notifica ao gerenciador que a perspectiva foi finalizada.
	 * 
	 * @param perspective perspectiva que foi finalizada.
	 */
	void notifyFinished(Perspective perspective);

	
	/**
	 * Inicia uma das perspectivas mantidas pelo gerenciador que atenda aos requisitos definidos pelo Intent, que podem ser:
	 * <ul>
	 * 	<li>o nome da classe da persepctiva;</li>
	 * 	<li>uma ação que a perspectiva suporta;</li>
	 * 	<li>qualquer número de categorias que a persepctiva possui.</li>
	 * </ul>
	 * Também é possível definir flags no Intent, de acordo com as opções disponibilizadas por esta interface.
	 * 
	 * @param perspectiveIntent Intent contendo os requisitos da perspectiva.
	 */
	void startPerspective(Intent perspectiveIntent);
	
	/**
	 * Inicia uma das perspectivas mantidas pelo gerenciador para obter um resultado depois que ela for finalizada.
	 * Será utilizada uma perspectiva que atenda aos requisitos definidos pelo Intent, que podem ser:
	 * <ul>
	 * 	<li>o nome da classe da persepctiva;</li>
	 * 	<li>uma ação que a perspectiva suporta;</li>
	 * 	<li>qualquer número de categorias que a persepctiva possui.</li>
	 * </ul>
	 * O resultado será passado para a perspectiva fonte através da chamada do método {@link Perspective#onPerspectiveResult(java.io.Serializable, int, Object)}.<br>
	 * <br>
	 * Também é possível definir flags no Intent, de acordo com as opções disponibilizadas por esta interface.
	 * 
	 * @param sourcePerspective perspective fonte da chamada.
	 * @param perspectiveIntent Intent contendo os requisitos da perspectiva.
	 * @param requestKey chave de requisição que será passada junto com o resultado.
	 */
	void startPerspectiveForResult(Perspective sourcePerspective, Intent perspectiveIntent, Serializable requestKey);


    /**
     * Define se os commits executados com as perspectivas devem ser feitos através do {@link FragmentTransaction#commitAllowingStateLoss()}.<br>
     * Isto só deve ser utilizado em casos especiais quando o estado atual das perspectivas pode ser descartado e substituído pelo último
     * salvo durante {@link Perspective#onSaveInstanceState(Bundle)}.
     *
     * @param commitAllowingStateLoss <code>true</code> se os commits devem ser feitos com possível perda de estados e <code>false</code> caso contrário.
     */
    void setCommitAllowingStateLoss(boolean commitAllowingStateLoss);

    /**
     * Indica se os commits executados com as perspectivas são feitos através do {@link FragmentTransaction#commitAllowingStateLoss()}.
     *
     * @return <code>true</code> se os commits são feitos com possível perda de estados e <code>false</code> caso contrário.
     */
    boolean isCommitAllowingStateLoss();
}
