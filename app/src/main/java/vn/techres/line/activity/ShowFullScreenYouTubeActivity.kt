package vn.techres.line.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import vn.techres.line.base.BaseBindingActivity
import vn.techres.line.data.model.newfeed.YouTube
import vn.techres.line.databinding.ActivityShowFullScreenYoutubeBinding
import vn.techres.line.helper.techresenum.TechresEnum

class ShowFullScreenYouTubeActivity : BaseBindingActivity<ActivityShowFullScreenYoutubeBinding>() {
    private var youtube: YouTube? = null
    override val bindingInflater: (LayoutInflater) -> ActivityShowFullScreenYoutubeBinding
        get() = ActivityShowFullScreenYoutubeBinding::inflate

    override fun onSetBodyView() {
        setData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(binding.lnVideoYouTube)
    }

    private fun setData() {
        youtube = Gson().fromJson(
            intent.getStringExtra(TechresEnum.VIDEO_YOUTUBE.toString()),
            object : TypeToken<YouTube>() {}.type
        )


        if (youtube != null) {
            lifecycle.addObserver(binding.youtubePlayerView)
            binding.youtubePlayerView.isFullScreen()
            binding.youtubePlayerView.toggleFullScreen()
            binding.youtubePlayerView.addYouTubePlayerListener(object :
                AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadOrCueVideo(
                        lifecycle,
                        youtube!!.videoId ?: "",
                        youtube!!.currentSecond ?: 0f
                    )
                }
            })
            binding.youtubePlayerView.addFullScreenListener(object :
                YouTubePlayerFullScreenListener {
                override fun onYouTubePlayerEnterFullScreen() {
                }

                override fun onYouTubePlayerExitFullScreen() {
                    binding.youtubePlayerView.visibility = View.GONE
                    onBackPressed()
                }
            })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}