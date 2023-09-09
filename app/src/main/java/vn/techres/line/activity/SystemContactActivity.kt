package vn.techres.line.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import vn.techres.line.adapter.friend.SystemContactAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.contact.ContactData
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.friend.response.FriendResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.ContactParams
import vn.techres.line.data.model.params.FriendParams
import vn.techres.line.data.model.params.GroupPersonalParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivitySystemContactBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.MultiplePermission.shouldAskPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.ContactUtils.syncContactDevice
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.contact.ContactUtilsHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SystemContactActivity : BaseBindingActivity<ActivitySystemContactBinding>(),
    SystemContactHandler {
    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private val application = TechResApplication()
    private var mSocket: Socket? = null

    private var listFriendSystem = ArrayList<Friend>()
    private var systemContactAdapter: SystemContactAdapter? = null
    private var limit = -1
    private var page = 1
    private var data = ArrayList<Friend>()

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    )
    private val requiredPermissionsCode = 200

    override val bindingInflater: (LayoutInflater) -> ActivitySystemContactBinding
        get() = ActivitySystemContactBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        mSocket = application.getSocketInstance(this)
        mSocket?.connect()

        systemContactAdapter = SystemContactAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = systemContactAdapter
        systemContactAdapter?.setClickFriend(this)
        setData()
        setClick()
    }

    override fun onResume() {
        super.onResume()
        if (!this.shouldAskPermissions(requiredPermissions)) {
            binding.btnSyncContact.visibility = View.VISIBLE
        } else {
            binding.btnSyncContact.visibility = View.GONE
            requestMultiplePermissionWithListener()
        }
    }

    /**
     * Permission
     * */
    private fun requestMultiplePermissionWithListener() {
        requestPermissions(requiredPermissions, requiredPermissionsCode, object :
            RequestPermissionListener {

            override fun onCallPermissionFirst(
                namePermission: String,
                requestPermission: () -> Unit
            ) {
                requestPermission.invoke()
            }

            override fun onPermissionRationaleShouldBeShown(
                namePermission: String,
                requestPermission: () -> Unit
            ) {
                requestPermission.invoke()
            }

            override fun onPermissionPermanentlyDenied(
                namePermission: String,
                openAppSetting: () -> Unit
            ) {
                dialogPermission(resources.getString(R.string.title_permission_contact),
                    String.format(
                        resources.getString(R.string.note_permission_denied),
                        resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    resources.getString(R.string.step_two_open_permission),
                    R.drawable.ic_phone_book, "", 0, object : DialogPermissionHandler {
                        override fun confirmDialog() {
                            openAppSetting.invoke()
                        }

                        override fun dismissDialog() {
                        }
                    }
                )
            }

            override fun onPermissionGranted() {
                data = ArrayList()
                syncContacts()
                binding.btnSyncContact.visibility = View.VISIBLE
            }
        })
    }

    private fun syncContacts() {
        syncContactDevice(object : ContactUtilsHandler {
            override fun onSyncContact(result: ArrayList<ContactData>) {
                updatePhoneUser(result)

            }

            override fun onSyncEndContact() {
                createContactsTogether()
            }

            override fun onSyncDbAndDevice(result: ArrayList<ContactData>) {
                if (result.size > 0) {
                    updatePhoneUser(result).apply {
                        createContactsTogether()
                    }
                }
            }
        })
    }


    private fun setClick() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnSyncContact.setOnClickListener {
            syncContacts()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    systemContactAdapter?.searchFullText(newText)
                }
                return true
            }
        })
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loading.rlLoading.show()
        } else if (!isLoading) {
            run {
                binding.loading.rlLoading.hide()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        if (listFriendSystem.size == 0) {
            binding.shimmerSystemContact.show()
            binding.shimmerSystemContact.startShimmerAnimation()
            getFriendContacts()
        } else {
            binding.shimmerSystemContact.hide()
            systemContactAdapter?.notifyDataSetChanged()
        }
    }

    private fun getFriendContacts() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            String.format("%s%s%s%s", "/api/customer-phones/friend?limit=", limit, "&page=", page)
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getContact(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FriendResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: FriendResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        listFriendSystem.clear()
                        listFriendSystem = response.data.list
                        systemContactAdapter?.setDataSource(listFriendSystem)
                        binding.shimmerSystemContact.hide()
                        binding.shimmerSystemContact.hide()
                    } else {
                        Toast.makeText(
                            this@SystemContactActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    private fun updatePhoneUser(array: ArrayList<ContactData>) {
        val arrayList = array.map { it.phone?.get(0) ?: "" }.toMutableList()
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
                        setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            setLoading(false)
                            AloLineToast.makeText(
                                this@SystemContactActivity,
                                resources.getString(R.string.sync_contact_success),
                                AloLineToast.LENGTH_LONG,
                                AloLineToast.SUCCESS
                            ).show()
                        }
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
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        getFriendContacts()
                    }
                }
            })
    }

    private fun clickFriendChat(id: Int?) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(id ?: 0)
        createGroupPersonal(memberList, id!!)
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
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                    //comment
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(responseGroup: GroupPersonalResponse) {
                        if (responseGroup.status == AppConfig.SUCCESS_CODE) {
                            val group = responseGroup.data
                            createChatPersonal(memberId)
                            val bundle = Bundle()
                            bundle.putString(
                                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(
                                    group
                                )
                            )
                            cacheManager.put(
                                TechresEnum.GROUP_CHAT.toString(), Gson().toJson(
                                    group
                                )
                            )
                            cacheManager.put(
                                TechresEnum.CHAT_PERSONAL.toString(),
                                TechresEnum.GROUP_CHAT.toString()
                            )
                            val intent =
                                Intent(this@SystemContactActivity, ChatActivity::class.java)
                            intent.putExtra(
                                TechresEnum.GROUP_CHAT.toString(),
                                Gson().toJson(responseGroup.data)
                            )
                            startActivity(intent)
                        } else {
                            Toast.makeText(
                                this@SystemContactActivity,
                                responseGroup.message,
                                Toast.LENGTH_LONG
                            ).show()
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

    override fun clickItem(position: Int, id: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), id)
        startActivity(intent)
    }

    override fun clickAddFriend(position: Int, id: Int?) {
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
        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT
        addFriendParams.params.contact_to_user_id = id
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
                        //comment
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                        //SUCCESS_CODE
                        }
                    }
                })
        }
    }

    override fun clickNotAcceptFriend(
        position: Int,
        avatar: String?,
        fullName: String?,
        id: Int?
    ) {
        val friendParams = FriendParams()
        friendParams.http_method = AppConfig.POST
        friendParams.request_url = "/api/contact-froms/not-accept"
        friendParams.project_id = AppConfig.PROJECT_CHAT
        friendParams.params.contact_from_user_id = id


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
                     //comment
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            //SUCCESS_CODE
                        }
                    }
                })
        }
    }

    override fun clickChatFriend(position: Int, avatar: String?, fullName: String?, id: Int?) {
        clickFriendChat(id)
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

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

}