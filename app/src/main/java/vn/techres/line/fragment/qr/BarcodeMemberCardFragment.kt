package vn.techres.line.fragment.qr

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.params.BarCodeParams
import vn.techres.line.data.model.params.UpdateRestaurantParams
import vn.techres.line.data.model.params.UserNodejsParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.response.RestaurantCardDetailResponse
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentBarcodeMemberCardBinding
import vn.techres.line.fragment.main.MainFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService


/**
 * A simple [Fragment] subclass.
 */
@Suppress("UNREACHABLE_CODE")

class BarcodeMemberCardFragment :
    BaseBindingFragment<FragmentBarcodeMemberCardBinding>(FragmentBarcodeMemberCardBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var lastText = ""
    private val requiredPermissionCamera = arrayOf(
        Manifest.permission.CAMERA
    )
    private val requiredPermissionCameraCode = 1002
    private var user = User()
    private var flashMode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pauseAndWait()
    }

    override fun onResume() {
        super.onResume()
        binding.barcodeView.resume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.qrCode)
        binding.barcodeView.setStatusText("")

        requestMultiplePermissionCamera()

        setListener()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.imgFlash.setOnClickListener {
            if (hasFlash() == false) {
                context?.let {
                    Toast.makeText(
                        it,
                        it.resources.getString(R.string.empty_flash_light),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                if (flashMode == 0) {
                    binding.barcodeView.setTorchOn()
                    binding.imgFlash.setBackgroundResource(R.drawable.ic_flash)
                    flashMode = 1
                } else if (flashMode == 1) {
                    flashMode = 0
                    binding.barcodeView.setTorchOff()
                    binding.imgFlash.setBackgroundResource(R.drawable.flash_off)
                }
            }
        }
    }


    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean? {
        return mainActivity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            if (result?.text == null || result.text.equals(lastText)) {
                return
            }
            lastText = result.text
            barCode(result.text)
        }

        override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            super.possibleResultPoints(resultPoints)
        }
    }

    private fun requestMultiplePermissionCamera() {
        requestPermissions(requiredPermissionCamera, requiredPermissionCameraCode,
            object : RequestPermissionListener {
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
                        requireActivity().resources.getString(R.string.title_approve_permission_camera),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                        R.drawable.ic_pink_camera, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionGranted() {
                    binding.barcodeView.initializeFromIntent(mainActivity?.intent)
                    binding.barcodeView.decodeContinuous(callback)
                    binding.barcodeView.barcodeView.cameraSettings.requestedCameraId =
                        Camera.CameraInfo.CAMERA_FACING_BACK
                    binding.barcodeView.resume()
                }

            })
    }

    private fun barCode(qrResult: String) {
        mainActivity?.setLoading(true)
        val barCodeParams = BarCodeParams()
        barCodeParams.http_method = AppConfig.POST
        barCodeParams.request_url = "/api/membership-cards/register"

        barCodeParams.params.membership_code = qrResult
        barCodeParams.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .barCodeRequest(it)
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
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            val restaurantCard = response.data
                            context?.let {
                                FancyToast.makeText(
                                    it,
                                    response.message,
                                    FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                    false
                                ).show()
                            }
//                            val bundle = Bundle()
//                            bundle.putString(TechresEnum.BARCODE_FRAGMENT.toString(), Gson().toJson(response.data))
//                            mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
//                            mainActivity?.supportFragmentManager?.popBackStack()
                            val bundle = Bundle()
                            bundle.putString(
                                TechresEnum.REGISTER_RESTAURANT.toString(),
                                TechresEnum.REGISTER_RESTAURANT.toString()
                            )

                            saveRestaurantInfo(restaurantCard)
                            cacheManager.put(
                                TechresEnum.RESTAURANT_ID.toString(),
                                restaurantCard.restaurant_id.toString()
                            )
                            cacheManager.put(TechresEnum.KEY_CHECK_LOAD_REVIEW.toString(), "false")
//                            updateRestaurantId(restaurantCard.restaurant_id)
                            updateRestaurant(restaurantCard.restaurant_id)

                            mainActivity?.clearBackstack()
                            mainActivity?.supportFragmentManager?.commit {
                                setCustomAnimations(
                                    R.anim.translate_from_right,
                                    R.anim.translate_to_left,
                                    R.anim.translate_from_left,
                                    R.anim.translate_to_right
                                )
                                add<MainFragment>(R.id.frameContainer, args = bundle)
                                addToBackStack(null)
                            }
                        } else {
                            lastText = ""
                            context?.let {
                                FancyToast.makeText(
                                    it,
                                    response.message,
                                    FancyToast.LENGTH_LONG, FancyToast.ERROR,
                                    false
                                ).show()
//                                binding.barcodeView.resume()
                               // mainActivity?.supportFragmentManager?.popBackStack()
                            }
                        }

                    }
                })
        }
        mainActivity?.setLoading(false)
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
        params.project_id = AppConfig.PROJECT_CHAT
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleOnRequestPermissionResult(
            requiredPermissionCameraCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_camera),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                        R.drawable.ic_pink_camera, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                requestPermission.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_approve_permission_camera),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_camera_step_two),
                        R.drawable.ic_pink_camera, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionGranted() {
                    binding.barcodeView.initializeFromIntent(mainActivity?.intent)
                    binding.barcodeView.decodeContinuous(callback)
                    binding.barcodeView.barcodeView.cameraSettings.requestedCameraId =
                        Camera.CameraInfo.CAMERA_FACING_BACK
                    binding.barcodeView.resume()
                }
            }
        )

    }

    override fun onBackPress(): Boolean {
        return true
    }

}
