package vn.techres.line.activity

import android.content.Intent
import android.view.LayoutInflater
import android.widget.Toast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.databinding.ActivityDisableAccountBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class DisableAccountActivity : BaseBindingActivity<ActivityDisableAccountBinding>(){
    var id = 0
    override val bindingInflater: (LayoutInflater) -> ActivityDisableAccountBinding
        get() = ActivityDisableAccountBinding::inflate

    override fun onSetBodyView() {
        id = CurrentUser.getCurrentUser(this).id
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnDelete.setOnClickListener {
            disableAccount()
        }
    }

    private fun disableAccount() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 1
        baseRequest.request_url = "/api/customers/$id/is-deactived"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .removePost(baseRequest)
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
                        Toast.makeText(this@DisableAccountActivity, "Tài khoản của bạn đã xoá thành công", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@DisableAccountActivity, LoginActivity::class.java)
                        startActivity(intent)
                        this@DisableAccountActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        this@DisableAccountActivity.finish()
                    }

                }
            })
    }

}