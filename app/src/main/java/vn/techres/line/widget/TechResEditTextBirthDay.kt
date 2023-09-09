package vn.techres.line.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class TechResEditTextBirthDay : AppCompatEditText {
    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     * @param attrs the attrs
     * @param defStyle the def style
     */

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     * @param attrs the attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context) : super(context) {
        init()
    }

    companion object {
        const val FONTS = "fonts/UVNDaLat-R.ttf"
    }

    private fun init(attrs: AttributeSet) {
        init(attrs)
        // Read layout attributes
        var fontWeight = 0
        if (typeface != null) fontWeight = typeface.style
        if (fontWeight == Typeface.NORMAL) {
            val type = Typeface.createFromAsset(
                context.assets,
                FONTS
            )
            this.typeface = type
        } else {
            val type = Typeface.createFromAsset(
                context.assets,
                FONTS
            )
            this.typeface = type

        }
    }


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