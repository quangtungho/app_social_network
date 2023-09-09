package vn.techres.line.adapter.friend


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import vn.techres.line.R
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendRequestBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.FriendHandler


class FriendRequestAdapter (val context: Context) :
    RecyclerView.Adapter<FriendRequestAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var friendHandler: FriendHandler? = null
    @SuppressLint("UseRequireInsteadOfGet")

    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickFriend(friendHandler: FriendHandler) {
        this.friendHandler = friendHandler
    }

    inner class ItemHolder(val binding: ItemFriendRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val data = dataSource[position]
            binding.imgAvatar.load(String.format("%s%s", nodeJs.api_ads, dataSource[position].avatar.thumb))
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
                size(500, 500)
            }
            binding.tvName.text = dataSource[position].full_name

            binding.btnAddAccept.visibility = View.VISIBLE
            binding.imgNotAccept.visibility = View.VISIBLE

            binding.btnAddAccept.setOnClickListener{
                binding.btnAddAccept.visibility = View.GONE
                binding.imgNotAccept.visibility = View.GONE
                binding.tvFriendStatus.text = context.getString(R.string.accepted_friend)
                friendHandler!!.clickAcceptFriend(position, data)
            }

            binding.imgNotAccept.setOnClickListener{
                binding.btnAddAccept.visibility = View.GONE
                binding.imgNotAccept.visibility = View.GONE
                binding.tvFriendStatus.text = context.getString(R.string.not_accepted_friend)
                friendHandler!!.clickCancelFriendRequest(position, data)
            }

            itemView.setOnClickListener {
                data.user_id?.let { it1 ->
                    friendHandler!!.clickItemMyFriend(
                        position,
                        it1
                    )
                }
            }
        }

    }

}