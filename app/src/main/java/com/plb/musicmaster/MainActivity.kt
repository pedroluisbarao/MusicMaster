package com.plb.musicmaster

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.util.Log
import android.widget.ImageView
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    //private lateinit var mediaPlayer: MediaPlayer
    private lateinit var moreButton: Button
    private lateinit var settingsButton: Button
    private lateinit var playButton: Button
    private lateinit var songImageView: ImageView
    private lateinit var songTextView: TextView
    //private lateinit var stopButton: Button
    //private lateinit var selectMusicTrack: Button
    private lateinit var musicServer: MusicServer
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var masterModeRadioButton: RadioButton
    private lateinit var slaveModeRadioButton: RadioButton
    //private lateinit var mediaControlHelper: MediaControlHelper
    private lateinit var audioFilesRecyclerView: RecyclerView
    private lateinit var audioFilesAdapter: AudioFilesAdapter

    //var serverIpAddress: String = "192.168.1.65" // Valor padrão
    private var currentMode: String = "Slave"
    //private var mediaPlayer: MediaPlayer? = null

    //private val MORE_ACTIVITY_REQUEST_CODE = 1
    private val REQUEST_CODE_PERMISSION = 100
    private val REQUEST_CODE = 314

    companion object {
        private const val PICK_MUSIC_REQUEST = 1
    }

    object MusicServerManager {
        var musicServer: MusicServer? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        moreButton = findViewById(R.id.moreButton)
        settingsButton = findViewById(R.id.settingsButton)
        playButton = findViewById(R.id.playButtonBottom)
        //stopButton = findViewById(R.id.stopButton)
        //selectMusicTrack = findViewById(R.id.selectSampleButton)
        modeRadioGroup = findViewById(R.id.modeRadioGroup)
        masterModeRadioButton = findViewById(R.id.masterMode)
        slaveModeRadioButton = findViewById(R.id.slaveMode)
        songImageView = findViewById(R.id.songImageView)
        songTextView = findViewById(R.id.songTextView)

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val serverIpAddress = sharedPreferences.getString("connectorIp", "192.168.1.633")
        val song = sharedPreferences.getString("lastplayedsong", "Over the Horizon")
        val songTitle = sharedPreferences.getString("lastplayedtitle", "Over the Horizon")
        Log.d("lastplayedsong", "Selected: $song")
        val editor = sharedPreferences.edit()
        editor.putString("buttonState", "Play")
        editor.apply()

        var buttonState = "Play"
        var counter = 0
        Toast.makeText(this, "Counter: $counter", Toast.LENGTH_SHORT).show()

        val deviceMode = sharedPreferences.getString("currentMode", "Slave")
        currentMode = deviceMode.toString()

        audioFilesRecyclerView = findViewById(R.id.audioFilesRecyclerView)
        audioFilesRecyclerView.layoutManager = LinearLayoutManager(this)

        checkStoragePermission()  // Solicita permissão e carrega arquivos de áudio


        //next e previous começam do inicio da lista. e nao donde esta a reproduao ataulmente!!! mas depoi de usar fica bem


        //mediaPlayer = MediaPlayer.create(this, R.raw.sample)

        musicServer = MusicServer(this, 12345)

        musicServer.start()
        musicServer.loadMusicList()

        // Set default mode
        masterModeRadioButton.isChecked = false
        slaveModeRadioButton.isChecked = true

        modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentMode = when (checkedId) {
                R.id.masterMode -> "Master"
                R.id.slaveMode -> "Slave"
                else -> "Master"
            }
            updateMode()
        }

        if(buttonState=="Play" && counter==0){   //entering the app
            playButton.setBackgroundResource(R.drawable.button_play)
        }





        if (song != null) {
            songTextView.text = songTitle
        }



        Toast.makeText(this, "Server started", Toast.LENGTH_SHORT).show()

        moreButton.setOnClickListener() {
            val intent = Intent(this, MoreActivity::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            //val intent = Intent(this, SettingsActivity::class.java)
            //startActivityForResult(intent, SETTINGS_REQUEST_CODE)
        }

        playButton.setOnClickListener{
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
                        if(counter==0){
                            musicServer.playSpecificMusic(song)
                            counter++
                        }
                        else
                            musicServer.playMusic()
                    }

                }
                buttonState = "Pause"
                SongActivity.MusicUtils.updatePlayButton(this, playButton, "Pause")
            }
            else{
                if (currentMode == ("Master")) {
                    val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
                    val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
                    client.sendCommand("stop")
                    Toast.makeText(this, "Stop command sent to $ipAddress", Toast.LENGTH_SHORT).show()
                    //stopMusic()
                }
                else{
                    musicServer.stopMusic()
                }
                buttonState = "Play"
                SongActivity.MusicUtils.updatePlayButton(this, playButton, "Play")
            }

        }

        songTextView.setOnClickListener{
            MusicServerManager.musicServer = musicServer
            val intent = Intent(this,SongActivity::class.java)
            //intent.putExtra(media)
            startActivityForResult(intent, REQUEST_CODE)
        }
        /*playButton.setOnClickListener {
            //play music
            /*Toast.makeText(this, "play button", Toast.LENGTH_SHORT).show()
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start() // Toca a música
                Toast.makeText(this, "Playing music", Toast.LENGTH_SHORT).show()
            }*/
            if (currentMode == ("Master")) {
                val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
                val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
                client.sendCommand("play")
                Toast.makeText(this, "Play command sent to $ipAddress", Toast.LENGTH_SHORT).show()
                //playMusic()
            }
            else{
                musicServer.playMusic()
            }



        }*/

        /*stopButton.setOnClickListener {
            //stop music
            /*Toast.makeText(this, "stop button", Toast.LENGTH_SHORT).show()
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause() // Pausa a música
                Toast.makeText(this, "Music paused", Toast.LENGTH_SHORT).show()
            }*/
            if (currentMode == ("Master")) {
                val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
                val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
                client.sendCommand("stop")
                Toast.makeText(this, "Stop command sent to $ipAddress", Toast.LENGTH_SHORT).show()
                //stopMusic()
            }
            else{
                musicServer.stopMusic()
            }




        }*/



    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_MUSIC_REQUEST)
    }


    private fun checkStoragePermission() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadAudioFiles()
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAudioFiles()
        } else {
            Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAudioFiles() {
        val audioList = mutableListOf<AudioFile>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE)

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

        cursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)

            while (it.moveToNext()) {
                val path = it.getString(dataIndex)
                val title = it.getString(titleIndex)
                val songName = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))

                audioList.add(AudioFile(title, path))
                //Log.d("Main Acti List", "Title: $title, Songname: $songName")
            }
        }

        audioFilesAdapter = AudioFilesAdapter(audioList) { selectedTitle, selectedFile ->
            //Toast.makeText(this, "Selected: $selectedFile", Toast.LENGTH_SHORT).show()
            // Aqui você pode lidar com o arquivo selecionado, por exemplo, iniciar a reprodução
            val fileName = File(selectedFile).name
            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

            if (currentMode == ("Master")) {
                val serverIpAddress = sharedPreferences.getString("connectorIp", "192.168.1.65")
                val client = SocketClient(serverIpAddress, 12345) // Substitua com o IP do servidor
                client.sendCommand("play $fileName")
                Toast.makeText(this, "Play command sent to $serverIpAddress", Toast.LENGTH_SHORT).show()
            }
            else{
                musicServer.playSpecificMusic(fileName)
            }
            //SongActivity.MusicUtils.updateSongInfo(this, songTextView, fileName, selectedTitle)
            val mp3info = SongActivity.MusicUtils.getMP3Metadata(musicServer.getSongPath(), songImageView)
            SongActivity.MusicUtils.updateSongInfo(this, songTextView,musicServer.getSongName(), musicServer.getSongTitle(), mp3info["album"], mp3info["artist"])
            //playButton.setBackgroundResource(R.drawable.button_pause)
            SongActivity.MusicUtils.updatePlayButton(this, playButton, "Pause")
            songImageView.setImageResource(R.drawable.music_master_icon2)




        }
        audioFilesRecyclerView.adapter = audioFilesAdapter
    }


    private fun updateMode() {
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        if (currentMode == "Master") {
            //Toast.makeText(this, "Master mode activated", Toast.LENGTH_SHORT).show()
            val editor = sharedPreferences.edit()
            editor.putString("deviceMode", "Master")
            editor.apply()
        } else {
            //Toast.makeText(this, "Slave mode activated", Toast.LENGTH_SHORT).show()
            val editor = sharedPreferences.edit()
            editor.putString("deviceMode", "Slave")
            editor.apply()
        }
    }

    /*private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_MUSIC_REQUEST)
    }*/

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MUSIC_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val selectedMusicUri = data.data
            if (selectedMusicUri != null) {
                musicServer.updateMusicUri(selectedMusicUri)
                Toast.makeText(this, "Music selected: $selectedMusicUri", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No music selected", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val songTitle = sharedPreferences.getString("lastplayedtitle", "Over the Horizon")
        val playButtonState = sharedPreferences.getString("buttonState", "Play")

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the returned data
            val resultData = data?.getStringExtra("key")
            // Update your activity based on the returned data
            if(playButtonState=="Pause")
                playButton.setBackgroundResource(R.drawable.button_pause)
            else
                playButton.setBackgroundResource(R.drawable.button_play)
            songTextView.text = songTitle
            if(title.length>14)
                songTextView.textSize = 18f
            if(title.length>25)
                songTextView.textSize = 12f
            else if(title.length<=14)
                songTextView.textSize = 30f// For example, update a TextView
        }

        when (requestCode) {
            PICK_MUSIC_REQUEST -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedMusicUri = data.data
                    if (selectedMusicUri != null) {
                        //musicServer.updateMusicUri(selectedMusicUri)
                        Toast.makeText(this, "Music selected: $selectedMusicUri", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "No music selected", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            /*MORE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    val newIpAddress = data?.getStringExtra("NEW_IP_ADDRESS")
                    if (newIpAddress != null) {
                        // Update the server IP address in MainActivity
                        serverIpAddress = newIpAddress
                        Toast.makeText(this, "New IP Address: $serverIpAddress", Toast.LENGTH_SHORT).show()
                    }
                }
            }*/
        }
    }


    /*override fun onDestroy() {
        super.onDestroy()
        musicServer.stopServer()

    }*/

    private fun getIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            ipAddress and 0xFF,
            ipAddress shr 8 and 0xFF,
            ipAddress shr 16 and 0xFF,
            ipAddress shr 24 and 0xFF
        )
    }

    data class AudioFile(val title: String, val path: String)



}
