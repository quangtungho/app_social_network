package vn.techres.line.interfaces.contact

import vn.techres.line.data.model.contact.ContactData

interface ContactUtilsHandler {
    fun onSyncContact(result: ArrayList<ContactData>)
    fun onSyncEndContact()
    fun onSyncDbAndDevice(result: ArrayList<ContactData>)
}