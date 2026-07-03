package henyard.dodgerush.dewpond.ui.level

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentLevelSelectBinding
import henyard.dodgerush.dewpond.databinding.ItemLevelBinding
import henyard.dodgerush.dewpond.game.Levels
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.SoundManager
import henyard.dodgerush.dewpond.util.setClickSound

/**
 * Grid of levels on the brown board. Levels up to [AppPrefs.unlockedLevel] are
 * playable (green + earned stars); the rest show a padlock. Tapping an unlocked
 * level starts the game at that level.
 */
class LevelSelectFragment : BaseFragment(R.layout.fragment_level_select) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentLevelSelectBinding.bind(view)
        val prefs = AppPrefs(requireContext())

        binding.btnBack.setClickSound { findNavController().popBackStack() }

        binding.levelGrid.layoutManager = GridLayoutManager(requireContext(), COLUMNS)
        binding.levelGrid.adapter = LevelAdapter(
            unlockedLevel = prefs.unlockedLevel,
            starsOf = prefs::starsForLevel,
            onLevelClick = ::startLevel,
        )
    }

    private fun startLevel(level: Int) {
        SoundManager.playSfx(requireContext(), SoundManager.Sfx.CLICK)
        findNavController().navigate(R.id.action_level_to_game, bundleOf(ARG_LEVEL to level))
    }

    companion object {
        const val ARG_LEVEL = "level"
        private const val COLUMNS = 4
    }

    private class LevelAdapter(
        private val unlockedLevel: Int,
        private val starsOf: (Int) -> Int,
        private val onLevelClick: (Int) -> Unit,
    ) : RecyclerView.Adapter<LevelAdapter.LevelHolder>() {

        override fun getItemCount() = Levels.TOTAL

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelHolder {
            val itemBinding = ItemLevelBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return LevelHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: LevelHolder, position: Int) {
            val level = position + 1
            val unlocked = level <= unlockedLevel
            holder.bind(level, unlocked, if (unlocked) starsOf(level) else 0, onLevelClick)
        }

        private class LevelHolder(
            private val binding: ItemLevelBinding,
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(level: Int, unlocked: Boolean, stars: Int, onClick: (Int) -> Unit) {
                if (!unlocked) {
                    binding.tileBg.setImageResource(R.drawable.level_plate_lock)
                    binding.levelNumber.visibility = View.GONE
                    binding.starRow.visibility = View.GONE
                    binding.tileContainer.isClickable = false
                    binding.tileContainer.setOnClickListener(null)
                    binding.tileContainer.contentDescription =
                        binding.root.context.getString(R.string.cd_locked_level)
                    return
                }

                binding.tileBg.setImageResource(R.drawable.level_plate)
                binding.levelNumber.visibility = View.VISIBLE
                binding.levelNumber.text = level.toString()
                binding.tileContainer.contentDescription = level.toString()

                if (stars > 0) {
                    binding.starRow.visibility = View.VISIBLE
                    val starViews = listOf(binding.star1, binding.star2, binding.star3)
                    starViews.forEachIndexed { i, iv ->
                        iv.setImageResource(
                            if (i < stars) R.drawable.star_active else R.drawable.star_inactive
                        )
                    }
                } else {
                    binding.starRow.visibility = View.GONE
                }

                binding.tileContainer.isClickable = true
                binding.tileContainer.setOnClickListener { onClick(level) }
            }
        }
    }
}
