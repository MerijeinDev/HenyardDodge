package henyard.dodgerush.dewpond.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentProfileBinding
import henyard.dodgerush.dewpond.databinding.ViewProfileRowBinding
import henyard.dodgerush.dewpond.game.AchievementCatalog
import henyard.dodgerush.dewpond.game.Levels
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.setBackSound
import henyard.dodgerush.dewpond.util.setClickSound

/** Profile screen: player name/avatar plus overall stats and best records,
 * all read from [AppPrefs]. The name can be changed via the EDIT button. */
class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    private lateinit var prefs: AppPrefs
    private lateinit var binding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        prefs = AppPrefs(requireContext())

        binding.btnBack.setBackSound { findNavController().popBackStack() }
        binding.btnEdit.setClickSound { showEditNameDialog() }

        binding.sectionOverall.sectionLabel.setText(R.string.profile_overall_stats)
        binding.sectionRecords.sectionLabel.setText(R.string.profile_best_records)

        bindStats()
    }

    private fun bindStats() {
        val completed = AchievementCatalog.items.count { it.isComplete(prefs) }

        binding.playerName.text = playerName()
        row(binding.rowTotalScore, R.string.profile_total_score, "%,d".format(prefs.totalScore))
        row(binding.rowLevelReached, R.string.profile_level_reached,
            prefs.unlockedLevel.coerceAtMost(Levels.TOTAL).toString())
        row(binding.rowAchievements, R.string.profile_achievements,
            "$completed/${AchievementCatalog.items.size}")
        row(binding.rowCoins, R.string.profile_coins, "%,d".format(prefs.coins))
        row(binding.rowBestRun, R.string.profile_best_run_score, "%,d".format(prefs.bestRunScore))
        row(binding.rowNoHit, R.string.profile_longest_no_hit, formatTime(prefs.longestNoHitSeconds))
    }

    private fun row(row: ViewProfileRowBinding, labelRes: Int, value: String) {
        row.statLabel.setText(labelRes)
        row.statValue.text = value
    }

    private fun playerName(): String =
        prefs.nickname.ifBlank { getString(R.string.profile_default_name) }

    private fun formatTime(seconds: Int): String =
        "%02d:%02d".format(seconds / 60, seconds % 60)

    private fun showEditNameDialog() {
        val input = EditText(requireContext()).apply {
            setText(prefs.nickname)
            hint = getString(R.string.profile_edit_hint)
            setSingleLine()
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.profile_edit_title)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                prefs.nickname = input.text.toString().trim()
                binding.playerName.text = playerName()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
