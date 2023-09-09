package vn.techres.line.fragment.game.drink

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sbrukhanda.fragmentviewpager.FragmentViewPager
import com.sbrukhanda.fragmentviewpager.FragmentVisibilityListener
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.game.drink.FragmentStateDrinkAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.game.drink.DrinkTable
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentDrinksGameTableBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.techresenum.TechresEnum


/**
 * A simple [Fragment] subclass.
 */
class DrinksGameTableFragment : BaseBindingFragment<FragmentDrinksGameTableBinding>(
    FragmentDrinksGameTableBinding::inflate), FragmentVisibilityListener {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var pagerAdapter: FragmentStateDrinkAdapter? = null
    private var fragmentPagerDrink: FragmentViewPager? = null
    private var user = User()
    private var nodeJs = ConfigNodeJs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(mainActivity!!.baseContext)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(mainActivity!!.baseContext)
        setData()
        pagerAdapter = FragmentStateDrinkAdapter(childFragmentManager)
        fragmentPagerDrink!!.offscreenPageLimit = 1
        fragmentPagerDrink!!.addOnPageChangeListener(
            TabLayout.TabLayoutOnPageChangeListener(
                binding.tabLayoutDrink
            )
        )
        fragmentPagerDrink!!.adapter = pagerAdapter
        binding.tabLayoutDrink.setupWithViewPager(fragmentPagerDrink)
        setTabLayout()
        binding.imgSearchDrink.setOnClickListener {
            if (binding.svDrink.visibility == View.VISIBLE) {
                binding.tvNameUserDrink.visibility = View.VISIBLE
                binding.svDrink.visibility = View.GONE
            } else {
                binding.tvNameUserDrink.visibility = View.GONE
                binding.svDrink.visibility = View.VISIBLE
            }
        }

    }

    override fun onFragmentVisible() {
        fragmentPagerDrink!!.notifyPagerVisible()
    }

    override fun onFragmentInvisible() {
        fragmentPagerDrink!!.notifyPagerInvisible()
    }

    private fun setData() {
        Glide.with(this)
            .load(
                String.format("%s%s", nodeJs.api_ads, user.avatar_three_image.medium)
            )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .apply(
                RequestOptions().placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
            )
            .into(binding.imgAvatarUserDrink)
        binding.tvNameUserDrink.text = user.name
    }

    private fun setTabLayout() {
        val bundle = arguments
        val drinkTableList = ArrayList<DrinkTable>()
        if (bundle != null) {
            val howManyMinutes = Gson().fromJson<Branch>(
                bundle.getString(TechresEnum.HOW_MANY_MINUTES.toString()),
                object :
                    TypeToken<Branch>() {}.type
            )
            pagerAdapter!!.setDataHowManyMinutes(howManyMinutes)
        }
        for (i in 0..1) {
            val drinkTable = DrinkTable()
            drinkTable.id = i
            drinkTable.nameTab = if (i == 0) {
                mainActivity!!.baseContext.getString(R.string.tab_drink)
            } else {
                mainActivity!!.baseContext.getString(R.string.tab_rules)
            }
            drinkTableList.add(drinkTable)
        }

        pagerAdapter!!.setDataSource(drinkTableList)
        for (i in drinkTableList.indices) {
            val tab = binding.tabLayoutDrink.getTabAt(i)!!
            tab.setCustomView(R.layout.custom_tab_drink_game)
            val tvTabDrink = tab.customView!!.findViewById<TextView>(R.id.tvTabDrink)
            tvTabDrink.text = drinkTableList[i].nameTab
        }
    }

    override fun onBackPress() : Boolean{
        return true
    }

}
