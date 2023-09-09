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
import vn.techres.line.databinding.ItemFriendRequestContactBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.ClickItemHandler
import vn.techres.line.interfaces.FriendHandler

class FriendRequestContactAdapter(val context: Context) :
    RecyclerView.Adapter<FriendRequestContactAdapter.ItemHolder>() {
    private var dataSource = ArrayList<Friend>()
    private var friendHandler: FriendHandler? = null
    private var clickItemHandler: ClickItemHandler? = null
    @SuppressLint("UseRequireInsteadOfGet")
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickFriend(friendHandler: FriendHandler) {
        this.friendHandler = friendHandler
    }

    fun setClickSeeMore(clickItemHandler: ClickItemHandler) {
        this.clickItemHandler = clickItemHandler
    }

    inner class ItemHolder(val binding: ItemFriendRequestContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemFriendRequestContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            val data = dataSource[position]

            if (data.user_id == 0){
                binding.rlMain.visibility = View.GONE
                binding.lnSeeMore.visibility = View.VISIBLE
            }else {
                binding.rlMain.visibility = View.VISIBLE
                binding.lnSeeMore.visibility = View.GONE
                binding.imgUnAccept.visibility = View.VISIBLE
                binding.btnAccept.visibility = View.VISIBLE
                binding.tvStatus.visibility = View.GONE
            }

            binding.imgAvatar.load(String.format("%s%s", nodeJs.api_ads, data.avatar.thumb))
            {
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(500, 500)
            }
            binding.tvName.text = data.full_name

            itemView.setOnClickListener{
                data.user_id?.let { it1 ->
                    friendHandler!!.clickItemMyFriend(
                        position, it1
                    )
                }
            }
            binding.btnAccept.setOnClickListener{
//            binding.btnAccept.visibility = View.GONE
//            binding.imgUnAccept.visibility = View.GONE
//            binding.tvStatus.visibility = View.VISIBLE
//            binding.tvStatus.text = context.getString(R.string.accepted_friend)
                friendHandler!!.clickAcceptFriend(position,
                    data)
            }
            binding.imgUnAccept.setOnClickListener{
//            binding.btnAccept.visibility = View.GONE
//            binding.imgUnAccept.visibility = View.GONE
//            binding.tvStatus.visibility = View.VISIBLE
//            binding.tvStatus.text = context.getString(R.string.not_accepted_friend)
                friendHandler!!.clickCancelFriendRequest(position, data)
            }
            binding.lnSeeMore.setOnClickListener{
                clickItemHandler!!.ClickItem(position)
            }
        }

    }

}