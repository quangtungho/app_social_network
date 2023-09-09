package vn.techres.line.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.location.Location
import android.location.LocationManager
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.ShippingAddressParams
import vn.techres.line.data.model.response.ShippingAddressResponse
import vn.techres.line.data.model.utils.EventBusMap
import vn.techres.line.databinding.ActivityMapsBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Utils.getCompleteAddressString
import vn.techres.line.helper.Utils.getCompleteWardString
import vn.techres.line.helper.WriteLog
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService


class MapsActivity : BaseBindingActivity<ActivityMapsBinding>(),
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveCanceledListener,
    GoogleMap.OnCameraIdleListener,
    OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var lat = 0.0
    private var lng = 0.0
    private var address = ""
    private var content = ""
    override val bindingInflater: (LayoutInflater) -> ActivityMapsBinding
        get() = ActivityMapsBinding::inflate

    override fun onSetBodyView() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.containerView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setListener()

    }

    private fun setListener() {
        closeKeyboard(binding.edtNoteAddress)
        binding.imgMyLocation.setOnClickListener {
            setMyLocation()?.let {
                binding.imgMyLocation.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this@MapsActivity, R.color.main_bg))
                val latLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
            }
        }

        binding.btnConfirmAddress.setOnClickListener {
            createAddress(lat, lng, address)
            EventBus.getDefault().post(EventBusMap(lat, lng, content, address, binding.edtNoteAddress.text.toString()))
            onBackPressed()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap
        with(googleMap) {
            setOnCameraIdleListener(this@MapsActivity)
            setOnCameraMoveStartedListener(this@MapsActivity)
            setOnCameraMoveListener(this@MapsActivity)
            setOnCameraMoveCanceledListener(this@MapsActivity)
            // We will provide our own zoom controls.
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isMapToolbarEnabled = false
            // Add a marker in Sydney and move the camera
            setMyLocation()?.let {
                binding.imgMyLocation.imageTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this@MapsActivity, R.color.main_bg))
                val latLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
                address = getCompleteAddressString(this@MapsActivity, it.latitude, it.longitude)
                content = getCompleteWardString(this@MapsActivity, it.latitude, it.longitude)
                binding.tvAddress.text = address
                if(content != ""){
                    binding.tvContentAddress.text = content
                }else{
                    binding.tvContentAddress.text = resources.getString(R.string.empty_content_address)
                }

            }
        }
    }

    private fun setMyLocation(): Location? {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val localGpsLocation = if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
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
    private fun createAddress(latitude: Double, longitude : Double, address : String) {
        val shippingAddressParams = ShippingAddressParams()
        shippingAddressParams.http_method = AppConfig.POST
        shippingAddressParams.request_url = "/api/customer-shipping-addresses/0"
        shippingAddressParams.params.id = 0
        shippingAddressParams.params.lat = latitude
        shippingAddressParams.params.lng = longitude
        shippingAddressParams.params.address_full_text = address
        shippingAddressParams.params.is_default = 1
        shippingAddressParams.let {
            ServiceFactory.createRetrofitService(

                TechResService::class.java
            )

                .updateAddress(shippingAddressParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ShippingAddressResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: ShippingAddressResponse) {
                    }
                })
        }
    }
    override fun onCameraMoveStarted(p0: Int) {
    }

    override fun onCameraMove() {
        binding.imgMyLocation.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this@MapsActivity, R.color.main_gray_dim))
    }

    override fun onCameraMoveCanceled() {

    }

    override fun onCameraIdle() {
        val centerLatLang = mMap.projection.visibleRegion.latLngBounds.center
        content = getCompleteWardString(this@MapsActivity, centerLatLang.latitude, centerLatLang.longitude)
        address = getCompleteAddressString(this@MapsActivity, centerLatLang.latitude, centerLatLang.longitude)
        binding.tvAddress.text = address
        if(content != ""){
            binding.tvContentAddress.text = content
        }else{
            binding.tvContentAddress.text = resources.getString(R.string.empty_content_address)
        }
        lat = centerLatLang.latitude
        lng = centerLatLang.longitude
    }

    override fun onBackPressed() {
        super.onBackPressed()
        closeKeyboard(binding.edtNoteAddress)
        finish()
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

}