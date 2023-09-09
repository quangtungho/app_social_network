package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.glxn.qrgen.android.QRCode
import vn.techres.line.R
import vn.techres.line.adapter.gift.GiftBranchAdapter
import vn.techres.line.adapter.gift.GiftFoodAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.gift.GiftDetail
import vn.techres.line.data.model.gift.GiftDetailResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ActivityGiftDetailBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class GiftDetailActivity : BaseBindingActivity<ActivityGiftDetailBinding>(){
    override val bindingInflater: (LayoutInflater) -> ActivityGiftDetailBinding
        get() = ActivityGiftDetailBinding::inflate
    private var dataGift = GiftDetail()
    private var data = GiftDetail()
    private var configNodeJs = ConfigNodeJs()
    private var adapterBranch: GiftBranchAdapter? = null
    private var adapterFood: GiftFoodAdapter? = null


    override fun onSetBodyView() {
        binding.rlLoading.visibility = View.VISIBLE
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        adapterBranch = GiftBranchAdapter(this)
        binding.rvBranch.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rvBranch.adapter = adapterBranch

        adapterFood = GiftFoodAdapter(this)
        binding.rvFoods.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rvFoods.adapter = adapterFood

        intent?.let {
            dataGift = Gson().fromJson(it.getStringExtra(TechresEnum.DATA_GIFT_DETAIL.toString()), object :
                TypeToken<GiftDetail>() {}.type)
        }

        getGiftDetail(dataGift.id?:0)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgBarcode.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_show_qr)
            val imgVoucher = dialog.findViewById(R.id.imgVoucher) as ImageView
            val imgBtnClose = dialog.findViewById(R.id.imgBtnClose) as ImageView

            try {
                val bitmap =
                    QRCode.from(data.code).withSize(200, 200)
                        .withCharset("UTF-8").bitmap()
                imgVoucher.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            imgBtnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }
    }

    private fun getGiftDetail(id: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/customer-gifts/$id"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getGiftDetail(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GiftDetailResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: GiftDetailResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        data = response.data
                        try {
                            val bitmap =
                                QRCode.from(data.code).withSize(200, 200)
                                    .withCharset("UTF-8").bitmap()
                            binding.imgBarcode.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        Utils.showImage(binding.imgBanner, String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            data.restaurant_gift_banner_url
                        ))

                        if (data.food.size > 0){
                            binding.lnFoods.visibility = View.VISIBLE
                        }
                        adapterBranch!!.setDataSource(data.branches)
                        adapterFood!!.setDataSource(data.food)

                        binding.txtName.text = data.name?:""
                        binding.txtExpiry.text = String.format("%s %s","Hạn sử dụng:", data.expire_at)
//                        val dates = data.restaurant_promotion.to_date!!.substring(0,2).toInt() - data.restaurant_promotion.from_date!!.substring(0,2).toInt() + 1
                        val dates = 7
                        binding.txtExpiryDay.text = String.format("%s %s %s","Hết hạn sử dụng sau",dates.toString(), "ngày")
                        binding.txtContent.text = Html.fromHtml(data.restaurant_gift_content, Html.FROM_HTML_MODE_COMPACT)
                        binding.txtTutorial.text = Html.fromHtml(data.restaurant_gift_use_guide, Html.FROM_HTML_MODE_COMPACT)
                        binding.txtRules.text = Html.fromHtml(data.restaurant_gift_term, Html.FROM_HTML_MODE_COMPACT)
                        binding.rlLoading.visibility = View.GONE

                        binding.webViewTutorial.loadDataWithBaseURL("", data.restaurant_gift_use_guide?:"", "text/html", "utf-8", "")
                        binding.webViewRules.loadDataWithBaseURL("", data.restaurant_gift_term?:"", "text/html", "utf-8", "")
                    }else {
                        Toast.makeText(this@GiftDetailActivity, response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }

}