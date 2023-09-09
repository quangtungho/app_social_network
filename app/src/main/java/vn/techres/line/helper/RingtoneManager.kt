package vn.techres.line.helper

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RawRes
import timber.log.Timber
import java.io.IOException

class RingtoneManager(val context: Context) {
    private val tag = "RingtoneManager"

    private var uri: Uri? = null

    private val vibratePattern = longArrayOf(0, 1000, 500)

    private var vibrator: Vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private var player: MediaPlayer? = null

    private var audioManager : AudioManager? = null

    private var originalVolume : Int? = null

    private var originalStream : Boolean = false

    private lateinit var mOnRingtoneListener: OnRingtoneListener

    interface OnRingtoneListener {
        fun onComplete()
    }

    constructor(context: Context, @RawRes resource: Int) : this(context) {
        uri = Uri.parse("android.resource://" + context.packageName + "/" + resource)
    }

    fun start(looping: Boolean = true, vibrate: Boolean = false, complete: Boolean = false, ringerStream: Boolean = false, originalStream : Boolean = false) {
        this.originalStream = originalStream
        player?.release()
        if (uri == null) {
            uri = notificationUri()
            uri?.let { player = createMediaPlayer(AudioManager.STREAM_RING) }
        } else {
            if(ringerStream){
                uri?.let { player = createMediaPlayer(AudioManager.STREAM_MUSIC) }
            }else{
                uri?.let { player = createMediaPlayer(AudioManager.STREAM_VOICE_CALL) }
            }
        }

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        player?.isLooping = looping
        val ringerMode = audioManager?.ringerMode
        if(originalStream){
            this.originalVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0, 0)
        }
        if (isNeedVibrate(context, player, ringerMode ?: 0, vibrate)) {
            vibrator.let {
                if (Build.VERSION.SDK_INT >= 26) {
                    it.vibrate(
                        VibrationEffect.createWaveform(
                            vibratePattern,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(vibratePattern, 1)
                }
            }
        }

        player?.let {
            try {
                if (!it.isPlaying) {
                    it.prepare()
                    it.start()
                    Timber.d("play ringtone")
                } else {
                    Timber.d("Ringtone is playing already")
                }
            } catch (e: IllegalStateException) {
                Timber.d("e= $e")
                player = null
            }

        }
        if (complete) {
            player?.setOnCompletionListener {
                stop()
                mOnRingtoneListener.onComplete()
            }
        }
    }

    fun setOnCompleteListener(listener: OnRingtoneListener) {
        this.mOnRingtoneListener = listener
    }

    fun stop() {
        if (player != null) {
            Timber.d("stop ringer")
            player?.release()
            player = null
        }
        if(originalStream){
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume ?: 0, 0)
        }
        vibrator.cancel()
    }

    private fun createMediaPlayer(streamType: Int): MediaPlayer? {
        return try {
            val mediaPlayer = MediaPlayer()

            mediaPlayer.setOnErrorListener { _, _, _ ->
                player = null
                false
            }
            mediaPlayer.setDataSource(context, uri!!)
            mediaPlayer.setAudioStreamType(streamType)
            mediaPlayer
        } catch (e: IOException) {
            Timber.e(e)
            null
        }
    }

    private fun isNeedVibrate(
        context: Context,
        player: MediaPlayer?,
        ringerMode: Int,
        vibrate: Boolean
    ): Boolean {
        return if (player == null) {
            true
        } else {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (!vibrator.hasVibrator()) {
                return false
            }
            return if (vibrate) {
                ringerMode != AudioManager.RINGER_MODE_SILENT
            } else {
                ringerMode == AudioManager.RINGER_MODE_VIBRATE
            }
        }
    }

    private fun notificationUri(): Uri? {
        var uri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        if (uri == null) {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        }
        return uri
    }
}