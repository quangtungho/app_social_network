package vn.techres.line.roomdatabase.chat

import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.techres.line.data.model.chat.Group


@Dao
interface GroupDao {
    /**
     * Thêm danh sách group
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @WorkerThread
    fun insertAll(groupList: ArrayList<Group>)
    /**
     * Thêm một group
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @WorkerThread
    fun insertOne(vararg group: Group)
    /**
     * Lấy danh sách group
     * */
    @Query("SELECT * FROM groupTable Where user_id = :userID  ORDER BY number_message_not_seen DESC, strftime('%Y-%m-%d %H:%M:%S', created_at_time) DESC")
    fun getGroup(userID: Int) : MutableList<Group>
    /**
     * lấy một group
     * */
    @Query("SELECT * FROM groupTable WHERE _id = :groupId AND user_id = :userID")
    fun getOneGroup(groupId: String, userID: Int) : MutableList<Group>
    /**
     * Lấy danh sách group số lượng
     * */
//    @Query("SELECT * FROM groupTable ORDER BY strftime('%Y-%m-%d %H:%M:%S', created_last_message) DESC LIMIT (:position), 50")
    @Query("SELECT * FROM (SELECT * FROM groupTable WHERE (strftime('%s', 'now') - strftime('%s', created_at_time)) < (5*60) ORDER BY strftime('%Y-%m-%d %H:%M:%S', created_at_time) DESC) UNION SELECT * FROM (SELECT * FROM groupTable WHERE (strftime('%s', 'now') - strftime('%s', created_at_time)) > (5*60) AND number_message_not_seen > 0 ORDER BY strftime('%Y-%m-%d %H:%M:%S', created_at_time) DESC ) UNION SELECT * FROM (SELECT * FROM groupTable WHERE (strftime('%s', 'now') - strftime('%s', created_at_time)) > (5*60) AND number_message_not_seen == 0 ORDER BY strftime('%Y-%m-%d %H:%M:%S', created_at_time) DESC ) LIMIT (:position), 50 ")
    fun getGroupPage(position : Int) : MutableList<Group>

    /**
     * Cập nhật background
     * */
    @Query("UPDATE groupTable SET background = :background WHERE _id = :groupId")
    fun updateBackgroundGroup(groupId: String, background: String)
    /**
     * Cập nhật user id
     * */
    @Query("UPDATE groupTable SET user_id = :userID WHERE user_id = 0")
    fun updateUserIdGroup(userID: Int)
    /**
     * Cập nhật group đã xem
     * */
    @Query("UPDATE groupTable SET number_message_not_seen = 0 WHERE user_id = :userID AND _id = :groupId")
    fun updateSeenGroup(userID: Int, groupId: String)
    /**
     * Cập nhật thông tin group
     * */
    @Query("UPDATE groupTable SET number_message_not_seen = :number AND last_message = :last_message AND last_message_type = :last_message_type AND created_last_message = :created_last_message AND status_last_message = :status_last_message, user_name_last_message=:user_name_last_message AND user_last_message_id= :user_last_message_id AND name = :name WHERE user_id = :userID AND _id = :groupId")
    fun updateInforGroup(number:Int, last_message:String,last_message_type:String,created_last_message:String,status_last_message:Int,user_name_last_message:String,user_last_message_id:Int,name:String,userID: Int, groupId: String)
    /**
     * Xóa tất cả group
     * */
    @Query("DELETE FROM groupTable")
    fun deleteGroupAll()
    /**
     * Xóa một group
     * */
    @Query("DELETE FROM groupTable WHERE _id = :groupId AND user_id = :userID")
    fun deleteGroup(groupId : String, userID: Int)

}