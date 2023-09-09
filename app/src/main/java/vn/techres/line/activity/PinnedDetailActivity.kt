package vn.techres.line.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shashank.sony.fancytoastlib.FancyToast
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
import vn.techres.line.adapter.chat.PinnedDetailAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.chat.request.PinnedRequest
import vn.techres.line.data.model.chat.request.RevokePinnedChatRequest
import vn.techres.line.data.model.chat.response.PinnedResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityPinnedDetailBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.chat.EventBusScrollMessPin
import vn.techres.line.interfaces.chat.PinnedDetailHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*
import java.util.stream.Collectors
import kotlin.math.ceil

class PinnedDetailActivity : BaseBindingActivity<ActivityPinnedDetailBinding>(),
    PinnedDetailHandler {
    private var pinnedList = ArrayList<MessagesByGroup>()
    private var pinnedDetailAdapter: PinnedDetailAdapter? = null
    private var group = Group()
    private var isCheck = 0
    private var page = 1
    private var limit = 30
    private var totalRecord = 0f
    private var isCheckBack = false

    //socket
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    override val bindingInflater: (LayoutInflater) -> ActivityPinnedDetailBinding
        get() = ActivityPinnedDetailBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        user = CurrentUser.getCurrentUser(this)
        mSocket?.connect()
    }

    override fun onSetBodyView() {
        pinnedDetailAdapter = PinnedDetailAdapter(this)
        pinnedDetailAdapter?.setPinnedDetailHandler(this)
        binding.rcPinned.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcPinned.adapter = pinnedDetailAdapter
        binding.tvTitleHomeHeader.text =
            resources.getString(R.string.list_pinned)
                .uppercase(Locale.getDefault())
        intent?.let {
            group = Gson().fromJson(
                intent.getStringExtra(
                    TechresEnum.GROUP_CHAT.toString()
                ), object :
                    TypeToken<Group>() {}.type
            )
            getPinned(page)
            isCheck =
                if (intent.getStringExtra(TechresEnum.PINNED_DETAIL.toString()) == TechresEnum.CHAT_PERSONAL.toString()) {
                    0
                } else {
                    1
                }
        }
        binding.swipeRefresh.setOnRefreshListener {
            page = 1
            pinnedList = ArrayList()
            getPinned(page)
        }
        binding.imgBack.setOnClickListener {
            isCheckBack = true
            onBackPressed()
        }

        binding.rcPinned.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y <= 0) {
                        if (page < ceil((totalRecord / limit).toDouble())) {
                            page++
                            getPinned(page)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })
    }

    private fun getPinned(page: Int) {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            String.format(
                "%s%s%s%s%s%s",
                "/api/pinned-messages?group_id=",
                group._id,
                "&limit=",
                limit,
                "&page=",
                page
            )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java

        )
            .getPinnedDetail(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<PinnedResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: PinnedResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        response.data?.let {
                            if (pinnedList.size == 0) {
                                pinnedList = it.list
                            } else {
                                pinnedList.addAll(pinnedList.size, it.list)
                            }
                            totalRecord = (it.total_record ?: 0).toFloat()
                            pinnedDetailAdapter?.setDataSource(pinnedList)
                            if (!isCheckBack) {
                                binding.swipeRefresh.isRefreshing = false
                            }
                        }
                    }
                }
            })
    }


    private fun removePinned(messagesByGroup: MessagesByGroup) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = String.format(
            "%s%s%s%s",
            "/api/pinned-messages/remove?random_key=",
            messagesByGroup.random_key,
            "&receiver_id=",
            messagesByGroup.receiver_id
        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .removePinned(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        FancyToast.makeText(
                            this@PinnedDetailActivity,
                            resources.getString(R.string.success_delete_pinned),
                            FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                            false
                        ).show()
                    }

                }
            })

    }

    private fun pinned(messagesByGroup: MessagesByGroup, isCheck: Int) {
        val pinnedRequest = PinnedRequest()
        pinnedRequest.random_key = messagesByGroup.random_key
        pinnedRequest.member_id = user.id
        pinnedRequest.group_id = group._id
        pinnedRequest.message_type = TechResEnumChat.TYPE_PINNED.toString()
        pinnedRequest.key_message_error = Utils.getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(pinnedRequest))
            if (isCheck == 1) {
                mSocket?.emit(TechResEnumChat.PINNED_MESSAGE_ALO_LINE.toString(), jsonObject)
                WriteLog.d("PINNED_MESSAGE_ALO_LINE", jsonObject.toString())
            } else {
                mSocket?.emit(
                    TechResEnumChat.PINNED_MESSAGE_PERSONAL_ALO_LINE.toString(),
                    jsonObject
                )
                WriteLog.d("PINNED_MESSAGE_PERSONAL_ALO_LINE", jsonObject.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun unPinned(idPinnedMessage: String, isCheck: Int) {
        val revokePinnedChatRequest = RevokePinnedChatRequest()
        revokePinnedChatRequest.random_key = idPinnedMessage
        revokePinnedChatRequest.group_id = group._id
        revokePinnedChatRequest.member_id = user.id
        revokePinnedChatRequest.message_type = TechResEnumChat.TYPE_REMOVE_PINNED.toString()
        revokePinnedChatRequest.key_message_error = Utils.getRandomString(10)
        try {
            val jsonObject = JSONObject(Gson().toJson(revokePinnedChatRequest))

            if (isCheck == 0) {
                mSocket?.emit(
                    TechResEnumChat.REVOKE_PINNED_PERSONAL_ALO_LINE.toString(),
                    jsonObject
                )
                WriteLog.d("REVOKE_PINNED_PERSONAL_ALO_LINE", jsonObject.toString())

            } else {
                mSocket?.emit(
                    TechResEnumChat.REVOKE_PINNED_MESSAGE_ALO_LINE.toString(),
                    jsonObject
                )
                WriteLog.d("REVOKE_PINNED_MESSAGE_ALO_LINE", jsonObject.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        FancyToast.makeText(
            this@PinnedDetailActivity,
            resources.getString(R.string.success_unpinned),
            FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
            false
        ).show()
    }

    fun onBackPress(): Boolean {
        isCheckBack = true
        return true
    }

    override fun onRemove(messagesByGroup: MessagesByGroup) {
        pinnedList.forEachIndexed { index, messagesByGroup ->
            if (messagesByGroup.random_key == messagesByGroup.random_key) {
                if (messagesByGroup.status == 1) {
                    pinnedList[index].random_key?.let { unPinned(it, isCheck) }
                }
                removePinned(messagesByGroup)
                pinnedList.removeAt(index)
                pinnedDetailAdapter?.notifyItemRemoved(index)
                pinnedDetailAdapter?.notifyItemRangeChanged(index, pinnedList.size)
                return
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPinRetry(messagesByGroup: MessagesByGroup) {
        pinnedList.forEachIndexed { index, messagesByGroup ->
            if (messagesByGroup.random_key == messagesByGroup.random_key) {
                pinned(pinnedList[index], isCheck)
                pinnedList[index].status = 1
                val listPinned: ArrayList<MessagesByGroup> =
                    pinnedList.stream()
                        .filter { x -> x.status == 1 && x.random_key != messagesByGroup.random_key }
                        .collect(Collectors.toList()) as ArrayList<MessagesByGroup>
                val listRetryPinned: ArrayList<MessagesByGroup> =
                    pinnedList.stream().filter { x -> x.random_key == messagesByGroup.random_key }
                        .collect(Collectors.toList()) as ArrayList<MessagesByGroup>
                val listUnPinned = pinnedList.stream().filter { x -> x.status == 0 }
                    .collect(Collectors.toList()) as ArrayList<MessagesByGroup>
                listPinned.sortByDescending {
                    it.created_at
                }
                pinnedList.clear()
                pinnedList.addAll(listRetryPinned)
                pinnedList.addAll(listPinned)
                pinnedList.addAll(listUnPinned)
                pinnedDetailAdapter?.notifyDataSetChanged()
                return
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onPinUnpin(mess: MessagesByGroup) {
        pinnedList.forEachIndexed { index, messagesByGroup ->
            if (messagesByGroup.random_key == mess.random_key) {
                pinnedList[index].random_key?.let { unPinned(it, isCheck) }
                pinnedList[index].status = 0
                val listPinned: ArrayList<MessagesByGroup> =
                    pinnedList.stream()
                        .filter { x -> x.status == 1 && x.random_key != mess.random_key }
                        .collect(Collectors.toList()) as ArrayList<MessagesByGroup>
                val listUnPinned = pinnedList.stream().filter { x -> x.status == 0 }
                    .collect(Collectors.toList()) as ArrayList<MessagesByGroup>
                listPinned.sortByDescending {
                    it.created_at
                }
                pinnedList.clear()
                pinnedList.addAll(listPinned)
                pinnedList.addAll(listUnPinned)
                pinnedDetailAdapter?.notifyDataSetChanged()
                return
            }
        }
    }

    @Subscribe
    fun onSrollMessagePin(event: EventBusScrollMessPin) {
        isCheckBack = true
        onBackPressed()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}