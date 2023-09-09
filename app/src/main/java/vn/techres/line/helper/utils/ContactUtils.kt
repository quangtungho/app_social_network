package vn.techres.line.helper.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import vn.techres.line.R
import vn.techres.line.activity.TechResApplication
import vn.techres.line.data.model.contact.CallLogData
import vn.techres.line.data.model.contact.ContactData
import vn.techres.line.interfaces.contact.ContactUtilsHandler
import java.util.*

object ContactUtils {
    /**
     * @author aminography
     */

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
//    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun Context.isContactExists(
        phoneNumber: String
    ): Boolean {
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.NUMBER,
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )
        contentResolver.query(lookupUri, projection, null, null, null).use {
            return (it?.moveToFirst() == true)
        }
    }

    fun Context.syncContactDevice(
        contactUtilsHandler: ContactUtilsHandler
    ){
        var page = 1
        var numberContact: Int
        TechResApplication.applicationContext().getAppDatabase()?.runInTransaction {
            TechResApplication.applicationContext().getContactDao()?.deleteAllContact()
            if((TechResApplication.applicationContext().getContactDao()?.getTotalRecordContact() ?: 0) > 0){
                val listContactDB = TechResApplication.applicationContext().getContactDao()?.getAllContact() as ArrayList<ContactData>
                val listContactDevice = ArrayList<ContactData>()
                do {
                    retrieveAllContacts(retrieveAvatar = true, limit = 10, offset = page).let {
                        numberContact = it.size
                        if (it.size > 0){
                            page ++
                            listContactDevice.addAll(it)
                        }
                    }
                }while (numberContact == 0)
                if(listContactDevice.size > 0){
                    TechResApplication.applicationContext().getContactDao()?.insertAllContact(listContactDevice)
                    val listSync = deleteRepeated(listContactDevice, listContactDB) as ArrayList<ContactData>
                    contactUtilsHandler.onSyncDbAndDevice(listSync)
                }
            }else{
                do {
                    retrieveAllContacts(retrieveAvatar = true, limit = 10, offset = page).let {
                        numberContact = it.size
                        if (it.size > 0){
                            page ++
                            TechResApplication.applicationContext().getContactDao()?.insertAllContact(it)
                            contactUtilsHandler.onSyncContact(it)
                        }else{
                            contactUtilsHandler.onSyncEndContact()
                            numberContact = 1
                        }
                    }
                }while (numberContact == 0)
            }
        }

    }

    private fun deleteRepeated(
        list: ArrayList<ContactData>,
        list2: ArrayList<ContactData>
    ): List<ContactData> {
        return list2.filterNot { isTheSameID(it, list) }.toMutableList()
    }
    private fun isTheSameID(contactData: ContactData, list1: List<ContactData>): Boolean {
        list1.forEach {
            if (contactData.phone?.get(0) == it.phone?.get(0)){
                return true
            }
        }
        return false
    }

    @JvmOverloads
    fun Context.retrieveAllContacts(
        searchPattern: String = "",
        retrieveAvatar: Boolean = true,
        limit: Int = -1,
        offset: Int = -1
    ): ArrayList<ContactData> {
        val result: ArrayList<ContactData> = ArrayList()

        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACT_PROJECTION,
            if (searchPattern.isNotBlank()) "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE '%?%'" else null,
            if (searchPattern.isNotBlank()) arrayOf(searchPattern) else null,
            if (limit > 0 && offset > -1) "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC LIMIT $limit OFFSET $offset"
            else ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )?.use {
            if (it.moveToFirst()) {
                do {
                    val contactId = it.getLong(it.getColumnIndexOrThrow(CONTACT_PROJECTION[0]))
                    val name = it.getString(it.getColumnIndexOrThrow(CONTACT_PROJECTION[2])) ?: ""
                    val hasPhoneNumber = it.getString(it.getColumnIndexOrThrow(CONTACT_PROJECTION[3])).toInt()
                    val avatar = if (retrieveAvatar) retrieveAvatar(contactId) else null
                    val phoneNumber: List<String> = if (hasPhoneNumber > 0) {
                        retrievePhoneNumber(contactId)
                    } else {
                        mutableListOf()
                    }
                    if (phoneNumber.isNotEmpty()) {
                        result.add(
                            ContactData(
                                contactId,
                                name,
                                phoneNumber,
                                avatar?.path ?: ""
                            )
                        )
                    }


                } while (it.moveToNext())
            }
        }
        return result
    }

    @JvmOverloads
    fun Context.retrieveRecentCallsContacts(
        searchPattern: String = "",
        limit: Int = -1,
        offset: Int = -1
    ): ArrayList<CallLogData> {

        val result: ArrayList<CallLogData> = ArrayList()
        var index = 1
        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            CALL_LOG_CALL_PROJECTION,
            if (searchPattern.isNotBlank()) "${CallLog.Calls.NUMBER} LIKE '%?%'" else null,
            if (searchPattern.isNotBlank()) arrayOf(searchPattern) else null,
            if (limit > 0 && offset > -1) "${CallLog.Calls.DATE} DESC LIMIT $limit OFFSET $offset"
            else CallLog.Calls.DATE + " DESC"
        )?.use {
            if (it.moveToFirst()) {
                do {
                    val id = (limit * offset) + index
                    val callLogId = it.getLong(it.getColumnIndexOrThrow(CALL_LOG_CALL_PROJECTION[0]))
                    val name = it.getString(it.getColumnIndexOrThrow(CALL_LOG_CALL_PROJECTION[1]))
                        ?: ""
                    val phoneNumber =
                        it.getString(it.getColumnIndexOrThrow(CALL_LOG_CALL_PROJECTION[2])) ?: ""
                    val callType =
                        it.getString(it.getColumnIndexOrThrow(CALL_LOG_CALL_PROJECTION[3])) ?: ""
                    val callDate =
                        it.getString(it.getColumnIndexOrThrow(CALL_LOG_CALL_PROJECTION[4])) ?: ""
                    val callDayTime = Date(callDate.toLong())
                    val callDuration =
                        it.getString(it.getColumnIndexOrThrow(CALL_LOG_CALL_PROJECTION[5])) ?: ""
                    val avatar = it.getString(it.getColumnIndexOrThrow(CALL_LOG_CALL_PROJECTION[6])) ?: ""
                    result.add(
                        CallLogData(
                            id,
                            callLogId,
                            name,
                            phoneNumber,
                            avatar,
                            callType,
                            callDayTime,
                            callDuration,
                            listColor.random()
                        )
                    )
                    index++
                } while (it.moveToNext())
            }
        }
        return result
    }

    private fun Context.retrievePhoneNumber(contactId: Long): List<String> {
        val result: MutableList<String> = mutableListOf()
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} =?",
            arrayOf(contactId.toString()),
            null
        )?.use {
            if (it.moveToFirst()) {
                do {
                    result.add(it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)))
                } while (it.moveToNext())
            }
        }
        return result
    }

    private fun Context.retrieveAvatar(contactId: Long): Uri? {
        return contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} =? AND ${ContactsContract.Data.MIMETYPE} = '${ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE}'",
            arrayOf(contactId.toString()),
            null
        )?.use {
            if (it.moveToFirst()) {
                val contactUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI,
                    contactId
                )
                Uri.withAppendedPath(
                    contactUri,
                    ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                )
            } else null
        }
    }

    private val CONTACT_PROJECTION = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.HAS_PHONE_NUMBER
    )

    private val CALL_LOG_CALL_PROJECTION = arrayOf(
        CallLog.Calls._ID,
        CallLog.Calls.CACHED_NAME,
        CallLog.Calls.NUMBER,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION,
        CallLog.Calls.CACHED_PHOTO_URI
    )
}