package vn.techres.line.adapter.contact

import android.annotation.SuppressLint
import android.content.Context
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.data.model.contact.CallLogData
import vn.techres.line.databinding.ItemRecentCallsBinding
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.utils.loadImage
import vn.techres.line.interfaces.contact.RecentCallsHandler
import java.text.DateFormat
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class RecentCallsAdapter(var context: Context) :
    RecyclerView.Adapter<RecentCallsAdapter.ViewHolder>() {

    private var dataSources: ArrayList<CallLogData> = ArrayList()
    private var dataSourcesTemp: ArrayList<CallLogData> = ArrayList()
    private var recentCallsHandler: RecentCallsHandler? = null

    fun setRecentCallsHandler(recentCallsHandler: RecentCallsHandler) {
        this.recentCallsHandler = recentCallsHandler
    }

    fun setDataSource(dataSources: ArrayList<CallLogData>) {
        this.dataSources = dataSources
        this.dataSourcesTemp = dataSources
        notifyDataSetChanged()
    }

    fun getDataSource(): ArrayList<CallLogData> {
        return dataSources
    }

    fun searchFullText(keyword: String) {
        val key = keyword.lowercase(Locale.getDefault()).trim()
        dataSources = if (StringUtils.isNullOrEmpty(key)) {
            dataSourcesTemp
        } else {
            dataSourcesTemp.stream().filter {
                it.full_name!!.lowercase(Locale.ROOT).contains(key) ||
                        it.phone!!.lowercase(Locale.ROOT).contains(key)
            }.collect(Collectors.toList()) as ArrayList<CallLogData>
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecentCallsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, dataSources[position], recentCallsHandler)
    }

    override fun getItemCount(): Int {
        return dataSources.size
    }

    class ViewHolder(val binding: ItemRecentCallsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            context: Context,
            callLogData: CallLogData,
            recentCallsHandler: RecentCallsHandler?
        ) {
            if (callLogData.avatar != "") {
                binding.cvAvatarColor.hide()
                binding.imgContact.loadImage(
                    callLogData.avatar,
                    R.drawable.ic_user_placeholder_circle,
                    true
                )
            } else {
                binding.cvAvatarColor.show()
                binding.imgAvatarColor.setImageResource(callLogData.color)
                val listString = callLogData.full_name?.split("\\s".toRegex())
                binding.tvNameAvatar.text = if (listString?.size ?: 0 > 1) {
                    String.format(
                        "%s%s",
                        listString?.get(0)?.get(0) ?: "",
                        listString?.get(1)?.get(0) ?: ""
                    )
                } else {
                    if (callLogData.full_name ?: "" != "") {
                        (callLogData.full_name?.get(0) ?: "").toString()
                    }else{
                        callLogData.phone?.slice(0..2)
                    }
                }
            }

            binding.tvNameContact.text = if (callLogData.full_name ?: "" != "") {
                callLogData.full_name
            } else {
                callLogData.phone ?: ""
            }

            val date = when (callLogData.type?.toInt()) {
                CallLog.Calls.INCOMING_TYPE -> {
                    String.format(
                        "%s: %s",
                        context.resources.getString(R.string.to),
                        TimeFormatHelper.convertMillieToHMmSs(
                            (callLogData.call_duration ?: "0").toLong()
                        )
                    )

                }
                CallLog.Calls.OUTGOING_TYPE -> {
                    if ((callLogData.call_duration ?: "0").toInt() == 0) {
                        context.resources.getString(R.string.disconnect)
                    } else {
                        String.format(
                            "%s: %s",
                            context.resources.getString(R.string.go),
                            TimeFormatHelper.convertMillieToHMmSs(
                                (callLogData.call_duration ?: "0").toLong()
                            )
                        )
                    }

                }
                CallLog.Calls.MISSED_TYPE -> {
                    context.resources.getString(R.string.missed_call)
                }
                CallLog.Calls.VOICEMAIL_TYPE -> {
                    ""
                }
                CallLog.Calls.REJECTED_TYPE -> {
                    context.getString(R.string.refused)
                }
                CallLog.Calls.BLOCKED_TYPE -> {
                    context.getString(R.string.blocked)
                }
                CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> {
                    ""
                }
                else -> {
                    ""
                }
            }
            binding.tvPhoneContact.text =
                String.format(
                    "%s %s %s",
                    callLogData.phone ?: "",
                    getTime(callLogData.call_day_time),
                    date
                )
            binding.rbChooseContact.isChecked = callLogData.isCheck
            binding.root.setOnClickListener {
                recentCallsHandler?.onChoose(callLogData)
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun getTime(date: Date?): String? {
            if (date == null) {
                return ""
            }
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date)
        }
    }

}