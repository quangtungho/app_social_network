package vn.techres.line.fragment.branch

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.newsfeed.FoodsTakeAwayAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.data.model.cart.CartFoodTakeAway
import vn.techres.line.data.model.eventbus.EventBusAddFoodInCart
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.DetailBranchResponse
import vn.techres.line.data.model.response.FoodTakeAwayResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.FragmentBranchFoodsBinding
import vn.techres.line.fragment.confirm.ConFirmOrderFragment
import vn.techres.line.fragment.food.DetailDishFragment
import vn.techres.line.fragment.main.MainFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Currency
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.DetailBranchHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.text.DecimalFormat
import kotlin.collections.ArrayList


class BranchFoodsFragment : BaseBindingFragment<FragmentBranchFoodsBinding>(FragmentBranchFoodsBinding::inflate), DetailBranchHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var adapterFoodTakeAway: FoodsTakeAwayAdapter? = null
    private var foodTakeAway = ArrayList<FoodTakeAway>()
    private var detail = BranchDetail()
    private var idBranch = 0
    private var branch: Branch? = null
    private var fade: Animation? = null
    private var configJava = ConfigJava()
    private var configNodeJs = ConfigNodeJs()
    private var currentCartFoodTakeAway = CartFoodTakeAway()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(requireActivity().baseContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (foodTakeAway.size == 0) {
            getFoodTakeAway(idBranch)
            getDetailRestaurant()
        } else {
            adapterFoodTakeAway!!.notifyDataSetChanged()
            setHeader(detail)
        }
        setButtonCart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text =
            requireActivity().resources.getString(R.string.list_food_take_away)
        fade = AnimationUtils.loadAnimation(context, R.anim.anim_birthday)
        binding.rcFoodTakeAway.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }
        adapterFoodTakeAway = FoodsTakeAwayAdapter(requireActivity())
        adapterFoodTakeAway?.setOnClick(this)
        binding.rcFoodTakeAway.adapter = adapterFoodTakeAway
        arguments?.let {
            branch = Gson().fromJson<Branch>(
                it.getString(TechresEnum.KEY_ID_FOOD_RES.toString()),
                object :
                    TypeToken<Branch>() {}.type
            )
            idBranch = branch?.id ?: 0
        }


        setListener()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.lnSeenCart.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), ConFirmOrderFragment())
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAddFoodInCart(event: EventBusAddFoodInCart) {
        if (event.boolean){
            getFoodTakeAway(idBranch)
            getDetailRestaurant()
        }
    }


    private fun setButtonCart() {
        currentCartFoodTakeAway = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(mainActivity!!.baseContext)
        if (currentCartFoodTakeAway.food.size == 0) {
            binding.lnSeenCart.hide()
        } else {
            binding.lnSeenCart.show()
            animationFood(currentCartFoodTakeAway)
        }

    }


    private fun animationFood(cartFoodTakeAway : CartFoodTakeAway) {
        var itemQuantityCount = 0
        var itemPriceCount = 0f
        for (i in cartFoodTakeAway.food.indices) {
            itemQuantityCount += cartFoodTakeAway.food[i].quantity
            itemPriceCount += cartFoodTakeAway.food[i].quantity * cartFoodTakeAway.food[i].price!!
        }

        binding.txtBranchName.text = String.format("%s %s", requireActivity().resources.getString(R.string.restaurant), cartFoodTakeAway.branch_name)
        binding.txtTotalFood.text = String.format("%s %s", itemQuantityCount.toString(), requireActivity().resources.getString(R.string.food_choose))
        binding.txtTotalFood.alpha = 1F
        binding.txtTotalFood.startAnimation(fade)
        binding.txtTotalMoney.text =
            Currency.formatCurrencyDecimal(itemPriceCount.toString().toFloat())
        binding.txtTotalMoney.startAnimation(fade)
        if (binding.txtTotalFood.text == "0") {
            binding.lnSeenCart.hide()
        }
    }

    private fun setHeader(detail: BranchDetail) {
        Glide.with(binding.imgLogoBranch)
            .load(String.format("%s%s", configNodeJs.api_ads, detail.image_logo_url.medium))
            .transform(CenterCrop(), RoundedCorners(10))
            .placeholder(R.drawable.ic_restaurant_image_holder)
            .into(binding.imgLogoBranch)

        binding.tvNameBranch.text = detail.name
        binding.rbBranch.rating = (detail.star ?: 0).toFloat()
        detail.image_urls =
            detail.image_urls.map {
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    it
                )
            } as ArrayList<String>
        binding.slBranchFood.setItems(detail.image_urls)
        binding.slBranchFood.addTimerToSlide(5000)
        binding.tvRatingBranch.text = DecimalFormat("0.0").format(detail.star ?: 0F)
        if (detail.serve_time.isNotEmpty()){
            binding.tvTimeOpenBranch.text = String.format(
            "%s: %s",
            requireActivity().resources.getString(R.string.open_door),
            detail.serve_time[0].day_of_week_name
        )
        binding.tvTimeBranch.text = String.format(
            "%s: mở %s - đóng %s",
            requireActivity().resources.getString(R.string.time),
            detail.serve_time[0].open_time,
            detail.serve_time[0].close_time
        )
        }
    }

    private fun getDetailRestaurant() {
        mainActivity?.setLoading(true)

        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/branches/$idBranch"
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
                        setHeader(detail)
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                    mainActivity?.setLoading(false)
                }
            })

    }

    private fun getFoodTakeAway(branchId: Int) {
        mainActivity?.setLoading(true)
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/foods/take-away?branch_id=$idBranch&category_type=-1&page=1&limit=5000&is_random=0"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getFoodTakeAway(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FoodTakeAwayResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: FoodTakeAwayResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        foodTakeAway = response.data
                        adapterFoodTakeAway?.setDataSource(foodTakeAway)
                        adapterFoodTakeAway?.setBranchID(branchId)
                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                    mainActivity?.setLoading(false)
                }
            })

    }

    override fun onDetailBranch(int: Int) {

    }

    override fun onDetailDish(id: Int) {

    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onFoodsTakeAway(foodAwayTake: FoodTakeAway, position: Int) {
        val bundle = Bundle()
        try {
            bundle.putString(
                TechresEnum.DETAIL_DISH.toString(),
                Gson().toJson(foodAwayTake)
            )
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        val detailDishFragment = DetailDishFragment()
        detailDishFragment.arguments = bundle
        mainActivity?.addOnceFragment(this, detailDishFragment)
    }

    override fun onIncrease() {
        setButtonCart()
    }

    override fun onDecrease() {
        setButtonCart()
    }

    override fun onDetailFood(foodAwayTake: FoodTakeAway, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this.mainActivity!!, R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_detail_dish)
        bottomSheetDialog.setCancelable(true)
        val imgAvatar = bottomSheetDialog.findViewById<ImageView>(R.id.imgAvatar)
        val tvNameFood = bottomSheetDialog.findViewById<TextView>(R.id.tvNameFood)
        val tvPriceFood = bottomSheetDialog.findViewById<TextView>(R.id.tvPriceFood)
        val tvDescription = bottomSheetDialog.findViewById<TextView>(R.id.tvDescription)
        val btnAdd = bottomSheetDialog.findViewById<Button>(R.id.btnAdd)

        if (imgAvatar != null) {
            Glide.with(imgAvatar)
                .load(String.format("%s%s", configNodeJs.api_ads, foodAwayTake.avatar?.original))
                .placeholder(R.drawable.food_demo)
                .centerCrop()
                .into(imgAvatar)
        }

        tvNameFood!!.text = foodAwayTake.name
        tvPriceFood!!.text = String.format(
            "%s%s",
            Currency.formatCurrencyDecimal((foodAwayTake.price ?: 0).toFloat()),
            resources.getString(R.string.denominations)
        )
        tvDescription!!.text = foodAwayTake.description

        btnAdd!!.setOnClickListener {
            currentCartFoodTakeAway = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(mainActivity!!.baseContext)
            if(currentCartFoodTakeAway.food.size == 0){
                foodAwayTake.quantity += 1
                currentCartFoodTakeAway.food.add(foodAwayTake)
            } else{
                if (currentCartFoodTakeAway.food.any { it.id == foodAwayTake.id}){
                    currentCartFoodTakeAway.food[position].quantity += 1
                }else{
                    foodAwayTake.quantity += 1
                    currentCartFoodTakeAway.food.add(foodAwayTake)
                }
            }

            currentCartFoodTakeAway.id_branch = branch!!.id
            currentCartFoodTakeAway.branch_name = branch!!.name
            currentCartFoodTakeAway.branch_avatar = branch!!.image_logo_url.medium
            CurrentCartFoodTakeAway.saveCurrentCartFoodTakeaway(mainActivity!!.baseContext, currentCartFoodTakeAway)
            setButtonCart()
            adapterFoodTakeAway!!.notifyItemChanged(position)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun addDish(foodAwayTake: FoodTakeAway, position: Int) {
        if (foodAwayTake.branch_id == currentCartFoodTakeAway.id_branch || currentCartFoodTakeAway.id_branch == 0){
            addFood(foodAwayTake, position)
            binding.lnSeenCart.show()
        }else{
            val bottomSheetDialog = BottomSheetDialog(this.mainActivity!!, R.style.SheetDialog)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_cancel_cart)
            bottomSheetDialog.setCancelable(true)
            val btnContinue = bottomSheetDialog.findViewById<Button>(R.id.btnContinue)
            val btnCancel = bottomSheetDialog.findViewById<Button>(R.id.btnCancel)

            btnContinue!!.setOnClickListener {
                currentCartFoodTakeAway = CartFoodTakeAway()
                addFood(foodAwayTake, position)
                bottomSheetDialog.dismiss()
            }

            btnCancel!!.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }


    }

    fun addFood(foodAwayTake: FoodTakeAway, position: Int){
        foodAwayTake.quantity += 1
        currentCartFoodTakeAway.food.add(foodAwayTake)
        currentCartFoodTakeAway.id_branch = branch!!.id
        currentCartFoodTakeAway.branch_name = branch!!.name
        currentCartFoodTakeAway.branch_avatar = branch!!.image_logo_url.medium
        CurrentCartFoodTakeAway.saveCurrentCartFoodTakeaway(mainActivity!!.baseContext, currentCartFoodTakeAway)
        setButtonCart()
        adapterFoodTakeAway!!.notifyItemChanged(position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)

    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPress(): Boolean {
        activity?.window?.statusBarColor = AlolineColorUtil(mainActivity!!.baseContext).convertColor(R.color.white)
        return true
    }

}