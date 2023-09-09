package vn.techres.line.activity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.adapter.BestFriendSelectedAdapter
import vn.techres.line.adapter.friend.AddBestFriendAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.friend.response.FriendResponse
import vn.techres.line.data.model.params.AddBestFriendParams
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityAddBestFriendBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.WriteLog
import vn.techres.line.interfaces.AddBestFriendHandler
import vn.techres.line.interfaces.RemoveBestFriendHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService


class AddBestFriendActivity : BaseBindingActivity<ActivityAddBestFriendBinding>(),
    AddBestFriendHandler, RemoveBestFriendHandler {
    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private val application = TechResApplication()
    private var mSocket: Socket? = null

    private var limit = -1
    private var page = 1

    private var friendList = ArrayList<Friend>()
    private var chooseFriendList = ArrayList<Friend>()
    private var addBestFriendAdapter: AddBestFriendAdapter? = null
    private var bestFriendSelectedAdapter: BestFriendSelectedAdapter? = null

    override val bindingInflater: (LayoutInflater) -> ActivityAddBestFriendBinding
        get() = ActivityAddBestFriendBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        mSocket = application.getSocketInstance(this)
        mSocket!!.connect()
        addBestFriendAdapter = AddBestFriendAdapter(this)
        bestFriendSelectedAdapter = BestFriendSelectedAdapter(this)
        binding.recyclerViewFriend.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewFriendSelected.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewFriend.adapter = addBestFriendAdapter
        binding.recyclerViewFriendSelected.adapter = bestFriendSelectedAdapter
        addBestFriendAdapter?.setChooseBestFriend(this)
        bestFriendSelectedAdapter?.setRemoveBestFriend(this)

        setData()
        setClick()
    }

    private fun setClick() {
        binding.imgBtnAdd.setOnClickListener {
            addBestFriend()
        }
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    addBestFriendAdapter?.searchFullText(newText)
                }
                return true
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setData() {
        if (friendList.size == 0){
            binding.shimmerBestFriend.visibility = View.VISIBLE
            binding.shimmerBestFriend.startShimmerAnimation()
            getListFriend()
        }else{
            binding.shimmerBestFriend.visibility = View.GONE
        }

        bestFriendSelectedAdapter?.setDataSource(chooseFriendList)
    }

    private fun getListFriend() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = String.format("%s%s%s%s", "/api/friends/not-in-close-friend?limit=", limit, "&page=", page)
        restaurant().restaurant_id?.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .getListFriendNotBestFriend(baseRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<FriendResponse> {
                    override fun onComplete() {
                        WriteLog.d("COMPLETE", "Complete")
                    }
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {
                        WriteLog.d("SUBSCRIBE", "Subscribe")
                    }
                    override fun onNext(response: FriendResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            friendList.clear()
                            friendList = response.data.list
                            addBestFriendAdapter!!.setDataSource(friendList)
                            binding.shimmerBestFriend.visibility = View.GONE
                            binding.shimmerBestFriend.hide()
                        } else {
                            Toast.makeText(
                                this@AddBestFriendActivity,
                                response.message,
                                Toast.LENGTH_LONG).show()
                        }
                    }
                })
        }
    }

    private fun addBestFriend(){
        val listIdBestFriend = ArrayList<Int>()
        val params = AddBestFriendParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/friends/create-close-friend"
        params.project_id = AppConfig.PROJECT_CHAT
        for (i in chooseFriendList.indices){
            listIdBestFriend.add(chooseFriendList[i].user_id?:0)
        }
        params.params.list_user_ids = listIdBestFriend
        params.params.type = 1
        params.params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )

                .addBestFriend(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {
                        WriteLog.d("COMPLETE", "Complete")
                    }
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }
                    override fun onSubscribe(d: Disposable) {
                        WriteLog.d("SUBSCRIBE", "Subscribe")
                    }
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            for(i in chooseFriendList.indices){
                                chooseFriendList[i].type = 4
                            }
                            TechResApplication.applicationContext().getFriendDao()?.insertAllData(chooseFriendList)
                            EventBus.getDefault().post(chooseFriendList)
                            onBackPressed()
                        } else{
                            Toast.makeText(
                                this@AddBestFriendActivity,
                                response.message,
                                Toast.LENGTH_LONG).show()
                        }
                    }
                })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun chooseBestFriend(friend: Friend) {
        friendList = addBestFriendAdapter?.getDataSource() ?: ArrayList()
        val position = friendList.indexOfFirst { it.user_id == friend.user_id }
        if (friend.isCheck == true) {
            chooseFriendList.removeAt(chooseFriendList.indexOfFirst { it.user_id == friend.user_id })
            friendList[position].isCheck = false
        } else {
            chooseFriendList.add(friend)
            friendList[position].isCheck = true
        }
        bestFriendSelectedAdapter?.notifyDataSetChanged()
        addBestFriendAdapter?.notifyItemChanged(position)
        binding.recyclerViewFriendSelected.scrollToPosition(chooseFriendList.size - 1)
        if (chooseFriendList.size != 0){
            binding.cvConfirm.visibility = View.VISIBLE
        }else{
            binding.cvConfirm.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun removeBestFriend(friend: Friend) {
        friendList = addBestFriendAdapter?.getDataSource() ?: ArrayList()
        val position = chooseFriendList.indexOfFirst { it.user_id == friend.user_id }
        val positionTag = friendList.indexOfFirst { it.user_id == friend.user_id }
        chooseFriendList.removeAt(position)
        friendList[positionTag].isCheck = false
        addBestFriendAdapter?.notifyItemChanged(positionTag)
        bestFriendSelectedAdapter?.notifyDataSetChanged()
        if (chooseFriendList.size != 0){
            binding.cvConfirm.visibility = View.VISIBLE
        }else{
            binding.cvConfirm.visibility = View.GONE
        }
    }

}