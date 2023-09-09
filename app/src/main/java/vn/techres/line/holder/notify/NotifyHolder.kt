package vn.techres.line.holder.notify

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.Notifications
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemNotificationBinding
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.makeLinks
import vn.techres.line.interfaces.NotificationsHandler

class NotifyHolder(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bin(data: Notifications, notificationsHandler: NotificationsHandler?, configNodeJs: ConfigNodeJs) {
        when(data.is_viewed){
            0 ->{
                binding.imgBackground.setBackgroundResource(R.color.white)
            }
            1 ->{
                binding.imgBackground.setBackgroundResource(R.color.orange_20)
            }
            else ->{
                binding.imgBackground.setBackgroundResource(R.color.white)
            }
        }

        //0: friend: thông báo nhận được lời mời kết bạn
        //1: friend: thông báo ai đó đã chấp nhận lời mời kết bạn
        //2: branch review: tất cả các thông báo liên quan đến branch review (comment, post, reaction, reply comment)
        //3: change info: người dùng thay đối tên
        binding.txtStatusFriend.visibility = View.GONE
        when(data.type){
            0 ->{
                binding.imgStatusFriend.visibility = View.VISIBLE
                binding.imgStatusNewsFeed.visibility = View.GONE
                binding.lnFriend.visibility = View.VISIBLE
            }
            1 ->{
                binding.imgStatusFriend.visibility = View.VISIBLE
                binding.imgStatusNewsFeed.visibility = View.GONE
                binding.lnFriend.visibility = View.GONE
            }
            2 ->{
                binding.imgStatusFriend.visibility = View.GONE
                binding.imgStatusNewsFeed.visibility = View.VISIBLE
                binding.lnFriend.visibility = View.GONE
            }
            else ->{
                binding.imgStatusFriend.visibility = View.GONE
                binding.imgStatusNewsFeed.visibility = View.GONE
                binding.lnFriend.visibility = View.GONE
            }
        }

        Utils.getGlide(
            binding.imgAvatar,
            data.customer.avatar.original, configNodeJs
        )
        val s = data.customer.full_name + " " + data.content

        val links = ArrayList<Pair<String, View.OnClickListener>>()
        binding.tvContentNotify.text = s
        if (s.contains(data.customer.full_name ?: "")) {
            data.customer.full_name?.let {
                val pair = Pair(
                    it, View.OnClickListener {
                        notificationsHandler!!.markNotification(data)
                    }
                )
                links.add(pair)
            }
            binding.tvContentNotify.makeLinks(links)
        }


        binding.tvTimeNotify.text = data.created_at

        binding.tvContentNotify.setOnClickListener {
            notificationsHandler!!.markNotification(data)
        }

        itemView.setOnClickListener {
            notificationsHandler!!.markNotification(data)
        }

        itemView.setOnLongClickListener {
            notificationsHandler!!.onAction(data)
            true
        }

        binding.btnRefuseFriend.setOnClickListener {
            notificationsHandler!!.onRefuseFriend(data)
            binding.lnFriend.visibility = View.GONE
            binding.txtStatusFriend.visibility = View.VISIBLE
            binding.txtStatusFriend.text = "Đã từ chối kết bạn"
        }

        binding.btnAcceptFriend.setOnClickListener {
            notificationsHandler!!.onAcceptFriend(data)
            binding.lnFriend.visibility = View.GONE
            binding.txtStatusFriend.visibility = View.VISIBLE
            binding.txtStatusFriend.text = "Đã đồng ý kết bạn"
        }
    }



}