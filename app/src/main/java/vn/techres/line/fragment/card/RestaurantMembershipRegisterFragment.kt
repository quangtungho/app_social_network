package vn.techres.line.fragment.card


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.shashank.sony.fancytoastlib.FancyToast
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider.REQUEST_CHECK_SETTINGS
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.restaurant.RestaurantMembershipRegisterAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.params.BarCodeParams
import vn.techres.line.data.model.params.RestaurantNearbyParams
import vn.techres.line.data.model.params.UpdateRestaurantParams
import vn.techres.line.data.model.params.UserNodejsParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.RestaurantRegister
import vn.techres.line.data.model.restaurant.response.RestaurantCardDetailResponse
import vn.techres.line.data.model.restaurant.response.RestaurantRegisterResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.EventBusRestaurantCard
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentRestaurantRegisterMemberCardBinding
import vn.techres.line.fragment.main.MainFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.MultiplePermission.shouldAskPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.ClickMembershipRegisterHandler
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService


/**
 * A simple [Fragment] subclass.
 */
class RestaurantMembershipRegisterFragment :
    BaseBindingFragment<FragmentRestaurantRegisterMemberCardBinding>(
        FragmentRestaurantRegisterMemberCardBinding::inflate
    ),
    ClickMembershipRegisterHandler, OnRefreshFragment {
    val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var restaurantMembershipRegisterAdapter: RestaurantMembershipRegisterAdapter? = null
    private var restaurantNearby = ArrayList<RestaurantRegister>()
    private var configJava = ConfigJava()
    private var latitude = 0.0
    private var longitude = 0.0
    private var user = User()
    private var nodeJs = ConfigNodeJs()

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val requiredPermissionsCode = 200
    private lateinit var locationManager: LocationManager
    var gpsStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(mainActivity!!.baseContext)
        user = CurrentUser.getCurrentUser(requireActivity())
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.white)
        restaurantMembershipRegisterAdapter = RestaurantMembershipRegisterAdapter(requireActivity())
        restaurantMembershipRegisterAdapter?.setClickMembershipRegister(this)
        binding.rcRestaurantRegister.layoutManager =
            PreCachingLayoutManager(mainActivity!!.baseContext, RecyclerView.VERTICAL, false)
        binding.rcRestaurantRegister.setHasFixedSize(true)
        binding.rcRestaurantRegister.setItemViewCacheSize(1)
        binding.rcRestaurantRegister.adapter = restaurantMembershipRegisterAdapter

        mainActivity?.setOnRefreshFragment(this)
        mainActivity?.setOnBackClick(this)

        setListener()
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
        checkPermission()

    }

    private fun Fragment.hideKeyboard() {
        val activity = this.activity
        if (activity is AppCompatActivity) {
            activity.hideKeyboard()
        }
    }

    private fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

    }

    private fun setListener() {
        binding.svRestaurantRegister.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    restaurantMembershipRegisterAdapter?.searchFullText(newText)
                }
                return true
            }
        })
        binding.swipeRefresh.setOnRefreshListener {
            restaurantNearby = ArrayList()
            setGPS()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.btnPermission.setOnClickListener {
            binding.btnPermission.isEnabled = false
            Handler().postDelayed({
                binding.btnPermission.isEnabled = true
                checkPermission()
            }, 2000)
        }

        binding.btnTryAgain.setOnClickListener {
            binding.shimmerLoading.show()
            binding.lnGPS.hide()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (requestCode == -1) {
                setGPS()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnRefreshFragment(this)
        mainActivity?.removeOnBackClick(this)
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
                Toast.makeText(context, "Vui lòng vào mục cài đặt -> ứng dụng -> quyền ứng dụng để bật lại quyền truy cập vị trí.", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionGranted() {
                binding.lnPermission.visibility = View.GONE
                binding.shimmerLoading.show()
                binding.swipeRefresh.isEnabled = true
                setGPS()
            }
        })
    }

    // Check GPS
    private fun setGPS() {
        this.locationManager =
            mainActivity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsStatus) {
            //GPS Is Enabled
            latitude = LocationTrack(mainActivity!!).getLatitude()
            longitude = LocationTrack(mainActivity!!).getLongitude()
            getRestaurantNearby(latitude, longitude)
        } else {
            //GPS Is Disabled
            statusCheck()
        }
    }

    private fun getRestaurantNearby(latitude: Double, longitude: Double) {
        val restaurantNearbyParams = RestaurantNearbyParams()
        restaurantNearbyParams.http_method = AppConfig.GET
        restaurantNearbyParams.request_url = "/api/branches/nearby"
        restaurantNearbyParams.params.lat = latitude
        restaurantNearbyParams.params.lng = longitude
        restaurantNearbyParams.params.is_for_register_membership = 1
        restaurantNearbyParams.params.limit = 10
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getRestaurantNearby(restaurantNearbyParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantRegisterResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: RestaurantRegisterResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        restaurantNearby = response.data.list
                        if (restaurantNearby.size == 0) {
                            binding.lnEmptyRestaurant.show()
                            binding.lnRestaurantRegister.hide()
                        } else {
                            binding.lnEmptyRestaurant.hide()
                            binding.lnRestaurantRegister.show()
                        }
                        restaurantMembershipRegisterAdapter?.setDataSource(restaurantNearby)
                        binding.shimmerLoading.hide()
                    } else Toast.makeText(
                        mainActivity,
                        getString(R.string.api_error),
                        Toast.LENGTH_LONG
                    )
                        .show()
                    mainActivity?.setLoading(false)
                }
            })

    }


    override fun onClickMembershipRegister(position: Int, code: String) {
        mainActivity?.setLoading(true)
        val barCodeParams = BarCodeParams()
        barCodeParams.http_method = AppConfig.POST
        barCodeParams.request_url = "/api/membership-cards/register"
        barCodeParams.params.membership_code = code

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .barCodeRequest(barCodeParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantCardDetailResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: RestaurantCardDetailResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                            val restaurantCard = response.data
                            restaurantNearby.removeAt(position)
                            restaurantMembershipRegisterAdapter?.notifyDataSetChanged()
                            FancyToast.makeText(
                                context,
                                mainActivity!!.baseContext.getString(R.string.note_success_register),
                                FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                false
                            ).show()
                            saveRestaurantInfo(restaurantCard)
                            cacheManager.put(
                                TechresEnum.RESTAURANT_ID.toString(),
                                restaurantCard.restaurant_id.toString()
                            )
                            cacheManager.put(TechresEnum.KEY_CHECK_LOAD_REVIEW.toString(), "false")
                            updateRestaurantId(restaurantCard.restaurant_id)
                            updateRestaurant(restaurantCard.restaurant_id)
                            val bundle = Bundle()
                            bundle.putString(
                                TechresEnum.REGISTER_RESTAURANT.toString(),
                                TechresEnum.REGISTER_RESTAURANT.toString()
                            )
                            mainActivity?.clearBackstack()
                            mainActivity?.supportFragmentManager?.commit {
                                setCustomAnimations(
                                    R.anim.translate_from_right,
                                    R.anim.translate_to_left,
                                    R.anim.translate_from_left,
                                    R.anim.translate_to_right
                                )
                                add<MainFragment>(R.id.frameContainer, args = bundle)
                            }
                        }
                        AppConfig.UNAUTHORIZED -> {
                            FancyToast.makeText(
                                context,
                                response.message,
                                FancyToast.LENGTH_LONG, FancyToast.ERROR,
                                false
                            ).show()
                        }
                    }
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun updateRestaurantId(restaurantID: Int?) {
        mainActivity?.setLoading(true)
        val params = UserNodejsParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/user-party/update-restaurant-user"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.user_id = user.id
        params.params.restaurant_id = restaurantID

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .updateRestaurantId(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    mainActivity?.setLoading(false)
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun updateRestaurant(restaurantID: Int?) {
        mainActivity?.setLoading(true)

        val params = UpdateRestaurantParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customer-membership-card/create"
        params.params.restaurant_id = restaurantID

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .updateRestaurant(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    mainActivity?.setLoading(false)
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    mainActivity?.setLoading(false)
                }
            })


    }

    private fun statusCheck() {
        val manager = mainActivity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.interval = 1500
            locationRequest.fastestInterval = 3000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            val task: Task<LocationSettingsResponse> =
                LocationServices.getSettingsClient(mainActivity!!.baseContext)
                    .checkLocationSettings(builder.build())
            task.addOnCompleteListener { task ->
                try {
                    task.getResult(ApiException::class.java)
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val resolvableApiException = e as ResolvableApiException
                            resolvableApiException.startResolutionForResult(
                                mainActivity!!,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (sendIntentException: IntentSender.SendIntentException) {
                            sendIntentException.printStackTrace()
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        }
                    }
                }
            }
        } else {
            binding.shimmerLoading.hide()
            binding.lnGPS.show()
        }
    }

    fun checkPermission(){
        if (!requireActivity().shouldAskPermissions(requiredPermissions)) {
            binding.lnPermission.visibility = View.GONE
            binding.shimmerLoading.startShimmerAnimation()
            binding.shimmerLoading.visibility = View.VISIBLE
            binding.swipeRefresh.isEnabled = true
            setGPS()
        } else {
            binding.swipeRefresh.isEnabled = false
            binding.shimmerLoading.visibility = View.GONE
            binding.lnPermission.visibility = View.VISIBLE
            requestMultiplePermissionWithListener()
        }
    }

    override fun onCallBack(bundle: Bundle) {
        if (bundle.get(TechresEnum.BARCODE_FRAGMENT.toString()) != null) {
            EventBus.getDefault().post(EventBusRestaurantCard(true))
        }
    }

    override fun onBackPress(): Boolean {
        return when (cacheManager.get(TechresEnum.ACCOUNT_FRAGMENT.toString())) {
            TechresEnum.ACCOUNT_FRAGMENT.toString() -> {
                cacheManager.put(TechresEnum.ACCOUNT_FRAGMENT.toString(), "")
                true
            }
            else -> {
                false
            }
        }
    }
}
