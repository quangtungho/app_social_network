package vn.techres.line.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * Created by tuan.nguyen on 20/01/17.
 */
class TechResTextViewLight : AppCompatTextView {
    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     * @param attrs the attrs
     * @param defStyle the def style
     */
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {
        init()
    }

    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     * @param attrs the attrs
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!,
        attrs
    ) {
        init()
    }

    /**
     * Instantiates a new my text view.
     *
     * @param context the context
     */
    constructor(context: Context?) : super(context!!) {
        init()
    }

    /**
     * Inits the.
     */
    fun init() {
        val tf = Typeface.createFromAsset(
            context.assets,
            "fonts/Roboto-Light.ttf"
        )
        setTypeface(tf, Typeface.NORMAL)
    }
}