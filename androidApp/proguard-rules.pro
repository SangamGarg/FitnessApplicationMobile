# Add project specific ProGuard / R8 rules here.
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep kotlinx.serialization annotated DTO classes
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class *$$serializer {
    *;
}
-keepclassmembers class * {
    @kotlinx.serialization.SerialName *;
}
