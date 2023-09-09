package vn.techres.line.activity

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.thread.PictureThreadUtils
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.greenrobot.eventbus.EventBus
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.newsfeed.ImagesAdapter
import vn.techres.line.adapter.utils.TagReviewAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateCustomerMediaParams
import vn.techres.line.data.model.params.ReviewBranchEditReviewParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.DataPostedResponse
import vn.techres.line.data.model.response.ImageResponse
import vn.techres.line.data.model.response.OneReviewBranchReponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.Media
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityEditMyPostBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.formatNameFile
import vn.techres.line.helper.Utils.hideKeyboard
import vn.techres.line.helper.linkpreview.GetLinkPreviewListener
import vn.techres.line.helper.linkpreview.LinkPreview
import vn.techres.line.helper.linkpreview.LinkUtil
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.MediaHandler
import vn.techres.line.interfaces.TagReviewHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.view.GridSpacingDecoration
import java.io.File

class EditMyPostActivity : BaseBindingActivity<ActivityEditMyPostBinding>(), TagReviewHandler,
    MediaHandler {
    private var dialog: Dialog? = null
    private var serviceRate: Float = 0f
    private var foodRate: Float = 0f
    private var priceRate: Float = 0f
    private var spaceRate: Float = 0f
    private var hygieneRate: Float = 0f
    private var averageRate: Float = 0.0f
    private var user = User()
    private var configJava = ConfigJava()
    private var reviewBranch = PostReview()
    private var idReview = ""
    private var tagReviewAdapter: TagReviewAdapter? = null
    private var imagesAdapter: ImagesAdapter? = null
    private var lnUpload: LinearLayout? = null
    private var listMediaShow = ArrayList<Media>()
    private var listMediaCreateLink = ArrayList<Media>()
    private var listMediaUpdate = ArrayList<Media>()
    private var numLoad = 0
    private var branchId = 0
    private var configNodeJs = ConfigNodeJs()
    private var selectList: List<LocalMedia> = ArrayList()
    private var clipboardManager: ClipboardManager? = null
    private var site = ""
    private var title = ""
    private var description = ""
    private var urlImage = ""
    private var linkUrl = ""
    private var contentPaste = ""
    private var type = 0

    private var dataPosted = PostReview()

    override val bindingInflater: (LayoutInflater) -> ActivityEditMyPostBinding
        get() = ActivityEditMyPostBinding::inflate

    override fun onSetBodyView() {
        configJava = CurrentConfigJava.getConfigJava(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        user = CurrentUser.getCurrentUser(this)
        
        tagReviewAdapter = TagReviewAdapter(this)
        binding.itemInputReview.tagRecyclerView.adapter = tagReviewAdapter
        tagReviewAdapter?.setTagReviewHandler(this)

        imagesAdapter = ImagesAdapter(this)
        binding.rcMedia.adapter = imagesAdapter
        imagesAdapter?.setDeleteMediaHandler(this)

        setData()
        setListener()
    }

    private fun setData() {
        user = CurrentUser.getCurrentUser(this)


        intent?.let {
            idReview = it.getStringExtra(TechresEnum.ID_REVIEW.toString())?.toString() ?: ""
            type = it.getIntExtra("Type Post", 1)
        }

        getMyPost(idReview)

        binding.itemInputReview.tagRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        binding.rcMedia.layoutManager = GridLayoutManager(this, 3)
        binding.rcMedia.addItemDecoration(GridSpacingDecoration(3, 3))

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

        getTagReview()
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.itemInputReview.lnClickReviewPopup.setOnClickListener {
            showDialog()
        }
        binding.lnAddMedia.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .theme(R.style.picture_WeChat_style)
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .isWithVideoImage(true)
                .maxSelectNum(30)
                .minSelectNum(0)
                .maxVideoSelectNum(5)
                .selectionMode(PictureConfig.MULTIPLE)
                .isSingleDirectReturn(false)
                .isPreviewImage(true)
                .isPreviewVideo(true)
                .isOpenClickSound(true)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }

        binding.txtPost.setOnClickListener {
            checkUpdateReview()
        }

        if(selectList.isEmpty()){
            binding.itemInputPost.edtContentPost.addTextChangedListener(object : TextWatcher {
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
                    if (selectList.isEmpty() && listMediaShow.isEmpty()){
                        val primaryClipData: ClipData? = clipboardManager!!.primaryClip
                        if (primaryClipData != null){
                            val item: ClipData.Item = primaryClipData.getItemAt(0)
                            contentPaste = item.text.toString()
                            if (s.contains(contentPaste)){
                                binding.progress.visibility = View.VISIBLE
                                binding.rlLinkPreview.visibility = View.GONE
                                LinkUtil.getInstance().getLinkPreview(
                                    this@EditMyPostActivity,
                                    contentPaste,
                                    object : GetLinkPreviewListener {
                                        override fun onSuccess(link: LinkPreview) {
                                            PictureThreadUtils.runOnUiThread {
                                                try {
                                                    binding.progress.visibility = View.GONE
                                                    binding.rlLinkPreview.visibility = View.VISIBLE
                                                    if (link.imgLink == null) {
                                                        binding.imgPreview.visibility = View.GONE
                                                    } else {
                                                        binding.imgPreview.visibility = View.VISIBLE
                                                        Glide.with(binding.imgPreview)
                                                            .load(link.imgLink)
                                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                            .centerCrop()
                                                            .into(binding.imgPreview)
                                                        urlImage = link.imgLink ?: ""
                                                    }

                                                    if (link.siteName != null) {
                                                        binding.tvSite.visibility = View.VISIBLE
                                                        binding.tvSite.text = link.siteName ?: ""
                                                        site = link.siteName ?: ""
                                                    } else {
                                                        binding.tvSite.visibility = View.GONE
                                                    }

                                                    if (link.title != null) {
                                                        binding.tvTitle.visibility = View.VISIBLE
                                                        binding.tvTitle.text = link.title ?: ""
                                                        title = link.title ?: ""
                                                    } else {
                                                        binding.tvTitle.visibility = View.GONE
                                                    }

                                                    if (link.description != null) {
                                                        binding.tvDesc.visibility = View.VISIBLE
                                                        binding.tvDesc.text = link.description ?: ""
                                                        description = link.description ?: ""
                                                    } else {
                                                        binding.tvDesc.visibility = View.GONE
                                                    }

                                                    if (link.link != null) {
                                                        linkUrl = link.link
                                                        binding.rlLinkPreview.setOnClickListener {
                                                            val browserIntent =
                                                                Intent(
                                                                    Intent.ACTION_VIEW,
                                                                    Uri.parse(link.link)
                                                                )
                                                            startActivity(browserIntent)
                                                        }
                                                    } else {
                                                        linkUrl = contentPaste
                                                    }
                                                }catch (ex: Exception){}
                                            }
                                        }

                                        override fun onFailed(e: Exception) {
                                            PictureThreadUtils.runOnUiThread {
                                                binding.progress.visibility = View.GONE
                                                binding.rlLinkPreview.visibility = View.VISIBLE
                                                Toast.makeText(
                                                    this@EditMyPostActivity,
                                                    e.message,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    })
                            }
                        }
                    }
                }
            })
            binding.imgBtnClose.setOnClickListener {
                binding.rlLinkPreview.visibility = View.GONE
                linkUrl = ""
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                selectList = PictureSelector.obtainMultipleResult(data)

                if(selectList.isNotEmpty()){
                    linkUrl = ""
                    title = ""
                    description = ""
                    urlImage = ""
                    site = ""
                    binding.rlLinkPreview.visibility = View.GONE
                }

                selectList.forEach {
                    val path = it.realPath
                    val file = File(path)
                    val fileMedia = Media()

                    if (path.contains("mp4")) {
                        fileMedia.fileName = it.fileName
                        fileMedia.original = path
                        fileMedia.type = 0
                        fileMedia.typePath = 1
                        fileMedia.width = Utils.getBitmapRotationVideo(file)?.width ?: 0
                        fileMedia.height = Utils.getBitmapRotationVideo(file)?.height ?: 0
                        listMediaShow.add(fileMedia)

                    } else {
                        fileMedia.fileName = it.fileName
                        fileMedia.original = path
                        fileMedia.type = 1
                        fileMedia.typePath = 1
                        fileMedia.width = Utils.getBitmapRotationImage(file)?.width ?: 0
                        fileMedia.height = Utils.getBitmapRotationImage(file)?.height ?: 0
                        listMediaShow.add(fileMedia)
                    }
                }
                imagesAdapter?.setDataSource(listMediaShow)

                //Upload media
                for (i in listMediaShow.indices){
                    listMediaCreateLink.clear()
                    if (listMediaShow[i].typePath == 1){
                        val temp = Media()
                        temp.type = listMediaShow[i].type
                        temp.typePath = 1
                        temp.original = listMediaShow[i].url.original.toString()
                        listMediaCreateLink.add(temp)

                    }
                }

            }
        }
    }

    private fun getLink(nameFile: String, width: Int, height: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url =
            "/api-upload/get-link-file-by-user-customer?type=6&name_file=$nameFile&width=$width&height=$height"
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


                override fun onNext(response: ImageResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val media = Media()
                        if (response.data!!.link_original.contains("mp4")) {
                            media.type = 0
                            media.original = response.data!!.link_original
                            media.width = response.data!!.width
                            media.height = response.data!!.height
                            media.ratio = response.data!!.ratio
                            onCreateMedia(0, media.original!!)
                        } else {
                            media.type = 1
                            media.original = response.data!!.link_original
                            media.width = response.data!!.width
                            media.height = response.data!!.height
                            media.ratio = response.data!!.ratio
                            onCreateMedia(1, media.original!!)
                        }

                        listMediaUpdate.add(media)

                    } else {
                        Toast.makeText(this@EditMyPostActivity, response.message, Toast.LENGTH_LONG).show()
                    }

                }
            })
    }

    private fun onCreateMedia(mediaType: Int, urlMedia: String) {
        val createCustomerMediaParams = CreateCustomerMediaParams()
        createCustomerMediaParams.http_method = 1
        createCustomerMediaParams.request_url = "/api/customer-media-album-contents/create"
        createCustomerMediaParams.params.customer_media_album_id = -1
        createCustomerMediaParams.params.media_type = mediaType
        createCustomerMediaParams.params.url = urlMedia
        createCustomerMediaParams.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .createCustomerMediaRequest(it)
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
                                Toast.makeText(this@EditMyPostActivity, response.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                })
        }
    }

    private fun uploadPhoto(photo: String, name: String) {
        val serverUrlString: String =
            String.format(
                "%s/api-upload/upload-file-by-user-customer/%s/%s",
                configNodeJs.api_ads,
                6,
                name
            )
        val paramNameString = "send_file"
        try {
            MultipartUploadRequest(
                this,
                serverUrlString
            )
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
                        numLoad++
                        if (listMediaCreateLink.size == numLoad) {
                            if (branchId == 0) {
                                onUpdatePost(listMediaUpdate, type)
                            } else {
                                onUpdateReview(listMediaUpdate)
                            }
                        }
                    }
                })

        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getMyPost(idReview: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/branch-reviews/$idReview"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getDetailReviewBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OneReviewBranchReponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: OneReviewBranchReponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        reviewBranch = response.data
                        listMediaShow = response.data.media_contents
                        for (i in listMediaShow.indices){
                            val m = Media()
                            m.original = listMediaShow[i].original?:""
                            m.type = listMediaShow[i].type
                            m.width = listMediaShow[i].width
                            m.height = listMediaShow[i].height
                            m.ratio = listMediaShow[i].ratio
                            listMediaUpdate.add(m)
                        }

                        branchId = reviewBranch.branch.id!!


                        try {
                            Utils.getImage(
                                binding.imgAvatar,
                                reviewBranch.customer.avatar.thumb,
                                configNodeJs
                            )
                        }catch (ex: Exception){
                        }
                        binding.tvName.text  = reviewBranch.customer.full_name

                        if(branchId == 0){
                            binding.itemInputReview.lnEditReview.visibility = View.GONE
                            binding.itemInputPost.lnEditPost.visibility = View.VISIBLE
                            binding.itemInputPost.edtContentPost.setText(reviewBranch.content)
                            binding.itemInputPost.edtTitlePost.setText(reviewBranch.title)
                        }else{
                            binding.itemInputReview.lnEditReview.visibility = View.VISIBLE
                            binding.itemInputPost.lnEditPost.visibility = View.GONE
                            try {
                                binding.itemInputReview.imgAvatarBranch.load(getLinkDataNode(
                                    reviewBranch.branch.logo_image_url.original ?: "")){
                                    crossfade(true)
                                    scale(Scale.FILL)
                                    placeholder(R.drawable.logo_aloline_placeholder)
                                    error(R.drawable.logo_aloline_placeholder)
                                    transformations(RoundedCornersTransformation(10F))
                                }
                            }catch (ex: Exception){
                            }
                            binding.itemInputReview.txtBranchName.text  = reviewBranch.branch.name
                            binding.itemInputReview.txtBranchAddress.text  = reviewBranch.branch.address_full_text

                            serviceRate = reviewBranch.service_rate ?: 0F
                            foodRate = reviewBranch.food_rate ?: 0F
                            priceRate = reviewBranch.price_rate ?: 0F
                            spaceRate = reviewBranch.space_rate ?: 0F
                            hygieneRate = reviewBranch.hygiene_rate ?: 0F

                            binding.itemInputReview.rbRate.rating =((serviceRate + foodRate + priceRate + spaceRate + hygieneRate) / 5)
                            binding.itemInputReview.edtContentReview.setText(reviewBranch.content)
                            binding.itemInputReview.edtTitleReview.setText(reviewBranch.title)
                        }

                        // Link
                        if (reviewBranch.url != "") {
                            binding.rlLinkPreview.visibility = View.VISIBLE
                            binding.progress.visibility = View.GONE
                            if (reviewBranch.url_json_content.image != "") {
                                binding.imgPreview.visibility = View.VISIBLE
                                //glide
                                Glide.with(binding.imgPreview)
                                    .load(reviewBranch.url_json_content.image)
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .centerCrop()
                                    .into(binding.imgPreview)
                            } else {
                                binding.imgPreview.visibility = View.GONE
                            }

                            if (reviewBranch.url_json_content.canonical_url != "") {
                                binding.tvSite.visibility = View.VISIBLE
                                binding.tvSite.text = reviewBranch.url_json_content.canonical_url
                            } else {
                                binding.tvSite.visibility = View.GONE
                            }

                            if (reviewBranch.url_json_content.title != "") {
                                binding.tvTitle.visibility = View.VISIBLE
                                binding.tvTitle.text = reviewBranch.url_json_content.title
                            } else {
                                binding.tvTitle.visibility = View.GONE
                            }

                            if (reviewBranch.url_json_content.description != ""){
                                binding.tvDesc.visibility = View.VISIBLE
                                binding.tvDesc.text = reviewBranch.url_json_content.description
                            }else{
                                binding.tvDesc.visibility = View.GONE
                            }

                            binding.rlLinkPreview.setOnClickListener {
                            }
                        } else {
                            binding.rlLinkPreview.visibility = View.GONE
                        }

                        imagesAdapter?.setDataSource(listMediaShow)
                    } else Toast.makeText(this@EditMyPostActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                }
            })
    }

    private fun getLinkDataNode(url: String): String {
        return String.format(
            "%s%s",
            CurrentConfigNodeJs.getConfigNodeJs(this).api_ads,
            url
        )
    }

    private fun showDialog() {

        dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.review_popup)

        val imgClose = dialog?.findViewById(R.id.imgClose) as ImageView
        val btnReviewRate = dialog?.findViewById(R.id.btnReviewRate) as Button

        val ratingService = dialog?.findViewById(R.id.ratingService) as RatingBar
        val ratingFood = dialog?.findViewById(R.id.ratingFood) as RatingBar
        val ratingPrice = dialog?.findViewById(R.id.ratingPrice) as RatingBar
        val ratingSpace = dialog?.findViewById(R.id.ratingSpace) as RatingBar
        val ratingHygiene = dialog?.findViewById(R.id.ratingHygiene) as RatingBar
        val imgService = dialog?.findViewById(R.id.imgService) as ImageView
        val imgFood = dialog?.findViewById(R.id.imgFood) as ImageView
        val imgPrice = dialog?.findViewById(R.id.imgPrice) as ImageView
        val imgSpace = dialog?.findViewById(R.id.imgSpace) as ImageView
        val imgHygiene = dialog?.findViewById(R.id.imgHygiene) as ImageView
        val txtService = dialog?.findViewById(R.id.txtService) as TextView
        val txtFood = dialog?.findViewById(R.id.txtFood) as TextView
        val txtPrice = dialog?.findViewById(R.id.txtPrice) as TextView
        val txtSpace = dialog?.findViewById(R.id.txtSpace) as TextView
        val txtHygiene = dialog?.findViewById(R.id.txtHygiene) as TextView
        val txtCongratulations = dialog?.findViewById(R.id.txtCongratulations) as TextView
        val txtTks = dialog?.findViewById(R.id.txtTks) as TextView


        val gender = if (user.gender == 1){
            "anh"
        }else {
            "chị"
        }

        txtCongratulations.text = String.format("%s %s %s %s %s %s %s %s", "Tập thể", reviewBranch.branch.name,
            "sẽ rất vui và hạnh phúc khi nhận được những lời đánh giá chân thành từ", gender, user.name, ". Chúng tôi sẽ luôn cố gắng nổ lực để đưa đến", gender,
            "một dịch vụ tốt nhất và những món ăn ngon nhất.")

        txtTks.text = String.format("%s %s %s", "Chúc", gender, "và gia đình nhiều sức khỏe, thành công và hạnh phúc !")

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

        ratingService.setOnRatingBarChangeListener { _, rating, _ ->
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingService.rating = 1f
                    serviceRate = rating + 1
                }
                1f -> {
                    imgService.setImageResource(R.drawable.ic_rating_1)
                    txtService.text = resources.getString(R.string.bad)
                    serviceRate = rating
                }
                2f -> {
                    imgService.setImageResource(R.drawable.ic_rating_2)
                    txtService.text = resources.getString(R.string.least)
                    serviceRate = rating
                }
                3f -> {
                    imgService.setImageResource(R.drawable.ic_rating_3)
                    txtService.text = resources.getString(R.string.okey)
                    serviceRate = rating
                }
                4f -> {
                    imgService.setImageResource(R.drawable.ic_rating_4)
                    txtService.text = resources.getString(R.string.rather)
                    serviceRate = rating
                }
                5f -> {
                    imgService.setImageResource(R.drawable.ic_rating_5)
                    txtService.text = resources.getString(R.string.great)
                    serviceRate = rating
                }
            }
        }

        ratingFood.setOnRatingBarChangeListener { _, rating, _ ->
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingFood.rating = 1f
                    foodRate = rating + 1
                }
                1f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_1)
                    txtFood.text = resources.getString(R.string.bad)
                    foodRate = rating
                }
                2f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_2)
                    txtFood.text = resources.getString(R.string.least)
                    foodRate = rating
                }
                3f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_3)
                    txtFood.text = resources.getString(R.string.okey)
                    foodRate = rating
                }
                4f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_4)
                    txtFood.text = resources.getString(R.string.rather)
                    foodRate = rating
                }
                5f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_5)
                    txtFood.text = resources.getString(R.string.great)
                    foodRate = rating
                }
            }
        }

        ratingPrice.setOnRatingBarChangeListener { _, rating, _ ->
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingPrice.rating = 1f
                    priceRate = rating + 1
                }
                1f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_1)
                    txtPrice.text = resources.getString(R.string.bad)
                    priceRate = rating
                }
                2f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_2)
                    txtPrice.text = resources.getString(R.string.least)
                    priceRate = rating
                }
                3f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_3)
                    txtPrice.text = resources.getString(R.string.okey)
                    priceRate = rating
                }
                4f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_4)
                    txtPrice.text = resources.getString(R.string.rather)
                    priceRate = rating
                }
                5f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_5)
                    txtPrice.text = resources.getString(R.string.great)
                    priceRate = rating
                }
            }
        }

        ratingSpace.setOnRatingBarChangeListener { _, rating, _ ->
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingSpace.rating = 1f
                    spaceRate = rating + 1
                }
                1f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_1)
                    txtSpace.text = resources.getString(R.string.bad)
                    spaceRate = rating
                }
                2f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_2)
                    txtSpace.text = resources.getString(R.string.least)
                    spaceRate = rating
                }
                3f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_3)
                    txtSpace.text = resources.getString(R.string.okey)
                    spaceRate = rating
                }
                4f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_4)
                    txtSpace.text = resources.getString(R.string.rather)
                    spaceRate = rating
                }
                5f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_5)
                    txtSpace.text = resources.getString(R.string.great)
                    spaceRate = rating
                }
            }
        }

        ratingHygiene.setOnRatingBarChangeListener { _, rating, _ ->
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingHygiene.rating = 1f
                    hygieneRate = rating + 1
                }
                1f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_1)
                    txtHygiene.text = resources.getString(R.string.bad)
                    hygieneRate = rating
                }
                2f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_2)
                    txtHygiene.text = resources.getString(R.string.least)
                    hygieneRate = rating
                }
                3f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_3)
                    txtHygiene.text = resources.getString(R.string.okey)
                    hygieneRate = rating
                }
                4f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_4)
                    txtHygiene.text = resources.getString(R.string.rather)
                    hygieneRate = rating
                }
                5f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_5)
                    txtHygiene.text = resources.getString(R.string.great)
                    hygieneRate = rating
                }
            }
        }

        imgClose.setOnClickListener {
            dialog?.dismiss()
        }

        btnReviewRate.setOnClickListener {
            averageRate = ((serviceRate + foodRate + priceRate + spaceRate + hygieneRate) / 5)
            WriteLog.d("startserviceRate", serviceRate.toString())
            WriteLog.d("startfoodRate", foodRate.toString())
            WriteLog.d("startpriceRate", priceRate.toString())
            WriteLog.d("startspaceRate", spaceRate.toString())
            WriteLog.d("starthygieneRate", hygieneRate.toString())
            WriteLog.d("start", averageRate.toString())
            binding.itemInputReview.rbRate.rating = averageRate
            dialog?.dismiss()
        }

        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()

    }

    private fun getTagReview() {
        val lisTagReview = ArrayList<String>()
        lisTagReview.add("phục vụ tốt")
        lisTagReview.add("đồ ăn ngon")
        lisTagReview.add("giá cả phải chăng")
        lisTagReview.add("view đẹp")
        lisTagReview.add("vị trí dễ tìm")
        lisTagReview.add("không gian ấm cúng")
        lisTagReview.add("phù hợp cho cặp đôi")

        tagReviewAdapter?.setDataSource(lisTagReview)
    }

    override fun onClick(tagReview: String) {
        if (binding.itemInputReview.edtContentReview.text.isNotEmpty()) {
            binding.itemInputReview.edtContentReview.append(String.format("%s%s", ", ", tagReview))
        } else {
            binding.itemInputReview.edtContentReview.append(tagReview)
        }
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

    override fun onRemoveMedia(position: Int, nameFile: String) {

//        if (listMediaShow[position].typePath == 1){
//            for (i in listMediaCreateLink.indices){
//                if (listMediaCreateLink[i].original == listMediaShow[position].original){
//                    listMediaCreateLink.removeAt(i) }
//            }
//        }
        if (listMediaCreateLink.size != 0){
            if (listMediaShow[position].typePath == 1){
                for (i in listMediaCreateLink.indices){
                    if (listMediaCreateLink[i].original == listMediaShow[position].original){
                        listMediaCreateLink.removeAt(i)
                    }
                }
            }
        }
        listMediaUpdate.removeAt(position)
        listMediaShow.removeAt(position)
        imagesAdapter?.notifyDataSetChanged()
    }

    private fun onUpdateReview(listMedia: ArrayList<Media>) {

        val params = ReviewBranchEditReviewParams()
        params.http_method = 1
        params.project_id = AppConfig.PROJECT_CHAT
        params.request_url = "/api/branch-reviews/$idReview"

        params.params.branch_id = branchId
        params.params.title = binding.itemInputReview.edtTitleReview.text.toString()
        params.params.content = binding.itemInputReview.edtContentReview.text.toString()
        params.params.rate = averageRate.toDouble()
        params.params.service_rate = serviceRate
        params.params.food_rate = foodRate
        params.params.price_rate = priceRate
        params.params.space_rate = spaceRate
        params.params.hygiene_rate = hygieneRate
        params.params.branch_review_status = 1
        params.params.media_contents = listMedia
        params.params.url = linkUrl
        params.params.url_json_content.url = linkUrl
        params.params.url_json_content.title = title
        params.params.url_json_content.description = description
        params.params.url_json_content.image = urlImage
        params.params.url_json_content.canonical_url = site

        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .updateReviewBranch(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<DataPostedResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) = WriteLog.d("ERROR", e.message.toString())
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: DataPostedResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            lnUpload?.visibility = View.GONE
                            Toast.makeText(
                                this@EditMyPostActivity,
                                resources.getString(R.string.edit_post_review),
                                Toast.LENGTH_LONG
                            ).show()
                            dataPosted = response.data
                            dataPosted.status_reload = "edit"
                            EventBus.getDefault().post(dataPosted)
                            onBackPressed()
                        } else Toast.makeText(this@EditMyPostActivity, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }
    }

    private fun onUpdatePost(listMedia: ArrayList<Media> , type: Int) {
        val params = ReviewBranchEditReviewParams()
        params.http_method = 1
        params.project_id = AppConfig.PROJECT_CHAT
        params.request_url = "/api/branch-reviews/$idReview"

        params.params.branch_id = -1
        params.params.title = binding.itemInputPost.edtTitlePost.text.toString()
        params.params.content = binding.itemInputPost.edtContentPost.text.toString()

        params.params.rate = -1.0
        params.params.service_rate = -1f
        params.params.food_rate = -1f
        params.params.price_rate = -1f
        params.params.space_rate = -1f
        params.params.hygiene_rate = -1f
        params.params.branch_review_status = type
        params.params.media_contents = listMedia

        params.params.url = linkUrl
        params.params.url_json_content.url = linkUrl
        params.params.url_json_content.title = title
        params.params.url_json_content.description = description
        params.params.url_json_content.image = urlImage
        params.params.url_json_content.canonical_url = site
        params.params.media_contents = listMedia

        params.let {
            ServiceFactory.createRetrofitServiceNode(

                TechResService::class.java
            )
                .updateReviewBranch(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<DataPostedResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) = WriteLog.d("ERROR", e.message.toString())
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: DataPostedResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            lnUpload?.visibility = View.GONE
                            Toast.makeText(
                                this@EditMyPostActivity,
                                resources.getString(R.string.edit_post_review),
                                Toast.LENGTH_LONG
                            ).show()
                            dataPosted = response.data
                            dataPosted.status_reload = "edit"
                            EventBus.getDefault().post(dataPosted)
                            onBackPressed()
                        } else Toast.makeText(this@EditMyPostActivity, response.message, Toast.LENGTH_LONG)
                            .show()
                    }
                })
        }
    }

    private fun checkUpdateReview() {
        hideKeyboard(binding.txtPost)
        if (branchId != 0) {
            if (binding.itemInputReview.edtContentReview.text.isNotEmpty() && binding.itemInputReview.rbRate.rating.toDouble() > 0.0) {
                if (listMediaCreateLink.size != 0) {
                    lnUpload?.visibility = View.VISIBLE
                    binding.txtPost.visibility = View.GONE

                    for (i in listMediaShow.indices) {
                        if (listMediaShow[i].typePath == 1){
                            getLink(formatNameFile(listMediaShow[i].fileName), listMediaShow[i].width, listMediaShow[i].height)
                            uploadPhoto(listMediaShow[i].original!!, formatNameFile(listMediaShow[i].fileName))
                        }
                    }

                } else {
                    lnUpload?.visibility = View.VISIBLE
                    binding.txtPost.visibility = View.GONE
                    onUpdateReview(listMediaUpdate)
                }

            } else if (binding.itemInputReview.edtContentReview.text.isEmpty() && binding.itemInputReview.rbRate.rating.toDouble() > 0.0) {
                AloLineToast.makeText(
                    this,
                    resources.getString(R.string.content_review_restaurant_error),
                    AloLineToast.LENGTH_LONG,
                    AloLineToast.WARNING
                ).show()
            } else if (binding.itemInputReview.edtContentReview.text.isNotEmpty() && binding.itemInputReview.rbRate.rating.toDouble() == 0.0) {
                AloLineToast.makeText(
                    this,
                    resources.getString(R.string.star_review_restaurant_error),
                    AloLineToast.LENGTH_LONG,
                    AloLineToast.WARNING
                ).show()
            } else {
                AloLineToast.makeText(
                    this,
                    resources.getString(R.string.star_comment_review_restaurant_error),
                    AloLineToast.LENGTH_LONG,
                    AloLineToast.WARNING
                ).show()
            }

        } else {
            if (binding.itemInputPost.edtContentPost.text.isNotEmpty() || binding.itemInputPost.edtTitlePost.text.isNotEmpty() || listMediaShow.size > 0) {
                binding.lnUpload.visibility = View.VISIBLE
                if (listMediaCreateLink.size != 0) {
                    lnUpload?.visibility = View.VISIBLE
                    binding.txtPost.visibility = View.GONE

                    for (i in listMediaShow.indices) {
                        if (listMediaShow[i].typePath == 1){
                            getLink(listMediaShow[i].fileName, listMediaShow[i].width, listMediaShow[i].height)
                            uploadPhoto(listMediaShow[i].original!!, listMediaShow[i].fileName)
                        }
                    }
                } else {
                    lnUpload?.visibility = View.VISIBLE
                    binding.txtPost.visibility = View.GONE
                    onUpdatePost(listMediaUpdate, type)
                }

            } else if (binding.itemInputPost.edtContentPost.text.isEmpty() && binding.itemInputPost.edtTitlePost.text.isEmpty() && listMediaShow.size == 0) {
                AloLineToast.makeText(this, resources.getString(R.string.my_post_null), AloLineToast.LENGTH_LONG, AloLineToast.WARNING).show()
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

}