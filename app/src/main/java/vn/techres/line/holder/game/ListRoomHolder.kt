package vn.techres.line.holder.game

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.shashank.sony.fancytoastlib.FancyToast
import vn.techres.line.R
import vn.techres.line.data.model.game.QRCodeGame
import vn.techres.line.data.model.game.RoomGame
import vn.techres.line.databinding.ItemListRoomBinding
import vn.techres.line.helper.TimeFormatHelper.getTime
import vn.techres.line.interfaces.ClickGame

class ListRoomHolder(var binding: ItemListRoomBinding) : RecyclerView.ViewHolder(binding.root) {

    var listRoomId = ArrayList<String>()
    fun bind(
        context: Context,
        roomGame: RoomGame,
        listDataFile: ArrayList<QRCodeGame>,
        roomId: ArrayList<String>,
        clickGame: ClickGame?
    ) {
        listRoomId = roomId
        binding.txtGameId.text = String.format("%s%s", "#", roomGame.room_id)
        binding.txtRoomName.text = roomGame.name_room
        binding.txtUserCount.text = (roomGame.total_user_join_room ?: 0).toString()
        binding.txtRoomBrachName.text = roomGame.branch_name
        binding.txtRoundCount.text = String.format("%s %s", "Vòng", roomGame.lucky_wheel_times)
        binding.txtTimeStart.text = getTime(roomGame.created_at)

        for (i in listDataFile.indices) {
            listRoomId.add(listDataFile[i].room_id ?: "")
        }

        if (roomGame.status == 0) {
            binding.lnStatusGame.setBackgroundResource(R.drawable.bg_main_corner_top)
            binding.txtStatusGame.text = "Chưa mở"
            binding.txtGameStatus.text = "Phòng Chưa Mở"
            binding.txtStatusGame.setBackgroundResource(R.drawable.bg_status_game_main)
            binding.txtStatusGame.setOnClickListener {
                FancyToast.makeText(
                    context,
                    "Phòng game chưa mở, bạn vui lòng quay lại sau",
                    FancyToast.LENGTH_LONG,
                    FancyToast.WARNING,
                    false
                ).show()
            }
        } else {
            if (roomGame.status == 1) {
                binding.txtGameStatus.text = "Phòng Sắp Bắt Đầu"
                binding.lnStatusGame.setBackgroundResource(R.drawable.bg_status_game)
                for (i in listDataFile.indices) {
                    if (listDataFile[i].room_id == roomGame.room_id) {
                        roomGame.branch_id = listDataFile[i].branch_id
                        roomGame.restaurant_id = listDataFile[i].restaurant_id
                        roomGame.is_join = 1
                    }
                }
                if (listRoomId.contains(roomGame.room_id)) {
                    binding.txtStatusGame.text = "Chơi Ngay"
                    binding.txtStatusGame.setBackgroundResource(R.drawable.bg_play_game_red)
                } else {
                    binding.txtStatusGame.text = "Tham Gia"
                    binding.txtStatusGame.setBackgroundResource(R.drawable.bg_play_game)
                }
                binding.txtStatusGame.setOnClickListener {
                    clickGame?.onClickGame(
                        roomGame
                    )
                }
                binding.root.setOnClickListener {
                    clickGame?.onClickGame(
                        roomGame
                    )
                }

            } else if (roomGame.status == 2) {
                binding.txtGameStatus.text = "Phòng Đang Chạy"
                binding.lnStatusGame.setBackgroundResource(R.drawable.bg_status_game)
                binding.txtStatusGame.setOnClickListener {
                    FancyToast.makeText(
                        context,
                        "Vòng chơi đã bắt đầu, vui lòng đợi trong giây lát",
                        FancyToast.LENGTH_LONG,
                        FancyToast.WARNING,
                        false
                    ).show()
                }
            }
        }
    }
}