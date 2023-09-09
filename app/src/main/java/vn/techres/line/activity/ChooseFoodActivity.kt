package vn.techres.line.activity

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.adapter.booking.CartFoodAdapter
import vn.techres.line.adapter.food.ChooseMenuFoodAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.booking.FoodMenu
import vn.techres.line.data.model.eventbus.EventBusChooseFood
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.MenuResponse
import vn.techres.line.databinding.ActivityChooseFoodBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Currency
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.ChooseMenuFoodHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

class ChooseFoodActivity : BaseBindingActivity<ActivityChooseFoodBinding>(), ChooseMenuFoodHandler {
    //bottom sheet
    private var rcFoodBooking: RecyclerView? = null
    private var tvAddFoodBooking: TextView? = null
    private var tvTotalMoney: TextView? = null
    private var imgCloseDialog: ImageView? = null

    private var adapterMenu: ChooseMenuFoodAdapter? = null
    private var adapterChoose: CartFoodAdapter? = null
    private var foods = ArrayList<FoodMenu>()
    private var foodsChoose = ArrayList<FoodMenu>()
    private var bottomSheetDialog: BottomSheetDialog? = null
    private var fade: Animation? = null
    private var branchID = 0
    private var menuFood = ""

    override val bindingInflater: (LayoutInflater) -> ActivityChooseFoodBinding
        get() = ActivityChooseFoodBinding::inflate

    override fun onSetBodyView() {
        intent?.let {
            branchID = it.getIntExtra(TechresEnum.ID_BRANCH.toString(), 0)
            menuFood = it.getStringExtra(TechresEnum.MENU_FOOD.toString())?:""
        }

        fade = AnimationUtils.loadAnimation(this, R.anim.anim_birthday)

        adapterMenu = ChooseMenuFoodAdapter(this)
        adapterChoose = CartFoodAdapter(this)
        adapterMenu?.setChooseMenuFoodHandler(this)
        adapterChoose?.setChooseMenuFoodHandler(this)

        binding.rcMenuFood.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        binding.rcMenuFood.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.rcMenuFood.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        binding.rcMenuFood.setHasFixedSize(true)
        binding.rcMenuFood.adapter = adapterMenu

        //bottom sheet
        bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        bottomSheetDialog?.setContentView(R.layout.dialog_food_booking)
        bottomSheetDialog?.setCancelable(true)
        tvAddFoodBooking = bottomSheetDialog?.findViewById(R.id.tvAddFoodBooking)
        rcFoodBooking = bottomSheetDialog?.findViewById(R.id.rcFoodBooking)
        tvTotalMoney = bottomSheetDialog?.findViewById(R.id.tvTotalMoney)
        imgCloseDialog = bottomSheetDialog?.findViewById(R.id.imgCloseDialog)
        rcFoodBooking?.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        rcFoodBooking?.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(rcFoodBooking?.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        rcFoodBooking?.setHasFixedSize(true)
        rcFoodBooking?.adapter = adapterChoose

        setData()
        setListener()
    }

    private fun setData() {
        binding.lnConfirmFood.show()
        getFoods()
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        imgCloseDialog?.setOnClickListener {
            bottomSheetDialog?.dismiss()
        }
        binding.lnConfirmFood.setOnClickListener {
            onBackPressed()
        }
        binding.imgCartFood.setOnClickListener {
            dialogFoodChoose()
        }

        binding.svFoodMenu.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    adapterMenu?.searchFullText(newText)
                }
                return true
            }
        })
    }

    private fun animationFood(foodSelected: ArrayList<FoodMenu>) {
        var itemQuantityCount = 0
        var itemPriceCount = 0F

        foodSelected.forEach {
            itemQuantityCount += it.quantity
            itemPriceCount += it.quantity * (it.price)!!.toFloat()
        }
        binding.tvTotalFood.let{
            it.text = String.format(
                "%s%s",
                this.resources.getString(R.string.unit_quantity),
                itemQuantityCount
            )
            it.alpha = 1F
            it.startAnimation(fade)
        }
        binding.tvTotalPrice.let {
            it.text = String.format(
                "%s %s", Currency.formatCurrencyDecimal(
                    itemPriceCount.toString().toFloat()
                ), this.resources.getString(R.string.denominations)
            )
            it.startAnimation(fade)
        }


        if (itemQuantityCount == 0) {
            binding.lnConfirmFood.hide()
        } else {
            binding.lnConfirmFood.show()
        }
    }

    private fun setCart(food: FoodMenu, foodSelected: ArrayList<FoodMenu>) {
        when (food.quantity) {
            0 -> {
                foodSelected.removeIf { it.id == food.id }
                adapterChoose?.notifyItemRemoved(foodSelected.indexOfFirst { it.id == food.id })
            }
            else -> {
                if (foodSelected.any { it.id == food.id }) {
                    foodSelected[foodSelected.indexOfFirst { it.id == food.id }] = food
                } else {
                    foodSelected.add(food)
                }
                adapterChoose?.notifyItemChanged(foodSelected.indexOfFirst { it.id == food.id })
            }
        }
    }

    private fun getTotalPrice(foodSelected: ArrayList<FoodMenu>): String {
        var itemPriceCount = 0F
        foodSelected.forEach {
            itemPriceCount += it.quantity * (it.price)!!.toFloat()
        }
        return String.format(
            "%s %s", Currency.formatCurrencyDecimal(
                itemPriceCount.toString().toFloat()
            ), this.getString(R.string.denominations)
        )
    }

    private fun dialogFoodChoose() {
        adapterChoose?.setDataSource(foodsChoose)
        tvAddFoodBooking!!.hide()
        tvTotalMoney!!.text = getTotalPrice(foods)
        bottomSheetDialog?.show()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getFoods() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/foods/booking?branch_id=${branchID}&category_type=-1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getMenu(
                baseRequest
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MenuResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: MenuResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        foods = response.data!!.list
                        if (menuFood != ""){
                            val food = Gson().fromJson<ArrayList<FoodMenu>>(
                                menuFood,
                                object : TypeToken<ArrayList<FoodMenu>>() {}.type
                            )
                            foodsChoose = food
                            loop@ for (i in foods.indices) {
                                for (j in food.indices) {
                                    if (foods[i].id == food[j].id) {
                                        foods[i].quantity = food[j].quantity
                                        continue@loop
                                    }
                                }
                            }
                        }
                        animationFood(foods)
                        adapterMenu?.setDataSource(foods)
                    } else
                        Toast.makeText(this@ChooseFoodActivity, response.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onPlusQuality(food: FoodMenu) {
        adapterMenu?.let { it ->
            foods = it.getDataSource()
            val position = foods.indexOfFirst { it.id == food.id }

            if (foods[position].quantity in 0..99) {
                foods[position].quantity++
                it.notifyItemChanged(position)
                setCart(foods[position], foodsChoose)
                animationFood(foods)
                tvTotalMoney!!.text = getTotalPrice(foods)
            }
        }

    }

    override fun onMinusQuality(food: FoodMenu) {
        adapterMenu?.let { it ->
            foods = it.getDataSource()
            val position = foods.indexOfFirst { it.id == food.id }
            if (foods[position].quantity > 0) {
                foods[position].quantity--
                it.notifyItemChanged(position)
                setCart(foods[position], foodsChoose)
                animationFood(foods)
                tvTotalMoney!!.text = getTotalPrice(foods)
            }
        }

    }

    override fun onPlusQualityCart(position: Int) {
        adapterMenu?.let { it ->
            foods = it.getDataSource()
            if (foodsChoose[position].quantity in 0..99) {
                foodsChoose[position].quantity++
                foods[foods.indexOfFirst { it.id == foodsChoose[position].id }] = foodsChoose[position]
                it.notifyItemChanged(foods.indexOfFirst { it.id == foodsChoose[position].id })
                adapterChoose?.notifyItemChanged(position)
                setCart(foodsChoose[position], foodsChoose)
                animationFood(foods)
                tvTotalMoney!!.text = getTotalPrice(foods)
            }
        }

    }

    override fun onMinusQualityCart(position: Int) {
        adapterMenu?.let {
            foods = it.getDataSource()
            if (foodsChoose.size > 0) {
                if (foodsChoose[position].quantity > 0) {
                    foodsChoose[position].quantity--
                    foods[foods.indexOfFirst { foodMenu -> foodMenu.id == foodsChoose[position].id }] =
                        foodsChoose[position]
                    it.notifyItemChanged(foods.indexOfFirst { foodMenu ->  foodMenu.id == foodsChoose[position].id })
                    setCart(foodsChoose[position], foodsChoose)
                    animationFood(foods)
                    tvTotalMoney!!.text = getTotalPrice(foods)
                }
            }
        }

    }

    override fun onRemoveFood(food: FoodMenu) {
        adapterMenu?.let { it ->
            foods = it.getDataSource()
            val position = foods.indexOfFirst { it.id == food.id }
            foods[position].quantity = 0
            it.notifyItemChanged(position)
            setCart(foods[position], foodsChoose)
            animationFood(foods)
            tvTotalMoney!!.text = getTotalPrice(foods)
        }

    }

    override fun onRemoveFoodCart(position: Int) {
        val index = foods.indexOfFirst { it.id == foodsChoose[position].id }
        foods[index].quantity = 0
        adapterMenu?.notifyItemChanged(index)
        foodsChoose.removeAt(position)
        adapterChoose?.notifyDataSetChanged()
        animationFood(foods)
        tvTotalMoney!!.text = getTotalPrice(foods)
        if (foodsChoose.size == 0) {
            bottomSheetDialog?.dismiss()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        EventBus.getDefault().post(EventBusChooseFood(foodsChoose))
    }
}