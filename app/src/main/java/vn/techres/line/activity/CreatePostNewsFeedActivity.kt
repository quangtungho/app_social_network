package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import coil.size.Scale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.share.Sharer
import com.facebook.share.model.ShareHashtag
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.widget.ShareDialog
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateCustomerMediaParams
import vn.techres.line.data.model.params.ListReviewBranchParam
import vn.techres.line.data.model.params.ReviewBranchParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.DataPostedResponse
import vn.techres.line.data.model.response.ImageResponse
import vn.techres.line.data.model.response.ReviewBranchResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.Media
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityCreatePostNewsFeedBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.linkpreview.GetLinkPreviewListener
import vn.techres.line.helper.linkpreview.LinkPreview
import vn.techres.line.helper.linkpreview.LinkUtil
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ChatUtils.getNameFileFormatTime
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File

class CreatePostNewsFeedActivity : BaseBindingActivity<ActivityCreatePostNewsFeedBinding>() {
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var imageLink = ArrayList<String>()
    private var listFile = ArrayList<Media>()
    private var listPost = ArrayList<String>()
    private var urlImageList = ArrayList<Media>()
    private var dataPosted = PostReview()
    private var dataDrafts = ArrayList<PostReview>()
    private var numLoad = 0

    private var callbackManager: CallbackManager? = null
    private var shareDialog: ShareDialog? = null
    private var selectList: ArrayList<LocalMedia> = ArrayList()
    private var checkPost = false

    private var clipboardManager: ClipboardManager? = null
    private var site = ""
    private var title = ""
    private var description = ""
    private var urlImage = ""
    private var linkUrl = ""
    private var contentPaste = ""

    override val bindingInflater: (LayoutInflater) -> ActivityCreatePostNewsFeedBinding
        get() = ActivityCreatePostNewsFeedBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)


//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        width = displayMetrics.widthPixels
//        height = displayMetrics.heightPixels

        // Initialize facebook SDK.
        FacebookSdk.sdkInitialize(this.applicationContext)

        // Create a callbackManager to handle the login responses.
        callbackManager = CallbackManager.Factory.create()

        shareDialog = ShareDialog(this)

        // this part is optional
        shareDialog!!.registerCallback(callbackManager, callback)

        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")

//        binding.txtClickDrafts.setOnClickListener {
//            val intent = Intent(this, DraftsActivity::class.java)
//            intent.putExtra(TechresEnum.DATA_DRAFTS.toString(), Gson().toJson(dataDrafts))
//            startActivity(intent)
//            this.overridePendingTransition(
//                R.anim.translate_from_right,
//                R.anim.translate_to_right
//            )
//        }

        clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

        getPostDrafts()

        setData()
        setClick()


    }

    private fun setData() {
        binding.imgAvatar.load(
            String.format(
                "%s%s",
                nodeJs.api_ads,
                user.avatar_three_image.thumb
            )
        ) {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
            size(500, 500)
        }
        binding.tvName.text = user.name
    }

    private fun setClick() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.edtContentPost.addTextChangedListener(object : TextWatcher {
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
                if (selectList.isEmpty()) {
                    val primaryClipData: ClipData? = clipboardManager!!.primaryClip
                    if (primaryClipData != null) {
                        val item: ClipData.Item = primaryClipData.getItemAt(0)
                        if (item.text != null) {
                            contentPaste = item.text.toString()
                            if (s.contains(contentPaste)) {
                                binding.progress.visibility = View.VISIBLE
                                binding.rlLinkPreview.visibility = View.GONE
                                LinkUtil.getInstance().getLinkPreview(
                                    this@CreatePostNewsFeedActivity,
                                    contentPaste,
                                    object : GetLinkPreviewListener {
                                        override fun onSuccess(link: LinkPreview) {
                                            PictureThreadUtils.runOnUiThread {
                                                binding.progress.visibility = View.GONE
                                                binding.rlLinkPreview.visibility = View.VISIBLE
                                                if (link.imgLink == null) {
                                                    binding.imgPreview.visibility = View.GONE
                                                } else {
                                                    binding.imgPreview.visibility = View.VISIBLE
                                                    getMediaGlide(binding.imgPreview, link.imgLink)
                                                    urlImage = link.imgLink ?: ""
                                                }

                                                if (link.siteName != null) {
                                                    binding.txtSite.visibility = View.VISIBLE
                                                    binding.txtSite.text = link.siteName ?: ""
                                                    site = link.siteName ?: ""
                                                } else {
                                                    binding.txtSite.visibility = View.GONE
                                                }

                                                if (link.title != null) {
                                                    binding.txtTitle.visibility = View.VISIBLE
                                                    binding.txtTitle.text = link.title ?: ""
                                                    title = link.title ?: ""
                                                } else {
                                                    binding.txtTitle.visibility = View.GONE
                                                }

                                                if (link.description != null) {
                                                    binding.txtDesc.visibility = View.VISIBLE
                                                    binding.txtDesc.text = link.description ?: ""
                                                    description = link.description ?: ""
                                                } else {
                                                    binding.txtDesc.visibility = View.GONE
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
                                            }
                                        }

                                        override fun onFailed(e: java.lang.Exception) {
                                            PictureThreadUtils.runOnUiThread {
                                                binding.progress.visibility = View.GONE
//                                            binding.rlLinkPreview.visibility = View.VISIBLE
//                                            Toast.makeText(
//                                                this@CreatePostNewsFeedActivity,
//                                                e.message,
//                                                Toast.LENGTH_SHORT
//                                            ).show()
                                            }
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        })

        binding.lnAddMediaPost.setOnClickListener {
            chooseMedia()
        }

        binding.lnMedia.setOnClickListener {
            chooseMedia()
        }

        binding.tvPost.setOnClickListener {

            checkPost(1)
        }

        binding.imgBtnClose.setOnClickListener {
            binding.rlLinkPreview.visibility = View.GONE
            linkUrl = ""
        }

        binding.itemMediaOne.imgDeleteOneOne.setOnClickListener {
            selectList.removeAt(0)
            listFile.removeAt(0)
            showMedia(listFile)
        }

        binding.itemMediaTwo.imgDeleteOneTwo.setOnClickListener {
            selectList.removeAt(0)
            listFile.removeAt(0)
            showMedia(listFile)
        }
        binding.itemMediaTwo.imgDeleteTwoTwo.setOnClickListener {
            selectList.removeAt(1)
            listFile.removeAt(1)
            showMedia(listFile)
        }

        binding.itemMediaThree.imgDeleteOneThree.setOnClickListener {
            selectList.removeAt(0)
            listFile.removeAt(0)
            showMedia(listFile)
        }
        binding.itemMediaThree.imgDeleteTwoThree.setOnClickListener {
            selectList.removeAt(1)
            listFile.removeAt(1)
            showMedia(listFile)
        }
        binding.itemMediaThree.imgDeleteThreeThree.setOnClickListener {
            selectList.removeAt(2)
            listFile.removeAt(2)
            showMedia(listFile)
        }

        binding.itemMediaFour.imgDeleteOneFour.setOnClickListener {
            selectList.removeAt(0)
            listFile.removeAt(0)
            showMedia(listFile)
        }
        binding.itemMediaFour.imgDeleteTwoFour.setOnClickListener {
            selectList.removeAt(1)
            listFile.removeAt(1)
            showMedia(listFile)
        }
        binding.itemMediaFour.imgDeleteThreeFour.setOnClickListener {
            selectList.removeAt(2)
            listFile.removeAt(2)
            showMedia(listFile)
        }
        binding.itemMediaFour.imgDeleteFourFour.setOnClickListener {
            selectList.removeAt(3)
            listFile.removeAt(3)
            showMedia(listFile)
        }

        binding.itemMediaFive.imgDeleteOneFive.setOnClickListener {
            selectList.removeAt(0)
            listFile.removeAt(0)
            showMedia(listFile)
        }
        binding.itemMediaFive.imgDeleteTwoFive.setOnClickListener {
            selectList.removeAt(1)
            listFile.removeAt(1)
            showMedia(listFile)
        }
        binding.itemMediaFive.imgDeleteThreeFive.setOnClickListener {
            selectList.removeAt(2)
            listFile.removeAt(2)
            showMedia(listFile)
        }
        binding.itemMediaFive.imgDeleteFourFive.setOnClickListener {
            selectList.removeAt(3)
            listFile.removeAt(3)
            showMedia(listFile)
        }
        binding.itemMediaFive.imgDeleteFiveFive.setOnClickListener {
            selectList.removeAt(4)
            listFile.removeAt(4)
            showMedia(listFile)
        }


        binding.itemMediaOne.imgOneTypeOne.setOnClickListener {
            onShowDetailMedia(listFile, 0)
        }

        binding.itemMediaTwo.imgOneTypeTwo.setOnClickListener {
            onShowDetailMedia(listFile, 0)
        }
        binding.itemMediaTwo.imgTwoTypeTwo.setOnClickListener {
            onShowDetailMedia(listFile, 1)
        }

        binding.itemMediaThree.imgOneTypeThree.setOnClickListener {
            onShowDetailMedia(listFile, 0)
        }
        binding.itemMediaThree.imgTwoTypeThree.setOnClickListener {
            onShowDetailMedia(listFile, 1)
        }
        binding.itemMediaThree.imgThreeTypeThree.setOnClickListener {
            onShowDetailMedia(listFile, 2)
        }

        binding.itemMediaFour.imgOneTypeFour.setOnClickListener {
            onShowDetailMedia(listFile, 0)
        }
        binding.itemMediaFour.imgTwoTypeFour.setOnClickListener {
            onShowDetailMedia(listFile, 1)
        }
        binding.itemMediaFour.imgThreeTypeFour.setOnClickListener {
            onShowDetailMedia(listFile, 2)
        }
        binding.itemMediaFour.imgFourTypeFour.setOnClickListener {
            onShowDetailMedia(listFile, 3)
        }

        binding.itemMediaFive.imgOneTypeFive.setOnClickListener {
            onShowDetailMedia(listFile, 0)
        }
        binding.itemMediaFive.imgTwoTypeFive.setOnClickListener {
            onShowDetailMedia(listFile, 1)
        }
        binding.itemMediaFive.imgThreeTypeFive.setOnClickListener {
            onShowDetailMedia(listFile, 2)
        }
        binding.itemMediaFive.imgFourTypeFive.setOnClickListener {
            onShowDetailMedia(listFile, 3)
        }
        binding.itemMediaFive.imgFiveTypeFive.setOnClickListener {
            onShowDetailMedia(listFile, 4)
        }
    }

    private fun chooseMedia() {
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
            .selectionData(selectList)
            .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    override fun onResume() {
        super.onResume()
        getPostDrafts()
        if (listFile.size != 0) {
            showMedia(listFile)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                selectList = PictureSelector.obtainMultipleResult(data) as ArrayList<LocalMedia>
                imageLink.clear()
                listFile.clear()

                if (selectList.isNotEmpty()) {
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
                    imageLink.add(path)
                    val fileMedia = Media()

                    if (path.contains("mp4")) {
                        fileMedia.fileName = it.fileName
                        fileMedia.original = path
                        fileMedia.type = 0
                        fileMedia.width = Utils.getBitmapRotationVideo(file)?.width ?: 0
                        fileMedia.height = Utils.getBitmapRotationVideo(file)?.height ?: 0
                        listFile.add(fileMedia)

                    } else {
                        fileMedia.fileName = it.fileName
                        fileMedia.original = path
                        fileMedia.type = 1
                        fileMedia.width = Utils.getBitmapRotationImage(file)?.width ?: 0
                        fileMedia.height = Utils.getBitmapRotationImage(file)?.height ?: 0
                        listFile.add(fileMedia)
                    }
                }
            }
            // Call callbackManager.onActivityResult to pass login result to the LoginManager via callbackManager.
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkPost(type: Int) {
        if (binding.edtContentPost.text.isNotEmpty() || binding.edtTitle.text.isNotEmpty() || imageLink.size > 0) {
            binding.lnUpload.visibility = View.VISIBLE
            if (listFile.isNotEmpty()) {
                binding.tvPost.visibility = View.GONE
                listPost.clear()
                urlImageList.clear()
                for (i in listFile.indices) {
                    getNameFileFormatTime(listFile[i].fileName)?.let {
                        getLink(
                            it,
                            listFile[i].width,
                            listFile[i].height,
                            listFile[i].original!!,
                            type
                        )
                    }
                }
            } else {
                binding.tvPost.visibility = View.GONE
                onCreatePost(urlImageList, type)
            }

        } else if (binding.edtContentPost.text.isEmpty() && binding.edtTitle.text.isEmpty() && imageLink.size == 0) {
            AloLineToast.makeText(
                this,
                resources.getString(R.string.my_post_null),
                AloLineToast.LENGTH_LONG,
                AloLineToast.WARNING
            ).show()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")

    /**
     * Set show media
     * Type: video = 0; image = 1
     */

    private fun showMedia(arrayList: ArrayList<Media>) {
        when (arrayList.size) {
            0 -> {
                binding.itemMediaOne.lnMediaOne.visibility = View.GONE
                binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
                binding.itemMediaThree.lnMediaThree.visibility = View.GONE
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE
            }
            1 -> {
                binding.itemMediaOne.lnMediaOne.visibility = View.VISIBLE
                binding.itemMediaOne.imgDeleteOneOne.visibility = View.VISIBLE
                binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
                binding.itemMediaThree.lnMediaThree.visibility = View.GONE
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE
//                val ratioOne = arrayList[0].height/arrayList[0].width
                if (arrayList[0].type == 0) {
                    binding.itemMediaOne.lnVideo.visibility = View.VISIBLE
                    binding.itemMediaOne.imgOneTypeOne.visibility = View.GONE
                    binding.itemMediaOne.thumbnailVideo.visibility = View.VISIBLE
                    binding.itemMediaOne.imgPlay.visibility = View.VISIBLE
                    binding.itemMediaOne.playerView.visibility = View.GONE
                    binding.itemMediaOne.btnVolume.visibility = View.GONE
                    Glide.with(this)
                        .load(arrayList[0].original)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                        )
                        .into(binding.itemMediaOne.thumbnailVideo)
                } else {
                    binding.itemMediaOne.lnVideo.visibility = View.GONE
                    binding.itemMediaOne.imgOneTypeOne.visibility = View.VISIBLE
                    binding.itemMediaOne.thumbnailVideo.visibility = View.GONE
                    binding.itemMediaOne.imgPlay.visibility = View.GONE
                    binding.itemMediaOne.playerView.visibility = View.GONE
                    binding.itemMediaOne.btnVolume.visibility = View.GONE
                    Glide.with(this)
                        .load(arrayList[0].original)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_placeholder)
                        )
                        .into(binding.itemMediaOne.imgOneTypeOne)
                }

            }
            2 -> {
                binding.itemMediaOne.lnMediaOne.visibility = View.GONE
                binding.itemMediaTwo.lnMediaTwo.visibility = View.VISIBLE
                binding.itemMediaTwo.imgDeleteOneTwo.visibility = View.VISIBLE
                binding.itemMediaTwo.imgDeleteTwoTwo.visibility = View.VISIBLE
                binding.itemMediaThree.lnMediaThree.visibility = View.GONE
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE

                if (arrayList[0].type == 0) {
                    binding.itemMediaTwo.imgOneTypeTwoPlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaTwo.imgOneTypeTwoPlay.visibility = View.GONE

                if (arrayList[1].type == 0) {
                    binding.itemMediaTwo.imgTwoTypeTwoPlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaTwo.imgTwoTypeTwoPlay.visibility = View.GONE

                getMediaGlide(binding.itemMediaTwo.imgOneTypeTwo, arrayList[0].original)
                getMediaGlide(binding.itemMediaTwo.imgTwoTypeTwo, arrayList[1].original)
            }
            3 -> {
                binding.itemMediaOne.lnMediaOne.visibility = View.GONE
                binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
                binding.itemMediaThree.lnMediaThree.visibility = View.VISIBLE
                binding.itemMediaThree.imgDeleteOneThree.visibility = View.VISIBLE
                binding.itemMediaThree.imgDeleteTwoThree.visibility = View.VISIBLE
                binding.itemMediaThree.imgDeleteThreeThree.visibility = View.VISIBLE
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE

                if (arrayList[0].type == 0) {
                    binding.itemMediaThree.imgOneTypeThreePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaThree.imgOneTypeThreePlay.visibility = View.GONE

                if (arrayList[1].type == 0) {
                    binding.itemMediaThree.imgTwoTypeThreePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaThree.imgTwoTypeThreePlay.visibility = View.GONE

                if (arrayList[2].type == 0) {
                    binding.itemMediaThree.imgThreeTypeThreePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaThree.imgThreeTypeThreePlay.visibility = View.GONE

                getMediaGlide(binding.itemMediaThree.imgOneTypeThree, arrayList[0].original)
                getMediaGlide(binding.itemMediaThree.imgTwoTypeThree, arrayList[1].original)
                getMediaGlide(binding.itemMediaThree.imgThreeTypeThree, arrayList[2].original)
            }
            4 -> {
                binding.itemMediaOne.lnMediaOne.visibility = View.GONE
                binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
                binding.itemMediaThree.lnMediaThree.visibility = View.GONE
                binding.itemMediaFour.lnMediaFour.visibility = View.VISIBLE
                binding.itemMediaFour.imgDeleteOneFour.visibility = View.VISIBLE
                binding.itemMediaFour.imgDeleteTwoFour.visibility = View.VISIBLE
                binding.itemMediaFour.imgDeleteThreeFour.visibility = View.VISIBLE
                binding.itemMediaFour.imgDeleteFourFour.visibility = View.VISIBLE
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE

                if (arrayList[0].type == 0) {
                    binding.itemMediaFour.imgOneTypeFourPlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFour.imgOneTypeFourPlay.visibility = View.GONE

                if (arrayList[1].type == 0) {
                    binding.itemMediaFour.imgTwoTypeFourPlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFour.imgTwoTypeFourPlay.visibility = View.GONE

                if (arrayList[2].type == 0) {
                    binding.itemMediaFour.imgThreeTypeFourPlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFour.imgThreeTypeFourPlay.visibility = View.GONE

                if (arrayList[3].type == 0) {
                    binding.itemMediaFour.imgFourTypeFourPlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFour.imgFourTypeFourPlay.visibility = View.GONE

                getMediaGlide(binding.itemMediaFour.imgOneTypeFour, arrayList[0].original)
                getMediaGlide(binding.itemMediaFour.imgTwoTypeFour, arrayList[1].original)
                getMediaGlide(binding.itemMediaFour.imgThreeTypeFour, arrayList[2].original)
                getMediaGlide(binding.itemMediaFour.imgFourTypeFour, arrayList[3].original)
            }
            5 -> {
                binding.itemMediaOne.lnMediaOne.visibility = View.GONE
                binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
                binding.itemMediaThree.lnMediaThree.visibility = View.GONE
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.tvMoreImage.visibility = View.GONE
                binding.itemMediaFive.lnMediaFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteOneFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteTwoFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteThreeFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteFourFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteFiveFive.visibility = View.VISIBLE

                if (arrayList[0].type == 0) {
                    binding.itemMediaFive.imgOneTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgOneTypeFivePlay.visibility = View.GONE

                if (arrayList[1].type == 0) {
                    binding.itemMediaFive.imgTwoTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgTwoTypeFivePlay.visibility = View.GONE

                if (arrayList[2].type == 0) {
                    binding.itemMediaFive.imgThreeTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgThreeTypeFivePlay.visibility = View.GONE

                if (arrayList[3].type == 0) {
                    binding.itemMediaFive.imgFourTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgFourTypeFivePlay.visibility = View.GONE

                if (arrayList[4].type == 0) {
                    binding.itemMediaFive.imgFiveTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgFiveTypeFivePlay.visibility = View.GONE

                getMediaGlide(binding.itemMediaFive.imgOneTypeFive, arrayList[0].original)
                getMediaGlide(binding.itemMediaFive.imgTwoTypeFive, arrayList[1].original)
                getMediaGlide(binding.itemMediaFive.imgThreeTypeFive, arrayList[2].original)
                getMediaGlide(binding.itemMediaFive.imgFourTypeFive, arrayList[3].original)
                getMediaGlide(binding.itemMediaFive.imgFiveTypeFive, arrayList[4].original)
            }
            else -> {
                binding.itemMediaOne.lnMediaOne.visibility = View.GONE
                binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
                binding.itemMediaThree.lnMediaThree.visibility = View.GONE
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.tvMoreImage.visibility = View.VISIBLE
                binding.itemMediaFive.lnMediaFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteOneFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteTwoFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteThreeFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteFourFive.visibility = View.VISIBLE
                binding.itemMediaFive.imgDeleteFiveFive.visibility = View.VISIBLE
                binding.itemMediaFive.tvMoreImage.text =
                    String.format("%s %s", "+", arrayList.size - 5)

                if (arrayList[0].type == 0) {
                    binding.itemMediaFive.imgOneTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgOneTypeFivePlay.visibility = View.GONE

                if (arrayList[1].type == 0) {
                    binding.itemMediaFive.imgTwoTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgTwoTypeFivePlay.visibility = View.GONE

                if (arrayList[2].type == 0) {
                    binding.itemMediaFive.imgThreeTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgThreeTypeFivePlay.visibility = View.GONE

                if (arrayList[3].type == 0) {
                    binding.itemMediaFive.imgFourTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgFourTypeFivePlay.visibility = View.GONE

                if (arrayList[4].type == 0) {
                    binding.itemMediaFive.imgFiveTypeFivePlay.visibility = View.VISIBLE
                } else
                    binding.itemMediaFive.imgFiveTypeFivePlay.visibility = View.GONE

                getMediaGlide(binding.itemMediaFive.imgOneTypeFive, arrayList[0].original)
                getMediaGlide(binding.itemMediaFive.imgTwoTypeFive, arrayList[1].original)
                getMediaGlide(binding.itemMediaFive.imgThreeTypeFive, arrayList[2].original)
                getMediaGlide(binding.itemMediaFive.imgFourTypeFive, arrayList[3].original)
                getMediaGlide(binding.itemMediaFive.imgFiveTypeFive, arrayList[4].original)
            }
        }
    }

    /**
     * Use glide show media
     */
    private fun getMediaGlide(imageView: ImageView, string: String?) {
        Glide.with(this)
            .load(string)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .error(R.drawable.ic_image_placeholder)
            .into(imageView)
    }

    private fun getLink(nameFile: String, width: Int, height: Int, linkImage: String, type: Int) {
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
                        urlImageList.add(media)
                        uploadPhoto(linkImage, nameFile, type)
                    } else {
                        Toast.makeText(
                            this@CreatePostNewsFeedActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            })
    }

    private fun uploadPhoto(photo: String, name: String, type: Int) {
        val serverUrlString: String =
            String.format(
                "%s/api-upload/upload-file-by-user-customer/%s/%s",
                nodeJs.api_ads,
                6,
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
                        numLoad++
                        if (urlImageList.size == numLoad) {
                            onCreatePost(urlImageList, type)
                        }
                    }
                })

        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }


    /**
     * CREATE POST
     * Type = 0 --> Tạo bài nháp
     * Type = 1 --> Tạo bài viết
     */
    private fun onCreatePost(listLinkImage: ArrayList<Media>, type: Int) {
        val reviewBranchParams = ReviewBranchParams()
        reviewBranchParams.http_method = 1
        reviewBranchParams.project_id = AppConfig.PROJECT_CHAT
        reviewBranchParams.request_url = "/api/branch-reviews"

        reviewBranchParams.params.content = binding.edtContentPost.text.toString().trim()
        reviewBranchParams.params.rate = -1.0
        reviewBranchParams.params.branch_id = -1
        reviewBranchParams.params.title = binding.edtTitle.text.toString().trim()
        reviewBranchParams.params.media_contents = listLinkImage
        reviewBranchParams.params.service_rate = -1f
        reviewBranchParams.params.food_rate = -1f
        reviewBranchParams.params.price_rate = -1f
        reviewBranchParams.params.space_rate = -1f
        reviewBranchParams.params.hygiene_rate = -1f
        reviewBranchParams.params.branch_review_status = type
        reviewBranchParams.params.url = linkUrl
        reviewBranchParams.params.url_json_content.url = linkUrl
        reviewBranchParams.params.url_json_content.title = title
        reviewBranchParams.params.url_json_content.description = description
        reviewBranchParams.params.url_json_content.image = urlImage
        reviewBranchParams.params.url_json_content.canonical_url = site

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .createReviewBranch(reviewBranchParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DataPostedResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: DataPostedResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                            binding.lnUpload.visibility = View.GONE
                            if (type == 1) {
                                Toast.makeText(
                                    this@CreatePostNewsFeedActivity,
                                    resources.getString(R.string.success_review_restaurant),
                                    Toast.LENGTH_LONG
                                ).show()
                                shareFacebook(
                                    binding.edtTitle.text.toString(),
                                    binding.edtContentPost.text.toString()
                                )
                            } else {
                                Toast.makeText(
                                    this@CreatePostNewsFeedActivity,
                                    resources.getString(R.string.success_save_to_draft),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            checkPost = true
                            if (type == 1) {
                                dataPosted = response.data
                            }
                            dataPosted.status_reload = "create"
                            onBackPressed()
                        }

                        else -> {
                            Toast.makeText(
                                this@CreatePostNewsFeedActivity,
                                resources.getString(R.string.error_sever_post),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            binding.lnUpload.visibility = View.GONE
                        }
                    }
                }
            })
    }

    //=============== Handle Facebook share dialog ==========
    private fun shareFacebook(title: String, contentPost: String) {
        val hashtag = ShareHashtag.Builder()
        hashtag.hashtag =
            "#monngon #monnuong #quannhau #monnuongsaigon #dattiecsinhnhat #nhahang #quanan @quannuong"
        val contentPhoto = SharePhotoContent.Builder()
        val photos = ArrayList<SharePhoto>()
        for (i in imageLink.indices) {
            val photo = SharePhoto.Builder()
            photo.setImageUrl(Uri.parse(imageLink[i]))
            photo.setCaption(title)
            photos.add(photo.build())
        }

//        content_photo.addPhotos()
//        val content = ShareLinkContent.Builder()
//            .setContecntTitle("Tutorialwing - Free programming tutorials")
//            .setImageUrl(Uri.parse("https://scontent-sin6-1.xx.fbcdn.net/t31.0-8/13403381_247495578953089_8113745370016563192_o.png"))
//            .setContentDescription("Tutorialwing is an online platform for free programming tutorials. These tutorials are designed for beginners as well as experienced programmers.")
//            .setContentUrl(Uri.parse("https://www.facebook.com/tutorialwing/"))
//            .setShareHashtag(
//                ShareHashtag.Builder()
//                    .setHashtag("#Tutorialwing")
//                    .build()
//            )
//            .setQuote("Learn and share your knowledge")
//            .build()

        contentPhoto.addPhotos(photos)
        contentPhoto.setShareHashtag(hashtag.build())
        contentPhoto.setRef(contentPost)
        shareDialog?.show(contentPhoto.build())

    }

    private val callback: FacebookCallback<Sharer.Result?> =
        object : FacebookCallback<Sharer.Result?> {
            override fun onSuccess(result: Sharer.Result?) {
                Log.v("TAG", "Successfully posted")
                // Write some code to do some operations when you shared content successfully.
                onBackPressed()
            }

            override fun onCancel() {
                Log.v("TAG", "Sharing cancelled")
                // Write some code to do some operations when you cancel sharing content.
                onBackPressed()
            }

            override fun onError(error: FacebookException) {
                Log.v("TAG", error.localizedMessage ?: "")
                // Write some code to do some operations when some error occurs while sharing content.
                onBackPressed()
            }
        }


    private fun onCreateMedia(mediaType: Int, urlMedia: String) {

        val createCustomerMediaParams = CreateCustomerMediaParams()
        createCustomerMediaParams.http_method = 1
        createCustomerMediaParams.request_url = "/api/customer-media-album-contents/create"
        createCustomerMediaParams.params.customer_media_album_id = -1
        createCustomerMediaParams.params.media_type = mediaType
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
                        AppConfig.SUCCESS_CODE -> {}
                        else -> {
                            Toast.makeText(
                                this@CreatePostNewsFeedActivity,
                                response.message,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }

                }
            })

    }

    private fun getPostDrafts() {
        val listReviewBranchParam = ListReviewBranchParam()
        listReviewBranchParam.http_method = 0
        listReviewBranchParam.project_id = AppConfig.PROJECT_CHAT
        listReviewBranchParam.request_url = "/api/branch-reviews"
        listReviewBranchParam.params.restaurant_id = -1
        listReviewBranchParam.params.branch_review_status = 0
        listReviewBranchParam.params.branch_id = -1
        listReviewBranchParam.params.customer_id = CurrentUser.getCurrentUser(this).id
        listReviewBranchParam.params.types = "0,1,3"
        listReviewBranchParam.params.limit = 100
        listReviewBranchParam.params.page = 1


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
                        if (response.data.total_record != 0) {
                            dataDrafts = response.data.list
                            binding.lnDrafts.visibility = View.VISIBLE
                            binding.txtContentDrafts.text = String.format(
                                "%s %s %s",
                                "Hiện tại bạn đang có",
                                response.data.total_record,
                                "bài nháp, "
                            )
                            binding.txtClickDrafts.setOnClickListener {
                                val intent = Intent(
                                    this@CreatePostNewsFeedActivity,
                                    DetailPostDraftsActivity::class.java
                                )
                                startActivity(intent)
                            }

                        } else {
                            binding.lnDrafts.visibility = View.GONE
                        }
                    }
                }
            })

    }

    private fun onShowDetailMedia(url: ArrayList<Media>, position: Int) {
        val list = ArrayList<String>()
        url.forEach {
            list.add(it.original ?: "")
        }
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onBackPressed() {
        if (checkPost) {
            EventBus.getDefault().post(dataPosted)
            super.onBackPressed()
        } else {
            if (binding.edtContentPost.text.isNotEmpty() || binding.edtTitle.text.isNotEmpty() || imageLink.size > 0) {
                val bottomSheetDialog = BottomSheetDialog(this)
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_call_back_create_post)
                bottomSheetDialog.setCancelable(true)
                val clSave = bottomSheetDialog.findViewById<ConstraintLayout>(R.id.clSave)
                val txtCanclePost = bottomSheetDialog.findViewById<TextView>(R.id.txtCanclePost)
                val txtEditContinue = bottomSheetDialog.findViewById<TextView>(R.id.txtEditContinue)


                clSave!!.setOnClickListener {
                    checkPost(0)
                    bottomSheetDialog.dismiss()
                }
                txtCanclePost!!.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    super.onBackPressed()
                }

                txtEditContinue!!.setOnClickListener {
                    bottomSheetDialog.dismiss()
                }
                bottomSheetDialog.show()
            } else {
                super.onBackPressed()
            }
        }
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}