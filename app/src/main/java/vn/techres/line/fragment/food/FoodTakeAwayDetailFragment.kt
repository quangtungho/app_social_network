package vn.techres.line.fragment.food

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.food.FoodTakeAwayDetailAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.FoodTakeAwayResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentFoodTakeAwayDetailBinding
import vn.techres.line.fragment.branch.ChooseBranchFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.OnRefreshFragment
import vn.techres.line.interfaces.food.FoodTakeAwayDetailHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.view.GridSpacingDecoration
import java.util.*
import kotlin.collections.ArrayList

class FoodTakeAwayDetailFragment : BaseBindingFragment<FragmentFoodTakeAwayDetailBinding>(FragmentFoodTakeAwayDetailBinding::inflate),
    OnRefreshFragment, FoodTakeAwayDetailHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var foodList = ArrayList<FoodTakeAway>()
    private var adapter : FoodTakeAwayDetailAdapter? = null
    private var branch : Branch? = null
    private var user = User()
    private var configNodeJs = ConfigNodeJs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        user = CurrentUser.getCurrentUser(requireActivity())
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text =
            requireActivity().resources.getString(R.string.see_more)
        mainActivity?.setOnRefreshFragment(this)

        adapter = FoodTakeAwayDetailAdapter(requireActivity())
        adapter?.setFoodTakeAwayDetailHandler(this)
        val layoutManager = GridLayoutManager(requireActivity().baseContext, 2)
        binding.rcFoodTakeAway.layoutManager = layoutManager
        binding.rcFoodTakeAway.addItemDecoration(GridSpacingDecoration(10, 2))
        binding.rcFoodTakeAway.itemAnimator = DefaultItemAnimator()
        binding.rcFoodTakeAway.adapter = adapter

        arguments?.let {
            branch = Gson().fromJson(
                it.getString(TechresEnum.FOOD_TAKE_AWAY_DETAIL_FRAGMENT.toString()), object :
                    TypeToken<Branch>() {}.type
            )
            setData(branch)
            setListener()
        }

    }

    private fun setData(branch: Branch?){
        if(foodList.size == 0){
            branch?.id?.let { getFoodTakeAway(it) }
        }else{
            adapter?.setDataSource(foodList)
        }
        binding.tvBranchName.text = String.format("%s\n%s", requireActivity().resources.getString(R.string.branch), branch?.name ?: "")
    }
    private fun setListener(){
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.tvChangeBranch.setOnClickListener {
            val bundle = Bundle()
            val chooseBranchFragment = ChooseBranchFragment()
            chooseBranchFragment.arguments = bundle
            mainActivity?.addOnceFragment(this, chooseBranchFragment)
        }

        binding.svFood.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    adapter?.searchFullText(newText)
                }
                return true
            }
        })

    }

    private fun getFoodTakeAway(branchId: Int) {
        mainActivity?.setLoading(true)
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/foods/take-away?branch_id=$branchId&category_type=-1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getFoodTakeAway(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FoodTakeAwayResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: FoodTakeAwayResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        foodList = response.data
                        adapter?.setDataSource(foodList)
                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                    mainActivity?.setLoading(false)
                }
            })
    }

    override fun onCallBack(bundle: Bundle) {
        if(bundle.getString(TechresEnum.CHOOSE_BRANCH_FRAGMENT.toString()) != null){
            val branch = Gson().fromJson<Branch>(
                bundle.getString(TechresEnum.CHOOSE_BRANCH_FRAGMENT.toString()), object :
                    TypeToken<Branch>() {}.type
            )
            if(this.branch?.id != branch.id){
                foodList = ArrayList()
                branch?.id?.let { getFoodTakeAway(it) }
                binding.tvBranchName.text = String.format("%s\n%s", requireActivity().resources.getString(R.string.branch), branch?.name ?: "")
                this.branch = branch
            }
        }
    }

    override fun onChooseFood(food: FoodTakeAway) {
        val bundle = Bundle()
        bundle.putString(TechresEnum.FOOD_DETAIL_FRAGMENT.toString(), Gson().toJson(food))
        val foodDetailFragment = FoodDetailFragment()
        foodDetailFragment.arguments = bundle
        mainActivity?.addOnceFragment(this, foodDetailFragment)
    }

    override fun onBackPress() : Boolean {
        return true
    }
}