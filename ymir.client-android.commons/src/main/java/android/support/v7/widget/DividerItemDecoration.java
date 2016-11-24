package android.support.v7.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * Decorador de itens de {@link RecyclerView} que coloca disivores entre eles.<br>
 * Se possuir apenas uma coluna, coloca só divisores verticais, caso contrário, horizontais também.
 *
 * @author Thiago Gesser
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private final Drawable divider;
    private final int numberOfColumns;

    public DividerItemDecoration(Drawable divider) {
        this(divider, 1);
    }

    public DividerItemDecoration(Drawable divider, int numberOfColumns) {
        if (numberOfColumns <= 0) {
            throw new IllegalArgumentException("numberOfColumns <= 0");
        }

        this.divider = divider;
        this.numberOfColumns = numberOfColumns;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, State state) {
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final int row = i / numberOfColumns;
            final int col = i % numberOfColumns;
            final View child = parent.getChildAt(i);

            //Vertical
            if (row > 0) {
                final int bottom = child.getTop();
                final int top = bottom - divider.getIntrinsicHeight();
                final int right = child.getRight() + divider.getIntrinsicWidth(); //Necessário para que as duas linhas se encontrem.
                divider.setBounds(child.getLeft(), top, right, bottom);
                divider.draw(c);
            }

            //Horizontal
            if (col > 0) {
                final int right = child.getLeft();
                final int left = right - divider.getIntrinsicWidth();
                divider.setBounds(left, child.getTop(), right, child.getBottom());
                divider.draw(c);
            }
        }
    }

    @Override
    //Utiliza a versão deprecada pois já disponibiliza o itemPosition.
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        int row = itemPosition / numberOfColumns;
        int col = itemPosition % numberOfColumns;

        int top = row > 0 ? divider.getIntrinsicHeight() : 0;
        int left = col > 0 ? divider.getIntrinsicWidth() : 0;
        outRect.set(left, top, 0, 0);
    }
}