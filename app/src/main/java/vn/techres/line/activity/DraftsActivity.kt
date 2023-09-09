package vn.techres.line.activity

import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vn.techres.data.line.model.PostReview
import vn.techres.line.adapter.newsfeed.DraftsAdapter
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import vn.techres.line.databinding.ActivityDraftsBinding
import vn.techres.line.helper.*
import vn.techres.line.helper.techresenum.TechresEnum
import java.lang.reflect.Type

class DraftsActivity : BaseBindingActivity<ActivityDraftsBinding>() {
    private var dataDraft = ArrayList<PostReview>()
    private var nodeJs = ConfigNodeJs()
    private var user = User()
    private var adapterDrafts: DraftsAdapter? = null

    override val bindingInflater: (LayoutInflater) -> ActivityDraftsBinding
        get() = ActivityDraftsBinding::inflate

    override fun onSetBodyView() {
        user = CurrentUser.getCurrentUser(this)
        nodeJs = CurrentConfigNodeJs.getConfigNodeJs(this)

//        dataDraft = Gson().fromJson(
//            intent.getStringExtra(TechresEnum.DATA_DRAFTS.toString()),
//            object : TypeToken<ArrayList<PostReview>>() {}.type
//        )

        val type: Type = object : TypeToken<ArrayList<PostReview?>?>() {}.type
        dataDraft = Gson().fromJson<Any>(intent.getStringExtra(TechresEnum.DATA_DRAFTS.toString()), type) as ArrayList<PostReview>

        adapterDrafts = DraftsAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = adapterDrafts

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        Toast.makeText(this, dataDraft.size.toString(), Toast.LENGTH_SHORT).show()

    }


}