package vn.techres.line.holder.message

import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView

class ReplyQuicklyHolder(
  private val binding : ViewDataBinding
) : RecyclerView.ViewHolder(binding.root){
    fun bin(){
        binding.run {
            setVariable(BR._all, "")
            executePendingBindings()
        }
    }
}