package vn.techres.line.holder.message.information.right

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessageViewer
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemStickerInfomationRightBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.interfaces.chat.ViewerMessageHandler

class StickerInformationRightHolder(private val binding  : ItemStickerInfomationRightBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context : Context,
        nodeJs: ConfigNodeJs,
        dataSource : ArrayList<MessagesByGroup>,
        receivedViewer : ArrayList<MessageViewer>,
        chatGroupHandler: ChatGroupHandler?,
        viewerMessageHandler : ViewerMessageHandler?,
        receivedViewerNotSeen : ArrayList<MessageViewer>
    ){
        val message = dataSource[bindingAdapterPosition]
        binding.tvTimeSticker.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        Glide.with(context)
            .load(ChatUtils.getUrl(message.files[0].link_original!!, nodeJs))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.ic_sticker)
            .error(R.drawable.ic_sticker)
            .into(binding.imgSticker)
        if (message.today == 1) {
            binding.tvTimeHeader.visibility = View.VISIBLE
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else binding.tvTimeHeader.visibility = View.GONE

//        if (message.reactions.reactions_count == 0) {
//            binding.lnReactionContainer.visibility = View.GONE
//        } else {
//            ChatUtils.setReaction(
//                binding.lnReactionContainer,
//                binding.reaction,
//                message.reactions
//            )
//        }
//        binding.reaction.imgReactionPress.visibility = View.GONE

        /**
         * Message viewed
         * */
        binding.tvSeen.hide()
        binding.tvReceived.hide()
        binding.rcViewerSeen.hide()
        binding.rcViewerReceived.hide()
        if(bindingAdapterPosition == 0){
            binding.tvStatusView.text = context.getString(R.string.received)
            if(receivedViewerNotSeen.size > 0){
                binding.tvReceived.show()
                binding.rcViewerReceived.show()
                ChatUtils.setMessageViewerSeen(context, binding.rcViewerReceived, receivedViewerNotSeen, viewerMessageHandler)
            }
            if (receivedViewer.size > 0){
                binding.tvSeen.show()
                binding.rcViewerSeen.show()
                binding.tvStatusView.text = context.getString(R.string.seen)
                ChatUtils.setMessageViewerSeen(context, binding.rcViewerSeen, receivedViewer, viewerMessageHandler)
            }
        }
    }
}