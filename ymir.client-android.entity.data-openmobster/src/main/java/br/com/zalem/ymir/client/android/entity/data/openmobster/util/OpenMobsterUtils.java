package br.com.zalem.ymir.client.android.entity.data.openmobster.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.openmobster.core.mobileCloud.android.configuration.AppSystemConfig;
import org.openmobster.core.mobileCloud.android.configuration.Configuration;
import org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObjectDatabase;
import org.openmobster.core.mobileCloud.android.module.sync.SyncException;
import org.openmobster.core.mobileCloud.android.module.sync.SyncService;
import org.openmobster.core.mobileCloud.android.storage.DBException;
import org.openmobster.core.mobileCloud.android_native.framework.CloudService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import br.com.zalem.ymir.client.android.entity.data.openmobster.util.MobileBeanJsonSerializer.MobileBeanChannelDataInfo;

/**
 * Disponibiliza métodos para a utilização do OpenMobster, agregando funções que estão espalhadas em várias partes diferentes em um único local.
 *
 * @author Thiago Gesser
 */
public final class OpenMobsterUtils {

    private OpenMobsterUtils() {
    }


    /**
     * Chama o método {@link #setup(Activity, boolean, boolean)} passando <code>true</code> para o parâmetro <code>sync</code>.
     *
     * @param activity Activity atual.
     * @param online <code>true</code> se o dispositivo utilizará as funções online do OpenMobster e <code>false</code> caso contrário.
     */
    public static void setup(Activity activity, boolean online) {
        setup(activity, online, true);
    }

    /**
     * Inicia toda a estrutura do OpenMobster. Deve ser chamado antes de qualquer interação com seus serviços.<br>
     * A estrutura do OpenMobster pode ser parada através do método {@link #shutdown(Context)}.
     *
     * @param activity Activity.
     * @param online <code>true</code> se o dispositivo utilizará as funções online do OpenMobster e <code>false</code> caso contrário.
     * @param sync <code>true</code> para disparar uma sincrinização inicial e <code>false</code> caso contrário. Só é utilizado se for <code>online</code>.
     */
    public static void setup(Activity activity, boolean online, boolean sync) {
        if (online) {
            //Inicializa o container e os serviços de sincronização do OpenMobster, se ainda não foram iniciados.
            CloudService.getInstance().start(activity, sync);
        } else {
            //Se for offline, não precisa ativar os serviços de sincronização e sim apenas o container.
            CloudService.getInstance().startContainerOffline(activity);
        }
    }

    /**
     * Encerra toda a estrutura do OpenMobster. Deve ser chamado antes do {@link #setup(Activity, boolean, boolean)} caso a estrutura já
     * esteja iniciada.
     *
     * @param context contexto.
     */
    public static void shutdown(Context context) {
        CloudService.getInstance().stop(context);
    }


    /**
     * Chama o método {@link #activateDevice(String, int, String, String, DeviceActivationListener)} passando o ip e porta do servidor
     * definidos no arquivo de configuração do OpenMobster (openmobster-app.xml).
     *
     * @param userName nome do usuário, geralmente um email.
     * @param password senha do usuário. Pode ser o próprio nome caso esta autenticação não seja utilizada em favor de um oAuth, por exemplo.
     * @param listener listener de ativação do dispositivo no servidor. Pode ser nulo.
     */
    public static void activateDevice(String userName, String password, DeviceActivationListener listener) {
        CloudService cloudService = CloudService.getInstance();
        activateDevice(cloudService.getServer(), cloudService.getPort(), userName, password, listener);
    }

    /**
     * Ativa o dispositivo no servidor do OpenMobster, habilitando-o para utilizar as funções online.<br>
     * O dispositivo pode ser desativado (desligado) do servidor através do método {@link #deactivateDevice()}.
     *
     * @param serverIp ip do servidor.
     * @param serverPort porta do servidor.
     * @param userName nome do usuário, geralmente um email.
     * @param password senha do usuário. Pode ser o próprio nome caso esta autenticação não seja utilizada em favor de um oAuth, por exemplo.
     * @param listener listener de ativação do dispositivo no servidor. Pode ser nulo.
     */
    public static void activateDevice(String serverIp, int serverPort, String userName, String password, DeviceActivationListener listener) {
        new AutoActivationAsyncTask(serverIp, serverPort, userName, password, listener).execute();
    }

    /**
     * Desativa o dispositivo do servidor do OpenMobster, tornando-o incapaz de utilizar as funções online. Isto apenas desliga-o do contato
     * com o servidor, não apagando nenhum dado enviado previamente.<br>
     * O dispositivo pode ser ativado novamente através do método {@link #activateDevice(String, int, String, String, DeviceActivationListener)}.
     */
    public static void deactivateDevice() {
        CloudService.getInstance().deactivateDevice();
    }

    /**
     * Indica se o dispositivo está ativado para utilizar as funções online do OpenMobster.
     *
     * @return <code>true</code> se o dispositivo está ativado e <code>false</code> caso contrário.
     */
    public static boolean isDeviceActivated() {
        return CloudService.getInstance().isDeviceActivated();
    }


    /**
     * Popula os canais com os dados definidos nos arquivos referenciados pelos identificadores. Os arquivos devem ter sido gerados
     * pelo {@link MobileBeanJsonSerializer}.<br>
     * <br>
     * Se é a primeira vez que o método é chamado no dispositivo, todos os canais são inicializados após a inserção de dados, tornando-os
     * prontos para o uso. Os canais que não possuiam população ficam vazios.<br>
     * Se não é a primeira vez que o método é chamado, os dados de cada canal são substituídos se a versão definida no arquivo for maior que a
     * versão da população atual.
     *
     * @param context contexto.
     * @param populationsResIds identificadores dos arquivos de população.
     */
    public static void populateChannels(Context context, int... populationsResIds) {
        Context applicationContext = context.getApplicationContext();
        Configuration config = Configuration.getInstance(applicationContext);

        Thread task;
        if (config.isActive()) {
            task = new OfflineDataUpdateTask(applicationContext, populationsResIds);
        } else {
            task = new OfflineActivationTask(applicationContext, populationsResIds);
        }
        task.start();
    }

    /**
     * Define se os canais estão inicializados e prontos para o uso.<br>
     * Se eles estiverem sendo marcados como inicializados, uma sincronização dos canais será disparada.
     *
     * @param context contexto.
     * @param booted <code>true</code> para definir que os canais estão inicializados e <code>false</code> caso contrário.
     * @param channels canais de dados.
     */
    public static void bootChannels(Context context, boolean booted, String... channels) {
        Context applicationContext = context.getApplicationContext();
        Configuration configuration = Configuration.getInstance(applicationContext);
        for (String channel : channels) {
            configuration.setBooted(channel, booted);
        }
        configuration.save(context);

        if (isDeviceActivated()) {
            CloudService.getInstance().backgroundSync();
        }
    }

    /**
     * Limpa o log de alterações (criações, mudanças e exclusões) nos dados dos canais.
     */
    public static void clearChangelog() {
        try {
            SyncService syncService = SyncService.getInstance();
            if (syncService != null) {
                SyncService.getInstance().clearChangeLog();
            }
        } catch (SyncException e) {
            //Ignora o erro por se tratar de apenas uma limpeza.
            Log.e(OpenMobsterUtils.class.getSimpleName(), "Error cleaning the changelog", e);
        }
    }


	/*
	 * Métodos/classes auxiliares
	 */

    private static SharedPreferences getOfflineActivationDataVersions(Context context) {
        //Chave = nome do canal, valor = versão da população
        return context.getSharedPreferences("OfflinePopulationsVersions", Context.MODE_PRIVATE);
    }


    /**
     * Thread que executa o processo de ativação offline do OpenMobster.
     */
    static final class OfflineActivationTask extends Thread {

        private final Context context;
        private final int[] populationsResIds;

        public OfflineActivationTask(Context context, int[] populationsResIds) {
            this.context = context;
            this.populationsResIds = populationsResIds;
        }

        @Override
        @SuppressWarnings("TryFinallyCanBeTryWithResources")
        public void run() {
            Configuration config = Configuration.getInstance(context);

            try {
                AppSystemConfig appConfig = AppSystemConfig.getInstance();
                Set<String> channels = appConfig.getChannels();


                //Inicializa os canais que possuem população primeiro. Desta forma, partes da aplicação já podem ir sendo utilizadas.
                Resources resources = context.getResources();
                MobileBeanJsonSerializer serializer = new MobileBeanJsonSerializer(context);
                MobileObjectDatabase moDB = MobileObjectDatabase.getInstance();
                Editor dataVersionsEditor = getOfflineActivationDataVersions(context).edit();
                for (int populationResId : populationsResIds) {
                    InputStream dataIn = resources.openRawResource(populationResId);
                    MobileBeanChannelDataInfo info;
                    try {
                        info = serializer.deserialize(dataIn, false);
                    } finally {
                        dataIn.close();
                    }

                    //Seta a versão para depois saber se precisa atualizar ou não os dados.
                    String channel = info.getChannelName();
                    Integer version = info.getVersion();
                    if (version != null) {
                        dataVersionsEditor.putInt(channel, version);
                    }
                }
                dataVersionsEditor.apply();

                //Inicializa todos os canais de uma vez para manter a ordem definida, que é importante para a verificação do Configuration.isBooted().
                for (String channel : channels) {
                    initializeChannel(channel, config, moDB);
                }
            } catch (DBException | IOException e) {
                throw new RuntimeException(e);
            }

            //Define que a app está pronta para ser utilizada e salva a configuração.
            config.setActive(true);
            config.save(context);
        }

        private void initializeChannel(String channel, Configuration config, MobileObjectDatabase moDB) throws DBException {
            //Cria a tabela no banco se ainda nao existir.
            moDB.checkStorage(context, channel);

            //Adiciona o canal na configuraçao e já coloca-o como pronto para uso.
            config.addMyChannel(channel);
            config.setBooted(channel);
        }
    }

    /**
     * Thread que executa o processo de atualização dos dados provenientes de uma ativação offline do OpenMobster.
     */
    static final class OfflineDataUpdateTask extends Thread {

        private final Context context;
        private final int[] populationsResIds;

        public OfflineDataUpdateTask(Context context, int[] populationsResIds) {
            this.context = context;
            this.populationsResIds = populationsResIds;
        }

        @Override
        @SuppressWarnings("TryFinallyCanBeTryWithResources")
        public void run() {
            Configuration config = Configuration.getInstance(context);

            try {
                Resources resources = context.getResources();
                MobileBeanJsonSerializer serializer = new MobileBeanJsonSerializer(context);
                SharedPreferences dataVersions = getOfflineActivationDataVersions(context);
                Editor dataVersionsEditor = dataVersions.edit();
                for (int populationResId : populationsResIds) {
                    MobileBeanChannelDataInfo info;
                    InputStream dataIn = resources.openRawResource(populationResId);
                    try {
                        info = serializer.getInfo(dataIn);
                    } finally {
                        dataIn.close();
                    }

                    String channel = info.getChannelName();
                    Integer populationVersion = info.getVersion();
                    int currentDataVersion = dataVersions.getInt(channel, -1);
                    //Se a versão da nova população é maior do que a versão atual dos dados, atualiza-os.
                    if (populationVersion != null && populationVersion > currentDataVersion) {
                        dataIn = resources.openRawResource(populationResId);
                        try {
                            serializer.deserialize(dataIn, true);
                        } finally {
                            dataIn.close();
                        }
                        dataVersionsEditor.putInt(channel, populationVersion);
                    }

                }
                dataVersionsEditor.apply();
            } catch (DBException | IOException e) {
                throw new RuntimeException(e);
            } finally {
                //Define que todos os canais já podem ser utilizados normalmente.
                config.setBooting(false);
            }
        }
    }
	
	/**
	 * Task que executa a ativação automática do OpenMobster em background, baseado nos dados passados no construtor.
	 * Se ocorrer um erro na ativação, será mostrado um dialogo para o usuário e a Activity será terminada ao pressionar o Ok.
	 */
	static final class AutoActivationAsyncTask extends AsyncTask<Void, Void, Boolean> {
		
		private final String serverIp;
		private final int serverPort;
		private final String userName;
		private final String password;
        private final DeviceActivationListener listener;

		public AutoActivationAsyncTask(String serverIp, int serverPort, String userName, String password, DeviceActivationListener listener) {
			this.serverIp = serverIp;
			this.serverPort = serverPort;
			this.userName = userName;
			this.password = password;
            this.listener = listener;
        }
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
                CloudService.getInstance().activateDevice(serverIp, serverPort, userName, password);
                return true;
			//É obrigado a usar um "catch Exception" pq o openmobster lança uns RuntimeException aqui no meio.
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

        @Override
		protected void onPostExecute(Boolean success) {
            if (listener != null) {
                listener.onFinishDeviceActivation(success);
            }
		}
	}

    /**
     * Listener da finalização do processo de ativação (online) do dispositivo no servidor.
     */
    public interface DeviceActivationListener {

        /**
         * Chamado quando o processo de ativação terminou.
         *
         * @param success <code>true</code> se o dispositivo foi ativado com sucesso e <code>false</code> caso contrário.
         */
        void onFinishDeviceActivation(boolean success);
    }
}
