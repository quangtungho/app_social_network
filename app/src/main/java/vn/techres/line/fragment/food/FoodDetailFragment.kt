package vn.techres.line.fragment.food

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.food.SuggestFoodDetailAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.cart.CartFoodTakeAway
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentFoodDetailBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.food.FoodDetailHandler
import java.text.DecimalFormat

class FoodDetailFragment :
    BaseBindingFragment<FragmentFoodDetailBinding>(FragmentFoodDetailBinding::inflate),
    FoodDetailHandler {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var food: FoodTakeAway? = null
    private var adapter: SuggestFoodDetailAdapter? = null
    private var user = User()
    private var configNodeJs = ConfigNodeJs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = "Chi Tiết"
        adapter = SuggestFoodDetailAdapter(requireActivity())
        adapter?.setFoodDetailHandler(this)
        binding.rcFoodSuggest.layoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
        binding.rcFoodSuggest.adapter = adapter
        arguments?.let {
            food = Gson().fromJson(
                it.getString(TechresEnum.FOOD_DETAIL_FRAGMENT.toString()), object :
                    TypeToken<FoodTakeAway>() {}.type
            )
            setData(food)
            setListener()
        }
    }

    private fun setData(foodTakeAway: FoodTakeAway?) {
        Glide.with(binding.imgAvatarFood)
            .load(String.format("%s%s", configNodeJs.api_ads, foodTakeAway?.avatar?.original))
            .centerCrop()
            .into(binding.imgAvatarFood)
        binding.tvNameFood.text = foodTakeAway?.name ?: ""
        binding.tvPriceFood.text = String.format(
            "%s%s",
            Currency.formatCurrencyDecimal(foodTakeAway?.price ?: 0F),
            requireActivity().resources.getString(R.string.denominations)
        )
        binding.tvRatingFood.text = DecimalFormat("0.0").format(foodTakeAway?.branchStar ?: 0F)
        binding.tvNumberChoose.text = if (foodTakeAway?.quantity == 0) {
            foodTakeAway.quantity = 1
            foodTakeAway.quantity.toString()
        } else {
            (foodTakeAway?.quantity ?: 0).toString()
        }
        binding.tvAddFood.text = String.format(
            "%s %s %s%s",
            resources.getString(R.string.cart),
            "-",
            Currency.formatCurrencyDecimal(
                (foodTakeAway?.price?.times(
                    binding.tvNumberChoose.text.toString().toInt()
                )) ?: 0F
            ),
            requireActivity().resources.getString(R.string.denominations)
        )
    }

    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.lnAddCart.setOnClickListener {
            food?.let {
                saveCacheCart(it.branch_id ?: 0, it)
            }
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.imgPlus.setOnClickListener {
            if ((food?.quantity ?: 0) < 100) {
                food?.quantity = (food?.quantity ?: 0) + 1
                binding.tvNumberChoose.text = food?.quantity.toString()
                binding.tvAddFood.text = String.format(
                    "%s %s %s%s",
                    requireActivity().resources.getString(R.string.cart),
                    "-",
                    Currency.formatCurrencyDecimal(
                        (food?.price?.times(
                            binding.tvNumberChoose.text.toString().toInt()
                        )) ?: 0F
                    ),
                    requireActivity().resources.getString(R.string.denominations)
                )
            }
        }
        binding.imgMinus.setOnClickListener {
            if (binding.tvNumberChoose.text.toString().toInt() > 0) {
                food?.quantity = (food?.quantity ?: 1) - 1
                binding.tvNumberChoose.text = food?.quantity.toString()
                binding.tvAddFood.text = String.format(
                    "%s %s %s%s",
                    requireActivity().resources.getString(R.string.cart),
                    "-",
                    Currency.formatCurrencyDecimal(
                        (food?.price?.times(
                            binding.tvNumberChoose.text.toString().toInt()
                        )) ?: 0F
                    ),
                    requireActivity().resources.getString(R.string.denominations)
                )
            }
            if (food?.quantity == 0) {
                binding.tvAddFood.text = resources.getString(R.string.back_list)
            }
        }
    }

    override fun onChooseFoodSuggest(food: FoodTakeAway) {
    }

    private fun saveCacheCart(idBranch: Int, food: FoodTakeAway) {
        var cart = CartFoodTakeAway()
        val arrayList = ArrayList<FoodTakeAway>()
        val jsonCart = cacheManager.get(TechresEnum.KEY_CART_CHOOSE.toString())
        if (!jsonCart.isNullOrBlank()) {
            cart = Gson().fromJson(jsonCart, object :
                TypeToken<CartFoodTakeAway>() {}.type)
            if (cart.id_branch == idBranch) {
                var isCheckFood = false
                cart.food.forEach {
                    if (it.id == food.id) {
                        it.quantity = food.quantity
                        isCheckFood = false
                        return@forEach
                    } else {
                        isCheckFood = true
                    }
                }
                if (isCheckFood) {
                    arrayList.add(food)
                }
                cacheManager.put(TechresEnum.KEY_CART_CHOOSE.toString(), Gson().toJson(cart))
                if (arrayList.size == 0) {
                    cacheManager.remove(TechresEnum.KEY_CHECK_ID_BRANCH.toString())
                    cacheManager.remove(TechresEnum.KEY_CART_CHOOSE.toString())
                }
            } else {
                dialogCheckBranch()
            }
        } else {
            arrayList.add(food)
            cart.id_branch = idBranch
            cart.food = arrayList
            cacheManager.put(TechresEnum.KEY_CART_CHOOSE.toString(), Gson().toJson(cart))
            if (arrayList.size == 0) {
                cacheManager.remove(TechresEnum.KEY_CHECK_ID_BRANCH.toString())
                cacheManager.remove(TechresEnum.KEY_CART_CHOOSE.toString())
            }
        }
        cacheManager.put(TechresEnum.KEY_CHECK_CON_FIRM.toString(), 0.toString())
    }

    private fun dialogCheckBranch() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_branch_check)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes = lp
        val btnBackBranch = dialog.findViewById(R.id.btnBackBranch) as Button
        val btnDeleteBranch = dialog.findViewById(R.id.btnDeleteBranch) as Button
        btnBackBranch.setOnClickListener {
            dialog.dismiss()
        }
        btnDeleteBranch.setOnClickListener {
            cacheManager.put(
                TechresEnum.KEY_CHECK_ID_BRANCH.toString(),
                (food?.branch_id ?: 0).toString()
            )
            cacheManager.remove(TechresEnum.KEY_CART_CHOOSE.toString())
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPress() : Boolean {
        return true
    }
}