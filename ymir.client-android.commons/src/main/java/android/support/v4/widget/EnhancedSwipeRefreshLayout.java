package android.support.v4.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import br.com.zalem.ymir.client.android.commons.R;


/**
 * Versão aprimorada do {@link android.support.v4.widget.SwipeRefreshLayout} cujo as cores da barra de refresh são configuráveis através
 * de estilos (tema ou layout) e permite que o swipe funcione para mais de um filho.
 *
 * @author Thiago Gesser
 */
public final class EnhancedSwipeRefreshLayout extends SwipeRefreshLayout {

	private final int color1;
	private final int color2;
	private final int color3;
	private final int color4;
    private View[] swipeableViews;

	public EnhancedSwipeRefreshLayout(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.swipeRefreshLayoutStyle);
	}

	public EnhancedSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EnhancedSwipeRefreshLayout, defStyle, 0);
        //Os valores padrão estão de acordo com o SwipeProgressBar.
    	color1 = a.getColor(R.styleable.EnhancedSwipeRefreshLayout_color1, 0xB3000000);
    	color2 = a.getColor(R.styleable.EnhancedSwipeRefreshLayout_color2, 0x80000000);
    	color3 = a.getColor(R.styleable.EnhancedSwipeRefreshLayout_color3, 0x4d000000);
    	color4 = a.getColor(R.styleable.EnhancedSwipeRefreshLayout_color4, 0x1a000000);
    	a.recycle();
	}

    /**
     * Define as Views que podem sofrer o swipe, sendo que apenas uma delas será o alvo de cada vez. Para isto, a primeira View que estiver
     * visível será utilizada, permitindo que o alvo seja alternado através da alteração de visibilidade. Para garantir o funcionamento
     * correto, as Views definidas desta forma devem ser scrolláveis.<br>
     * Apenas Views que estão dentro da hierarquia do EnhancedSwipeRefreshLayout podem ser utilizadas desta forma. Isto é recomendado para
     * quando a View filha do EnhancedSwipeRefreshLayout é customizada e vários elemntos podem sofrer swipe. Por exemplo, uma lista de registros
     * que ao ficar vazia mostra outra View.
     *
     * @param swipeableViews Views que fazem parte da hierarquia do EnhancedSwipeRefreshLayout e podem sofrer swipe.
     */
    public void setSwipeableViews(View... swipeableViews) {
        this.swipeableViews = swipeableViews;
    }

    @Override
    public boolean canChildScrollUp() {
        if (swipeableViews != null) {
            for (View view : swipeableViews) {
                if (view.getVisibility() == View.VISIBLE) {
                    return ViewCompat.canScrollVertically(view, -1);
                }
            }
        }
        return super.canChildScrollUp();
    }

    @Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		setColorSchemeColors(color1, color2, color3, color4);
	}

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return AndroidBugsUtils.applyWorkaroundForBug163954(e);
        }
    }
}
