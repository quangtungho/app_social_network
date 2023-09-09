package vn.techres.line.helper.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.progressindicator.LinearProgressIndicator
import vn.techres.line.activity.TechResApplication
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.show

class GlideImageLoader() {
    private var mImageView: ImageView? = null
    private var mProgressBar: LinearProgressIndicator? = null

    constructor(imageView: ImageView?, progressBar: LinearProgressIndicator?) : this() {
        mImageView = imageView
        mProgressBar = progressBar
    }

    fun load(url: String?, options: RequestOptions?) {
        if (url == null || options == null) return
        onConnecting()
        //set listener & start
        ProgressAppGlideModule().expect(url, object : ProgressAppGlideModule.UIonProgressListener {
            override fun onProgress(bytesRead: Long, expectedLength: Long) {
                if (mProgressBar != null) {
                    mProgressBar?.progress = (100 * bytesRead / expectedLength).toInt()
                }
            }

            override val granualityPercentage: Float
                get() = 1.0f
        })

        //Get Image
        mImageView?.let {
            Glide.with(it)
                .asBitmap()
                .load(url)
                .transition(BitmapTransitionOptions.withCrossFade())
                .apply(options.diskCacheStrategy(DiskCacheStrategy.ALL))
                .priority(Priority.HIGH)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        ProgressAppGlideModule().forget(url)
                        onFinished()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        ProgressAppGlideModule().forget(url)
                        onFinished()
                        return false
                    }

                })
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onLoadCleared(placeholder: Drawable?) {}
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        val bitmap: Bitmap = Utils.resizeBitmap(
                            resource,
                            TechResApplication.widthView * 2 / 3
                        )
                        it.layoutParams.height = bitmap.height
                        it.layoutParams.width = bitmap.width
                        it.setImageBitmap(bitmap)
                    }
                })
        }

    }


    private fun onConnecting() {
        if (mProgressBar != null) mProgressBar?.show()
    }

    private fun onFinished() {
        if (mProgressBar != null && mImageView != null) {
            mProgressBar?.hide()
            mImageView?.show()
        }
    }
}