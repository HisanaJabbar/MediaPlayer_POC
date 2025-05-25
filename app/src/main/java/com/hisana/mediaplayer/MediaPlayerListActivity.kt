package com.hisana.mediaplayer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hisana.mediaplayer.databinding.ActivityMediaPlayerListBinding

class MediaPlayerListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaPlayerListBinding
    private var musicList = mutableListOf<Data>()
    private lateinit var adapter: MediaPlayerListAdapter

    companion object{
        private const val REQUEST_CODE_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaPlayerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MediaPlayerListAdapter(musicList)
        binding.mediaListRv.apply {
            layoutManager = LinearLayoutManager(this@MediaPlayerListActivity)
            adapter = this@MediaPlayerListActivity.adapter
        }
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == REQUEST_CODE_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadMusicFromDevice()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions(){
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                REQUEST_CODE_PERMISSION
            )
        } else {
            loadMusicFromDevice()
        }
    }

    private fun loadMusicFromDevice(){
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC "

        contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol     = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)


            while (cursor.moveToNext()) {
                val id     = cursor.getLong(idCol)
                val title  = cursor.getString(titleCol)
                val artist = cursor.getString(artistCol)
                val contentUri = ContentUris.withAppendedId(uri, id).toString()

                Log.d("TAG","URI: $contentUri,Title:$title,Artist:$artist")

                musicList += Data(contentUri,title,artist)

            }
            adapter.notifyDataSetChanged()
        }
    }
}