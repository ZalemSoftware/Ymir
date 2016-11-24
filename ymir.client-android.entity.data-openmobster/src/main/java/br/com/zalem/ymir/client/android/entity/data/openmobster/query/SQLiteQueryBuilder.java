package br.com.zalem.ymir.client.android.entity.data.openmobster.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Construtor de queries específico para SQLite.<br>
 * Permite a construção de queries abstraindo totalmente o SQL. Entretanto, nenhuma verificação é feita quanto ao
 * conteúdo da query, cabendo ao utilizador garantir a montagem correta.<br>
 * Os parâmetros adicionados durante a montagem da query podem ser obtidos através do método {@link #getParameters()}
 * e a query em si pode ser obtida através do {@link #toString()}.<br>
 * <br>
 * Como o SQLite só aceita parâmetros String e todos os valores adicionados na query são tratados como parâmetros,
 * os valoes devem ser passados como String.
 *
 * @author Thiago Gesser
 */
public final class SQLiteQueryBuilder {
	
	/**
	 * Tipos de cast suportados para valores e colunas da query.
	 */
	public enum CastType {
		INTEGER("integer"), REAL("real");
		
		private final String SQLName;
		
		CastType(String sqlName) {
			this.SQLName = sqlName;
		}

		String getSQLName() {
			return SQLName;
		}
	}
	
	/**
	 * Tipos de like suportados.
	 */
	public enum LikeType {
		STARTS_WITH("%s%%"), ENDS_WITH("%%%s"), CONTAINS("%%%s%%"); //%% = % no String.format()
	
		private final String format;
		
		LikeType(String format) {
			this.format = format;
		}
		
		public String getFormat() {
			return format;
		}
	}
	
	private final StringBuilder sb = new StringBuilder();
	private final List<String> parameters = new ArrayList<>();
	
	private boolean newColList;
	
	/**
	 * Adiciona um comando <code>SELECT</code> na query.
	 * 
	 * @param distinct <code>true</code> se o <code>DISTINCT</code> deve ser usado também e <code>false</code> caso contrário. 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder select(boolean distinct) {
		sb.append("SELECT ");
		if (distinct) {
			sb.append("DISTINCT ");
		}
		startColList();
		return this;
	}
	
	/**
	 * Adiciona uma coluna para seleção na query juntamente com seu alias.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param colAlias o alias da coluna ou <code>null</code> se não houver.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder selectColumn(String tableAlias, String colName, String colAlias) {
		newColItem();
		
		sb.append(getColFullname(tableAlias, colName));
		if (colAlias != null) {
			sb.append(" AS '").append(colAlias).append("'");
		}
		return this;
	}
	
	/**
	 * Adiciona uma expressão para seleção na query.
	 * 
	 * @param expr expressão de seleção de dados.
	 * @param alias o alias da expressão ou <code>null</code> se não houver.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder selectExpr(String expr, String alias) {
		newColItem();
		
		sb.append(expr);
		if (alias != null) {
			sb.append(" AS '").append(alias).append("'");
		}
		return this;
	}

    /**
     * Adiciona um comando <code>SELECT *</code> na query.
     *
     * @return o próprio construtor.
     */
    public SQLiteQueryBuilder selectAll() {
        sb.append("SELECT *");
        return this;
    }
	
	/**
	 * Adiciona o comando <code>FROM</code> na query juntamente com a tabela e seu alias.
	 * 
	 * @param tableName nome da tabela.
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder from(String tableName, String tableAlias) {
		sb.append(" FROM ");
		sb.append(tableName);
		if (tableAlias != null) {
			sb.append(" ").append(tableAlias);
		}
		return this;
	}
	
	/**
	 * Adiciona o comando <code>JOIN</code> na query juntamente com a tabela, seu alias e o comando <code>ON</code> com
	 * a junção de uma coluna da tabela fonte com uma coluna da tabela do JOIN.
	 * 
	 * @param joinTableName nome da tabela do JOIN.
	 * @param joinTableAlias o alias da tabela do JOIN ou <code>null</code> se não houver.
	 * @param joinTableCol a coluna da tabela do JOIN.
	 * @param srcTableAlias o alias da tabela fonte ou <code>null</code> se não houver.
	 * @param srcTableCol a coluna da tabela fonte.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder join(String joinTableName, String joinTableAlias, String joinTableCol, String srcTableAlias, String srcTableCol) {
		String joinExpr = getColFullname(joinTableAlias, joinTableCol);
		String srcExpr = getColFullname(srcTableAlias, srcTableCol);
		return join(joinTableName, joinTableAlias, joinExpr, srcExpr);
	}
	
	/**
	 * Adiciona o comando <code>JOIN</code> na query juntamente com a tabela, seu alias e o comando <code>ON</code> com
	 * a expressão fonte igual à expressão de junção. 
	 * 
	 * @param joinTableName nome da tabela do JOIN.
	 * @param joinTableAlias o alias da tabela do JOIN ou <code>null</code> se não houver.
	 * @param joinExpr expressão de junção.
	 * @param srcExpr expressão fonte.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder join(String joinTableName, String joinTableAlias, String joinExpr, String srcExpr) {
		sb.append(" JOIN ").append(joinTableName);
		if (joinTableAlias != null) {
			sb.append(" ").append(joinTableAlias);
		}
		sb.append(" ON ").append(srcExpr).append(" = ").append(joinExpr);
		return this;
	}
	
	/**
	 * Adiciona um comando <code>WHERE</code> na query.
	 * 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder where() {
		sb.append(" WHERE ");
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna igual ao valor</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param valueParam o valor que será colocado como parâmetro.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder eq(String tableAlias, String colName, String valueParam) {
		appendParamRestriction(tableAlias, colName, "=", valueParam, null);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna menor que o valor</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param valueParam o valor que será colocado como parâmetro.
	 * @param castType tipo de cast que deve ser feito na coluna/valor ou <code>null</code> se nenhum cast precisar ser feito.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder lt(String tableAlias, String colName, String valueParam, CastType castType) {
		appendParamRestriction(tableAlias, colName, "<", valueParam, castType);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna maior que o valor</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param valueParam o valor que será colocado como parâmetro.
	 * @param castType tipo de cast que deve ser feito na coluna/valor ou <code>null</code> se nenhum cast precisar ser feito.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder gt(String tableAlias, String colName, String valueParam, CastType castType) {
		appendParamRestriction(tableAlias, colName, ">", valueParam, castType);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna menor ou igual ao valor</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param valueParam o valor que será colocado como parâmetro.
	 * @param castType tipo de cast que deve ser feito na coluna/valor ou <code>null</code> se nenhum cast precisar ser feito.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder le(String tableAlias, String colName, String valueParam, CastType castType) {
		appendParamRestriction(tableAlias, colName, "<=", valueParam, castType);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna maior ou igual ao valor</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param valueParam o valor que será colocado como parâmetro.
	 * @param castType tipo de cast que deve ser feito na coluna/valor ou <code>null</code> se nenhum cast precisar ser feito.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder ge(String tableAlias, String colName, String valueParam, CastType castType) {
		appendParamRestriction(tableAlias, colName, ">=", valueParam, castType);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna está nos valores</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param valuesParams os valores que serão colocados como parâmetros.
	 * @param castType tipo de cast que deve ser feito na coluna/valores ou <code>null</code> se nenhum cast precisar ser feito.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder in(String tableAlias, String colName, String[] valuesParams, CastType castType) {
		String[] params = new String[valuesParams.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = addParameter(valuesParams[i]);
		}
		appendRestriction(tableAlias, colName, "IN", scopeValues(params), castType);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna está nos valores resultantes do subselect</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param subselect o subselect.
	 * @param castType tipo de cast que deve ser feito na coluna/valores ou <code>null</code> se nenhum cast precisar ser feito.
	 * @param parameters parâmetros do subselect.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder in(String tableAlias, String colName, String subselect, CastType castType, String... parameters) {
		appendRestriction(tableAlias, colName, "IN", "(" + subselect + ")", castType);
		Collections.addAll(this.parameters, parameters);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna entre o primeiro e o segundo valor</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param valueParam1 o primeiro valor que será colocado como parâmetro.
	 * @param valueParam2 o segundo valor que será colocado como parâmetro.
	 * @param castType tipo de cast que deve ser feito na coluna/valores ou <code>null</code> se nenhum cast precisar ser feito.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder between(String tableAlias, String colName, String valueParam1, String valueParam2, CastType castType) {
		String value1 = addParameter(valueParam1);
		String value2 = addParameter(valueParam2);
		if (castType != null) {
			value1 = cast(value1, castType);
			value2 = cast(value2, castType);
		}
		
		String betweenExpr = String.format("%s AND %s", value1, value2);
		appendRestriction(tableAlias, colName, "BETWEEN", betweenExpr, castType);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna como um texto</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param text texto que será colocado como parâmetro.
	 * @param likeType tipo de comparação que será usada no texto.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder like(String tableAlias, String colName, String text, LikeType likeType) {
		String likeValue = String.format(likeType.getFormat(), text);
		appendParamRestriction(tableAlias, colName, "LIKE", likeValue, null);
		return this;
	}
	
	/**
	 * Chama o método {@link #glob0(String, String...)} passando o nome completo da coluna como expressão fonte.
	 *  
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param globExprs expressões que serão concatenadas para a formação do parâmetro usado na condição GLOB.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder glob(String tableAlias, String colName, String... globExprs) {
		String colFullname = getColFullname(tableAlias, colName, null);
		return glob0(colFullname, globExprs);
	}
	
	/**
	 * Adiciona a condição de <code>coluna como um texto</code> utilizando a condição GLOB.<br>
	 * O parâmetro utilizado na condição GLOB é formado pela concatenação das expressões passadas de parâmetro.
	 * As expressões suportadas devem ser acessdas através dos métodos estáticos desta classe que iniciam com <code>glob</code>,
	 * como {@link #globLiteral(String...)} e {@link #globZeroOrMoreCharacters()}.<br>
	 * <br>
	 * Exemplo de utilização:<br>
	 * <pre>
	 * //Buscando qualquer número decimal negativo.
	 * sqlBuilder.glob("x.value", globLiteral("-"), globZeroOrMoreCharacters(), globLiteral("."), globZeroOrMoreCharacters());
	 * </pre>
	 * 
	 * @param srcExpr expressão cujo o valor resultante será utilizado como fonte para o GLOB
	 * @param globExprs expressões que serão concatenadas para a formação do parâmetro usado na condição GLOB.
	 * @return o próprio construtor.
	 */
	//"glob0" para evitar ambiguidade com o "glob" devido ao varargs.
	public SQLiteQueryBuilder glob0(String srcExpr, String... globExprs) {
		String globCond;
		if (globExprs.length == 1) {
			globCond = globExprs[0];
		} else {
			StringBuilder globBuilder = new StringBuilder();
			for (String globExpr : globExprs) {
				globBuilder.append(globExpr);
			}
			globCond = globBuilder.toString();
		}
		
		String condParamter = addParameter(globCond);
		appendRestriction(srcExpr, "GLOB", condParamter);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna é nula</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder isNull(String tableAlias, String colName) {
		sb.append(getColFullname(tableAlias, colName)).append(" ISNULL");
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna aplicada a um "e lógico" com uma máscara de bits é igual ao valor</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param bitMask a máscara de bits que será colocada como parâmetro.
	 * @param value o valor que será colocado como parâmetro.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder bitwiseEq(String tableAlias, String colName, byte bitMask, byte value) {
		String bitwiseOp = bitwiseAnd(tableAlias, colName, bitMask);
		String bitwiseValue = addBitwiseParameter(value);
		appendRestriction(bitwiseOp, "=", bitwiseValue);
		return this;
	}
	
	/**
	 * Adiciona a condição de <code>coluna aplicada a um "e lógico" com uma máscara de bits está nos valores</code> na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param bitMask a máscara de bits que será colocada como parâmetro.
	 * @param values os valores que serão colocados como parâmetros.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder bitwiseIn(String tableAlias, String colName, byte bitMask, byte[] values) {
		String bitwiseOp = bitwiseAnd(tableAlias, colName, bitMask);
		
		String[] params = new String[values.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = addBitwiseParameter(values[i]);
		}
		appendRestriction(bitwiseOp, "IN", scopeValues(params));
		return this;
	}
	
	/**
	 * Adiciona um comando <code>NOT</code> na query.
	 * 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder not() {
		sb.append("NOT ");
		return this;
	}

	/**
	 * Adiciona um comando <code>AND</code> na query.
	 * 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder and() {
		sb.append(" AND ");
		return this;
	}

	/**
	 * Adiciona um comando <code>OR</code> na query.
	 * 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder or() {
		sb.append(" OR ");
		return this;
	}

	/**
	 * Adiciona uma abertura de escopo <code>(</code> query.
	 * 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder o() {
		sb.append("(");
		return this;
	}

	/**
	 * Adiciona o fechamento de escopo <code>)</code> query.
	 * 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder c() {
		sb.append(")");
		return this;
	}

	/**
	 * Adiciona um comando <code>ORDER BY</code> na query.
	 * 
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder orderBy() {
		sb.append(" ORDER BY ");
		startColList();
		return this;
	}
	
	/**
	 * Adiciona uma coluna de ordenação na query.
	 * 
	 * @param tableAlias o alias da tabela ou <code>null</code> se não houver.
	 * @param colName o nome da coluna.
	 * @param asc <code>true</code> se a ordem for ascendente ou <code>false</code> se for descendente.
	 * @param castType tipo de cast que deve ser feito na coluna ou <code>null</code> se nenhum cast precisar ser feito.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder orderByColumn(String tableAlias, String colName, boolean asc, CastType castType) {
		newColItem();
		
		sb.append(getColFullname(tableAlias, colName, castType));
		
		//Especifica o NOCASE para que a ordenação por texto não seja case sensitive.
		if (castType == null) {
			sb.append(" COLLATE NOCASE");
		}
		
		if (asc) {
			sb.append(" ASC");
		} else {
			sb.append(" DESC");
		}
		return this;
	}

    /**
     * Adiciona um comando <code>EXISTS</code> na query.
     *
     * @return o próprio construtor.
     */
    public SQLiteQueryBuilder exists() {
        sb.append("EXISTS ");
        return this;
    }

    /**
     * Adiciona um limitador de regisros retornados pela query.
     *
     * @return o próprio construtor.
     */
    public SQLiteQueryBuilder limit(int limit) {
        sb.append(" LIMIT ").append(limit);
        return this;
    }
	
	/**
	 * Adiciona a query e os parâmetros do construtor passado de parâmetro neste construtor.
	 * 
	 * @param qb o construtor que terá sua query e parâmetros adicionados.
	 * @return o próprio construtor.
	 */
	public SQLiteQueryBuilder append(SQLiteQueryBuilder qb) {
		sb.append(qb.sb);
		parameters.addAll(qb.parameters);
		return this;
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
	
	/**
	 * Obtém os parâmetros adicionados até o momento neste construtor de query.
	 * 
	 * @return os parâmetros obtidos.
	 */
	public String[] getParameters() {
        return parameters.toArray(new String[parameters.size()]);
	}
	
	
	/*
	 * Métodos utilitários para a montagem de expressões para a condição GLOB do SQLite.
	 * As demais expressões devem ser implementadas conforme surgir a demanda.  
	 */
	
	/**
	 * Adapta as expressões literais para serem usadas na condição GLOB.
	 * 
	 * @return as expressões adaptadas e concatenadas.
	 */
	public static String globLiteral(String... exprs) {
		if (exprs.length == 1) {
			return escapeGlobSpecialCharacters(exprs[0]);
		}
		
		StringBuilder builder = new StringBuilder();
		for (String expr : exprs) {
			builder.append(escapeGlobSpecialCharacters(expr));
		}
		return builder.toString();
	}
	
	/**
	 * Obtém a expressão de <code>zero ou mais caracteres</code> para ser usada na condição GLOB.
	 * 
	 * @return a expressão obtida.
	 */
	public static String globZeroOrMoreCharacters() {
		return "*";
	}
	
	/**
	 * Obtém a expressão de <code>um caractere</code> para ser usada na condição GLOB.
	 * 
	 * @return a expressão obtida.
	 */
	public static String globOneCharacter() {
		return "?";
	}
	
	/**
	 * Obtém a expressão de <code>um caractere que não é um dígito</code> para ser usada na condição GLOB.
	 * 
	 * @return a expressão obtida.
	 */
	public static String globOneCharacterNotDigit() {
		return "[^0-9]";
	}
	
	
	/*
	 * Métodos utilitários para o uso de operações e funções do SQLite na montagem da query.
	 * As demais funções e operações devem ser implementadas conforme surgir a demanda.
	 */
	
	/**
	 * Obtém o nome completo da coluna.
	 * 
	 * @param tableAlias alias da tabela, se houver.
	 * @param colName nome da coluna.
	 * @return o nome completo da coluna.
	 */
	public static String getColFullname(String tableAlias, String colName) {
		String fullName = colName;
		if (tableAlias != null) {
			fullName = tableAlias + "." + colName;
		}
		return fullName;
	}

	/**
	 * Cria a operação de adição entre dois operandos.
	 * 
	 * @param leftOperand operando da esquerda.
	 * @param rightOperand operando da direita.
	 * @return a operação criada.
	 */
	public static String opAdd(String leftOperand, String rightOperand) {
		return leftOperand + " + " + rightOperand;
	}
	
	/**
	 * Cria a operação de subtração entre dois operandos.
	 * 
	 * @param leftOperand operando da esquerda.
	 * @param rightOperand operando da direita.
	 * @return a operação criada.
	 */
	public static String opSubtract(String leftOperand, String rightOperand) {
		return leftOperand + " - " + rightOperand;
	}
	
	/**
	 * Cria uma chamada para a função de obtenção de tamanho de texto.
	 * 
	 * @param strExpr expressão de texto ou texto diretamente.
	 * @return a chamada criada.
	 */
	public static String fnLength(String strExpr) {
		return String.format("length(%s)", strExpr);
	}
	
	/**
	 * Cria uma chamada para a função de obtenção de parte de texto.
	 * 
	 * @param strExpr expressão de texto ou texto diretamente.
	 * @param indexExpr expressão de número ou número diretamente que representa o índice do início da parte do texto.
	 * @param countExpr expressão de número ou número diretamente que representa quantos caracteres devem ser selecionados.
	 * @return a chamada criada.
	 */
	public static String fnSubstr(String strExpr, String indexExpr, String countExpr) {
		return String.format("substr(%s, %s, %s)", strExpr, indexExpr, countExpr);
	}
	
	
	/*
	 * Métodos auxiliares
	 */
	
	private String addParameter(String parameter) {
		parameters.add(parameter);
		return "?";
	}
	
	private String addBitwiseParameter(byte value) {
		return cast(addParameter(String.valueOf(value)), CastType.INTEGER);
	}
	
	private void appendParamRestriction(String tableAlias, String colName, String operator, String valueParam, CastType castType) {
		appendRestriction(tableAlias, colName, operator, addParameter(valueParam), castType);
	}
	
	private void appendRestriction(String tableAlias, String colName, String operator, String rightOperand, CastType castType) {
		String leftOperand = getColFullname(tableAlias, colName, castType);
		appendRestriction(leftOperand, operator, rightOperand);
	}
	
	private void appendRestriction(String leftOperand, String operator, String rightOperand) {
		sb.append(leftOperand).append(" ").append(operator).append(" ").append(rightOperand);
	}

	private String scopeValues(String[] values) {
		StringBuilder valuesBuilder = new StringBuilder("(");
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				valuesBuilder.append(", ");
			}
			valuesBuilder.append(values[i]);
		}
		valuesBuilder.append(")");
		return valuesBuilder.toString();
	}
	
	private String bitwiseAnd(String tableAlias, String colName, byte bitMask) {
		return String.format("(%s & %s)", getColFullname(tableAlias, colName), addParameter(String.valueOf(bitMask)));
	}
	
	private void startColList() {
		newColList = true;
	}
	
	private void newColItem() {
		if (newColList) {
			newColList = false;
		} else {
			sb.append(", ");
		}
	}
	
	private static String getColFullname(String tableAlias, String colName, CastType castType) {
		String fullName = getColFullname(tableAlias, colName);
		if (castType != null) {
			fullName = cast(fullName, castType);
		}
		return fullName;
	}
	
	private static String cast(String value, CastType castType) {
		return String.format("cast(%s as %s)", value, castType.getSQLName());
	}
	
	private static String escapeGlobSpecialCharacters(String str) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			//O escape é feito colocando colchetes ao redor do caractere especial.
			if (c == '[' || c == ']' || c == '?' || c == '*') {
				builder.append('[').append(c).append(']');
				continue;
			}
			
			builder.append(c);
		}
		return builder.toString();
	}
}
