package com.plb.musicmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class SongsFolderAdapter(private val audioList: List<MainActivity.AudioFile>, private val onClick: (String, String) -> Unit) :
    RecyclerView.Adapter<SongsFolderAdapter.SongsFolderViewHolder>() {

    class SongsFolderViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsFolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.audio_list_item, parent, false)
        return SongsFolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongsFolderViewHolder, position: Int) {
        val audioFile = audioList[position] // Now this is of type AudioFile
        val fileName = File(audioFile.path).name
        val title = audioFile.title // Get the title from the AudioFile data class
        val textView = holder.view.findViewById<TextView>(R.id.audioFileName)
        val imageView = holder.view.findViewById<ImageView>(R.id.audioFileIcon)

        textView.text = "   $title" // Set the title as the text
        SongActivity.MusicUtils.getMP3Metadata(audioFile.path, imageView) // Set the icon
        holder.view.setOnClickListener { onClick(audioFile.title, audioFile.path) }
    }

    override fun getItemCount(): Int = audioList.size
}
