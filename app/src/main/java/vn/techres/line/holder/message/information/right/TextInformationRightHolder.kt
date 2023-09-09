package vn.techres.line.holder.message.information.right

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.databinding.ItemTextInfomationRightBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ChooseNameUserHandler
import vn.techres.line.interfaces.chat.ViewerMessageHandler

class TextInformationRightHolder(private val binding: ItemTextInfomationRightBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context: Context,
        dataSource: ArrayList<MessagesByGroup>,
        receivedViewer: ArrayList<MessageViewer>,
        chatGroupHandler: ChatGroupHandler?,
        chooseNameUserHandler: ChooseNameUserHandler?,
        viewerMessageHandler: ViewerMessageHandler?,
        receivedViewerNotSeen: ArrayList<MessageViewer>
    ) {
        val message = dataSource[bindingAdapterPosition]
        //set message

        ChatUtils.setClickTagName(
            message.message.toString(),
            message.list_tag_name,
            binding.tvMessage,
            chooseNameUserHandler
        )
        //
        binding.tvTimeText.text = TimeFormatHelper.getDateFromStringDay(message.created_at)

//        if (message.reactions.reactions_count == 0) {
//            binding.reaction.lnReaction.hide()
//            binding.reaction.imgReactionPress.show()
//            binding.reaction.imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
//            if (bindingAdapterPosition > 0) {
//                if (dataSource[bindingAdapterPosition].sender.member_id == dataSource[bindingAdapterPosition - 1].sender.member_id) {
//                    binding.lnReactionContainer.hide()
//                } else {
//                    binding.lnReactionContainer.show()
//                }
//            } else {
//                binding.lnReactionContainer.show()
//            }
//        } else {
//            ChatUtils.setReaction(
//                binding.lnReactionContainer,
//                binding.reaction,
//                message.reactions
//            )
//        }
//        binding.reaction.imgReactionPress.hide()


        //timer
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.hide()
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