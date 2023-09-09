package vn.techres.line.activity

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.adapter.point.ListDetailsMemberCardAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.ListDetailsMemberCard
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.ListPointHistoryParams
import vn.techres.line.data.model.response.DetailsMemberCardResponse
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.restaurant.response.RestaurantCardDetailResponse
import vn.techres.line.databinding.ActivityPointCardBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Currency
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.math.ceil

class PointCardActivity : BaseBindingActivity<ActivityPointCardBinding>() {
    private var dataPointHistory = ArrayList<ListDetailsMemberCard>()
    private var listHistoryPoint = ArrayList<ListDetailsMemberCard>()
    private var dataPointCard = RestaurantCard()
    private var adapterDetailsMemberCard: ListDetailsMemberCardAdapter? = null
    private var idCard: Int? = 0
    private var colorText: Int? = 0
    private var page = 1
    private var total = 0
    private var limit = 10
    private var pointAdd: String = ""
    private var pointAccumulated: String = ""
    private var pointPromotion: String = ""
    private var pointValue: String = ""

    override val bindingInflater: (LayoutInflater) -> ActivityPointCardBinding
        get() = ActivityPointCardBinding::inflate

    override fun onSetBodyView() {
        adapterDetailsMemberCard = ListDetailsMemberCardAdapter(this)
        binding.rcvDetailsMemberCard.adapter = adapterDetailsMemberCard

        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        binding.rcvDetailsMemberCard.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        setData()
        setListener()
    }

    private fun setListener() {

        binding.rcvDetailsMemberCard.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(
                rcvDetailsMemberCard: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(rcvDetailsMemberCard, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y > 0) {
                        if (page <= ceil(
                                (total / limit).toString()
                                    .toDouble()
                            )
                        ) {
                            page++
                            getListPointHistory(page)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            dataPointHistory.clear()
            page = 1
            getPointCardRefresh()
            binding.rcvDetailsMemberCard.recycledViewPool.clear()
            adapterDetailsMemberCard?.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setData() {
        Handler(Looper.getMainLooper()).postDelayed({
            dataPointHistory.clear()
            getPointCardRefresh()
        }, 250)
    }

    private fun getListPointHistory(page: Int) {
        val listPointHistoryParams = ListPointHistoryParams()
        listPointHistoryParams.http_method = AppConfig.GET
        listPointHistoryParams.request_url = "/api/membership-cards/$idCard/point-used-histories"

        listPointHistoryParams.params.restaurant_id =
            restaurant().restaurant_id!!.toInt()
        listPointHistoryParams.params.page = page
        listPointHistoryParams.params.limit = limit
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getListMemberCard(
                listPointHistoryParams
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DetailsMemberCardResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: DetailsMemberCardResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        listHistoryPoint = response.data.list
                        total = response.data.total_record!!
                        limit = response.data.limit!!
                        if (dataPointHistory.size == 0) {
                            dataPointHistory = listHistoryPoint
                        } else {
                            dataPointHistory.addAll(dataPointHistory.size, listHistoryPoint)
                        }
                        adapterDetailsMemberCard?.setDataSource(dataPointHistory)

                    } else Toast.makeText(this@PointCardActivity, response.message, Toast.LENGTH_LONG)
                }
            })
    }

    private fun setColorPoint() {
        binding.tvPointNumber.text = Currency.formatCurrencyDecimal(pointAdd.toFloat())
        binding.tvPointAccumulatedNumber.text = Currency.formatCurrencyDecimal(pointAccumulated.toFloat())
        binding.tvPointDiscountNumber.text = Currency.formatCurrencyDecimal(pointPromotion.toFloat())
        binding.tvPointValueNumber.text = Currency.formatCurrencyDecimal(pointValue.toFloat())
    }

    private fun getPointCardRefresh() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/membership-cards/" + restaurant().id!!.toInt()
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getRestaurantCardDetail(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantCardDetailResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: RestaurantCardDetailResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataPointCard = response.data
                        saveRestaurantInfo(this@PointCardActivity, dataPointCard)
                        val restaurant = restaurant()
                        idCard = restaurant.id
                        binding.tvRestaurantName.text = restaurant.restaurant_name
                        binding.tvRestaurantAddressPoint.text = restaurant.restaurant_address
                        pointAdd = restaurant.point.toString()
                        pointAccumulated = restaurant.accumulate_point.toString()
                        pointPromotion = restaurant.promotion_point.toString()
                        pointValue = restaurant.alo_point.toString()
                        if (restaurant.restaurant_membership_card_color_hex_code!!.isNotEmpty()){
                            colorText = Color.parseColor(restaurant.restaurant_membership_card_color_hex_code!!)
                        }

                        setColorPoint()
                        getListPointHistory(page)
                    }
                }
            })
    }

}