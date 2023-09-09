package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import vn.techres.data.line.model.PostReview
import vn.techres.line.R
import vn.techres.line.adapter.newsfeed.DraftsPostAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.params.*
import vn.techres.line.data.model.request.RemovePostDraftRequest
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.response.DataDraftResponse
import vn.techres.line.data.model.response.DataPostedResponse
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityDetailPostDraftsBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.interfaces.draft.DraftHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class DetailPostDraftsActivity : BaseBindingActivity<ActivityDetailPostDraftsBinding>(),
    DraftHandler {
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var adapterDraftsPost: DraftsPostAdapter? = null
    private var list = ArrayList<PostReview>()
    private var positionEdit: Int = 0

    override val bindingInflater: (LayoutInflater) -> ActivityDetailPostDraftsBinding
        get() = ActivityDetailPostDraftsBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

        adapterDraftsPost = DraftsPostAdapter(this)
        binding.rcDraftsPost.adapter = adapterDraftsPost
        binding.rcDraftsPost.layoutManager =
            LinearLayoutManager(this@DetailPostDraftsActivity, RecyclerView.VERTICAL, false)
        binding.rcDraftsPost.setHasFixedSize(true)
        binding.rcDraftsPost.setItemViewCacheSize(1)
        adapterDraftsPost!!.setClickGift(this)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        setData()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    //Reload lai list bai viet nhap cach 1
//    override fun onResume() {
//        super.onResume()
//        list.clear()
//        setData()
//    }
    //Reload lai list bai viet nhap cach 2
    @Subscribe
    fun eventReload(data: PostReview) {
        list.add(positionEdit, data)
        list.removeAt(positionEdit + 1)
        adapterDraftsPost!!.setDataSource(list)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    private fun setData() {
        if (list.size == 0) {
            getDraftsPost()
        } else {
            adapterDraftsPost!!.setDataSource(list)
        }
    }

    private fun getDraftsPost() {
        val draftPostParams = DraftPostParams()
        draftPostParams.http_method = 0
        draftPostParams.is_production_mode = 1
        draftPostParams.project_id = 1485
        draftPostParams.request_url = "/api/branch-reviews"

        val draftPostRequest = DraftPostRequest()
        draftPostRequest.customer_id = CurrentUser.getCurrentUser(this).id
        draftPostParams.params = draftPostRequest

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getDraftsPost(draftPostParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DataDraftResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: DataDraftResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        list = response.data.list
                        list.let { adapterDraftsPost?.setDataSource(list) }
                    } else Toast.makeText(
                        this@DetailPostDraftsActivity,
                        resources.getString(R.string.error_sever_post),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })
    }

    override fun onPostDraft(draf: PostReview, position: Int) {
        postDraft(draf, position)

    }

    private fun postDraft(draf: PostReview, position: Int) {
        val reviewBranchParams = ReviewBranchParams()
        reviewBranchParams.http_method = 1
        reviewBranchParams.project_id = AppConfig.PROJECT_CHAT
        reviewBranchParams.request_url = "/api/branch-reviews"
        reviewBranchParams.params.content = draf.content
        reviewBranchParams.params.rate = -1.0
        reviewBranchParams.params.branch_id = -1
        reviewBranchParams.params.title = draf.title
        reviewBranchParams.params.media_contents = draf.media_contents
        reviewBranchParams.params.service_rate = -1f
        reviewBranchParams.params.food_rate = -1f
        reviewBranchParams.params.price_rate = -1f
        reviewBranchParams.params.space_rate = -1f
        reviewBranchParams.params.hygiene_rate = -1f
        reviewBranchParams.params.branch_review_status = 1
        reviewBranchParams.params.url = draf.url
        reviewBranchParams.params.url_json_content.url = draf.url_json_content.url
        reviewBranchParams.params.url_json_content.title = draf.url_json_content.title
        reviewBranchParams.params.url_json_content.description = draf.url_json_content.description
        reviewBranchParams.params.url_json_content.image = draf.url_json_content.image
        reviewBranchParams.params.url_json_content.canonical_url =
            draf.url_json_content.canonical_url

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .createReviewBranch(reviewBranchParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<DataPostedResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("NotifyDataSetChanged")
                override fun onNext(response: DataPostedResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                            removePostDraft(draf._id!!,position)
                        }
                        else -> {
                            binding.lnUpload.visibility = View.GONE
                        }
                    }
                }
            })
    }

    private fun removePostDraft(_id: String, position: Int) {
        val removePostDraftParams = RemovePostDraftParams()
        removePostDraftParams.http_method = 1
        removePostDraftParams.project_id = AppConfig.PROJECT_CHAT
        removePostDraftParams.request_url = String.format("/api/branch-reviews/%s/remove", _id)

        val removePostDraftRequest = RemovePostDraftRequest()
        removePostDraftRequest._id = _id
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .removePostDraft(removePostDraftParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("NotifyDataSetChanged")
                override fun onNext(response: BaseResponse) {
                    when (response.status) {
                        AppConfig.SUCCESS_CODE -> {
                            list.removeAt(position)
                            adapterDraftsPost!!.notifyDataSetChanged()
                            binding.lnUpload.visibility = View.GONE
                            Toast.makeText(
                                this@DetailPostDraftsActivity,
                                resources.getString(R.string.success_review_restaurant),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@DetailPostDraftsActivity,
                                resources.getString(R.string.error_sever_post),
                                Toast.LENGTH_LONG
                            )
                                .show()
                            binding.lnUpload.visibility = View.GONE
                        }
                    }
                }
            })
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onEditDraft(draf: PostReview, position: Int, type: Int) {
        positionEdit = position
        val intent = Intent(this, EditMyPostActivity::class.java)
        intent.putExtra(TechresEnum.ID_REVIEW.toString(), draf._id ?: "")
        intent.putExtra("Type Post", 0)
        startActivity(intent)
        overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDeleteDraft(draf: PostReview, position: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_delete_post_new_feed)

        val btnDeleteNow = dialog.findViewById(R.id.btnDeleteNow) as Button
        val btnBack = dialog.findViewById(R.id.btnBack) as Button

        btnDeleteNow.setOnClickListener {
            val itemId = draf._id
            val deleteDraft = BaseParams()
            deleteDraft.http_method = 1
            deleteDraft.is_production_mode = 1
            deleteDraft.project_id = 1485
            deleteDraft.request_url = "/api/branch-reviews/$itemId/remove"

            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .deleteDraftsPost(deleteDraft)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                    }

                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        when (response.status) {
                            AppConfig.SUCCESS_CODE -> {
                                binding.lnUpload.visibility = View.GONE
                                Toast.makeText(
                                    this@DetailPostDraftsActivity,
                                    resources.getString(R.string.success_delete_draft),
                                    Toast.LENGTH_LONG
                                ).show()

                                list.removeAt(position)
                                adapterDraftsPost!!.notifyDataSetChanged()
//                                adapterDraftsPost?.setDataSource(list)
//                                setData()

                            }
                            else -> {
                                Toast.makeText(
                                    this@DetailPostDraftsActivity,
                                    resources.getString(R.string.error_sever_post),
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                binding.lnUpload.visibility = View.GONE
                            }
                        }
                    }
                })

            dialog.dismiss()
//          onBackPressed()
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

}