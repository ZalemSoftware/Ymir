package android.preference;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

/**
 * Versão aprimorada do {@link PreferenceCategory}.<br>
 * Categorias irmãs do tipo EnhancedPreferenceCategory com a mesma chave são consideradas iguais, fazendo com que seus conteúdos sejam concatenados
 * em uma única categoria. Desta forma, é possível declarar preferências da mesma categoria em arquivos diferentes.
 *
 * @author Thiago Gesser
 */
public final class EnhancedPreferenceCategory extends PreferenceCategory {

    private EnhancedPreferenceCategory targetCategory;

    public EnhancedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EnhancedPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EnhancedPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnhancedPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EnhancedPreferenceCategory)) {
            return false;
        }

        if (!hasKey()) {
            return false;
        }
        EnhancedPreferenceCategory anCategory = (EnhancedPreferenceCategory) o;
        if (!anCategory.hasKey()) {
            return false;
        }
        if (!getKey().equals(anCategory.getKey())) {
            return false;
        }

        //TODO Verificar este contorno quando a versão mínima do Android for 4.3+ ou a parte de preferências da support library for melhorada.
        //Contorno para fazer com que a categoria não seja adicionada no pai se já tiver um irmão igual (pois o pai faz um "contains" antes
        //de adicionar) e com que os filhos da categoria sejam adicionadas no irmão igual.
        //Foi necessário fazer o comportamento desta forma pelas seguintes razões:
            //O PreferenceScreen é final, o que impede de estendê-lo para controlar os filhos iguais.
            //Não há forma de obter a categoria pai pois:
                //o Preference não armazena o pai;
                //o "onParentChanged" não existia nas versões 4.3 ou inferior.
        //A API de preferências da support library é limitada em algumas classes (o EditTextPreference, por exemplo, não daria para fazer o EnhancedEditTextPreference).
        targetCategory = anCategory;
        return true;
    }

    @Override
    public boolean addPreference(@NonNull Preference preference) {
        if (targetCategory != null) {
            targetCategory.addPreference(preference);
            return false;
        }

        return super.addPreference(preference);
    }
}
