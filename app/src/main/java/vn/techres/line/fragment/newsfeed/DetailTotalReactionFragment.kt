package vn.techres.line.fragment.newsfeed

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import vn.techres.line.activity.ChatActivity
import vn.techres.line.activity.MediaSliderActivity
import vn.techres.line.activity.ProfileActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.newsfeed.DetailTotalReactionAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.*
import vn.techres.line.data.model.chat.request.personal.CreateGroupPersonalRequest
import vn.techres.line.data.model.chat.response.GroupPersonalResponse
import vn.techres.line.data.model.friend.AddFriendRequest
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.reaction.InteractiveUser
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.ReactionPostResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentDetailTotalReactionBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.techresenum.TechresEnumFriend
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.ClickProfile
import vn.techres.line.interfaces.DetailTotalReactionHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

class DetailTotalReactionFragment : BaseBindingFragment<FragmentDetailTotalReactionBinding>(FragmentDetailTotalReactionBinding::inflate), DetailTotalReactionHandler, ClickProfile {

    private var configJava = ConfigJava()
    private var data = ArrayList<InteractiveUser>()
    private var adapter: DetailTotalReactionAdapter? = null

    //socket
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var configNodeJs = ConfigNodeJs()
    private var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        configJava = CurrentConfigJava.getConfigJava(requireActivity().baseContext)
        mSocket?.connect()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        adapter = DetailTotalReactionAdapter(requireActivity().baseContext)
        binding.rcReaction.layoutManager =
            LinearLayoutManager(requireActivity().baseContext, RecyclerView.VERTICAL, false)
        binding.rcReaction.adapter = adapter
        adapter?.setClickFriend(this@DetailTotalReactionFragment)
        adapter?.setClickProfile(this@DetailTotalReactionFragment)
        arguments?.let {
            Handler(Looper.getMainLooper()).postDelayed({
                getReviewReactionDetail(
                    it.getString(TechresEnum.ID_REVIEW.toString())?:"", it.getInt(
                        TechresEnum.ID_REACTION.toString()
                    )
                )
            }, 100)
        }
        setListener()
    }

    fun newInstance(idReview: String, idReaction: Int): DetailTotalReactionFragment {
        val args = Bundle()
        args.putString(TechresEnum.ID_REVIEW.toString(), idReview)
        args.putInt(TechresEnum.ID_REACTION.toString(), idReaction)
        val fragment = DetailTotalReactionFragment()
        fragment.arguments = args
        return fragment
    }


    @SuppressLint("UseRequireInsteadOfGet")
    private fun setListener() {

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

    private fun getReviewReactionDetail(idReview: String, idReaction: Int){
        val baseParams = BaseParams()
        baseParams.http_method = AppConfig.GET
        baseParams.project_id = AppConfig.PROJECT_CHAT
        baseParams.request_url = "/api/branch-reviews-reactions/$idReview?page=1&limit=100&reaction_id=$idReaction"
        ServiceFactory.run {
            createRetrofitServiceNode(
                TechResService::class.java
            )
                .getReviewReactionDetail(baseParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ReactionPostResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                   
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: ReactionPostResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            data = response.data.list_user_reaction
                            adapter!!.setDataSource(data)
                        } else Toast.makeText(
                            requireActivity(),
                            requireActivity().baseContext.getString(R.string.api_error),
                            Toast.LENGTH_LONG
                        ).show()
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
                            val intent = Intent(requireActivity(), ChatActivity::class.java)
                            intent.putExtra(TechresEnum.GROUP_CHAT.toString(), Gson().toJson(responseGroup.data))
                            startActivity(intent)
                            requireActivity().overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
                        } else {
                            Toast.makeText(requireActivity(), responseGroup.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                })
        }
    }

    override fun clickItemMyFriend(interactiveUser: InteractiveUser) {
        val intent = Intent(requireActivity(), ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), interactiveUser.user_id ?: 0)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onChat(interactiveUser: InteractiveUser) {
        val memberList = ArrayList<Int>()
        memberList.add(user.id)
        memberList.add(interactiveUser.user_id ?: 0)
        createGroupPersonal(memberList, interactiveUser.user_id!!)
    }

    override fun clickAddFriend(interactiveUser: InteractiveUser) {

        if (interactiveUser.status == 4) {
            addFriend(interactiveUser)

        } else {
            approveToContact(interactiveUser)
        }
    }

    private fun addFriend(interactiveUser: InteractiveUser) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.avatar_sender = user.avatar_three_image.original
        addFriendRequest.full_name_sender = user.name
        addFriendRequest.phone_sender = user.phone
        addFriendRequest.user_id_received = interactiveUser.user_id
        addFriendRequest.avatar_received = interactiveUser.avatar.original
        addFriendRequest.full_name_received = interactiveUser.full_name
        addFriendRequest.phone_received = interactiveUser.phone
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket!!.emit(TechresEnumFriend.REQUEST_TO_CONTACT.toString(), jsonObject)
            WriteLog.d("REQUEST_TO_CONTACT", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    
        val addFriendParams = FriendParams()
        addFriendParams.http_method = AppConfig.POST
        addFriendParams.request_url = "/api/contact-tos/request"
        addFriendParams.project_id = AppConfig.PROJECT_CHAT

        addFriendParams.params.contact_to_user_id = interactiveUser.user_id
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
                    override fun onNext(response: BaseResponse) {

                    }
                })
        }
    }

    private fun approveToContact(interactiveUser: InteractiveUser) {
        val addFriendRequest = AddFriendRequest()
        addFriendRequest.user_id_sender = user.id
        addFriendRequest.user_id_received = interactiveUser.user_id
        try {
            val jsonObject = JSONObject(Gson().toJson(addFriendRequest))
            mSocket!!.emit(TechresEnumFriend.APPROVE_TO_CONTACT.toString(), jsonObject)
            WriteLog.d("APPROVE_TO_CONTACT", jsonObject.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val params = FriendAcceptParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/contact-froms/accept"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.contact_from_user_id = interactiveUser.user_id


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

                    }
                })
        }
    }

    override fun clickProfile(position: Int, userId: Int) {
        val intent = Intent(requireActivity(), ProfileActivity::class.java)
        intent.putExtra(TechresEnum.ID_USER.toString(), userId)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    override fun onAvatar(url: String, position: Int) {
        val list = ArrayList<String>()
        list.add(String.format("%s%s", configNodeJs.api_ads, url))
        val intent = Intent(requireActivity(), MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
    }

    override fun onBackPress() : Boolean {
        return true
    }
}
