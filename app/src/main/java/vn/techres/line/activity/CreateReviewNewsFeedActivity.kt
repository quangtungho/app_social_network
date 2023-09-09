package vn.techres.line.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.utils.TagReviewAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.data.model.eventbus.BranchSelectedEventBus
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateCustomerMediaParams
import vn.techres.line.data.model.params.ReviewBranchParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.DataPostedResponse
import vn.techres.line.data.model.response.ImageResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.Media
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityCreateReviewNewsFeedBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.TagReviewHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.util.*

class CreateReviewNewsFeedActivity : BaseBindingActivity<ActivityCreateReviewNewsFeedBinding>(),
    OnRefreshFragment,
    TagReviewHandler {

    private var listFile = ArrayList<Media>()
    private var urlImageList = ArrayList<Media>()
    private var listPost = ArrayList<String>()
    private var imageLink = ArrayList<String>()
    private var branchID: Int? = 0
    private var branch: Branch? = null
    private var dialog: Dialog? = null
    private var serviceRate: Float = 0.0f
    private var foodRate: Float = 0.0f
    private var priceRate: Float = 0.0f
    private var spaceRate: Float = 0.0f
    private var hygieneRate: Float = 0.0f
    private var averageRate: Float = 0.0f
    private var tagReviewAdapter: TagReviewAdapter? = null
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var numLoad = 0
    private var selectList: ArrayList<LocalMedia> = ArrayList()
    private var checkPost = false
    private var dataPosted = PostReview()
    private var branchName = "PostReview()"
    private var branchDetail = BranchDetail()

    override val bindingInflater: (LayoutInflater) -> ActivityCreateReviewNewsFeedBinding
        get() = ActivityCreateReviewNewsFeedBinding::inflate

    override fun onSetBodyView() {
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        user = CurrentUser.getCurrentUser(this)
        tagReviewAdapter = TagReviewAdapter(this)
        binding.tagRecyclerView.adapter = tagReviewAdapter
        tagReviewAdapter!!.setTagReviewHandler(this)
        binding.tagRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        intent.let {
            branchDetail =
                Gson().fromJson(it.getStringExtra(TechresEnum.BRANCH_DETAIL.toString()), object :
                    TypeToken<BranchDetail>() {}.type)
        }

        getTagReview()
        setData()
        setClick()

    }

    private fun setData(){
        if (branchDetail.id != 0){
            binding.lnChooseBranch.isEnabled = false
            binding.txtChooseBranch.visibility = View.GONE
            binding.lnBranchSelected.visibility = View.VISIBLE
            binding.imgAvatarBranch.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    branchDetail.image_logo_url.medium
                )
            ) {
                crossfade(true)
                scale(Scale.FILL)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                transformations(RoundedCornersTransformation(10F))
            }
            binding.txtNameBranch.text = branchDetail.name
            binding.txtAddressBranch.text = branchDetail.address_full_text
            branchID = branchDetail.id
            branchName = branchDetail.name ?: ""
        }
    }

    private fun setClick() {
        binding.lnChooseBranch.setOnClickListener {
            if (branchDetail.id == 0){
                val intent = Intent(this, ChooseBranchActivity::class.java)
                intent.putExtra(TechresEnum.TYPE_CHOOSE_BRANCH.toString(), "create_review_news_feed")
                startActivity(intent)
                overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
            }

        }
        binding.lnAddMedia.setOnClickListener {
            chooseMedia()
        }
        binding.lnMedia.setOnClickListener {
            chooseMedia()
        }
        binding.lnClickReviewPopup.setOnClickListener {
            if (branchID != 0) {
                showDialog()
            } else {
                AloLineToast.makeText(
                    this,
                    resources.getString(R.string.choose_restaurant_review),
                    AloLineToast.LENGTH_LONG,
                    AloLineToast.WARNING
                ).show()
            }
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvPost.setOnClickListener {
            Utils.hideKeyboard(binding.flChooseBranch)
            checkPostReview()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventBusChooseBranch(branchSelected: BranchSelectedEventBus) {
        if (branchSelected.data.id != 0) {
            binding.txtChooseBranch.visibility = View.GONE
            binding.lnBranchSelected.visibility = View.VISIBLE
            binding.imgAvatarBranch.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    branchSelected.data.image_logo_url.medium
                )
            ) {
                crossfade(true)
                scale(Scale.FILL)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                transformations(RoundedCornersTransformation(10F))
            }
            binding.txtNameBranch.text = branchSelected.data.name
            binding.txtAddressBranch.text = branchSelected.data.address_full_text
            branchID = branchSelected.data.id
            branchName = branchSelected.data.name ?: ""
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                selectList = PictureSelector.obtainMultipleResult(data) as ArrayList<LocalMedia>
                imageLink.clear()
                listFile.clear()

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
        }
    }

    override fun onResume() {
        super.onResume()
        if (listFile.size != 0) {
            showMedia(listFile)
        }
    }

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
                binding.itemMediaTwo.lnMediaTwo.visibility = View.GONE
                binding.itemMediaOne.imgDeleteOneOne.visibility = View.VISIBLE
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
                binding.itemMediaThree.lnMediaThree.visibility = View.GONE
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE
                binding.itemMediaTwo.imgDeleteOneTwo.visibility = View.VISIBLE
                binding.itemMediaTwo.imgDeleteTwoTwo.visibility = View.VISIBLE
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
                binding.itemMediaFour.lnMediaFour.visibility = View.GONE
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE
                binding.itemMediaThree.imgDeleteOneThree.visibility = View.VISIBLE
                binding.itemMediaThree.imgDeleteTwoThree.visibility = View.VISIBLE
                binding.itemMediaThree.imgDeleteThreeThree.visibility = View.VISIBLE

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
                binding.itemMediaFive.lnMediaFive.visibility = View.GONE
                binding.itemMediaFour.imgDeleteOneFour.visibility = View.VISIBLE
                binding.itemMediaFour.imgDeleteTwoFour.visibility = View.VISIBLE
                binding.itemMediaFour.imgDeleteThreeFour.visibility = View.VISIBLE
                binding.itemMediaFour.imgDeleteFourFour.visibility = View.VISIBLE

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
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(imageView)
    }

    private fun getLinkReview(nameFile: String, width: Int, height: Int, linkImage: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
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
                        uploadPhoto(linkImage, nameFile)
                    } else {
                        Toast.makeText(
                            this@CreateReviewNewsFeedActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            })
    }

    private fun uploadPhoto(photo: String, name: String) {
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
                            onCreateReview(urlImageList, 1)
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
    private fun onCreateReview(listLinkImage: ArrayList<Media>, type: Int) {
        val reviewBranchParams = ReviewBranchParams()
        reviewBranchParams.http_method = 1
        reviewBranchParams.project_id = AppConfig.PROJECT_CHAT
        reviewBranchParams.request_url = "/api/branch-reviews"
        reviewBranchParams.params.restaurant_id = restaurant().restaurant_id!!
        reviewBranchParams.params.content = binding.edtContent.text.toString()
        reviewBranchParams.params.rate = binding.rbCreateNewRate.rating.toDouble()
        reviewBranchParams.params.branch_id = branchID
        reviewBranchParams.params.title = binding.edtTitle.text.toString()
        reviewBranchParams.params.media_contents = listLinkImage
        reviewBranchParams.params.service_rate = serviceRate
        reviewBranchParams.params.food_rate = foodRate
        reviewBranchParams.params.price_rate = priceRate
        reviewBranchParams.params.space_rate = spaceRate
        reviewBranchParams.params.hygiene_rate = hygieneRate
        reviewBranchParams.params.branch_review_status = type
        reviewBranchParams.params.url = ""
        reviewBranchParams.params.url_json_content.url = ""
        reviewBranchParams.params.url_json_content.title = ""
        reviewBranchParams.params.url_json_content.description = ""
        reviewBranchParams.params.url_json_content.image = ""
        reviewBranchParams.params.url_json_content.canonical_url = ""
        reviewBranchParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createReviewBranch(it)
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
                                Toast.makeText(
                                    this@CreateReviewNewsFeedActivity,
                                    resources.getString(R.string.success_review_restaurant),
                                    Toast.LENGTH_LONG
                                ).show()

                                checkPost = true
                                dataPosted = response.data
                                dataPosted.status_reload = "create"
                                onBackPressed()
                            }
                            else -> {
                                Toast.makeText(
                                    this@CreateReviewNewsFeedActivity,
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
                                Toast.makeText(
                                    this@CreateReviewNewsFeedActivity,
                                    response.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    }
                })
        }
    }

    private fun checkPostReview() {
        if (branchID == 0) {
            AloLineToast.makeText(
                this,
                resources.getString(R.string.choose_restaurant_review),
                AloLineToast.LENGTH_LONG,
                AloLineToast.WARNING
            ).show()
        } else {
            if (binding.edtContent.text.isNotEmpty() && binding.rbCreateNewRate.rating.toDouble() > 0.0) {
                binding.lnUpload.visibility = View.VISIBLE
                if (imageLink.isNotEmpty()) {
                    listPost.clear()
                    urlImageList.clear()
                    for (i in listFile.indices) {
                        ChatUtils.getNameFileFormatTime(listFile[i].fileName)?.let {
                            getLinkReview(
                                it,
                                listFile[i].width,
                                listFile[i].height,
                                listFile[i].original!!,
                            )
                        }
                    }
                } else {
                    onCreateReview(urlImageList, 1)
                }
            } else if (binding.edtContent.text.isEmpty() && binding.rbCreateNewRate.rating.toDouble() > 0.0) {
                AloLineToast.makeText(
                    this,
                    resources.getString(R.string.content_review_restaurant_error),
                    AloLineToast.LENGTH_LONG,
                    AloLineToast.WARNING
                ).show()
            } else if (binding.edtContent.text.isNotEmpty() && binding.rbCreateNewRate.rating.toDouble() == 0.0) {
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
        }

    }

    override fun onCallBack(bundle: Bundle) {
        this.branch = Gson().fromJson(
            bundle.getString(TechresEnum.KEY_CHOOSE_BRANCH.toString()),
            object :
                TypeToken<Branch>() {}.type
        )
        this.removeOnRefreshFragment(this)
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

        val gender = if (user.gender == 1) {
            "anh"
        } else {
            "chị"
        }

        txtCongratulations.text = String.format(
            "%s %s %s %s %s %s %s %s",
            "Tập thể",
            branchName,
            "sẽ rất vui và hạnh phúc khi nhận được những lời đánh giá chân thành từ",
            gender,
            user.name,
            ". Chúng tôi sẽ luôn cố gắng nổ lực để đưa đến",
            gender,
            "dịch vụ tốt nhất và những món ăn ngon nhất."
        )

        txtTks.text = String.format(
            "%s %s %s",
            "Chúc",
            gender,
            "và gia đình nhiều sức khỏe, thành công và hạnh phúc !"
        )


        ratingService.rating = serviceRate
        ratingFood.rating = foodRate
        ratingPrice.rating = priceRate
        ratingSpace.rating = spaceRate
        ratingHygiene.rating = hygieneRate

        when (serviceRate) {
            1f -> {
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
            1f -> {
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
            1f -> {
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
            1f -> {
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
            1f -> {
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
            if (serviceRate == 0f || foodRate == 0f || priceRate == 0f || spaceRate == 0f || hygieneRate == 0f) {
                AloLineToast.makeText(
                    this,
                    resources.getString(R.string.toast_check_rating_review),
                    AloLineToast.LENGTH_LONG,
                    AloLineToast.WARNING
                ).show()
            } else {
                averageRate = ((serviceRate + foodRate + priceRate + spaceRate + hygieneRate) / 5)
                binding.rbCreateNewRate.rating = averageRate
                dialog?.dismiss()
            }
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

        tagReviewAdapter!!.setDataSource(lisTagReview)
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

    override fun onClick(tagReview: String) {
        if (binding.edtContent.text.isNotEmpty()) {
            binding.edtContent.append(String.format("%s%s", ", ", tagReview))
        } else {
            binding.edtContent.append(
                tagReview.lowercase(Locale.getDefault())
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
        }
    }

    override fun onBackPressed() {
        if (checkPost) {
            EventBus.getDefault().post(dataPosted)
            super.onBackPressed()
        } else {
            if (binding.edtContent.text.isNotEmpty() || binding.edtTitle.text.isNotEmpty()) {
                val bottomSheetDialog = BottomSheetDialog(this)
                bottomSheetDialog.setContentView(R.layout.bottom_sheet_call_back_create_post)
                bottomSheetDialog.setCancelable(true)
                val clSave = bottomSheetDialog.findViewById<ConstraintLayout>(R.id.clSave)
                val txtCanclePost = bottomSheetDialog.findViewById<TextView>(R.id.txtCanclePost)
                val txtEditContinue = bottomSheetDialog.findViewById<TextView>(R.id.txtEditContinue)
                clSave!!.setOnClickListener {
//                    onCreateReview(urlImageList, 0)
                    bottomSheetDialog.dismiss()
                }
                clSave.visibility = View.GONE
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

}