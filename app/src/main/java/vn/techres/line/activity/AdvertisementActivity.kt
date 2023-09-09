package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import cn.jzvd.JZVideoPlayerStandard
import com.androidadvance.topsnackbar.TSnackbar
import com.giphy.sdk.analytics.GiphyPingbacks.context
import vn.techres.line.R
import vn.techres.line.adapter.advert.VideoDemoAdvertAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.avert.VideoDemoAdvert
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityAdvertisementBinding
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.utils.AlolineColorUtil

class AdvertisementActivity : BaseBindingActivity<ActivityAdvertisementBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityAdvertisementBinding
        get() = ActivityAdvertisementBinding::inflate
    private var adapter: VideoDemoAdvertAdapter? = null
    private var videoDemoList = ArrayList<VideoDemoAdvert>()
    private var user = User()
    private var configJava = ConfigJava()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(this@AdvertisementActivity.baseContext)
        user = CurrentUser.getCurrentUser(this@AdvertisementActivity.baseContext)
    }

    override fun onSetBodyView() {
        this.window?.statusBarColor = AlolineColorUtil(context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = resources.getString(R.string.registry_advertisement)
        adapter = VideoDemoAdvertAdapter(this@AdvertisementActivity)
        setData()
        setListener()
    }
    override fun onPause() {
        super.onPause()
        JZVideoPlayerStandard.releaseAllVideos()
    }

    override fun onStop() {
        super.onStop()
        JZVideoPlayerStandard.releaseAllVideos()
    }
    private fun setData() {
        val videoDemoAdvert = VideoDemoAdvert()
        videoDemoAdvert.url = "/public/defaults/video_ads_1.mp4"
        val videoDemoAdvert2 = VideoDemoAdvert()
        videoDemoAdvert2.url = "/public/defaults/video_ads_2.mp4"
        val videoDemoAdvert3 = VideoDemoAdvert()
        videoDemoAdvert3.url = "/public/defaults/video_ads_3.mp4"
        videoDemoList.add(videoDemoAdvert)
        videoDemoList.add(videoDemoAdvert2)
        videoDemoList.add(videoDemoAdvert3)
        adapter?.setDataSource(videoDemoList)
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
        binding.viewPagerCard.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                JZVideoPlayerStandard.releaseAllVideos()
                videoDemoList.forEach {
                    it.auto = 0
                }
                videoDemoList[position].auto = 1
                adapter?.notifyDataSetChanged()
            }
        })
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
          onBackPressed()
        }

        binding.lnPolicy.setOnClickListener {
            dialogArgee()
        }

        binding.btnActivate.setOnClickListener {
            if (!binding.cbPolicy.isChecked) {
                val snackBar: TSnackbar = TSnackbar.make(
                    binding.btnActivate,
                    baseContext.getString(R.string.please_access_policy),
                    TSnackbar.LENGTH_LONG
                )
                snackBar.setActionTextColor(Color.WHITE)
                val snackbarView: View = snackBar.view
                snackbarView.setBackgroundColor(
                    ContextCompat.getColor(
                        baseContext,
                        R.color.black
                    )

                )
                val textView =
                    snackbarView.findViewById<View>(com.androidadvance.topsnackbar.R.id.snackbar_text) as TextView
                textView.setTextColor(Color.WHITE)
                snackBar.show()
            } else {
                val intent = Intent(this, FormRegistryAdvertActivity::class.java)
                startActivity(intent)
                this@AdvertisementActivity.overridePendingTransition(
                    R.anim.translate_from_right,
                    R.anim.translate_to_right
                )
            }
        }
    }

    private fun dialogArgee() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.fragment_advertising_policy)

        val tvContextPolicy = dialog.findViewById(R.id.tvContextPolicy) as TextView
        val btnAgree = dialog.findViewById(R.id.btnAgree) as Button
        tvContextPolicy.text = this@AdvertisementActivity.baseContext.getString(R.string.context_policy)

        btnAgree.setOnClickListener {
            binding.cbPolicy.isChecked = true
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

}
