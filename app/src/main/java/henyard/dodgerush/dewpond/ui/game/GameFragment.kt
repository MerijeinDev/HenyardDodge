package henyard.dodgerush.dewpond.ui.game

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentGameBinding
import henyard.dodgerush.dewpond.game.GameView
import henyard.dodgerush.dewpond.game.HenCatalog
import henyard.dodgerush.dewpond.game.Levels
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGameBinding.bind(view)

        level = Levels.clamp(arguments?.getInt(LevelSelectFragment.ARG_LEVEL, 1) ?: 1)

        heartViews = listOf(binding.heart1, binding.heart2, binding.heart3)

        binding.gameView.level = level
        binding.gameView.levelDurationMs = Levels.durationMs(level)
        binding.gameView.heroDrawableRes =
            HenCatalog.spriteFor(AppPrefs(requireContext()).selectedHenIndex)
        binding.gameView.listener = this

        binding.btnPause.setOnClickListener { showPause() }
        binding.btnResume.root.setOnClickListener { hidePause() }
        binding.btnPauseMenu.root.setOnClickListener { toMenu() }
        binding.btnRetry.root.setOnClickListener { retry() }
        binding.btnResultMenu.root.setOnClickListener { toMenu() }

        binding.btnResume.label.text = getString(R.string.resume)
        binding.btnPauseMenu.label.text = getString(R.string.menu)
        binding.btnRetry.label.text = getString(R.string.retry)
        binding.btnResultMenu.label.text = getString(R.string.menu)

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

    private fun retry() {
        findNavController().navigate(
            R.id.action_game_retry,
            bundleOf(LevelSelectFragment.ARG_LEVEL to level)
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
    }

    override fun onGameOver(score: Int, coins: Int, won: Boolean, stars: Int) {
        val prefs = AppPrefs(requireContext())
        if (coins > 0) prefs.coins += coins
        if (won) prefs.recordLevelWin(level, stars)
        binding.resultTitle.setText(if (won) R.string.you_win else R.string.game_over)
        binding.resultScore.text = getString(R.string.score_label) + "  " + score
        val starViews = listOf(binding.star1, binding.star2, binding.star3)
        starViews.forEachIndexed { i, iv ->
            iv.setImageResource(
                if (i < stars) R.drawable.star_active else R.drawable.star_inactive
            )
        }
        binding.resultStars.visibility = if (won) View.VISIBLE else View.GONE
        binding.resultOverlay.visibility = View.VISIBLE
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
