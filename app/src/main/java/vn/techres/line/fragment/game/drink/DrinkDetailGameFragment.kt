package vn.techres.line.fragment.game.drink

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shashank.sony.fancytoastlib.FancyToast
import io.socket.client.Socket
import net.glxn.qrgen.android.QRCode
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.TechResAppGame
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.game.drink.BeverageList
import vn.techres.line.data.model.game.drink.ResultDrinkGame
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentDrinkDetailGameBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import java.util.*

class DrinkDetailGameFragment : BaseBindingFragment<FragmentDrinkDetailGameBinding>(FragmentDrinkDetailGameBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var drinkGame = BeverageList()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var techResAppGame = TechResAppGame()
    private var mSocket: Socket? = null
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(mainActivity!!.baseContext)
        user = CurrentUser.getCurrentUser(mainActivity!!.baseContext)
        mSocket = techResAppGame.getSocketInstance(restaurant().restaurant_id.toString())
        mSocket?.connect()
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
        binding.header.tvTitleHomeHeader.text =
            resources.getString(R.string.how_many_minutes).uppercase(Locale.getDefault())

        arguments?.let {
            drinkGame =
                Gson().fromJson(it.getString(TechresEnum.DRINK_DETAIL_GAME.toString()), object :
                    TypeToken<BeverageList>() {}.type)
            setData(drinkGame)
        }
    }
    private fun setData(beverageList: BeverageList) {
        getGlide(binding.imgAvatarDrinkDetail, beverageList.avatar)
        getGlide(binding.imgBackgroundDrinkDetail, beverageList.avatar)

        Glide.with(this)
            .load(String.format("%s%s", nodeJs.api_ads, beverageList.drink_background.link))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transform(CenterCrop(), GranularRoundedCorners(70F, 70F, 0F, 0F))
            .into(binding.imgBackgroundContainerDrinkDetail)

        Glide.with(this)
            .load(String.format("%s%s",nodeJs.api_ads, beverageList.article_avatar.cans))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transform(CenterCrop(), RoundedCorners(10))
            .into(binding.imgDescriptionOne)

        Glide.with(this)
            .load(String.format("%s%s",nodeJs.api_ads, beverageList.article_avatar.log))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transform(CenterCrop(), RoundedCorners(10))
            .into(binding.imgDescriptionTwo)

        Glide.with(this)
            .load(String.format("%s%s",nodeJs.api_ads, beverageList.article_avatar.container))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transform(CenterCrop(), RoundedCorners(10))
            .into(binding.imgDescriptionThree)

        binding.tvNameDrinkDetail.text = beverageList.name
        binding.tvLinkDescriptionDrinkDetail.text =
            String.format("%s : %s", mainActivity!!.baseContext.getString(R.string.total_vote), beverageList.total_vote)
        val bitmap =
            QRCode.from(String.format("%s:%s", user.id, beverageList.code)).withSize(200, 200)
                .bitmap()
        binding.tvDescriptionDrinkDetailOne.text = String.format("%s%s", beverageList.article_content.capacity, mainActivity!!.baseContext.getString(
            R.string.mililitre
        ))
        binding.tvDescriptionDrinkDetailTwo.text = String.format("%s%s", beverageList.article_content.weight, mainActivity!!.baseContext.getString(
            R.string.gam
        ))
        binding.tvDescriptionDrinkDetailThree.text = String.format("%s%s", beverageList.article_content.container, mainActivity!!.baseContext.getString(
            R.string.can
        ))
        binding.imgQRCodeDrink.setImageBitmap(bitmap)

        val dialogQR = Dialog(this.mainActivity!!)
        dialogQR.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogQR.setCancelable(true)
        dialogQR.setContentView(R.layout.dialog_show_image)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialogQR.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialogQR.window!!.attributes = lp
        val imgShow = dialogQR.findViewById(R.id.imgShow) as ImageView
        imgShow.setImageBitmap(bitmap)
        binding.imgQRCodeDrink.setOnClickListener {
            dialogQR.show()
        }
        onSocket(dialogQR)
    }
    private fun getGlide(imageView: ImageView, string: String?) {
        Glide.with(this)
            .load(
                String.format("%s%s", nodeJs.api_ads, string)
            )
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerInside()
            .into(imageView)
    }
    private fun onSocket(dialog: Dialog){
        mSocket?.on(String.format("%s/%s", "res-submit", user.id)) { args ->
            mainActivity?.runOnUiThread {
                WriteLog.d("res-submit", args[0].toString())
                val resultDrinkGame =
                    Gson().fromJson<ResultDrinkGame>(args[0].toString(), object :
                        TypeToken<ResultDrinkGame>() {}.type)
                dialog.dismiss()
                if(resultDrinkGame.status == 1){
                    FancyToast.makeText(
                        context,
                        mainActivity!!.baseContext.getString(R.string.success_drink_game),
                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                        false
                    ).show()
                    onBackPress()
                }else{
                    FancyToast.makeText(
                        context,
                        mainActivity!!.baseContext.getString(R.string.error_drink_game),
                        FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                        false
                    ).show()
                }

            }
        }
    }

    override fun onBackPress() : Boolean{
        return true
    }



}