package vn.techres.line.adapter.profile

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kohii.v1.core.Manager
import kohii.v1.core.Playback
import kohii.v1.exoplayer.Kohii
import kohii.v1.media.VolumeInfo
import org.jetbrains.annotations.NotNull
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ItemPostBinding
import vn.techres.line.helper.CacheManager
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.holder.newfeed.ItemPostHolder
import vn.techres.line.holder.profile.ProfileFourHolder
import vn.techres.line.holder.profile.ProfileOneHolder
import vn.techres.line.holder.profile.ProfileThreeHolder
import vn.techres.line.holder.profile.ProfileTwoHolder
import vn.techres.line.interfaces.newsfeed.NewsFeedHandler
import vn.techres.line.interfaces.profile.FriendsInProfileHandler
import vn.techres.line.interfaces.profile.ProfileHandler
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners

import com.bumptech.glide.request.RequestOptions.bitmapTransform

import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import vn.techres.line.data.model.friend.Friend


class ProfileAdapter(var context: Context, var kohii: Kohii, var manager: Manager, val lifecycle: Lifecycle, val width: Int, val height: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val cacheManager: CacheManager = CacheManager.getInstance()

    private var dataProfile = User()
    private var dataSource = ArrayList<PostReview>()

    private var listFriend = ArrayList<Friend>()

    private var user = CurrentUser.getCurrentUser(context)
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    var playback: Playback? = null
    var volumeInfo: VolumeInfo? = null

    private var friendsInProfileAdapter: FriendsInProfileAdapter? = null
    private var friendsInProfileHandler: FriendsInProfileHandler? = null
    private var checkStatusProfile: Int? = 0
    private var totalFriend: Int? = 0

    private var profileHandler: ProfileHandler? = null
    private var newsFeedHandler: NewsFeedHandler? = null

    private var muteVolume = true

    fun setDataProfile(dataProfile: User) {
        this.dataProfile = dataProfile
    }

    fun setProfileHandler(profileHandler: ProfileHandler) {
        this.profileHandler = profileHandler
    }


    fun setNewsFeedHandler(newsFeedHandler: NewsFeedHandler) {
        this.newsFeedHandler = newsFeedHandler
    }

    fun setCheckStatusProfile(checkStatusProfile: Int) {
        this.checkStatusProfile = checkStatusProfile
    }

    fun setFriendsInProfileHandler(friendsInProfileHandler: FriendsInProfileHandler) {
        this.friendsInProfileHandler = friendsInProfileHandler
    }

    fun setDataSource(dataSource: ArrayList<PostReview>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    
    fun setListFriend(listFriend: ArrayList<Friend>, totalFriend: Int) {
        this.listFriend = listFriend
        this.totalFriend = totalFriend
    }
 
    fun stopVideo() {
        kohii.lockManager(manager)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            profileOne -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_header_profile, parent, false
                )
                ProfileOneHolder(view)
            }
            profileTwo -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_profile_infor, parent, false
                )
                ProfileTwoHolder(view)
            }
            profileThree -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_profile_friend, parent, false
                )
                ProfileThreeHolder(view)
            }
            profileFour -> {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.item_profile_media, parent, false
                )
                ProfileFourHolder(view)
            }
            else -> {
                ItemPostHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false), context, kohii, manager, lifecycle, width, height)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> {
                profileOne
            }
            1 -> {
                profileTwo
            }
            2 -> {
                profileThree
            }
            3 -> {
                profileFour
            }
            else -> {
                timeline
            }
        }
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(@NotNull holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            profileOne -> {
                onBindProfileOne(holder as ProfileOneHolder, position)
            }
            profileTwo -> {
                onBindProfileTwo(holder as ProfileTwoHolder)
            }
            profileThree -> {
                onBindProfileThree(holder as ProfileThreeHolder)
            }
            profileFour -> {
                onBinProfileFour(holder as ProfileFourHolder)
            }
            else -> {
                (holder as ItemPostHolder).bin(dataSource[position], position, user, configNodeJs, newsFeedHandler!!)
            }
        }
    }

    private fun onBindProfileOne(holder: ProfileOneHolder, position: Int) {
        getImage(holder.imgProfile, dataProfile.avatar_three_image.medium, configNodeJs)
        if (dataProfile.cover_urls.size != 0) {
            getImageCover(
                holder.imgCoverPhoto,
                String.format("%s%s", configNodeJs.api_ads, dataProfile.cover_urls[0])
            )
        } else {
            holder.imgCoverPhoto.setImageResource(R.drawable.bgcoverprofile)
        }

        holder.imgProfile.setOnClickListener {
            profileHandler!!.clickImage(dataProfile.avatar_three_image.original?:"", position)
        }

        holder.imgCoverPhoto.setOnClickListener {
            if (dataProfile.cover_urls.size!= 0){
                profileHandler!!.clickImage(dataProfile.cover_urls[0], position)
            }
        }

        holder.txtFullName.text = dataProfile.name

        //Set button profile under Name
        when (checkStatusProfile) {
            0 -> {
                holder.lnCreatePostProfile.visibility = View.VISIBLE
                holder.imgProfileMore.visibility = View.GONE
                holder.lnProfileFriendly.visibility = View.GONE
                holder.lnAddFriendProfile.visibility = View.GONE
                holder.lnCancelRequestFriendProfile.visibility = View.GONE
                holder.lnReplyFriendProfile.visibility = View.GONE
                holder.lnChatProfile.visibility = View.GONE

            }
            1 -> {
                holder.lnProfileFriendly.visibility = View.VISIBLE
                holder.lnChatProfile.visibility = View.VISIBLE
                holder.imgProfileMore.visibility = View.GONE
                holder.lnCreatePostProfile.visibility = View.GONE
                holder.lnAddFriendProfile.visibility = View.GONE
                holder.lnCancelRequestFriendProfile.visibility = View.GONE
                holder.lnReplyFriendProfile.visibility = View.GONE
            }
            2 -> {
                holder.lnChatProfile.visibility = View.VISIBLE
                holder.lnCancelRequestFriendProfile.visibility = View.VISIBLE
                holder.imgProfileMore.visibility = View.GONE

                holder.lnCreatePostProfile.visibility = View.GONE
                holder.lnProfileFriendly.visibility = View.GONE
                holder.lnCreatePostProfile.visibility = View.GONE
                holder.lnAddFriendProfile.visibility = View.GONE
                holder.lnReplyFriendProfile.visibility = View.GONE
            }
            3 -> {
                holder.lnReplyFriendProfile.visibility = View.VISIBLE
                holder.lnChatProfile.visibility = View.VISIBLE
                holder.imgProfileMore.visibility = View.GONE
                holder.lnCancelRequestFriendProfile.visibility = View.GONE
                holder.lnProfileFriendly.visibility = View.GONE
                holder.lnCreatePostProfile.visibility = View.GONE
                holder.lnAddFriendProfile.visibility = View.GONE
            }

            4 -> {
                holder.lnAddFriendProfile.visibility = View.VISIBLE
                holder.lnChatProfile.visibility = View.VISIBLE
                holder.imgProfileMore.visibility = View.GONE
                holder.lnReplyFriendProfile.visibility = View.GONE
                holder.lnCancelRequestFriendProfile.visibility = View.GONE
                holder.lnProfileFriendly.visibility = View.GONE
                holder.lnCreatePostProfile.visibility = View.GONE
            }
        }

        holder.lnCreatePostProfile.setOnClickListener {
            profileHandler!!.createPostProfile()
        }

        holder.lnChatProfile.setOnClickListener {
            profileHandler!!.chat()
        }

        holder.lnProfileFriendly.setOnClickListener {
            bottomUnFriend(holder)
        }

        holder.lnCancelRequestFriendProfile.setOnClickListener {
            holder.lnCancelRequestFriendProfile.visibility = View.GONE
            holder.lnAddFriendProfile.visibility = View.VISIBLE

            profileHandler!!.onCancelRequestFriend()
        }

        holder.lnReplyFriendProfile.setOnClickListener {
            bottomSheetReplyFriend(holder)
        }

        holder.lnAddFriendProfile.setOnClickListener {
            holder.lnAddFriendProfile.visibility = View.GONE
            holder.lnCancelRequestFriendProfile.visibility = View.VISIBLE
            profileHandler!!.onAddFriend()
        }
    }

    private fun onBindProfileTwo(holder: ProfileTwoHolder) {
        //Set information
        if (dataProfile.id == user.id) {
            holder.txtPhone.text = dataProfile.phone
            holder.txtEditProfile.visibility = View.VISIBLE
            holder.txtReportUser.visibility = View.GONE
        } else {
            holder.txtEditProfile.visibility = View.GONE
            holder.txtReportUser.visibility = View.VISIBLE
        }

        if (dataProfile.email == "") {
            holder.txtEmail.visibility = View.GONE
            holder.txtEmail1.visibility = View.GONE
            holder.imageView6.visibility = View.GONE
        } else {
            holder.txtEmail.text = dataProfile.email
        }

        holder.txtGender.text = setGender(dataProfile.gender)

        holder.txtBirthday.text =
            Editable.Factory.getInstance().newEditable((dataProfile.birthday))

        if (dataProfile.address_full_text == "") {
            holder.txtAddress.visibility = View.GONE
            holder.txtAddress1.visibility = View.GONE
            holder.imageView9.visibility = View.GONE
        } else {
            if (dataProfile.id == user.id) {
                holder.txtAddress.text = dataProfile.address_full_text
            }
        }

        holder.txtEditProfile.setOnClickListener {
            profileHandler?.clickEditProfile()
        }

        holder.txtReportUser.setOnClickListener {
            profileHandler?.clickReportUser()
        }
    }

    private fun onBindProfileThree(holder: ProfileThreeHolder) {
        //Show friends
        if (listFriend.size == 0) {
            holder.recyclerViewFriends.visibility = View.GONE
        } else {
            holder.recyclerViewFriends.visibility = View.VISIBLE
            holder.txtFriendNumber.text = totalFriend.toString()

            friendsInProfileAdapter = FriendsInProfileAdapter(context)
            friendsInProfileAdapter!!.setClickFriendsInProfile(friendsInProfileHandler)
            val layoutManager = FlexboxLayoutManager(context)

            holder.recyclerViewFriends.layoutManager = layoutManager
            holder.recyclerViewFriends.layoutManager = GridLayoutManager(context, 3)
//            holder.recyclerViewFriends.addItemDecoration(GridSpacingDecoration(0, 3))
            friendsInProfileAdapter!!.setDataSource(listFriend)
//            snapHelper.attachToRecyclerView(holder.recyclerViewFriends)
            holder.recyclerViewFriends.adapter = friendsInProfileAdapter
        }

        holder.txtSearchFriend.setOnClickListener {
            profileHandler!!.searchFriend()
        }

        holder.btnViewAll.setOnClickListener {
            profileHandler!!.clickViewAllFriend()
        }
    }

    private fun onBinProfileFour(holder: ProfileFourHolder) {
        holder.btnImage.setOnClickListener {
            profileHandler!!.clickMediaImage()
        }

        holder.btnVideo.setOnClickListener {
            profileHandler!!.clickMediaVideo()
        }

        holder.btnAlbum.setOnClickListener {
            profileHandler!!.clickMediaAlbum()
        }
    }


    /**
     * Show image cover
     */
    private fun getImageCover(imageView: ImageView, string: String?) {

        val multiLeft = MultiTransformation(
            CenterCrop(),
            GranularRoundedCorners(25f, 25f, 0f,0f),
        )

        Glide.with(context)
            .load(string)
            .apply(bitmapTransform(multiLeft)
                .placeholder(R.drawable.bgcoverprofile)
                .error(R.drawable.bgcoverprofile))
            .into(imageView)


//        Glide.with(context)
//            .load(string)
//            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//            .centerCrop()
//            .apply(
//                RequestOptions().placeholder(R.drawable.bgcoverprofile)
//                    .error(R.drawable.bgcoverprofile)
//                    .transform(GranularRoundedCorners(30f,30f,0f,0f))
//            )
//            .into(imageView)
    }

    /**
     * CHECK GENDER
     * Male = 1
     * Female = 0
     */
    private fun setGender(i: Int?): String {
        return when (i) {
            1 -> context.resources.getString(R.string.male)
            0 -> context.resources.getString(R.string.female)
            else -> context.resources.getString(R.string.other)
        }
    }

    /**
     * Bottom sheet dialog unfriend
     */
    private fun bottomUnFriend(holder: ProfileOneHolder) {
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_un_friend)
        bottomSheetDialog.setCancelable(true)
        val lnUnFriend = bottomSheetDialog.findViewById<LinearLayout>(R.id.lnUnFriend)

        lnUnFriend!!.setOnClickListener {

            holder.lnProfileFriendly.visibility = View.GONE
            holder.lnAddFriendProfile.visibility = View.VISIBLE
            profileHandler!!.onUnfriend(dataProfile)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun bottomSheetReplyFriend(holder: ProfileOneHolder) {
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_friend_reply)
        bottomSheetDialog.setCancelable(true)
        val lnAcceptFriend = bottomSheetDialog.findViewById<LinearLayout>(R.id.lnAcceptFriend)
        val lnDeleteAddFriend = bottomSheetDialog.findViewById<LinearLayout>(R.id.lnDeleteAddFriend)

        lnAcceptFriend!!.setOnClickListener {
            holder.lnReplyFriendProfile.visibility = View.GONE
            holder.lnProfileFriendly.visibility = View.VISIBLE
            profileHandler!!.onAcceptFriend(dataProfile)
            bottomSheetDialog.dismiss()
        }
        lnDeleteAddFriend!!.setOnClickListener {
            holder.lnReplyFriendProfile.visibility = View.GONE
            holder.lnAddFriendProfile.visibility = View.VISIBLE
            profileHandler!!.onUnAcceptFriend(dataProfile)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    companion object {
        private const val profileOne = 0
        private const val profileTwo = 1
        private const val profileThree = 2
        private const val profileFour = 3
        private const val timeline = 4
        private const val genderNull = 2
    }

}