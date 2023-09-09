package vn.techres.line.fragment.game.luckywheel

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.core.JsonProcessingException
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.activity.TechResApplication
import vn.techres.line.activity.game.LuckyWheelActivity
import vn.techres.line.adapter.game.luckywheel.ListRoomGameAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.game.QRCodeGame
import vn.techres.line.data.model.game.RoomGame
import vn.techres.line.data.model.params.ListRoomGameParams
import vn.techres.line.data.model.response.game.ListroomGameResponse
import vn.techres.line.databinding.FragmentListRoomGameBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.PrefUtils
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.WriteLog.d
import vn.techres.line.helper.techresenum.TechResEnumGame
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.ClickGame
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.IOException

class ListRoomGameFragment :
    BaseBindingFragment<FragmentListRoomGameBinding>(FragmentListRoomGameBinding::inflate),
    ClickGame {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var typeGame = 0
    private var listRoomGameAdapter: ListRoomGameAdapter? = null
    private var mSocket: Socket? = null
    private var gameList: ArrayList<RoomGame> = ArrayList()
    private var listDataToFile = ArrayList<QRCodeGame>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            typeGame = it.getInt(TechresEnum.TYPE_GAME.toString(), 1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        read()

        listRoomGameAdapter = ListRoomGameAdapter(
            requireActivity()
        )
        binding.mRecyclerView.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }

        binding.mRecyclerView.adapter = listRoomGameAdapter

        listRoomGameAdapter?.setClickGame(this)

        restaurant().restaurant_id?.let { getListRoomGame(it) }
        setListener()
        onRegistrySocket()
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.swipeRefresh.setOnRefreshListener {
            restaurant().restaurant_id?.let { getListRoomGame(it) }

        }
        binding.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    listRoomGameAdapter?.searchFullText(newText)
                }
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        restaurant().restaurant_id?.let { getListRoomGame(it) }
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    private fun getListRoomGame(restaurantId: Int) {
        val params = ListRoomGameParams()
        params.http_method = AppConfig.GET
        params.project_id = AppConfig.PROJECT_LUCKY_WHEEL
        params.request_url = "/api/room-lucky-wheel/get-list-room"
        params.params.id_article_game =
            arguments?.getString(TechresEnum.GAME_ID.toString()).toString()
        params.params.restaurant_id = restaurantId
        params.params.current_type = ""
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListRoomGame(
                params
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ListroomGameResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    mainActivity?.setLoading(false)
                    d("ERROR", e.message.toString())
                    binding.swipeRefresh.isRefreshing = false
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ListroomGameResponse) {

                    if (response.status == AppConfig.SUCCESS_CODE) {
                        gameList = response.data
                        listRoomGameAdapter?.setDataSource(gameList, listDataToFile)
                    }
                    binding.swipeRefresh.isRefreshing = false
                    mainActivity?.setLoading(false)
                }
            })
    }

    override fun onClickGame(roomGame: RoomGame) {
//        val intent = if(typeGame == 1){
//            Intent(mainActivity, LuckyWheelActivity::class.java)
//        }else{
//            Intent(mainActivity, LuckyWheelTetActivity::class.java)
//        }
        val intent = Intent(mainActivity, LuckyWheelActivity::class.java)
        if (roomGame.is_join == 1) {
            intent.putExtra(
                TechresEnum.BRANCH_ID.toString(),
                roomGame.branch_id
            )

            intent.putExtra(
                TechresEnum.RESTAURANT_ID.toString(),
                roomGame.restaurant_id
            )

            intent.putExtra(
                TechresEnum.ROOM_ID.toString(),
                roomGame.room_id
            )

            intent.putExtra(
                TechresEnum.ID_ARTICLE_GAME.toString(),
                roomGame.id_article_game
            )
        } else {
            try {
                intent.putExtra(
                    TechresEnum.ROOM_ID.toString(),
                    roomGame.room_id
                )

                intent.putExtra(
                    TechresEnum.ID_ARTICLE_GAME.toString(),
                    roomGame.id_article_game
                )
                intent.putExtra(
                    TechresEnum.BRANCH_ID.toString(),
                    roomGame.branch_id
                )
                PrefUtils.getInstance()
                    .putString(TechresEnum.ID_ARTICLE_GAME.name, roomGame.id_article_game)

            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
        }

//        val qrCodeGameFragment = QRCodeGameFragment()
//        qrCodeGameFragment.arguments = bundle
//        mainActivity?.addOnceFragment(this, qrCodeGameFragment)

        mainActivity?.startActivity(intent)
        mainActivity?.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    override fun onBackPress(): Boolean {
        return true
    }

    fun onRegistrySocket() {
        mSocket = activity?.let {
            TechResApplication.instance?.getSocketGameInstance(
                it,
                restaurant().restaurant_id ?: 0
            )
        }
        mSocket?.connect()
        mSocket?.on(TechResEnumGame.RES_ROOM_IS_PLAYING.toString()) {
            Thread {
                try {
                    WriteLog.d("RES_ROOM_IS_PLAYING", it[0].toString())
//                    socketLuckyWheelHandler?.onNumber()
//                    val playingLuckyWheelResponse = Gson().fromJson<PlayingLuckyWheelResponse>(
//                        it[0].toString(),
//                        object :
//                            TypeToken<PlayingLuckyWheelResponse>() {}.type
//                    )
//                    gameList.forEachIndexed { index, roomGame ->
//                        if (roomGame.room_id.equals(playingLuckyWheelResponse.room_id)) {
//                            roomGame.status = playingLuckyWheelResponse.status
//                        }
//                    }
//                    if (listRoomGameAdapter != null)
//                        listRoomGameAdapter?.setDataSource(gameList, listDataToFile)
                    restaurant().restaurant_id?.let { it1 -> getListRoomGame(it1) }
                } catch (e: IOException) {
                }
            }.start()
        }
    }
}