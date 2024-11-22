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
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView


class MusicServer(private val context: Context, private val port: Int) {
    private var serverSocket: ServerSocket? = null
    private var mediaPlayer: MediaPlayer? = null
    //private var musicUri: Uri? = null
    private var musicServerListener: MusicServerListener? = null
    //private var modeID: Int = 1
    private var seekBar: SeekBar? = null
    private var timeRight: TextView? = null
    private var timeLeft: TextView? = null
    private val handler = Handler(Looper.getMainLooper())
    private val musicList = mutableListOf<Pair<String, Uri>>()
    private var currentMusicIndex = -1

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

    fun setSeekBar(seekBar: SeekBar, timeLeft: TextView, timeRight: TextView) {
        this.seekBar = seekBar
        this.timeRight = timeRight
        this.timeLeft = timeLeft
        setupSeekBar()
    }

    fun setMusicServerListener(listener: MusicServerListener) {
        this.musicServerListener = listener
    }

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

    /*fun loadMusicList() {
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME)

        val cursor = context.contentResolver.query(musicUri, projection, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val songName = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                val uri = ContentUris.withAppendedId(musicUri, id)
                musicList.add(Pair(songName, uri))
                Log.d("MusicList", "Title: $songName, URI: $musicUri")
            }
        }

        // Sort the musicList alphabetically by song name
        musicList.sortBy { it.first }

        // Set the current index to 0 if there are any songs
        if (musicList.isNotEmpty()) {
            currentMusicIndex = 0
        }
    }*/

    fun loadMusicList() {
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE)

        // Specify sort order by song name (DISPLAY_NAME) in ascending order
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor = context.contentResolver.query(musicUri, projection, null, null, sortOrder)
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val songName = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val uri = ContentUris.withAppendedId(musicUri, id)
                musicList.add(Pair(songName, uri))
                Log.d("Music Server List", "Title: $title")
            }
        }

        // Set the current index to 0 if there are any songs
        if (musicList.isNotEmpty()) {
            currentMusicIndex = 0
        }
    }

    fun playSpecificMusic(musicName: String) {
        // Search for the music by name and play it
        val music = musicList.find { it.first.equals(musicName, ignoreCase = true) }
        if (music != null) {
            // Set the current music index to the index of the selected music
            currentMusicIndex = musicList.indexOf(music) // Update index to the new music

            mediaPlayer?.reset()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, music.second)
                prepare()
            }
            mediaPlayer?.start()
            seekBar?.max = songDuration()
            setupSeekBar()
            mediaPlayer?.setOnCompletionListener {
                musicServerListener?.onMusicStopped(music.first)
                //playNextMusic() // Automatically play the next music after completion
            }
            // Optionally show a toast or update UI here
        } else {
            Toast.makeText(context, "Music file not found", Toast.LENGTH_SHORT).show()
        }
    }

    /*    fun playSpecificMusic(musicName: String) {
        // Search for the music by name and play it
        val music = musicList.find { it.first.equals(musicName, ignoreCase = true) }
        if (music != null) {
            mediaPlayer?.reset()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, music.second)
                prepare()
            }
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                musicServerListener?.onMusicStopped(music.first)
                //playNextMusic()
            }
            //Toast.makeText(context, "Playing: ${music.first}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Music file not found", Toast.LENGTH_SHORT).show()
        }
    }*/


    /*fun playSpecificMusic(musicName: String) {
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
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, uri)
                    prepare()
                }
                mediaPlayer?.start()
                //createMediaNotification()
            } else {
                Toast.makeText(context, "Music file not found", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                Toast.makeText(context, "Error accessing media library", Toast.LENGTH_SHORT).show()
            }

        }
    }*/

    fun playMusic() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                seekBar?.max = songDuration()
                setupSeekBar()
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

    fun playNextMusic() {
        if (musicList.isEmpty()) return

        // Move to the next music
        currentMusicIndex = (currentMusicIndex + 1) % musicList.size // Loop back to start
        playMusicFromCurrentIndex()
    }

    fun playPreviousMusic() {
        if (musicList.isEmpty()) return

        // Move to the previous music
        currentMusicIndex = (currentMusicIndex - 1 + musicList.size) % musicList.size // Loop back to end
        playMusicFromCurrentIndex()
    }

    private fun playMusicFromCurrentIndex() {
        if (currentMusicIndex in musicList.indices) {
            val music = musicList[currentMusicIndex] // Get the Pair (songName, Uri)
            mediaPlayer?.reset()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, music.second) // Use the Uri
                prepare()
            }
            //Toast.makeText(context, "Playing: ${music.first}", Toast.LENGTH_SHORT).show()
            mediaPlayer?.start()
            seekBar?.max = songDuration()
            setupSeekBar()
            mediaPlayer?.setOnCompletionListener {

                musicServerListener?.onMusicStopped(music.first)
                //playNextMusic()
            }

        } else {
            Toast.makeText(context, "No music to play", Toast.LENGTH_SHORT).show()
        }
    }

    fun getSongName(): String {
        return if (currentMusicIndex in musicList.indices) {
            musicList[currentMusicIndex].first // Return the song name from the Pair
        } else {
            "No music playing"
        }
    }

    fun getSongTitle(): String{
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(getSongName())

        val cursor = context.contentResolver.query(musicUri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                return title
            }
        }
        return "No music playing"
    }

    fun getSongPath(): String {
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(getSongName())

        val cursor = context.contentResolver.query(musicUri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                //val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                return path
            }
        }
        return "No music playing"
    }

    fun getSongPathFromName(musicName: String): String {
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(musicName)

        val cursor = context.contentResolver.query(musicUri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                //val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                return path
            }
        }
        return "No music playing"
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    fun songDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }


    private fun setupSeekBar() {
        if(seekBar!=null) {
            mediaPlayer?.let { player ->
                seekBar!!.max = player.duration

                val updateSeekBarRunnable = object : Runnable {
                    override fun run() {
                        if (player.isPlaying) {
                            seekBar!!.progress = player.currentPosition
                            val currentMinutes = player.currentPosition / 1000 / 60
                            val currentSeconds = player.currentPosition / 1000 % 60
                            val durationMinutes = player.duration / 1000 / 60
                            val durationSeconds = player.duration / 1000 % 60
                            timeLeft!!.text = String.format("%d:%02d", currentMinutes, currentSeconds)
                            timeRight!!.text = String.format("%d:%02d", durationMinutes, durationSeconds)
                            handler.postDelayed(this, 1000)
                        }
                    }
                }

                handler.post(updateSeekBarRunnable)

                // Handle seek bar changes from the user
                seekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            player.seekTo(progress)
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        handler.removeCallbacks(updateSeekBarRunnable)
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        handler.post(updateSeekBarRunnable)
                    }
                })
            }
        }
    }
    /*private fun setupSeekBar(seekBar: SeekBar) {
        // Set the maximum value of the seek bar to the duration of the song
        mediaPlayer?.setOnPreparedListener {
            seekBar.max = mediaPlayer?.duration ?: 0
        }

        // Update seek bar periodically as the song progresses
        val updateSeekBarRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    seekBar.progress = player.currentPosition
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }

        // Start updating the seek bar when the song starts
        mediaPlayer?.start()
        handler.post(updateSeekBarRunnable)

        // Allow the user to seek through the song
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Pause the update while the user is dragging the seek bar
                handler.removeCallbacks(updateSeekBarRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Resume updating the seek bar when the user releases the slider
                handler.post(updateSeekBarRunnable)
            }
        })
    }*/




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

