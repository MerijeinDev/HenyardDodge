package henyard.dodgerush.dewpond.ui.hen

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentChooseYourHenBinding
import henyard.dodgerush.dewpond.databinding.ItemHenCellBinding
import henyard.dodgerush.dewpond.game.HenCatalog
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.setBackSound
import henyard.dodgerush.dewpond.util.setClickSound

/**
 * Lets the player pick one of the hens and set a nickname, then confirm. The
 * choice is persisted via [AppPrefs] and used as the player sprite in the game.
 */
class ChooseYourHenFragment : BaseFragment(R.layout.fragment_choose_your_hen) {

    private lateinit var prefs: AppPrefs
    private lateinit var cells: List<ItemHenCellBinding>
    private var selectedIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChooseYourHenBinding.bind(view)
        prefs = AppPrefs(requireContext())

        cells = listOf(
            binding.hen0, binding.hen1, binding.hen2,
            binding.hen3, binding.hen4, binding.hen5,
        )
        cells.forEachIndexed { index, cell ->
            cell.henImage.setImageResource(HenCatalog.hens[index].thumbnailRes)
            cell.root.setClickSound { select(index) }
        }

        select(HenCatalog.clampIndex(prefs.selectedHenIndex))

        prefs.nickname.takeIf { it.isNotBlank() }?.let { binding.nicknameInput.setText(it) }

        binding.btnConfirm.label.text = getString(R.string.confirm)
        binding.btnConfirm.root.setClickSound { confirm(binding) }
        binding.btnBack.setBackSound { findNavController().popBackStack() }
    }

    private fun select(index: Int) {
        selectedIndex = index
        cells.forEachIndexed { i, cell ->
            cell.henCheck.visibility = if (i == index) View.VISIBLE else View.GONE
        }
    }

    private fun confirm(binding: FragmentChooseYourHenBinding) {
        prefs.selectedHenIndex = selectedIndex
        prefs.nickname = binding.nicknameInput.text.toString().trim()
            .ifBlank { getString(R.string.default_nickname) }
        findNavController().navigate(R.id.action_choose_hen_to_menu)
    }
}
