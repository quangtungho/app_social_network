package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.adapter.album.ImageFromAlbumAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.album.ClickImageAlbum
import vn.techres.line.data.model.album.EventBus.EventBusImageUpload
import vn.techres.line.data.model.album.ImageFolder
import vn.techres.line.data.model.album.params.ImageAlbumParams
import vn.techres.line.data.model.album.request.ImageAlbumRequest
import vn.techres.line.data.model.album.response.ImageAlbumResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityImageFromAlbumBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlbumUtils
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.util.*

class ImageFromAlbumActivity : BaseBindingActivity<ActivityImageFromAlbumBinding>(),
    ClickImageAlbum {
    var mAdapter: ImageFromAlbumAdapter? = null
    var mList = ArrayList<ImageFolder>()
    var categoryId = ""
    private var user = User()
    private var selectList: List<LocalMedia> = ArrayList()
    private var numberFile = 0
    private var configNodeJs = ConfigNodeJs()
    private var currentPage = 1
    private var resultsPage  = 0
    private var totalPage = 0
    private var isLoadingMore = false
    var isLoading = false
    val LIMIT = 6

    override val bindingInflater: (LayoutInflater) -> ActivityImageFromAlbumBinding
        get() = ActivityImageFromAlbumBinding::inflate

    @SuppressLint("SetTextI18n")
    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        categoryId = intent.getStringExtra("folder_id")?.toString() ?: ""
        binding.title.text = "Album : " + intent.getStringExtra("folder_name")
        binding.rcvImageFolder.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.rcvImageFolder.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        mAdapter = ImageFromAlbumAdapter(this)
        mAdapter!!.setClickImage(this)
        AlbumUtils.configRecyclerViewImage(binding.rcvImageFolder, mAdapter, this)

        setListener()
        getImageAlbum(1, categoryId, user.id)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onImageSuccess(event: EventBusImageUpload) {
        binding.loadingData.visibility = View.GONE
        getImageAlbum(1, categoryId, user.id)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    fun setListener() {
        binding.rcvImageFolder.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                (Objects.requireNonNull(binding.rcvImageFolder.layoutManager) as LinearLayoutManager).findFirstVisibleItemPosition()
                val lastPosition =
                    (Objects.requireNonNull(binding.rcvImageFolder.layoutManager) as LinearLayoutManager).findLastVisibleItemPosition()
                if (!isLoadingMore) {
                    if (!isLoading) {
                        if (lastPosition >= mList.size - 2) {
                            isLoading = true
                            currentPage++
                            if(currentPage == resultsPage) {
                                isLoadingMore = true
                            }
                            getImageAlbumNext(
                                currentPage, categoryId,
                                user.id
                            )
                        }
                    }
                }
            }
        })
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.addImage.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofAll())
                .theme(R.style.picture_WeChat_style)
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .isWithVideoImage(true)
                .maxSelectNum(10)
                .minSelectNum(0)
                .maxVideoSelectNum(5)
                .selectionMode(PictureConfig.MULTIPLE)
                .isSingleDirectReturn(false)
                .isPreviewImage(true)
                .isPreviewVideo(true)
                .isOpenClickSound(true)
                .selectionData(ArrayList())
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PictureConfig.CHOOSE_REQUEST -> {
                selectList = ArrayList()
                numberFile = 0
                selectList = PictureSelector.obtainMultipleResult(data)
                if (selectList.isNotEmpty()) {
                    binding.loadingData.visibility = View.VISIBLE
                    selectList.forEach {
                        val file = File(it.realPath)
                        if (it.mimeType == "video/mp4") {
                            AlbumUtils.uploadFile(
                                file,
                                file.name,
                                configNodeJs,
                                user,
                                this,
                                categoryId,
                                this
                            )
                        } else {
                            AlbumUtils.uploadFile(
                                file,
                                file.name,
                                configNodeJs,
                                user,
                                this,
                                categoryId,
                                this
                            )
                        }
                    }
                }
                numberFile = selectList.size
                PrefUtils.getInstance().putInt("NumberFile",numberFile)
            }
        }
    }

    private fun getImageAlbum(
        page: Int,
        categoryId: String,
        userId: Int
    ) {
        val params = ImageAlbumParams()
        params.project_id = AppConfig.PROJECT_UPLOAD
        params.http_method = AppConfig.GET
        params.request_url = "/api-upload/get-category-album/$categoryId"
        params.params = ImageAlbumRequest(page, userId,LIMIT)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getImageToAlbum(
                params
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ImageAlbumResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: ImageAlbumResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        mList.clear()
                        mList.addAll(response.data.list)
                        mAdapter?.setDataSource(mList)
                        binding.rcvImageFolder.scrollToPosition(0)
                        totalPage = response.data.total_records
                        currentPage = 1
                        isLoadingMore = false
                        isLoading = false
                        resultsPage = if (totalPage % LIMIT == 0) {
                            totalPage / LIMIT
                        } else totalPage / LIMIT + 1
                    }
                }
            })
    }

    private fun getImageAlbumNext(
        page: Int,
        categoryId: String,
        userId: Int
    ) {
        val params = ImageAlbumParams()
        params.project_id = AppConfig.PROJECT_UPLOAD
        params.http_method = AppConfig.GET
        params.request_url = "/api-upload/get-category-album/$categoryId"
        params.params = ImageAlbumRequest(page, userId,LIMIT)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getImageToAlbum(
                params
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ImageAlbumResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: ImageAlbumResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val index = mList.size
                        mList.addAll(response.data.list)
                        mAdapter?.notifyItemRangeInserted(index, response.data.list.size)
                    }
                }
            })
    }

    private fun removeImage(
        categoryId: String,
        imageId: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api-upload/remove-image-album/$categoryId/$imageId"

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .removeImageFromAlbum(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        mList.forEachIndexed { index, imageFolder ->
                            if (imageFolder._id == imageId) {
                                mList.remove(imageFolder)
                                mAdapter?.notifyItemRemoved(index)
                                mAdapter?.notifyItemChanged(index, mAdapter!!.itemCount)
                                return
                            }
                        }
                    }
                }
            })
    }

    override fun clickImageAlbum(link: List<ImageFolder>, position: Int) {
        val url = ArrayList<String>()
        link.forEach {
            url.add(String.format("%s%s", configNodeJs.api_ads, it.link))
        }
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(url))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
    }

    override fun clickImageAlbumMore(imageFolder: ImageFolder, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_option_image_album, popup.menu)

        AlbumUtils.setForceShowIcon(popup)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            if (item.itemId == R.id.action_delete) {
                removeImage(categoryId, imageFolder._id)
            }
            false
        }
        popup.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}