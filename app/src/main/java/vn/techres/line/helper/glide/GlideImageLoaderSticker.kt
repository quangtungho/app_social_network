package vn.techres.line.helper.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show

class GlideImageLoaderSticker() {
    private var mImageView: ImageView? = null
    private var mProgressBar: ProgressBar? = null

    constructor(imageView: ImageView?, progressBar: ProgressBar?) : this(){
        mImageView = imageView
        mProgressBar = progressBar
    }

    fun load(url: String?, options: RequestOptions?) {
        if (url == null || options == null) return
        onConnecting()
        //set Listener & start
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
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(options.diskCacheStrategy(DiskCacheStrategy.ALL))
                .listener(object : RequestListener<Drawable?> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        ProgressAppGlideModule().forget(url)
                        onFinished()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable?>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        ProgressAppGlideModule().forget(url)
                        onFinished()
                        return false
                    }
                })
                .dontAnimate()
                .into(it)
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