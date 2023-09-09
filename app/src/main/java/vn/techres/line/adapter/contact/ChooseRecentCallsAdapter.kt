package vn.techres.line.adapter.contact

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.contact.CallLogData
import vn.techres.line.databinding.ItemChooseContactBinding
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.contact.RecentCallsHandler

class ChooseRecentCallsAdapter : RecyclerView.Adapter<ChooseRecentCallsAdapter.ViewHolder>() {
    private var dataSources: ArrayList<CallLogData> = ArrayList()
    private var recentCallsHandler: RecentCallsHandler? = null
    fun setDataSource(dataSources: ArrayList<CallLogData>) {
        this.dataSources = dataSources
        notifyDataSetChanged()
    }

    fun setRecentCallsHandler(recentCallsHandler: RecentCallsHandler) {
        this.recentCallsHandler = recentCallsHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChooseContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSources[position], recentCallsHandler)
    }

    override fun getItemCount(): Int {
        return dataSources.size
    }

    class ViewHolder(val binding: ItemChooseContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(callLogData: CallLogData, recentCallsHandler: RecentCallsHandler?) {
            if (callLogData.avatar != "") {
                binding.imgAvatar.loadImage(
                    callLogData.avatar,
                    R.drawable.ic_user_placeholder_circle,
                    true
                )
            } else {
                binding.imgAvatar.setImageResource(callLogData.color)
                val listString = callLogData.full_name?.split("\\s".toRegex())
                binding.tvNameAvatar.text = if (listString?.size ?: 0 > 1) {
                    String.format(
                        "%s%s",
                        listString?.get(0)?.get(0) ?: "",
                        listString?.get(1)?.get(0) ?: ""
                    )
                } else {
                    if (callLogData.full_name ?: "" != "") {
                        (callLogData.full_name?.get(0) ?: "").toString()
                    } else {
                        callLogData.phone?.slice(0..2)
                    }
                }
            }
            binding.imgRemove.setOnClickListener {
                recentCallsHandler?.onRemove(callLogData)
            }
        }
    }
}