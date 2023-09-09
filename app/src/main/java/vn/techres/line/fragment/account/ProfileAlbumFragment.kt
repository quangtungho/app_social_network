package vn.techres.line.fragment.account

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.activity.ProfileActivity
import vn.techres.line.adapter.account.ProfileAlbumAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.mediaprofile.MediaAlbumResponse
import vn.techres.line.data.model.mediaprofile.MediaProfile
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.CreateAlbumParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.ConfigJava
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentProfileAlbumBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigJava
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.interfaces.CreateAlbumHandler
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService

class ProfileAlbumFragment : BaseBindingFragment<FragmentProfileAlbumBinding>(FragmentProfileAlbumBinding::inflate), CreateAlbumHandler {
    private val profileActivity: ProfileActivity?
        get() = activity as ProfileActivity?
    private var configJava = ConfigJava()
    private var user = User()
    private var profileAlbumAdapter: ProfileAlbumAdapter? = null
    private var mediaProfile = ArrayList<MediaProfile>()
    private var dialog: Dialog? = null
    private var idProfile = 0
    private var name = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configJava = CurrentConfigJava.getConfigJava(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.recyclerView.layoutManager = GridLayoutManager(requireActivity().baseContext, 2)
        profileAlbumAdapter = ProfileAlbumAdapter(requireActivity().baseContext)
        binding.recyclerView.adapter = profileAlbumAdapter
        profileAlbumAdapter?.setClickAlbum(this)

        arguments?.let {
            idProfile = it.getInt(TechresEnum.ID_USER.toString())
            name = it.getString(TechresEnum.USERNAME.toString()).toString()
        }
        binding.tvName.text = name
        getListAlbum()

        binding.imgBack.setOnClickListener {
            profileActivity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun getListAlbum() {
        val baseRequest = BaseParams()
        baseRequest.http_method = 0
        baseRequest.request_url =
            "/api/customer-media-albums?customer_id=$idProfile&status=-1&limit=20&page=1"
        ServiceFactory.createRetrofitService(
            TechResService::class.java
        )
            .getListAlbum(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<MediaAlbumResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: MediaAlbumResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        mediaProfile = response.data.list
                        mediaProfile.add(0, MediaProfile())
                        profileAlbumAdapter!!.setDataSource(mediaProfile)

                    } else Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                }
            })
    }

    private fun dialogCreateAlbum() {
        dialog = this.context?.let { Dialog(it) }
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.dialog_create_album)

        val edtAlbumName = dialog?.findViewById(R.id.edtAlbumName) as EditText
        val btnCreateAlbum = dialog?.findViewById(R.id.btnCreateAlbum) as Button

        btnCreateAlbum.setOnClickListener {
            createAlbum(edtAlbumName.text.toString())
            dialog!!.dismiss()
        }

        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    private fun createAlbum(albumName: String) {
        val params = CreateAlbumParams()
        params.http_method = 1
        params.request_url = "/api/customer-media-albums/create"

        params.params.name = albumName
        params.params.sort = 1
        params.let {
            ServiceFactory.createRetrofitService(
                TechResService::class.java
            )
                .createAlbum(params)
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
                                mediaProfile.clear()
                                getListAlbum()
                                Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                            else -> {
                                Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                })
        }

    }

    override fun onClick(position: Int) {
        dialogCreateAlbum()
    }

    override fun onOneAlbum(position: Int, id: Int) {
        val bundle = Bundle()
        bundle.putInt(TechresEnum.ID_USER.toString(), id)
        bundle.putString(TechresEnum.USERNAME.toString(), name)
    }

    override fun onBackPress() : Boolean {
        return false
    }
}