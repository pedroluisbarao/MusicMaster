package com.plb.musicmaster

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

class MusicPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun playMusic() {
        val musicUri = getMusicUri() // Obtém o URI da música que você deseja tocar
        if (musicUri != null) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, musicUri)
            mediaPlayer?.start()
        } else {
            Log.e("MusicPlayer", "Music URI is null, cannot play music.")
        }
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun getMusicUri(): Uri? {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME),
            null, null, null
        )
        cursor?.use {
            val idIndex = it.getColumnIndex(MediaStore.Audio.Media._ID)
            if (idIndex != -1 && it.moveToFirst()) {

                val id = it.getLong(idIndex)
                return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
            }
        }
        Log.e("MusicPlayer", "No music found or cursor is null.")
        return null
    }
}

