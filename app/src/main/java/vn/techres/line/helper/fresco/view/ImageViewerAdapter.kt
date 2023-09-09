package vn.techres.line.helper.fresco.view

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import vn.techres.line.R
import vn.techres.line.helper.fresco.adapter.RecyclingPagerAdapter
import vn.techres.line.helper.fresco.adapter.ViewHolder
import vn.techres.line.helper.fresco.model.MediaSource
import kotlin.math.ln


class ImageViewerAdapter() : RecyclingPagerAdapter<ViewHolder>() {
    private var context: Context? = null
    private var dataSet: ImageViewer.DataSet<MediaSource>? = null
    private var holders: HashSet<ImageViewHolder>? = null
    private var holderVideo: VideoViewHolder? = null
    private var imageRequestBuilder: ImageRequestBuilder? = null
    private var hierarchyBuilder: GenericDraweeHierarchyBuilder? = null
    private var isZoomingAllowed = false
    private val video = 1
    private val image = 0

    constructor(
        context: Context?, dataSet: ImageViewer.DataSet<MediaSource>?,
        imageRequestBuilder: ImageRequestBuilder?,
        hierarchyBuilder: GenericDraweeHierarchyBuilder?,
        isZoomingAllowed: Boolean
    ) : this() {
        this.context = context
        this.dataSet = dataSet
        holders = HashSet()
        this.imageRequestBuilder = imageRequestBuilder
        this.hierarchyBuilder = hierarchyBuilder
        this.isZoomingAllowed = isZoomingAllowed
    }


    fun getUrl(index: Int): String? {
        return dataSet?.format(index)
    }

    fun getItem(index: Int): MediaSource? {
        return dataSet?.getItem(index)
    }

    fun releaseVideo() {
        holderVideo.let {
            it?.playerView?.player?.release()
        }
    }

    fun stopVideo() {
        holderVideo.let {
            it?.playerView?.player?.playWhenReady = false
        }
    }

    fun seekBarVideo(): SeekBar? {
        return holderVideo?.sbMedia
    }

    fun play(): SimpleExoPlayer? {
        return holderVideo?.player
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

    override val itemCount: Int
        get() = dataSet!!.data.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return if (viewType == video) {
            val view =
                LayoutInflater.from(parent!!.context)
                    .inflate(R.layout.item_video_holder, parent, false)
            VideoViewHolder(view!!)
        } else {
            val view =
                LayoutInflater.from(parent!!.context)
                    .inflate(R.layout.item_zoom_image, parent, false)
            ImageViewHolder(view!!)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val media = dataSet!!.data[position]
        releaseVideo()
        if (getItemViewType(position) == image) {
            (holder as ImageViewHolder).bind(
                media,
                context
            )
        } else {
            holderVideo = (holder as VideoViewHolder)
            holderVideo?.bind(
                position,
                media,
                context
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet!!.data[position].type == 0) {
            image
        } else {
            video
        }
    }

    class ImageViewHolder(itemView: View) : ViewHolder(itemView) {
        private var imageView: TouchImageView = itemView.findViewById(R.id.mBigImage)
        fun bind(
            mediaSource: MediaSource,
            context: Context?
        ) {
            context?.let {
                Glide.with(it)
                    .load(
                        mediaSource.url
                    )
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerInside()
                    .apply(
                        RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                            .error(R.drawable.ic_image_placeholder)
                    )
                    .into(imageView)
            }
        }
    }

    class VideoViewHolder(itemView: View) : ViewHolder(itemView) {
        var position = -1
        private var maxVolume = 100
        var audioManager: AudioManager? = null
        var player: SimpleExoPlayer? = null
        var sbMedia: SeekBar? = null
        var playerView: PlayerView? = itemView.findViewById(R.id.playerMedia)
        private var currentPosition = 0.toLong()
        fun bind(
            position: Int,
            mediaSource: MediaSource,
            context: Context?
        ) {
            this.position = position
            if (context != null) {
                this.audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                this.player = SimpleExoPlayer.Builder(context).build()
            }
            playerView?.let { startPlayingVideo(it, context!!, mediaSource.url) }
            sbMedia = playerView?.findViewById(R.id.sbMedia)
            maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            sbMedia!!.max = maxVolume
            sbMedia!!.progress = maxVolume * audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC) / audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            sbMedia!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val volume = (1 - ln((maxVolume - progress).toDouble()) / ln(maxVolume.toFloat())).toFloat()
                    player?.volume = volume
                    player?.let { audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0) }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })
        }

        private fun startPlayingVideo(
            playerView: PlayerView,
            ctx: Context,
            context: String
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
            val uriOfContentUrl: Uri = Uri.parse(context)
            val mediaSource: com.google.android.exoplayer2.source.MediaSource =
                ProgressiveMediaSource.Factory(
                    dataSourceFactory
                )
                    .createMediaSource(uriOfContentUrl)
            player?.prepare(mediaSource)
            player?.playWhenReady = true
            player?.addListener(listener)
            playerView.player = player
            playerView.player?.seekTo(currentPosition)
        }

        private val listener = object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_READY -> {

                    }
                    ExoPlayer.STATE_IDLE -> {

                    }
                    Player.STATE_BUFFERING -> {

                    }
                    Player.STATE_ENDED -> {

                    }
                }
                super.onPlayerStateChanged(playWhenReady, playbackState)
            }
        }

        override fun detach(parent: ViewGroup) {
            currentPosition = playerView?.player?.currentPosition!!
            playerView?.player?.release()
            super.detach(parent)
        }
//        seekBarVideo()!!.progress = audioManager.getStreamVolume(play()!!.audioStreamType)
//        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
//            if (event != null) {
//                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP){
//                    val volume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC) + 10
//                    if(volume < 100){
//                        sbMedia!!.progress = audioManager!!.getStreamVolume(volume)
//                    }else{
//                        sbMedia!!.progress = audioManager!!.getStreamVolume(100)
//
//                    }
//                }
//                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
//                    val volume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC) - 10
//                    if(volume < 0){
//                        sbMedia!!.progress = audioManager!!.getStreamVolume(volume)
//                    }else{
//                        sbMedia!!.progress = audioManager!!.getStreamVolume(0)
//                    }
//                }
//            }
//            return true
//        }
    }

}