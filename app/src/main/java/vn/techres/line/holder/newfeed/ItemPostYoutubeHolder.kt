package vn.techres.line.holder.newfeed

import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import kohii.v1.core.Manager
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import timber.log.Timber
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.data.model.LevelValue
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.data.model.reaction.ValueReaction
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemPostBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.newsfeed.NewsFeedHandler

class ItemPostYoutubeHolder(
    val binding: ItemPostBinding,
    var context: Context,
    var kohii: Kohii,
    var manager: Manager,
    val lifecycle: Lifecycle,
    val width: Int,
    val height: Int
) : RecyclerView.ViewHolder(binding.root) {

    private var listLevelValue = ArrayList<LevelValue>()
    private var listSaveBranchID = ArrayList<Int>()
    private var listValueReaction = ArrayList<ValueReaction>()
    private var valueLike = 0
    private var valueExciting = 0
    private var valueSad = 0
    private var valueValue = 0
    private var valueNegative = 0
    private var valueNothing = 0

    var playback: Playback? = null
    var volumeInfo: VolumeInfo? = null
    private var youTubePlayerPause: YouTubePlayer? = null
    private var currentTimeYoutube = 0f
    private var youTubePlayer: YouTubePlayer? = null
    private var currentVideoId = ""
    private val cacheManager: CacheManager = CacheManager.getInstance()

    fun bin(
        data: PostReview,
        position: Int,
        user: User,
        configNodeJs: ConfigNodeJs,
        newsFeedHandler: NewsFeedHandler
    ) {
        if (data.loading == 1) {
            binding.cvItemNewFeed.visibility = View.GONE
            binding.itemLoadPage.lnLoadPage.visibility = View.VISIBLE
        } else {
            binding.cvItemNewFeed.visibility = View.VISIBLE
            binding.itemLoadPage.lnLoadPage.visibility = View.GONE
            lifecycle.addObserver(binding.itemNewsFeedYouTube.youtubePlayerView)
            cueVideo(getVideoIdYouTube(data.url!!))
            //Set header
            if (data.customer.member_id == CurrentUser.getCurrentUser(context).id) {
                binding.itemNewsFeedHeader.imgMore.visibility = View.VISIBLE
            } else {
                binding.itemNewsFeedHeader.imgMore.visibility = View.GONE
            }
            Utils.getImage(
                binding.itemNewsFeedHeader.imgAvatar,
                data.customer.avatar.thumb ?: "",
                configNodeJs
            )
            binding.itemNewsFeedHeader.tvName.text = data.customer.full_name
            if (data.created_at != null) {
                binding.itemNewsFeedHeader.tvTimeCreate.text =
                    TimeFormatHelper.timeAgoString(data.created_at)
            }

            //show see more
            if (data.title!!.trim().isEmpty() && data.content!!.trim().isEmpty()) {
                binding.itemNewsFeedHeader.viewLineHeader.visibility = View.GONE
            } else {
                binding.itemNewsFeedHeader.viewLineHeader.visibility = View.VISIBLE
            }
            var checkContentSeeMore = true
            if (data.title!!.trim().isEmpty()) {
                binding.itemNewsFeedHeader.tvTitle.visibility = View.GONE
            } else {
                binding.itemNewsFeedHeader.tvTitle.visibility = View.VISIBLE
                binding.itemNewsFeedHeader.tvTitle.text = data.title!!.trim()
            }

            if (data.content!!.trim().isEmpty()) {
                binding.itemNewsFeedHeader.tvContent.visibility = View.GONE
            } else {
                binding.itemNewsFeedHeader.tvContent.visibility = View.VISIBLE
                if (data.content?.trimIndent()!!.length >= 300) {
                    val spannableContent = SpannableString(
                        String.format(
                            "%s%s",
                            data.content!!.trimIndent().substring(0, 300),
                            "...Xem thêm"
                        )
                    )
                    spannableContent.setSpan(
                        ForegroundColorSpan(context.getColor(R.color.blue_logo)),
                        300, // start
                        311, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    binding.itemNewsFeedHeader.tvContent.text = spannableContent
                    checkContentSeeMore = true
                } else {
                    binding.itemNewsFeedHeader.tvContent.text = data.content!!.trim()
                }
            }

            //click see more content
            binding.itemNewsFeedHeader.tvContent.setOnClickListener {
                if (data.content?.trimIndent()!!.length >= 300) {
                    if (checkContentSeeMore) {
                        val spannableContent = SpannableString(
                            String.format(
                                "%s %s",
                                data.content!!.trim(),
                                "Thu gọn"
                            )
                        )
                        spannableContent.setSpan(
                            ForegroundColorSpan(context.getColor(R.color.blue_logo)),
                            data.content!!.trim().length, // start
                            data.content!!.trim().length + 8, // end
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                        binding.itemNewsFeedHeader.tvContent.text = spannableContent
                        checkContentSeeMore = false
                    } else {
                        val spannableContent = SpannableString(
                            String.format(
                                "%s%s",
                                data.content!!.trimIndent().substring(0, 300),
                                "...Xem thêm"
                            )
                        )
                        spannableContent.setSpan(
                            ForegroundColorSpan(context.getColor(R.color.blue_logo)),
                            300, // start
                            311, // end
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                        binding.itemNewsFeedHeader.tvContent.text = spannableContent
                        checkContentSeeMore = true
                    }
                }
            }


            //Copy text
            binding.itemNewsFeedHeader.tvTitle.setOnLongClickListener {
                copyText(binding.itemNewsFeedHeader.tvTitle, data.title!!)
                return@setOnLongClickListener true
            }
            binding.itemNewsFeedHeader.tvContent.setOnLongClickListener {
                copyText(binding.itemNewsFeedHeader.tvContent, data.content!!)
                return@setOnLongClickListener true
            }
            binding.itemNewsFeedLink.rlLinkPreview.visibility = View.GONE
            binding.itemNewsFeedYouTube.youtubePlayerView.visibility = View.GONE
            kohii.setUp("").bind(binding.itemMediaOne.playerView)
            binding.itemMediaOne.lnMediaOne.visibility = View.GONE
            binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
            binding.itemMediaThree.lnMediaThree.visibility = View.GONE
            binding.itemMediaFour.lnMediaFour.visibility = View.GONE
            binding.itemMediaFive.lnMediaFive.visibility = View.GONE
            // Link
            binding.itemNewsFeedLink.rlLinkPreview.visibility = View.GONE
            binding.itemNewsFeedYouTube.youtubePlayerView.visibility = View.VISIBLE
            binding.itemNewsFeedYouTube.youtubePlayerView.addYouTubePlayerListener(object :
                AbstractYouTubePlayerListener() {
                override fun onReady(youtube: YouTubePlayer) {
                    super.onReady(youtube)
                    youTubePlayer = youtube
                    youTubePlayer?.cueVideo(
                        getVideoIdYouTube(data.url!!),
                        0f
                    )
                    binding.itemNewsFeedYouTube.youtubePlayerView.getPlayerUiController()
                        .setFullScreenButtonClickListener {
                            val youtube = YouTube()
                            youtube.videoId = getVideoIdYouTube(data.url!!)
                            youtube.currentSecond = currentTimeYoutube
                            newsFeedHandler.onShowFullVideoYouTube(youtube)
                        }
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    super.onStateChange(youTubePlayer, state)
                    if (state == PlayerConstants.PlayerState.PLAYING) {
                        youTubePlayerPause = youTubePlayer
                    }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    super.onCurrentSecond(youTubePlayer, second)
                    currentTimeYoutube = second
                }

                override fun onError(
                    youTubePlayer: YouTubePlayer,
                    error: PlayerConstants.PlayerError
                ) {
                    super.onError(youTubePlayer, error)
                    WriteLog.d("video error", error.toString())
                }
            })

            binding.itemNewsFeedBranch.lnBranch.visibility = View.GONE


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

            if (listValueReaction.size != 0) {
                valueLike = listValueReaction[5].value ?: 0
                valueExciting = listValueReaction[4].value ?: 0
                valueSad = listValueReaction[3].value ?: 0
                valueValue = listValueReaction[2].value ?: 0
                valueNegative = listValueReaction[1].value ?: 0
                valueNothing = listValueReaction[0].value ?: 0
            }

            binding.itemNewsFeedReaction.tvOneCount.text = data.reaction_one_count!!.toString()
            binding.itemNewsFeedReaction.tvTwoCount.text = data.reaction_two_count!!.toString()
            binding.itemNewsFeedReaction.tvThreeCount.text = data.reaction_three_count!!.toString()
            binding.itemNewsFeedReaction.tvFourCount.text = data.reaction_four_count!!.toString()
            binding.itemNewsFeedReaction.tvFiveCount.text = data.reaction_five_count.toString()
            binding.itemNewsFeedReaction.tvSixCount.text = data.reaction_six_count.toString()

            when (data.my_reaction_id) {
                1 -> {
                    setTextColorReaction(
                        binding.itemNewsFeedReaction.tvReactionOne,
                        binding.itemNewsFeedReaction.tvReactionTwo,
                        binding.itemNewsFeedReaction.tvReactionThree,
                        binding.itemNewsFeedReaction.tvReactionFour,
                        binding.itemNewsFeedReaction.tvReactionFive,
                        binding.itemNewsFeedReaction.tvReactionSix
                    )
                }
                2 -> {
                    setTextColorReaction(
                        binding.itemNewsFeedReaction.tvReactionTwo,
                        binding.itemNewsFeedReaction.tvReactionOne,
                        binding.itemNewsFeedReaction.tvReactionThree,
                        binding.itemNewsFeedReaction.tvReactionFour,
                        binding.itemNewsFeedReaction.tvReactionFive,
                        binding.itemNewsFeedReaction.tvReactionSix
                    )
                }
                3 -> {
                    setTextColorReaction(
                        binding.itemNewsFeedReaction.tvReactionThree,
                        binding.itemNewsFeedReaction.tvReactionOne,
                        binding.itemNewsFeedReaction.tvReactionTwo,
                        binding.itemNewsFeedReaction.tvReactionFour,
                        binding.itemNewsFeedReaction.tvReactionFive,
                        binding.itemNewsFeedReaction.tvReactionSix
                    )
                }
                4 -> {
                    setTextColorReaction(
                        binding.itemNewsFeedReaction.tvReactionFour,
                        binding.itemNewsFeedReaction.tvReactionOne,
                        binding.itemNewsFeedReaction.tvReactionTwo,
                        binding.itemNewsFeedReaction.tvReactionThree,
                        binding.itemNewsFeedReaction.tvReactionFive,
                        binding.itemNewsFeedReaction.tvReactionSix
                    )
                }
                5 -> {
                    setTextColorReaction(
                        binding.itemNewsFeedReaction.tvReactionFive,
                        binding.itemNewsFeedReaction.tvReactionOne,
                        binding.itemNewsFeedReaction.tvReactionTwo,
                        binding.itemNewsFeedReaction.tvReactionThree,
                        binding.itemNewsFeedReaction.tvReactionFour,
                        binding.itemNewsFeedReaction.tvReactionSix
                    )
                }
                6 -> {
                    setTextColorReaction(
                        binding.itemNewsFeedReaction.tvReactionSix,
                        binding.itemNewsFeedReaction.tvReactionOne,
                        binding.itemNewsFeedReaction.tvReactionTwo,
                        binding.itemNewsFeedReaction.tvReactionThree,
                        binding.itemNewsFeedReaction.tvReactionFour,
                        binding.itemNewsFeedReaction.tvReactionFive
                    )
                }
                else -> {
                    setTextColorReactionNull(
                        binding.itemNewsFeedReaction.tvReactionOne,
                        binding.itemNewsFeedReaction.tvReactionTwo,
                        binding.itemNewsFeedReaction.tvReactionThree,
                        binding.itemNewsFeedReaction.tvReactionFour,
                        binding.itemNewsFeedReaction.tvReactionFive,
                        binding.itemNewsFeedReaction.tvReactionSix
                    )
                }
            }

            //Set click reaction
            binding.itemNewsFeedReaction.lnReactionOne.setOnClickListener {
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionOne)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionTwo)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionThree)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFour)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFive)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionSix)

                when (data.my_reaction_id) {
                    1 -> {
                        data.my_reaction_id = 0
                        data.reaction_one_count = data.reaction_one_count!! - 1
                        data.reaction_point = data.reaction_point!! - valueLike

                        binding.itemNewsFeedReaction.tvOneCount.text =
                            data.reaction_one_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReactionNull(
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                    else -> {
                        when (data.my_reaction_id) {
                            2 -> {
                                data.reaction_two_count = data.reaction_two_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueExciting

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            3 -> {
                                data.reaction_three_count = data.reaction_three_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueSad

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            4 -> {
                                data.reaction_four_count = data.reaction_four_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueValue

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            5 -> {
                                data.reaction_five_count = data.reaction_five_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueNegative

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            6 -> {
                                data.reaction_six_count = data.reaction_six_count!! - 1
                                data.reaction_point = data.reaction_point!!

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                        }
                        data.my_reaction_id = 1
                        data.reaction_one_count = data.reaction_one_count!! + 1
                        data.reaction_point = data.reaction_point!! + valueLike
                        binding.itemNewsFeedReaction.tvOneCount.text =
                            data.reaction_one_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReaction(
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                }
                newsFeedHandler.onReaction(data._id!!, 1)
            }

            binding.itemNewsFeedReaction.lnReactionTwo.setOnClickListener {
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionOne)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionTwo)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionThree)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFour)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFive)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionSix)
                when (data.my_reaction_id) {
                    2 -> {
                        data.my_reaction_id = 0
                        data.reaction_two_count = data.reaction_two_count!! - 1
                        data.reaction_point = data.reaction_point!! - valueExciting

                        binding.itemNewsFeedReaction.tvTwoCount.text =
                            data.reaction_two_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()

                        setTextColorReactionNull(
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                    else -> {
                        when (data.my_reaction_id) {
                            1 -> {
                                data.reaction_one_count = data.reaction_one_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueLike

                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            3 -> {
                                data.reaction_three_count = data.reaction_three_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueSad

                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            4 -> {
                                data.reaction_four_count = data.reaction_four_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueValue

                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            5 -> {
                                data.reaction_five_count = data.reaction_five_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueNegative

                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            6 -> {
                                data.reaction_six_count = data.reaction_six_count!! - 1
                                data.reaction_point = data.reaction_point

                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                        }
                        data.my_reaction_id = 2
                        data.reaction_two_count = data.reaction_two_count!! + 1
                        data.reaction_point = data.reaction_point!! + valueExciting

                        binding.itemNewsFeedReaction.tvTwoCount.text =
                            data.reaction_two_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReaction(
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                }

                newsFeedHandler.onReaction(data._id!!, 2)
            }

            binding.itemNewsFeedReaction.lnReactionThree.setOnClickListener {
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionOne)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionTwo)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionThree)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFour)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFive)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionSix)
                when (data.my_reaction_id) {
                    3 -> {
                        data.my_reaction_id = 0
                        data.reaction_three_count = data.reaction_three_count!! - 1
                        data.reaction_point = data.reaction_point!! - valueSad

                        binding.itemNewsFeedReaction.tvThreeCount.text =
                            data.reaction_three_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()

                        setTextColorReactionNull(
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                    else -> {
                        when (data.my_reaction_id) {
                            1 -> {
                                data.reaction_one_count = data.reaction_one_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueLike

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            2 -> {
                                data.reaction_two_count = data.reaction_two_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueExciting

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            4 -> {
                                data.reaction_four_count = data.reaction_four_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueValue

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            5 -> {
                                data.reaction_five_count = data.reaction_five_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueNegative

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            6 -> {
                                data.reaction_six_count = data.reaction_six_count!! - 1
                                data.reaction_point = data.reaction_point

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                        }
                        data.my_reaction_id = 3
                        data.reaction_three_count = data.reaction_three_count!! + 1
                        data.reaction_point = data.reaction_point!! + valueSad

                        binding.itemNewsFeedReaction.tvThreeCount.text =
                            data.reaction_three_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReaction(
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                }
                newsFeedHandler.onReaction(data._id!!, 3)
            }

            binding.itemNewsFeedReaction.lnReactionFour.setOnClickListener {
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionOne)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionTwo)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionThree)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFour)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFive)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionSix)
                when (data.my_reaction_id) {
                    4 -> {
                        data.my_reaction_id = 0
                        data.reaction_four_count = data.reaction_four_count!! - 1
                        data.reaction_point = data.reaction_point!! - valueValue

                        binding.itemNewsFeedReaction.tvFourCount.text =
                            data.reaction_four_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReactionNull(
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                    else -> {
                        when (data.my_reaction_id) {
                            1 -> {
                                data.reaction_one_count = data.reaction_one_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueLike

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            2 -> {
                                data.reaction_two_count = data.reaction_two_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueExciting

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            3 -> {
                                data.reaction_three_count = data.reaction_three_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueSad

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            5 -> {
                                data.reaction_five_count = data.reaction_five_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueNegative

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            6 -> {
                                data.reaction_six_count = data.reaction_six_count!! - 1
                                data.reaction_point = data.reaction_point

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                        }
                        data.my_reaction_id = 4
                        data.reaction_four_count = data.reaction_four_count!! + 1
                        data.reaction_point = data.reaction_point!! + valueValue

                        binding.itemNewsFeedReaction.tvFourCount.text =
                            data.reaction_four_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReaction(
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                }
                newsFeedHandler.onReaction(data._id!!, 4)
            }

            binding.itemNewsFeedReaction.lnReactionFive.setOnClickListener {
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionOne)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionTwo)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionThree)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFour)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFive)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionSix)
                when (data.my_reaction_id) {
                    5 -> {
                        data.my_reaction_id = 0
                        data.reaction_five_count = data.reaction_five_count!! - 1
                        data.reaction_point = data.reaction_point!! - valueNegative

                        binding.itemNewsFeedReaction.tvFiveCount.text =
                            data.reaction_five_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReactionNull(
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                    else -> {
                        when (data.my_reaction_id) {
                            1 -> {
                                data.reaction_one_count = data.reaction_one_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueLike

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            2 -> {
                                data.reaction_two_count = data.reaction_two_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueExciting

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            3 -> {
                                data.reaction_three_count = data.reaction_three_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueSad

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            4 -> {
                                data.reaction_four_count = data.reaction_four_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueValue

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            6 -> {
                                data.reaction_six_count = data.reaction_six_count!! - 1
                                data.reaction_point = data.reaction_point

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvSixCount.text =
                                    data.reaction_six_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                        }
                        data.my_reaction_id = 5
                        data.reaction_five_count = data.reaction_five_count!! + 1
                        data.reaction_point = data.reaction_point!! + valueNegative

                        binding.itemNewsFeedReaction.tvFiveCount.text =
                            data.reaction_five_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReaction(
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                }
                newsFeedHandler.onReaction(data._id!!, 5)
            }

            binding.itemNewsFeedReaction.lnReactionSix.setOnClickListener {
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionOne)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionTwo)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionThree)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFour)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionFive)
                enabledClickReaction(binding.itemNewsFeedReaction.lnReactionSix)
                when (data.my_reaction_id) {
                    6 -> {
                        data.my_reaction_id = 0
                        data.reaction_six_count = data.reaction_six_count!! - 1

                        binding.itemNewsFeedReaction.tvSixCount.text =
                            data.reaction_six_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReactionNull(
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive,
                            binding.itemNewsFeedReaction.tvReactionSix
                        )
                    }
                    else -> {
                        when (data.my_reaction_id) {
                            1 -> {
                                data.reaction_one_count = data.reaction_one_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueLike

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            2 -> {
                                data.reaction_two_count = data.reaction_two_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueExciting

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            3 -> {
                                data.reaction_three_count = data.reaction_three_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueSad

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            4 -> {
                                data.reaction_four_count = data.reaction_four_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueValue

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                            5 -> {
                                data.reaction_five_count = data.reaction_five_count!! - 1
                                data.reaction_point = data.reaction_point!! - valueNegative

                                binding.itemNewsFeedReaction.tvTwoCount.text =
                                    data.reaction_two_count.toString()
                                binding.itemNewsFeedReaction.tvOneCount.text =
                                    data.reaction_one_count.toString()
                                binding.itemNewsFeedReaction.tvThreeCount.text =
                                    data.reaction_three_count.toString()
                                binding.itemNewsFeedReaction.tvFourCount.text =
                                    data.reaction_four_count.toString()
                                binding.itemNewsFeedReaction.tvFiveCount.text =
                                    data.reaction_five_count.toString()
                                binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                                    data.reaction_point.toString()
                            }
                        }
                        data.my_reaction_id = 6
                        data.reaction_six_count = data.reaction_six_count!! + 1

                        binding.itemNewsFeedReaction.tvSixCount.text =
                            data.reaction_six_count.toString()
                        binding.itemNewsFeedValueCommentShare.tvPointValue.text =
                            data.reaction_point.toString()
                        setTextColorReaction(
                            binding.itemNewsFeedReaction.tvReactionSix,
                            binding.itemNewsFeedReaction.tvReactionOne,
                            binding.itemNewsFeedReaction.tvReactionTwo,
                            binding.itemNewsFeedReaction.tvReactionThree,
                            binding.itemNewsFeedReaction.tvReactionFour,
                            binding.itemNewsFeedReaction.tvReactionFive
                        )
                    }
                }
                newsFeedHandler.onReaction(data._id!!, 6)
            }

            //Value, level value
            binding.itemNewsFeedValueCommentShare.tvPointValue.text = data.reaction_point.toString()

            if (CacheManager.getInstance().get(TechresEnum.LEVER_VALUE.toString()) != null) {
                listLevelValue = Gson().fromJson(
                    CacheManager.getInstance().get(TechresEnum.LEVER_VALUE.toString()), object :
                        TypeToken<ArrayList<LevelValue>>() {}.type
                )
            }

            try {
                when (data.alo_point_bonus_level_id) {
                    0 -> {
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.text = String.format(
                            "%s: %s",
                            listLevelValue[0].name,
                            listLevelValue[0].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.text = String.format(
                            "%s: %s",
                            listLevelValue[1].name,
                            listLevelValue[1].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.text =
                            String.format(
                                "%s: %s",
                                listLevelValue[2].name,
                                listLevelValue[2].point_target
                            )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.text = String.format(
                            "%s: %s",
                            listLevelValue[3].name,
                            listLevelValue[3].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.setBackgroundResource(
                            R.color.main_gray
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.setBackgroundResource(
                            R.color.main_gray
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.setBackgroundResource(
                            R.color.main_gray
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.setBackgroundResource(
                            R.color.main_gray
                        )
                    }
                    1 -> {
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.text = String.format(
                            "%s: %s",
                            listLevelValue[0].name,
                            listLevelValue[0].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.text = String.format(
                            "%s: %s",
                            listLevelValue[1].name,
                            listLevelValue[1].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.text =
                            String.format(
                                "%s: %s",
                                listLevelValue[2].name,
                                listLevelValue[2].point_target
                            )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.text = String.format(
                            "%s: %s",
                            listLevelValue[3].name,
                            listLevelValue[3].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.setBackgroundResource(
                            R.color.main_gray
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.setBackgroundResource(
                            R.color.main_gray
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.setBackgroundResource(
                            R.color.main_gray
                        )
                    }
                    2 -> {
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.text = String.format(
                            "%s: %s",
                            listLevelValue[0].name,
                            listLevelValue[0].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.text = String.format(
                            "%s: %s",
                            listLevelValue[1].name,
                            listLevelValue[1].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.text =
                            String.format(
                                "%s: %s",
                                listLevelValue[2].name,
                                listLevelValue[2].point_target
                            )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.text = String.format(
                            "%s: %s",
                            listLevelValue[3].name,
                            listLevelValue[3].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.setBackgroundResource(
                            R.color.main_gray
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.setBackgroundResource(
                            R.color.main_gray
                        )
                    }
                    3 -> {
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.text = String.format(
                            "%s: %s",
                            listLevelValue[0].name,
                            listLevelValue[0].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.text = String.format(
                            "%s: %s",
                            listLevelValue[1].name,
                            listLevelValue[1].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.text =
                            String.format(
                                "%s: %s",
                                listLevelValue[2].name,
                                listLevelValue[2].point_target
                            )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.text = String.format(
                            "%s: %s",
                            listLevelValue[3].name,
                            listLevelValue[3].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.setBackgroundResource(
                            R.color.main_gray
                        )
                    }
                    else -> {
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.text = String.format(
                            "%s: %s",
                            listLevelValue[data.alo_point_bonus_level_id!! - 3].name,
                            listLevelValue[data.alo_point_bonus_level_id!! - 3].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.text = String.format(
                            "%s: %s",
                            listLevelValue[data.alo_point_bonus_level_id!! - 2].name,
                            listLevelValue[data.alo_point_bonus_level_id!! - 2].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.text =
                            String.format(
                                "%s: %s",
                                listLevelValue[data.alo_point_bonus_level_id!! - 1].name,
                                listLevelValue[data.alo_point_bonus_level_id!! - 1].point_target
                            )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.text = String.format(
                            "%s: %s",
                            listLevelValue[data.alo_point_bonus_level_id!!].name,
                            listLevelValue[data.alo_point_bonus_level_id!!].point_target
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueOne.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueTwo.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueThree.setBackgroundResource(
                            R.color.blue_logo
                        )
                        binding.itemNewsFeedValueCommentShare.tvLevelValueFour.setBackgroundResource(
                            R.color.main_gray
                        )
                    }
                }
            } catch (Ex: Exception) {
            }


            //Comment
            binding.itemNewsFeedValueCommentShare.tvCommentCount.text = String.format(
                "%s %s", data.comment_count.toString(), context.resources.getString(
                    R.string.comment
                )
            )
            when (data.comments.size) {
                0 -> {
                    binding.itemNewsFeedComment.lnCommentOne.visibility = View.GONE
                    binding.itemNewsFeedComment.lnCommentTwo.visibility = View.GONE
                    binding.itemNewsFeedComment.lnCommentThree.visibility = View.GONE
                }
                1 -> {
                    binding.itemNewsFeedComment.lnCommentOne.visibility = View.VISIBLE
                    binding.itemNewsFeedComment.lnCommentTwo.visibility = View.GONE
                    binding.itemNewsFeedComment.lnCommentThree.visibility = View.GONE

                    try {
                        Utils.getImage(
                            binding.itemNewsFeedComment.ivCommentAvatarOne,
                            data.comments[0].customer_avatar.thumb, configNodeJs
                        )
                    } catch (ex: Exception) {
                    }
                    binding.itemNewsFeedComment.tvCommentUserNameOne.text =
                        data.comments[0].customer_name

                    if (data.comments[0].content.equals("")) {
                        binding.itemNewsFeedComment.tvCommentContentOne.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.tvCommentContentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.tvCommentContentOne.text =
                            data.comments[0].content
                    }

                    if (data.comments[0].image_urls.size == 0 && data.comments[0].sticker!!.isNotEmpty()) {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.VISIBLE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgStickerOne,
                            data.comments[0].sticker!!, configNodeJs
                        )
                    } else if (data.comments[0].image_urls.size != 0 && data.comments[0].sticker!!.isEmpty()) {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.GONE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgCommentOne,
                            data.comments[0].image_urls[0].original, configNodeJs
                        )
                    } else {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.GONE
                    }

                    binding.itemNewsFeedComment.tvCommentContentOne.text = data.comments[0].content
                    binding.itemNewsFeedComment.tvCommentTimeOne.text =
                        TimeFormatHelper.timeAgoString(data.comments[0].created_at)

                    if (data.comments[0].my_reaction_id != 1) {
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }

                    if (data.comments[0].customer_like_ids.size == 0) {
                        binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentOne.text =
                            data.comments[0].customer_like_ids.size.toString()
                    }

                }
                2 -> {
                    binding.itemNewsFeedComment.lnCommentOne.visibility = View.VISIBLE
                    binding.itemNewsFeedComment.lnCommentTwo.visibility = View.VISIBLE
                    binding.itemNewsFeedComment.lnCommentThree.visibility = View.GONE

                    try {
                        Utils.getImage(
                            binding.itemNewsFeedComment.ivCommentAvatarOne,
                            data.comments[0].customer_avatar.thumb, configNodeJs
                        )
                    } catch (ex: Exception) {
                    }
                    try {
                        Utils.getImage(
                            binding.itemNewsFeedComment.ivCommentAvatarTwo,
                            data.comments[1].customer_avatar.thumb, configNodeJs
                        )
                    } catch (ex: Exception) {
                    }
                    binding.itemNewsFeedComment.tvCommentUserNameOne.text =
                        data.comments[0].customer_name
                    binding.itemNewsFeedComment.tvCommentUserNameTwo.text =
                        data.comments[1].customer_name

                    if (data.comments[0].content.equals("")) {
                        binding.itemNewsFeedComment.tvCommentContentOne.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.tvCommentContentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.tvCommentContentOne.text =
                            data.comments[0].content
                    }

                    if (data.comments[1].content.equals("")) {
                        binding.itemNewsFeedComment.tvCommentContentTwo.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.tvCommentContentTwo.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.tvCommentContentTwo.text =
                            data.comments[1].content
                    }

                    if (data.comments[0].image_urls.size == 0 && data.comments[0].sticker!!.isNotEmpty()) {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.VISIBLE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgStickerOne,
                            data.comments[0].sticker!!, configNodeJs
                        )
                    } else if (data.comments[0].image_urls.size != 0 && data.comments[0].sticker!!.isEmpty()) {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.GONE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgCommentOne,
                            data.comments[0].image_urls[0].original, configNodeJs
                        )
                    } else {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.GONE
                    }

                    if (data.comments[1].image_urls.size == 0 && data.comments[1].sticker!!.isNotEmpty()) {
                        binding.itemNewsFeedComment.cvCommentTwo.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerTwo.visibility = View.VISIBLE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgStickerTwo,
                            data.comments[1].sticker!!, configNodeJs
                        )
                    } else if (data.comments[1].image_urls.size != 0 && data.comments[1].sticker!!.isEmpty()) {
                        binding.itemNewsFeedComment.cvCommentTwo.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.imgStickerTwo.visibility = View.GONE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgCommentTwo,
                            data.comments[1].image_urls[0].original, configNodeJs
                        )
                    } else {
                        binding.itemNewsFeedComment.cvCommentTwo.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerTwo.visibility = View.GONE
                    }

                    binding.itemNewsFeedComment.tvCommentTimeOne.text =
                        TimeFormatHelper.timeAgoString(data.comments[0].created_at)
                    binding.itemNewsFeedComment.tvCommentTimeTwo.text =
                        TimeFormatHelper.timeAgoString(data.comments[1].created_at)

                    if (data.comments[0].my_reaction_id != 1) {
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }

                    if (data.comments[0].customer_like_ids.size == 0) {
                        binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentOne.text =
                            data.comments[0].customer_like_ids.size.toString()
                    }

                    if (data.comments[1].my_reaction_id != 1) {
                        binding.itemNewsFeedComment.tvFavoriteTwo.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        binding.itemNewsFeedComment.tvFavoriteTwo.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }

                    if (data.comments[1].customer_like_ids.size == 0) {
                        binding.itemNewsFeedComment.lnReactionCommentTwo.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.lnReactionCommentTwo.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentTwo.text =
                            data.comments[1].customer_like_ids.size.toString()
                    }
                }
                else -> {
                    binding.itemNewsFeedComment.lnCommentOne.visibility = View.VISIBLE
                    binding.itemNewsFeedComment.lnCommentTwo.visibility = View.VISIBLE
                    binding.itemNewsFeedComment.lnCommentThree.visibility = View.VISIBLE

                    try {
                        Utils.getImage(
                            binding.itemNewsFeedComment.ivCommentAvatarOne,
                            data.comments[0].customer_avatar.thumb, configNodeJs
                        )
                    } catch (ex: Exception) {
                    }
                    try {
                        Utils.getImage(
                            binding.itemNewsFeedComment.ivCommentAvatarTwo,
                            data.comments[1].customer_avatar.thumb,
                            configNodeJs
                        )
                    } catch (ex: Exception) {
                    }
                    try {
                        Utils.getImage(
                            binding.itemNewsFeedComment.ivCommentAvatarThree,
                            data.comments[2].customer_avatar.thumb,
                            configNodeJs
                        )
                    } catch (ex: Exception) {
                    }
                    binding.itemNewsFeedComment.tvCommentUserNameOne.text =
                        data.comments[0].customer_name
                    binding.itemNewsFeedComment.tvCommentUserNameTwo.text =
                        data.comments[1].customer_name
                    binding.itemNewsFeedComment.tvCommentUserNameThree.text =
                        data.comments[2].customer_name

                    if (data.comments[0].content.equals("")) {
                        binding.itemNewsFeedComment.tvCommentContentOne.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.tvCommentContentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.tvCommentContentOne.text =
                            data.comments[0].content
                    }

                    if (data.comments[1].content.equals("")) {
                        binding.itemNewsFeedComment.tvCommentContentTwo.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.tvCommentContentTwo.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.tvCommentContentTwo.text =
                            data.comments[1].content
                    }

                    if (data.comments[2].content.equals("")) {
                        binding.itemNewsFeedComment.tvCommentContentThree.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.tvCommentContentThree.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.tvCommentContentThree.text =
                            data.comments[2].content
                    }

                    if (data.comments[0].image_urls.size == 0 && data.comments[0].sticker!!.isNotEmpty()) {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.VISIBLE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgStickerOne,
                            data.comments[0].sticker!!, configNodeJs
                        )
                    } else if (data.comments[0].image_urls.size != 0 && data.comments[0].sticker!!.isEmpty()) {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.GONE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgCommentOne,
                            data.comments[0].image_urls[0].original, configNodeJs
                        )
                    } else {
                        binding.itemNewsFeedComment.cvCommentOne.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerOne.visibility = View.GONE
                    }

                    if (data.comments[1].image_urls.size == 0 && data.comments[1].sticker!!.isNotEmpty()) {
                        binding.itemNewsFeedComment.cvCommentTwo.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerTwo.visibility = View.VISIBLE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgStickerTwo,
                            data.comments[1].sticker!!, configNodeJs
                        )
                    } else if (data.comments[1].image_urls.size != 0 && data.comments[1].sticker!!.isEmpty()) {
                        binding.itemNewsFeedComment.cvCommentTwo.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.imgStickerTwo.visibility = View.GONE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgCommentTwo,
                            data.comments[1].image_urls[0].original, configNodeJs
                        )
                    } else {
                        binding.itemNewsFeedComment.cvCommentTwo.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerTwo.visibility = View.GONE
                    }

                    if (data.comments[2].image_urls.size == 0 && data.comments[2].sticker!!.isNotEmpty()) {
                        binding.itemNewsFeedComment.cvCommentThree.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerThree.visibility = View.VISIBLE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgStickerThree,
                            data.comments[2].sticker!!, configNodeJs
                        )
                    } else if (data.comments[2].image_urls.size != 0 && data.comments[2].sticker!!.isEmpty()) {
                        binding.itemNewsFeedComment.cvCommentThree.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.imgStickerThree.visibility = View.GONE
                        Utils.getGlide(
                            binding.itemNewsFeedComment.imgCommentThree,
                            data.comments[2].image_urls[0].original, configNodeJs
                        )
                    } else {
                        binding.itemNewsFeedComment.cvCommentThree.visibility = View.GONE
                        binding.itemNewsFeedComment.imgStickerThree.visibility = View.GONE
                    }

                    binding.itemNewsFeedComment.tvCommentTimeOne.text =
                        TimeFormatHelper.timeAgoString(data.comments[0].created_at)
                    binding.itemNewsFeedComment.tvCommentTimeTwo.text =
                        TimeFormatHelper.timeAgoString(data.comments[1].created_at)
                    binding.itemNewsFeedComment.tvCommentTimeThree.text =
                        TimeFormatHelper.timeAgoString(data.comments[2].created_at)

                    if (data.comments[0].my_reaction_id != 1) {
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }

                    if (data.comments[0].customer_like_ids.size == 0) {
                        binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentOne.text =
                            data.comments[0].customer_like_ids.size.toString()
                    }

                    if (data.comments[1].my_reaction_id != 1) {
                        binding.itemNewsFeedComment.tvFavoriteTwo.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        binding.itemNewsFeedComment.tvFavoriteTwo.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }

                    if (data.comments[1].customer_like_ids.size == 0) {
                        binding.itemNewsFeedComment.lnReactionCommentTwo.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.lnReactionCommentTwo.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentTwo.text =
                            data.comments[1].customer_like_ids.size.toString()
                    }

                    if (data.comments[2].my_reaction_id != 1) {
                        binding.itemNewsFeedComment.tvFavoriteThree.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        binding.itemNewsFeedComment.tvFavoriteThree.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }

                    if (data.comments[2].customer_like_ids.size == 0) {
                        binding.itemNewsFeedComment.lnReactionCommentThree.visibility = View.GONE
                    } else {
                        binding.itemNewsFeedComment.lnReactionCommentThree.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentThree.text =
                            data.comments[2].customer_like_ids.size.toString()
                    }
                }
            }
            try {
                Utils.getImage(
                    binding.itemNewsFeedComment.ivMyAvatar,
                    user.avatar_three_image.original,
                    configNodeJs
                )
            } catch (ex: Exception) {
            }


            //Set click
            if (user.id.toString().isNotBlank()) {
                // Set buttom Comment
                binding.itemNewsFeedValueCommentShare.lnComment.setOnClickListener {
                    newsFeedHandler.onComment(
                        position,
                        TechresEnum.TYPE_COMMENT_TEXT.toString(),
                        data,
                        -1
                    )
                }
                binding.itemNewsFeedComment.lnComments.setOnClickListener {
                    newsFeedHandler.onComment(
                        position,
                        TechresEnum.TYPE_COMMENT_TEXT.toString(),
                        data,
                        -1
                    )
                }

                // Set buttom Share
                binding.itemNewsFeedValueCommentShare.lnShare.setOnClickListener {
                    newsFeedHandler.onShare()
                }
                // Set buttom more
                binding.itemNewsFeedHeader.imgMore.setOnClickListener {
                    newsFeedHandler.onButtonMore(position)
                }

                binding.itemNewsFeedHeader.imgAvatar.setOnClickListener {
                    newsFeedHandler.onAvatar(
                        data.customer.avatar.thumb ?: "",
                        position
                    )
                }

                binding.itemNewsFeedHeader.lnNamePost.setOnClickListener {
                    data.customer.member_id?.let { it1 ->
                        newsFeedHandler.onClickProfile(
                            position,
                            it1
                        )
                    }
                }

                binding.itemNewsFeedValueCommentShare.lnValue.setOnClickListener {
                    newsFeedHandler.onDetailValuePost(data._id)
                }

//                binding.itemNewsFeedComment.tagingCommentEdt.addTextChangedListener(object : TextWatcher {
//                    override fun onTextChanged(
//                        s: CharSequence,
//                        start: Int,
//                        before: Int,
//                        count: Int
//                    ) {
//                    }
//
//                    override fun beforeTextChanged(
//                        s: CharSequence,
//                        start: Int,
//                        count: Int,
//                        after: Int
//                    ) {
//                    }
//
//                    override fun afterTextChanged(s: Editable) {
//
//                        if (s.isNotEmpty() || !StringUtils.isNullOrEmpty(selectedChatImg)) {
//                            binding.itemNewsFeedComment.imgSent.visibility = View.VISIBLE
//                            binding.itemNewsFeedComment.mediaIv.visibility = View.GONE
//                        } else {
//                            binding.itemNewsFeedComment.imgSent.visibility = View.GONE
//                            binding.itemNewsFeedComment.mediaIv.visibility = View.VISIBLE
//
//                        }
//                    }
//                })

                binding.itemNewsFeedComment.imgSent.setOnClickListener {
                    data._id?.let { it1 ->
                        newsFeedHandler.onSentComment(
                            position,
                            it1, binding.itemNewsFeedComment.tagingCommentEdt.text.toString()
                        )
                    }
                    binding.itemNewsFeedComment.tagingCommentEdt.setText("")
                    binding.itemNewsFeedComment.tagingCommentEdt.requestFocus()
                    val imm =
                        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(
                        binding.itemNewsFeedComment.tagingCommentEdt.windowToken,
                        0
                    )
                }

                binding.itemNewsFeedComment.mediaIv.setOnClickListener {
                    newsFeedHandler.onComment(
                        position,
                        TechresEnum.TYPE_COMMENT_MEDIA.toString(),
                        data,
                        -1
                    )
                }

                binding.itemNewsFeedComment.tvCommentClick.setOnClickListener {
                    newsFeedHandler.onComment(
                        position,
                        TechresEnum.TYPE_COMMENT_TEXT.toString(),
                        data,
                        -1
                    )
                }

                //Comment
                binding.itemNewsFeedComment.ivCommentAvatarOne.setOnClickListener {
                    newsFeedHandler.onAvatar(
                        data.comments[0].customer_avatar.original ?: "",
                        position
                    )
                }

                binding.itemNewsFeedComment.tvCommentUserNameOne.setOnClickListener {
                    newsFeedHandler.onClickProfile(position, data.comments[0].customer_id ?: 0)
                }

                binding.itemNewsFeedComment.imgCommentOne.setOnClickListener {
                    newsFeedHandler.onAvatar(
                        data.comments[0].image_urls[0].original ?: "",
                        position
                    )
                }

                binding.itemNewsFeedComment.ivCommentAvatarTwo.setOnClickListener {
                    newsFeedHandler.onAvatar(
                        data.comments[1].customer_avatar.original ?: "",
                        position
                    )
                }

                binding.itemNewsFeedComment.tvCommentUserNameTwo.setOnClickListener {
                    newsFeedHandler.onClickProfile(position, data.comments[1].customer_id ?: 0)
                }

                binding.itemNewsFeedComment.imgCommentTwo.setOnClickListener {
                    newsFeedHandler.onAvatar(
                        data.comments[1].image_urls[0].original ?: "",
                        position
                    )
                }

                binding.itemNewsFeedComment.ivCommentAvatarThree.setOnClickListener {
                    newsFeedHandler.onAvatar(
                        data.comments[2].customer_avatar.original ?: "",
                        position
                    )
                }

                binding.itemNewsFeedComment.tvCommentUserNameThree.setOnClickListener {
                    newsFeedHandler.onClickProfile(position, data.comments[2].customer_id ?: 0)
                }

                binding.itemNewsFeedComment.imgCommentThree.setOnClickListener {
                    newsFeedHandler.onAvatar(
                        data.comments[2].image_urls[0].original ?: "",
                        position
                    )
                }

                binding.itemNewsFeedComment.txtReplyOne.setOnClickListener {
                    newsFeedHandler.onComment(
                        position,
                        TechresEnum.TYPE_COMMENT_REPLY.toString(),
                        data,
                        0
                    )
                }
                binding.itemNewsFeedComment.txtReplyTwo.setOnClickListener {
                    newsFeedHandler.onComment(
                        position,
                        TechresEnum.TYPE_COMMENT_REPLY.toString(),
                        data,
                        1
                    )
                }
                binding.itemNewsFeedComment.txtReplyThree.setOnClickListener {
                    newsFeedHandler.onComment(
                        position,
                        TechresEnum.TYPE_COMMENT_REPLY.toString(),
                        data,
                        2
                    )
                }

                binding.itemNewsFeedComment.tvFavoriteOne.setOnClickListener {
                    if (data.comments[0].my_reaction_id == 1) {
                        data.comments[0].my_reaction_id = 0
                        data.comments[0].customer_like_ids.removeIf { it == user.id }
                        if (data.comments[0].customer_like_ids.size == 0) {
                            binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.GONE
                        } else {
                            binding.itemNewsFeedComment.lnReactionCommentOne.visibility =
                                View.VISIBLE
                        }
                        binding.itemNewsFeedComment.txtReactionCommentOne.text =
                            data.comments[0].customer_like_ids.size.toString()
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        data.comments[0].my_reaction_id = 1
                        data.comments[0].customer_like_ids.add(user.id)
                        binding.itemNewsFeedComment.lnReactionCommentOne.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentOne.text =
                            data.comments[0].customer_like_ids.size.toString()
                        binding.itemNewsFeedComment.tvFavoriteOne.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }
                    newsFeedHandler.onReactionComment(data._id!!, data.comments[0].comment_id!!)
                }

                binding.itemNewsFeedComment.tvFavoriteTwo.setOnClickListener {
                    if (data.comments[1].my_reaction_id == 1) {
                        data.comments[1].my_reaction_id = 0
                        data.comments[1].customer_like_ids.removeIf { it == user.id }
                        if (data.comments[1].customer_like_ids.size == 0) {
                            binding.itemNewsFeedComment.lnReactionCommentTwo.visibility = View.GONE
                        } else {
                            binding.itemNewsFeedComment.lnReactionCommentTwo.visibility =
                                View.VISIBLE
                        }
                        binding.itemNewsFeedComment.txtReactionCommentTwo.text =
                            data.comments[1].customer_like_ids.size.toString()
                        binding.itemNewsFeedComment.tvFavoriteTwo.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        data.comments[1].my_reaction_id = 1
                        data.comments[1].customer_like_ids.add(user.id)
                        binding.itemNewsFeedComment.lnReactionCommentTwo.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentTwo.text =
                            data.comments[1].customer_like_ids.size.toString()
                        binding.itemNewsFeedComment.tvFavoriteTwo.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }
                    newsFeedHandler.onReactionComment(data._id!!, data.comments[1].comment_id!!)
                }

                binding.itemNewsFeedComment.tvFavoriteThree.setOnClickListener {
                    if (data.comments[2].my_reaction_id == 1) {
                        data.comments[2].my_reaction_id = 0
                        data.comments[2].customer_like_ids.removeIf { it == user.id }
                        if (data.comments[2].customer_like_ids.size == 0) {
                            binding.itemNewsFeedComment.lnReactionCommentThree.visibility =
                                View.GONE
                        } else {
                            binding.itemNewsFeedComment.lnReactionCommentThree.visibility =
                                View.VISIBLE
                        }
                        binding.itemNewsFeedComment.txtReactionCommentThree.text =
                            data.comments[2].customer_like_ids.size.toString()
                        binding.itemNewsFeedComment.tvFavoriteThree.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.text_gray
                            )
                        )
                    } else {
                        data.comments[2].my_reaction_id = 1
                        data.comments[2].customer_like_ids.add(user.id)
                        binding.itemNewsFeedComment.lnReactionCommentThree.visibility = View.VISIBLE
                        binding.itemNewsFeedComment.txtReactionCommentThree.text =
                            data.comments[2].customer_like_ids.size.toString()
                        binding.itemNewsFeedComment.tvFavoriteThree.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    }
                    newsFeedHandler.onReactionComment(data._id!!, data.comments[2].comment_id!!)
                }
            }

        }

    }

    private fun getVideoIdYouTube(youTubeUrl: String): String {
//        val pattern =
//            "https?://(?:[0-9A-Z-]+\\.)?(?:youtu\\.be/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|</a>))[?=&+%\\w]*"
//        val compiledPattern: Pattern = Pattern.compile(
//            pattern,
//            Pattern.CASE_INSENSITIVE
//        )
//        val matcher: Matcher = compiledPattern.matcher(youTubeUrl)
//        return if (matcher.find()) {
//            matcher.group(1)
//        } else ""
        var link = ""
        val a: Int
        val b: Int
        if (youTubeUrl.contains(context.resources.getString(R.string.link_youtube_1))) {
            a = youTubeUrl.indexOf(".be/")
            link = youTubeUrl.substring(a + 4)
        } else if (youTubeUrl.contains(context.resources.getString(R.string.link_youtube_4))) {
            a = youTubeUrl.indexOf("?v=")
            if (youTubeUrl.contains("&")) {
                b = youTubeUrl.indexOf("&")
                link = youTubeUrl.substring(a + 3, b)
            } else link = youTubeUrl.substring(a + 3)
        } else if (youTubeUrl.contains(context.resources.getString(R.string.link_youtube_3))) {
            a = youTubeUrl.indexOf("?")
            b = youTubeUrl.indexOf("shorts/")
            link = youTubeUrl.substring(b + 7, a)
        }
        Timber.d("load link id : ")
        Timber.d(link)
        return link!!
    }

    private fun copyText(view: View, data: String) {
        val popup = PopupMenu(context, view)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.menu_copy, popup.menu)

        popup.setOnMenuItemClickListener {
            if (it.itemId == R.id.copy) {
                val cm: ClipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.text = data.trim()
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

    fun cueVideo(videoId: String) {
        currentVideoId = videoId
        if (this@ItemPostYoutubeHolder.youTubePlayer == null) {
            return
        }
        binding.itemNewsFeedYouTube.youtubePlayerView.getYouTubePlayerWhenReady(
            object :
                YouTubePlayerCallback {
                override fun onYouTubePlayer(initializedYouTubePlayer: YouTubePlayer) {
                    this@ItemPostYoutubeHolder.youTubePlayer = initializedYouTubePlayer
                    this@ItemPostYoutubeHolder.youTubePlayer!!.cueVideo(currentVideoId, 0f)
                }
            })
    }

    fun enabledClickReaction(view: View) {
        view.isEnabled = false
        Handler().postDelayed({ view.isEnabled = true }, 3000)

    }

    companion object {
        private const val mediaVideoType = 0
    }
}