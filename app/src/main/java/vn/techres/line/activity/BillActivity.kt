package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import vn.techres.line.R
import vn.techres.line.adapter.bill.BillAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.OrderDetailBill
import vn.techres.line.data.model.RealtimeBillOrder
import vn.techres.line.data.model.bill.BillTable
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.UsingPointToOrderParams
import vn.techres.line.data.model.response.BillTableResponse
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.restaurant.response.RestaurantCardDetailResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityBillBinding
import vn.techres.line.fragment.food.ChooseFoodRewardPointsFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Currency
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.bill.BillHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

class BillActivity : BaseBindingActivity<ActivityBillBinding>(), BillHandler {
    //point
    private var point = 0
    private var membershipPointUsed = 0
    private var maximumPointAllowUseInEachBill = 0

    //accumulate
    private var accumulatePoint = 0
    private var membershipAccumulatePointUsed = 0
    private var maximumAccumulatePointAllowUseInEachBill = 0

    //promotion
    private var promotionPoint = 0
    private var membershipPromotionPointUsed = 0
    private var maximumPromotionPointAllowUseInEachBill = 0

    //value
    private var valuePoint = 0
    private var membershipAloPointUsed = 0
    private var maximumAloPointAllowUseInEachBill = 0

    //data
    private var idOrder: Int? = 0
    private var restaurantCard = RestaurantCard()
    private var adapterBill: BillAdapter? = null
    private var listBill = ArrayList<OrderDetailBill>()
    private var data = BillTable()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()
    private var mSocket: Socket? = null
    private var keyRealtime: String = ""
    private var techResApplicationNodeJs = TechResApplicationNodeJs()

    override val bindingInflater: (LayoutInflater) -> ActivityBillBinding
        get() = ActivityBillBinding::inflate

    override fun onSetBodyView() {
        adapterBill = BillAdapter(this)
        adapterBill?.setBillHandler(this)
        binding.rclBill.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rclBill.adapter = adapterBill
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.bill_pay)
        if (mSocket?.connected() == false) {
            mSocket =
                techResApplicationNodeJs.getSocketInstance(TechResApplication.applicationContext())
            mSocket?.connect()
            mSocket?.emit("join_room", keyRealtime)
        } else {
            mSocket?.emit("join_room", keyRealtime)
        }

        idOrder = intent.getIntExtra(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(), 0)
        getPointCard(idOrder!!)
        setListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        configJava = CurrentConfigJava.getConfigJava(this)
        mSocket = techResApplicationNodeJs.getSocketInstance(this)

        keyRealtime = String.format(
            "%s/%s/%s",
            "customers",
            user.id.toString(),
            "orders"
        )
        setSocketEmit()
    }

    override fun onPause() {
        super.onPause()
        unRegisterSocket()
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegisterSocket()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            getPointCard(idOrder!!)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.lnOrderPoint.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(TechresEnum.KEY_ID_BRANCH_POINT.toString(), data.branch_id!!)
            bundle.putString(TechresEnum.ORDER_ID.toString(), data.id!!.toString())
            bundle.putInt(
                TechresEnum.POINT_ORDER.toString(),
                ((point - membershipPointUsed) + (accumulatePoint - membershipAccumulatePointUsed))
            )
            val isTakeAway = -1
            cacheManager.put(
                TechresEnum.KEY_CHECK_CHOOSE_FOOD_POINT.toString(),
                this.resources.getString(R.string.bill_fragment)
            )
            cacheManager.put(TechresEnum.IS_TAKE_AWAY.toString(), isTakeAway.toString())
            val chooseFoodRewardPointsFragment = ChooseFoodRewardPointsFragment()
            chooseFoodRewardPointsFragment.arguments = bundle
            this.addOnceFragment(ChooseFoodRewardPointsFragment(), chooseFoodRewardPointsFragment)
        }

        //Use point to order
        binding.footer.footerPayment.btnUsePoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.POINT.toString(), "ok")
        }
        binding.footer.footerPayment.btnUseCumulativePoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.ACCUMULATE.toString(), "ok")
        }

        binding.footer.footerPayment.btnUsePromotionPoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.PROMOTION.toString(), "ok")
        }

        binding.footer.footerPayment.btnUseValuePoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.VALUE.toString(), "ok")
        }

        //Cancel point to order
        binding.footer.footerPayment.btnCancelPoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.CANCEL_POINT.toString(), "cancel")
        }
        binding.footer.footerPayment.btnCancelAccumulativePoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.CANCEL_ACCUMULATE.toString(), "cancel")
        }

        binding.footer.footerPayment.btnCancelPromotionPoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.CANCEL_PROMOTION.toString(), "cancel")
        }

        binding.footer.footerPayment.btnCancelValuePoint.setOnClickListener {
            dialogQuestionBill(TechresEnum.CANCEL_VALUE.toString(), "cancel")
        }
    }

    private fun unRegisterSocket() {
        mSocket?.off(keyRealtime)
        mSocket?.emit("leave_room", keyRealtime)
        mSocket?.disconnect()
    }

    private fun dialogQuestionBill(typePoint: String, typeUsing: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_using_point_bill)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val edNoteBill = dialog.findViewById(R.id.tvTwo) as TextView
        val btnUsingPointBill = dialog.findViewById(R.id.btnUsingPointBill) as Button
        val btnBackBill = dialog.findViewById(R.id.btnBackBill) as Button

        if (typeUsing == "ok") {
            edNoteBill.text = this.resources.getString(R.string.using_point_notification)
            btnUsingPointBill.text = this.resources.getString(R.string.using)
        } else {
            edNoteBill.text = this.resources.getString(R.string.cancel_using_point_notification)
            btnUsingPointBill.text = this.resources.getString(R.string.cancel_using_point)
        }

        btnBackBill.setOnClickListener {
            dialog.dismiss()
        }

        btnUsingPointBill.setOnClickListener {
            // using point
            when (typePoint) {
                TechresEnum.POINT.toString() -> {
                    if ((membershipAccumulatePointUsed + membershipPromotionPointUsed) > 0) {
                        val differencePoint =
                            maximumPointAllowUseInEachBill - (membershipAccumulatePointUsed + membershipPromotionPointUsed)
                        if (differencePoint <= point) {
                            membershipPointUsed = differencePoint
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        } else {
                            membershipPointUsed = point
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        }
                    } else {
                        if (point <= maximumPointAllowUseInEachBill) {
                            membershipPointUsed = point
                            usingPointToOrder(
                                data.id!!,
                                point,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        } else {
                            usingPointToOrder(
                                data.id!!,
                                maximumPointAllowUseInEachBill,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                            membershipPointUsed = maximumPointAllowUseInEachBill
                        }
                    }
                }
                TechresEnum.ACCUMULATE.toString() -> {
                    if ((membershipPointUsed + membershipPromotionPointUsed) > 0) {
                        val differencePoint =
                            maximumAccumulatePointAllowUseInEachBill - (membershipPointUsed + membershipPromotionPointUsed)
                        if (differencePoint <= accumulatePoint) {
                            membershipAccumulatePointUsed = differencePoint
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        } else {
                            membershipAccumulatePointUsed = accumulatePoint
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        }
                    } else {
                        if (accumulatePoint <= maximumAccumulatePointAllowUseInEachBill) {
                            membershipAccumulatePointUsed = accumulatePoint
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        } else {
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                maximumAccumulatePointAllowUseInEachBill,
                                membershipPromotionPointUsed,
                                0
                            )
                            membershipAccumulatePointUsed = maximumAccumulatePointAllowUseInEachBill
                        }
                    }

                }
                TechresEnum.PROMOTION.toString() -> {
                    if ((membershipPointUsed + membershipAccumulatePointUsed) > 0) {
                        val differencePromotion =
                            maximumAccumulatePointAllowUseInEachBill - (membershipPointUsed + membershipAccumulatePointUsed)
                        if (differencePromotion <= promotionPoint) {
                            membershipPromotionPointUsed =
                                if (differencePromotion <= maximumPromotionPointAllowUseInEachBill) {
                                    differencePromotion
                                } else {
                                    maximumPromotionPointAllowUseInEachBill
                                }
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        } else {
                            membershipAccumulatePointUsed = promotionPoint
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        }
                    } else {
                        if (promotionPoint <= maximumPromotionPointAllowUseInEachBill) {
                            membershipPromotionPointUsed = promotionPoint
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                membershipPromotionPointUsed,
                                0
                            )
                        } else {
                            usingPointToOrder(
                                data.id!!,
                                membershipPointUsed,
                                membershipAccumulatePointUsed,
                                maximumPromotionPointAllowUseInEachBill,
                                0
                            )
                            membershipPromotionPointUsed = maximumPromotionPointAllowUseInEachBill
                        }
                    }
                }
                TechresEnum.VALUE.toString() -> {
                    if (valuePoint <= maximumAloPointAllowUseInEachBill) {
                        usingPointToOrder(
                            data.id!!,
                            0,
                            0,
                            0,
                            valuePoint
                        )
                    } else {
                        usingPointToOrder(
                            data.id!!,
                            0,
                            0,
                            0,
                            maximumAloPointAllowUseInEachBill
                        )
                    }
                }
                TechresEnum.CANCEL_POINT.toString() -> {
                    usingPointToOrder(
                        data.id!!,
                        0,
                        membershipAccumulatePointUsed,
                        membershipPromotionPointUsed,
                        0
                    )
                }
                TechresEnum.CANCEL_ACCUMULATE.toString() -> {
                    usingPointToOrder(
                        data.id!!,
                        membershipPointUsed,
                        0,
                        membershipPromotionPointUsed,
                        0
                    )
                }
                TechresEnum.CANCEL_PROMOTION.toString() -> {
                    usingPointToOrder(
                        data.id!!,
                        membershipPointUsed,
                        membershipAccumulatePointUsed,
                        0,
                        0
                    )
                }
                TechresEnum.CANCEL_VALUE.toString() -> {
                    usingPointToOrder(
                        data.id!!,
                        0,
                        0,
                        0,
                        0
                    )
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun usingPointToOrder(
        id: Int,
        pointValues: Int,
        accumulatePoint: Int,
        promotionPointValues: Int,
        aloPointValues: Int
    ) {

        val usingPointToOrderParams = UsingPointToOrderParams()
        usingPointToOrderParams.http_method = AppConfig.POST
        usingPointToOrderParams.request_url = "/api/orders/$id/use-point"

        usingPointToOrderParams.params.point = pointValues
        usingPointToOrderParams.params.accumulate_point = accumulatePoint
        usingPointToOrderParams.params.promotion_point = promotionPointValues
        usingPointToOrderParams.params.alo_point = aloPointValues
        setLoading(true)

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).usingPointToOrder(usingPointToOrderParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BillTableResponse> {
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    setLoading(false)
                }


                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }

                override fun onNext(response: BillTableResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        getBillTable(idOrder!!, restaurantCard)
                    } else Toast.makeText(
                        this@BillActivity,
                        this@BillActivity.getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    this@BillActivity.setLoading(false)
                }
            })
    }

    private fun setSocketEmit() {
        mSocket?.on(keyRealtime) {
            this.runOnUiThread {
                WriteLog.d("key_realtime", it[0].toString())
                Gson().fromJson<RealtimeBillOrder>(it[0].toString(), object :
                    TypeToken<RealtimeBillOrder>() {}.type)
                getPointCard(idOrder ?: 0)
            }
        }
    }

    private fun getPointCard(idOrder: Int) {
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
                override fun onComplete() {
                    //Complete
                }
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    this@BillActivity.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: RestaurantCardDetailResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        restaurantCard = response.data
                        getBillTable(idOrder, restaurantCard)
                        saveRestaurantInfo(this@BillActivity, restaurantCard)
                    }
                    this@BillActivity.setLoading(false)
                }
            })

    }

    private fun getBillTable(idTable: Int, restaurantCard: RestaurantCard) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/orders/$idTable"

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getBillTable(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BillTableResponse> {
                override fun onComplete() {
                    //complete
                }
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {
                    //Subscribe
                }
                override fun onNext(response: BillTableResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        data = response.data
                        listBill = data.order_details
                        if (data.order_detail_points.isNotEmpty()) {
                            listBill.addAll(data.order_detail_points)
                        }
                        setDataBill(data, restaurantCard)
                        setVisibilityLayout(data)
                        adapterBill?.setDataSource(listBill)
                    } else Toast.makeText(
                        this@BillActivity,
                        getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()
                    setLoading(false)
                }
            })


    }

    private fun setDataBill(data: BillTable, restaurantCard: RestaurantCard) {
        binding.footer.tvPointPurchase.text = String.format(
            "%s %s",
            data.membership_point_added.toString(),
            this.resources.getString(R.string.point_mini)
        )
        binding.headerPayment.tvReceiptTable.text = data.table_name.toString()
        binding.headerPayment.tvReceiptOrderNumber.text = data.id.toString()
        binding.headerPayment.tvReceiptDate.text = data.payment_day.toString()
        binding.headerPayment.tvReceiptEmployee.text = data.employee_name.toString()
        binding.headerPayment.tvCustomerName.text = data.customer_name
        binding.headerPayment.tvCustomerPhoneNumber.text = data.customer_phone

        binding.footer.tvAmount.text = String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(data.amount!!.toFloat()),
            this.resources.getString(R.string.denominations)
        )

        binding.footer.tvDiscount.text = String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(data.discount_amount!!.toFloat()),
            this.resources.getString(R.string.denominations)
        )
        binding.footer.tvPercentDiscount.text =
            String.format("(%s%s", data.discount_percent.toString(), "%):")
        if ((data.discount_percent ?: 0.0) > 0) {
            binding.footer.tvStatusNotePromotionBill.show()
        } else {
            binding.footer.tvStatusNotePromotionBill.hide()
        }
        binding.footer.tvPercentVat.text = String.format("(%s%s", data.vat.toString(), "%):")
        binding.footer.tvVat.text = String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(data.vat_amount!!.toFloat()),
            this.resources.getString(R.string.denominations)
        )

        binding.footer.tvTotalAmount.text = String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(data.total_final_amount!!.toFloat()),
            this.resources.getString(R.string.denominations)
        )

        maximumPointAllowUseInEachBill = data.maximum_point_allow_use_in_each_bill!!
        maximumAccumulatePointAllowUseInEachBill =
            data.maximum_accumulate_point_allow_use_in_each_bill!!
        maximumAloPointAllowUseInEachBill = data.maximum_alo_point_allow_use_in_each_bill!!
        maximumPromotionPointAllowUseInEachBill =
            data.maximum_promotion_point_allow_use_in_each_bill!!

        membershipPointUsed = data.membership_point_used!!
        membershipAccumulatePointUsed = data.membership_accumulate_point_used!!
        membershipPromotionPointUsed = data.membership_promotion_point_used!!
        membershipAloPointUsed = data.membership_alo_point_used!!
        binding.footer.tvTotalPointUsed.text = String.format(
            "%s %s",
            data.membership_total_point_used,
            this.resources.getString(R.string.point_mini)
        )

        // point
        point = restaurantCard.point!!
        setPoint(
            data.membership_point_used!!,
            point,
            binding.footer.footerPayment.tvPoint
        )

        // accumulate point
        accumulatePoint = restaurantCard.accumulate_point!!
        setPoint(
            data.membership_accumulate_point_used!!,
            accumulatePoint,
            binding.footer.footerPayment.tvAccumulatePoint
        )

        // promotion point
        promotionPoint = restaurantCard.promotion_point!!
        setPoint(
            data.membership_promotion_point_used!!,
            promotionPoint,
            binding.footer.footerPayment.tvPromotionPoint
        )
        binding.footer.footerPayment.tvNotePromotionPoint.text = String.format(
            "%s %s%s %s",
            this.resources.getString(R.string.using),
            restaurantCard.maximum_percent_order_amount_to_promotion_point_allow_use_in_each_bill,
            this.resources.getString(R.string.percent),
            this.resources.getString(R.string.note_promotion_point)
        )
        // alo point
        valuePoint = restaurantCard.alo_point!!
        setPoint(
            data.membership_alo_point_used!!,
            valuePoint,
            binding.footer.footerPayment.tvValuePoint
        )
        if (data.maximum_alo_point_allow_use_in_each_bill!! > 0) {
            binding.footer.footerPayment.tvNoteAloPoint.text = String.format(
                "%s %s%s %s %s %s",
                this.resources.getString(R.string.note_alo_point_1),
                restaurantCard.maximum_percent_order_amount_to_alo_point_allow_use_in_each_bill,
                this.resources.getString(R.string.percent),
                this.resources.getString(R.string.note_alo_point_2),
                Currency.formatCurrencyDecimal(restaurantCard.maximum_money_by_alo_point_allow_use_in_each_bill),
                this.resources.getString(R.string.denominations)
            )
        } else {
            binding.footer.footerPayment.tvNoteAloPoint.text = String.format(
                "%s %s", restaurantCard.restaurant_name,
                this.resources.getString(R.string.note_not_yet_supported_use_point_value)
            )
        }

        binding.footer.footerPayment.tvNoteAloPoint.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.red
            )
        )

    }

    private fun setPoint(
        pointUsing: Int,
        point: Int,
        textView: TextView
    ) {
        if (pointUsing > 0) {
            if (point >= pointUsing) {
                textView.text = Currency.formatCurrencyDecimal((point - pointUsing).toFloat())
            }
        } else {
            textView.text = Currency.formatCurrencyDecimal(point.toFloat())
        }
    }

    private fun setVisibilityLayout(data: BillTable) {
        if (data.order_status != 2) {
            binding.headerPayment.imgPayment.hide()
            binding.footer.tvThankYou.show()
            binding.footer.tvThankYou.show()
            when (data.order_status) {
                1, 4, 5 -> {
                    binding.lnOrderPoint.hide()
                }
                else -> {
                    binding.lnOrderPoint.show()
                }
            }
            if (data.is_allow_use_point == 1) {
                binding.footer.footerPayment.lnUsePoint.show()
                binding.footer.footerPayment.lnUseAccumulatePoint.show()
                binding.footer.footerPayment.lnUsePromotionPoint.show()
                binding.footer.footerPayment.lnUseValuePoint.show()
                setVisibilityButton(data)
            } else {
                binding.footer.footerPayment.lnUsePoint.hide()
                binding.footer.footerPayment.lnUseAccumulatePoint.hide()
                binding.footer.footerPayment.lnUsePromotionPoint.hide()
                binding.footer.footerPayment.lnUseValuePoint.hide()
            }
        } else {
            binding.footer.footerPayment.lnUsePoint.hide()
            binding.footer.footerPayment.lnUseAccumulatePoint.hide()
            binding.footer.footerPayment.lnUsePromotionPoint.hide()
            binding.footer.footerPayment.lnUseValuePoint.hide()
            binding.headerPayment.imgPayment.show()
            binding.footer.tvThankYou.show()
            binding.lnOrderPoint.hide()
            binding.footer.tvThankYou.hide()
        }
        // Tạm thời đóng gọi món bằng điểm
        binding.lnOrderPoint.hide()
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.rlLoading.show()
        } else if (!isLoading) {
            run {
                binding.loading.rlLoading.hide()
            }
        }
    }

    fun addOnceFragment(fromFragment: Fragment, toFragment: Fragment) {
        val isInBackStack =
            supportFragmentManager.findFragmentByTag(toFragment.javaClass.simpleName)
        if (isInBackStack == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.translate_from_right,
                    R.anim.translate_to_left,
                    R.anim.translate_from_left,
                    R.anim.translate_to_right
                )
                .add(R.id.frameContainer, toFragment, toFragment.javaClass.simpleName)
                .hide(fromFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
        }
    }

    private fun setVisibilityButton(billTable: BillTable) {
        //point
        if (point > 0 && billTable.maximum_point_allow_use_in_each_bill!! > 0) {
            if (billTable.membership_point_used!! > 0) {
                binding.footer.footerPayment.lnUsePoint.show()
                binding.footer.footerPayment.imgCheckUsePoint.show()
                binding.footer.footerPayment.btnUseValuePoint.hide()
                binding.footer.footerPayment.btnUsePromotionPoint.show()
                binding.footer.footerPayment.btnUsePoint.hide()
                if (data.order_status == 4) {
                    binding.footer.footerPayment.btnCancelPoint.hide()
                } else {
                    binding.footer.footerPayment.btnCancelPoint.show()
                }
            } else {
                if ((billTable.membership_accumulate_point_used
                        ?: (0 + (billTable.membership_promotion_point_used
                            ?: 0))) < (billTable.maximum_point_allow_use_in_each_bill ?: 0)
                ) {
                    binding.footer.footerPayment.lnUsePoint.show()
                    binding.footer.footerPayment.imgCheckUsePoint.hide()
                    binding.footer.footerPayment.btnUsePoint.show()
                    binding.footer.footerPayment.btnCancelPoint.hide()
                } else {
                    binding.footer.footerPayment.lnUsePoint.hide()
                }
            }
        } else {
            binding.footer.footerPayment.lnUsePoint.hide()
        }
        //accumulate
        if (accumulatePoint > 0 && (billTable.maximum_accumulate_point_allow_use_in_each_bill
                ?: 0) > 0
        ) {
            if ((billTable.membership_accumulate_point_used ?: 0) > 0) {
                binding.footer.footerPayment.lnUseAccumulatePoint.show()
                binding.footer.footerPayment.imgCheckUseCumulativePoint.show()
                binding.footer.footerPayment.btnUseValuePoint.hide()
                binding.footer.footerPayment.btnUsePromotionPoint.show()
                binding.footer.footerPayment.btnUseCumulativePoint.hide()
                if (data.is_allow_use_point == 0) {
                    binding.footer.footerPayment.btnCancelAccumulativePoint.hide()
                } else {
                    binding.footer.footerPayment.btnCancelAccumulativePoint.show()
                }
            } else {
                if ((billTable.membership_promotion_point_used!! + billTable.membership_point_used!!) < billTable.maximum_point_allow_use_in_each_bill!!) {
                    binding.footer.footerPayment.lnUseAccumulatePoint.show()
                    binding.footer.footerPayment.imgCheckUseCumulativePoint.hide()
                    binding.footer.footerPayment.btnUseCumulativePoint.show()
                    binding.footer.footerPayment.btnCancelAccumulativePoint.hide()
                } else {
                    binding.footer.footerPayment.lnUseAccumulatePoint.hide()
                }
            }
        } else {
            binding.footer.footerPayment.lnUseAccumulatePoint.hide()
        }
        //promotion
        if (promotionPoint > 0 && billTable.maximum_promotion_point_allow_use_in_each_bill!! > 0) {
            if (billTable.membership_promotion_point_used!! > 0) {
                binding.footer.footerPayment.lnUsePromotionPoint.show()
                binding.footer.footerPayment.imgCheckUsePromotionPoint.show()
                binding.footer.footerPayment.btnCancelPromotionPoint.show()
                binding.footer.footerPayment.btnUseValuePoint.hide()
                binding.footer.footerPayment.btnUsePromotionPoint.hide()
                if (data.is_allow_use_point == 0) {
                    binding.footer.footerPayment.btnCancelPromotionPoint.hide()
                } else {
                    binding.footer.footerPayment.btnCancelPromotionPoint.show()
                }
            } else {
                if ((billTable.membership_point_used!! + billTable.membership_accumulate_point_used!!) < (billTable.maximum_point_allow_use_in_each_bill
                        ?: 0)
                ) {
                    binding.footer.footerPayment.lnUsePromotionPoint.show()
                    binding.footer.footerPayment.imgCheckUsePromotionPoint.hide()
                    binding.footer.footerPayment.btnUsePromotionPoint.show()
                    binding.footer.footerPayment.btnCancelPromotionPoint.hide()
                } else {
                    binding.footer.footerPayment.lnUsePromotionPoint.hide()
                }
            }
        } else {
            binding.footer.footerPayment.lnUsePromotionPoint.hide()
        }
        //alo point
        if (billTable.maximum_alo_point_allow_use_in_each_bill!! > 0) {
            if (valuePoint > 0) {
                if (billTable.membership_alo_point_used!! > 0) {
                    binding.footer.footerPayment.lnUseValuePoint.show()
                    binding.footer.footerPayment.imgCheckUseValuePoint.show()
                    binding.footer.footerPayment.btnUseValuePoint.hide()
                    binding.footer.footerPayment.btnUsePromotionPoint.hide()
                    binding.footer.footerPayment.btnUseCumulativePoint.hide()
                    binding.footer.footerPayment.btnUsePoint.hide()
                    if (data.is_allow_use_point == 0) {
                        binding.footer.footerPayment.btnCancelValuePoint.hide()
                    } else {
                        binding.footer.footerPayment.btnCancelValuePoint.show()
                    }
                } else {
                    if (billTable.membership_promotion_point_used!! > 0 || billTable.membership_point_used!! > 0 || billTable.membership_accumulate_point_used!! > 0) {
                        binding.footer.footerPayment.lnUseValuePoint.hide()
                    } else {
                        binding.footer.footerPayment.lnUseValuePoint.show()
                        binding.footer.footerPayment.imgCheckUseValuePoint.hide()
                        binding.footer.footerPayment.btnUseValuePoint.show()
                        binding.footer.footerPayment.btnCancelValuePoint.hide()
                    }
                }
            } else {
                binding.footer.footerPayment.lnUseValuePoint.hide()
            }
        } else {
            binding.footer.footerPayment.lnUseValuePoint.hide()
        }
    }

    override fun onGiftFood(food: OrderDetailBill) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_note_food_bill)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window?.attributes = lp
        val tvNoteFoodBill = dialog.findViewById(R.id.tvNoteFoodBill) as TextView
        val btnBackBill = dialog.findViewById(R.id.btnBackBill) as Button

        tvNoteFoodBill.text = this.getString(R.string.note_gift_bill)

        btnBackBill.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onNoteFood(food: OrderDetailBill) {
        val dialog = Dialog(this)
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

        tvNoteFoodBill.text = food.note

        btnBackBill.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val bundle = Bundle()
        bundle.putString(TechresEnum.BILL_FRAGMENT.toString(), TechresEnum.BILL_FRAGMENT.toString())
        this.getOnRefreshFragment().onCallBack(bundle)
    }
}