package com.plb.musicmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ArtistAdapter(private val artists: Array<String>, private val clickListener: (String) -> Unit) :
    RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folders_list_item, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]
        val textView = holder.view.findViewById<TextView>(R.id.FolderName)
        holder.itemView.setOnClickListener {
            clickListener(artists[position]) // Chama o listener ao clicar na pasta
        }
        textView.text = artist
    }

    override fun getItemCount(): Int = artists.size
}
