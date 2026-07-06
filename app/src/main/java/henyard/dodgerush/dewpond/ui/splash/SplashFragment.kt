package henyard.dodgerush.dewpond.ui.splash

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.navigation.fragment.findNavController
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.ui.base.ReinflatingFragment
import henyard.dodgerush.dewpond.util.NetworkUtils

/**
 * Loading screen. Free orientation (enforced centrally by MainActivity), so it
 * swaps between its portrait and landscape layouts on rotation via
 * [ReinflatingFragment]. Shows the wooden progress bar filling up
 * (loader_clear + clipped loader_fill).
 */
class SplashFragment : ReinflatingFragment() {

    override val layoutRes = R.layout.fragment_splash

    private var navigated = false
    private var animator: ValueAnimator? = null
    private var currentLevel = 0
    private var loaderFill: Drawable? = null

    override fun onBindContent(content: View) {
        val bar = content.findViewById<FrameLayout>(R.id.loaderBar)
        loaderFill = (bar.background as? LayerDrawable)
            ?.findDrawableByLayerId(R.id.timer_fill)
        loaderFill?.level = currentLevel
        bar.invalidate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start the fill animation + navigation once. The activity handles config
        // changes itself, so rotation re-inflates the layout (onBindContent) but
        // does NOT recreate the fragment — the animator keeps running.
        if (animator != null) return

        animator = ValueAnimator.ofInt(0, 10_000).apply {
            duration = 2200L
            interpolator = DecelerateInterpolator()
            addUpdateListener { a ->
                currentLevel = a.animatedValue as Int
                loaderFill?.level = currentLevel
                view.findViewById<View>(R.id.loaderBar)?.invalidate()
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
        loaderFill = null
    }
}