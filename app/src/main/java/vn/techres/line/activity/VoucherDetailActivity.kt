package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.glxn.qrgen.android.QRCode
import vn.techres.line.R
import vn.techres.line.adapter.branch.ChooseBranchAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.voucher.Voucher
import vn.techres.line.data.model.voucher.VoucherDetail
import vn.techres.line.data.model.voucher.VoucherDetailResponse
import vn.techres.line.data.model.voucher.VoucherResponse
import vn.techres.line.databinding.ActivityVoucherDetailBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.PreCachingLayoutManager
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class VoucherDetailActivity : BaseBindingActivity<ActivityVoucherDetailBinding>(){
    override val bindingInflater: (LayoutInflater) -> ActivityVoucherDetailBinding
        get() = ActivityVoucherDetailBinding::inflate
    private var dataVoucher = Voucher()
    private var data = VoucherDetail()
    private var configNodeJs = ConfigNodeJs()
    private var chooseBranchAdapter: ChooseBranchAdapter? = null


    override fun onSetBodyView() {
        binding.rlLoading.visibility = View.VISIBLE
        configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        intent?.let {
            dataVoucher = Gson().fromJson(it.getStringExtra(TechresEnum.VOUCHER_INFORMATION.toString()), object :
                TypeToken<Voucher>() {}.type)
        }

        chooseBranchAdapter = ChooseBranchAdapter(this)
        val preCachingLayoutManager =
            PreCachingLayoutManager(this, RecyclerView.VERTICAL, false)
        preCachingLayoutManager.setExtraLayoutSpace(1)
        chooseBranchAdapter!!.setHasStableIds(true)
        binding.recyclerView.layoutManager = preCachingLayoutManager
        binding.recyclerView.adapter = chooseBranchAdapter
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.setItemViewCacheSize(1)

        getVoucherDetail()

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgBarcode.setOnClickListener {
            val dialog: Dialog? = Dialog(this)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_show_qr)
            val imgVoucher = dialog.findViewById(R.id.imgVoucher) as ImageView
            val imgBtnClose = dialog.findViewById(R.id.imgBtnClose) as ImageView

            try {
                val bitmap =
                    QRCode.from(data.restaurant_promotion.code).withSize(200, 200)
                        .withCharset("UTF-8").bitmap()
                imgVoucher.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            imgBtnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        binding.btnTakeVoucher.setOnClickListener {
            takeVoucher(dataVoucher)
        }
    }

    private fun getVoucherDetail() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = String.format(
            "%s%s",
            "/api/customer-restaurant-vouchers/",
            dataVoucher.id
        )
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getVoucherDetail(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<VoucherDetailResponse> {
                override fun onComplete() {
                    WriteLog.d("COMPLETE", "Complete")
                }
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {
                    WriteLog.d("SUCBSCRIBE", "Subscribe")
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: VoucherDetailResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        data = response.data

                        if (data.restaurant_voucher_id == 0){
                            binding.imgBarcode.visibility = View.GONE
                            binding.btnTakeVoucher.visibility = View.VISIBLE
                        }else{
                            binding.imgBarcode.visibility = View.VISIBLE
                            binding.btnTakeVoucher.visibility = View.GONE
                        }

                        try {
                            val bitmap =
                                QRCode.from(data.restaurant_promotion.code).withSize(200, 200)
                                    .withCharset("UTF-8").bitmap()
                            binding.imgBarcode.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        binding.imgBanner.load(String.format(
                            "%s%s",
                            configNodeJs.api_ads,
                            data.restaurant_promotion.banner_image_url
                        )){
                            crossfade(true)
                            scale(Scale.FIT)
                            placeholder(R.drawable.ic_image_placeholder)
                            error(R.drawable.ic_image_placeholder)
                            size(500, 500)
                        }

                        binding.txtName.text = data.restaurant_promotion.name?:""
                        binding.txtExpiry.text = String.format("%s %s","Hạn sử dụng:", data.restaurant_promotion.to_date)
                        binding.txtTimeProviso.text = String.format("%s %s %s %s, %s %s%s %s%s","Thời gian sử dụng từ ngày", data.restaurant_promotion.from_date, "đến ngày", data.restaurant_promotion.to_date, "trong khung giờ từ", data.restaurant_promotion.from_hour, "h đến", data.restaurant_promotion.to_hour, "h.")
                        chooseBranchAdapter!!.setDataSource(data.restaurant_promotion.branch)
                        val dates = data.restaurant_promotion.to_date!!.substring(0,2).toInt() - data.restaurant_promotion.from_date!!.substring(0,2).toInt() + 1

                        binding.txtExpiryDay.text = String.format("%s %s %s","Hết hạn sử dụng sau",dates.toString(), "ngày")
                        binding.rlLoading.visibility = View.GONE
                    }else {
                        Toast.makeText(this@VoucherDetailActivity, response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }

    private fun takeVoucher(data: Voucher) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api/customer-restaurant-vouchers/${data.id}/accept"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getVoucher(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<VoucherResponse> {
                override fun onComplete() {
                    WriteLog.d("COMPLETE", "Complete")
                }
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {
                    WriteLog.d("SUCBSCRIBE", "Subscribe")
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: VoucherResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        binding.imgBarcode.visibility = View.VISIBLE
                        binding.btnTakeVoucher.visibility = View.GONE
                    }else {
                        Toast.makeText(this@VoucherDetailActivity, response.message, Toast.LENGTH_LONG)
                    }
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}