package vn.techres.line.fragment.newsfeed


import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.sbrukhanda.fragmentviewpager.FragmentVisibilityListener
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.newsfeed.DetailReactiveManagerAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.ReactionSummaryReview
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.databinding.FragmentDetailReactionManagerBinding
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil

class DetailReactionManagerFragment : BaseBindingFragment<FragmentDetailReactionManagerBinding>(FragmentDetailReactionManagerBinding::inflate), FragmentVisibilityListener {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var configJava = ConfigJava()
    private var pagerAdapter: DetailReactiveManagerAdapter? = null
    private var data = ArrayList<ReactionSummaryReview>()
    private var reviewId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        reviewId = requireArguments().getString(TechresEnum.ID_REVIEW.toString())?:""

        pagerAdapter = DetailReactiveManagerAdapter(childFragmentManager)
        binding.fragmentPagerReaction.offscreenPageLimit = 1
        binding.fragmentPagerReaction.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(binding.tabLayoutReaction)
        )
        binding.fragmentPagerReaction.adapter = pagerAdapter
        binding.tabLayoutReaction.setupWithViewPager(binding.fragmentPagerReaction)

        if (data.size == 0) {
            getReviewReactionSummary()
        } else {
            setCategoryReaction(data, reviewId)
        }

        binding.imgBack.setOnClickListener {
//            mainActivity!!.findNavController(R.id.nav_host).popBackStack()
            onBackPress()
        }
    }


    override fun onBackPress() : Boolean {
        return true
    }

    private fun getReviewReactionSummary() {
//        mainActivity?.setLoading(true)
//        val baseRequest = BaseParams()
//        baseRequest.http_method = 0
//        baseRequest.request_url = "/api/branch-reviews/$idReview/reactions/summary"
//        ServiceFactory.run {
//            createRetrofitService(
//                TechResService::class.java
//            )
//                .getReviewReactionSummary(baseRequest)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object : Observer<ReactionSummaryReviewResponse> {
//                    override fun onComplete() {}
//
//                    override fun onError(e: Throwable) {
//                        e.message?.let { WriteLog.d("ERROR", it) }
//                    }
//
//                    override fun onSubscribe(d: Disposable) {}
//                    override fun onNext(response: ReactionSummaryReviewResponse) {
//                        if (response.status == AppConfig.SUCCESS_CODE) {
//                            data = response.data
//                            setCategoryReaction(data, idReview)
//                        } else Toast.makeText(
//                            mainActivity,
//                            response.message,
//                            Toast.LENGTH_LONG
//                        ).show()
//                        mainActivity?.setLoading(false)
//                    }
//                })
//
//        }
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
}
