package br.com.zalem.ymir.client.android.entity.data.openmobster.util;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import org.openmobster.core.mobileCloud.android.configuration.Configuration;
import org.openmobster.core.mobileCloud.mgr.AppActivation;

import br.com.zalem.ymir.client.android.entity.data.openmobster.R;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.OpenMobsterUtils.AutoActivationAsyncTask;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.OpenMobsterUtils.DeviceActivationListener;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.OpenMobsterUtils.OfflineActivationTask;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.OpenMobsterUtils.OfflineDataUpdateTask;

/**
 * Ativador da estrutura do OpenMobster.<br>
 * Executa as ações necessárias para que o OpenMobster esteja apto a ser utilizado pelas activities.<br>
 * <br>
 * Deve ser registrado como listener de activities da aplicação através do método {@link Application#registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks)}.
 *
 * @author Thiago Gesser
 */
public final class OpenMobsterActivator implements ActivityLifecycleCallbacks {

    private final int[] offlinePopulationsResIds;
    private final Integer onlineAutoActivationResId;

    private boolean offlinePopulationsUptodate;
    private AutoActivationListener onlineAutoActivationListener;
    private Thread offlineActivationTask;

    //Só permite instanciação pelos construtores estáticos.
    private OpenMobsterActivator(int[] offlinePopulationsResIds, Integer onlineAutoActivationResId) {
        this.offlinePopulationsResIds = offlinePopulationsResIds;
        this.onlineAutoActivationResId = onlineAutoActivationResId;
    }

    /**
     * Cria um ativador que habilita o OpenMobster a funcionar de forma <code>offline</code>, sem a necessidade de ser ativado através de um servidor.<br>
     * Também é possível configurar os dados iniciais de cada entidade através de recursos do tipo <code>json</code> gerados pelo {@link MobileBeanJsonSerializer}.
     *
     * @param populationsResIds identificadores dos recursos json que contém os dados iniciais das entidades.
     * @return o ativador criado.
     */
    public static OpenMobsterActivator createOfflineActivator(int... populationsResIds) {
        if (populationsResIds == null) {
            throw new NullPointerException("populationsResIds == null");
        }

        return new OpenMobsterActivator(populationsResIds, null);
    }

    /**
     * Cria um ativador <code>online</code> que se ativa automaticamente no servidor. Ele se baseia em um recurso do tipo <code>array de Strings</code>
     * que contém as informações necessárias para a conexão e autenticação. O array deve ser composto por 4 Strings, de acordo com a seguinte ordem:
     * <b>endereço do servidor</b>, <b>porta do servidor</b>, <b>nome do usuário</b>  e <b>senha</b>.<br>
     * <br>
     * Por exemplo:
     * <pre>{@code
     *  <string-array name="openmobster_auto_activation">
     *      <item>algumlugar.noip.me</item>
     *      <item>1502</item>
     *      <item>teste@teste.com.br</item>
     *      <item>asd123</item>
     *  </string-array>
     * }</pre>
     * @param autoActivationResId identificador do recurso do tipo <code>array de Strings</code> que contém as informações necessárias para a ativação.
     * @return o ativador criado.
     */
    public static OpenMobsterActivator createAutomaticOnlineActivator(int autoActivationResId) {
        if (autoActivationResId <= 0) {
            throw new IllegalArgumentException("autoActivationResId <= 0");
        }

        return new OpenMobsterActivator(null, autoActivationResId);
    }

    /**
     * Cria um ativador <code>online</code> que utiliza a forma padrão do OpenMobster de ser ativado em um servidor (digitada através de vários Dialogs).
     *
     * @return o ativador criado.
     */
    public static OpenMobsterActivator createDefaultOnlineActivator() {
        return new OpenMobsterActivator(null, null);
    }


	@Override
	public void onActivityStarted(Activity activity) {
		//Se já está rodando alguma task de ativação, deixa ela rolar.
        if (onlineAutoActivationListener != null) {
            onlineAutoActivationListener.setActivity(activity);
            return;
        }
        if (offlineActivationTask != null) {
            return;
        }

        boolean isOnline = offlinePopulationsResIds == null;
        OpenMobsterUtils.setup(activity, isOnline);


        Configuration config = Configuration.getInstance(activity);
        if (config.isActive()) {
            //Se já está ativado no modo offline, verifica se as populações precisam ser atualizadas.
            if (!isOnline && !offlinePopulationsUptodate) {
                //Define que todos os canais devem aguardar pela atualização dos dados (será revertido no final da task).
                config.setBooting(true);

                Context applicationContext = activity.getApplicationContext();
                offlineActivationTask = new OfflineDataUpdateTask(applicationContext, offlinePopulationsResIds);
                offlineActivationTask.start();
                offlinePopulationsUptodate = true;
            }
		} else {
            //Se o OpenMobster ainda não está ativado, é obrigado a fazer isto antes dos dados poderem ser acessados.
            if (isOnline) {
                if (onlineAutoActivationResId != null) {
                    String[] activationParams = activity.getResources().getStringArray(onlineAutoActivationResId);
                    autoActivateOnline(activity, activationParams);
                } else {
                    //Utiliza o mecanismo padrão de ativação online do OpenMobster.
                    AppActivation appActivation = AppActivation.getInstance(activity);
                    appActivation.start();
                }
            } else {
                Context applicationContext = activity.getApplicationContext();
                offlineActivationTask = new OfflineActivationTask(applicationContext, offlinePopulationsResIds);
                offlineActivationTask.start();
                offlinePopulationsUptodate = true;
            }
        }
	}

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (onlineAutoActivationListener != null) {
            onlineAutoActivationListener.setActivity(null);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

	@Override
	public void onActivityResumed(Activity activity) {
	}

	@Override
	public void onActivityPaused(Activity activity) {
	}

	@Override
	public void onActivityStopped(Activity activity) {
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
	}


	/*
	 * Métodos auxiliares
	 */

	private void autoActivateOnline(Activity activity, String[] activationParams) {
		if (activationParams.length != 4) {
			throw new IllegalArgumentException("The openmobster auto activation string array should have exactly 4 items: server ip, server port, user name and password.");
		}

		String serverIp = activationParams[0];
		if (TextUtils.isEmpty(serverIp)) {
			throw new IllegalArgumentException("The openmobster auto activation string at index 0 (server ip) is empty.");
		}
		int serverPort;
		try {
			serverPort = Integer.parseInt(activationParams[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("The openmobster auto activation string at index 1 (server port) is invalid: " + activationParams[1]);
		}
		String userName = activationParams[2];
		if (TextUtils.isEmpty(userName)) {
			throw new IllegalArgumentException("The openmobster auto activation string at index 2 (user name) is empty.");
		}
		String password = activationParams[3];
		if (TextUtils.isEmpty(password)) {
			throw new IllegalArgumentException("The openmobster auto activation string at index 3 (password) is empty.");
		}

        onlineAutoActivationListener = new AutoActivationListener(activity);
        new AutoActivationAsyncTask(serverIp, serverPort, userName, password, onlineAutoActivationListener).execute();
	}

    private final class AutoActivationListener implements DeviceActivationListener {

        private Activity activity;

        public AutoActivationListener(Activity activity) {

        }

        public void setActivity(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onFinishDeviceActivation(boolean success) {
            onlineAutoActivationListener = null;
            if (activity == null) {
                return;
            }

            //Se houve erro na conexão, apresenta isto para o usuário saber como agir.
            new AlertDialog.Builder(activity).
                    setTitle(R.string.auto_activation_error).
                    setMessage(R.string.unreachable_server_dialog_message).
                    setCancelable(false).
                    setPositiveButton(android.R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (activity != null) {
                                activity.finish();
                            }
                        }
                    }).
            show();
        }
    }
}
