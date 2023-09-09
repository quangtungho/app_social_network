package vn.techres.line.adapter.branch

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
import vn.techres.line.R
import vn.techres.line.data.model.branch.SaveBranch
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemSaveBranchBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.Utils.setClick
import vn.techres.line.interfaces.branch.SaveBranchHandler

class BranchSaveAdapter(var context : Context) : RecyclerView.Adapter<BranchSaveAdapter.ViewHolder>(){
    private var saveBranchList = ArrayList<SaveBranch>()
    private var saveBranchHandler : SaveBranchHandler? = null
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(saveBranchList : ArrayList<SaveBranch>){
        this.saveBranchList = saveBranchList
        notifyDataSetChanged()
    }

    fun setSaveBranchHandler(saveBranchHandler : SaveBranchHandler){
        this.saveBranchHandler = saveBranchHandler
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSaveBranchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return saveBranchList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(configNodeJs, saveBranchList[position], saveBranchHandler)
    }

    class ViewHolder(private val binding : ItemSaveBranchBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(configNodeJs: ConfigNodeJs, saveBranch : SaveBranch, saveBranchHandler : SaveBranchHandler?){
            binding.imgSaveBranch.load(String.format("%s%s", configNodeJs.api_ads, saveBranch.branch_logo)){
                crossfade(true)
                scale(Scale.FIT)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                transformations(RoundedCornersTransformation(10F))
                size(500, 500)
            }
            binding.tvNameSaveBranch.text = saveBranch.branch_name
            binding.txtRatingBranch.text = saveBranch.branch_rating.toString()
            binding.txtRatingBranch.text = String.format("%.1f", saveBranch.branch_rating!!)
            binding.tvBranchAddress.text = saveBranch.branch_address
            binding.root.setOnClickListener {
                binding.root.setClick()
                saveBranchHandler!!.onSaveBranch(bindingAdapterPosition, saveBranch.branch_id!!)
            }
        }
    }
}