package vn.techres.line.broadcast

import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.contact.ContactData
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.ContactParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.ContactUtils.syncContactDevice
import vn.techres.line.interfaces.contact.ContactUtilsHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class ContactContentObserver : ContentObserver(Handler(Looper.getMainLooper())) {

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        TechResApplication.applicationContext().syncContactDevice(object : ContactUtilsHandler {
            override fun onSyncContact(result: ArrayList<ContactData>) {
                updatePhoneUser(result)
            }

            override fun onSyncEndContact() {
                createContactsTogether()
            }

            override fun onSyncDbAndDevice(result: ArrayList<ContactData>) {
                if(result.size > 0){
                    updatePhoneUser(result).apply {
                        createContactsTogether()
                    }
                }
            }
        })
    }

    override fun deliverSelfNotifications(): Boolean {
        return true
    }

    private fun updatePhoneUser(array: ArrayList<ContactData>){
        val arrayList = array.map { it.phone?.get(0) ?: ""}.toMutableList()
        val params = ContactParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customer-phones/create"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.list_phone = arrayList
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createContact(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())

                    }
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                    }
                })
        }
    }

    private fun createContactsTogether() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api/customer-phones/create-friend"
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .createContactTogether(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                }
            })
    }

}