package vn.techres.line.activity.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.game.QRCodeGame
import vn.techres.line.data.model.params.QRCodeGameParams
import vn.techres.line.data.model.response.game.JoinGameResponse
import vn.techres.line.databinding.ActivityLuckyWheelBinding
import vn.techres.line.fragment.game.QRCodeGameFragment
import vn.techres.line.fragment.game.luckywheel.LuckyWheelGameFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.game.BackLuckyWheel
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.FileNotFoundException
import java.io.IOException

class LuckyWheelActivity : BaseBindingActivity<ActivityLuckyWheelBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityLuckyWheelBinding
        get() = ActivityLuckyWheelBinding::inflate
    private var backLuckyWheel: BackLuckyWheel? = null

    override fun onSetBodyView() {
        intent?.let {
            val bundle = Bundle()
            bundle.putInt(
                TechresEnum.BRANCH_ID.toString(),
                it.getIntExtra(TechresEnum.BRANCH_ID.toString(), 0)
            )
            bundle.putInt(
                TechresEnum.RESTAURANT_ID.toString(),
                it.getIntExtra(TechresEnum.RESTAURANT_ID.toString(), 0)
            )
            bundle.putString(
                TechresEnum.ROOM_ID.toString(),
                it.getStringExtra(TechresEnum.ROOM_ID.toString())
            )
            bundle.putString(
                TechresEnum.ID_ARTICLE_GAME.toString(),
                it.getStringExtra(TechresEnum.ID_ARTICLE_GAME.toString())
            )
            if (it.getBooleanExtra(TechresEnum.IS_GAME.toString(), false)) {
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    add<LuckyWheelGameFragment>(R.id.frameContainer, args = bundle)
                    addToBackStack(null)
                }
            } else {
                it.getStringExtra(TechresEnum.ROOM_ID.toString())?.let { it1 -> read(it1, bundle) }
            }

        }
    }

    override fun onBackPressed() {
        if (onBackClickFragments[onBackClickFragments.size - 1].onBack()) {
            onBackClickFragments[onBackClickFragments.size - 1].onBack()
            finish()
            overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
        } else {
            onBackClickFragments[onBackClickFragments.size - 1].onBack()
        }
    }

    fun setBackLuckyWheel(backLuckyWheel: BackLuckyWheel?) {
        this.backLuckyWheel = backLuckyWheel
    }

    fun addOnceFragment(fromFragment: Fragment, toFragment: Fragment) {
        val isInBackStack =
            supportFragmentManager.findFragmentByTag(toFragment.javaClass.simpleName)
        if (isInBackStack == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.translate_from_right,
                    R.anim.translate_to_left,
                    R.anim.translate_from_left,
                    R.anim.translate_to_right
                )
                .add(R.id.frameContainer, toFragment, toFragment.javaClass.simpleName)
                .hide(fromFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
        }
    }

    fun replaceOnceFragment(fromFragment: Fragment, toFragment: Fragment) {
        val isInBackStack =
            supportFragmentManager.findFragmentByTag(toFragment.javaClass.simpleName)
        if (isInBackStack == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.translate_from_right,
                    R.anim.translate_to_left,
                    R.anim.translate_from_left,
                    R.anim.translate_to_right
                )
                .replace(R.id.frameContainer, toFragment, toFragment.javaClass.simpleName)
                .hide(fromFragment)
                .addToBackStack(toFragment.javaClass.simpleName)
                .commit()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun read(roomId: String, bundle: Bundle) {
        try {
            val listDataToFile = ArrayList<QRCodeGame>()
            TechResApplication.instance?.getQrCodeDao()?.let { listDataToFile.addAll(it.getListRoom()) }
            var isCheck = 0
            listDataToFile.forEachIndexed { _, qrCodeGame ->
                if (qrCodeGame.room_id.equals(roomId)) {
                    isCheck = 1
                    qrCodeGame.room_id?.let {
                        qrCodeGame.restaurant_id?.let { it1 ->
                            qrCodeGame.branch_id?.let { it2 ->
                                checkRoom(
                                    it1, it2,
                                    it, bundle
                                )
                            }
                        }
                    }
                }
            }
            if (isCheck == 0) {
                supportFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.translate_from_right,
                        R.anim.translate_to_left,
                        R.anim.translate_from_left,
                        R.anim.translate_to_right
                    )
                    add<QRCodeGameFragment>(R.id.frameContainer, args = bundle)
                    addToBackStack(null)
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun checkRoom(restaurantId: Int, branchId: Int, roomId: String, bundle: Bundle) {
        val params = QRCodeGameParams()
        params.http_method = AppConfig.POST
        params.project_id = AppConfig.PROJECT_LUCKY_WHEEL
        params.request_url = "/api/qr-code/check-qr-code-befor-join-room"
        params.params.branch_id = branchId
        params.params.restaurant_id = restaurantId
        params.params.room_id = roomId

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .checkRoom(params)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<JoinGameResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    FancyToast.makeText(
                        this@LuckyWheelActivity,
                        getString(R.string.join_room_game_false),
                        FancyToast.LENGTH_LONG,
                        FancyToast.ERROR,
                        false
                    ).show()
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: JoinGameResponse) {

                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val qrCodeGame = response.data
//                        listDataToFile.add(qrCodeGame!!)
                        if (qrCodeGame?.status == 1) {
//                            FancyToast.makeText(
//                                this@LuckyWheelActivity,
//                                getString(R.string.join_room_game_success),
//                                FancyToast.LENGTH_LONG,
//                                FancyToast.SUCCESS,
//                                false
//                            ).show()
                            qrCodeGame.row?.let {
                                bundle.putInt(
                                    TechresEnum.ROW_GAME.toString(),
                                    it
                                )
                            }
                            supportFragmentManager.commit {
                                setCustomAnimations(
                                    R.anim.translate_from_right,
                                    R.anim.translate_to_left,
                                    R.anim.translate_from_left,
                                    R.anim.translate_to_right
                                )
                                add<LuckyWheelGameFragment>(R.id.frameContainer, args = bundle)
                                addToBackStack(null)
                            }
                        } else {
                            FancyToast.makeText(
                                this@LuckyWheelActivity,
                                getString(R.string.join_room_game_false),
                                FancyToast.LENGTH_LONG,
                                FancyToast.WARNING,
                                false
                            ).show()
                        }

                    } else Toast.makeText(
                        this@LuckyWheelActivity,
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()

                }
            })
    }
}