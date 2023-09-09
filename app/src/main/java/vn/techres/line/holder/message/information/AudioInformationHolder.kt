package vn.techres.line.holder.message.information

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import vn.techres.line.R
import vn.techres.line.helper.utils.ChatUtils
import vn.techres.line.helper.TimeFormatHelper
import vn.techres.line.helper.Utils.getImage
import vn.techres.line.interfaces.chat.ChatGroupHandler
import vn.techres.line.data.model.chat.MessagesByGroup
import vn.techres.line.data.model.utils.ConfigNodeJs
import vn.techres.line.data.model.utils.User
import java.io.IOException
import java.util.concurrent.TimeUnit

class AudioInformationHolder(view : View) : RecyclerView.ViewHolder(view) {
    private var run: Runnable? = null
    private val seekHandler = Handler(Looper.myLooper()!!)

    private var imgPlayAudio: ImageView = view.findViewById(R.id.imgPlayAudio)
    private var seekBarAudio: SeekBar = view.findViewById(R.id.seekBarAudio)
    private var tvTimePlayAudio: TextView = view.findViewById(R.id.tvTimePlayAudio)
    private var tvTimeAudio: TextView = view.findViewById(R.id.tvTimeAudio)
    private var imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
    private var tvNameAudio: TextView = view.findViewById(R.id.tvNameAudio)
    private var cvAudio: CardView = view.findViewById(R.id.cvAudio)

    //reaction
    private var lnReactionContainer: LinearLayout = view.findViewById(R.id.lnReactionContainer)
    private var lnReaction: LinearLayout = view.findViewById(R.id.lnReaction)
    private var imgReactionOne: ImageView = view.findViewById(R.id.imgReactionOne)
    private var imgReactionTwo: ImageView = view.findViewById(R.id.imgReactionTwo)
    private var imgReactionThree: ImageView = view.findViewById(R.id.imgReactionThree)

    //press reaction
    private var imgReactionPress: ImageView = view.findViewById(R.id.imgReactionPress)

    //count
    private var tvReactionCount: TextView = view.findViewById(R.id.tvReactionCount)

    //today
    private var tvTimeHeader: TextView = view.findViewById(R.id.tvTimeHeader)

    fun bin(
        user: User,
        configNodeJs: ConfigNodeJs,
        position: Int,
        dataSource : ArrayList<MessagesByGroup>,
        chatGroupHandler: ChatGroupHandler?
    ){
        val message = dataSource[position]
//        holder.imgReplyAction.background = MessageSwipeActionDrawable(context)
        tvNameAudio.text = message.sender.full_name
        getImage(imgAvatar, message.sender.avatar?.medium, configNodeJs)
        tvTimeAudio.text = TimeFormatHelper.getDateFromStringDay(
            message.created_at
        )
        /**
         * visible and gone avatar user
         * */
        if (!ChatUtils.checkSenderMessage(message, user)) {
            if (position >= 0 && (position + 1) < dataSource.size) {
                if (dataSource[position].sender.member_id == dataSource[position + 1].sender.member_id) {
                    tvNameAudio.visibility = View.GONE
                    cvAudio.visibility = View.INVISIBLE
                } else {
                    tvNameAudio.visibility = View.VISIBLE
                    cvAudio.visibility = View.VISIBLE
                }
            }
        } else {
            cvAudio.visibility = View.GONE
            tvNameAudio.visibility = View.GONE
        }

        /**
         * time message
         * */
        if (message.today == 1) {
            tvTimeHeader.visibility = View.VISIBLE
            tvTimeHeader.text = TimeFormatHelper.getDateFromStringDayMonthYear(
                message.interval_of_time
            )
        } else tvTimeHeader.visibility = View.GONE
        /**
         * create and set time audio
         * */
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            if (message.local == 1) {
                mediaPlayer.setDataSource(message.files[0].link_original)
            } else {
                mediaPlayer.setDataSource(ChatUtils.getUrl(message.files[0].link_original, configNodeJs))
            }
            mediaPlayer.prepare() // might take long for buffering.
        } catch (e: IOException) {
            e.printStackTrace()
        }
        seekBarAudio.max = mediaPlayer.duration
        seekBarAudio.tag = position
        seekBarAudio.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        tvTimePlayAudio.text = String.format(
            "0:00/%s",
            TimeFormatHelper.calculateDuration(mediaPlayer.duration)
        )
        imgPlayAudio.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                imgPlayAudio.setImageResource(R.drawable.ic_pause_chat)
                run = Runnable {
                    // Updateing SeekBar every 100 miliseconds
                    seekBarAudio.progress = mediaPlayer.currentPosition
                    seekHandler.postDelayed(run!!, 100)
                    //For Showing time of audio(inside runnable)
                    val miliSeconds = mediaPlayer.currentPosition
                    if (miliSeconds != 0) {
                        //if audio is playing, showing current time;
                        val minutes =
                            TimeUnit.MILLISECONDS.toMinutes(
                                miliSeconds.toLong()
                            )
                        val seconds =
                            TimeUnit.MILLISECONDS.toSeconds(
                                miliSeconds.toLong()
                            )
                        if (minutes == 0L) {
                            tvTimePlayAudio.text = String.format(
                                "0:%s/%s",
                                seconds,
                                TimeFormatHelper.calculateDuration(mediaPlayer.duration)
                            )
                        } else {
                            if (seconds >= 60) {
                                val sec = seconds - minutes * 60
                                tvTimePlayAudio.text = String.format(
                                    "%s:%s/%s",
                                    minutes,
                                    sec,
                                    TimeFormatHelper.calculateDuration(mediaPlayer.duration)
                                )
                            }
                        }
                    } else {
                        //Displaying total time if audio not playing
                        val totalTime = mediaPlayer.duration
                        val minutes =
                            TimeUnit.MILLISECONDS.toMinutes(
                                totalTime.toLong()
                            )
                        val seconds =
                            TimeUnit.MILLISECONDS.toSeconds(
                                totalTime.toLong()
                            )
                        if (minutes == 0L) {
                            tvTimePlayAudio.text = String.format("0:%s", seconds)
                        } else {
                            if (seconds >= 60) {
                                val sec =
                                    seconds - minutes * 60
                                tvTimePlayAudio.text = String.format("%s:%s", minutes, sec)
                            }
                        }
                    }
                }
                run!!.run()
            } else {
                mediaPlayer.pause()
                imgPlayAudio.setImageResource(R.drawable.ic_play_button)
            }
        }
        /**
         * Set reaction
         * */
        if (message.reactions.reactions_count == 0) {
            lnReaction.visibility = View.GONE
            imgReactionPress.visibility = View.VISIBLE
            imgReactionPress.setImageResource(R.drawable.ic_heart_border_black)
        } else {
//            ChatUtils.setReaction(
//                lnReactionContainer,
//                tvReactionCount,
//                message
//            )
        }

        imgReactionPress.setOnClickListener {
            chatGroupHandler!!.onPressReaction(message, it)
        }
    }
}