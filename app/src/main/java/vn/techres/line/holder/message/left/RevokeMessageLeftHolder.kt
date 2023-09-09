package vn.techres.line.holder.message.left

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemRevokeChatLeftBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx

class RevokeMessageLeftHolder(private val binding : ItemRevokeChatLeftBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context : Context,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource : ArrayList<MessagesByGroup>
    ){
        val message = dataSource[position]
        if (message.today == 1) {
            binding.tvTimeHeader.show()
            binding.tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else {
            binding.tvTimeHeader.hide()
        }
        binding.tvNameRevoke.text = message.sender.full_name
        getImage(binding.imgAvatar, message.sender.avatar?.medium, configNodeJs)
//        binding.tvTimeRevoke.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeRevoke, position, dataSource)
        val lp = binding.rlRevoke.layoutParams as ViewGroup.MarginLayoutParams
        if (position > 0) {
            if (ChatUtils.checkTimeOld(dataSource[position - 1].created_at, message.created_at)
                && dataSource[position].message_type == dataSource[position - 1].message_type) {
                lp.setMargins(dpToPx(8), 0, 0, dpToPx(4))
            }else{
                lp.setMargins(dpToPx(8), 0, 0, dpToPx(14))
            }
        }else{
            lp.setMargins(dpToPx(8), 0, 0, dpToPx(14))
        }
        /**
         * Message viewed
         * */
        if(bindingAdapterPosition == 0){
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            binding.tvStatusView.text = context.getString(R.string.received)
            message.message_viewed.let {
                ChatUtils.setMessageViewer(context, binding.rcViewer, it)
                binding.rcViewer.show()
                binding.tvStatusView.hide()
            }
        }else{
            binding.rcViewer.hide()
            binding.tvStatusView.hide()
        }
    }
}