package vn.techres.line.adapter.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendChatBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.FriendOnlineHandler

class FriendsOnlineChatAdapter(
    var context: Context
) : RecyclerView.Adapter<FriendsOnlineChatAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var friendOnlineHandler: FriendOnlineHandler? = null

    fun setFriendOnlineHandler(friendOnlineHandler: FriendOnlineHandler) {
        this.friendOnlineHandler = friendOnlineHandler
    }

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    inner class ItemHolder(val binding: ItemFriendChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemFriendChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            val item = dataSource[position]

            getImage(binding.imgFiendchat, item.avatar.thumb, nodeJs)
            binding.imgUserOnline.setImageResource(R.drawable.circle_green)
            binding.tvName.text = item.full_name

            holder.itemView.setOnClickListener {
                friendOnlineHandler!!.onCreateChat(position)
            }
        }

    }
}