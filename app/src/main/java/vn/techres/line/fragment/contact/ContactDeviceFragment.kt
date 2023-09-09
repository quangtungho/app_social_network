package vn.techres.line.fragment.contact

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.activity.MainActivity
import vn.techres.line.adapter.contact.ContactDeviceAdapter
import vn.techres.line.base.BaseBindingFragment
import vn.techres.line.data.model.contact.ContactDevice
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.FragmentContactDeviceBinding
import vn.techres.line.helper.CurrentUser
import vn.techres.line.helper.MultiplePermission.handleOnRequestPermissionResult
import vn.techres.line.helper.MultiplePermission.requestPermissions
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.permissionsIsGranted
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.techresenum.TechresEnum
import vn.techres.line.helper.utils.AlolineColorUtil
import vn.techres.line.helper.utils.dialogPermission
import vn.techres.line.interfaces.contact.ContactDeviceHandler
import vn.techres.line.interfaces.dialog.DialogPermissionHandler
import vn.techres.line.interfaces.permission.PermissionResultListener
import vn.techres.line.interfaces.permission.RequestPermissionListener
import java.util.*
import kotlin.collections.ArrayList

class ContactDeviceFragment : BaseBindingFragment<FragmentContactDeviceBinding>(FragmentContactDeviceBinding::inflate), ContactDeviceHandler {
    val mainActivity: MainActivity?
        get() = activity as MainActivity?

    private var listContacts = ArrayList<ContactDevice>()
    private var adapter: ContactDeviceAdapter? = null
    private var user = User()
    //permission
    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS
    )

    private val requiredPermissionsCode = 200
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.window?.statusBarColor = AlolineColorUtil(view.context).convertColor(R.color.white)
        binding.header.tvTitleHomeHeader.text = requireActivity().resources.getString(R.string.title_contacts)
        binding.rcContactDevice.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        adapter = ContactDeviceAdapter(requireActivity())
        adapter?.setContactDeviceHandler(this)
        binding.rcContactDevice.adapter = adapter
        user = CurrentUser.getCurrentUser(requireActivity())
        cacheManager.put(TechresEnum.CURRENT_MAIN.toString(), "")

        if (!permissionsIsGranted( requireActivity(), requiredPermissions)) {
            binding.lnPermission.show()
            binding.rcContactDevice.hide()
        } else {
            binding.lnPermission.hide()
            binding.rcContactDevice.show()
            syncContacts()
        }
        binding.btnPermission.setOnClickListener {
            requestMultiplePermissionWithListener()
        }
        binding.header.btnBack.setOnClickListener { 
            mainActivity?.supportFragmentManager?.popBackStack()
        }
    }
    override fun onResume() {
        super.onResume()
        mainActivity?.setOnBackClick(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity?.removeOnBackClick(this)
    }
    @SuppressLint("Recycle")
    private fun syncContacts() {
        binding.pbLoading.show()
        var isCheck: Boolean
        Thread {
            // a potentially time consuming task
            val contentResolver: ContentResolver = mainActivity!!.contentResolver
            var contactId: String?
            val cursor: Cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )!!
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val hasPhoneNumber =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                            .toInt()
                    if (hasPhoneNumber > 0) {
                        contactId =
                            cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                        val phoneCursor: Cursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf<String?>(contactId),
                            null
                        )!!
                        if (phoneCursor.moveToNext()) {
                            val contactDevice = ContactDevice()
                            contactDevice.phone =
                                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            contactDevice.full_name =
                                phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            listContacts.add(contactDevice)
                        }
                        phoneCursor.close()
                    }
                }
                isCheck = true
            } else {
                isCheck = false
            }

            cursor.close()
            Handler(Looper.getMainLooper()).postDelayed({
                if (isCheck) {
                    binding.rcContactDevice.show()
                    binding.lnEmptyContact.hide()
                } else {
                    binding.rcContactDevice.hide()
                    binding.lnEmptyContact.show()
                }
                binding.pbLoading.hide()
                adapter?.setDataSource(listContacts)
            }, 500)

        }.start()
    }

    /**
     * Permission
     * */
    private fun requestMultiplePermissionWithListener(){
        requestPermissions(requiredPermissions, requiredPermissionsCode, object :
            RequestPermissionListener {

            override fun onCallPermissionFirst(namePermission: String, requestPermission: () -> Unit) {
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
                    String.format(requireActivity().resources.getString(R.string.note_permission_denied),
                        requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                        namePermission,
                        namePermission
                    ),
                    requireActivity().resources.getString(R.string.step_two_open_permission),
                    R.drawable.ic_phone_book, "", 0,  object : DialogPermissionHandler {
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
                syncContacts()
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
                        String.format(requireActivity().resources.getString(R.string.note_permission_reject),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name,
                            namePermission,
                            namePermission
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission),
                        R.drawable.ic_phone_book, "", 0,  object : DialogPermissionHandler {
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
                        String.format(requireActivity().resources.getString(R.string.note_permission_denied),
                            requireActivity().resources.getString(R.string.brother_and_sister) + " " + user.name
                        ),
                        requireActivity().resources.getString(R.string.step_two_open_permission),
                        R.drawable.ic_phone_book, "", 0,  object : DialogPermissionHandler {
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
                    syncContacts()
                }
            })
    }

    override fun onChoosePhone(position: Int) {
        EventBus.getDefault().post(listContacts[position])
        mainActivity?.supportFragmentManager?.popBackStack()
    }

    override fun onBackPress() : Boolean{
        return true
    }
}