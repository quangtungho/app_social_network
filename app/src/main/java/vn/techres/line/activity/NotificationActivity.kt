package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.giphy.sdk.analytics.GiphyPingbacks.context
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.account.NotificationAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.Notifications
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.FriendAcceptParams
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.NotificationResponse
import vn.techres.line.data.model.response.NotificationsResponse
import vn.techres.line.data.model.response.OneReviewBranchReponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityNotificationBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.NotificationsHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class NotificationActivity : BaseBindingActivity<ActivityNotificationBinding>(),
    NotificationsHandler {
    private var notificationAdapter: NotificationAdapter? = null
    private var dataNotification = ArrayList<Notifications>()
    private var configJava = ConfigJava()
    private var user = User()
    private var nodeJs = ConfigNodeJs()

    private var totalRecord = 0

    private var limit = 1000
    private var type = -1
    private var positionList = ""
    private var isViewed = -1
    private var checkSwipeRefresh = false
    private var mSocket: Socket? = null
    private val application = TechResApplication()
    private var detailPost = PostReview()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(this@NotificationActivity)
        user = CurrentUser.getCurrentUser(this@NotificationActivity)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this@NotificationActivity)
        mSocket = application.getSocketInstance(baseContext)
        mSocket?.connect()
    }

    override val bindingInflater: (LayoutInflater) -> ActivityNotificationBinding
        get() = ActivityNotificationBinding::inflate

    override fun onSetBodyView() {
        this.window?.statusBarColor = AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.notification)
        binding.rcNotification.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        notificationAdapter = NotificationAdapter(this)
        notificationAdapter?.setOnClickNotification(this)
        binding.rcNotification.adapter = notificationAdapter
        setData()
        setListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        if (dataNotification.size == 0){
            binding.shimmerNotification.visibility = View.VISIBLE
            binding.shimmerNotification.startShimmerAnimation()
            getNotification(limit, type, positionList, isViewed)
        }else{
            binding.shimmerNotification.visibility = View.GONE
            notificationAdapter!!.notifyDataSetChanged()
        }

    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
          onBackPressed()
        }

        binding.swipeRefresh.setOnRefreshListener {
            checkSwipeRefresh = true
            binding.swipeRefresh.isRefreshing = true
            getNotification(limit, type, positionList, isViewed)
        }

        binding.rcNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
                WriteLog.d("ahihi: ", y.toString())
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y <= 0) {
//                        if (page < ceil((totalRecord / limit).toDouble())) {
//                            page++
//                            getNotification(page)
//                        }
//                        if (newState == totalRecord - 1){
//                            getNotification(limit, type, positionList, isViewed)
//                        }
                        WriteLog.d("ahuhu: ", newState.toString())
                    } else {
                        y = 0
                    }
                }
            }
        })
    }

    private fun getNotification(limit: Int, type: Int, position: String, isViewed: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/aloline-notification?limit=$limit&type=$type&position=$position&isViewed=$isViewed"
        baseRequest.project_id = AppConfig.PROJECT_LOG
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        ).getNotifications(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<NotificationResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: NotificationResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        binding.shimmerNotification.visibility = View.GONE
                        binding.shimmerNotification.hide()
                        if (checkSwipeRefresh){
                            dataNotification.clear()
                            binding.swipeRefresh.isRefreshing = false
                        }
                        val data = response.data.list
                        totalRecord = response.data.total_record!!

                        if (dataNotification.size == 0) {
                            dataNotification = data
                        } else {
                            dataNotification.addAll(dataNotification.size, data)
                        }
                        notificationAdapter?.setDataSource(dataNotification)
                    }
                }
            })
    }

    private fun postMakeNotification(id: String){
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.project_id = AppConfig.PROJECT_LOG
        baseRequest.request_url = "/api/aloline-notification/mark-viewed/$id"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .postMarkViewedNotification(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<NotificationsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: NotificationsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                    }
                }
            })
    }

    private fun postRemoveNotification(id: String){
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.project_id = AppConfig.PROJECT_LOG
        baseRequest.request_url = "/api/aloline-notification/remove/$id"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .removeNotification(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    //onNext
                }
            })
    }

    override fun markNotification(notifications: Notifications) {
        when(notifications.type){
            0 , 1, 3 ->{
                val intent = Intent(this@NotificationActivity, ProfileActivity::class.java)
                intent.putExtra(TechresEnum.ID_USER.toString(), notifications.redirect_to)
                startActivity(intent)
                overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
            } else ->{
                val intent = Intent(this, CommentActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(TechresEnum.ID_POST.toString(), notifications.redirect_to)
                intent.putExtra(TechresEnum.CHECK_COMMENT_CHOOSE.toString(), TechresEnum.VALUE_COMMENT_NOTIFY.toString())
                intent.putExtra(TechresEnum.DETAIL_POST_COMMENT.toString(), Gson().toJson(PostReview()))
                startActivity(intent)
            }
        }
        postMakeNotification(notifications._id)
    }

    override fun onAction(notifications: Notifications) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_notification)
        bottomSheetDialog.setCancelable(true)
        val imgAvatarNotify = bottomSheetDialog.findViewById<ImageView>(R.id.imgAvatarNotify)
        val tvContentNotify = bottomSheetDialog.findViewById<TextView>(R.id.tvContentNotify)
        val btnTurnOffNotify = bottomSheetDialog.findViewById<Button>(R.id.btnTurnOffNotify)
        val btnSeenNotify = bottomSheetDialog.findViewById<Button>(R.id.btnSeenNotify)
        imgAvatarNotify?.load(String.format("%s%s", nodeJs.api_ads, notifications.customer.avatar.original)) {
            crossfade(true)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
        }
        tvContentNotify?.text = notifications.content
        btnSeenNotify?.setOnClickListener {
            postMakeNotification(notifications._id)
            bottomSheetDialog.dismiss()
        }
        btnTurnOffNotify?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    override fun onAcceptFriend(notifications: Notifications) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = notifications.customer.user_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("approve-to-contact", jsonObject)
            WriteLog.d("approve-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = notifications.customer.user_id


        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .acceptFriend(it)
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
                        }
                    }
                })
        }
    }

    override fun onRefuseFriend(notifications: Notifications) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = notifications.customer.user_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("not-accept-approve-to-contact", jsonObject)
            WriteLog.d("not-accept-approve-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val friendParams = FriendParams()
        friendParams.http_method = AppConfig.POST
        friendParams.request_url = "/api/contact-froms/not-accept"
        friendParams.project_id = AppConfig.PROJECT_CHAT
        friendParams.params.contact_from_user_id = notifications.customer.user_id
        friendParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .notAcceptFriend(it)
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
                            postRemoveNotification(notifications._id)
                        }
                    }
                })
        }
    }


}