package henyard.dodgerush.dewpond.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

/**
 * Lightweight audio hub for the game. Sound effects and background tracks are
 * resolved from `res/raw` by name at runtime (see each [Sfx] entry and
 * [MusicTrack]). Playback respects the user's music/SFX toggles in [AppPrefs].
 *
 * Call [init] once at app start (e.g. [MainActivity.onCreate]); [playSfx] will
 * lazily init if needed. Call [release] when tearing down.
 */
object SoundManager {

    /** Named sound effects the game may trigger (asset = `res/raw/<resName>`). */
    enum class Sfx(val resName: String) {
        CLICK("click"),        // UI button tap
        BACK("button_back"),   // back / close button
        NOTIFY("notify"),      // popup / overlay appears
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

    /** Background loops: `music_menu` for hub screens, `music_game` in gameplay. */
    enum class MusicTrack(val resName: String) {
        MENU("music_menu"),
        GAME("music_game"),
    }

    private var soundPool: SoundPool? = null
    private val loaded = mutableMapOf<Sfx, Int>()
    private var music: MediaPlayer? = null
    private var activeTrack: MusicTrack? = null
    private var initialized = false

    // Cached once at init: reads stay live (SharedPreferences-backed) while
    // avoiding a fresh AppPrefs allocation on every playSfx, which sits on the
    // per-collision game-loop path.
    private var prefs: AppPrefs? = null

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        prefs = AppPrefs(context.applicationContext)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attrs).build()
        soundPool = pool
        val app = context.applicationContext
        for (sfx in Sfx.entries) {
            val resId = rawId(app, sfx.resName)
            if (resId != 0) loaded[sfx] = pool.load(app, resId, 1)
        }
    }

    /** Plays a one-shot effect if SFX are enabled and the asset is loaded. */
    fun playSfx(context: Context, sfx: Sfx) {
        ensureInit(context)
        if (prefs?.sfxEnabled != true) return
        val pool = soundPool ?: return
        val sampleId = loaded[sfx] ?: return
        pool.play(sampleId, 1f, 1f, 1, 0, 1f)
    }

    fun startMenuMusic(context: Context) = startMusic(context, MusicTrack.MENU)

    fun startGameMusic(context: Context) = startMusic(context, MusicTrack.GAME)

    /** Starts/loops a background track if music is enabled and the asset exists. */
    fun startMusic(context: Context, track: MusicTrack = MusicTrack.MENU) {
        ensureInit(context)
        if (prefs?.musicEnabled != true) return
        if (activeTrack == track) {
            music?.takeIf { !it.isPlaying }?.start()
            return
        }
        stopMusic(releasePlayer = true)
        activeTrack = track
        val app = context.applicationContext
        val resId = rawId(app, track.resName)
        if (resId == 0) return
        music = MediaPlayer.create(app, resId)?.apply {
            isLooping = true
            start()
        }
    }

    fun pauseMusic() {
        music?.takeIf { it.isPlaying }?.pause()
    }

    /** Resumes the last background track after returning from background. */
    fun resumeMusic(context: Context) {
        ensureInit(context)
        if (prefs?.musicEnabled != true) return
        val track = activeTrack ?: return
        if (music != null) {
            music?.takeIf { !it.isPlaying }?.start()
        } else {
            startMusic(context, track)
        }
    }

    fun stopMusic(releasePlayer: Boolean = false) {
        music?.let { player ->
            if (player.isPlaying) player.pause()
            if (releasePlayer) {
                player.release()
                music = null
                activeTrack = null
            }
        } ?: run {
            if (releasePlayer) activeTrack = null
        }
    }

    /** Reflects a change to the music toggle immediately. */
    fun applyMusicSetting(context: Context) {
        ensureInit(context)
        if (prefs?.musicEnabled != true) {
            stopMusic(releasePlayer = true)
            return
        }
        startMusic(context, activeTrack ?: MusicTrack.MENU)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        loaded.clear()
        stopMusic(releasePlayer = true)
        prefs = null
        initialized = false
    }

    private fun ensureInit(context: Context) {
        if (!initialized) init(context.applicationContext)
    }

    private fun rawId(context: Context, name: String): Int =
        context.resources.getIdentifier(name, "raw", context.packageName)
}
