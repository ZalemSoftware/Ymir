package br.com.zalem.ymir.client.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * SquareLayout é uma extensão de {@link android.widget.FrameLayout} que obrigatoriamente terá medidas quadradas, ou seja,
 * a largura e a altura sempre serão iguais. Funciona a partir de uma <code>medida base</code> (largura ou altura),
 * sendo que o valor correspondente da medida base será refletido para a outra medida no momento do <code>layout</code>.
 * A configuração da <code>medida base</code> pode ser feita através do atributo de xml <code>baseMeasure</code>
 * ou através do método {@link #setBaseMeasure(br.com.zalem.ymir.client.android.widget.SquareLayout.Measure)}.
 *
 * @author Thiago Gesser
 */
public final class SquareLayout extends FrameLayout {
	
	public enum Measure {WIDTH, HEIGHT}
	
	private Measure baseMeasure;
	private int minSize = Integer.MAX_VALUE;
	private boolean remeasureNeeded;
	
    public SquareLayout(Context context) {
        super(context);
    }
    
    public SquareLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SquareLayout);
        try {
        	int bmInt = a.getInteger(R.styleable.SquareLayout_baseMeasure, Measure.HEIGHT.ordinal());
        	if (bmInt < 0 || bmInt >= Measure.values().length) {
        		throw new IllegalArgumentException("Invalid baseMeasure: " + bmInt);
        	}
        	baseMeasure = Measure.values()[bmInt];
        } finally {
        	a.recycle();
        }
    }
    
    public void setBaseMeasure(Measure baseMeasure) {
    	if (baseMeasure == null) {
    		throw new NullPointerException("baseMeasure == null");
    	}
    	
		this.baseMeasure = baseMeasure;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//Utiliza a medida base configurada para determinar o tamanho do quadrado.
		int baseMeasureSize;
		int otherMeasureSize;
		switch (baseMeasure) {
			case WIDTH:
				baseMeasureSize = MeasureSpec.getSize(widthMeasureSpec);
				otherMeasureSize = MeasureSpec.getSize(heightMeasureSpec);
				break;

			case HEIGHT:
				baseMeasureSize = MeasureSpec.getSize(heightMeasureSpec);
				otherMeasureSize = MeasureSpec.getSize(widthMeasureSpec);
				break;

			default:
				throw new RuntimeException("Unsupported Measure: " + baseMeasure);
		}

		//Só é necessário remensurar se este layout tentar ocupar um espaço diferente do tamanho disponível para ele.
		remeasureNeeded = baseMeasureSize != otherMeasureSize;

		//TODO rever esta lógica. Ela foi necessária pq a aparição do teclado virtual na tela de detalhes (filtro) fazia com
		//que o layout do cabeçalho ficasse perdido.
		if (baseMeasureSize > 0 && baseMeasureSize < minSize) {
			minSize = baseMeasureSize;
		}

		super.onMeasure(MeasureSpec.makeMeasureSpec(minSize, MeasureSpec.EXACTLY),
						MeasureSpec.makeMeasureSpec(minSize, MeasureSpec.EXACTLY));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (remeasureNeeded) {
			View parent = (View) getParent();
			if (parent != null) {
				//Se o layout acabou ocupando mais do que o tamanho disponível para ficar poder ficar quadrado, é obrigado a
				//remensurar o pai, se não há o risto de uma View irmã ficará com o tamanho / posicionamento errado.
				int widthMeasureSpec = MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth(), MeasureSpec.EXACTLY);
				int heightMeasureSpec = MeasureSpec.makeMeasureSpec(parent.getMeasuredHeight(), MeasureSpec.EXACTLY);
				parent.measure(widthMeasureSpec, heightMeasureSpec);
			}
			remeasureNeeded = false;
		}

		super.onLayout(changed, left, top, right, bottom);
	}
}
