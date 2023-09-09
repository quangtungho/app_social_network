package vn.techres.line.adapter.account

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.databinding.ItemNotificationBinding
import vn.techres.line.holder.notify.NotifyHolder
import vn.techres.line.interfaces.NotificationsHandler
import vn.techres.line.data.model.Notifications
import vn.techres.line.helper.CurrentConfigNodeJs

class NotificationAdapter(val context: Context) : RecyclerView.Adapter<NotifyHolder>() {
    private var dataSource = ArrayList<Notifications>()
    private var notificationsHandler: NotificationsHandler? = null
    private var nodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setOnClickNotification(onMarkClick: NotificationsHandler) {
        this.notificationsHandler = onMarkClick
    }

    fun setDataSource(dataSource: ArrayList<Notifications>) {
        this.dataSource = dataSource
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifyHolder {
        return NotifyHolder(
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    override fun onBindViewHolder(holder: NotifyHolder, position: Int) {
        holder.bin(dataSource[position], notificationsHandler, nodeJs)
    }
}