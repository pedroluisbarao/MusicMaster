package com.plb.musicmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class AudioFilesAdapter(private val audioList: List<MainActivity.AudioFile>, private val onClick: (String, String) -> Unit) :
    RecyclerView.Adapter<AudioFilesAdapter.AudioViewHolder>() {

    class AudioViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.audio_list_item, parent, false)
        return AudioViewHolder(view)
    }

    /*override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFilePath = audioList[position]
        val fileName = File(audioFilePath).name
        val textView = holder.view.findViewById<TextView>(R.id.audioFileName)
        textView.text = fileName.dropLast(4) // Remove the file extension
        holder.view.setOnClickListener { onClick(audioFilePath) }
    }*/

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFile = audioList[position] // Now this is of type AudioFile
        val fileName = File(audioFile.path).name
        val title = audioFile.title // Get the title from the AudioFile data class
        val textView = holder.view.findViewById<TextView>(R.id.audioFileName)
        val imageView = holder.view.findViewById<ImageView>(R.id.audioFileIcon)

        textView.text = "   "+title // Set the title as the text
        SongActivity.MusicUtils.getMP3Metadata(audioFile.path, imageView) // Set the icon
        holder.view.setOnClickListener { onClick(audioFile.title, audioFile.path) }
    }

    override fun getItemCount() = audioList.size
}
