package vn.techres.line.fragment.account


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.alexbbb.uploadservice.MultipartUploadRequest
import com.androidadvance.topsnackbar.TSnackbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.LoginActivity
import vn.techres.line.activity.PrivacyPolicyActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateCustomerMediaParams
import vn.techres.line.data.model.params.RegistryRequestParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.ImageResponse
import vn.techres.line.databinding.FragmentRegistryBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.generateID
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class RegistryFragment :
    BaseBindingFragment<FragmentRegistryBinding>(FragmentRegistryBinding::inflate) {
    private val loginActivity: LoginActivity?
        get() = activity as LoginActivity?

    private var phone: String? = null
    private var avatar: String? = ""
    private var avatarPath: String? = ""
    private var avatarName: String? = ""
    private var isGender: Int? = 2
    private val REQUEST_PICKER_AVATAR = 1
    private val userAgent = "Techres/" + com.alexbbb.uploadservice.BuildConfig.VERSION_NAME

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.white)

        binding.btnRegister.setOnClickListener {
            Utils.hideKeyboard(binding.btnRegister)
            binding.btnRegister.isEnabled = false
            Handler().postDelayed({ binding.btnRegister.isEnabled = true }, 3000)
            checkRegistry()
        }

        binding.imgBack.setOnClickListener {
            Utils.hideKeyboard(binding.btnRegister)
            loginActivity?.supportFragmentManager?.popBackStack()
        }

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            isGender = when (checkedId) {
                R.id.rbMale -> 1
                R.id.rbFemale -> 0
                else -> 2
            }
        }

        binding.edBirthday.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireActivity(), R.style.SheetDialog)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_date_picker)
            bottomSheetDialog.setCancelable(false)
            val imgClose = bottomSheetDialog.findViewById<ImageView>(R.id.imgClose)
            val btnConfirm = bottomSheetDialog.findViewById<Button>(R.id.btnConfirm)
            val datePicker =
                bottomSheetDialog.findViewById<com.ycuwq.datepicker.date.DatePicker>(R.id.datePicker)
            var dateOfBirth = ""
            var sMonth: String
            var sDay: String
            datePicker?.setOnDateSelectedListener { year, month, day ->
                sDay = if (day < 10) {
                    "0$day"
                } else {
                    "$day"
                }
                sMonth = if (month < 10) {
                    "0$month"
                } else {
                    "$month"
                }
                dateOfBirth = String.format("%s/%s/%s", sDay, sMonth, year)
            }

            imgClose?.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            btnConfirm?.setOnClickListener {
                binding.edBirthday.setText(dateOfBirth)
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        binding.imgCameraEditProfile.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .maxSelectNum(1)
                .minSelectNum(0)
                .isMaxSelectEnabledMask(true)
                .isOpenClickSound(true)
                .forResult(REQUEST_PICKER_AVATAR)
        }

        binding.txtPrivacyPolicy.setOnClickListener {
            val intent = Intent(loginActivity, PrivacyPolicyActivity::class.java)
            intent.putExtra(TechresEnum.WEBVIEW_TYPE.toString(), 0)
            loginActivity?.startActivity(intent)
            loginActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        binding.txtTermsUse.setOnClickListener {
            val intent = Intent(loginActivity, PrivacyPolicyActivity::class.java)
            intent.putExtra(TechresEnum.WEBVIEW_TYPE.toString(), 1)
            loginActivity?.startActivity(intent)
            loginActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val selectList = PictureSelector.obtainMultipleResult(data)
            if (selectList.size != 0) {
                val file = File(selectList[0].realPath)
                if (requestCode == REQUEST_PICKER_AVATAR) {
                    Glide.with(this)
                        .load(
                            file.path
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .apply(
                            RequestOptions().placeholder(R.drawable.ic_user_placeholder)
                                .error(R.drawable.ic_user_placeholder)
                        )
                        .into(binding.imgAvatar)
                    avatarPath = file.path
                    avatarName = file.name
                }
            }
        }
    }

    private fun onRegister(avatar: String) {
        loginActivity?.setLoading(true)
        val registryRequestParams = RegistryRequestParams()
        registryRequestParams.request_url = "/api/customers/register"
        registryRequestParams.http_method = AppConfig.POST
        registryRequestParams.project_id = AppConfig.PROJECT_OAUTH
        registryRequestParams.params.avatar = avatar
        registryRequestParams.params.last_name = binding.edtLastNameCustomer.text.toString()
        registryRequestParams.params.first_name = binding.edtFirstNameCustomer.text.toString()
        registryRequestParams.params.device_uid = generateID(requireActivity())
        registryRequestParams.params.phone = binding.edPhone.text.toString()
        registryRequestParams.params.birthday = binding.edBirthday.text.toString()
        registryRequestParams.params.gender = isGender


        registryRequestParams.let {
            ServiceFactory.createRetrofitService(

                TechResService::class.java
            )
                .register(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        loginActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: BaseResponse) {
                        loginActivity?.setLoading(false)
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            phone = binding.edPhone.text.toString()
                            senDataBundle()
                            onCreateMedia(1, avatar)

                        } else {
                            Utils.setSnackBar(
                                binding.btnRegister, response.message.toString()
                            )
                        }

                    }
                })
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkRegistry() {
        var date: LocalDate? = null
        if (binding.edBirthday.text.toString().isNotEmpty()) {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            date = LocalDate.parse(binding.edBirthday.text.toString(), formatter)
        }

        if (binding.edPhone.text.toString().trim() == "" || binding.edBirthday.text.toString()
                .trim() == "" || binding.edtLastNameCustomer.text.toString()
                .trim() == "" || binding.edtFirstNameCustomer.text.toString()
                .trim() == ""
//            || avatarPath == ""
        ) {
            setSnackBar(requireActivity().resources.getString(R.string.input_enough))
        } else if (binding.edPhone.text.toString().trim() == "") {
            setSnackBar("Số điện thoại không được để trống!!!")
        } else if (binding.edPhone.text.toString().trim().length < 10) {
            setSnackBar(requireActivity().resources.getString(R.string.input_phone_false))
        } else if (binding.edBirthday.text.toString().trim() == "") {
            setSnackBar("Ngày sinh không được để trống!!!")
        } else if (date!! > LocalDate.now()) {
            setSnackBar("Ngày sinh không được lớn hơn ngày hiện tại!!!")
        } else if (binding.edtLastNameCustomer.text.toString()
                .trim() == "" || binding.edtFirstNameCustomer.text.toString().trim() == ""
        ) {
            setSnackBar("Họ tên không được để trống!!!")
        }
//        else if (avatarPath == "") {
//            setSnackBar("Ảnh đại diện không được để trống!!!")
//        }
        else {
            if (binding.edPhone.text.toString().trim().length < 10) {
                setSnackBar(requireActivity().resources.getString(R.string.input_phone_false))
            } else {
                onRegister(avatar!!)
//                getLinkAvatar(avatarName ?: "")
//                uploadPhoto(avatarName ?: "", avatarPath ?: "", 1)
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun senDataBundle() {
        val bundle = Bundle()
        bundle.putString(TechresEnum.CUSTOMER_PHONE.toString(), phone)
        val verifyPhoneFragment = VerifyPhoneFragment()
        verifyPhoneFragment.arguments = bundle
        loginActivity?.addOnceFragment(this, verifyPhoneFragment)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setSnackBar(s: String) {
        val snackBar: TSnackbar = TSnackbar.make(
            view!!,
            s,
            TSnackbar.LENGTH_LONG
        )
        snackBar.setActionTextColor(Color.WHITE)
        val snackBarView: View = snackBar.view
        snackBarView.setBackgroundColor(Color.parseColor("#FFA233"))
        val textView =
            snackBarView.findViewById<View>(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackBar.show()
    }

    private fun getLinkAvatar(nameFile: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url =
            "/api-upload/get-link-file-by-user-customer?type=1&name_file=$nameFile&width=0&height=0"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getLinkImageReviewRestaurent(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ImageResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: ImageResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        avatar = response.data!!.link_original
                        onRegister(avatar!!)
                    }
                }
            })
    }

    private fun uploadPhoto(avatarName: String, avatarPath: String, type: Int) {
        val nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        val user = CurrentUser.getCurrentUser(requireActivity())
        //============ Upload image to server via service ===============
        val serverUrlString: String =
            String.format(
                "%s/api-upload/upload-file-by-user-customer/%s/%s",
                nodeJs.api_ads,
                type,
                avatarName
            )
        val paramNameString = resources.getString(R.string.send_file)
        val uploadID = UUID.randomUUID().toString()
        try {
            MultipartUploadRequest(context, uploadID, serverUrlString)
                .addFileToUpload(avatarPath, paramNameString)
                .addHeader(
                    resources.getString(R.string.Authorization),
                    user.nodeAccessToken
                )
                .setCustomUserAgent(userAgent)
                .setMaxRetries(3)
                .startUpload()
            // these are the different exceptions that may be thrown
        } catch (exc: FileNotFoundException) {
//            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        } catch (exc: IllegalArgumentException) {
//            Toast.makeText(
//                context,
//                "Missing some arguments. " + exc.message,
//                Toast.LENGTH_LONG
//            ).show()
        } catch (exc: MalformedURLException) {
//            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun onCreateMedia(mediaType: Int, urlMedia: String) {
        val createCustomerMediaParams = CreateCustomerMediaParams()
        createCustomerMediaParams.http_method = AppConfig.POST
        createCustomerMediaParams.request_url = "/api/customer-media-album-contents/create"

        createCustomerMediaParams.params.customer_media_album_id = -1
        createCustomerMediaParams.params.media_type = mediaType
        createCustomerMediaParams.params.url = urlMedia

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .createCustomerMediaRequest(createCustomerMediaParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {}
            })

    }

    override fun onBackPress(): Boolean {
        return true
    }
}
