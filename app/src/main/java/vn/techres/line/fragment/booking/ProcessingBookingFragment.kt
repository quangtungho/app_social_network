package vn.techres.line.fragment.booking

import android.content.Context
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.DetailBookingActivity
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.booking.ProcessBookingAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.booking.Booking
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BookingResponse
import vn.techres.line.databinding.FragmentProcessingBookingBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.ProcessBookingHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.math.ceil

class ProcessingBookingFragment : BaseBindingFragment<FragmentProcessingBookingBinding>(FragmentProcessingBookingBinding::inflate), ProcessBookingHandler{
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var booking = ArrayList<Booking>()
    private var processBookingAdapter: ProcessBookingAdapter? = null
    private var cancelBooking = Booking()
    private var page = 1
    private var total = 0
    private var limit = 10

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)

        processBookingAdapter = ProcessBookingAdapter(mainActivity!!)
        binding.rcProcessing.layoutManager = LinearLayoutManager(mainActivity!!, RecyclerView.VERTICAL,false)
        binding.rcProcessing.adapter = processBookingAdapter
        processBookingAdapter?.setDetailBooking(this)

        setData()
        setEvent()
    }

    private fun setData() {
        if (booking.size == 0) {
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerViewContainer.startShimmerAnimation()
            getListBooking(page)
        } else {
            processBookingAdapter?.setDataSource(booking)
        }
    }

    private fun setEvent() {
        binding.rcProcessing.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y > 0) {
                        if (page <= ceil((total / limit).toString().toDouble())) {
                            page++
                            getListBooking(page)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            page = 1
            booking.clear()
            getListBooking(page)
        }
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun bookingChangeStatus(booking: Booking) {
        this.booking.removeIf { it.id == booking.id }
        processBookingAdapter?.setDataSource(this.booking)
        checkBooking(this.booking)
    }

    private fun checkBooking(arrayList: ArrayList<Booking>) {
        if (arrayList.size == 0) {
            binding.rcProcessing.hide()
            binding.lnEmpty.show()
        } else {
            binding.lnEmpty.hide()
            binding.rcProcessing.show()
        }
    }

    private fun getListBooking(page: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format("%s%s", "/api/bookings?booking_status=1,2,3,7,9&page=", page)
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getListProcessingBooking(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BookingResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BookingResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        binding.swipeRefresh.isRefreshing = false
                        val data = response.data!!.list
                        total = response.data!!.total_record!!
                        limit = response.data!!.limit!!
                        if (booking.size == 0) {
                            booking = data
                        } else {
                            booking.addAll(booking.size, data)
                        }
                        checkBooking(booking)
                        processBookingAdapter?.setDataSource(booking)
                        binding.shimmerViewContainer.visibility = View.GONE
                        binding.shimmerViewContainer.hide()
                    } else
                        context?.let {
                            Toast.makeText(
                                it,
                                it.resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                }
            })
    }

    override fun onDetailBooking(position: Int) {
        val intent = Intent(mainActivity, DetailBookingActivity::class.java)
        intent.putExtra(TechresEnum.BOOKING.toString(), Gson().toJson(booking[position]))
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