package vn.techres.line.fragment.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.branch.ChooseBranchAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.branch.response.BranchResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.databinding.FragmentChooseBranchGameBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.BranchHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import kotlin.collections.ArrayList

class ChooseBranchGameFragment : BaseBindingFragment<FragmentChooseBranchGameBinding>(FragmentChooseBranchGameBinding::inflate), BranchHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var chooseBranchAdapter: ChooseBranchAdapter? = null
    private var branchList = ArrayList<Branch>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        binding.header.tvTitleHomeHeader.text = mainActivity!!.baseContext.getString(R.string.list_branch)
        chooseBranchAdapter = ChooseBranchAdapter(mainActivity!!.baseContext)
        chooseBranchAdapter?.setChooseRestaurant(this)
        
        binding.rcBranchChooseGame.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }
        binding.rcBranchChooseGame.adapter = chooseBranchAdapter
        getListBranch()
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun getListBranch() {
        mainActivity?.setLoading(true)
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/branches?restaurant_id=" + restaurant().restaurant_id!!
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getListBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        branchList = response.data.list
                        chooseBranchAdapter?.setDataSource(branchList)
                    } else Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                    mainActivity?.setLoading(false)
                }
            })
    }

    override fun onChooseBranch(branch: Branch) {
        val bundle = Bundle()
        bundle.putString(TechresEnum.KEY_CHOOSE_BRANCH.toString(), Gson().toJson(branch))
        val gameFragment = GameFragment()
        gameFragment.arguments = bundle
        mainActivity?.addOnceFragment(this, gameFragment)
    }

    override fun onBackPress() : Boolean {
        return true
    }

}
