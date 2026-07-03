package henyard.dodgerush.dewpond.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * Lightweight audio hub for the game. It is intentionally asset-agnostic:
 * sound effects and the background track are resolved from `res/raw` by name at
 * runtime, so the app compiles and runs with or without the audio files present.
 * Drop the matching `res/raw/<name>.(ogg|wav|mp3)` files in to make it audible
 * (see each [Sfx] entry and [MUSIC_RES] for the expected file names); playback
 * always respects the user's music/SFX toggles in [AppPrefs].
 *
 * Usage: call [init] once (e.g. from the Application/first screen), then
 * [playSfx] / [startMusic] / [stopMusic]. Call [release] when tearing down.
 */
object SoundManager {

    /** Named sound effects the game may trigger (asset = `res/raw/<resName>`). */
    enum class Sfx(val resName: String) {
        CLICK("click"),        // UI button tap
        COIN("coin"),          // coin pickup
        GRAIN("grain"),        // grain pickup ("чмок")
        BONUS("bonus"),        // power-up pickup ("ding")
        HIT("hit"),            // chicken takes a hit
        SHIELD("shield"),      // shield absorbs a hit ("clink")
        HAY_THUD("thud"),      // hay bale lands ("thud")
        BARREL("barrel"),      // barrel rolls in
        FOX_DASH("fox_dash"),  // fox dash from the edge
        SUCCESS("success"),    // level survived
        LOSE("lose"),          // out of lives
        ERROR("error"),        // action refused (e.g. not enough coins)
    }

    /** Background track for both the menu and gameplay (`res/raw/bg_music`). */
    private const val MUSIC_RES = "bg_music"

    private var soundPool: SoundPool? = null
    private val loaded = mutableMapOf<Sfx, Int>()
    private var music: MediaPlayer? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build()
        soundPool = pool
        val app = context.applicationContext
        for (sfx in Sfx.values()) {
            val resId = rawId(app, sfx.resName)
            if (resId != 0) loaded[sfx] = pool.load(app, resId, 1)
        }
    }

    /** Plays a one-shot effect if SFX are enabled and the asset exists. */
    fun playSfx(context: Context, sfx: Sfx) {
        if (!AppPrefs(context).sfxEnabled) return
        val pool = soundPool ?: return
        val id = loaded[sfx] ?: return
        pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    /** Starts/loops the background track if music is enabled and present. */
    fun startMusic(context: Context) {
        if (!AppPrefs(context).musicEnabled) return
        if (music != null) {
            music?.takeIf { !it.isPlaying }?.start()
            return
        }
        val app = context.applicationContext
        val resId = rawId(app, MUSIC_RES)
        if (resId == 0) return
        music = MediaPlayer.create(app, resId)?.apply {
            isLooping = true
            start()
        }
    }

    fun stopMusic() {
        music?.let { if (it.isPlaying) it.pause() }
    }

    /** Reflects a change to the music toggle immediately. */
    fun applyMusicSetting(context: Context) {
        if (AppPrefs(context).musicEnabled) startMusic(context) else stopMusic()
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded.clear()
        music?.release()
        music = null
        initialized = false
    }

    private fun rawId(context: Context, name: String): Int =
        context.resources.getIdentifier(name, "raw", context.packageName)
}
