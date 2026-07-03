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

    /** Index into [henyard.dodgerush.dewpond.game.HenCatalog.hens] of the chosen hen. */
    var selectedHenIndex: Int
        get() = prefs.getInt(KEY_HEN_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_HEN_INDEX, value).apply()

    /** Player nickname (empty until the player sets one). */
    var nickname: String
        get() = prefs.getString(KEY_NICKNAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_NICKNAME, value).apply()

    private companion object {
        const val NAME = "henyard_prefs"
        const val KEY_ONBOARDING_SEEN = "onboarding_seen"
        const val KEY_HEN_INDEX = "hen_index"
        const val KEY_NICKNAME = "nickname"
    }
}
