package vn.techres.line.roomdatabase.contact

import androidx.annotation.WorkerThread
import androidx.room.*
import vn.techres.line.data.model.contact.ContactData

@Dao
interface ContactDataDao {
    /**
     * Thêm một contact
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOneContact(vararg contactData: ContactData)
    /**
     * Thêm danh sách contact
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @WorkerThread
    fun insertAllContact(listContactData: ArrayList<ContactData>)
    /**
     * Câp nhật một contact
     * */
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateOneContactData(contactData: ContactData)
    /**
     * Lấy tin nhắn
     * */
    @Query("SELECT * FROM contactTable")
    fun getAllContact() : MutableList<ContactData>
    /**
     *
     * */
    @Query("SELECT * FROM contactTable LIMIT (:position), 50")
    fun getContactPage(position : Int) : MutableList<ContactData>
    /**
     * Lấy số lượng contact
     * */
    @Query("SELECT COUNT(*) FROM contactTable")
    fun getTotalRecordContact() : Int
    /**
     * TÌm số điện thoại
     * */
    @Query("SELECT * FROM contactTable WHERE phone = :phoneList ")
    fun findPhoneContact(phoneList : List<String>) : ContactData
    /**
     * Xóa tất cả contact
     * */
    @Query("DELETE FROM contactTable")
    fun deleteAllContact()
}