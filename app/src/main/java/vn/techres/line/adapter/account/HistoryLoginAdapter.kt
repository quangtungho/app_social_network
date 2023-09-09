package vn.techres.line.adapter.account

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemHistoryLoginBinding

class HistoryLoginAdapter(var context: Context) : RecyclerView.Adapter<HistoryLoginAdapter.HistoryLoginHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryLoginHolder {
        return HistoryLoginHolder(ItemHistoryLoginBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HistoryLoginHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 0
    }

    class HistoryLoginHolder(var binding : ItemHistoryLoginBinding) : RecyclerView.ViewHolder(binding.root){

    }

}