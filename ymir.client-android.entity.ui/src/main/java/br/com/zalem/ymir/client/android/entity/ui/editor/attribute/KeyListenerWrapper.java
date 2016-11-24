package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.text.Editable;
import android.text.InputFilter;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Engloba um {@link KeyListener}, passando a atuar como o KeyListener englobado.<br>
 * Pode ser utilizado para definir um KeyListener sem que o {@link TextView} use ele como {@link InputFilter} também.
 *
 * @author Thiago Gesser
 */
public final class KeyListenerWrapper implements KeyListener {
	private final KeyListener internalKeyListener;
	private final int inputTypeFlags;
	
	public KeyListenerWrapper(KeyListener internalKeyListener) {
		this(internalKeyListener, 0);
	}
	
	public KeyListenerWrapper(KeyListener internalKeyListener, int inputTypeFlags) {
		this.internalKeyListener = internalKeyListener;
		this.inputTypeFlags = inputTypeFlags;
	}
	
	@Override
	public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event) {
		return internalKeyListener.onKeyUp(view, text, keyCode, event);
	}
	
	@Override
	public boolean onKeyOther(View view, Editable text, KeyEvent event) {
		return internalKeyListener.onKeyOther(view, text, event);
	}
	
	@Override
	public boolean onKeyDown(View view, Editable text, int keyCode, KeyEvent event) {
		return internalKeyListener.onKeyDown(view, text, keyCode, event);
	}
	
	@Override
	public int getInputType() {
		return internalKeyListener.getInputType() | inputTypeFlags;
	}
	
	@Override
	public void clearMetaKeyState(View view, Editable content, int states) {
		internalKeyListener.clearMetaKeyState(view, content, states);
	}
}
