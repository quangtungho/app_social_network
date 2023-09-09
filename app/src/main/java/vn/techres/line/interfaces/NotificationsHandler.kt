package vn.techres.line.interfaces

import vn.techres.line.data.model.Notifications

interface NotificationsHandler {
    fun markNotification(notifications : Notifications)
    fun onAction(notifications : Notifications)
    fun onAcceptFriend(notifications : Notifications)
    fun onRefuseFriend(notifications : Notifications)
}