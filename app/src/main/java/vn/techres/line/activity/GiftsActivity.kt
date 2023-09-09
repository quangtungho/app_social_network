package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.gift.ReceivingGiftsAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.gift.Gift
import vn.techres.line.data.model.gift.GiftListResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityReceivingGiftsBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.interfaces.gift.GiftHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class GiftsActivity : BaseBindingActivity<ActivityReceivingGiftsBinding>(), GiftHandler {
    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private var receivingGiftsAdapter: ReceivingGiftsAdapter? = null
    private var dataGift = ArrayList<Gift>()

    override val bindingInflater: (LayoutInflater) -> ActivityReceivingGiftsBinding
        get() = ActivityReceivingGiftsBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        receivingGiftsAdapter = ReceivingGiftsAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = receivingGiftsAdapter
        receivingGiftsAdapter!!.setClickGift(this)

        getGifts()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getGifts() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/customer-gifts?restaurant_id=-1&restaurant_brand_id=-1&customer_id=${user.id}&restaurant_gift_id=-1&restaurant_gift_object_value=-1&restaurant_gift_object_quantity=-1&restaurant_gift_type=-1&is_opened=0&is_expired=-1&from&to&key_search"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getGiftList(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GiftListResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {}
                @SuppressLint("ShowToast")
                override fun onNext(response: GiftListResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataGift = response.data.list
                        receivingGiftsAdapter!!.setDataSource(dataGift)
                    }
                }
            })
    }

    private fun takeGift(id: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api/customer-gifts/$id/open"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getGiftList(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GiftListResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {}
                @SuppressLint("ShowToast")
                override fun onNext(response: GiftListResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataGift.removeIf { it.id == id }
                        receivingGiftsAdapter!!.setDataSource(dataGift)
                        dialogGetGiftSuccess()
                        if (dataGift.size == 0){
                            binding.lnEmptyGif.visibility = View.VISIBLE
                        }
                    }
                }
            })
    }

    private fun dialogGetGiftSuccess(){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_get_gift_success)

        val btnExit = dialog.findViewById(R.id.btnExit) as Button
        val btnGoGift = dialog.findViewById(R.id.btnGoGift) as Button

        btnExit.setOnClickListener {
            dialog.dismiss()
        }

        btnGoGift.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, GiftActivity::class.java)
            startActivity(intent)
            this.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onGetGift(gift: Gift) {
        takeGift(gift.id?:0)
    }
}