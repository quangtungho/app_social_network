package vn.techres.line.holder.message

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemNotificationCompleteBillBinding
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.ChooseOrderCustomerHandler

class NotificationCompleteBillHolder(private val binding: ItemNotificationCompleteBillBinding) :
    RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    fun bind(
        configNodeJs: ConfigNodeJs,
        messagesByGroup: MessagesByGroup,
        chooseOrderDetail: ChooseOrderCustomerHandler
    ) {

        itemView.setOnClickListener {
            messagesByGroup.message_complete_bill?.order_id?.let { it1 ->
                chooseOrderDetail.chooseOrder(
                    it1
                )
            }
        }
//        ChatUtils.getGlide(binding.avatar, messagesByGroup.sender.avatar?.medium, configNodeJs)

        binding.txtMoney.text = String.format("Tổng tiền: %s", ChatUtils.getDecimalFormattedString(messagesByGroup.message_complete_bill?.total_amount.toString()))
        binding.txtNameRestaurant.text = String.format("Nhà hàng: %s", messagesByGroup.message_complete_bill?.branch_name)
        binding.txtAddress.text = String.format("Địa chỉ: %s", messagesByGroup.message_complete_bill?.address)
        binding.txtPhone.text = String.format("Điện thoại: %s", messagesByGroup.message_complete_bill?.restaurant_phone)
        if (messagesByGroup.message_complete_bill?.payment_date!= ""){
            val a = messagesByGroup.message_complete_bill?.payment_date?.indexOf(" ")
            val date = a?.let { messagesByGroup.message_complete_bill?.payment_date?.substring(0, it) }
            val time = a?.let { messagesByGroup.message_complete_bill?.payment_date?.substring(it + 1) }
            binding.txtTime.text = "Ngày $date vào lúc $time"
        }

    }
}