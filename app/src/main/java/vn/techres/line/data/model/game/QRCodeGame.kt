package vn.techres.line.data.model.game

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject
import java.io.Serializable

@JsonObject
@Entity(tableName = "qrCodeGame")
class QRCodeGame : Serializable {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
    @JsonField(name = ["status"])
    @ColumnInfo(name = "status")
    var status: Int? = 0

    @JsonField(name = ["branch_id"])
    @ColumnInfo(name = "branch_id")
    var branch_id: Int? = 0

    @JsonField(name = ["restaurant_id"])
    @ColumnInfo(name = "restaurant_id")
    var restaurant_id: Int? = 0

    @JsonField(name = ["room_id"])
    @ColumnInfo(name = "room_id")
    var room_id: String? = ""
    @JsonField(name = ["row"])
    @ColumnInfo(name = "row")
    var row: Int? = 0

}