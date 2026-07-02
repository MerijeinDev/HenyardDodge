package henyard.dodgerush.dewpond.ui.splash

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.databinding.FragmentSplashBinding
import henyard.dodgerush.dewpond.ui.base.BaseFragment
import henyard.dodgerush.dewpond.util.NetworkUtils

/**
 * Loading screen. Shown in landscape (orientation is enforced centrally by
 * MainActivity for this destination). Shows the wooden progress bar filling up
 * (loader_clear + clipped loader_done).
 */
class SplashFragment : BaseFragment(R.layout.fragment_splash) {

    private var navigated = false
    private var animator: ValueAnimator? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSplashBinding.bind(view)

        binding.loaderFill.setImageLevel(0)

        animator = ValueAnimator.ofInt(0, 10_000).apply {
            duration = 2200L
            interpolator = DecelerateInterpolator()
            addUpdateListener { a ->
                val level = a.animatedValue as Int
                binding.loaderFill.setImageLevel(level)
            }
            start()
        }

        view.postDelayed({ goToNext() }, 2400L)
    }

    private fun goToNext() {
        if (navigated || !isAdded) return
        navigated = true
        if (NetworkUtils.isOnline(requireContext())) {
            findNavController().navigate(R.id.action_splash_to_notifications)
        } else {
            findNavController().navigate(R.id.action_splash_to_no_internet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animator?.cancel()
        animator = null
    }
}
