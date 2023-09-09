package vn.techres.line.fragment.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alexbbb.uploadservice.MultipartUploadRequest
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.shashank.sony.fancytoastlib.FancyToast
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.socket.client.Socket
import org.json.JSONException
import org.json.JSONObject
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.adapter.chat.MemberTemporaryAdapter
import vn.techres.line.base.BaseBindingChatFragment
import vn.techres.line.data.model.chat.Group
import vn.techres.line.data.model.chat.request.MembersRequest
import vn.techres.line.data.model.chat.request.group.CreateGroupChatRequest
import vn.techres.line.data.model.chat.response.CreateGroupResponse
import vn.techres.line.data.model.chat.response.FileNodeJsResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.GroupParams
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentCreateProfileGroupBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.media.GlideEngine
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.io.File
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.util.*

open class CreateProfileGroupFragment : BaseBindingChatFragment<FragmentCreateProfileGroupBinding>(
    FragmentCreateProfileGroupBinding::inflate,
    true
) {
    private var members = ArrayList<MembersRequest>()
    private var adapter: MemberTemporaryAdapter? = null
    private var avatarGroup = ""
    private var useragent = ""
    private val application = TechResApplication()
    private var mSocket: Socket? = null
    private var nodeJs = ConfigNodeJs()
    private var user = User()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rcMember.setHasFixedSize(true)
        binding.rcMember.layoutManager =
            activity?.let { LinearLayoutManager(it, RecyclerView.VERTICAL, false) }
        adapter = MemberTemporaryAdapter(requireActivity().baseContext)
        binding.rcMember.adapter = adapter

        binding.tvTitleHomeHeader.text =
            requireActivity().baseContext.getString(R.string.create_group)
                .uppercase(Locale.getDefault())
        val isCheck = cacheManager.get(TechresEnum.CREATE_PROFILE_GROUP_CHAT.toString())
        if (!isCheck.isNullOrBlank()) {
            arguments?.let {
                members =
                    Gson().fromJson(it.getString(TechresEnum.MEMBER_CHAT.toString()), object :
                        TypeToken<ArrayList<MembersRequest>>() {}.type)
                adapter?.setDataSource(members)
            }
        }

        binding.rlAvatarGroup.setOnClickListener {
            PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .isCamera(true)
                .isWithVideoImage(true)
                .maxSelectNum(1)
                .minSelectNum(0)
                .maxVideoSelectNum(0)
                .selectionMode(PictureConfig.MULTIPLE)
                .isSingleDirectReturn(false)
                .isPreviewImage(true)
                .isPreviewVideo(true)
                .isOpenClickSound(true)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }
        binding.tvDone.setOnClickListener {
            binding.tvDone.isEnabled = false
            hideKeyboard()
            if ((binding.edNameGroup.text ?: "").isEmpty()) {
                val snackBar = Snackbar.make(
                    binding.root,
                    requireActivity().baseContext.getString(R.string.name_group_error),
                    Snackbar.LENGTH_LONG
                )
                snackBar.show()
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.tvDone.isEnabled = true
                }, 1500)
            } else
                if ((binding.edNameGroup.text ?: "").isNotEmpty()) {
                    val membersList = ArrayList<Int>()
                    members.forEach {
                        it.member_id?.let { it1 -> membersList.add(it1) }
                    }
                    createGroups(membersList)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.tvDone.isEnabled = true
                    }, 1500)
                }
        }
        binding.imgBack.setOnClickListener{
            onBackPress()
            activityChat?.supportFragmentManager?.popBackStack()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSocket = application.getSocketInstance(requireActivity().baseContext)
        user = CurrentUser.getCurrentUser(requireActivity().baseContext)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(requireActivity().baseContext)
        mSocket?.connect()
    }

//emit Socket

    private fun createGroup(members: ArrayList<Int>, group: Group) {
        val createGroupChatRequest = CreateGroupChatRequest()
        createGroupChatRequest.members = members
        createGroupChatRequest.group_id = group._id
        try {
            val jsonObject = JSONObject(Gson().toJson(createGroupChatRequest))
            WriteLog.d("NEW_GROUP_CREATE_ALO_LINE", jsonObject.toString())
            mSocket?.emit(TechResEnumChat.NEW_GROUP_CREATE_ALO_LINE.toString(), jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun uploadFile(file: File) {
        /*============ Upload file to server via service ===============*/
        val serverUrlString =
            String.format(
                "%s/api-upload/upload-file-by-group/%s/%s",
                nodeJs.api_ads,
                TechResEnumChat.TYPE_GROUP_FILE.toString(),
                file.name
            )

        val paramNameString = resources.getString(R.string.send_file)
        val uploadID = UUID.randomUUID().toString()
        try {
            MultipartUploadRequest(requireActivity(), uploadID, serverUrlString)
                .addFileToUpload(file.path, paramNameString)
                .addHeader(
                    resources.getString(R.string.Authorization),
                    user.nodeAccessToken
                )
                .setCustomUserAgent(useragent)
                .setMaxRetries(3)
                .startUpload()
            // these are the different exceptions that may be thrown
        } catch (exc: FileNotFoundException) {
            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        } catch (exc: IllegalArgumentException) {
            Toast.makeText(
                context,
                "Missing some arguments. " + exc.message,
                Toast.LENGTH_LONG
            ).show()
        } catch (exc: MalformedURLException) {
            Toast.makeText(context, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getAvatarGroup(nameFile: String) {
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.GET
        baseRequest.project_id = AppConfig.PROJECT_UPLOAD
        baseRequest.request_url = String.format(
            "%s%s%s%s%s%s%s%s",
            "/api-upload/get-link-file-by-group?type=",
            TechResEnumChat.TYPE_GROUP_FILE.toString(),
            "&name_file=",
            nameFile,
            "&width=",
            0,
            "&height=",
            0

        )
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getAvatarGroup(
                baseRequest
            )

            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<FileNodeJsResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())
                }


                override fun onSubscribe(d: Disposable) {}


                @SuppressLint("ShowToast")
                override fun onNext(response: FileNodeJsResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        avatarGroup = response.data.link_medium ?: ""
                    }

                }
            })

    }

    private fun createGroups(membersList: ArrayList<Int>) {
        closeKeyboard(binding.edNameGroup)

        val params = GroupParams()
        params.http_method = AppConfig.POST
        params.project_id = AppConfig.getProjectChat()
        params.request_url = "/api/groups/create"
        if (avatarGroup.isEmpty()) {
            params.params.avatar = ""
        } else {
            params.params.avatar = avatarGroup
        }

        params.params.name = binding.edNameGroup.text.toString()
        params.params.members = membersList
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createGroup(params)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CreateGroupResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())
                        Handler(Looper.getMainLooper()).postDelayed({
                            binding.tvDone.isEnabled = true
                        }, 1500)
                    }

                    override fun onSubscribe(d: Disposable) {}

                    @SuppressLint("ShowToast")
                    override fun onNext(response: CreateGroupResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            createGroup(membersList, response.data)
                            FancyToast.makeText(
                                requireActivity(),
                                requireActivity().resources.getString(R.string.create_group_success),
                                FancyToast.LENGTH_LONG, FancyToast.SUCCESS,
                                false
                            ).show()
//                            EventBus.getDefault().post(EventBusCreateGroup(response.data))
                            cacheManager.put(TechresEnum.CURRENT_PAGE.toString(), 3.toString())
                            activityChat?.finish()
                            activityChat?.overridePendingTransition(
                                R.anim.translate_from_right,
                                R.anim.translate_to_right
                            )
                            cacheManager.remove(TechresEnum.CREATE_PROFILE_GROUP_CHAT.toString())
                        } else {
                            Toast.makeText(requireActivity(), response.message, Toast.LENGTH_LONG)
                                .show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.tvDone.isEnabled = true
                            }, 1500)
                        }
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                val selectList = PictureSelector.obtainMultipleResult(data)
                val file = File(selectList[0].realPath)
                binding.imgGroupChat.setImageURI(Uri.fromFile(file))
                getAvatarGroup(file.name)
                uploadFile(file)
            }
        }
    }

    override fun onBackPress(): Boolean {
        val bundle = Bundle()
        bundle.putString(TechresEnum.MEMBER_CHAT.toString(), Gson().toJson(members))
        activityChat?.getOnRefreshFragment()?.onCallBack(bundle)
        return true
    }
}