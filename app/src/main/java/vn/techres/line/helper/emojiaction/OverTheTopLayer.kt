package vn.techres.line.helper.emojiaction

import android.app.Activity
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import vn.techres.line.R
import vn.techres.line.helper.WriteLog
import java.lang.ref.WeakReference

class OverTheTopLayer  {
    class OverTheTopLayerException(msg: String?) : RuntimeException(msg)

    private var mWeakActivity: WeakReference<Activity>? = null
    private var mWeakRootView: WeakReference<ViewGroup>? = null
    private var mCreatedOttLayer: FrameLayout? = null
    private var mScalingFactor = 1.0f
    private var mDrawLocation = intArrayOf(0, 0)
    private var mBitmap: Bitmap? = null
    private var idRes: Int? = 0



    /**
     * To create a layer on the top of activity
     *
     */
    fun with(weakReferenceActivity: Activity?): OverTheTopLayer {
        mWeakActivity = WeakReference<Activity>(weakReferenceActivity)
        return this
    }

    /**
     * Draws the image as per the drawable resource id on the given location pixels.
     *
     */
    fun generateBitmap(
        resources: Resources?,
        drawableResId: Int,
        mScalingFactor: Float,
        location: IntArray?
    ): OverTheTopLayer {
        var location = location
        if (location == null) {
            location = intArrayOf(0, 0)
        } else if (location.size != 2) {
            throw OverTheTopLayerException("Requires location as an array of length 2 - [x,y]")
        }
        val bitmap = BitmapFactory.decodeResource(resources, drawableResId)
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * mScalingFactor).toInt(),
            (bitmap.height * mScalingFactor).toInt(), false
        )
        mBitmap = scaledBitmap
        mDrawLocation = location
        return this
    }

    fun setBitmap(bitmap: Bitmap?, location: IntArray?): OverTheTopLayer {
        var location = location
        if (location == null) {
            location = intArrayOf(0, 0)
        } else if (location.size != 2) {
            throw OverTheTopLayerException("Requires location as an array of length 2 - [x,y]")
        }
        mBitmap = bitmap
        mDrawLocation = location
        return this
    }

    fun setSRC(idRes : Int) : OverTheTopLayer{
        this.idRes = idRes
        return this
    }
    /**
     * Holds the scaling factor for the image.
     *
     * @param scale
     * @return
     */
    fun scale(scale: Float): OverTheTopLayer {
        if (scale <= 0) {
            throw OverTheTopLayerException("Scaling should be > 0")
        }
        mScalingFactor = scale
        return this
    }

    /**
     * Attach the OTT layer as the child of the given root view.
     * @return
     */
    fun attachTo(rootView: ViewGroup?): OverTheTopLayer? {
        mWeakRootView = WeakReference<ViewGroup>(rootView)
        return this
    }

    /**
     * Creates an OTT.
     * @return
     */
    fun create(): FrameLayout? {
        if (mCreatedOttLayer != null) {
            destroy()
        }
        if (mWeakActivity == null) {
            throw OverTheTopLayerException("Could not create the layer as not activity reference was provided.")
        }
        val activity: Activity? = mWeakActivity?.get()
        if (activity != null) {
            val attachingView: ViewGroup? = if (mWeakRootView != null && mWeakRootView?.get() != null) {
                mWeakRootView?.get()
            } else {
                activity.findViewById<View>(R.id.content) as ViewGroup
            }
            val imageView = ImageView(activity)
            idRes?.let { imageView.setImageResource(it) }
            imageView.measure(
                View.MeasureSpec.makeMeasureSpec(dpToPx(25), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(dpToPx(25), View.MeasureSpec.AT_MOST)
            )
            var params = imageView.layoutParams as FrameLayout.LayoutParams?
            if (params == null) {
                params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.NO_GRAVITY
                )
                imageView.layoutParams = params
            }
            params.width = dpToPx(25)
            params.height = dpToPx(25)
            imageView.layoutParams = params
            val ottLayer = FrameLayout(activity)
            val topLayerParam = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.BOTTOM
            )
            ottLayer.layoutParams = topLayerParam
            ottLayer.addView(imageView)
            attachingView?.addView(ottLayer)
            mCreatedOttLayer = ottLayer
        } else {
            WriteLog.e(
                OverTheTopLayer::class.java.simpleName,
                "Could not create the layer. Reference to the activity was lost"
            )
        }
        return mCreatedOttLayer
    }

    /**
     * Kills the OTT
     */
    fun destroy() {
        if (mWeakActivity == null) {
            throw OverTheTopLayerException("Could not create the layer as not activity reference was provided.")
        }
        val activity: Activity? = mWeakActivity?.get()
        if (activity != null) {
            val attachingView: ViewGroup? = if (mWeakRootView != null && mWeakRootView?.get() != null) {
                mWeakRootView?.get()
            } else {
                activity.findViewById<View>(R.id.content) as ViewGroup
            }
            if (mCreatedOttLayer != null) {
                attachingView?.removeView(mCreatedOttLayer)
                mCreatedOttLayer = null
            }
        } else {
            WriteLog.e(
                OverTheTopLayer::class.java.simpleName,
                "Could not destroy the layer as the layer was never created."
            )
        }
    }

    /**
     * Applies the animation to the image view present in OTT.
     * @param animation
     */
    fun applyAnimation(animation: Animation?) {
        if (mCreatedOttLayer != null) {
            val drawnImageView: ImageView = mCreatedOttLayer?.getChildAt(0) as ImageView
            drawnImageView.startAnimation(animation)
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }
}