package vn.techres.line.fragment.account

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.LoginActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.params.ForgotPasswordParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.databinding.FragmentForgotPasswordBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Utils.hideKeyboard
import vn.techres.line.helper.Utils.setSnackBar
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

/**
 * A simple [Fragment] subclass.
 */
class ForgotPasswordFragment :
    BaseBindingFragment<FragmentForgotPasswordBinding>(FragmentForgotPasswordBinding::inflate) {
    private var phone = ""
    private val loginActivity: LoginActivity?
        get() = activity as LoginActivity?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.btnContinue.setOnClickListener {
            hideKeyboard(binding.btnContinue)
            if (binding.edtPhoneForgot.text.toString().length < 10) {
                setSnackBar(
                    binding.edtPhoneForgot,
                    requireActivity().resources.getString(R.string.input_phone_false)
                )
            } else {
                forgotPassword()
            }
        }
        binding.head.toolbarBack.setOnClickListener {
            hideKeyboard(binding.btnContinue)
            loginActivity?.supportFragmentManager?.popBackStack()
        }
        binding.head.toolbarTitle.text = "Quên Mật Khẩu"
        binding.edtPhoneForgot.addTextChangedListener(textPhoneWatcher)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    fun senDataBundle() {
        val bundle = Bundle()
        cacheManager.put(
            TechresEnum.FORGOT_PASSWORD.toString(),
            TechresEnum.FORGOT_PASSWORD.toString()
        )
        bundle.putString(
            TechresEnum.CUSTOMER_PHONE.toString(),
            binding.edtPhoneForgot.text.toString()
        )
        val verifyPhoneFragment = VerifyPhoneFragment()
        verifyPhoneFragment.arguments = bundle
        loginActivity?.addOnceFragment(this, verifyPhoneFragment)
    }
    private fun checkInput(): Boolean {
        return phone != ""
    }

    private val textPhoneWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun afterTextChanged(s: Editable) {
            phone = s.toString()
            if (!checkInput()) {
                binding.btnContinue.setBackgroundResource(R.drawable.bg_btn_login_empty)
                binding.btnContinue.isEnabled = false
            } else {
                binding.btnContinue.setBackgroundResource(R.drawable.bg_btn_login)
                binding.btnContinue.isEnabled = true
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    private fun forgotPassword() {
        loginActivity?.setLoading(true)
        val params = ForgotPasswordParams()
        params.http_method = AppConfig.POST
        params.project_id = AppConfig.PROJECT_OAUTH
        params.request_url = "/api/customers/forgot-password"

        params.params.phone = binding.edtPhoneForgot.text.toString()
        params.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .forgotPassword(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        loginActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        loginActivity?.setLoading(false)
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            senDataBundle()
                        } else setSnackBar(
                            binding.btnContinue,response.message.toString()
                        )
                    }
                })
        }
    }


    override fun onBackPress(): Boolean {
        return true
    }
}
