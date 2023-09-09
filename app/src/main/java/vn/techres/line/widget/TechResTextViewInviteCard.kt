package vn.techres.line.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class TechResTextViewInviteCard: AppCompatTextView {

    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     * @param attrs the attrs
     * @param defStyle the def style
     */
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init(attrs)
    }

    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     * @param attrs the attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(
        context,
        attrs
    ) {
        init(attrs)
    }
    companion object {
        const val FONTS = "fonts/UVNDaLat-R.ttf"
    }
    private fun init(attrs: AttributeSet) {
        // Read layout attributes
        var fontWeight = 0
        if (typeface != null) fontWeight = typeface.style
        when (fontWeight) {
            Typeface.NORMAL -> {
                val type = Typeface.createFromAsset(
                    context.assets,
                    "fonts/Roboto-Regular.ttf"
                )
                this.typeface = type
            }
            Typeface.BOLD -> {
                val type = Typeface.createFromAsset(
                    context.assets,
                    "fonts/Roboto-Bold.ttf"
                )
                this.typeface = type
            }
            Typeface.ITALIC -> {
                val type = Typeface.createFromAsset(
                    context.assets,
                    "font/Roboto-Italic.ttf"
                )
                this.typeface = type
            }
        }
    }

    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     */
    constructor(context: Context?) : super(context!!) {}

    /**
     * Inits the.
     */
    fun init() {
        val tf = Typeface.createFromAsset(
            context.assets,
            FONTS
        )
        setTypeface(tf, Typeface.NORMAL)
    }
}