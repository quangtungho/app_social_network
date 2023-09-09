package vn.techres.line.roomdatabase.qrcode

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.techres.line.data.model.game.QRCodeGame

@Dao
interface QrCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneQrCode(qrCodeGame: QRCodeGame)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @WorkerThread
    fun insertAllQrCode(listQrCode: ArrayList<QRCodeGame>)

    @Query("DELETE FROM qrCodeGame")
    fun deleteAllQrCode()
    @Query("SELECT * FROM qrCodeGame ")
    fun getListRoom() : MutableList<QRCodeGame>
}