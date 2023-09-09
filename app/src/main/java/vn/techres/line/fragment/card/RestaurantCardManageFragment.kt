package vn.techres.line.fragment.card


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.restaurant.RestaurantCardManageAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.eventbus.EventBusRegisterRestaurantCard
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.restaurant.response.RestaurantCardResponse
import vn.techres.line.data.model.utils.EventBusRestaurantCard
import vn.techres.line.databinding.FragmentRestaurantCardManageBinding
import vn.techres.line.fragment.main.MainFragment
import vn.techres.line.fragment.qr.BarcodeMemberCardFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

/**
 * A simple [Fragment] subclass.
 */
class RestaurantCardManageFragment : BaseBindingFragment<FragmentRestaurantCardManageBinding>(FragmentRestaurantCardManageBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var adapter: RestaurantCardManageAdapter? = null
    private var dataCard = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        adapter = RestaurantCardManageAdapter(childFragmentManager, requireActivity())
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        binding.pager.adapter = adapter
        binding.tabLayout.setupWithViewPager(binding.pager)
        binding.pager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout))
        binding.pager.offscreenPageLimit = 0
        getRestaurantCard()
        binding.fabScanQR.apply {
            setOnClickListener {
                mainActivity?.addOnceFragment(this@RestaurantCardManageFragment, BarcodeMemberCardFragment())
            }
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.pager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    @Subscribe()
    fun onChooseRestaurantCard(eventBusRestaurantCard: EventBusRestaurantCard) {
        if (eventBusRestaurantCard.check) {
            binding.pager.currentItem = 0
        }
    }
    @Subscribe()
    fun onChooseRegisterRestaurantCard(event: EventBusRegisterRestaurantCard) {
        mainActivity?.supportFragmentManager?.popBackStack()
        mainActivity?.supportFragmentManager?.commit {
            setCustomAnimations(
                R.anim.translate_from_right,
                R.anim.translate_to_left,
                R.anim.translate_from_left,
                R.anim.translate_to_right
            )
            add<MainFragment>(R.id.frameContainer)
            addToBackStack(null)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }
    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun getRestaurantCard() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api/membership-cards?restaurant_id=-1"
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            ).getRestaurantCard(baseRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RestaurantCardResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: RestaurantCardResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            dataCard = response.data.list.size
                            context?.let {
                                if (dataCard == 0) {
                                    binding.pager.currentItem = 1
                                }else
                                {
                                    binding.pager.currentItem = 0
                                }
                            }

                        } else {
                            Toast.makeText(mainActivity, response.message, Toast.LENGTH_LONG)
                        }
                        mainActivity?.setLoading(false)
                    }
                })
    }

    override fun onBackPress() : Boolean{
        return when (cacheManager.get(TechresEnum.ACCOUNT_FRAGMENT.toString())) {
            TechresEnum.ACCOUNT_FRAGMENT.toString() -> {
                cacheManager.put(TechresEnum.ACCOUNT_FRAGMENT.toString(), "")
                true
            }
            else -> {
                false
            }
        }
    }
}
