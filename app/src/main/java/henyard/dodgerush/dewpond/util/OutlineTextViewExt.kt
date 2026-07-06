package henyard.dodgerush.dewpond.util

import android.util.TypedValue
import androidx.core.content.ContextCompat
import henyard.dodgerush.dewpond.R
import henyard.dodgerush.dewpond.widget.OutlineTextView

/**
 * Applies the shared dialog-button label look to an [OutlineTextView]: white fill
 * with an outlined stroke. Used by the dialog-style buttons on the grey screens
 * (NoInternet restart, Notifications allow/deny) so the spec lives in one place
 * instead of being re-typed per fragment.
 *
 * [primary] = the large yellow call-to-action (25sp, #C34800 stroke); otherwise
 * the smaller blue skip/secondary button (15sp, #013071 stroke).
 */
fun OutlineTextView.applyDialogButtonStyle(primary: Boolean) {
    val sizePx = resources.getDimension(
        if (primary) R.dimen.text_dialog_btn_primary else R.dimen.text_dialog_btn_skip,
    )
    setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx)
    setTextColor(ContextCompat.getColor(context, R.color.white))
    setOutlineColor(
        ContextCompat.getColor(context, if (primary) R.color.text_stroke else R.color.btn_blue_stroke),
    )
    setOutlineWidth(resources.displayMetrics.density * if (primary) 1.5f else 1f)
}