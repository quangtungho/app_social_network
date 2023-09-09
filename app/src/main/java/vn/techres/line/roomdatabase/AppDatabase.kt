package vn.techres.line.roomdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import vn.techres.line.data.local.contact.device.AvatarDeviceDBConvert
import vn.techres.line.data.local.contact.device.PhoneDBConvert
import vn.techres.line.data.local.message.GiphyDBConvert
import vn.techres.line.data.local.message.ListMessageDBConvert
import vn.techres.line.data.local.message.ListSenderDBConvert
import vn.techres.line.data.model.chat.*
import vn.techres.line.data.model.chat.response.TypingUser
import vn.techres.line.data.model.contact.ContactData
import vn.techres.line.data.model.friend.Friend
import vn.techres.line.data.model.game.QRCodeGame
import vn.techres.line.roomdatabase.chat.GroupDao
import vn.techres.line.roomdatabase.chat.MessageDao
import vn.techres.line.roomdatabase.contact.ContactDataDao
import vn.techres.line.roomdatabase.contact.FriendDao
import vn.techres.line.roomdatabase.qrcode.QrCodeDao

@Database(
    entities = [Friend::class,Group::class, MessagesByGroup::class, ContactData::class, QRCodeGame::class], version = 33
)

@TypeConverters(
    Converters::class, TagName::class, Members::class, Sender::class,
    Reply::class, Pinned::class, ShareMessage::class, PhoneMessage::class, LinkMessage::class, Vote::class, MessageVideoCall::class, Reaction::class,
    ListSenderDBConvert::class, FileNodeJs::class, GiphyDBConvert::class, TypingUser::class, TagName::class, MessageViewer::class, Sticker::class,
    ListMessageDBConvert::class, PhoneDBConvert::class, AvatarDeviceDBConvert::class, MessageEvent::class, MessageCompleteBill::class, MessageSettings::class
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao?
    abstract fun groupDao(): GroupDao?
    abstract fun qrCodeDao(): QrCodeDao?
    abstract fun contactDataDao(): ContactDataDao?
    abstract fun friendDao(): FriendDao?


    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        operator fun invoke(context: Context) = instance ?: synchronized(this) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "AloLineDatabase"
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .setJournalMode(JournalMode.AUTOMATIC)
                .build()
    }

}