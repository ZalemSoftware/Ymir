package android.support.v7.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * Versão aprimorada da {@link RecyclerView}. Disponibiliza funcionalidades que, apesar de básicas para um {@link ListView}, ainda não são
 * suportadas pela RecyclerView.<br>
 * Permite a definição de uma <code>Empty View</code> através do metodo {@link #setEmptyView(View)} para ser mostrada quando não existem
 * itens. A implementação desta parte foi obtida <a href="http://stackoverflow.com/a/27801394">daqui</a>.
 * Além disso, é possível definir os seguintes atributos no xml de layout do RecyclerView:
 * <ul>
 *     <li><b>numColumns</b>: se defindo, aplica um {@link LinearLayoutManager} caso o valor seja 1 ou um {@link GridLayoutManager}
 *     caso o valor seja maior do que 1.</li>
 *     <li><b>divider</b>: se definido, aplica um {@link DividerItemDecoration} que coloca um divisor entre os itens do RecyclerView.</li>
 *     <li><b>fixedSize</b>: indica se todas as Views dos itens do RecyclerView possuem o mesmo tamanho.</li>
 * </ul>
 *
 * @author Thiago Gesser
 */
public class EnhancedRecyclerView extends RecyclerView {

    private View emptyView;
    private final AdapterDataObserver emptinessObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public EnhancedRecyclerView(Context context) {
        this(context, null);
    }

    public EnhancedRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EnhancedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EnhancedRecyclerView, 0, defStyle);
        boolean fixedSize = a.getBoolean(R.styleable.EnhancedRecyclerView_fixedSize, false);
        int numColumns = a.getInteger(R.styleable.EnhancedRecyclerView_numColumns, 0);
        Drawable divider = a.getDrawable(R.styleable.EnhancedRecyclerView_divider);
        a.recycle();

        setHasFixedSize(fixedSize);
        if (numColumns == 1) {
            setLayoutManager(new LinearLayoutManager(context));
        } else if (numColumns > 1) {
            setLayoutManager(new GridLayoutManager(context, numColumns));
        }

        if (divider != null) {
            addItemDecoration(new DividerItemDecoration(divider, numColumns > 1 ? numColumns : 1));
        }
    }

    void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(emptinessObserver);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(emptinessObserver);
        }

        checkIfEmpty();
    }

    /**
     * Define uma {@link View} que será mostrada quando o adapter estiver vazio.
     *
     * @param emptyView a View.
     */
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }
}
