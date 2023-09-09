package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.giphy.sdk.analytics.GiphyPingbacks.context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.adapter.booking.DetailBookingAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.booking.Booking
import vn.techres.line.data.model.booking.BookingFood
import vn.techres.line.data.model.params.CancelBookingParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ActivityDetailBookingBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.booking.DetailBookingHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.text.SimpleDateFormat

class DetailBookingActivity : BaseBindingActivity<ActivityDetailBookingBinding>(),
    DetailBookingHandler {
    private var booking = Booking()
    private var adapter: DetailBookingAdapter? = null
    private var configNodeJs = ConfigNodeJs()

    override val bindingInflater: (LayoutInflater) -> ActivityDetailBookingBinding
        get() = ActivityDetailBookingBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this@DetailBookingActivity)
    }

    override fun onSetBodyView() {
        this.window?.statusBarColor = AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.detail_booking)
        binding.rcBookingFood.layoutManager =
            LinearLayoutManager(this@DetailBookingActivity, RecyclerView.VERTICAL, false)
        adapter = DetailBookingAdapter(this@DetailBookingActivity)
        adapter?.setDetailBookingHandler(this@DetailBookingActivity)
        binding.rcBookingFood.adapter = adapter

        booking = Gson().fromJson(
            intent.getStringExtra(TechresEnum.BOOKING.toString()),
            object :
                TypeToken<Booking>() {}.type
        )
        getDetailRestaurant(booking)

        setListener()
    }

    private fun getDetailRestaurant(booking: Booking) {
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.imgLogoBranch.load(
            String.format(
                "%s%s",
                configNodeJs.api_ads,
                booking.branch_picture
            )
        ) {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
            transformations(RoundedCornersTransformation(10F))
        }
        binding.txtNameBranch.text = booking.branch_name
        binding.txtAddressBranch.text = booking.branch_address
        binding.txtStatus.text = booking.booking_status_text
        adapter?.setDataSource(booking.food_request)
        when (booking.booking_status) {
            1 -> {
                binding.txtStatus.setBackgroundResource(R.drawable.corners_blue_4dp)
//                binding.txtStatus.setBackgroundResource(R.drawable.corners_green_4dp)
            }
            2 -> {
                binding.txtStatus.text = resources.getString(R.string.confirmed_booking)
                binding.txtStatus.setBackgroundResource(R.drawable.corners_green_4dp)
                binding.btnCancelBooking.hide()
            }
            3 -> {
                binding.txtStatus.setBackgroundResource(R.drawable.corners_yellow_4dp)
                binding.btnCancelBooking.hide()
            }
            4 -> {
                binding.txtStatus.setBackgroundResource(R.drawable.corners_orange_4dp)
                binding.btnCancelBooking.hide()
                binding.btnCall.hide()
            }
            5 -> {
                binding.txtStatus.setBackgroundResource(R.drawable.corners_red_4dp)
                binding.btnCancelBooking.hide()
                binding.btnCall.hide()
            }
            6 -> {
                binding.txtStatus.setBackgroundResource(R.drawable.corners_purple_4dp)
                binding.btnCancelBooking.hide()
                binding.btnCall.hide()
            }
        }

        binding.txtDateTime.text = setTimeBooking(booking.booking_time)

        binding.txtNumberPeople.text = booking.number_slot.toString()
        binding.txtRequest.text = if (booking.orther_requirements != "") {
            booking.orther_requirements
        } else {
            ""
        }
        if (booking.deposit_amount == 0.00) {
            binding.txtDeposit.text = String.format(
                "%s %s", resources.getString(R.string.zero), getString(R.string.denominations)
            )
        } else {
            binding.txtDeposit.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(booking.deposit_amount.toFloat()),
                resources.getString(R.string.denominations)
            )
        }

        binding.tvTotalPriceBooking.text = getTotalPrice(booking.food_request)

        var food = 0
        booking.food_request.forEach {
            food += (it.quantity ?: 0F).toInt()
        }
        binding.tvQualityFood.text =
            String.format("%s %s", food.toString(), resources.getString(R.string.food_choose))

        cacheManager.put(TechresEnum.BRANCH_ID.toString(), booking.branch_id.toString())
    }

    private fun setListener() {
        binding.imgLogoBranch.setOnClickListener {
//            val bundle = Bundle()
//            bundle.putInt(TechresEnum.BRANCH_ID.toString(), booking.branch_id)
//            mainActivity?.let {
//                Navigation.findNavController(
//                    it,
//                    R.id.nav_host
//                ).navigate(R.id.action_detailBookingFragment_to_detailBranchFragment, bundle)
//            }

        }

        binding.btnCall.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:" + booking.branch_phone)
            startActivity(dialIntent)
        }

        binding.btnCancelBooking.setOnClickListener {
            val dialog = Dialog(this@DetailBookingActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.dialog_cancel_booking)
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            dialog.window!!.attributes = lp

            val btnConfirm = dialog.findViewById(R.id.btnConfirm) as Button
            val btnConfirmHin = dialog.findViewById(R.id.btnConfirmHin) as Button
            val btnBack = dialog.findViewById(R.id.btnBack) as Button
            val edtNoteReasonCancel = dialog.findViewById(R.id.edtNoteReasonCancel) as EditText
            btnBack.setOnClickListener {
                dialog.dismiss()
            }
            val a = edtNoteReasonCancel.text

            edtNoteReasonCancel.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    if (p0!!.isNotEmpty() || StringUtils.isNullOrEmpty(edtNoteReasonCancel.toString())) {
                        btnConfirm.show()
                        btnConfirmHin.hide()
                    } else {
                        btnConfirm.hide()
                        btnConfirmHin.show()
                    }
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })
            btnConfirm.setOnClickListener {
                onCancelBooking(a.toString())
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    private fun getTotalPrice(food: ArrayList<BookingFood>): String {
        var itemPriceCount = 0F
        food.forEach {
            itemPriceCount += it.quantity!!.toFloat() * (it.price)!!.toFloat()
        }
        return String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(itemPriceCount.toString().toFloat()),
            this.baseContext.getString(R.string.denominations)
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun setTimeBooking(date: String?): String {
        if (StringUtils.isNullOrEmpty(date)) {
            return ""
        }
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val newDate = format.parse(date!!)
        val dayFormat = SimpleDateFormat("dd/MM/yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        return if (newDate != null) {
            String.format(
                "Lúc %s ngày %s",
                timeFormat.format(newDate),
                dayFormat.format(newDate)

            )
        } else {
            ""
        }

    }

    private fun onCancelBooking(string: String) {
//        this.setLoading(true)
        val params = CancelBookingParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/bookings/" + booking.id + "/cancel"

        params.params.id = booking.id
        params.params.cancel_reason = string
        params.params.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .cancelBooking(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            Toast.makeText(
                                this@DetailBookingActivity,
                                context.resources.getString(R.string.success_booking_cancel),
                                Toast.LENGTH_LONG
                            ).show()
                            EventBus.getDefault().post(booking)

                            onBackPressed()
                        }
                    }
                })
        }
//        this?.setLoading(false)
    }

    override fun onGift(bookingFood: BookingFood) {
        val dialog = Dialog(this@DetailBookingActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_note_food_bill)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val tvNoteFoodBill = dialog.findViewById(R.id.tvNoteFoodBill) as TextView
        val btnBackBill = dialog.findViewById(R.id.btnBackBill) as Button

        tvNoteFoodBill.text = this.baseContext.getString(R.string.note_gift_bill)

        btnBackBill.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}