package vn.techres.line.adapter.newsfeed

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.R
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemFriendNewFeedBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.interfaces.newsfeed.FriendSuggestNewFeedHandler

class FriendSuggestNewsFeedAdapter(val context: Context) :
    RecyclerView.Adapter<FriendSuggestNewsFeedAdapter.ItemHolder>() {

    private var dataSource = ArrayList<Friend>()

    @SuppressLint("UseRequireInsteadOfGet")
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)
    private var friendSuggestNewFeedHandler: FriendSuggestNewFeedHandler? = null

    fun setDataSource(dataSource: ArrayList<Friend>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setFriendSuggest(friendSuggestNewFeedHandler: FriendSuggestNewFeedHandler?) {
        this.friendSuggestNewFeedHandler = friendSuggestNewFeedHandler
    }

    inner class ItemHolder(val binding: ItemFriendNewFeedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val binding =
            ItemFriendNewFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        with(holder) {
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
                transformations(RoundedCornersTransformation(20f, 20f, 0f, 0f))
                placeholder(R.drawable.ic_user_placeholder)
                error(R.drawable.ic_user_placeholder)
                size(500, 500)
            }
            binding.tvName.text = dataSource[position].full_name
            binding.tvFriendTogether.text = String.format(
                "%s %s", dataSource[position].total_mutual_friend, context.resources.getString(
                    R.string.mutual_friends
                )
            )

            when (dataSource[position].total_mutual_friend) {
                0 -> {
                    binding.rlFriendOne.visibility = View.GONE
                    binding.rlFriendTwo.visibility = View.GONE
                    binding.rlFriendThree.visibility = View.GONE
                }
                1 -> {
                    binding.rlFriendOne.visibility = View.VISIBLE
                    binding.rlFriendTwo.visibility = View.GONE
                    binding.rlFriendThree.visibility = View.GONE

                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                dataSource[position].mutual_friend[0].friend_avatar.thumb
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .into(binding.imgFriendOne)

                }
                2 -> {
                    binding.rlFriendOne.visibility = View.VISIBLE
                    binding.rlFriendTwo.visibility = View.VISIBLE
                    binding.rlFriendThree.visibility = View.GONE

                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                dataSource[position].mutual_friend[0].friend_avatar.thumb
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .into(binding.imgFriendOne)


                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                dataSource[position].mutual_friend[1].friend_avatar.thumb
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .into(binding.imgFriendTwo)

                }
                else -> {
                    binding.rlFriendOne.visibility = View.VISIBLE
                    binding.rlFriendTwo.visibility = View.VISIBLE
                    binding.rlFriendThree.visibility = View.VISIBLE

                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                dataSource[position].mutual_friend[0].friend_avatar.thumb
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .into(binding.imgFriendOne)


                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                dataSource[position].mutual_friend[1].friend_avatar.thumb
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .into(binding.imgFriendTwo)


                    Glide.with(context)
                        .load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                dataSource[position].mutual_friend[2].friend_avatar.thumb
                            )
                        )
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .centerCrop()
                        .into(binding.imgFriendThree)

                }
            }

            binding.btnAddFriend.visibility = View.VISIBLE
            binding.tvSentRequest.visibility = View.GONE

            binding.btnAddFriend.setOnClickListener {
                binding.btnAddFriend.visibility = View.GONE
                binding.tvSentRequest.visibility = View.VISIBLE
                friendSuggestNewFeedHandler!!.onAddFriend(position)
                dataSource.removeAt(position)
                notifyDataSetChanged()
            }

            itemView.setOnClickListener {
                friendSuggestNewFeedHandler!!.onClickItemFriend(position)
            }

            binding.imgAvatar.setOnClickListener {
                friendSuggestNewFeedHandler!!.onClickAvatar(
                    dataSource[position].avatar.original ?: "", position
                )
            }
        }

    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

}