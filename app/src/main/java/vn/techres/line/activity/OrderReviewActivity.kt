package vn.techres.line.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.giphy.sdk.analytics.GiphyPingbacks.context
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.order.OrderReviewAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.OrderReview
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.OrderReviewParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.OrderReviewResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityOrderReviewBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.OnLickItem
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class OrderReviewActivity : BaseBindingActivity<ActivityOrderReviewBinding>(), OnLickItem {
    private var orderReviewAdapter: OrderReviewAdapter? = null
    private var configJava = ConfigJava()
    private var dataOrderReview = ArrayList<OrderReview>()
    private var user = User()
    private var dialog: Dialog? = null
    private var serviceRate = 1
    private var foodRate = 1
    private var priceRate = 1
    private var spaceRate = 1
    private var hygieneRate = 1
    private var page = 1

    override val bindingInflater: (LayoutInflater) -> ActivityOrderReviewBinding
        get() = ActivityOrderReviewBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(baseContext)
    }

    override fun onSetBodyView() {
        this@OrderReviewActivity.window?.statusBarColor =
            AlolineColorUtil(context).convertColor(R.color.white)
        orderReviewAdapter = OrderReviewAdapter(this@OrderReviewActivity)
        binding.recyclerOrderReview.layoutManager =
            LinearLayoutManager(this@OrderReviewActivity, RecyclerView.VERTICAL, false)
        binding.recyclerOrderReview.adapter = orderReviewAdapter
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.order_review)
        orderReviewAdapter?.setOnlickItem(this)

        setData()
        setListener()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.swipeRefreshOrderReview.setOnRefreshListener {
            getOrderReview(page)
            binding.swipeRefreshOrderReview.isRefreshing = false
        }
    }

    private fun setData() {
        user = CurrentUser.getCurrentUser(this@OrderReviewActivity)
        if (dataOrderReview.size == 0) {
            getOrderReview(page)
            binding.shimmerViewContainer.startShimmerAnimation()
        } else {
            orderReviewAdapter?.setDataSource(dataOrderReview)
        }

    }

    private fun getOrderReview(page: Int) {
        val params = BaseParams()
        params.http_method = 0
        params.request_url =
            "/api/orders/waiting-reviews?page=$page&limit=100&restaurant_id=" +
                    restaurant().restaurant_id!!
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getListOrderReview(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OrderReviewResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: OrderReviewResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataOrderReview = response.data!!.list
                        context.let {
                            if (dataOrderReview.size == 0) {
                                binding.lnOrderReviewNull.visibility = View.VISIBLE
                                binding.recyclerOrderReview.visibility = View.GONE
                            } else {
                                binding.lnOrderReviewNull.visibility = View.GONE
                                binding.recyclerOrderReview.visibility = View.VISIBLE
                            }
                        }
                        handleLoadDataProfileSuccess()
                    } else Toast.makeText(
                        this@OrderReviewActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })

    }

    override fun lickPosition(position: Int) {
        showDialogNotificalAccumulate(position)
    }

    private fun showDialogNotificalAccumulate(position: Int) {

        dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.dialog_accumulate_points)

        val imgClose = dialog?.findViewById(R.id.imgClose) as ImageView
        val imgTutorial = dialog?.findViewById(R.id.imgTutorial) as ImageView
        val btnSend = dialog?.findViewById(R.id.btnSend) as Button
        val ratingService = dialog?.findViewById(R.id.ratingService) as RatingBar
        val ratingFood = dialog?.findViewById(R.id.ratingFood) as RatingBar
        val ratingPrice = dialog?.findViewById(R.id.ratingPrice) as RatingBar
        val ratingSpace = dialog?.findViewById(R.id.ratingSpace) as RatingBar
        val ratingHygiene = dialog?.findViewById(R.id.ratingHygiene) as RatingBar
        val imgService = dialog?.findViewById(R.id.imgService) as ImageView
        val imgFood = dialog?.findViewById(R.id.imgFood) as ImageView
        val imgPrice = dialog?.findViewById(R.id.imgPrice) as ImageView
        val imgSpace = dialog?.findViewById(R.id.imgSpace) as ImageView
        val imgHygiene = dialog?.findViewById(R.id.imgHygiene) as ImageView
        val txtService = dialog?.findViewById(R.id.txtService) as TextView
        val txtFood = dialog?.findViewById(R.id.txtFood) as TextView
        val txtPrice = dialog?.findViewById(R.id.txtPrice) as TextView
        val txtSpace = dialog?.findViewById(R.id.txtSpace) as TextView
        val txtHygiene = dialog?.findViewById(R.id.txtHygiene) as TextView

        val txtUserName = dialog?.findViewById(R.id.txtUserName) as TextView
        val txtTime = dialog?.findViewById(R.id.txtTime) as TextView
        val txtPoint = dialog?.findViewById(R.id.txtPoint) as TextView
        val txtRestaurantName = dialog?.findViewById(R.id.txtRestaurantName) as TextView

        Glide.with(baseContext)
            .load(R.drawable.howtoreview)
            .into(imgTutorial)

        txtUserName.text = user.username
        if (dataOrderReview.size != 0) {
            txtTime.text = dataOrderReview[position].payment_date
            txtPoint.text = String.format(
                "%s %s",
                dataOrderReview[position].membership_point_added,
                baseContext.getString(R.string.point)
            )
            txtRestaurantName.text = dataOrderReview[position].restaurant_name
        }

        ratingService.setOnRatingBarChangeListener { _, rating, _ ->
            imgService.visibility = View.VISIBLE
            txtService.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingService.rating = 1f
                    serviceRate = rating.toInt() + 1
                }
                1f -> {
                    imgService.setImageResource(R.drawable.ic_rating_1)
                    txtService.text = resources.getString(R.string.bad)
                    serviceRate = rating.toInt()
                }
                2f -> {
                    imgService.setImageResource(R.drawable.ic_rating_2)
                    txtService.text = resources.getString(R.string.least)
                    serviceRate = rating.toInt()
                }
                3f -> {
                    imgService.setImageResource(R.drawable.ic_rating_3)
                    txtService.text = resources.getString(R.string.okey)
                    serviceRate = rating.toInt()
                }
                4f -> {
                    imgService.setImageResource(R.drawable.ic_rating_4)
                    txtService.text = resources.getString(R.string.rather)
                    serviceRate = rating.toInt()
                }
                5f -> {
                    imgService.setImageResource(R.drawable.ic_rating_5)
                    txtService.text = resources.getString(R.string.great)
                    serviceRate = rating.toInt()
                }
            }
        }

        ratingFood.setOnRatingBarChangeListener { _, rating, _ ->
            imgFood.visibility = View.VISIBLE
            txtFood.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingFood.rating = 1f
                    foodRate = rating.toInt() + 1
                }
                1f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_1)
                    txtFood.text = resources.getString(R.string.bad)
                    foodRate = rating.toInt()
                }
                2f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_2)
                    txtFood.text = resources.getString(R.string.least)
                    foodRate = rating.toInt()
                }
                3f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_3)
                    txtFood.text = resources.getString(R.string.okey)
                    foodRate = rating.toInt()
                }
                4f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_4)
                    txtFood.text = resources.getString(R.string.rather)
                    foodRate = rating.toInt()
                }
                5f -> {
                    imgFood.setImageResource(R.drawable.ic_rating_5)
                    txtFood.text = resources.getString(R.string.great)
                    foodRate = rating.toInt()
                }
            }
        }

        ratingPrice.setOnRatingBarChangeListener { _, rating, _ ->
            imgPrice.visibility = View.VISIBLE
            txtPrice.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingPrice.rating = 1f
                    priceRate = rating.toInt() + 1
                }
                1f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_1)
                    txtPrice.text = resources.getString(R.string.bad)
                    priceRate = rating.toInt()
                }
                2f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_2)
                    txtPrice.text = resources.getString(R.string.least)
                    priceRate = rating.toInt()
                }
                3f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_3)
                    txtPrice.text = resources.getString(R.string.okey)
                    priceRate = rating.toInt()
                }
                4f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_4)
                    txtPrice.text = resources.getString(R.string.rather)
                    priceRate = rating.toInt()
                }
                5f -> {
                    imgPrice.setImageResource(R.drawable.ic_rating_5)
                    txtPrice.text = resources.getString(R.string.great)
                    priceRate = rating.toInt()
                }
            }
        }

        ratingSpace.setOnRatingBarChangeListener { _, rating, _ ->
            imgSpace.visibility = View.VISIBLE
            txtSpace.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingSpace.rating = 1f
                    spaceRate = rating.toInt() + 1
                }
                1f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_1)
                    txtSpace.text = resources.getString(R.string.bad)
                    spaceRate = rating.toInt()
                }
                2f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_2)
                    txtSpace.text = resources.getString(R.string.least)
                    spaceRate = rating.toInt()
                }
                3f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_3)
                    txtSpace.text = resources.getString(R.string.okey)
                    spaceRate = rating.toInt()
                }
                4f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_4)
                    txtSpace.text = resources.getString(R.string.rather)
                    spaceRate = rating.toInt()
                }
                5f -> {
                    imgSpace.setImageResource(R.drawable.ic_rating_5)
                    txtSpace.text = resources.getString(R.string.great)
                    spaceRate = rating.toInt()
                }
            }
        }

        ratingHygiene.setOnRatingBarChangeListener { _, rating, _ ->
            imgHygiene.visibility = View.VISIBLE
            txtHygiene.visibility = View.VISIBLE
            // Called when the user swipes the RatingBar
            when (rating) {
                0f -> {
                    ratingHygiene.rating = 1f
                    hygieneRate = rating.toInt() + 1
                }
                1f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_1)
                    txtHygiene.text = resources.getString(R.string.bad)
                    hygieneRate = rating.toInt()
                }
                2f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_2)
                    txtHygiene.text = resources.getString(R.string.least)
                    hygieneRate = rating.toInt()
                }
                3f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_3)
                    txtHygiene.text = resources.getString(R.string.okey)
                    hygieneRate = rating.toInt()
                }
                4f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_4)
                    txtHygiene.text = resources.getString(R.string.rather)
                    hygieneRate = rating.toInt()
                }
                5f -> {
                    imgHygiene.setImageResource(R.drawable.ic_rating_5)
                    txtHygiene.text = resources.getString(R.string.great)
                    hygieneRate = rating.toInt()
                }
            }
        }

        imgClose.setOnClickListener {
            dialog?.dismiss()
        }

        btnSend.setOnClickListener {
            dataOrderReview[position].id?.let { it1 -> createOrderReview(it1) }
            dialog?.dismiss()
        }

        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.show()
    }

    private fun createOrderReview(idBill: Int) {
        val orderReviewParams = OrderReviewParams()
        orderReviewParams.http_method = 1
        orderReviewParams.request_url = "/api/orders/$idBill/review"

        orderReviewParams.params.service_rate = serviceRate
        orderReviewParams.params.food_rate = foodRate
        orderReviewParams.params.price_rate = priceRate
        orderReviewParams.params.space_rate = spaceRate
        orderReviewParams.params.hygiene_rate = hygieneRate

        orderReviewParams.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .createReviewOrder(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        when (response.status) {
                            AppConfig.SUCCESS_CODE -> {
                                dataOrderReview.clear()
                                page = 1
                                getOrderReview(page)
                                Toast.makeText(
                                    this@OrderReviewActivity,
                                    resources.getString(R.string.success_order_review),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            else -> {
                                Toast.makeText(
                                    this@OrderReviewActivity,
                                    response.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                    }
                })
        }
    }

    //Xử lí dữ liệu khi request thành công
    private fun handleLoadDataProfileSuccess() {
        Handler().postDelayed({
            binding.shimmerViewContainer.stopShimmerAnimation()
            binding.shimmerViewContainer.visibility = View.GONE
            orderReviewAdapter?.setDataSource(dataOrderReview)
        }, 1000)

    }

}