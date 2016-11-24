package br.com.zalem.ymir.client.android.entity.ui.text.mask.basic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import br.com.zalem.ymir.client.android.entity.ui.text.mask.IImageMask;

/**
 * Máscara que formata a imagem em um círculo a partir de suas extremidades, deixando o conteúdo fora do círculo transparente.
 *
 * @author Thiago Gesser
 */
public final class CircularImageMask implements IImageMask {

    private final Resources resources;

    public CircularImageMask(Context context) {
        resources = context.getApplicationContext().getResources();
    }

    @Override
    public Drawable formatImage(Bitmap value) {
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, value);
        roundedBitmapDrawable.setCircular(true);
        return roundedBitmapDrawable;
    }
}
