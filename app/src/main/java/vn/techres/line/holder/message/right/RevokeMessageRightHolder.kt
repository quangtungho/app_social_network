package vn.techres.line.holder.message.right

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.databinding.ItemRevokeChatRightBinding
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.ChatUtils.checkTimeOld
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx

class RevokeMessageRightHolder(private val binding : ItemRevokeChatRightBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
        context : Context,
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
//        binding.tvTimeRevoke.text = TimeFormatHelper.getDateFromStringDay(message.created_at)
        ChatUtils.checkTimeMessages(message.created_at, binding.tvTimeRevoke, position, dataSource)
        val lp = binding.rlRevoke.layoutParams as ViewGroup.MarginLayoutParams
        if (position > 0) {
            if (checkTimeOld(dataSource[position - 1].created_at, message.created_at)
                && dataSource[position].message_type == dataSource[position - 1].message_type) {
                lp.setMargins(dpToPx(8), 0, dpToPx(16), dpToPx(4))
            }else{
                lp.setMargins(dpToPx(8), 0, dpToPx(16), dpToPx(14))
            }
        }else{
            lp.setMargins(dpToPx(8), 0, dpToPx(16), dpToPx(14))
        }
        /**
         * Message viewed
         * */
        if(bindingAdapterPosition == 0){
            binding.rcViewer.hide()
            binding.tvStatusView.show()
            binding.tvStatusView.text = context.getString(R.string.received)
            message.message_viewed?.let {
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