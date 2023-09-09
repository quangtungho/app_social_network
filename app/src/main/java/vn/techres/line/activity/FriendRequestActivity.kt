package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.adapter.friend.FriendRequestAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.friend.response.FriendResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.FriendAcceptParams
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityFriendRequestBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.FriendHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class FriendRequestActivity : BaseBindingActivity<ActivityFriendRequestBinding>(), FriendHandler {
    private var listFriendRequest = ArrayList<Friend>()
    private var friendRequestAdapter: FriendRequestAdapter? = null

    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    override val bindingInflater: (LayoutInflater) -> ActivityFriendRequestBinding
        get() = ActivityFriendRequestBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        mSocket = application.getSocketInstance(this)
        mSocket!!.connect()

        friendRequestAdapter = FriendRequestAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = friendRequestAdapter
        friendRequestAdapter!!.setClickFriend(this)

        setData()
        setClick()
    }

    private fun setClick() {
        binding.swipeRefresh.setOnRefreshListener {
            getListFriendRequest()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        if (listFriendRequest.size == 0){
            binding.shimmerFriendRequest.visibility = View.VISIBLE
            binding.shimmerFriendRequest.startShimmerAnimation()
            getListFriendRequest()
        }else{
            binding.shimmerFriendRequest.visibility = View.GONE
            friendRequestAdapter!!.notifyDataSetChanged()
        }

    }

    private fun getListFriendRequest() {
        val params = BaseParams()
        params.http_method = AppConfig.GET
        params.request_url = "/api/contact-froms"
        params.project_id = AppConfig.PROJECT_CHAT

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListFriendRequest(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                @SuppressLint("NotifyDataSetChanged")
                override fun onNext(response: FriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        listFriendRequest.clear()
                        binding.swipeRefresh.isRefreshing = false
                        binding.shimmerFriendRequest.visibility = View.GONE
                        binding.shimmerFriendRequest.hide()
                        listFriendRequest = response.data.list
                        friendRequestAdapter!!.setDataSource(listFriendRequest)
                    } else Toast.makeText(
                        this@FriendRequestActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

    }

    private fun acceptFriend(userID: Int?) {
        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = userID


        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .acceptFriend(params)
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

    private fun notAcceptFriend(userID: Int?) {
        val friendParams = FriendParams()
        friendParams.http_method = AppConfig.POST
        friendParams.request_url = "/api/contact-froms/not-accept"
        friendParams.project_id = AppConfig.PROJECT_CHAT
        friendParams.params.contact_from_user_id = userID
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
                    override fun onSubscribe(d: Disposable) {
                        //comment
                    }
                    override fun onNext(response: BaseResponse) {
                        //comment
                    }
                })
        }
    }

    override fun clickItemMyFriend(position: Int, userID: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), userID)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun clickCancelFriendRequest(
        position: Int,
        data: Friend
    ) {
        notAcceptFriend(data.user_id)
    }

    override fun clickAcceptFriend(
        position: Int,
        data: Friend
    ) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = data.user_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket!!.emit("approve-to-contact", jsonObject)
            WriteLog.d("approve-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        acceptFriend(data.user_id)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}