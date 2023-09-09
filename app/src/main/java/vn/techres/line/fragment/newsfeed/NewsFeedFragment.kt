package vn.techres.line.fragment.newsfeed

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZVideoPlayerStandard
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.alexbbb.uploadservice.MultipartUploadRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import kohii.v1.exoplayer.Kohii
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.activity.*
import vn.techres.line.adapter.newsfeed.NewsFeedAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.OrderReview
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.data.model.branch.response.SaveBranchResponse
import vn.techres.line.data.model.eventbus.CommentEventBusData
import vn.techres.line.data.model.eventbus.EventBusSaveBranch
import vn.techres.line.data.model.eventbus.UserUpdateEventBus
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.friend.FriendSuggestResponse
import vn.techres.line.data.model.main.DisconnectNodeRequest
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.profile.response.UpdateProfileResponse
import vn.techres.line.data.model.reaction.ValueReaction
import vn.techres.line.data.model.reaction.ValueReactionResponse
import vn.techres.line.data.model.response.*
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.utils.*
import vn.techres.line.databinding.FragmentNewsFeedBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.getCurrentPosition
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.newsfeed.FriendSuggestNewFeedHandler
import vn.techres.line.interfaces.newsfeed.NewsFeedHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.util.*
import kotlin.math.ceil

@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class NewsFeedFragment :
    BaseBindingFragment<FragmentNewsFeedBinding>(FragmentNewsFeedBinding::inflate), NewsFeedHandler,
    FriendSuggestNewFeedHandler {
    val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var internalStorage = InternalStorage()
    private var adapterReview: NewsFeedAdapter? = null
    private var page = 1
    private var total = 0
    private var limit = 50
    private var isLoading = true
    private var data = ArrayList<PostReview>()
    private var review = ArrayList<PostReview>()
    private var configJava = ConfigJava()
    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private var dataOrderReview = ArrayList<OrderReview>()
    private var reactionData = ArrayList<ValueReaction>()
    private var serviceRate = 1
    private var foodRate = 1
    private var priceRate = 1
    private var spaceRate = 1
    private var hygieneRate = 1
    private var orderReview = "0"
    private var currentPost = ""
    private var isCheckCurrentPost = false
    private var listIDBranch = ArrayList<Int>()
    private var comment = Comment()
    private var positionPost = 0
    private var listFriendSuggest = ArrayList<Friend>()
    private val application = TechResApplication()
    private var mSocket: Socket? = null

    //Picture
    private val userAgent = "Techres/" + com.alexbbb.uploadservice.BuildConfig.VERSION_NAME
    private val REQUEST_PICKER_AVATAR = 1
    private var urlAvatar = ""
    private var file = File("")

    //Dialog review order
    private var dialog: Dialog? = null
    private var txtUserName: TextView? = null
    private var txtTime: TextView? = null
    private var txtPoint: TextView? = null
    private var txtRestaurantName: TextView? = null
    private var imgClose: ImageView? = null
    private var btnSend: Button? = null
    private var ratingService: RatingBar? = null
    private var ratingFood: RatingBar? = null
    private var ratingPrice: RatingBar? = null
    private var ratingSpace: RatingBar? = null
    private var ratingHygiene: RatingBar? = null
    private var imgService: ImageView? = null
    private var imgFood: ImageView? = null
    private var imgPrice: ImageView? = null
    private var imgSpace: ImageView? = null
    private var imgHygiene: ImageView? = null
    private var txtService: TextView? = null
    private var txtFood: TextView? = null
    private var txtPrice: TextView? = null
    private var txtSpace: TextView? = null
    private var txtHygiene: TextView? = null

    //Dialog update avatar
    private var dialogUpdateAvatar: Dialog? = null
    private var imgAvatarUpdate: ImageView? = null
    private var imgCamera: ImageView? = null
    private var imgCloseUpdate: ImageView? = null
    private var btnUpdate: Button? = null

    private var checkSwipeRefresh = false

    private var youTubePlayer: YouTubePlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(mainActivity)
        configJava = CurrentConfigJava.getConfigJava(mainActivity!!)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(mainActivity!!)
        mSocket = application.getSocketInstance(mainActivity!!.baseContext)
        mSocket?.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)
        //Dialog order review
        dialog = Dialog(mainActivity!!.baseContext)
        dialog = this.context?.let { Dialog(it) }!!
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(false)
        dialog!!.setContentView(R.layout.dialog_accumulate_points)

        //Dialog update avatar
        dialogUpdateAvatar = Dialog(mainActivity!!.baseContext)
        dialogUpdateAvatar = this.context?.let { Dialog(it) }!!
        dialogUpdateAvatar!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogUpdateAvatar!!.setCancelable(true)
        dialogUpdateAvatar!!.setContentView(R.layout.dialog_update_avatar)

        //Dialog review order
        val imgTutorial = dialog?.findViewById(R.id.imgTutorial) as ImageView
        txtUserName = dialog!!.findViewById(R.id.txtUserName) as TextView
        txtTime = dialog!!.findViewById(R.id.txtTime) as TextView
        txtPoint = dialog!!.findViewById(R.id.txtPoint) as TextView
        txtRestaurantName = dialog!!.findViewById(R.id.txtRestaurantName) as TextView
        imgClose = dialog!!.findViewById(R.id.imgClose) as ImageView
        btnSend = dialog!!.findViewById(R.id.btnSend) as Button
        ratingService = dialog!!.findViewById(R.id.ratingService) as RatingBar
        ratingFood = dialog!!.findViewById(R.id.ratingFood) as RatingBar
        ratingPrice = dialog!!.findViewById(R.id.ratingPrice) as RatingBar
        ratingSpace = dialog!!.findViewById(R.id.ratingSpace) as RatingBar
        ratingHygiene = dialog!!.findViewById(R.id.ratingHygiene) as RatingBar
        imgService = dialog!!.findViewById(R.id.imgService) as ImageView
        imgFood = dialog!!.findViewById(R.id.imgFood) as ImageView
        imgPrice = dialog!!.findViewById(R.id.imgPrice) as ImageView
        imgSpace = dialog!!.findViewById(R.id.imgSpace) as ImageView
        imgHygiene = dialog!!.findViewById(R.id.imgHygiene) as ImageView
        txtService = dialog!!.findViewById(R.id.txtService) as TextView
        txtFood = dialog!!.findViewById(R.id.txtFood) as TextView
        txtPrice = dialog!!.findViewById(R.id.txtPrice) as TextView
        txtSpace = dialog!!.findViewById(R.id.txtSpace) as TextView
        txtHygiene = dialog!!.findViewById(R.id.txtHygiene) as TextView

        Glide.with(mainActivity!!.baseContext)
            .load(R.drawable.howtoreview)
            .into(imgTutorial)

        //Dialog update avatar
        imgAvatarUpdate = dialogUpdateAvatar!!.findViewById(R.id.imgAvatar) as ImageView
        imgCamera = dialogUpdateAvatar!!.findViewById(R.id.imgCamera) as ImageView
        imgCloseUpdate = dialogUpdateAvatar!!.findViewById(R.id.imgClose) as ImageView
        btnUpdate = dialogUpdateAvatar!!.findViewById(R.id.btnUpdate) as Button

        val kohii = Kohii[this]
        val manager = kohii.register(this).addBucket(binding.recyclerViewNewsFeed)
        val windowManager: WindowManager =
            context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        user = CurrentUser.getCurrentUser(mainActivity!!)
        adapterReview = NewsFeedAdapter(
            mainActivity!!,
            kohii,
            manager,
            lifecycle,
            mainActivity!!,
            width,
            height
        )
        adapterReview!!.setHasStableIds(true)
        adapterReview!!.setNewFeedHandler(this@NewsFeedFragment)
        adapterReview!!.setClickFriendSuggest(this@NewsFeedFragment)

        binding.recyclerViewNewsFeed.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
        }
        binding.recyclerViewNewsFeed.adapter = adapterReview
        binding.recyclerViewNewsFeed.setItemViewCacheSize(10)
        setListener()
        setData()
    }

    override fun onPause() {
        super.onPause()
        JZVideoPlayerStandard.releaseAllVideos()
        adapterReview!!.pauseYouTube()
    }
    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPost(dataPost: PostReview) {
        if (dataPost._id!! != "") {
            when (dataPost.status_reload) {
                "create" -> {
                    data.add(3, dataPost)
                    adapterReview!!.setDataSource(data)
                }
                "edit" -> {
                    data.add(positionPost, dataPost)
                    data.removeAt(positionPost + 1)

                    adapterReview!!.setDataSource(data)
                }
            }

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMyComment(postList: CommentEventBusData) {
        if (postList.data.size != 0) {
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
            adapterReview!!.setDataSource(data)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClickTab(scrollScreen: ScrollScreen) {
        if (scrollScreen.currentPage == 2) {
            if (scrollScreen.click == 2) {
                binding.recyclerViewNewsFeed.scrollToPosition(0)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserUpdate(dataUser: UserUpdateEventBus) {
        binding.imgAvatar.load(
            String.format(
                "%s%s",
                nodeJs.api_ads,
                dataUser.data.avatar_three_image.thumb
            )
        )
        adapterReview!!.notifyDataSetChanged()
    }

//    override fun onResume() {
//        super.onResume()
//        adapterReview!!.startVideo()
//    }
    /**
     *  override fun onResume() {
    super.onResume()
    if (data.isEmpty()){
    page = 1
    binding.recyclerViewNewsFeed.recycledViewPool.clear()
    getListSaveBranch()
    getListFriendSuggest()
    getListBranchReview(page)
    adapterReview!!.notifyDataSetChanged()
    }
    adapterReview!!.startVideo()
    mainActivity?.setOnBackClick(this)
    }
     */

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSaveBranch(boolean: EventBusSaveBranch) {
        if (boolean.isCheck) {
            adapterReview!!.notifyDataSetChanged()
        }
    }

    /**
     * override fun onResume() {
    super.onResume()
    if (data.isEmpty()){
    page = 1
    binding.recyclerViewNewsFeed.recycledViewPool.clear()
    getListSaveBranch()
    getListFriendSuggest()
    getListBranchReview(page)
    adapterReview!!.notifyDataSetChanged()
    }
    adapterReview!!.startVideo()
    mainActivity?.setOnBackClick(this)
    }
     */


    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onStop() {
        super.onStop()
        adapterReview!!.stopVideo()
        //  adapterReview!!.stopVideo()
    }

    private fun setData() {
        binding.tvTotalValue.text = restaurant().alo_point.toString()
        binding.imgAvatar.load(String.format("%s%s", nodeJs.api_ads, user.avatar_three_image.thumb))
        {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
            size(500, 500)
            transformations(RoundedCornersTransformation(10F))
        }
        // Review bill and update avatar
        if (cacheManager.get(TechresEnum.CHECK_UPDATE_AVATAR.toString()).equals("false")) {
            getOrderReview()
            cacheManager.put(TechresEnum.CHECK_UPDATE_AVATAR.toString(), "true").toString()
        }


        if (cacheManager.get(TechresEnum.KEY_CHECK_LOAD_REVIEW.toString()) == "false") {
            binding.lnLoading.visibility = View.VISIBLE
            binding.shimmerNewsFeed.startShimmerAnimation()
            cacheManager.put(TechresEnum.KEY_CHECK_LOAD_REVIEW.toString(), "true").toString()
            data.clear()
            if (reactionData.size == 0) {
                getListValueReaction()
                getLevelValue()
            }
            getListSaveBranch()
            getListFriendSuggest()
            getListBranchReview(page)
            getProfile()
        } else {
            adapterReview!!.setDataSource(data)
            adapterReview!!.setListFriendSuggest(listFriendSuggest)
        }
    }

    private fun setListener() {
        binding.swipeRefresh.setOnRefreshListener {
            checkSwipeRefresh = true
            page = 1
            /**
             *  binding.recyclerViewNewsFeed.recycledViewPool.clear()
             */

            getListSaveBranch()
            getListFriendSuggest()
            getListBranchReview(page)
            /**
             *  getProfile()
            adapterReview!!.notifyDataSetChanged()
             */
            Handler().postDelayed({ binding.swipeRefresh.isRefreshing = false }, 500)
        }

        binding.lnLeverValue.setOnClickListener {
//            mainActivity?.let {
//                Navigation.findNavController(
//                    it,
//                    R.id.nav_host
//                ).navigate(R.id.historyLevelValueFragment)
//            }
        }
        binding.tvSearchView.setOnClickListener {
            val intent = Intent(mainActivity, SearchNewsFeedActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        binding.imgAvatar.setOnClickListener {
            onClickProfile(user.id)
        }

        binding.recyclerViewNewsFeed.addOnScrollListener(object :
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
                            adapterReview!!.notifyItemInserted(data.size)
                            Handler().postDelayed({
                                getListBranchReview(page)
                            }, 1000)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })
    }

    private fun getListBranchReview(page: Int) {
        val listReviewBranchParam = ListReviewBranchParam()
        listReviewBranchParam.http_method = 0
        listReviewBranchParam.project_id = AppConfig.PROJECT_CHAT
        listReviewBranchParam.request_url = "/api/branch-reviews"
        listReviewBranchParam.params.restaurant_id = -1
        listReviewBranchParam.params.branch_review_status = 1
        listReviewBranchParam.params.branch_id = -1
        listReviewBranchParam.params.customer_id = -1
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
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                            if (checkSwipeRefresh) {
                                Handler().postDelayed(
                                    { binding.swipeRefresh.isRefreshing = false },
                                    500
                                )
                                checkSwipeRefresh = false
                                data.clear()
                                review.clear()
                            }
                            review = response.data.list
                            total = response.data.total_record!!
                            limit = response.data.limit!!
                            if (data.size == 0) {
                                data.add(PostReview())
                                data.add(PostReview())
                                data.add(PostReview())
                                data.addAll(data.size, review)
                                if (review.size > 23) {
                                    data.addAll(23, listOf(PostReview()))
                                }
                                WriteLog.d(NewsFeedFragment::class.java.simpleName, "Loading item")
                            } else {
                                WriteLog.d(NewsFeedFragment::class.java.simpleName, "removeIf item")
                                isLoading = true
                                data.removeIf { item -> item.loading == 1 }
                                data.addAll(data.size, review)
                            }

                            adapterReview!!.startVideo()
                            adapterReview!!.setDataSource(data)

                            Handler().postDelayed({
                                binding.lnLoading.visibility = View.GONE
                                binding.shimmerNewsFeed.hide()
                            }, 1500)
                        }
                        AppConfig.TOKEN_EXPIRED -> {
                            val dialog = Dialog(mainActivity!!)
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                            dialog.setCancelable(true)
                            dialog.setContentView(R.layout.dialog_move_to_login)
                            val btnGoToLogin = dialog.findViewById(R.id.btnGoToLogin) as TextView

                            btnGoToLogin.setOnClickListener {
                                logoutToken()
                                dialog.dismiss()
                            }

                            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                            dialog.show()
                        }
                        else -> {
                            data = ArrayList()
                            data.add(0, PostReview())
                            adapterReview!!.setDataSource(data)
                        }
                    }
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

        restaurant().restaurant_id?.let {
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


    }

    private fun bottomSheetLogin() {
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun showDialogNotificalAccumulate() {
        txtUserName!!.text = user.username
        if (dataOrderReview.size != 0) {
            txtTime!!.text = dataOrderReview[0].payment_date
            txtPoint!!.text = String.format(
                "%s %s",
                dataOrderReview[0].membership_point_added,
                mainActivity?.baseContext?.getString(R.string.point) ?: ""
            )
            txtRestaurantName!!.text = dataOrderReview[0].restaurant_name
        }

        ratingService!!.setOnRatingBarChangeListener { _, rating, _ ->
            imgService!!.visibility = View.VISIBLE
            txtService!!.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingService!!.rating = 1f
                    serviceRate = rating.toInt() + 1
                }
                1f -> {
                    imgService!!.setImageResource(R.drawable.ic_rating_1)
                    txtService!!.text = resources.getString(R.string.bad)
                    serviceRate = rating.toInt()
                }
                2f -> {
                    imgService!!.setImageResource(R.drawable.ic_rating_2)
                    txtService!!.text = resources.getString(R.string.least)
                    serviceRate = rating.toInt()
                }
                3f -> {
                    imgService!!.setImageResource(R.drawable.ic_rating_3)
                    txtService!!.text = resources.getString(R.string.okey)
                    serviceRate = rating.toInt()
                }
                4f -> {
                    imgService!!.setImageResource(R.drawable.ic_rating_4)
                    txtService!!.text = resources.getString(R.string.rather)
                    serviceRate = rating.toInt()
                }
                5f -> {
                    imgService!!.setImageResource(R.drawable.ic_rating_5)
                    txtService!!.text = resources.getString(R.string.great)
                    serviceRate = rating.toInt()
                }
            }
        }

        ratingFood!!.setOnRatingBarChangeListener { _, rating, _ ->
            imgFood!!.visibility = View.VISIBLE
            txtFood!!.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingFood!!.rating = 1f
                    foodRate = rating.toInt() + 1
                }
                1f -> {
                    imgFood!!.setImageResource(R.drawable.ic_rating_1)
                    txtFood!!.text = resources.getString(R.string.bad)
                    foodRate = rating.toInt()
                }
                2f -> {
                    imgFood!!.setImageResource(R.drawable.ic_rating_2)
                    txtFood!!.text = resources.getString(R.string.least)
                    foodRate = rating.toInt()
                }
                3f -> {
                    imgFood!!.setImageResource(R.drawable.ic_rating_3)
                    txtFood!!.text = resources.getString(R.string.okey)
                    foodRate = rating.toInt()
                }
                4f -> {
                    imgFood!!.setImageResource(R.drawable.ic_rating_4)
                    txtFood!!.text = resources.getString(R.string.rather)
                    foodRate = rating.toInt()
                }
                5f -> {
                    imgFood!!.setImageResource(R.drawable.ic_rating_5)
                    txtFood!!.text = resources.getString(R.string.great)
                    foodRate = rating.toInt()
                }
            }
        }

        ratingPrice!!.setOnRatingBarChangeListener { _, rating, _ ->
            imgPrice!!.visibility = View.VISIBLE
            txtPrice!!.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingPrice!!.rating = 1f
                    priceRate = rating.toInt() + 1
                }
                1f -> {
                    imgPrice!!.setImageResource(R.drawable.ic_rating_1)
                    txtPrice!!.text = resources.getString(R.string.bad)
                    priceRate = rating.toInt()
                }
                2f -> {
                    imgPrice!!.setImageResource(R.drawable.ic_rating_2)
                    txtPrice!!.text = resources.getString(R.string.least)
                    priceRate = rating.toInt()
                }
                3f -> {
                    imgPrice!!.setImageResource(R.drawable.ic_rating_3)
                    txtPrice!!.text = resources.getString(R.string.okey)
                    priceRate = rating.toInt()
                }
                4f -> {
                    imgPrice!!.setImageResource(R.drawable.ic_rating_4)
                    txtPrice!!.text = resources.getString(R.string.rather)
                    priceRate = rating.toInt()
                }
                5f -> {
                    imgPrice!!.setImageResource(R.drawable.ic_rating_5)
                    txtPrice!!.text = resources.getString(R.string.great)
                    priceRate = rating.toInt()
                }
            }
        }

        ratingSpace!!.setOnRatingBarChangeListener { _, rating, _ ->
            imgSpace!!.visibility = View.VISIBLE
            txtSpace!!.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingSpace!!.rating = 1f
                    spaceRate = rating.toInt() + 1
                }
                1f -> {
                    imgSpace!!.setImageResource(R.drawable.ic_rating_1)
                    txtSpace!!.text = resources.getString(R.string.bad)
                    spaceRate = rating.toInt()
                }
                2f -> {
                    imgSpace!!.setImageResource(R.drawable.ic_rating_2)
                    txtSpace!!.text = resources.getString(R.string.least)
                    spaceRate = rating.toInt()
                }
                3f -> {
                    imgSpace!!.setImageResource(R.drawable.ic_rating_3)
                    txtSpace!!.text = resources.getString(R.string.okey)
                    spaceRate = rating.toInt()
                }
                4f -> {
                    imgSpace!!.setImageResource(R.drawable.ic_rating_4)
                    txtSpace!!.text = resources.getString(R.string.rather)
                    spaceRate = rating.toInt()
                }
                5f -> {
                    imgSpace!!.setImageResource(R.drawable.ic_rating_5)
                    txtSpace!!.text = resources.getString(R.string.great)
                    spaceRate = rating.toInt()
                }
            }
        }

        ratingHygiene!!.setOnRatingBarChangeListener { _, rating, _ ->
            imgHygiene!!.visibility = View.VISIBLE
            txtHygiene!!.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingHygiene!!.rating = 1f
                    hygieneRate = rating.toInt() + 1
                }
                1f -> {
                    imgHygiene!!.setImageResource(R.drawable.ic_rating_1)
                    txtHygiene!!.text = resources.getString(R.string.bad)
                    hygieneRate = rating.toInt()
                }
                2f -> {
                    imgHygiene!!.setImageResource(R.drawable.ic_rating_2)
                    txtHygiene!!.text = resources.getString(R.string.least)
                    hygieneRate = rating.toInt()
                }
                3f -> {
                    imgHygiene!!.setImageResource(R.drawable.ic_rating_3)
                    txtHygiene!!.text = resources.getString(R.string.okey)
                    hygieneRate = rating.toInt()
                }
                4f -> {
                    imgHygiene!!.setImageResource(R.drawable.ic_rating_4)
                    txtHygiene!!.text = resources.getString(R.string.rather)
                    hygieneRate = rating.toInt()
                }
                5f -> {
                    imgHygiene!!.setImageResource(R.drawable.ic_rating_5)
                    txtHygiene!!.text = resources.getString(R.string.great)
                    hygieneRate = rating.toInt()
                }
            }
        }

        imgClose!!.setOnClickListener {
            dialog!!.dismiss()
            orderReview = cacheManager.put(TechresEnum.ORDER_REVIEW_KEY.toString(), "1").toString()
            if (user.avatar_three_image.original == "") {
                showDialogUpdateAvatar()
            }
        }

        btnSend!!.setOnClickListener {
            dataOrderReview[0].id?.let { it1 -> createOrderReview(it1) }
            orderReview = cacheManager.put(TechresEnum.ORDER_REVIEW_KEY.toString(), "1").toString()
            dialog!!.dismiss()
            if (user.avatar_three_image.original == "") {
                showDialogUpdateAvatar()
            }
        }

        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.show()
    }

    private fun getOrderReview() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/orders/waiting-reviews?page=1&limit=100&restaurant_id=" + restaurant().restaurant_id!!
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getListOrderReview(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OrderReviewResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: OrderReviewResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataOrderReview = response.data!!.list
                        if (dataOrderReview.size != 0) {
                            if (orderReview == "0") {
                                showDialogNotificalAccumulate()
                            }
                        } else {
                            if (user.avatar_three_image.original == "") {
                                showDialogUpdateAvatar()
                            }
                        }
                    }

                }
            })
    }

    private fun createOrderReview(idBill: Int) {
        mainActivity!!.setLoading(true)

        val params = OrderReviewParams()
        params.http_method = 1
        params.request_url = "/api/orders/$idBill/review"

        params.params.service_rate = serviceRate
        params.params.food_rate = foodRate
        params.params.price_rate = priceRate
        params.params.space_rate = spaceRate
        params.params.hygiene_rate = hygieneRate
        params.let {
            ServiceFactory.createRetrofitService(

                TechResService::class.java
            )

                .createReviewOrder(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        mainActivity!!.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        when (response.status) {
                            AppConfig.SUCCESS_CODE -> {
                                Toast.makeText(
                                    mainActivity,
                                    resources.getString(R.string.success_order_review),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                        mainActivity!!.setLoading(false)
                    }
                })
        }
    }

    private fun saveBranch(branchId: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 1
        baseRequest.request_url =
            String.format("%s%s", "/api/customers/%s/save-branch?branch_id=%s", user.id, branchId)
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
                    mainActivity!!.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
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
//                            adapterReview!!.notifyDataSetChanged()
                        }
                        else -> {
                            Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                    mainActivity!!.setLoading(false)
                }
            })
    }

    //Get list id branch saved
    private fun getListSaveBranch() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url ="/api/customers/${CurrentUser.getCurrentUser(mainActivity!!.baseContext).id}/saved-branches"

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getSaveBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SaveBranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: SaveBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val saveBranchList = response.data.list
                        listIDBranch.clear()
                        listIDBranch = saveBranchList.map { it.branch_id } as ArrayList<Int>
                        cacheManager.put(
                            TechresEnum.LIST_SAVE_BRANCH.toString(),
                            Gson().toJson(listIDBranch)
                        )
//                        adapterReview!!.notifyDataSetChanged()
                    }
                }
            })


    }

    //Get reaction (favourite, exciting, moving, value, negative, meaningless)
    private fun getListValueReaction() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/review-reactions/get-list-status?status=1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getValueReation(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ValueReactionResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ValueReactionResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        reactionData = response.data
                        cacheManager.put(
                            TechresEnum.VALUE_REACTION.toString(),
                            Gson().toJson(response.data)
                        )
                    }
                }
            })
    }

    private fun lookAtPhoto(url: String, position: Int) {
        val list = ArrayList<String>()
        list.add(String.format("%s%s", nodeJs.api_ads, url))
        val intent = Intent(mainActivity, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onReaction(id: String, idReaction: Int) {
        if (user.id == 0) {
            bottomSheetLogin()
        } else {
            postReaction(id, idReaction)
        }

    }

    override fun onComment(
        positionPost: Int,
        type: String,
        detailPost: PostReview,
        positionComment: Int
    ) {
        this.positionPost = positionPost
        val intent = Intent(context, CommentActivity::class.java)
        intent.putExtra(TechresEnum.DETAIL_POST_COMMENT.toString(), Gson().toJson(detailPost))
        intent.putExtra(TechresEnum.POSITION_REPLY.toString(), positionComment)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_from_right
        )
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
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onShare() {
        if (user.id == 0) {
            bottomSheetLogin()
        } else {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody =
                "Application Link : https://play.google.com/store/apps/details?id=${mainActivity!!.packageName}"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"))
        }
    }

    override fun onButtonMore(position: Int) {
        this.positionPost = position
        val bottomSheetDialog = BottomSheetDialog(this.mainActivity!!)
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
            val intent = Intent(mainActivity, EditMyPostActivity::class.java)
            intent.putExtra(TechresEnum.ID_REVIEW.toString(), data[position]._id ?: "")
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun postReport(position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this.mainActivity!!, R.style.DialogStyle)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_post_repost)
        bottomSheetDialog.setCancelable(true)
        val btnSendReport = bottomSheetDialog.findViewById<Button>(R.id.btnSendReport)
        val edtContent = bottomSheetDialog.findViewById<EditText>(R.id.edtContent)

        showKeyboard(edtContent!!)

        btnSendReport!!.setOnClickListener {
            if (edtContent.text.toString().isEmpty()){
                Toast.makeText(context, requireContext().getString(R.string.enter_content_please), Toast.LENGTH_SHORT).show()
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
        val dialog: Dialog? = this.context?.let { Dialog(it) }!!
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
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
            list.add(String.format("%s%s", nodeJs.api_ads, it.original))
        }
        val intent = Intent(mainActivity, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        adapterReview!!.stopVideo()
        //  adapterReview!!.stopVideo()
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_down,
            R.anim.translate_from_down
        )
    }

    override fun onBranchReviewDetail(id: String?) {
//        cacheManager.put(TechresEnum.ID_REVIEW.toString(), id.toString())
//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.action_mainFragment_to_detailReviewRestaurantFragment)
//        }
    }

    override fun onBranchDetail(id: Int?) {
//        val bundle = Bundle()
//        bundle.putInt(TechresEnum.BRANCH_ID.toString(), id!!.toInt())
//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.action_mainFragment_to_branchDetailFragment, bundle)
//        }
        val intent = Intent(requireActivity(), BranchDetailActivity::class.java)
        intent.putExtra(TechresEnum.BRANCH_ID.toString(), id ?: 0)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onMedia(url: ArrayList<Media>, position: Int, seekTo: Int) {
        val list = ArrayList<String>()
        url.forEach {
            list.add(String.format("%s%s", nodeJs.api_ads, it.original))
        }
        val intent = Intent(mainActivity, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        adapterReview!!.stopVideo()
        //   adapterReview!!.stopVideo()
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_down,
            R.anim.translate_from_down
        )
    }

    override fun onReactionDetail(position: Int, id: String?) {
        if (user.id == 0) {
            bottomSheetLogin()
        } else {
            val intent = Intent(requireActivity(), ReactionDetailManagerActivity::class.java)
            intent.putExtra(TechresEnum.ID_REVIEW.toString(), id)
            intent.putExtra(
                TechresEnum.TYPE_BACK.toString(),
                Integer.parseInt(TechresEnum.TYPE_REVIEW_POST.toString())
            )
            startActivity(intent)
            requireActivity().overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
    }

    override fun onPost() {
        if (user.id == 0) {
            bottomSheetLogin()
        } else {
            val intent = Intent(context, CreatePostNewsFeedActivity::class.java)
            intent.putExtra(
                TechresEnum.TYPE_REVIEW_POST.toString(),
                0
            )
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
    }

    override fun onReview() {
        val intent = Intent(context, CreateReviewNewsFeedActivity::class.java)
        intent.putExtra(TechresEnum.BRANCH_DETAIL.toString(), Gson().toJson(BranchDetail()))
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onMyAvatar() {
        if (user.id == 0) {
            bottomSheetLogin()
        } else {
            onClickProfile(user.id)
        }
    }

    override fun onAvatar(url: String, position: Int) {
        lookAtPhoto(url, position)
    }

    override fun onSaveBranch(position: Int, branchID: Int) {
        listIDBranch.clear()
        listIDBranch = Gson().fromJson(
            CacheManager.getInstance().get(TechresEnum.LIST_SAVE_BRANCH.toString()), object :
                TypeToken<ArrayList<Int>>() {}.type
        )
        saveBranch(branchID)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onDetailRatingReview(
        serviceRate: Float,
        foodRate: Float,
        priceRate: Float,
        spaceRate: Float,
        hygieneRate: Float
    ) {
        val dialog: Dialog? = this.context?.let { Dialog(it) }!!
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
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

        if (user.id == 0) {
            bottomSheetLogin()
        } else {
            val intent = Intent(requireActivity(), ReactionDetailManagerActivity::class.java)
            intent.putExtra(TechresEnum.ID_REVIEW.toString(), idReview)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
    }

    override fun onClickProfile(position: Int, userID: Int) {
        if (user.id == 0) {
            bottomSheetLogin()
        } else {
            onClickProfile(userID)
        }
    }

    override fun onSentComment(position: Int, id: String, content: String) {
        createReviewCommentRestaurant(position, id, content)
    }

    override fun onShowFullVideoYouTube(youtube: YouTube) {
        val intent = Intent(mainActivity, ShowFullScreenYouTubeActivity::class.java)
        intent.putExtra(TechresEnum.VIDEO_YOUTUBE.toString(), Gson().toJson(youtube))
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onLink(url: String) {
        val browserIntent =

            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    override fun onReactionComment(idPost: String, idComment: String) {
        postReactionComment(idPost, idComment)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun dialogQuestionDeletePost(position: Int) {
//        val dialog = Dialog(this.mainActivity!!)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setCancelable(true)
//        dialog.setContentView(R.layout.dialog_delete_post_new_feed)
        val dialog = this.context?.let { Dialog(it) }!!
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_post_new_feed)

        val btnDeleteNow = dialog.findViewById(R.id.btnDeleteNow) as Button
        val btnBack = dialog.findViewById(R.id.btnBack) as Button

        btnDeleteNow.setOnClickListener {
            removePost(data[position]._id!!)
            data.removeAt(position)
            adapterReview!!.notifyDataSetChanged()
            dialog.dismiss()
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun createReviewCommentRestaurant(position: Int, id: String, content: String) {

        val commentReviewBranchParams = CommentParams()
        commentReviewBranchParams.http_method = 1

        commentReviewBranchParams.request_url = "/api/branch-reviews/$id/comments"

        commentReviewBranchParams.params.content = content.trim()


        commentReviewBranchParams.let {
            ServiceFactory.createRetrofitService(

                TechResService::class.java
            )

                .createCommentReview(commentReviewBranchParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CreateCommentResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: CreateCommentResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            comment = response.data
                            if (data.size != 0) {
                                data[position].comment_count = data[position].comment_count?.plus(1)
                                when (data[position].comment_count) {
                                    1, 2, 3 -> {
                                        data[position].comments.add(comment)
                                        adapterReview!!.setDataSource(data)
                                    }
                                    else -> {
                                        data[position].comments.add(comment)
                                        data[position].comments.removeAt(0)
                                        adapterReview!!.setDataSource(data)
                                    }
                                }
                            }
                        } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }
    }

    private fun getListFriendSuggest() {
        val params = BaseParams()
        params.http_method = 0
        params.project_id = AppConfig.PROJECT_CHAT
        params.request_url = "/api/customer-membership-cards/suggestion"

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getFriendSuggest(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendSuggestResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: FriendSuggestResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        listFriendSuggest.clear()
                        listFriendSuggest = response.data
                        adapterReview!!.setListFriendSuggest(listFriendSuggest)
                        adapterReview!!.notifyItemChanged(2)
                    } else {
                        Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG).show()
                    }
                }
            })
    }

    // Get level of value
    private fun getLevelValue() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/alo-point-bonus-levels"
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        ServiceFactory.createRetrofitServiceNode(

            TechResService::class.java
        )

            .getLevelValue(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<LevelValueResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: LevelValueResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        cacheManager.put(
                            TechresEnum.LEVER_VALUE.toString(),
                            Gson().toJson(response.data)
                        )
                    }
                }
            })
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun showDialogUpdateAvatar() {
        imgCamera!!.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .maxSelectNum(1)
                .minSelectNum(0)
                .isMaxSelectEnabledMask(true)
                .isOpenClickSound(true)
                .forResult(REQUEST_PICKER_AVATAR)
        }

        imgCloseUpdate!!.setOnClickListener {
            dialogUpdateAvatar!!.dismiss()
        }

        btnUpdate!!.setOnClickListener {
            getLinkAvatar(file.name)
            uploadPhoto(file, 1)
            dialogUpdateAvatar!!.dismiss()
        }

        dialogUpdateAvatar!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogUpdateAvatar!!.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val selectList = PictureSelector.obtainMultipleResult(data)
            file = File(selectList[0].realPath)
            if (requestCode == REQUEST_PICKER_AVATAR) {
                Glide.with(this)
                    .load(
                        file.path
                    )
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .apply(
                        RequestOptions().placeholder(R.drawable.ic_user_placeholder)
                            .error(R.drawable.ic_user_placeholder)
                    )
                    .into(imgAvatarUpdate!!)

                if (file.name != "") {
                    btnUpdate!!.visibility = View.VISIBLE
                } else {
                    btnUpdate!!.visibility = View.GONE
                }
            }
        }
    }

    private fun getLinkAvatar(nameFile: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url =
            "/api-upload/get-link-file-by-user-customer?type=1&name_file=$nameFile&width=0&height=0"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getLinkImageReviewRestaurent(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ImageResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: ImageResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        urlAvatar = response.data!!.link_original
                        onCreateMedia(response.data!!.link_original)
                        updateAvatar(urlAvatar)

                    } else {
                        Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG).show()
                    }
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun uploadPhoto(photo: File, type: Int) {
        mainActivity!!.setLoading(true)
        //============ Upload image to server via service ===============
        val serverUrlString: String =
            String.format(
                "%s/api-upload/upload-file-by-user-customer/%s/%s",
                nodeJs.api_ads,
                type,
                photo.name
            )
        val paramNameString = resources.getString(R.string.send_file)
        val uploadID = UUID.randomUUID().toString()
        try {
            MultipartUploadRequest(mainActivity, uploadID, serverUrlString)
                .addFileToUpload(photo.path, paramNameString)
                .addHeader(
                    resources.getString(R.string.Authorization),
                    user.nodeAccessToken
                )
                .setCustomUserAgent(userAgent)
                .setMaxRetries(3)
                .startUpload()
            // these are the different exceptions that may be thrown
        } catch (exc: FileNotFoundException) {
            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        } catch (exc: IllegalArgumentException) {
            Toast.makeText(
                context,
                "Missing some arguments. " + exc.message,
                Toast.LENGTH_LONG
            ).show()
        } catch (exc: MalformedURLException) {
            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun onCreateMedia(urlMedia: String) {
        val createCustomerMediaParams = CreateCustomerMediaParams()
        createCustomerMediaParams.http_method = AppConfig.POST
        createCustomerMediaParams.request_url = "/api/customer-media-album-contents/create"

        createCustomerMediaParams.params.customer_media_album_id = -1
        createCustomerMediaParams.params.media_type = 1
        createCustomerMediaParams.params.url = urlMedia

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .createCustomerMediaRequest(createCustomerMediaParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                        }
                        else -> {
                            Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                }
            })
    }

    private fun updateAvatar(urlImage: String) {
        val params = UpdateAvatarParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customers/${user.id}/avatar"
        params.params.avatar = urlImage
        params.params.chat_token = CurrentUser.getAccessTokenNodeJs(mainActivity!!.baseContext)

        params.let {
            ServiceFactory.createRetrofitService(

                TechResService::class.java
            )

                .updateAvatar(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<UpdateProfileResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: UpdateProfileResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            user.avatar_three_image.medium = urlAvatar
                            user.avatar_three_image.thumb = urlAvatar
                            user.avatar_three_image.original = urlAvatar
                            user.avatar = urlAvatar
                            CurrentUser.saveUserInfo(activity!!, user)
                            Toast.makeText(
                                mainActivity,
                                mainActivity!!.baseContext.getString(R.string.update_avatar_success),
                                Toast.LENGTH_LONG
                            ).show()
                        } else Toast.makeText(
                            mainActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }

    private fun postReactionComment(idPost: String, idComment: String) {
        val postReactionCommentParams = PostReactionCommentParams()
        postReactionCommentParams.http_method = 1
        postReactionCommentParams.project_id = AppConfig.PROJECT_CHAT
        postReactionCommentParams.request_url = "/api/branch-reviews-comment-reactions"
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

    private fun getProfile() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/customers/${user.id}"
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
                        user.is_allow_advert = response.data.is_allow_advert
                        CurrentUser.saveUserInfo(mainActivity!!.baseContext, user)
                    }
                }
            })

    }

    private fun onClickProfile(idProfile: Int) {
        val intent = Intent(mainActivity, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), idProfile)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    private fun logoutToken() {
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            TechResApplication.applicationContext().getContactDao()?.deleteAllContact()
        }
        try {
            internalStorage.writeObject(
                context!!,
                TechresEnum.STORAGE_USER_PHONE.toString(),
                CurrentUser.getCurrentUser(mainActivity!!.baseContext).phone
            )
            internalStorage.writeObject(
                context!!,
                TechresEnum.STORAGE_USER_PASSWORD.toString(),
                ""
            )
            internalStorage.writeObject(
                context!!,
                TechresEnum.STORAGE_USER_LOGIN_TYPE.toString(),
                1
            )
        } catch (e: Exception) {
        }
        setDataLogout()
    }


    private fun setDataLogout() {
        CurrentUser.saveUserInfo(requireActivity(), User())
        cacheManager.clear()
        disconnectNodeJs()
        saveRestaurantInfo(RestaurantCard())
        if (user.id == 0) {
            cacheManager.put(
                TechresEnum.KEY_LOGOUT.toString(), "0"
            )
        } else {
            cacheManager.put(
                TechresEnum.KEY_LOGOUT.toString(), "1"
            )
        }
        getConfigNodeJs()
        val intent = Intent(mainActivity, LoginActivity::class.java)
        startActivity(intent)
        mainActivity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        mainActivity?.finish()
    }

    private fun disconnectNodeJs() {
        val user = CurrentUser.getCurrentUser(requireActivity())
        val disconnectNodeRequest = DisconnectNodeRequest()
//        disconnectNodeRequest.avatar = user.avatar_three_image.original
//        disconnectNodeRequest.full_name = user.name
        disconnectNodeRequest.member_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(disconnectNodeRequest))
            mSocket?.emit(TechResEnumChat.CLIENT_DISCONNECTION_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun getConfigNodeJs() {
        val configsNodeJsParams = ConfigsNodeJsParams()
        configsNodeJsParams.http_method = AppConfig.GET
        configsNodeJsParams.project_id = AppConfig.PROJECT_OAUTH_NODE
        configsNodeJsParams.request_url = "/api/oauth-configs-nodejs/get-configs"
        configsNodeJsParams.params.secret_key = TechresEnum.SECRET_KEY.toString()
        configsNodeJsParams.params.type =
            mainActivity!!.baseContext.getString(R.string.type_chat_demo)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getConfigsNodeJs(

                configsNodeJsParams
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ConfigNodeJsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ConfigNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        context?.let {
                            CurrentConfigNodeJs.saveConfigNodeJs(
                                it,
                                response.data
                            )
                        }
                    }
                }
            })
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)

    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onClickItemFriend(position: Int) {
        onClickProfile(listFriendSuggest[position].user_id!!)
    }

    override fun onAddFriend(position: Int) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = listFriendSuggest[position].user_id!!
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket!!.emit("request-to-contact", jsonObject)
            WriteLog.d("request-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val addFriendParams = FriendParams()
        addFriendParams.http_method = 1
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT
        addFriendParams.params.contact_to_user_id = listFriendSuggest[position].user_id!!
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
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {}
                })
        }
    }

    override fun onSeeMore() {
        val intent = Intent(context, FriendSuggestActivity::class.java)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right, +
            R.anim.translate_to_right
        )
    }

    override fun onClickAvatar(url: String, position: Int) {
        lookAtPhoto(url, position)
    }

    override fun onBackPress(): Boolean {
        val position = binding.recyclerViewNewsFeed.getCurrentPosition()
        return position != 0
    }
}

