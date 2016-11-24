package br.com.zalem.ymir.client.android.entity.ui.editor.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import br.com.zalem.ymir.client.android.entity.ui.editor.AbstractFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.IFieldEditorVisitor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.BooleanFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DateFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.DecimalFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.EnumAttributeEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.ImageFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.IntegerFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TextFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.attribute.TimeFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.EnumRelationshipEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.MultipleRelationshipFieldEditor;
import br.com.zalem.ymir.client.android.entity.ui.editor.relationship.SingleRelationshipFieldEditor;

/**
 * Adapter de editores de campos para um layout customizado.<br>
 * O layout é definido através de um recurso de layout que pode conter qualquer estrutura de Views. Os editores são colocados no layout através da
 * substituição de determinadas Views contidas nele. Para que uma View seja substituída por um editor, ela deve declarar a propriedade <code>tag</code>
 * com o nome do campo do editor precedido por um dos seguintes prefixos: "{@value #ATTRIBUTE_NAME_PREFIX}" para editores de atributos e "{@value #RELATIONSHIP_NAME_PREFIX}"
 * para editores de relacionamentos. Os atributos de layout da View substituída são repassados para a View do editor.<br>
 * Exemplo de layout:
 *
 * <pre>{@code
 *  <ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
 *      android:layout_width="match_parent"
 *      android:layout_height="match_parent"
 *      android:paddingTop="@dimen/default_field_editor_list_vertical_margin"
 *      android:paddingBottom="@dimen/default_field_editor_list_vertical_margin"
 *      android:paddingLeft="@dimen/default_field_editor_list_horizontal_margin"
 *      android:paddingRight="@dimen/default_field_editor_list_horizontal_margin" >
 *
 *      <LinearLayout
 *          android:layout_width="match_parent"
 *          android:layout_height="wrap_content"
 *          android:orientation="vertical"
 *          android:showDividers="middle"
 *          android:divider="?attr/entityEditingFieldsDivider">
 *
 *          <!-- Será substituída pelo editor do relacionamento "product" -->
 *          <View
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"
 *              android:tag="relationship_product" />
 *
 *          <LinearLayout
 *              android:layout_width="match_parent"
 *              android:layout_height="wrap_content"
 *              android:orientation="horizontal">
 *
 *              <!-- Será substituída pelo editor do atributo "quantity" -->
 *              <View
 *                  android:layout_weight="1"
 *                  android:layout_width="0dp"
 *                  android:layout_height="wrap_content"
 *                  android:tag="attribute_quantity" />
 *              <!-- Será substituída pelo editor do atributo "stock" -->
 *              <View
 *                  android:layout_weight="1"
 *                  android:layout_width="0dp"
 *                  android:layout_height="wrap_content"
 *                  android:tag="attribute_stock" />
 *          </LinearLayout>
 *      </LinearLayout>
 *  </ScrollView>
 * }</pre>
 *
 * @author Thiago Gesser
 */
public final class LayoutFieldEditorAdapter extends DefaultFieldEditorAdapter {

    private static final String ATTRIBUTE_NAME_PREFIX = "attribute_";
    private static final String RELATIONSHIP_NAME_PREFIX = "relationship_";
    private final int layoutResId;

    public LayoutFieldEditorAdapter(Context context, String layoutName) {
        this(context, Collections.<AbstractFieldEditor<?>>emptyList(), layoutName);
    }

    public LayoutFieldEditorAdapter(Context context, List<AbstractFieldEditor<?>> editors, String layoutName) {
        this(context, editors, getLayoutResId(context, layoutName));
    }

    public LayoutFieldEditorAdapter(Context context, List<AbstractFieldEditor<?>> editors, int layoutResId) {
        super(context, editors);
        this.layoutResId = layoutResId;
    }

    @Override
    @SuppressLint("Assert")
    public View getView(LayoutInflater inflater, ViewGroup parent) {
        View layoutView = inflater.inflate(layoutResId, parent, false);
        //Utiliza o contexto da View pai pois ela pode estar influenciado por outro tema.
        inflater = LayoutInflater.from(layoutView.getContext());

        //Substitui os Stubs pelos editores.
        IsRelationshipEditorVisitor isRelationshipVisitor = new IsRelationshipEditorVisitor();
        for (AbstractFieldEditor<?> editor : getActiveEditors()) {
            String prefix;
            if (editor.accept(isRelationshipVisitor)) {
                prefix = RELATIONSHIP_NAME_PREFIX;
            } else {
                prefix = ATTRIBUTE_NAME_PREFIX;
            }

            View stubView = layoutView.findViewWithTag(prefix + editor.getFieldName());
            if (stubView == null) {
                //Se o stub n foi declarado no layout, não mostra o editor.
                continue;
            }

            ViewGroup stubParent = (ViewGroup) stubView.getParent();
            if (stubParent == null) {
                //O stub é o próprio layout, então pode parar tudo por aqui.
                assert layoutView == stubView;
                return editor.onCreateView(inflater, parent);
            }

            //Remove o stub do pai.
            int stubIndex = stubParent.indexOfChild(stubView);
            stubParent.removeViewInLayout(stubView);

            //Adiciona a View do editor no lugar do stub.
            View editorView = editor.onCreateView(inflater, stubParent);
            ViewGroup.LayoutParams layoutParams = stubView.getLayoutParams();
            if (layoutParams != null) {
                stubParent.addView(editorView, stubIndex, layoutParams);
            } else {
                stubParent.addView(editorView, stubIndex);
            }
        }

        return layoutView;
    }


    private static int getLayoutResId(Context context, String layoutName) {
        int layoutResId = context.getResources().getIdentifier(layoutName, "layout", context.getPackageName());
        if (layoutResId == 0) {
            throw new IllegalArgumentException("There is no layout resource with the name: " + layoutName);
        }
        return layoutResId;
    }


    /**
     * Visitador que verifica se determinado editor é de um relacionamento ou de um atributo.
     */
    private static final class IsRelationshipEditorVisitor implements IFieldEditorVisitor {

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
        public boolean visit(EnumAttributeEditor editor) {
            return false;
        }

        @Override
        public boolean visit(EnumRelationshipEditor editor) {
            return true;
        }

        @Override
        public boolean visit(SingleRelationshipFieldEditor editor) {
            return true;
        }

        @Override
        public boolean visit(MultipleRelationshipFieldEditor editor) {
            return true;
        }
    }
}
