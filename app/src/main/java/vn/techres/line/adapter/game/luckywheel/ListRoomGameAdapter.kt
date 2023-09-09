package vn.techres.line.adapter.game.luckywheel

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.data.model.game.QRCodeGame
import vn.techres.line.data.model.game.RoomGame
import vn.techres.line.databinding.ItemListRoomBinding
import vn.techres.line.helper.StringUtils
import vn.techres.line.holder.game.ListRoomHolder
import vn.techres.line.interfaces.ClickGame
import java.util.*
import java.util.stream.Collectors

class ListRoomGameAdapter(var context : Context) : RecyclerView.Adapter<ListRoomHolder>() {
    private var dataSource: ArrayList<RoomGame> = ArrayList()
    private var dataSourceTemp = ArrayList<RoomGame>()

    private var listRoomId = ArrayList<String>()
    private var listDataFile = ArrayList<QRCodeGame>()
    private var clickGame : ClickGame? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setDataSource(dataSource: ArrayList<RoomGame>, listDataFile : ArrayList<QRCodeGame>){
        this.dataSource = dataSource
        this.listDataFile = listDataFile
        notifyDataSetChanged()
    }

    fun setClickGame(clickGame : ClickGame){
        this.clickGame = clickGame
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        dataSource = if (StringUtils.isNullOrEmpty(key)) {
            dataSourceTemp
        } else {
            dataSourceTemp.stream().filter {
                it.room_id?.lowercase(Locale.ROOT)
                    ?.contains(key) == true || it.prefix?.lowercase(Locale.ROOT)
                    ?.contains(key) == true || it.normalize_name?.lowercase(Locale.ROOT)
                    ?.contains(key) == true || it.name_room?.lowercase(Locale.ROOT)
                    ?.contains(key) == true || it.branch_name?.lowercase(Locale.ROOT)
                    ?.contains(key) == true
            }.collect(Collectors.toList()) as ArrayList<RoomGame>
        }
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListRoomHolder {
        return ListRoomHolder(ItemListRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ListRoomHolder, position: Int) {
        holder.bind(context, dataSource[position], listDataFile, listRoomId, clickGame)
        listRoomId = holder.listRoomId
    }

    override fun getItemCount(): Int {
       return dataSource.size
    }
}