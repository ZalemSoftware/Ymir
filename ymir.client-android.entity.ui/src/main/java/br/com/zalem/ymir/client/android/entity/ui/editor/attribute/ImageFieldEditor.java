package br.com.zalem.ymir.client.android.entity.ui.editor.attribute;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;
import br.com.zalem.ymir.client.android.entity.ui.R;
import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractLabeledFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.text.mask.IImageMask;
import br.com.zalem.ymir.client.android.util.PendingFeatureException;

/**
 * Editor de campo referente a um atributo do tipo <code>imagem</code> da entidade.
 *
 * @author Thiago Gesser
 */
public final class ImageFieldEditor extends AbstractLabeledFieldEditor<Bitmap> implements OnClickListener {

    private final IImageMask mask;

    private IImageFieldEditorListener listener;
    private ImageView valueImageView;
    private ImageView nullImageView;

    public ImageFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual, String help,
                            IImageMask mask) {
		super(fieldName, label, editable, hidden, virtual, help);
		this.mask = mask;

        //Sempre mostra o label deste campo.
        setManualLabelVisibility(true);
	}

    @Override
    protected ViewGroup createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent) {
        ViewGroup rootView = super.createView(inflater, label, editable, parent);

		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.entity_field_editor_image, parent, false);
        valueImageView = (ImageView) layout.findViewById(R.id.entity_field_editor_image_value);
		valueImageView.setOnClickListener(this);
        nullImageView = (ImageView) layout.findViewById(R.id.entity_field_editor_image_null);
        nullImageView.setOnClickListener(this);

        rootView.addView(layout, 1);
        return rootView;
	}

    @Override
    protected void destroyView() {
        super.destroyView();

        valueImageView = null;
        nullImageView = null;
	}

    @Override
    protected void refreshView(Bitmap newValue) {
        super.refreshView(newValue);

        if (newValue == null) {
            nullImageView.setVisibility(View.VISIBLE);
            valueImageView.setVisibility(View.GONE);
        } else {
            valueImageView.setVisibility(View.VISIBLE);
            nullImageView.setVisibility(View.GONE);
            valueImageView.setImageDrawable(mask.formatImage(newValue));
        }
    }

    @Override
    public View getView() {
        return getValue() == null ? nullImageView : valueImageView;
    }

    @Override
	protected Bitmap internalLoadValue(IEntityRecord record, String fieldName) {
		return record.getImageValue(fieldName);
	}

	@Override
	protected void internalStoreValue(IEntityRecord record, String fieldName, Bitmap value) {
		record.setImageValue(fieldName, value);
	}

	@Override
	protected Bitmap internalRestoreState(Bundle bundle, String key) {
        super.internalRestoreState(bundle, key);

		return bundle.getParcelable(key);
	}

	@Override
	protected void internalSaveState(Bundle bundle, String key, Bitmap value) {
        super.internalSaveState(bundle, key, value);

        bundle.putParcelable(key, value);
	}

    @Override
    protected void tintError(boolean hasError, boolean hadError) {
        //Não pinta este editor quando houver erro.
    }

	@Override
	public boolean accept(IFieldEditorVisitor visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public void onClick(View v) {
        //Se o listener retornou true, significa que ele mesmo tratou a criação.
        if (listener != null && listener.onImageSelection(this)) {
            return;
        }

        throw new PendingFeatureException("Default ImageFieldEditor selection");
	}


    /**
     * Define o listener de ações efetuadas pelo editor.
     *
     * @param listener o listener.
     */
    public void setEditorListener(IImageFieldEditorListener listener) {
        this.listener = listener;
    }

    /**
     * Obtém o listener definido para as ações efetuadas pelo editor.
     *
     * @return o listener obtido ou <code>null</code> caso nenhum listener tenha sido definido.
     */
    public IImageFieldEditorListener getEditorListener() {
        return listener;
    }


    /**
     * Listener de ações efetuadas pelo editor de imagem.<br>
     * Todos os métodos possuem um retorno booleano que indica se o próprio listener vai tratar a ação ou se vai deixar o
     * tratamento para o editor.
     *
     * @author Thiago Gesser
     */
    public interface IImageFieldEditorListener {

        /**
         * Chamado quando o usuário iniciou a seleção de uma imagem pelo editor.
         *
         * @return <code>true</code> se a ação de selecionar imagem já foi tratada e <code>false</code> se é o editor que deve tratar esta ação.
         */
        boolean onImageSelection(ImageFieldEditor editor);
    }
}