package br.com.zalem.ymir.client.android.entity.data.cursor;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;

/**
 * Cursor de acesso aos dados de registros de entidades provenientes de uma query.<br> 
 * Provê a navegação pelos resultados através dos métodos <code>move*</code> e a obtenção dos dados através dos métodos
 * <code>get*Value</code>. No caso de queries que não selecionaram campos específicos, o registro completo pode ser obtido
 * através do método {@link #getEntityRecord()}.<br>
 * Depois de utilizado, o cursor deve ser fechado através do método {@link #close()}.
 *
 * @author Thiago Gesser
 */
public interface IEntityRecordCursor {

    /**
     * Obtém o número de resultados do cursor.
     *
     * @return o número de resultados obtido.
     */
    int getCount();

    /**
     * Obtém a posição corrente do cursor no conjunto de resultados. O primeiro resultado está na posição 0, então quando
     * o cursor está antes do primeiro resultado sua posição será -1. O mesmo se aplica para o último resultado, quando o cursor
     * estiver depois dela, estará na posição {@link #getCount()}. 
     *
     * @return a posição corrente.
     */
    int getPosition();

    /**
     * Move o cursor por uma quantidade relativa, para frente ou para trás, da posição corrente. Números positivos o movem
     * para frente e negativos o movem para trás. Se a posição final estiver fora dos limites do cursor então a posição
     * resultante será estabelecida em -1 ou {@link #getCount()}, dependendo se o valor está fora do começo ou do final
     * do cursor, respectivamente.
     * <p>
     * 
     * @param offset número que será aplicado à posição corrente.
     * @return <code>true</code> se a destino requisitado foi alcançado de maneira exata e <code>false</code> se o destino
     * ficou fora dos limites do cursor.
     */
    boolean move(int offset);

    /**
     * Move o cursor para uma posição absoluta. A gama de valores válidos é de -1 até {@link #getCount()}.
     * 
     * @param position a posição para se mover.
     * @return <code>true</code> se a posição é alcançável e o cursor foi movido e <code>false</code> caso contrário.
     */
    boolean moveToPosition(int position);

    /**
     * Move o cursor para o primeiro resultado.
     *
     * @return <code>true</code> se o movimento foi feito com sucesso e <code>false</code> se o cursor estava vazio.
     */
    boolean moveToFirst();

    /**
     * Move o cursor para o último resultado.
     *
     * @return <code>true</code> se o movimento foi feito com sucesso e <code>false</code> se o cursor estava vazio.
     */
    boolean moveToLast();

    /**
     * Move o cursor para o próximo resultado.
     * 
     * @return <code>true</code> se o movimento foi feito com sucesso e <code>false</code> se o cursor passou do
     * último resultado.
     */
    boolean moveToNext();

    /**
     * Move o cursor para o resultado anterior.
     * 
     * @return <code>true</code> se o movimento foi feito com sucesso e <code>false</code> se o cursor passou do
     * primeiro resultado.
     */
    boolean moveToPrevious();

    /**
     * Verifica se o cursor está posicionado no primeiro resultado.
     * 
     * @return <code>true</code> se o cursor está no primeiro resultado e <code>false</code> caso contrário.
     */
    boolean isFirst();
    
    /**
     * Verifica se o cursor está posicionado no último resultado.
     * 
     * @return <code>true</code> se o cursor está no último resultado e <code>false</code> caso contrário.
     */
    boolean isLast();

    /**
     * Verifica se o cursor está posicionado antes da primeiro resultado.
     * 
     * @return <code>true</code> se o cursor está antes da primeiro resultado e <code>false</code> caso contrário.
     */
    boolean isBeforeFirst();

    /**
     * Verifica se o cursor está posicionado depois da último resultado.
     * 
     * @return <code>true</code> se o cursor está depois da último resultado e <code>false</code> caso contrário.
     */
    boolean isAfterLast();

    /**
     * Obtém o índice de base zero do campo.
     * 
     * @param fieldName o nome do campo.
     * @return o índice obtido do campo.
     * @throws IllegalArgumentException se o campo não existe.
     */
    int getFieldIndex(String fieldName);

    /**
     * Obtém o nome do campo presente no índice.
     *
     * @param fieldIndex índice de base zero do campo.
     * @return o nome da coluna obtido.
     */
    String getFieldName(int fieldIndex);

    /**
     * Obtém um array de Strings com os nomes de todos os campos do cursor de acordo com a ordem
     * em que eles estão dispostos nos resultados.
     * 
     * @return os nomes dos campos obtidos.
     */
    String[] getFieldNames();
	
    /**
     * Verifica se o valor em determinado campo é nulo.
     *
     * @param fieldIndex índice de base zero do campo.
     * @return <code>true</code> se o valor é nulo e <code>false</code> caso contrário.
     */
    boolean isNull(int fieldIndex);
    
    
    /**
	 * Obtém o valor do tipo <code>inteiro</code> do campo requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo <code>inteiro</code> ou se o valor não estiver de acordo com o tipo do campo.
	 */
	Integer getIntegerValue(int fieldIndex);
	
    /**
	 * Obtém o valor do tipo <code>decimal</code> do campo requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo <code>decimal</code> ou se o valor não estiver de acordo com o tipo do campo.
	 */
	Double getDecimalValue(int fieldIndex);
	
    /**
	 * Obtém o valor do tipo <code>texto</code> do campo requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo <code>texto</code> ou se o valor não estiver de acordo com o tipo do campo.
	 */
	String getTextValue(int fieldIndex);
	
	
    /**
	 * Obtém o valor do tipo <code>booleano</code> do campo requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo <code>booleano</code> ou se o valor não estiver de acordo com o tipo do campo.
	 */
	Boolean getBooleanValue(int fieldIndex);
	
    /**
	 * Obtém o valor do tipo <code>data</code> do campo requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo <code>data</code> ou se o valor não estiver de acordo com o tipo do campo.
	 */
	Date getDateValue(int fieldIndex);
	
    /**
	 * Obtém o valor do tipo <code>hora</code> do campo requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo <code>hora</code> ou se o valor não estiver de acordo com o tipo do campo.
	 */
	Time getTimeValue(int fieldIndex);

    /**
     * Obtém o valor do tipo <code>data e hora</code> do campo requisitado.
     *
     * @param fieldIndex índice de base zero do campo.
     * @return o valor obtido.
     * @throws IllegalArgumentException se o campo não for do tipo <code>data e hora</code> ou se o valor não estiver de acordo com o tipo do campo.
     */
    Timestamp getDatetimeValue(int fieldIndex);
	
    /**
	 * Obtém o valor do tipo <code>caractere</code> do campo requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo <code>caractere</code> ou se o valor não estiver de acordo com o tipo do campo.
	 */
	Character getCharacterValue(int fieldIndex);
	
    /**
	 * Obtém o valor do tipo <code>registro de entidade</code> proveniente do campo de relacionamento requisitado.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o campo não for do tipo de relacionamento suportado.
	 */
	IEntityRecord getRelationshipValue(int fieldIndex);
	
    /**
	 * Obtém o valor do campo requisitado. O tipo do valor será de acordo com o tipo do campo.
	 * 
	 * @param fieldIndex índice de base zero do campo.
	 * @return o valor obtido.
	 * @throws IllegalArgumentException se o valor não estiver de acordo com o tipo do campo.
	 */
	Object getValue(int fieldIndex);
	
    /**
	 * Obtém o registro completo da entidade.<br>
	 * Só pode ser utilizado em queries onde não foram selecionados campos específicos.
	 * 
	 * @return o registro obtido ou <code>null</code> caso o registro da posição corrente não exista mais.
	 */
	IEntityRecord getEntityRecord();
	
    /**
     * Fecha o cursor, liberando todos os recursos e tornando-o completamente inválido para a obtenção de dados.
     */
    void close();

    /**
     * Verifica se o cursor está fechado.
     * 
     * @return <code>true</code> se o cursor está fechado e <code>false</code> caso contrário.
     */
    boolean isClosed();
}
