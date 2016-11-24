package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.internal.widget.TintManager;
import android.util.AttributeSet;
import android.widget.CheckBox;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * Variação do {@link AppCompatCheckBox} que permite a utilização de drawables diferentes do padrão para o botão do checkbox, de acordo com
 * o bug descrito em: {@link #applyWorkaroundForGetTintDrawable(Drawable)}.<br>
 * Baseado no {@link AppCompatCheckBox}.
 */
public class AppCompatBCheckBox extends CheckBox {

    private static final int[] TINT_ATTRS = {
        android.R.attr.button
    };

    private TintManager mTintManager;
    private Drawable mButtonDrawable;

    public AppCompatBCheckBox(Context context) {
        this(context, null);
    }

    public AppCompatBCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public AppCompatBCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AppCompatBCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppCompatBCheckBox, defStyleAttr, defStyleRes);
        boolean tintAPI21 = a.getBoolean(R.styleable.AppCompatBCheckBox_tintAPI21, false);
        a.recycle();

        if (tintAPI21 || TintManager.SHOULD_BE_USED) {
            mTintManager = TintManager.get(context);

            a = context.obtainStyledAttributes(attrs, TINT_ATTRS, defStyleAttr, 0);
            setButtonDrawable(a.getDrawable(0));
            a.recycle();
        }
    }

    @Override
    public void setButtonDrawable(Drawable buttonDrawable) {
        if (mTintManager != null) {
            buttonDrawable = applyWorkaroundForGetTintDrawable(buttonDrawable);
        }
        mButtonDrawable = buttonDrawable;
        super.setButtonDrawable(buttonDrawable);
    }

    @Override
    public void setButtonDrawable(int resId) {
        if (mTintManager == null) {
            super.setButtonDrawable(resId);
            return;
        }

        Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
        setButtonDrawable(drawable);
    }

    @Override
    public int getCompoundPaddingLeft() {
        int padding = super.getCompoundPaddingLeft();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Before JB-MR1 the button drawable wasn't taken into account for padding. We'll
            // workaround that here
            if (mButtonDrawable != null) {
                padding += mButtonDrawable.getIntrinsicWidth();
            }
        }
        return padding;
    }


    /**
     * Contorno para a maior gambiarra já vista no Android: para obter o {@link ColorStateList} utilizado no tint do drawable do botão,
     * ele verifica se o id do drawable é igual ao id do drawable padrão do componente. Ou seja, se o CheckBox utiliza um drawable diferente,
     * ele não identifica que é o CheckBox que está chamando o método e consequentemente não retorna o ColorStateList...<br>
     * A correção utiliza parte do método {@link TintManager#getDrawable(int, boolean)} e simplesmente passa o id do drawable padrão do botão
     * do CheckBox para que ele retorne o ColorStateList correto.<br>
     * <br>
     * Este contorno não está no {@link AndroidBugsUtils} porque estas classes de AppCompat já são contornos, além de utilizarem parte da API interna
     * do Android. Desta forma, não seria interessante colocar algo tão específico no AndroidBugsUtils.
     *
     * @param drawable drawable do botão do checkbox.
     * @return o drawable tintado ou <code>null</code> se o id não aponta para nenhum drawable.
     */
    public Drawable applyWorkaroundForGetTintDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        ColorStateList tintList = mTintManager.getTintList(R.drawable.abc_btn_check_material);
        if (tintList == null) {
            return null;
        }

        drawable = drawable.mutate();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(drawable, tintList);
        return drawable;
    }
}
