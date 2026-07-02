package henyard.dodgerush.dewpond.util

import android.content.Context

/** Lightweight persisted app flags backed by SharedPreferences. */
class AppPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(NAME, Context.MODE_PRIVATE)

    /** Whether the how-to-play onboarding has already been shown. */
    var onboardingSeen: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_SEEN, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_SEEN, value).apply()

    private companion object {
        const val NAME = "henyard_prefs"
        const val KEY_ONBOARDING_SEEN = "onboarding_seen"
    }
}
