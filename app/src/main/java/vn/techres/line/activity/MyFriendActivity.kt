package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import vn.techres.line.adapter.friend.ListFriendAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.local.contact.myfriend.MyFriendResponse
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.params.GroupPersonalParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityMyFriendBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.ListFriendHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class MyFriendActivity : BaseBindingActivity<ActivityMyFriendBinding>(), ListFriendHandler {
    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private val application = TechResApplication()
    private var mSocket: Socket? = null

    private var listFriend = ArrayList<Friend>()
    private var listFriendAdapter: ListFriendAdapter? = null

    private var limit = -1
    private var page = 1
    private var idProfile = 0
    
    override val bindingInflater: (LayoutInflater) -> ActivityMyFriendBinding
        get() = ActivityMyFriendBinding::inflate

    override fun onSetBodyView() {
        intent?.let {
            idProfile = it.getIntExtra(TechresEnum.ID_USER.toString(), 0)
        }

        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        mSocket = application.getSocketInstance(this)
        mSocket!!.connect()

        listFriendAdapter = ListFriendAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = listFriendAdapter
        listFriendAdapter?.setClickFriend(this)

        setData()
        setClick()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        if (listFriend.size == 0){
            binding.shimmerFriend.visibility = View.VISIBLE
            binding.shimmerFriend.startShimmerAnimation()
            getListFriend()
        }else{
            binding.shimmerFriend.visibility = View.GONE
            listFriendAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setClick() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    listFriendAdapter?.searchFullText(newText)
                }
                return true
            }
        })
    }
    
    private fun getListFriend() {
        val params = BaseParams()
        params.http_method = AppConfig.GET
        params.project_id = AppConfig.PROJECT_CHAT
        params.request_url =String.format("%s%s%s%s%s%s", "/api/friends?user_id=", idProfile, "&limit=", limit, "&page=", page)
        restaurant().restaurant_id?.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .getListFriend(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<MyFriendResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: MyFriendResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            listFriend.clear()
                            listFriend = response.data.list
                            listFriendAdapter?.setDataSource(listFriend)
                            binding.shimmerFriend.visibility = View.GONE
                            binding.shimmerFriend.hide()
                        } else{
                            Toast.makeText(
                                this@MyFriendActivity,
                                response.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

    override fun clickFriendProfile(position: Int, id: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), id)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun clickAddFriend(
        position: Int,
        avatar: String?,
        fullName: String?,
        id: Int
    ) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("request-to-contact", jsonObject)
            WriteLog.d("request-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        addFriend(id)
    }

    override fun clickFriendChat(position: Int, avatar: String?, fullName: String?, id: Int) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(id)
        createGroupPersonal(memberList, id)
    }

    private fun addFriend(userID: Int?){
        val params = FriendParams()
        params.http_method = AppConfig.GET
        params.project_id = AppConfig.PROJECT_CHAT
        params.request_url = "/api/contact-tos/request"
        params.params.contact_to_user_id = userID
        params.let {
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
                    override fun onNext(response: BaseResponse) {
                        if (response.status != AppConfig.SUCCESS_CODE) {
                            Toast.makeText(this@MyFriendActivity, this@MyFriendActivity.baseContext.getString(R.string.api_error), Toast.LENGTH_LONG).show()
                        }
                    }
                })
        }
    }

    private fun createGroupPersonal(memberList: ArrayList<Int>, memberId : Int) {
        val groupPersonalParams = GroupPersonalParams()
        groupPersonalParams.http_method = AppConfig.POST
        groupPersonalParams.project_id = AppConfig.getProjectChat()
        groupPersonalParams.request_url = "/api/groups/create-personal"
        groupPersonalParams.params.members = memberList
        groupPersonalParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createChatPersonal(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<GroupPersonalResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(responseGroup: GroupPersonalResponse) {
                        if (responseGroup.status == AppConfig.SUCCESS_CODE) {
                            val group = responseGroup.data
                            createChatPersonal(memberId)

                            cacheManager.put(
                                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(
                                    group
                                )
                            )
                            cacheManager.put(
                                TechresEnum.CHAT_PERSONAL.toString(),
                                TechresEnum.GROUP_CHAT.toString()
                            )
                            val intent = Intent(this@MyFriendActivity, ChatActivity::class.java)
                            intent.putExtra(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(responseGroup.data))
                            startActivity(intent)
                            overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
                        } else {
                            Toast.makeText(this@MyFriendActivity, responseGroup.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                })
        }
    }

    private fun createChatPersonal(memberId: Int) {
        val createChatPersonalRequest = CreateGroupPersonalRequest()
        createChatPersonalRequest.member_id = memberId
        createChatPersonalRequest.admin_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(createChatPersonalRequest))
            mSocket?.emit(TechResEnumChat.NEW_GROUP_CREATE_PERSONAL_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }



}