package vn.techres.line.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.giphy.sdk.analytics.GiphyPingbacks.context
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.AdvertContractParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityFormRegistryAdvertBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class FormRegistryAdvertActivity : BaseBindingActivity<ActivityFormRegistryAdvertBinding>() {

    override val bindingInflater: (LayoutInflater) -> ActivityFormRegistryAdvertBinding
        get() = ActivityFormRegistryAdvertBinding::inflate
    private var user = User()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(this)
    }

    override fun onSetBodyView() {
        this.window?.statusBarColor = AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.form_registry_advert)
        setData()
        setListener()
    }

    private fun setData() {
        binding.edtNameCustomer.text =
            Editable.Factory.getInstance().newEditable(CurrentUser.getCurrentUser(this).name)
        binding.edtPhoneCustomer.text =
            Editable.Factory.getInstance().newEditable(CurrentUser.getCurrentUser(this).phone)
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.btnActivateForm.setOnClickListener {
            if (binding.edtNameCustomer.text?.isEmpty() == true) {
                setTextFieldError(binding.tipNameCustomer)
                binding.edtNameCustomer.error = resources.getString(R.string.name_customer_error)
            } else {
                setTextFieldTrue(binding.tipNameCustomer)
                binding.edtNameCustomer.error = null
            }
            //phone
            if (binding.edtPhoneCustomer.text?.isEmpty() == true) {
                setTextFieldError(binding.tipPhoneCustomer)
                binding.edtPhoneCustomer.error = resources.getString(R.string.phone_customer_error)
            } else {
                setTextFieldTrue(binding.tipPhoneCustomer)
                binding.edtPhoneCustomer.error = null
            }
            //address
            if (binding.edtAddressCustomer.text?.isEmpty() == true) {
                setTextFieldError(binding.tipAddressCustomer)
                binding.edtAddressCustomer.error = resources.getString(R.string.address_error)
            } else {
                setTextFieldTrue(binding.tipAddressCustomer)
                binding.edtAddressCustomer.error = null
            }
            //name company
            if (binding.edtNameCompanyCustomer.text?.isEmpty() == true) {
                setTextFieldError(binding.tipNameCompanyCustomer)
                binding.edtNameCompanyCustomer.error =
                    resources.getString(R.string.name_company_error)
            } else {
                setTextFieldTrue(binding.tipNameCompanyCustomer)
                binding.edtNameCompanyCustomer.error = null
            }
            if (binding.edtNameCustomer.text?.isNotEmpty() == true && binding.edtPhoneCustomer.text?.isNotEmpty() == true
                && binding.edtAddressCustomer.text?.isNotEmpty() == true && binding.edtNameCompanyCustomer.text?.isNotEmpty() == true
            ) {
                postCustomerAdvertContracts(
                    binding.edtNameCustomer.text.toString(),
                    binding.edtPhoneCustomer.text.toString(),
                    binding.edtEmailCustomer.text.toString(),
                    binding.edtAddressCustomer.text.toString(),
                    binding.edtNameCompanyCustomer.text.toString(),
                    binding.edtDescriptionAdvert.text.toString().trim()
                )
            }
        }
    }

    private fun setTextFieldTrue(textInputLayout: TextInputLayout) {
        textInputLayout.hintTextColor = ColorStateList.valueOf(
            ContextCompat.getColor(
                baseContext,
                R.color.main_bg
            )
        )
        textInputLayout.defaultHintTextColor =
            resources.getColorStateList(R.color.text_hint_appearance, null)
        textInputLayout.setBoxStrokeColorStateList(
            resources.getColorStateList(
                R.color.text_input_box_stroke,
                null
            )
        )
    }

    private fun setTextFieldError(textInputLayout: TextInputLayout) {
        textInputLayout.hintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.red))
        textInputLayout.defaultHintTextColor =
            ColorStateList.valueOf(ContextCompat.getColor(baseContext, R.color.red))
        textInputLayout.setBoxStrokeColorStateList(
            resources.getColorStateList(
                R.color.text_input_box_stroke_error,
                null
            )
        )
    }

    private fun postCustomerAdvertContracts(
        name: String,
        phone: String,
        email: String,
        address: String,
        companyName: String,
        note: String
    ) {
        val params = AdvertContractParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customer-advert-contacts/create"

        params.params.full_name = name
        params.params.phone = phone
        params.params.email = email
        params.params.address = address
        params.params.company_name = companyName
        params.params.note = note
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .postCustomerAdvertContracts(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val intent = Intent(
                            this@FormRegistryAdvertActivity,
                            AdvertisementRegistrySuccessActivity::class.java
                        )
                        startActivity(intent)
                        overridePendingTransition(
                            R.anim.translate_from_right,
                            R.anim.translate_to_right
                        )
                    } else Toast.makeText(
                        this@FormRegistryAdvertActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

}