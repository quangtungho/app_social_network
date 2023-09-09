package vn.techres.line.fragment.food

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import coil.load
import coil.size.Scale
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.food.FoodPurcharePoint
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.FragmentDetailDishBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil

@SuppressLint("UseRequireInsteadOfGet")
class DetailFoodPointFragment : BaseBindingFragment<FragmentDetailDishBinding>(FragmentDetailDishBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?
    
    private var branchID = 0
    private var totalPoint = 0
    private var totalPointChoose = 0
    private var foodPoint = FoodPurcharePoint()
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
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = requireActivity().resources.getString(R.string.detail_food)
        arguments?.let {
            val bundle = it.getString(TechresEnum.KEY_FOOD_POINTS.toString()).toString()
            branchID = it.getInt(TechresEnum.KEY_ID_BRANCH_POINT.toString())
            totalPoint = it.getInt(TechresEnum.KEY_TOTAL_POINT.toString())
            totalPointChoose = it.getInt(TechresEnum.KEY_TOTAL_POINT_CHOOSE.toString())
            try {
                foodPoint = Gson().fromJson(
                    bundle,
                    object : TypeToken<FoodPurcharePoint>() {}.type
                )
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
            setData()
        }
        setListener()
    }

    private fun setData() {
        binding.imgAvatarFood.load(String.format("%s%s", nodeJs.api_ads, foodPoint.food_image?.original)){
            crossfade(true)
            placeholder(R.drawable.logo_aloline_placeholder)
            error(R.drawable.logo_aloline_placeholder)
            scale(Scale.FILL)
        }
        binding.tvNameFood.text = foodPoint.name
        binding.tvDescription.text = foodPoint.description
        binding.tvPriceFood.text = String.format(
            "%s %s",
            foodPoint.point_to_purchase,
            resources.getString(R.string.point_mini)
        )
        if ((totalPoint - totalPointChoose) >= foodPoint.point_to_purchase!!) {
            if (foodPoint.quantity == 0) {
                totalPointChoose += foodPoint.point_to_purchase!!
                foodPoint.quantity = 1
            }
        } else {
            binding.tvAddFood.text = resources.getString(R.string.backmenu)
        }
        binding.tvNumberChoose.text = foodPoint.quantity.toString()
        binding.tvAddFood.text = String.format(
            "%s %s %s %s",
            resources.getString(R.string.addtofood),
            "-",
            foodPoint.point_to_purchase!!.times(binding.tvNumberChoose.text.toString().toInt()),
            resources.getString(R.string.point_mini)
        )
    }
    private fun setListener() {
        binding.header.btnBack.setOnClickListener {
            mainActivity?.supportFragmentManager?.popBackStack()
        }
        binding.imgPlus.setOnClickListener {
            if ((totalPoint - totalPointChoose) >= foodPoint.point_to_purchase!!) {
                foodPoint.quantity++
                totalPointChoose += foodPoint.point_to_purchase!!
                binding.tvNumberChoose.text = foodPoint.quantity.toString()
            } else {
                dialogNotificationPoint()
            }
            if (binding.tvNumberChoose.text.toString() != "0") {
                binding.tvAddFood.text = String.format(
                    "%s %s %s %s", resources.getString(R.string.addtofood),
                    "-",
                    foodPoint.point_to_purchase?.times(binding.tvNumberChoose.text.toString().toInt()),
                    resources.getString(R.string.point_mini)
                )
            }
        }
        binding.imgMinus.setOnClickListener {
            if (binding.tvNumberChoose.text.toString().toInt() > 0) {
                foodPoint.quantity--
                totalPointChoose -= 1 * foodPoint.point_to_purchase!!
                binding.tvNumberChoose.text = foodPoint.quantity.toString()
                binding.tvAddFood.text = String.format(
                    "%s %s %s %s", resources.getString(R.string.addtofood),
                    "-",
                    foodPoint.point_to_purchase!!.times(binding.tvNumberChoose.text.toString().toInt()),
                    resources.getString(R.string.point_mini)
                )
            }
            if (foodPoint.quantity == 0) {
                binding.tvAddFood.text = resources.getString(R.string.backmenu)
            }
        }
        binding.lnAddCart.setOnClickListener {
            try {
                var idBranchCheck = 0
                val bundleFoodPoint = Bundle()
                val checkIDBranch = cacheManager.get(TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString())
                if (!checkIDBranch.isNullOrBlank()) {
                    idBranchCheck = checkIDBranch.toInt()
                }
                if (idBranchCheck != 0) {
                    if (idBranchCheck == branchID) {
                        if (totalPoint <= totalPointChoose) {
                            dialogNotificationPoint()
                        } else {
                            bundleFoodPoint.putString(
                                TechresEnum.KEY_FOOD_POINTS.toString(),
                                Gson().toJson(foodPoint)
                            )
                            bundleFoodPoint.putInt(
                                TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString(),
                                branchID
                            )
                            cacheManager.put(
                                TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString(),
                                branchID.toString()
                            )
                            mainActivity?.getOnRefreshFragment()?.onCallBack(bundleFoodPoint)
                            mainActivity?.supportFragmentManager?.popBackStack()
                        }
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
                                TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString(),
                                branchID.toString()
                            )
                            cacheManager.remove(TechresEnum.KEY_CART_POINT_CHOOSE.toString())
                            dialog.dismiss()
                        }
                        dialog.show()
                    }
                } else {
                    bundleFoodPoint.putString(
                        TechresEnum.KEY_FOOD_POINTS.toString(),
                        Gson().toJson(foodPoint)
                    )
                    bundleFoodPoint.putInt(
                        TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString(),
                        branchID
                    )
                    cacheManager.put(
                        TechresEnum.KEY_CHECK_ID_BRANCH_POINT.toString(),
                        branchID.toString()
                    )
                    mainActivity?.getOnRefreshFragment()?.onCallBack(bundleFoodPoint)
                    mainActivity?.supportFragmentManager?.popBackStack()
                }
            } catch (e: JsonProcessingException) {
                e.printStackTrace()
            }
        }
    }

    private fun dialogNotificationPoint() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_notification_food_point)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        dialog.window?.attributes = lp
        val btnFoodPoint = dialog.findViewById(R.id.btnBack) as Button
        btnFoodPoint.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPress() : Boolean{
        return true
    }

}