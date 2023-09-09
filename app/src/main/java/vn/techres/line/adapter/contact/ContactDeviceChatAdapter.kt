package vn.techres.line.adapter.contact

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.contact.ContactNodeChat
import vn.techres.line.databinding.ItemContactDeviceChatBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.holder.contact.ContactDeviceChatHolder
import vn.techres.line.interfaces.chat.ContactDeviceChatHandler
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class ContactDeviceChatAdapter(var context : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listContacts = ArrayList<ContactNodeChat>()
    private var listContactsTemp = ArrayList<ContactNodeChat>()
    private lateinit var contactDeviceChatHandler: ContactDeviceChatHandler
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(listContacts: ArrayList<ContactNodeChat>) {
        this.listContacts = listContacts
        this.listContactsTemp = listContacts
        notifyDataSetChanged()
    }

    fun setContactDeviceChatHandler(contactDeviceChatHandler: ContactDeviceChatHandler) {
        this.contactDeviceChatHandler = contactDeviceChatHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        listContacts = if (StringUtils.isNullOrEmpty(key)) {
            listContactsTemp
        } else {
            listContactsTemp.stream().filter {
                it.full_name?.lowercase(Locale.ROOT)?.contains(key) ?: false ||
                        it.phone?.lowercase(Locale.ROOT)?.contains(key) ?: false
            }.collect(Collectors.toList()) as ArrayList<ContactNodeChat>
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ContactDeviceChatHolder(
            ItemContactDeviceChatBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactDeviceChatHolder).bind(configNodeJs, listContacts[position], contactDeviceChatHandler)
    }

    override fun getItemCount(): Int {
        return listContacts.size
    }

    fun getDataSource() : ArrayList<ContactNodeChat>{
        return listContacts
    }
}