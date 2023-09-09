package vn.techres.line.fragment.voucher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.VoucherDetailActivity
import vn.techres.line.adapter.voucher.VoucherUsedAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.data.model.voucher.Voucher
import vn.techres.line.data.model.voucher.VoucherResponse
import vn.techres.line.databinding.FragmentVoucherNotUsedBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.VoucherHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class VoucherNotUsedFragment : BaseBindingFragment<FragmentVoucherNotUsedBinding>(
    FragmentVoucherNotUsedBinding::inflate), VoucherHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var adapter: VoucherUsedAdapter? = null
    private var data = ArrayList<Voucher>()
    private var user = User()
    private var configJava = ConfigJava()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(mainActivity!!)
        configJava = CurrentConfigJava.getConfigJava(mainActivity!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.rcVoucher.layoutManager = LinearLayoutManager(mainActivity!!, RecyclerView.VERTICAL, false)
        adapter = VoucherUsedAdapter(mainActivity!!)
        adapter!!.setVoucherHandler(this)
        binding.rcVoucher.adapter = adapter
        setData()
        setEvent()
    }

    private fun setData() {
        if (data.size == 0) {
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerViewContainer.startShimmerAnimation()
            getVoucher()
        } else {
            adapter?.setDataSource(data)
        }
    }

    private fun setEvent() {
        binding.swipeRefresh.setOnRefreshListener {
            data.clear()
            getVoucher()
            binding.shimmerViewContainer.startShimmerAnimation()
            binding.swipeRefresh.isRefreshing = false

        }
    }

    private fun getVoucher() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s",
            "/api/customer-restaurant-vouchers?customer_id=",
            user.id,
            "&restaurant_voucher_id=",
            -1,
            "&restaurant_brand_id=",
            -1,
            "&is_used=",
            0,
            "&is_expired=",
            -1,
            "&promotion_campaign_id=",
            -1,
            "&limit=",
            -1,
            "&page=",
            1,
            "&is_get_customer_restaurant_voucher_different_zezo=",
            1
        )
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getVoucher(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<VoucherResponse> {
                override fun onComplete() {
                    WriteLog.d("COMPLETE", "Complete")}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {
                    WriteLog.d("SUCBSCRIBE", "Subscribe")
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: VoucherResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        data = response.data.list
                        checkVoucher(data)
                        adapter!!.setDataSource(data)
                        binding.shimmerViewContainer.hide()
                    }else {
                        Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }

    private fun checkVoucher(arrayList: ArrayList<Voucher>) {
        if (arrayList.size == 0) {
            binding.rcVoucher.hide()
            binding.llNoItemVoucher.show()
        } else {
            binding.llNoItemVoucher.hide()
            binding.rcVoucher.show()
        }
    }

    override fun onBackPress() : Boolean{
        return true
    }

    override fun onClickVoucher(data: Voucher) {
        val intent = Intent(mainActivity, VoucherDetailActivity::class.java)
        intent.putExtra(TechresEnum.VOUCHER_INFORMATION.toString(), Gson().toJson(data))
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onClickTakeVoucher(data: Voucher) {
        WriteLog.d("onClickTakeVoucher", "ClickTakeVoucher")
    }
}