# ViewModel (created via ViewModelProvider.Factory reflection)
-keep class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Fragment (FragmentManager recreates by class name after rotation)
-keep class * extends androidx.fragment.app.Fragment { *; }

# Enum (values/valueOf by name, e.g. when serialized to Prefs)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Kotlin data class component functions (destructuring)
-keepclassmembers class * { public ** component*(); }

# Source/line info for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Custom Views inflated from XML layouts (LayoutInflater resolves by exact name)
-keep class henyard.dodgerush.dewpond.game.GameView { *; }
-keep class henyard.dodgerush.dewpond.widget.OutlineTextView { *; }