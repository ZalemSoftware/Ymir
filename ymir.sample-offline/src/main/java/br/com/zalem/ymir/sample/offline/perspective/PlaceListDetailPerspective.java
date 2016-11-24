package br.com.zalem.ymir.sample.offline.perspective;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.perspective.EntityEditingPerspective;
import br.com.zalem.ymir.client.android.entity.ui.perspective.EntityListDetailPerspective;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import br.com.zalem.ymir.sample.offline.R;

import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_ENTITY;
import static br.com.zalem.ymir.sample.offline.EntityConstants.EXPENSE_RELATIONSHIP_PLACE;

/**
 * Perspectiva customizada de lista/detalhe para a entidade <code>Local</code>.<br>
 * Adiciona uma ação de criar um gasto específico para um local tanto na lista quanto no detalhe.
 *
 * @author Thiago Gesser
 */
public final class PlaceListDetailPerspective extends EntityListDetailPerspective {

    /*
     * Métodos da ação de criar um novo gasto para o local a partir de um registro da lista.
     */

    @Override
    public void onCreateRecordActionMenu(YmirMenu menu, YmirMenuInflater menuInflater) {
        menuInflater.inflate(R.xml.place_record_actions, menu);

        super.onCreateRecordActionMenu(menu, menuInflater);
    }

    @Override
    public boolean isRecordActionItemAvailable(IEntityRecord record, YmirMenuItem item) {
        if (item.getId() == R.id.action_create_expense) {
            return true;
        }

        return super.isRecordActionItemAvailable(record, item);
    }

    @Override
    public void onRecordActionItemSelected(IEntityRecord record, YmirMenuItem item) {
        if (item.getId() == R.id.action_create_expense) {
            createNewExpenseFor(record);
            return;
        }

        super.onRecordActionItemSelected(record, item);
    }


    /*
     * Métodos da ação de criar um novo gasto para o local a partir dos detalhes.
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.place_detail_actions, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean enableCreateExpenseAction = getMode() == MODE_DETAIL && detailFragment.getEntityRecord() != null;
        MenuItem createExpenseMenuItem = menu.findItem(R.id.action_create_expense);
        createExpenseMenuItem.setVisible(enableCreateExpenseAction).setEnabled(enableCreateExpenseAction);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create_expense) {
            IEntityRecord place = detailFragment.getEntityRecord();
            createNewExpenseFor(place);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /*
     * Métodos auxiliares
     */

    private void createNewExpenseFor(IEntityRecord place) {
        //Cria um Gasto com o Local já configurado.
        IEntityDAO expenseDAO = getDataManager().getEntityDAO(EXPENSE_ENTITY);
        IEntityRecord expense = expenseDAO.create();
        expense.setRelationshipValue(EXPENSE_RELATIONSHIP_PLACE, place);

        //Abre a perspectiva de edição do Gasto criado.
        Intent intent = new Intent();
        intent.addCategory(EXPENSE_ENTITY);
        intent.setAction(EntityEditingPerspective.ENTITY_EDITING_ACTION);
        intent.putExtra(EntityEditingPerspective.EDITING_RECORD_EXTRA, expenseDAO.toSavedState(expense));
        startPerspective(intent);
    }
}
