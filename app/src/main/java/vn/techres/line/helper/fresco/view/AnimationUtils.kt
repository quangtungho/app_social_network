package vn.techres.line.helper.fresco.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewConfiguration

object AnimationUtils {
    private fun AnimationUtils() {
        throw AssertionError()
    }

    fun animateVisibility(view: View) {
        val isVisible = view.visibility == View.VISIBLE
        val from = if (isVisible) 1.0f else 0.0f
        val to = if (isVisible) 0.0f else 1.0f
        val animation = ObjectAnimator.ofFloat(view, "alpha", from, to)
        animation.duration = ViewConfiguration.getDoubleTapTimeout().toLong()
        if (isVisible) {
            animation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
        } else view.visibility = View.VISIBLE
        animation.start()
    }
}