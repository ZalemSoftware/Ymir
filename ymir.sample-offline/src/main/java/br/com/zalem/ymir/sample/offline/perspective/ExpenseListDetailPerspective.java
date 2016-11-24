package br.com.zalem.ymir.sample.offline.perspective;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.configuration.BasicMaskType;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityListFragment;
import br.com.zalem.ymir.client.android.entity.ui.perspective.EntityListDetailPerspective;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IDecimalMask;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.sample.offline.EntityConstants;
import br.com.zalem.ymir.sample.offline.R;

/**
 * Perspectiva customizada de lista/detalhe para a entidade <code>Gasto</code>.<br>
 * Adiciona um totalizador na parte de baixo da perspectiva.
 *
 * @author Thiago Gesser
 */
public final class ExpenseListDetailPerspective extends EntityListDetailPerspective {

    private IDecimalMask totalMask;
    private ViewGroup totalBar;
    private TextView totalValueView;

    @Override
    protected void initializeFragments(MaskManager maskManager) {
        super.initializeFragments(maskManager);

        totalMask = (IDecimalMask) maskManager.getBasicMask(BasicMaskType.CURRENCY_DEFAULT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Utiliza um layout customizado para a perspectiva, que contém a barra de total embaixo.
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.expense_list_detail_perspective, container, false);
        totalBar = (ViewGroup) view.findViewById(R.id.expense_list_total);
        totalValueView = (TextView) totalBar.findViewById(R.id.expense_list_total_value);

        //Coloca o layout normal da perspectiva no meio do novo layout customizado.
        View listDetailView = super.onCreateView(inflater, container, savedInstanceState);
        view.addView(listDetailView, 0, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1));
        return view;
    }

    @Override
    protected void showList() {
        super.showList();

        totalBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void showDetail(IEntityRecord record) {
        super.showDetail(record);

        totalBar.setVisibility(View.GONE);
    }


    @Override
    public void afterRecordsChange(EntityListFragment listFragment, List<IEntityRecord> expenses) {
        super.afterRecordsChange(listFragment, expenses);

        //Calcula o total no início e sempre que os registros são filtrados.
        calculateTotal(expenses);
    }

    @Override
    protected void afterDeleteRecord(IEntityRecord record) {
        super.afterDeleteRecord(record);

        //Recalcula quando um registro é excluído.
        EntityListFragment fragment = (EntityListFragment) listFragment;
        calculateTotal(fragment.getRecords());
    }


    /*
     * Métodos auxiliares
     */

    private void calculateTotal(List<IEntityRecord> expenses) {
        double total = 0;
        for (IEntityRecord expense : expenses) {
            double value = expense.getDecimalValue(EntityConstants.EXPENSE_ATTRIBUTE_VALUE); //Value nunca deve ser nulo.
            total += value;
        }

        CharSequence formattedTotal = totalMask.formatDecimal(total);
        totalValueView.setText(formattedTotal);
    }
}
