package vn.techres.line.activity.game

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import vn.techres.line.R
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.LuckyItem
import vn.techres.line.databinding.ActivityLuckyWheelOfflineBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import java.util.*

class LuckyWheelOfflineActivity : BaseBindingActivity<ActivityLuckyWheelOfflineBinding>() {
    var data =  ArrayList<LuckyItem>()
    private var mPlayer: MediaPlayer? = null

    override val bindingInflater: (LayoutInflater) -> ActivityLuckyWheelOfflineBinding
        get() = ActivityLuckyWheelOfflineBinding::inflate

    override fun onSetBodyView() {
        setMusicBackground()
        val luckyItem1 = LuckyItem()
        luckyItem1.topText = "100"
        luckyItem1.icon = R.drawable.logo_aloline_placeholder
        luckyItem1.color = -0xc20
        data.add(luckyItem1)

        val luckyItem2 = LuckyItem()
        luckyItem2.topText = "200"
        luckyItem2.icon = R.drawable.logo_aloline_placeholder
        luckyItem2.color = -0x1f4e
        data.add(luckyItem2)

        val luckyItem3 = LuckyItem()
        luckyItem3.topText = "300"
        luckyItem3.icon = R.drawable.logo_aloline_placeholder
        luckyItem3.color = -0x3380
        data.add(luckyItem3)

        //////////////////

        //////////////////
        val luckyItem4 = LuckyItem()
        luckyItem4.topText = "400"
        luckyItem4.icon = R.drawable.logo_aloline_placeholder
        luckyItem4.color = -0xc20
        data.add(luckyItem4)

        val luckyItem5 = LuckyItem()
        luckyItem5.topText = "500"
        luckyItem5.icon = R.drawable.logo_aloline_placeholder
        luckyItem5.color = -0x1f4e
        data.add(luckyItem5)

        val luckyItem6 = LuckyItem()
        luckyItem6.topText = "600"
        luckyItem6.icon = R.drawable.logo_aloline_placeholder
        luckyItem6.color = -0x3380
        data.add(luckyItem6)
        //////////////////

        //////////////////////
        //////////////////

        //////////////////////
        val luckyItem7 = LuckyItem()
        luckyItem7.topText = "700"
        luckyItem7.icon = R.drawable.logo_aloline_placeholder
        luckyItem7.color = -0xc20
        data.add(luckyItem7)

        val luckyItem8 = LuckyItem()
        luckyItem8.topText = "800"
        luckyItem8.icon = R.drawable.logo_aloline_placeholder
        luckyItem8.color = -0x1f4e
        data.add(luckyItem8)


        val luckyItem9 = LuckyItem()
        luckyItem9.topText = "900"
        luckyItem9.icon = R.drawable.logo_aloline_placeholder
        luckyItem9.color = -0x3380
        data.add(luckyItem9)

        binding.luckyWheel.setData(data)
        binding.luckyWheel.setRound(5)

        /*luckyWheelView.setLuckyWheelBackgrouldColor(0xff0000ff);
        luckyWheelView.setLuckyWheelTextColor(0xffcc0000);
        luckyWheelView.setLuckyWheelCenterImage(getResources().getDrawable(R.drawable.icon));
        luckyWheelView.setLuckyWheelCursorImage(R.drawable.ic_cursor);*/


        /*luckyWheelView.setLuckyWheelBackgrouldColor(0xff0000ff);
        luckyWheelView.setLuckyWheelTextColor(0xffcc0000);
        luckyWheelView.setLuckyWheelCenterImage(getResources().getDrawable(R.drawable.icon));
        luckyWheelView.setLuckyWheelCursorImage(R.drawable.ic_cursor);*/

        Glide.with(binding.imgBg).load(String.format("%s%s", CurrentConfigNodeJs.getConfigNodeJs(this).api_ads, "/public/icons/wheel-frame.gif"))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.imgBg)

        binding.play.setOnClickListener {
            val index = getRandomIndex()
            binding.luckyWheel.startLuckyWheelWithTargetIndex(index)
        }

//        luckyWheelView.setLuckyRoundItemSelectedListener(object : LuckyRoundItemSelectedListener() {
//            fun LuckyRoundItemSelected(index: Int) {
//                Toast.makeText(applicationContext, data[index].topText, Toast.LENGTH_SHORT).show()
//            }
//        })

        binding.luckyWheel.setLuckyRoundItemSelectedListener { index ->
            Toast.makeText(
                applicationContext,
                data[index].topText,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getRandomIndex(): Int {
        val rand = Random()
        return rand.nextInt(data.size - 1) + 0
    }

//    private fun getRandomRound(): Int {
//        val rand = Random()
//        return rand.nextInt(10) + 15
//    }

    private fun setMusicBackground() {
        mPlayer = MediaPlayer.create(this, R.raw.soundlwo)
        mPlayer?.isLooping = true
        mPlayer?.start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mPlayer?.stop()
    }
}