package br.com.zalem.ymir.client.android.perspective;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.AndroidBugsUtils;
import android.support.v4.app.Fragment.SavedState;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.DrawerLayout.SimpleDrawerListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Stack;

import br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment;
import br.com.zalem.ymir.client.android.fragment.ConfirmationDialogFragment.IConfirmationDialogListener;
import br.com.zalem.ymir.client.android.menu.YmirMenu;
import br.com.zalem.ymir.client.android.menu.YmirMenuInflater;
import br.com.zalem.ymir.client.android.menu.YmirMenuItem;
import br.com.zalem.ymir.client.android.perspective.PerspectiveInfo.IntentFilter;
import br.com.zalem.ymir.client.android.perspective.PerspectiveInfo.LaunchMode;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;
import br.com.zalem.ymir.client.android.util.Utils;
import roboguice.activity.RoboActionBarActivity;

/**
 * Activity de perspectivas do Ymir. Atua como um gerenciador de perspectivas, as quais são disponibilizadas ao usuário
 * através do menu de navegação lateral.<br>
 * As perspectivas são definidas através de um recurso XML que deve ser referenciado por um meta-data com o nome
 * {@link #PERSPECTIVES_METADATA} na declaração da Activity (AndroidManifest.xml). As regras de formatação do XML podem
 * ser vistas em {@link PerspectiveInfoInflater}.<br> 
 * Os itens do menu são definidos através de um recurso XML que deve ser referenciado por um meta-data com o nome
 * {@link #NAVIGATION_MENU_METADATA}. As regras de formatação do XML podem ser vistas em {@link YmirMenuInflater}.<br>
 * 
 * @see Perspective
 *
 * @author Thiago Gesser
 */
public class PerspectiveActivity extends RoboActionBarActivity implements IPerspectiveManager {

	private static final String PERSPECTIVES_METADATA = PerspectiveActivity.class.getPackage().getName() + ".perspectives";
	private static final String NAVIGATION_MENU_METADATA = PerspectiveActivity.class.getPackage().getName() + ".navigation-menu";
	
	private static final String SAVED_CURRENT_PERSPECTIVE_ID = "SAVED_CURRENT_PERSPECTIVE_ID";
	private static final String SAVED_PERSPECTIVES_INSTANCES = "SAVED_PERSPECTIVES_INSTANCES";
	private static final String SAVED_PERSPECTIVES_BACKSTACK = "SAVED_PERSPECTIVES_BACKSTACK";
	private static final String SAVED_SELECTED_MENU_ITEM = "SAVED_SELECTED_MENU_ITEM";

	protected ListView navList;
	private NavigationMenuListAdapter navListAdapter;

	private DrawerLayout navDrawerLayout;
    private YmirMenu navMenu;
	private NavigationMenuToggle navToggle;

	private Toolbar toolbar;
    private FloatingActionButton fab;
    private YmirMenu fabMenu;

    private Perspective currentPerspective;
	private SparseArray<PerspectiveInstance> perspectivesInstances;
	private Stack<Integer> perspectivesBackStack;
	private PerspectiveInfo[] perspectivesInfos;
	private BitSet usedInstancesIds;
    private boolean commitAllowingStateLoss;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidBugsUtils.applyWorkaroundForSearchViewIconVsLogoBug(this);
		
		ActivityInfo activityInfo = getActivityInfo();

		//Carrega as Perspectivas a partir do xml configurado como metadata da Activity.
		perspectivesInfos = loadPerspectives(activityInfo);
		
		//Carrega o Menu de Navegação a partir do xml configurado como metadata da Activity.
        navMenu = loadNavigationMenu(activityInfo);
		
		//Obtém e configura as Views.
		createView(savedInstanceState);
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        syncActionBarState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        navToggle.onConfigurationChanged(newConfig);
    }

	@Override
	@SuppressLint("Assert")
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navToggle.onOptionsItemSelected(item)) {
        	return true;
        }
        
	    if (item.getItemId() == android.R.id.home) {
	    	//Se a perspectiva for usar o Up, delega pra ela.
    		if (currentPerspective.isUpEnabled()) {
    			currentPerspective.onUpPressed();
    			return true;
    		}
    		
    		//Se o navToggle não tratou, significa que o Up está habilitado e como a perspectiva corrente não tratou,
    		//significa a finalização da perspectiva corrente (se possível).
    		assert isUpEnabled();
    		finishCurrentPerspective(false);
  			return true;
	    }
	    
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onBackPressed() {
		if (isNavigationMenuOpen()) {
			closeNavigationMenu();
			return;
		}

		//Não faz nada se a Perspectiva tratou o back. 
		if (currentPerspective.onBackPressed()) {
			return;
		}
		
		finishCurrentPerspective(true);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

        saveViewState(outState);
	}


    /**
     * Cria e configura a View da Activity.
     */
    protected void createView(Bundle savedInstanceState) {
        setContentView(R.layout.perspective_activity);

        toolbar = (Toolbar) findViewById(R.id.perspective_toolbar);
        setSupportActionBar(toolbar);
        navList = (ListView) findViewById(R.id.perspective_navigation_menu_list);
        navDrawerLayout = (DrawerLayout) findViewById(R.id.perspective_drawer_layout);

        navListAdapter = new NavigationMenuListAdapter(this, navMenu);
        navList.setAdapter(navListAdapter);
        navList.setOnItemClickListener(new NavigationMenuItemClickListener());

        navToggle = new NavigationMenuToggle(this, navDrawerLayout, R.string.navigation_menu_open, R.string.navigation_menu_close);
        navDrawerLayout.setDrawerListener(navToggle);

        fab = (FloatingActionButton) findViewById(R.id.perspective_fab);
        fab.setOnClickListener(new FABClickListener());

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        //Inicializa com os valores padrão na primeira vez e restaura o estado anterior nas demais vezes.
        usedInstancesIds = new BitSet();
        int selectedMenuItem;
        if (savedInstanceState == null) {
            perspectivesInstances = new SparseArray<>();
            perspectivesBackStack = new Stack<>();

            //Inicia na primeira perspectiva do menu.
            selectedMenuItem = 0;
            Intent perspectiveIntent = navMenu.getItem(selectedMenuItem).getIntent();
            startPerspective(perspectiveIntent, false);
        } else {
            perspectivesInstances = savedInstanceState.getSparseParcelableArray(SAVED_PERSPECTIVES_INSTANCES);
            perspectivesBackStack = AndroidBugsUtils.applyWorkaroundForBug3847(savedInstanceState, SAVED_PERSPECTIVES_BACKSTACK);

            //Inicializa o Bitset de ids utilizados.
            for (int i = 0; i < perspectivesInstances.size(); i++ ) {
                PerspectiveInstance instance = perspectivesInstances.valueAt(i);
                usedInstancesIds.set(instance.getId());
            }

            //Inicia na perspectiva que estava sendo utilizada antes da destruição da Activity.
            int currentPerspectiveId = savedInstanceState.getInt(SAVED_CURRENT_PERSPECTIVE_ID);
            PerspectiveInstance currentPerspectiveInstance = perspectivesInstances.get(currentPerspectiveId);
            startPerspective(currentPerspectiveInstance, false, false);

            //Recupera o item do menu selecionado.
            selectedMenuItem = savedInstanceState.getInt(SAVED_SELECTED_MENU_ITEM);

            //Se o fragmento do dialogo de confirmação foi restaurado, define o listener nele novamente.
            ConfirmationDialogFragment dialogFragment = (ConfirmationDialogFragment) getSupportFragmentManager().findFragmentByTag(ConfirmationDialogFragment.CONFIRMATION_DIALOG_FRAGMENT_TAG);
            if (dialogFragment != null) {
                dialogFragment.setListener(new PerspectiveFinishConfirmationDialogListener());
            }
        }

        setSelectedMenuItem(selectedMenuItem);
    }

    /**
     * Sincroniza o estado da Action Bar / Navigation Drawer.
     */
    protected void syncActionBarState() {
        navToggle.syncState();

        //Apenas após o "syncState" é possível saber se o menu estava aberto.
        if (isNavigationMenuOpen()) {
            //Precisa invalidar aqui apenas se o menu de navegação está aberto pois por padrão todas as ações dos fragmentos já são mostradas.
            invalidateActionBar(true);
        }
    }

    /**
     * Salva o estado da View da Activity.
     *
     * @param outState bundle utilizado para armazenar os estados salvos.
     */
    protected void saveViewState(Bundle outState) {
        outState.putInt(SAVED_CURRENT_PERSPECTIVE_ID, currentPerspective.getPerspectiveId());
        outState.putInt(SAVED_SELECTED_MENU_ITEM, navListAdapter.getSelectedItem());

        /*
         * Clona para garantir que não haverá problemas no caso das perspectivas terem sido manipuladas após o salvamento do estado (com commitAllowingStateLoss).
         * O problema pode acontecer no caso destas coleções ficarem armazenadas na memoria do Bundle, fazendo com que qualquer alteração feita
         * após o estado ser salvo reflita nelas. Assim, quando o estado for recuperado, o id da perspectiva corrente vai estar com um estado
         * mas as coleções com outro.
         */
        outState.putSparseParcelableArray(SAVED_PERSPECTIVES_INSTANCES, perspectivesInstances.clone());
        outState.putSerializable(SAVED_PERSPECTIVES_BACKSTACK, (Serializable) perspectivesBackStack.clone());
    }


    /**
     * Obtém a perspectiva corrente.
     *
     * @return a perspectiva obtida.
     */
    protected final Perspective getCurrentPerspective() {
        return currentPerspective;
    }

    /**
     * Salva o estado da perspectiva, possibilitando que ela seja recuperada com os mesmos dados presentes no momento.
     *
     * @param perspective perspectiva que terá o estado salvo.
     */
    protected final void savePerspectiveState(Perspective perspective) {
        PerspectiveInstance perspectiveInstance = perspectivesInstances.get(perspective.getPerspectiveId());
        savePerspectiveState(perspective, perspectiveInstance);
    }

    /**
     * Inicia uma perspectiva sinalizando que isto foi disparado de uma ação global. Uma ação global é aquela que só é mostrada quando
     * o menu de navegação está aberto, o que denota a área global da aplicação. Geralmente são ações da toolbar.<br>
     * O menu de navegação será fechado durante a abertura da perspectiva.
     *
     * @param perspectiveIntent o intent da perspectiva.
     */
    protected void startPerspectiveFromGlobalAction(Intent perspectiveIntent) {
        startPerspectiveFromMenu(perspectiveIntent, -1, -1);
    }


    /**
     * Abre o menu de navegação lateral.
     */
    protected final void openNavigationMenu() {
        getSupportFragmentManager().executePendingTransactions();

        //Abre o menu de navegação através do workaround para garantir que o toggle seja chamado.
        AndroidBugsUtils.applyWorkaroundForBug69711(navDrawerLayout, navToggle, getNavigationMenuView());
    }

    /**
     * Fecha o menu de navegação lateral.
     */
    protected final void closeNavigationMenu() {
        navDrawerLayout.closeDrawer(getNavigationMenuView());
    }

    /**
     * Indica se o menu de navegação lateral está aberto.
     *
     * @return <code>true</code> se o menu está aberto e <code>false</code> caso contrário.
     */
    protected final boolean isNavigationMenuOpen() {
        return navDrawerLayout.isDrawerOpen(getNavigationMenuView());
    }

    /**
     * Obtém a View do menu de navegação.
     *
     * @return a View obtida.
     */
    protected View getNavigationMenuView() {
        return navList;
    }

    /**
     * Obtém o {@link NavigationMenuToggle} da Activity.
     *
     * @return o NavigationMenuToggle obtido.
     */
    protected NavigationMenuToggle getNavigationMenuToggle() {
        return navToggle;
    }


    /*
	 * Métodos do IPerspectiveManager.
	 */
	
	@Override
	public void notifyAppBarChanged(Perspective perspective) {
		if (!checkCurrentPerspective(perspective)) {
			return;
		}
		
		updateActionBar();
	}

    @Override
    public void notifyFABsChanged(Perspective perspective) {
        if (!checkCurrentPerspective(perspective)) {
            return;
        }

        invalidateFABs();
    }

    @Override
	public void notifyVisibilityChanged(Perspective perspective) {
		if (perspective != currentPerspective) {
			return;
		}
		
		updateActionBar();
	}
	
	@Override
	public void notifyReady(Perspective perspective) {
		if (!checkCurrentPerspective(perspective)) {
			return;
		}
		
		updateActionBar();
	}
	
	@Override
	public void notifyFinished(Perspective perspective) {
		if (perspective == currentPerspective) {
			finishCurrentPerspective(false);
		} else {
			boolean removed = perspectivesBackStack.remove(Integer.valueOf(perspective.getPerspectiveId()));
			if (!removed) {
				//Apenas a perspectiva corrente ou uma da backstack deveriam notificar seu encerramento. Como isto pode ser ignorado, só loga o problema.
				Log.e(PerspectiveActivity.class.getSimpleName(), "An inactive perspective (not in the backstack) tried to notify its finish. Perspective intent: " + perspective.getIntent());
				return;
			}
			finishPerspective(perspective, false);
		}
	}

	@Override
	public void startPerspective(Intent perspectiveIntent) {
        tryStartPerspective(perspectiveIntent, null);
	}

	@Override
	public void startPerspectiveForResult(Perspective sourcePerspective, Intent perspectiveIntent, Serializable requestKey) {
		PerspectiveResultRequest resultRequest = new PerspectiveResultRequest(sourcePerspective.getPerspectiveId(), requestKey);
		tryStartPerspective(perspectiveIntent, resultRequest);
	}

    @Override
    public final boolean isCommitAllowingStateLoss() {
        return commitAllowingStateLoss;
    }

    @Override
    public final void setCommitAllowingStateLoss(boolean commitAllowingStateLoss) {
        this.commitAllowingStateLoss = commitAllowingStateLoss;
    }
	
	
	/*
	 * Métodos auxiliares para o gerenciamento das perspectivas. 
	 */
	
	private void tryStartPerspective(Intent perspectiveIntent, PerspectiveResultRequest resultRequest) {
		//Se tem trabalho não finalizado, precisa da confirmação do usuário primeiro.
		if (!checkUnfinishedWork(perspectiveIntent, resultRequest)) {
			return;
		}
		
		startPerspective(perspectiveIntent, resultRequest);
	}
	
	private void startPerspective(Intent perspectiveIntent, PerspectiveResultRequest resultRequest) {
		startPerspective(perspectiveIntent, resultRequest, false);
		
		//Deixa o item selecionado do menu atualizado com a perspectiva que foi aberta, independente de onde o Intent veio.
		updateSelectedMenuItem();		
	}
	
	private void startPerspective(Intent perspectiveIntent, boolean fromMenu) {
		startPerspective(perspectiveIntent, null, fromMenu);
	}

	private void startPerspective(Intent perspectiveIntent, PerspectiveResultRequest resultRequest, boolean fromMenu) {
		//Busca as informações de perspectiva alvo de acordo com o Intent.
		int perspectiveInfoIndex = findPerspectiveInfoIndex(perspectiveIntent);
		PerspectiveInfo perspectiveInfo = perspectivesInfos[perspectiveInfoIndex];

        PerspectiveInstance perspectiveInstance;
        boolean newInstanceFlag = (perspectiveIntent.getFlags() & IPerspectiveManager.INTENT_FLAG_NEW_INSTANCE) != 0;
        if (newInstanceFlag) {
            //Se definiu a flag de nova instância, ignora o launch mode da perspectiva e cria uma nova.
            perspectiveInstance = createPerspectiveInstance(perspectiveInfoIndex, perspectiveIntent);
        } else {
            switch (perspectiveInfo.getLaunchMode()) {
                case SINGLE:
                    //Verifica se a instância única já foi criada, se não cria-a.
                    perspectiveInstance = findSinglePerspectiveInstance(perspectiveInfoIndex);
                    if (perspectiveInstance == null) {
                        perspectiveInstance = createPerspectiveInstance(perspectiveInfoIndex, perspectiveIntent);
                    } else {
                        perspectiveInstance.setIntent(perspectiveIntent);
                    }
                    break;
                case STANDARD:
                    //Sempre cria uma instância nova.
                    perspectiveInstance = createPerspectiveInstance(perspectiveInfoIndex, perspectiveIntent);
                    break;

                default:
                    throw new IllegalStateException("Unsupported LaunchMode: " + perspectiveInfo.getLaunchMode());
            }
        }

		perspectiveInstance.setResultRequest(resultRequest);
		
		startPerspective(perspectiveInstance, fromMenu, false);
	}
	
	private void startPerspective(PerspectiveInstance perspectiveInstance, boolean fromBackstack) {
		startPerspective(perspectiveInstance, false, fromBackstack);
		
		//Deixa o item selecionado do menu atualizado com a perspectiva que foi aberta, independente de onde o Intent veio.
		updateSelectedMenuItem();
	}
	
	//TODO Rever parâmetros. Poderia ter um esquema de flags internas ao invés dos parâmetros booleanos...
	private void startPerspective(PerspectiveInstance perspectiveInstance, boolean fromMenu, boolean fromBackstack) {
		Intent perspectiveIntent = perspectiveInstance.getIntent();
		PerspectiveInfo perspectiveInfo = perspectivesInfos[perspectiveInstance.getPerspectiveInfoIndex()];
		
		//Executa as ações determinadas pelas flags do Intent.
		int flags = perspectiveIntent.getFlags();
		boolean clearBackstack = (flags & IPerspectiveManager.INTENT_FLAG_CLEAR_BACKSTACK) != 0;
		if (clearBackstack) {
			clearPerspectivesBackStack();
		}
		
		//Precisa verificar o "isAdded" pq trocas de perspectivas muito rápidas podem fazer com que o "currentPerspective" não esteja mais na Activity neste momento.
		if (currentPerspective != null && currentPerspective.isAdded()) {
			Utils.hideSoftInput(PerspectiveActivity.this);

			boolean pushToBackstack = !fromBackstack && !clearBackstack;
			finishPerspective(currentPerspective, pushToBackstack);
			if (pushToBackstack) {
				//Coloca a instância de perspectiva na pilha para ser reaberta no caso do "back" ser apertado.
				perspectivesBackStack.push(currentPerspective.getPerspectiveId());
			}
		}

        destroyFABs();

		//Como o Android possui um comportamento não documentado de restaurar automaticamente os fragmentos (ao rotacionar, por exemplo), verifica se ele já não está criado.
		FragmentManager fragmentManager = getSupportFragmentManager();
		Perspective newPerspective = (Perspective) fragmentManager.findFragmentByTag(perspectiveInstance.getTag());
		//Precisa verificar o "isAdded" também porque quando uma perspectiva volta a ser iniciada rapidamente, o Android ainda continua retornando ela no "findFragmentByTag", mesmo ela tendo sido removida. 			
		if (newPerspective == null || !newPerspective.isAdded()) { 
			//Instancia a Perspectiva baseado na classe configurada.
			try {
				newPerspective = perspectiveInfo.getPerspectiveClass().newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}

			//Seta os argumentos configurados na declaração da perspectiva.
			newPerspective.setArguments(perspectiveInfo.getArguments());
			
			//Restaura o estado anterior da Perspectiva, se houver.
			SavedState savedState = perspectiveInstance.getSavedState();
			if (savedState != null) {
				AndroidBugsUtils.applyWorkaroundForBug37484(savedState, this);
				newPerspective.setInitialSavedState(savedState);
				//Não precisa mais do estado salvo.
				perspectiveInstance.setSavedState(null);
			}
			
			//Adiciona a Perspectiva na tela.
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			if (fromMenu) {
				//Alteração de perspectiva vinda do menu de navegação tem que ser feita de maneira mais suave para evitar travamentos visuais.
				transaction.add(R.id.perspective_perspectives_container, newPerspective, perspectiveInstance.getTag());
				transaction.hide(newPerspective);
			} else {
				transaction.replace(R.id.perspective_perspectives_container, newPerspective, perspectiveInstance.getTag());
			}
            commitTransaction(transaction);
        }

		//Inicializa sempre pois estes dados não são restaurados.
		boolean forResult = perspectiveInstance.getResultRequest() != null;
		newPerspective.initialize(this, perspectiveInfo, perspectiveIntent, perspectiveInstance.getId(), forResult);
		currentPerspective = newPerspective;

        //Troca o tema da perspectiva se houver. Se não, mantém o da perspectiva anterior.
		int newThemeId = perspectiveInfo.getTheme();
        if (newThemeId > 0) {
            changePerspectiveTheme(newThemeId, fromMenu);
        }

        createFABs(fromMenu);
	}

    private void startPerspectiveFromMenu(Intent perspectiveIntent, int currentSelectedPosition, int newSelectedPosition) {
        //Se tem trabalho não finalizado, precisa da confirmação do usuário primeiro.
        if (!checkUnfinishedWork(perspectiveIntent, null)) {
            if (currentSelectedPosition >= 0) {
                //A lista seta como checado automaticamente no click, então tem que voltar para o original.
                navList.setItemChecked(currentSelectedPosition, true);
            }
            closeNavigationMenu();
            return;
        }

        //Esconde a perspectiva atual (que depois se tornará a antiga).
        Perspective oldPerspective = hideCurrentPerspective();

        //Cria a nova perspectiva escondida para evitar travamentos na hora do fechamento do menu de navegação.
        startPerspective(perspectiveIntent, true);
        //Como o menu está aberto, as ações do fragmento estão escondidas, então tem que definir como false para o estado ficar correto (por padrão é true).
        currentPerspective.setMenuVisibility(false);

        if (newSelectedPosition >= 0) {
            setSelectedMenuItem(newSelectedPosition);
        }

        //Utiliza este listener para mostrar a perspectiva criada apenas depois que o menu de navegação fechar,
        //evitando assim alguns travamentos. Ele será executado apenas uma vez, sendo removido depois.
        navToggle.setDrawerListener(new ShowPerspectiveOnMenuCloseListener(oldPerspective), true);

        //O resto das ações será feito quando o menu de navegaçao estiver totalmente fechado.
        //Utiliza o lock ao invés do close para que o usuário não consiga reabrir o menu enquanto ele está fechando.
        navDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    private void finishPerspective(Perspective perspective, boolean saveState) {
        PerspectiveInstance perspectiveInstance = perspectivesInstances.get(perspective.getPerspectiveId());
        PerspectiveInfo perspectiveInfo = perspectivesInfos[perspectiveInstance.getPerspectiveInfoIndex()];
        boolean newInstanceFlag = (perspectiveInstance.getIntent().getFlags() & IPerspectiveManager.INTENT_FLAG_NEW_INSTANCE) != 0;
        boolean isSingle = !newInstanceFlag && perspectiveInfo.getLaunchMode() == LaunchMode.SINGLE;

        //Se for uma instância single, sempre salva o estado.
        if (isSingle || saveState) {
            savePerspectiveState(perspective, perspectiveInstance);
        } else {
            //Se não salva o estado, não precisa mais guardar a instância.
            destroyPerspectiveInstance(perspectiveInstance);
        }
    }

    private void savePerspectiveState(Perspective perspective, PerspectiveInstance perspectiveInstance) {
        SavedState savedState = getSupportFragmentManager().saveFragmentInstanceState(perspective);
        perspectiveInstance.setSavedState(savedState);
        perspectiveInstance.setHasUnfinishedWork(perspective.hasUnfinishedWork());
    }

    private boolean checkUnfinishedWork(Intent perspectiveIntent, PerspectiveResultRequest resultRequest) {
		//Se é pra limpar o backstack, verifica se alguma perspectiva tem trabalhos inacabados. Neste caso, exige a confirmação do usuário.
        boolean clearBackstack = (perspectiveIntent.getFlags() & IPerspectiveManager.INTENT_FLAG_CLEAR_BACKSTACK) != 0;
        if (clearBackstack) {
            boolean ignoreUnfinishedWork = (perspectiveIntent.getFlags() & IPerspectiveManager.INTENT_FLAG_IGNORE_UNFINISHED_WORK) != 0;
            if (!ignoreUnfinishedWork && hasUnfinishedWork()) {
                showPerspectiveFinishConfirmationDialog(perspectiveIntent, resultRequest);
                return false;
            }
        }

		return true;
	}

	//Verifica se há algum trabalho inacabado na perspectiva atual ou em alguma perspectiva da backstack.
	private boolean hasUnfinishedWork() {
		if (currentPerspective.hasUnfinishedWork()) {
			return true;
		}
		
		for (Integer perspectiveId : perspectivesBackStack) {
			PerspectiveInstance instance = perspectivesInstances.get(perspectiveId);
			if (instance.hasUnfinishedWork()) {
				return true;
			}
		}
		return false;
	}
	
	private int findPerspectiveInfoIndex(Intent perspectiveIntent) {
		ComponentName componentName = perspectiveIntent.getComponent();
		String intentClass = componentName != null ? componentName.getClassName() : null;
		String intentAction = perspectiveIntent.getAction();
		Set<String> intentCategories = perspectiveIntent.getCategories();
		
		//Busca a perspectiva que se enquadra no intent.
		for (int i = 0; i < perspectivesInfos.length; i++) {
			PerspectiveInfo perspectiveInfo = perspectivesInfos[i];
			if (!perspectiveMatches(perspectiveInfo, intentClass, intentAction, intentCategories)) {
				continue;
			}
			
			//Seleciona a perspectiva que passou por todos os critérios do Intent.
			return i;
		}
		
		throw new RuntimeException("No perspective was able to attend to the following Intent: " + perspectiveIntent);
	}
	
	private boolean perspectiveMatches(PerspectiveInfo perspectiveInfo, Intent intent) {
		ComponentName componentName = intent.getComponent();
		String intentClass = componentName != null ? componentName.getClassName() : null;
		
		return perspectiveMatches(perspectiveInfo, intentClass, intent.getAction(), intent.getCategories());
	}
	
	private boolean perspectiveMatches(PerspectiveInfo perspectiveInfo, String intentClass, String intentAction, Set<String> intentCategories) {
		Class<? extends Perspective> perspectiveClass = perspectiveInfo.getPerspectiveClass();
		IntentFilter intentFilter = perspectiveInfo.getIntentFilter();
		
		//Se o intent definiu uma classe, a classe da perspectiva deve ser exatamente igual.
		if (intentClass != null && !perspectiveClass.getName().equals(intentClass)) {
			return false;
		}
		
		//Se o intent definiu uma ação, a perspectiva deve ter declarado esta ação no IntentFilter.
		if (intentAction != null && (intentFilter == null || !intentFilter.hasAction(intentAction))) {
			return false;
		}
		
		//Se o intent definiu categorias, a perspectiva deve ter declarado todas elas no IntentFilter.
		if (intentCategories != null) {
			for (String intentCategory : intentCategories) {
				if (!intentFilter.hasCategory(intentCategory)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	private Perspective hideCurrentPerspective() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
		transaction.hide(currentPerspective);
        commitTransaction(transaction);

        return currentPerspective;
	}
	
	private void showCurrentPerspective(Perspective oldPerspectiveToRemove) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		transaction.show(currentPerspective);
        commitTransaction(transaction);

        //Faz em transações separadas pois estava ocorrendo uma piscada na troca de perspectivas onde a antiga possuia uma barra na parte de baixo.
		if (oldPerspectiveToRemove != null) {
			transaction = getSupportFragmentManager().beginTransaction();
			transaction.remove(oldPerspectiveToRemove);
            commitTransaction(transaction);
        }
	}

    private void commitTransaction(FragmentTransaction transaction) {
        if (commitAllowingStateLoss) {
            transaction.commitAllowingStateLoss();
        } else {
            transaction.commit();
        }
    }
	
	
	/*
	 * Métodos auxiliares 
	 */
	
	private ActivityInfo getActivityInfo() {
		try {
			return getPackageManager().getActivityInfo(this.getComponentName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private YmirMenu loadNavigationMenu(ActivityInfo activityInfo) {
		YmirMenuInflater navMenuInflater = new YmirMenuInflater(this);
		
		//Obtém o xml do menu de navegação definido no metadata.
		XmlResourceParser xmlParser = activityInfo.loadXmlMetaData(getPackageManager(), NAVIGATION_MENU_METADATA);
		if (xmlParser == null) {
			throw new RuntimeException("Navigation menu metadata (name = \"" + NAVIGATION_MENU_METADATA + "\") is missing.");
		}
		YmirMenu navivationMenu;
        try {
        	navivationMenu = navMenuInflater.inflate(xmlParser);
        } finally {
        	xmlParser.close();
        }
        
        //Verificações.
		if (navivationMenu.size() == 0) {
			throw new RuntimeException("The navigation menu has no items.");
		}
		for (int i = 0; i < navivationMenu.size(); i++) {
			YmirMenuItem item = navivationMenu.getItem(i);
			if (TextUtils.isEmpty(item.getTitle())) {
				throw new RuntimeException(String.format(Locale.US, "The navigation menu item #%d has no title.", i));
			}
			if (item.getIntent() == null) {
				throw new RuntimeException(String.format(Locale.US, "The navigation menu item #%d has no Perspective Intent.", i));
			}
		}
		
		return navivationMenu;
	}
	
	private PerspectiveInfo[] loadPerspectives(ActivityInfo activityInfo) {
		 PerspectiveInfoInflater perspectivesInflater = new PerspectiveInfoInflater(this);
		
		//Obtém o xml das perspectivas definido no metadata.
		XmlResourceParser xmlParser = activityInfo.loadXmlMetaData(getPackageManager(), PERSPECTIVES_METADATA);
		if (xmlParser == null) {
			throw new RuntimeException("Perspectives metadata (name = \"" + PERSPECTIVES_METADATA + "\") is missing.");
		}
        try {
        	PerspectiveInfo[] perspectivesInfos = perspectivesInflater.inflate(xmlParser);
    		if (perspectivesInfos.length == 0) {
    			throw new RuntimeException("No perspective was defined.");
    		}
    		return perspectivesInfos;
        } finally {
        	xmlParser.close();
        }
	}

	private PerspectiveInstance createPerspectiveInstance(int perspectiveInfoIndex, Intent intent) {
		//Obtém o próximo id utilizável.
		int id = usedInstancesIds.nextClearBit(0);
		usedInstancesIds.set(id);
		
		PerspectiveInstance instance = new PerspectiveInstance(id, perspectiveInfoIndex, intent);
		perspectivesInstances.put(id, instance);
		return instance;
	}
	
	private void destroyPerspectiveInstance(PerspectiveInstance perspectiveInstance) {
		int id = perspectiveInstance.getId();
		perspectivesInstances.remove(id);
		usedInstancesIds.clear(id);
	}
	
	private PerspectiveInstance findSinglePerspectiveInstance(int perspectiveInfoIndex) {
		for (int i = 0; i < perspectivesInstances.size(); i++) {
			PerspectiveInstance perspectiveIndex = perspectivesInstances.valueAt(i);
			if (perspectiveIndex.getPerspectiveInfoIndex() == perspectiveInfoIndex) {
				return perspectiveIndex;
			}
		}
		return null;
	}
	
	private void updateSelectedMenuItem() {
		PerspectiveInstance currentInstance = perspectivesInstances.get(currentPerspective.getPerspectiveId());
		PerspectiveInfo curremtPerspectiveInfo = perspectivesInfos[currentInstance.getPerspectiveInfoIndex()];
		
		//Define o item selecionado se algum item do menu de navegação possui um Intent que corresponde a perspectiva corrente.
		for (int i = 0; i < navListAdapter.getCount(); i++) {
			YmirMenuItem item = navListAdapter.getItem(i);
            if (NavigationMenuListAdapter.isSeparator(item)) {
                continue;
            }

			if (perspectiveMatches(curremtPerspectiveInfo, item.getIntent())) {
                setSelectedMenuItem(i);
				return;
			}
		}
	}

    private void setSelectedMenuItem(int position) {
        //Necessário para alterar a cor.
        navListAdapter.setSelectedItem(position);
        //Necessário para alterar o background.
        navList.setItemChecked(position, true);
    }

	private void updateActionBar() {
		updateActionBar(isNavigationMenuOpen());
	}

	private void updateActionBar(boolean isNavigationMenuOpen) {
		if (isNavigationMenuOpen) {
			//Atualiza a action bar com o tíulo / drawer da aplicação.
			getSupportActionBar().setTitle(getTitle());
			navToggle.setDrawerIndicatorEnabled(true);
		} else if (!currentPerspective.isHidden()) {
			//Atualiza a action bar com o tíulo / drawer da perspectiva corrente.
			updateActionBarTitle();

			navToggle.setDrawerIndicatorEnabled(!isUpEnabled());
		}
	}
	
	private void updateActionBarTitle() {
		//Atualiza a action bar com o tíulo da perspectiva. Se ele for nulo, usa o título do item do menu de navegação.
		String title = currentPerspective.getTitle();
        if (title == null) {
			title = navListAdapter.getSelectedNavigationMenuItem().getTitle();
		}
        getSupportActionBar().setTitle(title);
	}

	private void invalidateActionBar(boolean isNavigationMenuOpen) {
		updateActionBar(isNavigationMenuOpen);
		
		//Só faz a invalidação das ações da ActionBar se o próprio fragmento não invalidou.
		if (!currentPerspective.setOptionsMenuVisibility(!isNavigationMenuOpen)) {
			invalidateOptionsMenu();
		}
	}

	private boolean isUpEnabled() {
		if (currentPerspective.isUpEnabled()) {
			return true;
		}
		
		//Se possui item na backstack e não está atualmente em uma perspectiva do menu, permite que o Up tbm volte no backstack de perspectivas.
		if (perspectivesBackStack.isEmpty()) {
			return false;
		}
		YmirMenuItem selectedItem = navListAdapter.getSelectedNavigationMenuItem();
		return !perspectiveMatches(currentPerspective.getInfo(), selectedItem.getIntent());
	}
	
	private void popPerspectiveBackstack() {
		Integer perspectiveInstanceId = perspectivesBackStack.pop();
		PerspectiveInstance perspectiveInstance = perspectivesInstances.get(perspectiveInstanceId);
		startPerspective(perspectiveInstance, true);
	}
	
	private void clearPerspectivesBackStack() {
		//Destroi as instâncias de perspectiva não single que estavam na backstack.
		for (Integer instanceId : perspectivesBackStack) {
			PerspectiveInstance perspectiveInstance = perspectivesInstances.get(instanceId);
			PerspectiveInfo perspectiveInfo = perspectivesInfos[perspectiveInstance.getPerspectiveInfoIndex()];
			
			//Não destroi as instâncias single.
            boolean newInstanceFlag = (perspectiveInstance.getIntent().getFlags() & IPerspectiveManager.INTENT_FLAG_NEW_INSTANCE) != 0;
			if (!newInstanceFlag && perspectiveInfo.getLaunchMode() == LaunchMode.SINGLE) {
				continue;
			}
			
			destroyPerspectiveInstance(perspectiveInstance);
		}
		perspectivesBackStack.clear();
	}
	
	private boolean checkCurrentPerspective(Perspective perspective) {
		if (perspective != currentPerspective) {
			//Apenas loga o problema, pois ele pode ser ignorado.
			Log.e(PerspectiveActivity.class.getSimpleName(), "An old perspective tried to notify changes. Perspective intent: " + perspective.getIntent());
			return false;
		}
		return true;
	}

	private void finishCurrentPerspective(boolean moveToBack) {
		finishCurrentPerspective(moveToBack, false);
	}
	
	private void finishCurrentPerspective(boolean moveToBack, boolean ignoreUnfinishedWork) {
		boolean hasSuccessorPerspective = hasSuccessorPerspective();
		
		//Se possui trabalhos inacabados, pede a confirmação do usuário antes. Só é necessário se houver uma perspectiva
		//sucessora ou se a Activity não for escondida pq neste caso a perspectiva atual continuará em funcionamento.
		if (!ignoreUnfinishedWork && currentPerspective.hasUnfinishedWork() && (hasSuccessorPerspective || !moveToBack)) {
			showPerspectiveFinishConfirmationDialog(null, null);
			return;
		}
		
		if (hasSuccessorPerspective) {
			startSuccessorPerspective();
			return;
		}
		
		//Se não há nenhuma outra perspectiva para assumir, finaliza o Aplicativo ou apenas esconde ele.  
		if (moveToBack) {
			moveTaskToBack(false);
		} else {
			finish();
		}
	}

	private boolean hasSuccessorPerspective() {
		return currentPerspective.isForResult() || !perspectivesBackStack.isEmpty();
	}
	
	private void startSuccessorPerspective() {
		//Se foi aberta para um resultado, reinicia a perspectiva que a abriu e lhe envia o resultado.
		if (currentPerspective.isForResult()) {
			Perspective resultPerspective = currentPerspective;
			
			//Obtém o id de quem chamou a perspectiva para um resultado.
			PerspectiveInstance resultInstance = perspectivesInstances.get(resultPerspective.getPerspectiveId());
			PerspectiveResultRequest resultRequest = resultInstance.getResultRequest();
			int callerId = resultRequest.getCallerId();
			
			//Remove o id de quem chamou da backstack pq ela vai ser iniciada agora.
			perspectivesBackStack.remove(Integer.valueOf(callerId));
			
			//Inicia a perspectiva que chamou a outra para um resultado.
			PerspectiveInstance callerInstance = perspectivesInstances.get(callerId);
			startPerspective(callerInstance, true); //Como está restaurando uma perspectiva já aberta, atua da mesma forma que uma perspectiva trazida da backstack.
			getSupportFragmentManager().executePendingTransactions();

			//Passa o resultado para quem chamou a perspectiva para um resultado.
			currentPerspective.onPerspectiveResult(resultRequest.getRequestKey(), resultPerspective.getResultCode(), resultPerspective.getResultData());
			return;
		}
		
		//Reabre a perspectiva do topo da pilha, se houver.
		popPerspectiveBackstack();
	}
	
	private void showPerspectiveFinishConfirmationDialog(Intent perspectiveIntent, PerspectiveResultRequest resultRequest) {
		Bundle arguments = new Bundle();
		arguments.putParcelable(PerspectiveFinishConfirmationDialogListener.INTENT_ARGUMENT, perspectiveIntent);
		arguments.putParcelable(PerspectiveFinishConfirmationDialogListener.RESULT_REQUEST_ARGUMENT, resultRequest);
		arguments.putInt(ConfirmationDialogFragment.MESSAGE_ARGUMENT, R.string.unfinished_work_confirmation);
		arguments.putInt(ConfirmationDialogFragment.POSITIVE_BUTTON_ARGUMENT, R.string.unfinished_work_confirmation_positive);

		ConfirmationDialogFragment confirmationFrag = new ConfirmationDialogFragment();
		confirmationFrag.setArguments(arguments);
		confirmationFrag.setListener(new PerspectiveFinishConfirmationDialogListener());
		confirmationFrag.show(getSupportFragmentManager(), ConfirmationDialogFragment.CONFIRMATION_DIALOG_FRAGMENT_TAG);
	}


    //Altera o tema e aplica as cores primárias na Toolbar (todas as versões) e StatusBar (API21+)
    @SuppressWarnings("ResourceType")
    private void changePerspectiveTheme(int themeResId, boolean animate) {
        Theme curTheme = getTheme();
        int[] colorsAttrs = {R.attr.colorPrimary, R.attr.colorPrimaryDark};

        //Obtém as cores primárias do tema antigo.
        TypedArray typedArray = curTheme.obtainStyledAttributes(colorsAttrs);
        int prevPrimaryColor = typedArray.getColor(0, -1);
        int prevPrimaryColorDark = typedArray.getColor(1, -1);
        typedArray.recycle();

        //Cria e aplica um novo tema a partir do tema original, evitando restícios do tema antigo.
        Theme newTheme = getResources().newTheme();
        newTheme.applyStyle(getActivityThemeResId(), true);
        newTheme.applyStyle(themeResId, true);
        curTheme.setTo(newTheme);

        //Obtém as cores primárias do tema novo.
        typedArray = newTheme.obtainStyledAttributes(colorsAttrs);
        int primaryColor = typedArray.getColor(0, -1);
        int primaryColorDark = typedArray.getColor(1, -1);
        typedArray.recycle();

        //Se há uma cor primária diferente, aplica-a na Toolbar.
        if (primaryColor != -1 && prevPrimaryColor != primaryColor) {
            if (animate && prevPrimaryColor != -1) {
                animateColorChange(prevPrimaryColor, primaryColor, new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        toolbar.setBackgroundColor((Integer) animator.getAnimatedValue());
                    }
                });
            } else {
                //Executa o que tem pendente para a troca de cor ser ao mesmo tempo da criação das Views.
                getSupportFragmentManager().executePendingTransactions();
                toolbar.setBackgroundColor(primaryColor);
            }
        }

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            //Se há uma cor primária escura diferente, aplica-a na StatusBar.
            if (primaryColorDark != -1 && prevPrimaryColorDark != primaryColorDark) {
                if (animate && prevPrimaryColorDark != -1) {
                    animateColorChange(prevPrimaryColorDark, primaryColorDark, new ValueAnimator.AnimatorUpdateListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            getWindow().setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    });
                } else {
                    getWindow().setStatusBarColor(primaryColorDark);
                }
            }
        }
    }

    private void animateColorChange(int prevColor, int color, ValueAnimator.AnimatorUpdateListener animatorListener) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), prevColor, color);
        colorAnimation.addUpdateListener(animatorListener);
        colorAnimation.setDuration(700);
        colorAnimation.setStartDelay(0);
        colorAnimation.start();
    }

    private int getActivityThemeResId() {
        int theme = getActivityInfo().theme;
        if (theme > 0) {
            return theme;
        }
        return getApplicationInfo().theme;
    }


    private void createFABs(final boolean fromPerspectiveMenu) {
        //Faz dentro de um post para que a perspectiva consiga passar pelo "onActivityCreated".
        fab.post(new Runnable() {
            @Override
            public void run() {
                //Verifica o "isDestroyed" do FragmentManager pois o da Activity é só API 17+.
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentManager.isDestroyed()) {
                    return;
                }

                if (!currentPerspective.hasFABs()) {
                    return;
                }

                fragmentManager.executePendingTransactions();

                fabMenu = new YmirMenu();
                YmirMenuInflater menuInflater = new YmirMenuInflater(PerspectiveActivity.this);
                currentPerspective.onCreateFABs(fabMenu, menuInflater);

                //Se a alteração da perspectiva foi disparada pelo menu, deixa pra mostrar o FAB junto com a perspectiva (depois do fechamento do menu).
                invalidateFABs(false, !fromPerspectiveMenu);
            }
        });
    }

    private void destroyFABs() {
        if (fabMenu == null) {
            return;
        }

        hideFABs();
        fabMenu = null;
    }

    private void invalidateFABs() {
        //Esconde o FAB antigo primeiro e, se necessário, mostra o novo.
        invalidateFABs(true, true);
    }

    @SuppressWarnings("deprecation")
    private void invalidateFABs(boolean hide, boolean show) {
        if (fabMenu == null) {
            return;
        }

        if (hide) {
            hideFABs();
        }

        //Obtém o item visível apenas.
        YmirMenuItem fabItem = null;
        for (int i = 0; i < fabMenu.size(); i++) {
            YmirMenuItem curFabItem = fabMenu.getItem(i);
            if (currentPerspective.isFABAvailable(curFabItem)) {
                if (fabItem != null) {
                    throw new PendingFeatureException("Two or more FABs visible at once");
                }
                fabItem = curFabItem;
            }
        }
        if (fabItem == null) {
            return;
        }

        fab.setTag(fabItem);

        //Seta o ícone do FAB.
        int iconResourceId = fabItem.getIconResourceId();
        if (iconResourceId <= 0) {
            throw new IllegalStateException("An icon is required to use the FAB");
        }
        Drawable drawable = getIconDrawable(this, iconResourceId);
        fab.setImageDrawable(drawable);

        //Seta a cor do FAB. Se foi definida uma cor no item, utiliza ela, se não a cor padrão do tema.
        int color = fabItem.getColor();
        if (color == -1) {
            //Caso não haja cor, volta para a padrão do tema.
            //TODO por enquanto a support library não declara um "fabStyle", fazendo com que o componente utilize diretamente o Widget_Design_FloatingActionButton. Se isto mudar algum dia, a cor original deve ser pega do estilo referenciado por "fabStyle".
            TypedArray typedArray = getTheme().obtainStyledAttributes(R.style.Widget_Design_FloatingActionButton, new int[] {R.attr.backgroundTint});
            color = typedArray.getColor(0, -1);
            typedArray.recycle();
        }
        fab.setBackgroundTintList(ColorStateList.valueOf(color));

        //Se é do menu, deixa para mostrar o FAB apenas depois da perspectiva.
        if (show) {
            showFAB();
        }
    }

    private void hideFABs() {
        fab.hide();
        fab.setTag(null);
    }

    private void tryShowFAB() {
        if (fab.getTag() != null) {
            showFAB();
        }
    }

    @SuppressWarnings("ResourceType")
    private void showFAB() {
        //Se há alteração nas margens do FAB, aplica-as e atualiza o layout antes de mostrar.
        TypedArray typedArray = getTheme().obtainStyledAttributes(new int[] {R.attr.perspectiveFABMarginEnd, R.attr.perspectiveFABMarginBottom});
        int marginEnd = typedArray.getDimensionPixelSize(0, 0);
        int marginBottom = typedArray.getDimensionPixelSize(1, 0);
        typedArray.recycle();
        MarginLayoutParams fabLayoutParams = (MarginLayoutParams) fab.getLayoutParams();
        if (marginEnd != fabLayoutParams.getMarginEnd() || marginBottom != fabLayoutParams.bottomMargin) {
            fabLayoutParams.setMarginEnd(marginEnd);
            fabLayoutParams.bottomMargin = marginBottom;
            fab.requestLayout();
        }

        fab.show();
    }


    @SuppressWarnings("deprecation")
    private static Drawable getIconDrawable(Context context, int iconResourceId) {
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            return context.getDrawable(iconResourceId);
        }

        return context.getResources().getDrawable(iconResourceId);
    }

	
	/*
	 * Classes auxiliares
	 */

    /**
     * Listener de click no FAB da Activity.
     */
    private final class FABClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (currentPerspective == null) {
                return;
            }

            YmirMenuItem fabItem = (YmirMenuItem) v.getTag();
            if (fabItem == null) {
                return;
            }

            currentPerspective.onFABClicked(fabItem);
        }
    }

	/**
	 * Listener de clicks dos items do menu. Quando um item é clicado, inicia-se o processo de alteração de perspectiva.
	 * Primeiro a perspectiva atual é escondida e em seguida a nova é criada, mas também escondida. Apenas depois que o 
	 * menu de navegação fechar é que a nova perspectiva será mostrada. Isto tudo é necessário para evitar travamentos
	 * na animação do menu provenientes do carregamento da nova perspectiva.
	 */
	private final class NavigationMenuItemClickListener implements OnItemClickListener {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int currentSelectedPosition = navListAdapter.getSelectedItem();
			if (position == currentSelectedPosition) {
				return;
			}
			YmirMenuItem menuItem = navListAdapter.getItem(position);
			Intent perspectiveIntent = menuItem.getIntent();

            startPerspectiveFromMenu(perspectiveIntent, currentSelectedPosition, position);
        }
	}

    /**
	 * Listener de fechamento do menu de navegação utilizado para mostrar uma perspectiva que acabou de ser criada.
	 */
	private final class ShowPerspectiveOnMenuCloseListener extends SimpleDrawerListener {
		
		private final Perspective oldPerspective;
		
		public ShowPerspectiveOnMenuCloseListener(Perspective oldPerspective) {
			this.oldPerspective = oldPerspective;
		}
		
		@Override
		public void onDrawerClosed(View drawerView) {
			//Mostra a perspectiva atual e remove a antiga.
			showCurrentPerspective(oldPerspective);
			
			//Permite a abertura do menu novamente.
			navDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

            //Mostra o FAB apenas depois da perspectiva, se houver.
            tryShowFAB();
		}
	}
	
	
	/**
	 * Controla todas as alterações na ActionBar referentes ao menu de navegação.
	 */
	protected final class NavigationMenuToggle extends ActionBarDrawerToggle {
		
		private DrawerListener drawerListener;
		private boolean oneTimeListener;

        public NavigationMenuToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }
		
		@Override
		public void onDrawerOpened(View drawerView) {
			super.onDrawerOpened(drawerView);
			
			if (drawerListener != null) {
				drawerListener.onDrawerOpened(drawerView);
				checkOneTimeListener();
			}
			
			//Esconde o soft keyboard, se houver.
			Utils.hideSoftInput(PerspectiveActivity.this);
			
			//Atualiza a action bar com o tíulo / ações da aplicação.
			invalidateActionBar(true);
		}
		
		@Override
		public void onDrawerClosed(View drawerView) {
			super.onDrawerClosed(drawerView);
			
			if (drawerListener != null) {
				drawerListener.onDrawerClosed(drawerView);
				checkOneTimeListener();
			}
			
			//Atualiza a ActionBar com o título / ações da perspectiva corrente.
			invalidateActionBar(false);
			
			//Se a perspectiva ainda não foi mostrada, atualiza o título para que o usuário tenha a impressão de que 
			//a alteração está sendo feita. O Up e as ações serão atualizados quando a perspectiva for mostrada de fato.
			if (currentPerspective.isHidden()) {
				updateActionBarTitle();
			}
		}
		
		private void checkOneTimeListener() {
			if (oneTimeListener) {
				drawerListener = null;
				oneTimeListener = false;
			}
		}
		
		public void setDrawerListener(DrawerListener drawerListener, boolean oneTimeListener) {
			this.drawerListener = drawerListener;
			this.oneTimeListener = oneTimeListener; 
		}
	}
	
	/**
	 * Adapter utilizado pelo menu de navegação para compor sua lista de items. Os items serão {@link android.widget.TextView} cujo
	 * a cor do texto e do icone variam de acordo com a cor definida no item de menu.<br>
	 * O item selecinado pode ser definido através do método {@link #setSelectedItem}.
	 */
	private static final class NavigationMenuListAdapter extends BaseAdapter {

		private final Context context;
		private final List<YmirMenuItem> items;
		private final LayoutInflater inflater;
		private final int iconSize;
		
		private int selectedItem = -1;

		public NavigationMenuListAdapter(Context context, YmirMenu navMenu) {
			this.context = context;
			this.inflater = LayoutInflater.from(context);

            //Coloca os separadores de grupos entre dos itens.
            items = new ArrayList<>();
            for (int i = 0; i < navMenu.size(); i++) {
                YmirMenuItem item = navMenu.getItem(i);
                int groupId = item.getGroupId();

                //Se o grupo do item corrente é diferente do anterior, tem que colocar um divisor.
                if (i > 0 && groupId != navMenu.getItem(i -1).getGroupId()) {
                    items.add(null);
                }
                items.add(item);
            }

			//Obtém o iconSize aqui para aplicá-lo no getView porque o TextView não suporta definir esta propriedade por xml de layout.
			iconSize = context.getResources().getDimensionPixelSize(R.dimen.navigation_menu_list_item_icon_size);
		}
		
		@Override
        @SuppressLint("ViewHolder")
		public View getView(int position, View convertView, ViewGroup parent) {
	        YmirMenuItem item = getItem(position);
            if (isSeparator(item)) {
                //Como não ha necessidade de reutilizar as views normais, não reutiliza o separador também. Por isto, nem é necessário declarar dois tipos de Views através do getViewTypeCount().
                return inflater.inflate(R.layout.perspective_navigation_menu_list_separator, parent, false);
            }

            //Não reutiliza o convertView para que os TextViews não selecionados voltem para a cor original.
	        TextView textView = (TextView) inflater.inflate(R.layout.perspective_navigation_menu_list_item, parent, false);

	        //Define o texto do item
	        textView.setText(item.getTitle());
	        
	        //Define o ícone do item.
            int color = item.getColor();
	        int iconResourceId = item.getIconResourceId();
	        if (iconResourceId > 0) {
                Drawable icon = getIconDrawable(context, iconResourceId);
                if (icon == null) {
                    throw new RuntimeException("Invalid menu item icon: " + iconResourceId);
                }

                //Define a cor do ícone, se houver.
                if (color != -1) {
                    icon = DrawableCompat.wrap(icon.mutate());
                    DrawableCompat.setTint(icon, color);
                }

				//Define o iconSize aqui porque o TextView não suporta definir esta propriedade no xml de layout.
	        	icon.setBounds(0, 0, iconSize, iconSize);

	            textView.setCompoundDrawables(icon, null, null, null);
	        }

	        //Define a cor do texto selecionado, se houver.
			if (color != -1 && position == selectedItem) {
                textView.setTextColor(color);
            }

			return textView;
		}
		
		@Override
		public YmirMenuItem getItem(int position) {
			return items.get(position);
		}
		
		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public boolean isEnabled(int position) {
			return position != selectedItem && !isSeparator(items.get(position));
		}

		public void setSelectedItem(int selectedItem){
			this.selectedItem = selectedItem;
			notifyDataSetChanged();
		}

		public int getSelectedItem() {
			return selectedItem;
		}

		public YmirMenuItem getSelectedNavigationMenuItem() {
			if (selectedItem == -1) {
				return null;
			}
			return items.get(selectedItem);
		}

        public static boolean isSeparator(YmirMenuItem item) {
            return item == null;
        }
    }

	
	/**
	 * Instância de uma perspectiva que foi definida na configuração de perspectivas. 
	 */
	protected static final class PerspectiveInstance implements Parcelable {
		private final int id;
		private final int perspectiveInfoIndex;
		private Intent intent;
		private PerspectiveResultRequest resultRequest;
		
		private SavedState savedState;
		private boolean hasUnfinishedWork;
		
		public PerspectiveInstance(int id, int perspectiveInfoIndex, Intent intent) {
			this.id = id;
			this.perspectiveInfoIndex = perspectiveInfoIndex;
			this.intent = intent;
		}
		
		public int getId() {
			return id;
		}
		
		public int getPerspectiveInfoIndex() {
			return perspectiveInfoIndex;
		}
		
		public SavedState getSavedState() {
			return savedState;
		}
		
		public void setSavedState(SavedState savedState) {
			this.savedState = savedState;
		}

		public Intent getIntent() {
			return intent;
		}

		public void setIntent(Intent intent) {
			this.intent = intent;
		}
		
		public String getTag() {
			return String.valueOf(id);
		}

		public PerspectiveResultRequest getResultRequest() {
			return resultRequest;
		}
		
		public void setResultRequest(PerspectiveResultRequest resultRequest) {
			this.resultRequest = resultRequest;
		}
		
		public void setHasUnfinishedWork(boolean hasUnfinishedWork) {
			this.hasUnfinishedWork = hasUnfinishedWork;
		}
		
		public boolean hasUnfinishedWork() {
			return hasUnfinishedWork;
		}
		
		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(id);
			dest.writeInt(perspectiveInfoIndex);
			dest.writeParcelable(intent, flags);
			dest.writeParcelable(resultRequest, flags);
			dest.writeParcelable(savedState, flags);
			AndroidBugsUtils.applyWorkaroundForBug5973_write(dest, hasUnfinishedWork);
		}
		
		@SuppressWarnings("unused")
		public static final Creator<PerspectiveInstance> CREATOR = new Creator<PerspectiveInstance>() {
			@Override
			public PerspectiveInstance createFromParcel(Parcel source) {
				PerspectiveInstance instance = new PerspectiveInstance(source.readInt(), source.readInt(), 
																	   AndroidBugsUtils.applyWorkaroundForIntentDefaultClassloaderBug(source));
				
				PerspectiveResultRequest resultRequest = AndroidBugsUtils.applyWorkaroundForParcelableDefaultClassloaderBug(source);
				instance.setResultRequest(resultRequest);
				SavedState savedState = AndroidBugsUtils.applyWorkaroundForParcelableDefaultClassloaderBug(source);
				instance.setSavedState(savedState);
				instance.setHasUnfinishedWork(AndroidBugsUtils.applyWorkaroundForBug5973_read(source));
				return instance;
			}
			
			@Override
			public PerspectiveInstance[] newArray(int size) {
				return new PerspectiveInstance[size];
			}
		};
	}
	
	/**
	 * Contém as informações de uma requisição de abertura de perspectiva para um resultado, servindo para que o
	 * resultado possa ser enviado corretamente para quem fez a requisição.
	 */
	private static final class PerspectiveResultRequest implements Parcelable {
		private final int callerId;
		private final Serializable requestKey;

		public PerspectiveResultRequest(int callerId, Serializable requestKey) {
			this.callerId = callerId;
			this.requestKey = requestKey;
		}
		
		public int getCallerId() {
			return callerId;
		}
		
		public Serializable getRequestKey() {
			return requestKey;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(callerId);
			dest.writeSerializable(requestKey);
		}
		
		@SuppressWarnings("unused")
		public static final Creator<PerspectiveResultRequest> CREATOR = new Creator<PerspectiveResultRequest>() {

			@Override
			public PerspectiveResultRequest createFromParcel(Parcel source) {
				return new PerspectiveResultRequest(source.readInt(), source.readSerializable());
			}

			@Override
			public PerspectiveResultRequest[] newArray(int size) {
				return new PerspectiveResultRequest[size];
			}
			
		};
	}
	
	/**
	 * Listener do dialogo de confirmação para o encerramento de perspectiva com trabalhos inacabados.
	 */
	private final class PerspectiveFinishConfirmationDialogListener implements IConfirmationDialogListener {
		
		private static final String INTENT_ARGUMENT = "INTENT_ARGUMENT";
		private static final String RESULT_REQUEST_ARGUMENT = "RESULT_REQUEST_ARGUMENT";
		
		@Override
		public void onConfirm(ConfirmationDialogFragment fragment) {
			Bundle arguments = fragment.getArguments();
			Intent intent = arguments.getParcelable(INTENT_ARGUMENT);
			if (intent != null) { 
				PerspectiveResultRequest resultRequest = arguments.getParcelable(RESULT_REQUEST_ARGUMENT);
				startPerspective(intent, resultRequest);
				return;
			}
			
			finishCurrentPerspective(false, true);
		}

		@Override
		public void onCancel(ConfirmationDialogFragment fragment) {
		}
	}
}
