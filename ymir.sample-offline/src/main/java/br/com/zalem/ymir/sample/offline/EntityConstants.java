package br.com.zalem.ymir.sample.offline;

/**
 * Dispõe as constantes dos nomes das entidades e seus atributos / relacionamentos.
 *
 * @author Thiago Gesser
 */
public final class EntityConstants {

	private EntityConstants() {
	}

    //Pedido
    public static final String EXPENSE_ENTITY = "Expense";
    public static final String EXPENSE_ATTRIBUTE_VALUE = "value";
    public static final String EXPENSE_ATTRIBUTE_DATE = "date";
    public static final String EXPENSE_RELATIONSHIP_PLACE = "place";
    public static final String EXPENSE_RELATIONSHIP_PRODUCT = "product";
    public static final String EXPENSE_VATTRIBUTE_QUANTITY = "vQuantity";

	//Place
    public static final String PLACE_ENTITY = "Place";
	public static final String PLACE_ATTRIBUTE_NAME = "name";
	public static final String PLACE_ATTRIBUTE_STREET = "street";
	public static final String PLACE_ATTRIBUTE_STREETNUMBER = "streetNumber";
	public static final String PLACE_ATTRIBUTE_NEIGHBORHOOD = "neighborhood";
	public static final String PLACE_ATTRIBUTE_CITY = "city";
	public static final String PLACE_ATTRIBUTE_STATE = "state";
	public static final String PLACE_ATTRIBUTE_POSTALCODE = "postalCode";
	public static final String PLACE_ATTRIBUTE_FULLADDRESS = "fullAddress";

	//Produto
    public static final String PRODUCT_ENTITY = "Product";
	public static final String PRODUCT_ATTRIBUTE_NAME = "name";
	public static final String PRODUCT_ATTRIBUTE_PRICE = "price";
	public static final String PRODUCT_ATTRIBUTE_TYPE = "type";
}
