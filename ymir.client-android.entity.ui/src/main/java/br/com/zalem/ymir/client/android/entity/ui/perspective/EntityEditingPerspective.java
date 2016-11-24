package br.com.zalem.ymir.client.android.entity.ui.perspective;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.inject.Inject;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCrop.Options;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.data.RelationshipViolationException;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingFieldMapping;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingPermissions;
import br.com.zalem.ymir.client.android.entity.ui.configuration.editing.IEditingTab;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractEnumFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractEnumFieldEditor.OnListValuesListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor.OnValueChangeListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor.FieldEditorVisitorAdapter;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.BooleanFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DateFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DecimalFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.EnumAttributeEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.ImageFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.ImageFieldEditor.IImageFieldEditorListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.IntegerFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TextFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TimeFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.EnumRelationshipEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.MultipleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.MultipleRelationshipFieldEditor.IMultipleRelationshipFieldEditorListener;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.SingleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.SingleRelationshipFieldEditor.ISingleRelationshipFieldEditorListener;
import br.com.zalem.ymir.client.android.entity.ui.event.IEntityUIEventManager;
import br.com.zalem.ymir.client.android.entity.ui.fragment.AbstractEntityEditingFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityEditingFragment;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityEditingPagerFragment;
import br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.MaskManager;
import br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment;
import br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment.IConfirmationDialogListener;
import br.com.zalem.ymir.client.android.fragment.TaskFragment;
import br.com.zalem.ymir.client.android.fragment.TaskFragment.FragmentAsyncTask;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import br.com.zalem.ymir.client.android.perspective.Perspective;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils.createEntitiesDisplayList;
import static br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils.createMessage;
import static br.com.zalem.ymir.client.android.entity.ui.text.MessageUtils.getEntityDisplayName;

/**
 * Perspectiva de edição de registro de entidade.<br>
 * A configuração de edição da entidade é definida através do {@link IEditingConfig} que é obtido pelo {@link IEntityUIConfigManager}.
 * Através desta configuração são definidos os campos disponíveis para edição e quais ações são permitidas para cada tipo de registro.<br>
 * A edição dos campos é feita por editores ({@link AbstractFieldEditor}), que são dispostos pelo fragmento {@link EntityEditingFragment}
 * ou o {@link EntityEditingPagerFragment}, dependendo da configuração.<br>
 * <br>
 * O EntitySelectionPerspective utiliza a entidade definida através da categoria de seu {@link Intent}. Por este motivo,
 * deve ser definida exatamente uma categoria. A única action suportada é a {@link #ENTITY_EDITING_ACTION}.<br>
 * <br>
 * O registro que será editado pela perspectiva pode ser passado como o seguinte <code>extra</code> no Intent: {@link #EDITING_RECORD_EXTRA}.<br>
 * Se nenhum registro for passado desta forma, um novo será criado.<br>
 * Depois de editado, o registro pode ser salvo ou enviado através das ações na ActionBar. A ação de remoção/exclusão de registro
 * só estará disponível se o registro não tiver sido criado pela própria Perspectiva, ou seja, apenas se o registro tiver sido passado
 * como Extra.<br>
 * <br>
 * O EntitySelectionPerspective também pode ser aberto para um resultado. Desta forma, a ação de salvar dará lugar a ação de confirmar
 * e a ação de enviar não será disponibilizada. Existem os seguintes possíveis resultados para esta persepctiva:
 * <ul>
 * 	<li>{@link Perspective#RESULT_CODE_CANCELED}: se a perspectiva foi finalizada sem a confirmação (através do Back ou Up, por exemplo). Não haverá dado de resultado;</li>
 * 	<li>{@link Perspective#RESULT_CODE_OK}: se a perspectiva foi finalizada com ação de confirmação. O registro editado ({@link IEntityRecord}) será o dado de resultado;</li>
 * 	<li>{@link #RESULT_CODE_REMOVED}: se a perspectiva foi finalizada com a ação de remoção do registro. Não haverá dado de resultado.</li>
 * </ul>
 */
public class EntityEditingPerspective extends Perspective {
	
	/**
	 * Ação de edição de registro de entidade.
	 */
	public static final String ENTITY_EDITING_ACTION = EntityListDetailPerspective.class.getPackage().getName() + ".EDITING";
	
	/**
	 * Extra do tipo {@link Parcelable} que é o estado salvo do registro que será editado. O estado salvo pode ser obtido
	 * através do método {@link IEntityDAO#toSavedState(IEntityRecord)}. 
	 */
	public static final String EDITING_RECORD_EXTRA = "EDITING_RECORD_EXTRA";
	/**
	 * Extra do tipo {@link Parcelable} que é o estado salvo do registro mestre do registro que será editado. Este extra
	 * geralmente é utilizado em edições de composições, onde é necessário que o registro que faz parte da composição tenha
	 * acesso seu mestre (fonte da composição).
	 */
	public static final String MASTER_RECORD_EXTRA = "MASTER_RECORD_EXTRA";
    /**
     * Extra do tipo <code>boolean</code> que determina se a perspectiva deve habilitar a ação de remoção como um resultado da edição.<br>
     * Só é utilizado se a perspectiva foi aberta para um resultado.
     */
	public static final String ENABLE_REMOVE_RESULT_EXTRA = "ENABLE_REMOVE_RESULT_EXTRA";
    /**
     * Extra do tipo <code>boolean</code> que determina se a perspectiva deve habilitar a ação de resumir o registro sendo editado.<br>
     * Se definido, o valor deste extra sobrescreve o colocado nas configuração da entidade.
     */
	public static final String ENABLE_SUMMARIZE_EXTRA = "ENABLE_SUMMARIZE_EXTRA";

	/**
	 * Argumento do tipo <code>array de Strings</code> que define quais relacionamentos (detalhes) devem receber uma cópia do 
	 * registro atual (mestre) no momento de sua edição.<br>
	 * O registro mestre é enviado à Perspectiva de edição do registro detalhe através do extra {@link #MASTER_RECORD_EXTRA} no Intent.<br>
	 * <br>
	 * Apenas relacionamentos do tipo <code>composição</code> são suportados atualmente.
	 */
	public static final String PROVIDE_MASTER_ARGUMENT = "PROVIDE_MASTER";
	/**
	 * Argumento do tipo <code>String</code> que define qual relacionamento receberá o registro mestre vindo do extra
	 * {@link #MASTER_RECORD_EXTRA} do Intent.<br>
	 * O relacionamento deve real e não possuir nenhum editor apontando para ele.
	 */
	public static final String RECEIVE_MASTER_ARGUMENT = "RECEIVE_MASTER";
    /**
     * Argumento que habilita o uso do FAB (Floating Action Button) de salvamento na perspectiva, fazendo com que a ação não seja mostrada na App Bar.<br>
     * <br>
     * Não é necessário definir valor para o argumento, apenas sua presença já habilita o comportamento.
     */
    public static final String ENABLE_FAB_SAVE = "ENABLE_FAB_SAVE";
    /**
     * Argumento que habilita o uso do FAB de envio na perspectiva, fazendo com que a ação não seja mostrada na App Bar.<br>
     * <br>
     * Não é necessário definir valor para o argumento, apenas sua presença já habilita o comportamento.
     */
    public static final String ENABLE_FAB_SEND = "ENABLE_FAB_SEND";
    /**
     * Argumento que habilita o uso do FAB de confirmação na perspectiva, fazendo com que a ação não seja mostrada na App Bar.<br>
     * <br>
     * Não é necessário definir valor para o argumento, apenas sua presença já habilita o comportamento.
     */
    public static final String ENABLE_FAB_CONFIRM = "ENABLE_FAB_CONFIRM";


    /**
	 * Código de resultado para a remoção do registro.
	 */
	public static final int RESULT_CODE_REMOVED = 0;
	
	private static final String EDITING_FRAGMENT_TAG = "EDITING_FRAGMENT_TAG";

	@Inject
	private IEntityDataManager dataManager;
	
	@Inject
	private IEntityUIConfigManager configManager;
	
	@Inject(optional = true)
	private IEntityUIEventManager eventManager;

	protected AbstractEntityEditingFragment editingFragment;
	protected IEditingConfig editingConfig;

	private EntityEditingRecord editingRecord;
	private IEntityRecord originalRecord;
	private IEntityDAO entityDAO;
	private Menu menu;

	//Utilizado apenas se houver eventManager.
	private EntityEditingErrorHandler errorHandler;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.entity_editing_perspective, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		
		Intent intent = getIntent();
		//Verifica se é uma ação suportada.
		if (!ENTITY_EDITING_ACTION.equals(intent.getAction())) {
			throw new IllegalArgumentException("Unsupported action: " + intent.getAction());
		}
		
		/*
		 * Obtém as configurações da perspectiva.
		 */
		Set<String> categories = intent.getCategories();
		if (categories == null) {
			throw new IllegalArgumentException("No category (referring to the entity name) was defined.");
		}
		if (categories.size() > 1) {
			throw new IllegalArgumentException("Only one category (referring to the entity name) is allowed.");
		}
		String entityName = categories.iterator().next();
		entityDAO = dataManager.getEntityDAO(entityName);
		IEntityMetadata entityMetadata = entityDAO.getEntityMetadata();
		
		IEntityConfig entityConfig = configManager.getEntityConfig(entityName);
		if (entityConfig == null || entityConfig.getEditing() == null) {
			throw new IllegalArgumentException(String.format("Missing required configuration \"%s\" of entity %s.", "editing", entityName));
		}
		editingConfig = entityConfig.getEditing();
		
		/*
		 * Obtém o registro que será editado.
		 */
		Bundle extras = intent.getExtras();
		Parcelable recordSavedState = extras != null ? extras.getParcelable(EDITING_RECORD_EXTRA) : null;
		if (recordSavedState == null) {
			//Se não foi passado um registro já existente para editar, cria um novo.
			originalRecord = entityDAO.create();
		} else {
			//Recupera o registro passado no extra.
			originalRecord = entityDAO.fromSavedState(recordSavedState);
		}
		
		/*
		 * Atribui os fragmentos da perspectiva.
		 * Cria apenas na primeira vez pq depois o Android cria eles automaticamente (comportamento não documentado).
		 */
		FragmentManager fragmentManager = getChildFragmentManager();
		if (savedInstanceState == null) {
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			
			createFragments(fragmentTransaction, R.id.entity_editing_perspective_fragments_container);
			
			fragmentTransaction.commit();
		} else {
			//Como o Android possui um comportamento não documentado de restaurar automaticamente os fragmentos, apenas
			//obtém as instâncias já criadas por ele.
			restoreFragments(fragmentManager);
		}

		/*
		 * Inicializa os fragmentos da perspectiva.
		 */
		Context context = getActivity();
		//Utiliza um MaskManager comum na criação dos formatadores, evitando assim a criação desnecessária de máscaras iguais.
		MaskManager maskManager = new MaskManager(context);
		initializeFragments(context, entityMetadata, maskManager);

		//Ajusta os editores para funcionarem corretamente com a perspectiva.
		editingFragment.visitEditors(new FieldEditorConfigurationVisitor());

		/*
		 * Cria o registro especial de edição.
		 */
		createEditingRecord();

		/*
		 * Inicializa o tratador de erros, necessário apenas se houver um gerenciador de eventos.
		 */
		if (eventManager != null) {
            errorHandler = new EntityEditingErrorHandler(context, editingFragment, savedInstanceState);
		}

		/*
		 * Executa as ações finais que precisam ser feitas apenas na primeira vez.
		 */
		if (savedInstanceState == null) {
			checkProvideMasterArgument();
			
			//Carrega apenas aqui pq depois o próprio fragmento já guarda os valores.
			editingFragment.loadValues(originalRecord, true);
			
			if (eventManager != null) {
				eventManager.fireStartEditRecordEvent(editingRecord, errorHandler);
			}
		}

		//Dependendo de onde a perspectiva foi aberta, o onActivityCreated pode acontecer antes do onCreateOptionsMenu, então só pode configurar o menu quando ele estiver setado.
		if (menu != null) {
			configureMenuItems();
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		
        //Configura o listener de alteração dos editores. Coloca o listener apenas neste momento para que o carregamento dos valores nos
        //editores (inicial ou RECUPERAÇÃO) não seja considerado como evento de alteração.
        editingFragment.setFieldEditorsValueChangeListener(new FieldEditorValueChangeListener());
	}
	
	@Override
	public void onPerspectiveResult(Serializable requestKey, int resultCode, Object data) {
		boolean removed;
		switch (resultCode) {
			case RESULT_CODE_CANCELED:
				return;
			case RESULT_CODE_OK:
				removed = false;
				break;
			case RESULT_CODE_REMOVED:
				removed = true;
				break;
				
			default:
				throw new IllegalArgumentException("Unsupported resultCode: " + resultCode);
		}
		
		EntityRecordRequestKey recordRequestKey = (EntityRecordRequestKey) requestKey;
		IEntityRecord entityRecord = (IEntityRecord) data;

		//Executa a ação retornada no editor correto de acordo com o nome do relacionamento.
		boolean done = editingFragment.visitEditors(new EntityRecordSelectionVisitor(recordRequestKey, entityRecord, removed));
		if (!done) {
			throw new IllegalStateException("RelationshipFieldEditor not found for relationship: " + recordRequestKey.getFieldName());
		}
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        handleImageSelectionResult(requestCode, resultCode, data);
    }

    @Override
	public boolean hasUnfinishedWork() {
		//Se o registro foi removido, não á trabalho inacabado.
		if (originalRecord == null) {
			return false;
		}
		
		return editingFragment.isDirty();
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		
		if (isForResult()) {
			inflater.inflate(R.menu.entity_editing_perspective_for_result_actions, menu);
		} else {
			inflater.inflate(R.menu.entity_editing_perspective_not_for_result_actions, menu);
		}
		
		if (isSummarizeEnabled()) {
			inflater.inflate(R.menu.entity_editing_perspective_summarize_action, menu);
		}
		this.menu = menu;
		
		//Dependendo de onde a perspectiva foi aberta, o onCreateOptionsMenu pode acontecer antes do onActivityCreated, então só pode configurar o menu quando o reigstro estiver setado.
		if (originalRecord != null) {
			configureMenuItems();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Não pode usar switch-case porque a partir do ADT 14 os ids de bibliotecas são gerados como campos não finais. Mais informações em: http://tools.android.com/tips/non-constant-fields
		int itemId = item.getItemId();
		if (itemId == R.id.action_save) {
			//Armazena os valores dos editores no registro e salva-o local.
			saveRecord(false);
			return true;
		}
		if (itemId == R.id.action_send) {
			//Armazena os valores dos editores no registro e envia-o para a fonte de dados.
			saveRecord(true);			
			return true;
		}
		if (itemId == R.id.action_confirm) {
			//Armazena os valores dos editores no registro e retorna o registro.
			confirmRecordForResult();
			return true;
		}
		if (itemId == R.id.action_summarize) {
			Parcelable savedState = entityDAO.toSavedState(copyRecord());
			
			Intent intent = new Intent();
			intent.setAction(EntityDetailPerspective.ENTITY_DETAIL_ACTION);
			intent.addCategory(entityDAO.getEntityMetadata().getName());
			intent.putExtra(EntityDetailPerspective.RECORD_EXTRA, savedState);
			intent.putExtra(EntityDetailPerspective.DISABLE_EDITING_EXTRA, true);
			startPerspective(intent);
			return true;
		}
		if (itemId == R.id.action_delete) {
			if (isForResult()) {
				setResult(RESULT_CODE_REMOVED, null);
				originalRecord = null;
				finish();
			} else {
				//Pede a confirmação antes de excluir o registro.
                String message = createMessage(getActivity(), R.string.record_deletion_confirmation, getEntityDisplayName(configManager, entityDAO.getEntityMetadata().getName(), false));
				Bundle arguments = new Bundle();
				arguments.putString(ConfirmationDialogFragment.MESSAGE_STRING_ARGUMENT, message);
                arguments.putInt(ConfirmationDialogFragment.POSITIVE_BUTTON_ARGUMENT, R.string.record_deletion_confirmation_positive);

                ConfirmationDialogFragment confirmationFrag = new ConfirmationDialogFragment();
				confirmationFrag.setArguments(arguments);
				confirmationFrag.setListener(new RecordDeletionConfirmationDialogListener());
				confirmationFrag.show(getChildFragmentManager(), ConfirmationDialogFragment.CONFIRMATION_DIALOG_FRAGMENT_TAG);
			}
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}


    @Override
    public boolean hasFABs() {
        return isFABEnabled(ENABLE_FAB_SAVE) || isFABEnabled(ENABLE_FAB_SEND) || isFABEnabled(ENABLE_FAB_CONFIRM);
    }

    @Override
    public void onCreateFABs(YmirMenu fabMenu, YmirMenuInflater menuInflater) {
        super.onCreateFABs(fabMenu, menuInflater);

        menuInflater.inflate(R.xml.entity_editing_perspective_fabs, fabMenu);
    }

    @Override
    public boolean isFABAvailable(YmirMenuItem fabItem) {
        int itemId = fabItem.getId();
        if (itemId == R.id.action_save) {
            return !isForResult() && isFABEnabled(ENABLE_FAB_SAVE) && isSaveActionEnabled();
        }
        if (itemId == R.id.action_send) {
            return !isForResult() && isFABEnabled(ENABLE_FAB_SEND) && isSendActionEnabled();
        }
        if (itemId == R.id.action_confirm) {
            return isForResult() && isFABEnabled(ENABLE_FAB_CONFIRM);
        }

        return super.isFABAvailable(fabItem);
    }

    @Override
    public void onFABClicked(YmirMenuItem fabItem) {
        int itemId = fabItem.getId();
        if (itemId == R.id.action_save) {
            //Armazena os valores dos editores no registro e salva-o local.
            saveRecord(false);
            return;
        }
        if (itemId == R.id.action_send) {
            //Armazena os valores dos editores no registro e envia-o para a fonte de dados.
            saveRecord(true);
            return;
        }
        if (itemId == R.id.action_confirm) {
            //Armazena os valores dos editores no registro e retorna o registro.
            confirmRecordForResult();
            return;
        }

        super.onFABClicked(fabItem);
    }


    @Override
    public boolean onBackPressed() {
        //Se está salvando, não permite nenhuma ação do back.
        if (isSavingRecord()) {
            return true;
        }

        return super.onBackPressed();
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (errorHandler != null) {
            errorHandler.onSaveState(outState);
        }
        AndroidBugsUtils.applyWorkaroundForBug74222_onSaveInstanceState(this);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            AndroidBugsUtils.applyWorkaroundForBug74222_onRestoreInstanceState(this, savedInstanceState);
        }
    }

	
	/**
	 * Cria os fragmentos utilizados por esta perspectiva.
	 * 
	 * @param fragmentTransaction transação utilizado na criação dos fragmentos.
	 * @param containerViewId id do container dos fragmentos.
	 */
	protected void createFragments(FragmentTransaction fragmentTransaction, int containerViewId) {
		if (useEntityEditingPager(editingConfig)) {
			editingFragment = new EntityEditingPagerFragment();
		} else {
			editingFragment = new EntityEditingFragment();
		}
		
        if (hasFABs()) {
            editingFragment.setTheme(R.style.ThemeOverlay_Ymir_EntityFragment_FAB);
        }

		fragmentTransaction.add(containerViewId, editingFragment, EDITING_FRAGMENT_TAG);
	}
	
	/**
	 * Restaura os fragmentos criados anteriormente por esta perspectiva.
	 * 
	 * @param fragmentManager o gerenciador de fragmentos utilizado na criação dos fragmentos.
	 */
	protected void restoreFragments(FragmentManager fragmentManager) {
		editingFragment =  (AbstractEntityEditingFragment) fragmentManager.findFragmentByTag(EDITING_FRAGMENT_TAG);
		
		//Se o fragmento de confirmação estava aberto, recupera ele.
		ConfirmationDialogFragment confirmationFrag = (ConfirmationDialogFragment) fragmentManager.findFragmentByTag(ConfirmationDialogFragment.CONFIRMATION_DIALOG_FRAGMENT_TAG);
		if (confirmationFrag != null) {
            confirmationFrag.setListener(new RecordDeletionConfirmationDialogListener());
		}
	}
	
	/**
	 * Inicializa os fragmentos criados por esta perspectiva.
	 * 
	 * @param context contexto.
	 * @param entityMetadata metadados da entidade cujo o registro será editado.
	 * @param maskManager gerenciador de máscaras.
	 */
	protected void initializeFragments(Context context, IEntityMetadata entityMetadata, MaskManager maskManager) {
		if (useEntityEditingPager(editingConfig)) {
			EntityEditingPagerFragment frag = (EntityEditingPagerFragment) editingFragment;
			frag.initialize(context, entityMetadata, editingConfig.getTabs(), dataManager, configManager, maskManager);
		} else {
			EntityEditingFragment frag = (EntityEditingFragment) editingFragment;
			IEditingFieldMapping[] fieldMappings = editingConfig.getFields();
            String layout = editingConfig.getLayout();
            frag.initialize(context, entityMetadata, fieldMappings, dataManager, configManager, maskManager, layout);
		}
	}

	/**
	 * Cria um tipo de registro especial para edição que engloba o registro original e os editores, fazendo com que as operações de obtenção e
	 * alteração de valores do registro sejam feitos diretamente nos editores. A obtenção de valores de campos não editáveis é feita diretamente no
	 * registro englobado, mas a alteração destes não é permitida.<br>
	 * O registro criado pode ser obtido posteriormente através do método {@link #getRecord()}.
	 */
	protected void createEditingRecord() {
		editingRecord = new EntityEditingRecord(originalRecord);

		//Se o registro mestre foi enviado e a perspectiva está configurada para recebê-lo, coloca-o no editingRecord.
		receiveMasterRecord();

		//Atribui os editores ao registro especial de edição.
		FieldEditorMappingVisitor mappingVisitor = new FieldEditorMappingVisitor();
        editingFragment.visitEditors(mappingVisitor);
        editingRecord.setEditors(mappingVisitor.getAttributesEditors(), mappingVisitor.getRelationshipsEditors());
	}

    /**
     * Indica se a perspectiva deve ativar o recurso de mensagem automática de erro global a partir dos campos que contém erro atualmente.<br>
     * Se ativado, a mensagem se atualiza conforme os campos com erro se alteram.
     *
     * @param errorFields campos que contém erro atualmente.
     * @return <code>true</code> se a mensagem automática de erro global deve ser ativada e <code>false</code> caso contrário.
     */
    protected boolean useAutomaticGlobalError(SortedSet<String> errorFields) {
        return !errorHandler.hasManualGlobalError() && errorFields.size() != 1;
    }

	
	/**
     * Chamado a partir de uma ação nos editores para a criação do {@link Intent} que será utilizado para lançar a perspectiva de seleção de registros.
	 * 
	 * @param relationshipName o nome do relacionamento que requer a seleção de registros.
	 * @param targetEntity a entidade cujo seus registros serão selecionados.
	 * @return a Intent criada ou <code>null</code> para cancelar a ação.
	 */
	protected Intent onCreateRecordSelectionIntent(String relationshipName, IEntityMetadata targetEntity) {
		Intent intent = new Intent();
        intent.addCategory(targetEntity.getName());
		intent.setAction(EntitySelectionPerspective.ENTITY_SELECTION_ACTION);
		return intent;
	}
	
	/**
	 * Chamado a partir de uma ação nos editores para a criação do {@link Intent} que será utilizado para lançar a perspectiva de edição de registro.
	 *
	 * @param relationshipName o nome do relacionamento que requer a edição de registro.
	 * @param targetEntity a entidade cujo seu registro será criado/editado.
	 * @param record o registro que será editado ou <code>null</code> se um novo registro deve ser criado.
	 * @return a Intent criada ou <code>null</code> para cancelar a ação.
	 */
	protected Intent onCreateRecordEditingIntent(String relationshipName, IEntityMetadata targetEntity, IEntityRecord record) {
		String entityName = targetEntity.getName();
		Intent intent = new Intent();
        intent.addCategory(entityName);
        intent.setAction(ENTITY_EDITING_ACTION);
		
		if (record != null) {
			IEntityDAO dao = dataManager.getEntityDAO(entityName);
			Parcelable savedState = dao.toSavedState(record);
			intent.putExtra(EDITING_RECORD_EXTRA, savedState);
			intent.putExtra(ENABLE_REMOVE_RESULT_EXTRA, true);
		}
		
		if (provideMasterRecordTo(relationshipName)) {
			IEntityRecord thisRecord = copyRecord();
			
			//Se este registro possui um mestre, aplica-o na cópia para que ele seja acessável.
			IEntityRecord masterRecord = editingRecord.getMasterRecord();
			if (masterRecord != null) {
				thisRecord.setRelationshipValue(editingRecord.getMasterRelationship(), masterRecord);
			}
			
			intent.putExtra(MASTER_RECORD_EXTRA, entityDAO.toSavedState(thisRecord));
		}
		
		return intent;
	}
	
	/**
	 * Chamado a partir de uma ação nos editores para a criação do {@link Intent} que será utilizado para lançar a perspectiva de detalhamento de
     * registro.
	 * 
	 * @param record o registro que será detalhado.
	 * @return a Intent criada ou <code>null</code> para cancelar a ação.
	 */
	protected Intent onCreateRecordDetailingIntent(IEntityRecord record) {
		String entityName = record.getEntityMetadata().getName();
		IEntityDAO dao = dataManager.getEntityDAO(entityName);

		Intent intent = new Intent();
		intent.setAction(EntityDetailPerspective.ENTITY_DETAIL_ACTION);
		intent.addCategory(entityName);

		boolean useRecordInstance = record.isNew() || record.isDirty();
		if (useRecordInstance) {
			Parcelable recordSavedState = dao.toSavedState(record);
			intent.putExtra(EntityDetailPerspective.RECORD_EXTRA, recordSavedState);
		} else {
			intent.putExtra(EntityDetailPerspective.RECORD_ID_EXTRA, record.getId());
		}

		return intent;
	}

    /**
     * Chamado quando o valor de um editor de atributo é alterado.
     *
     * @param editor editor que teve o valor alterado.
     */
    protected void onAttributeEditorValueChanged(AbstractFieldEditor<?> editor) {
        if (eventManager == null) {
            return;
        }

        eventManager.fireEditRecordAttributeEvent(editingRecord, editor.getFieldName(), errorHandler);
    }

    /**
     * Chamado quando o valor de um editor de relacionamento é alterado.
     *
     * @param editor editor que teve o valor alterado.
     */
    protected void onRelationshipEditorValueChanged(AbstractFieldEditor<?> editor) {
        if (eventManager == null) {
            return;
        }

        eventManager.fireEditRecordRelationshipEvent(editingRecord, editor.getFieldName(), errorHandler);
    }

	
	/**
	 * Obtém o registro que está sendo editado por esta perspectiva.<br>
	 * Seus dados estarão sempre atualizados com os definidos nos editores e alterações feitas neste registro refletirão
	 * nos próprios editores. Apenas os campos que possuem editores definidos podem ser alterados desta forma.
	 * 
	 * @return o registro sendo editado.
	 */
	public final IEntityRecord getRecord() {
		return editingRecord;
	}

	/**
	 * Obtém o registro original que contém os dados iniciais sendo editados.<br>
	 * <br>
	 * <b>IMPORTANTE:</b> Não é recomendado alterar este registro pois as mudanças podem afetar outras perspectivas, mesmo sem salvar. Por exemplo,
	 * se outra perspectiva está usando a mesma referência do registro original, ela será impactada por qualquer alteração feita nele.
	 *
	 * @return o registro original.
	 */
	public IEntityRecord getOriginalRecord() {
		return originalRecord;
	}

	public final IEntityDataManager getDataManager() {
		return dataManager;
	}
	
	public final void setDataManager(IEntityDataManager entityManager) {
		this.dataManager = entityManager;
	}
	
	public final IEntityUIConfigManager getConfigManager() {
		return configManager;
	}
	
	public final void setConfigManager(IEntityUIConfigManager configManager) {
		this.configManager = configManager;
	}

    public IEntityUIEventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(IEntityUIEventManager eventManager) {
        this.eventManager = eventManager;
    }


    /*
	 * Métodos auxiliares
	 */
	
	@SuppressLint("Assert")
	private void saveRecord(boolean sync) {
		assert !isForResult();
        if (isSavingRecord()) {
            return;
        }
		
		if (eventManager != null) {
			boolean cancel = eventManager.fireBeforeSaveRecordEvent(editingRecord, sync, errorHandler);
			if (cancel) {
                onCancelSubmit();
				return;
			}
		}

		editingRecord.enableInnerChanges();
		if (eventManager != null) {
			eventManager.fireSaveRecordEvent(editingRecord, sync);
		}
		editingRecord.disableInnerChanges();

        editingFragment.storeValues(originalRecord, true);

        //Inicia o salvamento do registro em background enquanto exibe um Progress.
        startRecordSaving(sync);
	}

    private void startRecordSaving(boolean sync) {
        TaskFragment<IEntityRecord, Void, Boolean> taskFragment = new TaskFragment<>();
        String entityDisplayName = MessageUtils.getEntityDisplayName(configManager, entityDAO.getEntityMetadata().getName(), false);
        int msgFormatResId = sync ? R.string.record_saving_sync_format : R.string.record_saving_format;
        String message = createMessage(getActivity(), msgFormatResId, entityDisplayName);
        taskFragment.setProgressMessage(message);

        RecordSavingTask recordSavingTask = new RecordSavingTask(entityDAO, sync);
        taskFragment.startTask(recordSavingTask, getActivity(), getChildFragmentManager(), originalRecord);
    }

    private boolean isSavingRecord() {
        return TaskFragment.isExecutingTask(getChildFragmentManager());
    }

    private void afterSaveRecord(boolean sync, boolean successful) {
        Context context = getActivity();
        int msgFormatResId = successful ? (sync ? R.string.record_saved_sync_format : R.string.record_saved_format) : R.string.record_saved_error_format;
        String message = createMessage(context, msgFormatResId, getEntityDisplayName(configManager, entityDAO.getEntityMetadata().getName(), false));
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        finish();

        if (eventManager != null) {
            eventManager.fireAfterSaveRecordEvent(editingRecord, sync);
        }
    }

    private void onCancelSubmit() {
        if (errorHandler == null) {
            return;
        }

        //Scrolla até o primeiro campo com erro, se houver.
        SortedSet<String> errorFields = errorHandler.getErrorFields();
        if (errorFields.size() > 0) {
            String firstErrorField = errorFields.first();
            editingFragment.scrollToEditor(firstErrorField);

            //Se for necessário, destaca o erro dentro do editor.
            AbstractFieldEditor<?> errorEditor = editingFragment.findEditor(firstErrorField);
            errorEditor.accept(new HighlightEditorErrorVisitor());
        }

        //Se não possui nenhum erro global definido, utiliza a mensagem automática que é atualizada conforme os erros são adicionadas/removidas.
        //Se há apenas um campo com erro, nem vale a pena mostrar a mensagem pois o usuário já será direcionado ao campo. Entretanto, se há zero campos com erro, mostra uma mensagem padrão.
        if (useAutomaticGlobalError(errorFields)) {
            errorHandler.setAutomaticGlobalError();
        }
    }

	@SuppressLint("Assert")
	private void confirmRecordForResult() {
		assert isForResult();
		
		if (eventManager != null) {
			boolean cancel = eventManager.fireBeforeConfirmEditRecordEvent(editingRecord, errorHandler);
			if (cancel) {
                onCancelSubmit();
				return;
			}
		}

		editingRecord.enableInnerChanges();
		if (eventManager != null) {
			eventManager.fireConfirmEditRecordEvent(editingRecord);
        }
        editingRecord.disableInnerChanges();

		editingFragment.storeValues(originalRecord, true);
        setResult(RESULT_CODE_OK, originalRecord);
        finish();
		
		if (eventManager != null) {
			eventManager.fireAfterConfirmEditRecordEvent(editingRecord);
		}
	}
	
	private static boolean useEntityEditingPager(IEditingConfig editingConfig) {
		IEditingTab[] tabs = editingConfig.getTabs();
		return tabs != null && tabs.length > 0;
	}
	
	private void configureMenuItems() {
		//Configura a visibilidade da ação de remoção/exclusão de acordo com o objetivo da perspectiva.
		MenuItem removeMenuItem = menu.findItem(R.id.action_delete);
		boolean enableRemoveAction = false;
		if (isForResult()) {
			enableRemoveAction = getIntent().getBooleanExtra(ENABLE_REMOVE_RESULT_EXTRA, false);

            //Configura a visibilidade da ação de confirmar.
            MenuItem confirmMenuItem = menu.findItem(R.id.action_confirm);
            boolean enableConfirmAction = !isFABEnabled(ENABLE_FAB_CONFIRM);
            confirmMenuItem.setVisible(enableConfirmAction).setEnabled(enableConfirmAction);
		} else {
			if (!originalRecord.isNew()) {
				IEditingPermissions permissions;
				if (originalRecord.isLocal()) {
					permissions = editingConfig.getLocalPermissions();
				} else {
					permissions = editingConfig.getDataSourcePermissions();
				}
				enableRemoveAction = permissions != null && permissions.canDelete();
			}

            //Configura a visibilidade da ação de salvar.
            MenuItem saveMenuItem = menu.findItem(R.id.action_save);
            if (saveMenuItem != null) {
                boolean enableSaveAction = !isFABEnabled(ENABLE_FAB_SAVE) && isSaveActionEnabled();
                saveMenuItem.setVisible(enableSaveAction).setEnabled(enableSaveAction);
            }

            //Configura a visibilidade da ação de enviar.
            MenuItem sendMenuItem = menu.findItem(R.id.action_send);
            if (sendMenuItem != null) {
                boolean enableSendAction = !isFABEnabled(ENABLE_FAB_SEND) && isSendActionEnabled();
                sendMenuItem.setVisible(enableSendAction).setEnabled(enableSendAction);
            }
		}

		removeMenuItem.setVisible(enableRemoveAction).setEnabled(enableRemoveAction);
	}

    private boolean isSaveActionEnabled() {
        //Só pode salvar local (criar/editar) se o canCreate/canUpdate local estiver liberado.
        IEditingPermissions localPermissions = editingConfig.getLocalPermissions();
        return localPermissions != null && (originalRecord.isNew() ? localPermissions.canCreate() : localPermissions.canUpdate());
    }

    private boolean isSendActionEnabled() {
        IEditingPermissions dataSourcePermissions = editingConfig.getDataSourcePermissions();
        //Só pode enviar pra fonte de dados (criar/editar) se o canCreate/canUpdate do dataSource estiver liberado.
        return dataSourcePermissions != null && (originalRecord.isLocal() ? dataSourcePermissions.canCreate() : dataSourcePermissions.canUpdate());
    }

	private IEntityRecord copyRecord() {
		IEntityRecord copy = entityDAO.copy(originalRecord);

		//Armazena os valores dos editores no registro (sem tirar a sujeira).
		editingFragment.storeValues(copy, false);
		
		return copy;
	}
	
	private boolean isSummarizeEnabled() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(ENABLE_SUMMARIZE_EXTRA)) {
            return extras.getBoolean(ENABLE_SUMMARIZE_EXTRA);
        }

        return editingConfig.isEnableSummarize();
	}

	private boolean provideMasterRecordTo(String composition) {
		String[] provideMasterComps = getProvideMasterArgument();
		if (provideMasterComps == null) {
			return false;
		}
		
		for (String provideMasterComp : provideMasterComps) {
			if (provideMasterComp.equals(composition)) {
				return true;
			}
		}
		return false;
	}
	
	private String[] getProvideMasterArgument() {
		Bundle arguments = getArguments();
		if (arguments == null) {
			return null;
		}
		
		return arguments.getStringArray(PROVIDE_MASTER_ARGUMENT);
	}
	
	private void checkProvideMasterArgument() {
		String[] provideMasterComps = getProvideMasterArgument();
		if (provideMasterComps == null) {
			return;
		}
		
		IEntityMetadata entityMetadata = editingRecord.getEntityMetadata();
		for (String provideMasterComp : provideMasterComps) {
			IEntityRelationship relationship = entityMetadata.getRelationship(provideMasterComp);
			if (!MetadataUtils.isComposition(relationship)) { 
				throw new IllegalArgumentException(String.format("The argument %s contains a relationship that is not a composition: %s.", PROVIDE_MASTER_ARGUMENT, provideMasterComp));
			}
		}
	}
	
	private void receiveMasterRecord() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}
		
		Bundle arguments = getArguments();
		if (arguments == null) {
			return;
		}
		
		String masterRelationshipName = arguments.getString(RECEIVE_MASTER_ARGUMENT);
		if (masterRelationshipName == null) {
			return;
		}
			
		Parcelable savedMasterRecord = extras.getParcelable(MASTER_RECORD_EXTRA);
		if (savedMasterRecord == null) {
			return;
		}
		
		IEntityMetadata recordMetadata = originalRecord.getEntityMetadata();
		IEntityRelationship masterRelationship = recordMetadata.getRelationship(masterRelationshipName);
		IEntityDAO masterDAO = dataManager.getEntityDAO(masterRelationship.getTarget().getName());
		IEntityRecord masterRecord = masterDAO.fromSavedState(savedMasterRecord);
		
		editingRecord.setMasterRecord(masterRecord, masterRelationshipName);
	}
	
	private void deleteRecord() {
        Context context = getActivity();
        if (eventManager != null) {
			boolean cancel = eventManager.fireBeforeDeleteRecordEvent(originalRecord);
			if (cancel) {
				Toast.makeText(context, R.string.deletion_denied, Toast.LENGTH_SHORT).show();
				return;
			}
		}

        String entityDisplayName = getEntityDisplayName(configManager, entityDAO.getEntityMetadata().getName(), false);
        try {
            entityDAO.delete(originalRecord, true);

            String message = createMessage(context, R.string.record_deleted_format, entityDisplayName);
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            if (eventManager != null) {
                eventManager.fireAfterDeleteRecordEvent(originalRecord);
            }

            originalRecord = null;
            finish();
        } catch (RelationshipViolationException e) {
            String message = createMessage(context, R.string.record_deletion_relationship_violation_format,
                                           createEntitiesDisplayList(context, configManager, true, e.getSourceEntities()), entityDisplayName);
            new AlertDialog.Builder(context).
                setTitle(R.string.operation_denied).
                setMessage(message).
                setPositiveButton(android.R.string.ok, null).
            show();
        }
	}

    private boolean isFABEnabled(String argument) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            return arguments.containsKey(argument);
        }

        return false;
    }


    /*
     * Métodos para a definição de imagens em editores deste tipo de campo. O processo é dividido em duas partes:
     *     - a primeira utiliza o EasyImage para permitir que o usuário selecione uma foto da galeria ou tire uma foto com a câmera.
     *     - a segunda utiliza o uCrop para que o usuário escolha qual parte da imagem deseja definir no campo.
     *
     * OBS: Diferentemente do mecanismo de Perspectivas, o mecanismo de Activities não permite passar dados complexos (serializáveis)
     * como código de requisição para a abertura de Activities para resultado.
     * Desta forma, não há como identificar o editor do campo que está requisitando a imagem sem depender do salvamento de um estado
     * (como um atributo nesta perspectiva ou um preference, por exemplo). Isto não é o ideal pois a identificação do editor é um "parâmetro"
     * desta ação e não um "estado", o que pode gerar confusão quanto aos reais estados da perspectiva.
     * Por isto, foi adotado uma forma de identificar o editor através de seu índice na lista de editores do fragmento de edição,
     * passando este índice no código da requisição.
     * Como o codigo da requisição é um int, ele possui 4 bytes. 2 deles já são utilizados pelo FragmentActivity para identificar
     * o fragmento que está chamando uma Activity para o resultado, restando apens 2 bytes para utilizar aqui. O índice do
     * editor é introduzido como o primeiro byte, deixando o outro byte para o código de requisição real.
     * O único limitador para esta técnica é que o índice do editor não pode ser maior do que 255 (o valor máximo de 1 byte),
     */

    private void startImageSelectionActivity(ImageFieldEditor editor) {
        int editorIndex = editingFragment.getEditors().indexOf(editor);
        if (editorIndex > 255) {
            throw new PendingFeatureException("Image editor in a position greater than 255");
        }

        EasyImage.openChooserWithGallery(EntityEditingPerspective.this, null, editorIndex);
    }

    private void handleImageSelectionResult(int requestCode, int resultCode, Intent data) {
        final Activity context = getActivity();

        //Tratamento da segunda parte da seleção de imagem: cortar a imagem selecionada.
        if (resultCode == Activity.RESULT_OK && (requestCode & 0xff) == UCrop.REQUEST_CROP) {
            //Obtém o bitmap da imagem.
            Uri output = UCrop.getOutput(data);
            if (output == null) {
                handleImageSelectionError("Problem obtaining the result output of uCrop.", null);
                return;
            }
            String imagePath = output.getPath();
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                handleImageSelectionError("Could not load the bitmap of the following file:  " + imagePath, null);
                return;
            }

            //Seta o bitmap no editor do campo.
            int editorIndex = requestCode >> 8;
            AbstractFieldEditor<?> editor = editingFragment.getEditors().get(editorIndex);
            if (editor instanceof ImageFieldEditor) {
                ImageFieldEditor imageEditor = (ImageFieldEditor) editor;
                imageEditor.setValue(bitmap);
            } else {
                handleImageSelectionError("ImageFieldEditor not found for the index: " + editorIndex, null);
            }

            //Deleta o arquivo temporário com a imagem.
            if (!new File(imagePath).delete()) {
                Log.w(EntityEditingPerspective.class.getSimpleName(), "Problem deleting image temp file: " + imagePath);
            }
            return;
        }

        //Tratamento da primeira parte da seleção de imagem: escolher a imagem da galera ou tirar foto com a câmera.
        EasyImage.handleActivityResult(requestCode, resultCode, data, context, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int editorIndex) {
                handleImageSelectionError(String.format(Locale.US, "EasyImage.onImagePickerError, source: %s, type: %d. ", source, editorIndex), e);
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int editorIndex) {
                int requestCode = (editorIndex << 8) + (UCrop.REQUEST_CROP & 0xff);

                Options options = new Options();
                options.setOvalDimmedLayer(true);
                options.setShowCropFrame(false);
                options.setShowCropGrid(false);
                options.setCompressionFormat(CompressFormat.PNG);

                UCrop.of(Uri.fromFile(imageFile), Uri.fromFile(imageFile)).
                        withOptions(options).
                        withAspectRatio(1, 1).
                        withMaxResultSize(196, 196).
                        start(context, EntityEditingPerspective.this, requestCode);
            }
        });
    }

    private void handleImageSelectionError(String msg, Throwable e) {
        msg = "Image picking error. " + msg;
        if (e == null) {
            Log.e(EntityEditingPerspective.class.getSimpleName(), msg);
        } else {
            Log.e(EntityEditingPerspective.class.getSimpleName(), msg, e);
        }

        new AlertDialog.Builder(getActivity()).
            setTitle(R.string.entity_editing_image_selection_error).
            setMessage(R.string.entity_editing_image_selection_error_msg).
            setPositiveButton(android.R.string.ok, null).
        show();
    }


	/*
	 * Classes auxliares
	 */
	
	/**
	 * Visitador utilizado para aplicar configuraçoes específicas da perspectiva nos editores.
	 */
	private final class FieldEditorConfigurationVisitor extends FieldEditorVisitorAdapter { 
		
		//Só cria os listeners se forem necessário.
		private ImageFieldEditoorListener imageEditorListener;
		private RelationshipFieldEditorListener relationshipEditorListener;
		private EnumFieldEditorValuesListener enumEditorValuesListener;

        @Override
        public boolean visit(ImageFieldEditor editor) {
            editor.setEditorListener(getImageFieldListener());
            return false;
        }

        @Override
		public boolean visit(SingleRelationshipFieldEditor editor) {
			editor.setEditorListener(getRelationshipFieldListener());
            return false;
		}
		
		@Override
		public boolean visit(MultipleRelationshipFieldEditor editor) {
			editor.setEditorListener(getRelationshipFieldListener());
			return false;
		}
		
		@Override
		public boolean visit(EnumAttributeEditor editor) {
			trySetEnumEditorValuesListener(editor);
            return false;
		}

		@Override
		public boolean visit(EnumRelationshipEditor editor) {
			trySetEnumEditorValuesListener(editor);
            return false;
		}

		
		/*
		 * Métodos auxiliares 
		 */

        private ImageFieldEditoorListener getImageFieldListener() {
            if (imageEditorListener == null) {
                imageEditorListener = new ImageFieldEditoorListener();
            }
            return imageEditorListener;
        }

		@SuppressWarnings("unchecked")
		public void trySetEnumEditorValuesListener(AbstractEnumFieldEditor editor) {
			if (eventManager == null) {
				return;
			}

			if (enumEditorValuesListener == null) {
				enumEditorValuesListener = new EnumFieldEditorValuesListener();
			}

			editor.setOnListValuesListener(enumEditorValuesListener);
        }

        private RelationshipFieldEditorListener getRelationshipFieldListener() {
			if (relationshipEditorListener == null) {
				relationshipEditorListener = new RelationshipFieldEditorListener();
			}
			return relationshipEditorListener;
		}
	}

    /**
     * Listener das ações dos editores de imagens.<br>
     * Consome a ação de seleção de imagem, deixando que a perspective trate-a através do {@link #startActivityForResult(Intent, int)}.
     */
    private final class ImageFieldEditoorListener implements IImageFieldEditorListener {

        @Override
        public boolean onImageSelection(ImageFieldEditor editor) {
            startImageSelectionActivity(editor);
            return true;
        }
    }

    /**
	 * Listener das ações dos editores de relacionamento utilizados na perspectiva.<br>
	 * Consome todas as ações que envolvem a seleção, criação, edição e detalhamento dos registros com a abertura de 
	 * perspectivas que tratam estas ações.
	 */
	private final class RelationshipFieldEditorListener implements ISingleRelationshipFieldEditorListener, IMultipleRelationshipFieldEditorListener {
		
		/*
		 * Ações dos editores de relacionamentos singulares.
		 */

		@Override
		public boolean onRecordSelection(SingleRelationshipFieldEditor editor) {
			startRecordSelectionPerspective(editor.getFieldName(), editor.getRelationshipEntity());
			return true;
		}

		@Override
		public boolean onRecordCreation(SingleRelationshipFieldEditor editor) {
			startRecordEditingPerspective(editor.getFieldName(), editor.getRelationshipEntity());
			return true;
		}

		@Override
		public boolean onRecordEditing(SingleRelationshipFieldEditor editor) {
			startRecordEditingPerspective(editor.getFieldName(), editor.getRelationshipEntity(), editor.getValue(), null);
			return true;
		}

		@Override
		public boolean onRecordDetailing(SingleRelationshipFieldEditor editor) {
			startRecordDetailingPerspective(editor.getValue());
			return true;
		}

		@Override
		public boolean onRecordRemoval(SingleRelationshipFieldEditor editor) {
			//Deixa o próprio editor cuidar da remoção.
			return false;
		}

		
		/*
		 * Ações dos editores de relacionamentos múltiplos.
		 */
		
		@Override
		public boolean onRecordSelection(MultipleRelationshipFieldEditor editor) {
			startRecordSelectionPerspective(editor.getFieldName(), editor.getRelationshipEntity());
			return true;
		}

		@Override
		public boolean onRecordCreation(MultipleRelationshipFieldEditor editor) {
			startRecordEditingPerspective(editor.getFieldName(), editor.getRelationshipEntity());
			return true;
		}

		@Override
		public boolean onRecordEditing(MultipleRelationshipFieldEditor editor, int position) {
			startRecordEditingPerspective(editor.getFieldName(), editor.getRelationshipEntity(), editor.getRecord(position), position);
			return true; 
		}

		@Override
		public boolean onRecordDetailing(MultipleRelationshipFieldEditor editor, int position) {
			startRecordDetailingPerspective(editor.getRecord(position));
			return true;
		}

		@Override
		public boolean onRecordRemoval(MultipleRelationshipFieldEditor editor, int position) {
			//Deixa o próprio editor cuidar da remoção.
			return false;
		}
		
		
		/*
		 * Métodos auxiliares
		 */
		
		private void startRecordSelectionPerspective(String relationshipName, IEntityMetadata entity) {
			Intent intent = onCreateRecordSelectionIntent(relationshipName, entity);
			if (intent == null) {
				return;
			}

			EntityRecordRequestKey requestKey = new EntityRecordRequestKey(relationshipName);
			startPerspectiveForResult(intent, requestKey);
        }

        private void startRecordEditingPerspective(String relationshipName, IEntityMetadata entity) {
			startRecordEditingPerspective(relationshipName, entity, null, null);
		}
		
		private void startRecordEditingPerspective(String relationshipName, IEntityMetadata entity, IEntityRecord record, Integer position) {
			Intent intent = onCreateRecordEditingIntent(relationshipName, entity, record);
			if (intent == null) {
				return;
			}

			EntityRecordRequestKey requestKey = new EntityRecordRequestKey(relationshipName, position);
			startPerspectiveForResult(intent, requestKey);
		}

		private void startRecordDetailingPerspective(IEntityRecord record) {
			Intent intent = onCreateRecordDetailingIntent(record);
			if (intent == null) {
				return;
			}
			startPerspective(intent);
		}
	}
	
	
	/**
	 * Chave de requisição utilizada na abertura de perspectivas para um resultado pelo {@link RelationshipFieldEditorListener}. 
	 */
	private static final class EntityRecordRequestKey implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private final String fieldName;
		private final Integer position;

		public EntityRecordRequestKey(String relationshipName) {
			this(relationshipName, null);
		}
		
		public EntityRecordRequestKey(String fieldName, Integer position) {
			this.fieldName = fieldName;
			this.position = position;
		}
		
		public String getFieldName() {
			return fieldName;
		}
		
		public Integer getPosition() {
			return position;
		}
	}
	
	/**
	 * Visitador utilizado para aplicar o resultado das ações de seleção, criação e edição consumidas pelo {@link RelationshipFieldEditorListener}.
	 */
	private static final class EntityRecordSelectionVisitor extends FieldEditorVisitorAdapter {
		
		private final IEntityRecord record;
		private final EntityRecordRequestKey requestKey;
		private final boolean removed;

		private EntityRecordSelectionVisitor(EntityRecordRequestKey requestKey, IEntityRecord record, boolean removed) {
			this.requestKey = requestKey;
			this.record = record;
			this.removed = removed;
		}
		
		@Override
		public boolean visit(SingleRelationshipFieldEditor editor) {
			if (!editor.getFieldName().equals(requestKey.getFieldName())) {
				return false;
			}
			
			if (removed) {
				editor.setValue(null);
			} else {
				editor.setValue(record);
			}
			return true;
		}
		
		@Override
		public boolean visit(MultipleRelationshipFieldEditor editor) {
			if (!editor.getFieldName().equals(requestKey.getFieldName())) {
				return false;
			}
			
			Integer position = requestKey.getPosition();
			if (removed) {
				editor.removeRecord(position);
			} else {
				if (position == null) {
					editor.addRecord(record);
				} else {
					editor.replaceRecord(position, record);
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Visitador que coloca os editores separados em mapas de atributos e relacionamentos, de acordo com o seu tipo.<br>
	 * É utilizado para configurar o registro de edição para os eventos do {@link IEntityUIEventManager}. Desta forma,
	 * só é usado se houver um IEntityUIEventManager configurado.
	 */
	private static final class FieldEditorMappingVisitor implements IFieldEditorVisitor {
		
		private final Map<String, AbstractFieldEditor<?>> attributesEditors;
		private final Map<String, AbstractFieldEditor<?>> relationshipsEditors;

		public FieldEditorMappingVisitor() {
			this(new HashMap<String, AbstractFieldEditor<?>>(), new HashMap<String, AbstractFieldEditor<?>>());
		}
		
		public FieldEditorMappingVisitor(Map<String, AbstractFieldEditor<?>> attributesEditors, Map<String, AbstractFieldEditor<?>> relationshipsEditors) {
			this.attributesEditors = attributesEditors;
			this.relationshipsEditors = relationshipsEditors;
		}

		@Override
		public boolean visit(IntegerFieldEditor editor) {
			mapAttributeEditor(editor);
			return false;
		}

		@Override
		public boolean visit(DecimalFieldEditor editor) {
			mapAttributeEditor(editor);
			return false;
		}

		@Override
		public boolean visit(TextFieldEditor editor) {
			mapAttributeEditor(editor);
			return false;
		}

		@Override
		public boolean visit(BooleanFieldEditor editor) {
			mapAttributeEditor(editor);
            return false;
		}

		@Override
		public boolean visit(DateFieldEditor editor) {
			mapAttributeEditor(editor);
            return false;
		}

		@Override
		public boolean visit(TimeFieldEditor editor) {
			mapAttributeEditor(editor);
            return false;
		}

		@Override
		public boolean visit(ImageFieldEditor editor) {
			mapAttributeEditor(editor);
            return false;
		}

		@Override
		public boolean visit(EnumAttributeEditor editor) {
			mapAttributeEditor(editor);
            return false;
		}

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
			mapRelationshipEditor(editor);
            return false;
        }

        @Override
		public boolean visit(SingleRelationshipFieldEditor editor) {
			mapRelationshipEditor(editor);
            return false;
		}

		@Override
		public boolean visit(MultipleRelationshipFieldEditor editor) {
			mapRelationshipEditor(editor);
            return false;
		}
		
		public Map<String, AbstractFieldEditor<?>> getAttributesEditors() {
			return attributesEditors;
		}
		
		public Map<String, AbstractFieldEditor<?>> getRelationshipsEditors() {
			return relationshipsEditors;
		}
		
		
		private void mapAttributeEditor(AbstractFieldEditor<?> editor) {
			if (attributesEditors.put(editor.getFieldName(), editor) != null) {
				throw new IllegalArgumentException("There are two or more editors pointing to the same attribute: " + editor.getFieldName());
			}
		}
		
		private void mapRelationshipEditor(AbstractFieldEditor<?> editor) {
			if (relationshipsEditors.put(editor.getFieldName(), editor) != null) {
				throw new IllegalArgumentException("There are two or more editors pointing to the same relationship: " + editor.getFieldName());
			}
		}
	}

    /**
     * Visitador que destaca partes importantes de determiandos editores com erros.
     */
    private static class HighlightEditorErrorVisitor extends FieldEditorVisitorAdapter {

        @Override
        public boolean visit(MultipleRelationshipFieldEditor editor) {
            if (!editor.hasRecordErrors()) {
                return false;
            }

            List<IEntityRecord> records = editor.getValue();
            if (records == null) {
                return false;
            }

            //Scrolla para o primeiro registro com erro.
            for (int i = 0; i < records.size(); i++) {
                IEntityRecord record = records.get(i);
                if (editor.hasRecordError(record)) {
                    editor.scrollToRecord(i);
                    break;
                }
            }
            return false;
        }
    }
	
	/**
	 * Listener de alteração dos valores dos editores que dispara o evento de edição no {@link IEntityUIEventManager} de acordo com o tipo do editor.<br>
	 * Só é utilizado se houver um IEntityUIEventManager configurado.
	 */
	private final class FieldEditorValueChangeListener implements OnValueChangeListener, IFieldEditorVisitor {

		@Override
		public void onValueChange(AbstractFieldEditor<?> editor) {
			editor.accept(this);
		}

		@Override
		public boolean visit(IntegerFieldEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(DecimalFieldEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(TextFieldEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(BooleanFieldEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(DateFieldEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(TimeFieldEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(ImageFieldEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(EnumAttributeEditor editor) {
            onAttributeEditorValueChanged(editor);
			return false;
		}

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
            onRelationshipEditorValueChanged(editor);
            return false;
        }

        @Override
		public boolean visit(SingleRelationshipFieldEditor editor) {
            onRelationshipEditorValueChanged(editor);
			return false;
		}

		@Override
		public boolean visit(MultipleRelationshipFieldEditor editor) {
            onRelationshipEditorValueChanged(editor);
            if (errorHandler != null) {
                errorHandler.notifyMultipleRelationshipEditorValueChanged(editor);
            }
            return false;
		}
	}
	
	/**
	 * Listener de listagem dos valores das enumerações que dispara o evento no {@link IEntityUIEventManager}.<br>
	 * Só é utilizado se houver um IEntityUIEventManager configurado.
	 */
	private final class EnumFieldEditorValuesListener implements OnListValuesListener {
		@Override
		public List<?> beforeListValues(AbstractEnumFieldEditor editor) {
			return eventManager.fireBeforeListEnumValuesEvent(editingRecord, editor.getFieldName(), editor.getDefaultValues(), errorHandler);
		}

        @Override
		public void afterListValues(AbstractEnumFieldEditor editor, List values) {
			eventManager.fireAfterListEnumValuesEvent(editingRecord, editor.getFieldName(), values, errorHandler);
		}
    }


    /**
     * Listener do dialogo de confirmação, que exclui o registro de fato se o usuário assim decidiu.
     */
    private final class RecordDeletionConfirmationDialogListener implements IConfirmationDialogListener {

        @Override
        public void onConfirm(ConfirmationDialogFragment fragment) {
            deleteRecord();
        }

        @Override
        public void onCancel(ConfirmationDialogFragment fragment) {
        }
    }


    /**
     * Task de salvamento de registro.<br>
     * Após a finalização, o método {@link EntityEditingPerspective#afterSaveRecord(boolean, boolean)} é chamado.
     */
    private static final class RecordSavingTask extends FragmentAsyncTask<IEntityRecord, Void, Boolean> {

        private final IEntityDAO entityDAO;
        private final boolean sync;

        public RecordSavingTask(IEntityDAO entityDAO, boolean sync) {
            this.entityDAO = entityDAO;
            this.sync = sync;
        }

        @Override
        protected Boolean safeDoInBackground(IEntityRecord... record) {
            return entityDAO.save(record[0], sync);
        }

        @Override
        protected void saferOnPostExecute(Boolean successful) {
            EntityEditingPerspective perspective = (EntityEditingPerspective) getFragment().getParentFragment();
            perspective.afterSaveRecord(sync, successful);
        }
    }
}
