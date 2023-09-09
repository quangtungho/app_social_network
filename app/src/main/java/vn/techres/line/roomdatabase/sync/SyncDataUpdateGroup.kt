package vn.techres.line.roomdatabase.sync

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.chat.GroupsResponse
import vn.techres.line.data.model.chat.params.GroupParams
import vn.techres.line.data.model.chat.request.group.GroupRequest
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SyncDataUpdateGroup(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    private var tag = "SyncDataUpdateGroup"

    override fun doWork(): Result {
        // Do the work here--in this case
        getGroup()
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun getGroup() {
        val request = GroupRequest("", 10000)
        val baseRequest = GroupParams(request)
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = "/api/group-pagination"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getGroups(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GroupsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {

                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: GroupsResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                            TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
//                                if(pageTemp == 1){
//                                    val totalPage = response.data.total_records ?: 1
//                                    this@SyncDataUpdateGroup.totalPage = if (totalPage % 50 == 0) {
//                                        totalPage / 50
//                                    } else totalPage / 50 + 1
                                    TechResApplication.applicationContext().getGroupDao()?.deleteGroupAll()
                                    TechResApplication.applicationContext().getGroupDao()?.insertAll(response.data.list)
                                    TechResApplication.applicationContext().getGroupDao()?.updateUserIdGroup(CurrentUser.getCurrentUser(applicationContext).id)
//                                }else{
//                                    TechResApplication.applicationContext().getGroupDao()?.insertAll(response.data.list)
//                                    EventBus.getDefault().post(EventBusFinishSyncPageGroup(page))
//                                }
                            }
//                            EventBus.getDefault().post(EventBusFinishSyncGroup(true))
                        }
                    }
                }
            })
    }
}