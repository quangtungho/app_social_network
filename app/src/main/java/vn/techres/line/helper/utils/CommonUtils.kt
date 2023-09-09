package vn.techres.line.helper.utils

import android.content.res.Resources

class CommonUtils {
    companion object {

        val MIN_KEYBOARD_HEIGHT_PX = 150

        fun dpToPx(dp: Int): Int {
            return (dp * Resources.getSystem().displayMetrics.density).toInt()
        }
        fun pxToDp(px: Int): Int {
            return (px / Resources.getSystem().displayMetrics.density).toInt()
        }

    }
}