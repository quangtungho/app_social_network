package vn.techres.line.holder.message.information

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.R
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User

class StickerInformationHolder(view : View) : RecyclerView.ViewHolder(view) {
    private var imgSticker: ImageView = view.findViewById(R.id.imgSticker)
    private var cvSticker: CardView = view.findViewById(R.id.cvSticker)
    private var imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
    private var tvNameSticker: TextView = view.findViewById(R.id.tvNameSticker)
    private var tvTimeSticker: TextView = view.findViewById(R.id.tvTimeSticker)

    //reaction
    private var lnReactionContainer: LinearLayout = view.findViewById(R.id.lnReactionContainer)
    private var lnReaction: LinearLayout = view.findViewById(R.id.lnReaction)
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
        context : Context,
        user: User,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource : ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?
    ){
        val message = dataSource[position]
//        holder.imgReplyAction.background = MessageSwipeActionDrawable(context)

        tvNameSticker.text = message.sender.full_name
        getImage(imgAvatar, message.sender.avatar?.medium, configNodeJs)
        tvTimeSticker.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        Glide.with(context)
            .load(ChatUtils.getUrl(message.files[0].link_original!!, configNodeJs))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(imgSticker)
        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSource.size) {
                if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id) {
                    tvNameSticker.visibility = View.GONE
                    cvSticker.visibility = View.INVISIBLE
                } else {
                    tvNameSticker.visibility = View.VISIBLE
                    cvSticker.visibility = View.VISIBLE
                }
            }
        } else {
            cvSticker.visibility = View.GONE
            tvNameSticker.visibility = View.GONE
        }
        if (message.today == 1) {
            tvTimeHeader.visibility = View.VISIBLE
            tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else tvTimeHeader.visibility = View.GONE

        if (message.reactions.reactions_count == 0) {
            lnReactionContainer.visibility = View.GONE
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