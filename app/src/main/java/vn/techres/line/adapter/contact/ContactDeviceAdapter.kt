package vn.techres.line.adapter.contact

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemContactDeviceBinding
import vn.techres.line.holder.contact.ContactDeviceHolder
import vn.techres.line.interfaces.contact.ContactDeviceHandler
import vn.techres.line.data.model.contact.ContactDevice

class ContactDeviceAdapter(var context: Context) : RecyclerView.Adapter<ContactDeviceHolder>() {
    private var dataSources = ArrayList<ContactDevice>()
    private var contactDeviceHandler : ContactDeviceHandler? = null
    fun setDataSource(dataSources: ArrayList<ContactDevice>) {
        this.dataSources = dataSources
        notifyDataSetChanged()
    }

    fun setContactDeviceHandler(contactDeviceHandler : ContactDeviceHandler){
        this.contactDeviceHandler = contactDeviceHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactDeviceHolder {
        return ContactDeviceHolder(
            ItemContactDeviceBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactDeviceHolder, position: Int) {
        holder.bind(dataSources, position, contactDeviceHandler)
    }

    override fun getItemCount(): Int {
        return dataSources.size
    }
}