package vn.techres.line.helper.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.DisplayMetrics
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import coil.size.Scale
import com.blankj.utilcode.util.ActivityUtils.startActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.giphy.sdk.analytics.GiphyPingbacks.sharedPref
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.TechResApplication.Companion.mSocket
import vn.techres.line.adapter.chat.MessageViewerAdapter
import vn.techres.line.adapter.chat.ViewerInformationAdapter
import vn.techres.line.call.CallActivity
import vn.techres.line.data.model.chat.*
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemReactionMessageBinding
import vn.techres.line.helper.CenterLayoutManager
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.getResources
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.emojiaction.Direction
import vn.techres.line.helper.emojiaction.ZeroGravityAnimation
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.helper.videocall.TechResEnumVideoCall
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.ViewerMessageHandler
import vn.techres.line.widget.PreCachingLayoutManager
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


object ChatUtils {
    fun setReaction(
        linearLayoutContainer: LinearLayout,
        binding: ItemReactionMessageBinding,
        reaction: Reaction
    ) {
        binding.imgReactionPress.show()
        binding.lnReaction.show()
        linearLayoutContainer.show()
        binding.tvReactionCount.text = reaction.reactions_count.toString()
        val reactionList = getReactionItem(reaction)
        reactionList.sortByDescending { it.number }
        setImageReaction(binding.imgReactionOne, reactionList[0])
        setImageReaction(binding.imgReactionTwo, reactionList[1])
        setImageReaction(binding.imgReactionThree, reactionList[2])

        when (reaction.last_reactions) {
            TechResEnumChat.TYPE_REACTION_LIKE.toString().toInt() -> {
                binding.imgReactionPress.setImageResource(R.drawable.ic_like)
            }
            TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt() -> {
                binding.imgReactionPress.setImageResource(R.drawable.ic_heart)
            }
            TechResEnumChat.TYPE_REACTION_SMILE.toString().toInt() -> {
                binding.imgReactionPress.setImageResource(R.drawable.ic_smile)
            }
            TechResEnumChat.TYPE_REACTION_WOW.toString().toInt() -> {
                binding.imgReactionPress.setImageResource(R.drawable.ic_wow)
            }
            TechResEnumChat.TYPE_REACTION_ANGRY.toString().toInt() -> {
                binding.imgReactionPress.setImageResource(R.drawable.ic_angry)
            }
            TechResEnumChat.TYPE_REACTION_SAD.toString().toInt() -> {
                binding.imgReactionPress.setImageResource(R.drawable.ic_sad)
            }
            else -> {
                binding.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            }
        }
    }

    fun getReactionItem(reaction: Reaction): ArrayList<ReactionItem> {
        val reactionList = ArrayList<ReactionItem>()
        val reactionItemLike = ReactionItem()
        reactionItemLike.name = TechResEnumChat.TYPE_REACTION_LIKE.toString()
        reactionItemLike.number = reaction.like
        reactionList.add(reactionItemLike)

        val reactionItemWow = ReactionItem()
        reactionItemWow.name = TechResEnumChat.TYPE_REACTION_WOW.toString()
        reactionItemWow.number = reaction.wow
        reactionList.add(reactionItemWow)

        val reactionItemAngy = ReactionItem()
        reactionItemAngy.name = TechResEnumChat.TYPE_REACTION_ANGRY.toString()
        reactionItemAngy.number = reaction.angry
        reactionList.add(reactionItemAngy)

        val reactionItemSad = ReactionItem()
        reactionItemSad.name = TechResEnumChat.TYPE_REACTION_SAD.toString()
        reactionItemSad.number = reaction.sad
        reactionList.add(reactionItemSad)

        val reactionItemSmile = ReactionItem()
        reactionItemSmile.name = TechResEnumChat.TYPE_REACTION_SMILE.toString()
        reactionItemSmile.number = reaction.smile
        reactionList.add(reactionItemSmile)

        val reactionItemLove = ReactionItem()
        reactionItemLove.name = TechResEnumChat.TYPE_REACTION_LOVE.toString()
        reactionItemLove.number = reaction.love
        reactionList.add(reactionItemLove)
        return reactionList
    }

    fun getReactionItem(reaction: UserReaction): ArrayList<ReactionItem>? {
        val reactionList = ArrayList<ReactionItem>()
        val reactionItemLike = ReactionItem()
        reactionItemLike.name = TechResEnumChat.TYPE_REACTION_LIKE.toString()
        reactionItemLike.number = reaction.like
        reactionList.add(reactionItemLike)

        val reactionItemWow = ReactionItem()
        reactionItemWow.name = TechResEnumChat.TYPE_REACTION_WOW.toString()
        reactionItemWow.number = reaction.wow
        reactionList.add(reactionItemWow)

        val reactionItemAngy = ReactionItem()
        reactionItemAngy.name = TechResEnumChat.TYPE_REACTION_ANGRY.toString()
        reactionItemAngy.number = reaction.angry
        reactionList.add(reactionItemAngy)

        val reactionItemSad = ReactionItem()
        reactionItemSad.name = TechResEnumChat.TYPE_REACTION_SAD.toString()
        reactionItemSad.number = reaction.sad
        reactionList.add(reactionItemSad)

        val reactionItemSmile = ReactionItem()
        reactionItemSmile.name = TechResEnumChat.TYPE_REACTION_SMILE.toString()
        reactionItemSmile.number = reaction.smile
        reactionList.add(reactionItemSmile)

        val reactionItemLove = ReactionItem()
        reactionItemLove.name = TechResEnumChat.TYPE_REACTION_LOVE.toString()
        reactionItemLove.number = reaction.love
        reactionList.add(reactionItemLove)
        return reactionList
    }

    fun setImageReaction(imageView: ImageView, reactionItem: ReactionItem) {
        if (reactionItem.number!! > 0) {
            imageView.show()
        } else {
            imageView.visibility = View.GONE
        }
        when (reactionItem.name) {
            TechResEnumChat.TYPE_REACTION_LIKE.toString() -> {
                imageView.setImageResource(R.drawable.ic_like)
            }
            TechResEnumChat.TYPE_REACTION_LOVE.toString() -> {
                imageView.setImageResource(R.drawable.ic_heart)
            }
            TechResEnumChat.TYPE_REACTION_SMILE.toString() -> {
                imageView.setImageResource(R.drawable.ic_smile)
            }
            TechResEnumChat.TYPE_REACTION_WOW.toString() -> {
                imageView.setImageResource(R.drawable.ic_wow)
            }
            TechResEnumChat.TYPE_REACTION_ANGRY.toString() -> {
                imageView.setImageResource(R.drawable.ic_angry)
            }
            TechResEnumChat.TYPE_REACTION_SAD.toString() -> {
                imageView.setImageResource(R.drawable.ic_sad)
            }
        }
    }

    fun getAvatar(imageView: ImageView?, string: String?) {
        imageView?.load(string) {
            crossfade(true)
            scale(Scale.FIT)
            memoryCachePolicy(CachePolicy.ENABLED)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
            size(300, 300)
        }
    }

    fun getAvatarGroup(imageView: ImageView, string: String?, configNodeJs: ConfigNodeJs) {
        Glide.with(imageView)
            .load(
                String.format("%s%s", configNodeJs.api_ads, string)
            )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .apply(
                RequestOptions().placeholder(R.drawable.logo_aloline_placeholder)
                    .error(R.drawable.logo_aloline_placeholder)
            )
            .into(imageView)
    }

    fun getUrl(url: String?, configNodeJs: ConfigNodeJs): String {
        return String.format(
            "%s%s",
            configNodeJs.api_ads,
            url
        )
    }

    fun setClickTagName(
        message: String,
        listTagName: ArrayList<TagName>,
        tvMessage: TextView,
        chooseNameUserHandler: ChooseNameUserHandler?
    ) {
        var messageHtml = message
        val links = ArrayList<Pair<String, View.OnClickListener>>()
        if (listTagName.isNotEmpty()) {
            listTagName.forEach { tagName ->
                val name = String.format(
                    "%s%s", "@", tagName.full_name
                )
                messageHtml = messageHtml.replace(tagName.key_tag_name ?: "", name)
                val pair = Pair(
                    "@" + tagName.full_name, View.OnClickListener {
                        chooseNameUserHandler?.onChooseTagName(tagName)
                    }
                )
                links.add(pair)
            }
        }
        WriteLog.d("nhanpro", messageHtml)


        //See more
        var checkContentSeeMore = true
        if (messageHtml.trimIndent()!!.length >= 320) {
            val spannableContent = SpannableString(
                String.format(
                    "%s%s",
                    messageHtml.trimIndent().substring(0, 320),
                    "...Xem thêm"
                )
            )
            spannableContent.setSpan(
                ForegroundColorSpan(Color.BLUE),
                320, // start
                331, // end
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            tvMessage.text = spannableContent
            checkContentSeeMore = true
        } else {
            tvMessage.text = messageHtml
        }

        tvMessage.setOnClickListener {
            if (messageHtml.trimIndent()!!.length >= 320) {
                if (checkContentSeeMore) {
                    val spannableContent = SpannableString(
                        String.format(
                            "%s %s",
                            messageHtml.trim(),
                            "Thu gọn"
                        )
                    )
                    spannableContent.setSpan(
                        ForegroundColorSpan(Color.MAGENTA),
                       messageHtml.trim().length, // start
                       messageHtml.trim().length + 8, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    tvMessage.text = spannableContent
                    checkContentSeeMore = false
                } else {
                    val spannableContent = SpannableString(
                        String.format(
                            "%s%s",
                           messageHtml.trimIndent().substring(0, 320),
                            "...Xem thêm"
                        )
                    )
                    spannableContent.setSpan(
                        ForegroundColorSpan(Color.BLUE),
                        320, // start
                        331, // end
                        Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                    tvMessage.text = spannableContent
                    checkContentSeeMore = true
                }
            }
        }


        if (links.isNotEmpty()) {
            tvMessage.makeLinks(links)
        }
    }


    fun checkSenderMessage(message: MessagesByGroup, user: User): Boolean {
        return message.sender.member_id ?: 0 == user.id
    }

    fun TextView.makeLinks(links: ArrayList<Pair<String, View.OnClickListener>>) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            if (!link.first.contains("@All")) {
                val clickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(textPaint: TextPaint) {
                        // use this to change the link color
                        textPaint.color = textPaint.linkColor
                        // toggle below value to enable/disable
                        // the underline shown below the clickable text
                        textPaint.isUnderlineText = false
                    }

                    override fun onClick(view: View) {
                        Selection.setSelection((view as TextView).text as Spannable, 0)
                        view.invalidate()
                        link.second.onClick(view)
                    }

                }
                startIndexOfLink = this.text.toString().indexOf(
                    link.first,
                    startIndexOfLink + 1
                )
                if (startIndexOfLink != -1) {
                    spannableString.setSpan(
                        clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } else {
                val clickableSpan = object : ClickableSpan() {
                    override fun updateDrawState(textPaint: TextPaint) {
                        // use this to change the link color
                        textPaint.color = textPaint.linkColor
                        // toggle below value to enable/disable
                        // the underline shown below the clickable text
                        textPaint.isUnderlineText = false
                    }

                    override fun onClick(view: View) {
                    }

                }
                startIndexOfLink = this.text.toString().indexOf(
                    link.first,
                    startIndexOfLink + 1
                )
                if (startIndexOfLink != -1) {
                    spannableString.setSpan(
                        clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    fun TextView.setTagName(
        listTagName: ArrayList<TagName>,
        @ColorInt color: Int
    ) {
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        listTagName.forEach {
            val nameTagName = String.format(
                "%s%s", "@", it.full_name ?: ""
            )
            startIndexOfLink = this.text.toString().indexOf(
                nameTagName,
                startIndexOfLink + 1
            )
            spannableString.setSpan(
                ForegroundColorSpan(color), startIndexOfLink, startIndexOfLink + nameTagName.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    fun setMessageItemChat(
        context: Context,
        user: User,
        group: Group,
        textView: TextView,
        lastName: String
    ) {
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        when (group.last_message_type) {
            TechResEnumChat.TYPE_TEXT.toString() -> {
                if (group.member.tag_name != "") {
                    textView.text = group.member.tag_name ?: ""
                } else {
                    val listTagName = group.list_tag_name
                    var message = group.last_message
                    listTagName?.let {
                        it.forEach { tagName ->
                            message = message?.replace(
                                tagName.key_tag_name.toString(),
                                "@" + tagName.full_name
                            )
                        }
                    }
                    textView.text =
                        String.format(
                            "%s%s %s",
                            lastName,
                            context.resources.getString(R.string.two_dots),
                            message
                        )
                }

            }
            TechResEnumChat.TYPE_IMAGE.toString() -> {
                textView.text =
                    String.format(
                        "%s%s %s",
                        lastName,
                        context.resources.getString(R.string.two_dots),
                        context.resources.getString(R.string.send_a_photo)
                    )
            }
            TechResEnumChat.TYPE_STICKER.toString() -> {
                textView.text =
                    String.format(
                        "%s%s %s",
                        lastName,
                        context.resources.getString(R.string.two_dots),
                        context.resources.getString(R.string.send_a_sticker)
                    )
            }
            TechResEnumChat.TYPE_VIDEO.toString() -> {
                textView.text =
                    String.format(
                        "%s%s %s",
                        lastName,
                        context.resources.getString(R.string.two_dots),
                        context.resources.getString(R.string.send_a_video)
                    )
            }
            TechResEnumChat.TYPE_AUDIO.toString() -> {
                textView.text =
                    String.format(
                        "%s%s %s",
                        lastName,
                        context.resources.getString(R.string.two_dots),
                        context.resources.getString(R.string.send_a_audio)
                    )
            }
            TechResEnumChat.TYPE_FILE.toString() -> {
                textView.text =
                    String.format(
                        "%s%s %s",
                        lastName,
                        context.resources.getString(R.string.two_dots),
                        context.resources.getString(R.string.send_a_file)
                    )
            }
            TechResEnumChat.TYPE_REPLY.toString() -> {
                if (group.member.tag_name != "") {
                    textView.text = group.member.tag_name
                } else {
                    textView.text = String.format(
                        "%s%s %s",
                        lastName,
                        context.resources.getString(R.string.two_dots),
                        context.resources.getString(R.string.send_a_reply)
                    )
                }

            }
            TechResEnumChat.TYPE_PINNED.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.pinned_user)
                )
            }
            TechResEnumChat.TYPE_ADD_NEW_USER.toString() -> {
//                textView.text = String.format(
//                    "%s%s %s",
//                    lastName,
//                    context.resources.getString(R.string.two_dots),
//                    context.getString(R.string.add_member)
//                )
                if ((group.last_message ?: "").contains(user.name ?: "")) {
                    val message = group.last_message?.replace(
                        group.user_name_last_message ?: "", context.getString(
                            R.string.you
                        )
                    )
                    textView.text = message ?: ""
                } else {
                    textView.text = group.last_message
                }

            }
            TechResEnumChat.TYPE_KICK_MEMBER.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.kick_member)
                )
            }
            TechResEnumChat.TYPE_AUTHORIZE.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.authorize_member)
                )
            }
            TechResEnumChat.TYPE_REMOVE_PINNED.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_remove_pinned)
                )
            }
            TechResEnumChat.TYPE_REVOKE.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_revoke_message)
                )
            }
            TechResEnumChat.TYPE_GROUP_NAME.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_update_name)
                )
            }
            TechResEnumChat.TYPE_AVATAR.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_update_avatar)
                )
            }
            TechResEnumChat.TYPE_BACKGROUND.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_update_background)
                )
            }
            TechResEnumChat.TYPE_LEAVE_GROUP.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_member_leave_group)
                )
            }
            TechResEnumChat.TYPE_FILE.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.sent_file_to_message)
                )
            }
            TechResEnumChat.TYPE_SHARE.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_message_share)
                )
            }
            TechResEnumChat.TYPE_CREATE_GROUP.toString() -> {
                textView.text = group.last_message
            }
            TechResEnumChat.TYPE_CREATE_GROUP_PERSONAL.toString() -> {
//                if (lastName == context.getString(R.string.you)) {
//                    textView.text = String.format(
//                        "%s%s %s",
//                        lastName,
//                        context.resources.getString(R.string.two_dots),
//                        message
//                    )
//                } else {
//                    textView.text = String.format(
//                        "%s%s %s",
//                        lastName,
//                        context.resources.getString(R.string.two_dots),
//                        message
//                    )
//                }
                textView.text = group.last_message
            }
            TechResEnumChat.TYPE_LINK.toString() -> {
                textView.text = String.format(
                    "%s%s %s",
                    lastName,
                    context.resources.getString(R.string.two_dots),
                    context.getString(R.string.last_message_link)
                )
            }
            TechResEnumVideoCall.TYPE_CALL_PHONE.toString() -> {
                if (group.user_last_message_id == user.id){
                    textView.text = context.getString(R.string.last_message_call_phone)
                    when(group.call_status){
                        TechResEnumVideoCall.CALL_COMPLETE.toString() ->{
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.decor_csc_voicecall_out_small,
                                0,
                                0,
                                0
                            )
                        }
                        TechResEnumVideoCall.CALL_REJECT.toString() ->{
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icn_csc_voicecall_cancel_small,
                                0,
                                0,
                                0
                            )
                        }
                        else ->{
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.decor_csc_voicecall_out_miss_small,
                                0,
                                0,
                                0
                            )
                        }
                    }
                }else{
                    when(group.call_status){
                        TechResEnumVideoCall.CALL_COMPLETE.toString() ->{
                            textView.text = context.getString(R.string.call_phone_to)
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.decor_csc_voicecall_in_small,
                                0,
                                0,
                                0
                            )
                        }
                        TechResEnumVideoCall.CALL_CANCEL.toString(), TechResEnumVideoCall.CALL_NO_ANSWER.toString() ->{
                            textView.text = context.getString(R.string.last_message_call_phone_miss_call)
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_miss_call_audio,
                                0,
                                0,
                                0
                            )
                        }
                        else ->{
                            textView.text = context.getString(R.string.call_phone_to)
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icn_csc_voicecall_cancel_small,
                                0,
                                0,
                                0
                            )
                        }
                    }
                }
            }

            TechResEnumVideoCall.TYPE_CALL_VIDEO.toString() -> {
                if (group.user_last_message_id == user.id){
                    textView.text = context.getString(R.string.last_message_call_video)
                    when(group.call_status){
                        TechResEnumVideoCall.CALL_COMPLETE.toString() ->{
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.decor_csc_videocall_out_small,
                                0,
                                0,
                                0
                            )
                        }
                        TechResEnumVideoCall.CALL_REJECT.toString() ->{
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icn_csc_videocall_cancel_small,
                                0,
                                0,
                                0
                            )
                        }
                        else ->{
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.decor_csc_videocall_miss_small,
                                0,
                                0,
                                0
                            )
                        }
                    }
                }else{
                    when(group.call_status){
                        TechResEnumVideoCall.CALL_COMPLETE.toString() ->{
                            textView.text = context.getString(R.string.call_video_to)
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.decor_csc_videocall_in_small,
                                0,
                                0,
                                0
                            )
                        }
                        TechResEnumVideoCall.CALL_CANCEL.toString(), TechResEnumVideoCall.CALL_NO_ANSWER.toString() ->{
                            textView.text = context.getString(R.string.last_message_call_video_miss_call)
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icn_csc_videocall_miss_small,
                                0,
                                0,
                                0
                            )
                        }
                        else ->{
                            textView.text = context.getString(R.string.call_video_to)
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icn_csc_videocall_cancel_small,
                                0,
                                0,
                                0
                            )
                        }
                    }
                }
            }

            TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                textView.text =
                    String.format(
                        "%s: %s",
                        lastName,
                        group.last_message ?: ""
                    )
            }
            TechResEnumChat.TYPE_COMPLETE_BILL.toString() -> {
                textView.text =
                    group.last_message ?: ""
            }
            else -> {
                textView.text =
                    String.format(
                        "%s: %s",
                        lastName,
                        group.last_message ?: ""
                    )
            }
        }
    }

    fun setMessageViewer(
        context: Context,
        recyclerView: RecyclerView,
        dataSource: ArrayList<MessageViewer>
    ) {
        val adapter = MessageViewerAdapter(context)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, true)
        recyclerView.adapter = adapter
        adapter.setDataSource(dataSource)
    }

    fun setMessageViewerSeen(
        context: Context,
        recyclerView: RecyclerView,
        dataSource: ArrayList<MessageViewer>,
        viewerMessageHandler: ViewerMessageHandler?
    ) {
        val adapter = ViewerInformationAdapter(context)
        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = adapter
        adapter.setViewerMessageHandler(viewerMessageHandler)
        adapter.setDataSource(dataSource)
    }

    fun setReactionClick(message: MessagesByGroup): Int {
        return when (message.reactions.last_reactions) {
            TechResEnumChat.TYPE_REACTION_LIKE.toString().toInt() -> {
                R.drawable.ic_like
            }
            TechResEnumChat.TYPE_REACTION_LOVE.toString().toInt() -> {
//                R.drawable.ic_fb_love
                R.drawable.ic_heart
            }
            TechResEnumChat.TYPE_REACTION_SMILE.toString().toInt() -> {
                R.drawable.ic_smile
            }
            TechResEnumChat.TYPE_REACTION_WOW.toString().toInt() -> {
                R.drawable.ic_wow
            }
            TechResEnumChat.TYPE_REACTION_ANGRY.toString().toInt() -> {
                R.drawable.ic_angry
            }
            TechResEnumChat.TYPE_REACTION_SAD.toString().toInt() -> {
                R.drawable.ic_sad
            }
            else -> {
//                R.drawable.ic_fb_love
                R.drawable.ic_heart_twinkle
            }
        }
    }

    fun flyEmoji(resId: Int, activity: Activity?, container: ViewGroup, x: Float, y: Float) {
        val animation = ZeroGravityAnimation()
        animation.setCount(1)
        animation.setScalingFactor(0.2f)
        animation.setOriginationDirection(Direction.BOTTOM)
        animation.setDestinationDirection(Direction.TOP)
        animation.setImage(resId)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                //onAnimationStart
            }

            override fun onAnimationEnd(animation: Animation?) {
                //onAnimationEnd
            }

            override fun onAnimationRepeat(animation: Animation?) {
                //onAnimationRepeat
            }

        }
        )
        animation.play(activity, container, x, y)
    }

    fun setImageFile(imageView: ImageView, mineType: String?) {
        when (mineType) {
            TechResEnumChat.TYPE_JPEG.toString(), TechResEnumChat.TYPE_JPG.toString(), TechResEnumChat.TYPE_SVG.toString(), TechResEnumChat.TYPE_PNG.toString()
            -> {
                imageView.setImageResource(R.drawable.icon_file_photo)
            }
            TechResEnumChat.TYPE_DOCX.toString(), TechResEnumChat.TYPE_DOC.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_word)
            }
            TechResEnumChat.TYPE_AI.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_ai)
            }
            TechResEnumChat.TYPE_DMG.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_dmg)
            }
            TechResEnumChat.TYPE_XLSX.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_excel)
            }
            TechResEnumChat.TYPE_HTML.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_html)
            }
            TechResEnumChat.TYPE_MP3.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_music)
            }
            TechResEnumChat.TYPE_PDF.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_pdf)
            }
            TechResEnumChat.TYPE_PPTX.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_powerpoint)
            }
            TechResEnumChat.TYPE_PSD.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_psd)
            }
            TechResEnumChat.TYPE_REC.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_record)
            }
            TechResEnumChat.TYPE_EXE.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_setup)
            }
            TechResEnumChat.TYPE_SKETCH.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_sketch)
            }
            TechResEnumChat.TYPE_TXT.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_txt)
            }
            TechResEnumChat.TYPE_MP4.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_video)
            }
            TechResEnumChat.TYPE_XML.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_xml)
            }
            TechResEnumChat.TYPE_ZIP.toString(), TechResEnumChat.TYPE_RAR.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_zip)
            }
            TechResEnumChat.TYPE_SMMX.toString() -> {
                imageView.setImageResource(R.drawable.icon_file_smmx)
            }
            else -> {
                imageView.setImageResource(R.drawable.icon_file_attach)
            }
        }
    }

    fun setLayoutParams(
        view: View,
        height: Int,
        width: Int
    ) {
        val layoutParams = view.layoutParams
        val maxLength = TechResApplication.widthView / 2
        try {
            when {
                height > width -> {
                    val targetHeight = TechResApplication.heightView / 2
//                    if (height <= targetHeight) { // if image already smaller than the required height
//                        return
//                    }
                    val aspectRatio = width.toDouble() / height.toDouble()
                    var targetWidth = (targetHeight * aspectRatio).toInt() + dpToPx(50)
                    if (targetWidth > maxLength) {
                        targetWidth = maxLength
                    }
                    layoutParams?.height = targetHeight
                    layoutParams?.width = targetWidth
                    view.layoutParams = layoutParams
                }
                height == width -> {
                    layoutParams?.height = maxLength
                    layoutParams?.width = maxLength
                    view.layoutParams = layoutParams
                }
                else -> {
                    val targetWidth = maxLength + TechResApplication.widthView / 4
//                    if (width <= targetWidth) { // if image already smaller than the required height
//                        return
//                    }
                    val aspectRatio = height.toDouble() / width.toDouble()
                    val targetHeight = (targetWidth * aspectRatio).toInt() + dpToPx(20)
                    layoutParams?.height = targetHeight
                    layoutParams?.width = targetWidth
                    view.layoutParams = layoutParams
                }
            }
        } catch (e: Exception) {
            //e
        }
    }

    fun checkTimeOld(strDateOld: String?, strDate: String?): Boolean {
        return try {
            if (strDateOld == null && strDate == null) {
                return false
            }
            @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy/MM/dd hh:mm")
            try {
                WriteLog.d("strDate :", format.parse(
                    strDate
                ).time.toString())
                WriteLog.d("strDateOld :", format.parse(
                    strDateOld
                ).time.toString())

                return Math.abs(
                    format.parse(
                        strDateOld
                    ).time - format.parse(strDate).time
                ) / 1000 / 60 < 5
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            false
        } catch (e: Exception) {
            false
        }
    }

    fun checkTimeMessages(
        str_date: String?,
        timeMessage: TextView,
        position: Int,
        dataSource: ArrayList<MessagesByGroup>
    ) {
        timeMessage.text = TimeFormatHelper.getDateFromStringDay(str_date)
        if (position > 0) {
            if (checkTimeOld(dataSource[position - 1].created_at, str_date)
                && dataSource[position].sender.member_id == dataSource[position - 1].sender.member_id
            ) {
                timeMessage.hide()
            } else {
                timeMessage.show()
            }
        } else {
            timeMessage.show()
        }
    }

    fun setViewTimeLeft(view: View, containerView: View, top: Float = 0f) {
        val lp = containerView.layoutParams as ViewGroup.MarginLayoutParams
        if (view.visibility == View.VISIBLE) {
            lp.setMargins(dpToPx(8), dpToPx(top.toInt()), 0, dpToPx(8))
        } else {
            lp.setMargins(dpToPx(8), dpToPx(4), 0, dpToPx(4))
        }
    }

    fun setViewTimeRight(view: View, containerView: View) {
        val lp = containerView.layoutParams as ViewGroup.MarginLayoutParams
        if (view.visibility == View.VISIBLE) {
            lp.setMargins(0, 0, dpToPx(16), dpToPx(8))
        } else {
            lp.setMargins(0, 0, dpToPx(16), dpToPx(4))
        }
    }

    fun configRecyclerView(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?
    ) {
        val centerLayoutManager = CenterLayoutManager(
            TechResApplication.applicationContext(),
            RecyclerView.VERTICAL,
            true
        )
        recyclerView.layoutManager = centerLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(50)
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter
    }

    @Throws(ClassNotFoundException::class)
    fun connectToRoom(
        context: Context,
        groupID: String?,
        idReceiver: Int,
        nameReceiver: String?,
        avatarReceiver: String?,
        idCreate: Int,
        typeOption: Int,
        message_type: String,
        app_call: String?,
        key_room_receive: String?
    ) {
        val key_room = if (key_room_receive == ""){
            Utils.getRandomString(20)
        }else{
            key_room_receive
        }
        val intent = Intent(context, CallActivity::class.java)
        intent.putExtra(TechresEnum.EXTRA_NAME_PERSONAL.toString(), nameReceiver)
        intent.putExtra(TechresEnum.EXTRA_AVATAR_PERSONAL.toString(), avatarReceiver)


        intent.putExtra(TechresEnum.EXTRA_ID_GROUP.toString(), groupID)
        intent.putExtra(TechresEnum.EXTRA_KEY_ROOM.toString(), key_room)
        intent.putExtra(TechresEnum.EXTRA_MESSAGE_TYPE.toString(), message_type)
        intent.putExtra(TechresEnum.EXTRA_MEMBER_CREATE.toString(), idCreate)
        intent.putExtra(TechresEnum.EXTRA_TYPE_OPTION.toString(), typeOption)

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun connectToRoomFromBroadcast(
        context: Intent,
        groupID: String?,
        nameReceiver: String?,
        avatarReceiver: String?,
        idCreate: Int,
        typeOption: Int,
        key_room: String?,
        message_type: String?
    ) {
        context.putExtra(TechresEnum.EXTRA_ID_GROUP.toString(), groupID)
        context.putExtra(TechresEnum.EXTRA_NAME_PERSONAL.toString(), nameReceiver)
        context.putExtra(TechresEnum.EXTRA_AVATAR_PERSONAL.toString(), avatarReceiver)
        context.putExtra(TechresEnum.EXTRA_KEY_ROOM.toString(), key_room)
        context.putExtra(TechresEnum.EXTRA_TYPE_OPTION.toString(), typeOption)
        context.putExtra(TechresEnum.EXTRA_MEMBER_CREATE.toString(), idCreate)
        context.putExtra(TechresEnum.EXTRA_MESSAGE_TYPE.toString(), message_type)
        startActivity(context)
    }

    fun getLinkPhoto(photo: String?, domain: String?): String? {
        return java.lang.String.format("%s%s", domain, photo)
    }

    fun callPhotoAvatar(url: String?, view: ImageView?, domain: String?) {
        if (view != null) {
            Glide.with(TechResApplication.instance!!.applicationContext)
                .asBitmap()
                .load(getLinkPhoto(url, domain))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(
                    RequestOptions().placeholder(R.drawable.logo_aloline_placeholder)
                        .error(R.drawable.logo_aloline_placeholder)
                )
                .centerCrop()
                .into(view)
        }
    }
    fun callPhotoLocal(url: String?, view: ImageView?) {
        if (view != null) {
            Glide.with(TechResApplication.instance!!.applicationContext)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .apply(
                    RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                )
                .centerCrop()
                .into(view)
        }
    }

    fun emitSocket(key: String?, `object`: Any?) {
        try {
            val gson = Gson()
            Timber.e("%s : %s", key, gson.toJson(`object`))

            mSocket!!.emit(key, JSONObject(gson.toJson(`object`)))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun emitSocket(key: String?, `object`: JSONObject?) {
        mSocket!!.emit(key, `object`)
    }

    fun getPipRatio(): Rational? {
        //   anything with aspect ration below 0.5 and above 2.5 (roughly) will be
        // unpractical or unpleasant to use
        val metrics: DisplayMetrics = getResources(
            TechResApplication.applicationContext()
        )!!.displayMetrics
        Timber.e("numerator %s", metrics.xdpi.roundToInt())
        return Rational(metrics.xdpi.roundToInt() + 50, metrics.xdpi.roundToInt() + 200)
    }

    fun configRecyclerHoziView(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>?
    ) {
        val preCachingLayoutManager = PreCachingLayoutManager(
            TechResApplication.applicationContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        preCachingLayoutManager.setType(0)
        recyclerView.layoutManager = preCachingLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(50)
        recyclerView.isDrawingCacheEnabled = true
        recyclerView.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.adapter = adapter
    }

    fun getDecimalFormattedString(value: String): String? {
        val lst = StringTokenizer(value, ".")
        var str1 = value
        var str2 = ""
        if (lst.countTokens() > 1) {
            str1 = lst.nextToken()
            str2 = lst.nextToken()
        }
        var str3 = StringBuilder()
        var i = 0
        var j = -1 + str1.length
        if (str1[-1 + str1.length] == '.') {
            j--
            str3 = StringBuilder(".")
        }
        var k = j
        while (true) {
            if (k < 0) {
                if (str2.length > 0) str3.append(".").append(str2)
                return str3.toString()
            }
            if (i == 3) {
                str3.insert(0, ",")
                i = 0
            }
            str3.insert(0, str1[k])
            i++
            k--
        }
    }

    fun getGlide(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(it)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        string
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .centerCrop()
                .placeholder(R.drawable.logo_aloline_placeholder)
                .error(R.drawable.logo_aloline_placeholder)
                .into(it)
        }
    }

    fun callPhoto(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(imageView)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        string
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(imageView)
        }
    }

    fun callPhotoBirtday(imageView: ImageView?, string: String?, configNodeJs: ConfigNodeJs) {
        imageView?.let {
            Glide.with(imageView)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        string
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(imageView)
        }
    }

    open fun getNameFileFormatTime(path: String?): String? {
        return String.format(
            "%s.%s",
            System.currentTimeMillis().toString() + Utils.getRandomString(5),
            path?.let { FileUtils.getMimeType(it) }
        )
    }
    fun isNumeric(toCheck: String): Boolean {
        val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
        return toCheck.matches(regex)
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor.use {
                if(cursor!!.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        return uri.path!!.substring(uri.path!!.lastIndexOf('/') + 1)
    }

    fun fileFromContentUri(context: Context, contentUri: Uri): File {
        // Preparing Temp file name
        val fileExtension = getFileExtension(context, contentUri)
        val fileName = "temp_file" + if (fileExtension != null) ".$fileExtension" else ""

        // Creating Temp file
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        try {
            val oStream = FileOutputStream(tempFile)
            val inputStream = context.contentResolver.openInputStream(contentUri)

            inputStream?.let {
                copy(inputStream, oStream)
            }

            oStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tempFile
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        val fileType: String? = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(fileType)
    }

    @Throws(IOException::class)
    private fun copy(source: InputStream, target: OutputStream) {
        val buf = ByteArray(8192)
        var length: Int
        while (source.read(buf).also { length = it } > 0) {
            target.write(buf, 0, length)
        }
    }
}