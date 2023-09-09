package vn.techres.line.fragment.account

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.MediaSliderActivity
import vn.techres.line.adapter.account.ProfileImageAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.mediaprofile.MediaProfile
import vn.techres.line.data.model.mediaprofile.MediaProfileResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentProfileImageBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.ClickMediaProfile
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import vn.techres.line.view.GridSpacingDecoration
import kotlin.math.ceil

class ProfileImageFragment : BaseBindingFragment<FragmentProfileImageBinding>(FragmentProfileImageBinding::inflate), ClickMediaProfile {

    private var profileImageAdapter: ProfileImageAdapter? = null
    private var data = ArrayList<MediaProfile>()
    private var configJava = ConfigJava()
    private var user = User()
    private var idProfile = 0
    private var page = 1
    private var total = 0
    private var limit = 30
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity().baseContext, 3)
        binding.recyclerView.addItemDecoration(GridSpacingDecoration(7, 3))
        profileImageAdapter = ProfileImageAdapter(requireActivity().baseContext)
        binding.recyclerView.adapter = profileImageAdapter
        profileImageAdapter!!.setClickMediaProfile(this)

        arguments?.let {
            idProfile = it.getInt(TechresEnum.ID_USER.toString())

        }

        getImage(idProfile, page)

        binding.imgBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            var y = 0
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                y = dy
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    if (y > 0) {
                        if (page <= ceil((total / limit).toDouble())) {
                            page++
                            getImage(idProfile, page)
                        }
                    } else {
                        y = 0
                    }
                }
            }
        })
    }

    private fun getImage(userId: Int, page: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/customer-media-album-contents?media_type=1&customer_id=$userId&customer_media_album_id=-1&page=$page&limit=$limit"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getMediaProfile(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MediaProfileResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: MediaProfileResponse) {

                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val mediaProfile = response.data.list
                        total = response.data.total_record?:0
                        limit = response.data.limit?:0
                        if (mediaProfile.size == 0) {
                            binding.lnEmptyNotify.visibility = View.VISIBLE
                        }

                        if (data.size == 0) {
                            data = mediaProfile
                        } else {
                            data.addAll(data.size, mediaProfile)
                        }



                        profileImageAdapter!!.setDataSource(data)

                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                }
            })
    }

    override fun onClickMedia(url: ArrayList<MediaProfile>, position: Int) {
        val configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        val list = ArrayList<String>()
        url.forEach {
            list.add(String.format("%s%s", configNodeJs.api_ads, it.url.original))
        }
        val intent = Intent(requireActivity(), MediaSliderActivity::class.java)
        intent.putExtra(TechresEnum.DATA_MEDIA.toString(), Gson().toJson(list))
        intent.putExtra(TechresEnum.POSITION_MEDIA.toString(), position)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.translate_from_right, R.anim.translate_to_right)

    }

    override fun onLongClickMedia(id: Int, position: Int) {
        bottomSheetClickMedia(id, position)

    }

    private fun deleteImageMedia(id: Int, position: Int) {
        val baseRequest = BaseParams()
        baseRequest.http_method = 1
        baseRequest.request_url = "/api/customer-media-album-contents/$id/delete"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .deleteImageMedia(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        data.removeAt(position)
                        profileImageAdapter!!.notifyDataSetChanged()
                        Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                }
            })
    }

    private fun bottomSheetClickMedia(id: Int, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_click_media)
        bottomSheetDialog.setCancelable(true)
        val lnDelete = bottomSheetDialog.findViewById<LinearLayout>(R.id.lnDelete)

        lnDelete!!.setOnClickListener {
            deleteImageMedia(id, position)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    override fun onBackPress() : Boolean {
        return false
    }
}