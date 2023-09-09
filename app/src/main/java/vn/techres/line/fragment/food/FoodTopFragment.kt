package vn.techres.line.fragment.food

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.MediaSliderActivity
import vn.techres.line.adapter.home.FoodTopAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.food.reponse.FoodBestSellerResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.FragmentFoodTopBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.ClickOneImage
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class FoodTopFragment : BaseBindingFragment<FragmentFoodTopBinding>(FragmentFoodTopBinding::inflate), ClickOneImage {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var adapterFoodTop: FoodTopAdapter? = null
    private var configJava = ConfigJava()
    private var configNodeJs = ConfigNodeJs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())

    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.food_top)
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        binding.rcFoodTop.layoutManager = GridLayoutManager(requireActivity(), 1)
        adapterFoodTop = FoodTopAdapter(requireActivity())
        binding.rcFoodTop.adapter = adapterFoodTop
        adapterFoodTop?.setClickOneImage(this)
        arguments?.let {
            val branchId = it.getInt(TechresEnum.BRANCH_ID.toString())
            getFoodBestSellerBranch(branchId, 10, 1)
        }
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun getFoodBestSellerBranch(idBranch: Int, limit: Int, page: Int) {
      
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/foods/best-seller?branch_id=$idBranch&limit=$limit&page=$page"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getFoodBestSellerBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FoodBestSellerResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}

          
                @SuppressLint("ShowToast")
                override fun onNext(response: FoodBestSellerResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val foodBestSellerList = response.data
                        adapterFoodTop?.setDataSource(foodBestSellerList)
                    }
   
                }
            })
    }


    override fun onClickImage(url: String, position: Int) {
        lookAtPhoto(url, position)
    }

    private fun lookAtPhoto(url: String, position: Int) {
        val list = ArrayList<String>()
        list.add(String.format("%s%s", configNodeJs.api_ads, url))
        val intent = Intent(mainActivity, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onBackPress() : Boolean{
        return true
    }

}
