package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.restaurant.MediaRestaurantFromAlbumAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.album.ImageFolder
import vn.techres.line.data.model.album.response.ImageAlbumResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityMediaRestaurantFromAlbumBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.restaurant.MediaRestaurantAlbumHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.view.GridSpacingDecoration

class MediaRestaurantFromAlbumActivity: BaseBindingActivity<ActivityMediaRestaurantFromAlbumBinding>(),
    MediaRestaurantAlbumHandler {

    private var user = User()
    private var configNodeJs = ConfigNodeJs()
    private var categoryId = ""
    private var adapter: MediaRestaurantFromAlbumAdapter? = null
    private var page = 1
    private var limit = -1

    override val bindingInflater: (LayoutInflater) -> ActivityMediaRestaurantFromAlbumBinding
        get() = ActivityMediaRestaurantFromAlbumBinding::inflate

    @SuppressLint("SetTextI18n")
    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        categoryId = intent.getStringExtra("folder_id")!!.toString()
        binding.title.text = "Album : " + intent.getStringExtra("folder_name")

        binding.rcvImageFolder.layoutManager = GridLayoutManager(this, 3)
        binding.rcvImageFolder.addItemDecoration(GridSpacingDecoration(7, 3))
        adapter = MediaRestaurantFromAlbumAdapter(this)
        adapter!!.setClick(this)
        binding.rcvImageFolder.adapter = adapter

        getImageAlbum(categoryId, page)
        setListener()
    }

    fun setListener() {
//        binding.rcvImageFolder.addOnScrollListener(object :
//            RecyclerView.OnScrollListener() {
//            var y = 0
//            override fun onScrolled(
//                recyclerView: RecyclerView,
//                dx: Int,
//                dy: Int
//            ) {
//                super.onScrolled(recyclerView, dx, dy)
//                y = dy
//            }
//
//            override fun onScrollStateChanged(
//                recyclerView: RecyclerView,
//                newState: Int
//            ) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
//                    if (y > 0) {
//                        if (page <= ceil((total / limit).toString().toDouble())) {
//                            page++
//                            getImageAlbum(category_id, page)
//                        }
//                    } else {
//                        y = 0
//                    }
//                }
//            }
//        })

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getImageAlbum(categoryId: String, page: Int) {
        val params = BaseParams()
        params.project_id = AppConfig.PROJECT_UPLOAD
        params.http_method = AppConfig.GET
        params.request_url = "/api-upload/media/list-image?category_id=${categoryId}&limit=$limit&page=$page"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMediaRestaurant(
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
                        val listMedia = response.data.list
                        if (listMedia.size == 0) {
                            binding.lnEmptyNotify.visibility = View.VISIBLE
                        }
                        adapter!!.setDataSource(listMedia)
                    }
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun clickMediaRestaurantAlbum(data: ArrayList<ImageFolder>, position: Int) {
        val list = ArrayList<String>()
        data.forEach {
            list.add(String.format("%s%s", configNodeJs.api_ads, it.link))
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
}