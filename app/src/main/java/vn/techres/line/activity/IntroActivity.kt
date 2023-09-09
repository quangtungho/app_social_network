package vn.techres.line.activity

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import com.ramotion.paperonboarding.PaperOnboardingFragment
import com.ramotion.paperonboarding.PaperOnboardingPage
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.databinding.ActivityIntroBinding
import vn.techres.line.helper.PrefManager
import java.util.*

class IntroActivity : BaseBindingActivity<ActivityIntroBinding>() {
    private lateinit var prefManager: PrefManager

    override val bindingInflater: (LayoutInflater) -> ActivityIntroBinding
        get() = ActivityIntroBinding::inflate

    override fun onSetBodyView() {
        // Checking for first time launch - before calling setContentView()
        prefManager = PrefManager(this)
        if (!prefManager.isFirstTimeLaunch) {
            launchHomeScreen()
        }

        binding.txtStart.setOnClickListener {
            launchHomeScreen()
        }

        val fragmentManager = supportFragmentManager

        val onBoardingFragment = PaperOnboardingFragment.newInstance(getDataForOnBoarding())

        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_container, onBoardingFragment)
        fragmentTransaction.commit()

        onBoardingFragment.setOnRightOutListener {
            launchHomeScreen()
        }
    }

    private fun launchHomeScreen() {
        prefManager.isFirstTimeLaunch = false
        startActivity(Intent(this@IntroActivity, MainActivity::class.java))
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
        finish()
    }

    private fun getDataForOnBoarding(): ArrayList<PaperOnboardingPage> {
        // prepare data
        val scr1 = PaperOnboardingPage(
            resources.getString(R.string.intro_1), resources.getString(R.string.intro_1_key),
            Color.parseColor("#FFA233"), R.drawable.intro_1, R.drawable.intro_1_key
        )
        val scr2 = PaperOnboardingPage(
            resources.getString(R.string.intro_2), resources.getString(R.string.intro_2_key),
            Color.parseColor("#ff000000"), R.drawable.intro_2, R.drawable.intro_2_key
        )
        val scr3 = PaperOnboardingPage(
            resources.getString(R.string.intro_3), resources.getString(R.string.intro_3_key),
            Color.parseColor("#FFA233"), R.drawable.intro_3, R.drawable.intro_3_key
        )
        val scr4 = PaperOnboardingPage(
            resources.getString(R.string.intro_4), resources.getString(R.string.intro_4_key),
            Color.parseColor("#ff000000"), R.drawable.intro_4, R.drawable.intro_3_key
        )
        val scr5 = PaperOnboardingPage(
            resources.getString(R.string.intro_5), resources.getString(R.string.intro_5_key),
            Color.parseColor("#FFA233"), R.drawable.intro_5, R.drawable.intro_5_key
        )
        val scr6 = PaperOnboardingPage(
            resources.getString(R.string.intro_6), resources.getString(R.string.intro_6_key),
            Color.parseColor("#ff000000"), R.drawable.intro_6, R.drawable.intro_6_key
        )
        val elements = ArrayList<PaperOnboardingPage>()
        elements.add(scr1)
        elements.add(scr2)
        elements.add(scr3)
        elements.add(scr4)
        elements.add(scr5)
        elements.add(scr6)
        return elements
    }

}