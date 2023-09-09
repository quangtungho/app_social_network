package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.adapter.branch.ChooseBranchAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.branch.response.BranchResponse
import vn.techres.line.data.model.eventbus.BranchSelectedEventBus
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityChooseBranchBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.BranchHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class ChooseBranchActivity : BaseBindingActivity<ActivityChooseBranchBinding>(), BranchHandler {
    private var chooseBranchAdapter: ChooseBranchAdapter? = null
    private var branchList = ArrayList<Branch>()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var configJava = ConfigJava()
    private var typeChoose = ""
    override val bindingInflater: (LayoutInflater) -> ActivityChooseBranchBinding
        get() = ActivityChooseBranchBinding::inflate

    override fun onSetBodyView() {
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        user = CurrentUser.getCurrentUser(this)
        configJava = CurrentConfigJava.getConfigJava(this)

        typeChoose = intent.getStringExtra(TechresEnum.TYPE_CHOOSE_BRANCH.toString())?.toString() ?: ""

        chooseBranchAdapter = ChooseBranchAdapter(this)
        chooseBranchAdapter!!.setHasStableIds(true)
        chooseBranchAdapter!!.setChooseRestaurant(this)

        val preCachingLayoutManager =
            PreCachingLayoutManager(this, RecyclerView.VERTICAL, false)
        preCachingLayoutManager.setExtraLayoutSpace(1)
        chooseBranchAdapter!!.setHasStableIds(true)
        binding.rcChooseBranch.layoutManager = preCachingLayoutManager
        binding.rcChooseBranch.adapter = chooseBranchAdapter
        binding.rcChooseBranch.setHasFixedSize(true)
        binding.rcChooseBranch.setItemViewCacheSize(1)

        if (branchList.size == 0) {
            Handler(Looper.myLooper()!!).postDelayed({
                if (typeChoose == "create_review_news_feed"){
                    getListBranchReview()
                }else{
                    getListBranch()
                }

            }, 200)

        } else {
            chooseBranchAdapter!!.setDataSource(branchList)
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.svRestaurant.setOnClickListener {
            binding.svRestaurant.isIconified = false
        }


        binding.svRestaurant.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@ChooseBranchActivity, query, Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    chooseBranchAdapter!!.searchFullText(newText)
                }
                return true
            }
        })
    }

    override fun onChooseBranch(branch: Branch) {
        when (typeChoose) {
            "create_booking" -> {
                EventBus.getDefault().post(BranchSelectedEventBus(branch))
                onBackPressed()
            }
            "create_review_news_feed" -> {
                EventBus.getDefault().post(BranchSelectedEventBus(branch))
                onBackPressed()
            }
            "restaurant_image" -> {
                val intent = Intent(this, RestaurantImageActivity::class.java)
                intent.putExtra(TechresEnum.BRANCH_ID.toString(), branch.id)
                startActivity(intent)
                this.overridePendingTransition(
                    R.anim.translate_from_right,
                    R.anim.translate_to_right
                )
            }
        }
    }

    private fun getListBranch() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/branches?restaurant_id=${restaurant().restaurant_id!!}"
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
                        chooseBranchAdapter!!.setDataSource(branchList)
                    }

                }
            })

    }

    private fun getListBranchReview() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/branches/review?restaurant_id=${restaurant().restaurant_id!!}"
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
                        chooseBranchAdapter!!.setDataSource(branchList)
                    }

                }
            })

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}