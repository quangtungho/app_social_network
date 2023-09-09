package vn.techres.line.fragment.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.ContactDeviceActivity
import vn.techres.line.adapter.contact.ChooseContactAdapter
import vn.techres.line.adapter.contact.ContactDeviceChatAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.contact.ContactChat
import vn.techres.line.data.model.contact.ContactData
import vn.techres.line.data.model.contact.ContactDevice
import vn.techres.line.data.model.contact.ContactNodeChat
import vn.techres.line.data.model.contact.response.ContactChatResponse
import vn.techres.line.data.model.params.BaseParams
import vn.techres.line.data.model.params.ContactChatParams
import vn.techres.line.data.model.params.ContactParams
import vn.techres.line.data.model.response.BaseResponse
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentContactDeviceChatBinding
import vn.techres.line.helper.AppConfig
import vn.techres.line.helper.utils.ContactUtils.retrieveAllContacts
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.MultiplePermission.shouldAskPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.chat.ContactDeviceChatHandler
import vn.techres.line.interfaces.contact.ContactHandler
import vn.techres.line.interfaces.contact.ContactNumberHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import vn.techres.line.services.ServiceFactory
import vn.techres.line.services.TechResService
import java.util.*
import kotlin.collections.ArrayList

class ContactDeviceChatFragment :
    BaseBindingFragment<FragmentContactDeviceChatBinding>(
        FragmentContactDeviceChatBinding::inflate
    ),
    ContactDeviceChatHandler, ContactHandler {

    private val contactDeviceActivity: ContactDeviceActivity?
        get() = activity as ContactDeviceActivity?

    private lateinit var contactNumberHandler: ContactNumberHandler
    private var listContacts = ArrayList<ContactNodeChat>()
    private var listContactsChoose = ArrayList<ContactNodeChat>()
    private lateinit var adapter: ContactDeviceChatAdapter
    private lateinit var adapterChoose: ChooseContactAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var layoutManagerChoose: LinearLayoutManager
    private var number = 0
    private var page = 0
    private var user = User()

    private val listColor = arrayOf(
        R.color.brown,
        R.color.sienna,
        R.color.indian_red,
        R.color.slate_blue,
        R.color.royal_blue,
        R.color.DarkGoldenrod3,
        R.color.medium_blue,
        R.color.my_text_primary,
        R.color.goldenrod4,
        R.color.DarkOrchid4
    )

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    )

    private val requiredPermissionsCode = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        contactDeviceActivity?.setContactHandler(this)

        layoutManager = LinearLayoutManager(
            requireActivity().baseContext,
            RecyclerView.VERTICAL,
            false
        )
        layoutManagerChoose = LinearLayoutManager(
            requireActivity().baseContext,
            RecyclerView.HORIZONTAL,
            false
        )
        adapter = ContactDeviceChatAdapter(requireActivity().baseContext)
        adapterChoose = ChooseContactAdapter(requireActivity().baseContext)
        adapter.setContactDeviceChatHandler(this)
        adapterChoose.setContactDeviceChatHandler(this)
        binding.rcContactDevice.layoutManager = layoutManager
        binding.rcChooseContact.layoutManager = layoutManagerChoose
        binding.rcContactDevice.adapter = adapter
        binding.rcChooseContact.adapter = adapterChoose

        binding.rcContactDevice.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.rcContactDevice.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false
        binding.rcChooseContact.itemAnimator?.changeDuration = 0
        (Objects.requireNonNull(binding.rcChooseContact.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations =
            false

        adapterChoose.setDataSource(listContactsChoose)
        adapter.setDataSource(listContacts)

        if (!requireActivity().shouldAskPermissions(requiredPermissions)) {
            binding.lnPermission.hide()
            binding.rcContactDevice.show()
            binding.pbLoading.show()
            Handler(Looper.getMainLooper()).postDelayed({
                setData(1)
            }, 200)
        } else {
            binding.lnPermission.show()
            binding.rcContactDevice.hide()
        }

        binding.imgSend.setOnClickListener {
            val list = ArrayList<ContactDevice>()
            listContactsChoose.forEach { contact ->
                list.add(
                    ContactDevice(
                        0,
                        contact.full_name,
                        contact.phone,
                        if (contact.avatar != null) contact.avatar?.medium else "",
                        false
                    )
                )
            }
            EventBus.getDefault().post(list)
            contactDeviceActivity?.onBackPressed()
        }

        binding.btnPermission.setOnClickListener {
            requestMultiplePermissionWithListener()
        }
        attachGroupOnScrollListener()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contactNumberHandler = context as ContactNumberHandler
    }
    /**
     * Permission
     * */
    private fun requestMultiplePermissionWithListener() {
        requestPermissions(requiredPermissions, requiredPermissionsCode, object :
            RequestPermissionListener {

            override fun onCallPermissionFirst(
                namePermission: String,
                requestPermission: () -> Unit
            ) {
                requestPermission.invoke()
            }

            override fun onPermissionRationaleShouldBeShown(
                namePermission: String,
                requestPermission: () -> Unit
            ) {
                requestPermission.invoke()
            }

            override fun onPermissionPermanentlyDenied(
                namePermission: String,
                openAppSetting: () -> Unit
            ) {
                dialogPermission(requireActivity().resources.getString(R.string.title_permission_contact),
                    String.format(
                        requireActivity().resources.getString(R.string.note_permission_denied),
                        requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    requireActivity().resources.getString(R.string.step_two_open_permission),
                    R.drawable.ic_phone_book, "", 0, object : DialogPermissionHandler {
                        override fun confirmDialog() {
                            openAppSetting.invoke()
                        }

                        override fun dismissDialog() {
                        }
                    }
                )
            }

            override fun onPermissionGranted() {
                binding.lnPermission.hide()
                binding.rcContactDevice.show()
                setData(1)
            }

        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handleOnRequestPermissionResult(
            requiredPermissionsCode,
            requestCode,
            permissions,
            grantResults,
            object : PermissionResultListener {
                override fun onPermissionRationaleShouldBeShown(
                    namePermission: String,
                    requestPermission: () -> Unit
                ) {

                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_permission_contact),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission),
                        R.drawable.ic_phone_book, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                requestPermission.invoke()
                            }

                            override fun dismissDialog() {
                            }

                        }
                    )
                }

                override fun onPermissionPermanentlyDenied(
                    namePermission: String,
                    openAppSetting: () -> Unit
                ) {
                    dialogPermission(
                        requireActivity().resources.getString(R.string.title_permission_contact),
                        String.format(
                            requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission),
                        R.drawable.ic_phone_book, "", 0, object : DialogPermissionHandler {
                            override fun confirmDialog() {
                                openAppSetting.invoke()
                            }

                            override fun dismissDialog() {
                            }
                        }
                    )
                }

                override fun onPermissionGranted() {
                    binding.lnPermission.hide()
                    binding.rcContactDevice.show()
                    setData(1)
                }
            })
    }

    private fun attachGroupOnScrollListener() {
        binding.rcContactDevice.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastPosition =
                    (Objects.requireNonNull(binding.rcContactDevice.layoutManager) as LinearLayoutManager).findLastVisibleItemPosition()
                if(lastPosition >= listContacts.size - 10 && listContacts.size >= (50 * page)){

                }
//                if (firstVisibleItem + visibleItemCount >= totalItemCount - 5 && listContacts.size >= 30) {
//                    binding.rcContactDevice.removeOnScrollListener(this)
//                    page++
//                    setData(page)
//                }
            }

        })
    }

    private fun setData(page: Int) {
        requireActivity().retrieveAllContacts("", true).let {
            WriteLog.d("retrieveAllContacts", Gson().toJson(it))
            getContact(getContactDevice(it))
            updatePhoneUser(getContactDevice(it))
        }
    }

    private fun getContactDevice(
        contactDevice: ArrayList<ContactData>
    ): ArrayList<ContactChat> {
        val array = ArrayList<ContactChat>()
        contactDevice.forEach {
            array.add(ContactChat(it.contact_id?.toInt(), it.full_name, it.phone?.get(0), it.avatar))
        }
        return array
    }

    private fun getContact(array: ArrayList<ContactChat>) {
        val baseRequest = ContactChatParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.project_id = AppConfig.getProjectChat()
        baseRequest.request_url = "/api/customer-phones/contact-phone-system"
        baseRequest.params.list_user = array
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .getContactChat(
                baseRequest
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<ContactChatResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    binding.pbLoading.hide()
                    WriteLog.d("ERROR", e.message.toString())
                }

                override fun onSubscribe(d: Disposable) {}

                @SuppressLint("ShowToast")
                override fun onNext(response: ContactChatResponse) {
                    if (response.status == AppConfig.SUCCESS_CODE) {
                        val contact = response.data
                        contact.map { it.color = listColor.random() }
                        adapter.setDataSource(contact)
                    }
                    binding.pbLoading.hide()
                }
            })
    }

    private fun setViewChoose(number : String){
        binding.tvNumberChoose.apply {
            if(number.length > 1){
                setPadding(dpToPx(7), dpToPx(5), dpToPx(7), dpToPx(5))
            }else{
                setPadding(dpToPx(10), dpToPx(5), dpToPx(10), dpToPx(5))
            }
            text = number
        }
    }

    private fun updatePhoneUser(array: ArrayList<ContactChat>){
        val arrayList = array.map { it.phone ?: "" }.toMutableList()
        val params = ContactParams()
        params.http_method = AppConfig.POST
        params.request_url = "/api/customer-phones/create"
        params.project_id = AppConfig.PROJECT_CHAT
        params.params.list_phone = arrayList
        params.let {
            ServiceFactory.createRetrofitServiceNode(
                TechResService::class.java
            )
                .createContact(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BaseResponse> {
                    override fun onComplete() {}
                    override fun onError(e: Throwable) {
                        WriteLog.d("ERROR", e.message.toString())

                    }
                    override fun onSubscribe(d: Disposable) {}
                    override fun onNext(response: BaseResponse) {
                        if (response.status == AppConfig.SUCCESS_CODE) {
                            createContactsTogether()
                        }
                    }
                })
        }
    }

    private fun createContactsTogether(){
        val baseRequest = BaseParams()
        baseRequest.http_method = AppConfig.POST
        baseRequest.request_url = "/api/customer-phones/create-friend"
        baseRequest.project_id = AppConfig.PROJECT_CHAT
        ServiceFactory.createRetrofitServiceNode(
            TechResService::class.java
        )
            .createContactTogether(baseRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<BaseResponse> {
                override fun onComplete() {}
                override fun onError(e: Throwable) {
                    WriteLog.d("ERROR", e.message.toString())

                }
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(response: BaseResponse) {

                }
            })
    }

    override fun onChoosePhone(contactNodeChat: ContactNodeChat) {
        listContacts = adapter.getDataSource()
        val position = listContacts.indexOfFirst { it.phone == contactNodeChat.phone }
        listContacts[position].isCheck = if (contactNodeChat.isCheck) {
            number -= 1
            listContactsChoose.removeIf { listContacts[position].phone == it.phone }
            false
        } else {
            number += 1
            listContactsChoose.add(listContacts[position])
            true
        }
        if (number == 0) {
            binding.lnChooseContact.hide()
        } else {
            binding.lnChooseContact.show()
        }
        setViewChoose(number.toString())
        adapter.notifyItemChanged(position)
        adapterChoose.notifyDataSetChanged()
        binding.rcChooseContact.smoothScrollToPosition(adapterChoose.itemCount)
    }

    override fun onRemoveContact(contactNodeChat: ContactNodeChat) {
        listContacts = adapter.getDataSource()
        number -= 1
        val position = listContacts.indexOfFirst { it.phone == contactNodeChat.phone }
        val positionChoose = listContactsChoose.indexOfFirst { it.phone == contactNodeChat.phone }
        setViewChoose(number.toString())
        listContacts[position].isCheck = false
        listContactsChoose.removeAt(positionChoose)
        adapter.notifyItemChanged(position)
        adapterChoose.notifyDataSetChanged()
        if (adapterChoose.itemCount == 0) {
            binding.lnChooseContact.hide()
        }
    }

    override fun onResearch(key: String) {
        activity?.runOnUiThread {
            adapter.searchFullText(key)
        }
    }

    override fun onNumberChoose(number: Int) {
        activity?.runOnUiThread {
            this.number = number
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun dialogPermissionContact() {
        val dialog: Dialog? = this.context?.let { Dialog(it) }!!
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_permission_contact)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)
        btnOk.setOnClickListener {
            ActivityCompat.requestPermissions(
                requireActivity(),
                requiredPermissions,
                requiredPermissionsCode
            )

            dialog.dismiss()
        }
        btnNo.setOnClickListener {
            onBackPress()
            dialog.dismiss()
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    override fun onBackPress() : Boolean{
        return true
    }


}