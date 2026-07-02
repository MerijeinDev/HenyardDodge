package henyard.dodgerush.dewpond.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * Base for free-orientation screens that must swap between their `layout/` and
 * `layout-land/` variants on rotation.
 *
 * The activity handles configuration changes itself (see manifest configChanges),
 * so fragments are NOT recreated on rotation. To still pick up the correct
 * orientation-specific layout, we host the content inside a [FrameLayout] and
 * re-inflate [layoutRes] whenever the configuration changes.
 */
abstract class ReinflatingFragment : Fragment() {

    @get:LayoutRes
    protected abstract val layoutRes: Int

    private var host: FrameLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val fl = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }
        host = fl
        rebind()
        return fl
    }

    private fun rebind() {
        val fl = host ?: return
        fl.removeAllViews()
        val content = LayoutInflater.from(requireContext()).inflate(layoutRes, fl, false)
        fl.addView(content)
        onBindContent(content)
    }

    /** Wire up the freshly inflated [content] (find views, set listeners, text). */
    protected abstract fun onBindContent(content: View)

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        rebind()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        host = null
    }
}
