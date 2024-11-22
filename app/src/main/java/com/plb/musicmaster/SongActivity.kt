package com.plb.musicmaster

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SongActivity : AppCompatActivity(), MusicServerListener {

    private lateinit var playButton: Button
    private lateinit var songName: TextView
    private lateinit var songAlbumArtist: TextView
    private lateinit var songImageView: ImageView
    private lateinit var nextButton: Button
    private lateinit var previousButton: Button
    private lateinit var musicServer: MusicServer
    private lateinit var songLine: SeekBar
    private lateinit var timeRight: TextView
    private lateinit var timeLeft: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song)

        musicServer = MainActivity.MusicServerManager.musicServer!!
        musicServer.setMusicServerListener(this)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previousButton)
        songLine = findViewById(R.id.line)
        timeRight = findViewById(R.id.time_right)
        timeLeft = findViewById(R.id.time_left)
        musicServer.setSeekBar(songLine, timeLeft, timeRight)
        playButton = findViewById(R.id.playButton)
        songImageView = findViewById(R.id.songImageView)
        songName = findViewById(R.id.songName)
        songAlbumArtist = findViewById(R.id.songAlbumArtist)// Initialize ipInput here
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        var song = sharedPreferences.getString("lastplayedsong", "Over the Horizon")
        var songTitle = sharedPreferences.getString("lastplayedtitle", "Over the Horizon")
        var buttonState = sharedPreferences.getString("buttonState", "Play")
        val currentMode = sharedPreferences.getString("currentMode", "Slave")

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            // Handle back navigation
            onBackPressed()
        }


        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true) // Show the back button


        toolbar.setNavigationOnClickListener {
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        if(buttonState=="Play"){ //update the buttons
            playButton.setBackgroundResource(R.drawable.button_play)
        }
        else{
            playButton.setBackgroundResource(R.drawable.button_pause)
        }

        val mp3Info= MusicUtils.getMP3Metadata(musicServer.getSongPathFromName(song!!), songImageView)
        songName.text = songTitle
        songAlbumArtist.text = "${mp3Info["album"]} - ${mp3Info["artist"]}"


        playButton.setOnClickListener {
            if(buttonState=="Play"){
                if (currentMode == ("Master")) {
                    val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
                    val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
                    client.sendCommand("play")
                    Toast.makeText(this, "Play command sent to $ipAddress", Toast.LENGTH_SHORT).show()
                    //playMusic()
                }
                else{
                    //val song = sharedPreferences.getString("lastplayedsong", "Over the Horizon")
                    if (song != null){
                        //musicServer.playSpecificMusic(song)
                        musicServer.playMusic()
                    }

                }
                buttonState = "Pause"
                MusicUtils.updatePlayButton(this, playButton,"Pause")
            }
            else{
                if (currentMode == ("Master")) {
                    val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
                    val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
                    client.sendCommand("stop")
                    Toast.makeText(this, "Stop command sent to $ipAddress", Toast.LENGTH_SHORT).show()
                }
                else{
                    musicServer.stopMusic()
                }
                buttonState = "Play"
                MusicUtils.updatePlayButton(this, playButton,"Play")
            }
        }

        nextButton.setOnClickListener {
            if(!musicServer.isPlaying()){ //avoid playing the song after the first one of the list
                if (song != null) {
                    musicServer.playSpecificMusic(song)

                }
            }
            if (currentMode == ("Master")) {
                val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
                val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
                client.sendCommand("next")
                Toast.makeText(this, "Next command sent to $ipAddress", Toast.LENGTH_SHORT).show()
                //nextMusic()
            }
            else{
                musicServer.playNextMusic()
                //MusicUtils.updateSongName(this, musicServer.getSongName())
                //Log.d("next", "songNew: $songNew, songSharedPreferences: $song")
            }
            buttonState = "Pause"
            MusicUtils.updatePlayButton(this, playButton,"Pause")
            val mp3info = MusicUtils.getMP3Metadata(musicServer.getSongPath(), songImageView)
            MusicUtils.updateSongInfo(this, songName,musicServer.getSongName(), musicServer.getSongTitle(), mp3info["album"], mp3info["artist"])
        }

        previousButton.setOnClickListener {
            if(!musicServer.isPlaying()){ //avoid playing the song after the first one of the list
                if (song != null) {
                    musicServer.playSpecificMusic(song)

                }
            }
            if (currentMode == ("Master")) {
                val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
                val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
                client.sendCommand("previous")
                Toast.makeText(this, "Previous command sent to $ipAddress", Toast.LENGTH_SHORT).show()
                //previousMusic()

            }
            else{
                musicServer.playPreviousMusic()
                //MusicUtils.updateSongName(this, musicServer.getSongName())
                //Log.d("previous", "songNew: $songNew, songSharedPreferences: $song")
            }
            buttonState = "Pause"
            MusicUtils.updatePlayButton(this, playButton, "Pause")

            val mp3info = MusicUtils.getMP3Metadata(musicServer.getSongPath(), songImageView)
            MusicUtils.updateSongInfo(this, songName,musicServer.getSongName(), musicServer.getSongTitle(), mp3info["album"], mp3info["artist"])
            //Log.d("mp3info", "album: ${mp3info["album"]}, artist: ${mp3info["artist"]}")
        }
        //MusicUtils.getMP3Metadata(musicServer.getSongPath(), songImageView)

    }




    object MusicUtils {

        fun updatePlayButton(context: Context, playButton: Button, state: String) {
            val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("buttonState", state)
            editor.apply()
            if(state == "Play") {
                playButton.setBackgroundResource(R.drawable.button_play)
            } else {
                playButton.setBackgroundResource(R.drawable.button_pause)
            }
        }

        fun updateSongInfo(context: Context, songName: TextView, songNew: String, title: String, album: String? = null, artist: String? = null) {
            val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("lastplayedsong", songNew)
            editor.putString("lastplayedtitle", title)
            editor.putString("lastplayedalbum", album)
            editor.putString("lastplayedartist", artist)
            editor.apply()
            //songName.text = songNew.dropLast(4)
            songName.text = title
            /*if(title.length>14)
                songName.textSize = 18f*/
            if(title.length>25)
                songName.textSize = 18f
            if(title.length>35)
                songName.textSize = 12f
            else if(title.length<=14)
                songName.textSize = 30f
        }
        fun getMP3Metadata(filePath: String, albumImageView: ImageView): Map<String, String?> {
            val retriever = MediaMetadataRetriever()

            // Use the file path to set the data source
            retriever.setDataSource(filePath)

            // Extract album, artist, and album art metadata
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            // Album art is retrieved as a byte array
            val albumArtBytes = retriever.embeddedPicture

            // Check if there is album art
            if (albumArtBytes != null) {
                // Convert the byte array to a Bitmap
                val albumCover = BitmapFactory.decodeByteArray(albumArtBytes, 0, albumArtBytes.size)
                // Set the album cover to an ImageView
                albumImageView.setImageBitmap(albumCover)
                Toast.makeText(albumImageView.context, "Album art found!!", Toast.LENGTH_SHORT).show()
            } else {
                // No album art found, set a default image
                albumImageView.setImageResource(R.drawable.music_master_icon2)
            }

            // Release the retriever after use
            retriever.release()

            return mapOf(
                "album" to album,
                "artist" to artist
            )
        }
    }



    override fun onMusicStopped(songName: String) {
        runOnUiThread {
            // Handle music stop here
            //Toast.makeText(this, "$songName has finished playing", Toast.LENGTH_SHORT).show()
            // Update your UI, switch to next song, etc.
            songLine.progress = 0
            nextButton.performClick()
        }
    }





}



