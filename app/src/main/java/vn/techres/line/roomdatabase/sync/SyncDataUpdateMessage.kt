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
import vn.techres.line.data.model.chat.response.MessagesResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.EventBusFinishSyncMessage
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SyncDataUpdateMessage(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private val tag = "SyncDataUpdateMessage"
    private var lastMessage ="-1"
    override fun doWork(): Result {
        inputData.let {
            lastMessage = it.getString(TechResEnumChat.LAST_MESSAGE_GROUP.toString()) ?: "-1"
            if (it.getString(TechResEnumChat.CONVERSATION_TYPE.toString()) == TechResEnumChat.GROUP.toString()) {
                lastMessage.let { it1 ->
                    getMessageGroupNext(
                        it.getString(TechResEnumChat.GROUP_ID.toString()) ?: "",
                        it1
                    )
                }
            } else {
                lastMessage.let { it1 ->
                    getMessagePersonNext(
                        it.getString(TechResEnumChat.GROUP_ID.toString()) ?: "",
                        it1
                    )
                }
            }

        }
        return Result.success()
    }

//    private fun getMessageGroupNext(
//        group_id: String,
//        next_cursors: String
//    ) {
//        val baseRequest = BaseParams()
//        baseRequest.http_method = AppConfig.GET
//        baseRequest.project_id = AppConfig.getProjectChat()
//        baseRequest.request_url = String.format(
//            "%s%s%s%s%s%s%s%s%s%s",
//            "/api/messages/pagination?group_id=",
//            group_id,
//            "&limit=",
//            10000,
//            "&random_key=",
//            -1,
//            "&pre_cursor=",
//            -1,
//            "&next_cursor=",
//            next_cursors
//        )
//        ServiceFactory.createRetrofitServiceNode(
//            TechResService::class.java
//        )
//            .getMessages(baseRequest)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : Observer<MessagesResponse> {
//                override fun onComplete() {}
//                override fun onError(e: Throwable) {
//                    WriteLog.d("ERROR", e.message.toString())
//                }
//
//
//                override fun onSubscribe(d: Disposable) {}
//
//
//                @SuppressLint("ShowToast")
//                override fun onNext(response: MessagesResponse) {
//                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        response.data.list.forEach { it.user_id = CurrentUser.getCurrentUser(applicationContext).id}
//                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
//                            TechResApplication.applicationContext().getMessageDao()
//                                ?.insertAllMessage(response.data.list)
//                        }
//                        EventBus.getDefault().post(EventBusFinishSyncMessage(true))
//                    }
//                }
//            })
//    }
    private fun getMessageGroupNext(
    groupId: String,
    randomKey: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s",
            "/api/messages/all-message-for-group?groupId=",
            groupId,
            "&randomKey=",
            randomKey,
            "&limit=",
            1000
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessages(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessagesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: MessagesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        response.data.list.forEach { it.user_id = CurrentUser.getCurrentUser(applicationContext).id}
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            if(randomKey.equals("-1")) {
                                TechResApplication.applicationContext().getMessageDao()
                                    ?.deleteMessageGroup(groupId, CurrentUser.getCurrentUser(applicationContext).id)
                            }
                            TechResApplication.applicationContext().getMessageDao()
                                ?.insertAllMessage(response.data.list)
                            TechResApplication.applicationContext().getMessageDao()
                                ?.updateDataUserId(groupId, CurrentUser.getCurrentUser(applicationContext).id)
                        }
                        EventBus.getDefault().post(EventBusFinishSyncMessage(true))
                    }
                }
            })
    }

//    private fun getMessagePersonNext(
//        group_id: String,
//        next_cursors: String
//    ) {
//        val baseRequest = BaseParams()
//        baseRequest.http_method = AppConfig.GET
//        baseRequest.project_id = AppConfig.getProjectChat()
//        baseRequest.request_url = String.format(
//            "%s%s%s%s%s%s%s%s%s%s",
//            "/api/messages-personal/pagination?group_id=",
//            group_id,
//            "&limit=",
//            10000,
//            "&random_key=",
//            -1,
//            "&pre_cursor=",
//            -1,
//            "&next_cursor=",
//            next_cursors
//        )
//        ServiceFactory.createRetrofitServiceNode(
//            TechResService::class.java
//        )
//            .getMessagesPersonal(baseRequest)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : Observer<MessagesResponse> {
//                override fun onComplete() {}
//                override fun onError(e: Throwable) {
//                    WriteLog.d("ERROR", e.message.toString())
//                }
//
//
//                override fun onSubscribe(d: Disposable) {}
//
//
//                @SuppressLint("ShowToast")
//                override fun onNext(response: MessagesResponse) {
//                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        response.data.list.forEach { it.user_id = CurrentUser.getCurrentUser(applicationContext).id}
//                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
//                            TechResApplication.applicationContext().getMessageDao()
//                                ?.insertAllMessage(response.data.list)
//                        }
//                        EventBus.getDefault().post(EventBusFinishSyncMessage(true))
//                    }
//                }
//            })
//    }
    private fun getMessagePersonNext(
    groupId: String,
    randomKey: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s",
            "/api/messages-personals/all-message-for-group?groupId=",
            groupId,
            "&randomKey=",
            randomKey,
            "&limit=",
            1000
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getMessagesPersonal(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MessagesResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: MessagesResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        response.data.list.forEach { it.user_id = CurrentUser.getCurrentUser(applicationContext).id}
                        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                          if(randomKey.equals("-1")) {
                              TechResApplication.applicationContext().getMessageDao()
                                  ?.deleteMessageGroup(groupId, CurrentUser.getCurrentUser(applicationContext).id)
                          }
                            TechResApplication.applicationContext().getMessageDao()
                                ?.insertAllMessage(response.data.list)
                            TechResApplication.applicationContext().getMessageDao()
                                ?.updateDataUserId(groupId, CurrentUser.getCurrentUser(applicationContext).id)
                        }
                        EventBus.getDefault().post(EventBusFinishSyncMessage(true))
                    }
                }
            })
    }
}