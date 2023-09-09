package vn.techres.line.holder.contact

import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.databinding.ItemContactDeviceBinding
import vn.techres.line.interfaces.contact.ContactDeviceHandler
import vn.techres.line.data.model.contact.ContactDevice

class ContactDeviceHolder(private val binding : ItemContactDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(dataSource : ArrayList<ContactDevice>, position : Int, contactDeviceHandler : ContactDeviceHandler?){
        binding.imgContact.setImageResource(R.drawable.ic_user_placeholder)
        binding.tvNameContact.text = dataSource[position].full_name
        binding.tvPhoneContact.text = dataSource[position].phone
        binding.root.setOnClickListener {
            contactDeviceHandler!!.onChoosePhone(position)
        }
    }
}