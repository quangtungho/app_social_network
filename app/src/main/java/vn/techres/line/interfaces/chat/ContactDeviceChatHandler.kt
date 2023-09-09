package vn.techres.line.interfaces.chat

import vn.techres.line.data.model.contact.ContactNodeChat

interface ContactDeviceChatHandler {
    fun onChoosePhone(contactNodeChat: ContactNodeChat)
    fun onRemoveContact(contactNodeChat: ContactNodeChat)
}