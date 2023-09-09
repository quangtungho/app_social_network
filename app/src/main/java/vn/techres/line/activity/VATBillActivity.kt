package vn.techres.line.activity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.adapter.bill.FoodVatParentAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.bill.FoodVATResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.databinding.ActivityVatBillActivityBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class VATBillActivity : BaseBindingActivity<ActivityVatBillActivityBinding>() {
    private var parentAdapter: FoodVatParentAdapter? = null
    private var orderID = 0
    private var vatAmount = ""


    override val bindingInflater: (LayoutInflater) -> ActivityVatBillActivityBinding
        get() = ActivityVatBillActivityBinding::inflate

    override fun onSetBodyView() {
        intent?.let {
            vatAmount = it.getStringExtra(TechresEnum.FOOD_VAT_AMOUNT.toString())!!.toString()
            orderID = it.getIntExtra(TechresEnum.ORDER_ID.toString(), 0)
        }
        binding.txtTotalAmount.text = vatAmount

        parentAdapter = FoodVatParentAdapter(this)
        binding.recyclerViewParent.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewParent.adapter = parentAdapter

        getListFoodVAT(orderID)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getListFoodVAT(idOrder: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/orders/$idOrder/order-detail-by-vat-percent"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getListFoodVAT(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FoodVATResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: FoodVATResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        parentAdapter!!.setDataSource(response.data)
                    }
                }
            })

    }

}