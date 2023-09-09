package vn.techres.line.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.ShippingUnitAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.deliveries.BundleDeliveries
import vn.techres.line.data.model.deliveries.ConfigDeliveries
import vn.techres.line.data.model.deliveries.DeliveriesResponse
import vn.techres.line.data.model.eventbus.EventBusUnitDeliveries
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.databinding.FragmentChooseShippingUnitBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentDeliveries
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.ShippingUnitHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class ChooseShippingUnitFragment : BaseBindingFragment<FragmentChooseShippingUnitBinding>(FragmentChooseShippingUnitBinding::inflate), ShippingUnitHandler  {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var adapter: ShippingUnitAdapter? = null
    private var bundleDeliveries = BundleDeliveries()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            bundleDeliveries = Gson().fromJson(
                it.getString(TechresEnum.KEY_DELIVERIES.toString()),
                object :
                    TypeToken<BundleDeliveries>() {}.type
            )
        }

        adapter = ShippingUnitAdapter(mainActivity!!.baseContext)
        binding.recyclerView.layoutManager = LinearLayoutManager(mainActivity!!.baseContext, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = adapter
        adapter!!.setClickShippingUnit(this)

        callAPICustomerSettings(bundleDeliveries.branch_id?:0, bundleDeliveries.lat?:0.0, bundleDeliveries.lng?:0.0)

        binding.imgBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    //Call api shipping unit
    private fun callAPICustomerSettings(branchId: Int, lat: Double, lng: Double) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/third-party-deliveries?branch_id=$branchId&lat=$lat&lng=$lng"

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .callAPICustomerSettings(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DeliveriesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: DeliveriesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        adapter!!.setDataSource(response.data)
                    }
                }
            })
    }

    override fun clickShippingUnit(configDeliveries: ConfigDeliveries) {
        CurrentDeliveries.saveCurrentConfigDeliveries(mainActivity!!.baseContext, configDeliveries)
        EventBus.getDefault().post(EventBusUnitDeliveries(configDeliveries))
        mainActivity?.supportFragmentManager?.popBackStack()
    }

    override fun onBackPress(): Boolean {
        return true
    }

}