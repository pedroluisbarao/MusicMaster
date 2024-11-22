package com.plb.musicmaster

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import android.widget.Toast
import android.content.ContentUris
import android.content.Intent


class MusicServer(private val context: Context, private val port: Int) {
    private var serverSocket: ServerSocket? = null
    private var mediaPlayer: MediaPlayer? = null
    private var musicUri: Uri? = null
    //private var modeID: Int = 1

    fun start() {
        Thread {
            try {
                serverSocket = ServerSocket(port)
                while (true) {
                    val clientSocket = serverSocket?.accept()
                    handleClient(clientSocket)

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    /*private fun handleClient(clientSocket: Socket?) {
        clientSocket?.let { socket ->
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)

            try {
                var message: String?
                while (reader.readLine().also { message = it } != null) {
                    when (message) {
                        "play" -> {
                            playMusic()
                            writer.println("Music is playing")
                        }
                        "stop" -> {
                            stopMusic()
                            writer.println("Music is stopped")
                        }
                        else -> writer.println("Unknown command")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    socket.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }*/

    private fun handleClient(clientSocket: Socket?) {
        clientSocket?.let { socket ->
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val writer = PrintWriter(OutputStreamWriter(socket.getOutputStream()), true)

            try {
                var message: String?
                while (reader.readLine().also { message = it } != null) {
                    when {
                        message!!.startsWith("play ") -> {
                            // Extract the music name by removing the "play " prefix
                            val musicName = message!!.substringAfter("play ").trim()
                            playSpecificMusic(musicName) // Pass the music name to the playMusic method
                            writer.println("Playing: $musicName")
                        }
                        message == "play" -> {
                            // Handle simple play command
                            playMusic() // Implement default play logic if needed
                            writer.println("Music is playing")
                        }
                        message == "stop" -> {
                            stopMusic()
                            writer.println("Music is stopped")
                        }
                        else -> writer.println("Unknown command")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    socket.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /*fun updateMode(mode: String){
        if (mode == "Slave") {
            modeID = 0
            //Toast.makeText(this, "Slave mode activated", Toast.LENGTH_SHORT).show()
        } else {
            modeID = 1
            //Toast.makeText(this, "Master mode activated", Toast.LENGTH_SHORT).show()

        }
    }*/

    fun updateMusicUri(uri: Uri) {
        musicUri = uri
        mediaPlayer?.release() // Release previous player if exists
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, uri)
            prepare()
        }
    }

    fun playSpecificMusic(musicName: String) {
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(musicName)

        val cursor = context.contentResolver.query(musicUri, projection, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val uri = ContentUris.withAppendedId(musicUri, id)

                mediaPlayer?.reset()
                mediaPlayer?.setDataSource(context, uri)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                //createMediaNotification()
            } else {
                Toast.makeText(context, "Music file not found", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(context, "Error accessing media library", Toast.LENGTH_SHORT).show()
        }
    }

    fun playMusic() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    fun stopMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun stopServer() {
        serverSocket?.close()
        mediaPlayer?.release()
    }

    /*private fun createMediaNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, NotificationReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing Music")
            .setContentText("Your music is playing")
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(null))

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }*/


}
