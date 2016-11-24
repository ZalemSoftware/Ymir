package br.com.zalem.ymir.client.android.entity.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.zalem.ymir.client.android.entity.data.IEntityRecord;

/**
 * Define os atributos básicos e as responsabilidades que cada editor de campo de entidade deve possuir.
 * 
 * @author Thiago Gesser
 */
public abstract class AbstractFieldEditor <T> {
	
	private static final String SAVED_FIELD = ".field";
	private static final String SAVED_DIRTY = ".dirty";
			
	private final String fieldName;
	private final boolean virtual;
	private final String label;
	private boolean editable;
	private boolean hidden;

	private OnValueChangeListener valueChangeListener;
	private boolean viewCreated;
	private boolean dirty;
	private T value;

	public AbstractFieldEditor(String fieldName, String label, boolean editable, boolean hidden, boolean virtual) {
		this.fieldName = fieldName;
		this.label = label;
		this.editable = editable;
		this.hidden = hidden;
		this.virtual = virtual;
	}
	
	/**
	 * Avisa ao editor que ele deve cria uma nova {@link View} que permita exibir e alterar o valor do campo deste editor.<br>
	 *
     * @param inflater inflater de layout.
	 * @param parent a View pai.
	 * @return a View criada ou <code>null</code> se o editor está escondido.
	 */
	public final View onCreateView(LayoutInflater inflater, ViewGroup parent) {
		//Editor escondido não possui View.
		if (hidden) {
			return null;
		}
		
		View view = createView(inflater, label, editable, parent);
		viewCreated = true;
        onViewCreated();

		refreshView(value);
		return view;
	}
	
	/**
	 * Avisa ao editor que sua {@link View} criada no {@link #onCreateView(LayoutInflater, ViewGroup)} está sendo destruída.<br>
	 */
	public final void onDestroyView() {
		viewCreated = false;
		destroyView();
	}
	
	/**
	 * Carrega o valor do campo que este editor representa do registro, passando a mostrar o valor carregado
	 * na View do editor.<br>
	 * 
	 * @param record o registro que terá o valor do campo carregado.
	 * @param clearDirt indica se a sujeira do editor deve ser limpa.
	 */
	public final void loadValue(IEntityRecord record, boolean clearDirt) {
		//Se o campo é virtual, não é ligado à fonte de dados, então não carregao valor.
		if (!virtual) {
			setValue(internalLoadValue(record, fieldName));
		}
		
		if (clearDirt) {
			dirty = false;
		}
	}
	
	/**
	 * Armazena o valor atual deste editor no campo que ele representa do registro.<br>
	 * 
	 * @param record o registro que terá o valor do campo armazenado.
	 * @param clearDirt indica se a sujeira do editor deve ser limpa.
	 */
	public final void storeValue(IEntityRecord record, boolean clearDirt) {
		if (!canStoreValue()) {
			return;
		}
		
		//Se o campo é virtual, não é ligado à fonte de dados, então não armazena o valor.
		if (!virtual) {
			internalStoreValue(record, fieldName, getValue());
		}
		
		if (clearDirt) {
			dirty = false;
		}
	}
	
	/**
	 * Salva o estado atual do editor no <code>bundle</code>.
	 * 
	 * @param bundle bundle que terá o estado do editor salvo.
	 */
	public final void saveState(Bundle bundle) {
		internalSaveState(bundle, fieldName + SAVED_FIELD, getValue());
		bundle.putBoolean(fieldName + SAVED_DIRTY, dirty);
	}
	
	/**
	 * Restaura o estado salvo do editor a partir do <code>bundle</code>.
	 * 
	 * @param bundle bundle que será utilizado para restaurar o estado do editor.
	 */
	public final void restoreState(Bundle bundle) {
		value = internalRestoreState(bundle, fieldName + SAVED_FIELD);
		dirty = bundle.getBoolean(fieldName + SAVED_DIRTY);
	}
	
	/**
	 * Define o listener de alteração de valor deste editor.
	 * 		
	 * @param valueChangeListener listener de alteração de valor.
	 */
	public final void setOnValueChangeListener(OnValueChangeListener valueChangeListener) {
		this.valueChangeListener = valueChangeListener; 
	}
	
	/**
	 * Obtém o listener de alteração de valor deste editor.
	 * 
	 * @return o listener obtido.
	 */
	public final OnValueChangeListener getOnValueChangeListener() {
		return valueChangeListener;
	}
	
	/**
	 * Obtém o nome do campo deste editor.
	 * 
	 * @return o nome do campo obtido.
	 */
	public final String getFieldName() {
		return fieldName;
	}
	
	/**
	 * Obtém o rótulo deste editor.
	 * 
	 * @return o rótulo obtido.
	 */
	public final String getLabel() {
		return label;
	}
	
	/**
	 * Indica se este editor está habilitado para a edição.
	 * 
	 * @return <code>true</code> se é editável e <code>false</code> caso contrário.
	 */
	public final boolean isEditable() {
		return editable;
	}

	/**
	 * Define se o editor deve ser editável.
	 *
	 * @param editable <code>true</code> se o editor deve ser editável e <code>false</code> caso contrário.
	 */
	public final void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * Indica se este editor está escondido.
	 *
	 * @return <code>true</code> se está escondido e <code>false</code> caso contrário.
	 */
	public final boolean isHidden() {
		return hidden;
	}

	/**
	 * Define se o editor deve ficar escondido.
	 *
	 * @param hidden <code>true</code> se o editor deve ficar escondido e <code>false</code> caso contrário.
	 */
	public final void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Obtém o valor atual do editor.
	 *
	 * @return o valor obtido.
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Define o novo valor do editor, atualizando a View automaticmente.
	 *
	 * @param value o novo valor.
	 */
	public void setValue(T value) {
		this.value = value;

		tryRefreshView(value);

		notifyValueChanged();
	}

	/**
	 * Indica se o editor é referente a um campo virtual.
	 *
	 * @return <code>true</code> se o editor é referente a um campo virtual e <code>false</code> caso contrário.
	 */
	public final boolean isVirtual() {
		return virtual;
	}

	/**
	 * Indica se o editor está sujo.
	 * 
	 * @return <code>true</code> se o editor está sujo e <code>false</code> caso contrário.
	 */
	public final boolean isDirty() {
		return dirty;
	}


	/**
	 * Notifica que o valor do campo foi alterado, avisando ao listener (se houver) e sujando o campo.
	 */
	protected final void notifyValueChanged() {
		dirty = true;
		if (valueChangeListener != null) {
			valueChangeListener.onValueChange(this);
		}
	}

	/**
	 * Se a View do editor já foi criada, atualiza-a com o novo valor.
	 *
	 * @param newValue novo valor.
	 */
	protected final void tryRefreshView(T newValue) {
		if (viewCreated) {
			refreshView(newValue);
		}
	}

	/**
	 * Indica se a View deste editor foi criada.
	 *
	 * @return <code>true</code> se a View foi criada e <code>false</code> caso contrário.
	 */
	protected final boolean isViewCreated() {
		return viewCreated;
	}


	/**
	 * Determina se o valor deste campo pode ser armazenado no registro. Por padrão, o valor só pode ser
	 * armazenado se o campo estive sujo.<br>
	 * Pode ser sobrescrito pela subclasse para customizar decisão de armazenar ou não o valor do campo.
	 *  
	 * @return <code>true</code> se o valor puder ser armazenado e <code>false</code> caso contrário.
	 */
	protected boolean canStoreValue() {
		return dirty;
	}

    /**
     * Chamado logo após a criação da View do editor através do metódo {@link #createView(LayoutInflater, String, boolean, ViewGroup)}.<br>
     * Pode ser utilizado para fazer inicializações tendo a certeza que toda a hierarquia de Views foi criada.
     */
    protected void onViewCreated() {
    }
	
	/**
	 * Deve chamar o método <code>visit</code> do visitador correspondente à sua classe.
	 *  
	 * @param visitor visitador.
	 * @return o retorno do método <code>visit</code> chamado.
	 */
	public abstract boolean accept(IFieldEditorVisitor visitor);

    /**
     * Obtém a {@link View} utilizada para exibir e alterar os valores do editor.
     *
     * @return a View obtida ou <code>null</code> se a View ainda não tiver sido criada.
     */
    public abstract View getView();

	/**
	 * Cria uma {@link View} que permita exibir e alterar o valor do campo deste editor.<br>
	 * Pode ser utilizado para o armazenamento de referências às Views criadas visando o uso posterior no {@link #refreshView(Object)}. 
	 *
	 * @param inflater inflater de recursos de layout
	 * @param label rótulo do campo
	 * @param editable define se o campo é editável ou não.
	 * @param parent a View pai.
	 * @return a View criada.
	 */
	protected abstract View createView(LayoutInflater inflater, String label, boolean editable, ViewGroup parent);
	
	/**
	 * Destrói qualquer referência para Views criadas no {@link #onCreateView(LayoutInflater, ViewGroup)}.
	 */
	protected abstract void destroyView();
	
	/**
	 * Atualiza os dados nas Views criadas no {@link #onCreateView(LayoutInflater, ViewGroup)} de acordo com o novo valor definido no
	 * campo.
	 * 
	 * @param newValue novo valor.
	 */
	protected abstract void refreshView(T newValue);

	/**
	 * Obtém o valor do campo que este editor representa do registro.
	 * 
	 * @param record o registro que terá o valor do campo obtido.
	 * @param fieldName o nome do campo.
	 * @return o valor obtido.
	 */
	protected abstract T internalLoadValue(IEntityRecord record, String fieldName);
	
	/**
	 * Armazena o valor desde editor no campo que ele representa do registro.
	 * 
	 * @param record o registro que terá o valor do campo armazenado.
	 * @param fieldName o nome do campo.
	 * @param value o valor.
	 */
	protected abstract void internalStoreValue(IEntityRecord record, String fieldName, T value);

	/**
	 * Restaura o estado salvo do editor a partir do <code>bundle</code>, retornando seu valor.
	 * 
	 * @param bundle bundle que será utilizado para restaurar o estado do editor.
	 * @param key chave base que foi utilizada no salvamento dos estados.
	 * @return o valor restaurado do editor.
	 */
	protected abstract T internalRestoreState(Bundle bundle, String key);
	
	/**
	 * Salva o estado atual do editor no <code>bundle</code>, incluindo seu valor.
	 * 
	 * @param bundle bundle que terá o estado do editor salvo.
	 * @param key chave base que deve ser utilizada no salvamento dos estados.
	 * @param value valor que deve ser salvo.
	 */
	protected abstract void internalSaveState(Bundle bundle, String key, T value);
	
	
	/**
	 * Listener de alteração de valor de editores de campos.
	 */
	public interface OnValueChangeListener {
		
		/**
		 * Chamado quando o valor do editor é alterado.
		 * 
		 * @param editor editor que teve o valor alterado.
		 */
		void onValueChange(AbstractFieldEditor<?> editor);
	}

}