package vn.techres.line.adapter.branch

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.branch.Branch
import vn.techres.line.databinding.ItemBranchBinding
import vn.techres.line.interfaces.branch.BranchHandler

class BranchAdapter(var context : Context) : RecyclerView.Adapter<BranchAdapter.ViewHolder>(){
    private var branchHandler : BranchHandler? = null
    private var branchList = ArrayList<Branch>()

    fun setBranchHandler(branchHandler : BranchHandler){
        this.branchHandler = branchHandler
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(branchList : ArrayList<Branch>){
        this.branchList = branchList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBranchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return branchList.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, branchList[position], branchHandler)
    }

    class ViewHolder(private val binding : ItemBranchBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(context : Context, branch: Branch, branchHandler : BranchHandler?){
            val nameBranch =  branch.address_full_text?.split(",")
            binding.tvNameBranch.text = String.format("%s %s", context.resources.getString(R.string.title_address_branch), nameBranch?.get(0))
            binding.clImageChecked.visibility = View.GONE
            if (branch.is_check == false){
                binding.clImageChecked.visibility = View.GONE
                binding.tvNameBranch.background = ContextCompat.getDrawable(context, R.drawable.corner_gray_light_4dp)
                binding.tvNameBranch.setTextColor(ContextCompat.getColor(context, R.color.text_color))
            }else{
                binding.clImageChecked.visibility = View.VISIBLE
                binding.tvNameBranch.background = ContextCompat.getDrawable(context, R.drawable.gradient_orange_to_yellow_radius_4dp)
                binding.tvNameBranch.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            binding.clContainerBranch.setOnClickListener {
                branchHandler?.onClick(bindingAdapterPosition)
            }
        }
    }

}