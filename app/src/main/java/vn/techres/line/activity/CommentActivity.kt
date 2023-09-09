package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.aghajari.emojiview.listener.PopupListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import kohii.v1.exoplayer.Kohii
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.comment.CommentAdapter
import vn.techres.line.adapter.comment.DetailReactionCommentAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.branch.SaveBranch
import vn.techres.line.data.model.branch.response.SaveBranchResponse
import vn.techres.line.data.model.chat.Sticker
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.eventbus.CommentEventBusData
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.newfeed.Comment
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.reaction.InteractiveUser
import vn.techres.line.data.model.reaction.InteractiveUserResponse
import vn.techres.line.data.model.response.*
import vn.techres.line.data.model.utils.*
import vn.techres.line.databinding.ActivityCommentBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.fresco.model.MediaSource
import vn.techres.line.helper.fresco.view.ImageViewer
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.techresenum.TechresEnumFriend
import vn.techres.line.interfaces.KeyBoardComment
import vn.techres.line.interfaces.comment.CommentHandler
import vn.techres.line.interfaces.comment.ReactionCommentHandler
import vn.techres.line.interfaces.comment.ReplyCommentHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import kotlin.math.ceil

class CommentActivity : BaseBindingActivity<ActivityCommentBinding>(), CommentHandler,
    ReplyCommentHandler, ReactionCommentHandler,
    View.OnClickListener, PopupListener, KeyBoardComment {
    private var adapter: CommentAdapter? = null
    private var dataComment = ArrayList<Comment>()
    private var commentList = ArrayList<Comment>()
    private var comment = Comment()

    private var dataDetailPost = PostReview()
    private var replyComment = Comment()
    private var imgContainerVisible = false
    private var listMediaComment = ArrayList<MediaPost>()
    private var saveBranchList = ArrayList<SaveBranch>()
    private var listIDBranch = ArrayList<Int>()
    private var layoutManager: LinearLayoutManager? = null

    private var idPost = ""

    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var idCommentReply = ""
    private var positionComment = 0
    private var positionReply = 0
    private var nameTagReply = ""
    private var chooseStyleComment = ""
    private var detailReactionCommentAdapter: DetailReactionCommentAdapter? = null
    private var dataDetailReaction = ArrayList<InteractiveUser>()

    private var page = 1
    private var total = 0
    private var limit = 50

    //keyboard
    private var isShowingKeyBoard = false
    private var isKeyBoard = false
    private var urlSticker = ""
    private var checkShowKeyboard = false


    override val bindingInflater: (LayoutInflater) -> ActivityCommentBinding
        get() = ActivityCommentBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        configJava = CurrentConfigJava.getConfigJava(this)

        mSocket = application.getSocketInstance(this)
        mSocket!!.connect()
        dataComment.clear()

        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        layoutManager = LinearLayoutManager(this)

        intent?.let {
            dataDetailPost = Gson().fromJson(
                it.getStringExtra(TechresEnum.DETAIL_POST_COMMENT.toString()),
                object :
                    TypeToken<PostReview>() {}.type
            )
            chooseStyleComment = it.getStringExtra(TechresEnum.CHECK_COMMENT_CHOOSE.toString())?:""
            idPost = if (dataDetailPost._id.isNullOrEmpty()){
                it.getStringExtra(TechresEnum.ID_POST.toString())?:""
            }else {
                dataDetailPost._id.toString()
            }
            positionReply = it.getIntExtra(TechresEnum.POSITION_REPLY.toString(), 0)
        }



        val pager = UIView.loadView(this, binding.tagingCommentEdt)
        binding.layoutKeyboard.initPopupView(
            pager,
            UIView.loadUtilities(this, binding.tagingCommentEdt)
        )
        binding.layoutKeyboard.setPopupListener(this)
        UIView.setUtilitiesChatHandler(this)
        val kohii = Kohii[this]
        val manager = kohii.register(this).addBucket(binding.rclComment)

        val displayMetrics = DisplayMetrics()
        val windowManager: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        adapter = CommentAdapter(this, kohii, manager, width, height)
        binding.rclComment.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter?.setEventComment(this)
        adapter?.setEventReplyComment(this)
        binding.rclComment.adapter = adapter
        binding.rclComment.layoutManager = layoutManager

        binding.ownerAvatarImg.load(getLinkDataNode(user.avatar_three_image.thumb ?: "")) {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
        }
        binding.imgBack.setOnClickListener(this)
        binding.imgSent.setOnClickListener(this)
        binding.mediaIv.setOnClickListener(this)
        binding.itemPreview.deleteImg.setOnClickListener(this)
        binding.txtCancleReply.setOnClickListener(this)
        binding.imgSticker.setOnClickListener(this)
        binding.tagingCommentEdt.setOnClickListener(this)
        binding.tagingCommentEdt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {

            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {

                if (s.isNotEmpty() || listMediaComment.size != 0) {
                    binding.imgSent.visibility = View.VISIBLE
//                    mediaIv!!.visibility = GONE
                } else {
                    binding.imgSent.visibility = View.GONE
//                    mediaIv!!.visibility = VISIBLE

                }
            }
        })

        binding.rclComment.addOnScrollListener(object :
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
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y > 0) {
                        if (page <= ceil((total / limit).toString().toDouble())) {
                            page++
                            getListCommentReviewRestaurant(page)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            dataComment.clear()
            page = 1
            getListCommentReviewRestaurant(page)
            binding.swipeRefresh.isRefreshing = false
        }

        setData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPostEditProfile(dataEdit: PostReview) {
        if (dataEdit._id!! != "") {
            when (dataEdit.status_reload) {
                "edit" -> {
                    dataDetailPost = dataEdit
                    adapter?.setDataDetailPost(dataDetailPost)
                    adapter?.notifyItemChanged(0)
                }
            }

        }
    }

    override fun onLongClickComment(position: Int, data: Comment, type: String) {
        closeKeyboard(binding.tagingCommentEdt)
        this.positionComment = position
        dialogLongClickComment(data, type)

    }

    override fun onLongClickReplyComment(
        positionComment: Int,
        positionReply: Int,
        data: Comment,
        type: String
    ) {
        closeKeyboard(binding.tagingCommentEdt)
        this.positionComment = positionComment
        this.positionReply = positionReply
        dialogLongClickComment(data, type)
    }

    override fun showImage(position: Int, url: String) {
        lookAtPhoto(url, position)
    }

    override fun showImageComment(photo: String) {
    }

    override fun replyComment(
        positionComment: Int,
        positionReply: Int,
        data: Comment,
        type: String,
        idCommentParent: String
    ) {
        this.positionComment = positionComment
        this.positionReply = positionReply
        showKeyboard(binding.tagingCommentEdt)
        nameTagReply = data.customer_name.toString()
        binding.txtNameReply.text = nameTagReply
        binding.lnReply.visibility = View.VISIBLE
        idCommentReply = if (idCommentParent == "") {
            data.comment_id!!
        } else {
            idCommentParent
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgSent -> {
                binding.layoutKeyboard.onBackPressed()
                binding.imgSent.isEnabled = false
                if (listMediaComment.size != 0) {
                    //Comment media
                    getLink(
                        listMediaComment[0].fileName,
                        listMediaComment[0].width,
                        listMediaComment[0].height
                    )
                    uploadPhoto(listMediaComment[0].original, listMediaComment[0].fileName)
                } else {
                    //Comment text
                    if (idCommentReply != "") {
                        createReplyComment(idPost, idCommentReply, nameTagReply)
                    } else {
                        createComment()
                    }
                }
            }

            R.id.imgBack -> {
                onBackPressed()
            }

            R.id.mediaIv -> {
                closeKeyboard(binding.tagingCommentEdt)
                binding.layoutKeyboard.onBackPressed()
                PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .imageEngine(GlideEngine.createGlideEngine())
                    .isCamera(true)
                    .maxSelectNum(1)
                    .minSelectNum(0)
                    .isMaxSelectEnabledMask(true)
                    .isOpenClickSound(true)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
            R.id.deleteImg -> {
                urlSticker = ""
                listMediaComment.clear()
                hideImageContainer()
            }
            R.id.txtCancleReply -> {
                cancelReply()
            }
            R.id.imgSticker -> {
                if (isShowingKeyBoard) {
                    binding.layoutKeyboard.openKeyboard()
                } else {
                    binding.layoutKeyboard.setView(1)
                    binding.layoutKeyboard.show()
                    binding.tagingCommentEdt
                }
            }
            R.id.tagingCommentEdt -> {
                binding.tagingCommentEdt.requestFocus()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PictureConfig.CHOOSE_REQUEST) {
            listMediaComment.clear()
            val selectList = PictureSelector.obtainMultipleResult(data)
            val fileMedia = MediaPost()
            try {
                fileMedia.fileName = selectList[0].fileName
                fileMedia.original = selectList[0].path
                fileMedia.type = 0
                fileMedia.width =
                    Utils.getBitmapRotationImage(File(selectList[0].realPath))?.width ?: 0
                fileMedia.height =
                    Utils.getBitmapRotationImage(File(selectList[0].realPath))?.height ?: 0
                listMediaComment.add(fileMedia)
                binding.itemPreview.previewImg.setImageURI(Uri.parse(listMediaComment[0].original))
                urlSticker = ""
                showImageContainer()
            } catch (ex: Exception) {
            }
        }
    }

    override fun clickAddFriend(interactiveUser: InteractiveUser) {
        if (interactiveUser.status == 4) {
            addFriend(interactiveUser)
        } else {
            approveToContact(interactiveUser)
        }
    }

    override fun clickItemMyFriend(interactiveUser: InteractiveUser) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), interactiveUser.user_id!!)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onChat(interactiveUser: InteractiveUser) {
        val memberList = java.util.ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(interactiveUser.user_id ?: 0)
        createGroupPersonal(memberList, interactiveUser.user_id!!)
    }

    override fun clickProfile(position: Int, userId: Int) {
        closeKeyboard(binding.tagingCommentEdt)
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), userId)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onAvatar(url: String, position: Int) {
        val listPhoto = java.util.ArrayList<MediaSource>()
        listPhoto.add(
            MediaSource(
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    url
                ), 0, 0, 0
            )

        )
        let {
            ImageViewer.Builder(it, listPhoto)
                .setStartPosition(position)
                .setBackgroundColorRes(R.color.overlay_dark_70)
                .show()
        }
    }

    override fun onAvatar(url: String) {
        lookAtPhoto(url)
    }

    override fun onComment() {
        binding.lnContainerSendComment.visibility = View.VISIBLE
    }

    override fun onShare() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val shareBody =
            "Application Link : https://play.google.com/store/apps/details?id=${this.packageName}"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App link")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
        startActivity(Intent.createChooser(sharingIntent, "Share App Link Via :"))
    }

    override fun onButtonMore() {
        Utils.hideKeyboard(binding.tagingCommentEdt)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_more_new_feed)
        bottomSheetDialog.setCancelable(true)
        val btnDeletePostsNewFeed = bottomSheetDialog.findViewById<Button>(R.id.btnDeletePostsNewFeed)
        val btnEditPostsNewFeed = bottomSheetDialog.findViewById<Button>(R.id.btnEditPostsNewFeed)
        val btnSendReport = bottomSheetDialog.findViewById<Button>(R.id.btnPostsRepost)

        if (dataDetailPost.customer.member_id != user.id){
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
            dialogQuestionDeletePost()
        }

        btnSendReport.setOnClickListener {
            bottomSheetDialog.dismiss()
            postReport()
        }

        btnEditPostsNewFeed.setOnClickListener {
            val intent = Intent(this, EditMyPostActivity::class.java)
            intent.putExtra(TechresEnum.ID_REVIEW.toString(), idPost)
            startActivity(intent)
            this.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun postReport() {
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
                createReport(idPost, dataDetailPost.customer.full_name!! , edtContent.text.toString())
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
        closeKeyboard(binding.tagingCommentEdt)
        binding.layoutKeyboard.onBackPressed()
        val list = ArrayList<String>()
        url.forEach {
            list.add(String.format("%s%s", configNodeJs.api_ads, it.original))
        }
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onBranchDetail(id: Int?) {
        val intent = Intent(this, BranchDetailActivity::class.java)
        intent.putExtra(TechresEnum.BRANCH_ID.toString(), id ?: 0)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onBranchReviewDetail(id: Int?) {
    }

    override fun onReaction(id: String, idReaction: Int) {
        postReaction(id, idReaction)
    }

    override fun onReactionDetail() {
        closeKeyboard(binding.tagingCommentEdt)
        binding.layoutKeyboard.onBackPressed()
        val intent = Intent(this, ReactionDetailManagerActivity::class.java)
        intent.putExtra(TechresEnum.ID_REVIEW.toString(), idPost)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onSaveBranch(branchID: Int) {
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

    private fun getDetailBranch(_id: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/branch-reviews/$_id"
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .detailBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DetailCommentResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: DetailCommentResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataDetailPost = response.data
                        adapter?.setDataDetailPost(dataDetailPost)
                    }
                }

            })
    }


    private fun postReactionComment(idComment: String) {
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

    private fun getDetailReactionComment(list: ArrayList<Int>) {

        val reactionCommentParams = ReactionCommentParams()
        reactionCommentParams.http_method = AppConfig.POST
        reactionCommentParams.project_id = AppConfig.PROJECT_CHAT
        reactionCommentParams.request_url = "/api/contact-froms/list-user-comment"
        reactionCommentParams.params.list_user_id = list
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getListReactionComment(reactionCommentParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<InteractiveUserResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: InteractiveUserResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataDetailReaction = response.data
                        detailReactionCommentAdapter!!.setDataSource(dataDetailReaction)
                    } else {
                        Toast.makeText(
                            this@CommentActivity,
                            resources.getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    override fun onClickProfile(userID: Int) {
        closeKeyboard(binding.tagingCommentEdt)
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), userID)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onReactionComment(idComment: String) {
        postReactionComment(idComment)
    }

    override fun onDetailReactionComment(data: ArrayList<Int>) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_detail_reaction_comment)
        bottomSheetDialog.setCancelable(true)
        val recyclerView = bottomSheetDialog.findViewById<RecyclerView>(R.id.recyclerView)

        detailReactionCommentAdapter = DetailReactionCommentAdapter(this)
        detailReactionCommentAdapter!!.setReactionCommentHandler(this)
        val layoutManager = FlexboxLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = detailReactionCommentAdapter

        getDetailReactionComment(data)

        bottomSheetDialog.show()

    }

    override fun onShowFullVideoYouTube(youtube: YouTube) {
        val intent = Intent(this, ShowFullScreenYouTubeActivity::class.java)
        intent.putExtra(TechresEnum.VIDEO_YOUTUBE.toString(), Gson().toJson(youtube))
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onLink(url: String) {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onDismiss() {
        updateButton(false)
    }

    override fun onShow() {
        updateButton(true)
    }

    override fun onKeyboardOpened(height: Int) {
        isKeyBoard = true
        updateButton(false)
    }

    override fun onKeyboardClosed() {
        isKeyBoard = false
        updateButton(binding.layoutKeyboard.isShowing)
    }

    override fun onViewHeightChanged(height: Int) {
    }

    override fun onChooseSticker(sticker: Sticker) {
        listMediaComment.clear()
        urlSticker = sticker.link_original ?: ""
        WriteLog.d("zxc", urlSticker)
        Glide.with(this)
            .load(getLinkDataNode(sticker.link_original ?: ""))
            .into(binding.itemPreview.previewImg)

        showImageContainer()
    }

    override fun onImportantMessage() {

    }


    private fun dialogLongClickComment(data: Comment, type: String) {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_more_comment)
        bottomSheetDialog.setCancelable(true)
        val txtReply = bottomSheetDialog.findViewById<TextView>(R.id.txtReply)
        val txtCopy = bottomSheetDialog.findViewById<TextView>(R.id.txtCopy)
        val txtDelete = bottomSheetDialog.findViewById<TextView>(R.id.txtDelete)
        val txtEdit = bottomSheetDialog.findViewById<TextView>(R.id.txtEdit)
        val txtReport = bottomSheetDialog.findViewById<TextView>(R.id.txtReport)

        if (data.customer_id != user.id) {
            txtDelete!!.visibility = View.GONE
            txtEdit!!.visibility = View.GONE
        }

        txtReply!!.setOnClickListener {
            showKeyboard(binding.tagingCommentEdt)
            binding.txtNameReply.text = data.customer_name
            binding.lnReply.visibility = View.VISIBLE
            idCommentReply = data.comment_id ?: ""
            nameTagReply = data.customer_name ?: ""
            bottomSheetDialog.dismiss()
        }
        txtCopy!!.setOnClickListener {
            copyText(data.content ?: "")
            bottomSheetDialog.dismiss()
        }
        txtDelete!!.setOnClickListener {
            bottomSheetDialog.dismiss()
            deleteMyComment(data, type)
        }
        txtEdit!!.setOnClickListener {
            dialogEditComment(data, type)
            bottomSheetDialog.dismiss()
        }
        txtReport!!.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getLinkDataNode(url: String): String {
        return String.format(
            "%s%s",
            CurrentConfigNodeJs.getConfigNodeJs(this).api_ads,
            url
        )
    }

    private fun setData() {
        WriteLog.d("chooseStyleComment", chooseStyleComment)
        when (chooseStyleComment) {
            TechresEnum.TYPE_COMMENT_MEDIA.toString() -> {
                PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .imageEngine(GlideEngine.createGlideEngine())
                    .isCamera(true)
                    .maxSelectNum(1)
                    .minSelectNum(0)
                    .isMaxSelectEnabledMask(true)
                    .isOpenClickSound(true)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
                adapter!!.setDataDetailPost(dataDetailPost)
            }
            TechresEnum.TYPE_COMMENT_TEXT.toString() -> {
                if (!checkShowKeyboard) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        showKeyboard(binding.tagingCommentEdt)
                    }, 50)
                }
                checkShowKeyboard = true
                adapter!!.setDataDetailPost(dataDetailPost)
            }
            TechresEnum.TYPE_COMMENT_REPLY.toString() -> {
                positionComment = positionReply + 1
                when (positionReply) {
                    0 -> {
                        nameTagReply = dataDetailPost.comments[0].customer_name ?: ""
                        idCommentReply = dataDetailPost.comments[0].comment_id ?: ""
                    }
                    1 -> {
                        nameTagReply = dataDetailPost.comments[1].customer_name ?: ""
                        idCommentReply = dataDetailPost.comments[1].comment_id ?: ""
                    }
                    2 -> {
                        nameTagReply = dataDetailPost.comments[2].customer_name ?: ""
                        idCommentReply = dataDetailPost.comments[2].comment_id ?: ""
                    }
                }
                binding.txtNameReply.text = nameTagReply
                binding.lnReply.visibility = View.VISIBLE
                if (!checkShowKeyboard) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        showKeyboard(binding.tagingCommentEdt)
                    }, 50)
                }
                adapter!!.setDataDetailPost(dataDetailPost)
            }
            TechresEnum.TYPE_SEARCH_NEWS_FEED.toString() -> {
                binding.lnContainerSendComment.visibility = View.GONE
                adapter!!.setDataDetailPost(dataDetailPost)
            }
            TechresEnum.VALUE_COMMENT_NOTIFY.toString() -> {
                getDetailBranch(idPost)
            }

        }

        getListSaveBranch()

        if (dataComment.size == 0) {
            getListCommentReviewRestaurant(page)
        } else {
            adapter!!.setDataSource(dataComment)
        }
    }

    private fun getListSaveBranch() {

        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = String.format(
            "%s%s%s",
            "/api/customers/",
            CurrentUser.getCurrentUser(this).id,
            "/saved-branches"
        )
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
                        saveBranchList = response.data.list
                        listIDBranch.clear()
                        listIDBranch = saveBranchList.map { it.branch_id ?: 0 } as ArrayList<Int>
                        adapter!!.setListSaveBranchID(listIDBranch)
                    }
                }

            })
    }

    private fun getListCommentReviewRestaurant(page: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/branch-reviews-comments/$idPost?page=$page&limit=$limit"

        ServiceFactory.run {
            createRetrofitServiceNode(
                TechResService::class.java
            )
                .getCommentReviewBranch(
                    baseRequest
                )

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CommentReviewBranchResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }


                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: CommentReviewBranchResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            commentList = response.data!!.list
                            total = response.data!!.total_record ?: 0
                            limit = response.data!!.limit ?: 0
                            if (dataComment.size == 0) {
                                dataComment.add(Comment())
                                dataComment.addAll(dataComment.size, commentList)
                            } else {
                                dataComment.addAll(dataComment.size, commentList)
                            }
                            adapter!!.setDataSource(dataComment)
                        } else Toast.makeText(
                            this@CommentActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }

    private fun deleteMyComment(data: Comment, type: String) {
        val deleteCommentReplyParams = DeleteCommentReplyParams()
        deleteCommentReplyParams.http_method = 1
        deleteCommentReplyParams.project_id = AppConfig.PROJECT_CHAT
        deleteCommentReplyParams.request_url = "/api/branch-reviews-comments/remove"

        deleteCommentReplyParams.params.branch_review_id = idPost
        deleteCommentReplyParams.params.comment_id = data.comment_id

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .deleteCommentReviewBranch(
                deleteCommentReplyParams
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataDetailPost.comment_count = dataDetailPost.comment_count!! - 1
                        if (type == TechresEnum.TYPE_COMMENT.toString()) {
                            dataComment.removeAt(positionComment)

                        } else {
                            dataComment[positionComment].reply_comment.removeAt(positionReply)
                            dataComment[positionComment].not_answered = false
                        }
                        adapter!!.notifyDataSetChanged()
                    } else Toast.makeText(this@CommentActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                }
            })

    }

    private fun dialogEditComment(data: Comment, type: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_edit_comment)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val body = dialog.findViewById(R.id.edtContent) as EditText
        body.setText(data.content)
        val yesBtn = dialog.findViewById(R.id.btnUpdate) as TextView
        val noBtn = dialog.findViewById(R.id.btnCacel) as TextView

        val rlPhoto = dialog.findViewById(R.id.rlPhoto) as RelativeLayout
        val imgPhoto = dialog.findViewById(R.id.imgPhoto) as ImageView
        val imgDeletePhoto = dialog.findViewById(R.id.imgDeletePhoto) as ImageView

        if (data.image_urls.size != 0) {
            rlPhoto.visibility = View.VISIBLE
            Glide.with(this)
                .load(
                    String.format(
                        "%s%s",
                        configNodeJs.api_ads,
                        data.image_urls[0].original
                    )
                )
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .apply(
                    RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_placeholder)
                )
                .into(imgPhoto)
        } else {
            rlPhoto.visibility = View.GONE
        }

        imgPhoto.setOnClickListener {
            lookAtPhoto(data.image_urls[0].original ?: "", 0)
        }

        imgDeletePhoto.setOnClickListener {
            data.image_urls.clear()
            rlPhoto.visibility = View.GONE
        }

        yesBtn.setOnClickListener {
            editComment(data, body.text.toString(), type)
            dialog.dismiss()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun editComment(data: Comment, content: String, type: String) {
        val commentReviewBranchEditParams = CommentReviewBranchEditParams()
        commentReviewBranchEditParams.http_method = 1
        commentReviewBranchEditParams.project_id = AppConfig.PROJECT_CHAT
        commentReviewBranchEditParams.request_url = "/api/branch-reviews-comments/edit"

        commentReviewBranchEditParams.params.content = content.trim()
        commentReviewBranchEditParams.params.branch_review_id = idPost
        commentReviewBranchEditParams.params.comment_id = data.comment_id

//        commentReviewBranchEditParams.params.image_urls = listMediaComment
        commentReviewBranchEditParams.params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .editCommentBranch(commentReviewBranchEditParams)
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
                            if (type == TechresEnum.TYPE_COMMENT.toString()) {
                                dataComment[positionComment].content = content
                            } else {
                                dataComment[positionComment].reply_comment[positionReply].content =
                                    content
                                dataComment[positionComment].not_answered = false
                            }
                            adapter!!.notifyItemChanged(positionComment)

                        } else Toast.makeText(
                            this@CommentActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        )
                            .show()

                    }
                })
        }
    }

    private fun showImageContainer() {
        imgContainerVisible = true
        binding.lnPreviewimage.visibility = View.VISIBLE
        binding.imgSent.visibility = View.VISIBLE
    }

    private fun hideImageContainer() {
        imgContainerVisible = false
        binding.lnPreviewimage.visibility = View.GONE
        binding.imgSent.visibility = View.GONE
    }

    private fun getLink(nameFile: String, width: Int, height: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url =
            "/api-upload/get-link-file-by-user-customer?type=3&name_file=$nameFile&width=$width&height=$height"
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
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: ImageResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {

                        listMediaComment[0].type = response.data!!.type
                        listMediaComment[0].original = response.data!!.link_original
                        listMediaComment[0].width = response.data!!.width
                        listMediaComment[0].height = response.data!!.height
                        listMediaComment[0].ratio = response.data!!.ratio

                        if (idCommentReply != "") {
                            createReplyComment(idPost, idCommentReply, nameTagReply)
                        } else {
                            createComment()
                        }
                    } else {
                        Toast.makeText(this@CommentActivity, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            })
    }

    private fun uploadPhoto(photo: String, name: String) {
        val serverUrlString: String =
            String.format(
                "%s/api-upload/upload-file-by-user-customer/%s/%s",
                configNodeJs.api_ads,
                3,
                name
            )
        val paramNameString = "send_file"
        try {
            MultipartUploadRequest(this, serverUrlString)
                .setMethod("POST")
                .addFileToUpload(photo, paramNameString)
                .addHeader("Authorization", user.nodeAccessToken)
                .setMaxRetries(3)
                .setNotificationConfig { _: Context?, uploadId: String? ->
                    TechResApplication.applicationContext().getNotificationConfig(
                        this,
                        uploadId,
                        R.string.multipart_upload
                    )
                }
                .subscribe(this, this, object : RequestObserverDelegate {
                    override fun onSuccess(
                        context: Context,
                        uploadInfo: UploadInfo,
                        serverResponse: ServerResponse
                    ) {
                    }

                    override fun onProgress(
                        context: Context,
                        uploadInfo: UploadInfo
                    ) {
                    }

                    override fun onError(
                        context: Context,
                        uploadInfo: UploadInfo,
                        exception: Throwable
                    ) {
                    }

                    override fun onCompletedWhileNotObserving() {}
                    override fun onCompleted(
                        context: Context,
                        uploadInfo: UploadInfo
                    ) {
//                        onCreatePost(urlImageList, 1)
                    }
                })
        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun createComment() {
        val commentReviewBranchParams = CommentParams()
        commentReviewBranchParams.http_method = 1
        commentReviewBranchParams.request_url = "/api/branch-reviews-comments"
        commentReviewBranchParams.project_id = AppConfig.PROJECT_CHAT
        commentReviewBranchParams.params.branch_review_id = idPost
        commentReviewBranchParams.params.content = binding.tagingCommentEdt.text.toString().trim()
        commentReviewBranchParams.params.image_urls = listMediaComment
        commentReviewBranchParams.params.sticker = urlSticker
        binding.tagingCommentEdt.setText("")
        closeKeyboard(binding.tagingCommentEdt)
        binding.imgSent.isEnabled = true
        hideImageContainer()
//        mediaIv!!.visibility = VISIBLE
        ServiceFactory.createRetrofitServiceNode(
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
                        listMediaComment.clear()
                        comment = response.data
                        dataComment.add(1, comment)
                        dataDetailPost.comment_count = comment.total_comment
                        adapter!!.setDataSource(dataComment)
                        // Scroll to bottom on new comments
                        binding.rclComment.smoothScrollToPosition(1)
                    } else Toast.makeText(this@CommentActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                }
            })
    }

    private fun createReplyComment(idPost: String, idComment: String, nameTag: String) {
        val commentReviewBranchParams = CommentParams()
        commentReviewBranchParams.http_method = 1
        commentReviewBranchParams.request_url = "/api/branch-reviews-comments/replies"
        commentReviewBranchParams.project_id = AppConfig.PROJECT_CHAT
        commentReviewBranchParams.params.branch_review_id = idPost
        commentReviewBranchParams.params.comment_id = idComment
        commentReviewBranchParams.params.content =
            String.format("%s %s", nameTag, binding.tagingCommentEdt.text.toString().trim())
        commentReviewBranchParams.params.image_urls = listMediaComment
        commentReviewBranchParams.params.sticker = urlSticker
        binding.tagingCommentEdt.setText("")
        closeKeyboard(binding.tagingCommentEdt)
        binding.imgSent.isEnabled = true
        hideImageContainer()
//        mediaIv!!.visibility = VISIBLE
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .createReplyComment(commentReviewBranchParams)
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
                        listMediaComment.clear()
                        replyComment = response.data
                        dataComment[positionComment].reply_comment.add(replyComment)
                        dataDetailPost.comment_count = replyComment.total_comment
                        dataComment[positionComment].not_answered = false
                        adapter!!.notifyItemChanged(0)
                        adapter!!.notifyItemChanged(positionComment)
                        cancelReply()
//                        binding.rclComment.smoothScrollToPosition(positionComment)
                    } else Toast.makeText(this@CommentActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                }
            })
    }

    private fun lookAtPhoto(url: String) {
        val listPhoto = ArrayList<MediaSource>()
        listPhoto.add(
            MediaSource(
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    url
                ), 0, 0, 0
            )

        )
        let {
            ImageViewer.Builder(it, listPhoto)
                .setStartPosition(0)
                .setBackgroundColorRes(R.color.overlay_dark_70)
                .show()
        }
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
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                }
            })
    }

//    private fun View.showKeyboard() {
//        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
//    }
//
//    private fun View.hideKeyboard() {
//        if (idCommentReply == 0){
//            cancelReply()
//        }
//        requestFocus()
//        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(windowToken, 0)
//    }

    private fun copyText(data: String) {
        val clipboard = this.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
        val clip: ClipData = ClipData.newPlainText("label", data)
        clipboard!!.setPrimaryClip(clip)
        Toast.makeText(
            this,
            resources.getString(R.string.copied_clipboard),
            Toast.LENGTH_SHORT
        ).show()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun dialogQuestionDeletePost() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_post_new_feed)

        val btnDeleteNow = dialog.findViewById(R.id.btnDeleteNow) as Button
        val btnBack = dialog.findViewById(R.id.btnBack) as Button

        btnDeleteNow.setOnClickListener {
            removePost(idPost)
            dialog.dismiss()
            onBackPressed()
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun cancelReply() {
        binding.lnReply.visibility = View.GONE
        idCommentReply = ""
        nameTagReply = ""
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
        baseRequest.http_method = 1
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
                override fun onNext(response: BaseResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                        }
                        else -> {
                            Toast.makeText(
                                this@CommentActivity,
                                response.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
            })
    }

    private fun createGroupPersonal(memberList: java.util.ArrayList<Int>, memberId: Int) {
        val groupPersonalParams = GroupPersonalParams()
        groupPersonalParams.http_method = AppConfig.POST
        groupPersonalParams.project_id = AppConfig.getProjectChat()
        groupPersonalParams.request_url = "/api/groups/create-personal"
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

                    @SuppressLint("ShowToast")
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
                            val intent = Intent(this@CommentActivity, ChatActivity::class.java)
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
                                this@CommentActivity,
                                responseGroup.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                })
        }
    }

    private fun createChatPersonal(memberId: Int) {
        val createChatPersonalRequest = CreateGroupPersonalRequest()
        createChatPersonalRequest.member_id = memberId
        createChatPersonalRequest.admin_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(createChatPersonalRequest))
            mSocket!!.emit(
                TechResEnumChat.NEW_GROUP_CREATE_PERSONAL_ALO_LINE.toString(),
                jsonObject
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun addFriend(interactiveUser: InteractiveUser) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.avatar_sender = user.avatar_three_image.original
        addFriendRequest.full_name_sender = user.name
        addFriendRequest.phone_sender = user.phone
        addFriendRequest.user_id_received = interactiveUser.user_id
        addFriendRequest.avatar_received = interactiveUser.avatar.original
        addFriendRequest.full_name_received = interactiveUser.full_name
        addFriendRequest.phone_received = interactiveUser.phone
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket!!.emit(TechresEnumFriend.REQUEST_TO_CONTACT.toString(), jsonObject)
            WriteLog.d("REQUEST_TO_CONTACT", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT

        addFriendParams.params.contact_to_user_id = interactiveUser.user_id
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

                    }
                })
        }
    }

    private fun approveToContact(interactiveUser: InteractiveUser) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = interactiveUser.user_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket!!.emit(TechresEnumFriend.APPROVE_TO_CONTACT.toString(), jsonObject)
            WriteLog.d("APPROVE_TO_CONTACT", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = interactiveUser.user_id

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

                    }
                })
        }
    }

    /**
     * Status Keyboard
     * */
    private fun updateButton(emo: Boolean) {
        if (isShowingKeyBoard == emo) return
        isShowingKeyBoard = emo
        if (emo) {
            binding.imgSticker.setImageResource(R.drawable.ic_keyboard_chat_message)
        } else {
            binding.imgSticker.setImageResource(R.drawable.ic_sticker)

        }
    }

    private fun lookAtPhoto(url: String, position: Int) {
        val list = ArrayList<String>()
        list.add(String.format("%s%s", configNodeJs.api_ads, url))
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
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
        if (dataComment.size != 0) {
            dataComment[0].total_comment = dataDetailPost.comment_count
            EventBus.getDefault().post(CommentEventBusData(dataComment))
        }

        if(!binding.layoutKeyboard.onBackPressed() && !isKeyBoard) {

        } else {
            binding.layoutKeyboard.onBackPressed()
            closeKeyboard(binding.tagingCommentEdt)

        }
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}