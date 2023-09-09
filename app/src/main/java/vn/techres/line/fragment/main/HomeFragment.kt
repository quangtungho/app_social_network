package vn.techres.line.fragment.main

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import coil.load
import coil.size.Scale
import com.airbnb.lottie.LottieAnimationView
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
import vn.techres.line.activity.*
import vn.techres.line.activity.game.LuckyWheelOfflineActivity
import vn.techres.line.adapter.branch.BranchAdapter
import vn.techres.line.adapter.food.FoodTakeAwayHomeAdapter
import vn.techres.line.adapter.home.FoodTopAdapter
import vn.techres.line.adapter.home.SliderAdvertAdapter
import vn.techres.line.adapter.voucher.VoucherAdapter
import vn.techres.line.base.BaseBindingStubFragment
import vn.techres.line.data.model.RestaurantBranch
import vn.techres.line.data.model.RestaurantSocialContents
import vn.techres.line.data.model.avert.Advert
import vn.techres.line.data.model.booking.CountGiftBookingResponse
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.branch.response.BranchResponse
import vn.techres.line.data.model.cart.CartFoodTakeAway
import vn.techres.line.data.model.food.FoodBestSeller
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.food.reponse.FoodBestSellerResponse
import vn.techres.line.data.model.gift.OpenListGiftParams
import vn.techres.line.data.model.gift.OpenListGiftRequest
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.*
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.restaurant.response.RestaurantCardDetailResponse
import vn.techres.line.data.model.utils.*
import vn.techres.line.data.model.voucher.Voucher
import vn.techres.line.data.model.voucher.VoucherResponse
import vn.techres.line.databinding.FragmentHomeBinding
import vn.techres.line.fragment.branch.ChooseBranchFragment
import vn.techres.line.fragment.confirm.ConFirmOrderFragment
import vn.techres.line.fragment.food.FoodTakeAwayDetailFragment
import vn.techres.line.fragment.food.FoodTopFragment
import vn.techres.line.fragment.game.ChooseBranchGameFragment
import vn.techres.line.fragment.game.GameFragment
import vn.techres.line.fragment.qr.BarcodeMemberCardFragment
import vn.techres.line.fragment.qr.QRCodeManagerFragment
import vn.techres.line.fragment.recharge.MobileRechargeFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.Currency
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.*
import vn.techres.line.interfaces.branch.BranchHandler
import vn.techres.line.interfaces.food.FoodHomeHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
//import vn.techres.photo.StartActivity
import java.util.*
import kotlin.math.abs


@SuppressLint("UseRequireInsteadOfGet")
class HomeFragment : BaseBindingStubFragment<FragmentHomeBinding>(R.layout.fragment_home, true),
    BranchHandler, SliderAdvertHandler,
    ReviewPostHandler, ClickOneImage, ClickMedia, LikeHandler, FoodHomeHandler, VoucherHandler {

    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var infoBirthdayGift = ArrayList<InfoBirthdayGift>()
    private var branchList = ArrayList<Branch>()
    private var foodBestSellerList = ArrayList<FoodBestSeller>()
    private var adapterFoodTop: FoodTopAdapter? = null
    private var adapterFoodTakeAway: FoodTakeAwayHomeAdapter? = null
    private var adapterBranch: BranchAdapter? = null

    private var listImageAvatar = ArrayList<String>()
    private var listImageBanner = ArrayList<String>()
    private var listRestaurantBranchName = ArrayList<String>()

    private var fade: Animation? = null
    private var logoBranch = ""
    private var idBranch = 0
    private var positionBranch = 0
    private var adapterSliderAdvert: SliderAdvertAdapter? = null
    private var adapterVoucher: VoucherAdapter? = null
    private var currentPage = 0
    private var timer: Timer? = null
    private val delayMs: Long = 1000 //delay in milliseconds before task is to be executed
    private val periodMs: Long = 5000 // time in milliseconds between successive task executions.
    private var isCheck = false
    private var currentPageVoucher = 0
    private var timerVoucher: Timer? = null
    private var isCheckVoucher = false
    private var sliderAdventList = ArrayList<Advert>()
    private var foodTakeAwayList = ArrayList<FoodTakeAway>()
    private var branch = Branch()
    private var data = ArrayList<RestaurantSocialContents>()
    private var listBannerRestaurant = ArrayList<String>()
    private var restaurantCard: RestaurantCard? = null
    private var configNodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()
    private var dataVoucher = ArrayList<Voucher>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)
    }

    override fun onCreateViewAfterViewStubInflated(savedInstanceState: Bundle?) {
        user = CurrentUser.getCurrentUser(requireActivity())
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
        fade = AnimationUtils.loadAnimation(context, R.anim.anim_home_birthday)
        stubBinding?.food?.rcFoodTop?.layoutManager =
            GridLayoutManager(mainActivity?.baseContext, 1)
        stubBinding?.food?.rcBranch?.layoutManager =
            PreCachingLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        stubBinding?.food?.rcFoodTakeAway?.layoutManager =
            PreCachingLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        (stubBinding?.food?.rcFoodTop?.itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations =
            false
        adapterFoodTop = FoodTopAdapter(requireActivity())
        adapterBranch = BranchAdapter(requireActivity())
        adapterFoodTakeAway = FoodTakeAwayHomeAdapter(requireActivity())
        adapterSliderAdvert = SliderAdvertAdapter(requireActivity())
        adapterVoucher = VoucherAdapter(requireActivity())
        adapterSliderAdvert?.setSliderBannerHandler(this)
        adapterVoucher?.setVoucherHandler(this)
        adapterBranch?.setBranchHandler(this)
        adapterFoodTakeAway?.setFoodHomeHandler(this)
//        adapterFoodTakeAway?.setFoodTakeAwayHandler(this)

        stubBinding?.food?.rcBranch?.adapter = adapterBranch
        stubBinding?.food?.rcFoodTakeAway?.adapter = adapterFoodTakeAway
        stubBinding?.food?.rcFoodTop?.adapter = adapterFoodTop

        setBannerADS()
        setVoucher()
        setDataHome()
        setListener()

    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
        getCountMyGift()
        getCountReceivingGift()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClickTab(scrollScreen: ScrollScreen) {
        if (scrollScreen.click == 0) {
            stubBinding?.nsvMain?.scrollTo(0, 0)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onQRCode(eventBusRestaurantCard: EventBusRestaurantCard) {
        if (eventBusRestaurantCard.check) {
            setDataHome()
            mainActivity?.setLoading(false)
        }
    }

    private fun setDataHome() {
        val restaurant = restaurant()
        data.clear()
        getRestaurantBranch()
        getAdvertHome()
        getPointCardRefresh()
//        getCountGift()
        getRestaurantSlogan()
        getVoucher()
        if (branchList.size == 0) {
            getListBranch()
        } else {
            adapterBranch?.setDataSource(branchList)
            adapterFoodTop?.setDataSource(foodBestSellerList)
            getFoodBestSellerBranch(branchList[positionBranch].id ?: 0, 4, 1)
            if (foodTakeAwayList.size > 0) {
                adapterFoodTakeAway?.setDataSource(foodTakeAwayList)
            }
        }

        stubBinding?.imgBannerGame?.load("https://stcv4.hnammobile.com//mini-game/7/minigame-doan-dung-tinh-nang-nhan-ngay-qua-khung.png?v=1591869389") {
            crossfade(true)
            scale(Scale.FIT)
        }

        stubBinding?.header?.imgAvatarUserHome?.load(
            String.format(
                "%s%s",
                configNodeJs.api_ads,
                user.avatar_three_image.thumb
            )
        ) {
            crossfade(true)
            scale(Scale.FIT)
            placeholder(R.drawable.ic_user_placeholder)
            error(R.drawable.ic_user_placeholder)
            size(300, 300)
        }
        stubBinding?.header?.tvNameHome?.text = String.format("%s %s", "Xin chào,", user.name)
        stubBinding?.header?.txtRestaurantName?.text = restaurant.restaurant_name
        stubBinding?.food?.tvNameRestaurant?.text = restaurant.restaurant_name

        if (infoBirthdayGift.size == 0) {
            getInfoBirthday()
        } else {
            stubBinding?.birthday?.rlBirthday?.show()
            stubBinding?.birthday?.rlBirthday?.startAnimation(fade)
        }

        listBannerRestaurant = restaurant.restaurant_image_urls.map {
            String.format(
                "%s%s",
                configNodeJs.api_ads,
                it
            )
        } as ArrayList<String>
        listBannerRestaurant.let { stubBinding?.header?.sliderRestaurant?.setItems(it) }
        stubBinding?.header?.sliderRestaurant?.addTimerToSlide(3000)
        val jsonCart = cacheManager.get(TechresEnum.KEY_CART_CHOOSE.toString())
        if (!jsonCart.isNullOrBlank()) {
            val cart = Gson().fromJson<CartFoodTakeAway>(jsonCart, object :
                TypeToken<CartFoodTakeAway>() {}.type)
            stubBinding?.rlCart?.show()
            stubBinding?.tvCountCart?.text = if (cart.food.size > 9) {
                mainActivity?.resources?.getString(R.string.maximum_cart_food) ?: ""
            } else {
                cart.food.size.toString()
            }

        } else {
            stubBinding?.rlCart?.hide()
        }
    }
    private fun setDataBranch(listRestaurantBranch: ArrayList<RestaurantBranch>) {
        listRestaurantBranch.forEachIndexed { _, s ->
            s.name.toString().let { item -> listRestaurantBranchName.add(item) }
            s.logo_url.toString().let { item -> listImageAvatar.add(item) }
            s.banner.toString().let { item -> listImageBanner.add(item) }
        }

        stubBinding?.header?.txtBranchName?.post(object : Runnable {
            var i = 0
            override fun run() {
                try {
                    stubBinding?.header?.txtBranchName?.text = listRestaurantBranchName[i]
                    i++
                    if (i == listRestaurantBranch.size) i = 0
                    stubBinding?.header?.txtBranchName?.postDelayed(this, 10000)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })

        stubBinding?.header?.imgBranchAvatar?.post(object : Runnable {
            var i = 0
            override fun run() {
                try {
                    stubBinding?.header?.imgBranchAvatar?.load(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            listImageAvatar[i]
                        )
                    ) {
                        crossfade(true)
                        scale(Scale.FIT)
                        placeholder(R.drawable.logo_aloline_placeholder)
                        error(R.drawable.logo_aloline_placeholder)
                        size(300, 300)
                    }
                    i++
                    if (i == listRestaurantBranch.size) i = 0
                    stubBinding?.header?.imgBranchAvatar?.postDelayed(this, 10000)
                } catch (ex: Exception) {

                }
            }
        })

        stubBinding?.header?.imgBannerRestaurantBranch?.post(object : Runnable {
            var i = 0
            override fun run() {
                try {
                    stubBinding?.header?.imgBannerRestaurantBranch?.load(
                        String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            listImageBanner[i]
                        )
                    ) {
                        crossfade(true)
                        scale(Scale.FIT)
                        placeholder(R.drawable.logo_aloline_placeholder)
                        error(R.drawable.logo_aloline_placeholder)
                    }
                    i++
                    if (i == listRestaurantBranch.size) i = 0
                    stubBinding?.header?.imgBannerRestaurantBranch?.postDelayed(this, 10000)
                } catch (ex: Exception) {

                }
            }
        })
    }

    private fun setListener() {
        stubBinding?.food?.rcBranch?.let { ViewCompat.setNestedScrollingEnabled(it, false) }
        stubBinding?.food?.rcFoodTop?.let { ViewCompat.setNestedScrollingEnabled(it, false) }
        stubBinding?.rlBannerGame?.show()

        stubBinding?.swipeRefresh?.setOnRefreshListener {
            getRestaurantBranch()
            getInfoBirthday()
            getAdvertHome()
            getRestaurantSlogan()
            getPointCardRefresh()
            getCountMyGift()
            getCountReceivingGift()
            getVoucher()
            stubBinding?.food?.rcBranch?.smoothScrollToPosition(0)
            stubBinding?.rlBannerGame?.show()
            stubBinding?.swipeRefresh?.isRefreshing = false
        }
        stubBinding?.fbCart?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), ConFirmOrderFragment())
        }
        stubBinding?.birthday?.rlBirthday?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(
                TechresEnum.HOME_FRAGMENT.toString(),
                TechresEnum.HOME_FRAGMENT.toString()
            )
            val barcodeMemberCardFragment = BarcodeMemberCardFragment()
            barcodeMemberCardFragment.arguments = bundle
            mainActivity?.addOnceFragment(MainFragment(), barcodeMemberCardFragment)
        }
        stubBinding?.utilities?.llOrderHome?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(
                TechresEnum.KEY_REWARD_POINT.toString(),
                requireActivity().resources.getString(R.string.OrderHome)
            )
            val chooseBranchFragment = ChooseBranchFragment()
            chooseBranchFragment.arguments = bundle
            mainActivity?.addOnceFragment(MainFragment(), chooseBranchFragment)
        }

        stubBinding?.utilities?.lnRecharge?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), MobileRechargeFragment())
        }

        stubBinding?.utilities?.lnQRCode?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), QRCodeManagerFragment())
        }

        stubBinding?.utilities?.lnCreateBooking?.setOnClickListener {
            val intent = Intent(mainActivity, CreateBookingActivity::class.java)
            startActivity(intent)
            mainActivity!!.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.utilities?.lnOrderByPoint?.setOnClickListener {
            val bundleRewardPoints = Bundle()
            bundleRewardPoints.putString(
                TechresEnum.KEY_REWARD_POINT.toString(),
                requireActivity().resources.getString(R.string.title_core)
            )
            val chooseBranchFragment = ChooseBranchFragment()
            chooseBranchFragment.arguments = bundleRewardPoints
            mainActivity?.addOnceFragment(MainFragment(), chooseBranchFragment)
        }
        stubBinding?.utilities?.lnUtilities?.setOnClickListener {
            mainActivity?.let {
//                Handler(Looper.getMainLooper()).postDelayed({
//                    startActivity(
//                        Intent(
//                            mainActivity,
//                            StartActivity::class.java
//                        )
//                    )
//                }, 100)
            }
        }
        stubBinding?.utilities?.rlGift?.setOnClickListener {
            val intent = Intent(mainActivity, GiftActivity::class.java)
            startActivity(intent)
            mainActivity!!.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.header?.rlPointHome?.setOnClickListener {
            val intent = Intent(mainActivity, PointCardActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.header?.tvPointHome?.setOnClickListener {
            val intent = Intent(mainActivity, PointCardActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.utilities?.lnPlayGame?.setOnClickListener {
            mainActivity?.addOnceFragment(MainFragment(), ChooseBranchGameFragment())
        }
        stubBinding?.utilities?.lnImageStore?.setOnClickListener {
            val intent = Intent(mainActivity, AlbumHomeActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        stubBinding?.utilities?.lnRestaurantImage?.setOnClickListener {
            val intent = Intent(mainActivity, ChooseBranchActivity::class.java)
            intent.putExtra(TechresEnum.TYPE_CHOOSE_BRANCH.toString(), "restaurant_image")
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right)

        }
        stubBinding?.imgCloseBannerGame?.setOnClickListener {
            stubBinding?.rlBannerGame?.hide()
        }

        stubBinding?.rlBannerGame?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(TechresEnum.KEY_CHOOSE_BRANCH.toString(), Gson().toJson(branch))
            val gameFragment = GameFragment()
            gameFragment.arguments = bundle
            mainActivity?.addOnceFragment(MainFragment(), gameFragment)
//            val intent = Intent(mainActivity , LuckyWheelTetActivity::class.java)
//            startActivity(intent)
        }

        stubBinding?.food?.txtMoreFoodTop?.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(TechresEnum.BRANCH_ID.toString(), branchList[positionBranch].id!!)
            val foodTopFragment = FoodTopFragment()
            foodTopFragment.arguments = bundle
            mainActivity?.addOnceFragment(MainFragment(), foodTopFragment)
        }

        stubBinding?.utilities?.lnPointHistory?.setOnClickListener {
            val intent = Intent(mainActivity, PointCardActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.header?.imgAvatarUserHome?.setOnClickListener {
            val intent = Intent(mainActivity, ProfileActivity::class.java)
            intent.putExtra(TechresEnum.ID_USER.toString(), user.id)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.food?.tvMoreFoodTakeAway?.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(
                TechresEnum.FOOD_TAKE_AWAY_DETAIL_FRAGMENT.toString(),
                Gson().toJson(branch)
            )
//            mainActivity?.let {
//                Navigation.findNavController(
//                    it,
//                    R.id.nav_host
//                ).navigate(R.id.action_mainFragment_to_foodTakeAwayDetailFragment, bundle)
//            }
            val foodTakeAwayDetailFragment = FoodTakeAwayDetailFragment()
            foodTakeAwayDetailFragment.arguments = bundle
            mainActivity?.addOnceFragment(MainFragment(), foodTakeAwayDetailFragment)
        }

        stubBinding?.cvLuckyWheelOffline?.setOnClickListener {
            val intent = Intent(mainActivity, LuckyWheelOfflineActivity::class.java)
            startActivity(intent)
        }

        stubBinding?.dGift?.setOnClickListener {
            getListGift()
        }
    }

    private fun dialogGiftBooking() {
        val dialog = this.context?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(false)
        dialog?.setContentView(R.layout.diaglog_gift_booking)
        val imgCloseGiftBooking = dialog?.findViewById(R.id.imgCloseGiftBooking) as ImageView
        val lottieAnimationViewGiftBooking =
            dialog.findViewById(R.id.lottieAnimationViewGiftBooking) as LottieAnimationView
        lottieAnimationViewGiftBooking.setOnClickListener {
            cacheManager.put(
                TechresEnum.CHOOSE_GIFT_BOOKING.toString(),
                TechresEnum.CHOOSE_GIFT_BOOKING.toString()
            )
            dialog.dismiss()
        }
        imgCloseGiftBooking.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun getInfoBirthday() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/restaurant-birthday-gifts"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getInforBirthdayGift(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<InforBirthdayGiftResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: InforBirthdayGiftResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        infoBirthdayGift = response.data
                    }
                    mainActivity?.setLoading(false)

                }
            })
    }

    private fun getFoodTakeAway(branch: Branch) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format(
            "/api/foods/take-away?branch_id=%s&category_type=-1&page=1&limit=20&is_random=1",
            branch.id
        )
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
                        foodTakeAwayList = response.data
//                        context?.let {
//                            val nameBranch =  branch.address_full_text?.split(",")
//                            val address = String.format("%s %s", requireActivity().resources.getString(R.string.title_address_branch), nameBranch?.get(0))
//                        }

                        adapterFoodTakeAway?.setDataSource(foodTakeAwayList)
                    }
                }
            })
    }

    private fun getFoodBestSellerBranch(idBranch: Int, limit: Int, page: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s",
            "/api/foods/best-seller?branch_id=",
            idBranch,
            "&limit=",
            limit,
            "&page=",
            page
        )
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getFoodBestSellerBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FoodBestSellerResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: FoodBestSellerResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        foodBestSellerList = response.data
                        if (foodBestSellerList.size == 0) {
                            stubBinding?.food?.lnFoodTop?.hide()
                        } else {
                            stubBinding?.food?.lnFoodTop?.show()
                        }
                        adapterFoodTop?.setDataSource(foodBestSellerList)
                        adapterFoodTop?.setClickOneImage(this@HomeFragment)
                    }
                }
            })
    }

    private fun getListBranch() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            String.format("%s%s", "/api/branches?restaurant_id=", restaurant().restaurant_id)
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getListBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        branchList = response.data.list
//                        setDataBranch(branchList, 0)
                        adapterBranch?.setDataSource(branchList)

                        try {
                            branch = branchList[0]
                            logoBranch = branchList[0].logo_image_url.original ?: ""
//                            getFoodTakeAway(branchList[0].id!!, logoBranch)
                            getFoodBestSellerBranch(branchList[0].id ?: 0, 4, 1)
                            getFoodTakeAway(branchList[0])
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }

                    }
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun getCountMyGift() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
//        baseRequest.request_url = "/api/bookings/gift-count"
        baseRequest.request_url =
            "/api/customer-gifts/count?customer_id=${user.id}&is_opened=1&is_used=0&is_expired=0"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getCountGift(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CountGiftBookingResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: CountGiftBookingResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        when {
                            data == 0 -> {
                                stubBinding?.utilities?.tvCountGift?.hide()
                            }
                            data!! > 0 -> {
                                stubBinding?.utilities?.tvCountGift?.show()
                                stubBinding?.utilities?.tvCountGift?.text = data.toString()
//                                if (cacheManager.get(TechresEnum.CHOOSE_GIFT_BOOKING.toString()) != TechresEnum.CHOOSE_GIFT_BOOKING.toString()) {
//                                    dialogGiftBooking()
//                                }
                            }
                            data >= 99 -> {
                                stubBinding?.utilities?.tvCountGift?.show()
                                stubBinding?.utilities?.tvCountGift?.text =
                                    requireActivity().resources.getString(R.string.limit_count_chat)
//                                if (cacheManager.get(TechresEnum.CHOOSE_GIFT_BOOKING.toString()) != TechresEnum.CHOOSE_GIFT_BOOKING.toString()) {
//                                    dialogGiftBooking()
//                                }
                            }
                        }

                    }

                }
            })
    }

    private fun getCountReceivingGift() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
//        baseRequest.request_url = "/api/bookings/gift-count"
        baseRequest.request_url =
            "/api/customer-gifts/count?customer_id=${user.id}&is_opened=0&is_used=0&is_expired=0"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getCountGift(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CountGiftBookingResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {

                }

                @SuppressLint("ShowToast")
                override fun onNext(response: CountGiftBookingResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data
                        if (data!! > 0) {
                            stubBinding!!.dGift.visibility = View.VISIBLE
                        } else {
                            stubBinding!!.dGift.visibility = View.GONE
                        }
                    }

                }
            })
    }


    private fun getPointCardRefresh() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format("%s%s", "/api/membership-cards/", restaurant().id)
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getRestaurantCardDetail(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantCardDetailResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: RestaurantCardDetailResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        restaurantCard = response.data
                        stubBinding?.header?.tvPointHome?.text = String.format(
                            "%s %s",
                            Currency.formatCurrencyDecimal(
                                (restaurantCard?.total_all_point ?: 0).toFloat()
                            ),
                            "Điểm"
                        )
                        stubBinding?.header?.txtRestaurantName?.text = String.format(
                            "%s %s",
                            "Thuộc về",
                            restaurantCard?.restaurant_name
                        )
                        if (restaurantCard != null) {
                            if (mainActivity != null) {
                                saveRestaurantInfo(restaurantCard)
                            }
                        }
                    }
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun likeUnlikeRestaurantSocialContents(restaurantSocialContentID: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api/restaurant-social-contents/$restaurantSocialContentID/like"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .likeUnlikeRestaurantSocialContents(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClick(position: Int) {
        positionBranch = position
        when (branchList[position].is_check) {
            false -> {
                branchList.forEach {
                    it.is_check = false
                    if (it == branchList[position]) {
                        it.is_check = true
                        idBranch = branchList[position].id!!
                        logoBranch = branchList[position].logo_image_url.original ?: ""
                        this.branch = branchList[position]
                        getFoodBestSellerBranch(branchList[position].id!!, 4, 1)
                        getFoodTakeAway(branchList[position])
                    }
                }
                stubBinding?.food?.rcBranch?.smoothScrollToPosition(position)
                adapterBranch?.notifyDataSetChanged()
            }
            else -> {}
        }
    }


    private fun getAdvertHome() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/adverts/get-advert-for-mobile-app?media_type=2&limit=10&is_running=1&page=1&status=2"
        restaurant().restaurant_id?.let { _ ->
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .getAdverttHome(baseRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<AdvertResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: AdvertResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            val slider = response.data.list
                            if (sliderAdventList.size == 0) {
                                stubBinding?.banner?.lnBannerHome?.hide()
                            }
                            if (!sliderAdventList.containsAll(slider)) {
                                stubBinding?.banner?.lnBannerHome?.show()
                                sliderAdventList = response.data.list

                                adapterSliderAdvert?.setDataSource(sliderAdventList)
                            }
                        }
                        mainActivity?.setLoading(false)
                    }
                })
        }
    }

    private fun getRestaurantSlogan() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/restaurant-slogans?restaurant_id=" + restaurant().restaurant_id!!.toInt()
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getRestaurantSlogan(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantSloganResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: RestaurantSloganResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data.list
                        var contentSlogan = ""

                        if (data.isEmpty()) {
                            stubBinding?.header?.txtNotificationScrolling?.hide()
                        } else {
                            for (i in data.indices) {
                                contentSlogan =
                                    contentSlogan + "                    " + data[i].content
                            }

                            stubBinding?.header?.txtNotificationScrolling?.text = contentSlogan
                            stubBinding?.header?.txtNotificationScrolling?.show()
                            stubBinding?.header?.txtNotificationScrolling?.isSelected = true
                        }

                    } else {
                        Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                    }
                    mainActivity?.setLoading(false)

                }
            })
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
            0,
            "&promotion_campaign_id=",
            -1,
            "&limit=",
            -1,
            "&page=",
            1,
            "&is_get_customer_restaurant_voucher_different_zezo=",
            -1
        )
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getVoucher(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<VoucherResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: VoucherResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataVoucher = response.data.list
                        if (dataVoucher.size != 0) {
                            stubBinding?.vpVoucher!!.visibility = View.VISIBLE
                            adapterVoucher?.setDataSource(dataVoucher)
                        } else {
                            stubBinding?.vpVoucher!!.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }

    private fun takeVoucher(data: Voucher) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api/customer-restaurant-vouchers/${data.id}/accept"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getVoucher(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<VoucherResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: VoucherResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val intent = Intent(mainActivity, VoucherDetailActivity::class.java)
                        intent.putExtra(
                            TechresEnum.VOUCHER_INFORMATION.toString(),
                            Gson().toJson(data)
                        )
                        startActivity(intent)
                        mainActivity?.overridePendingTransition(
                            R.anim.translate_from_right,
                            R.anim.translate_to_right
                        )
                    } else {
                        Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }

    override fun onClickBanner(position: Int) {

//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.action_mainFragment_to_webViewFragment)
//        }
//        val url = sliderAdventList[position].url
//        if (url!!.startsWith("https:
//            val uri = Uri.parse(url)
//            val intent = Intent(Intent.ACTION_VIEW, uri)
//            startActivity(intent)
//        } else {
//            Toast.makeText(this.context, "Invalid Url", Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onClickShare(position: Int) {
    }

    override fun onClickImage(url: String, position: Int) {
        lookAtPhoto(url, position)
    }

    override fun onClickImageMedia(url: ArrayList<Media>, position: Int) {
        val list = ArrayList<String>()
        url.forEach {
            list.add(String.format("%s%s", configNodeJs.api_ads, it.url.original))
        }
        val intent = Intent(mainActivity, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        startActivity(intent)
    }

    override fun onClickLike(position: Int, id: Int?) {
        likeUnlikeRestaurantSocialContents(id!!)
    }

    override fun onClickUnLike(position: Int, id: Int?) {
        likeUnlikeRestaurantSocialContents(id!!)
    }

    private fun lookAtPhoto(url: String, position: Int) {
        val list = ArrayList<String>()
        list.add(String.format("%s%s", configNodeJs.api_ads, url))
        val intent = Intent(mainActivity, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    private fun setBannerADS() {
        stubBinding?.banner?.vpBannerADS?.apply {
            this.adapter = adapterSliderAdvert
            this.clipToPadding = false
            this.clipChildren = false
            this.offscreenPageLimit = 1
            this.getChildAt(0)?.overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(1))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.95f + r * 0.09f
            }

            /*After setting the adapter use the timer */
            if (!isCheck) {
                val handler = Handler(Looper.myLooper()!!)
                val update = Runnable {
                    if (currentPage == sliderAdventList.size) {
                        currentPage = 0
                    }
                    this.setCurrentItem(currentPage++, true)
                }
                timer = Timer() // This will create a new Thread
                timer?.schedule(object : TimerTask() {
                    // task to be scheduled
                    override fun run() {
                        handler.post(update)
                    }
                }, delayMs, periodMs)
                this@HomeFragment.isCheck = true
            }
            this.setPageTransformer(compositePageTransformer)
        }
    }

    private fun setVoucher() {
        stubBinding?.vpVoucher?.apply {
            this.adapter = adapterVoucher
            this.clipToPadding = false
            this.clipChildren = false
            this.offscreenPageLimit = 1
            this.getChildAt(0)?.overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS

            val compositePageTransformer = CompositePageTransformer()
            compositePageTransformer.addTransformer(MarginPageTransformer(1))
            compositePageTransformer.addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.95f + r * 0.09f
            }

            /*After setting the adapter use the timer */
            if (!isCheckVoucher) {
                val handler = Handler(Looper.myLooper()!!)
                val update = Runnable {
                    if (currentPageVoucher == dataVoucher.size) {
                        currentPageVoucher = 0
                    }
                    this.setCurrentItem(currentPageVoucher++, true)
                }
                timerVoucher = Timer() // This will create a new Thread
                timerVoucher?.schedule(object : TimerTask() {
                    // task to be scheduled
                    override fun run() {
                        handler.post(update)
                    }
                }, delayMs, periodMs)
                this@HomeFragment.isCheckVoucher = true
            }
            this.setPageTransformer(compositePageTransformer)
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

    override fun onChooseFoodTakeAway(position: Int) {

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
        takeVoucher(data)
    }

    override fun onBackPress(): Boolean {
        return true
    }
    private fun getListGift() {
        mainActivity?.setLoading(true)
        val request = OpenListGiftRequest()
        request.os_name = "mobile"
//        request.restaurant_id = user?.restaurant_id
        val params = OpenListGiftParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customer-gifts/open-list-gift"
        params.params = request
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getListGift(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                    mainActivity?.setLoading(false)
                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        val intent = Intent(mainActivity, GiftsActivity::class.java)
//                        startActivity(intent)
//                        mainActivity?.overridePendingTransition(
//                            R.anim.translate_from_right,
//                            R.anim.translate_to_right
//                        )
                        val intent = Intent(mainActivity, GiftActivity::class.java)
                        startActivity(intent)
                        mainActivity!!.overridePendingTransition(
                            R.anim.translate_from_right,
                            R.anim.translate_to_right
                        )
                    } else {
                        Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }

    private fun getRestaurantBranch() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/restaurant-brand?restaurant_id=${restaurant().restaurant_id}&status=1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getRestaurantBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantBranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: RestaurantBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        setDataBranch(response.data)
                    } else {
                        Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }
}
