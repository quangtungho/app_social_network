package vn.techres.line.activity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.advert.AdvertPackageAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.AdvertResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityAdvertPackageBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.CurrentUser
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

@Suppress("DEPRECATION")
class AdvertPackageActivity : BaseBindingActivity<ActivityAdvertPackageBinding>() {
    private var adapterVideo: AdvertPackageAdapter? = null
    private var adapterBanner: AdvertPackageAdapter? = null
    private var user = User()
    private var configJava = ConfigJava()
    
    override val bindingInflater: (LayoutInflater) -> ActivityAdvertPackageBinding
        get() = ActivityAdvertPackageBinding::inflate

    
    override fun onSetBodyView() {
        configJava = CurrentConfigJava.getConfigJava(this)
        user = CurrentUser.getCurrentUser(this)
        
        binding.recyclerViewVideo.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewBanner.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapterVideo = AdvertPackageAdapter(this)
        adapterBanner = AdvertPackageAdapter(this)
        binding.recyclerViewVideo.adapter = adapterVideo
        binding.recyclerViewBanner.adapter = adapterBanner

        getAdvertVideo()
        getAdvertBanner()

        setListener()
    }

    @SuppressLint("ResourceAsColor")
    private fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnVideo.setOnClickListener {
            binding.recyclerViewVideo.visibility = View.VISIBLE
            binding.recyclerViewBanner.visibility = View.GONE
            binding.btnVideo.setBackgroundResource(R.drawable.border_orange_50dp)
            binding.btnVideo.setTextColor(resources.getColor(R.color.white))
            binding.btnBanner.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnBanner.setTextColor(resources.getColor(R.color.main_bg))
        }

        binding.btnBanner.setOnClickListener {
            binding.recyclerViewVideo.visibility = View.GONE
            binding.recyclerViewBanner.visibility = View.VISIBLE
            binding.btnVideo.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnVideo.setTextColor(resources.getColor(R.color.main_bg))
            binding.btnBanner.setBackgroundResource(R.drawable.border_orange_50dp)
            binding.btnBanner.setTextColor(resources.getColor(R.color.white))
        }
    }

    private fun getAdvertVideo() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/customers/adverts?status=-1&is_running=-1&page=1&limit=100&media_type=0&type=-1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getAdvert(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AdvertResponse> {
                override fun onComplete() {
                   //COMMENT
                }
                override fun onError(e: Throwable) {
//                   COMMENT
                }

                override fun onSubscribe(d: Disposable) {
               //COMMENT
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: AdvertResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        adapterVideo!!.setDataSource(response.data.list)
                    } else Toast.makeText(this@AdvertPackageActivity, response.message, Toast.LENGTH_LONG)
                }
            })
    }

    private fun getAdvertBanner() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/customers/adverts?status=-1&is_running=-1&page=1&limit=100&media_type=1&type=-1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getAdvert(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AdvertResponse> {
                override fun onComplete() {
                  //COMMENT
                }
                override fun onError(e: Throwable) {
                  //COMMENT
                }

                override fun onSubscribe(d: Disposable) {
                  //COMMENT
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: AdvertResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        adapterBanner!!.setDataSource(response.data.list)
                    } else Toast.makeText(this@AdvertPackageActivity, response.message, Toast.LENGTH_LONG)
                }
            })
    }

}