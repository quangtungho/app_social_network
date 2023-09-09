package vn.techres.line.holder.message.information.right

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemBusinessCardInfomationRightBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ViewerMessageHandler

class BusinessCardInformationRightHolder(private val binding: ItemBusinessCardInfomationRightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        configNodeJs: ConfigNodeJs,
        dataSources: ArrayList<MessagesByGroup>,
        receivedViewer: ArrayList<MessageViewer>,
        chatGroupHandler: ChatGroupHandler?,
        viewerMessageHandler: ViewerMessageHandler?,
        receivedViewerNotSeen : ArrayList<MessageViewer>
    ) {
        val message = dataSources[bindingAdapterPosition]

        binding.tvNameContact.text = message.message_phone?.full_name
        val content = SpannableString(message.message_phone?.phone)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.tvPhoneContact.text = content
        binding.tvContentPhoneContact.text = message.message_phone?.phone
        Utils.getImage(
            binding.imgAvatarContact,
            message.message_phone?.avatar?.medium,
            configNodeJs
        )
        //reaction
//        if (message.reactions.reactions_count == 0) {
//            binding.reaction.lnReaction.visibility = View.GONE
//            binding.reaction.imgReactionPress.visibility = View.VISIBLE
//            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
//            if (bindingAdapterPosition > 0) {
//                if (dataSources[bindingAdapterPosition].sender.member_id == dataSources[bindingAdapterPosition - 1].sender.member_id) {
//                    binding.lnReactionContainer.visibility = View.GONE
//                } else {
//                    binding.lnReactionContainer.visibility = View.VISIBLE
//                }
//            } else {
//                binding.lnReactionContainer.visibility = View.VISIBLE
//            }
//        } else {
//            ChatUtils.setReaction(
//                binding.lnReactionContainer,
//                binding.reaction,
//                message.reactions
//            )
//        }
        //timer
        if (message.today == 1) {
            binding.tvTimeHeader.visibility = View.VISIBLE
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.visibility = View.GONE

        binding.tvTimeBusinessCard.text = TimeFormatHelper.getDateFromStringDay(message.created_at)

//        binding.reaction.imgReactionPress.visibility = View.GONE

        binding.view.hide()
        binding.lnMain.hide()
        /**
         * Message viewed
         * */
        binding.tvSeen.hide()
        binding.tvReceived.hide()
        binding.rcViewerSeen.hide()
        binding.rcViewerReceived.hide()
        if (bindingAdapterPosition == 0) {
            binding.tvStatusView.text = context.getString(R.string.received)
            if (receivedViewerNotSeen.size > 0) {
                binding.tvReceived.show()
                binding.rcViewerReceived.show()
                ChatUtils.setMessageViewerSeen(
                    context,
                    binding.rcViewerReceived,
                    receivedViewerNotSeen,
                    viewerMessageHandler
                )
            }
            if (receivedViewer.size > 0) {
                binding.tvSeen.show()
                binding.rcViewerSeen.show()
                binding.tvStatusView.text = context.getString(R.string.seen)
                ChatUtils.setMessageViewerSeen(
                    context,
                    binding.rcViewerSeen,
                    receivedViewer,
                    viewerMessageHandler
                )
            }
        }
    }
}