package henyard.dodgerush.dewpond.ui.game

import android.animation.ArgbEvaluator
import android.graphics.drawable.GradientDrawable
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

/**
 * Gameplay screen (portrait). Hosts the [GameView] and the HUD/overlays.
 */
class GameFragment : BaseFragment(R.layout.fragment_game), GameView.Listener {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var heartViews: List<ImageView>
    private var level = 1

    private val argbEval = ArgbEvaluator()
    private var totalSeconds = 1
    private var timerBg: GradientDrawable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGameBinding.bind(view)

        level = Levels.clamp(arguments?.getInt(LevelSelectFragment.ARG_LEVEL, 1) ?: 1)

        heartViews = listOf(binding.heart1, binding.heart2, binding.heart3)

        totalSeconds = (Levels.durationMs(level) / 1000).toInt().coerceAtLeast(1)
        timerBg = binding.timerText.background?.mutate() as? GradientDrawable

        val skin = ShopCatalog.skinById(AppPrefs(requireContext()).equippedSkin)
        binding.gameView.level = level
        binding.gameView.tuning = Levels.tuningFor(level)
        binding.gameView.levelDurationMs = Levels.durationMs(level)
        binding.gameView.heroDrawableRes = skin.previewRes
        binding.gameView.skinAbility = skin.ability
        binding.gameView.listener = this

        binding.btnPause.setOnClickListener { showPause() }
        binding.btnResume.root.setOnClickListener { hidePause() }
        binding.btnPauseReplay.root.setOnClickListener { retry() }
        binding.btnPauseMenu.root.setOnClickListener { toMenu() }
        binding.btnNext.root.setOnClickListener { goToLevel(level + 1) }
        binding.btnRetry.root.setOnClickListener { retry() }
        binding.btnResultMenu.root.setOnClickListener { toMenu() }

        binding.btnResume.label.text = getString(R.string.pause_continue)
        binding.btnPauseReplay.label.text = getString(R.string.replay)
        binding.btnPauseMenu.label.text = getString(R.string.home)
        // Shrink the overlay button labels so they fit, without touching the
        // shared yellow/blue button default used by Play and other screens.
        listOf(
            binding.btnResume.label, binding.btnPauseReplay.label, binding.btnPauseMenu.label,
            binding.btnNext.label, binding.btnRetry.label, binding.btnResultMenu.label,
        ).forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) }
        binding.btnNext.label.text = getString(R.string.next)
        binding.btnRetry.label.text = getString(R.string.replay)
        binding.btnResultMenu.label.text = getString(R.string.home)

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
        timerBg?.setColor(timerColor(fraction))
    }

    /** Timer fill fades green → yellow → red as the remaining [fraction] drops. */
    private fun timerColor(fraction: Float): Int {
        val green = 0xFF4CAF50.toInt()
        val yellow = 0xFFF5B301.toInt()
        val red = 0xFFE53935.toInt()
        return if (fraction >= 0.5f) {
            argbEval.evaluate((1f - fraction) / 0.5f, green, yellow) as Int
        } else {
            argbEval.evaluate((0.5f - fraction) / 0.5f, yellow, red) as Int
        }
    }

    override fun onGameOver(result: GameResult) {
        val prefs = AppPrefs(requireContext())
        val levelReward = if (result.won) Levels.coinReward(level) else 0
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
            untouchableWin = result.won && result.hits == 0,
        )
        if (result.won) prefs.recordLevelWin(level, result.stars)

        binding.resultTitle.setText(if (result.won) R.string.you_win else R.string.game_over)

        val starViews = listOf(binding.star1, binding.star2, binding.star3)
        starViews.forEachIndexed { i, iv ->
            iv.setImageResource(
                if (i < result.stars) R.drawable.star_active else R.drawable.star_inactive
            )
        }
        binding.resultStars.visibility = if (result.won) View.VISIBLE else View.GONE

        // Win → result table; Lose → dazed chicken illustration.
        binding.winStats.visibility = if (result.won) View.VISIBLE else View.GONE
        binding.gameOverChicken.visibility = if (result.won) View.GONE else View.VISIBLE
        binding.resultReward.text = getString(R.string.result_reward, coinsEarned)
        binding.resultNoHit.text = formatTime(result.longestNoHitSeconds)
        binding.resultGrain.text = result.grain.toString()

        // Win → NEXT (until the last level) + HOME; Lose → REPLAY + HOME.
        val hasNext = result.won && level < Levels.TOTAL
        binding.btnNext.root.visibility = if (hasNext) View.VISIBLE else View.GONE
        binding.btnRetry.root.visibility = if (result.won) View.GONE else View.VISIBLE

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
}
