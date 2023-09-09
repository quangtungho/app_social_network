package vn.techres.line.activity

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.giphy.sdk.analytics.GiphyPingbacks.context
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.adapter.branch.BranchSaveAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.branch.SaveBranch
import vn.techres.line.data.model.branch.response.SaveBranchResponse
import vn.techres.line.data.model.eventbus.EventBusSaveBranch
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.databinding.ActivitySaveBranchBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.branch.SaveBranchHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SaveBranchActivity : BaseBindingActivity<ActivitySaveBranchBinding>(), SaveBranchHandler {
    private var branchSaveAdapter: BranchSaveAdapter? = null
    private var saveBranchList = ArrayList<SaveBranch>()

    override val bindingInflater: (LayoutInflater) -> ActivitySaveBranchBinding
        get() = ActivitySaveBranchBinding::inflate

    override fun onSetBodyView() {
        this@SaveBranchActivity.window?.statusBarColor =
            AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.favorite_restaurant)
        binding.rcSaveBranch.layoutManager =
            LinearLayoutManager(this@SaveBranchActivity, RecyclerView.VERTICAL, false)
        branchSaveAdapter = BranchSaveAdapter(baseContext)
        branchSaveAdapter?.setSaveBranchHandler(this)
        binding.rcSaveBranch.adapter = branchSaveAdapter
        getSaveBranch()
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSaveBranch(boolean: EventBusSaveBranch) {
        if (boolean.isCheck) {
            getSaveBranch()
        }
    }

    private fun getSaveBranch() {

        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/customers/" + CurrentUser.getCurrentUser(this@SaveBranchActivity).id + "/saved-branches"


        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getSaveBranch(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<SaveBranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: SaveBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        saveBranchList = response.data.list
                        branchSaveAdapter?.setDataSource(saveBranchList)
                    }
                }
            })

    }

    override fun onSaveBranch(position: Int, branchID: Int) {
//        val bundle = Bundle()
//        bundle.putInt(TechresEnum.BRANCH_ID.toString(), branchID)
//        mainActivity?.let {
//            Navigation.findNavController(
//                it,
//                R.id.nav_host
//            ).navigate(R.id.action_saveBranchFragment_to_detailBranchFragment, bundle)
//        }
        val intent = Intent(this@SaveBranchActivity, BranchDetailActivity::class.java)
        intent.putExtra(TechresEnum.BRANCH_ID.toString(), branchID)
        startActivity(intent)
        this@SaveBranchActivity.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    fun onAttach(context: Context) {
        this.onAttach(context)
        EventBus.getDefault().register(this)

    }

    fun onDetach() {
        this.onDetach()
        EventBus.getDefault().unregister(this)
    }

}