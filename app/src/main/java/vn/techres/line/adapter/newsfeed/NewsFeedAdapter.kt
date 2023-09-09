package vn.techres.line.adapter.newsfeed

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import kohii.v1.core.Manager
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import org.jetbrains.annotations.NotNull
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.databinding.ItemPostBinding
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.holder.newfeed.*
import vn.techres.line.interfaces.newsfeed.FriendSuggestNewFeedHandler
import vn.techres.line.interfaces.newsfeed.NewsFeedHandler


class NewsFeedAdapter(
    var context: Context,
    var kohii: Kohii,
    var manager: Manager,
    val lifecycle: Lifecycle,
    val activity: MainActivity,
    val width: Int,
    val height: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val cacheManager: CacheManager = CacheManager.getInstance()
    private var dataSource = ArrayList<PostReview>()
    private var listFriendSuggest = ArrayList<Friend>()

    private var newsFeedHandler: NewsFeedHandler? = null
    private var user = CurrentUser.getCurrentUser(context)
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)


    private var friendSuggestNewsFeedAdapter: FriendSuggestNewsFeedAdapter? = null
    private var friendSuggestNewFeedHandler: FriendSuggestNewFeedHandler? = null

    private val snapHelper = LinearSnapHelper()

    var playback: Playback? = null
    val volumeInfo: VolumeInfo? = null

    private var youTubePlayerPause: YouTubePlayer? = null

    fun setDataSource(dataSource: ArrayList<PostReview>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    fun setListFriendSuggest(listFriendSuggest: ArrayList<Friend>) {
        this.listFriendSuggest = listFriendSuggest
    }

    fun setNewFeedHandler(newsFeedHandler: NewsFeedHandler) {
        this.newsFeedHandler = newsFeedHandler
    }

    fun setClickFriendSuggest(friendSuggestNewFeedHandler: FriendSuggestNewFeedHandler) {
        this.friendSuggestNewFeedHandler = friendSuggestNewFeedHandler
    }

    fun stopVideo() {
        kohii.lockManager(manager)
    }

    fun startVideo() {
        kohii.unlockManager(manager)
    }

    fun pauseYouTube() {
        youTubePlayerPause?.pause()

    }

    // Bên trong class adapter
    override fun getItemId(position: Int): Long {
        return dataSource[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            createNewFeedPosition -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_header_review_post, parent, false
                )
                CreateNewFeedViewHolder(view)
            }
            updateProfilePosition -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_update_profile_newfeed, parent, false
                )
                UpdateProfileViewHolder(view)
            }
            friendSuggestOnePosition, friendSuggestTwoPosition -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_friend_suggest_new_feed, parent, false
                )
                FriendSuggestViewHolder(view)
            }
            itemReviewYoutube -> {
                ItemPostYoutubeHolder(
                    ItemPostBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), context, kohii, manager, lifecycle, width, height
                )
            }
            else -> {
                ItemPostHolder(
                    ItemPostBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), context, kohii, manager, lifecycle, width, height
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val newsFeed: PostReview = dataSource[position]
        return when (position) {
            0 -> {
                createNewFeedPosition
            }
            1 -> {
                updateProfilePosition
            }
            2 -> {
                friendSuggestOnePosition
            }
            23 -> {
                friendSuggestOnePosition
            }
            else -> {
                if (newsFeed.url != "" && newsFeed.url!!.contains(context.resources.getString(R.string.link_youtube_1)) || newsFeed.url!!.contains(
                        context.resources.getString(R.string.link_youtube_3)
                    ) || newsFeed.url!!.contains(context.resources.getString(R.string.link_youtube_4))
                ) {
                    itemReviewYoutube
                } else {
                    itemReview
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(@NotNull holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            createNewFeedPosition -> {
                onBindCreateNewFeed(holder as CreateNewFeedViewHolder)
            }
            updateProfilePosition -> {
                onBindUpdateProfile(holder as UpdateProfileViewHolder)
            }
            friendSuggestOnePosition -> {
                onBindFriendSuggest(holder as FriendSuggestViewHolder)
            }
            friendSuggestTwoPosition -> {
                onBindFriendSuggest(holder as FriendSuggestViewHolder)
            }
            itemReviewYoutube -> {
                (holder as ItemPostYoutubeHolder).bin(
                    dataSource[position],
                    position,
                    user,
                    configNodeJs,
                    newsFeedHandler!!
                )
            }
            else -> {
                (holder as ItemPostHolder).bin(
                    dataSource[position],
                    position,
                    user,
                    configNodeJs,
                    newsFeedHandler!!
                )
            }
        }
    }

    private fun onBindCreateNewFeed(holder: CreateNewFeedViewHolder) {
        holder.cvCreateReview.setOnClickListener {
            newsFeedHandler!!.onReview()
        }

        holder.cvCreatePost.setOnClickListener {
            newsFeedHandler!!.onPost()
        }
    }

    private fun onBindUpdateProfile(holder: UpdateProfileViewHolder) {
        holder.tvUpdateProfile.visibility = View.GONE
    }

    private fun onBindFriendSuggest(holder: FriendSuggestViewHolder) {
        if (listFriendSuggest.size == 0) {
            holder.lnFriendSuggest.visibility = View.GONE
        } else {
            holder.lnFriendSuggest.visibility = View.VISIBLE

            friendSuggestNewsFeedAdapter = FriendSuggestNewsFeedAdapter(context)
            friendSuggestNewsFeedAdapter!!.setFriendSuggest(friendSuggestNewFeedHandler)
            val layoutManager = FlexboxLayoutManager(context)
            holder.recyclerViewFriendSuggest.layoutManager = layoutManager
            holder.recyclerViewFriendSuggest.layoutManager =
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            friendSuggestNewsFeedAdapter!!.setDataSource(listFriendSuggest)
            snapHelper.attachToRecyclerView(holder.recyclerViewFriendSuggest)
            holder.recyclerViewFriendSuggest.adapter = friendSuggestNewsFeedAdapter

            holder.tvSeeMoreFriendSuggest.setOnClickListener {
                friendSuggestNewFeedHandler!!.onSeeMore()
            }
        }

    }


    companion object {
        private const val createNewFeedPosition = 0
        private const val updateProfilePosition = 1
        private const val friendSuggestOnePosition = 2
        private const val friendSuggestTwoPosition = 13
        private const val itemReview = 100
        private const val itemReviewYoutube = 131
    }
}