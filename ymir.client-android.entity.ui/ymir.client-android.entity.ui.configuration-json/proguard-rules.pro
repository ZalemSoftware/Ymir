#
# Configurações necessárias por causa da estrutura de JSON (Jackson).
#
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
    public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *;
}

# Mantém as classes que representam as configurações em arquivos JSON.
-keep public class br.com.zalem.ymir.client.android.entity.ui.configuration.view.layout.** { *;}
-keep public class br.com.zalem.ymir.client.android.entity.ui.configuration.json.** {
    public <init>(...);
    public void set*(***);
    public *** get*();
}