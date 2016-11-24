package br.com.zalem.ymir.client.android.util;

/**
 * Mapa simplificado de chave/valor cujo as chaves são itens de um <b>enum</b>.<br>
 * Comparado a um {@link java.util.EnumMap}, o <code>SimpleEnumMap</code> gasta menos memória e suas operações possuem melhor performance.
 * Entretanto, o <code>SimpleEnumMap</code> não oferece todas as operações especificados pelo {@link java.util.Map}, por isto não implementa esta interface.<br>
 * Desta forma, é preferível utilizar o <code>SimpleEnumMap</code> quando o seu limitado número de operações atende a demanda. 
 *
 * @author Thiago Gesser
 */
public final class SimpleEnumMap <K extends Enum<K>, V> {
	
	private final Object[] map;
	
	/**
	 * Cria um <code>SimpleEnumMap</code> baseado em um <b>enum</b>. As chaves deste mapa serão obrigatoriamente itens deste <b>enum</b>.
	 *  
	 * @param enumClass classe do <b>enum</b> em que este mapa irá se basear.
	 */
	public SimpleEnumMap(Class<K> enumClass) {
		Enum<K>[] enumConstants = enumClass.getEnumConstants();
		if (enumConstants == null) {
			throw new IllegalArgumentException(enumClass.getName() + " is not an Enum class.");
		}
		
		map = new Object[enumConstants.length];
	}
	
	/**
	 * Obtém o valor associado à chave.<br>
	 * Valores podem ser associados com as chaves atráves do método {@link #put(Enum, Object)}.
	 * 
	 * @param key chave.
	 * @return o valor associado à chave ou <code>null</code> se ela ainda não teve um valor associado. 
	 */
	@SuppressWarnings("unchecked")
	public V get(K key) {
		return (V) map[key.ordinal()];
	}
	
	/**
	 * Associa um valor a uma chave.<br>
	 * O valor associado pode ser obtido posteriormente através do método {@link #get(Enum)}. 
	 * 
	 * @param key chave
	 * @param value valor
	 */
	public void put(K key, V value) {
		map[key.ordinal()] = value;
	}
}
