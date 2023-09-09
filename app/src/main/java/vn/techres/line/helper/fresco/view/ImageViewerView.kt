package vn.techres.line.helper.fresco.view

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager.widget.ViewPager
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.imagepipeline.request.ImageRequestBuilder
import vn.techres.line.R
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.fresco.model.MediaSource
import vn.techres.line.helper.fresco.view.AnimationUtils.animateVisibility
import kotlin.math.abs

class ImageViewerView : RelativeLayout, OnDismissListener,
    SwipeToDismissListener.OnViewMoveListener {
    private var audioManager: AudioManager? = null
    private var backgroundView: View? = null
    private var rlController: View? = null
    private var imgBack: ImageButton? = null
    private var imgDownload: ImageButton? = null
    private var pager: CustomViewPager? = null
    private var adapter: ImageViewerAdapter? = null
    private var directionDetector: SwipeDirectionDetector? = null
    private var scaleDetector: ScaleGestureDetector? = null
    private var pageChangeListener: ViewPager.OnPageChangeListener? = null
    private var gestureDetector: GestureDetectorCompat? = null

    private var dismissContainer: ViewGroup? = null
    private var swipeDismissListener: SwipeToDismissListener? = null
    private var overlayView: View? = null

    private var direction: SwipeDirectionDetector.Direction? = null

    private var customImageRequestBuilder: ImageRequestBuilder? = null
    private var customDraweeHierarchyBuilder: GenericDraweeHierarchyBuilder? = null
    private var wasScaled = false
    private var onDismissListener: OnDismissListener? = null
    private var isOverlayWasClicked = false

    private var isZoomingAllowed = true
    private var isSwipeToDismissAllowed = true
    private val maxVolume = 100

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }


    fun setUrls(dataSet: ImageViewer.DataSet<MediaSource>?, startPosition: Int) {

        adapter = ImageViewerAdapter(
            context,
            dataSet,
            customImageRequestBuilder,
            customDraweeHierarchyBuilder,
            isZoomingAllowed
        )
        pager!!.adapter = adapter
        pager!!.offscreenPageLimit = 0
        setStartPosition(startPosition)
    }

    fun setCustomImageRequestBuilder(customImageRequestBuilder: ImageRequestBuilder?) {
        this.customImageRequestBuilder = customImageRequestBuilder
    }

    fun setCustomDraweeHierarchyBuilder(customDraweeHierarchyBuilder: GenericDraweeHierarchyBuilder?) {
        this.customDraweeHierarchyBuilder = customDraweeHierarchyBuilder
    }

    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView)
            .setBackgroundColor(color)
    }

    fun setOverlayView(view: View?) {
        overlayView = view
        if (overlayView != null) {
            dismissContainer!!.addView(view)
        }
    }

    fun allowZooming(allowZooming: Boolean) {
        isZoomingAllowed = allowZooming
    }

    fun allowSwipeToDismiss(allowSwipeToDismiss: Boolean) {
        isSwipeToDismissAllowed = allowSwipeToDismiss
    }

    fun setImageMargin(marginPixels: Int) {
        pager!!.pageMargin = marginPixels
    }

    fun setContainerPadding(paddingPixels: IntArray) {
        pager!!.setPadding(
            paddingPixels[0],
            paddingPixels[1],
            paddingPixels[2],
            paddingPixels[3]
        )
    }

    private fun init() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        inflate(context, R.layout.item_viewer, this)
        backgroundView = findViewById(R.id.backgroundView)
        rlController = findViewById(R.id.rlController)
        pager = findViewById(R.id.pager)
        imgBack = findViewById(R.id.imgBack)
        imgDownload = findViewById(R.id.imgDownload)
        dismissContainer = findViewById(R.id.container)
        dismissContainer!!.setOnTouchListener(swipeDismissListener)
        swipeDismissListener = SwipeToDismissListener(
            findViewById(R.id.dismissView),
            this, this
        )

        directionDetector = object : SwipeDirectionDetector(context) {
            override fun onDirectionDetected(direction: Direction?) {
                this@ImageViewerView.direction = direction
            }
        }

        scaleDetector = ScaleGestureDetector(
            context,
            SimpleOnScaleGestureListener()
        )

        gestureDetector = GestureDetectorCompat(context, object : SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (pager!!.isScrolled()) {
                    onClick(e, isOverlayWasClicked)
                }
                return false
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (event != null) {
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                WriteLog.d("KEYCODE_VOLUME_DOWN", "Giam")
//                adapter?.seekBarVideo()?.progress =
//                    audioManager.getStreamVolume(adapter!!.play()!!.audioStreamType)
            }
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (event != null) {
            if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                WriteLog.d("KEYCODE_VOLUME_UP", "Tang")
//                adapter?.seekBarVideo()?.progress =
//                    audioManager.getStreamVolume(adapter!!.play()!!.audioStreamType)
//                adapter!!.seekBarVideo()!!.notify()
            }
        }
        return true
    }

    fun getBackPress(): ImageButton? {
        return imgBack
    }

    fun getDowLoad(): ImageButton? {
        return imgDownload
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        pager!!.dispatchTouchEvent(event)
        swipeDismissListener!!.onTouch(dismissContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)

        scaleDetector!!.onTouchEvent(event)
        gestureDetector!!.onTouchEvent(event)

//        if (direction != null) {
//
//        }

        return if (event.action == MotionEvent.ACTION_DOWN) {
            super.dispatchTouchEvent(event)
        } else {
            false
        }


////        onUpDownEvent(event)
////        if (direction == null) {
////            if (scaleDetector!!.isInProgress || event.pointerCount > 1) {
////                wasScaled = true
////                return pager!!.dispatchTouchEvent(event)
////            }
////        }
//        return super.dispatchTouchEvent(event)
    }

    override fun onDismiss() {
        adapter?.releaseVideo()
        if (onDismissListener != null) {
            onDismissListener!!.onDismiss()
        }
    }

    override fun onViewMove(translationY: Float, translationLimit: Int) {
        val alpha = 1.0f - 1.0f / translationLimit / 4 * abs(translationY)
        backgroundView!!.alpha = alpha
        dismissContainer!!.alpha = alpha
        if (overlayView != null) overlayView!!.alpha = alpha
    }


    fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        this.onDismissListener = onDismissListener
    }
//
//    fun resetScale() {
//        adapter!!.resetScale(pager!!.currentItem)
//    }
//
//    fun isScaled(): Boolean {
//        return adapter!!.isScaled(pager!!.currentItem)
//    }

    fun getUrl(): String? {
        return adapter?.getUrl(pager!!.currentItem)
    }

    fun getItem(): MediaSource? {
        return adapter?.getItem(pager!!.currentItem)
    }

    fun releaseVideo() {
        adapter?.releaseVideo()
    }

    fun stopVideo() {
        adapter?.stopVideo()
    }

    fun setPageChangeListener(pageChangeListener: ViewPager.OnPageChangeListener) {
        pager!!.removeOnPageChangeListener(pageChangeListener)
        this.pageChangeListener = pageChangeListener
        pager!!.addOnPageChangeListener(pageChangeListener)
        pageChangeListener.onPageSelected(pager!!.currentItem)
    }

    private fun setStartPosition(position: Int) {
        pager!!.currentItem = position
    }

    private fun onUpDownEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            onActionUp(event)
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            onActionDown(event)
        }
        scaleDetector!!.onTouchEvent(event)
        gestureDetector!!.onTouchEvent(event)
    }

    private fun onActionDown(event: MotionEvent) {
//        direction = null
        wasScaled = false
        pager!!.dispatchTouchEvent(event)
        swipeDismissListener!!.onTouch(dismissContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun onActionUp(event: MotionEvent) {
        swipeDismissListener!!.onTouch(dismissContainer, event)
        pager!!.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun onClick(event: MotionEvent, isOverlayWasClicked: Boolean) {
        if (overlayView != null && !isOverlayWasClicked) {
            animateVisibility(overlayView!!)
            super.dispatchTouchEvent(event)
        }
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean {
        return overlayView != null && overlayView!!.visibility == VISIBLE && overlayView!!.dispatchTouchEvent(
            event
        )
    }

}