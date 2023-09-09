package vn.techres.line.helper

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class TopCropImageView : AppCompatImageView {
    constructor(context: Context) : super(context){
        scaleType = ScaleType.MATRIX
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        scaleType = ScaleType.MATRIX
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        scaleType = ScaleType.MATRIX
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        computeMatrix()
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        computeMatrix()
        return super.setFrame(l, t, r, b)
    }


    private fun computeMatrix() {
        if (drawable == null) return
        val matrix: Matrix = imageMatrix
        val scaleFactor = width / drawable.intrinsicWidth.toFloat()
        matrix.setScale(scaleFactor, scaleFactor, 0F, 0F)
        imageMatrix = matrix
    }
}