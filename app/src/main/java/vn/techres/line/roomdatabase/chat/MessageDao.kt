package vn.techres.line.roomdatabase.chat

import androidx.annotation.WorkerThread
import androidx.room.*
import vn.techres.line.data.model.chat.*

@Dao
interface MessageDao {
    /**
     * Thêm một tin nhắn
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneMessage(vararg messagesByGroup: MessagesByGroup)

    /**
     * Thêm list tin nhắn
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @WorkerThread
    fun insertAllMessage(listMessage: ArrayList<MessagesByGroup>)

    /**
     * Câp nhật một message
     * */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateOneMessage(messagesByGroup: MessagesByGroup)

    /**
     * Lấy tin nhắn
     * */
    @Query("SELECT * FROM messageTable WHERE receiver_id = :groupId")
    fun getAllMessageGroup(groupId: String): MutableList<MessagesByGroup>

    /**
     * Lấy 50 tin nhắn group mới nhất
     * */
    @Query("SELECT * FROM messageTable WHERE receiver_id = :groupId ORDER BY strftime('%Y-%m-%d %H:%M:%S', created_at) DESC LIMIT (:position), 50")
    fun getMessageNewGroup(groupId: String, position: Int): MutableList<MessagesByGroup>

    /**
     * Lấy 4 hình ảnh hoặc video mới nhất
     * */
    @Query("SELECT * FROM messageTable WHERE receiver_id = :groupId ORDER BY strftime('%Y-%m-%d %H:%M:%S', created_at) DESC LIMIT 0,4")
    fun getMessageImageAndVideoNew(groupId: String): MutableList<MessagesByGroup>

    /**
     * Lấy số lượng tin nhắn
     * */
    @Query("SELECT COUNT(*) FROM messageTable WHERE receiver_id = :groupId")
    fun getTotalRecordMessage(groupId: String): Int
    /**
     * TÌm kiếm tin nhắn
     * */
//    @Query("")
//    fun getListMessageSearch(groupId: String, randomKey: String)
    /**
     * Lấy một randomKey tin nhắn
     * */
    @Query("SELECT * FROM messageTable WHERE random_key = :randomKey AND receiver_id = :groupId AND user_id = :userId")
    fun getOneMessage(randomKey: String, groupId: String, userId: Int): MessagesByGroup

    /**
     * Cập nhật randomKey
     * */
    @Query("UPDATE messageTable SET status = 0 WHERE random_key = :randomKey")
    fun updateStatusMessage(randomKey: String)

    /**
     * Cập nhật is local
     * */
    @Query("UPDATE messageTable SET is_local = 0 WHERE key_message = :key_message")
    fun updateIsLocalMessage(key_message: String)

    /**
     * Cập nhật message_type
     * */
    @Query("UPDATE messageTable SET message_type = :type WHERE random_key = :randomKey")
    fun updateMessageType(randomKey: String, type: String)

    /**
     * Cập nhật reaction
     * */
    @Query("UPDATE messageTable SET reactions = :reaction WHERE random_key = :randomKey")
    fun updateReaction(randomKey: String, reaction: Reaction)

    /**
     * Xóa tất cả tin nhắn trong bản
     * */
    @Query("DELETE FROM messageTable")
    fun deleteMessageAll()

    /**
     * Xóa tin nhắn một nhóm
     * */
    @Query("DELETE FROM messageTable WHERE receiver_id = :receiverId AND user_id = :userId")
    fun deleteMessageGroup(receiverId: String, userId: Int)

    /**
     * giải tán một nhóm
     * */
    @Query("DELETE FROM messageTable WHERE receiver_id = :receiverId")
    fun deleteMessageGroup(receiverId: String)

    /**
     * Xóa một tin nhắn
     * */
    @Query("DELETE FROM messageTable WHERE random_key = :randomKey")
    fun deleteOneMessage(randomKey: String)

    /**
     * Xóa một tin nhắn key_message
     * */
    @Query("DELETE FROM messageTable WHERE key_message = :keyMessage")
    fun deleteOneMessageLocal(keyMessage: String)

    /**
     * Lấy tin nhắn cuối cùng trong group
     * */
    @Query("SELECT *  FROM messageTable WHERE receiver_id = :groupId AND user_id = :userId Order By CAST(random_key AS NUMBERIC(20,0)) DESC LIMIT 1")
    fun getLastMessage(groupId: String, userId: Int): MessagesByGroup

    /**
     * Lấy tin nhắn đầu tiên trong group
     * */
    @Query("SELECT *  FROM messageTable WHERE receiver_id = :groupId AND user_id = :userId Order By CAST(random_key AS NUMBERIC(20,0)) LIMIT 1")
    fun getFirtMessage(groupId: String, userId: Int): MessagesByGroup

    /**
     * Lấy danh sách tin nhắn mới
     * */
    @Query("SELECT *  FROM messageTable WHERE receiver_id = :groupId AND user_id = :userId Order By CAST(random_key AS NUMBERIC(20,0)) DESC LIMIT 30")
    fun getMessage(groupId: String, userId: Int): MutableList<MessagesByGroup>

    /**
     * Lấy danh sách tin nhắn scroll phía trước
     * */
    @Query("SELECT *  FROM messageTable WHERE receiver_id = :groupId AND user_id = :userId AND CAST(random_key AS NUMBERIC(20,0)) <= :randomKey Order By CAST(random_key AS NUMBERIC(20,0)) DESC LIMIT :limit")
    fun getMessageScrollPre(
        groupId: String,
        userId: Int,
        randomKey: Long,
        limit: Int
    ): MutableList<MessagesByGroup>

    /**
     * Lấy danh sách tin nhắn scroll phía sau
     * */
    @Query("SELECT *  FROM messageTable WHERE receiver_id = :groupId AND user_id = :userId AND CAST(random_key AS NUMBERIC(20,0)) > :randomKey Order By CAST(random_key AS NUMBERIC(20,0)) LIMIT :limit")
    fun getMessageScrollNext(
        groupId: String,
        userId: Int,
        randomKey: Long,
        limit: Int
    ): MutableList<MessagesByGroup>

    /**
     * Lấy danh sách tin nhắn cũ theo LIMIT
     * */
    @Query("SELECT *  FROM messageTable WHERE receiver_id = :groupId AND CAST(random_key AS NUMBERIC(20,0)) < :randomKey AND user_id = :userId Order By CAST(random_key AS NUMBERIC(20,0)) DESC LIMIT :limit")
    fun getMessagePre(
        groupId: String,
        randomKey: Long,
        limit: Int,
        userId: Int
    ): MutableList<MessagesByGroup>

    /**
     * Lấy danh sách tin nhắn mới theo LIMIT
     * */
    @Query("SELECT *  FROM messageTable WHERE receiver_id = :groupId AND CAST(random_key AS NUMBERIC(20,0)) > :randomKey AND user_id = :userId Order By CAST(random_key AS NUMBERIC(20,0)) DESC LIMIT :limit")
    fun getMessageNext(
        groupId: String,
        randomKey: Long,
        limit: Int,
        userId: Int
    ): MutableList<MessagesByGroup>

    /**
     * Thu hồi tin nhắn
     **/
    @TypeConverters(FileNodeJs::class)
    @Query("UPDATE messageTable SET status = :status, files = :files, message_type = :message_type  WHERE receiver_id=:groupID AND random_key = :randomkey AND user_id = :userId")
    fun updateDataRevoke(
        groupID: String?,
        randomkey: String?,
        status: Int?,
        userId: Int,
        files: ArrayList<FileNodeJs>,
        message_type: String?
    )

    /**
     * Thu hồi tin nhắn reply
     **/
    @TypeConverters(Reply::class)
    @Query("UPDATE messageTable SET message_reply = :messageReply  WHERE receiver_id=:groupID AND random_key = :randomkey AND user_id = :userId")
    fun updateDataRevokeReply(
        groupID: String?,
        randomkey: String?,
        userId: Int,
        messageReply: Reply?
    )

    /**
     * Thu hồi reaction
     **/
    @TypeConverters(Reaction::class)
    @Query("UPDATE messageTable SET reactions = :reaction  WHERE receiver_id=:groupID AND random_key = :randomkey AND user_id = :userId")
    fun updateDataReaction(
        groupID: String?,
        randomkey: String?,
        reaction: Reaction?,
        userId: Int
    )

    /**
     * Thu hồi tin nhắn
     **/
    @TypeConverters(Reaction::class)
    @Query("UPDATE messageTable SET status = 0  WHERE receiver_id=:groupID AND random_key = :randomkey AND user_id = :userId")
    fun revokeMessageByGroup(
        groupID: String?,
        randomkey: String?,
        userId: Int
    )

    /**
     * update user id
     **/
    @TypeConverters(Reaction::class)
    @Query("UPDATE messageTable SET user_id = :userId  WHERE user_id = 0 And receiver_id = :groupID")
    fun updateDataUserId(
        groupID: String?,
        userId: Int
    )

    /**
     * update viewed
     **/
//    @TypeConverters(MessageViewer::class)
//    @Query("UPDATE messageTable SET message_viewed = :messageViewed  WHERE user_id = :userId And receiver_id = :groupID And random_key = :randomKey")
//    fun updateDataViewed(
//        groupID: String?,
//        userId: Int,
//        randomKey:String,
//        messageViewed:MessageViewer
//    )
    /**
     * tìm kiếm tin nhắn
     **/
    @Query("SELECT * FROM messageTable WHERE receiver_id=:groupId AND user_id = :userId AND message_type = 'text' AND UPPER(message) LIKE '%'||:s||'%' Order By CAST(random_key AS NUMBERIC(20,0))")
    fun getListMessageSearch(
        groupId: String?,
        userId: Int,
        s: String?
    ): MutableList<MessagesByGroup>
    /**
     * tìm kiếm tin nhắn
     **/
    @Query("SELECT * FROM messageTable WHERE receiver_id=:groupId AND user_id = :userId AND (message_type = 'video' Or message_type = 'image') Order By CAST(random_key AS NUMBERIC(20,0)) DESC Limit 4")
    fun getImageAndVideoGroup(
        groupId: String?,
        userId: Int): MutableList<MessagesByGroup>
   /**
     * hình ảnh trong group
     **/
    @Query("SELECT * FROM messageTable WHERE receiver_id=:groupId AND user_id = :userId AND message_type = 'image' Order By CAST(random_key AS NUMBERIC(20,0)) DESC")
    fun getImageInGroup(
        groupId: String?,
        userId: Int): MutableList<MessagesByGroup>
    /**
     * video trong group
     **/
    @Query("SELECT * FROM messageTable WHERE receiver_id=:groupId AND user_id = :userId AND message_type = 'video' Order By CAST(random_key AS NUMBERIC(20,0)) DESC")
    fun getVideoInGroup(
        groupId: String?,
        userId: Int): MutableList<MessagesByGroup>
/**
     * file trong group
     **/
    @Query("SELECT * FROM messageTable WHERE receiver_id=:groupId AND user_id = :userId AND message_type = 'file' Order By CAST(random_key AS NUMBERIC(20,0)) DESC")
    fun getFileInGroup(
        groupId: String?,
        userId: Int): MutableList<MessagesByGroup>
    /**
     * link trong group
     **/
    @Query("SELECT * FROM messageTable WHERE receiver_id=:groupId AND user_id = :userId AND message_type = 'link' Order By CAST(random_key AS NUMBERIC(20,0)) DESC")
    fun getLinkInGroup(
        groupId: String?,
        userId: Int): MutableList<MessagesByGroup>

    /**
     * Lấy tin nhắn offline
     * */
    @Query("SELECT *  FROM messageTable WHERE offline = 1")
    fun getOfflineMessage() : MutableList<MessagesByGroup>

    /**
     * Xóa tin nhắn offline
     * */
    @Query("DELETE FROM messageTable WHERE offline = 1 AND random_key = :random_key")
    fun deleteMessageOffline(random_key: String)
}