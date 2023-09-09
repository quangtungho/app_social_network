package vn.techres.line.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.giphy.sdk.analytics.GiphyPingbacks.context
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.restaurant.PrerogativesCardAdapter
import vn.techres.line.adapter.restaurant.RestaurantCardDetailAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.restaurant.RestaurantCardLevel
import vn.techres.line.data.model.restaurant.response.RestaurantCardLevelResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityRestaurantCardDetailBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.Utils.ifShow
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.invisible
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class RestaurantCardDetailActivity : BaseBindingActivity<ActivityRestaurantCardDetailBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityRestaurantCardDetailBinding
        get() = ActivityRestaurantCardDetailBinding::inflate
    private var adapter: RestaurantCardDetailAdapter? = null
    private var adapterPrerogatives: PrerogativesCardAdapter? = null
    private var configJava = ConfigJava()
    private var nodeJs = ConfigNodeJs()
    private var user = User()

    private var data = ArrayList<RestaurantCardLevel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(baseContext)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(baseContext)
        configJava = CurrentConfigJava.getConfigJava(baseContext)
    }

    override fun onSetBodyView() {
        this.window?.statusBarColor = AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text =
            resources.getString(R.string.title_restaurant_card_detail)
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        getRestaurantCardLevel()
        setListener()
        adapter = RestaurantCardDetailAdapter(this)
        adapterPrerogatives = PrerogativesCardAdapter(this)
        binding.rcPrerogativesCard.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcPrerogativesCard.adapter = adapterPrerogatives
    }

    private fun setPager() {
        binding.viewPagerCard.adapter = adapter
        binding.viewPagerCard.clipToPadding = false
        binding.viewPagerCard.clipChildren = false
        binding.viewPagerCard.offscreenPageLimit = 3
        binding.viewPagerCard.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_ALWAYS

        binding.viewPagerCard.setPageTransformer { page, position ->
            MarginPageTransformer(1)
            val r = 1 - kotlin.math.abs(position)
            page.scaleY = 0.95f + r * 0.09f
            when {
                position < -1 -> page.alpha = 0.1f
                position <= 1 -> page.alpha = 0.2f.coerceAtLeast(1 - kotlin.math.abs(position))
                else -> page.alpha = 0.1f
            }
        }
        binding.viewPagerCard.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val card = data[position]
                if ((card.point_to_level_up ?: "0").toBigDecimal() != 0.toBigDecimal()) {
                    binding.progressLevelCard.show()
                    binding.tvPointCardLevel.text = String.format(
                        "%s %s",
                        Currency.formatCurrencyDecimal((card.total_point ?: 0).toFloat()),
                        resources.getString(R.string.denominations)
                    )
                    binding.tvExperienceCard.text = String.format(
                        "%s/%s %s",
                        Currency.formatCurrencyDecimal((card.total_point ?: 0).toFloat()),
                        Currency.formatCurrencyDecimal((card.point_to_level_up ?: "0").toFloat()),
                        baseContext.getString(R.string.denominations)
                    )
                    val progressLever =
                        (((card.total_point
                            ?: 0).toBigDecimal() * 100.toBigDecimal()) / (card.point_to_level_up
                            ?: "0").toBigDecimal())
                    if (progressLever > 100.toBigDecimal())
                        binding.progressLevelCard.progress = 100
                    else
                        binding.progressLevelCard.progress = progressLever.toInt()
                } else {
                    binding.tvExperienceCard.text = ""
                    binding.progressLevelCard.invisible()
                }
                binding.cvContent.ifShow(card.prerogatives.size > 0)
                adapterPrerogatives?.setDataSource(card.prerogatives)
            }
        })
        binding.viewPagerCard.setCurrentItem(data.indexOfFirst { it.is_my_level == 1 }, true)
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvHistoryPointCard.setOnClickListener {
            val intent = Intent(this, PointCardActivity::class.java)
            startActivity(intent)
            overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }
        binding.tvFormAccumulatePoints.setOnClickListener {

        }

        binding.tvQuestionCard.setOnClickListener {

        }
    }

    private fun getRestaurantCardLevel() {

        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format(
            "%s%s",
            "/api/restaurant-membership-cards?restaurant_id=",
            restaurant().restaurant_id
        )
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getRestaurantCardLevel(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<RestaurantCardLevelResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    e.message?.let { WriteLog.d("ERROR", it) }
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: RestaurantCardLevelResponse) {
                    data = response.data
                    adapter?.setDataSource(data)
                    setPager()
                }
            })

    }
}