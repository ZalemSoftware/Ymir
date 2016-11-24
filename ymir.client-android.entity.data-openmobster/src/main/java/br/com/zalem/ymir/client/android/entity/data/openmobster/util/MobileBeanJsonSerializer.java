package br.com.zalem.ymir.client.android.entity.data.openmobster.util;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObject;
import org.openmobster.core.mobileCloud.android.module.mobileObject.MobileObjectDatabase;
import org.openmobster.core.mobileCloud.android.storage.DBException;
import org.openmobster.core.mobileCloud.android.storage.Database;
import org.openmobster.core.mobileCloud.android.storage.Record;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 * Serializador/deserializador de dados de MobileBean em arquivos no formato <code>json</code>.
 * Permite salvar todos os dados de determinado canal em um arquivo json através dos métodos {@link #serialize(String, OutputStream)} e {@link #serialize(String, JsonGenerator)},
 * podendo recuperá-los através dos métodos {@link #deserialize(InputStream, boolean)} e {@link #deserialize(JsonParser, boolean)}.
 *
 * @author Thiago Gesser
 */
public class MobileBeanJsonSerializer {

    private static final String CHANNEL_FIELD = "channelName";
    private static final String VERSION_FIELD = "version";
    private static final String RECORDS_FIELD = "records";

    private final ObjectMapper objectMapper;
    private final Context context;

    public MobileBeanJsonSerializer(Context context) {
        this(context, new ObjectMapper());
        objectMapper.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        objectMapper.enable(Feature.ALLOW_COMMENTS);
    }

    public MobileBeanJsonSerializer(Context context, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.context = context;
    }

    /**
     * Chama o método {@link #serialize(String, OutputStream, Integer)} passando <code>null</code> como versão.
     *
     * @param channelName o nome do canal cujo os dados serão serializados.
     * @param out saída de dados, normalmente um arquivo.
     */
    public void serialize(String channelName, OutputStream out) throws DBException, IOException {
        serialize(channelName, out, null);
    }

    /**
     * Serializa todos os Mobile Beans de determinado canal contidos no banco de dados do OpenMobster para a saída de dados no formato <code>json</code>.<br>
     * Os Mobile Beans podem ser recuperados posteriormente através do método {@link #deserialize(InputStream, boolean)}.
     *
     * @param channelName o nome do canal cujo os dados serão serializados.
     * @param out saída de dados, normalmente um arquivo.
     * @param version versão dos dados.
     */
    public void serialize(String channelName, OutputStream out, Integer version) throws DBException, IOException {
        MobileBeanChannelData data = getChannelData(channelName, version);
        objectMapper.writeValue(out, data);
    }

    /**
     * Chama o metodo {@link #serialize(String, JsonGenerator, Integer)} passando <code>null</code> como versão.
     *
     * @param channelName o nome do canal cujo os dados serão serializados.
     * @param generator gerador de json.
     */
    public void serialize(String channelName, JsonGenerator generator) throws DBException, IOException {
        serialize(channelName, generator, null);
    }

    /**
     * Serializa todos os Mobile Beans de determinado canal contidos no banco de dados do OpenMobster para o gerador de json.<br>
     * Os Mobile Beans podem ser recuperados posteriormente através do método {@link #deserialize(JsonParser, boolean)}.
     *
     * @param channelName o nome do canal cujo os dados serão serializados.
     * @param generator gerador de json.
     * @param version versão dos dados.
     */
    public void serialize(String channelName, JsonGenerator generator, Integer version) throws DBException, IOException {
        MobileBeanChannelData data = getChannelData(channelName, version);
        objectMapper.writeValue(generator, data);
    }

    /**
     * Deserializa todos os Mobile Beans contidos na entrada de dados no formato <code>json</code>, de acordo com um dos métodos de serialização desta classe.
     * Os Mobile Beans são adicionandos ao banco de dados do OpenMobster.
     *
     * @param in entrada de dados, normalmente um arquivo.
     * @param deleteAll define se os dados anteriores do canal devem ser excluídos.
     * @return informações sobre os dados do canal cujo os MobileBeans foram deserializados.
     */
    public MobileBeanChannelDataInfo deserialize(InputStream in, boolean deleteAll) throws IOException, DBException {
        MobileBeanChannelData data = objectMapper.readValue(in, MobileBeanChannelData.class);
        if (deleteAll) {
            MobileObjectDatabase.getInstance().deleteAll(data.getChannelName());
        }
        insertChannelData(data);

        data.setRecords(null);
        return data;
    }

    /**
     * Deserializa todos os Mobile Beans contidos no parser json, o qual deve estar no formato definido pelos métodos de serialização desta classe.
     * Os Mobile Beans são adicionandos ao banco de dados do OpenMobster.
     *
     * @param parser parser json
     * @param deleteAll define se os dados anteriores do canal devem ser excluídos.
     *               .
     * @return informações sobre os dados do canal cujo os MobileBeans foram deserializados.
     */
    public MobileBeanChannelDataInfo deserialize(JsonParser parser, boolean deleteAll) throws IOException, DBException {
        MobileBeanChannelData data = objectMapper.readValue(parser, MobileBeanChannelData.class);
        if (deleteAll) {
            MobileObjectDatabase.getInstance().deleteAll(data.getChannelName());
        }
        insertChannelData(data);

        data.setRecords(null);
        return data;
    }


    /**
     * Obtem as informaçoes sobre os dados de um canal em formato json, gerado por um dos métodos de serialização desta classe.
     *
     * @param in entrada de dados, normalmente um arquivo.
     * @return as informações obtidas
     */
    public MobileBeanChannelDataInfo getInfo(InputStream in) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(in);
        try {
            String channelName = null;
            Integer version = null;
            for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
                String fieldName = parser.getCurrentName();

                if (CHANNEL_FIELD.equals(fieldName)) {
                    channelName = parser.nextTextValue();
                    continue;
                }
                if (VERSION_FIELD.equals(fieldName)) {
                    int versionInt = parser.nextIntValue(-1);
                    version = versionInt == -1 ? null : versionInt;
                    continue;
                }

                //Se chegou nos records, não gasta tempo indo procurar a versão...
                if (RECORDS_FIELD.equals(fieldName)) {
                    break;
                }
            }

            return new MobileBeanChannelDataInfo(channelName, version);
        } finally {
            parser.close();
        }
    }


    /*
     * Métodos / classes auxiliares
     */

    @SuppressWarnings("unchecked")
    private MobileBeanChannelData getChannelData(String channelName, Integer version) throws DBException {
        if (version != null && version < 0) {
            throw new IllegalArgumentException("version < 0");
        }

        Set<Record> records = Database.getInstance(context).selectAll(channelName, true);
        Map<String, String>[] recordsValues = null;
        if (records != null) {
            recordsValues = new Map[records.size()];
            int i = 0;
            for (Record record : records) {
                recordsValues[i++] = record.getState();
            }
        }

        return new MobileBeanChannelData(channelName, version, recordsValues);
    }

    private void insertChannelData(MobileBeanChannelData data) {
        Map<String, String>[] records = data.getRecords();
        if (records != null) {
            //Salva a partir de MobileObject ao invés de Record para utilizar o contorno para o problema de valores nulos do OpenMobster.
            MobileObjectDatabase objDB = MobileObjectDatabase.getInstance();
            for (Map<String, String> recordValues : records) {
                Record record = new Record(recordValues);
                MobileObject mo = new MobileObject(record);
                objDB.create(mo);
            }
        }
    }

    /**
     * Representa as informações dos dados de um canal.
     */
    public static class MobileBeanChannelDataInfo {
        private String channelName;
        private Integer version;

        //Utilizado pelo Jackson.
        private MobileBeanChannelDataInfo() {
        }

        private MobileBeanChannelDataInfo(String channelName, Integer version) {
            this.channelName = channelName;
            this.version = version;
        }

        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }
    }

    /**
     * Representa os dados dos Mobile Beans de um canal.
     */
    private static final class MobileBeanChannelData extends MobileBeanChannelDataInfo {

        private Map<String, String>[] records;

        //Utilizado pelo Jackson.
        private MobileBeanChannelData() {
        }

        private MobileBeanChannelData(String channelName, Integer version, Map<String, String>[] records) {
            super(channelName, version);
            this.records = records;
        }

        public Map<String, String>[] getRecords() {
            return records;
        }

        public void setRecords(Map<String, String>[] records) {
            this.records = records;
        }

    }
}
