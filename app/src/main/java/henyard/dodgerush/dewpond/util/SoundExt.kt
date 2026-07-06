package henyard.dodgerush.dewpond.util

import android.view.View

/**
 * Sets a click listener that first plays the shared UI click effect, then runs
 * [action]. Use in place of [View.setOnClickListener] for buttons so every tap
 * gets consistent audio feedback (respects the SFX toggle via [SoundManager]).
 */
fun View.setClickSound(action: (View) -> Unit) {
    setOnClickListener {
        SoundManager.playSfx(context, SoundManager.Sfx.CLICK)
        action(it)
    }
}

/**
 * Like [setClickSound] but plays the distinct back/close effect. Use on "back"
 * and "close" buttons so navigating backwards sounds different from a forward tap.
 */
fun View.setBackSound(action: (View) -> Unit) {
    setOnClickListener {
        SoundManager.playSfx(context, SoundManager.Sfx.BACK)
        action(it)
    }
}
