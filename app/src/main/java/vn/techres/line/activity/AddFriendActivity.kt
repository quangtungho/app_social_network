package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
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
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.adapter.friend.AddFriendAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.eventbus.EventBusChangeStatusFriend
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.friend.response.SearchFriendResponse
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.params.GroupPersonalParams
import vn.techres.line.data.model.params.SearchFriendParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityAddFriendBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.SystemContactHandler
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.math.ceil

class AddFriendActivity : BaseBindingActivity<ActivityAddFriendBinding>(), SystemContactHandler {
    private var data = ArrayList<Friend>()
    private var addFriendAdapter: AddFriendAdapter? = null
    private var nodeJs = ConfigNodeJs()

    //socket
    private var mSocket: Socket? = null
    private var user = User()

    private var page = 1
    private var total = 0
    private var limit = 100
    override val bindingInflater: (LayoutInflater) -> ActivityAddFriendBinding
        get() = ActivityAddFriendBinding::inflate

    override fun onSetBodyView() {
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        addFriendAdapter = AddFriendAdapter(
            this
        )
        binding.recyclerviewFriend.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerviewFriend.adapter = addFriendAdapter
        addFriendAdapter?.setClickFriend(this)
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                page = 1
                data.clear()
                if (query != null) {
                    searchAddFriend(query, page)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == "") {
                    data.clear()
                    searchAddFriend(newText, page)
                }
                return false
            }
        })
        binding.recyclerviewFriend.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }
            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y > 0) {
                        if (page <= ceil((total / limit).toString().toDouble())) {
                            page++
                            searchAddFriend("", page)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })
    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }
    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
    @Subscribe
    fun onChangeStatusFriend(event: EventBusChangeStatusFriend) {
        data.forEachIndexed { index, friend ->
            if (friend.user_id == event.id) {
                friend.status = event.status
                addFriendAdapter!!.notifyItemChanged(index)
                return@forEachIndexed
            }
        }
    }
    override fun onResume() {
        super.onResume()
        if (data.size == 0) {
            Handler(Looper.myLooper()!!).postDelayed({
                page = 1
                data.clear()
                searchAddFriend("", page)
            }, 220)
        } else {
            addFriendAdapter?.setDataSource(data)
        }
    }
    private fun searchAddFriend(keyword: String, page: Int) {
        val searchFriendParams = SearchFriendParams()
        searchFriendParams.params.keyword = keyword
        searchFriendParams.http_method = AppConfig.GET
        searchFriendParams.project_id = AppConfig.PROJECT_CHAT
        searchFriendParams.request_url = String.format(
            "%s%s%s%s",
            "/api/customer-phones/search-user-by-keyword?page=",
            page,
            "&limit=",
            limit
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )

            .searchAddFriend(searchFriendParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SearchFriendResponse> {
                override fun onComplete() {
                   //comment
                }
                override fun onError(e: Throwable) {
                    //comment
                }
                override fun onSubscribe(d: Disposable) {
                    //comment
                }
                override fun onNext(response: SearchFriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val friendListSearch = response.data.list
                        total = response.data.total_record ?: 0

                        if (data.size == 0) {
                            data = friendListSearch
                        } else {
                            data.addAll(data.size, friendListSearch)
                        }

                        addFriendAdapter?.setDataSource(data)
                    } else {
                        Toast.makeText(
                            this@AddFriendActivity,
                            resources.getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }
    override fun clickItem(position: Int, id: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), data[position].user_id ?: 0)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
    override fun clickAddFriend(position: Int, id: Int?) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = data[position].user_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("request-to-contact", jsonObject)
            WriteLog.d("request-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT
        addFriendParams.params.contact_to_user_id = data[position].user_id
        addFriendParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .addFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {
                        //comment
                    }

                    override fun onError(e: Throwable) {
                     //comment
                    }

                    override fun onSubscribe(d: Disposable) {
                       //comment
                    }

                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            data[position].status = 2
                        } else {
                            Toast.makeText(
                                this@AddFriendActivity,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }
    override fun clickNotAcceptFriend(position: Int, avatar: String?, fullName: String?, id: Int?) {
        val friendParams = FriendParams()
        friendParams.http_method = AppConfig.POST
        friendParams.request_url = "/api/contact-froms/not-accept"
        friendParams.project_id = AppConfig.PROJECT_CHAT
        friendParams.params.contact_from_user_id = data[position].user_id

        friendParams.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .notAcceptFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {
                        //comment
                    }

                    override fun onError(e: Throwable) {
                        //comment
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
    override fun clickChatFriend(position: Int, avatar: String?, fullName: String?, id: Int?) {
        clickFriendChat(position)
    }
    override fun clickAvatar(url: String, position: Int) {
        lookAtPhoto(url, position)
    }
    private fun lookAtPhoto(url: String, position: Int) {
        val list = ArrayList<String>()
        list.add(String.format("%s%s", nodeJs.api_ads, url))
        val intent = Intent(this, MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }
    private fun clickFriendChat(position: Int) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(data[position].user_id ?: 0)
        createGroupPersonal(memberList, data[position].user_id!!)
        binding.searchView.clearFocus()
    }
    private fun createGroupPersonal(memberList: ArrayList<Int>, memberId: Int) {
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
                    override fun onComplete() {
                        //comment
                    }

                    override fun onError(e: Throwable) {
                        //comment
                    }

                    override fun onSubscribe(d: Disposable) {
                        //comment
                    }

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
                            val intent = Intent(this@AddFriendActivity, ChatActivity::class.java)
                            intent.putExtra(
                                TechresEnum.GROUP_CHAT.toString(),
                                Gson().toJson(responseGroup.data)
                            )
                            startActivity(intent)
                            overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                        } else {
                            Toast.makeText(
                                this@AddFriendActivity,
                                responseGroup.message,
                                Toast.LENGTH_LONG
                            )
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