package vn.techres.line.roomdatabase.sync

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.local.contact.bestfriend.BestFriendResponse
import vn.techres.line.data.local.contact.friendonline.FriendOnlineResponse
import vn.techres.line.data.local.contact.friendrequestcontact.FriendRequestContactResponse
import vn.techres.line.data.local.contact.myfriend.MyFriendResponse
import vn.techres.line.data.local.contact.newfriend.NewFriendResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.EventBusFinishSyncContact
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SyncDataUpdateContact(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private var user = CurrentUser.getCurrentUser(appContext)
    private var count = 0

    override fun doWork(): Result {
        // Do the work here--in this case
        TechResApplication.applicationContext().getFriendDao()?.deleteAllData()
        callApiFriend()
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun callApiFriend() {
        getListFriend()
        getNewFriend()
        getFriendOnline()
        getFriendRequestContact()
        getBestFriend()
    }


    private fun getListFriend() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/friends?user_id=${user.id}&limit=${-1}&page=${1}"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getListFriend(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MyFriendResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: MyFriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            val friendList = response.data.list
                            TechResApplication.applicationContext().getFriendDao()
                                ?.insertAllData(friendList)
                            count += 1
                            callAppSuccess()
                        }

                    }
                }
            })
    }

    private fun getNewFriend() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = String.format("/api/friends/new-friend?limit=%s&page=%s", -1, 1)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getFriendsNew(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<NewFriendResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: NewFriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            val friendList = response.data.list
                            TechResApplication.applicationContext().getFriendDao()
                                ?.insertAllData(friendList)
                            count += 1
                            callAppSuccess()
                        }
                    }
                }
            })
    }

    private fun getFriendOnline() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url =
            String.format("%s%s%s%s", "/api/friends/online?limit=", -1, "&page=", 1)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListFriendOnline(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendOnlineResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: FriendOnlineResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            val friendList = response.data.list
                            TechResApplication.applicationContext().getFriendDao()
                                ?.insertAllData(friendList)
                            count += 1
                            callAppSuccess()
                        }
                    }
                }
            })
    }

    private fun getFriendRequestContact() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/contact-froms/total-request"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .getFriendTotalRequest(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendRequestContactResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("SetTextI18n")
                override fun onNext(response: FriendRequestContactResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            val friendList = response.data.contact_from.list
                            TechResApplication.applicationContext().getFriendDao()
                                ?.insertAllData(friendList)
                            count += 1
                            callAppSuccess()
                        }
                    }
                }
            })
    }

    private fun getBestFriend() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url =
            String.format("%s%s%s%s", "/api/friends/close-friend?limit=", -1, "&page=", 1)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListBestFriend(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BestFriendResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BestFriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            val friendList = response.data.list
                            TechResApplication.applicationContext().getFriendDao()
                                ?.insertAllData(friendList)
                            count += 1
                            callAppSuccess()
                        }
                    }
                }
            })
    }

    //Handle call app success
    private fun callAppSuccess() {
        when (count) {
            5 -> {
                EventBus.getDefault().post(EventBusFinishSyncContact(true))
            }
        }
    }
}