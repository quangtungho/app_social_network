package vn.techres.line.holder.message.information

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import java.io.IOException

class ReplyInformationHolder(view : View) : RecyclerView.ViewHolder(view) {
    private var tvNameReply: TextView = view.findViewById(R.id.tvNameReply)
    private var cvReply: CardView = view.findViewById(R.id.cvReply)
    private var imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
    private var tvMessageReply: TextView = view.findViewById(R.id.tvMessageReply)
    private var tvTimeReply: TextView = view.findViewById(R.id.tvTimeReply)

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

    private var imgReply: ImageView = view.findViewById(R.id.imgReply)
    private var imgReplyLinkPlay: ImageView = view.findViewById(R.id.imgReplyLinkPlay)
    private var tvReplyName: TextView = view.findViewById(R.id.tvReplyName)
    private var tvReplyContent: TextView = view.findViewById(R.id.tvReplyContent)
    private var rlReplyThumbContainer: RelativeLayout = view.findViewById(R.id.rlReplyThumbContainer)
    fun bin(
        context : Context,
        user: User,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource : ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?
    ){
        val message = dataSource[position]
//        holder.imgReplyAction.background = MessageSwipeActionDrawable(context)

        //set message Reply
        ChatUtils.setClickTagName(
            message.message.toString(),
            message.list_tag_name,
            tvMessageReply,
            chooseNameUserHandler
        )
        //
        tvTimeReply.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        tvReplyName.text = message.message_reply?.sender?.full_name
        tvNameReply.text = message.sender.full_name
        getImage(imgAvatar, message.sender.avatar?.medium, configNodeJs)
        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSource.size) {
                if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id) {
                    tvNameReply.visibility = View.GONE
                    cvReply.visibility = View.INVISIBLE
                } else {
                    tvNameReply.visibility = View.VISIBLE
                    cvReply.visibility = View.VISIBLE
                }
            }
        } else {
            cvReply.visibility = View.GONE
            tvNameReply.visibility = View.GONE
        }
        if (message.today == 1) {
            tvTimeHeader.visibility = View.VISIBLE
            tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else tvTimeHeader.visibility = View.GONE
        if (message.message_reply?.files?.size != 0) {
            rlReplyThumbContainer.visibility = View.VISIBLE
        } else {
            rlReplyThumbContainer.visibility = View.GONE
        }
        if (message.message_reply?.status == 1) {
            when (message.message_reply?.message_type) {
                TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_REPLY.toString() -> {
                    ChatUtils.setClickTagName(
                        message.message_reply!!.message.toString(),
                        message.message_reply!!.list_tag_name,
                        tvReplyContent,
                        chooseNameUserHandler
                    )
                }
                TechResEnumChat.TYPE_IMAGE.toString() -> {
                    tvReplyContent.text = context.resources.getString(R.string.pinned_image)
                    imgReplyLinkPlay.visibility = View.GONE
                    getGlide(
                        imgReply,
                        message.message_reply!!.files[0].link_original,
                                configNodeJs
                    )
                }
                TechResEnumChat.TYPE_VIDEO.toString() -> {

                    tvReplyContent.text = context.resources.getString(R.string.pinned_video)
                    imgReplyLinkPlay.visibility = View.VISIBLE
                    try {
                        val bitmap =
                            Utils.retriveVideoFrameFromVideo(
                                ChatUtils.getUrl(
                                    message.message_reply!!.files[0].link_original!!,
                                    configNodeJs
                                )
                            )
                        imgReply.setImageBitmap(bitmap)
                    } catch (e: IOException) {

                    }
                }
                TechResEnumChat.TYPE_STICKER.toString() -> {
                    tvReplyContent.text =
                        context.resources.getString(R.string.pinned_sticker)
                    imgReplyLinkPlay.visibility = View.GONE
                    getGlide(
                        imgReply,
                        message.message_reply!!.files[0].link_original,
                                configNodeJs
                    )
                }
            }
        } else {
            tvReplyContent.text = context.resources.getString(R.string.revoke_message)
            rlReplyThumbContainer.visibility = View.GONE
        }

        if (message.reactions.reactions_count == 0) {
            lnReaction.visibility = View.GONE
            imgReactionPress.visibility = View.VISIBLE
            imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
            if (position > 0) {
                if (dataSource[position].sender.member_id == dataSource[position - 1].sender.member_id) {
                    lnReactionContainer.visibility = View.GONE
                } else {
                    lnReactionContainer.visibility = View.VISIBLE
                }
            } else {
                lnReactionContainer.visibility = View.VISIBLE
            }
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
    }
}