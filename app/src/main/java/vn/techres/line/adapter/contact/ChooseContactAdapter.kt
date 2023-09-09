package vn.techres.line.adapter.contact

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.contact.ContactNodeChat
import vn.techres.line.databinding.ItemChooseContactBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.holder.contact.ChooseContactHolder
import vn.techres.line.interfaces.chat.ContactDeviceChatHandler

class ChooseContactAdapter(var context : Context) : RecyclerView.Adapter<ChooseContactHolder>() {
    private var listContacts = ArrayList<ContactNodeChat>()
    private lateinit var contactDeviceChatHandler: ContactDeviceChatHandler
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setDataSource(listContacts: ArrayList<ContactNodeChat>) {
        this.listContacts = listContacts
        notifyDataSetChanged()
    }
    fun setContactDeviceChatHandler(contactDeviceChatHandler: ContactDeviceChatHandler) {
        this.contactDeviceChatHandler = contactDeviceChatHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseContactHolder {
        return ChooseContactHolder(ItemChooseContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ChooseContactHolder, position: Int) {
        holder.bind(configNodeJs, listContacts[position], contactDeviceChatHandler)
    }

    override fun getItemCount(): Int {
        return listContacts.size
    }
}