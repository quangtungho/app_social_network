package vn.techres.line.adapter.address

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.address.Address
import vn.techres.line.databinding.ItemSavedAddressBinding
import vn.techres.line.interfaces.address.SaveAddressHandler

class SavedAddressAdapter(var context : Context) : RecyclerView.Adapter<SavedAddressAdapter.ViewHolder>() {
    private var dataSource = ArrayList<Address>()
    private var saveAddressHandler : SaveAddressHandler? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource : ArrayList<Address>){
        this.dataSource = dataSource
        notifyDataSetChanged()
    }
    fun setSaveAddressHandler(saveAddressHandler : SaveAddressHandler){
        this.saveAddressHandler = saveAddressHandler
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSavedAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSource[position], saveAddressHandler)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    class ViewHolder(private val binding : ItemSavedAddressBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(address: Address, saveAddressHandler : SaveAddressHandler?){
            binding.tvName.text = address.name
            binding.tvAddress.text = address.address_full_text
            binding.root.setOnClickListener {
                saveAddressHandler?.onChooseAddressRepair(address)
            }
        }
    }
}