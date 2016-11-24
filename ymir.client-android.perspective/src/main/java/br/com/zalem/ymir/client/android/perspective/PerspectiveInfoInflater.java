package br.com.zalem.ymir.client.android.perspective;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import br.com.zalem.ymir.client.android.perspective.PerspectiveInfo.IntentFilter;

/**
 * Inflater de definições de perspectivas, utilizado para transformar recursos XML em arrays de {@link PerspectiveInfo}.<br>
 * Cada recurso XML pode conter a definição de qualquer número de perspectivas. Uma perspectiva deve declarar sua classe
 * e pode definir um título e um filtro de {@link android.content.Intent}. O filtro será utilizados no momento de escolha de qual perspectiva
 * atenderá determinado Intent. Sendo assim, podem haver declarações de diferentes perspectivas que usam a mesma classe
 * mas que se diferem através de filtros de Intent. Desta forma, a aplicação poderá selecionar qual perspectiva quer abrir,
 * mesmo que elas sejam do mesmo tipo.<br>
 * O filtro de Intent pode ter qualquer número de categorias e ações. Desta forma, a perspectiva só será selecionada se
 * o Intent tiver todas as categorias e pelo menos uma das ações declaradas no filtro.<br>
 * <br>
 * Também é possível definir argumentos para as Perspectivas. Estes argumentos serão passados para ela no momento de sua criação e
 * podem ser obtidos através do método {@link Perspective#getArguments()}. Existem dois tipos de valores suportados pelos
 * argumentos, de acordo com os seguintes atributos XML. :
 * <ul>
 * 	<li><b>value</b>: um valor String. Pode ser obtido pela perspectiva através de {@link android.os.Bundle#getString(String)};</li>
 * 	<li><b>arrayValue</b>: um array de valores String, separados por "," (vírgula). Pode ser obtido pela perspectiva através de {@link android.os.Bundle#getStringArray(String)}.</li>
 * </ul>
 * Apenas um valor deve ser definido para cada argumento.<br>
 * <br>
 * Segue um exemplo de recurso XML:
 * <pre>{@code
 *  <?xml version="1.0" encoding="utf-8"?>
 *  <perspectives xmlns:ymir="http://schemas.android.com/apk/res-auto">
 *    <perspective ymir:title="@string/perspective1_title"
 *      ymir:className="br.com.code.ymir.client.android.example.Perspective1">
 *      <intent-filter>
 *         <action ymir:name="br.com.code.ymir.client.android.example.ACTION_1" />
 *         <action ymir:name="br.com.code.ymir.client.android.example.ACTION_2" />
 *         <category ymir:name="Category1" />
 *      </intent-filter>
 *      <argument ymir:key="ARGUMENT_1" ymir:value="value1" />
 *      <argument ymir:key="ARRAY_ARGUMENT_1" ymir:arrayValue="value1, value2, value3" />
 *    </perspective>
 *    
 *    <perspective ymir:className="br.com.code.ymir.client.android.example.Perspective2">
 *      <intent-filter>
 *         <action ymir:name="br.com.code.ymir.client.android.example.ACTION_2" />
 *         <category ymir:name="Category2" />
 *         <category ymir:name="Category3" />
 *         <category ymir:name="Category4" />
 *      </intent-filter>
 *    </perspective>
 *    
 *    <perspective ymir:title="@string/perspective3_title"
 *      ymir:theme="@style/PerspectiveTheme"
 *      ymir:className="br.com.code.ymir.client.android.example.Perspective3" />
 *    </perspective>
 *  </perspectives>
 * }</pre>
 * 
 * @author Thiago Gesser
 */
public final class PerspectiveInfoInflater {

	private static final String PERSPECTIVES_XML_TAG = "perspectives";
	private static final String PERSPECTIVE_XML_TAG = "perspective";
	private static final String INTENT_FILTER_XML_TAG = "intent-filter";
	private static final String ACTION_XML_TAG = "action";
	private static final String CATEGORY_XML_TAG = "category";
	private static final String ARGUMENT_XML_TAG = "argument";
	
	private final Context context;

	public PerspectiveInfoInflater(Context context) {
		this.context = context;
	}

	/**
	 * Infla um array de {@link PerspectiveInfo} a partir de um recurso XML de declaração de perspectivas.
	 * 
	 * @param perspectivesRes recurso XML de declaração de perspectivas.
	 * @return o array de PerspectiveInfo gerado.
	 * @throws android.view.InflateException se houve algum problema no formato ou nas tags do XML.
	 * @throws IllegalArgumentException se algum atributo do menu ou dos items estava errado.
	 */
	@SuppressWarnings("TryFinallyCanBeTryWithResources")
    public PerspectiveInfo[] inflate(int perspectivesRes) {
        XmlResourceParser xmlParser = context.getResources().getLayout(perspectivesRes);
        try {
            return inflate(xmlParser);
        } finally {
        	xmlParser.close();
        }
	}
	
	/**
	 * Infla um array de {@link PerspectiveInfo} a partir de um XmlResourceParser de um XML de declaração de perspectivas.
	 * 
	 * @param xmlParser parser de um XML de declaração de perspectivas.
	 * @return o array de PerspectiveInfo gerado.
	 * @throws android.view.InflateException se houve algum problema no formato ou nas tags do XML.
	 * @throws IllegalArgumentException se algum atributo do menu ou dos items estava errado.
	 */
	public PerspectiveInfo[] inflate(XmlResourceParser xmlParser) {
		try {
	        ArrayList<PerspectiveInfo> perspectives = null;
	        PerspectiveInfo currentPerspective = null;
	        IntentFilter currentIntentFilter = null;
	        
	        int tagType = xmlParser.next();
	        while (tagType != XmlPullParser.END_DOCUMENT) {
	            if (tagType == XmlPullParser.START_TAG) {
	                if (xmlParser.getName().equals(PERSPECTIVES_XML_TAG)) {
	                	if (perspectives != null) {
	                		throw new InflateException("\"perspectives\" tag must be declared only once.");
	                	}
	                	perspectives = new ArrayList<>();
	                	
	                } else if (xmlParser.getName().equals(PERSPECTIVE_XML_TAG)) {
	                	if (perspectives == null || currentPerspective != null) {
	                		throw new InflateException("\"perspective\" tags can only be declared inside a \"perspectives\" tag.");
	                	}
	                	
	                    AttributeSet attr = Xml.asAttributeSet(xmlParser);
	                    currentPerspective = new PerspectiveInfo(context, attr);
	                    perspectives.add(currentPerspective);
	                    
	                } else if (xmlParser.getName().equals(INTENT_FILTER_XML_TAG)) {
	                	if (currentPerspective == null || currentIntentFilter != null) {
	                		throw new InflateException("\"intent-filter\" tag can only be declared inside a \"perspective\" tag.");
	                	}
	                	if (currentPerspective.getIntentFilter() != null) {
	                		throw new InflateException("\"intent-filter\" tag must be declared only once per perspective.");
	                	}
	                	currentIntentFilter = new IntentFilter();
	                	currentPerspective.setIntentFilter(currentIntentFilter);
	                	
	                } else {
						Theme theme = context.getTheme();
						if (xmlParser.getName().equals(ACTION_XML_TAG)) {
                            if (currentIntentFilter == null) {
                                throw new InflateException("\"action\" tags can only be declared inside an \"intent-filter\" tag.");
                            }
                            TypedArray a = theme.obtainStyledAttributes(Xml.asAttributeSet(xmlParser), R.styleable.Action, 0, 0);
                            try {
                                String action = a.getString(R.styleable.Action_name);
                                currentIntentFilter.addAction(action);
                            } finally {
                                a.recycle();
                            }

                        } else if (xmlParser.getName().equals(CATEGORY_XML_TAG)) {
                            if (currentIntentFilter == null) {
                                throw new InflateException("\"category\" tags can only be declared inside an \"intent-filter\" tag.");
                            }
                            TypedArray a = theme.obtainStyledAttributes(Xml.asAttributeSet(xmlParser), R.styleable.Category, 0, 0);
                            try {
                                String category = a.getString(R.styleable.Category_name);
                                currentIntentFilter.addCategory(category);
                            } finally {
                                a.recycle();
                            }

                        } else if (xmlParser.getName().equals(ARGUMENT_XML_TAG)) {
                            if (currentPerspective == null) {
                                throw new InflateException("\"argument\" tag can only be declared inside a \"perspective\" tag.");
                            }
                            Bundle arguments = currentPerspective.getArguments();
                            if (arguments == null) {
                                arguments = new Bundle();
                                currentPerspective.setArguments(arguments);
                            }
                            TypedArray a = theme.obtainStyledAttributes(Xml.asAttributeSet(xmlParser), R.styleable.Argument, 0, 0);
                            try {
                                String key = a.getString(R.styleable.Argument_key);
                                if (key == null) {
                                    throw new InflateException("Argument's \"key\" is null");
                                }
                                String value = a.getString(R.styleable.Argument_value);
                                String arrayValue = a.getString(R.styleable.Argument_arrayValue);
                                if (arrayValue != null) {
                                    if (value != null) {
                                        throw new InflateException("\"argument\" tag can only declare value or arrayValue, not both.");
                                    }
                                    String[] arrayValues = TextUtils.split(arrayValue, "\\s*,\\s*");
                                    arguments.putStringArray(key, arrayValues);
                                } else {
                                    //Suporta a colocação apenas da chave, com o valor nulo.
                                    arguments.putString(key, value);
                                }
                            } finally {
                                a.recycle();
                            }

                        } else {
                            throw new InflateException("Unknown tag: " + xmlParser.getName());
                        }
					}
	            } else if (tagType == XmlPullParser.END_TAG) {
	            	if (xmlParser.getName().equals(PERSPECTIVE_XML_TAG)) {
	                    currentPerspective = null;
	                } else if (xmlParser.getName().equals(INTENT_FILTER_XML_TAG)) {
	                	currentIntentFilter = null;
	                }
            	}
	            tagType = xmlParser.next();
	        }
	        
	        if (perspectives == null) {
	        	throw new InflateException("\"perspectives\" tag is missing");
	        }
	        
	        return perspectives.toArray(new PerspectiveInfo[perspectives.size()]);
	    } catch (XmlPullParserException | IOException e) {
	        throw new InflateException("Error inflating perspectives XML", e);
	    }
    }
	
}
