package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import kohii.v1.exoplayer.Kohii
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.branch.BranchDetailAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.data.model.eventbus.EventBusSaveBranch
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.DetailBranchResponse
import vn.techres.line.data.model.response.ReviewBranchResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.Media
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityBranchDetailBinding
import vn.techres.line.fragment.branch.DetailMoreBranchFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.fresco.view.ImageViewer
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.branch.BranchDetailHandler
import vn.techres.line.interfaces.newsfeed.NewsFeedHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*
import kotlin.math.ceil

class BranchDetailActivity : BaseBindingActivity<ActivityBranchDetailBinding>(),
    BranchDetailHandler, NewsFeedHandler {
    private var adapter: BranchDetailAdapter? = null

    private var branchDetail: BranchDetail? = null
    private var dataTimeline = ArrayList<PostReview>()
    private var data = ArrayList<PostReview>()
    private var listIDBranch = ArrayList<Int>()
    private var user = User()
    private var configNodeJs = ConfigNodeJs()
    private var configJava = ConfigJava()
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var layoutManager: LinearLayoutManager? = null

    private var branchID = 0
    private var page = 1
    private var total = 0
    private var limit = 50
    private var positionPost = 0
    private var isLoading = true
    private var loadDataBranchDetail = false

    override val bindingInflater: (LayoutInflater) -> ActivityBranchDetailBinding
        get() = ActivityBranchDetailBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configJava = CurrentConfigJava.getConfigJava(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        mSocket = application.getSocketInstance(this)
        mSocket?.connect()
        intent?.let {
            branchID = it.getIntExtra(TechresEnum.BRANCH_ID.toString(), 0)
        }

        val kohii = Kohii[this]
        val manager = kohii.register(this).addBucket(binding.recyclerView)

        val displayMetrics = DisplayMetrics()
        val windowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        user = CurrentUser.getCurrentUser(this)
        adapter = BranchDetailAdapter(this, kohii, manager, lifecycle, width, height)
        adapter?.setBranchDetailHandler(this)
        adapter?.setNewsFeedHandler(this)
        layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.recyclerView.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false

        setListener()
    }

    private fun setData() {
        if (loadDataBranchDetail) {
            adapter?.setDataBranchDetail(branchDetail!!)
            adapter?.setDataSource(data)
        } else {
            loadDataBranchDetail = true
            getDetailBranch(branchID)
            getListBranchReview(page, branchID)
        }
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
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
                            adapter?.notifyItemInserted(data.size)
                            Handler(Looper.getMainLooper()).postDelayed({
                                getListBranchReview(page, branchID)
                            }, 1000)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            page = 1
            dataTimeline.clear()
            data.clear()
            loadDataBranchDetail = true
            getDetailBranch(branchID)
            getListBranchReview(page, branchID)
            adapter?.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun getDetailBranch(idBranch: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/branches/$idBranch"

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getDetailBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DetailBranchResponse> {
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }


                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }
                override fun onNext(response: DetailBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        branchDetail = response.data
                        branchDetail?.let {
                            adapter?.setDataBranchDetail(it)
                        }

                    } else Toast.makeText(
                        this@BranchDetailActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })

    }


    private fun getListBranchReview(page: Int, branchId: Int) {
        val listReviewBranchParam = ListReviewBranchParam()
        listReviewBranchParam.http_method = 0
        listReviewBranchParam.project_id = AppConfig.PROJECT_CHAT
        listReviewBranchParam.request_url = "/api/branch-reviews"
        listReviewBranchParam.params.restaurant_id =
            restaurant().restaurant_id
        listReviewBranchParam.params.branch_id = branchId
        listReviewBranchParam.params.branch_review_status = 1
        listReviewBranchParam.params.customer_id = -1
        listReviewBranchParam.params.types = "1"
        listReviewBranchParam.params.limit = limit
        listReviewBranchParam.params.page = page


        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListReviewBranch(listReviewBranchParam)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ReviewBranchResponse> {
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }
                override fun onNext(response: ReviewBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataTimeline = response.data.list
                        total = response.data.total_record!!
                        limit = response.data.limit!!

                        if (data.size == 0) {
                            data.add(PostReview())
                            data.add(PostReview())
                            data.add(PostReview())
                            data.addAll(data.size, dataTimeline)
                        } else {
                            isLoading = true
                            data.removeIf { item -> item.loading == 1 }
                            data.addAll(data.size, dataTimeline)
                        }
                        adapter?.setDataSource(data)
                    } else Toast.makeText(
                        this@BranchDetailActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMyComment(postList: ArrayList<Comment>) {
        if (postList.size != 0) {
//            val totalCommentPosition = postList[0].total_commnent
            postList.removeAt(0)
            when (postList.size) {
                0 -> {
                    data[positionPost].comments.clear()
                }
                1 -> {
                    data[positionPost].comments.clear()
                    data[positionPost].comments.add(postList[0])
                }
                2 -> {
                    data[positionPost].comments.clear()
                    data[positionPost].comments.add(postList[0])
                    data[positionPost].comments.add(postList[1])
                }
                else -> {
                    data[positionPost].comments.clear()
                    data[positionPost].comments.add(postList[0])
                    data[positionPost].comments.add(postList[1])
                    data[positionPost].comments.add(postList[2])
                }
            }
//            data[positionPost].comment_count = totalCommentPosition
            adapter?.setDataSource(data)
        }
    }

    override fun onResume() {
        super.onResume()
        setData()
    }

    private fun saveBranch(branchId: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = String.format(
            "%s%s%s%s",
            "/api/customers/",
            user.id,
            "/save-branch?branch_id=",
            branchId
        )
        ServiceFactory.createRetrofitService(

            TechResService::class.java
        )

            .saveBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }

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
                            adapter!!.notifyDataSetChanged()
                            val isClick = EventBusSaveBranch(true)
                            EventBus.getDefault().post(isClick)
                        }
                        else -> {
                            Toast.makeText(
                                this@BranchDetailActivity,
                                response.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
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

    override fun clickPostReview() {
        val branchDetailBundle = Gson().toJson(branchDetail)
        val intent = Intent(this, CreateReviewNewsFeedActivity::class.java)
        intent.putExtra(TechresEnum.BRANCH_DETAIL.toString(), branchDetailBundle)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun clickCheckIn() {
//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.createPostNewFeedFragment)
//        }
    }

    override fun seeMore(branchDetail: BranchDetail?) {
        val bundle = Bundle()
        val json = Gson().toJson(branchDetail)
        bundle.putString(TechresEnum.BRANCH_DETAIL.toString(), json)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            setCustomAnimations(
                R.anim.translate_from_right,
                R.anim.translate_to_left,
                R.anim.translate_from_left,
                R.anim.translate_to_right
            )
            add<DetailMoreBranchFragment>(R.id.frameBranchDetail, args = bundle)
            addToBackStack(DetailMoreBranchFragment().tag)
        }
    }

    override fun clickSaveBranch(list: ArrayList<Int>) {
        listIDBranch = list
        saveBranch(branchID)
    }

    override fun onMyAvatar() {
        //Avatar
    }

    override fun onAvatar(url: String, position: Int) {
        lookAtPhoto(url, position)
    }

    override fun onPost() {
        //Post
    }

    override fun onReview() {
        //Review
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
    }

    override fun onMedia(url: ArrayList<Media>, position: Int, seekTo: Int) {
        //Media
    }

    override fun onBranchDetail(id: Int?) {
//        val bundle = Bundle()
//        bundle.putInt(TechresEnum.BRANCH_ID.toString(), id ?: 0)
//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.branchDetailFragment, bundle)
//        }
        val intent = Intent(this, BranchDetailActivity::class.java)
        intent.putExtra(TechresEnum.BRANCH_ID.toString(), id ?: 0)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onBranchReviewDetail(id: String?) {
        //BranchReviewDetail
    }

    override fun onReaction(id: String, idReaction: Int) {
        postReaction(id, idReaction)
    }

    override fun onReactionDetail(position: Int, id: String?) {
        val bundle = Bundle()
        bundle.putString(TechresEnum.ID_REVIEW.toString(), id!!.toString())
//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.detailReactionManagerFragment, bundle)
//        }
        val intent = Intent(this, ReactionDetailManagerActivity::class.java)
        intent.putExtra(TechresEnum.ID_REVIEW.toString(), id)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
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
        val dialog: Dialog?
        dialog = Dialog(this)
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
        val bundle = Bundle()
        bundle.putString(TechresEnum.ID_REVIEW.toString(), idReview!!.toString())
//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.detailReactionManagerFragment, bundle)
//        }
    }

    override fun onClickProfile(position: Int, userID: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), userID)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onSentComment(position: Int, idComment: String, content: String) {
        //SentComment
    }

    override fun onShowFullVideoYouTube(youtube: YouTube) {
        //ShowFullVideoYouTube
    }

    override fun onLink(url: String) {
        //Link
    }

    override fun onReactionComment(idPost: String, idComment: String) {
        postReactionComment(idPost, idComment)
    }

    private fun lookAtPhoto(url: String, position: Int) {
        val listPhoto = ArrayList<vn.techres.line.helper.fresco.model.MediaSource>()
        listPhoto.add(
            vn.techres.line.helper.fresco.model.MediaSource(
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    url
                ), 0, 0, 0
            )

        )
        this.let {
            ImageViewer.Builder(it, listPhoto)
                .setStartPosition(position)
                .setBackgroundColorRes(R.color.overlay_dark_70)
                .show()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun dialogQuestionDeletePost(position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_post_new_feed)

        val btnDeleteNow = dialog.findViewById(R.id.btnDeleteNow) as Button
        val btnBack = dialog.findViewById(R.id.btnBack) as Button

        btnDeleteNow.setOnClickListener {
            removePost(data[position]._id ?: "")
            data.removeAt(position)
            adapter?.notifyDataSetChanged()
            dialog.dismiss()
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun postReaction(id: String, idReaction: Int) {

        val postReactionReviewParams = PostReactionReviewParams()
        postReactionReviewParams.http_method = 1
        postReactionReviewParams.project_id = AppConfig.PROJECT_CHAT
        postReactionReviewParams.request_url = "/api/branch-reviews-reactions"

        postReactionReviewParams.params.reaction_id = idReaction
        postReactionReviewParams.params.branch_review_id = id
        postReactionReviewParams.params.java_access_token = user.access_token


        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .postReactionReview(postReactionReviewParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }
                override fun onNext(response: BaseResponse) {
                    //Next
                }
            })
    }

    private fun removePost(id: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 1
        baseRequest.request_url = "/api/branch-reviews/$id/remove"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .removePost(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }
                override fun onNext(response: BaseResponse) {
                    //Next
                }
            })
    }

    private fun postReactionComment(idPost: String, idComment: String) {
        val postReactionCommentParams = PostReactionCommentParams()
        postReactionCommentParams.http_method = 1
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
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }
                override fun onNext(response: BaseResponse) {
                    //Next
                }
            })
    }
}