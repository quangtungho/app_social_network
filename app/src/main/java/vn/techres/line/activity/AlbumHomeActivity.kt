package vn.techres.line.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import vn.techres.line.R
import vn.techres.line.adapter.album.FolderAlbumAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.album.ClickFolderAlbum
import vn.techres.line.data.model.album.FolderAlbum
import vn.techres.line.data.model.album.params.AlbumParams
import vn.techres.line.data.model.album.params.AlbumRequest
import vn.techres.line.data.model.album.params.ShareAlbumParams
import vn.techres.line.data.model.album.params.UpdateNameAlbumParams
import vn.techres.line.data.model.album.request.ShareAlbumRequest
import vn.techres.line.data.model.album.request.UpdateNameAlbumRequest
import vn.techres.line.data.model.album.response.AlbumResponse
import vn.techres.line.data.model.album.response.CreateAlbumResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.databinding.ActivityAlbumHomeBinding
import vn.techres.line.databinding.BottomsheetAlbumBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.AlbumUtils
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*

class AlbumHomeActivity : BaseBindingActivity<ActivityAlbumHomeBinding>(),
    ClickFolderAlbum {
    var mAdapter: FolderAlbumAdapter? = null
    var mList = ArrayList<FolderAlbum>()
    var nameFolder = ""
    private var dialog: Dialog? = null
    var page = 1

    override val bindingInflater: (LayoutInflater) -> ActivityAlbumHomeBinding
        get() = ActivityAlbumHomeBinding::inflate

    override fun onSetBodyView() {
        binding.folderAlbum.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.folderAlbum.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        mAdapter = FolderAlbumAdapter(this)
        mAdapter!!.setClickFolder(this)
        this.let { AlbumUtils.configRecyclerViewFolder(binding.folderAlbum, mAdapter, it) }
        setListener()
        getFolder(page, this.let { CurrentUser.getCurrentUser(it).id })
    }

    fun setListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
        binding.addFolder.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog() {
        nameFolder = ""
        dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.dialog_create_folder)
        val edtNameFolder = dialog?.findViewById(R.id.edtNameFolder) as EditText
        val txtCancel = dialog?.findViewById(R.id.txtCancel) as TextView
        val txtSave = dialog?.findViewById(R.id.txtSave) as TextView
//        val imgSticker = dialog?.findViewById(R.id.imgSticker) as ImageView
        edtNameFolder.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                txtSave.isEnabled = s != ""
                nameFolder = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //COMMENT
            }

            override fun afterTextChanged(s: Editable?) {
                //COMMENT
            }
        })
        txtCancel.setOnClickListener {
            dialog?.dismiss()
        }

        txtSave.setOnClickListener {
            if (nameFolder.trim() != "")
                createFolder(nameFolder.trim(), this)
            else
                Toast.makeText(this, "Tạo không thành công", Toast.LENGTH_LONG).show()
            dialog?.dismiss()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    private fun getFolder(
        page: Int,
        userId: Int
    ) {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.http_method = AppConfig.GET
        baseRequest.request_url = "/api-upload/get-album?limit=1000&page=$page&user_id=$userId"

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getFolderAlbum(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<AlbumResponse> {
                override fun onComplete() {
                    //COMMENT
                }

                override fun onError(e: Throwable) {
                    //COMMENT
                }


                override fun onSubscribe(d: Disposable) {
                    //COMMENT
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: AlbumResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        mList.clear()
                        mList.addAll(response.data.list)
                        mAdapter?.setDataSource(mList)
                        binding.folderAlbum.scrollToPosition(0)
                    }
                }
            })
    }

    private fun createFolder(
        name: String,
        context: Context
    ) {
        val baseRequest = AlbumParams()
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api-upload/create-category-album"
        baseRequest.params = AlbumRequest(name)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .createFolderAlbum(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CreateAlbumResponse> {
                override fun onComplete() {
                    //COMMENT
                }

                override fun onError(e: Throwable) {
                    //COMMENT
                }


                override fun onSubscribe(d: Disposable) {
                    //COMMENT
                }

                @SuppressLint("ShowToast", "NotifyDataSetChanged")
                override fun onNext(response: CreateAlbumResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        WriteLog.d("uploading...", "success")
                        mList.add(0, response.data)
                        mAdapter!!.notifyItemInserted(0)
                        mAdapter!!.notifyDataSetChanged()
                        Toast.makeText(context, "Đã tạo album thành công", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            })
    }

    private fun showDialogUpdateName(categoryId: String) {
        nameFolder = ""
        dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.dialog_update_name_folder)
        val edtNameFolder = dialog?.findViewById(R.id.edtNameFolder) as EditText
        val txtCancel = dialog?.findViewById(R.id.txtCancel) as TextView
        val txtSave = dialog?.findViewById(R.id.txtSave) as TextView
//        val imgSticker = dialog?.findViewById(R.id.imgSticker) as ImageView
        edtNameFolder.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                txtSave.isEnabled = s != ""
                nameFolder = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //COMMENT
            }

            override fun afterTextChanged(s: Editable?) {
                //COMMENT
            }
        })
        txtCancel.setOnClickListener {
            dialog?.dismiss()
        }

        txtSave.setOnClickListener {
            updateNameFolder(nameFolder, categoryId)
            dialog?.dismiss()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    private fun updateNameFolder(
        name: String,
        categoryId: String
    ) {
        val params = UpdateNameAlbumParams()
        params.project_id = AppConfig.PROJECT_UPLOAD
        params.http_method = AppConfig.POST
        params.request_url = "/api-upload/rename-album/$categoryId"
        params.params = UpdateNameAlbumRequest(name)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .updateNameFolderAlbum(
                params
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<CreateAlbumResponse> {
                override fun onComplete() {
                    //COMMENT
                }

                override fun onError(e: Throwable) {
                    //COMMENT
                }


                override fun onSubscribe(d: Disposable) {
                    //COMMENT
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: CreateAlbumResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        WriteLog.d("uploading...", "success")
                        mList.forEachIndexed { index, folderAlbum ->
                            if (folderAlbum._id == response.data._id) {
                                mList[index] = response.data
                                mAdapter?.notifyItemChanged(index)
                                Toast.makeText(
                                    this@AlbumHomeActivity,
                                    "Sửa tên album thành công",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            })
    }

    private fun shareAlbum(
        pass: String,
        categoryId: String
    ) {
        val params = ShareAlbumParams()
        params.project_id = AppConfig.PROJECT_UPLOAD
        params.http_method = AppConfig.POST
        params.request_url = "/api-upload/share-link/$categoryId"
        params.params = ShareAlbumRequest(pass)
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .shareAlbum(
                params
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                    //COMMENT
                }

                override fun onError(e: Throwable) {
                    //COMMENT
                }

                override fun onSubscribe(d: Disposable) {
                    //COMMENT
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        Toast.makeText(
                            this@AlbumHomeActivity,
                            "Chia sẻ thành công",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        getFolder(
                            1,
                            this@AlbumHomeActivity.let { CurrentUser.getCurrentUser(it).id })
                    }
                }
            })
    }

    private fun cancelShareAlbum(
        categoryId: String
    ) {
        val params = BaseParams()
        params.project_id = AppConfig.PROJECT_UPLOAD
        params.http_method = AppConfig.GET
        params.request_url = "/api-upload/cancel-share-link/$categoryId"
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .cancelShareAlbum(
                params
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                    //COMMENT
                }

                override fun onError(e: Throwable) {
                    //COMMENT
                }

                override fun onSubscribe(d: Disposable) {
                    //COMMENT
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        Toast.makeText(this@AlbumHomeActivity, "Đã huỷ chia sẻ", Toast.LENGTH_SHORT)
                            .show()
                        getFolder(
                            1,
                            this@AlbumHomeActivity.let { CurrentUser.getCurrentUser(it).id })
                    }
                }
            })
    }

    private fun removeAlbum(
        categoryId: String
    ) {
        val baseRequest = BaseParams()
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api-upload/remove-album/$categoryId"

        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .removeFolderAlbum(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {
                   //COMMENT
                }

                override fun onError(e: Throwable) {
                   //COMMENT
                }


                override fun onSubscribe(d: Disposable) {
                  //COMMENT
                }

                @SuppressLint("ShowToast")
                override fun onNext(response: BaseResponse) {
                    mList.forEachIndexed { index, folderAlbum ->
                        if (folderAlbum._id == categoryId) {
                            mList.remove(folderAlbum)
                            mAdapter?.notifyItemRemoved(index)
                            mAdapter?.notifyItemChanged(index, mAdapter!!.itemCount)
                            return
                        }
                    }
                }
            })
    }

    private fun showDialogShareAlbum(categoryId: String) {
        nameFolder = ""
        dialog = Dialog(this)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCancelable(true)
        dialog?.setContentView(R.layout.dialog_input_pass_share_album)
        val edtPassword = dialog?.findViewById(R.id.edtPassword) as EditText
        val txtCancel = dialog?.findViewById(R.id.txtCancel) as TextView
        val txtSave = dialog?.findViewById(R.id.txtSave) as TextView
//        val imgSticker = dialog?.findViewById(R.id.imgSticker) as ImageView
        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                txtSave.isEnabled = s != ""
                nameFolder = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
               //COMMENT
            }

            override fun afterTextChanged(s: Editable?) {
              //COMMENT
            }
        })
        txtCancel.setOnClickListener {
            dialog?.dismiss()
        }

        txtSave.setOnClickListener {
            shareAlbum(nameFolder, categoryId)
            dialog?.dismiss()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.show()
    }

    override fun clickFolderAlbum(folderAlbum: FolderAlbum) {
        val intent = Intent(this, ImageFromAlbumActivity::class.java)
        intent.putExtra("folder_id", folderAlbum._id)
        intent.putExtra("folder_name", folderAlbum.folder_name)
        startActivity(intent)
        this.overridePendingTransition(
            R.anim.translate_from_right,
            R.anim.translate_to_right
        )
    }

    @SuppressLint("SetTextI18n")
    override fun longClickFolderAlbum(folderAlbum: FolderAlbum) {
        val bindingBottomSheetAlbum = BottomsheetAlbumBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(this, R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingBottomSheetAlbum.root)
        if (folderAlbum.is_share == 0)
            bindingBottomSheetAlbum.btnShare.text = "Chia sẻ album"
        else
            bindingBottomSheetAlbum.btnShare.text = "Huỷ chia sẻ album"
        bindingBottomSheetAlbum.btnDeleteAlbum.setOnClickListener {
            removeAlbum(folderAlbum._id)
            dialog.dismiss()
        }
        bindingBottomSheetAlbum.btnUpdateAlbum.setOnClickListener {
            showDialogUpdateName(folderAlbum._id)
            dialog.dismiss()
        }
        bindingBottomSheetAlbum.btnShare.setOnClickListener {
            if (folderAlbum.is_share == 0)
                showDialogShareAlbum(folderAlbum._id)
            else
                cancelShareAlbum(folderAlbum._id)
            dialog.dismiss()
        }
        dialog.show()
    }

}