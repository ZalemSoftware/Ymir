package android.support.v4.app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.List;
import java.util.Stack;

/**
 * Utilitário para aplicar contornos aos bugs do Android cujo as soluções ainda não foram liberadas em versões oficiais.<br>
 * O objetivo desta classe é evitar que vários locais do código da aplicação tenham que ser alterados para contornar um bug.
 * Desta forma, o contorno do bug se limita a apenas uma chamada de método. Assim, quando o bug for corrigido, será questão
 * de apenas remover a chamada do método, sem nenhum outro impacto para o código. 
 *
 * @author Thiago Gesser
 */
public final class AndroidBugsUtils {
	
	private AndroidBugsUtils() {
	}

	/**
	 * Contorno para o bug: https://code.google.com/p/android/issues/detail?id=37484
	 * <br>
	 * O {@link android.os.Bundle} do SavedState não fica com o ClassLoader certo depois de ser restaurado de uma activity que foi destruída,
	 * ocasionando em um BadParcelableException -> ClassNotFoundException.<br>
	 * O contorno é setar o ClassLoader correto no SavedState.
	 * 
	 * @param savedState SavedState que será ajeitado.
	 * @param context contexto cujo o ClassLoader será colocado no SavedState.
	 */
	public static void applyWorkaroundForBug37484(SavedState savedState, Context context) {
		savedState.mState.setClassLoader(context.getClassLoader());
	}

    private static Fragment[] retainedFragments;
    /**
     * Contorno para o bug: https://code.google.com/p/android/issues/detail?id=74222
     * <br>
     * Fragmentos aninhados (filhos de outros fragmentos) com {@link DialogFragment#getRetainInstance() retainInstance} == <code>true</code>
     * não retém a instancia, passando pelo processo normal de destruíção e recuperação como um fragmento normal.<br>
     * Esta parte do contorno armazena as instâncias dos fragmentos aninhados que devem ser retidos para utilizá-los mais tarde no momento em que eles
     * forem recuperados, através do método {@link #applyWorkaroundForBug74222_onRestoreInstanceState(Fragment, Bundle)}.
     *
     * @param parent fragmento pai dos aninhados que podem ser retidos.
     */
    public static void applyWorkaroundForBug74222_onSaveInstanceState(Fragment parent) {
        retainedFragments = null;

        List<Fragment> fragments = parent.getChildFragmentManager().getFragments();
        int fragmentsSize = fragments.size();
        for (int i = 0; i < fragmentsSize; i++) {
            Fragment fragment = fragments.get(i);
            if (fragment == null || !fragment.getRetainInstance()) {
                continue;
            }

            if (retainedFragments == null) {
                retainedFragments = new Fragment[fragmentsSize];
            }
            retainedFragments[i] = fragment;
        }
    }

    /**
     * Contorno para o bug: https://code.google.com/p/android/issues/detail?id=74222
     * <br>
     * Fragmentos aninhados (filhos de outros fragmentos) com {@link DialogFragment#getRetainInstance() retainInstance} == <code>true</code>
     * não retém a instancia, passando pelo processo normal de destruíção e recuperação como um fragmento normal.<br>
     * Esta parte do contorno restaura as instâncias dos fragmentos aninhados que foram armazenadas através do método {@link #applyWorkaroundForBug74222_onSaveInstanceState(Fragment)}.
     * A restauração é feita através da alteração do estado salvo do {@link FragmentManager}, fazendo as instâncias dos fragmentos passarem
     * pelas inicializações de fragmentos retidos e a recolocação dos valores perdidos quando foram destruídas erroneamente.
     *
     * @param parent fragmento pai dos aninhados que podem ser retidos.
     * @param savedInstanceState estado salvo do fragmento pai.
     */
    public static void applyWorkaroundForBug74222_onRestoreInstanceState(Fragment parent, Bundle savedInstanceState) {
        if (retainedFragments == null) {
            return;
        }

        FragmentManagerState fms = savedInstanceState.getParcelable(FragmentActivity.FRAGMENTS_TAG);
        if (fms != null) {
            FragmentState[] states = fms.mActive;
            if (states != null) {
                if (states.length == retainedFragments.length) {
                    FragmentActivity activity = parent.getActivity();
                    for (int i = 0; i < retainedFragments.length; i++) {
                        Fragment fragment = retainedFragments[i];
                        if (fragment == null) {
                            continue;
                        }

                        FragmentState state = states[i];
                        state.mInstance = fragment;

                        //Atribuições necessárias para fragmentos retidos.
                        fragment.mSavedViewState = null;
                        fragment.mBackStackNesting = 0;
                        fragment.mInLayout = false;
                        fragment.mAdded = false;
                        fragment.mTarget = null;
                        if (state.mSavedFragmentState != null) {
                            state.mSavedFragmentState.setClassLoader(activity.getClassLoader());
                            fragment.mSavedViewState = state.mSavedFragmentState.getSparseParcelableArray(FragmentManagerImpl.VIEW_STATE_TAG);
                            fragment.mSavedFragmentState = state.mSavedFragmentState;
                        }

                        //Atribuições necessárias porque durante a destriução do pai, todas estas informações foram zeradas no fragmento retido.
                        fragment.setIndex(state.mIndex, parent);
                        fragment.mFromLayout = state.mFromLayout;
                        fragment.mFragmentId = state.mFragmentId;
                        fragment.mContainerId = state.mContainerId;
                        fragment.mTag = state.mTag;
                        fragment.mDetached = state.mDetached;
                        fragment.mFragmentManager = (FragmentManagerImpl) activity.getSupportFragmentManager();

                    }
                }
            }
        }

        retainedFragments = null;
    }

    /**
     * Contorno para o bug em que o {@link android.support.v4.app.FragmentManager} de um fragmento ({@link android.support.v4.app.Fragment#getChildFragmentManager()})
     * causa IllegalStateException ao ser obtido antes do fragmento ser atachado à Activity. Como não há nenhuma documentação
     * informando que o FragmentoManager do fragmento só pode ser obtido depois disso, é considerado um bug.<br>
     * O contorno é ter um FragmentManager que engloba o fragmento e só chama o {@link android.support.v4.app.Fragment#getChildFragmentManager()}
     * quando necessário, ou seja, depois do fragmento já ser atachado.
     *
     * @param owner fragmento dono do FragmentManager.
     */
    public static FragmentManager applyWorkaroundForInvalidFragmentManagerStateBug(Fragment owner) {
        return new InvalidStateWorkaroundFragmentManager(owner);
    }

    /**
     * Contorno para o bug em que o Android não invalida o options menu (action bar) quando um fragmento na seguinte situação é removido:
     * <ul>
     * 	<li>o fragmento não possui options menu ({@link android.support.v4.app.Fragment#hasOptionsMenu()});</li>
     * 	<li>o fragmento tem fragmentos filhos que possuem options menu.</li>
     * </ul>
     * O resultado é que acabam ficando ações mortas dos menus provenientes dos filhos do fragmento que foi removido.<br>
     * O contorno é declarar que o fragmento possui options menu.
     *
     * @param fragment fragmento que será corrigido.
     */
    public static void applyWorkaroundForDeadOptionsMenuBug(Fragment fragment) {
        fragment.setHasOptionsMenu(true);
    }


	/**
	 * Contorno para o bug em que o {@link ClassLoader} padrão utilizado pelo {@link android.os.Parcel} não contém as classes
	 * da própria aplicação ou de suas bibliotecas (contém apenas as classes do Android), ocasionando {@link ClassNotFoundException}
	 * ao tentar ler um {@link android.os.Parcelable} de uma classe da Aplicação. Como a documentação do {@link android.os.Parcel#readParcelable(ClassLoader)},
	 * por exemplo, não informa que o ClassLoader padrão é um limitado apenas as classes do Android, ele leva a crer
	 * que irá funcionar normalmente para carregar um Parcelable proveniente de uma classe da Aplicação. Por isto, o
	 * ClassNotFoundException é considerado um bug.<br>
	 * O contorno é ler o Parcelable passando um ClassLoader que contenha as classes da aplicação.
	 *
	 * @param source parcel utilizado na obtenção do Parcelable.
	 * @return o Parcelable obtido.
	 */
	public static <T extends Parcelable> T applyWorkaroundForParcelableDefaultClassloaderBug(Parcel source) {
		return source.readParcelable(AndroidBugsUtils.class.getClassLoader());
	}
	/**
	 * Aplica a mesma correção do método {@link #applyWorkaroundForParcelableDefaultClassloaderBug(android.os.Parcel)} mas
	 * lê um {@link java.util.HashMap} do {@link android.os.Parcel} que pode conter {@link android.os.Parcelable} em seu conteúdo.
	 * @param source parcel utilizado na obtenção do HashMap.
	 * @return o HashMap obtido.
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> HashMap<K, V> applyWorkaroundForHashMapDefaultClassloaderBug(Parcel source) {
		return source.readHashMap(AndroidBugsUtils.class.getClassLoader());
	}
	/**
	 * Aplica a mesma correção do método {@link #applyWorkaroundForParcelableDefaultClassloaderBug(android.os.Parcel)} mas
	 * lê um {@link java.util.ArrayList} do {@link android.os.Parcel} que pode conter {@link android.os.Parcelable} em seu conteúdo.
	 * @param source parcel utilizado na obtenção do ArrayList.
	 * @return o ArrayList obtido.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> applyWorkaroundForArrayListDefaultClassloaderBug(Parcel source) {
		return source.readArrayList(AndroidBugsUtils.class.getClassLoader());
	}
	/**
	 * Contorno para o bug em que o {@link android.content.Intent} é recuperado de um {@link android.os.Parcel} mas acaba deixando seus <code>extras</code>
	 * ({@link android.os.Bundle}) sem o {@link ClassLoader} da aplicação. O correto seria o Intent instanciar um Bundle que já vem
	 * com o ClassLoader da aplicação definido (como na maioria de seus construtores) ou então utilizar o ClassLoader vindo
	 * do {@link android.os.Parcelable.ClassLoaderCreator#createFromParcel(android.os.Parcel, ClassLoader)}.
	 * O contorno lê o Intent do Parcel e define manualmente o ClassLoader da apliacção nos extras.
	 *
	 * @param source Parcel utilizado na obtenção do Intent.
	 * @return o Intent obtido.
	 */
	public static Intent applyWorkaroundForIntentDefaultClassloaderBug(Parcel source) {
		ClassLoader appClassLoader = AndroidBugsUtils.class.getClassLoader();
		Intent intent = source.readParcelable(appClassLoader);
		intent.setExtrasClassLoader(appClassLoader);
		return intent;
	}

	/**
	 * Contorno para o bug descrito em: https://groups.google.com/forum/#!msg/android-developers/r4oJO3DPPis/HDU1yhdlUFgJ
	 * <br>
	 * O SearchView troca o logo da aplicação pelo ícone qunado está mostrando o campo de pesquisa.
	 * O contorno é setar o logo como ícone da ActionBar.
	 *
	 * @param activity activity que terá o logo ajeitado.
	 */
	public static void applyWorkaroundForSearchViewIconVsLogoBug(Activity activity) {
		int logo = activity.getApplicationInfo().logo;
		if (logo == 0) {
			return;
		}

        ActionBar actionBar = activity.getActionBar();
        if (actionBar == null) {
            throw new IllegalStateException("ActionBar is null");
        }
        actionBar.setIcon(logo);
	}

	/**
	 * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=27526
	 * <br>
	 * O {@link android.support.v4.view.ViewPager} não chamada o método {@link android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)} na primeira vez em que
	 * ele é utilizado. Nas demais trocas de página funciona normalmente, então este contorno
	 * ajeita o {@link android.support.v4.view.PagerAdapter} que será utilizado para que ele faça a primeira chamada no momento devido.<br>
	 * O listener ainda deve ser adicionado no pager para que as demais chamadas sejam efetuadas.<br>
	 * <br>
	 * 21/08/2014: Novos testes revelaram que quando o ViewPager está sendo recuperado (depois de sair e voltar de uma tela, por exemplo)
	 * ele até faz a primeira chamada do listener, mas apenas se a página não for 0. Ou seja, se for navegado a uma página diferente
	 * da 0, sair e voltar, ele acaba chamando corretamente. Isto acaba fazendo com que se chame o listener duas vezes ao usar o workaround.
	 *
	 * @return o adapter ajeitado.
	 */
	public static PagerAdapter applyWorkaroundForBug27526(ViewPager pager, PagerAdapter adapter, OnPageChangeListener listener) {
		return new Bug27526WorkaroundPagerAdapter(adapter, pager, listener);
	}


	/**
	 * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=3847
	 * <br>
	 * Quando um objeto cujo a classe implementa {@link java.util.List} é salvo em um {@link android.os.Bundle} durante a destruição de uma
	 * Activity, ele restaura este objeto posteriormente sempre como um {@link java.util.ArrayList}, igorando a classe original do
	 * objeto.<br>
	 * A correção obtém o objeto salvo como Collection e adiciona o conteúdo na classe correta.
	 *
	 * @param bundle bundle que contém o objeto salvo.
	 * @param key chave para obter o objeto.
	 * @return o objeto corrigido.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Stack<T> applyWorkaroundForBug3847(Bundle bundle, String key) {
		Collection<T> col = (Collection<T>) bundle.getSerializable(key);
		Stack<T> ret = new Stack<>();
		ret.addAll(col);
		return ret;
	}


	/**
	 * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=34833
	 * <br>
	 * A classe {@link android.app.DatePickerDialog} sempre chama o callback {@link android.app.DatePickerDialog.OnDateSetListener} em seu <code>onStop</code>,
	 * fazendo com que o callback seja chamado duas vezes quando a seleção de data é confirmada e chamando também quando
	 * a seleção é cancelada.<br>
	 * O contorno extende a classe DatePickerDialog e faz com que o callback seja chamado apenas na situação correta.
	 *
	 * @param context contexto.
	 * @param callBack callback da seleção da data.
	 * @param year ano que será mostrado inicialmente.
	 * @param monthOfYear mês que será mostrado inicialmente.
	 * @param dayOfMonth dia que será mostrado inicialmente.
	 * @return um DatePickerDialog com o contorno do bug.
	 */
	public static DatePickerDialog applyWorkaroundForBug34833(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
		return new Bug34833WorkaroundDatePickerDialog(context, callBack, year, monthOfYear, dayOfMonth);
	}

	/**
	 * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=34833
	 * <br>
	 * A classe {@link android.app.TimePickerDialog} sempre chama o callback {@link android.app.TimePickerDialog.OnTimeSetListener} em seu <code>onStop</code>,
	 * fazendo com que o callback seja chamado duas vezes quando a seleção de data é confirmada e chamando também quando
	 * a seleção é cancelada.<br>
	 * O contorno extende a classe TimePickerDialog e faz com que o callback seja chamado apenas na situação correta.
	 *
	 * @param context contexto.
	 * @param callBack callback da seleção da data.
	 * @param hourOfDay hora que será mostrada inicialmente.
	 * @param minute minuto que será mostrado inicialmente.
	 * @param is24Hour indica se o formato da hora é em 24.
	 * @return um TimePickerDialog com o contorno do bug.
	 */
	public static TimePickerDialog applyWorkaroundForBug34833(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24Hour) {
		//Como o TimePickerDialog não possui um "getTimePicker", tem que obter o TimePicker por reflexão... caso ocorra algum problema,
		//retorna o TimePickerDialog normal ao invés de dar erro.
		try {
			return new Bug34833WorkaroundTimePickerDialog(context, callBack, hourOfDay, minute, is24Hour);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            return new TimePickerDialog(context, callBack, hourOfDay, minute, is24Hour);
        }
	}

    /**
     * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=17423
     * <br>
     * O {@link DialogFragment} com {@link DialogFragment#getRetainInstance() retainInstance} == <code>true</code> não exibe o Dialog ao ser restaurado
     * (devido a alteração de orientação, por exemplo) porque fica com a mensagem de <code>dismiss</code> pendente.<br>
     * A correção é definir a mensagem de <code>dismiss</code> como <code>null</code> quando a View do fragmento é destruído.
     *
     * @param dialogFragment fragmento.
     */
    public static void applyWorkaroundForBug17423(DialogFragment dialogFragment) {
        if (dialogFragment.getDialog() != null && dialogFragment.getRetainInstance()) {
            dialogFragment.getDialog().setDismissMessage(null);
        }
    }

    /**
     * Contorno para o bug em que um {@link AlertDialog} que foi construído sem título não mostra o título que foi setado posteriormente (através
     * do {@link AlertDialog#setTitle(CharSequence)}. O problema acontece porque o AlertDialog esconde a View que exibe o título se este valor
     * não for definido no momento de sua construção. Desta forma, se o título for definido posteriormente ele simplesmente não aparece.
     * O correto seria a View ter sua visibilidade alterada no momento da definição do novo título.<br>
     * O contorno faz com que seja definido um texto temporário como titulo, fazendo com que a View do titulo seja criada.<br>
     * <br>
     * Este contorno só deve ser utilizado se o titulo do AlertDialog for definido logo após sua criação.
     *
     * @param dialogBuilder construtor de AlertDialog.
     */
    public static void applyWorkaroundForAlertDialogWithInvisibleTitleBug(Builder dialogBuilder) {
        dialogBuilder.setTitle(" ");
    }

    /**
     * Contorno para o problema em que um {@link Dialog} com um {@link android.widget.ListView} com tamanho flexível (de acordo com o conteúdo)
	 * executa sucessivos layouts, fazendo com que haja uma demora para a exibição do Dialog. A demora é causada pela recriação das Views
	 * do ListView, o que até seria compreensível se fosse feita apenas uma vez a mais, devido a necessidade do Dialog saber o tamanho de
	 * seu conteúdo. Entretanto, o layout é executado inúmeras vezes desnecessariamente.<br>
	 * O contorno faz com que o tamanho do Dialog seja fixado após o layout, evitando futuros layouts desnecessários. Entretanto, este contorno
	 * não resolve completamente o problema, apenas reduz o número de layouts quase pela metade.<br>
	 * <br>
	 * Mais informações sobre o problema e a origem do contorno podem ser vistas <a href="http://stackoverflow.com/questions/19326142/why-listview-expand-collapse-animation-appears-much-slower-in-dialogfragment-tha">aqui</a>.
     *
     * @param dialog Dialog onde o contorno será aplicado.
     * @param dialogView View do Dialog.
     */
    public static void applyWorkaroundForAlertDialogWithFlexibleListViewBug(final Dialog dialog, final View dialogView) {
        ViewTreeObserver vto = dialogView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                // Key area to perform optimization. Fix the dialog size!
                // During ListView's rows animation, dialog will never need to
                // perform computation again.
                int width = dialog.getWindow().getDecorView().getWidth();
                int height = dialog.getWindow().getDecorView().getHeight();
                dialog.getWindow().setLayout(width, height);

                ViewTreeObserver obs = dialogView.getViewTreeObserver();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

    }

    /**
     * Contorno para o problema em que dispositivos da Samsung com Android 5 geram erro ao utilizar o {@link DatePickerDialog} com Material Design.
     * O erro é proveniente da própria implementação do Android 5 feita pela Samsung, que tenta formatar uma String passando um valor do tipo
     * errado.<br>
     * <br>
     * O contorno cria um ContextWrapper que trata a situação específica do erro, corrigindo a String com erro para o formato correto.
     * Baseado no seguinte <a href="http://stackoverflow.com/questions/28618405/datepicker-crashes-on-my-device-when-clicked-with-personal-app/34853067#34853067">post</a>.
     *
     * @param context contexto que será wrappeado com a correção.
     * @return um contexto com a correção.
     */
    public static Context applyWorkaroundForSamsung5DatePickerBug(Context context) {
        if ("samsung".equalsIgnoreCase(Build.MANUFACTURER) &&
           (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1)) {
            return new Samsung5DatePickerBugContextWrapper(context);
        }

        return context;
    }


	/**
	 * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=5973
	 * A classe {@link android.os.Parcel} não possui método para escrever um <code>boolean</code>.<br>
	 * A correção escreve um byte no lugar do boolean que depois deve ser lido através do método: {@link #applyWorkaroundForBug5973_read(android.os.Parcel)}.
	 *
	 * @param parcel parcel que terá o boolean escrito.
	 * @param b boolean
	 */
	public static void applyWorkaroundForBug5973_write(Parcel parcel, boolean b) {
		parcel.writeByte((byte) (b ? 1 : 0));
	}

	/**
	 * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=5973
	 * A classe {@link android.os.Parcel} não possui método para ler um <code>boolean</code>.<br>
	 * A correção lê um byte no lugar do boolean que deve ter sido escrito através do método {@link #applyWorkaroundForBug5973_write(android.os.Parcel, boolean)}.
	 *
	 * @param parcel parcel que terá o boolean lido.
	 * @return o boolean lido do parcel.
	 */
	public static boolean applyWorkaroundForBug5973_read(Parcel parcel) {
		return parcel.readByte() != 0;
	}

	/**
	 * Contorno para o bug em que o texto concebido através de um teclado com swipe é repetido quando o texto do {@link android.widget.TextView}
	 * é alterado através do {@link android.text.TextWatcher#afterTextChanged(android.text.Editable)}. Por exemplo, se o usuário concebe um "de" com swipe e
	 * o {@link android.text.Editable} com o "de" é alterado, qualquer outro input fará com que o "de" seja repetido. No caso de um espaço,
	 * o input que chegará será um "de ", ou seja, no final das contas o Editable ficará com "dede ".<br>
	 * Isto ocorre porque o teclado com swipe coloca um Span (ComposingText) que engloba o texto concebido com o swipe no Editable,
	 * mas este Span é excluído automaticamente quando o {@link android.text.Editable#replace(int, int, CharSequence)} é chamado
	 * (que é uma operação liberada para ser feita no {@link android.text.TextWatcher#afterTextChanged(android.text.Editable)}). O problema é que o
	 * teclado com swipe assume que o Span que ele adicionou vai estar lá, então ele manda substituir o texto englobado
	 * por ele pelo texto da palavra concebida somado ao novo input (como no exemplo, ele manda substituir o "de" por "de ").
	 * Mas como o Span não está mais lá, o {@link android.view.inputmethod.BaseInputConnection} assume que o texto deve ser inserido onde o cursor
	 * está, ou seja, ao invés de substituir o texto, ele apenas insere ele (fazendo com que o "de" se torne "dede ").<br> 
	 * <br>
	 * O contorno é englobar o {@link android.text.TextWatcher} com um que verifica se o texto possui o Span de comspoição antes de ser
	 * alterado e em caso afirmativo, ele chama o {@link android.view.inputmethod.InputMethodManager#restartInput(android.view.View)} para sinalizar ao teclado
	 * com swipe que ele não deve mais inserir o texto da palavra concebida. 
	 * 
	 * @param context contexto.
	 * @param textView TextView onde o TextWatcher está inserido.
	 * @param textWatcher TextWatcher que será corrigido.
	 * @return um TextWatcher que contorna o bug.
	 */
	public static TextWatcher applyWorkaroundForSwipeSpanBug(Context context, TextView textView, TextWatcher textWatcher) {
		return new SwipeSpanBugTextWatcher(context, textView ,textWatcher);
	}

    /**
     * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=163954 <br>
     * O SwipeRefreshLayout se perde em um evento de click misterioso (sem ação do usuário).
     * A correção verifica um {@link IllegalArgumentException} ocorrido durante o {@link SwipeRefreshLayout#onTouchEvent(MotionEvent)} e,
     * se for o bug, simplesmente ignora-o. Não há nenhum impacto ao usuário.
     *
     * @param e exceção ocorrida durante o onTouchEvent.
     */
    public static boolean applyWorkaroundForBug163954(IllegalArgumentException e) {
        if (e.getMessage().equals("pointerIndex out of range")) {
            return true;
        }
        throw e;
    }

    /**
     * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=52962 <br>
     * O BuildConfig.DEBUG de bibliotecas fica sempre como <code>false</code>, mesmo que a aplicação tenha sido buildada em modo DEBUG.<br>
     * O contorno obtém o BuildConfig.DEBUG da aplicação através de reflexão.
     *
     * @param context contexto.
     * @return <code>true</code> se a aplicação foi construída em modo DEBUG e <code>false</code> caso contrário.
     */
    @SuppressWarnings("TryWithIdenticalCatches")
    public static boolean applyWorkaroundForBug52962(Context context) {
        try {
            Class<?> bcClass = Class.forName(context.getApplicationInfo().packageName + ".BuildConfig");
            return bcClass.getDeclaredField("DEBUG").getBoolean(null);
        } catch (ClassNotFoundException ignored) {
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return false;
    }

    /**
     * Contorno para o bug descrito em: https://code.google.com/p/android/issues/detail?id=69711<br>
     * O <code>drawerLayout</code> não chama o listener (<code>actionBarToggle</code>) quando é aberto antes de sofrer layout.
     * O contorno faz com que o listener seja chamado caso o <code>drawerLayout</code> ainda não tenha sofrido layout.
     *
     * @param drawerLayout o {@link DrawerLayout}.
     * @param actionBarToggle o {@link ActionBarDrawerToggle}.
     * @param drawerView a View utilizada no drawer.
     */
    public static void applyWorkaroundForBug69711(DrawerLayout drawerLayout, ActionBarDrawerToggle actionBarToggle, View drawerView) {
        drawerLayout.openDrawer(drawerView);
        if (!ViewCompat.isLaidOut(drawerLayout)) {
            actionBarToggle.onDrawerOpened(drawerView);
        }
    }

	
	/*
	 * Classes auxiliares
	 */
	
	private static final class Bug27526WorkaroundPagerAdapter extends PagerAdapter {

		private final PagerAdapter internalAdapter;
		private final ViewPager pager;
		private OnPageChangeListener listener;
		
		public Bug27526WorkaroundPagerAdapter(PagerAdapter internalAdapter, ViewPager pager, OnPageChangeListener listener) {
			this.internalAdapter = internalAdapter;
			this.pager = pager;
			this.listener = listener;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return internalAdapter.getPageTitle(position);
		}
		
		@Override
		public void setPrimaryItem(ViewGroup container, final int position, Object object) {
			//O bug consiste no listener não ser chamado na primeira vez, então só faz isto uma vez também.
			if (listener != null) {
				//Posta o evento na Thread de UI para evitar problemas de ordem de eventos.
				OnPageSelectedAction action = new OnPageSelectedAction(listener, position);
				pager.addOnAttachStateChangeListener(action);
				pager.post(action);
				
				listener = null;
			}
			
			internalAdapter.setPrimaryItem(container, position, object);
		}
		
		@Override
		public int getCount() {
			return internalAdapter.getCount();
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			return internalAdapter.instantiateItem(container, position);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			internalAdapter.destroyItem(container, position, object);
		}
		
		@Override
		public Parcelable saveState() {
			return internalAdapter.saveState();
		}
		
		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
			internalAdapter.restoreState(state, loader);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return internalAdapter.isViewFromObject(arg0, arg1);
		}

        @Override
        public void startUpdate(ViewGroup container) {
            internalAdapter.startUpdate(container);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            internalAdapter.finishUpdate(container);
        }

        private static final class OnPageSelectedAction implements Runnable, OnAttachStateChangeListener {
			private final int position;
			private OnPageChangeListener listener;
			
			public OnPageSelectedAction(OnPageChangeListener listener, int position) {
				this.listener = listener;
				this.position = position;
			}
			
			@Override
			public void run() {
				if (listener != null) {
					listener.onPageSelected(position);
				}
			}

			@Override
			public void onViewAttachedToWindow(View v) {
			}

			@Override
			public void onViewDetachedFromWindow(View v) {
				//Se a view já foi desanexada da Window (destruída), o listener não pode mais ser chamado.
				//Isto acaba acontecendo ao mudar a orientação do dispositivo várias vezes rapidamente, pq este runnable
				//é colocado na fila e acaba sendo executado só depois que a View já foi destruída devido a alteração de orientação.
				listener = null;
			}
		}
	}
	
	private static final class InvalidStateWorkaroundFragmentManager extends FragmentManager {

		private final Fragment owner;

		public InvalidStateWorkaroundFragmentManager(Fragment owner) {
			this.owner = owner;
		}
		
		@Override
		public void addOnBackStackChangedListener(OnBackStackChangedListener arg0) {
			owner.getChildFragmentManager().addOnBackStackChangedListener(arg0);
		}

		@Override
		@SuppressLint("CommitTransaction")
		public FragmentTransaction beginTransaction() {
			return owner.getChildFragmentManager().beginTransaction();
		}

		@Override
		public void dump(String arg0, FileDescriptor arg1, PrintWriter arg2, String[] arg3) {
			owner.getChildFragmentManager().dump(arg0, arg1, arg2, arg3);			
		}

		@Override
		public boolean executePendingTransactions() {
			return owner.getChildFragmentManager().executePendingTransactions();
		}

		@Override
		public Fragment findFragmentById(int arg0) {
			return owner.getChildFragmentManager().findFragmentById(arg0);
		}

		@Override
		public Fragment findFragmentByTag(String arg0) {
			return owner.getChildFragmentManager().findFragmentByTag(arg0);
		}

		@Override
		public BackStackEntry getBackStackEntryAt(int arg0) {
			return owner.getChildFragmentManager().getBackStackEntryAt(arg0);
		}

		@Override
		public int getBackStackEntryCount() {
			return owner.getChildFragmentManager().getBackStackEntryCount();
		}

		@Override
		public Fragment getFragment(Bundle arg0, String arg1) {
			return owner.getChildFragmentManager().getFragment(arg0, arg1);
		}

		@Override
		public List<Fragment> getFragments() {
			return owner.getChildFragmentManager().getFragments();
		}

		@Override
		public void popBackStack() {
			owner.getChildFragmentManager().popBackStack();			
		}

		@Override
		public void popBackStack(String arg0, int arg1) {
			owner.getChildFragmentManager().popBackStack(arg0, arg1);			
		}

		@Override
		public void popBackStack(int arg0, int arg1) {
			owner.getChildFragmentManager().popBackStack(arg0, arg1);			
		}

		@Override
		public boolean popBackStackImmediate() {
			return owner.getChildFragmentManager().popBackStackImmediate();
		}

		@Override
		public boolean popBackStackImmediate(String arg0, int arg1) {
			return owner.getChildFragmentManager().popBackStackImmediate(arg0, arg1);
		}

		@Override
		public boolean popBackStackImmediate(int arg0, int arg1) {
			return owner.getChildFragmentManager().popBackStackImmediate(arg0, arg1);
		}

		@Override
		public void putFragment(Bundle arg0, String arg1, Fragment arg2) {
			owner.getChildFragmentManager().putFragment(arg0, arg1, arg2);
		}

		@Override
		public void removeOnBackStackChangedListener(OnBackStackChangedListener arg0) {
			owner.getChildFragmentManager().removeOnBackStackChangedListener(arg0);
		}

		@Override
		public SavedState saveFragmentInstanceState(Fragment arg0) {
			return owner.getChildFragmentManager().saveFragmentInstanceState(arg0);
		}

		@Override
		public boolean isDestroyed() {
			return owner.getChildFragmentManager().isDestroyed();
		}
	}
	
	private static final class Bug34833WorkaroundDatePickerDialog extends DatePickerDialog {

		private final OnDateSetListener callBack;

		public Bug34833WorkaroundDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
			super(context, null, year, monthOfYear, dayOfMonth);
			this.callBack = callBack;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			DatePicker datePicker = getDatePicker();
			callBack.onDateSet(datePicker, datePicker.getYear(),
                    		   datePicker.getMonth(), datePicker.getDayOfMonth());
		}
		
		@Override
		protected void onStop() {
			super.onStop();
			getDatePicker().clearFocus();
		}
	}
	
	private static final class Bug34833WorkaroundTimePickerDialog extends TimePickerDialog {
		
		private final OnTimeSetListener callBack;
		private final TimePicker timePicker;

		public Bug34833WorkaroundTimePickerDialog(Context context, OnTimeSetListener callBack, int hourOfDay, int minute, boolean is24HourView) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
			super(context, null, hourOfDay, minute, is24HourView);
			this.callBack = callBack;
			
			//É obrigado a obter o timePicker desta forma porque o TimePickerDialog não possui um "getTimePicker" (ao contrário do DatePickerDialog).
			Field field = TimePickerDialog.class.getDeclaredField("mTimePicker");
			field.setAccessible(true);
			timePicker = (TimePicker) field.get(this);
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			callBack.onTimeSet(timePicker, timePicker.getCurrentHour(), timePicker.getCurrentMinute());
		}
		
		@Override
		protected void onStop() {
			super.onStop();
			timePicker.clearFocus();
		}
	}
	
	private static final class SwipeSpanBugTextWatcher implements TextWatcher {
		
		private final TextWatcher innerWatcher;
		private final InputMethodManager inputMethodManager;
		private final TextView textView;
		
		public SwipeSpanBugTextWatcher(Context context, TextView textView, TextWatcher innerWatcher) {
			this.innerWatcher = innerWatcher;
			this.inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			this.textView = textView;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			innerWatcher.beforeTextChanged(s, start, count, after);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			innerWatcher.onTextChanged(s, start, before, count);
		}

		@Override
		public void afterTextChanged(Editable s) {
			boolean isComposing = BaseInputConnection.getComposingSpanStart(s) != -1;
			
			innerWatcher.afterTextChanged(s);
			
			if (isComposing) {
				inputMethodManager.restartInput(textView);
			}
		}
	}

    private static class Samsung5DatePickerBugContextWrapper extends ContextWrapper {
        private Resources wrappedResources;

        public Samsung5DatePickerBugContextWrapper(Context context) {
            super(context);
        }

        @Override
        public Resources getResources() {
            if(wrappedResources == null) {
                Resources r = super.getResources();
                wrappedResources = new Resources(r.getAssets(), r.getDisplayMetrics(), r.getConfiguration()) {
                    @NonNull
                    @Override
                    public String getString(int id, Object... formatArgs) throws NotFoundException {
                        try {
                            return super.getString(id, formatArgs);
                        } catch (IllegalFormatConversionException ifce) {
                            Log.e("DatePickerDialogFix", "IllegalFormatConversionException Fixed!", ifce);
                            String template = super.getString(id);
                            template = template.replaceAll("%" + ifce.getConversion(), "%s");
                            return String.format(getConfiguration().locale, template, formatArgs);
                        }
                    }
                };
            }

            return wrappedResources;
        }
    }
}
