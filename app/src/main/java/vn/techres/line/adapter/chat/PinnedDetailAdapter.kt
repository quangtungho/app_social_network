package vn.techres.line.adapter.chat

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.view.*
import android.widget.PopupMenu
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Scale
import org.greenrobot.eventbus.EventBus
import vn.techres.line.R
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.databinding.ItemPinnedBinding
import vn.techres.line.helper.CurrentConfigNodeJs
import vn.techres.line.helper.StringUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.helper.Utils.getGlide
import vn.techres.line.helper.Utils.hide
import vn.techres.line.helper.Utils.show
import vn.techres.line.helper.Utils.showImage
import vn.techres.line.helper.techresenum.TechResEnumChat
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.utils.FileUtils.getMimeType
import vn.techres.line.interfaces.chat.EventBusScrollMessPin
import vn.techres.line.interfaces.chat.PinnedDetailHandler

class PinnedDetailAdapter(var context: Context) :
    RecyclerView.Adapter<PinnedDetailAdapter.ViewHolder>() {

    private var messagesByGroup = ArrayList<MessagesByGroup>()
    private lateinit var pinnedDetailHandler: PinnedDetailHandler
    private var configNodeJs = CurrentConfigNodeJs.getConfigNodeJs(context)

    fun setPinnedDetailHandler(pinnedDetailHandler: PinnedDetailHandler) {
        this.pinnedDetailHandler = pinnedDetailHandler
    }

    fun setDataSource(messagesByGroup: ArrayList<MessagesByGroup>) {
        this.messagesByGroup = messagesByGroup
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPinnedBinding.inflate(LayoutInflater.from(context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(context, configNodeJs, position, messagesByGroup[position], pinnedDetailHandler)
    }

    override fun getItemCount(): Int {
        return messagesByGroup.size
    }

    class ViewHolder(private val binding: ItemPinnedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            context: Context,
            configNodeJs: ConfigNodeJs,
            position: Int,
            messagesByGroup: MessagesByGroup,
            pinnedDetailHandler: PinnedDetailHandler
        ) {
            binding.imgPinnedUser.load(
                String.format(
                    "%s%s",
                    configNodeJs.api_ads,
                    messagesByGroup.sender.avatar?.medium
                )
            )
            {
                crossfade(true)
                scale(Scale.FILL)
                placeholder(R.drawable.logo_aloline_placeholder)
                error(R.drawable.logo_aloline_placeholder)
                size(300, 300)
            }
            binding.tvPinnedName.text = String.format(
                "%s %s", messagesByGroup.sender.full_name, context.resources.getString(
                    R.string.pinned_user
                )
            )
            binding.tvPinnedTime.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                messagesByGroup.created_at
            )
            binding.tvSenderPinned.text = messagesByGroup.message_pinned?.sender?.full_name ?: ""
            when (messagesByGroup.message_pinned?.message_type) {
                TechResEnumChat.TYPE_TEXT.toString(), TechResEnumChat.TYPE_REPLY.toString() -> {
                    binding.rlPinnedLinkContainer.hide()
                    binding.tvPinnedMessage.show()
                    var messageHtml = messagesByGroup.message_pinned?.message ?: ""
                    messagesByGroup.message_pinned?.list_tag_name?.let {
                        it.forEach { tagName ->
                            val name = String.format(
                                "%s%s%s",
                                "<font color='#198be3' >",
                                "@" + tagName.full_name,
                                "</font>"
                            )
                            messageHtml = messageHtml.replace(tagName.key_tag_name.toString(), name)
                        }
                    }
                    messageHtml = messageHtml.replace("\n", "<br>")
                    binding.tvPinnedMessage.text =
                        Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_COMPACT)

                }
                TechResEnumChat.TYPE_IMAGE.toString(), TechResEnumChat.TYPE_STICKER.toString() -> {
                    binding.rlPinnedLinkContainer.show()
                    binding.cvMedia.show()
                    binding.imgLink.show()
                    binding.tvLinkDescription.hide()
                    binding.tvPinnedMessage.hide()
                    binding.imgPinnedPlay.hide()
                    binding.imgAudio.hide()
                    binding.imgFile.hide()
                    getGlide(
                        binding.imgLink,
                        messagesByGroup.message_pinned?.files?.get(0)?.link_original,
                        configNodeJs
                    )
                    if (messagesByGroup.message_pinned?.message_type == TechResEnumChat.TYPE_IMAGE.toString()) {
                        binding.tvLinkTitle.text =
                            context.resources.getString(R.string.pinned_image)
                    } else {
                        binding.tvLinkTitle.text =
                            context.resources.getString(R.string.pinned_sticker)
                    }
                }
                TechResEnumChat.TYPE_VIDEO.toString() -> {
                    binding.rlPinnedLinkContainer.show()
                    binding.imgPinnedPlay.show()
                    binding.cvMedia.show()
                    binding.imgLink.show()
                    binding.tvLinkDescription.hide()
                    binding.tvPinnedMessage.hide()
                    binding.imgAudio.hide()
                    binding.imgFile.hide()
                    getGlide(
                        binding.imgLink,
                        messagesByGroup.message_pinned!!.files[0].link_original,
                        configNodeJs
                    )
                    binding.tvLinkTitle.text = context.getString(R.string.pinned_video)
                }
                TechResEnumChat.TYPE_AUDIO.toString() -> {
                    binding.rlPinnedLinkContainer.show()
                    binding.tvLinkDescription.hide()
                    binding.tvPinnedMessage.hide()
                    binding.cvMedia.hide()
                    binding.imgFile.hide()
                    binding.imgAudio.show()
                    binding.tvLinkTitle.text = context.getString(R.string.pinned_audio)
                }
                TechResEnumChat.TYPE_FILE.toString() -> {
                    binding.rlPinnedLinkContainer.show()
                    binding.tvLinkDescription.hide()
                    binding.tvPinnedMessage.show()
                    binding.cvMedia.hide()
                    binding.imgAudio.hide()
                    binding.imgFile.show()
                    binding.tvLinkTitle.text = context.getString(R.string.pinned_file)
                    binding.tvPinnedMessage.text =
                        messagesByGroup.message_pinned?.files?.get(0)?.name_file?.replace("%20", " ") ?: ""
                    binding.tvLinkDescription.text =
                        Utils.setSizeFile(messagesByGroup.message_pinned?.files?.get(0)?.size ?: 0)
                    val mineType =
                        messagesByGroup.message_pinned?.files?.get(0)?.link_original?.let {
                            getMimeType(it)
                        }
                    ChatUtils.setImageFile(binding.imgFile, mineType)
                }
                TechResEnumChat.TYPE_LINK.toString() -> {
                    binding.tvPinnedMessage.show()
                    binding.cvMedia.show()
                    binding.imgAudio.hide()
                    binding.imgFile.hide()
                    binding.imgPinnedPlay.hide()
                    var messageHtml = messagesByGroup.message_pinned?.message ?: ""
                    messagesByGroup.message_pinned?.list_tag_name?.let {
                        it.forEach { tagName ->
                            val name = String.format(
                                "%s%s%s",
                                "<font color='#198be3' >",
                                "@" + tagName.full_name,
                                "</font>"
                            )
                            messageHtml = messageHtml.replace(tagName.key_tag_name.toString(), name)
                        }
                    }
                    messageHtml = messageHtml.replace("\n", "<br>")
                    if (!StringUtils.isNullOrEmpty(messageHtml)) {
                        binding.tvPinnedMessage.show()
                        binding.tvPinnedMessage.text =
                            Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_COMPACT)
                    } else {
                        binding.tvPinnedMessage.hide()
                    }
                    binding.tvLinkTitle.text = context.getString(R.string.pinned_link)
                    if(messagesByGroup.message_pinned?.message_link?.url.equals("")) {
                        binding.tvLinkDescription.text = ""
                    } else {
                        binding.tvLinkDescription.text =
                            messagesByGroup.message_pinned?.message_link?.url
                    }
                    showImage(
                        binding.imgLink,
                        messagesByGroup.message_pinned?.message_link?.media_thumb
                    )
                }
                TechResEnumChat.TYPE_BUSINESS_CARD.toString() -> {
                    binding.rlPinnedLinkContainer.show()
                    binding.cvMedia.show()
                    binding.imgLink.show()
                    binding.tvLinkDescription.hide()
                    binding.tvPinnedMessage.hide()
                    binding.imgPinnedPlay.hide()
                    binding.imgAudio.hide()
                    binding.imgFile.hide()
                    getImage(
                        binding.imgLink,
                        messagesByGroup.message_pinned?.message_phone?.avatar?.medium,
                        configNodeJs
                    )
                    binding.tvLinkTitle.text = String.format(
                        "%s %s",
                        context.resources.getString(R.string.pinned_business_card),
                        messagesByGroup.message_pinned?.message_phone?.full_name ?: ""
                    )
                }
            }
            binding.imgPinnedMore.setOnClickListener {
//                button1Clicked(binding.imgPinnedMore, messagesByGroup, pinnedDetailHandler)
                if (messagesByGroup.status == 0) {
                    val popupMenu = PopupMenu(context, it)
                    popupMenu.menuInflater.inflate(R.menu.menu_retry_pin, popupMenu.getMenu())
                    popupMenu.show()

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        if (menuItem.itemId == R.id.action_delete_retry) {
                            pinnedDetailHandler.onRemove(messagesByGroup)
                        } else if (menuItem.itemId == R.id.action_retry) {
                            pinnedDetailHandler.onPinRetry(messagesByGroup)
                        }
                        false
                    }
                } else {
                    val popupMenu = PopupMenu(context, it)
                    popupMenu.menuInflater.inflate(R.menu.menu_un_pin, popupMenu.getMenu())
                    popupMenu.show()

                    popupMenu.setOnMenuItemClickListener { menuItem ->
                        if (menuItem.itemId == R.id.action_delete) {
                            pinnedDetailHandler.onRemove(messagesByGroup)
                        } else if (menuItem.itemId == R.id.action_unPin) {
                            pinnedDetailHandler.onPinUnpin(messagesByGroup)
                        }
                        false
                    }
                }

            }
            itemView.setOnClickListener {
                var eventBus = EventBusScrollMessPin()
                eventBus.random_key = messagesByGroup.message_pinned?.random_key.toString()
                EventBus.getDefault().post(eventBus)
            }
        }

        @SuppressLint("RestrictedApi")
        private fun button1Clicked(
            v: View,
            messagesByGroup: MessagesByGroup,
            pinnedDetailHandler: PinnedDetailHandler
        ) {
            val menuBuilder = MenuBuilder(v.context)
            val inflater = MenuInflater(v.context)
            val optionsMenu = MenuPopupHelper(v.context, menuBuilder, v)
            optionsMenu.setForceShowIcon(true)
            if (messagesByGroup.status == 0) {
                inflater.inflate(R.menu.menu_retry_pin, menuBuilder)
                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    @SuppressLint("NonConstantResourceId")
                    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                        return when (item.itemId) {
                            R.id.action_delete_retry -> {
                                run {
                                    pinnedDetailHandler.onRemove(messagesByGroup)
                                }
                                true
                            }
                            R.id.action_retry -> {
                                run {
                                    pinnedDetailHandler.onPinRetry(messagesByGroup)
                                }
                                true
                            }
                            else -> false
                        }
                    }

                    override fun onMenuModeChange(menu: MenuBuilder) {}
                })
            } else {
                inflater.inflate(R.menu.menu_un_pin, menuBuilder)
                menuBuilder.setCallback(object : MenuBuilder.Callback {
                    @SuppressLint("NonConstantResourceId")
                    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
                        return when (item.itemId) {
                            R.id.action_delete -> {
                                run {
                                    pinnedDetailHandler.onRemove(messagesByGroup)
                                }
                                true
                            }
                            R.id.action_unPin -> {
                                run {
                                    pinnedDetailHandler.onPinUnpin(messagesByGroup)
                                }
                                true
                            }
                            else -> false
                        }
                    }

                    override fun onMenuModeChange(menu: MenuBuilder) {}
                })
            }
            optionsMenu.show()
        }
    }
}