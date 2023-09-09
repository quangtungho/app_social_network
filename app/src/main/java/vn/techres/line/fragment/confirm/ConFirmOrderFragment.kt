package vn.techres.line.fragment.confirm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.fragment.ChooseShippingUnitFragment
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.order.ConFirmOrderAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.FoodOnlineOrder
import vn.techres.line.data.model.ItemCart
import vn.techres.line.data.model.address.Address
import vn.techres.line.data.model.branch.BranchDetail
import vn.techres.line.data.model.cart.CartFoodTakeAway
import vn.techres.line.data.model.cart.ComFormData
import vn.techres.line.data.model.deliveries.BundleDeliveries
import vn.techres.line.data.model.deliveries.ConfigDeliveries
import vn.techres.line.data.model.eventbus.EventBusAddFoodInCart
import vn.techres.line.data.model.eventbus.EventBusUnitDeliveries
import vn.techres.line.data.model.food.FoodPurcharePoint
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateOnlineOrderParams
import vn.techres.line.data.model.params.ItemCartParams
import vn.techres.line.data.model.response.ComFormOrderResponse
import vn.techres.line.data.model.response.DetailBranchResponse
import vn.techres.line.data.model.response.OrderInvoiceResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentConFirmOrderBinding
import vn.techres.line.fragment.address.AddressFragment
import vn.techres.line.fragment.delivery.DeliveryOrderFragment
import vn.techres.line.fragment.paymem.PayMemMethodFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.EditItemCartHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class ConFirmOrderFragment :
    BaseBindingFragment<FragmentConFirmOrderBinding>(FragmentConFirmOrderBinding::inflate), EditItemCartHandler, OnMapReadyCallback {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var brandID = 0
    private var isCheck = true
    private var foodsOnlineOrder = ArrayList<FoodOnlineOrder>()
    private var adapter: ConFirmOrderAdapter? = null
    private var dataItemCart = ComFormData()
    private var branchDetail : BranchDetail? = null
    private var configJava = ConfigJava()
    private var configNodeJs = ConfigNodeJs()
    private var currentDeliveries = ConfigDeliveries()
    private var currentCartFoodTakeAway = CartFoodTakeAway()
    private var user = User()
    private var address: Address? = null
    private var mMap: GoogleMap? = null

    private var domainShippingUnit = ""
    private var apiKeyShippingUnit = ""

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val requiredPermissionsCode = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        currentCartFoodTakeAway = CurrentCartFoodTakeAway.getCurrentCartFoodTakeaway(mainActivity!!.baseContext)
        currentDeliveries = CurrentDeliveries.getCurrentConfigDeliveries(requireActivity())
        brandID = currentCartFoodTakeAway.id_branch!!
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.rcFoodOrder.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        adapter = ConFirmOrderAdapter(requireActivity())
        adapter?.setEditItemCartHandler(this@ConFirmOrderFragment)
        binding.rcFoodOrder.adapter = adapter
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.con_firm_order)
        initGoogleMap(savedInstanceState)
        setListener()

        if (currentDeliveries.name != ""){
            binding.txtNotShippingUnitYet.visibility = View.GONE
            binding.clShippingUnitSelected.visibility = View.VISIBLE
            binding.txtNameShipperUnit.text = currentDeliveries.name
            binding.txtMoneyDeliveries.text = "đ " + Currency.formatCurrencyDecimal(currentDeliveries.shipping_fee!!.toFloat())
            binding.txtDescriptionDeliveries.text =  "Đây là đài truyền hình tiếng nói Việt Nam, phát thanh từ Hà Nội thủ đô nước Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam."
            domainShippingUnit = currentDeliveries.api_domain.toString()
            apiKeyShippingUnit = currentDeliveries.api_key.toString()
        }else{
            binding.txtNotShippingUnitYet.visibility = View.VISIBLE
            binding.clShippingUnitSelected.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUnitDeliveriesUpdate(event: EventBusUnitDeliveries) {
        if (event.data.name != ""){
            binding.txtNotShippingUnitYet.visibility = View.GONE
            binding.clShippingUnitSelected.visibility = View.VISIBLE
            binding.txtNameShipperUnit.text = event.data.name
            binding.txtMoneyDeliveries.text = "đ " + Currency.formatCurrencyDecimal(event.data.shipping_fee!!.toFloat())
            binding.txtDescriptionDeliveries.text =  "Đây là đài truyền hình tiếng nói Việt Nam, phát thanh từ Hà Nội thủ đô nước Cộng Hòa Xã Hội Chủ Nghĩa Việt Nam."
            domainShippingUnit = event.data.api_domain.toString()
            apiKeyShippingUnit = currentDeliveries.api_key.toString()
        }else{
            binding.txtNotShippingUnitYet.visibility = View.VISIBLE
            binding.clShippingUnitSelected.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mvAddress.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mvAddress.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mvAddress.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mvAddress.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mvAddress.onLowMemory()
    }

    private fun setData() {
        getDetailRestaurant(brandID)
        setFoodTakeAway(currentCartFoodTakeAway.food)
        adapter?.setDataSource(currentCartFoodTakeAway.food)

        if (isCheck) {
            if (address != null) {
                binding.groupAddress.show()
                binding.groupEmptyMyAddress.hide()
                setAddress(address)
            } else {
                binding.groupEmptyMyAddress.show()
                binding.groupAddress.hide()
            }
        }
    }


    private fun setBranch(branchDetail: BranchDetail?){
        binding.imgAvatarBranch.loadImage(String.format("%s%s", configNodeJs.api_ads, branchDetail?.image_logo_url?.original), R.drawable.restaurant_image_holder)
        binding.txtNameRestaurant.text = String.format("%s: %s", requireContext().getString(R.string.belong_to_restaurant), restaurant().restaurant_name)
        binding.txtNameBranch.text = branchDetail?.name ?: ""
        binding.txtAddressBranch.text = branchDetail?.address_full_text ?: ""
        binding.txtRatingBranch.text = String.format("%.1f", branchDetail?.star ?: 0F)
    }

    private fun setAddress(address: Address?) {
        address?.let {
            binding.tvNameAddress.text = it.name ?: ""
            binding.tvAddress.text = it.address_full_text ?: ""
            binding.edtNoteAddress.text = Editable.Factory.getInstance().newEditable(it.note ?: "")
            mMap?.clear()
            val latLng = LatLng(it.lat ?: 0.0, it.lng ?: 0.0)
            val marker = mMap?.addMarker(
                MarkerOptions()
                    .position(latLng).title("My Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .draggable(false).visible(true)
            )
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
        }
    }

    private fun setListener() {

        binding.header.btnBack.setOnClickListener {
            EventBus.getDefault().post(EventBusAddFoodInCart(true))
            mainActivity?.supportFragmentManager?.popBackStack()
        }

        binding.tvAddPromotion.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_add_promotion)
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            lp.gravity = Gravity.CENTER
            dialog.window!!.attributes = lp
            val imgCloseAddPromotion = dialog.findViewById(R.id.imgCloseAddPromotion) as ImageView
            imgCloseAddPromotion.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
        binding.tvUpdateAddress.setOnClickListener {
//            requestMultiplePermissionWithListener()
            mainActivity?.addOnceFragment(this@ConFirmOrderFragment, AddressFragment())
        }
        binding.tvChangeAddress.setOnClickListener {
//            requestMultiplePermissionWithListener()
            mainActivity?.addOnceFragment(this@ConFirmOrderFragment, AddressFragment())
        }

        binding.btnCreateOnlineOrder.setOnClickListener {
            if (address != null) {
                createOnlineOrder()
            } else {
                AloLineToast.makeText(
                    requireActivity(),
                    resources.getString(R.string.empty_address),
                    AloLineToast.LENGTH_SHORT,
                    AloLineToast.WARNING
                ).show()
            }
        }

        binding.mvAddress.isEnabled = false

        binding.tvAddPaymentMethod.setOnClickListener {
            mainActivity?.addOnceFragment(this, PayMemMethodFragment())
        }

        binding.lnShippingUnit.setOnClickListener {
            val bundleDeliveries = BundleDeliveries()
            bundleDeliveries.branch_id = brandID
            bundleDeliveries.lat = address!!.lat
            bundleDeliveries.lng = address!!.lng

            val bundle = Bundle()
            bundle.putString(TechresEnum.KEY_DELIVERIES.toString(), Gson().toJson(bundleDeliveries))
            val chooseShippingUnitFragment = ChooseShippingUnitFragment()
            chooseShippingUnitFragment.arguments = bundle
            mainActivity?.addOnceFragment(this, chooseShippingUnitFragment)

        }

        binding.txtAddFood.setOnClickListener {
            EventBus.getDefault().post(EventBusAddFoodInCart(true))
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun setFoodTakeAway(arrayList: ArrayList<FoodTakeAway>) {
        foodsOnlineOrder = ArrayList()
        for (i in arrayList.indices) {
            val foodOnline = FoodOnlineOrder()
            foodOnline.food_id = arrayList[i].id
            foodOnline.quantity = arrayList[i].quantity
            foodOnline.note = arrayList[i].note
            foodOnline.is_use_point = arrayList[i].is_use_point
            foodsOnlineOrder.add(foodOnline)
            if (foodsOnlineOrder.size == arrayList.size){
                getComFormOrder(foodsOnlineOrder)
            }
        }

    }

//    private fun setFoodTakeAwayPoint(arrayList: ArrayList<FoodPurcharePoint>) {
//        for (i in arrayList.indices) {
//            val foodOnline = FoodOnlineOrder()
//            foodOnline.food_id = arrayList[i].id
//            foodOnline.quantity = arrayList[i].quantity
//            foodOnline.note = arrayList[i].note
//            foodOnline.is_use_point = arrayList[i].is_use_point
//            foodsOnlineOrder.add(foodOnline)
//        }
//    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(TechresEnum.MAPVIEW_BUNDLE_KEY.toString())
        }
        binding.mvAddress.onCreate(mapViewBundle)
        binding.mvAddress.getMapAsync(this)
        requestMultiplePermissionWithListener()
    }

    private fun requestMultiplePermissionWithListener() {
        requestPermissions(requiredPermissions, requiredPermissionsCode, object :
            RequestPermissionListener {

            override fun onCallPermissionFirst(
                namePermission: String,
                requestPermission: () -> Unit
            ) {
                requestPermission.invoke()
            }

            override fun onPermissionRationaleShouldBeShown(
                namePermission: String,
                requestPermission: () -> Unit
            ) {
                requestPermission.invoke()
            }

            override fun onPermissionPermanentlyDenied(
                namePermission: String,
                openAppSetting: () -> Unit
            ) {
                dialogPermission(
                    String.format(
                        requireActivity().resources.getString(R.string.title_permission),
                        namePermission
                    ),
                    String.format(
                        requireActivity().resources.getString(R.string.note_permission_denied),
                        requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    requireActivity().resources.getString(R.string.step_two_open_permission_location),
                    R.drawable.ic_location,
                    "",
                    0,
                    object : DialogPermissionHandler {
                        override fun confirmDialog() {
                            openAppSetting.invoke()
                        }

                        override fun dismissDialog() {
                            mainActivity?.supportFragmentManager?.popBackStack()
                        }

                    }
                )
            }

            override fun onPermissionGranted() {
                if(address != null){
                    val latLng = LatLng(address?.lat ?: 0.0, address?.lng ?: 0.0)
                    val marker = mMap?.addMarker(
                        MarkerOptions()
                            .position(latLng).title(requireActivity().resources.getString(R.string.my_location))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                            .draggable(false).visible(true)
                    )
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
                }else{
                    setMyLocation()?.let {
                        address = Address()
                        val content = Utils.getCompleteWardString(
                            requireActivity(),
                            it.latitude,
                            it.longitude
                        )
                        address?.id = 0
                        address?.name = if(content != ""){
                            content
                        }else{
                            resources.getString(R.string.empty_content_address)
                        }
                        address?.address_full_text = Utils.getCompleteAddressString(
                            requireActivity(),
                            it.latitude,
                            it.longitude
                        )
                        address?.lat = it.latitude
                        address?.lng = it.longitude
                        address?.type = 0
                    }
                }
                setData()
            }
        })
    }
    private fun setMyLocation(): Location? {
        val locationManager =
            mainActivity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val localGpsLocation = if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null
        } else {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        return localGpsLocation
    }

    private fun getDetailRestaurant(idBranch : Int) {
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
                        branchDetail = response.data
                        setBranch(branchDetail)
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                    mainActivity?.setLoading(false)
                }
            })

    }

    private fun getComFormOrder(foodList: ArrayList<FoodOnlineOrder>) {
        mainActivity?.setLoading(true)
        val itemCartRequest = ItemCartParams()
        itemCartRequest.http_method = AppConfig.POST
        itemCartRequest.request_url = "/api/orders/take-away/verify"
        itemCartRequest.params.branch_id = brandID
        itemCartRequest.params.foods = foodList

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .creatComFormOrder(itemCartRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ComFormOrderResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ComFormOrderResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataItemCart = response.data
                        adapter?.setPoint(dataItemCart.total_point ?: 0)
//                        adapter?.setDataSource(dataItemCart.list)
                        binding.tvAmount.text = String.format(
                            "%s %s",
                            Currency.formatCurrencyDecimal(dataItemCart.amount ?: 0F),
                            resources.getString(R.string.denominations)
                        )
                        binding.tvAmountDeliveryFee.text = String.format(
                            "%s %s",
                            Currency.formatCurrencyDecimal(currentDeliveries.shipping_fee!!.toFloat()),
                            resources.getString(R.string.denominations)
                        )
                        val totalEnd = dataItemCart.total_amount?.plus(currentDeliveries.shipping_fee!!)
                        binding.tvTotal.text = String.format(
                            "%s %s",
                            Currency.formatCurrencyDecimal(totalEnd!!.toFloat()),
                            resources.getString(R.string.denominations)
                        )
                        binding.btnCreateOnlineOrder.visibility = View.VISIBLE
                    } else Toast.makeText(
                        requireActivity(),
                        requireActivity().resources.getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    ).show()
                    mainActivity?.setLoading(false)
                }
            })

    }

    private fun createOnlineOrder() {
        mainActivity?.setLoading(true)
        val params = CreateOnlineOrderParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/orders/create"
        params.params.branch_id = brandID
        params.params.shipping_address = address
        params.params.note = binding.edtNoteAddress.text.toString()
        params.params.payment_method_id = 1
        params.params.voucher = ""
        params.params.foods = foodsOnlineOrder
        params.params.third_party_delivery_id = currentDeliveries.id
        params.params.receiver_name = user.name
        params.params.receiver_phone = user.phone

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .createOnlineOrder(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<OrderInvoiceResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: OrderInvoiceResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val idOrder = response.data
                        val bundleCreateOnlineOrder = Bundle()
                        bundleCreateOnlineOrder.putInt(
                            TechresEnum.KEY_ID_ORDER_CUSTOMER.toString(),
                            idOrder ?: 0
                        )
                        bundleCreateOnlineOrder.getString(TechresEnum.BRANCH_DETAIL.toString(), Gson().toJson(branchDetail))
                        bundleCreateOnlineOrder.getString(TechresEnum.ADDRESS_FRAGMENT.toString(), Gson().toJson(address))
                        when (cacheManager.get(TechresEnum.KEY_CHECK_CON_FIRM.toString())!!
                            .toInt()) {
                            1 -> {
                                cacheManager.remove(TechresEnum.KEY_CART_POINT_CHOOSE.toString())
                                cacheManager.remove(TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString())
                            }
                            else -> {
                                cacheManager.remove(TechresEnum.KEY_CART_CHOOSE.toString())
                                cacheManager.remove(TechresEnum.KEY_CHECK_ID_BRANCH.toString())
                            }
                        }
                        val deliveryOrderFragment = DeliveryOrderFragment()
                        deliveryOrderFragment.arguments = bundleCreateOnlineOrder
                        mainActivity?.addOnceFragment(
                            this@ConFirmOrderFragment,
                            deliveryOrderFragment
                        )
//                        val orderSuccessFragment = OrderSuccessFragment()
//                        orderSuccessFragment.arguments = bundleCreateOnlineOrder
//                        mainActivity?.addOnceFragment(
//                            this@ConFirmOrderFragment,
//                            orderSuccessFragment
//                        )
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                        .show()
                    mainActivity?.setLoading(false)
                }
            })
    }

    override fun onChooseItemCart(itemCart: ItemCart) {
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onChangeItemCart(listCart: ArrayList<FoodTakeAway>) {
        adapter!!.setDataSource(listCart)
        if (listCart.size == 0){
            binding.techResTextView30.visibility = View.GONE
            binding.edtNoteRestaurant.visibility = View.GONE
        }else{
            binding.techResTextView30.visibility = View.VISIBLE
            binding.edtNoteRestaurant.visibility = View.VISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(TechresEnum.MAPVIEW_BUNDLE_KEY.toString())
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(TechresEnum.MAPVIEW_BUNDLE_KEY.toString(), mapViewBundle)
        }
        binding.mvAddress.onSaveInstanceState(mapViewBundle)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.mMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            googleMap?.uiSettings?.isMapToolbarEnabled = false
            googleMap?.uiSettings?.isScrollGesturesEnabled = false
//            address?.let {
//                val latLng = LatLng(it.lat ?: 0.0, it.lng ?: 0.0)
//                val marker = googleMap?.addMarker(
//                    MarkerOptions()
//                        .position(latLng).title(requireActivity().resources.getString(R.string.my_location))
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
//                        .draggable(false).visible(true)
//                )
//                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
//                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
//            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleOnRequestPermissionResult(
            requiredPermissionsCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    dialogPermission(
                        String.format(
                            requireActivity().resources.getString(R.string.title_permission),
                            namePermission
                        ),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission_location),
                        R.drawable.ic_location,
                        "",
                        0,
                        object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                requestPermission.invoke()
                            }

                            override fun dismissDialog() {
                                mainActivity?.supportFragmentManager?.popBackStack()
                            }
                        }
                    )
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        String.format(
                            requireActivity().resources.getString(R.string.title_permission),
                            namePermission
                        ),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission_location),
                        R.drawable.ic_location,
                        "",
                        0,
                        object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                                mainActivity?.supportFragmentManager?.popBackStack()
                            }
                        }
                    )
                }

                override fun onPermissionGranted() {
                    if(address != null){
                        val latLng = LatLng(address?.lat ?: 0.0, address?.lng ?: 0.0)
                        val marker = mMap?.addMarker(
                            MarkerOptions()
                                .position(latLng).title(requireActivity().resources.getString(R.string.my_location))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                .draggable(false).visible(true)
                        )
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
                        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(marker?.position, 18F))
                    }else{
                        setMyLocation()?.let {
                            address = Address()
                            val content = Utils.getCompleteWardString(
                                requireActivity(),
                                it.latitude,
                                it.longitude
                            )
                            address?.id = 0
                            address?.name = if(content != ""){
                                content
                            }else{
                                resources.getString(R.string.empty_content_address)
                            }
                            address?.address_full_text = Utils.getCompleteAddressString(
                                requireActivity(),
                                it.latitude,
                                it.longitude
                            )
                            address?.lat = it.latitude
                            address?.lng = it.longitude
                            address?.type = 0
                        }
                    }
                    setData()
                }
            }
        )
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
        isCheck = false
        mainActivity?.setLoading(false)
        return true
    }

}
