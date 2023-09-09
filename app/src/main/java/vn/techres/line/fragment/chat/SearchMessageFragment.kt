package vn.techres.line.fragment.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.response.SearchMessageResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentSearchMessageBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

@SuppressLint("UseRequireInsteadOfGet")
class SearchMessageFragment : BaseBindingFragment<FragmentSearchMessageBinding>(FragmentSearchMessageBinding::inflate){
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private val application = TechResApplication()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var mSocket: Socket? = null
    private var group = Group()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity())
        mSocket?.connect()
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        user = CurrentUser.getCurrentUser(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            group = Gson().fromJson(
                it.getString(
                    TechresEnum.GROUP_CHAT.toString()
                ), object :
                    TypeToken<Group>() {}.type
            )
        }
        binding.svShareMessage.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != null && newText.trim { it <= ' ' }.isNotEmpty()){
                    getSearchMessageGroup(newText)
                    getSearchMessagePersonal(newText)
                }
                return true
            }

        })
    }

    private fun getSearchMessageGroup(string : String) {

        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/message-party/search-message?group_id=" + group._id!!.toString() + "&keyWord=$string"
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .getMessageGroup(
        
                    baseRequest
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<SearchMessageResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: SearchMessageResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {

                        } else {
                            Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG).show()
                        }
                        mainActivity?.setLoading(false)
                    }
                })

    }

    private fun getSearchMessagePersonal(string : String) {

        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        baseRequest.request_url = "/api/message-personal-party/search-message?group_id=" + group._id!!.toString() + "&keyWord=$string"
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .getMessagePersonal(
         
                    baseRequest
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<SearchMessageResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        mainActivity?.setLoading(false)
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: SearchMessageResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {

                        } else {
                            Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG).show()
                        }
                        mainActivity?.setLoading(false)
                    }
                })
        
    }

    override fun onBackPress() : Boolean {
        return true
    }

}

