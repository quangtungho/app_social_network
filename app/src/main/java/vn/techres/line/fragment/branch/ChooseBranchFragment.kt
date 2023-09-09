package vn.techres.line.fragment.branch

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.branch.ChooseBranchAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.branch.response.BranchResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentChooseBranchBinding
import vn.techres.line.fragment.food.ChooseFoodRewardPointsFragment
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.BranchHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.collections.ArrayList


class ChooseBranchFragment : BaseBindingFragment<FragmentChooseBranchBinding>(FragmentChooseBranchBinding::inflate), BranchHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var chooseBranchAdapter: ChooseBranchAdapter? = null
    private var branchList = ArrayList<Branch>()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        user = CurrentUser.getCurrentUser(requireActivity())
        configJava = CurrentConfigJava.getConfigJava(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }
    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = requireActivity().getString(R.string.list_branch)
        chooseBranchAdapter = ChooseBranchAdapter(requireActivity())
        chooseBranchAdapter?.setHasStableIds(true)
        chooseBranchAdapter?.setChooseRestaurant(this)
        val preCachingLayoutManager =
            PreCachingLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        preCachingLayoutManager.setExtraLayoutSpace(1)
        chooseBranchAdapter?.setHasStableIds(true)
        binding.rcChooseBranch.layoutManager = preCachingLayoutManager
        binding.rcChooseBranch.adapter = chooseBranchAdapter
        binding.rcChooseBranch.setHasFixedSize(true)
        binding.rcChooseBranch.setItemViewCacheSize(1)

        if (branchList.size == 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                getListBranch()
            }, 200)

        } else {
            chooseBranchAdapter?.setDataSource(branchList)
        }

        binding.svRestaurant.setOnClickListener {
            binding.svRestaurant.isIconified = false
        }
        binding.svRestaurant.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(context, query, Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    chooseBranchAdapter?.searchFullText(newText)
                }
                return true
            }
        })
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onChooseBranch(branch: Branch) {
        arguments?.let {
            val bundleCreateBooking = it.getString(TechresEnum.KEY_CREATE_BOOKING.toString())
            val bundleRewardPoints = it.getString(TechresEnum.KEY_REWARD_POINT.toString())

            val bundleReview = it.getString(TechresEnum.KEY_CHOOSE_RESTAURANT_REVIEW.toString())
            when {
                bundleCreateBooking.toString() == requireActivity().getString(R.string.title_booking) -> {
                    val bundle = Bundle()
                    bundle.putString(TechresEnum.KEY_CHOOSE_BRANCH.toString(), Gson().toJson(branch))
                    mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
                    mainActivity?.supportFragmentManager?.popBackStack()
                }

                bundleCreateBooking.toString() == resources.getString(R.string.choose_branch_new_feed) -> {
                    val jsonBranch = Gson().toJson(branch)
                    cacheManager.put(TechresEnum.BRANCH_CHOOSE_NEW_FEED.toString(), jsonBranch)
                    onBackPress()
                }
                bundleRewardPoints.toString() == mainActivity!!.baseContext.getString(R.string.title_core) -> {
                    val bundle = Bundle()
                    val isTakeAway = 1
                    bundle.putInt(TechresEnum.KEY_ID_BRANCH_POINT.toString(), branch.id ?: 0)

                    cacheManager.put(
                        TechresEnum.KEY_CHECK_CHOOSE_FOOD_POINT.toString(),
                        resources.getString(R.string.choose_food_point)
                    )
                    cacheManager.put(TechresEnum.IS_TAKE_AWAY.toString(), isTakeAway.toString())
                    val chooseFoodRewardPointsFragment = ChooseFoodRewardPointsFragment()
                    chooseFoodRewardPointsFragment.arguments = bundle
                    mainActivity?.addOnceFragment(this, chooseFoodRewardPointsFragment)
                }
                bundleRewardPoints.toString() == mainActivity!!.baseContext.getString(R.string.OrderHome) -> {
                    val bundle = Bundle()
                    cacheManager.put(
                        TechresEnum.KEY_CHOOSE_BRANCH.toString(),
                        resources.getString(R.string.branch_choose)
                    )
                    bundle.putString(TechresEnum.KEY_ID_FOOD_RES.toString(), Gson().toJson(branch))
                    val branchFoodsFragment = BranchFoodsFragment()
                    branchFoodsFragment.arguments = bundle
                    mainActivity?.addOnceFragment(this, branchFoodsFragment)
                }
                bundleReview.toString() == getString(R.string.review) -> {
                    EventBus.getDefault().post(branch)
                    mainActivity?.supportFragmentManager?.popBackStack()
                }

                else -> {
                    val bundle = Bundle()
                    bundle.putString(TechresEnum.CHOOSE_BRANCH_FRAGMENT.toString(), Gson().toJson(branch))
                    mainActivity?.getOnRefreshFragment()?.onCallBack(bundle)
                    mainActivity?.supportFragmentManager?.popBackStack()
                }
            }
        }
    }

    private fun getListBranch() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/branches?restaurant_id=" + restaurant().restaurant_id!!
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getListBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }
                override fun onSubscribe(d: Disposable) {}
                @SuppressLint("ShowToast")
                override fun onNext(response: BranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        branchList = response.data.list
                        chooseBranchAdapter?.setDataSource(branchList)
                    }
                }
            })
    }

    override fun onBackPress() : Boolean{
        return true
    }
    
}