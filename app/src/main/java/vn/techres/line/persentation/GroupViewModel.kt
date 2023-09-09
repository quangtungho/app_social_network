package vn.techres.line.persentation

/*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
*/
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.data.GroupData
import vn.techres.line.helper.Resource
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var isCheckLoading = true
    private var _page = 0
    private val _groupData = MutableLiveData<GroupData>()
    private val _group = MutableLiveData<List<Group>>()


    private var _searchData = MutableLiveData<Resource<GroupData>>()
    val searchData: LiveData<Resource<GroupData>>
        get() = _searchData
    private val _groupSearch = savedStateHandle.getLiveData<String>("groupName", "")


    fun getGroup(page: Int, limit: Int) {
//        isCheckLoading = false
//        val baseRequest = BaseParams()
//        baseRequest.http_method = AppConfig.GET
//        baseRequest.project_id = AppConfig.getProjectChat()
//        baseRequest.request_url = GroupBaseUrl.getGroups(page, limit)
//        ServiceFactory.createRetrofitServiceNode(
//            TechResService::class.java
//        )
//            .getGroups(baseRequest)
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(object : Observer<GroupsResponse> {
//                override fun onComplete() {}
//                override fun onError(e: Throwable) {
//
//                }
//
//                override fun onSubscribe(d: Disposable) {}
//
//                @SuppressLint("ShowToast")
//                override fun onNext(response: GroupsResponse) {
//                    when (response.status) {
//                        AppConfig.SUCCESS_CODE -> {
//                            _page = page
//                            if (_page == 1) {
//                                _groupData.postValue(
//                                    response.data
//                                )
//                                _group.postValue(response.data.list)
//                            } else {
//                                insertList(response.data)
//                            }
//                            savedStateHandle.set("group", _group.value)
//                        }
//                    }
//                }
//            })
    }

    fun insert(position: Int, group: Group) {
        val data = _groupData.value!!
        data.list.add(position, group)
        _groupData.postValue(data)

    }

    fun insertList(groupData: GroupData) {
        val dataGroup = _groupData.value!!
        dataGroup.list.addAll(dataGroup.list.size, groupData.list)
        _groupData.postValue(dataGroup)
        _group.postValue(dataGroup.list)
    }

}