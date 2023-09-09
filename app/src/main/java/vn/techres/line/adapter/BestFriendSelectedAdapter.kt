package vn.techres.line.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemFriendSelectedBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils
import vn.techres.line.interfaces.RemoveBestFriendHandler

class BestFriendSelectedAdapter(var context: Context) :
    RecyclerView.Adapter<BestFriendSelectedAdapter.ViewHolder>() {

    private var chooseBestFriend = ArrayList<Friend>()
    private var removeBestFriendHandler: RemoveBestFriendHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setRemoveBestFriend(removeBestFriendHandler: RemoveBestFriendHandler) {
        this.removeBestFriendHandler = removeBestFriendHandler
    }

    fun setDataSource(chooseBestFriend: ArrayList<Friend>) {
        this.chooseBestFriend = chooseBestFriend
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFriendSelectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return chooseBestFriend.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(nodeJs, chooseBestFriend[position], removeBestFriendHandler)
    }

    class ViewHolder(private val binding: ItemFriendSelectedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            configNodeJs: ConfigNodeJs,
            friend: Friend,
            removeBestFriendHandler: RemoveBestFriendHandler?
        ) {
            Utils.getImage(binding.imgAvatarFriendSelected, friend.avatar.medium, configNodeJs)
            binding.imgClearFriendSelected.setOnClickListener {
                removeBestFriendHandler?.removeBestFriend(friend)
            }
        }
    }
}