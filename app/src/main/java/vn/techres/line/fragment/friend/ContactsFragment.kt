package vn.techres.line.fragment.friend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.work.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.*
import vn.techres.line.adapter.friend.*
import vn.techres.line.base.BaseBindingStubFragment
import vn.techres.line.data.local.contact.friendrequestcontact.FriendRequestContactResponse
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.contact.ContactData
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.*
import vn.techres.line.databinding.FragmentContactsBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.MultiplePermission.shouldAskPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.ContactUtils.syncContactDevice
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.ClickFriendHandler
import vn.techres.line.interfaces.ClickItemHandler
import vn.techres.line.interfaces.FriendHandler
import vn.techres.line.interfaces.LongClickFriendHandler
import vn.techres.line.interfaces.contact.ContactUtilsHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.roomdatabase.sync.SyncDataUpdateContact
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

@SuppressLint("UseRequireInsteadOfGet")

class ContactsFragment :
    BaseBindingStubFragment<FragmentContactsBinding>(R.layout.fragment_contacts, true),
    ClickFriendHandler, LongClickFriendHandler, FriendHandler, ClickItemHandler {
    val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var configJava = ConfigJava()
    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private var myFriendAdapter: MyFriendAdapter? = null
    private var friendOnlineAdapter: FriendOnlineAdapter? = null
    private var friendNewAdapter: FriendNewAdapter? = null
    private var bestFriendAdapter: BestFriendAdapter? = null
    private var friendRequestContactAdapter: FriendRequestContactAdapter? = null

    private var listFriendNew = ArrayList<Friend>()
    private var friendList = ArrayList<Friend>()
    private var listFriendOnline = ArrayList<Friend>()
    private var listBestFriend = ArrayList<Friend>()
    private var listFriendContactFrom = ArrayList<Friend>()

    private var scrollScreen = ScrollScreen()

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    )
    private val requiredPermissionsCode = 200

    private var data = ArrayList<Friend>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            AlolineColorUtil(view.context).convertColor(R.color.main_bg)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(mainActivity!!)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(mainActivity!!)
        configJava = CurrentConfigJava.getConfigJava(mainActivity!!)
        mSocket = application.getSocketInstance(mainActivity!!)
        mSocket?.connect()
    }


    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onCreateViewAfterViewStubInflated(savedInstanceState: Bundle?) {
        myFriendAdapter = MyFriendAdapter(mainActivity!!)

        stubBinding?.rlFriends?.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }


        stubBinding?.rlFriends?.itemAnimator?.changeDuration = 0


        (Objects.requireNonNull(stubBinding?.rlFriends?.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false


        stubBinding?.rlFriends?.setHasFixedSize(true)


        stubBinding?.rlFriends?.adapter = myFriendAdapter

        friendOnlineAdapter = FriendOnlineAdapter(mainActivity!!)


        stubBinding?.rlFriendsOnline?.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }


        stubBinding?.rlFriendsOnline?.itemAnimator?.changeDuration = 0


        (Objects.requireNonNull(stubBinding?.rlFriendsOnline?.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false


        stubBinding?.rlFriendsOnline?.setHasFixedSize(true)


        stubBinding?.rlFriendsOnline?.adapter = friendOnlineAdapter

        friendNewAdapter = FriendNewAdapter(mainActivity!!)

        stubBinding?.rlFriendNew?.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }


        stubBinding?.rlFriendNew?.itemAnimator?.changeDuration = 0


        (Objects.requireNonNull(stubBinding?.rlFriendNew?.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false


        stubBinding?.rlFriendNew?.setHasFixedSize(true)


        stubBinding?.rlFriendNew?.adapter = friendNewAdapter

        bestFriendAdapter = BestFriendAdapter(mainActivity!!)

        stubBinding?.rlBestFriend?.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }


        stubBinding?.rlBestFriend?.itemAnimator?.changeDuration = 0


        (Objects.requireNonNull(stubBinding?.rlBestFriend?.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false


        stubBinding?.rlBestFriend?.setHasFixedSize(true)


        stubBinding?.rlBestFriend?.adapter = bestFriendAdapter

        friendRequestContactAdapter = FriendRequestContactAdapter(mainActivity!!)

        stubBinding?.itemContactFriendRequest?.rlFriendRequest?.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.HORIZONTAL, false) }


        stubBinding?.itemContactFriendRequest?.rlFriendRequest?.itemAnimator?.changeDuration = 0


        (Objects.requireNonNull(stubBinding?.itemContactFriendRequest?.rlFriendRequest?.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false


        stubBinding?.itemContactFriendRequest?.rlFriendRequest?.setHasFixedSize(true)


        stubBinding?.itemContactFriendRequest?.rlFriendRequest?.adapter =
            friendRequestContactAdapter


        friendOnlineAdapter?.setClickFriend(this)
        myFriendAdapter?.setClickFriend(this)
        friendNewAdapter?.setClickFriend(this)
        bestFriendAdapter?.setClickFriend(this)
        bestFriendAdapter?.setLongClickFriend(this)
        friendRequestContactAdapter?.setClickFriend(this)
        friendRequestContactAdapter?.setClickSeeMore(this)

        if (!requireActivity().shouldAskPermissions(requiredPermissions)) {
            stubBinding?.itemContactPermission?.lnContactPermission?.hide()
            syncContacts()

            mainActivity?.setLoading(false)
        }
        //else {
//            requestMultiplePermissionWithListener()
//            mainActivity?.setLoading(false)
        //   }


        //Save all data
        setData()
        setupDataSyncLocal()
    }

    //Event bus set click Tablayout scroll
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onClickTab(scrollScreen: ScrollScreen) {
        if (scrollScreen.click == 1) {
            stubBinding?.nsContacts?.scrollTo(0, 0)
        }
    }

    private fun onClickTabScroll(scrollScreen: ScrollScreen) {
        if (scrollScreen.click == 1) {
            stubBinding?.nsContacts?.scrollTo(0, 0)
        }
    }

    //Event bus update best friend list
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBestFriend(bestFriendSelected: ArrayList<Friend>) {
        if (bestFriendSelected.size != 0) {
            for (i in bestFriendSelected.indices) {
                listBestFriend.add(bestFriendSelected[i])
            }
            bestFriendAdapter?.setDataSource(listBestFriend)
        }
    }

    //Event bus update all data
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFinishSyncData(event: EventBusFinishSyncContact) {
        if (event.finish) {
            setData()
        }
    }

    override fun onResume() {
        super.onResume()
        getFriendRequestContact()
        onClickTabScroll(scrollScreen)
        setListener()
        mainActivity?.setOnBackClick(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            TechresEnumContact.SAVE_LIST_FRIEND.toString(),
            Gson().toJson(friendList)
        )
        outState.putString(
            TechresEnumContact.SAVE_FRIEND_NEW.toString(),
            Gson().toJson(listFriendNew)
        )
        outState.putString(
            TechresEnumContact.SAVE_FRIEND_ONLINE.toString(),
            Gson().toJson(listFriendOnline)
        )
        outState.putString(
            TechresEnumContact.SAVE_BEST_FRIEND.toString(),
            Gson().toJson(listBestFriend)
        )
        outState.putString(
            TechresEnumContact.SAVE_FRIEND_TOTAL_REQUEST.toString(),
            Gson().toJson(listFriendContactFrom)
        )
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        //Show data best friend
        listBestFriend.clear()
        val listBestFriendDao =
            TechResApplication.applicationContext().getFriendDao()?.getBestFriends() ?: ArrayList()
        listBestFriend.addAll(listBestFriendDao)
        bestFriendAdapter?.setDataSource(listBestFriend)

        //Show data friend online
        listFriendOnline.clear()
        val listFriendOnlineDao =
            TechResApplication.applicationContext().getFriendDao()?.getFriendsOnline()
                ?: ArrayList()
        listFriendOnline.addAll(listFriendOnlineDao)

        if (listFriendOnline.size == 0) {
            stubBinding?.tvNotYetStatus?.show()
        } else {
            stubBinding?.tvNotYetStatus?.hide()
            if (listFriendOnline.size > 5) {
                stubBinding?.txtSeeMoreFriendsOnline?.visibility = View.VISIBLE
                val friendOnlineTemp = listFriendOnline.take(5) as ArrayList
                friendOnlineAdapter?.setDataSource(friendOnlineTemp)
            } else {
                stubBinding?.txtSeeMoreFriendsOnline?.visibility = View.GONE
                friendOnlineAdapter?.setDataSource(listFriendOnline)
            }
        }


        //Show data friend request contact
        listFriendContactFrom.clear()
        val listFriendRequestContactDao =
            TechResApplication.applicationContext().getFriendDao()?.getFriendsRequest()
                ?: ArrayList()
        listFriendContactFrom.addAll(listFriendRequestContactDao)
        if (listFriendContactFrom.size == 0) {
            stubBinding?.itemContactFriendRequest?.lnFriendRequestHorizontal?.hide()
        } else {
            stubBinding?.itemContactFriendRequest?.lnFriendRequestHorizontal?.show()
        }
        if (listFriendContactFrom.size > 5) {
            val friendTempContactFrom = listFriendContactFrom.take(5) as ArrayList
            friendTempContactFrom.add(Friend())
            friendRequestContactAdapter?.setDataSource(friendTempContactFrom)
        } else {
            friendRequestContactAdapter?.setDataSource(listFriendContactFrom)
        }

        //Show data my friend
        friendList.clear()
        val listMyFriendDao =
            TechResApplication.applicationContext().getFriendDao()?.getMyFriends() ?: ArrayList()
        friendList.addAll(listMyFriendDao)
        myFriendAdapter?.setDataSource(friendList)
        stubBinding?.tvFriends?.text = String.format(
            "%s (%s)",
            mainActivity?.baseContext?.getString(R.string.list_friend),
            friendList.size
        )

        //Show data new friend
        listFriendNew.clear()
        val listFriendNewDao =
            TechResApplication.applicationContext().getFriendDao()?.getFriendsNew() ?: ArrayList()
        listFriendNew.addAll(listFriendNewDao)
        friendNewAdapter?.setDataSource(listFriendNew)
        if (listFriendNew.size == 0) {
            stubBinding?.tvFriendNew?.hide()
            stubBinding?.vFriendNew?.hide()
        } else {
            stubBinding?.tvFriendNew?.show()
            stubBinding?.vFriendNew?.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setListener() {

        stubBinding?.swipeRefresh?.setOnRefreshListener {
            stubBinding?.swipeRefresh?.isEnabled = false
            Handler().postDelayed({ stubBinding?.swipeRefresh?.isEnabled = true }, 3000)
            setupDataSyncLocal()
            stubBinding?.swipeRefresh?.isRefreshing = false
        }

        stubBinding?.itemContactPermission?.btnTurnOn?.setOnClickListener {
            requestMultiplePermissionWithListener()
        }

        stubBinding?.tvSearchView?.setOnClickListener {
            val intent = Intent(mainActivity, AddFriendActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.lnFriendFromPhoneBook?.setOnClickListener {
            val intent = Intent(mainActivity, SystemContactActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }


        stubBinding?.lnAddBestFriend?.setOnClickListener {
            val intent = Intent(context, AddBestFriendActivity::class.java)
            startActivity(intent)
            mainActivity?.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        stubBinding?.txtSeeMoreFriendsOnline?.setOnClickListener {
            stubBinding?.txtSeeMoreFriendsOnline?.visibility = View.GONE
            friendOnlineAdapter?.setDataSource(listFriendOnline)
        }

        stubBinding?.btnSyncContact?.setOnClickListener {

            if (!requireActivity().shouldAskPermissions(requiredPermissions)) {
                stubBinding?.itemContactPermission?.lnContactPermission?.hide()
                syncContacts()
                mainActivity?.setLoading(true)
            } else {
                requestMultiplePermissionWithListener()
            }
        }
    }

    private fun setupDataSyncLocal() {
        val builder = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
        val syncDataUpdateContact: WorkRequest =
            OneTimeWorkRequestBuilder<SyncDataUpdateContact>()
                .addTag("SyncContact")
                .setConstraints(builder.build())
                .build()
        WorkManager
            .getInstance(TechResApplication.applicationContext())
            .enqueue(syncDataUpdateContact)
    }

    override fun clickFriendProfile(position: Int, id: Int) {
        onClickProfile(id)
    }

    override fun clickFriendChat(position: Int, type: String) {
        when (type) {
            "friend_online" -> {
                clickFriendChat(listFriendOnline[position])
            }
            "best_friend" -> {
                clickFriendChat(listBestFriend[position])
            }
            "friend_new" -> {
                clickFriendChat(listFriendNew[position])
            }
            "my_friend" -> {
                clickFriendChat(friendList[position])
            }
        }
    }

    private fun clickFriendChat(friend: Friend) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(friend.user_id ?: 0)
        createGroupPersonal(memberList, friend.user_id!!)
    }

    //Api create group personal
    private fun createGroupPersonal(memberList: ArrayList<Int>, memberId: Int) {
        mainActivity?.setLoading(true)
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
                        mainActivity?.setLoading(false)
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
                            val intent = Intent(mainActivity, ChatActivity::class.java)
                            intent.putExtra(
                                TechresEnum.GROUP_CHAT.toString(),
                                Gson().toJson(responseGroup.data)
                            )
                            startActivity(intent)
                            mainActivity?.overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                        } else {
                            Toast.makeText(
                                mainActivity,
                                resources.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                        mainActivity?.setLoading(false)
                    }
                })
        }
    }

    //Api create personal
    private fun createChatPersonal(memberId: Int) {
        val createChatPersonalRequest = CreateGroupPersonalRequest()
        createChatPersonalRequest.member_id = memberId
        createChatPersonalRequest.admin_id = user.id
        try {
            val jsonObject = JSONObject(Gson().toJson(createChatPersonalRequest))
            mSocket!!.emit(
                TechResEnumChat.NEW_GROUP_CREATE_PERSONAL_ALO_LINE.toString(),
                jsonObject
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    override fun longClickFriend(position: Int, id: Int) {
        bottomSheetLongClickBestFriend(position, id)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun bottomSheetLongClickBestFriend(position: Int, id: Int) {
        val bottomSheetDialog = BottomSheetDialog(this.mainActivity!!)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_delete_best_friend)
        bottomSheetDialog.setCancelable(true)
        val lnDelete = bottomSheetDialog.findViewById<LinearLayout>(R.id.lnDelete)
        lnDelete!!.setOnClickListener {
            val listIdBestFriend = ArrayList<Int>()
            listIdBestFriend.add(id)
            deleteBestFriend(listIdBestFriend)
            listBestFriend.removeAt(position)
            bestFriendAdapter?.notifyDataSetChanged()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    //Api delete best friend
    private fun deleteBestFriend(listIdBestFriend: ArrayList<Int>) {
        val param = AddBestFriendParams()
        param.http_method = AppConfig.POST
        param.request_url = "/api/friends/create-close-friend"
        param.project_id = AppConfig.PROJECT_CHAT
        param.params.list_user_ids = listIdBestFriend
        param.params.type = 2

        param.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .addBestFriend(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
                            }
                        } else {
                            Toast.makeText(
                                mainActivity,
                                mainActivity!!.baseContext.getString(R.string.api_error),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }
    }

    //Api get friend request contact
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
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("SetTextI18n")
                override fun onNext(response: FriendRequestContactResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        if (response.data.contact_from.total_record == 0) {
                            stubBinding?.itemContactFriendRequest?.lnFriendRequestHorizontal?.hide()
                        } else {

                            stubBinding?.itemContactFriendRequest?.lnFriendRequestHorizontal?.show()
                        }
                        listFriendContactFrom.clear()
                        listFriendContactFrom = response.data.contact_from.list
                        if (response.data.contact_from.total_record!! > 5) {
                            listFriendContactFrom.add(Friend())
                        }

                        friendRequestContactAdapter?.setDataSource(listFriendContactFrom)
                    } else {

                        Toast.makeText(
                            mainActivity,
                            mainActivity!!.baseContext.getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    override fun clickItemMyFriend(position: Int, userID: Int) {
        onClickProfile(userID)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun clickCancelFriendRequest(
        position: Int,
        data: Friend
    ) {
        notAcceptFriend(data.user_id)
        listFriendContactFrom.removeAt(position)
        friendRequestContactAdapter?.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun clickAcceptFriend(
        position: Int,
        data: Friend
    ) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = data.user_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket?.emit("approve-to-contact", jsonObject)
            WriteLog.d("approve-to-contact", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        acceptFriend(data.user_id)
        listFriendContactFrom.removeAt(position)
        friendRequestContactAdapter!!.notifyDataSetChanged()
    }

    //Api accept friend
    private fun acceptFriend(userID: Int?) {
        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = userID
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
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            setupDataSyncLocal()
                        } else {
                            Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                })
        }
    }

    //Api not accept friend
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
                        mainActivity?.setLoading(false)
                    }
                    override fun onSubscribe(d: Disposable) {

                    }
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            setupDataSyncLocal()
                        } else {
                            Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                })
        }
    }

    //Set click profile
    private fun onClickProfile(idProfile: Int) {
        val intent = Intent(mainActivity, ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), idProfile)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
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
                dialogPermission(requireActivity().resources.getString(R.string.title_permission_contact),
                    String.format(
                        requireActivity().resources.getString(R.string.note_permission_denied),
                        requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    requireActivity().resources.getString(R.string.step_two_open_permission),
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
                stubBinding?.itemContactPermission?.lnContactPermission?.hide()
                data = ArrayList()
                syncContacts()
            }

        })
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleOnRequestPermissionResult(
            requiredPermissionsCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {
                    stubBinding?.itemContactPermission?.lnContactPermission?.show()
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_permission_contact),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission),
                        R.drawable.ic_phone_book, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                requestPermission.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    stubBinding?.itemContactPermission?.lnContactPermission?.show()
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_permission_contact),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission),
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
                    stubBinding?.itemContactPermission?.lnContactPermission?.hide()
//                    mainActivity?.setLoading(true)
                    data = ArrayList()
                    syncContacts()
                }
            })

    }

    private fun syncContacts() {
        requireActivity().syncContactDevice(object : ContactUtilsHandler {
            override fun onSyncContact(result: ArrayList<ContactData>) {
                if (result.isNotEmpty()){
                    updatePhoneUser(result)
                }
            }

            override fun onSyncEndContact() {
                createContactsTogether()
            }

            override fun onSyncDbAndDevice(result: ArrayList<ContactData>) {
                if (result.isNotEmpty()) {
                    updatePhoneUser(result).apply {
                        createContactsTogether()
                    }
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
                        mainActivity?.setLoading(false)

                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        mainActivity?.setLoading(false)
//                        AloLineToast.makeText(mainActivity!!.baseContext, resources.getString(R.string.sync_contact_success), AloLineToast.LENGTH_LONG, AloLineToast.SUCCESS).show()
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
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
//                        getFriendContacts(page)
                        mainActivity?.setLoading(false)
                    } else {
                        Toast.makeText(
                            mainActivity,
                            mainActivity!!.baseContext.getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
    }

    override fun ClickItem(position: Int) {
        val intent = Intent(context, FriendRequestActivity::class.java)
        startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPress(): Boolean {
        return true
    }
}