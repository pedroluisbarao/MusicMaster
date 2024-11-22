package com.plb.musicmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FolderAdapter(private val folders: Array<String>, private val clickListener: (String) -> Unit) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    class FolderViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folders_list_item, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        val textView = holder.view.findViewById<TextView>(R.id.FolderName)
        holder.itemView.setOnClickListener {
            clickListener(folders[position]) // Chama o listener ao clicar na pasta
        }
        textView.text = File(folder).name
    }

    override fun getItemCount(): Int = folders.size
}
