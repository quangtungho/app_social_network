package vn.techres.line.activity

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.tabs.TabLayout
import com.sbrukhanda.fragmentviewpager.FragmentVisibilityListener
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.newsfeed.DetailReactiveManagerAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.ReactionSummaryReview
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.ReactionSummaryReviewResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.databinding.ActivityReactionDetailManagerBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class ReactionDetailManagerActivity : BaseBindingActivity<ActivityReactionDetailManagerBinding>(),
    FragmentVisibilityListener {
    private var configJava = ConfigJava()
    private var pagerAdapter: DetailReactiveManagerAdapter? = null
    private var data = ArrayList<ReactionSummaryReview>()
    private var reviewId = ""

    override val bindingInflater: (LayoutInflater) -> ActivityReactionDetailManagerBinding
        get() = ActivityReactionDetailManagerBinding::inflate

    override fun onSetBodyView() {
        configJava = CurrentConfigJava.getConfigJava(this)
        intent?.let {
            reviewId = it.getStringExtra(TechresEnum.ID_REVIEW.toString())!!.toString()
        }
        

        pagerAdapter = DetailReactiveManagerAdapter(supportFragmentManager)
        binding.fragmentPagerReaction.offscreenPageLimit = 1
        binding.fragmentPagerReaction.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(binding.tabLayoutReaction)
        )
        binding.fragmentPagerReaction.adapter = pagerAdapter
        binding.tabLayoutReaction.setupWithViewPager(binding.fragmentPagerReaction)

        if (data.size == 0) {
            getReviewReactionSummary(reviewId)
        } else {
            setCategoryReaction(data, reviewId)
        }

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getReviewReactionSummary(idReview: String) {
        val baseParams = BaseParams()
        baseParams.http_method = 0
        baseParams.project_id = AppConfig.PROJECT_CHAT
        baseParams.request_url = "/api/branch-reviews-reactions/$idReview/summary"
        ServiceFactory.run {
            createRetrofitServiceNode(
                TechResService::class.java
            )
                .getReviewReactionSummary(baseParams)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ReactionSummaryReviewResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: ReactionSummaryReviewResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            data = response.data
                            setCategoryReaction(data, idReview)
                        } else Toast.makeText(
                            this@ReactionDetailManagerActivity,
                            response.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })

        }
    }

    override fun onFragmentVisible() {
        binding.fragmentPagerReaction.notifyPagerVisible()
    }

    override fun onFragmentInvisible() {
        binding.fragmentPagerReaction.notifyPagerInvisible()
    }

    private fun setCategoryReaction(
        reactionSummaryReviewList: ArrayList<ReactionSummaryReview>,
        idReview: String
    ) {
        val arrayIconReaction = ArrayList<Int>()
        arrayIconReaction.add(R.drawable.icon_lovely)
        arrayIconReaction.add(R.drawable.icon_exciting)
        arrayIconReaction.add(R.drawable.icon_sad)
        arrayIconReaction.add(R.drawable.ic_diamond)
        arrayIconReaction.add(R.drawable.icon_nagative)
        arrayIconReaction.add(R.drawable.icon_nothing)

        pagerAdapter?.setDataSource(reactionSummaryReviewList, idReview)
        for (i in 0 until reactionSummaryReviewList.size) {
            val tab = binding.tabLayoutReaction.getTabAt(i)
//            val urlImage = String.format("%s%s", nodeJs.api_ads, array[i].link_original!!)
            tab?.setCustomView(R.layout.custom_tab_detail_reaction)
            val imgTabCountReaction =
                tab?.customView!!.findViewById<ImageView>(R.id.imgTabCountReaction)
            val tvTabCountReaction =
                tab.customView!!.findViewById<TextView>(R.id.tvTabCountReaction)
            tvTabCountReaction.text = reactionSummaryReviewList[i].name
            tvTabCountReaction.text = String.format(
                "%s %s",
                reactionSummaryReviewList[i].number,
                reactionSummaryReviewList[i].name
            )
//            imgTabCountReaction.setImageResource(arrayIconReaction[0])
            when (reactionSummaryReviewList[i].reaction_id) {
                -1 -> imgTabCountReaction.setImageResource(R.drawable.ic_dehaze)
                1 -> imgTabCountReaction.setImageResource(arrayIconReaction[0])
                2 -> imgTabCountReaction.setImageResource(arrayIconReaction[1])
                3 -> imgTabCountReaction.setImageResource(arrayIconReaction[2])
                4 -> imgTabCountReaction.setImageResource(arrayIconReaction[3])
                5 -> imgTabCountReaction.setImageResource(arrayIconReaction[4])
                6 -> imgTabCountReaction.setImageResource(arrayIconReaction[5])
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }
}