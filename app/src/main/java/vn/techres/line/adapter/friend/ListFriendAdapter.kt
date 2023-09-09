package vn.techres.line.adapter.friend

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.interfaces.ListFriendHandler
import java.util.*
import java.util.stream.Collectors

class ListFriendAdapter(val context: Context) :
    RecyclerView.Adapter<ListFriendAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var dataSourceTemp: ArrayList<Friend>? = null
    private var listFriendHandler: ListFriendHandler? = null
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
                this.dataSourceTemp ?: ArrayList()
            } else {
                dataSourceTemp?.stream()?.filter {
                    it.full_name!!.lowercase(Locale.ROOT)
                        .contains(keyword) || it.normalize_name!!.lowercase(Locale.ROOT)
                        .contains(keyword) || it.prefix!!.lowercase(Locale.ROOT)
                        .contains(keyword)
                }?.collect(Collectors.toList()) as ArrayList<Friend>
            }
            notifyDataSetChanged()
        } catch (ex: Exception) {
        }

    }

    fun setClickFriend(listFriendHandler: ListFriendHandler) {
        this.listFriendHandler = listFriendHandler
    }

    inner class ItemHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.txtSection.visibility = View.VISIBLE
            binding.viewLineHeader.visibility = View.VISIBLE
            dataSource.sortWith { o1, o2 -> o1.full_name!!.trim().compareTo(o2.full_name!!.trim()) }
            val users: Friend = dataSource[position]

            binding.txtSection.text = users.full_name!!.trim().substring(0, 1)
            if (position > 0) {
                val i = position - 1
                if (i < dataSource.size && users.full_name!!.trim()
                        .substring(0, 1) == dataSource[i].full_name!!.trim().substring(0, 1)
                ) {
                    binding.txtSection.visibility = View.GONE
                    binding.viewLineHeader.visibility = View.GONE
                }
            }

            binding.imgAvatar.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    dataSource[position].avatar.thumb
                )
            )
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
                size(500, 500)
            }
            binding.tvName.text = dataSource[position].full_name

            when (dataSource[position].status) {
                0 -> {
                    //mình
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
                1 -> {
                    // đã bạn bè
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.VISIBLE
                }
                2 -> {
                    // đã gửi lời mời kết bạn đến người đó
                    binding.btnAddFriend.visibility = View.GONE
                    binding.txtSentFriend.visibility = View.VISIBLE
                    binding.imgChat.visibility = View.GONE
                }
                3 -> {
                    // được người đó gửi lời mời kết bạn
                    binding.btnAddFriend.visibility = View.VISIBLE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
                4 -> {
                    // chưa kết bạn
                    binding.btnAddFriend.visibility = View.VISIBLE
                    binding.txtSentFriend.visibility = View.GONE
                    binding.imgChat.visibility = View.GONE
                }
            }

            itemView.setOnClickListener {
                dataSource[position].user_id?.let { it1 ->
                    listFriendHandler!!.clickFriendProfile(
                        position,
                        it1
                    )
                }
            }

            binding.btnAddFriend.setOnClickListener {
                binding.btnAddFriend.visibility = View.GONE
                binding.txtSentFriend.visibility = View.VISIBLE
                dataSource[position].user_id?.let { it1 ->
                    listFriendHandler!!.clickAddFriend(
                        position,
                        dataSource[position].avatar.original,
                        dataSource[position].full_name,
                        it1
                    )
                }
            }

            binding.imgChat.setOnClickListener {
                dataSource[position].user_id?.let { it1 ->
                    listFriendHandler!!.clickFriendChat(
                        position,
                        dataSource[position].avatar.original,
                        dataSource[position].full_name,
                        it1
                    )
                }
            }
        }

    }

}