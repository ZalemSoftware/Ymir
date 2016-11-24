package br.com.zalem.ymir.client.android.entity.ui.text.mask;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Representa uma máscara de formatação de valores do tipo <code>imagem</code> para um {@link Drawable}.
 *
 * @author Thiago Gesser
 */
public interface IImageMask extends IMask {
	
	/**
	 * Formata um valor do tipo <code>imagem</code> para um {@link Drawable}.<br>
	 * 
	 * @param value valor do tipo <code>imagem</code>.
	 * @return um Drawable formatado.
	 */
	Drawable formatImage(Bitmap value);
}
