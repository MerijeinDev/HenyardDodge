package henyard.dodgerush.dewpond.ui.game

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentGameBinding
import henyard.dodgerush.dewpond.game.GameResult
import henyard.dodgerush.dewpond.game.GameView
import henyard.dodgerush.dewpond.game.Levels
import henyard.dodgerush.dewpond.game.ShopCatalog
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.ui.level.LevelSelectFragment
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.SoundManager
import henyard.dodgerush.dewpond.util.setClickSound

/**
 * Gameplay screen (portrait). Hosts the [GameView] and the HUD/overlays.
 */
class GameFragment : BaseFragment(R.layout.fragment_game), GameView.Listener {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var heartViews: List<ImageView>
    private var level = 1

    private var totalSeconds = 1
    private var timerFill: Drawable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGameBinding.bind(view)

        level = Levels.clamp(arguments?.getInt(LevelSelectFragment.ARG_LEVEL, 1) ?: 1)

        heartViews = listOf(binding.heart1, binding.heart2, binding.heart3)

        totalSeconds = (Levels.durationMs(level) / 1000).toInt().coerceAtLeast(1)
        timerFill = (binding.timerText.background as? LayerDrawable)
            ?.findDrawableByLayerId(R.id.timer_fill)
        timerFill?.level = MAX_LEVEL


        // Ensure SFX are loaded even if the game is entered without the menu;
        // the shared background track keeps playing across screens.
        SoundManager.init(requireContext())

        val skin = ShopCatalog.skinById(AppPrefs(requireContext()).equippedSkin)
        binding.gameView.level = level
        binding.gameView.tuning = Levels.tuningFor(level)
        binding.gameView.levelDurationMs = Levels.durationMs(level)
        binding.gameView.heroDrawableRes = skin.previewRes
        binding.gameView.skinAbility = skin.ability
        binding.gameView.listener = this

        binding.btnPause.setClickSound { showPause() }
        binding.btnResume.root.setClickSound { hidePause() }
        binding.btnPauseReplay.root.setClickSound { retry() }
        binding.btnPauseMenu.root.setClickSound { toMenu() }
        binding.btnNext.root.setClickSound { goToLevel(level + 1) }
        binding.btnRetry.root.setClickSound { retry() }
        binding.btnResultMenu.root.setClickSound { toMenu() }

        binding.btnResume.label.text = getString(R.string.pause_continue)
        binding.btnPauseReplay.label.text = getString(R.string.replay)
        binding.btnPauseMenu.label.text = getString(R.string.home)
        binding.btnNext.label.text = getString(R.string.next)
        binding.btnRetry.label.text = getString(R.string.replay)
        binding.btnResultMenu.label.text = getString(R.string.home)
        // Figma sizes: short labels ~24sp; the longer CONTINUE drops to 20sp to
        // fit. Set here so the shared Play button default stays untouched.
        listOf(
            binding.btnPauseReplay.label, binding.btnPauseMenu.label,
            binding.btnNext.label, binding.btnRetry.label, binding.btnResultMenu.label,
        ).forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f) }
        binding.btnResume.label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        binding.resultOverlay.isShown -> toMenu()
                        binding.pauseOverlay.isShown -> hidePause()
                        else -> showPause()
                    }
                }
            })
    }

    private fun showPause() {
        if (binding.resultOverlay.isShown) return
        binding.gameView.pauseGame()
        binding.pauseOverlay.visibility = View.VISIBLE
    }

    private fun hidePause() {
        binding.pauseOverlay.visibility = View.GONE
        binding.gameView.resumeGame()
    }

    private fun toMenu() {
        findNavController().popBackStack(R.id.mainMenuFragment, false)
    }

    private fun retry() = goToLevel(level)

    private fun goToLevel(target: Int) {
        findNavController().navigate(
            R.id.action_game_retry,
            bundleOf(LevelSelectFragment.ARG_LEVEL to Levels.clamp(target))
        )
    }

    // ---- GameView.Listener ----
    override fun onScoreChanged(score: Int) {
        binding.scoreText.text = score.toString()
    }

    override fun onLivesChanged(lives: Int) {
        heartViews.forEachIndexed { i, iv ->
            iv.setImageResource(
                if (i < lives) R.drawable.heart_active else R.drawable.heart_inactive
            )
        }
    }

    override fun onTimeChanged(secondsLeft: Int) {
        binding.timerText.text = "%02d:%02d".format(secondsLeft / 60, secondsLeft % 60)
        val fraction = (secondsLeft.toFloat() / totalSeconds).coerceIn(0f, 1f)
        timerFill?.level = (fraction * MAX_LEVEL).toInt()
        binding.timerText.invalidate()
    }

    override fun onGameOver(result: GameResult) {
        SoundManager.playSfx(
            requireContext(),
            if (result.cleared) SoundManager.Sfx.SUCCESS else SoundManager.Sfx.LOSE,
        )
        val prefs = AppPrefs(requireContext())
        val levelReward = if (result.cleared) Levels.coinReward(level) else 0
        val coinsEarned = result.coinsCollected + levelReward

        if (coinsEarned > 0) prefs.coins += coinsEarned
        prefs.recordRunScore(result.score)
        prefs.recordRunStats(
            grain = result.grain,
            coinsCollected = result.coinsCollected,
            hazardsDodged = result.hazardsDodged,
            foxDodged = result.foxDodged,
            shieldBlocks = result.shieldBlocks,
            survivedSeconds = result.survivedSeconds,
            longestNoHitSeconds = result.longestNoHitSeconds,
            flawlessClear = result.cleared && result.hits == 0,
        )
        if (result.cleared) prefs.recordLevelCleared(level, result.stars)

        binding.resultTitle.setText(if (result.cleared) R.string.you_cleared else R.string.game_over)

        val starViews = listOf(binding.star1, binding.star2, binding.star3)
        starViews.forEachIndexed { i, iv ->
            iv.setImageResource(
                if (i < result.stars) R.drawable.star_active else R.drawable.star_inactive
            )
        }
        binding.resultStars.visibility = if (result.cleared) View.VISIBLE else View.GONE

        // Cleared → result table; Lose → dazed chicken illustration.
        binding.clearStats.visibility = if (result.cleared) View.VISIBLE else View.GONE
        binding.gameOverChicken.visibility = if (result.cleared) View.GONE else View.VISIBLE
        binding.resultReward.text = getString(R.string.result_reward, coinsEarned)
        binding.resultNoHit.text = formatTime(result.longestNoHitSeconds)
        binding.resultGrain.text = result.grain.toString()

        // Cleared → NEXT (until the last level) + HOME; Lose → REPLAY + HOME.
        val hasNext = result.cleared && level < Levels.TOTAL
        binding.btnNext.root.visibility = if (hasNext) View.VISIBLE else View.GONE
        binding.btnRetry.root.visibility = if (result.cleared) View.GONE else View.VISIBLE

        binding.resultOverlay.visibility = View.VISIBLE
    }

    private fun formatTime(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        return "%d:%02d".format(s / 60, s % 60)
    }

    override fun onPause() {
        super.onPause()
        if (!binding.resultOverlay.isShown) showPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        /** ClipDrawable levels run 0..10000 (fully clipped .. fully shown). */
        const val MAX_LEVEL = 10_000
    }
}
