# Mantém as perspectivas pois elas podem referenciadas em xmls.
-keep public class * extends br.com.zalem.ymir.client.android.perspective.Perspective


#
# Configurações para as injeções de dependências (RoboGuice).
#
-dontwarn com.actionbarsherlock.**
-dontwarn javax.annotation.**
-dontwarn roboguice.**.*Sherlock*
-dontwarn roboguice.activity.RoboMapActivity

-libraryjars <java.home>/lib/rt.jar

-keep public class roboguice.** { *; }
-keep public class com.google.inject.** { *; }
-keepattributes *Annotation*,Signature

# Mantém os bancos de anotações.
-keep public class **.AnnotationDatabaseImpl

# Mantém os modulos e seus construtores.
-keep public class * extends com.google.inject.AbstractModule {
    public <init>();
    public <init>(android.app.Application);
}

# Mantém qualquer classe que contenha um construtor, atributo ou método com injeção, inclusive os próprios membros.
-keepclasseswithmembers class * {
    @com.google.inject.Inject <init>(...);
}
-keepclasseswithmembers class * {
    @com.google.inject.Inject <fields>;
}
-keepclasseswithmembers class * {
    @com.google.inject.Inject <methods>;
}

