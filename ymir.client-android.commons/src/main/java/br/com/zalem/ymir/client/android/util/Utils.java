package br.com.zalem.ymir.client.android.util;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Provê métodos utilitários para a aplicação em geral.
 *
 * @author Thiago Gesser
 */
public final class Utils {
	
	private Utils() {
	}

	/**
	 * Esconde o soft input (teclado) se ele estiver visível.
	 * 
	 * @param activity que terá o soft input escondido.
	 */
	public static void hideSoftInput(Activity activity) {
		View currentFocusedView = activity.getCurrentFocus();
		if (currentFocusedView != null) {
			InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
		}
	}
}
