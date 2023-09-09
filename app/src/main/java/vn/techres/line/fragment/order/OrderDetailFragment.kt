package vn.techres.line.fragment.order

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.bill.BillAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.OrderCustomer
import vn.techres.line.data.model.OrderDetailBill
import vn.techres.line.data.model.bill.BillTable
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BillTableResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.databinding.FragmentOrderDetailBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.Currency
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class OrderDetailFragment : BaseBindingFragment<FragmentOrderDetailBinding>(FragmentOrderDetailBinding::inflate) {
    private val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var dataOrderDetail = OrderCustomer()

    private var listBill = ArrayList<OrderDetailBill>()

    private var adapterBill: BillAdapter? = null

    private var data = BillTable()
    private var idOrder: Int? = null
    private var configJava = ConfigJava()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(mainActivity!!.baseContext)

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
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")
        binding.header.tvTitleHomeHeader.text = mainActivity!!.baseContext.getString(R.string.order_detail)
        binding.rcOrderDetail.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapterBill = BillAdapter(mainActivity!!.baseContext)
        binding.rcOrderDetail.adapter = adapterBill
        arguments?.let {
            idOrder = it.getInt(TechresEnum.KEY_ID_ORDER_CUSTOMER.toString())
            if (dataOrderDetail.order_details.size == 0) {
                getBillTable(idOrder!!)
            } else {
                adapterBill?.setDataSource(listBill)
            }
        }
       
    }
    private fun setBill(data : BillTable){
        context?.let {
            binding.headerPayment.imgPayment.show()
            binding.headerPayment.tvReceiptOrderNumber.text = data.id.toString()
            binding.headerPayment.tvReceiptDate.text = data.payment_day
            binding.headerPayment.tvReceiptTable.text = data.table_name
            binding.headerPayment.tvReceiptEmployee.text = data.employee_name
            binding.headerPayment.tvCustomerName.text = data.customer_name
            binding.headerPayment.tvCustomerPhoneNumber.text = data.customer_phone
            binding.tvPercentDiscount.text =
                String.format("(%s%s", data.discount_percent.toString(), "%)")
            binding.tvPercentVat.text = String.format("(%s%s", data.vat.toString(), "%)")
            binding.tvDiscount.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(data.discount_amount!!.toFloat()),
                requireActivity().resources.getString(R.string.denominations)
            )
            binding.tvTotalVat.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(data.vat_amount!!.toFloat()),
                requireActivity().resources.getString(R.string.denominations)
            )
            binding.tvTotalPay.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(data.total_amount!!.toFloat()),
                requireActivity().resources.getString(R.string.denominations)
            )
            binding.tvTotalMoney.text = String.format(
                "%s %s",
                Currency.formatCurrencyDecimal(data.amount!!.toFloat()),
                requireActivity().resources.getString(R.string.denominations)
            )
            binding.tvPointPurchase.text = String.format(
                "%s %s",
                data.membership_point_added,
                requireActivity().resources.getString(R.string.point_mini)
            )
            binding.tvTotalPointUsed.text = String.format(
                "%s %s",
                data.membership_total_point_used,
                requireActivity().resources.getString(R.string.point_mini)
            )
        }

    }
    private fun getBillTable(idTable: Int) {
        mainActivity?.setLoading(true)
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url = "/api/orders/$idTable"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getBillTable(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BillTableResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                    mainActivity?.setLoading(false)
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BillTableResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        data = response.data

                        setBill(data)
                        listBill = data.order_details
                        adapterBill?.setDataSource(listBill)
                    } else Toast.makeText(
                        mainActivity!!,
                        response.message,
                        Toast.LENGTH_LONG
                    ).show()
                    mainActivity?.setLoading(false)
                }
            })
    }

    override fun onBackPress() : Boolean{
        return true
    }

}
