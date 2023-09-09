package vn.techres.line.adapter.address

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.address.Address
import vn.techres.line.databinding.ItemAddressBinding
import vn.techres.line.interfaces.ChooseAddressHandler
import vn.techres.line.interfaces.DeleteAddressHandler

class AddressAdapter(var context: Context) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {
    private var addressList = ArrayList<Address>()
    private var chooseAddressHandler: ChooseAddressHandler? = null
    private var deleteAddressHandler: DeleteAddressHandler? = null

    fun setChooseAddressHandler(chooseAddressHandler: ChooseAddressHandler) {
        this.chooseAddressHandler = chooseAddressHandler
    }

    fun setDeleteAddressHandler(deleteAddressHandler: DeleteAddressHandler) {
        this.deleteAddressHandler = deleteAddressHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(addressList: ArrayList<Address>) {
        this.addressList = addressList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, addressList[position], chooseAddressHandler, deleteAddressHandler)
    }

    class ViewHolder(private val binding: ItemAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(context: Context, address: Address, chooseAddressHandler: ChooseAddressHandler?, deleteAddressHandler: DeleteAddressHandler?) {
            binding.tvTitleAddress.text = if(address.name == ""){
                context.resources.getString(R.string.empty_content_address)
            }else{
                address.name
            }
            binding.tvContentAddress.text = address.address_full_text
            binding.root.setOnClickListener {
                chooseAddressHandler?.onChooseAddress(address)
            }
            binding.root.setOnLongClickListener {
                deleteAddressHandler?.onDeleteAddress(address, binding.root)
                true
            }
        }
    }

}