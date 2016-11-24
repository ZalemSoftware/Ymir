package br.com.zalem.ymir.client.android.entity.ui.configuration.editing;

/**
 * Configurações que envolvem a edição de uma entidade.<br>
 * Deve ser definido pelo menos uma aba ou os campos no próprio IEditingConfig, mas não ambos.<br>
 * Deve ser definido pelo menos uma configuração de permissões (locais ou remoto), de modo que alguma edição nos registros 
 * seja habilitada.
 *
 * @author Thiago Gesser
 */
public interface IEditingConfig {

	/**
	 * <b>Configuração opcional se as permissões nos registros remotos já foram definidas</b><br>
	 * <br>
	 * Obtém as permissões das operações nos registros locais da entidade.
	 * 
	 * @return as permissões obtidas.
	 */
	IEditingPermissions getLocalPermissions();

	/**
	 * <b>Configuração opcional se as permissões nos registros locais já foram definidas</b><br>
	 * <br>
	 * Obtém as permissões das operações nos registros da entidade integrantes da fonte de dados.
	 * 
	 * @return as permissões obtidas.
	 */
	IEditingPermissions getDataSourcePermissions();


    /**
     * <b>Mutuamente exclusivo com a configuração de campos.</b><br>
     * <br>
     * Obtém as abas nas quais os campos de edição do registro devem estar divididos.<br>
     *
     * @return as abas obtidas.
     */
    IEditingTab[] getTabs();

    /**
     * <b>Mutuamente exclusivo com a configuração de abas.</b><br>
     * <br>
     * Obtém os campos a serem disponibilizados para a edição do registro.
     *
     * @return os campos obtidos.
     */
    IEditingFieldMapping[] getFields();

    /**
     * <b>Configuração opcional.</b><br>
     * <br>
     * Determina se o ação de resumir o registro sendo editado está habilitada. Por padrão está desabilitada.
     *
     * @return <code>true</code> se o resumo está habilitado e <code>false</code> caso contrário.
     */
    boolean isEnableSummarize();

    /**
     * <b>Configuração opcional</b><br>
     * <br>
     * Obtém o nome do recurso de layout que permite customizar a forma como os editores de campos são exibidos. Os editores substituirão as Views
     * que declaram a propriedade <code>tag</code> com o nome de seu campo. O nome do campo deve ser precedido por um dos seguintes prefixos:
     * "attribute_" para editores de atributos e "relationship_" para editores de relacionamentos. Os atributos de layout da View substituída
     * são repassados para a View do editor. Exemplo de layout:
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
     * @return o nome do layout customizado.
     */
    String getLayout();
}
