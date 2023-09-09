package vn.techres.line.adapter.comment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.like.LikeButton
import com.like.OnLikeListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kohii.v1.core.Playback
import kohii.v1.core.Scope
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.holder.newfeed.ItemCommentHolder
import vn.techres.line.data.model.LevelValue
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.reaction.ValueReaction
import vn.techres.line.databinding.ItemCommentBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.holder.newfeed.ItemDetailPostHolder
import vn.techres.line.interfaces.comment.CommentHandler
import vn.techres.line.interfaces.comment.ReplyCommentHandler
import java.util.regex.Matcher
import java.util.regex.Pattern

class CommentAdapter(val context: Context, var kohii: Kohii, var manager: kohii.v1.core.Manager, val width: Int, val height: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var postList = ArrayList<Comment>()
    private var dataDetailPost = PostReview()
    private var commentHandler: CommentHandler? = null
    private var replyCommentAdapter: ReplyCommentAdapter? = null
    private var replyCommentHandler: ReplyCommentHandler? = null
    private var user = CurrentUser.getCurrentUser(context)
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    private var listValueReaction = ArrayList<ValueReaction>()
    private var valueLike = 0
    private var valueExciting = 0
    private var valueSad = 0
    private var valueValue = 0
    private var valueNegative = 0
    private var valueNothing = 0

    private var listLevelValue = ArrayList<LevelValue>()
    private var listSaveBranchID = ArrayList<Int>()

    var playback: Playback? = null
    var volumeInfo: VolumeInfo? = null
    private var muteVolume = true

    private var youTubePlayerPause: YouTubePlayer? = null
    private var youTubePlayer: YouTubePlayer? = null
    private var currentTimeYoutube = 0f
    private var currentVideoId = ""
    private var updateReplyComment = true


    fun setDataSource(postList: ArrayList<Comment>) {
        this.postList = postList
        notifyDataSetChanged()
    }

    fun setDataDetailPost(dataDetailPost: PostReview) {
        this.dataDetailPost = dataDetailPost
    }

    fun setListSaveBranchID(listSaveBranchID: ArrayList<Int>) {
        this.listSaveBranchID = listSaveBranchID
    }

    fun setEventComment(commentHandler: CommentHandler) {
        this.commentHandler = commentHandler
    }

    fun setEventReplyComment(replyCommentHandler: ReplyCommentHandler) {
        this.replyCommentHandler = replyCommentHandler
    }

    fun setUpdateReplyComment(){
        updateReplyComment = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            detailPost -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_detail_post_comment, parent, false
                )
                ItemDetailPostHolder(view)
            }
            else -> {
                ItemCommentHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                detailPost
            }
            else -> {
                comment
            }
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            detailPost -> {
                onBinDetailPost(holder as ItemDetailPostHolder)
            }
            else -> {
                replyCommentAdapter = ReplyCommentAdapter(context)
                (holder as ItemCommentHolder).bin(postList[position], position, context, user, configNodeJs,
                    replyCommentAdapter!!, commentHandler!!, replyCommentHandler!!)
            }
        }

    }

    private fun onBinDetailPost(holder: ItemDetailPostHolder) {
        getImage(holder.imgAvatar, dataDetailPost.customer.avatar.thumb, configNodeJs)
        holder.tvName.text = dataDetailPost.customer.full_name
        if (dataDetailPost.created_at != null) {
            holder.tvTimeCreate.text = TimeFormatHelper.timeAgoString(dataDetailPost.created_at)
        }

        //show see more
        if (dataDetailPost.title!!.trim().isEmpty() && dataDetailPost.content!!.trim().isEmpty()){
            holder.viewLineHeader.visibility = View.GONE
        }else{
            holder.viewLineHeader.visibility = View.VISIBLE
        }
        var checkContentSeeMore = true
        if (dataDetailPost.title!!.trim().isEmpty()) {
            holder.tvTitle.visibility = View.GONE
        } else {
            holder.tvTitle.visibility = View.VISIBLE
            holder.tvTitle.text = dataDetailPost.title!!.trim()
        }

        if (dataDetailPost.content!!.trim().isEmpty()) {
            holder.tvContent.visibility = View.GONE
        } else {
            holder.tvContent.visibility = View.VISIBLE
            if (dataDetailPost.content?.trimIndent()!!.length >= 300) {
                val spannableContent = SpannableString(
                    String.format(
                        "%s%s",
                        dataDetailPost.content!!.trimIndent().substring(0, 300),
                        "...Xem thêm"
                    )
                )
                spannableContent.setSpan(
                    ForegroundColorSpan(context.getColor(R.color.blue_logo)),
                    300, // start
                    311, // end
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                holder.tvContent.text = spannableContent
                checkContentSeeMore = true
            } else {
                holder.tvContent.text = dataDetailPost.content!!.trim()
            }
        }

        //click see more content
        holder.tvContent.setOnClickListener {
            if (dataDetailPost.content!!.trimIndent().length >= 300) {
                if (checkContentSeeMore) {
                    val spannableContent = SpannableString(
                        String.format(
                            "%s %s",
                            dataDetailPost.content!!.trim(),
                            "Thu gọn"
                        )
                    )
                    spannableContent.setSpan(
                        ForegroundColorSpan(context.getColor(R.color.blue_logo)),
                        dataDetailPost.content!!.trim().length, // start
                        dataDetailPost.content!!.trim().length + 8, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    holder.tvContent.text = spannableContent
                    checkContentSeeMore = false
                } else {
                    val spannableContent = SpannableString(
                        String.format(
                            "%s%s",
                            dataDetailPost.content!!.trimIndent().substring(0, 300),
                            "...Xem thêm"
                        )
                    )
                    spannableContent.setSpan(
                        ForegroundColorSpan(context.getColor(R.color.blue_logo)),
                        300, // start
                        311, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    holder.tvContent.text = spannableContent
                    checkContentSeeMore = true
                }
            }
        }

        //Copy text
        holder.tvTitle.setOnLongClickListener {
            copyText(holder.tvTitle, dataDetailPost.title!!)
            return@setOnLongClickListener true
        }
        holder.tvContent.setOnLongClickListener {
            copyText(holder.tvContent, dataDetailPost.content!!)
            return@setOnLongClickListener true
        }

        // Link
        if (dataDetailPost.url_json_content.url != "" && !dataDetailPost.url!!.contains("youtu")) {
            holder.rlLinkPreview.visibility = View.VISIBLE
            if (dataDetailPost.url_json_content.image != "") {
                holder.imgPreview.visibility = View.VISIBLE
                //glide
                Glide.with(context)
                    .load(dataDetailPost.url_json_content.image)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .into(holder.imgPreview)
            } else {
                holder.imgPreview.visibility = View.GONE
            }

            if (dataDetailPost.url_json_content.canonical_url != "") {
                holder.txtSiteLink.visibility = View.VISIBLE
                holder.txtSiteLink.text = dataDetailPost.url_json_content.canonical_url
            } else {
                holder.txtSiteLink.visibility = View.GONE
            }

            if (dataDetailPost.url_json_content.title != "") {
                holder.txtTitleLink.visibility = View.VISIBLE
                holder.txtTitleLink.text = dataDetailPost.url_json_content.title
            } else {
                holder.txtTitleLink.visibility = View.GONE
            }

            if (dataDetailPost.url_json_content.description != ""){
                holder.txtDescLink.visibility = View.VISIBLE
                holder.txtDescLink.text = dataDetailPost.url_json_content.description
            }else{
                holder.txtDescLink.visibility = View.GONE
            }

            holder.rlLinkPreview.setOnClickListener {
                commentHandler!!.onLink(dataDetailPost.url_json_content.url ?: "")
            }
        } else {
            holder.rlLinkPreview.visibility = View.GONE
        }

        //YouTube
        if (dataDetailPost.media_contents.isEmpty() && dataDetailPost.url!!.contains("https://www.youtube.com/watch?v") || dataDetailPost.url!!.contains("https://youtu.be/"))
        {
            currentVideoId = getVideoIdYouTube(dataDetailPost.url!!)
            holder.youtubePlayerView.visibility = View.VISIBLE
            holder.youtubePlayerView.addYouTubePlayerListener(object :
                AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    super.onReady(youTubePlayer)
                    this@CommentAdapter.youTubePlayer = youTubePlayer
                    this@CommentAdapter.youTubePlayer?.cueVideo(currentVideoId, 0f)
                }
                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    super.onStateChange(youTubePlayer, state)
                    if (state == PlayerConstants.PlayerState.PLAYING) {
                        youTubePlayerPause = youTubePlayer
                    }
//                    holder.youtubePlayerView.getPlayerUiController()
//                        .setFullScreenButtonClickListener {
//                            val youtube = YouTube()
//                            youtube.videoId = currentVideoId
//                            youtube.currentSecond = currentTimeYoutube
//                            commentHandler!!.onShowFullVideoYouTube(youtube)
//                        }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)
                    currentTimeYoutube = second
                }
            })
        } else {
            holder.youtubePlayerView.visibility = View.GONE
        }


        //Media
        val arrayImageVideo = dataDetailPost.media_contents
        when (arrayImageVideo.size) {
            0 -> {
                kohii.setUp("").bind(holder.playerView)
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE
            }
            1 -> {
                holder.lnMediaOne.visibility = View.VISIBLE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE
                if (arrayImageVideo[0].type == mediaVideoType) {
                    holder.lnVideo.visibility = View.VISIBLE
                    holder.imgOneTypeOne.visibility = View.GONE
                    holder.lnVideo.layoutParams.width = width
                    if (dataDetailPost.media_contents[0].ratio > 1.5){
                        holder.lnVideo.layoutParams.height = (width * 1.5).toInt()
                    }else{
                        holder.lnVideo.layoutParams.height = (width * dataDetailPost.media_contents[0].ratio).toInt()
                    }
                    getGlide(holder.thumbnailVideo, arrayImageVideo[0].original!!, configNodeJs)
                    kohii.setUp(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            arrayImageVideo[0].original!!
                        )
                    )
                        .bind(holder.playerView)
                        {
                            playback = it
                            volumeInfo = it.volumeInfo

                            if (muteVolume) {
                                muteVolume = false
                                volumeInfo = VolumeInfo(true, 1f)
                                kohii.applyVolumeInfo(volumeInfo!!, playback!!, Scope.GLOBAL)
                            }

                            if (volumeInfo!!.mute)
                                holder.btnVolume.setImageResource(R.drawable.ic_volume_off)
                            else
                                holder.btnVolume.setImageResource(R.drawable.ic_volume_on)
                        }

                    holder.btnVolume.setOnClickListener {
                        if (volumeInfo!!.mute) {
                            volumeInfo = VolumeInfo(false, 1f)
                            holder.btnVolume.setImageResource(R.drawable.ic_volume_on)
                        } else {
                            volumeInfo = VolumeInfo(true, 1f)
                            holder.btnVolume.setImageResource(R.drawable.ic_volume_off)

                        }
                        kohii.applyVolumeInfo(volumeInfo!!, playback!!, Scope.GLOBAL)
                    }
                } else {
                    holder.lnVideo.visibility = View.GONE
                    holder.imgOneTypeOne.visibility = View.VISIBLE
                    holder.imgOneTypeOne.layoutParams.width = width
                    if (dataDetailPost.media_contents[0].ratio > 1.5){
                        holder.imgOneTypeOne.layoutParams.height = (width * 1.5).toInt()
                    }else{
                        holder.imgOneTypeOne.layoutParams.height = (width * dataDetailPost.media_contents[0].ratio).toInt()
                    }
                    kohii.setUp("").bind(holder.playerView)
                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                configNodeJs.api_ads,
                                arrayImageVideo[0].original!!
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                        )
                        .into(holder.imgOneTypeOne)
                }

                holder.lnMediaOne.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 0)
                }
            }
            2 -> {
                kohii.setUp("").bind(holder.playerView)
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.VISIBLE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE
                if (arrayImageVideo[0].type == mediaVideoType) {
                    holder.imgOneTypeTwoPlay.visibility = View.VISIBLE
                    getGlide(holder.imgOneTypeTwo, arrayImageVideo[0].original!!, configNodeJs)
                } else {
                    holder.imgOneTypeTwoPlay.visibility = View.GONE
                    getGlide(holder.imgOneTypeTwo, arrayImageVideo[0].original!!, configNodeJs)
                }
                if (arrayImageVideo[1].type == mediaVideoType) {
                    holder.imgTwoTypeTwoPlay.visibility = View.VISIBLE
                    getGlide(holder.imgTwoTypeTwo, arrayImageVideo[1].original!!, configNodeJs)
                } else {
                    holder.imgTwoTypeTwoPlay.visibility = View.GONE
                    getGlide(holder.imgTwoTypeTwo, arrayImageVideo[1].original!!, configNodeJs)
                }

                holder.imgOneTypeTwo.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 0)
                }
                holder.imgTwoTypeTwo.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 1)
                }
            }
            3 -> {
                kohii.setUp("").bind(holder.playerView)
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.VISIBLE
                holder.lnMediaFour.visibility = View.GONE
                holder.lnMediaFive.visibility = View.GONE

                if (arrayImageVideo[0].type == mediaVideoType) {
                    holder.imgOneTypeThreePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgOneTypeThree,
                        arrayImageVideo[0].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgOneTypeThreePlay.visibility = View.GONE
                    getGlide(holder.imgOneTypeThree, arrayImageVideo[0].original!!, configNodeJs)
                }

                if (arrayImageVideo[1].type == mediaVideoType) {
                    holder.imgTwoTypeThreePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgTwoTypeThree,
                        arrayImageVideo[1].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgTwoTypeThreePlay.visibility = View.GONE
                    getGlide(holder.imgTwoTypeThree, arrayImageVideo[1].original!!, configNodeJs)
                }

                if (arrayImageVideo[2].type == mediaVideoType) {
                    holder.imgThreeTypeThreePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgThreeTypeThree,
                        arrayImageVideo[2].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgThreeTypeThreePlay.visibility = View.GONE
                    getGlide(
                        holder.imgThreeTypeThree,
                        arrayImageVideo[2].original!!,
                        configNodeJs
                    )
                }

                holder.imgOneTypeThree.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 0)
                }
                holder.imgTwoTypeThree.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 1)
                }
                holder.imgThreeTypeThree.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 2)
                }
            }
            4 -> {
                kohii.setUp("").bind(holder.playerView)
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.VISIBLE
                holder.lnMediaFive.visibility = View.GONE

                if (arrayImageVideo[0].type == mediaVideoType) {
                    holder.imgOneTypeFourPlay.visibility = View.VISIBLE
                    getGlide(holder.imgOneTypeFour, arrayImageVideo[0].original!!, configNodeJs)
                } else {
                    holder.imgOneTypeFourPlay.visibility = View.GONE
                    getGlide(holder.imgOneTypeFour, arrayImageVideo[0].original!!, configNodeJs)
                }

                if (arrayImageVideo[1].type == mediaVideoType) {
                    holder.imgTwoTypeFourPlay.visibility = View.VISIBLE
                    getGlide(holder.imgTwoTypeFour, arrayImageVideo[1].original!!, configNodeJs)
                } else {
                    holder.imgTwoTypeFourPlay.visibility = View.GONE
                    getGlide(holder.imgTwoTypeFour, arrayImageVideo[1].original!!, configNodeJs)
                }

                if (arrayImageVideo[2].type == mediaVideoType) {
                    holder.imgThreeTypeFourPlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgThreeTypeFour,
                        arrayImageVideo[2].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgThreeTypeFourPlay.visibility = View.GONE
                    getGlide(holder.imgThreeTypeFour, arrayImageVideo[2].original!!, configNodeJs)
                }

                if (arrayImageVideo[3].type == mediaVideoType) {
                    holder.imgFourTypeFourPlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgFourTypeFour,
                        arrayImageVideo[3].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgFourTypeFourPlay.visibility = View.GONE
                    getGlide(holder.imgFourTypeFour, arrayImageVideo[3].original!!, configNodeJs)
                }

                holder.imgOneTypeFour.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 0)
                }
                holder.imgTwoTypeFour.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 1)
                }
                holder.imgThreeTypeFour.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 2)
                }
                holder.imgFourTypeFour.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 3)
                }
            }
            5 -> {
                kohii.setUp("").bind(holder.playerView)
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.tvMoreImage.visibility = View.GONE
                holder.lnMediaFive.visibility = View.VISIBLE

                if (arrayImageVideo[0].type == mediaVideoType) {
                    holder.imgOneTypeFivePlay.visibility = View.VISIBLE
                    getGlide(holder.imgOneTypeFive, arrayImageVideo[0].original!!, configNodeJs)
                } else {
                    holder.imgOneTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgOneTypeFive, arrayImageVideo[0].original!!, configNodeJs)
                }

                if (arrayImageVideo[1].type == mediaVideoType) {
                    holder.imgTwoTypeFivePlay.visibility = View.VISIBLE
                    getGlide(holder.imgTwoTypeFive, arrayImageVideo[1].original!!, configNodeJs)
                } else {
                    holder.imgTwoTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgTwoTypeFive, arrayImageVideo[1].original!!, configNodeJs)
                }

                if (arrayImageVideo[2].type == mediaVideoType) {
                    holder.imgThreeTypeFivePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgThreeTypeFive,
                        arrayImageVideo[2].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgThreeTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgThreeTypeFive, arrayImageVideo[2].original!!, configNodeJs)
                }

                if (arrayImageVideo[3].type == mediaVideoType) {
                    holder.imgFourTypeFivePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgFourTypeFive,
                        arrayImageVideo[3].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgFourTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgFourTypeFive, arrayImageVideo[3].original!!, configNodeJs)
                }

                if (arrayImageVideo[4].type == mediaVideoType) {
                    holder.imgFiveTypeFivePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgFiveTypeFive,
                        arrayImageVideo[4].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgFiveTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgFiveTypeFive, arrayImageVideo[4].original!!, configNodeJs)
                }

                holder.imgOneTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 0)
                }
                holder.imgTwoTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 1)
                }
                holder.imgThreeTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 2)
                }
                holder.imgFourTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 3)
                }
                holder.imgFiveTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 4)
                }
            }
            else -> {
                kohii.setUp("").bind(holder.playerView)
                holder.lnMediaOne.visibility = View.GONE
                holder.lnMediaTwo.visibility = View.GONE
                holder.lnMediaThree.visibility = View.GONE
                holder.lnMediaFour.visibility = View.GONE
                holder.tvMoreImage.visibility = View.VISIBLE
                holder.lnMediaFive.visibility = View.VISIBLE
                holder.tvMoreImage.text = String.format("%s %s", "+", arrayImageVideo.size - 5)

                if (arrayImageVideo[0].type == mediaVideoType) {
                    holder.imgOneTypeFivePlay.visibility = View.VISIBLE
                    getGlide(holder.imgOneTypeFive, arrayImageVideo[0].original!!, configNodeJs)
                } else {
                    holder.imgOneTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgOneTypeFive, arrayImageVideo[0].original!!, configNodeJs)
                }

                if (arrayImageVideo[1].type == mediaVideoType) {
                    holder.imgTwoTypeFivePlay.visibility = View.VISIBLE
                    getGlide(holder.imgTwoTypeFive, arrayImageVideo[1].original!!, configNodeJs)
                } else {
                    holder.imgTwoTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgTwoTypeFive, arrayImageVideo[1].original!!, configNodeJs)
                }

                if (arrayImageVideo[2].type == mediaVideoType) {
                    holder.imgThreeTypeFivePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgThreeTypeFive,
                        arrayImageVideo[2].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgThreeTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgThreeTypeFive, arrayImageVideo[2].original!!, configNodeJs)
                }

                if (arrayImageVideo[3].type == mediaVideoType) {
                    holder.imgFourTypeFivePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgFourTypeFive,
                        arrayImageVideo[3].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgFourTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgFourTypeFive, arrayImageVideo[3].original!!, configNodeJs)
                }

                if (arrayImageVideo[4].type == mediaVideoType) {
                    holder.imgFiveTypeFivePlay.visibility = View.VISIBLE
                    getGlide(
                        holder.imgFiveTypeFive,
                        arrayImageVideo[4].original!!,
                        configNodeJs
                    )
                } else {
                    holder.imgFiveTypeFivePlay.visibility = View.GONE
                    getGlide(holder.imgFiveTypeFive, arrayImageVideo[4].original!!, configNodeJs)
                }


                holder.imgOneTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 0)
                }
                holder.imgTwoTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 1)
                }
                holder.imgThreeTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 2)
                }
                holder.imgFourTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 3)
                }
                holder.imgFiveTypeFive.setOnClickListener {
                    commentHandler!!.onMedia(arrayImageVideo, 4)
                }
            }
        }

        //Review branch detail
        if (dataDetailPost.type == 1) {
            holder.lnBranch.visibility = View.VISIBLE
            if (dataDetailPost.branch != null) {
                holder.tvRatingReview.text = String.format(
                    "%.1f",
                    (dataDetailPost.service_rate!!.toFloat() + dataDetailPost.food_rate!!.toFloat() + dataDetailPost.price_rate!!.toFloat() + dataDetailPost.space_rate!!.toFloat() + dataDetailPost.hygiene_rate!!.toFloat()) / 5
                )
                Utils.getGlide(
                    holder.imgBranchLogo,
                    dataDetailPost.branch.logo_image_url?.original,
                    configNodeJs
                )
                holder.txtBranchName.text = dataDetailPost.branch.name
                holder.reviewRating.rating =
                    dataDetailPost.branch.rate_detail!!.average_rate!!.toFloat()
                holder.txtBranchVotes.text = String.format(
                    "%s %s %s %s",
                    String.format(
                        "%.1f",
                        dataDetailPost.branch.rate_detail!!.average_rate!!
                    ),
                    "/",
                    dataDetailPost.branch.rate_detail!!.rate_count.toString(),
                    context.resources.getString(
                        R.string.review
                    )
                )
                holder.txtBranchAddress.text = dataDetailPost.branch.address_full_text

                if (CacheManager.getInstance()
                        .get(TechresEnum.LIST_SAVE_BRANCH.toString()) != null
                ) {
                    listSaveBranchID = Gson().fromJson(
                        CacheManager.getInstance().get(TechresEnum.LIST_SAVE_BRANCH.toString()),
                        object :
                            TypeToken<ArrayList<Int>>() {}.type
                    )
                    holder.lbSaveBranch.isLiked =
                        listSaveBranchID.contains(dataDetailPost.branch.id)
                }

                holder.lnBranch.setOnClickListener {
                    commentHandler!!.onBranchDetail(dataDetailPost.branch.id)
                }

                holder.lnReviewRating.setOnClickListener {
                    commentHandler!!.onDetailRatingReview(
                        dataDetailPost.service_rate!!,
                        dataDetailPost.food_rate!!,
                        dataDetailPost.price_rate!!,
                        dataDetailPost.space_rate!!,
                        dataDetailPost.hygiene_rate!!
                    )
                }

                holder.lbSaveBranch.setOnLikeListener(object : OnLikeListener {
                    override fun liked(likeButton: LikeButton) {
                        dataDetailPost.branch!!.id?.let {
                            commentHandler!!.onSaveBranch(
                                it
                            )
                        }
                    }

                    override fun unLiked(likeButton: LikeButton) {
                        dataDetailPost.branch!!.id?.let {
                            commentHandler!!.onSaveBranch(
                                it
                            )
                        }
                    }
                })
            }
        } else {
            holder.lnBranch.visibility = View.GONE
        }

        /**
         * REACTION
         * 1 : Favourite
         * 2 : Exciting
         * 3 : Sad
         * 4 : Value
         * 5 : Negative
         * 6 : Notthing
         */
        if (CacheManager.getInstance().get(TechresEnum.VALUE_REACTION.toString()) != null) {
            listValueReaction = Gson().fromJson(
                CacheManager.getInstance().get(TechresEnum.VALUE_REACTION.toString()), object :
                    TypeToken<ArrayList<ValueReaction>>() {}.type
            )
        }

        if (listValueReaction.isNotEmpty()){
            valueLike = listValueReaction[5].value ?: 0
            valueExciting = listValueReaction[4].value ?: 0
            valueSad = listValueReaction[3].value ?: 0
            valueValue = listValueReaction[2].value ?: 0
            valueNegative = listValueReaction[1].value ?: 0
            valueNothing = listValueReaction[0].value ?: 0
        }

        holder.tvOneCount.text = dataDetailPost.reaction_one_count!!.toString()
        holder.tvTwoCount.text = dataDetailPost.reaction_two_count!!.toString()
        holder.tvThreeCount.text = dataDetailPost.reaction_three_count!!.toString()
        holder.tvFourCount.text = dataDetailPost.reaction_four_count!!.toString()
        holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
        holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()

        when (dataDetailPost.my_reaction_id) {
            1 -> {
                setTextColorReaction(
                    holder.tvReactionOne,
                    holder.tvReactionTwo,
                    holder.tvReactionThree,
                    holder.tvReactionFour,
                    holder.tvReactionFive,
                    holder.tvReactionSix
                )
            }
            2 -> {
                setTextColorReaction(
                    holder.tvReactionTwo,
                    holder.tvReactionOne,
                    holder.tvReactionThree,
                    holder.tvReactionFour,
                    holder.tvReactionFive,
                    holder.tvReactionSix
                )
            }
            3 -> {
                setTextColorReaction(
                    holder.tvReactionThree,
                    holder.tvReactionOne,
                    holder.tvReactionTwo,
                    holder.tvReactionFour,
                    holder.tvReactionFive,
                    holder.tvReactionSix
                )
            }
            4 -> {
                setTextColorReaction(
                    holder.tvReactionFour,
                    holder.tvReactionOne,
                    holder.tvReactionTwo,
                    holder.tvReactionThree,
                    holder.tvReactionFive,
                    holder.tvReactionSix
                )
            }
            5 -> {
                setTextColorReaction(
                    holder.tvReactionFive,
                    holder.tvReactionOne,
                    holder.tvReactionTwo,
                    holder.tvReactionThree,
                    holder.tvReactionFour,
                    holder.tvReactionSix
                )
            }
            6 -> {
                setTextColorReaction(
                    holder.tvReactionSix,
                    holder.tvReactionOne,
                    holder.tvReactionTwo,
                    holder.tvReactionThree,
                    holder.tvReactionFour,
                    holder.tvReactionFive
                )
            }
            else -> {
                setTextColorReactionNull(
                    holder.tvReactionOne,
                    holder.tvReactionTwo,
                    holder.tvReactionThree,
                    holder.tvReactionFour,
                    holder.tvReactionFive,
                    holder.tvReactionSix
                )
            }
        }

        //Set click reaction
        holder.lnReactionOne.setOnClickListener {
            when (dataDetailPost.my_reaction_id) {
                1 -> {
                    dataDetailPost.my_reaction_id = 0
                    dataDetailPost.reaction_one_count = dataDetailPost.reaction_one_count!! - 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! - valueLike

                    holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReactionNull(
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
                else -> {
                    when (dataDetailPost.my_reaction_id) {
                        2 -> {
                            dataDetailPost.reaction_two_count =
                                dataDetailPost.reaction_two_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueExciting

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        3 -> {
                            dataDetailPost.reaction_three_count =
                                dataDetailPost.reaction_three_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueSad

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        4 -> {
                            dataDetailPost.reaction_four_count =
                                dataDetailPost.reaction_four_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueValue

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        5 -> {
                            dataDetailPost.reaction_five_count =
                                dataDetailPost.reaction_five_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueNegative

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        6 -> {
                            dataDetailPost.reaction_six_count =
                                dataDetailPost.reaction_six_count!! - 1
                            dataDetailPost.reaction_point = dataDetailPost.reaction_point!!

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                    }
                    dataDetailPost.my_reaction_id = 1
                    dataDetailPost.reaction_one_count = dataDetailPost.reaction_one_count!! + 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! + valueLike
                    holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReaction(
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
            }
            commentHandler!!.onReaction(dataDetailPost._id!!, 1)
        }

        holder.lnReactionTwo.setOnClickListener {
            when (dataDetailPost.my_reaction_id) {
                2 -> {
                    dataDetailPost.my_reaction_id = 0
                    dataDetailPost.reaction_two_count = dataDetailPost.reaction_two_count!! - 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! - valueExciting

                    holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()

                    setTextColorReactionNull(
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
                else -> {
                    when (dataDetailPost.my_reaction_id) {
                        1 -> {
                            dataDetailPost.reaction_one_count =
                                dataDetailPost.reaction_one_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueLike

                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        3 -> {
                            dataDetailPost.reaction_three_count =
                                dataDetailPost.reaction_three_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueSad

                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        4 -> {
                            dataDetailPost.reaction_four_count =
                                dataDetailPost.reaction_four_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueValue

                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        5 -> {
                            dataDetailPost.reaction_five_count =
                                dataDetailPost.reaction_five_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueNegative

                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        6 -> {
                            dataDetailPost.reaction_six_count =
                                dataDetailPost.reaction_six_count!! - 1
//                            dataDetailPost.reaction_point = dataDetailPost.reaction_point

                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                    }
                    dataDetailPost.my_reaction_id = 2
                    dataDetailPost.reaction_two_count = dataDetailPost.reaction_two_count!! + 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! + valueExciting

                    holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReaction(
                        holder.tvReactionTwo,
                        holder.tvReactionOne,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
            }

            commentHandler!!.onReaction(dataDetailPost._id!!, 2)
        }

        holder.lnReactionThree.setOnClickListener {
            when (dataDetailPost.my_reaction_id) {
                3 -> {
                    dataDetailPost.my_reaction_id = 0
                    dataDetailPost.reaction_three_count = dataDetailPost.reaction_three_count!! - 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! - valueSad

                    holder.tvThreeCount.text = dataDetailPost.reaction_three_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()

                    setTextColorReactionNull(
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
                else -> {
                    when (dataDetailPost.my_reaction_id) {
                        1 -> {
                            dataDetailPost.reaction_one_count =
                                dataDetailPost.reaction_one_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueLike

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        2 -> {
                            dataDetailPost.reaction_two_count =
                                dataDetailPost.reaction_two_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueExciting

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        4 -> {
                            dataDetailPost.reaction_four_count =
                                dataDetailPost.reaction_four_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueValue

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        5 -> {
                            dataDetailPost.reaction_five_count =
                                dataDetailPost.reaction_five_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueNegative

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        6 -> {
                            dataDetailPost.reaction_six_count =
                                dataDetailPost.reaction_six_count!! - 1
                            //dataDetailPost.reaction_point = dataDetailPost.reaction_point

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                    }
                    dataDetailPost.my_reaction_id = 3
                    dataDetailPost.reaction_three_count = dataDetailPost.reaction_three_count!! + 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! + valueSad

                    holder.tvThreeCount.text = dataDetailPost.reaction_three_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReaction(
                        holder.tvReactionThree,
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
            }
            commentHandler!!.onReaction(dataDetailPost._id!!, 3)
        }

        holder.lnReactionFour.setOnClickListener {
            when (dataDetailPost.my_reaction_id) {
                4 -> {
                    dataDetailPost.my_reaction_id = 0
                    dataDetailPost.reaction_four_count = dataDetailPost.reaction_four_count!! - 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! - valueValue

                    holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReactionNull(
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
                else -> {
                    when (dataDetailPost.my_reaction_id) {
                        1 -> {
                            dataDetailPost.reaction_one_count =
                                dataDetailPost.reaction_one_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueLike

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        2 -> {
                            dataDetailPost.reaction_two_count =
                                dataDetailPost.reaction_two_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueExciting

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        3 -> {
                            dataDetailPost.reaction_three_count =
                                dataDetailPost.reaction_three_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueSad

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        5 -> {
                            dataDetailPost.reaction_five_count =
                                dataDetailPost.reaction_five_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueNegative

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        6 -> {
                            dataDetailPost.reaction_six_count =
                                dataDetailPost.reaction_six_count!! - 1
                            //dataDetailPost.reaction_point = dataDetailPost.reaction_point

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                    }
                    dataDetailPost.my_reaction_id = 4
                    dataDetailPost.reaction_four_count = dataDetailPost.reaction_four_count!! + 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! + valueValue

                    holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReaction(
                        holder.tvReactionFour,
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
            }
            commentHandler!!.onReaction(dataDetailPost._id!!, 4)
        }

        holder.lnReactionFive.setOnClickListener {
            when (dataDetailPost.my_reaction_id) {
                5 -> {
                    dataDetailPost.my_reaction_id = 0
                    dataDetailPost.reaction_five_count = dataDetailPost.reaction_five_count!! - 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! - valueNegative

                    holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReactionNull(
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
                else -> {
                    when (dataDetailPost.my_reaction_id) {
                        1 -> {
                            dataDetailPost.reaction_one_count =
                                dataDetailPost.reaction_one_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueLike

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        2 -> {
                            dataDetailPost.reaction_two_count =
                                dataDetailPost.reaction_two_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueExciting

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        3 -> {
                            dataDetailPost.reaction_three_count =
                                dataDetailPost.reaction_three_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueSad

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        4 -> {
                            dataDetailPost.reaction_four_count =
                                dataDetailPost.reaction_four_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueValue

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        6 -> {
                            dataDetailPost.reaction_six_count =
                                dataDetailPost.reaction_six_count!! - 1
                            //dataDetailPost.reaction_point = dataDetailPost.reaction_point

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                    }
                    dataDetailPost.my_reaction_id = 5
                    dataDetailPost.reaction_five_count = dataDetailPost.reaction_five_count!! + 1
                    dataDetailPost.reaction_point = dataDetailPost.reaction_point!! + valueNegative

                    holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReaction(
                        holder.tvReactionFive,
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionSix
                    )
                }
            }
            commentHandler!!.onReaction(dataDetailPost._id!!, 5)
        }

        holder.lnReactionSix.setOnClickListener {
            when (dataDetailPost.my_reaction_id) {
                6 -> {
                    dataDetailPost.my_reaction_id = 0
                    dataDetailPost.reaction_six_count = dataDetailPost.reaction_six_count!! - 1

                    holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReactionNull(
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive,
                        holder.tvReactionSix
                    )
                }
                else -> {
                    when (dataDetailPost.my_reaction_id) {
                        1 -> {
                            dataDetailPost.reaction_one_count =
                                dataDetailPost.reaction_one_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueLike

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        2 -> {
                            dataDetailPost.reaction_two_count =
                                dataDetailPost.reaction_two_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueExciting

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        3 -> {
                            dataDetailPost.reaction_three_count =
                                dataDetailPost.reaction_three_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueSad

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        4 -> {
                            dataDetailPost.reaction_four_count =
                                dataDetailPost.reaction_four_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueValue

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                        5 -> {
                            dataDetailPost.reaction_five_count =
                                dataDetailPost.reaction_five_count!! - 1
                            dataDetailPost.reaction_point =
                                dataDetailPost.reaction_point!! - valueNegative

                            holder.tvTwoCount.text = dataDetailPost.reaction_two_count.toString()
                            holder.tvOneCount.text = dataDetailPost.reaction_one_count.toString()
                            holder.tvThreeCount.text =
                                dataDetailPost.reaction_three_count.toString()
                            holder.tvFourCount.text = dataDetailPost.reaction_four_count.toString()
                            holder.tvFiveCount.text = dataDetailPost.reaction_five_count.toString()
                            holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                        }
                    }
                    dataDetailPost.my_reaction_id = 6
                    dataDetailPost.reaction_six_count = dataDetailPost.reaction_six_count!! + 1

                    holder.tvSixCount.text = dataDetailPost.reaction_six_count.toString()
                    holder.tvPointValue.text = dataDetailPost.reaction_point.toString()
                    setTextColorReaction(
                        holder.tvReactionSix,
                        holder.tvReactionOne,
                        holder.tvReactionTwo,
                        holder.tvReactionThree,
                        holder.tvReactionFour,
                        holder.tvReactionFive
                    )
                }
            }
            commentHandler!!.onReaction(dataDetailPost._id!!, 6)
        }

        //Value, level value
        holder.tvPointValue.text = dataDetailPost.reaction_point.toString()

        try {
            listLevelValue = Gson().fromJson(
                CacheManager.getInstance().get(TechresEnum.LEVER_VALUE.toString()), object :
                    TypeToken<ArrayList<LevelValue>>() {}.type
            )
        }catch (ex: Exception){}


        try {
            when (dataDetailPost.alo_point_bonus_level_id) {
                0 -> {
                    holder.tvLevelValueOne.text = String.format(
                        "%s: %s",
                        listLevelValue[0].name,
                        listLevelValue[0].point_target
                    )
                    holder.tvLevelValueTwo.text = String.format(
                        "%s: %s",
                        listLevelValue[1].name,
                        listLevelValue[1].point_target
                    )
                    holder.tvLevelValueThree.text = String.format(
                        "%s: %s",
                        listLevelValue[2].name,
                        listLevelValue[2].point_target
                    )
                    holder.tvLevelValueFour.text = String.format(
                        "%s: %s",
                        listLevelValue[3].name,
                        listLevelValue[3].point_target
                    )
                    holder.tvLevelValueOne.setBackgroundResource(R.color.gray_dark)
                    holder.tvLevelValueTwo.setBackgroundResource(R.color.gray_dark)
                    holder.tvLevelValueThree.setBackgroundResource(R.color.gray_dark)
                    holder.tvLevelValueFour.setBackgroundResource(R.color.gray_dark)
                }
                1 -> {
                    holder.tvLevelValueOne.text = String.format(
                        "%s: %s",
                        listLevelValue[0].name,
                        listLevelValue[0].point_target
                    )
                    holder.tvLevelValueTwo.text = String.format(
                        "%s: %s",
                        listLevelValue[1].name,
                        listLevelValue[1].point_target
                    )
                    holder.tvLevelValueThree.text = String.format(
                        "%s: %s",
                        listLevelValue[2].name,
                        listLevelValue[2].point_target
                    )
                    holder.tvLevelValueFour.text = String.format(
                        "%s: %s",
                        listLevelValue[3].name,
                        listLevelValue[3].point_target
                    )
                    holder.tvLevelValueOne.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueTwo.setBackgroundResource(R.color.gray_dark)
                    holder.tvLevelValueThree.setBackgroundResource(R.color.gray_dark)
                    holder.tvLevelValueFour.setBackgroundResource(R.color.gray_dark)
                }
                2 -> {
                    holder.tvLevelValueOne.text = String.format(
                        "%s: %s",
                        listLevelValue[0].name,
                        listLevelValue[0].point_target
                    )
                    holder.tvLevelValueTwo.text = String.format(
                        "%s: %s",
                        listLevelValue[1].name,
                        listLevelValue[1].point_target
                    )
                    holder.tvLevelValueThree.text = String.format(
                        "%s: %s",
                        listLevelValue[2].name,
                        listLevelValue[2].point_target
                    )
                    holder.tvLevelValueFour.text = String.format(
                        "%s: %s",
                        listLevelValue[3].name,
                        listLevelValue[3].point_target
                    )
                    holder.tvLevelValueOne.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueTwo.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueThree.setBackgroundResource(R.color.gray_dark)
                    holder.tvLevelValueFour.setBackgroundResource(R.color.gray_dark)
                }
                3 -> {
                    holder.tvLevelValueOne.text = String.format(
                        "%s: %s",
                        listLevelValue[0].name,
                        listLevelValue[0].point_target
                    )
                    holder.tvLevelValueTwo.text = String.format(
                        "%s: %s",
                        listLevelValue[1].name,
                        listLevelValue[1].point_target
                    )
                    holder.tvLevelValueThree.text = String.format(
                        "%s: %s",
                        listLevelValue[2].name,
                        listLevelValue[2].point_target
                    )
                    holder.tvLevelValueFour.text = String.format(
                        "%s: %s",
                        listLevelValue[3].name,
                        listLevelValue[3].point_target
                    )
                    holder.tvLevelValueOne.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueTwo.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueThree.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueFour.setBackgroundResource(R.color.gray_dark)
                }
                else -> {
                    holder.tvLevelValueOne.text = String.format(
                        "%s: %s",
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!! - 3].name,
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!! - 3].point_target
                    )
                    holder.tvLevelValueTwo.text = String.format(
                        "%s: %s",
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!! - 2].name,
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!! - 2].point_target
                    )
                    holder.tvLevelValueThree.text = String.format(
                        "%s: %s",
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!! - 1].name,
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!! - 1].point_target
                    )
                    holder.tvLevelValueFour.text = String.format(
                        "%s: %s",
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!!].name,
                        listLevelValue[dataDetailPost.alo_point_bonus_level_id!!].point_target
                    )
                    holder.tvLevelValueOne.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueTwo.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueThree.setBackgroundResource(R.color.blue_logo)
                    holder.tvLevelValueFour.setBackgroundResource(R.color.gray_dark)
                }
            }
        } catch (Ex: Exception) {
        }
        //Comment
        holder.tvCommentCount.text = String.format(
            "%s %s", dataDetailPost.comment_count.toString(), context.resources.getString(
                R.string.comment
            )
        )

        //Set click
        // Set buttom Share
        holder.lnShare.setOnClickListener {
            commentHandler!!.onShare()
        }
        // Set buttom more
        holder.imgMore.setOnClickListener {
            commentHandler!!.onButtonMore()
        }

        holder.imgAvatar.setOnClickListener {
            commentHandler!!.onAvatar(dataDetailPost.customer.avatar.original ?: "")
        }

        holder.lnNamePost.setOnClickListener {
            commentHandler!!.onClickProfile(dataDetailPost.customer.member_id ?: 0)
        }

        holder.lnValue.setOnClickListener {
            commentHandler!!.onReactionDetail()
        }

        holder.tvCommentCount.setOnClickListener {
            commentHandler!!.onComment()
        }
    }

    private fun setTextColorReaction(
        textChoose: TextView,
        textOne: TextView,
        textTwo: TextView,
        textThree: TextView,
        textFour: TextView,
        textFive: TextView
    ) {
        textChoose.setTextColor(ContextCompat.getColor(context, R.color.main_bg))
        textOne.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textTwo.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textThree.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textFour.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textFive.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
    }

    private fun setTextColorReactionNull(
        textOne: TextView,
        textTwo: TextView,
        textThree: TextView,
        textFour: TextView,
        textFive: TextView,
        textSix: TextView
    ) {
        textOne.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textTwo.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textThree.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textFour.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textFive.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
        textSix.setTextColor(ContextCompat.getColor(context, R.color.gray_text_title))
    }

    private fun copyText(view: View, data: String) {
        val popup = PopupMenu(context, view)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.menu_copy, popup.menu)

        popup.setOnMenuItemClickListener {
            if (it.itemId == R.id.copy) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip: ClipData = ClipData.newPlainText("label", data)
                clipboard!!.setPrimaryClip(clip)
                Toast.makeText(
                    context,
                    context.getString(R.string.copied_clipboard),
                    Toast.LENGTH_SHORT
                ).show()
            }
            true
        }

        popup.show()//showing popup menu
    }

    private fun getVideoIdYouTube(youTubeUrl: String): String {
        val pattern =
            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
        val compiledPattern: Pattern = Pattern.compile(
            pattern,
            Pattern.CASE_INSENSITIVE
        )
        val matcher: Matcher = compiledPattern.matcher(youTubeUrl)
        return if (matcher.find()) {
            matcher.group(1)
        } else ""
    }

    companion object {
        private const val detailPost = 0
        private const val comment = 1
        private const val mediaVideoType = 0
    }
}