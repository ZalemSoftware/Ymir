package br.com.zalem.ymir.client.android.entity.ui.editor;

import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.BooleanFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DateFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DecimalFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.ImageFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.IntegerFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.EnumRelationshipEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.MultipleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.SingleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TextFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TimeFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.EnumAttributeEditor;
import br.com.zalem.ymir.client.android.entity.ui.fragment.EntityEditingFragment;

/**
 * Visitador de editores de campos (subclasses de {@link AbstractFieldEditor}).<br>
 * Pode ser utilizado em um editor através do método {@link AbstractFieldEditor#accept(IFieldEditorVisitor)}.<br>
 * <br>
 * Os métodos de visitação retornam uma flag de controle que pode ser utilizada por classes que intermediam a visitação.
 * Por exemplo, o {@link EntityEditingFragment} utiliza este retorno para saber se deve interromper e visitação dos
 * editores que ele mantém.
 *
 * @see AbstractFieldEditor
 *
 * @author Thiago Gesser
 */
public interface IFieldEditorVisitor {

	/**
	 * Visita um {@link IntegerFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(IntegerFieldEditor editor);
	
	/**
	 * Visita um {@link DecimalFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(DecimalFieldEditor editor);
	
	/**
	 * Visita um {@link TextFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(TextFieldEditor editor);
	
	/**
	 * Visita um {@link BooleanFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(BooleanFieldEditor editor);
	
	/**
	 * Visita um {@link DateFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(DateFieldEditor editor);
	
	/**
	 * Visita um {@link TimeFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(TimeFieldEditor editor);

    /**
     * Visita um {@link ImageFieldEditor}.
     *
     * @param editor o editor que está sendo visitado.
     * @return flag de controle se for utilizada ou <code>false</code> caso contrário.
     */
    boolean visit(ImageFieldEditor editor);

	
	/**
	 * Visita um {@link SingleRelationshipFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(SingleRelationshipFieldEditor editor);
	
	/**
	 * Visita um {@link MultipleRelationshipFieldEditor}.
	 * 
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário. 
	 */
	boolean visit(MultipleRelationshipFieldEditor editor);
	
	/**
	 * Visita um {@link EnumAttributeEditor}.
	 *
	 * @param editor o editor que está sendo visitado.
	 * @return flag de controle se for utilizada ou <code>false</code> caso contrário.
	 */
	boolean visit(EnumAttributeEditor editor);

    /**
     * Visita um {@link EnumRelationshipEditor}.
     *
     * @param editor o editor que está sendo visitado.
     * @return flag de controle se for utilizada ou <code>false</code> caso contrário.
     */
    boolean visit(EnumRelationshipEditor editor);

	
	/**
	 * Adaptador de visitador de editores de campos que visa a escolha de quais métodos de visitação
	 * se deseja implementar.
	 * 
	 * @author Thiago Gesser
	 */
	abstract class FieldEditorVisitorAdapter implements IFieldEditorVisitor {

		@Override
		public boolean visit(IntegerFieldEditor editor) {
			return false;
		}

		@Override
		public boolean visit(DecimalFieldEditor editor) {
			return false;
		}

		@Override
		public boolean visit(TextFieldEditor editor) {
			return false;
		}

		@Override
		public boolean visit(BooleanFieldEditor editor) {
			return false;
		}

		@Override
		public boolean visit(DateFieldEditor editor) {
			return false;
		}

		@Override
		public boolean visit(TimeFieldEditor editor) {
			return false;
		}

		@Override
		public boolean visit(ImageFieldEditor editor) {
			return false;
		}

		@Override
		public boolean visit(SingleRelationshipFieldEditor editor) {
			return false;
		} 
		
		@Override
		public boolean visit(MultipleRelationshipFieldEditor editor) {
			return false;
		}

        @Override
        public boolean visit(EnumAttributeEditor editor) {
            return false;
        }

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
            return false;
        }
    }
}
