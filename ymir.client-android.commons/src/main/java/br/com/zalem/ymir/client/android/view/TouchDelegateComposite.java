package br.com.zalem.ymir.client.android.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewStub;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link TouchDelegate} que permite o uso de qualquer número de {@link View Views}.<br>
 * O Android permite o uso de apenas um TouchDelegate por View pai e como o TouchDelegate original recebe apenas uma View filha, não é possível
 * utilizar a funcionalidade original com mais Views.
 *
 * @author Thiago Gesser
 */
public final class TouchDelegateComposite extends TouchDelegate {

    private static final Rect RECT_STUB = new Rect();

    private final List<TouchDelegate> delegates;

    public TouchDelegateComposite(Context context) {
        super(RECT_STUB, new ViewStub(context));
        delegates = new ArrayList<>();
    }

    /**
     * Adiciona o {@link TouchDelegate} de uma View.
     *
     * @param delegate o TouchDelegate.
     */
    public void addDelegate(TouchDelegate delegate) {
        delegates.add(delegate);
    }

    /**
     * Remove um {@link TouchDelegate} previamente adicionado.
     *
     * @param delegate o TouchDelegate.
     */
    public void removeDelegate(TouchDelegate delegate) {
        delegates.remove(delegate);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        boolean res = false;
        float x = event.getX();
        float y = event.getY();
        for (TouchDelegate delegate : delegates) {
            event.setLocation(x, y);
            res = delegate.onTouchEvent(event) || res;
        }
        return res;
    }
}
