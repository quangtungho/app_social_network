package vn.techres.line.activity

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.newsfeed.SearchNewsFeedAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.ListReviewBranchParam
import vn.techres.line.data.model.response.ReviewBranchResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivitySearchNewsFeedBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.newsfeed.SearchNewsFeedHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class SearchNewsFeedActivity : BaseBindingActivity<ActivitySearchNewsFeedBinding>(), SearchNewsFeedHandler {
    private var page = 1
    private var total = 0
    private var limit = 30
    private var data = ArrayList<PostReview>()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var searchNewsFeedAdapter: SearchNewsFeedAdapter? = null
    private var isLoading = true

    override val bindingInflater: (LayoutInflater) -> ActivitySearchNewsFeedBinding
        get() = ActivitySearchNewsFeedBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        searchNewsFeedAdapter = SearchNewsFeedAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = searchNewsFeedAdapter
        searchNewsFeedAdapter?.setClickItemSearchNewsFeed(this)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.searchView.postDelayed({
            binding.searchView.requestFocus()
            imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_FORCED)
        }, 100)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText != "") {
                        Handler(Looper.myLooper()!!).postDelayed({
                            page = 1
                            data.clear()
                            getPostSearch(newText, page)
                        }, 220)
                    }
                }
                return true
            }
        })

        binding.searchView.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                showInputMethod(view.findFocus())
            }
        }
    }

    private fun showInputMethod(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    private fun getPostSearch(keyword: String, page: Int) {

        val listReviewBranchParam = ListReviewBranchParam()
        listReviewBranchParam.http_method = 0
        listReviewBranchParam.request_url = "/api/branch-reviews" +
                "?keyword=$keyword"
        listReviewBranchParam.params.restaurant_id = -1
        listReviewBranchParam.params.branch_id = -1
        listReviewBranchParam.params.branch_review_status = 1
        listReviewBranchParam.params.customer_id = -1
        listReviewBranchParam.params.types = "0,1,3"
        listReviewBranchParam.params.limit = limit
        listReviewBranchParam.params.page = page


        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getListReviewBranch(listReviewBranchParam)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ReviewBranchResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ReviewBranchResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val review = response.data.list
                        binding.txtSearchResult.visibility = View.VISIBLE
                        if (review.size == 0){
                            binding.txtSearchResult.text = resources.getString(R.string.result_not_found)
                        }else{
                            binding.txtSearchResult.text = resources.getString(R.string.search_results)
                        }
                        total = response.data.total_record!!
                        limit = response.data.limit!!
                        if (data.size == 0) {
                            data = review
                        } else {
                            isLoading = true
                            data.addAll(data.size, review)
                        }
                        searchNewsFeedAdapter!!.setDataSource(data)
                    }else{
                        binding.txtSearchResult.visibility = View.GONE
                    }
                }
            })
    }

    override fun onStop() {
        super.onStop()
        binding.searchView.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    override fun onClickItem(data: PostReview) {
        val intent = Intent(this, CommentActivity::class.java)
        intent.putExtra(TechresEnum.DETAIL_POST_COMMENT.toString(), Gson().toJson(data))
        intent.putExtra(TechresEnum.CHECK_COMMENT_CHOOSE.toString(), TechresEnum.TYPE_SEARCH_NEWS_FEED.toString())
        startActivity(intent)
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }


}