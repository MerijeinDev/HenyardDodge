package henyard.dodgerush.dewpond.ui.base

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * Thin base fragment for all screens.
 *
 * Screen orientation is driven centrally by [henyard.dodgerush.dewpond.MainActivity]
 * via a NavController destination listener (landscape for Splash/NoInternet/
 * Notifications, portrait everywhere else), so it is not handled here.
 */
abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId)
