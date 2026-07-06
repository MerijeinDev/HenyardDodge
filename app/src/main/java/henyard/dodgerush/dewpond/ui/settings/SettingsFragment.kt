package henyard.dodgerush.dewpond.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentSettingsBinding
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.AppPrefs
import henyard.dodgerush.dewpond.util.SoundManager
import henyard.dodgerush.dewpond.util.setBackSound
import henyard.dodgerush.dewpond.util.setClickSound
import henyard.dodgerush.dewpond.widget.OutlineTextView

/**
 * Settings screen: toggles for background music and sound effects, persisted in
 * [AppPrefs] and applied live via [SoundManager].
 */
class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private lateinit var prefs: AppPrefs

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingsBinding.bind(view)
        prefs = AppPrefs(requireContext())

        binding.btnBack.setBackSound { findNavController().popBackStack() }

        renderToggle(binding.toggleMusic, binding.toggleMusicLabel, prefs.musicEnabled)
        renderToggle(binding.toggleSound, binding.toggleSoundLabel, prefs.sfxEnabled)

        binding.toggleMusic.setClickSound {
            prefs.musicEnabled = !prefs.musicEnabled
            renderToggle(binding.toggleMusic, binding.toggleMusicLabel, prefs.musicEnabled)
            SoundManager.applyMusicSetting(requireContext())
        }
        binding.toggleSound.setOnClickListener {
            prefs.sfxEnabled = !prefs.sfxEnabled
            renderToggle(binding.toggleSound, binding.toggleSoundLabel, prefs.sfxEnabled)
            SoundManager.playSfx(requireContext(), SoundManager.Sfx.CLICK)
        }
    }

    private fun renderToggle(container: FrameLayout, label: OutlineTextView, on: Boolean) {
        container.setBackgroundResource(
            if (on) R.drawable.settings_toggle_on else R.drawable.settings_toggle_off
        )
        label.setText(if (on) R.string.settings_on else R.string.settings_off)
    }
}
