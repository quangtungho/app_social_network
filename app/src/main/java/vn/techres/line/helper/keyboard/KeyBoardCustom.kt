package vn.techres.line.helper.keyboard

import android.content.Context
import android.widget.FrameLayout
import vn.techres.line.R
import vn.techres.line.databinding.KeyboardCustomLayoutBinding

class KeyBoardCustom(context: Context) : FrameLayout(context) {
    private var binding : KeyboardCustomLayoutBinding? = null

    init {
        val view = inflate(context, R.layout.keyboard_custom_layout, this)
        binding = KeyboardCustomLayoutBinding.bind(view)
        setData()
    }
    private fun setData(){

    }
    fun closeKeyBoard(){

    }
    fun openKeyBoard(){

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}