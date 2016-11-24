package br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout;

import br.com.zalem.ymir.client.android.entity.ui.configuration.field.IFormattableFieldMapping;


/**
 * Mapeamento entre o campo do layout e o campo da entidade de dados.
 *
 * @author Thiago Gesser
 */
public interface ILayoutFieldMapping extends IFormattableFieldMapping {

	/**
	 * Obtém o campo de layout mapeado.<br>
	 * Os campos de layout disponíveis para o mapeamento irão depender do tipo de layout escolhido para o {@link ILayoutConfig}
	 * 
	 * @return o campo de layout obtido.
	 */
	LayoutField getLayoutField();

    /**
     * <b>Configuração opcional, sendo que o padrão é {@link LayoutFieldVisibility#VISIBLE}.</b><br>
     *
     * Indica a visibilidade do campo deste mapeamento no layout, tirando a necessidade de definir um atributo ou relacionamento caso ele não seja visível.<br>
     * Deve ser utilizado quando se deseja utilizar um layout onde nem todos os campos são necessários.<br>
     *
     * @return a visibilidade do campo de layout no mapeamento.
     */
    LayoutFieldVisibility getVisibility();

    /**
     * <b>Configuração opcional, permitida apenas para campos do tipo <code>imagem</code></b>
     *
     * Obtém o atributo do tipo <code>texto</code> que deve ser utiliado para representar o campo de imagem caso ele seja nulo.
     * Atualmente, o atributo deve ser referente à mesma entidade de onde a imagem será obtida e não à entidade dona do layout.
     *
     * @return o atributo obtido ou <code>null</code> caso não seja necessário uma representação da imagem quando o valor for nulo.
     */
    String getSurrogateAttribute();
}
