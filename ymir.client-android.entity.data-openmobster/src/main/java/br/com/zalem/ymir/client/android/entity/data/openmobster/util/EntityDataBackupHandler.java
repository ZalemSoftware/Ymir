package br.com.zalem.ymir.client.android.entity.data.openmobster.util;

import android.content.Context;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openmobster.android.api.sync.MobileBean;
import org.openmobster.core.mobileCloud.android.storage.DBException;
import org.openmobster.core.mobileCloud.android.storage.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import br.com.zalem.ymir.client.android.entity.data.openmobster.MobileBeanEntityDataManager;

/**
 * Auxiliar na criação/recuperação de backups dos dados das entidades baseados em {@link MobileBean}.
 * O backup é salvo no formato JSON e é dividido em duas partes: cabeçalho e dados.<br>
 * O {@link MobileBeanBackupHeader cabeçalho} contém as informações sobre o backup em si, podendo ser lido de forma rápida sem passar pelos dados.<br>
 * A parte de dados agrega todos os registros das entidades definidas para o backup, permitindo sua recuperação posterior.<br>
 * <br>
 * Os dados do backup são salvos através de algoritmos versionados. Desta forma, não importa o quão evoluído esteja o algoritmo atual de
 * salvamento dos registros, sempre será possivel recuperar backups antigos através dos algoritmos de versões anteriores.
 *
 * @see MobileBeanJsonSerializer
 *
 * @author Thiago Gesser
 */
public final class EntityDataBackupHandler {

    private static final String HEADER_FIELD = "header";
    private static final String DATA_FIELD = "data";

    private static final String BACKUP_VERSION_1 = "1";
    private static final String CURRENT_BACKUP_VERSION = BACKUP_VERSION_1;

    private final Context context;
    private final MobileBeanEntityDataManager dataManager;
    private final ObjectMapper objectMapper;

    public EntityDataBackupHandler(Context context, MobileBeanEntityDataManager dataManager) {
        this(context, dataManager, new ObjectMapper());
    }

    public EntityDataBackupHandler(Context context, MobileBeanEntityDataManager dataManager, ObjectMapper objectMapper) {
        this.dataManager = dataManager;
        this.objectMapper = objectMapper;
        this.context = context;
    }

    /**
     * Cria um backup no arquivo a partir dos registros atuais das entidades.
     *
     * @param file arquivo em que o backup será salvo.
     * @param entities entidades cujo os dados serão salvos.
     * @throws IOException se houve algum problema na gravação do arquivo.
     * @throws DBException se houve algum problema na leitura dos dados.
     */
    public void createBackup(File file, String... entities) throws IOException, DBException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            createBackup(out, entities);
        } finally {
            out.close();
        }
    }

    /**
     * Cria um backup na saída de dados a partir dos registros atuais das entidades.
     *
     * @param out saida de dados.
     * @param entities entidades cujo os dados serão salvos.
     * @throws IOException se houve algum problema na gravação do arquivo.
     * @throws DBException se houve algum problema na leitura dos registros.
     */
    public void createBackup(OutputStream out, String... entities) throws IOException, DBException {
        JsonGenerator generator = objectMapper.getFactory().createGenerator(out, JsonEncoding.UTF8);
        generator.writeStartObject();
        writeBackup(generator, CURRENT_BACKUP_VERSION, entities);
        generator.writeEndObject();
        generator.close();
    }


    /**
     * Recupera um backup do arquivo, substituindo os registros atuais das entidades definidas no backup pelos contidos nele.
     *
     * @param file arquivo que contém o backup.
     * @throws IOException se houve algum problema na leitura do arquivo.
     * @throws DBException se houve algum problema na substituição dos registros.
     */
    public void restoreBackup(File file) throws IOException, DBException {
        FileInputStream in = new FileInputStream(file);
        try {
            restoreBackup(in);
        } finally {
            in.close();
        }
    }

    /**
     * Recupera um backup da entrada de dados, substituindo os registros atuais das entidades definidas no backup pelos contidos nele.
     *
     * @param in entrada de dados.
     * @throws IOException se houve algum problema na leitura do arquivo.
     * @throws DBException se houve algum problema na substituição dos registros.
     */
    public void restoreBackup(InputStream in) throws IOException, DBException {
        JsonParser parser = objectMapper.getFactory().createParser(in);
        try {
            MobileBeanBackupHeader header = null;
            boolean restored = false;
            for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
                String fieldName = parser.getCurrentName();

                if (HEADER_FIELD.equals(fieldName)) {
                    parser.nextToken();
                    header = objectMapper.readValue(parser, MobileBeanBackupHeader.class);
                }

                if (DATA_FIELD.equals(fieldName)) {
                    if (header == null) {
                        throw new IOException(String.format("No %s found before %s declaration.", HEADER_FIELD, DATA_FIELD));
                    }

                    parser.nextToken();
                    switch (header.getVersion()) {
                        case BACKUP_VERSION_1:
                            restoreBackupData_v1(parser);
                            break;

                        default:
                            throw new IllegalArgumentException("Unsupported backup version: " + header.getVersion());
                    }
                    restored = true;
                }
            }

            if (!restored) {
                throw new IOException(String.format("No %s found within the json InputStream.", DATA_FIELD));
            }
        } finally {
            parser.close();
        }
    }


    /**
     * Obtém o cabeçalho a partir do arquivo de backup.
     *
     * @param file arquivo de backup.
     * @return o cabeçalho obtido.
     * @throws IOException se o arquivo não é de backup ou se houve algum outro problema na leitura.
     */
    public MobileBeanBackupHeader getBackupHeader(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return getBackupHeader(in);
        } finally {
            in.close();
        }
    }
    /**
     * Obtém o cabeçalho de backup a partir da entrada de dados.
     *
     * @param in entrada de dados.
     * @return o cabeçalho obtido.
     * @throws IOException se o os dados nao são de um backup ou se houve algum outro problema na leitura.
     */
    public MobileBeanBackupHeader getBackupHeader(InputStream in) throws IOException {
        JsonParser parser = objectMapper.getFactory().createParser(in);
        try {
            for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
                String fieldName = parser.getCurrentName();

                if (HEADER_FIELD.equals(fieldName)) {
                    parser.nextToken();
                    return objectMapper.readValue(parser, MobileBeanBackupHeader.class);
                }
            }
        } finally {
            parser.close();
        }

        throw new IOException(String.format("No %s found within the json InputStream.", HEADER_FIELD));
    }


    /*
     * Métodos auxiliares
     */

    private void writeBackup(JsonGenerator generator, String version, String... entities) throws IOException, DBException {
        Map<String, Integer> entitiesTotals = new HashMap<>();
        Database database = Database.getInstance(context);
        String[] channels = new String[entities.length];
        for (int i = 0; i < entities.length; i++) {
            String entity = entities[i];
            String channel = dataManager.getEntityMetadata(entity).getChannel();
            channels[i] = channel;

            int total = (int) database.selectCount(channel);
            entitiesTotals.put(entity, total);
        }

        //Header
        generator.writeFieldName(HEADER_FIELD);

        MobileBeanBackupHeader header = new MobileBeanBackupHeader(version, entitiesTotals);
        objectMapper.writeValue(generator, header);

        //Data
        generator.writeFieldName(DATA_FIELD);
        switch (version) {
            case BACKUP_VERSION_1:
                writeBackupData_v1(generator, channels);
                break;

            default:
                throw new IllegalArgumentException("Unsupported backup version: " + version);
        }
    }


    /*
     * Algoritmos da versão 1 de serialização/deserialização de dados.
     */

    private void writeBackupData_v1(JsonGenerator generator, String[] channels) throws IOException, DBException {
        MobileBeanJsonSerializer dataSerializer = new MobileBeanJsonSerializer(context, objectMapper);

        generator.writeStartArray();
        for (String channel : channels) {
            dataSerializer.serialize(channel, generator);
        }
        generator.writeEndArray();
    }

    private void restoreBackupData_v1(JsonParser parser) throws IOException, DBException {
        MobileBeanJsonSerializer dataSerializer = new MobileBeanJsonSerializer(context, objectMapper);

        Database db = Database.getInstance(context);
        db.beginTransaction();
        try {
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                dataSerializer.deserialize(parser, true);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }


    /*
     * Classe auxiliares.
     */

    /**
     * Representa o cabeçalho de um backup de Mobile Beans.
     */
    public static final class MobileBeanBackupHeader {

        private String version;
        private Date date;
        private Map<String, Integer> totals;

        public MobileBeanBackupHeader() {
        }

        public MobileBeanBackupHeader(String version, Map<String, Integer> totals) {
            this.version = version;
            this.date = new Date();
            this.totals = totals;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Map<String, Integer> getTotals() {
            return totals;
        }

        public void setTotals(Map<String, Integer> totals) {
            this.totals = totals;
        }
    }
}
