package br.com.zalem.ymir.client.android.widget;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.BitSet;

import br.com.zalem.ymir.client.android.commons.BuildConfig;

/**
 * Adapter de fragmentos para {@link android.support.v4.view.ViewPager}.<br>
 * Foi necessário criar uma implementação própria de adapter de fragmentos ao invés de usar um dos disponibilizados
 * pelo Android por questões de performance e de um melhor gerenciamento dos estados dos fragmentos. 
 *
 * @author Thiago Gesser
 */
public abstract class AbstractFragmentPagerAdapter <T extends Fragment> extends PagerAdapter {

	private static final String SAVED_FRAGMENTS_POSITIONS = "SAVED_FRAGMENTS_POSITIONS"; 
	private static final String SAVED_STATES = "SAVED_STATES";
	
	private final FragmentManager fragmentManager;
	private final T[] fragments;
	private final SavedState[] savedStates;

    private IFragmentPagerAdapterListener<T> listener;

	@SuppressWarnings("unchecked")
	public AbstractFragmentPagerAdapter(FragmentManager fragmentManager, int length) {
		this.fragmentManager = fragmentManager;
		
		//É obrigado a saber o tamanho desta forma pq se chamar o getCount() vai dar pau pq a classe filha ainda não atribui nada... Pena que não dá pra fazer nada antes de chamar o super().
		this.fragments = (T[]) new Fragment[length];
		this.savedStates = new SavedState[length];
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		//Se já está armazenado em memporia, só recupera.
		T fragment = fragments[position]; 
		if (fragment != null) {
			return fragment;
		}
		
		int containerId = container.getId();
		if (containerId == View.NO_ID) {
			throw new IllegalArgumentException("The ViewPager has no id");
		}
		
		//Cria o fragmento e restaura o estado anterior, se existir. Apenas fragmentos destruídos terão o estado armazenado.
		String fragmentTag = createFragmentTag(position);
		fragment = createFragment(position);
        if (listener != null) {
            listener.onFragmentCreated(fragment, position);
        }
		SavedState savedState = savedStates[position];
		if (savedState != null) {
			fragment.setInitialSavedState(savedState);
			savedStates[position] = null;
		}
		
		//Adiciona o fragmento na tela, salva-o em memória e faz sua inicialização.
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(containerId, fragment, fragmentTag);
		transaction.commit();
		fragments[position] = fragment;

		internalInitializeFragment(fragment, position);
		
		return fragment;
	}
	
	@Override
    @SuppressWarnings("unchecked")
	public void destroyItem(ViewGroup container, int position, Object object) {
		//Salva o estado do fragmento que será removido.
		T fragment = (T) object;
		savedStates[position] = fragmentManager.saveFragmentInstanceState(fragment);
		
		//Remove o fragmento da tela e a sua referência na memória.
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.remove(fragment);
		transaction.commit();
		fragments[position] = null;

        if (listener != null) {
            listener.onFragmentDestroyed(fragment, position);
        }
	}

	@Override
	public Parcelable saveState() {
		Bundle ret = new Bundle();
		//Salva as posições dos fragmentos em memória para recuperá-los posteriormente.
		BitSet fragmentPositions = new BitSet(getCount());
		for (int i = 0; i < fragments.length; i++) {
			if (fragments[i] != null) {
				fragmentPositions.set(i);
			}
		}
		ret.putSerializable(SAVED_FRAGMENTS_POSITIONS, fragmentPositions);
		
		//Salva os estados dos fragmentos que foram destruídos.
		ret.putParcelableArray(SAVED_STATES, savedStates);
		return ret;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void restoreState(Parcelable state, ClassLoader loader) {
		Bundle bundle = (Bundle) state;
		bundle.setClassLoader(loader);

		//Restaura os fragmentos mantidos automaticamente pelo Android.
		BitSet fragmentPositions = (BitSet) bundle.getSerializable(SAVED_FRAGMENTS_POSITIONS);
		int nextSetBit = 0;
		while ((nextSetBit = fragmentPositions.nextSetBit(nextSetBit)) != -1) {
			int fragmentPosition = nextSetBit++;
			String fragmentTag = createFragmentTag(fragmentPosition); 
			T fragment = (T) fragmentManager.findFragmentByTag(fragmentTag);
			//O fragmento deve existir, mas evita que algum problema na sua restauração automática pare a aplicação. 
			if (fragment != null) {
				fragments[fragmentPosition] = fragment;
				internalInitializeFragment(fragment, fragmentPosition);
			}
		}
		
		//Tem que fazer desta forma porque quando a Activity é destruída o array de saved states é recuperado
		//como Parcelable[], ou seja, o cast pra SavedState[] não funciona.
		Parcelable[] parcelables = bundle.getParcelableArray(SAVED_STATES);
		
		if (BuildConfig.DEBUG && parcelables.length != savedStates.length) {
			throw new AssertionError();
		}
		
		for (int i = 0; i < parcelables.length; i++) {
			savedStates[i] = (SavedState) parcelables[i];
		}
		
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return ((Fragment) object).getView() == view;
	}

    @Override
    public void finishUpdate(ViewGroup container) {
        fragmentManager.executePendingTransactions();
    }

    /**
	 * Obtém o fragmento de acordo com a posição ou <code>null</code> caso ele não exista (ou tenha sido destruído).
	 * 
	 * @param position a posição do fragmento.
	 * @return o fragmento obtido ou <code>null</code> caso ele não exista.
	 */
	public T getFragment(int position) {
		return fragments[position];
	}

    /**
     * Obtém a posição do fragmento existente (não destruído) neste adapter.
     *
     * @param fragment fragmento.
     * @return a posição do fragmento ou -1 caso ele tenha sido destruído ou não pertença a este adapter.
     */
    public int getPosition(T fragment) {
        if (fragment == null) {
            throw new IllegalArgumentException("fragment == null");
        }

        for (int i = 0; i < fragments.length; i++) {
            if (fragments[i] == fragment) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Define o listener das ações do adapter.
     *
     * @param listener novo listener.
     */
    public void setListener(IFragmentPagerAdapterListener<T> listener) {
        if (listener != null) {
            for (int i = 0; i < fragments.length; i++) {
                T fragment = fragments[i];
                if (fragment != null) {
                    listener.onFragmentInitialized(fragment, i);
                }
            }
        }

        this.listener = listener;
    }

    /**
     * Obtém o listener das ações do adapter.
     *
     * @return o listener obtido ou <code>null</code> se não há listener definido.
     */
    public IFragmentPagerAdapterListener<T> getListener() {
        return listener;
    }


    /**
	 * Pode ser sobrescrito para executar inicializações no fragmento. Esta inicialização é feita mesmo depois
	 * que o fragmento é restaurado de uma Activity destruída. Por isto, este é o local ideal para inicializar qualquer
	 * coisa que não vai ser salva/restaurada junto com o fragmento durante a destruição/restauração da Activity.
	 * 
	 * @param fragment fragmento a ser inicializado.
	 * @param position posição do fragmento.
	 */
	protected void initializeFragment(T fragment, int position) {
	}
	
	
	/**
	 * Cria o fragmento de acordo com sua posição. Inicializações que não poderão ser salvas com o fragmento durante
	 * a destruição da Activity devem ser feitas no método {@link #initializeFragment(T, int)}.
	 * 
	 * @param position posição do fragmento.
	 * @return o fragmento criado.
	 */
	protected abstract T createFragment(int position);
	
	
	/*
	 * Métodos auxiliares 
	 */
	
	private String createFragmentTag(int position) {
		return getClass().getSimpleName() + String.valueOf(position); 
	}
	
	private void internalInitializeFragment(T fragment, int position) {
		//Não cabe ao fragmento decidir quais ações serão mostradas, por isto inicia todos 
		//como se estivessem escondidos e depois o fragmento controla seus estados.
		fragment.setMenuVisibility(false);
		fragment.setUserVisibleHint(false);
		
		initializeFragment(fragment, position);

        if (listener != null) {
            listener.onFragmentInitialized(fragment, position);
        }
	}


    /**
     * Listener das ações do adapter.
     */
    public interface IFragmentPagerAdapterListener <T extends Fragment> {

        /**
         * Chamado quando um novo fragmento é criado.
         *
         * @param fragment fragmento criado.
         * @param position posiçao do fragmento.
         */
        void onFragmentCreated(T fragment, int position);

        /**
         * Chamado quando um fragmento é inicializado após sua criação ou a restauração.<br>
         * Além disso, se o listener foi setado quando já haviam fragmentos criados, este método também é chamado para estes fragmentos.
         *
         * @param fragment fragmento inicializado.
         * @param position posição do fragmento.
         */
        void onFragmentInitialized(T fragment, int position);

        /**
         * Chamado quando um fragmento é destruido.
         *
         * @param fragment fragmento destruído.
         * @param position posiçao do fragmento.
         */
        void onFragmentDestroyed(T fragment, int position);
    }

    /**
     * Listener de ações do adapter que permite apenas implementar os métodos necessários.
     */
    public static abstract class FragmentPagerAdapterListener <T extends Fragment> implements IFragmentPagerAdapterListener<T> {

        @Override
        public void onFragmentCreated(T fragment, int position) {
        }

        @Override
        public void onFragmentInitialized(T fragment, int position) {
        }

        @Override
        public void onFragmentDestroyed(T fragment, int position) {
        }
    }
}
