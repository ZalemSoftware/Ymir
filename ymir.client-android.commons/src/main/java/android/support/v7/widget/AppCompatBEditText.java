package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.view.TintableBackgroundView;
import android.support.v7.internal.widget.TintContextWrapper;
import android.support.v7.internal.widget.TintInfo;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * Variação do {@link AppCompatEditText} que permite a utilização de backgrounds diferentes do padrão, de acordo com o bug descrito em:
 * {@link #applyWorkaroundForGetTintList(TintManager)}.<br>
 * Baseado inteiramente no {@link AppCompatEditText}.
 */
public class AppCompatBEditText extends EditText implements TintableBackgroundView {

    private static final int[] TINT_ATTRS = {
        android.R.attr.background
    };

    private TintInfo mBackgroundTint;

    public AppCompatBEditText(Context context) {
        this(context, null);
    }

    public AppCompatBEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public AppCompatBEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AppCompatBEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppCompatBEditText, defStyleAttr, defStyleRes);
        boolean tintAPI21 = a.getBoolean(R.styleable.AppCompatBEditText_tintAPI21, false);
        a.recycle();

        if (tintAPI21 || TintManager.SHOULD_BE_USED) {
            TintTypedArray ta = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                    TINT_ATTRS, defStyleAttr, 0);
            if (ta.hasValue(0)) {
                ColorStateList tint = applyWorkaroundForGetTintList(ta.getTintManager());
                if (tint != null) {
                    setSupportBackgroundTintList(tint);
                }
            }
            ta.recycle();
        }
    }

    @Override
    public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
        if (mBackgroundTint == null) {
            mBackgroundTint = new TintInfo();
        }
        mBackgroundTint.mTintList = tint;
        mBackgroundTint.mHasTintList = true;

        applySupportBackgroundTint();
    }

    @Override
    @Nullable
    public ColorStateList getSupportBackgroundTintList() {
        return mBackgroundTint != null ? mBackgroundTint.mTintList : null;
    }

    @Override
    public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        if (mBackgroundTint == null) {
            mBackgroundTint = new TintInfo();
        }
        mBackgroundTint.mTintMode = tintMode;
        mBackgroundTint.mHasTintMode = true;

        applySupportBackgroundTint();
    }

    @Override
    @Nullable
    public PorterDuff.Mode getSupportBackgroundTintMode() {
        return mBackgroundTint != null ? mBackgroundTint.mTintMode : null;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        applySupportBackgroundTint();
    }

    private void applySupportBackgroundTint() {
        if (getBackground() != null && mBackgroundTint != null) {
            TintManager.tintViewBackground(this, mBackgroundTint);
        }
    }


    /**
     * Contorno para a maior gambiarra já vista no Android: para obter este {@link ColorStateList}, ele verifica se o id do backround
     * da View é igual ao id background padrão do componente. Ou seja, se a View utiliza um background diferente, ele não identifica
     * qual View está chamando o método e consequentemente não retorna o ColorStateList...<br>
     * A correção simplesmente passa o id do background padrão do EditText para que ele retorne o ColorStateList dela.<br>
     * <br>
     * Este contorno não está no {@link AndroidBugsUtils} porque estas classes de AppCompat já são contornos, além de utilizarem parte da API interna
     * do Android. Desta forma, não seria interessante colocar algo tão específico no AndroidBugsUtils.
     *
     * @param tintManager portador da gambiarra.
     * @return o ColorStateList do EditText.
     */
    public static ColorStateList applyWorkaroundForGetTintList(TintManager tintManager) {
        return tintManager.getTintList(R.drawable.abc_edit_text_material);
    }
}
