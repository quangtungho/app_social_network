package vn.techres.line.adapter.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import vn.techres.line.R
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendProfileBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.profile.FriendsInProfileHandler

class FriendsInProfileAdapter(val context: Context) :
    RecyclerView.Adapter<FriendsInProfileAdapter.ItemHolder>() {

    private var dataSource = ArrayList<Friend>()
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var friendsInProfileHandler: FriendsInProfileHandler? = null

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setClickFriendsInProfile(friendsInProfileHandler: FriendsInProfileHandler?) {
        this.friendsInProfileHandler = friendsInProfileHandler
    }

    inner class ItemHolder(val binding: ItemFriendProfileBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemFriendProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
            binding.imgFriend.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    dataSource[position].avatar.thumb
                )
            )
            {
                crossfade(true)
                scale(Scale.FIT)
                transformations(RoundedCornersTransformation(5f, 5f, 0f, 0f))
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(500, 500)
            }
            binding.txtFriend.text = dataSource[position].full_name

            itemView.setOnClickListener {
                dataSource[position].user_id?.let { it1 ->
                    friendsInProfileHandler!!.onClickItemFriend(
                        it1,
                        position
                    )
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

}