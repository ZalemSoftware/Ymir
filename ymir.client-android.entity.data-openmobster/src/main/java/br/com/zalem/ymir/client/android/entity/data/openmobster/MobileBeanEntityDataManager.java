package br.com.zalem.ymir.client.android.entity.data.openmobster;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.app.AndroidBugsUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.openmobster.android.api.sync.MobileBean;
import org.openmobster.android.utils.OpenMobsterBugUtils;
import org.openmobster.android.utils.OpenMobsterBugUtils.NullableObject;
import org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObjectDatabase;
import org.openmobster.core.mobileCloud.android.module.sync.daemon.Daemon;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.IEntityDAO;
import br.com.zalem.ymir.client.android.entity.data.IEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.openmobster.cache.IEntityRecordImageCache;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadataConfig;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadataConfigValidator;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadataException;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityRelationship;
import br.com.zalem.ymir.client.android.entity.data.openmobster.query.MobileBeanQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.openmobster.util.OpenMobsterUtils;
import br.com.zalem.ymir.client.android.entity.data.query.IQueryBuilder;
import br.com.zalem.ymir.client.android.entity.data.query.IQueryStatement;
import br.com.zalem.ymir.client.android.entity.data.util.MetadataUtils;
import br.com.zalem.ymir.client.android.entity.data.view.RelationshipArrayView;
import br.com.zalem.ymir.client.android.util.SafeAsyncTask;

/**
 * Gerenciador de entidades baseado no {@link org.openmobster.android.api.sync.MobileBean} do OpenMobster.<br>
 * Funcina a partir dos metadados das entidades passados no construtor. Apenas as entidades cujo os metadados foram
 * definidos desta forma poderão ser acessadas posteriormente através este gerenciador.
 *
 * @author Thiago Gesser
 */
public final class MobileBeanEntityDataManager implements IEntityDataManager {
	
	private final Map<String, MobileBeanEntityDAO> daos;
	private IEntityRecordImageCache imageCache;
	
	public MobileBeanEntityDataManager(EntityMetadataConfig... metadatasConfigs) throws EntityMetadataException {
		EntityMetadataConfigValidator.validate(metadatasConfigs);
		OpenMobsterBugUtils openMobsterBugUtils = OpenMobsterBugUtils.getInstance();

		daos = new HashMap<>(metadatasConfigs.length);
		for (EntityMetadataConfig metadataConfig : metadatasConfigs) {
			//Transforma as configurações de metadadados nos metadados otimizados que serão utilizados por este manager.
			EntityMetadata metadata = new EntityMetadata(metadataConfig);
			
			daos.put(metadataConfig.getName(), new MobileBeanEntityDAO(metadata, this));
			
			//Contorno para um bug do OpenMobster que apagava os dados das entidade.
			if (!metadata.isInternal()) {
				openMobsterBugUtils.addPersistentChannels(metadataConfig.getChannel());
			}
		}
		
        //Inicializa os metadados dos relacionamentos
        Map<String, List<EntityRelationship>> referencesToEntities = new HashMap<>(metadatasConfigs.length);
		for (EntityMetadataConfig metadataConfig : metadatasConfigs) {
			MobileBeanEntityDAO dao = daos.get(metadataConfig.getName());
			EntityMetadata metadata = dao.getEntityMetadata();
			metadata.initializeRelationships(metadataConfig.getRelationships(), this);
			
			//Contorno para um problema do OpenMobster de não armazenar campos nulos no banco, o que afeta as queries.
			configureNullableContent(openMobsterBugUtils, metadata);

            //Separa os relacionamentos por entidade alvo para depois definí-los na própria entidade alvo.
            for (EntityRelationship relationship : metadata.getRelationshipsMap().values()) {
                //Apenas se for relacionado pela fonte trata-se de uma referência real.
                if (!relationship.isRelatedBySource()) {
                    continue;
                }
                String targetName = relationship.getTarget().getName();
                List<EntityRelationship> referencesToEntity = referencesToEntities.get(targetName);
                if (referencesToEntity == null) {
                    referencesToEntity = new ArrayList<>();
                    referencesToEntities.put(targetName, referencesToEntity);
                }
                referencesToEntity.add(relationship);
            }
		}

        //Define em cada entidade os relacionamentos que apontam para elas.
        for (MobileBeanEntityDAO dao : daos.values()) {
            EntityMetadata entityMetadata = dao.getEntityMetadata();

            List<EntityRelationship> referencesToEntityList = referencesToEntities.get(entityMetadata.getName());
            EntityRelationship[] relatioshipsToEntity = referencesToEntityList == null ? new EntityRelationship[0] : referencesToEntityList.toArray(new EntityRelationship[referencesToEntityList.size()]);
            entityMetadata.setReferencesToMe(relatioshipsToEntity);
        }
	}

	@Override
	public EntityMetadata getEntityMetadata(String entityName) {
		return getMobileBeanEntityDAO(entityName).getEntityMetadata();
	}
	
	@Override
	public EntityMetadata[] getEntitiesMetadatas() {
        EntityMetadata[] entitiesMetadatas = new EntityMetadata[daos.size()];
		int i = 0;
		for (MobileBeanEntityDAO entityDAO : daos.values()) {
			entitiesMetadatas[i++] = entityDAO.getEntityMetadata();
		}
		return entitiesMetadatas;
	}

	@Override
	public MobileBeanEntityDAO getEntityDAO(String entityName) {
		return getMobileBeanEntityDAO(entityName);
	}
	
	@Override
	public IEntityDAO getEntityDAO(RelationshipArrayView dataView) {
		MobileBeanEntityRecord baseRecord = (MobileBeanEntityRecord) dataView.getRecord();
		String relName = dataView.getRelationship().getName();
		
		/*
		 * Não permite uma visão de dados em que:
		 * 	- o registro base é interno e não possui dono definido (ainda não foi salvo dentro do dono, estando só em memória) ou que o dono seja novo (não está no banco);
		 * 	- o registro base não é interno e é novo (não está no banco) ou o relacionamento alvo está sujo (pois os registros que serão trazidos do banco podem estar diferente).
		 */
		if (baseRecord.getEntityMetadata().isInternal()) {
			//Não verifica se o relacionamento está sujo pois atualmente registros internos já começam com este tipo de relacionamento sujo (ToDo.txt - 15).
			MobileBeanEntityRecord owner = ((InternalMobileBeanEntityRecord) baseRecord).getOwner(); 
			if (owner == null) {
				throw new IllegalArgumentException(String.format("Invalid RelationshipArrayView: the base record is internal and is not saved within any owner. Base record entity = %s, relationship = %s.", baseRecord.getEntityMetadata().getName(), relName));
			}
			if (owner.isNew()) {
				throw new IllegalArgumentException(String.format("Invalid RelationshipArrayView: the base record is internal and his owner is new. Base record entity = %s, relationship = %s.", baseRecord.getEntityMetadata().getName(), relName));
			}
		} else if (baseRecord.isNew() || baseRecord.isDirty(relName)) {
			throw new IllegalArgumentException(String.format("Invalid RelationshipArrayView: the base record is new or the relationship array is dirty. Base record entity = %s, relationship = %s.", baseRecord.getEntityMetadata().getName(), relName));
		}
		
		RelationshipArrayViewDAO dao = new RelationshipArrayViewDAO(dataView, this);
		
		//Verifica se a entidade existe.
		getMobileBeanEntityDAO(dao.getEntityMetadata().getName());
		
		return dao;
	}
	
	@Override
	public IQueryStatement query() {
		return newQueryBuilder();
	}

    @Override
    public IQueryBuilder newQueryBuilder() {
        return new MobileBeanQueryBuilder(this);
    }

    @Override
    public void deleteAll(AbstractEntityRecordDeletionMonitor monitor, String... entitiesNames) {
        new EntityRecordDeletionAsyncTask(monitor).execute(entitiesNames);
    }


    /**
     * Inicia uma sincronização dos dados com a nuvem em background. A sincronização só é executada se o dispositivo estiver ativado na nuvem
     * e se não houver outra sincronização em andamento.
     */
    public void scheduleSync() {
        if (!OpenMobsterUtils.isDeviceActivated()) {
            return;
        }

        Daemon.getInstance().scheduleSyncInitiation();
    }

    /**
	 * Define o cache de imagens de registros que será utilizado por este gerenciador entidades de dados.<br>
	 * O cache será utilizado no acesso de dados dos tipos {@link EntityAttributeType#IMAGE} e {@link EntityAttributeType#IMAGE_ARRAY}.
	 * Ao tentar obter um dado de um destes tipos, será verificado antes se ele não está no cache. Em caso negativo, a imagem
	 * ou o array de imagens será obtido da maneira normal e depois colocado no cache.
	 * 
	 * @param imageCache o cache de imagens que será utilizado pelo gerenciador.
	 */
	public void setImageCache(IEntityRecordImageCache imageCache) {
		if (this.imageCache != null) {
			this.imageCache.onDetach(this);
		}
		
		this.imageCache = imageCache;
		imageCache.onAttach(this);
	}
	
	/**
	 * Obtém o cache de imagens utilizado por este gerenciador de entidades de dados.
	 * 
	 * @return o cache obtido ou <code>null</code> se não há um cache designado.
	 */
	public IEntityRecordImageCache getImageCache() {
		return imageCache;
	}

	/**
	 * Cria um MobileBeanEntityManager a partir de metadados de entidades obtidos através de recursos no formato json.
	 * 
	 * @param objectMapper mapeador que será utilizado na deserialização para as instâncias de EntityMetadata.
	 * @param context contexto Android.
	 * @param jsonResIds ids dos recursos json.
	 * @return o MobileBeanEntityManager criado.
	 * @throws EntityMetadataException se há algum problema nas definições dos metadados.
	 */
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
    public static MobileBeanEntityDataManager fromJsonResources(ObjectMapper objectMapper, Context context, int... jsonResIds) throws EntityMetadataException {
        org.openmobster.core.mobileCloud.android.util.OpenMobsterUtils.setDebug(AndroidBugsUtils.applyWorkaroundForBug52962(context));

		EntityMetadataConfig[] metadatasConfigs = new EntityMetadataConfig[jsonResIds.length];
		Resources resources = context.getResources();
		for (int i = 0; i < jsonResIds.length; i++) {
			int jsonResId = jsonResIds[i];
			try {
				InputStream configIn = resources.openRawResource(jsonResId);
				try {
					metadatasConfigs[i] = objectMapper.readValue(configIn, EntityMetadataConfig.class);
				} finally {
					configIn.close();			
				}
			} catch (IOException e) {
				throwJsonMetadataException(resources, jsonResId, e);
			}
		}
		
		return new MobileBeanEntityDataManager(metadatasConfigs);
	}
	
	
	/*
	 * Métodos/classes auxiliares
	 */
	
	private MobileBeanEntityDAO getMobileBeanEntityDAO(String entityName) {
		MobileBeanEntityDAO dao = daos.get(entityName);
		if (dao == null) {
			throw new IllegalArgumentException("There is no entity with the name: " + entityName);
		}
		
		return dao;
	}

	private static void throwJsonMetadataException(Resources resources, int jsonResId, Exception e) {
		String jsonResName = resources.getResourceEntryName(jsonResId);
		throw new RuntimeException("An error occurred during the parse of the following metadata resource: " + jsonResName, e);
	}
	
	private void configureNullableContent(OpenMobsterBugUtils openMobsterBugUtils, EntityMetadata metadata) {
		List<String> nullableFields = new ArrayList<>();
		for (IEntityAttribute attribute : metadata.getAttributesMap().values()) {
			//Só os atributos que podem ser utilizados nas queries são relevantes para o contorno.
			if (MobileBeanQueryBuilder.isSupportedAttribute(attribute)) {
				nullableFields.add(attribute.getName());
			}
		}
		List<NullableObject> nullableObjects = new ArrayList<>();
		for (EntityRelationship relationship : metadata.getRelationshipsMap().values()) {
			EntityMetadata relEntity = relationship.getTarget();
			if (relEntity.isInternal()) {
				/*
				 * Apenas composições se tratam de objetos internos. Associações são apenas para referenciar o dono.
				 * Por exemplo, no seguinte cenário: "registro -> objetos internos pais -> objetos internos filhos", o
				 * "objeto interno filho" pode conter uma associação para o "objeto interno pai" para ter acesso a seu dono (que também é interno).
				 */
				if (MetadataUtils.isComposition(relationship)) {
					//Se for um relacionamento interno, precisa de um tratamento especial pois os dados do registro interno são armazenados dentro do registro dono.
					NullableObject nullableObject = new NullableObject(relationship.getName(), relEntity.getName(), !MetadataUtils.isSingleRelationship(relationship));
					nullableObjects.add(nullableObject);
				}
			} else {
				//Só os relacionamentos que podem ser utilizados nas queries são relevantes para o contorno.
				if (MobileBeanQueryBuilder.isSupportedRelationship(relationship)) {
					nullableFields.add(relationship.getName());
				}
			}
		}
		
		if (!metadata.isInternal()) {
			nullableFields.add(MobileBeanEntityRecord.TAGS_PROPERTY_NAME);
		}
		
		String[] nullableFieldsArray = nullableFields.isEmpty() ? null : nullableFields.toArray(new String[nullableFields.size()]);
		NullableObject[] nullableObjectsArray = nullableObjects.isEmpty() ? null : nullableObjects.toArray(new NullableObject[nullableObjects.size()]);
		if (metadata.isInternal()) {
			if (nullableFieldsArray != null) {
				openMobsterBugUtils.addObjectNullableFields(metadata.getName(), nullableFieldsArray);
			}
			if (nullableObjectsArray != null) {
				openMobsterBugUtils.addNullableObjects(metadata.getName(), nullableObjectsArray);
			}
		} else {
			if (nullableFieldsArray != null) {
				openMobsterBugUtils.addChannelNullableFields(metadata.getChannel(), nullableFieldsArray);
			}
			if (nullableObjectsArray != null) {
				openMobsterBugUtils.addNullableObjects(metadata.getChannel(), nullableObjectsArray);
			}
		}
	}


    /**
     * Task que executa a operação de exclusão dos registros das entidades.
     */
    private final class EntityRecordDeletionAsyncTask extends SafeAsyncTask<String, Void, Void> {

        private final AbstractEntityRecordDeletionMonitor monitor;
        private Handler handler;

        public EntityRecordDeletionAsyncTask(AbstractEntityRecordDeletionMonitor monitor) {
            this.monitor = monitor;
        }

        @Override
        protected void onPreExecute() {
            if (monitor != null) {
                monitor.beforeDeleteRecords();
                //Utiliza um handler para postar o "onDeleteRecord" na Thread de UI.
                handler = new Handler();
            }
        }

        @Override
        protected Void safeDoInBackground(String... entitiesNames) throws Exception {
            MobileBean.beginTransaction();
            try {
                MobileObjectDatabase db = MobileObjectDatabase.getInstance();
                for (String entityName : entitiesNames) {
                    if (monitor != null && monitor.isCanceled()) {
                        return null;
                    }

                    String channel = getEntityMetadata(entityName).getChannel();
                    db.deleteAll(channel);
                }

                if (monitor != null) {
                    //Garante que definiu como terminando antes da última verificação do cancelmaneto.
                    monitor.setFinishing(true);

                    if (monitor.isCanceled()) {
                        return null;
                    }
                }

                MobileBean.setTransactionSuccessful();
                return null;
            } finally {
                if (monitor != null) {
                    monitor.setFinishing(true);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            monitor.onDeleteRecords();
                        }
                    });
                    handler = null;
                }

                MobileBean.endTransaction();
            }
        }

        @Override
        protected void safeOnPostExecute(Void aVoid) {
            if (monitor != null) {
                monitor.afterDeleteRecords();
            }
        }
    }
}
