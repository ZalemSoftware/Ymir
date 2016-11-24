package br.com.zalem.ymir.client.android.entity.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Base para fragmentos que suportam a definição de um tema (overlay) que será aplicado no {@link LayoutInflater} utilizado na criação das Views.<br>
 * O tema pode ser definido através do argumento {@link #THEME_ARGUMENT} ou pelo método {@link #setTheme(int)}.<br>
 * A subclasse deve implementar o método {@link #onCreateThemedView(LayoutInflater, ViewGroup, Bundle)} que já recebe o LayoutInflater
 * influenciado pelo tema. Se necessário, este LayoutInflater também pode ser obtido através dos métodos {@link #getThemedLayoutInflater()} e
 * {@link #getThemedLayoutInflater(LayoutInflater)}.<br>
 * <br>
 * Se o fragmento não tiver um tema definido, o LayoutInflater utilizado será o normal, sem um tema influenciado-o.
 *
 * @author Thiago Gesser
 */
public abstract class AbstractThemedFragment extends Fragment {

    /**
     * Argumento do tipo <code>int</code> que define o identificador do tema que será aplicado nas Views criadas por este fragmento.<br>
     * Por padrão, nenhum tema é aplicado.
     */
    public static final String THEME_ARGUMENT = "THEME_ARGUMENT";

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater themedInflater = getThemedLayoutInflater(inflater);
        return onCreateThemedView(themedInflater, container, savedInstanceState);
    }

    /**
     * Define o tema do fragmento através do argumento {@link #THEME_ARGUMENT}.
     *
     * @param themeId identificador do tema.
     */
    public void setTheme(int themeId) {
        Bundle arguments = getArguments();
        if (arguments == null) {
            arguments = new Bundle();
            setArguments(arguments);
        }
        arguments.putInt(THEME_ARGUMENT, themeId);
    }


    /**
     * Obtém o tema do fragmento definido no argumento {@link #THEME_ARGUMENT}.
     *
     * @return o identificador do tema ou <code>-1</code> se ele não foi definido.
     */
    public int getTheme() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return -1;
        }

        return arguments.getInt(THEME_ARGUMENT, -1);
    }


    /**
     * Chamado para a criação das Views do fragmento, como descrito em {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * Se o fragmento possui um tema definido, o <code>inflater</code> estará influenciado por ele, fazendo com que as Views criadas sejam
     * afetadas.
     *
     * @param inflater LayoutInflater influenciado pelo do fragmento, se houver.
     * @param container View pai do fragmento.
     * @param savedInstanceState estado salvo do fragmento ou <code>null</code> caso não houver.
     * @return A View principal do fragmento.
     */
    protected abstract View onCreateThemedView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    /**
     * Obtém o {@link LayoutInflater} influenciado pelo tema do fragmento.
     *
     * @return o LayoutInflater influenciado pelo tema ou o normal caso o fragmento não tenha um tema definido.
     */
    protected LayoutInflater getThemedLayoutInflater() {
        return getThemedLayoutInflater(getActivity().getLayoutInflater());
    }

    /**
     * Obtém o {@link LayoutInflater} influenciado pelo tema do fragmento, utilizando o <code>inflater</code> como base.
     *
     * @param inflater LayoutInflater base do que será retornado.
     * @return o LayoutInflater influenciado pelo tema ou o normal caso o fragmento não tenha um tema definido.
     */
    protected LayoutInflater getThemedLayoutInflater(LayoutInflater inflater) {
        int themeId = getTheme();
        if (themeId > 0) {
            ContextThemeWrapper themedContext = new ContextThemeWrapper(inflater.getContext(), themeId);
            inflater = inflater.cloneInContext(themedContext);
        }
        return inflater;
    }
}
