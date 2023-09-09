package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ActivityMediaSliderBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.getFileNameFromURL
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.FileUtils
import vn.techres.line.helper.utils.FileUtils.getMimeType
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


class MediaSliderActivity : BaseBindingActivity<ActivityMediaSliderBinding>() {
    private var startPosition = 0
    private var isMediaCountVisible = false
    private var urlList = ArrayList<String>()
    private var configNodeJs = ConfigNodeJs()
    private var pagerAdapter: PagerAdapter? = null

    private var type = ""
    override val bindingInflater: (LayoutInflater) -> ActivityMediaSliderBinding
        get() = ActivityMediaSliderBinding::inflate

    override fun onSetBodyView() {
        setData()
    }

    private fun setData() {
        urlList = Gson().fromJson(
            intent.getStringExtra(TechresEnum.DATA_MEDIA.toString()),
            object : TypeToken<ArrayList<String>>() {}.type
        )

        startPosition = intent.getIntExtra(TechresEnum.POSITION_MEDIA.toString(), 0)
        type = intent.getStringExtra("TYPE") ?: ""
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        pagerAdapter = ScreenSlidePagerAdapter(this, urlList)
        binding.pager.adapter = pagerAdapter
        setStartPosition()
        if (isMediaCountVisible) {
            binding.number.show()
            binding.number.text = String.format(
                "%s/%s",
                binding.pager.currentItem + 1,
                urlList.size
            )
        }

        binding.ivClose.setOnClickListener { onBackPressed() }

        binding.ivDownload.setOnClickListener {
            if (type == "chat") {
                val from = File(urlList[binding.pager.currentItem])
                val to = File(
                    Environment.getExternalStorageDirectory(),
                    Environment.DIRECTORY_DOWNLOADS.toString() + File.separator + FileUtils.FOLDER_NAME + File.separator + Utils.getNameFileToPath(
                        urlList[binding.pager.currentItem]
                    )
                )
                if (to.exists()) {
                    when (getMimeType(urlList[binding.pager.currentItem])) {
                        TechResEnumChat.TYPE_MP4.toString() -> {
                            Toast.makeText(
                                baseContext,
                                "Video đã có sẵn",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                baseContext,
                                "Ảnh đã có sẵn",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    try {
                        Toast.makeText(
                            baseContext, resources.getString(R.string.success_download),
                            Toast.LENGTH_SHORT
                        ).show()
                        copyFile(from, to)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } else {
                val downloadManager =
                    baseContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(urlList[binding.pager.currentItem])
                val request = DownloadManager.Request(uri)
                request.setTitle(getFileNameFromURL(urlList[binding.pager.currentItem]))
                request.setDescription(resources.getString(R.string.download))
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS.toString(), getFileNameFromURL(
                        urlList[binding.pager.currentItem]
                    )
                )
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                downloadManager.enqueue(request)
                Toast.makeText(
                    baseContext, resources.getString(R.string.downloading),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {
                //onPageScrolled
            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(i: Int) {
                binding.number.text = String.format(
                    "%s/%s",
                    binding.pager.currentItem + 1,
                    urlList.size
                )
            }

            override fun onPageScrollStateChanged(i: Int) {
                //onPageScrollStateChanged
            }
        })
    }

    private fun setStartPosition() {
        if (startPosition >= 0) {
            if (startPosition > urlList.size) {
                binding.pager.currentItem = urlList.size - 1
                return
            }
            binding.pager.currentItem = startPosition
        } else {
            binding.pager.currentItem = 0
        }
        binding.pager.offscreenPageLimit = 0
    }

    class ScreenSlidePagerAdapter() : PagerAdapter() {
        private var context: Context? = null
        private var urlList = ArrayList<String>()
        var player: SimpleExoPlayer? = null

        constructor(context: Context, urlList: ArrayList<String>) : this() {
            this.context = context
            this.urlList = urlList
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            try {
                val inflater = context?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view: View
                if (playerView != null) {
                    playerView?.player?.playWhenReady = false
                }
                if (getMimeType(urlList[position]) == "mp4") {
                    view = inflater.inflate(R.layout.item_video_holder, container, false)
                    playerView = view.findViewById(R.id.playerMedia)

                    if (playerView != null) {
                        playerView?.player?.playWhenReady = false
                    }

                    this.player = SimpleExoPlayer.Builder(context!!).build()
                    playerView?.let { startPlayingVideo(it, context!!, urlList[position]) }
                    playerView?.player?.playWhenReady = true
                } else {
                    view = inflater.inflate(R.layout.item_image_media, container, false)
                    val imageView = view.findViewById<ImageView>(R.id.mBigImage)
                    Glide.with(imageView).load(urlList[position]).centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .placeholder(R.drawable.logo_aloline_placeholder)
                        .error(R.drawable.logo_aloline_placeholder)
                        .into(imageView)
                }

                container.addView(view)
                return view
            } catch (e: Exception) {
                return super.instantiateItem(container, position)
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
//            currentPosition = playerView?.player?.currentPosition!!
            playerView?.player?.playWhenReady = false
        }

        override fun getCount(): Int {
            return urlList.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        private fun startPlayingVideo(
            playerView: PlayerView,
            ctx: Context,
            contentUrl: String
        ) {
            val trackSelector = DefaultTrackSelector(ctx)
            trackSelector.setParameters(
                trackSelector.buildUponParameters().setMaxVideoSizeSd()
            )
            player = SimpleExoPlayer.Builder(ctx)
                .setTrackSelector(trackSelector)
                .build()
            val userAgent: String = Util.getUserAgent(ctx, ctx.getString(R.string.default_image))
            val dataSourceFactory = DefaultDataSourceFactory(ctx, userAgent)
            val uriOfContentUrl: Uri = Uri.parse(contentUrl)
            val mediaSource: com.google.android.exoplayer2.source.MediaSource =
                ProgressiveMediaSource.Factory(
                    dataSourceFactory
                )
                    .createMediaSource(uriOfContentUrl)
            player?.prepare(mediaSource)
            player?.playWhenReady = true
            player?.addListener(listener)
            playerView.player = player
            playerView.player?.seekTo(0)

        }

        private val listener = object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {
                        //STATE_READY
                    }
                    ExoPlayer.STATE_IDLE -> {
                        //STATE_IDLE
                    }
                    Player.STATE_BUFFERING -> {
                        //STATE_BUFFERING
                    }
                    Player.STATE_ENDED -> {
                        //STATE_ENDED
                    }
                }
                super.onPlayerStateChanged(playWhenReady, playbackState)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (playerView != null) {
            playerView?.player?.playWhenReady = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (playerView != null) {
            playerView?.player?.playWhenReady = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (playerView != null) {
            playerView?.player?.playWhenReady = false
        }
    }

    override fun onBackPressed() {
        urlList = ArrayList()
        if (playerView != null) {
            playerView?.player?.playWhenReady = false
        }
        finish()
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var playerView: PlayerView? = null
    }

    @Throws(IOException::class)
    fun copyFile(source: File?, destination: File?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            android.os.FileUtils.copy(FileInputStream(source), FileOutputStream(destination))
        }
    }


}
