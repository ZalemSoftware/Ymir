#
# Configurações necessárias por causa da estrutura de JSON (Jackson).
#
-keepattributes *Annotation*,EnclosingMethod,Signature
-keepnames class com.fasterxml.jackson.** { *; }
-keep class org.codehaus.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
    public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *;
}

# Mantém as classes que representam os arquivos JSON utilizados nas configurações dos metadados.
-keep public class br.com.zalem.ymir.client.android.entity.data.openmobster.metadata.** {
    public <init>(...);
    public void set*(***);
    public *** get*();
}

# Mantém as classes que representam os arquivos JSON de serialização dos dados e backups.
-keep class br.com.zalem.ymir.client.android.entity.data.openmobster.util.MobileBeanJsonSerializer$MobileBeanChannelData { *; }
-keep class br.com.zalem.ymir.client.android.entity.data.openmobster.util.MobileBeanJsonSerializer$MobileBeanChannelDataInfo { *; }
-keep class br.com.zalem.ymir.client.android.entity.data.openmobster.util.EntityDataBackupHandler$MobileBeanBackupHeader { *; }