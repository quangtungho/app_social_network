package vn.techres.line.roomdatabase.contact

import androidx.annotation.WorkerThread
import androidx.room.*
import vn.techres.line.data.model.friend.Friend


/**
 * OBJECT FRIEND TYPE
 * friends = 0
 * My friends = 1
 * Friends online = 2
 * Friends new = 3
 * Best friends = 4
 * Friends request = 5
 * Friends suggest = 6
 */
@Dao
interface FriendDao {

    //Insert all data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @WorkerThread
    fun insertAllData(myFriend: ArrayList<Friend>)

    //Insert an element
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(myFriend: Friend)

    //Select All Data
    @Query("SELECT * FROM tbl_friends")
    fun getAllData() : MutableList<Friend>

    //Get Friend
    @Query("SELECT * FROM tbl_friends WhERE type = 0")
    fun getFriendsSystem() : MutableList<Friend>
    @Query("SELECT * FROM tbl_friends WhERE type = 1")
    fun getMyFriends() : MutableList<Friend>
    @Query("SELECT * FROM tbl_friends WhERE type = 2")
    fun getFriendsOnline() : MutableList<Friend>
    @Query("SELECT * FROM tbl_friends WhERE type = 3")
    fun getFriendsNew() : MutableList<Friend>
    @Query("SELECT * FROM tbl_friends WhERE type = 4")
    fun getBestFriends() : MutableList<Friend>
    @Query("SELECT * FROM tbl_friends WhERE type = 5")
    fun getFriendsRequest() : MutableList<Friend>
    @Query("SELECT * FROM tbl_friends WhERE type = 6")
    fun getFriendsSuggest() : MutableList<Friend>

    //

    //Check existence
    @Transaction
    @Query("SELECT EXISTS (SELECT _id FROM tbl_friends WHERE _id = (:id))")
    fun checkData(id: String?): Boolean

    //Update an element
    @Update
    fun updateData(myFriend: Friend)

    //Delete all
    @Query("DELETE FROM tbl_friends")
    fun deleteAllData()

    //Delete an element
    @Delete
    fun deleteData(myFriend: Friend)

}