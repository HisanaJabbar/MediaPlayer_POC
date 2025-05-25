package com.hisana.mediaplayer

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hisana.mediaplayer.databinding.ItemsMediaPlayerListBinding

class MediaPlayerListAdapter(
    private val data : List<Data>
):RecyclerView.Adapter<MediaPlayerListAdapter.MediaPlayerListViewHolder>(){
    inner class MediaPlayerListViewHolder(val binding : ItemsMediaPlayerListBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPlayerListViewHolder {
        val view = ItemsMediaPlayerListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MediaPlayerListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: MediaPlayerListViewHolder, position: Int) {
        val item = data[position]
        holder.binding.apply {
            tvTitle.text = item.title
            tvArtistName.text = item.artist

            root.setOnClickListener {
                val uriString = item.uri.toString()
                Log.d("TAG","URI : $uriString")
                val intent = Intent(root.context,MediaPlayerActivity::class.java).apply {
                    putExtra("MUSIC_URI",uriString)
                }
                root.context.startActivity(intent)
            }
        }
    }
}