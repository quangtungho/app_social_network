package vn.techres.line.fragment.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import coil.load
import coil.size.Scale
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
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.ProfileActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.address.Cities
import vn.techres.line.data.model.address.District
import vn.techres.line.data.model.address.Ward
import vn.techres.line.data.model.address.response.CitiesResponse
import vn.techres.line.data.model.address.response.DistrictsResponse
import vn.techres.line.data.model.address.response.WardResponse
import vn.techres.line.data.model.eventbus.UserUpdateEventBus
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateCustomerMediaParams
import vn.techres.line.data.model.params.UpdateProfileParams
import vn.techres.line.data.model.profile.response.UpdateProfileResponse
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.ImageResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentEditProfileBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.util.*

open class EditProfileFragment : BaseBindingFragment<FragmentEditProfileBinding>(FragmentEditProfileBinding::inflate) {
    private val profileActivity: ProfileActivity?
        get() = activity as ProfileActivity?
    
    private val userAgent = "Techres/" + com.alexbbb.uploadservice.BuildConfig.VERSION_NAME
    private val REQUEST_PICKER_COVER = 0
    private val REQUEST_PICKER_AVATAR = 1
    private var dataWards = ArrayList<Ward>()
    private var dataDistrict = ArrayList<District>()
    private var dataCities = ArrayList<Cities>()
    private var wardArray: List<String>? = null
    private var districtArray: List<String>? = null
    private var cityArray: List<String>? = null
    private var arrDate: List<String>? = null
    private var dataProfile: User? = null
    private var isGender: Int? = 2
    private var idWardNew: Int? = 0
    private var idWard: Int? = 0
    private var idDistrictNew: Int? = 0
    private var idDistrict: Int? = 0
    private var idCityNew: Int? = 0
    private var idCity: Int? = 0
    private var nameWard: String? = ""
    private var nameDistrict: String? = ""
    private var nameCity: String? = ""
    private var cityPositionName: String? = ""
    private var districtPositionName: String? = ""
    private var wardPositionName: String? = ""
    private var avatar: String? = ""
    private var avatarUrl: String? = ""
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()
    private var listCoverUrl = ArrayList<String>()
    private var coverUrl = ArrayList<String>()

    //Picture
    private var themeId = 0
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        configJava = CurrentConfigJava.getConfigJava(requireActivity().baseContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        getProfile()

        themeId = R.style.picture_Sina_style

        binding.imgBack.setOnClickListener {
            profileActivity?.supportFragmentManager?.popBackStack()
        }
        binding.btnSave.setOnClickListener {
            pushUpdateProfile()
            if (binding.swNickName.isChecked){
                userNickNameNode(1)
            }else{
                userNickNameNode(0)
            }
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

        binding.txtEditCover.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .isGif(true)
                .maxSelectNum(1)
                .minSelectNum(0)
                .isMaxSelectEnabledMask(true)
                .isOpenClickSound(true)
                .forResult(REQUEST_PICKER_COVER)
        }

        binding.tvBirthdayCustomer.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(this.requireActivity(), R.style.SheetDialog)
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
                sDay = if (day < 10){
                    "0$day"
                }else{
                    "$day"
                }
                sMonth = if (month < 10){
                    "0$month"
                }else{
                    "$month"
                }
                dateOfBirth = String.format("%s/%s/%s", sDay, sMonth, year)
            }

            imgClose!!.setOnClickListener {
                bottomSheetDialog.dismiss()
            }

            btnConfirm!!.setOnClickListener {
                binding.tvBirthdayCustomer.setText(dateOfBirth)
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.show()
        }

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            isGender = when(checkedId){
                R.id.rbMale -> 1
                R.id.rbFemale -> 0
                else -> 2
            }
        }


        binding.dropdownCitiesCustomer.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                for (i in dataCities.indices) {
                    if (cityArray!![position] == dataCities[i].name) {
                        getDistricts(dataCities[i].id!!.toInt())
                        idCity = dataCities[i].id
                        nameCity = dataCities[i].name
                        break
                    }
                }
            }
        binding.dropdownDistrictsCustomer.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                for (i in dataDistrict.indices) {
                    if (districtArray!![position] == dataDistrict[i].name) {
                        getWards(dataDistrict[i].id!!.toInt())
                        idDistrict = dataDistrict[i].id
                        nameDistrict = dataDistrict[i].name
                        break
                    }
                }
            }
        binding.dropdownWardsCustomer.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                for (i in dataWards.indices) {
                    if (wardArray!![position] == dataWards[i].name) {
                        idWard = dataWards[i].id
                        nameWard = dataWards[i].name
                        break
                    }
                }
            }
        binding.tvLastNameCustomer.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tipLastNameCustomer.error = null
            }

        })

        binding.tvFirstNameCustomer.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tipFirstNameCustomer.error = null
            }

        })


    }

    private fun setBirthday() {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentMonth = now.get(Calendar.MONTH)
        val currentDay = now.get(Calendar.DAY_OF_MONTH)
        val date = String.format("%s/%s/%s", currentDay, currentMonth, currentYear)
        arrDate = date.split("/")
    }
    private fun setAdapterDropDown(
        autoCompleteTextView: AutoCompleteTextView,
        list: List<String>,
        string: String?
    ) {
        val adapter = ArrayAdapter(requireActivity().baseContext, R.layout.item_drop_down_cities, list)
        autoCompleteTextView.setAdapter(adapter)
        if (string != null) {
            autoCompleteTextView.setText(string, false)
        }
        adapter.notifyDataSetChanged()
    }




    private fun uploadPhoto(photo: File, type: Int) {
        //============ Upload image to server via service ===============
        val serverUrlString: String =
            String.format(
                "%s/api-upload/upload-file-by-user-customer/%s/%s",
                nodeJs.api_ads,
                type,
                photo.name
            )
        val paramNameString = resources.getString(R.string.send_file)
        val uploadID = UUID.randomUUID().toString()
        try {
            MultipartUploadRequest(profileActivity, uploadID, serverUrlString)
                .addFileToUpload(photo.path, paramNameString)
                .addHeader(
                    resources.getString(R.string.Authorization),
                    user.nodeAccessToken
                )
                .setCustomUserAgent(userAgent)
                .setMaxRetries(3)
                .startUpload()
        } catch (exc: FileNotFoundException) {
        } catch (exc: IllegalArgumentException) {
        } catch (exc: MalformedURLException) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val selectList = PictureSelector.obtainMultipleResult(data)
            if (selectList.isNotEmpty()){
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
                        .into(binding.imgEditProfile)
                    getLinkAvatar(file.name)
                    uploadPhoto(file, 1)
                }else if (requestCode == REQUEST_PICKER_COVER) {
                    Glide.with(this)
                        .load(
                            file.path
                        )
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .apply(
                            RequestOptions().placeholder(R.drawable.bgcoverprofile)
                                .error(R.drawable.bgcoverprofile)
                        )
                        .into(binding.imgCover)
                    getLinkCover(file.name)
                    uploadPhoto(file, 4)
                }
            }
        }
    }

    private fun getProfile() {

        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/customers/" + user.id

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getProfile(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<UpdateProfileResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: UpdateProfileResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataProfile = response.data
                        binding.imgEditProfile.load(
                            String.format(
                                "%s%s",
                                nodeJs.api_ads,
                                dataProfile!!.avatar_three_image.thumb
                            )
                        ) {
                            crossfade(true)
                            scale(Scale.FIT)
                            placeholder(R.drawable.ic_user_placeholder)
                            error(R.drawable.ic_user_placeholder)
                            size(500, 500)
                        }
                        if (dataProfile!!.cover_urls.size != 0) {
                            binding.imgCover.load(
                                String.format(
                                    "%s%s",
                                    nodeJs.api_ads,
                                    dataProfile!!.cover_urls[0]
                                )
                            ) {
                                crossfade(true)
                                scale(Scale.FIT)
                        
                                placeholder(R.drawable.bgcoverprofile)
                                error(R.drawable.bgcoverprofile)
                                size(500, 500)
                            }
                         
                        }else{
                            binding.imgCover.setImageResource(R.drawable.bgcoverprofile)
                        }

                    
                        avatar = dataProfile!!.avatar_three_image.thumb
                        coverUrl = dataProfile!!.cover_urls
                        binding.tvLastNameCustomer.text =
                            Editable.Factory.getInstance().newEditable(dataProfile!!.last_name)
                        binding.tvFirstNameCustomer.text =
                            Editable.Factory.getInstance().newEditable(dataProfile!!.first_name)


                        binding.tvNickNameCustomer.text =
                            Editable.Factory.getInstance().newEditable(dataProfile!!.nick_name)

                        binding.swNickName.isChecked = dataProfile!!.is_display_nick_name == 1

                        when(dataProfile!!.gender){
                            0 ->{
                                binding.rbFemale.isChecked = true
                                binding.rbMale.isChecked = false
                                isGender = 0
                            }
                            1 ->{
                                binding.rbFemale.isChecked = false
                                binding.rbMale.isChecked = true
                                isGender = 1
                            }
                            else ->{
                                binding.rbFemale.isChecked = false
                                binding.rbMale.isChecked = false
                                isGender = -1
                            }
                        }

                        binding.tvBirthdayCustomer.text =
                            Editable.Factory.getInstance().newEditable(dataProfile!!.birthday)
                        if (dataProfile!!.birthday != "") {
                            val date = dataProfile!!.birthday
                            arrDate = date.split("/")
                        } else {
                            setBirthday()
                        }
                        binding.tvPhoneCustomer.text =
                            Editable.Factory.getInstance().newEditable(dataProfile!!.phone)
//                            setTextFieldTrue(tipPhoneCustomer!!)
//                            tvPhoneCustomer!!.error = resources.getString(R.string.phone_customer_error)

                        binding.tvEmailCustomer.text =
                            Editable.Factory.getInstance().newEditable(dataProfile!!.email)
                        binding.tvApartmentNumberCustomer.text = Editable.Factory.getInstance()
                            .newEditable(dataProfile!!.street_name)
                        idWardNew = dataProfile!!.ward_id
                        idDistrictNew = dataProfile!!.district_id
                        idCityNew = dataProfile!!.city_id
                        getCities()
                        if (idCityNew != 0) {
                            getDistricts(idCityNew!!)
                            if (idDistrictNew != 0) {
                                getWards(idDistrictNew!!)
                            }
                        }

                    }

                }
            })
    }

    private fun pushUpdateProfile() {
        if (binding.tvFirstNameCustomer.text.toString() == "" || binding.tvLastNameCustomer.text.toString() == "") {
            setSnackbar(requireActivity().baseContext.getString(R.string.input_first_last_name))

        } else {
            val params = UpdateProfileParams()
            params.http_method = 1
            params.request_url = "/api/customers/" + user.id
            params.params.last_name = binding.tvLastNameCustomer.text.toString().trim()
            params.params.first_name = binding.tvFirstNameCustomer.text.toString().trim()
            params.params.nick_name = binding.tvNickNameCustomer.text.toString().trim()
            params.params.node_access_token = user.nodeAccessToken
            if (binding.tvNickNameCustomer.text.toString() != ""){
                if (binding.swNickName.isChecked) {
                    params.params.is_display_nick_name = 1
                } else {
                    params.params.is_display_nick_name = 0
                }
            }else{
                params.params.is_display_nick_name = 0
            }
            params.params.email = binding.tvEmailCustomer.text.toString().trim()
            params.params.birthday = binding.tvBirthdayCustomer.text.toString()
            params.params.gender = isGender

            if (avatarUrl == "") {
                params.params.avatar = avatar
            } else {
                params.params.avatar = avatarUrl
            }
            if (listCoverUrl.isEmpty()) {
                params.params.cover_urls = coverUrl
            } else {
                params.params.cover_urls = listCoverUrl
            }
          
            params.params.city_id = idCity.toString()
            params.params.city_name = nameCity.toString()
            params.params.district_id = idDistrict.toString()
            params.params.district_name = nameDistrict.toString()
            params.params.ward_id = idWard.toString()
            params.params.ward_name = nameWard.toString()
            params.params.street_name = binding.tvApartmentNumberCustomer.text.toString().trim()
            params.params.phone_number = binding.tvPhoneCustomer.text.toString().trim()
            params.let {
                ServiceFactory.createRetrofitService(
              
                    TechResService::class.java
                )

                    .updateProfile(params)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<UpdateProfileResponse> {
                        override fun onComplete() {}
                        override fun onError(e: Throwable) {
                            WriteLog.d("ERROR", e.message.toString())
                        }

                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(response: UpdateProfileResponse) {
                            if (response.status == AppConfig.SUCCESS_CODE) {
                                val userUpdate = response.data
                                EventBus.getDefault().post(UserUpdateEventBus(userUpdate))

                                user.avatar_three_image.medium = userUpdate.avatar
                                user.avatar_three_image.thumb = userUpdate.avatar
                                user.avatar_three_image.original = userUpdate.avatar
                                user.avatar = userUpdate.avatar
                                user.name = userUpdate.name
                                user.nick_name = userUpdate.nick_name

                                CurrentUser.saveUserInfo(activity!!, user)

                                requireActivity().supportFragmentManager.popBackStack()
                            } else Toast.makeText(
                                requireActivity(),
                                response.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            }
        }


    }

    private fun getWards(position: Int) {
        
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/address/district/$position/wards"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getWards(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<WardResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: WardResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataWards = response.data.list
                        if(dataWards.isEmpty()){
                            binding.dropdownWardsCustomer.text =
                                Editable.Factory.getInstance().newEditable(
                                    ""
                                )
                            idWard = 0
                            nameWard = ""
                        }else{
                            wardArray = dataWards.map { it.name!! }
                            if (idWardNew == 0) {
                                binding.dropdownWardsCustomer.text =
                                    Editable.Factory.getInstance().newEditable(
                                        wardArray!![0]
                                    )
                                wardPositionName = dataWards[0].name
                                nameWard = dataWards[0].name
                                idWard = dataWards[0].id
                            }
                            for (value in dataWards) {
                                if (value.id == idWardNew) {
                                    wardPositionName = value.name
                                    nameWard = value.name
                                    idWard = idWardNew
                                    break
                                }
                            }
                            setAdapterDropDown(
                                binding.dropdownWardsCustomer,
                                wardArray!!,
                                wardPositionName
                            )
                            wardPositionName = null
                            idWardNew = 0
                        }

                    } else Toast.makeText(
                        requireActivity(),
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

            
    }

    private fun getDistricts(position: Int) {
       
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/address/city/$position/districts"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getDistricts(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DistrictsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: DistrictsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataDistrict = response.data.list
                        districtArray = dataDistrict.map { it.name!! }
                        if(idDistrictNew == 0){
                            binding.dropdownWardsCustomer.text =
                                Editable.Factory.getInstance().newEditable(
                                    ""
                                )
                            binding.dropdownDistrictsCustomer.text =
                                Editable.Factory.getInstance().newEditable(
                                    districtArray!![0]
                                )
                        }else{
                            for (value in dataDistrict) {
                                if (value.id == idDistrictNew) {
                                    districtPositionName = value.name
                                    nameDistrict = value.name
                                    idDistrict = idDistrictNew

                                    break
                                }
                            }
                        }
                        setAdapterDropDown(
                            binding.dropdownDistrictsCustomer,
                            districtArray!!,
                            districtPositionName
                        )
                        districtPositionName = null
                        idDistrictNew = 0
                    } else Toast.makeText(
                        requireActivity(),
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
                    
    }

    private fun getCities() {
      
        val params = BaseParams()
        params.http_method = 0
        params.request_url = "/api/address/cities"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getCities(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CitiesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: CitiesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataCities = response.data.list
                        cityArray = dataCities.map { it.name!! }
                        for (value in dataCities) {
                            if (value.id == idCityNew) {
                                cityPositionName = value.name
                                nameCity = value.name
                                idCity = idCityNew
                                break
                            }
                        }
                        setAdapterDropDown(
                            binding.dropdownCitiesCustomer,
                            cityArray!!,
                            cityPositionName
                        )
                        cityPositionName = null
                    } else Toast.makeText(
                        requireActivity(),
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
                   
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
                        avatarUrl = response.data!!.link_original

                        onCreateMedia(1, response.data!!.link_original)

                    } else {
                        Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG).show()
//                        requireActivity().setEnableUpdateProfile(true)
                    }
                }
            })
       


    }

    private fun getLinkCover(nameFile: String) {
       
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url =
            "/api-upload/get-link-file-by-user-customer?type=4&name_file=$nameFile&width=0&height=0"
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
                        val coverUrl = response.data!!.link_original
                        listCoverUrl.clear()
                        listCoverUrl.add(coverUrl)
                        onCreateMedia(1, response.data!!.link_original)
                    } else {
                        Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG).show()
//                        requireActivity().setEnableUpdateProfile(true)
                    }
                }
            })

    }



    private fun userNickNameNode(isDisplayNickname: Int){
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url= "/api/user/use-nick-name?is_display_nick_name=$isDisplayNickname"
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getContact(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
//                            .show()
                    }
                }
            })
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
                override fun onNext(response: BaseResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                        }
                        else -> {
                            Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
  
                }
            })

    }

    @SuppressLint("UseRequireInsteadOfGet")

    private fun setSnackbar(s: String) {
        val snackbar: TSnackbar = TSnackbar.make(
            view!!,
            s,
            TSnackbar.LENGTH_LONG
        )
        snackbar.setActionTextColor(Color.WHITE)
        val snackbarView: View = snackbar.view
        snackbarView.setBackgroundColor(Color.parseColor("#FFA233"))
        val textView =
            snackbarView.findViewById<View>(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
        textView.setTextColor(Color.WHITE)
        snackbar.show()
    }


    override fun onBackPress() : Boolean {
        return true
    }

}
