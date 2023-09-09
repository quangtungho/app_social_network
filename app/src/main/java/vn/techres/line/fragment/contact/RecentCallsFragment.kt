package vn.techres.line.fragment.contact

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.ContactDeviceActivity
import vn.techres.line.adapter.contact.ChooseRecentCallsAdapter
import vn.techres.line.adapter.contact.RecentCallsAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.contact.CallLogData
import vn.techres.line.data.model.contact.ContactDevice
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentRecentCallsBinding
import vn.techres.line.helper.utils.ContactUtils.retrieveRecentCallsContacts
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.MultiplePermission.shouldAskPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.WriteLog
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.CommonUtils.Companion.dpToPx
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.contact.ContactHandler
import vn.techres.line.interfaces.contact.ContactNumberHandler
import vn.techres.line.interfaces.contact.RecentCallsHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import java.util.*
import kotlin.collections.ArrayList

class RecentCallsFragment :
    BaseBindingFragment<FragmentRecentCallsBinding>(FragmentRecentCallsBinding::inflate),
    RecentCallsHandler, ContactHandler {

    private val contactDeviceActivity: ContactDeviceActivity?
        get() = activity as ContactDeviceActivity?

    private var listContacts = ArrayList<CallLogData>()
    private var listContactsChoose = ArrayList<CallLogData>()
    private lateinit var contactNumberHandler : ContactNumberHandler
    private lateinit var adapter: RecentCallsAdapter
    private lateinit var adapterChoose: ChooseRecentCallsAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var layoutManagerChoose: LinearLayoutManager
    private var number = 0
    private var page = 1
    private var limit = 30
    private var user = User()

    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CALL_LOG
    )

    private val requiredPermissionsCode = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = CurrentUser.getCurrentUser(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        contactDeviceActivity?.setContactHandler(this)

        adapter = RecentCallsAdapter(requireActivity())
        adapterChoose = ChooseRecentCallsAdapter()
        adapter.setRecentCallsHandler(this)
        adapterChoose.setRecentCallsHandler(this)

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

        attachGroupOnScrollListener()

        if (!requireActivity().shouldAskPermissions(requiredPermissions)) {
            binding.lnPermission.hide()
            binding.rcContactDevice.show()
            binding.pbLoading.show()
            setData(1)
        } else {
            binding.lnPermission.show()
            binding.rcContactDevice.hide()
        }

        binding.btnPermission.setOnClickListener {
            requestMultiplePermissionWithListener()
        }

        binding.imgSend.setOnClickListener {
            val list = ArrayList<ContactDevice>()
            listContactsChoose.forEach { call ->
                list.add(ContactDevice(call.call_log_id?.toInt(), call.full_name, call.phone, call.avatar, false))
            }
            EventBus.getDefault().post(list)
//            contactNumberHandler.onDismissDialog()
            contactDeviceActivity?.onBackPressed()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contactNumberHandler = context as ContactNumberHandler
//        try {
//            if(parentFragment is ContactNumberHandler){
//                contactNumberHandler = parentFragment as ContactNumberHandler
//            }
//            if(parentFragment is ContactDeviceBottomSheetFragment){
//                (parentFragment as ContactDeviceBottomSheetFragment).setContactHandler(this)
//            }
//        }catch (e : Exception){
//            e.printStackTrace()
//        }
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
                    requireActivity().resources.getString(R.string.title_permission_diary_step_two),
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
                binding.pbLoading.show()
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
                        requireActivity().resources.getString(R.string.title_permission_diary_step_two),
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
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.title_permission_diary_step_two),
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
                    binding.pbLoading.show()
                    binding.rcContactDevice.show()
                    setData(1)
                }
            })
    }

    private fun attachGroupOnScrollListener() {
        binding.rcContactDevice.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = layoutManager.itemCount
                val visibleItemCount = layoutManager.childCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 5 && listContacts.size >= 30) {
                    page++
                    binding.rcContactDevice.removeOnScrollListener(this)
                    setData(page)
                }
            }
        })
    }

    private fun setData(page : Int){
        requireActivity().retrieveRecentCallsContacts("", limit, page).let{
            if(listContacts.size > 0){
                listContacts.addAll(listContacts.size, it)
            }else{
                listContacts = it
            }
            adapter.setDataSource(listContacts)
            WriteLog.d("retrieveRecentCallsContacts", Gson().toJson(listContacts))
            binding.pbLoading.hide()
        }
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onChoose(callLogData: CallLogData) {
        listContacts = adapter.getDataSource()
        val position = listContacts.indexOfFirst { it.id == callLogData.id }
        listContacts[position].isCheck = if (callLogData.isCheck) {
            number -= 1
            listContactsChoose.removeIf { listContacts[position].id == it.id }
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onRemove(callLogData: CallLogData) {
        listContacts = adapter.getDataSource()
        number -= 1
        val position = listContacts.indexOfFirst { it.id == callLogData.id }
        val positionChoose = listContactsChoose.indexOfFirst { it.id == callLogData.id }
        setViewChoose(number.toString())
        listContacts[position].isCheck = false
        listContactsChoose.removeAt(positionChoose)
        adapter.notifyItemChanged(position)
        adapterChoose.notifyDataSetChanged()
        if (adapterChoose.itemCount == 0) {
            binding.lnChooseContact.hide()
        }
    }

    override fun onBackPress() :Boolean {
        return true
    }
}