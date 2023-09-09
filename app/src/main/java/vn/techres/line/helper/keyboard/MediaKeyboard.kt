package vn.techres.line.helper.keyboard

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import com.aghajari.emojiview.view.AXEmojiLayout
import com.luck.picture.lib.animators.AlphaInAnimationAdapter
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.dialog.PictureLoadingDialog
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnRecyclerViewPreloadMoreListener
import com.luck.picture.lib.model.LocalMediaPageLoader
import com.luck.picture.lib.tools.ScreenUtils
import com.luck.picture.lib.tools.ValueOf
import com.luck.picture.lib.widget.RecyclerPreloadView
import vn.techres.line.R
import vn.techres.line.adapter.utils.MediaKeyboardAdapter
import vn.techres.line.interfaces.util.MediaKeyboardHandler

class MediaKeyboard(var mContext: Context?) : AXEmojiLayout(mContext),
    OnRecyclerViewPreloadMoreListener {
    private var mLoadingDialog: PictureLoadingDialog? = null
    private var rcMedia : RecyclerPreloadView? = null
    private var mediaKeyboardHandler : MediaKeyboardHandler? = null
    private var mediaKeyboardAdapter : MediaKeyboardAdapter? = null
    private var mOpenCameraCount = 0
    /**
     * if there more
     */
    private var isHasMore = true

    /**
     * page
     */
    private var mPage = 0
    companion object{
        const val MAX_PAGE_SIZE = 60
    }

    fun setMediaKeyboardHandler(mediaKeyboardHandler : MediaKeyboardHandler?){
        this.mediaKeyboardHandler = mediaKeyboardHandler
    }

    init {
        inflate(mContext, R.layout.media_keyboard_layout, this)
        rcMedia = findViewById(R.id.rcMedia)
        mediaKeyboardAdapter = MediaKeyboardAdapter(mContext)
        rcMedia?.addItemDecoration(GridSpacingItemDecoration(4, ScreenUtils.dip2px(mContext, 2f), false))
        rcMedia?.layoutManager = GridLayoutManager(mContext, 4)
        rcMedia?.adapter = AlphaInAnimationAdapter(mediaKeyboardAdapter)
        rcMedia?.setReachBottomRow(RecyclerPreloadView.BOTTOM_PRELOAD)
        rcMedia?.setOnRecyclerViewPreloadListener(this)
        mediaKeyboardAdapter?.setMediaKeyboardHandler(mediaKeyboardHandler)
        mediaKeyboardAdapter?.setDataSource(ArrayList())
        loadMoreData()
    }

    private fun loadMoreData() {
        if (mediaKeyboardAdapter != null) {
            if (isHasMore) {
                mPage++
 //                val bucketId = ValueOf.toLong(mTvPictureTitle.getTag(R.id.view_tag))
                val bucketId = ValueOf.toLong(0)
                LocalMediaPageLoader.getInstance(context)
                    .loadPageMediaData(bucketId, mPage, getPageLimit()
                    ) { result: List<LocalMedia?>, _: Int, isHasMore: Boolean ->
                        this.isHasMore = isHasMore
                        if (isHasMore) {
//                            hideDataNull()
                            val size = result.size
                            if (size > 0) {
                                val positionStart: Int = mediaKeyboardAdapter?.itemCount ?: 0
                                mediaKeyboardAdapter?.getData()?.addAll(result as ArrayList<LocalMedia>)
                                val itemCount: Int = mediaKeyboardAdapter?.itemCount ?: 0
                                mediaKeyboardAdapter?.notifyItemRangeChanged(positionStart, itemCount)
                            } else {
                                onRecyclerViewPreloadMore()
                            }
                            if (size < PictureConfig.MIN_PAGE_SIZE) {
                                rcMedia?.onScrolled(
                                    rcMedia?.scrollX ?: 0,
                                    rcMedia?.scrollY ?: 0
                                )
                            }
                        } else {
                            val isEmpty: Boolean = mediaKeyboardAdapter?.isDataEmpty() == true
                            if (isEmpty) {
//                                showDataNull(
//                                    if (bucketId == -1L) context.resources.getString(R.string.picture_empty) else context.resources.getString(
//                                        R.string.picture_data_null
//                                    ), R.drawable.picture_icon_no_data
//                                )
                            }
                        }
                    }
            }
        }
    }

    /**
     * getPageLimit
     * # If the user clicks to take a photo and returns, the Limit should be adjusted dynamically
     *
     * @return
     */
    private fun getPageLimit(): Int {
        val bucketId = ValueOf.toInt(-1)
        if (bucketId == -1) {
            val limit: Int =
                if (mOpenCameraCount > 0) MAX_PAGE_SIZE - mOpenCameraCount else MAX_PAGE_SIZE
            mOpenCameraCount = 0
            return limit
        }
        return MAX_PAGE_SIZE
    }

    /**
     * loading dialog
     */
    fun showPleaseDialog() {
        try {
            if (mLoadingDialog == null) {
                mLoadingDialog = PictureLoadingDialog(context)
            }
            if (mLoadingDialog?.isShowing == true) {
                mLoadingDialog?.dismiss()
            }
            mLoadingDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /**
     * get LocalMedia s
     */
    override fun onRecyclerViewPreloadMore() {
        loadMoreData()
    }
}