package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import androidx.recyclerview.widget.SimpleItemAnimator
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.restaurant.CategoryMediaRestaurantAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.CategoryMediaRestaurant
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.CategoryMediaRestaurantResponse
import vn.techres.line.databinding.ActivityRestaurantImageBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlbumUtils
import vn.techres.line.interfaces.restaurant.CategoryMediaRestaurantHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

class RestaurantImageActivity : BaseBindingActivity<ActivityRestaurantImageBinding>(),
    CategoryMediaRestaurantHandler {
    var mAdapter: CategoryMediaRestaurantAdapter? = null
    var mList = ArrayList<CategoryMediaRestaurant>()
    var branchId = 0
    var page = 1


    override val bindingInflater: (LayoutInflater) -> ActivityRestaurantImageBinding
        get() = ActivityRestaurantImageBinding::inflate

    override fun onSetBodyView() {

        branchId = intent.getIntExtra(TechresEnum.BRANCH_ID.toString(), 0)

        binding.folderAlbum.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.folderAlbum.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        mAdapter = CategoryMediaRestaurantAdapter(this)
        mAdapter!!.setClickCategoryMediaRestaurant(this)
        this.let { AlbumUtils.configRecyclerViewFolder(binding.folderAlbum, mAdapter, it) }
        setListener()
        getFolder()
    }

    fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getFolder() {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api-upload/media/restaurant-category?restaurant_id=${restaurant().restaurant_id}&branch_id=${branchId}&limit=-1&page=1"

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getCategoryMediaRestaurant(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CategoryMediaRestaurantResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: CategoryMediaRestaurantResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        mList.clear()
                        mList.addAll(response.data.list)
                        mAdapter?.setDataSource(mList)
                        binding.folderAlbum.scrollToPosition(0)
                    }
                }
            })
    }

    override fun clickCategoryMediaRestaurant(categoryMediaRestaurant: CategoryMediaRestaurant) {
        val intent = Intent(this, MediaRestaurantFromAlbumActivity::class.java)
        intent.putExtra("folder_id", categoryMediaRestaurant._id)
        intent.putExtra("folder_name", categoryMediaRestaurant.folder_name)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

}