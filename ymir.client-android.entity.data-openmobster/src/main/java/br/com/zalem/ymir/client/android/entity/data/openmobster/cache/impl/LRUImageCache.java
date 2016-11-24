package br.com.zalem.ymir.client.android.entity.data.openmobster.cache.impl;

import android.graphics.Bitmap;
import android.util.LruCache;

import org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObject;
import org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObjectDatabase;
import org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObjectDatabase.IMobileObjectDatabaseListener;
import org.openmobster.core.mobileCloud.android.service.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.metadata.EntityAttributeType;
import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityAttribute;
import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;
import br.com.zalem.ymir.client.android.entity.data.openmobster.cache.IEntityRecordImageCache;
import br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.EntityMetadata;

/**
 * Cache de imagens de registros de entidades com tamanho limitado e baseado no algoritmo <code>Least Recently Used</code>.<br>
 * Cada imagem ({@link android.graphics.Bitmap}) possui um tamanho que pode ser determinado bytes. Por isto, o tamanho máximo do cache
 * é definido em bytes também. As imagens são mantidas em uma fila, sendo que as novas são adicionadas no início dela. Cada vez que uma imagem é
 * acessada do cache, ela volta para o início da fila. Quando o limite de tamanho do cache é alcançado, as imagens do
 * final da fila são removidas até que o limite seja mantido novamente.<br>
 * <br>
 * O próprio LRUImageCache se encarrega de remover as imagens de registros que foram atualizados ou excluídos da fonte
 * de dados do OpenMobster.<br>
 * 
 * @see android.graphics.Bitmap
 * @see org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObjectDatabase
 *
 * @author Thiago Gesser
 */
public final class LRUImageCache extends LruCache<String, Bitmap[]> implements IEntityRecordImageCache, IMobileObjectDatabaseListener {
	
	//Utilizado para saber quais imagens remover do cache quando um registro for atualziado ou excluído.
	private Map<String, String[]> entitiesImgAttrs;


	/**
	 * Cria um LRUImageCache de acordo com o tamanho máximo, em bytes.
	 * 
	 * @param maxSize o tamanho máximo do cache, em bytes.
	 */
	public LRUImageCache(int maxSize) {
		super(maxSize);
	}
	
	@Override
	protected int sizeOf(String key, Bitmap[] images) {
		int size = 0;
		for (Bitmap image : images) {
			if (image == null) {
				continue;
			}
			size += image.getByteCount();
		}
		return size;
	}

	@Override
	public void addImage(String entityChannel, String recordId, String attribute, Bitmap image) {
		put(makeKey(entityChannel, recordId, attribute), new Bitmap[] {image});
	}

	@Override
	public void addImageArray(String entityChannel, String recordId, String attribute, Bitmap[] imageArray) {
		put(makeKey(entityChannel, recordId, attribute), imageArray);
	}

	@Override
	public Bitmap getImage(String entityChannel, String recordId, String attribute) {
		Bitmap[] images = get(makeKey(entityChannel, recordId, attribute));
		if (images == null) {
			return null;
		}
		
		//Se o tamanho for diferente de 1 ou se o primeiro valor for nulo, se trata de uma entrada do tipo array.
		Bitmap image = images.length == 1 ? images[0] : null;
		if (image == null) {
			throw new IllegalArgumentException(String.format("The cache entry referenced by %s, %s and %s is an array of images.", entityChannel, recordId, attribute));
		}
		return image;
	}

	@Override
	public Bitmap[] getImageArray(String entityChannel, String recordId, String attribute) {
		return get(makeKey(entityChannel, recordId, attribute));
	}
	
	
	@Override
	public void onAttach(MobileBeanEntityDataManager entityManager) {
		//Mapeia as entidades que possuem atributos com imagens para saber quais imagens remover do cache quando determinado registro é atualizado / excluído.
		entitiesImgAttrs = new HashMap<>();
        EntityMetadata[] entitiesMetadatas = entityManager.getEntitiesMetadatas();
		for (EntityMetadata entityMetadata : entitiesMetadatas) {
			ArrayList<String> imgAttrs = new ArrayList<>();
			for (IEntityAttribute attr : entityMetadata.getAttributes()) {
				if (attr.getType() == EntityAttributeType.IMAGE || attr.getType() == EntityAttributeType.IMAGE_ARRAY) {
					imgAttrs.add(attr.getName());
				}
			}
			
			if (!imgAttrs.isEmpty()) {
				entitiesImgAttrs.put(entityMetadata.getChannel(), imgAttrs.toArray(new String[imgAttrs.size()]));
			}
		}
		
		//Adiciona-se como listener da criação/atualização/exclusão de objetos do OpenMobster.
		if (Registry.isActiveAndStarted()) {
			MobileObjectDatabase.getInstance().addListener(this);
		} else {
			Registry.executeAfterStart(new Runnable() {
				@Override
				public void run() {
					MobileObjectDatabase.getInstance().addListener(LRUImageCache.this);
				}
			}, true);
		}
	}

	@Override
	public void onDetach(MobileBeanEntityDataManager entityManager) {
		MobileObjectDatabase.getInstance().removeListener(this);
		entitiesImgAttrs = null;
	}
	
	
	@Override
	public void onMobileObjectUpdated(MobileObject mo) {
		removeImagesFrom(mo);
	}

	@Override
	public void onMobileObjectDeleted(MobileObject mo) {
		removeImagesFrom(mo);
	}

    @Override
    public void onAllMobileObjectsDeleted(String channel) {
        //Por enquanto limpa o cache inteiro. Se for interessante no futuro, verificar uma forma (que não impacte em performance) de obter todas as chaves do channel e tirar apenas elas do cache.
        evictAll();
    }

	@Override
	public void onMobileObjectCreated(MobileObject mo, String id) {
		//Não faz nada quando um registro é adicionado.
	}

    @Override
    public void beforeMobileObjectCreated(MobileObject mobileObject) {
    }

    @Override
    public void beforeMobileObjectDeleted(MobileObject mobileObject) {
    }

    @Override
    public void beforeMobileObjectUpdated(MobileObject mobileObject) {
    }


    /*
	 * Métodos auxiliares
	 */
	
	private void removeImagesFrom(MobileObject mo) {
		String entityChannel = mo.getStorageId();
		String[] imgsAttrs = entitiesImgAttrs.get(entityChannel);
		//Se não possui entrada no mapa, não há imagens para remover do cache.
		if (imgsAttrs == null) {
			return;
		}
		
		//Remove as imagens dos atributos de imagens do registro.
		String recordId = mo.getRecordId();
		for (String imgAttr : imgsAttrs) {
			remove(makeKey(entityChannel, recordId, imgAttr));
		}
	}
	
	private static String makeKey(String entityChannel, String recordId, String attribute) {
		return entityChannel + ":" + recordId + "." + attribute;
	}
	
}