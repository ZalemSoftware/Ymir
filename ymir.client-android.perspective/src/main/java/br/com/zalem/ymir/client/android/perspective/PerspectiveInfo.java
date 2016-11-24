package br.com.zalem.ymir.client.android.perspective;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import br.com.zalem.ymir.client.android.widget.SquareLayout.Measure;

/**
 * Informações referentes a uma {@link Perspective}. Estas informações são obtidas através da definição de perspectivas,
 * que pode ser feita conforme descrito em {@link PerspectiveInfoInflater}.
 * 
 * @author Thiago Gesser
 */
public final class PerspectiveInfo {
	
	private final String title;
	private final int theme;
	private final Class<? extends Perspective> perspectiveClass;
	private final LaunchMode launchMode;
	private IntentFilter intentFilter;
	private Bundle arguments;
	
	@SuppressWarnings("unchecked")
	PerspectiveInfo(Context context, AttributeSet attr) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attr, R.styleable.Perspective, 0, 0);
		try {
			//"title" é opcional.
			this.title = a.getString(R.styleable.Perspective_title);

			//"theme" é opcional.
			this.theme = a.getResourceId(R.styleable.Perspective_theme, 0);

			//"className" é obrigatório.
			String perspectiveClassString = a.getString(R.styleable.Perspective_className);
			if (TextUtils.isEmpty(perspectiveClassString)) {
				throw new IllegalArgumentException("\"clazz\" is empty or null.");
			}
			
			try {
				Class<?> perspectiveClass = Class.forName(perspectiveClassString);
				if (!Perspective.class.isAssignableFrom(perspectiveClass)) {
					throw new IllegalArgumentException("\"clazz\" does not refer to a Perspective: " + perspectiveClass);
				}
				
				this.perspectiveClass = (Class<? extends Perspective>) perspectiveClass;
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			}
			
			//"launchMode" é opcional.
        	int launchModeInt = a.getInteger(R.styleable.Perspective_launchMode, LaunchMode.STANDARD.ordinal());
        	if (launchModeInt < 0 || launchModeInt >= Measure.values().length) {
        		throw new IllegalArgumentException("Invalid launchMode: " + launchModeInt);
        	}
			this.launchMode = LaunchMode.values()[launchModeInt];
		} finally {
			a.recycle();
		}
	}
	
	void setIntentFilter(IntentFilter intentFilter) {
		this.intentFilter = intentFilter;
	}
	
	void setArguments(Bundle arguments) {
		this.arguments = arguments;
	}
	
	public String getTitle() {
		return title;
	}

	public int getTheme() {
		return theme;
	}

	public Class<? extends Perspective> getPerspectiveClass() {
		return perspectiveClass;
	}
	
	public LaunchMode getLaunchMode() {
		return launchMode;
	}

	public IntentFilter getIntentFilter() {
		return intentFilter;
	}
	
	public Bundle getArguments() {
		return arguments;
	}
	
	
	/**
	 * Modo de lançamento de perspectivas.
	 */
	public enum LaunchMode {
		/**
		 * Modo padrão. Cria uma nova instância da perspectiva a cada lançamento.
		 */
		STANDARD,
		
		/**
		 * Modo singular. Cria a instâncida da perspectiva apenas no primeiro lançamento, passando a utilizar a mesma
		 * instância nos próximos lançamentos. -->
		 */
		SINGLE
	}
	
	/**
	 * Filtro de Intent utilizado por uma perspectiva.
	 */
	public static final class IntentFilter { 
		/*
		 * TODO Verificar sobre o uso de memória destes sets. Eles ficarão na memória durante toda a vida da aplicação,
		 * então talvez seja melhor sacrificar desempenho pela memória.
		 */
		private final Set<String> actions;
		private final Set<String> categories;
		
		IntentFilter() {
			this.actions = new HashSet<>();
			this.categories = new HashSet<>();
		}
		
		void addAction(String action) {
			if (TextUtils.isEmpty(action)) {
				throw new IllegalArgumentException("\"action\" is empty or null.");
			}
			
			actions.add(action.intern());
		}
		
		void addCategory(String category) {
			if (TextUtils.isEmpty(category)) {
				throw new IllegalArgumentException("\"category\" is empty or null.");
			}
			
			categories.add(category.intern());
		}
		
		public Set<String> getActions() {
			return Collections.unmodifiableSet(actions);
		}
		
		public Set<String> getCategories() {
			return Collections.unmodifiableSet(categories);
		}
		
		public boolean hasAction(String action) {
			return actions.contains(action);
		}
		
		public boolean hasCategory(String category) {
			return categories.contains(category);
		}
	}
}