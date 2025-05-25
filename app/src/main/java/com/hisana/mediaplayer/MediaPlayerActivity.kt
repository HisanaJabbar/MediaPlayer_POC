package com.hisana.mediaplayer

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.TimeUnit

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var seekBar : SeekBar
    private lateinit var textCurrentTime : TextView
    private lateinit var textTotalTime : TextView
    private lateinit var playBtn : ImageView
    private lateinit var pauseBtn : ImageView
    private lateinit var stopBtn : ImageView

    private val handler = Handler(Looper.getMainLooper())//it will update seek bar and current time text in every sec

    private val updateSeekbar : Runnable = object : Runnable{
        override fun run() {
            if(::mediaPlayer.isInitialized && mediaPlayer.isPlaying){
                seekBar.progress = mediaPlayer.currentPosition
                textCurrentTime.text = formatTime(mediaPlayer.currentPosition)

                handler.postDelayed(this,1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        seekBar = findViewById(R.id.seekBar)
        textCurrentTime = findViewById(R.id.textCurrentTime)
        textTotalTime = findViewById(R.id.textTotalTime)
        playBtn = findViewById(R.id.buttonPlay)
        pauseBtn = findViewById(R.id.buttonPause)
        stopBtn = findViewById(R.id.buttonStop)

        val musicString = intent.getStringExtra("MUSIC_URI")

        Log.d("TAG","Selected Music Uri: $musicString")

        if(musicString == null){
            Log.d("TAG","Selected music Uri is null")
            Toast.makeText(this,"No song selected",Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        else{
            Log.d("TAG","Selected music uri is success")
        }
        val musicUri = Uri.parse(musicString)

        /*mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MediaPlayerActivity,musicUri)
            setOnPreparedListener{
                seekBar.max = it.duration
                textTotalTime.text = formatTime(it.duration)
            }
            prepareAsync()
        }*/

        mediaPlayer = MediaPlayer.create(this,musicUri)//create MediaPlayer instance with audio

        mediaPlayer.setOnPreparedListener{
            seekBar.max = it.duration
            textTotalTime.text = formatTime(it.duration)
        }//configuring seekbar and time after mediaPlayer is ready

        playBtn.setOnClickListener{
            mediaPlayer?.start()
            handler.post(updateSeekbar)
        }

        pauseBtn.setOnClickListener{
            mediaPlayer.pause()
        }

        //stop button stop and reset ui and mediaPlayer
        stopBtn.setOnClickListener{
            mediaPlayer.stop()
            mediaPlayer = MediaPlayer.create(this,musicUri)
            mediaPlayer.prepareAsync()
            seekBar.progress = 0
            textCurrentTime.text = "0.00"
            textTotalTime.text = formatTime(mediaPlayer.duration)
        }

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar, progress : Int, fromUser : Boolean) {
                    if(fromUser){
                        mediaPlayer.seekTo(progress)
                        textTotalTime.text = formatTime(progress)
                    }
                }//function calls when user drag the seekbar not programmatically

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }

            })

    }

    private fun formatTime(milliseconds: Int):String{
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%d:%02d",minutes,seconds)
    }

    // // Clean up MediaPlayer and handler when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekbar){
            if(::mediaPlayer.isInitialized){
                mediaPlayer.release()
            }
        }
    }
}