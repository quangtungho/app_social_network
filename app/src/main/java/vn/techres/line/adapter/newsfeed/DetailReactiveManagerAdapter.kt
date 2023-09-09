package vn.techres.line.adapter.newsfeed

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sbrukhanda.fragmentviewpager.adapters.FragmentPagerAdapter
import vn.techres.line.fragment.newsfeed.DetailTotalReactionFragment
import vn.techres.line.data.model.ReactionSummaryReview

class DetailReactiveManagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    private var dataSource = ArrayList<ReactionSummaryReview>()
    private var idReview = ""


    fun setDataSource(dataSource: ArrayList<ReactionSummaryReview>, idReview: String) {
        this.dataSource = dataSource
        this.idReview = idReview
        notifyDataSetChanged()
    }

    override fun instantiateFragment(position: Int): Fragment {
        return DetailTotalReactionFragment().newInstance(
            idReview, dataSource[position].reaction_id!!
        )!!
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }
}