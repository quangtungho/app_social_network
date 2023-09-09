package vn.techres.line.helper.utils

import android.content.Context
import android.content.pm.ShortcutManager
import androidx.core.content.getSystemService

class NotificationUtil(var context: Context) {
    companion object {
        /**
         * The notification channel for messages. This is used for showing Bubbles.
         */
        private const val CHANNEL_NEW_MESSAGES = "new_messages"

        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
    }
    private val shortcutManager: ShortcutManager =
        context.getSystemService() ?: throw IllegalStateException()

}