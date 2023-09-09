package vn.techres.line.adapter.friend

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import vn.techres.line.R
import vn.techres.line.activity.FriendSuggestActivity
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendSuggestBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.FriendSuggestHandler

class FriendSuggestAdapter(val context: Context): RecyclerView.Adapter<FriendSuggestAdapter.ItemHolder>() {

    private var dataSource = ArrayList<Friend>()
    @SuppressLint("UseRequireInsteadOfGet")
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var friendSuggestHandler: FriendSuggestHandler? = null

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setFriendSuggestHandler(friendSuggestHandler: FriendSuggestActivity){
        this.friendSuggestHandler = friendSuggestHandler
    }

    inner class ItemHolder(val binding: ItemFriendSuggestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding = ItemFriendSuggestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder){
            binding.imgAvatar.load(String.format("%s%s", nodeJs.api_ads, dataSource[position].avatar.thumb))
            {
                crossfade(true)
                scale(Scale.FIT)
                transformations(RoundedCornersTransformation(20f, 20f, 0f, 0f))
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
                size(500, 500)
            }
            binding.tvName.text = dataSource[position].full_name
            if (dataSource[position].gender == 0){
                binding.tvGender.text = context.resources.getText(R.string.female)
            }else{
                binding.tvGender.text = context.resources.getText(R.string.male)
            }

            itemView.setOnClickListener {
                friendSuggestHandler!!.onClickItemFriend(dataSource[position].user_id?:0, position)
            }

            binding.btnAddFriend.setOnClickListener {
                friendSuggestHandler!!.onAddFriend(position)
                dataSource.removeAt(position)
                notifyDataSetChanged()
            }

            binding.btnSkipAddFriend.setOnClickListener {
                friendSuggestHandler!!.onSkipAddFriend(position)
                dataSource.removeAt(position)
                notifyDataSetChanged()

            }
        }

    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

}