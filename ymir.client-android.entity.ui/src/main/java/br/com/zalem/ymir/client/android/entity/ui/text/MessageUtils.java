package br.com.zalem.ymir.client.android.entity.ui.text;

import android.content.Context;

import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityConfig;
import br.com.zalem.ymir.client.android.entity.ui.configuration.IEntityUIConfigManager;

/**
 * Utilitario para a montagem de mensagens exibidas para o usuário.
 *
 * @author Thiago Gesser
 */
public final class MessageUtils {

    private MessageUtils() {
    }

    /**
     * Cria uma mensagem a partir de um formato e valores.
     *
     * @param context contexto.
     * @param msgFormatResId id do recurso do formato da mensagem.
     * @param values valores que irão compor a mensagem.
     * @return a mensagem criada.
     */
    public static String createMessage(Context context, int msgFormatResId, String... values) {
        return String.format(context.getString(msgFormatResId), (Object[]) values); //cast para evitar warning do gradle por estar mandando um vararg de tipo diferente.
    }

    /**
     * Cria uma lista de palavras de forma legivel para o usuario.
     *
     * @param context contexto.
     * @param words palavras da lista.
     * @return a lista criada.
     */
    public static String createWordsList(Context context, String... words) {
        if (words.length == 0) {
            return "";
        }
        if (words.length == 1) {
            return words[0];
        }

        String separator = context.getString(R.string.word_list_separator);
        String lastSeparator = context.getString(R.string.word_list_last_separator);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                if (i < words.length -1) {
                    builder.append(separator);
                } else {
                    builder.append(lastSeparator);
                }
            }
            builder.append(words[i]);
        }
        return builder.toString();
    }


    /**
     * Cria uma lista com os nomes de exibição das entidades.
     *
     * @param configManager gerenciador de configurações visuais das entidades.
     * @param plural <code>true</code> para obter os nomes em plural e <code>false</code> para singular.
     * @param entities nomes das entidades.
     * @return a lista criada.
     */
    public static String createEntitiesDisplayList(Context context, IEntityUIConfigManager configManager, boolean plural, String... entities) {
        String[] entitiesDisplayNames = new String[entities.length];
        for (int i = 0; i < entities.length; i++) {
            entitiesDisplayNames[i] = getEntityDisplayName(configManager, entities[i], plural);
        }
        return createWordsList(context, entitiesDisplayNames);
    }

    /**
     * Obtém o nome de exibição da entidade.
     *
     * @param configManager gerenciador de configurações visuais das entidades.
     * @param plural <code>true</code> para obter os nomes em plural e <code>false</code> para singular.
     * @param entity nome da entidade.
     * @return o nome de exibição obtido ou o próprio nome da entidade caso ela não possua configuração definida.
     */
    public static String getEntityDisplayName(IEntityUIConfigManager configManager, String entity, boolean plural) {
        IEntityConfig entityConfig = configManager.getEntityConfig(entity);
        if (entityConfig == null) {
            return entity;
        }
        return entityConfig.getDisplayName(plural);
    }
}
