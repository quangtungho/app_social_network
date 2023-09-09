package vn.techres.line.fragment.setting

import android.os.Bundle
import android.view.View
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.stranger.params.PermissionStrangerParams
import vn.techres.line.databinding.FragmentPermissionStrangerBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class PermissionStrangerFragment : BaseBindingFragment<FragmentPermissionStrangerBinding>(FragmentPermissionStrangerBinding::inflate){
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var defaultCallPhone = 0
    private var defaultCallVideo = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = requireActivity().resources.getString(R.string.permission_stranger)
        arguments?.let {
            defaultCallPhone = it.getInt(TechresEnum.PERMISSION_STRANGER_CALL_PHONE.toString(), 0)
            defaultCallVideo = it.getInt(TechresEnum.PERMISSION_STRANGER_CALL_VIDEO.toString(), 0)
            binding.switchButtonCallPhone.isChecked = defaultCallPhone == 1
            binding.switchButtonCallVideo.isChecked = defaultCallVideo == 1
        }

        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }

        binding.switchButtonCallPhone.setOnCheckedChangeListener { _, isCheckedDefault ->
            defaultCallPhone = when (isCheckedDefault) {
                true -> {
                    1
                }
                false -> {
                    0
                }
            }
        }

        binding.switchButtonCallVideo.setOnCheckedChangeListener { _, isCheckedDefault ->
            defaultCallVideo = when (isCheckedDefault) {
                true -> {
                    1
                }
                false -> {
                    0
                }
            }
        }
    }

    private fun setPermissionStranger(){
        val permissionStrangerParams = PermissionStrangerParams()
        permissionStrangerParams.http_method = AppConfig.POST
        permissionStrangerParams.project_id = AppConfig.getProjectChat()
        permissionStrangerParams.request_url = "/api/user/update-permission-call"
        permissionStrangerParams.params.is_call_phone = defaultCallPhone
        permissionStrangerParams.params.is_call_video = defaultCallPhone
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .setPermissionStranger(permissionStrangerParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
//                    if(response.status == AppConfig.SUCCESS_CODE){
//                    }
                }
            })
    }
    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onBackPress(): Boolean {
        setPermissionStranger()
        return true
    }

}