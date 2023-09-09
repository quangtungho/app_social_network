package vn.techres.line.adapter.game.drink

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sbrukhanda.fragmentviewpager.adapters.FragmentPagerAdapter
import vn.techres.line.fragment.game.drink.RulesDrinkFragment
import vn.techres.line.fragment.game.drink.DrinkGameFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.game.drink.DrinkTable

class FragmentStateDrinkAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {
    private var drinkTableList = ArrayList<DrinkTable>()
    private var branch = Branch()

    fun setDataSource(drinkTableList: ArrayList<DrinkTable>) {
        this.drinkTableList = drinkTableList
        notifyDataSetChanged()
    }

    fun setDataHowManyMinutes(branch: Branch) {
        this.branch = branch
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return drinkTableList.size
    }

    override fun instantiateFragment(position: Int): Fragment {
        return if (drinkTableList[position].id == 0) {
            DrinkGameFragment().newInstance(branch)!!
        } else {
            RulesDrinkFragment().newInstance(0)!!
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }

}