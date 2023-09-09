package vn.techres.line.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.gift.GiftAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.BranchGift
import vn.techres.line.data.model.gift.Gift
import vn.techres.line.data.model.gift.GiftListResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BranchGiftResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityGiftBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.gift.GiftHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class GiftActivity : BaseBindingActivity<ActivityGiftBinding>(), GiftHandler {
    private var user = User()
    private var nodeJs = ConfigNodeJs()
    private var giftsNotUsedAdapter: GiftAdapter? = null
    private var giftsUsedAdapter: GiftAdapter? = null
    private var giftsExpireAdapter: GiftAdapter? = null
    private var dataGift = ArrayList<Gift>()
    private var numGiftsNotUsed = 0
    private var numGiftsUsed = 0
    private var numGiftsExpire = 0
    private var branchList = ArrayList<BranchGift>()
    private var idBranchExchange = 0
    private var position = 0
    var listSpinnerString: java.util.ArrayList<String> = java.util.ArrayList()
    override val bindingInflater: (LayoutInflater) -> ActivityGiftBinding
        get() = ActivityGiftBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        giftsNotUsedAdapter = GiftAdapter(this)
        giftsUsedAdapter = GiftAdapter(this)
        giftsExpireAdapter = GiftAdapter(this)
        binding.recyclerViewNotUsed.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewUsed.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewExpire.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewNotUsed.adapter = giftsNotUsedAdapter
        binding.recyclerViewUsed.adapter = giftsUsedAdapter
        binding.recyclerViewExpire.adapter = giftsExpireAdapter
        giftsNotUsedAdapter!!.setClickGift(this)
        giftsUsedAdapter!!.setClickGift(this)
        giftsExpireAdapter!!.setClickGift(this)

        getAPIBranch()
        setListener()
        // binding.spinnerListTrademark.onItemSelectedListener = null
    }

    private fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
//        var position = 0
        binding.btnNotUsed.setOnClickListener {
            position = 0
            checkNull(numGiftsNotUsed)
            binding.recyclerViewExpire.visibility = View.GONE
            binding.recyclerViewUsed.visibility = View.GONE
            binding.recyclerViewNotUsed.visibility = View.VISIBLE
            binding.btnNotUsed.setBackgroundResource(R.drawable.border_orange_50dp)
            binding.btnNotUsed.setTextColor(resources.getColor(R.color.white))
            binding.btnUsed.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnUsed.setTextColor(resources.getColor(R.color.main_bg))
            binding.btnExpire.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnExpire.setTextColor(resources.getColor(R.color.main_bg))
        }
        binding.btnUsed.setOnClickListener {
            position = 1
            checkNull(numGiftsUsed)
            binding.recyclerViewUsed.visibility = View.VISIBLE
            binding.recyclerViewNotUsed.visibility = View.GONE
            binding.recyclerViewExpire.visibility = View.GONE
            binding.btnNotUsed.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnNotUsed.setTextColor(resources.getColor(R.color.main_bg))
            binding.btnExpire.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnExpire.setTextColor(resources.getColor(R.color.main_bg))
            binding.btnUsed.setBackgroundResource(R.drawable.border_orange_50dp)
            binding.btnUsed.setTextColor(resources.getColor(R.color.white))
        }

        binding.btnExpire.setOnClickListener {
            position = 2
            checkNull(numGiftsExpire)
            binding.recyclerViewExpire.visibility = View.VISIBLE
            binding.recyclerViewNotUsed.visibility = View.GONE
            binding.recyclerViewUsed.visibility = View.GONE
            binding.btnNotUsed.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnNotUsed.setTextColor(resources.getColor(R.color.main_bg))
            binding.btnUsed.setBackgroundResource(R.drawable.stroke_corners_orange_50dp)
            binding.btnUsed.setTextColor(resources.getColor(R.color.main_bg))
            binding.btnExpire.setBackgroundResource(R.drawable.border_orange_50dp)
            binding.btnExpire.setTextColor(resources.getColor(R.color.white))
        }


    }

    fun dataSpinner() {
        listSpinnerString.add(0, "Tất cả thương hiệu")
        for (item in branchList.indices) {
            binding.spinnerData.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                listSpinnerString
            )
        }
        binding.spinnerData.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    idBranchExchange = -1
                    getGiftsNotUsed(idBranchExchange)
                    getGiftsUsed(idBranchExchange)
                    getGiftsExpire(idBranchExchange)
                } else {
                    for (i in branchList.indices) {
                        if (listSpinnerString[p2] == branchList[i].name) {
                            idBranchExchange = branchList[i].id
                            getGiftsNotUsed(idBranchExchange)
                            getGiftsUsed(idBranchExchange)
                            getGiftsExpire(idBranchExchange)
                            break

                        }
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun getAPIBranch() {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/restaurant-brand/customer-gift"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getListBranchGift(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BranchGiftResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: BranchGiftResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        branchList = response.data
                        listSpinnerString.clear()
                        for (item in branchList.indices) {
                            listSpinnerString.add(branchList[item].name)
                            idBranchExchange = branchList[item].id
                        }
                        dataSpinner()
                    }
                }
            })
    }

    private fun getGiftsNotUsed(branchID: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/customer-gifts?restaurant_id=-1&restaurant_brand_id=${branchID}&customer_id=${user.id}&restaurant_gift_id=-1&restaurant_gift_object_value=-1&restaurant_gift_object_quantity=-1&restaurant_gift_type=-1&is_opened=1&is_expired=0&from&to&key_search&is_used=0"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getGiftList(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GiftListResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: GiftListResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataGift = response.data.list
                        if (response.data.list != null) {
                            numGiftsNotUsed = response.data.list.size
                            giftsNotUsedAdapter!!.setDataSource(dataGift)
                        } else
                            numGiftsNotUsed = 0
                        checkNull(numGiftsNotUsed)
                    }
                }
            })
    }

    private fun getGiftsUsed(branchID: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/customer-gifts?restaurant_id=-1&restaurant_brand_id=${branchID}&customer_id=${user.id}&restaurant_gift_id=-1&restaurant_gift_object_value=-1&restaurant_gift_object_quantity=-1&restaurant_gift_type=-1&is_opened=1&is_expired=0&from&to&key_search&is_used=1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getGiftList(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GiftListResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: GiftListResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataGift = response.data.list
                        numGiftsUsed = response.data.list.size
                        giftsUsedAdapter!!.setDataSource(dataGift)
                    }
                }
            })
    }

    private fun getGiftsExpire(branchID: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url =
            "/api/customer-gifts?restaurant_id=-1&restaurant_brand_id=${branchID}&customer_id=${user.id}&restaurant_gift_id=-1&restaurant_gift_object_value=-1&restaurant_gift_object_quantity=-1&restaurant_gift_type=-1&is_opened=1&is_expired=1&from&to&key_search"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        ).getGiftList(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<GiftListResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: GiftListResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        dataGift = response.data.list
                        numGiftsExpire = response.data.list.size
                        giftsExpireAdapter!!.setDataSource(dataGift)
                    }
                }
            })
    }

    private fun checkNull(count: Int) {
        if (count == 0) {
            binding.lnEmptyGif.visibility = View.VISIBLE
        } else {
            binding.lnEmptyGif.visibility = View.GONE
        }
    }


    override fun onGetGift(gift: Gift) {
        val intent = Intent(this, GiftDetailActivity::class.java)
        intent.putExtra(TechresEnum.DATA_GIFT_DETAIL.toString(), Gson().toJson(gift))
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

}