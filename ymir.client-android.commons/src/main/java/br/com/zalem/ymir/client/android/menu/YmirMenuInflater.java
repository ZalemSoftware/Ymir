package br.com.zalem.ymir.client.android.menu;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.InflateException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import br.com.zalem.ymir.client.android.commons.R;

/**
 * Inflater de menus do Ymir, utilizado para transformar recursos XML em objetos de {@link YmirMenu}.<br>
 * Cada recurso XML pode conter apenas uma definição de menu, mas não há limites para o número de items
 * neste menu.<br>
 * É possível definir um Intent nos itens do menu. Se o Intent tiver apenas uma categoria, ela pode ser
 * definida como um simples atributo. Se não, o Intent pode ter quantas tags filhas de categoria desejar.<br>
 * Segue um exemplo de recurso XML:
 * <pre>{@code
 *  <?xml version="1.0" encoding="utf-8"?>
 *  <ymirMenu xmlns:ymir="http://schemas.android.com/apk/res-auto">
 *    <item ymir:title="@string/title2">
 *      <intent
 *         ymir:className="br.com.code.ymir.client.android.example.Perspective1"
 *         ymir:action="br.com.code.ymir.client.android.example.ACTION_1"
 *         ymir:category="Category1" />
 *    </item>
 *    <item ymir:title="@string/title2">
 *      <intent ymir:action="br.com.code.ymir.client.android.example.ACTION_2">
 *         <category ymir:name="Category2" />
 *         <category ymir:name="Category3" />
 *         <category ymir:name="Category4" />
 *      </intent>
 *    </item>
 *  </ymirMenu>
 * }</pre>
 * 
 * @author Thiago Gesser
 */
public final class YmirMenuInflater {
	
	private static final String YMIR_MENU_XML_TAG = "ymirMenu";
	private static final String YMIR_MENU_GROUP_XML_TAG = "group";
	private static final String YMIR_MENU_ITEM_XML_TAG = "item";
	private static final String INTENT_XML_TAG = "intent";
	private static final String CATEGORY_XML_TAG = "category";
	
	private final Context context;

	public YmirMenuInflater(Context context) {
		this.context = context;
	}

	/**
	 * Infla um {@link YmirMenu} a partir de um recurso XML que define um menu.
	 * 
	 * @param menuRes recurso XML de menu.
	 * @return o YmirMenu gerado.
	 * @throws android.view.InflateException se houve algum problema no formato ou nas tags do XML.
	 * @throws IllegalArgumentException se algum atributo do menu ou dos items estava errado.
	 */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
	public YmirMenu inflate(int menuRes) {
        YmirMenu menu = new YmirMenu();
        inflate(menuRes, menu);
        return menu;
	}
	
    /**
     * Infla um {@link YmirMenu} a partir de um recurso XML e adiciona seus itens em outro <code>menu</code>.
     *
     * @param menuRes recurso XML do menu que será inflado.
     * @param menu menu que receberá os items inflados.
     * @throws android.view.InflateException se houve algum problema no formato ou nas tags do XML.
     * @throws IllegalArgumentException se algum atributo do menu ou dos items estava errado.
     */
    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    public void inflate(int menuRes, YmirMenu menu) {
        XmlResourceParser xmlParser = context.getResources().getLayout(menuRes);
        try {
            inflate(xmlParser, menu);
        } finally {
            xmlParser.close();
        }
    }

    /**
     * Infla um {@link YmirMenu} a partir de um XmlResourceParser de um XML menu.
     *
     * @param xmlParser parser de um XML de menu.
     * @return o YmirMenu gerado.
     * @throws android.view.InflateException se houve algum problema no formato ou nas tags do XML.
     * @throws IllegalArgumentException se algum atributo do menu ou dos items estava errado.
     */
    public YmirMenu inflate(XmlResourceParser xmlParser) {
        YmirMenu menu = new YmirMenu();
        inflate(xmlParser, menu);
        return menu;
    }


    /*
     * Métodos auxiliares
     */

    private void inflate(XmlResourceParser xmlParser, YmirMenu menu) {
        try {
            YmirMenu currentMenu = null;
            int currentGroupId = 0;
            YmirMenuItem currentItem = null;
            Intent currentIntent = null;
            boolean hasSingleCategory = false;

            int tagType = xmlParser.next();
            while (tagType != XmlPullParser.END_DOCUMENT) {
                if (tagType == XmlPullParser.START_TAG) {
                    if (xmlParser.getName().equals(YMIR_MENU_XML_TAG)) {
                        if (currentMenu != null) {
                            throw new InflateException("\"ymirMenu\" tag must be declared only once.");
                        }
                        currentMenu = menu;

                    } else if (xmlParser.getName().equals(YMIR_MENU_GROUP_XML_TAG)) {
                        if (currentMenu == null || currentGroupId != 0) {
                            throw new InflateException("\"group\" tags can only be declared inside a \"ymirMenu\" tag.");
                        }
                        AttributeSet attributeSet = Xml.asAttributeSet(xmlParser);
                        TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.YmirMenuGroup, 0, 0);
                        currentGroupId = a.getResourceId(R.styleable.YmirMenuGroup_id, 0);
                        if (currentGroupId == 0) {
                            throw new InflateException("\"group id\" should be defined.");
                        }

                    } else if (xmlParser.getName().equals(YMIR_MENU_ITEM_XML_TAG)) {
                        if (currentMenu == null || currentItem != null) {
                            throw new InflateException("\"item\" tags can only be declared inside a \"ymirMenu\" tag.");
                        }
                        AttributeSet attributeSet = Xml.asAttributeSet(xmlParser);
                        currentItem = new YmirMenuItem(context, attributeSet, currentGroupId);
                        currentMenu.addItem(currentItem);

                    } else if (xmlParser.getName().equals(INTENT_XML_TAG)) {
                        if (currentItem == null || currentIntent != null) {
                            throw new InflateException("\"intent\" tags can only be declared inside an \"item\" tag.");
                        }
                        currentIntent = createIntent(context, Xml.asAttributeSet(xmlParser));
                        currentItem.setIntent(currentIntent);
                        hasSingleCategory = currentIntent.getCategories() != null;

                    } else if (xmlParser.getName().equals(CATEGORY_XML_TAG)) {
                        if (currentIntent == null) {
                            throw new InflateException("\"category\" tags can only be declared inside an \"intent\" tag.");
                        }
                        if (hasSingleCategory) {
                            throw new InflateException("\"category\" tags can only be declared if the \"category\" attribute was not defined in the \"intent\" tag.");
                        }
                        String category = xmlParser.getAttributeValue(R.styleable.Category_name);
                        currentIntent.addCategory(category);

                    } else {
                        throw new InflateException("Unknown tag: " + xmlParser.getName());
                    }
                } else if (tagType == XmlPullParser.END_TAG) {
                    if (xmlParser.getName().equals(YMIR_MENU_ITEM_XML_TAG)) {
                        currentItem = null;
                    } else if (xmlParser.getName().equals(YMIR_MENU_GROUP_XML_TAG)) {
                        currentGroupId = 0;
                    } else if (xmlParser.getName().equals(INTENT_XML_TAG)) {
                        boolean hasClass = currentIntent.getComponent() != null;
                        boolean hasAction = currentIntent.getAction() != null;
                        boolean hasCategory = currentIntent.getCategories() != null;
                        if (!hasClass && !hasAction) {
                            throw new InflateException("\"intent\" tag must declare at least one of the following attributes: \"className\" or \"action\".");
                        }
                        if (hasCategory && !hasAction) {
                            throw new InflateException("\"intent\" tag can only declare \"categories\" if the \"action\" attribute is defined.");
                        }
                        currentIntent = null;
                    }
                }
                tagType = xmlParser.next();
            }

            if (currentMenu == null) {
                throw new InflateException("\"ymirMenu\" tag is missing");
            }
        } catch (XmlPullParserException | IOException e) {
            throw new InflateException("Error inflating ymir menu XML", e);
        }
    }

    private static Intent createIntent(Context context, AttributeSet attr) {
    	TypedArray a = context.getTheme().obtainStyledAttributes(attr, R.styleable.Intent, 0, 0);
    	try {
    		Intent intent = new Intent();
    		String className = a.getString(R.styleable.Intent_className);
    		if (className != null) {
    			try {
    				Class<?> intentClass = Class.forName(className);

    				intent.setClass(context, intentClass);
    			} catch (ClassNotFoundException e) {
    				throw new IllegalArgumentException(e);
    			}
    		}
    		
    		String action = a.getString(R.styleable.Intent_action);
    		if (action != null) {
    			intent.setAction(action);
    		}
    		
    		String category = a.getString(R.styleable.Intent_category);
    		if (category != null) {
    			intent.addCategory(category);
    		}
    		
    		int flags = a.getInt(R.styleable.Intent_flags, -1);
    		if (flags > 0) {
    			intent.addFlags(flags);
    		}
    		
    		return intent;
		} finally {
			a.recycle();
		}
	}
}