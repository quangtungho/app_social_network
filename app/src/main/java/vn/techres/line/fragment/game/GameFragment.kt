package vn.techres.line.fragment.game

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.game.luckywheel.GameListAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.game.Game
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.game.ListGameResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.FragmentGameBinding
import vn.techres.line.fragment.game.drink.DrinksGameTableFragment
import vn.techres.line.fragment.game.luckywheel.ListRoomGameFragment
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.ChooseGame
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

open class GameFragment : BaseBindingFragment<FragmentGameBinding>(FragmentGameBinding::inflate), ChooseGame {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var gameListAdapter: GameListAdapter? = null
    private var gameList = ArrayList<Game>()
    private var branch = Branch()
    private var nodeJs = ConfigNodeJs()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
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
        gameListAdapter = GameListAdapter(requireActivity())
        gameListAdapter?.setClickGame(this)
        binding.mRecyclerView.layoutManager = GridLayoutManager(mainActivity, 2)
        binding.mRecyclerView.adapter = gameListAdapter
        val bundle = arguments
        if(bundle != null){
            branch = Gson().fromJson(
                bundle.getString(TechresEnum.KEY_CHOOSE_BRANCH.toString()), object :
                    TypeToken<Branch>() {}.type
            )
            binding.rlLoadingGame.hide()
            binding.swipeRefreshContainerGame.hide()
            binding.llEmptyGame.show()
            if(gameList.isEmpty()){
                binding.rlLoadingGame.show()
                binding.swipeRefreshContainerGame.hide()
                Handler(Looper.getMainLooper()).postDelayed({
                    getListGame(branch)
                }, 300)
            }else{
                binding.rlLoadingGame.hide()
                binding.swipeRefreshContainerGame.show()
                gameListAdapter?.setDataSource(gameList)
            }
        }
        setListener()
    }

    private fun setListener(){
        binding.imgBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.swipeRefreshContainerGame.setOnRefreshListener {
            getListGame(branch)
        }
        binding.searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    gameListAdapter?.filter(newText)
                }
                return true
            }
        })
    }

    private fun getListGame(branch: Branch) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_LUCKY_WHEEL
        baseRequest.request_url = String.format("%s%s%s%s", "/api/article-game/get-list?restaurant_id=", restaurant().restaurant_id, "&branch_id=", branch.id)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getListGame(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ListGameResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    binding.rlLoadingGame.hide()
                    binding.swipeRefreshContainerGame.hide()
                    binding.llEmptyGame.show()
                    binding.swipeRefreshContainerGame.isRefreshing = false
                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: ListGameResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        gameList = response.data
                        if(gameList.isNotEmpty()){
                            binding.rlLoadingGame.hide()
                            binding.swipeRefreshContainerGame.show()
                            binding.llEmptyGame.hide()
                            gameListAdapter?.setDataSource(gameList)
                        }else{
                            binding.rlLoadingGame.hide()
                            binding.swipeRefreshContainerGame.hide()
                            binding.llEmptyGame.show()
                        }
                    } else {
                        binding.rlLoadingGame.hide()
                        binding.swipeRefreshContainerGame.hide()
                        binding.llEmptyGame.show()
                    }
                    mainActivity?.setLoading(false)
                    binding.swipeRefreshContainerGame.isRefreshing = false
                }
            })
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClickGame(game: Game) {
        val bundle = Bundle()
        when (game.link) {
            TechresEnum.HOW_MANY_MINUTES.toString() -> {
                bundle.putString(
                    TechresEnum.HOW_MANY_MINUTES.toString(),
                   arguments?.getString(TechresEnum.KEY_CHOOSE_BRANCH.toString())
                )
                val drinksGameTableFragment = DrinksGameTableFragment()
                drinksGameTableFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, drinksGameTableFragment)
            }
            TechresEnum.LUCKY_WHEEL.toString() -> {
                try {
                    bundle.putString(
                        TechresEnum.GAME_ID.toString(),
                        game._id
                    )
                    bundle.putInt(TechresEnum.TYPE_GAME.toString(), game.type ?: 1)
                } catch (e: JsonProcessingException) {
                    e.printStackTrace()
                }
                val listRoomGameFragment = ListRoomGameFragment()
                listRoomGameFragment.arguments = bundle
                mainActivity?.addOnceFragment(this, listRoomGameFragment)
            }
        }
    }

    override fun onBackPress() : Boolean{
        return true
    }
}