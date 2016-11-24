package br.com.zalem.ymir.client.android.entity.data;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import br.com.zalem.ymir.client.android.entity.data.metadata.IEntityMetadata;

/**
 * Representação genérica de um registro de uma entidade de dados.<br>
 * Cada registro é composto por um identificador único e qualquer número de atributos e relacionamentos.<br>
 * <br>
 * Todos os <code>get</code> e <code>set</code> dependem da existência de determianda atributo ou relacionamento no registro, então
 * um {@link IllegalArgumentException} será lançado caso o atributo ou relacionamento passado de parâmetro não exista.<br>
 * Os atributos e relacionamentos que o registro possue estão descritas através do {@link IEntityMetadata}, retornado pelo método 
 * {@link #getEntityMetadata()}.
 * Os <code>get</code> podem retornar <code>null</code> se não houver um valor designado para um atributo ou relacionamento no registro.
 * 
 * @author Thiago Gesser
 */
public interface IEntityRecord {
	
	/**
	 * Obtém os metadados da entidade que o registro representa.
	 * 
	 * @return os metadados da entidade.
	 */
	IEntityMetadata getEntityMetadata();
	
	/**
	 * Obtém o identificador único do registro.
	 * 
	 * @return o identificador obtido.
	 */
	Serializable getId();
	
	/**
	 * Obtém o status de sincronização do registro em relação à fonte de dados.
	 * 
	 * @return o status de sincronização obtido.
	 */
	SyncStatus getSyncStatus();
	
	/**
	 * Indica se o registro é local. Isto significa que ele foi criado no próprio dispositivo e ainda não foi sincronizado
	 * com a fonte de dados.<br>
	 * A partir do momento que um registro se torna parte da fonte de dados, ele deixa de ser local, mesmo
	 * tendo sido criado neste dispositivo.
	 * 
	 * @return <code>true</code> se o registro é local e <code>false</code> se ele é da fonte de dados.
	 */
	boolean isLocal();
	
	/**
	 * Indica se o registro é novo e ainda não foi salvo.
	 * 
	 * @return <code>true</code> se o registro for novo e <code>false</code> caso contrário.
	 */
	boolean isNew();
	
	/**
	 * Indica se o registro foi excluído.<br>
     * Registros excluídos não podem ser utilizados nas operações com a fonte de dados, permitindo apenas que seus dados sejam acessados.
	 *
	 * @return <code>true</code> se o registro foi excluído e <code>false</code> caso contrário.
	 */
	boolean isDeleted();
	
	/**
	 * Indica se o registro possui alterações não salvas.
	 * 
	 * @return <code>true</code> se o registro possuir alterações não salvas e <code>false</code> caso contrário.
	 */
	boolean isDirty();
	
	/**
	 * Indica se determinado campo (atributo ou relacionamento) do registro possui alterações não salvas.
	 * 
	 * @param fieldName o campo que será verificado.
	 * @return <code>true</code> se o campo possuir alterações não salvas e <code>false</code> caso contrário.
	 * @throws IllegalArgumentException se a entidade não possuir o campo.
	 */
	boolean isDirty(String fieldName);
	
	/**
	 * Indica se determinado campo (atributo ou relacionamento) do registro não possui valor designado.
	 * 
	 * @param fieldName o campo que será verificado.
	 * @return <code>true</code> se o campo não possui valor designado e <code>false</code> caso contrário. 
	 * @throws IllegalArgumentException se a entidade não possuir o campo.
	 */
	boolean isNull(String fieldName);


    /**
     * Obtém um valor do registro de acordo com o nome do atributo. O tipo do valor retornado depende do tipo do atributo.
     *
     * @param attribute nome do atributo.
     * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
     * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o atributo não era singular.
     */
    Object getAttributeValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>inteiro</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>inteiro</code>.
	 */
	Integer getIntegerValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>decimal</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>decimal</code>.
	 */
	Double getDecimalValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>texto</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>texto</code>. 
	 */
	String getTextValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>booleano</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>booleano</code>. 
	 */
	Boolean getBooleanValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>data</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>data</code>. 
	 */
	Date getDateValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>hora</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>hora</code>. 
	 */
	Time getTimeValue(String attribute);

    /**
     * Obtém um valor do tipo <code>data e hora</code> do registro de acordo com o nome do atributo.
     *
     * @param attribute nome do atributo.
     * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
     * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>data e hora</code>.
     */
    Timestamp getDatetimeValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>caractere</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>caractere</code>.
	 */
	Character getCharacterValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>imagem</code> do registro de acordo com o nome do atributo, sendo que a imagem tentará
	 * ser obtida primeiramente do <code>cache de imagens</code> e caso ela não esteja presente, será obtida dos dados 
	 * do registro.<br>
	 * Recomenda-se utilizar este método na Thread de UI apenas se houver a certeza de que não haverá impacto em outras
	 * ativididades de UI, pois há o risco da imagem ser obtida dos dados do registro, que é uma operação pesada.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>imagem</code>.
	 */
	Bitmap getImageValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>imagem</code> do registro de acordo com o nome do atributo, escolhendo se ele vai
	 * ser obtido do <code>cache de imagens</code> ou dos dados do registro.<br>
	 * Recomenda-se primeiramente tentar obter a imagem do cache na Thread de UI, por se tratar de uma operação leve.
	 * Caso a imagem não esteja no cache, recomenda-se obtê-la dos dados do registro em uma outra Thread, por se tratar
	 * de uma operação pesada (usando uma {@link android.os.AsyncTask}, por exemplo).
	 * 
	 * @param attribute nome do atributo.
	 * @param fromCache <code>true</code> se for para obter a imagem do cache e <code>false</code> se for para obtê-la dos dados do registro.
	 * @return <code>fromCache = true</code>: a imagem obtida ou <code>null</code> caso ela não esteja no cache.<br>
	 * 		   <code>fromCache = false</code>: a imagem obtida ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>imagem</code>.
	 */
	Bitmap getImageValue(String attribute, boolean fromCache);
	
	/**
	 * Obtém um valor do tipo <code>registro de entidade</code> proveniente de um relacionamento deste registro, de acordo com o nome do relacionamento.
	 * 
	 * @param relationship nome do relacionamento
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o relacionamento.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento ou se o valor não era um <code>registro de entidade</code>.
	 */
	IEntityRecord getRelationshipValue(String relationship);


    /**
     * Obtém um array de valores do registro de acordo com o nome do atributo. O tipo do array retornado depende do tipo do atributo.
     *
     * @param attribute nome do atributo.
     * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
     * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o atributo não era array.
     */
    Object[] getAttributeArrayValue(String attribute);

	/**
	 * Obtém um valor do tipo <code>array de inteiros</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de inteiros</code>.
	 */
	Integer[] getIntegerArrayValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>array de decimais</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de decimais</code>.
	 */
	Double[] getDecimalArrayValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>array de textos</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de textos</code>.
	 */
	String[] getTextArrayValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>array de booleanos</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de booleanos</code>.
	 */
	Boolean[] getBooleanArrayValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>array de datas</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de datas</code>.
	 */
	Date[] getDateArrayValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>array de horas</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de horas</code>.
	 */
	Time[] getTimeArrayValue(String attribute);

	/**
	 * Obtém um valor do tipo <code>array de datas e horas</code> do registro de acordo com o nome do atributo.
	 *
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de datas e horas</code>.
	 */
    Timestamp[] getDatetimeArrayValue(String attribute);

	/**
	 * Obtém um valor do tipo <code>array de caracteres</code> do registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de caracteres</code>.
	 */
	Character[] getCharacterArrayValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>array de imagens</code> do registro de acordo com o nome do atributo, sendo que o array
	 * tentará ser obtido primeiramente do <code>cache de imagens</code> e caso ele não esteja presente, será obtido dos 
	 * dados do registro.<br>
	 * Recomenda-se utilizar este método na Thread de UI apenas se houver a certeza de que não haverá impacto em outras
	 * ativididades de UI, pois há o risco das imagens serem obtidas dos dados do registro, que é uma operação pesada.
	 * 
	 * @param attribute nome do atributo.
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de imagens</code>.
	 */
	Bitmap[] getImageArrayValue(String attribute);
	
	/**
	 * Obtém um valor do tipo <code>array de imagens</code> do registro de acordo com o nome do atributo, escolhendo se
	 * ele vai ser obtido do <code>cache de imagens</code> ou dos dados do registro.<br>
	 * Recomenda-se primeiramente tentar obter o array de imagens do cache na Thread de UI, por se tratar de uma operação leve.
	 * Caso o array não esteja no cache, recomenda-se obtê-lo dos dados do registro em uma outra Thread, por se tratar
	 * de uma operação pesada (usando uma {@link android.os.AsyncTask}, por exemplo).
	 * 
	 * @param attribute nome do atributo.
	 * @param fromCache <code>true</code> se for para obter a imagem do cache e <code>false</code> se for para obtê-la dos dados do registro.
	 * @return <code>fromCache = true</code>: o array de imagens obtido ou <code>null</code> caso ele não esteja no cache.<br>
	 * 		   <code>fromCache = false</code>: o array de imagens obtido ou <code>null</code> se não havia valor designado para o atributo.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era um <code>array de imagens</code>.
	 */
	Bitmap[] getImageArrayValue(String attribute, boolean fromCache);
	
	/**
	 * Obtém um valor do tipo <code>array de registros de entidade</code> proveniente de um relacionamento deste registro, de acordo com o nome do relacionamento.
	 * 
	 * @param relationship nome do relacionamento
	 * @return o valor obtido ou <code>null</code> se não havia valor designado para o relacionamento.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento ou se o valor não era um <code>array de registros de entidades</code>.
	 */
	IEntityRecord[] getRelationshipArrayValue(String relationship);


    /**
     * Define um valor no registro de acordo com o nome do atributo. O tipo do valor deve ser adequado ao tipo do atributo.
     *
     * @param attribute nome do atributo.
     * @param value valor que será definido.
     * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era adequado para o atributo.
     */
    void setAttributeValue(String attribute, Object value);
	
	/**
	 * Define um valor do tipo <code>inteiro</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setIntegerValue(String attribute, Integer value);
	
	/**
	 * Define um valor do tipo <code>decimal</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setDecimalValue(String attribute, Double value);
	
	/**
	 * Define um valor do tipo <code>texto</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setTextValue(String attribute, String value);
	
	/**
	 * Define um valor do tipo <code>booleano</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setBooleanValue(String attribute, Boolean value);
	
	/**
	 * Define um valor do tipo <code>data</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setDateValue(String attribute, Date value);
	
	/**
	 * Define um valor do tipo <code>hora</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setTimeValue(String attribute, Time value);

	/**
	 * Define um valor do tipo <code>data e hora</code> no registro de acordo com o nome do atributo.
	 *
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo.
	 */
	void setDatetimeValue(String attribute, Timestamp value);

	/**
	 * Define um valor do tipo <code>caractere</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setCharacterValue(String attribute, Character value);
	
	/**
	 * Define um valor do tipo <code>imagem</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo.
	 */
	void setImageValue(String attribute, Bitmap value);
	
	/**
	 * Define um valor do tipo <code>registro de entidade</code> neste registro de acordo com o nome do relacionamento.
	 * 
	 * @param relationship nome do relacionamento
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento.
	 */
	void setRelationshipValue(String relationship, IEntityRecord value);

    /**
	 * Define um array de valores no registro de acordo com o nome do atributo. O tipo do array deve ser adequado ao tipo do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo ou se o valor não era adequado para o atributo.
	 */
	void setAttributeArrayValue(String attribute, Object[] value);

	/**
	 * Define um valor do tipo <code>array de inteiros</code> no registro de acordo com o nome do atributo.
	 *
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo.
	 */
	void setIntegerArrayValue(String attribute, Integer[] value);

	/**
	 * Define um valor do tipo <code>array de decimais</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setDecimalArrayValue(String attribute, Double[] value);
	
	/**
	 * Define um valor do tipo <code>array de textos</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setTextArrayValue(String attribute, String[] value);
	
	/**
	 * Define um valor do tipo <code>array de booleanos</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setBooleanArrayValue(String attribute, Boolean[] value);
	
	/**
	 * Define um valor do tipo <code>array de datas</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setDateArrayValue(String attribute, Date[] value);
	
	/**
	 * Define um valor do tipo <code>array de horas</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setTimeArrayValue(String attribute, Time[] value);

	/**
	 * Define um valor do tipo <code>array de datas e horas</code> no registro de acordo com o nome do atributo.
	 *
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo.
	 */
	void setDatetimeArrayValue(String attribute, Timestamp[] value);

	/**
	 * Define um valor do tipo <code>array de caracteres</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setCharacterArrayValue(String attribute, Character[] value);
	
	/**
	 * Define um valor do tipo <code>array de imagens</code> no registro de acordo com o nome do atributo.
	 * 
	 * @param attribute nome do atributo.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o atributo. 
	 */
	void setImageArrayValue(String attribute, Bitmap[] value);
	
	/**
	 * Define um valor do tipo <code>array de registros de entidade</code> neste registro de acordo com o nome do relacionamento.
	 * 
	 * @param relationship nome do relacionamento.
	 * @param value valor que será definido.
	 * @throws IllegalArgumentException se a entidade não possuir o relacionamento. 
	 */
	void setRelationshipArrayValue(String relationship, IEntityRecord[] value);
	
	/**
	 * Adiciona um valor do tipo <code>registro de entidade</code> em um <code>array de registros de entidade</code> 
	 * representado pelo nome do relacionamento.
	 * 
	 * @param relationship nome do relacionamento.
	 * @param value valor que será adicionado.
	 * @throws IllegalArgumentException se a entidade não possuir o relatcionamento ou se o relacionamento não era um <code>array de registros de entidades</code>.
	 * @throws NullPointerException se o valor for nulo. 
	 */
	void addRelationshipValue(String relationship, IEntityRecord value);
	
	/**
	 * Remove um valor do tipo <code>registro de entidade</code> de um <code>array de registros de entidade</code> 
	 * representado pelo nome do relacionamento.
	 * 
	 * @param relationship nome do relacionamento.
	 * @param value valor que será removido.
	 * @throws IllegalArgumentException se a entidade não possuir o relatcionamento, se o relacionamento não era um
	 * <code>array de registros de entidades</code> ou se o valor não estiver presente no array.
	 * @throws NullPointerException se o valor for nulo. 
	 */
	void removeRelationshipValue(String relationship, IEntityRecord value);
}