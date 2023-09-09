package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import kohii.v1.exoplayer.Kohii
import kotlinx.android.synthetic.main.activity_profile.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.profile.ProfileAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.local.contact.myfriend.MyFriendResponse
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.eventbus.CommentEventBusData
import vn.techres.line.data.model.eventbus.EventBusSaveBranch
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.profile.response.UpdateProfileResponse
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.ReviewBranchResponse
import vn.techres.line.data.model.response.StatusProfileResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.Media
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityProfileFriendBinding
import vn.techres.line.fragment.account.ProfileAlbumFragment
import vn.techres.line.fragment.account.ProfileImageFragment
import vn.techres.line.fragment.account.ProfileVideoFragment
import vn.techres.line.fragment.profile.EditProfileFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.hideKeyboard
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.newsfeed.NewsFeedHandler
import vn.techres.line.interfaces.profile.FriendsInProfileHandler
import vn.techres.line.interfaces.profile.ProfileHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.math.ceil

class ProfileFriendActivity : BaseBindingActivity<ActivityProfileFriendBinding>(),
    ProfileHandler, FriendsInProfileHandler, NewsFeedHandler {
    private var friendList = ArrayList<Friend>()
    private var data = ArrayList<PostReview>()
    private var listIDBranch = ArrayList<Int>()
    private var dataProfile = User()
    private var user = User()
    private var configNodeJs = ConfigNodeJs()
    private var configJava = ConfigJava()
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var profileAdapter: ProfileAdapter? = null

    private var idProfile = 0
    private var page = 1
    private var total = 0
    private var limit = 10
    private var totalFriend = 0
    private var positionPost = 0
    private var isLoading = true
    private var checkSwipeRefresh = false

    private var numCheckUpload = 0


    override val bindingInflater: (LayoutInflater) -> ActivityProfileFriendBinding
        get() = ActivityProfileFriendBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configJava = CurrentConfigJava.getConfigJava(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        mSocket = application.getSocketInstance(this)
        mSocket?.connect()

        intent?.let {
            idProfile = it.getIntExtra(TechresEnum.ID_USER.toString(), 0)
        }

        val kohii = Kohii[this]
        val manager = kohii.register(this).addBucket(binding.recyclerView)

        val displayMetrics = DisplayMetrics()
        val windowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        profileAdapter = ProfileAdapter(this, kohii, manager, lifecycle, width, height)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = profileAdapter
        profileAdapter?.setProfileHandler(this)
        profileAdapter?.setFriendsInProfileHandler(this)
        profileAdapter?.setNewsFeedHandler(this)

        setData()
        setListener()
    }

    private fun setData() {
        if (data.size == 0){
            binding.shimmerProfile.startShimmerAnimation()
            page = 1
            getListBranchReview(page)
        }else{
            profileAdapter?.setDataSource(data)
            getListFriend(idProfile)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setListener() {
        binding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState && isLoading) {
                    if (y > 0) {
                        if (page <= ceil((total / limit).toString().toDouble())) {
                            page++
                            isLoading = false
                            val reviewBranch = PostReview()
                            reviewBranch.loading = 1
                            data.add(reviewBranch)
                            profileAdapter?.notifyItemInserted(data.size)
                            Handler(Looper.getMainLooper()).postDelayed({
                                getListBranchReview(page)
                            }, 1000)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            checkSwipeRefresh = true
            page = 1
            data.clear()
            setData()
            profileAdapter!!.notifyDataSetChanged()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    fun checkLoadingProfile(){
        if (checkSwipeRefresh) {
            checkSwipeRefresh = false
            binding.swipeRefresh.isRefreshing = false
        }
        numCheckUpload = 0
        shimmerProfile.hide()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserUpdate(dataUser: User) {
        dataProfile = dataUser
        profileAdapter?.setDataProfile(dataProfile)
        profileAdapter?.notifyItemChanged(0)
        profileAdapter?.notifyItemChanged(1)
        if (user.id == dataProfile.id) {
            user.avatar_three_image.thumb = dataProfile.avatar_three_image.thumb
            user.avatar_three_image.medium = dataProfile.avatar_three_image.medium
            user.avatar_three_image.original =
                dataProfile.avatar_three_image.original
            user.name = dataProfile.name
            CurrentUser.saveUserInfo(this, user)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPostEditProfile(dataPostEdit: PostReview) {
        if (dataPostEdit._id!! != ""){
            when(dataPostEdit.status_reload){
                "create" -> {
                    data.add(4, dataPostEdit)
                    profileAdapter?.setDataSource(data)
                }
                "edit" -> {
                    data.add(positionPost, dataPostEdit)
                    data.removeAt(positionPost + 1)

                    profileAdapter?.setDataSource(data)
                }
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMyComment(postList: CommentEventBusData) {
        if (postList.data.size != 0){
            val totalCommentCount = postList.data[0].total_comment
            postList.data.removeAt(0)
            when (postList.data.size) {
                0 -> {
                    data[positionPost].comments.clear()
                }
                1 -> {
                    data[positionPost].comments.clear()
                    data[positionPost].comments.add(postList.data[0])
                }
                2 -> {
                    data[positionPost].comments.clear()
                    data[positionPost].comments.add(postList.data[0])
                    data[positionPost].comments.add(postList.data[1])
                }
                else -> {
                    data[positionPost].comments.clear()
                    data[positionPost].comments.add(postList.data[0])
                    data[positionPost].comments.add(postList.data[1])
                    data[positionPost].comments.add(postList.data[2])
                }
            }
            data[positionPost].comment_count = totalCommentCount
            profileAdapter!!.setDataSource(data)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSaveBranch(boolean: EventBusSaveBranch) {
        if (boolean.isCheck){
            profileAdapter!!.notifyDataSetChanged()
        }
    }


    //socket
    private fun addFriend(profile: User) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = profile.id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("request-to-contact", jsonObject)
            WriteLog.d("request-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT

        addFriendParams.params.contact_to_user_id = profile.id
        addFriendParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .addFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status != AppConfig.SUCCESS_CODE) {
                            Toast.makeText(
                                this@ProfileFriendActivity,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

    private fun unFriend(profile: User) {

        val params = FriendParams()
        params.http_method = AppConfig.POST
        params.request_url = "api/friends/reject"


        params.params.friend_user_id = profile.id
        params.project_id = AppConfig.PROJECT_CHAT
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .unFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {}
                })
        }
    }


    private fun cancelFriendRequest(profile: User) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = profile.id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("revoke-to-contact", jsonObject)
            WriteLog.d("revoke-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val params = FriendParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-tos/revoke"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_to_user_id = profile.id
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .revokeFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status != AppConfig.SUCCESS_CODE) {
                            Toast.makeText(
                                this@ProfileFriendActivity,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

    //Reponse
    private fun getProfile(userID: Int) {

        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/customers/$userID"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getProfile(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<UpdateProfileResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: UpdateProfileResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        numCheckUpload += 1
                        if (numCheckUpload == 4){
                            checkLoadingProfile()
                        }
                        dataProfile = response.data
                        profileAdapter?.setDataProfile(dataProfile)
                        profileAdapter?.notifyItemChanged(0)
                        profileAdapter?.notifyItemChanged(1)
                    }
                }
            })

    }

    private fun getListFriend(userID: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url =
            String.format("%s%s%s", "/api/friends?user_id=", userID, "&limit=6&page=1")

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getListFriend(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MyFriendResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: MyFriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        numCheckUpload += 1
                        if (numCheckUpload == 4){
                            checkLoadingProfile()
                        }
                        friendList.clear()
                        val friendListTemp = response.data.list
                        totalFriend = response.data.total_record ?: 0
                        if (totalFriend > 6) {
                            friendList.addAll(friendListTemp.subList(0, 6))
                            profileAdapter?.setListFriend(friendList, totalFriend)
                        } else {
                            profileAdapter?.setListFriend(friendListTemp, totalFriend)
                        }
                        profileAdapter?.notifyItemChanged(2)
                    } else Toast.makeText(
                        this@ProfileFriendActivity,
                        resources.getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun createChatPersonal(memberId: Int) {
        val createChatPersonalRequest = CreateGroupPersonalRequest()
        createChatPersonalRequest.member_id = memberId
        createChatPersonalRequest.admin_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(createChatPersonalRequest))
            mSocket?.emit(
                TechResEnumChat.NEW_GROUP_CREATE_PERSONAL_ALO_LINE.toString(),
                jsonObject
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun createGroupPersonal(memberList: ArrayList<Int>, memberId: Int) {
        val groupPersonalParams = GroupPersonalParams()
        groupPersonalParams.http_method = AppConfig.POST
        groupPersonalParams.request_url = "/api/groups/create-personal"
        groupPersonalParams.project_id = AppConfig.PROJECT_CHAT
        groupPersonalParams.params.members = memberList
        groupPersonalParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createChatPersonal(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<GroupPersonalResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(responseGroup: GroupPersonalResponse) {
                        if (responseGroup.status == AppConfig.SUCCESS_CODE) {
                            val group = responseGroup.data
                            createChatPersonal(memberId)
                            cacheManager.put(
                                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(
                                    group
                                )
                            )
                            cacheManager.put(
                                TechresEnum.CHAT_PERSONAL.toString(),
                                TechresEnum.GROUP_CHAT.toString()
                            )
                            val intent = Intent(this@ProfileFriendActivity, ChatActivity::class.java)
                            intent.putExtra(
                                TechresEnum.GROUP_CHAT.toString(),
                                Gson().toJson(responseGroup.data)
                            )
                            startActivity(intent)
                            overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                        } else {
                            Toast.makeText(
                                this@ProfileFriendActivity,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

    private fun getListBranchReview(page: Int) {
        val listReviewBranchParam = ListReviewBranchParam()
        listReviewBranchParam.http_method = AppConfig.GET
        listReviewBranchParam.project_id = AppConfig.PROJECT_CHAT
        listReviewBranchParam.request_url = "/api/branch-reviews"
        listReviewBranchParam.params.restaurant_id = -1
        listReviewBranchParam.params.branch_id = -1
        listReviewBranchParam.params.branch_review_status = 1
        listReviewBranchParam.params.customer_id = idProfile
        listReviewBranchParam.params.types = "0,1,3"
        listReviewBranchParam.params.limit = limit
        listReviewBranchParam.params.page = page

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListReviewBranch(listReviewBranchParam)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ReviewBranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ReviewBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        numCheckUpload += 1
                        if (numCheckUpload == 4){
                            checkLoadingProfile()
                        }
                        val dataTimeline = response.data.list
                        total = response.data.total_record ?: 0
                        limit = response.data.limit ?: 0

                        if (data.size == 0) {
                            data.add(PostReview())
                            data.add(PostReview())
                            data.add(PostReview())
                            data.add(PostReview())
                            data.addAll(data.size, dataTimeline)
                            getProfile(idProfile)
                            checkStatusProfile()
                            getListFriend(idProfile)
                        } else {
                            isLoading = true
                            data.removeIf { item -> item.loading == 1 }
                            data.addAll(data.size, dataTimeline)
                        }
                        profileAdapter?.setDataSource(data)
                    } else Toast.makeText(
                        this@ProfileFriendActivity,
                        resources.getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

    }

    private fun removePost(id: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 1
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/branch-reviews/$id/remove"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .removePost(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                }
            })

    }

    private fun postReaction(id: String, idReaction: Int) {

        val postReactionReviewParams = PostReactionReviewParams()
        postReactionReviewParams.http_method = 1
        postReactionReviewParams.project_id = AppConfig.PROJECT_CHAT
        postReactionReviewParams.request_url = "/api/branch-reviews-reactions"

        postReactionReviewParams.params.reaction_id = idReaction
        postReactionReviewParams.params.branch_review_id = id
        postReactionReviewParams.params.java_access_token = String.format(
            "%s %s",
            "Bearer",
            user.access_token
        )

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .postReactionReview(postReactionReviewParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                }
            })

    }

    private fun saveBranch(branchId: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api/customers/" + user.id + "/save-branch?branch_id=$branchId"
        ServiceFactory.createRetrofitService(

            TechResService::class.java
        )

            .saveBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                @SuppressLint("NotifyDataSetChanged")
                override fun onNext(response: BaseResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                            if (listIDBranch.contains(branchId)) {
                                listIDBranch.remove(branchId)
                            } else {
                                listIDBranch.add(branchId)
                            }
                            cacheManager.put(
                                TechresEnum.LIST_SAVE_BRANCH.toString(),
                                Gson().toJson(listIDBranch)
                            )
                            profileAdapter?.notifyDataSetChanged()
                        }
                        else -> {
                        }
                    }
                }
            })
    }


    private fun checkStatusProfile() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = String.format("%s%s", "/api/friends/status?user_id=", idProfile)

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .checkStatusProfile(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<StatusProfileResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: StatusProfileResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        numCheckUpload += 1
                        if (numCheckUpload == 4){
                            checkLoadingProfile()
                        }
                        response.data?.let { profileAdapter?.setCheckStatusProfile(it) }
                        profileAdapter?.notifyItemChanged(0)
                    } else Toast.makeText(
                        this@ProfileFriendActivity,
                        resources.getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun acceptFriend(profile: User) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = profile.id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("approve-to-contact", jsonObject)
            WriteLog.d("approve-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = profile.id


        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .acceptFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status != AppConfig.SUCCESS_CODE) {
                            Toast.makeText(
                                this@ProfileFriendActivity,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

    private fun unAcceptFriend(profile: User) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = profile.id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("not-accept-approve-to-contact", jsonObject)
            WriteLog.d("not-accept-approve-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val friendParams = FriendParams()
        friendParams.http_method = AppConfig.POST
        friendParams.request_url = "/api/contact-froms/not-accept"
        friendParams.project_id = AppConfig.PROJECT_CHAT
        friendParams.params.contact_from_user_id = profile.id
        friendParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .notAcceptFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                    }
                })
        }
    }

    override fun clickImage(url: String, position: Int) {
        lookAtPhoto(url, position)
    }

    override fun createPostProfile() {
        val intent = Intent(this, CreatePostNewsFeedActivity::class.java)
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onAddFriend() {
        addFriend(dataProfile)
    }

    override fun onUnfriend(data: User) {
        unFriend(data)
    }

    override fun onReplyFriend() {
    }

    override fun onCancelRequestFriend() {
        cancelFriendRequest(dataProfile)
    }

    override fun chat() {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(dataProfile.id)
        createGroupPersonal(memberList, dataProfile.id)
    }

    override fun clickButtonFriend() {

    }

    override fun onAcceptFriend(data: User) {
        acceptFriend(data)
    }

    override fun onUnAcceptFriend(data: User) {
        unAcceptFriend(data)
    }

    override fun clickEditProfile() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                R.anim.translate_from_right,
                R.anim.translate_to_left,
                R.anim.translate_from_left,
                R.anim.translate_to_right
            )
            add<EditProfileFragment>(R.id.frameProfileFriend, null)
            addToBackStack(EditProfileFragment().tag)
        }
    }

    override fun searchFriend() {
        val intent = Intent(this, AddFriendActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun clickViewAllFriend() {
        val intent = Intent(this, MyFriendActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), idProfile)
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun clickMediaImage() {
        val bundle = Bundle()
        bundle.putInt(TechresEnum.ID_USER.toString(), idProfile)
        bundle.putString(TechresEnum.USERNAME.toString(), dataProfile.name)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                R.anim.translate_from_right,
                R.anim.translate_to_left,
                R.anim.translate_from_left,
                R.anim.translate_to_right
            )
            add<ProfileImageFragment>(R.id.frameProfileFriend, args = bundle)
            addToBackStack(ProfileImageFragment().tag)
        }
    }

    override fun clickMediaVideo() {
        val bundle = Bundle()
        bundle.putInt(TechresEnum.ID_USER.toString(), idProfile)
        bundle.putString(TechresEnum.USERNAME.toString(), dataProfile.name ?: "")
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                R.anim.translate_from_right,
                R.anim.translate_to_left,
                R.anim.translate_from_left,
                R.anim.translate_to_right
            )
            add<ProfileVideoFragment>(R.id.frameProfileFriend, args = bundle)
            addToBackStack(ProfileVideoFragment().tag)
        }
    }

    override fun clickMediaAlbum() {
        val bundle = Bundle()
        bundle.putInt(TechresEnum.ID_USER.toString(), idProfile)
        bundle.putString(TechresEnum.USERNAME.toString(), dataProfile.name)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                R.anim.translate_from_right,
                R.anim.translate_to_left,
                R.anim.translate_from_left,
                R.anim.translate_to_right
            )
            add<ProfileAlbumFragment>(R.id.frameProfileFriend, args = bundle)
            addToBackStack(ProfileAlbumFragment().tag)
        }
    }

    override fun clickReportUser() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_report_user_profile)

        val txtSensitiveContent = dialog.findViewById(R.id.txtSensitiveContent) as TextView
        val txtBother = dialog.findViewById(R.id.txtBother) as TextView
        val txtCheat = dialog.findViewById(R.id.txtCheat) as TextView
        val txtOtherReason = dialog.findViewById(R.id.txtOtherReason) as TextView

        txtSensitiveContent.setOnClickListener {
            createReportUser(txtSensitiveContent.text.toString())
            dialog.dismiss()
        }
        txtBother.setOnClickListener {
            createReportUser(txtBother.text.toString())
            dialog.dismiss()
        }
        txtCheat.setOnClickListener {
            createReportUser(txtCheat.text.toString())
            dialog.dismiss()
        }
        txtOtherReason.setOnClickListener {
            dialog.dismiss()
            val dialogOtherReason = Dialog(this)
            dialogOtherReason.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialogOtherReason.setCancelable(true)
            dialogOtherReason.setContentView(R.layout.dialog_input_report_other_reason)

            val edtContent = dialogOtherReason.findViewById(R.id.edtContent) as EditText
            val btnCancel = dialogOtherReason.findViewById(R.id.btnCancel) as Button
            val btnSend = dialogOtherReason.findViewById(R.id.btnSend) as Button

            showKeyboard(edtContent)

            btnCancel.setOnClickListener {
                dialogOtherReason.dismiss()
            }

            btnSend.setOnClickListener {
                if (edtContent.text.toString().trim().isEmpty()){
                    Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show()
                }else{
                    createReportUser(edtContent.text.toString())
                    dialogOtherReason.dismiss()
                    closeKeyboard(edtContent)
                }
            }

            dialogOtherReason.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogOtherReason.show()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun createReportUser(content: String) {
        val reportUserParams = ReportUserParams()
        reportUserParams.http_method = 1
        reportUserParams.project_id = AppConfig.PROJECT_ALOLINE
        reportUserParams.request_url = "/api/customers/report-customer"
        reportUserParams.params.customer_id = idProfile
        reportUserParams.params.content = content
        reportUserParams.params.project_id = AppConfig.PROJECT_ALOLINE
        reportUserParams.params.type = 0
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )

            .reportUser(reportUserParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                    //onComplete
                }
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {
                    //onSubscribe
                }
                override fun onNext(response: BaseResponse) {
                    Toast.makeText(this@ProfileFriendActivity, "Báo cáo thành công", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onMyAvatar() {
    }

    override fun onAvatar(url: String, position: Int) {
        lookAtPhoto(url, position)
    }

    override fun onPost() {
    }

    override fun onReview() {
    }

    override fun onComment(
        positionPost: Int,
        type: String,
        detailPost: PostReview,
        positionComment: Int
    ) {
        this.positionPost = positionPost
        val intent = Intent(this, CommentActivity::class.java)
        intent.putExtra(TechresEnum.DETAIL_POST_COMMENT.toString(), Gson().toJson(detailPost))
        intent.putExtra(TechresEnum.POSITION_REPLY.toString(), positionComment)
        when (type) {
            TechresEnum.TYPE_COMMENT_TEXT.toString() -> {
                intent.putExtra(
                    TechresEnum.CHECK_COMMENT_CHOOSE.toString(),
                    TechresEnum.TYPE_COMMENT_TEXT.toString()
                )
            }
            TechresEnum.TYPE_COMMENT_MEDIA.toString() -> {
                intent.putExtra(
                    TechresEnum.CHECK_COMMENT_CHOOSE.toString(),
                    TechresEnum.TYPE_COMMENT_MEDIA.toString()
                )
            }
            TechresEnum.TYPE_COMMENT_REPLY.toString() -> {
                intent.putExtra(
                    TechresEnum.CHECK_COMMENT_CHOOSE.toString(),
                    TechresEnum.TYPE_COMMENT_REPLY.toString()
                )
            }
        }
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onShare() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            "Application Link : https://play.google.com/store/apps/details?id=${packageName}"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"))
    }

    override fun onButtonMore(position: Int) {
        this.positionPost = position
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_more_new_feed)
        bottomSheetDialog.setCancelable(true)
        val btnDeletePostsNewFeed = bottomSheetDialog.findViewById<Button>(R.id.btnDeletePostsNewFeed)
        val btnEditPostsNewFeed = bottomSheetDialog.findViewById<Button>(R.id.btnEditPostsNewFeed)
        val btnSendReport = bottomSheetDialog.findViewById<Button>(R.id.btnPostsRepost)

        if (data[position].customer.member_id != user.id){
            btnDeletePostsNewFeed!!.visibility = View.GONE
            btnEditPostsNewFeed!!.visibility = View.GONE
            btnSendReport!!.visibility = View.VISIBLE
        }else{
            btnDeletePostsNewFeed!!.visibility = View.VISIBLE
            btnEditPostsNewFeed!!.visibility = View.VISIBLE
            btnSendReport!!.visibility = View.GONE
        }

        btnDeletePostsNewFeed.setOnClickListener {
            bottomSheetDialog.dismiss()
            dialogQuestionDeletePost(position)
        }

        btnSendReport.setOnClickListener {
            bottomSheetDialog.dismiss()
            postReport(position)
        }

        btnEditPostsNewFeed.setOnClickListener {
            val intent = Intent(this, EditMyPostActivity::class.java)
            intent.putExtra(TechresEnum.ID_REVIEW.toString(), data[position]._id ?: "")
            startActivity(intent)
            this.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun postReport(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.DialogStyle)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_post_repost)
        bottomSheetDialog.setCancelable(true)
        val btnSendReport = bottomSheetDialog.findViewById<Button>(R.id.btnSendReport)
        val edtContent = bottomSheetDialog.findViewById<EditText>(R.id.edtContent)

        showKeyboard(edtContent!!)

        btnSendReport!!.setOnClickListener {
            if (edtContent.text.toString().isEmpty()){
                Toast.makeText(this, this.getString(R.string.enter_content_please), Toast.LENGTH_SHORT).show()
            }else{
                bottomSheetDialog.dismiss()
                createReport(data[position]._id!!, data[position].customer.full_name!! , edtContent.text.toString())
            }
        }
        bottomSheetDialog.show()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun createReport(idPost: String, namePost: String, content: String) {
        val postReportParams = PostReportParams()
        postReportParams.http_method = 1
        postReportParams.project_id = AppConfig.PROJECT_CHAT
        postReportParams.request_url = "/api/branch-reviews-report"
        postReportParams.params.branch_review_id = idPost
        postReportParams.params.customer_id = user.id
        postReportParams.params.content = content
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .postPostReport(postReportParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                    //onComplete
                }
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {
                    //onSubscribe
                }
                override fun onNext(response: BaseResponse) {
                    dialogDoneReport(namePost)
                }
            })
    }

    private fun dialogDoneReport(namePost: String){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_notification_report)

        val btnDone = dialog.findViewById(R.id.btnDone) as TextView
        val txtContentReport = dialog.findViewById(R.id.txtContentReport) as TextView

        txtContentReport.text = "Chúng tôi đã nhận được báo cáo\ncủa bạn và sẽ xem xét bài viết của ${namePost}."

        btnDone.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onMedia(url: ArrayList<Media>, position: Int) {
        val list = ArrayList<String>()
        url.forEach {
            list.add(String.format("%s%s", configNodeJs.api_ads, it.original))
        }
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onMedia(url: ArrayList<Media>, position: Int, seekTo: Int) {
    }

    override fun onBranchDetail(id: Int?) {
        val intent = Intent(this, BranchDetailActivity::class.java)
        intent.putExtra(TechresEnum.BRANCH_ID.toString(), id ?: 0)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onBranchReviewDetail(id: String?) {
    }

    override fun onReaction(id: String, idReaction: Int) {
        postReaction(id, idReaction)
    }

    override fun onReactionDetail(position: Int, id: String?) {
        val intent = Intent(this, ReactionDetailManagerActivity::class.java)
        intent.putExtra(TechresEnum.ID_REVIEW.toString(), id)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onSaveBranch(position: Int, branchID: Int) {
        saveBranch(branchID)
    }

    override fun onDetailRatingReview(
        serviceRate: Float,
        foodRate: Float,
        priceRate: Float,
        spaceRate: Float,
        hygieneRate: Float
    ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_detail_rating_review)

        val ratingService = dialog.findViewById(R.id.ratingService) as RatingBar
        val ratingFood = dialog.findViewById(R.id.ratingFood) as RatingBar
        val ratingPrice = dialog.findViewById(R.id.ratingPrice) as RatingBar
        val ratingSpace = dialog.findViewById(R.id.ratingSpace) as RatingBar
        val ratingHygiene = dialog.findViewById(R.id.ratingHygiene) as RatingBar

        val imgService = dialog.findViewById(R.id.imgService) as ImageView
        val imgFood = dialog.findViewById(R.id.imgFood) as ImageView
        val imgPrice = dialog.findViewById(R.id.imgPrice) as ImageView
        val imgSpace = dialog.findViewById(R.id.imgSpace) as ImageView
        val imgHygiene = dialog.findViewById(R.id.imgHygiene) as ImageView
        val txtService = dialog.findViewById(R.id.txtService) as TextView
        val txtFood = dialog.findViewById(R.id.txtFood) as TextView
        val txtPrice = dialog.findViewById(R.id.txtPrice) as TextView
        val txtSpace = dialog.findViewById(R.id.txtSpace) as TextView
        val txtHygiene = dialog.findViewById(R.id.txtHygiene) as TextView

        ratingService.rating = serviceRate
        ratingFood.rating = foodRate
        ratingPrice.rating = priceRate
        ratingSpace.rating = spaceRate
        ratingHygiene.rating = hygieneRate

        when (serviceRate) {
            0f, 1f -> {
                imgService.setImageResource(R.drawable.ic_rating_1)
                txtService.text = resources.getString(R.string.bad)
            }
            2f -> {
                imgService.setImageResource(R.drawable.ic_rating_2)
                txtService.text = resources.getString(R.string.least)
            }
            3f -> {
                imgService.setImageResource(R.drawable.ic_rating_3)
                txtService.text = resources.getString(R.string.okey)
            }
            4f -> {
                imgService.setImageResource(R.drawable.ic_rating_4)
                txtService.text = resources.getString(R.string.rather)
            }
            5f -> {
                imgService.setImageResource(R.drawable.ic_rating_5)
                txtService.text = resources.getString(R.string.great)
            }
        }

        when (foodRate) {
            0f, 1f -> {
                imgFood.setImageResource(R.drawable.ic_rating_1)
                txtFood.text = resources.getString(R.string.bad)
            }
            2f -> {
                imgFood.setImageResource(R.drawable.ic_rating_2)
                txtFood.text = resources.getString(R.string.least)
            }
            3f -> {
                imgFood.setImageResource(R.drawable.ic_rating_3)
                txtFood.text = resources.getString(R.string.okey)
            }
            4f -> {
                imgFood.setImageResource(R.drawable.ic_rating_4)
                txtFood.text = resources.getString(R.string.rather)
            }
            5f -> {
                imgFood.setImageResource(R.drawable.ic_rating_5)
                txtFood.text = resources.getString(R.string.great)
            }
        }

        when (priceRate) {
            0f, 1f -> {
                imgPrice.setImageResource(R.drawable.ic_rating_1)
                txtPrice.text = resources.getString(R.string.bad)
            }
            2f -> {
                imgPrice.setImageResource(R.drawable.ic_rating_2)
                txtPrice.text = resources.getString(R.string.least)
            }
            3f -> {
                imgPrice.setImageResource(R.drawable.ic_rating_3)
                txtPrice.text = resources.getString(R.string.okey)
            }
            4f -> {
                imgPrice.setImageResource(R.drawable.ic_rating_4)
                txtPrice.text = resources.getString(R.string.rather)
            }
            5f -> {
                imgPrice.setImageResource(R.drawable.ic_rating_5)
                txtPrice.text = resources.getString(R.string.great)
            }
        }

        when (spaceRate) {
            0f, 1f -> {
                imgSpace.setImageResource(R.drawable.ic_rating_1)
                txtSpace.text = resources.getString(R.string.bad)
            }
            2f -> {
                imgSpace.setImageResource(R.drawable.ic_rating_2)
                txtSpace.text = resources.getString(R.string.least)
            }
            3f -> {
                imgSpace.setImageResource(R.drawable.ic_rating_3)
                txtSpace.text = resources.getString(R.string.okey)
            }
            4f -> {
                imgSpace.setImageResource(R.drawable.ic_rating_4)
                txtSpace.text = resources.getString(R.string.rather)
            }
            5f -> {
                imgSpace.setImageResource(R.drawable.ic_rating_5)
                txtSpace.text = resources.getString(R.string.great)
            }
        }

        when (hygieneRate) {
            0f, 1f -> {
                imgHygiene.setImageResource(R.drawable.ic_rating_1)
                txtHygiene.text = resources.getString(R.string.bad)
            }
            2f -> {
                imgHygiene.setImageResource(R.drawable.ic_rating_2)
                txtHygiene.text = resources.getString(R.string.least)
            }
            3f -> {
                imgHygiene.setImageResource(R.drawable.ic_rating_3)
                txtHygiene.text = resources.getString(R.string.okey)
            }
            4f -> {
                imgHygiene.setImageResource(R.drawable.ic_rating_4)
                txtHygiene.text = resources.getString(R.string.rather)
            }
            5f -> {
                imgHygiene.setImageResource(R.drawable.ic_rating_5)
                txtHygiene.text = resources.getString(R.string.great)
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onDetailValuePost(idReview: String?) {
        val intent = Intent(this, ReactionDetailManagerActivity::class.java)
        intent.putExtra(TechresEnum.ID_REVIEW.toString(), idReview ?: "")
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onClickProfile(position: Int, userID: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), userID)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onSentComment(position: Int, id: String, content: String) {
//        createReviewCommentRestaurant(position, idComment, content)
    }

    override fun onShowFullVideoYouTube(youtube: YouTube) {
        val intent = Intent(this, ShowFullScreenYouTubeActivity::class.java)
        intent.putExtra(TechresEnum.VIDEO_YOUTUBE.toString(), Gson().toJson(youtube))
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onLink(url: String) {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onReactionComment(idPost: String, idComment: String) {
        postReactionComment(idPost, idComment)
    }

    override fun onClickItemFriend(id: Int, position: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), id)
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    private fun lookAtPhoto(url: String, position: Int) {
        val list = ArrayList<String>()
        list.add(String.format("%s%s", configNodeJs.api_ads, url))
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun dialogQuestionDeletePost(position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_post_new_feed)

        val btnDeleteNow = dialog.findViewById(R.id.btnDeleteNow) as Button
        val btnBack = dialog.findViewById(R.id.btnBack) as Button

        btnDeleteNow.setOnClickListener {
            removePost(data[position]._id!!)
            data.removeAt(position)
            profileAdapter?.notifyDataSetChanged()
            dialog.dismiss()
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun postReactionComment(idPost: String, idComment: String) {
        val postReactionCommentParams = PostReactionCommentParams()
        postReactionCommentParams.http_method = AppConfig.POST
        postReactionCommentParams.project_id = AppConfig.PROJECT_CHAT
        postReactionCommentParams.request_url = "/api/branch-reviews-reactions"
        postReactionCommentParams.params.branch_review_id = idPost
        postReactionCommentParams.params.comment_id = idComment
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .postReactionComment(postReactionCommentParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                }
            })
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

}