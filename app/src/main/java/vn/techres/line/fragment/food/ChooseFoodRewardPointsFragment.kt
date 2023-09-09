package vn.techres.line.fragment.food

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.food.ChooseRewardPointAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.*
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.data.model.food.FoodPurcharePoint
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.OrderFoodByPointParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.DetailBranchResponse
import vn.techres.line.data.model.response.FoodPointResponse
import vn.techres.line.data.model.utils.CartPointFoodTakeAway
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentChooseFoodRewardPointsBinding
import vn.techres.line.fragment.confirm.ConFirmOrderFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Currency
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.FoodsPointHandler
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.OnLickItem
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.collections.ArrayList

@Suppress("NAME_SHADOWING")
class ChooseFoodRewardPointsFragment :
    BaseBindingFragment<FragmentChooseFoodRewardPointsBinding>(FragmentChooseFoodRewardPointsBinding::inflate),
    FoodsPointHandler, OnLickItem,
    OnRefreshFragment {

    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var foodPointList = ArrayList<FoodPurcharePoint>()
    private var foodPointSelected = ArrayList<FoodPurcharePoint>()

    //private var cartPointCheck = CartPointFoodTakeAway()
    private var totalRecord: Int? = 0

    private var currentPoint = 0
    private var idBranch = 0
    private var idBranchCheck = 0
    private var detailFoodPoint = FoodPurcharePoint()
    private var detail: BranchDetail? = null
    private var rewardCount = 0
    private var quantityCount = 0
    private var totalPointChoose = 0
    private var totalPointOrder = 0

    private var fade: Animation? = null
    private var adapterPointAdapter: ChooseRewardPointAdapter? = null
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        mainActivity?.setOnRefreshFragment(this)
        fade = AnimationUtils.loadAnimation(requireActivity(), R.anim.anim_birthday)
        adapterPointAdapter = ChooseRewardPointAdapter(requireActivity())
        adapterPointAdapter?.onFoodsPointHandler(this@ChooseFoodRewardPointsFragment)
        adapterPointAdapter?.setOnClickItem(this@ChooseFoodRewardPointsFragment)
        adapterPointAdapter?.setHasStableIds(true)
        binding.rcFoodPoint.layoutManager = activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }
        binding.rcFoodPoint.adapter = adapterPointAdapter
        binding.header.tvTitleHomeHeader.text = requireActivity().resources.getString(R.string.title_detail_restaurant)
        arguments?.let {
            idBranch = it.getInt(TechresEnum.KEY_ID_BRANCH_POINT.toString())
            totalPointOrder = it.getInt(TechresEnum.POINT_ORDER.toString())
        }
        val checkIDBranch = cacheManager.get(TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString())
        if (!checkIDBranch.isNullOrBlank()) {
            idBranchCheck = checkIDBranch.toInt()
            binding.lnPurchasePoint.show()
        }
        val jsonCartPoint = cacheManager.get(TechresEnum.KEY_CART_POINT_CHOOSE.toString())
        if (!jsonCartPoint.isNullOrBlank()) {
            foodPointSelected = checkCartPoint(jsonCartPoint)
            setFoodPoint(foodPointSelected)
            animationFood(foodPointSelected)
            if (foodPointSelected.size != 0) {
                binding.lnPurchasePoint.show()
            } else {
                binding.lnPurchasePoint.hide()
            }
        }

        if (foodPointList.size == 0) {
            getDetailRestaurant()
            setPoint(totalPointOrder)
            getFoodPoint(idBranch)
        } else {
            adapterPointAdapter?.setDataSource(foodPointList)
            if (detail != null) {
                setHeader(detail!!)
            }
            setPoint(totalPointOrder)
        }
        setListener()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.lnPurchasePoint.setOnClickListener {
            val bundle = Bundle()
            val key = 1
            cacheManager.put(
                TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString(),
                idBranchCheck.toString()
            )
            cacheManager.put(TechresEnum.KEY_CHECK_CON_FIRM.toString(), key.toString())
            if (cacheManager.get(TechresEnum.IS_TAKE_AWAY.toString()) == "-1") {
                mainActivity?.removeOnRefreshFragment(this)
                orderFoodByPoint()
            } else if (cacheManager.get(TechresEnum.IS_TAKE_AWAY.toString()) == "1") {
                val conFirmOrderFragment = ConFirmOrderFragment()
                conFirmOrderFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, conFirmOrderFragment)
            }
        }
    }

    private fun animationFood(foodSelected: ArrayList<FoodPurcharePoint>) {
        var itemQuantityCount = 0
        var itemPriceCount = 0
        foodSelected.forEach {
            itemQuantityCount += it.quantity
            itemPriceCount += it.quantity * (it.point_to_purchase)!!.toInt()
        }
        binding.tvTotalFood.text =
            String.format(
                "%s%s",
                itemQuantityCount,
                requireActivity().resources.getString(R.string.unit_quantity)
            )
        binding.tvTotalFood.alpha = 1F
        binding.tvTotalFood.startAnimation(fade)
        binding.tvTotalPoint.text = String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(itemPriceCount.toString().toFloat()),
            getString(R.string.point_mini)
        )
        binding.tvTotalPoint.startAnimation(fade)
        if (itemQuantityCount == 0) {
            binding.lnPurchasePoint.hide()
        }
    }

    private fun setOrderFoodByPoint(arrayList: ArrayList<FoodPurcharePoint>): ArrayList<ListOrderFoodByPoint> {
        val array = ArrayList<ListOrderFoodByPoint>()
        for (i in arrayList.indices) {
            val listOrderFoodByPoint = ListOrderFoodByPoint()
            listOrderFoodByPoint.food_id = arrayList[i].id
            listOrderFoodByPoint.note = arrayList[i].note!!
            listOrderFoodByPoint.is_use_point = arrayList[i].is_use_point!!
            listOrderFoodByPoint.quantity = arrayList[i].quantity
            array.add(listOrderFoodByPoint)
        }
        return array
    }

    private fun setFoodPoint(foodPointSelected: ArrayList<FoodPurcharePoint>) {
        this.quantityCount = 0
        this.totalPointChoose = 0
        for (i in foodPointSelected.indices) {
            this.quantityCount += foodPointSelected[i].quantity
            this.totalPointChoose += (foodPointSelected[i].point_to_purchase!! * foodPointSelected[i].quantity)
        }
        this.rewardCount = currentPoint - this.totalPointChoose
    }

    private fun checkCartPoint(string: String): ArrayList<FoodPurcharePoint> {
        val cartPointCheck = Gson().fromJson<CartPointFoodTakeAway>(string, object :
            TypeToken<CartPointFoodTakeAway>() {}.type)
        cartPointCheck.let {
            return if (idBranch == idBranchCheck) {
                it.food
            } else {
                ArrayList()
            }
        }
    }

    private fun saveCacheCartPoint(idBranch: Int, arrayList: ArrayList<FoodPurcharePoint>) {
        val cart = CartPointFoodTakeAway()
        cart.id_branch = idBranch
        cart.food = arrayList
        val jsonCartPoint = Gson().toJson(cart)
        cacheManager.put(TechresEnum.KEY_CART_POINT_CHOOSE.toString(), jsonCartPoint)
        if (arrayList.size == 0) {
            cacheManager.remove(TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString())
            binding.lnPurchasePoint.hide()
        }
    }

    private fun setHeader(detail: BranchDetail) {
        detail.image_urls =
            detail.image_urls.map { String.format("%s%s", nodeJs.api_ads, it) } as ArrayList<String>
        detail.image_urls.let { binding.slBranchFoodPoint.setItems(it) }
        binding.txtNameBranch.text = detail.name
    }

    private fun setPoint(point: Int) {
        currentPoint = point
        binding.txtCurrentPoint.text = currentPoint.toString()
        binding.txtFinalPoint.text = (currentPoint - totalPointChoose).toString()
        binding.txtTotalPoint.text = totalPointChoose.toString()
        binding.txtTotalFoods.text = quantityCount.toString()
    }

    private fun getDetailRestaurant() {
        mainActivity?.setLoading(true)
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format("%s%s", "/api/branches/", idBranch)
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getDetailBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DetailBranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                    mainActivity?.setLoading(false)
                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: DetailBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        detail = response.data
                        setHeader(detail!!)
                    } else Toast.makeText(
                        mainActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun getFoodPoint(idBranch: Int) {
        mainActivity?.setLoading(true)
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            String.format(
                "%s%s%s%s%s%s",
                "/api/foods/purchase-by-point?branch_id=",
                idBranch,
                "&category_id=",
                TechresEnum.CATEGORY_ID.toString(),
                "&is_take_away=",
                cacheManager.get(
                    TechresEnum.IS_TAKE_AWAY.toString()
                ).toString()
            )
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getFoodsPoint(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FoodPointResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }
                override fun onSubscribe(d: Disposable) {}
                @SuppressLint("ShowToast")
                override fun onNext(response: FoodPointResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        foodPointList = response.data!!.list
                        totalRecord = response.data!!.total_record
                        adapterPointAdapter?.setDataSource(foodPointList)
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                    mainActivity?.setLoading(false)
                }
            })
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClickShow(position: Int, foodPurcharePoint: FoodPurcharePoint) {
        val bundle = Bundle()
        try {
            bundle.putInt(TechresEnum.KEY_TOTAL_POINT.toString(), currentPoint)
            bundle.putInt(TechresEnum.KEY_TOTAL_POINT_CHOOSE.toString(), totalPointChoose)
            bundle.putInt(TechresEnum.KEY_ID_BRANCH_POINT.toString(), idBranch)
            bundle.putString(
                TechresEnum.KEY_FOOD_POINTS.toString(),
                Gson().toJson(foodPurcharePoint)
            )
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        val detailFoodPointFragment = DetailFoodPointFragment()
        detailFoodPointFragment.arguments = bundle
        mainActivity?.addOnceFragment(this, detailFoodPointFragment)
    }

    override fun lickPosition(position: Int) {
        val dialog = Dialog(this.mainActivity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_image_food)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val imgLogo = dialog.findViewById(R.id.imgAvatarGroup) as ImageView
        val lnClose = dialog.findViewById(R.id.lnClose) as LinearLayout
        imgLogo.load(
            String.format(
                "%s%s",
                nodeJs.api_ads,
                foodPointList[position].food_image?.original
            )
        ) {
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
        }
        lnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onCallBack(bundle: Bundle) {
        val bundleFoodPoint = bundle.getString(TechresEnum.KEY_FOOD_POINTS.toString())
        val idBranchFoodPoint = bundle.getInt(TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString())
        if (bundleFoodPoint != null) {
            this.detailFoodPoint = Gson().fromJson(
                bundleFoodPoint,
                object : TypeToken<FoodPurcharePoint>() {}.type
            )
            when (detailFoodPoint.quantity) {
                0 -> {
                    foodPointSelected.removeIf { it.id == detailFoodPoint.id }
                }
                else -> {
                    if (foodPointSelected.any { it.id == detailFoodPoint.id }) {
                        foodPointSelected[foodPointSelected.indexOfFirst { it.id == detailFoodPoint.id }] =
                            detailFoodPoint
                    } else {
                        foodPointSelected.add(detailFoodPoint)
                    }
                }
            }
            saveCacheCartPoint(idBranchFoodPoint, foodPointSelected)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun orderFoodByPoint() {
        mainActivity?.setLoading(true)
        val params = OrderFoodByPointParams()
        params.http_method = AppConfig.POST
        params.request_url = String.format("%s%s%s", "/api/orders/", user.id, "/add-food")
        val orderId = arguments!!.getString(TechresEnum.ORDER_ID.toString())
        params.params.order_id = orderId!!.toInt()
        params.params.foods = setOrderFoodByPoint(foodPointSelected)
        params.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .orderFoodByPoint(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                        mainActivity?.setLoading(false)
                    }
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        val bundle = Bundle()
                        bundle.putInt(
                            TechresEnum.ORDER_ID.toString(),
                            orderId.toString().toInt()
                        )
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            cacheManager.remove(TechresEnum.KEY_CART_POINT_CHOOSE.toString())
                            mainActivity?.supportFragmentManager?.popBackStack()
                        } else Toast.makeText(
                            mainActivity,
                            getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        ).show()
                        mainActivity?.setLoading(false)
                    }
                })
        }
    }

    override fun onBackPress(): Boolean {
        cacheManager.remove(TechresEnum.KEY_CART_POINT_CHOOSE.toString())
        cacheManager.remove(TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString())
        return true
    }

}