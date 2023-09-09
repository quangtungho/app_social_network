package vn.techres.line.fragment.birthday

import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateReceiveBirthdayParams
import vn.techres.line.data.model.profile.response.UpdateProfileResponse
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.InfoBirthdayGift
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentFormConfirmBirthdayInfoBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class FormConfirmBirthdayInfoFragment : BaseBindingFragment<FragmentFormConfirmBirthdayInfoBinding>(FragmentFormConfirmBirthdayInfoBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var infoBirthdayGif = InfoBirthdayGift()
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
        binding.header.tvTitleHomeHeader.text = ""
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        arguments?.let {
            val json = it.getString(TechresEnum.KEY_BRANCH_NAME.toString())
            infoBirthdayGif = Gson().fromJson(json, object :
                TypeToken<InfoBirthdayGift>() {}.type)
            Glide.with(this).load(infoBirthdayGif.image_url).into(binding.imgLogoBranchs)
            binding.txtDetailNameFoods.text = infoBirthdayGif.branch_name
            getProfile()
            binding.btnConfirm.setOnClickListener {
                createConfirm()
            }
        }
        binding.header.btnBack.setOnClickListener {
            onBackPress()
        }
    }

    private fun getProfile() {
        mainActivity?.setLoading(true)
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
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: UpdateProfileResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val dataProfile = response.data
                        context?.let {
                            binding.edtName.text =
                                Editable.Factory.getInstance().newEditable(dataProfile.name)
                            binding.edtPhone.text =
                                Editable.Factory.getInstance().newEditable(dataProfile.phone)
                            binding.edtAddress.text = Editable.Factory.getInstance()
                                .newEditable(dataProfile.address_full_text)
                        }

                    }
                    mainActivity?.setLoading(false)
                }
            })

    }

    private fun createConfirm() {
        mainActivity?.setLoading(true)
        val params = CreateReceiveBirthdayParams()
        params.http_method = AppConfig.POST
        params.request_url =
            String.format("%s%s%s", "/api/customers/", user.id, "/receive-birthday-gift-request")

        params.params.customer_name = binding.edtName.text.toString()
        params.params.customer_phone = binding.edtPhone.text.toString()
        params.params.customer_address = binding.edtAddress.text.toString()
        params.params.customer_id = user.id
        params.params.branch_id = infoBirthdayGif.branch_id
        params.params.gift = infoBirthdayGif.gift
        params.params.restaurant_birthday_gift_id = infoBirthdayGif.id
        params.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .createReceiveBirthday(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d(TechresEnum.TAG_ERROR.toString(), e.message.toString())
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
//                            mainActivity?.let {
//                                Navigation.findNavController(
//                                    it,
//                                    R.id.nav_host
//                                ).navigate(R.id.confirmFragment)
//                            }
                        } else Toast.makeText(
                            mainActivity,
                            mainActivity!!.baseContext.resources.getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        )
                            .show()
                        mainActivity?.setLoading(false)
                    }
                })
        }
    }

    override fun onBackPress() : Boolean{
        return true
    }

}
