package vn.techres.line.adapter.librarydevice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemSuggestFileBinding
import vn.techres.line.holder.file.SuggestFileHolder
import vn.techres.line.interfaces.util.SuggestFileHandler

class SuggestFileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var listFile = ArrayList<String>()
    private lateinit var suggestHandler: SuggestFileHandler

    fun setDataSource(listFile: ArrayList<String>) {
        this.listFile = listFile
        notifyDataSetChanged()
    }

    fun setSuggestFileHandler(suggestHandler: SuggestFileHandler) {
        this.suggestHandler = suggestHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SuggestFileHolder(
            ItemSuggestFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SuggestFileHolder).bind(listFile[position], suggestHandler)
    }

    override fun getItemCount(): Int {
        return listFile.size
    }
}