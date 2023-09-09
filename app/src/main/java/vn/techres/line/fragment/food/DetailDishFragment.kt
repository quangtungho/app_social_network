package vn.techres.line.fragment.food

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import com.bumptech.glide.Glide
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.food.FoodTakeAway
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.FragmentDetailDishBinding
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import java.util.*

class DetailDishFragment :
    BaseBindingFragment<FragmentDetailDishBinding>(FragmentDetailDishBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    private var configNodeJs = ConfigNodeJs()
    private var branchID = 0
    private var detailFoodTakeAway = FoodTakeAway()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity())
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
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)

        arguments?.let {
            detailFoodTakeAway = Gson().fromJson(it.getString(TechresEnum.DETAIL_DISH.toString()).toString(), object :
                TypeToken<FoodTakeAway>() {}.type)
            branchID = detailFoodTakeAway.branch_id ?: 0
        }
        binding.header.tvTitleHomeHeader.text =  requireActivity().resources.getString(R.string.detail_food)
        Glide.with(binding.imgAvatarFood)
            .load(String.format("%s%s", configNodeJs.api_ads, detailFoodTakeAway.avatar?.original))
            .centerCrop()
            .into(binding.imgAvatarFood)
        binding.tvNameFood.text = detailFoodTakeAway.name
        binding.tvDescription.text = detailFoodTakeAway.description

        binding.tvPriceFood.text = String.format(
            "%s%s",
            Currency.formatCurrencyDecimal((detailFoodTakeAway.price ?: 0).toFloat()),
            resources.getString(R.string.denominations)
        )
        if (detailFoodTakeAway.quantity == 0) {
            detailFoodTakeAway.quantity = 1
            binding.tvNumberChoose.text = detailFoodTakeAway.quantity.toString()
        } else {
            binding.tvNumberChoose.text = detailFoodTakeAway.quantity.toString()
        }
        setListener()
    }

    private fun setListener(){
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.tvAddFood.text = String.format(
            "%s %s %s%s",
            resources.getString(R.string.cart),
            "-",
            Currency.formatCurrencyDecimal(
                (detailFoodTakeAway.price?.times(
                    binding.tvNumberChoose.text.toString().toInt()
                )) ?: 0F
            ),
            resources.getString(R.string.denominations)
        )
        binding.imgPlus.setOnClickListener {
            if (detailFoodTakeAway.quantity < 100) {
                detailFoodTakeAway.quantity++
                binding.tvNumberChoose.text = detailFoodTakeAway.quantity.toString()
                binding.tvAddFood.text = String.format(
                    "%s %s %s%s",
                    resources.getString(R.string.cart),
                    "-",
                    Currency.formatCurrencyDecimal(
                        (detailFoodTakeAway.price?.times(
                            binding.tvNumberChoose.text.toString().toInt()
                        )) ?: 0F
                    ),
                    resources.getString(R.string.denominations)
                )
            }
        }

        binding.imgMinus.setOnClickListener {
            if (binding.tvNumberChoose.text.toString().toInt() > 0) {
                detailFoodTakeAway.quantity--
                binding.tvNumberChoose.text = detailFoodTakeAway.quantity.toString()
                binding.tvAddFood.text = String.format(
                    "%s %s %s%s",
                    resources.getString(R.string.cart),
                    "-",
                    Currency.formatCurrencyDecimal(
                        (detailFoodTakeAway.price?.times(
                            binding.tvNumberChoose.text.toString().toInt()
                        )) ?: 0F
                    ),
                    resources.getString(R.string.denominations)
                )
            }
            if (detailFoodTakeAway.quantity == 0) {
                binding.tvAddFood.text = resources.getString(R.string.backmenu)
            }
        }

        binding.lnAddCart.setOnClickListener {
            try {
                var idBranchCheck: Int? = null
                val bundleFood = Bundle()
                val checkIDBranch = cacheManager.get(TechresEnum.KEY_CHECK_ID_BRANCH.toString())
                if (!checkIDBranch.isNullOrBlank()) {
                    idBranchCheck = checkIDBranch.toInt()
                }
                if (idBranchCheck != null) {
                    if (idBranchCheck == branchID) {
                        bundleFood.putString(
                            TechresEnum.KEY_FOOD.toString(),
                            Gson().toJson(detailFoodTakeAway)
                        )
                        bundleFood.putInt(TechresEnum.KEY_CHECK_ID_BRANCH.toString(), branchID)
                        cacheManager.put(
                            TechresEnum.KEY_CHECK_ID_BRANCH.toString(),
                            branchID.toString()
                        )
                        mainActivity?.getOnRefreshFragment()?.onCallBack(bundleFood)
                        mainActivity?.supportFragmentManager?.popBackStack()
                    } else {
                        val dialog = Dialog(requireActivity())
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialog.setCancelable(true)
                        dialog.setContentView(R.layout.dialog_branch_check)
                        val lp = WindowManager.LayoutParams()
                        lp.copyFrom(dialog.window!!.attributes)
                        lp.width = WindowManager.LayoutParams.MATCH_PARENT
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                        lp.gravity = Gravity.CENTER
                        dialog.window!!.attributes = lp
                        val btnBackBranch = dialog.findViewById(R.id.btnBackBranch) as Button
                        val btnDeleteBranch = dialog.findViewById(R.id.btnDeleteBranch) as Button
                        btnBackBranch.setOnClickListener {
                            dialog.dismiss()
                        }
                        btnDeleteBranch.setOnClickListener {
                            cacheManager.put(
                                TechresEnum.KEY_CHECK_ID_BRANCH.toString(),
                                branchID.toString()
                            )
                            cacheManager.remove(TechresEnum.KEY_CART_CHOOSE.toString())
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                } else {
                    bundleFood.putString(
                        TechresEnum.KEY_FOOD.toString(),
                        Gson().toJson(detailFoodTakeAway)
                    )
                    bundleFood.putInt(TechresEnum.KEY_CHECK_ID_BRANCH.toString(), branchID)
                    cacheManager.put(
                        TechresEnum.KEY_CHECK_ID_BRANCH.toString(),
                        branchID.toString()
                    )
                    mainActivity?.getOnRefreshFragment()?.onCallBack(bundleFood)
                    mainActivity?.supportFragmentManager?.popBackStack()
                }
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPress() : Boolean{
        return true
    }

}