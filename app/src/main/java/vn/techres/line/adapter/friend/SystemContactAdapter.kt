package vn.techres.line.adapter.friend

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendSystemContactBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.SystemContactHandler
import java.util.*
import java.util.stream.Collectors

class SystemContactAdapter(val context: Context) :
    RecyclerView.Adapter<SystemContactAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var dataSourceTemp: ArrayList<Friend>? = null
    private var systemContactHandler: SystemContactHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        this.dataSourceTemp = dataSource
        notifyDataSetChanged()
    }

    fun searchFullText(keyword: String) {
        try {
            val key = keyword.lowercase(Locale.getDefault())
            dataSource = if (StringUtils.isNullOrEmpty(key)) {
                this.dataSourceTemp!!
            } else {
                dataSourceTemp!!.stream().filter {
                    it.full_name!!.lowercase(Locale.ROOT)
                        .contains(key) || it.normalize_name!!.lowercase(Locale.ROOT)
                        .contains(key) || it.prefix!!.lowercase(Locale.ROOT)
                        .contains(key)
                }.collect(Collectors.toList()) as ArrayList<Friend>
            }
            notifyDataSetChanged()
        } catch (ex: Exception) {
        }

    }

    fun setClickFriend(systemContactHandler: SystemContactHandler) {
        this.systemContactHandler = systemContactHandler
    }

    inner class ItemHolder(val binding: ItemFriendSystemContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemFriendSystemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            binding.txtSection.visibility = android.view.View.VISIBLE
            binding.viewLineHeader.visibility = android.view.View.VISIBLE
            dataSource.sortWith { o1, o2 -> o1.full_name!!.trim().compareTo(o2.full_name!!.trim()) }
            val users: Friend = dataSource[position]

            binding.txtSection.text = users.full_name!!.trim().substring(0,1)
            if (position > 0) {
                val i = position - 1
                if (i < dataSource.size && users.full_name!!.trim().substring(0, 1) == dataSource[i].full_name!!.trim().substring(0, 1)
                ) {
                    binding.txtSection.visibility = android.view.View.GONE
                    binding.viewLineHeader.visibility = android.view.View.GONE
                }
            }


            when(dataSource[position].status){
                0 ->{
                    //mình
                    binding.btnAddFriend.visibility = android.view.View.GONE
                    binding.tvStatus.visibility = android.view.View.GONE
                    binding.imgChat.visibility = android.view.View.GONE
                    binding.imgNotAccept.visibility = android.view.View.GONE
                }
                1 ->{
                    // đã bạn bè
                    binding.btnAddFriend.visibility = android.view.View.GONE
                    binding.tvStatus.visibility = android.view.View.GONE
                    binding.imgChat.visibility = android.view.View.VISIBLE
                    binding.imgNotAccept.visibility = android.view.View.GONE
                }
                2 ->{
                    // đã gửi lời mời kết bạn đến người đó
                    binding.btnAddFriend.visibility = android.view.View.GONE
                    binding.tvStatus.visibility = android.view.View.VISIBLE
                    binding.imgChat.visibility = android.view.View.GONE
                    binding.imgNotAccept.visibility = android.view.View.GONE
                    binding.tvStatus.text = context.getString(vn.techres.line.R.string.sent_friend)
                }
                3 ->{
                    // được người đó gửi lời mời kết bạn
                    binding.btnAddFriend.visibility = android.view.View.GONE
                    binding.tvStatus.visibility = android.view.View.VISIBLE
                    binding.imgChat.visibility = android.view.View.GONE
                    binding.imgNotAccept.visibility = android.view.View.GONE
                    binding.tvStatus.text = context.getString(vn.techres.line.R.string.status_member_3)
                }
                4 ->{
                    // chưa kết bạn
                    binding.btnAddFriend.visibility = android.view.View.VISIBLE
                    binding.tvStatus.visibility = android.view.View.GONE
                    binding.imgChat.visibility = android.view.View.GONE
                    binding.imgNotAccept.visibility = android.view.View.GONE
                }
            }

            binding.imgAvatar.load(String.format("%s%s", nodeJs.api_ads, dataSource[position].avatar.thumb))
            {
                crossfade(true)
                scale(coil.size.Scale.FIT)
                placeholder(vn.techres.line.R.drawable.ic_user_placeholder)
                error(vn.techres.line.R.drawable.ic_user_placeholder)
                size(500, 500)
            }

            binding.tvName.text = dataSource[position].full_name

            itemView.setOnClickListener {
                dataSource[position].user_id?.let { it1 -> systemContactHandler!!.clickItem(position, it1) }
            }

            binding.btnAddFriend.setOnClickListener {
                binding.btnAddFriend.visibility = android.view.View.GONE
                binding.tvStatus.visibility = android.view.View.VISIBLE
                binding.imgChat.visibility = android.view.View.GONE
                binding.imgNotAccept.visibility = android.view.View.GONE
                binding.tvStatus.text = context.getString(vn.techres.line.R.string.sent_friend)
                systemContactHandler!!.clickAddFriend(position, dataSource[position].user_id)
            }

            binding.imgChat.setOnClickListener {
                systemContactHandler!!.clickChatFriend(position, dataSource[position].avatar.original, dataSource[position].full_name, dataSource[position].user_id)
            }

            binding.imgNotAccept.setOnClickListener {
                binding.btnAddFriend.visibility = android.view.View.GONE
                binding.tvStatus.visibility = android.view.View.VISIBLE
                binding.imgChat.visibility = android.view.View.GONE
                binding.imgNotAccept.visibility = android.view.View.GONE
                binding.tvStatus.text = context.getString(vn.techres.line.R.string.declined)
                systemContactHandler!!.clickNotAcceptFriend(position, dataSource[position].avatar.original, dataSource[position].full_name, dataSource[position].user_id)
            }
        }

    }


}