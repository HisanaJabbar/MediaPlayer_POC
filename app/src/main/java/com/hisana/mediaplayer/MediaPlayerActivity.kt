package com.hisana.mediaplayer

import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.util.concurrent.TimeUnit

class MediaPlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    //private lateinit var musicList : ArrayList<DataOne>
    private var currentPosition : Int = 0

    private lateinit var seekBar : SeekBar
    private lateinit var textCurrentTime : TextView
    private lateinit var textTotalTime : TextView
    private lateinit var playBtn : ImageView
    private lateinit var pauseBtn : ImageView
    private lateinit var stopBtn : ImageView
    private lateinit var nextBtn : ImageView
    private lateinit var previousBtn : ImageView

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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)//getParcelableArrayListExtra is deprecated so we have to add this for adding data class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        seekBar = findViewById(R.id.seekBar)
        textCurrentTime = findViewById(R.id.textCurrentTime)
        textTotalTime = findViewById(R.id.textTotalTime)
        playBtn = findViewById(R.id.buttonPlay)
        pauseBtn = findViewById(R.id.buttonPause)
        stopBtn = findViewById(R.id.buttonStop)
        nextBtn = findViewById(R.id.buttonPlayNext)
        previousBtn = findViewById(R.id.buttonPlayBack)

        Log.d("TAG", "Intent has song list? ${intent.hasExtra("SONGS_LIST")}")

         val musicList = intent.getParcelableArrayListExtra<DataOne>("SONGS_LIST")!!
        for(item in musicList){
            Log.d("TAG","MUSIC LIST : $item")
        }

        currentPosition = intent.getIntExtra("CURRENT_POSITION",0)

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

        mediaPlayer = MediaPlayer.create(this,musicUri)//create MediaPlayer instance with audio

        mediaPlayer.setOnPreparedListener{
            seekBar.max = it.duration
            textTotalTime.text = formatTime(it.duration)
        }//configuring seekbar and time after mediaPlayer is ready

        playBtn.setOnClickListener{
            mediaPlayer?.start()
            handler.post(updateSeekbar)
            Log.d("TAG","Current Position : $currentPosition")

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

            }
        )

        nextBtn.setOnClickListener{
            if(musicList.isNotEmpty()){
                currentPosition = (currentPosition + 1)
                Log.d("TAG","Current Position after clicking next button : $currentPosition")

                val nextSong = musicList[currentPosition]
                Log.d("TAG","NextSong : ${nextSong}")
                if(nextSong.uri.isNotEmpty()){
                    playNextSong(nextSong.uri)
                }else{
                    Toast.makeText(this,"Song not available",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Music List is Empty",Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun playNextSong(uriSong : String){
        val uri = Uri.parse(uriSong)

        if(::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MediaPlayerActivity,uri)
            prepare()
            start()
        }
        seekBar.max = mediaPlayer.duration
        textTotalTime.text = formatTime(mediaPlayer.duration)
        handler.post(updateSeekbar)
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