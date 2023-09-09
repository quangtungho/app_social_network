package vn.techres.line.helper.fresco.view

import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import vn.techres.line.helper.fresco.model.MediaSource
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.imagepipeline.request.ImageRequestBuilder
import vn.techres.line.R
import java.net.MalformedURLException
import java.net.URL
import kotlin.math.roundToInt

class ImageViewer() : OnDismissListener, DialogInterface.OnKeyListener {
    private val TAG = ImageViewer::class.java.simpleName

    private var builder: Builder? = null
    private var dialog: AlertDialog? = null
    private var viewer: ImageViewerView? = null

    constructor(builder: Builder?) : this() {
        this.builder = builder
        createDialog()
    }

    /**
     * Displays the built viewer if passed images list isn't empty
     */
    fun show() {
        if (builder!!.dataSet!!.data.isNotEmpty()) {
            dialog!!.show()
        } else {
            Log.w(TAG, "Images list cannot be empty! Viewer ignored.")
        }
    }

    fun getUrl(): String? {
        return viewer!!.getUrl()
    }

    fun getItem(): MediaSource? {
        return viewer!!.getItem()
    }

    fun releaseVideo() {
        viewer!!.releaseVideo()
    }

    fun stopVideo() {
        viewer!!.stopVideo()
    }

    /**
     * Set Alert dialog init [createDialog]
     * */
    private fun createDialog() {
        viewer = ImageViewerView(builder?.context)
        viewer!!.setCustomImageRequestBuilder(builder?.customImageRequestBuilder)
        viewer!!.setCustomDraweeHierarchyBuilder(builder?.customHierarchyBuilder)
        viewer!!.allowZooming(builder!!.isZoomingAllowed)
        viewer!!.allowSwipeToDismiss(builder!!.isSwipeToDismissAllowed)
        viewer!!.setOnDismissListener(this)
        viewer!!.setBackgroundColor(builder!!.backgroundColor)
        viewer!!.setOverlayView(builder?.overlayView)
        viewer!!.setImageMargin(builder!!.imageMarginPixels)
        viewer!!.setContainerPadding(builder!!.containerPaddingPixels)
        viewer!!.setUrls(builder?.dataSet, builder!!.startPosition)

        viewer!!.setPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (builder!!.imageChangeListener != null) {
                    builder!!.imageChangeListener!!.onImageChange(position)
                }
            }
        })
        viewer!!.getBackPress()!!.setOnClickListener {
            releaseVideo()
            dialog!!.dismiss()
        }
        viewer!!.getDowLoad()!!.setOnClickListener {
            download(builder!!.context!!)
        }
        dialog = AlertDialog.Builder(builder!!.context!!, getDialogStyle())
            .setView(viewer)
            .setOnKeyListener(this)
            .create()
        dialog!!.setOnDismissListener {
            releaseVideo()
            if (builder!!.onDismissListener != null) {
                builder!!.onDismissListener!!.onDismiss()
            }
        }
    }

    private fun download(context: Context) {
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(
            getItem()?.url.toString()
        )
        val request = DownloadManager.Request(uri)
        request.setTitle(
            getFileNameFromURL(
                getItem()?.url.toString()
            )
        )
        request.setDescription("downloading")
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            getFileNameFromURL(
                getItem()?.url.toString()
            )
        )
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager.enqueue(request)
        Toast.makeText(context, "đang tải xuống", Toast.LENGTH_LONG).show()
    }

    private fun getFileNameFromURL(url: String?): String {
        if (url == null) {
            return ""
        }
        try {
            val resource = URL(url)
            val host = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                // handle ...example.com
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')
        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // calculate the end index
        val endIndex = lastQMPos.coerceAtMost(lastHashPos)
        return url.substring(startIndex, endIndex)
    }

    /**
     * Fires when swipe to dismiss was initiated
     */
    override fun onDismiss() {
        dialog!!.dismiss()
    }

    /**
     * Resets image on KeyEvent.KEYCODE_BACK to normal scale if needed, otherwise - hide the viewer.
     */
    override fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP &&
            !event.isCanceled
        ) {
            dialog.cancel()
//            if (viewer!!.isScaled()) {
//                viewer!!.resetScale()
//            } else {
//
//            }
        }
        return true
    }

    /**
     * Creates new `ImageRequestBuilder`.
     */
    fun createImageRequestBuilder(): ImageRequestBuilder? {
        return ImageRequestBuilder.newBuilderWithSource(Uri.parse(""))
    }

    /**
     * Interface definition for a callback to be invoked when image was changed
     */
    interface OnImageChangeListener {
        fun onImageChange(position: Int)
    }

    /**
     * Interface definition for a callback to be invoked when viewer was dismissed
     */
    interface OnDismissListener {
        fun onDismiss()
    }

    @StyleRes
    private fun getDialogStyle(): Int {
        return if (builder!!.shouldStatusBarHide)
            R.style.AppTheme
        else R.style.AppTheme
    }

    /**
     * Interface used to format custom objects into an image url.
     */
    interface Formatter<T> {
        /**
         * Formats an image url representation of the object.
         *
         * @param t The object that needs to be formatted into url.
         * @return An url of image.
         */
        fun format(t: String): String
    }

    class DataSet<T>(val data: List<MediaSource>) {
        var formatter: Formatter<MediaSource>? = null
        fun format(position: Int): String {
            return format(data[position])
        }

        fun format(t: MediaSource): String {
            return formatter?.format(t.url) ?: t.toString()
        }

        fun getItem(position: Int): MediaSource {
            return data[position]
        }
    }

    /**
     * Builder class for [ImageViewer]
     */
    class Builder() {
        var context: Context? = null
        var dataSet: DataSet<MediaSource>? = null

        @ColorInt
        var backgroundColor = Color.BLACK
        var startPosition = 0
        var imageChangeListener: OnImageChangeListener? = null
        var onDismissListener: OnDismissListener? = null
        var overlayView: View? = null
        var imageMarginPixels = 0
        var containerPaddingPixels = IntArray(4)
        var customImageRequestBuilder: ImageRequestBuilder? = null
        var customHierarchyBuilder: GenericDraweeHierarchyBuilder? = null
        var shouldStatusBarHide = true
        var isZoomingAllowed = true
        var isSwipeToDismissAllowed = true

        /**
         * Constructor using a context and images urls array for this builder and the [ImageViewer] it creates.
         */
        constructor(context: Context?, images: List<MediaSource>?) : this() {
            this.context = context
            this.dataSet = DataSet(images!!)
        }

        /**
         * If you use an non-string collection, you can use custom [Formatter] to represent it as url.
         */
        fun setFormatter(formatter: Formatter<MediaSource>?): Builder {
            dataSet!!.formatter = formatter
            return this
        }

        /**
         * Set background color resource for viewer
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setBackgroundColorRes(@ColorRes color: Int): Builder {
            return setBackgroundColor(context!!.resources.getColor(color))
        }

        /**
         * Set background color int for viewer
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setBackgroundColor(@ColorInt color: Int): Builder {
            backgroundColor = color
            return this
        }

        /**
         * Set background color int for viewer
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setStartPosition(position: Int): Builder {
            startPosition = position
            return this
        }

        /**
         * Set [ImageViewer.OnImageChangeListener] for viewer.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setImageChangeListener(imageChangeListener: OnImageChangeListener?): Builder {
            this.imageChangeListener = imageChangeListener
            return this
        }

        /**
         * Set overlay view
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setOverlayView(view: View?): Builder {
            overlayView = view
            return this
        }

        /**
         * Set space between the images in px.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setImageMarginPx(marginPixels: Int): Builder {
            imageMarginPixels = marginPixels
            return this
        }

        /**
         * Set space between the images using dimension.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setImageMargin(context: Context, @DimenRes dimen: Int): Builder {
            imageMarginPixels = context.resources.getDimension(dimen).roundToInt()
            return this
        }

        /**
         * Set `start`, `top`, `end` and `bottom` padding for zooming and scrolling area in px.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setContainerPaddingPx(start: Int, top: Int, end: Int, bottom: Int): Builder {
            containerPaddingPixels = intArrayOf(start, top, end, bottom)
            return this
        }

        /**
         * Set `start`, `top`, `end` and `bottom` padding for zooming and scrolling area using dimension.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setContainerPadding(
            context: Context,
            @DimenRes start: Int, @DimenRes top: Int,
            @DimenRes end: Int, @DimenRes bottom: Int
        ): Builder {
            setContainerPaddingPx(
                context.resources.getDimension(start).roundToInt(),
                context.resources.getDimension(top).roundToInt(),
                context.resources.getDimension(end).roundToInt(),
                context.resources.getDimension(bottom).roundToInt()
            )
            return this
        }

        /**
         * Set common padding for zooming and scrolling area in px.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setContainerPaddingPx(padding: Int): Builder {
            containerPaddingPixels = intArrayOf(padding, padding, padding, padding)
            return this
        }

        /**
         * Set common padding for zooming and scrolling area using dimension.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setContainerPadding(context: Context, @DimenRes padding: Int): Builder {
            val paddingPx = context.resources.getDimension(padding).roundToInt()
            setContainerPaddingPx(paddingPx, paddingPx, paddingPx, paddingPx)
            return this
        }

        /**
         * Set status bar visibility. By default is true.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun hideStatusBar(shouldHide: Boolean): Builder {
            shouldStatusBarHide = shouldHide
            return this
        }

        /**
         * Allow or disallow zooming. By default is true.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun allowZooming(value: Boolean): Builder {
            isZoomingAllowed = value
            return this
        }

        /**
         * Allow or disallow swipe to dismiss gesture. By default is true.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun allowSwipeToDismiss(value: Boolean): Builder {
            isSwipeToDismissAllowed = value
            return this
        }

        /**
         * Set [ImageViewer.OnDismissListener] for viewer.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setOnDismissListener(onDismissListener: OnDismissListener?): Builder {
            this.onDismissListener = onDismissListener
            return this
        }

        /**
         * Set @`ImageRequestBuilder` for drawees. Use it for post-processing, custom resize options etc.
         * Use [ImageViewer.createImageRequestBuilder] to create its new instance.
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setCustomImageRequestBuilder(customImageRequestBuilder: ImageRequestBuilder?): Builder {
            this.customImageRequestBuilder = customImageRequestBuilder
            return this
        }

        /**
         * Set [GenericDraweeHierarchyBuilder] for drawees inside viewer.
         * Use it for drawee customizing (e.g. failure image, placeholder, progressbar etc.)
         * N.B.! Due to zoom logic there is limitation of scale type which always equals FIT_CENTER. Other values will be ignored
         *
         * @return This Builder object to allow for chaining of calls to set methods
         */
        fun setCustomDraweeHierarchyBuilder(customHierarchyBuilder: GenericDraweeHierarchyBuilder?): Builder {
            this.customHierarchyBuilder = customHierarchyBuilder
            return this
        }

        /**
         * Creates a [ImageViewer] with the arguments supplied to this builder. It does not
         * [ImageViewer.show] the dialog. This allows the user to do any extra processing
         * before displaying the dialog. Use [.show] if you don't have any other processing
         * to do and want this to be created and displayed.
         */
        private fun build(): ImageViewer {
            return ImageViewer(this)
        }

        /**
         * Creates a [ImageViewer] with the arguments supplied to this builder and
         * [ImageViewer.show]'s the dialog.
         */
        fun show(): ImageViewer {
            val dialog = build()
            dialog.show()
            return dialog
        }

    }

}