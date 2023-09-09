package vn.techres.line.fragment.card


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.restaurant.RestaurantCardAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.eventbus.EventBusRegisterRestaurantCard
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.UpdateRestaurantParams
import vn.techres.line.data.model.params.UserNodejsParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.restaurant.RestaurantCard
import vn.techres.line.data.model.restaurant.response.RestaurantCardResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.EventBusRestaurantCard
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentRestaurantCardBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.LickItem
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

/**
 * A simple [Fragment] subclass.
 */
class RestaurantCardFragment : BaseBindingFragment<FragmentRestaurantCardBinding>(FragmentRestaurantCardBinding::inflate), LickItem {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var adapterRestaurantCard: RestaurantCardAdapter? = null
    private var list = ArrayList<RestaurantCard>()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        adapterRestaurantCard = RestaurantCardAdapter(requireActivity())
        adapterRestaurantCard?.setLickItem(this)

        binding.rcMemberCard.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        binding.rcMemberCard.setHasFixedSize(true)
        binding.rcMemberCard.setItemViewCacheSize(1)
        adapterRestaurantCard?.setHasStableIds(true)
        binding.rcMemberCard.adapter = adapterRestaurantCard

        setData()
        setListener()
//        avatarPath = cacheManager.get(TechresEnum.AVATAR_PATH_REGISTER.toString())
//        avatarName = cacheManager.get(TechresEnum.AVATAR_NAME_REGISTER.toString())
//        if (avatarPath != ""){
//            getLinkAvatar(avatarName?:"")
//            uploadPhoto(avatarName?:"", avatarPath?:"", 1)
//            cacheManager.put(TechresEnum.AVATAR_PATH_REGISTER.toString(), "").toString()
//            cacheManager.put(TechresEnum.AVATAR_NAME_REGISTER.toString(), "").toString()
//        }
    }

    private fun setData(){
        if (list.size == 0){
            binding.shimmerLoading.visibility = View.VISIBLE
            binding.shimmerLoading.startShimmerAnimation()
            getRestaurantCard()
        }else{
            binding.shimmerLoading.visibility = View.GONE
            adapterRestaurantCard!!.setDataSource(list)
        }

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun setListener() {
        binding.svRestaurantCard.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    adapterRestaurantCard?.searchFullText(newText)
                }
                return true
            }
        })
        binding.swipeRefreshMemberCard.setOnRefreshListener {
            list.clear()
            getRestaurantCard()
            binding.swipeRefreshMemberCard.isRefreshing = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChooseRestaurantCard(eventBusRestaurantCard: EventBusRestaurantCard) {
        if (eventBusRestaurantCard.check) {
            getRestaurantCard()
        }
    }

    override fun onResume() {
        super.onResume()
        hideKeyboard()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    private fun getRestaurantCard() {

        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/membership-cards?restaurant_id=-1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getRestaurantCard(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantCardResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: RestaurantCardResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val data = response.data.list
                        binding.shimmerLoading.visibility = View.GONE
                        context?.let {
                            if (data.isEmpty()) {
                                binding.lnEmptyNotify.show()
                                binding.rcMemberCard.hide()
                            } else {
                                binding.lnEmptyNotify.hide()
                                binding.rcMemberCard.show()
                            }
                        }
                        if (!list.containsAll(data)) {
                            list = data
                            adapterRestaurantCard?.setDataSource(list)
                        }
                    }
                }
            })

    }

    override fun onClickItem(restaurant: RestaurantCard) {
        saveRestaurantInfo(restaurant)
        var user = CurrentUser.getCurrentUser(requireActivity())
        user.restaurant_id = restaurant.restaurant_id
        CurrentUser.saveUserInfo(requireActivity(), user)
        cacheManager.put(TechresEnum.RESTAURANT_ID.toString(), restaurant.restaurant_id.toString())
        cacheManager.put(TechresEnum.KEY_CHECK_LOAD_REVIEW.toString(), "false")
//        updateRestaurantId(restaurant.restaurant_id)
        updateRestaurant(restaurant.restaurant_id)
//        onBackPress()
        hideKeyboard()
        EventBus.getDefault().post(EventBusRegisterRestaurantCard())
//        onBackPress()
    }

    private fun Fragment.hideKeyboard() {
        val activity = this.activity
        if (activity is AppCompatActivity) {
            activity.hideKeyboard()
        }
    }

    private fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

    }

    private fun updateRestaurantId(restaurantID: Int?) {
        mainActivity?.setLoading(true)
        val params = UserNodejsParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/user-party/update-restaurant-user"
        params.params.user_id = user.id
        params.params.restaurant_id = restaurantID

        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .updateRestaurantId(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    mainActivity?.setLoading(false)
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {
                    mainActivity?.setLoading(false)
                }
            })
    }

    private fun updateRestaurant(restaurantID: Int?) {

        val params = UpdateRestaurantParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customer-membership-cards/create"
        params.params.restaurant_id = restaurantID
        params.project_id  = AppConfig.PROJECT_CHAT
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .updateRestaurant(params)
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

    override fun onBackPress() : Boolean{
        return when (cacheManager.get(TechresEnum.ACCOUNT_FRAGMENT.toString())) {
            TechresEnum.ACCOUNT_FRAGMENT.toString() -> {
                cacheManager.put(TechresEnum.ACCOUNT_FRAGMENT.toString(), "")
                true
            }
            else -> {
                false
            }
        }
    }
}
