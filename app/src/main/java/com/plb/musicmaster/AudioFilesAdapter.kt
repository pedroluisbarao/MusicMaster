package com.plb.musicmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class AudioFilesAdapter(private val audioList: List<String>, private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<AudioFilesAdapter.AudioViewHolder>() {

    class AudioViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.audio_list_item, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFilePath = audioList[position]
        val fileName = File(audioFilePath).name
        val textView = holder.view.findViewById<TextView>(R.id.audioFileName)
        textView.text = fileName.dropLast(4) // Remove the file extension
        holder.view.setOnClickListener { onClick(audioFilePath) }
    }

    override fun getItemCount() = audioList.size
}
