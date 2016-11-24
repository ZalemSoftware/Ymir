package br.com.zalem.ymir.client.android.entity.ui.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.BuildConfig;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.view.list.ITabbedListDisplayConfig;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityDetailFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityDetailPagerFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityListFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityListPagerFragment;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import br.com.zalem.ymir.client.android.perspective.Perspective;

/**
 * Perspectiva que dispõe uma lista de registros de uma entidade para a seleção de um deles. Desta forma, esta perspectia
 * pode apenas ser aberta para um resultado. Existem os seguintes possíveis resultados:<br>
 * <ul>
 * 	<li>{@link Perspective#RESULT_CODE_CANCELED}: se a perspectiva foi finalizada sem a seleção do registro (através do Back ou Up, por exemplo). Não haverá dado de resultado;</li>
 * 	<li>{@link Perspective#RESULT_CODE_OK}: ao selecionar um registro clicando nele ou no botão de confirmação no detalhamento. O registro selecionado ({@link IEntityRecord}) será o dado de resultado.</li>
 * </ul>
 * O detalhamento do registro pode ser visto clicando em sua ação de detalhes, localizado no canto superior direito.<br>
 * <br>
 * O EntitySelectionPerspective utiliza a entidade definida através da categoria de seu {@link Intent}. Por este motivo,
 * deve ser definida exatamente uma categoria. A única action suportada é a {@link #ENTITY_SELECTION_ACTION}.<br>
 * <br>
 * Para a lista de registros é utilizado o fragmento {@link EntityListFragment} ou o {@link EntityListPagerFragment} e 
 * para os detalhes de um registro o fragmento {@link EntityDetailFragment} ou o {@link EntityDetailPagerFragment},
 * dependendo da configuração de lista e detalhes definida para a entidade.<br>
 * 
 * @author Thiago Gesser
 */
public class EntitySelectionPerspective extends EntityListDetailPerspective {

	/**
	 * Ação de seleção de um registro de entidade.
	 */
	public static final String ENTITY_SELECTION_ACTION = EntityListDetailPerspective.class.getPackage().getName() + ".SELECTION";
	/**
	 * Ações suportadas pela perspectiva de seleção.
	 */
	public static final String[] SUPPORTED_ACTIONS = new String[] {ENTITY_SELECTION_ACTION};

    /**
     * Argumento que habilita o uso do FAB (Floating Action Button) de seleção na perspectiva, fazendo com que a ação não seja mostrada na App Bar.<br>
     * <br>
     * Não é necessário definir valor para o argumento, apenas sua presença já habilita o comportamento.
     */
    public static final String ENABLE_FAB_SELECT = "ENABLE_FAB_SELECT";


	@Override
	protected int getInitialMode(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			return MODE_LIST;
		}
		
		return savedInstanceState.getInt(SAVED_MODE);
	}
	
	@Override
	protected String[] getSupportedActions() {
		return SUPPORTED_ACTIONS;
	}

    @Override
    protected void configurePerspective(Bundle extras, IEntityDAO entityDAO) {
        super.configurePerspective(extras, entityDAO);

        if (isSelectFABEnabled()) {
            detailFragment.setTheme(R.style.ThemeOverlay_Ymir_EntityFragment_FAB);
        }
    }

    @Override
	protected ITabbedListDisplayConfig getListDisplayConfig() {
		ITabbedListDisplayConfig selectionConfig = entityConfig.getSelection();
		if (selectionConfig != null) {
			return selectionConfig;
		}

		return super.getListDisplayConfig();
	}

	@Override
    public void onEntityRecordClick(IEntityRecord entityRecord) {
        onRecordSelected(entityRecord);
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.entity_selection_perspective_actions, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean enableSelectAction = !isSelectFABEnabled() && isSelectActionEnabled();
		MenuItem selectMenuItem = menu.findItem(R.id.action_select);
		selectMenuItem.setVisible(enableSelectAction).setEnabled(enableSelectAction);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_select) {
            selectDetailRecord();
            return true;
		}
		
		return super.onOptionsItemSelected(item);
	}


    @Override
    public boolean hasFABs() {
        return super.hasFABs() || isSelectFABEnabled();
    }

    @Override
    public void onCreateFABs(YmirMenu fabMenu, YmirMenuInflater menuInflater) {
        super.onCreateFABs(fabMenu, menuInflater);

        menuInflater.inflate(R.xml.entity_selection_perspective_fabs, fabMenu);
    }

    @Override
    public boolean isFABAvailable(YmirMenuItem fabItem) {
        if (fabItem.getId() == R.id.action_select) {
            return isSelectFABEnabled() && isSelectActionEnabled();
        }

        return super.isFABAvailable(fabItem);
    }

    @Override
    public void onFABClicked(YmirMenuItem fabItem) {
        if (fabItem.getId() == R.id.action_select) {
            selectDetailRecord();
            return;
        }

        super.onFABClicked(fabItem);
    }


    @Override
    public void onCreateRecordActionMenu(YmirMenu menu, YmirMenuInflater menuInflater) {
        menuInflater.inflate(R.xml.entity_selection_perspective_record_actions, menu);
    }

    @Override
    public void onRecordActionItemSelected(IEntityRecord record, YmirMenuItem item) {
        if (item.getId() == R.id.record_action_detail) {
            showDetail(record);
        }
    }

    @Override
    public boolean isRecordActionItemAvailable(IEntityRecord record, YmirMenuItem item) {
        return item.getId() == R.id.record_action_detail;
    }


	/**
	 * Chamado quando um registro é selecionado, seja na lista ou no detalhe.
	 *
	 * @param selectedRecord o registro selecionado.
	 */
	protected void onRecordSelected(IEntityRecord selectedRecord) {
		setResult(RESULT_CODE_OK, selectedRecord);
		finish();
	}


    /*
     * Métodos auxiliares
     */

    private boolean isSelectFABEnabled() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            return arguments.containsKey(ENABLE_FAB_SELECT);
        }

        return false;
    }

    private boolean isSelectActionEnabled() {
        return getMode() == MODE_DETAIL && detailFragment.getEntityRecord() != null;
    }

    private void selectDetailRecord() {
        IEntityRecord entityRecord = detailFragment.getEntityRecord();
        //Esta ação só pode estar disponível se estiver no modo detalhe e houver registro.
        if (BuildConfig.DEBUG && !(getMode() == MODE_DETAIL && entityRecord != null)) {
            throw new AssertionError();
        }

        //Se o registro está modificado, pega uma versão limpa dele.
        if (entityRecord.isDirty()) {
            if (!entityDAO.refresh(entityRecord)) {
                //Se o registro n existe mais, simplesmente volta pro modo de lista.
                Log.w(getClass().getSimpleName(), String.format("The entity record referenced by the detail mode no longer exists. Id = %s, entity = %s.", entityRecord.getId(), entityDAO.getEntityMetadata().getName()));

                showList();
                return;
            }
        }

        onRecordSelected(entityRecord);
    }
}
