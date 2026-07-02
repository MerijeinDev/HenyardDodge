package henyard.dodgerush.dewpond.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import henyard.dodgerush.dewpond.R

/**
 * TextView that paints a coloured outline (stroke) behind the fill, for the
 * chunky cartoon look: light fill (white/cream via android:textColor) with a
 * coloured stroke (default #C34800 via app:outlineColor).
 *
 * Draws the text layout twice — stroke pass first, then fill on top — so the
 * fill colour comes straight from [getCurrentTextColor].
 */
class OutlineTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var outlineColor: Int = ContextDefaults.strokeColor(context)
    private var outlineWidthPx: Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics)

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.OutlineTextView)
            outlineColor = a.getColor(R.styleable.OutlineTextView_outlineColor, outlineColor)
            outlineWidthPx = a.getDimension(
                R.styleable.OutlineTextView_outlineWidth, outlineWidthPx
            )
            a.recycle()
        }
    }

    fun setOutlineColor(color: Int) {
        outlineColor = color
        invalidate()
    }

    fun setOutlineWidth(px: Float) {
        outlineWidthPx = px
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val layout = layout
        if (layout == null || outlineWidthPx <= 0f) {
            super.onDraw(canvas)
            return
        }

        val paint: Paint = paint
        val originalStyle = paint.style
        val fillColor = currentTextColor

        canvas.save()
        canvas.translate(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat())

        // Outline pass — the shadow layer is skipped to keep the stroke crisp.
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = outlineWidthPx
        paint.color = outlineColor
        layout.draw(canvas)

        // Fill pass.
        paint.style = Paint.Style.FILL
        paint.color = fillColor
        layout.draw(canvas)

        canvas.restore()
        paint.style = originalStyle
    }

    private object ContextDefaults {
        fun strokeColor(context: Context): Int =
            try {
                context.getColor(R.color.text_stroke)
            } catch (_: Exception) {
                Color.parseColor("#C34800")
            }
    }
}
