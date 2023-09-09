package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.github.appintro.model.SliderPage
import vn.techres.line.R
import vn.techres.line.helper.PrefManager

class DefaultIntro : AppIntro2() {
    private lateinit var prefManager: PrefManager

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefManager = PrefManager(this)

        addSlide(
            AppIntroFragment.newInstance(
                resources.getString(R.string.intro_1),
                resources.getString(R.string.intro_1_key),
                imageDrawable = R.drawable.intro_1,
                backgroundDrawable = R.drawable.back_slide1,
                titleTypefaceFontRes = R.font.lato,
                descriptionTypefaceFontRes = R.font.lato
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    resources.getString(R.string.intro_2),
                    resources.getString(R.string.intro_2_key),
                    imageDrawable = R.drawable.intro_2,
                    backgroundDrawable = R.drawable.back_slide2,
                    titleTypefaceFontRes = R.font.lato,
                    descriptionTypefaceFontRes = R.font.lato
                )
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    resources.getString(R.string.intro_3),
                    resources.getString(R.string.intro_3_key),
                    imageDrawable = R.drawable.intro_3,
                    backgroundDrawable = R.drawable.back_slide3,
                    titleTypefaceFontRes = R.font.lato,
                    descriptionTypefaceFontRes = R.font.lato
                )
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    resources.getString(R.string.intro_4),
                    resources.getString(R.string.intro_4_key),
                    imageDrawable = R.drawable.intro_4,
                    backgroundDrawable = R.drawable.back_slide4,
                    titleTypefaceFontRes = R.font.lato,
                    descriptionTypefaceFontRes = R.font.lato
                )
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    resources.getString(R.string.intro_5),
                    resources.getString(R.string.intro_5_key),
                    imageDrawable = R.drawable.intro_5,
                    backgroundDrawable = R.drawable.back_slide5,
                    titleTypefaceFontRes = R.font.lato,
                    descriptionTypefaceFontRes = R.font.lato
                )
            )
        )

        addSlide(
            AppIntroFragment.newInstance(
                SliderPage(
                    resources.getString(R.string.intro_6),
                    resources.getString(R.string.intro_6_key),
                    imageDrawable = R.drawable.intro_6,
                    backgroundDrawable = R.drawable.back_slide6,
                    titleTypefaceFontRes = R.font.lato,
                    descriptionTypefaceFontRes = R.font.lato
                )
            )
        )


        setTransformer(AppIntroPageTransformerType.Parallax())
    }

    public override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        prefManager.isFirstTimeLaunch = false

        startActivity(Intent(this, LoginActivity::class.java)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })

        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    public override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        prefManager.isFirstTimeLaunch = false
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }
}