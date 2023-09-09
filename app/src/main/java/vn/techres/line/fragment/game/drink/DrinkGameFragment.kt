package vn.techres.line.fragment.game.drink

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sbrukhanda.fragmentviewpager.FragmentVisibilityListener
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.game.drink.DrinkGameAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.game.drink.BeverageList
import vn.techres.line.data.model.game.drink.DrinkGame
import vn.techres.line.data.model.game.drink.PlayTimes
import vn.techres.line.data.model.game.drink.request.CheckPlayTimesRequest
import vn.techres.line.data.model.game.drink.request.ChooseDrinkRequest
import vn.techres.line.data.model.game.drink.response.ChooseDrinkResponse
import vn.techres.line.data.model.game.drink.response.PlayTimesResponse
import vn.techres.line.data.model.response.DrinkGameResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentDrinkGameBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.game.DrinkGameHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*


class DrinkGameFragment : BaseBindingFragment<FragmentDrinkGameBinding>(
    FragmentDrinkGameBinding::inflate), FragmentVisibilityListener, DrinkGameHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var drinkGameAdapter: DrinkGameAdapter? = null
    private var drinkGame = DrinkGame()
    private var playTimes : PlayTimes? = null
    //socket
    private var mSocket: Socket? = null
    private val application = TechResApplication()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var branch = Branch()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(mainActivity!!.baseContext)
        user = CurrentUser.getCurrentUser(mainActivity!!.baseContext)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(mainActivity!!.baseContext)
        mSocket!!.connect()
        val layoutManager = StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL)
        layoutManager.invalidateSpanAssignments()
        binding.rcDrinkGame.layoutManager = layoutManager
        binding.rcDrinkGame.itemAnimator = DefaultItemAnimator()
        drinkGameAdapter =
            DrinkGameAdapter(
                mainActivity!!.baseContext
            )
        drinkGameAdapter!!.setDrinkGameHandler(this@DrinkGameFragment)
        binding.rcDrinkGame.adapter = drinkGameAdapter
        resources.getString(R.string.how_many_minutes).uppercase(Locale.getDefault())
        branch = Gson().fromJson(
            arguments!!.getString(TechresEnum.DRINK_GAME.toString()), object :
                TypeToken<Branch>() {}.type
        )
        if(drinkGame.beverage_lists.size != 0){
            drinkGameAdapter!!.setDataSource(drinkGame.beverage_lists)
        }else{
            getListDrinkGame(branch)
        }
    }

    fun newInstance(branch: Branch): DrinkGameFragment {
        val args = Bundle()
        args.putString(TechresEnum.DRINK_GAME.toString(), Gson().toJson(branch))
        val fragment = DrinkGameFragment()
        fragment.arguments = args
        return fragment
    }


    private fun getBackground(string : String): Int {
        return when(string){
            "red" ->{
                R.drawable.drink_red
            }
            "blue" ->{
                R.drawable.drink_blue
            }
            "brow" ->{
                R.drawable.drin_brown
            }
            "orange" ->{
                R.drawable.drink_orange
            }
            "green" ->{
                R.drawable.drink_green
            }
            "yellow_green" ->{
                R.drawable.drink_yellow_green
            }
            else ->{
                R.drawable.drink_red
            }
        }
    }
    private fun dialogDrink(playTimes: PlayTimes?, beverageLists: BeverageList){
        val dialog = Dialog(mainActivity!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_play_times)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window!!.attributes = lp
        val tvNoteDrinkGame =
            dialog.findViewById(R.id.tvNoteDrinkGame) as TextView
        val btnOKDrinkGame =
            dialog.findViewById(R.id.btnOKDrinkGame) as Button
        val btnBackDrinkGame =
            dialog.findViewById(R.id.btnBackDrinkGame) as Button
        if (playTimes != null) {
            tvNoteDrinkGame.text = String.format(
                "%s %s %s",
                mainActivity!!.baseContext.getString(R.string.remaining_plays),
                playTimes.current_number_plays,
                mainActivity!!.baseContext.getString(R.string.remaining_plays_2)
            )
            btnOKDrinkGame.visibility = View.VISIBLE
        } else {
            btnOKDrinkGame.visibility = View.GONE
            tvNoteDrinkGame.text = mainActivity!!.baseContext.getString(R.string.out_of_move)
        }
        btnOKDrinkGame.setOnClickListener {
            dialog.dismiss()
            chooseDrink(beverageLists)
        }
        btnBackDrinkGame.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun getListDrinkGame(branch: Branch) {
        restaurant().restaurant_id?.let { it ->
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .getDrinkGame(it, branch.id!!, "public")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<DrinkGameResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: DrinkGameResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            drinkGame = response.data
                            if (drinkGame.beverage_lists.isNotEmpty()) {
//                                val arrayList = getBackground()
                                drinkGame.beverage_lists.map {
                                    it.drink_background.drawable = getBackground(it.drink_background.key)
                                }
                                drinkGame.beverage_lists.shuffle()
                                drinkGameAdapter!!.setDataSource(drinkGame.beverage_lists)
                            }
                        }
                    }
                })
        }
    }

    private fun getPlayTimes(branch: Branch, beverageLists: BeverageList) {
        val checkPlayTimesRequest = CheckPlayTimesRequest()
        val restaurant = restaurant()
        checkPlayTimesRequest.restaurant_id = restaurant.restaurant_id!!
        checkPlayTimesRequest.restaurant_name = restaurant.restaurant_name!!
        checkPlayTimesRequest.branch_id = branch.id!!
        checkPlayTimesRequest.branch_name = branch.name!!
        checkPlayTimesRequest.let { it ->
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .getPlayTimes(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<PlayTimesResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: PlayTimesResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            playTimes = response.data
                            dialogDrink(playTimes, beverageLists)
                        }else if(response.status == AppConfig.UNAUTHORIZED){
                            playTimes = response.data
                            dialogDrink(playTimes, beverageLists)
                        }
                    }
                })
        }
    }

    private fun chooseDrink(beverageLists: BeverageList) {
        val chooseDrinkRequest = ChooseDrinkRequest()
        chooseDrinkRequest.beverage_id = beverageLists.beverage_id
        chooseDrinkRequest.drink_code = beverageLists.code
        chooseDrinkRequest.let { it ->
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .chooseDrink(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ChooseDrinkResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        e.message?.let { WriteLog.d("ERROR", it) }
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: ChooseDrinkResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                        }
                        mainActivity?.setLoading(false)
                    }
                })
        }
    }


    override fun onFragmentVisible() {
    }

    override fun onFragmentInvisible() {
    }

    override fun onChoose(position: Int) {

        val bundle = Bundle()

        bundle.putString(
            TechresEnum.DRINK_DETAIL_GAME.toString(),
            Gson().toJson(drinkGame.beverage_lists[position])
        )

        getPlayTimes(branch, drinkGame.beverage_lists[position])

    }

    override fun onBackPress() : Boolean{
        return true
    }

}