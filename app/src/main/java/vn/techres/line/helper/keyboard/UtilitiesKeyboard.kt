package vn.techres.line.helper.keyboard

import android.content.Context
import android.widget.LinearLayout
import com.aghajari.emojiview.view.AXEmojiLayout
import vn.techres.line.R

class UtilitiesKeyboard(mContext: Context?) : AXEmojiLayout(mContext) {
    private var utilitiesChatHandler : UtilitiesChatHandler? = null

    init {
        setData(mContext)
    }

    fun setUtilitiesChatHandler(utilitiesChatHandler : UtilitiesChatHandler){
        this.utilitiesChatHandler = utilitiesChatHandler
    }

    fun setData(mContext: Context?){
        inflate(mContext, R.layout.item_footer_chat, this)
        findViewById<LinearLayout>(R.id.lnBusinessCard).setOnClickListener {
            utilitiesChatHandler?.onChooseBusinessCard()
        }
        findViewById<LinearLayout>(R.id.lnFile).setOnClickListener {
            utilitiesChatHandler?.onChooseFile()
        }

        findViewById<LinearLayout>(R.id.lnVote).setOnClickListener {
            utilitiesChatHandler?.onVote()
        }

        findViewById<LinearLayout>(R.id.lnMap).setOnClickListener {

        }

        findViewById<LinearLayout>(R.id.lnRepeat).setOnClickListener {

        }
        findViewById<LinearLayout>(R.id.lnMessageImportant).setOnClickListener {
            utilitiesChatHandler?.onImportantMessage()
        }
    }

}