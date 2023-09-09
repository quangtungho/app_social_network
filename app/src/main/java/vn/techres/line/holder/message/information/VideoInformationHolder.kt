package vn.techres.line.holder.message.information

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ImageMoreChatHandler

class VideoInformationHolder(view : View) : RecyclerView.ViewHolder(view) {
    private var playerView: PlayerView = view.findViewById(R.id.playerViewMessage)
//    private var imgPlayerView: ImageView = view.findViewById(R.id.imgPlayerView)
    private var cvVideoContainer: CardView = view.findViewById(R.id.cvVideoContainer)
    var imgReplyAction: ImageView = view.findViewById(R.id.imgReplyAction)

    private var tvTimeVideo: TextView = view.findViewById(R.id.tvTimeVideo)
    private var tvNameVideo: TextView = view.findViewById(R.id.tvNameVideo)
    private var cvVideo: CardView = view.findViewById(R.id.cvVideo)
    private var imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)

    //reaction
    private var lnReaction: LinearLayout = view.findViewById(R.id.lnReaction)
    private var lnReactionContainer: LinearLayout = view.findViewById(R.id.lnReactionContainer)
    private var imgReactionOne: ImageView = view.findViewById(R.id.imgReactionOne)
    private var imgReactionTwo: ImageView = view.findViewById(R.id.imgReactionTwo)
    private var imgReactionThree: ImageView = view.findViewById(R.id.imgReactionThree)

    //press reaction
    private var imgReactionPress: ImageView = view.findViewById(R.id.imgReactionPress)

    //count
    private var tvReactionCount: TextView = view.findViewById(R.id.tvReactionCount)

    //today
    private var tvTimeHeader: TextView = view.findViewById(R.id.tvTimeHeader)

    fun bin(
        context: Context,
        user: User,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        imageMoreChatHandler: ImageMoreChatHandler?
    ){
        val message = dataSource[position]

        tvTimeVideo.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        tvNameVideo.text = message.sender.full_name
        getImage(imgAvatar, message.sender.avatar?.medium, configNodeJs)

        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSource.size) {
                if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id) {
                    tvNameVideo.visibility = View.GONE
                    cvVideo.visibility = View.INVISIBLE
                } else {
                    tvNameVideo.visibility = View.VISIBLE
                    cvVideo.visibility = View.VISIBLE
                }
            }
        } else {
            cvVideo.visibility = View.GONE
            tvNameVideo.visibility = View.GONE
        }
        if (message.today == 1) {
            tvTimeHeader.visibility = View.VISIBLE
            tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else tvTimeHeader.visibility = View.GONE

//        Glide.with(context)
//            .load(
//                String.format(
//                    "%s%s",
//                    nodeJs.api_ads,
//                    message.files[0].link_original
//                )
//            )
//            .diskCacheStrategy(DiskCacheStrategy.ALL)
//            .centerInside()
//            .apply(
//                RequestOptions().placeholder(R.drawable.ic_image_placeholder)
//                    .error(R.drawable.ic_image_placeholder)
//            )
//            .into(imgPlayerView)

        val trackSelector = DefaultTrackSelector(context)
        trackSelector.setParameters(
            trackSelector.buildUponParameters().setMaxVideoSizeSd()
        )
        val player = SimpleExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
        val userAgent: String = Util.getUserAgent(
            context,
            context.getString(R.string.default_image)
        )
        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        val uriOfContentUrl: Uri = Uri.parse(
            String.format(
                "%s%s",
                configNodeJs.api_ads,
                message.files[0].link_original
            )
        )
        val mediaSource: com.google.android.exoplayer2.source.MediaSource = ProgressiveMediaSource.Factory(
            dataSourceFactory
        )
            .createMediaSource(uriOfContentUrl)
        player.prepare(mediaSource)
        player.playWhenReady = false
        player.volume = 0F
        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_BUFFERING -> {

                    }
                    ExoPlayer.STATE_ENDED -> {
                        player.retry()
                    }
                    ExoPlayer.STATE_IDLE -> {

                    }
                    ExoPlayer.STATE_READY -> {
//                        imgPlayerView.visibility = View.VISIBLE
                    }
                }
            }


        })
        playerView.player = player
        if (message.reactions.reactions_count == 0) {
            lnReaction.visibility = View.GONE
            imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            imgReactionPress.visibility = View.VISIBLE
        } else {
//            ChatUtils.setReaction(
//                imgReactionOne,
//                imgReactionTwo,
//                imgReactionThree,
//                imgReactionPress,
//                lnReaction,
//                lnReactionContainer,
//                tvReactionCount,
//                message
//            )
        }
        imgReactionPress.setOnClickListener {
            chatGroupHandler!!.onPressReaction(message, it)
        }

        cvVideoContainer.setOnClickListener {
            imageMoreChatHandler!!.onVideoMore(message.files, 0)
        }
    }
}