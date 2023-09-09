package vn.techres.line.fragment.order


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.BillActivity
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.order.OrderCustomerAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.OrderCustomer
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.OrderCustomerResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentOrdersCustomerBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.ChooseOrderCustomerHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.math.ceil

class OrdersCustomerFragment : BaseBindingFragment<FragmentOrdersCustomerBinding>(FragmentOrdersCustomerBinding::inflate), ChooseOrderCustomerHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var adapter: OrderCustomerAdapter? = null
    private var data = ArrayList<OrderCustomer>()
    private var page = 1
    private var limit = 0
    private var totalRecord = 0
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
        binding.rcOrder.layoutManager = LinearLayoutManager(mainActivity!!, RecyclerView.VERTICAL, false)
        adapter = OrderCustomerAdapter(mainActivity!!)
        adapter?.setChooseOrderCustomerHandler(this)
        binding.rcOrder.adapter = adapter

        setData()
        setEvent()
    }

    private fun setData() {
        if (data.size == 0) {
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerViewContainer.startShimmerAnimation()
            getOrderCustomer(page)
        } else {
            adapter?.setDataSource(data)
        }
    }

    private fun setEvent() {
        binding.swipeRefresh.setOnRefreshListener {
            page = 1
            data.clear()
            getOrderCustomer(page)
            binding.shimmerViewContainer.startShimmerAnimation()
            binding.swipeRefresh.isRefreshing = false

        }

        binding.rcOrder.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (page <= ceil((totalRecord / limit).toDouble())) {
                        page++
                        getOrderCustomer(page)
                    }
                }
            }
        })
    }

    private fun getOrderCustomer(page: Int) {

        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/customers/" + user.id + "/orders?order_statuses=" + resources.getString(R.string.orderCustomerUnConFirm) + "&page=$page" + "&restaurant_id=" + restaurant().restaurant_id!!
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getOrderCustomer(
                baseRequest
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OrderCustomerResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: OrderCustomerResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val dataOrderCustomer = response.data
                        limit = dataOrderCustomer.limit!!
                        totalRecord = dataOrderCustomer.total_record!!

                        if (data.size == 0) {
                            data = dataOrderCustomer.list
                        } else {
                            data.addAll(data.size, dataOrderCustomer.list)
                        }
                        checkOrders(data)
                        adapter?.setDataSource(data)
                        binding.shimmerViewContainer.hide()
                    }
                }
            })
    }

    private fun checkOrders(arrayList: ArrayList<OrderCustomer>) {
        if (arrayList.size == 0) {
            binding.rcOrder.hide()
            binding.llNoItemCart.show()
        } else {
            binding.llNoItemCart.hide()
            binding.rcOrder.show()
        }
    }

    override fun chooseOrder(position: Int) {
        val intent = Intent(mainActivity, BillActivity::class.java)
        intent.putExtra(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(), data[position].id ?: 0)
        startActivity(intent)
        mainActivity!!.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onBackPress() : Boolean{
        return true
    }
}


