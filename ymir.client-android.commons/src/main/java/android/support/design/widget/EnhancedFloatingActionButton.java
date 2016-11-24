package android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Versão aprimorada do {@link FloatingActionButton} que dispõe uma forma mais funcional para esconder e mostrar o FAB.<br>
 * <br>
 * Os métodos {@link #hide()} e {@link #show()} podem ser chamados mesmo enquanto o FAB ainda está sendo escondido/mostrado,
 * pois depois do término da animação o último estado definido será mantido. Por exemplo, se o FAB está sendo escondido e o metodo {@link #show()}
 * é chamado, o FAB voltará a ser mostrado depois de terminar de ser escondido (com a animaçao também).<br>
 * Se o estado final for igual ao atual, nada será feito. Por exemplo, se o FAB estiver sendo escondido e o {@link #show()} e {@link #hide()}
 * forem chamados em sequência, nada acontecerá depois que o FAB terminar de ser escondido pois o último estado já estará correto.<br>
 * <br>
 * Alem disso, se o {@link #setImageDrawable(Drawable)} ou o {@link #setBackgroundTintList(ColorStateList)} forem chamados durante o processo
 * de esconder/mostrar, seus valores serão armazenados para serem definidos apenas após o término da animação. Desta forma, evita-se piscadas
 * indesejadas no FAB.<br>
 * <br>
 * <br>
 * A implementação das animações de esconder/mostrar foram obtidas de {@link FloatingActionButtonHoneycombMr1}.
 */
public class EnhancedFloatingActionButton extends FloatingActionButton {

    private boolean hiding;
    private boolean showing;
    private Boolean showAfterAnimation;

    private Drawable pendingImageDrawable;
    private ColorStateList pendingBackgroundTintList;

    public EnhancedFloatingActionButton(Context context) {
        super(context, null);
    }

    public EnhancedFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnhancedFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void hide() {
        //Se já está escondendo, apenas garante que não será mostrado após a conclusão.
        if (hiding) {
            showAfterAnimation = null;
            return;
        }
        //Se está tentando esconder durante a animação de mostrar, sinaliza para que isto seja feito apenas após a conclusão.
        if (showing) {
            showAfterAnimation = false;
            return;
        }
        if (getVisibility() != View.VISIBLE) {
            return;
        }

        if (!ViewCompat.isLaidOut(this) || isInEditMode()) {
            setVisibility(View.GONE);
            return;
        }

        hiding = true;
        animate().
            scaleX(0f).
            scaleY(0f).
            alpha(0f).
            setDuration(FloatingActionButtonImpl.SHOW_HIDE_ANIM_DURATION).
            setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).
            setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    hiding = false;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    hiding = false;
                    setVisibility(View.GONE);
                    trySetPendingValues();

                    //Se mandou mostrar enquanto estava escondendo, começa a mostrar só agora.
                    if (showAfterAnimation != null && showAfterAnimation) {
                        showAfterAnimation = null;
                        show();
                    }
                }
            });
    }

    @Override
    public void show() {
        //Se já está mostrando, apenas garante que não será escondido após a conclusão.
        if (showing) {
            showAfterAnimation = null;
            return;
        }
        //Se está tentando mostrar durante a animação de esconder, sinaliza para que isto seja feito apenas após a conclusão.
        if (hiding) {
            showAfterAnimation = true;
            return;
        }
        if (getVisibility() == View.VISIBLE) {
            return;
        }

        if (!ViewCompat.isLaidOut(this) || isInEditMode()) {
            setVisibility(View.VISIBLE);
            setAlpha(1f);
            setScaleY(1f);
            setScaleX(1f);
            return;
        }

        showing = true;
        setAlpha(0f);
        setScaleY(0f);
        setScaleX(0f);
        animate().
            scaleX(1f).
            scaleY(1f).
            alpha(1f).
            setDuration(FloatingActionButtonImpl.SHOW_HIDE_ANIM_DURATION).
            setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR).
            setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    showing = false;

                    //Se mandou esconder enquanto estava mostrando, começa a esconder só agora.
                    if (showAfterAnimation != null && !showAfterAnimation) {
                        showAfterAnimation = null;
                        hide();
                    } else {
                        //Só seta os valores pendentes se não vai esconder novamente, evitando piscadas.
                        trySetPendingValues();
                    }
                }
            });
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        //Se está animando, deixa para definir o drawable apenas após a conclusão para evitar efeitos visuais estranhos.
        if (showing || hiding) {
            pendingImageDrawable = drawable;
            return;
        }

        super.setImageDrawable(drawable);
    }

    @Override
    public void setBackgroundTintList(ColorStateList tint) {
        //Se está animando, deixa para definir o tint apenas após a conclusão para evitar efeitos visuais estranhos.
        if (showing || hiding) {
            pendingBackgroundTintList = tint;
            return;
        }

        super.setBackgroundTintList(tint);
    }


    /*
     * Métodos auxiliares
     */

    private void trySetPendingValues() {
        if (pendingImageDrawable != null) {
            setImageDrawable(pendingImageDrawable);
            pendingImageDrawable = null;
        }

        if (pendingBackgroundTintList != null) {
            setBackgroundTintList(pendingBackgroundTintList);
            pendingBackgroundTintList = null;
        }
    }
}
