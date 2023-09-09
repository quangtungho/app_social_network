package vn.techres.line.activity

import android.os.Bundle
import android.text.util.Linkify
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.giphy.sdk.analytics.GiphyPingbacks.context
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.utils.SpinnerAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.ItemSpinner
import vn.techres.line.data.model.params.FeedbackForDevelopersParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityFeedbackForDevelopersBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class FeedbackForDevelopersActivity : BaseBindingActivity<ActivityFeedbackForDevelopersBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityFeedbackForDevelopersBinding
        get() = ActivityFeedbackForDevelopersBinding::inflate
    private var type: String? = null
    var gson = Gson()
    var user = User()
    var images: ArrayList<Int> = arrayListOf(
        0,
        R.drawable.ic_clock_organe,
        R.drawable.ic_grow_organe,
        R.drawable.ic_error_organe
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(this)
    }

    override fun onSetBodyView() {
        this.window?.statusBarColor = AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.feedback)
        binding.edtNameFeedback.text = CurrentUser.getCurrentUser(this).name
        binding.edtPhoneNumberFeedback.text = CurrentUser.getCurrentUser(this).phone
        //-----------------LINK Support-Phone_Website-----//
        binding.linkSupport.text = resources.getString(R.string.infomation_support_mail)
        binding.linkPhoneNumber.text = resources.getString(R.string.infomation_phone_number)
        binding.linkWebsite.text = resources.getString(R.string.infomation_website_link)
        Linkify.addLinks(binding.linkSupport, Linkify.EMAIL_ADDRESSES)
        Linkify.addLinks(binding.linkWebsite, Linkify.WEB_URLS)
        Linkify.addLinks(
            binding.linkPhoneNumber,
            Patterns.PHONE,
            "tel:",
            Linkify.sPhoneNumberMatchFilter,
            Linkify.sPhoneNumberTransformFilter
        )
        setListener()
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.spinnerFeedbackForDev.adapter = SpinnerAdapter(
            this,
            listOf(
                ItemSpinner(
                    R.drawable.ic_clock_organe,
                    baseContext.getString(R.string.feedbackOne)
                ),
                ItemSpinner(
                    R.drawable.ic_grow_organe,
                    baseContext.getString(R.string.feedbackTwo)
                ),
                ItemSpinner(
                    R.drawable.ic_error_organe,
                    baseContext.getString(R.string.feedbackThree)
                )
            )
        )
        binding.spinnerFeedbackForDev.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    binding.spinnerFeedbackForDev.adapter = SpinnerAdapter(
                        this@FeedbackForDevelopersActivity,
                        listOf(
                            ItemSpinner(
                                0,
                                baseContext.getString(R.string.choose_feedback)
                            )
                        )
                    )
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    type = (position + 1).toString()
                }
            }
        binding.btnPostFeedback.setOnClickListener {
            if (binding.edtContentFeedback.text.toString() == "") {
                Utils.setSnackBar(
                    binding.btnPostFeedback,
                    baseContext.getString(R.string.please_input_content)
                )
            } else {
                onCreateFeedback()
            }

        }
    }

    private fun onCreateFeedback() {
        val params = FeedbackForDevelopersParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/configs/app-error"
        params.params.error = binding.edtContentFeedback.text.toString()
        params.params.type = type
        ServiceFactory.run {
            createRetrofitService(
                TechResService::class.java
            )
                .feedbackForDevelopers(params)
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
//                            context.let {
//                                binding.edtContentFeedback.setText("")
//                                Utils.setSnackBar(
//                                    binding.btnPostFeedback,
//                                    resources.getString(R.string.tks_feedback_for_your)
//                                )
//                            }
                            Toast.makeText(
                                this@FeedbackForDevelopersActivity,
                                resources.getString(R.string.tks_feedback_for_your),
                                Toast.LENGTH_SHORT
                            ).show()
//
                            onBackPressed()
                        } else
                            Utils.setSnackBar(
                                binding.btnPostFeedback,
                                response.message.toString()
                            )
                    }

                })
        }
    }
}