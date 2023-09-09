package vn.techres.line.helper.emojiaction

import android.app.Activity
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import vn.techres.line.helper.WriteLog

class ZeroGravityAnimation  {
    private var mOriginationDirection: Direction = Direction.BOTTOM
    private var mDestinationDirection: Direction = Direction.TOP
    private var mCount = 1
    private var mImageResId = 0
    private var mScalingFactor = 1f
    private var mAnimationListener: Animation.AnimationListener? = null


    /**
     * Sets the orignal direction. The animation will originate from the given direction.
     *
     */
    fun setOriginationDirection(direction: Direction): ZeroGravityAnimation {
        mOriginationDirection = direction
        return this
    }

    /**
     * Sets the animation destination direction. The translate animation will proceed towards the given direction.
     * @param direction
     * @return
     */
    fun setDestinationDirection(direction: Direction): ZeroGravityAnimation {
        mDestinationDirection = direction
        return this
    }


    /**
     * Sets the image reference id for drawing the image
     * @param resId
     * @return
     */
    fun setImage(resId: Int): ZeroGravityAnimation {
        mImageResId = resId
        return this
    }

    /**
     * Sets the image scaling value.
     * @param scale
     * @return
     */
    fun setScalingFactor(scale: Float): ZeroGravityAnimation {
        mScalingFactor = scale
        return this
    }

    fun setAnimationListener(listener: Animation.AnimationListener?): ZeroGravityAnimation {
        mAnimationListener = listener
        return this
    }

    fun setCount(count: Int): ZeroGravityAnimation {
        mCount = count
        return this
    }


    /**
     * Starts the Zero gravity animation by creating an OTT and attach it to th given ViewGroup
     * @param activity
     * @param ottParent
     */
    fun play(activity: Activity?, ottParent: ViewGroup?, x: Float, y: Float) {
        if (mCount > 0) {
            for (i in 0 until mCount) {
                val layer = OverTheTopLayer()
                layer.with(activity)
                    .scale(mScalingFactor)
                    .attachTo(ottParent)
                    ?.setSRC(mImageResId)
                    ?.create()
                val widthA = activity?.resources?.displayMetrics?.widthPixels ?: 100
                val random = java.util.Random()

                val animation = TranslateAnimation(
                    x, random.nextInt(widthA).toFloat(), y, 0F
                )
                animation.duration = 3500.toLong()
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        if (i == 0 && mAnimationListener != null) {
                            mAnimationListener!!.onAnimationStart(animation)

                        }
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        layer.destroy()
                        if (i == mCount - 1 && mAnimationListener != null) {
                            mAnimationListener!!.onAnimationEnd(animation)
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                        //Code
                    }
                })
                layer.applyAnimation(animation)


            }
        } else {
            WriteLog.e(
                ZeroGravityAnimation::class.java.simpleName,
                "Count was not provided, animation was not started"
            )
        }
    }
}