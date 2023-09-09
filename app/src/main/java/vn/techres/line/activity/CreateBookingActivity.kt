package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.TimePicker
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import vn.techres.line.R
import vn.techres.line.adapter.booking.CartFoodAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.booking.FoodMenu
import vn.techres.line.data.model.booking.FoodOrder
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.data.model.eventbus.BranchSelectedEventBus
import vn.techres.line.data.model.eventbus.EventBusChooseFood
import vn.techres.line.data.model.params.CreateBookingParams
import vn.techres.line.data.model.response.CreateBookingResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ActivityCreateBookingBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.ChooseMenuFoodHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

class CreateBookingActivity : BaseBindingActivity<ActivityCreateBookingBinding>(), ChooseMenuFoodHandler {
    private var arrayFoods = ArrayList<FoodOrder>()
    private var itemCarts = ArrayList<FoodMenu>()
    private var branchDetail = Branch()
    private var adapterChoose: CartFoodAdapter? = null
    private var bottomSheetFoodsDialog: BottomSheetDialog? = null
    private var rcFoodBooking: RecyclerView? = null
    private var imgCloseDialog: ImageView? = null
    private var tvAddFoodBooking: TextView? = null
    private var tvTotalMoney: TextView? = null

    //time
    private var hourTime = 0
    private var minuteTime = 0
    private var time = ""

    //date
    private var pickedDate = ""
    private var day = 0
    private var month = 0
    private var year = 0

    private var testNum = 0

    private var nodeJs = ConfigNodeJs()
    override val bindingInflater: (LayoutInflater) -> ActivityCreateBookingBinding
        get() = ActivityCreateBookingBinding::inflate

    override fun onSetBodyView() {
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)
        
        setListener()
    }

    @SuppressLint("SimpleDateFormat", "UseRequireInsteadOfGet")
    private fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.edtNumberSlot.addTextChangedListener(textWatcher)

        binding.lnChooseBranchMain.setOnClickListener {
            val intent = Intent(this, ChooseBranchActivity::class.java)
            intent.putExtra(TechresEnum.TYPE_CHOOSE_BRANCH.toString(), "create_booking")
            startActivity(intent)
            this.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right
            )
        }

        binding.lnDate.setOnClickListener {
            if (branchDetail.id == 0) {
                snackBarPleaseRestaurantChoose()
            }else{
                dialogDatePicker()
            }
        }

        binding.lnTime.setOnClickListener {
            if (branchDetail.id == 0) {
                snackBarPleaseRestaurantChoose()
            }else{
                dialogTimerPicker()
            }
        }

        binding.imgReduction.setOnClickListener {
            if (branchDetail.id == 0) {
                snackBarPleaseRestaurantChoose()
            }else{
                testNum = binding.edtNumberSlot.text.toString().toInt()
                if (testNum > 0) {
                    testNum--
                    val str = testNum.toString()
                    binding.edtNumberSlot.text = Editable.Factory.getInstance().newEditable(str)
                }
            }
        }
        binding.imgIncrease.setOnClickListener {
            if (branchDetail.id == 0) {
                snackBarPleaseRestaurantChoose()
            }else{
                testNum = binding.edtNumberSlot.text.toString().toInt()
                testNum++
                val str = testNum.toString()
                binding.edtNumberSlot.text = Editable.Factory.getInstance().newEditable(str)
            }
        }
        binding.txtChooseFood.setOnClickListener {
            if (branchDetail.id == 0) {
                snackBarPleaseRestaurantChoose()
            }else{
                if (itemCarts.size != 0) {
                    dialogFoodChoose()
                } else {
                    val intent = Intent(this, ChooseFoodActivity::class.java)
                    intent.putExtra(TechresEnum.ID_BRANCH.toString(), branchDetail.id ?: 0)
                    intent.putExtra(TechresEnum.MENU_FOOD.toString(), Gson().toJson(itemCarts))
                    startActivity(intent)
                    this.overridePendingTransition(
                        R.anim.translate_from_right,
                        R.anim.translate_to_right)
                }
            }
        }
        binding.btnBooking.setOnClickListener {
            if (branchDetail.id == 0) {
                snackBarPleaseRestaurantChoose()
            }else{
                checkCreateBooking()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventBusChooseBranch(branchSelected: BranchSelectedEventBus) {
        if (branchSelected.data.id != 0) {
            branchDetail = branchSelected.data
            binding.lnChooseBranch.visibility = View.GONE
            binding.rlBranch.visibility = View.VISIBLE

            binding.imgAvatarBranch.load(
                String.format(
                    "%s%s",
                    nodeJs.api_ads,
                    branchSelected.data.image_logo_url.medium
                )
            ) {
                crossfade(true)
                scale(Scale.FILL)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                transformations(RoundedCornersTransformation(10F))
            }
            binding.txtNameBranch.text = branchSelected.data.name
            binding.txtAddressBranch.text = branchSelected.data.address_full_text
            binding.txtRatingBranch.text = String.format("%.1f", branchSelected.data.rating)

            setChooseMenu()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChooseFood(data: EventBusChooseFood) {
        if (data.foodsChoose.size != 0){
            itemCarts = data.foodsChoose
            for (i in itemCarts.indices) {
                val foodOrder = FoodOrder()
                foodOrder.food_id = data.foodsChoose[i].id
                foodOrder.quantity = data.foodsChoose[i].quantity
                arrayFoods.add(foodOrder)
            }
            setChooseMenu()
        }
    }
    
    private fun snackBarPleaseRestaurantChoose(){
        val snackBar = Snackbar.make(
            binding.root,
            this.resources.getString(R.string.choose_restaurant),
            Snackbar.LENGTH_LONG
        )
        snackBar.show()
    }

    private fun setChooseMenu() {
        if (itemCarts.isNotEmpty()) {
            val array = itemCarts.map { it.food_name }.joinToString()
            binding.txtChooseFood.text = array
        } else {
            binding.txtChooseFood.text =
                this.resources.getString(R.string.click_choose_food)
        }
    }
    
    private fun getTimer(hour: Int, minute: Int): String {
        val minuteTime = if (minute in 0..9)
            "0$minute"
        else
            minute.toString()
        return String.format("%s:%s", hour, minuteTime)
    }

    private fun getDate(date: Int): String {
        return if (date in 1..9) {
            "0$date"
        } else {
            date.toString()
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            if (s.toString() == "") {
                binding.edtNumberSlot.text = Editable.Factory.getInstance().newEditable("0")
            }
        }
    }

    private fun getTotalPrice(foodSelected: ArrayList<FoodMenu>): String {
        var itemPriceCount = 0F
        foodSelected.forEach {
            itemPriceCount += it.quantity * (it.price ?: 0).toFloat()
        }
        return String.format(
            "%s %s",
            Currency.formatCurrencyDecimal(itemPriceCount.toString().toFloat()),
            this.getString(R.string.denominations)
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setCart(foodSelected: ArrayList<FoodMenu>, position: Int) {
        when (foodSelected[position].quantity) {
            0 -> {
                foodSelected.removeAt(position)
                adapterChoose?.notifyDataSetChanged()
            }
            else -> {
                adapterChoose?.notifyItemChanged(position)
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun checkCreateBooking() {
        if (binding.txtDate.text == "") {
            val snackBar = Snackbar.make(
                binding.root,
                this.resources.getString(R.string.choose_all_create_booking),
                Snackbar.LENGTH_LONG
            )
            snackBar.show()
        }
        if (binding.txtTime.text == "") {
            val snackBar = Snackbar.make(
                binding.root,
                this.resources.getString(R.string.choose_all_create_booking),
                Snackbar.LENGTH_LONG
            )
            snackBar.show()
        }

        if (binding.edtNumberSlot.text.toString().toInt() == 0) {
            val snackBar = Snackbar.make(
                binding.root,
                this.resources.getString(R.string.choose_all_create_booking),
                Snackbar.LENGTH_LONG
            )
            snackBar.show()
        }
        if (binding.txtDate.text !== "" && binding.txtTime.text !== "" && binding.edtNumberSlot.text.toString()
                .toInt() != 0
        ) {
            onCreateBooking()
        }
    }

    private fun dialogTimerPicker() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_time_picker)
        bottomSheetDialog.setCancelable(false)
        val imgClose = bottomSheetDialog.findViewById<ImageView>(R.id.imgClose)
        val btnConfirm = bottomSheetDialog.findViewById<Button>(R.id.btnConfirm)
        val timePicker = bottomSheetDialog.findViewById<TimePicker>(R.id.timePicker)
        timePicker!!.setIs24HourView(true)
        if (hourTime != 0 || minuteTime != 0) {
            timePicker.minute = minuteTime
            timePicker.hour = hourTime
        }

        imgClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        btnConfirm?.setOnClickListener {
            binding.txtTime.text = getTimer(timePicker.hour, timePicker.minute)
            time = getTimer(timePicker.hour, timePicker.minute)
            hourTime = timePicker.hour
            minuteTime = timePicker.minute
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun dialogDatePicker() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.SheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_date_picker)
        bottomSheetDialog.setCancelable(false)
        val tvTitleChooseDate = bottomSheetDialog.findViewById<TextView>(R.id.tvTitleChooseDate)
        val imgClose = bottomSheetDialog.findViewById<ImageView>(R.id.imgClose)
        val btnConfirm = bottomSheetDialog.findViewById<Button>(R.id.btnConfirm)
        val datePicker =
            bottomSheetDialog.findViewById<com.ycuwq.datepicker.date.DatePicker>(R.id.datePicker)
        if (day != 0 && month != 0 && year != 0) {
            datePicker?.setDate(year, month, day)
        }
        tvTitleChooseDate?.text =
            this.resources.getString(R.string.choose_date_booking)

        imgClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        btnConfirm?.setOnClickListener {
            binding.txtDate.text = String.format(
                "%s/%s/%s",
                getDate(datePicker!!.day),
                getDate(datePicker.month),
                datePicker.year
            )
            pickedDate = String.format(
                "%s/%s/%s",
                getDate(datePicker.day),
                getDate(datePicker.month),
                datePicker.year
            )
            day = datePicker.day
            month = datePicker.month
            year = datePicker.year
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun dialogFoodChoose() {
        bottomSheetFoodsDialog = BottomSheetDialog(this, R.style.SheetDialog)
        bottomSheetFoodsDialog?.setContentView(R.layout.dialog_food_booking)
        bottomSheetFoodsDialog?.setCancelable(true)
        adapterChoose = CartFoodAdapter(this.baseContext)
        adapterChoose?.setChooseMenuFoodHandler(this)
        rcFoodBooking = bottomSheetFoodsDialog?.findViewById(R.id.rcFoodBooking)
        imgCloseDialog = bottomSheetFoodsDialog?.findViewById(R.id.imgCloseDialog)
        tvAddFoodBooking = bottomSheetFoodsDialog?.findViewById(R.id.tvAddFoodBooking)
        tvTotalMoney = bottomSheetFoodsDialog?.findViewById(R.id.tvTotalMoney)
        rcFoodBooking?.layoutManager = LinearLayoutManager(
            this.baseContext,
            RecyclerView.VERTICAL,
            false
        )
        rcFoodBooking?.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(rcFoodBooking?.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        rcFoodBooking?.setHasFixedSize(true)
        rcFoodBooking?.adapter = adapterChoose
        
        adapterChoose?.setDataSource(itemCarts)
        tvTotalMoney?.text = getTotalPrice(itemCarts)
        bottomSheetFoodsDialog?.show()

        imgCloseDialog?.setOnClickListener {
            bottomSheetFoodsDialog?.dismiss()
        }

        tvAddFoodBooking?.setOnClickListener {
            arrayFoods.clear()
            bottomSheetFoodsDialog?.dismiss()
            val intent = Intent(this, ChooseFoodActivity::class.java)
            intent.putExtra(TechresEnum.ID_BRANCH.toString(), branchDetail.id ?: 0)
            intent.putExtra(TechresEnum.MENU_FOOD.toString(), Gson().toJson(itemCarts))
            startActivity(intent)
            this.overridePendingTransition(
                R.anim.translate_from_right,
                R.anim.translate_to_right)
        }
    }

    //Create booking
    private fun onCreateBooking() {
        val params = CreateBookingParams()
        params.http_method = AppConfig.POST
        params.request_url = "api/bookings/create"
        params.params.branch_id = branchDetail.id
        params.params.note = ""
        params.params.booking_time =
            String.format("%s %s", binding.txtDate.text.toString(), binding.txtTime.text.toString())
        params.params.number_slot = binding.edtNumberSlot.text.toString().toInt()
        params.params.orther_requirements = binding.edtNote.text.toString()
        params.params.food_request = arrayFoods

        params.let {
            ServiceFactory.createRetrofitService(

                TechResService::class.java
            )

                .createBooking(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CreateBookingResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: CreateBookingResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            val intent = Intent(this@CreateBookingActivity, CreateBookingSuccessActivity::class.java)
                            intent.putExtra(TechresEnum.BRANCH_PHONE.toString(), response.data!!.branch_phone)
                            startActivity(intent)
                            this@CreateBookingActivity.overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                        } else {
                            val snackBar = Snackbar.make(
                                binding.root,
                                response.message.toString(),
                                Snackbar.LENGTH_LONG
                            )
                            snackBar.show()
                        }

                    }
                })
        }
    }

    override fun onPlusQuality(food: FoodMenu) {

    }

    override fun onMinusQuality(food: FoodMenu) {

    }

    override fun onPlusQualityCart(position: Int) {
        if (itemCarts[position].quantity in 0..99) {
            itemCarts[position].quantity++
            adapterChoose?.notifyItemChanged(position)
            tvTotalMoney?.text = getTotalPrice(itemCarts)
        }
    }

    override fun onMinusQualityCart(position: Int) {
        if (itemCarts[position].quantity > 0) {
            itemCarts[position].quantity--
            setCart(itemCarts, position)
            tvTotalMoney?.text = getTotalPrice(itemCarts)
        }else{
            setCart(itemCarts, position)
        }
        setChooseMenu()
    }

    override fun onRemoveFood(food: FoodMenu) {
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onRemoveFoodCart(position: Int) {
        itemCarts.removeAt(position)
        adapterChoose?.notifyDataSetChanged()
        tvTotalMoney?.text = getTotalPrice(itemCarts)
        setChooseMenu()
        if (itemCarts.size == 0) {
            bottomSheetFoodsDialog?.dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

}