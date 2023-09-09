package vn.techres.line.holder.message.information

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.helper.utils.ChatUtils.checkSenderMessage
import vn.techres.line.helper.utils.ChatUtils.setClickTagName
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User

class TextInformationHolder(view : View) : RecyclerView.ViewHolder(view) {
    var cvText: CardView = view.findViewById(R.id.cvText)
    var imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
    var tvNameText: TextView = view.findViewById(R.id.tvNameText)
    var tvMessage: TextView = view.findViewById(R.id.tvMessage)
    var tvTimeText: TextView = view.findViewById(R.id.tvTimeText)
    var rlMessage: RelativeLayout = view.findViewById(R.id.rlMessage)
    //reply
    var imgReplyAction: ImageView = view.findViewById(R.id.imgReplyAction)

    //today
    var tvTimeHeader: TextView = view.findViewById(R.id.tvTimeHeader)

    //reaction
    var lnReactionContainer: LinearLayout = view.findViewById(R.id.lnReactionContainer)
    var lnReaction: LinearLayout = view.findViewById(R.id.lnReaction)
    var imgReactionOne: ImageView = view.findViewById(R.id.imgReactionOne)
    var imgReactionTwo: ImageView = view.findViewById(R.id.imgReactionTwo)
    var imgReactionThree: ImageView = view.findViewById(R.id.imgReactionThree)

    //press reaction
    var imgReactionPress: ImageView = view.findViewById(R.id.imgReactionPress)

    //count
    var tvReactionCount: TextView = view.findViewById(R.id.tvReactionCount)

    fun bin(
        configNodeJs: ConfigNodeJs,
        user: User,
        position: Int,
        dataSource : ArrayList<MessagesByGroup>,
        chatGroupHandler : ChatGroupHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?
    ){
        val message = dataSource[position]
        //reply
//        holder.imgReplyAction.background = MessageSwipeActionDrawable(context)
        //set message
        setClickTagName(
            message.message.toString(),
            message.list_tag_name,
            tvMessage,
            chooseNameUserHandler
        )
        //
        tvTimeText.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        tvNameText.text = message.sender.full_name
        getImage(imgAvatar, message.sender.avatar?.medium, configNodeJs)

        if (!checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSource.size) {
                if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id && message.today != 1) {
                    tvNameText.visibility = View.GONE
                    cvText.visibility = View.INVISIBLE
                } else {
                    tvNameText.visibility = View.VISIBLE
                    cvText.visibility = View.VISIBLE
                }
            }
        } else {
            cvText.visibility = View.GONE
            tvNameText.visibility = View.GONE
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
        //timer
        if (message.today == 1) {
            tvTimeHeader.visibility = View.VISIBLE
            tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else tvTimeHeader.visibility = View.GONE
        //drag
    }
}