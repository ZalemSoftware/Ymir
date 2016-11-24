package android.support.v7.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.support.v4.view.TintableBackgroundView;
import android.support.v7.internal.widget.TintContextWrapper;
import android.support.v7.internal.widget.TintInfo;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.internal.widget.TintTypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * LinearLayout cujo as cores são influenciadas pelo <code>tint</code>.
 * <br>
 * Criado para suprir a necessidade que o Android ainda não atende. Baseado inteiramente no {@link AppCompatEditText}.
 */
public class AppCompatLinearLayout extends LinearLayout implements TintableBackgroundView {

    private static final int[] TINT_ATTRS = {
        android.R.attr.background
    };

    private TintInfo mBackgroundTint;

    public AppCompatLinearLayout(Context context) {
        this(context, null);
    }

    public AppCompatLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppCompatLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AppCompatLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(TintContextWrapper.wrap(context), attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppCompatLinearLayout, defStyleAttr, defStyleRes);
        boolean tintAPI21 = a.getBoolean(R.styleable.AppCompatLinearLayout_tintAPI21, false);
        a.recycle();

        if (tintAPI21 || TintManager.SHOULD_BE_USED) {
            TintTypedArray ta = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                    TINT_ATTRS, defStyleAttr, 0);
            if (ta.hasValue(0)) {
                ColorStateList tint = AppCompatBEditText.applyWorkaroundForGetTintList(ta.getTintManager());
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
}
