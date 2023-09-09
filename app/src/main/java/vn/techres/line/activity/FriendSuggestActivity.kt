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
import vn.techres.line.adapter.friend.FriendSuggestAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.friend.FriendSuggestResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityFriendSuggestBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.FriendSuggestHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class FriendSuggestActivity : BaseBindingActivity<ActivityFriendSuggestBinding>(),
    FriendSuggestHandler {
    private var listFriendSuggest = ArrayList<Friend>()
    private var friendSuggestAdapter: FriendSuggestAdapter? = null

    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private val application = TechResApplication()
    private var mSocket: Socket? = null

    override val bindingInflater: (LayoutInflater) -> ActivityFriendSuggestBinding
        get() = ActivityFriendSuggestBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        mSocket = application.getSocketInstance(this)
        mSocket!!.connect()

        friendSuggestAdapter = FriendSuggestAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = friendSuggestAdapter
        friendSuggestAdapter!!.setFriendSuggestHandler(this)

        setData()
        setEvent()

    }

    private fun setEvent() {
        binding.swipeRefresh.setOnRefreshListener {
            getListFriendSuggest()
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        if (listFriendSuggest.size == 0){
            binding.shimmerFriendSuggest.visibility = View.VISIBLE
            binding.shimmerFriendSuggest.startShimmerAnimation()
            getListFriendSuggest()
        }else{
            binding.shimmerFriendSuggest.visibility = View.GONE
            friendSuggestAdapter!!.notifyDataSetChanged()
        }

    }

    private fun getListFriendSuggest() {
        val params = BaseParams()
        params.http_method = AppConfig.GET
        params.request_url = "/api/customer-membership-cards/suggestion"
        params.project_id = AppConfig.PROJECT_CHAT

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getFriendSuggest(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendSuggestResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                @SuppressLint("NotifyDataSetChanged")
                override fun onNext(response: FriendSuggestResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        listFriendSuggest.clear()
                        binding.swipeRefresh.isRefreshing = false
                        listFriendSuggest = response.data
                        friendSuggestAdapter!!.setDataSource(listFriendSuggest)
                        binding.shimmerFriendSuggest.visibility = View.GONE
                        binding.shimmerFriendSuggest.hide()
                    } else Toast.makeText(
                        this@FriendSuggestActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })

    }

    override fun onClickItemFriend(id: Int, position: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), id)
        startActivity(intent)
        this.overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)

    }

    override fun onAddFriend(position: Int) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = listFriendSuggest[position].user_id!!
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket!!.emit("request-to-contact", jsonObject)
            WriteLog.d("request-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT
        addFriendParams.params.contact_to_user_id = listFriendSuggest[position].user_id!!
        addFriendParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .addFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {}
                })
        }
    }

    override fun onSkipAddFriend(position: Int) {
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}