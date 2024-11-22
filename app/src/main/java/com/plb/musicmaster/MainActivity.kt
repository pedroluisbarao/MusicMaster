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
import android.view.View
import android.widget.ImageView
import android.widget.TextView


class MainActivity : AppCompatActivity(), MusicServerListener {

    //private lateinit var mediaPlayer: MediaPlayer
    private lateinit var moreButton: Button
    private lateinit var settingsButton: Button
    private lateinit var playButton: Button
    private lateinit var songImageView: ImageView
    private lateinit var songTextView: TextView
    private lateinit var allSongsButton: Button
    private lateinit var playlistsButton: Button
    private lateinit var artistsButton: Button
    //private lateinit var stopButton: Button
    //private lateinit var selectMusicTrack: Button
    private lateinit var musicServer: MusicServer
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var masterModeRadioButton: RadioButton
    private lateinit var slaveModeRadioButton: RadioButton
    //private lateinit var mediaControlHelper: MediaControlHelper
    private lateinit var audioFilesRecyclerView: RecyclerView
    private lateinit var audioFilesAdapter: AudioFilesAdapter
    private lateinit var foldersRecyclerView: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var songsFolderRecyclerView: RecyclerView
    private lateinit var songsFolderAdapter: SongsFolderAdapter
    private lateinit var artistAdapter: ArtistAdapter
    private lateinit var artistsRecyclerView: RecyclerView
    private  var song: String? = null
    private var defaultSong: String = "Last Stop"
    private var buttonState: String = "Play"

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
        allSongsButton = findViewById(R.id.allSongsButton)
        playlistsButton = findViewById(R.id.playlistsButton)
        artistsButton = findViewById(R.id.artistsButton)


        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val serverIpAddress = sharedPreferences.getString("connectorIp", "192.168.1.633")
        song = sharedPreferences.getString("lastplayedsong", defaultSong)
        val songTitle = sharedPreferences.getString("lastplayedtitle", defaultSong)
        Log.d("lastplayedsong", "Selected: $song")
        val editor = sharedPreferences.edit()
        editor.putString("buttonState", "Play")
        editor.apply()

        buttonState = "Play"
        var counter = 0
        //Toast.makeText(this, "Counter: $counter", Toast.LENGTH_SHORT).show()

        val deviceMode = sharedPreferences.getString("currentMode", "Slave")
        currentMode = deviceMode.toString()

        audioFilesRecyclerView = findViewById(R.id.audioFilesRecyclerView)
        audioFilesRecyclerView.layoutManager = LinearLayoutManager(this)
        foldersRecyclerView = findViewById(R.id.FolderRecyclerView)
        foldersRecyclerView.layoutManager = LinearLayoutManager(this)
        songsFolderRecyclerView =  findViewById(R.id.SongsFolderRecyclerView)
        songsFolderRecyclerView.layoutManager = LinearLayoutManager(this)
        artistsRecyclerView = findViewById(R.id.ArtistRecyclerView)
        artistsRecyclerView.layoutManager = LinearLayoutManager(this)

        checkStoragePermission()  // Solicita permissão e carrega arquivos de áudio


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
            //SongActivity.MusicUtils.getMP3Metadata(musicServer.getSongPathFromName(song!!), songImageView)
        }

        if (currentMode == ("Master")) {  //to make sure the correct songs plays when playbutton is pressed
            val ipAddress = sharedPreferences.getString("connectorIp", "192.168.1.602")
            val client = SocketClient(ipAddress, 12345) // Substitua com o IP do servidor
            client.sendCommand("play $song")
            client.sendCommand("stop")
        }

        Toast.makeText(this, "Server started", Toast.LENGTH_SHORT).show()

        moreButton.setOnClickListener() {
            //
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        allSongsButton.setOnClickListener {
            loadAudioFiles() // This method will already fetch all available audio files
            //Toast.makeText(this, "Displaying all songs", Toast.LENGTH_SHORT).show()
        }

        playlistsButton.setOnClickListener {
            showFolders()
        }

        artistsButton.setOnClickListener {
            showArtists()
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
                            musicServer.playSpecificMusic(song!!)
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

        songTextView.setOnClickListener {
            MusicServerManager.musicServer = musicServer
            val intent = Intent(this, SongActivity::class.java)
            //intent.putExtra(media)
            startActivityForResult(intent, REQUEST_CODE)
        }

        songImageView.setOnClickListener{
            MusicServerManager.musicServer = musicServer
            val intent = Intent(this, SongActivity::class.java)
            //intent.putExtra(media)
            startActivityForResult(intent, REQUEST_CODE)
        }



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
            val mp3info = SongActivity.MusicUtils.getMP3Metadata(musicServer.getSongPathFromName(fileName), songImageView)
            SongActivity.MusicUtils.updateSongInfo(this, songTextView,fileName, musicServer.getSongTitleFromName(fileName), mp3info["album"], mp3info["artist"])
            //playButton.setBackgroundResource(R.drawable.button_pause)
            buttonState = "Pause"
            SongActivity.MusicUtils.updatePlayButton(this, playButton, "Pause")
            setSongTextSize()
            //songImageView.setImageResource(R.drawable.music_master_icon2)




        }
        audioFilesRecyclerView.visibility = View.VISIBLE
        audioFilesRecyclerView.adapter = audioFilesAdapter
        songsFolderRecyclerView.visibility = View.GONE
        foldersRecyclerView.visibility = View.GONE
        artistsRecyclerView.visibility = View.GONE
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val songTitle = sharedPreferences.getString("lastplayedtitle", defaultSong)
        val songName = sharedPreferences.getString("lastplayedsong", defaultSong)
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
            setSongTextSize()
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

    private fun showFolders() {
        val folders = musicServer.getFolders() // Chama o método para obter as pastas
        // Crie um adapter para mostrar as pastas no RecyclerView
        val folderAdapter = FolderAdapter(folders) { folder ->
            showSongsInFolder(folder) // Chama a função quando uma pasta é selecionada
            //Toast.makeText(this, "Selected: $folder", Toast.LENGTH_SHORT).show()
        }
        foldersRecyclerView.adapter = folderAdapter // Defina o adapter no RecyclerView
        foldersRecyclerView.visibility = View.VISIBLE // Mostre o RecyclerView de pastas
        audioFilesRecyclerView.visibility = View.GONE // Oculte o RecyclerView de músicas
        songsFolderRecyclerView.visibility = View.GONE
        artistsRecyclerView.visibility = View.GONE
    }

    private fun showSongsInFolder(folderPath: String) {

        val audioList = mutableListOf<AudioFile>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE)

        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("$folderPath/%")

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

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

        val songAdapter = SongsFolderAdapter(audioList) { selectedTitle, selectedFile ->
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
            val mp3info = SongActivity.MusicUtils.getMP3Metadata(musicServer.getSongPathFromName(fileName), songImageView)
            SongActivity.MusicUtils.updateSongInfo(this, songTextView,fileName, musicServer.getSongTitleFromName(fileName), mp3info["album"], mp3info["artist"])
            //playButton.setBackgroundResource(R.drawable.button_pause)
            buttonState = "Pause"
            SongActivity.MusicUtils.updatePlayButton(this, playButton, "Pause")
            setSongTextSize()
            //songImageView.setImageResource(R.drawable.music_master_icon2)
        }
        songsFolderRecyclerView.adapter = songAdapter // Defina o adapter no RecyclerView de músicas
        songsFolderRecyclerView.visibility = View.VISIBLE // Mostre o RecyclerView de músicas
        audioFilesRecyclerView.visibility = View.GONE // Mostre o RecyclerView de músicas
        foldersRecyclerView.visibility = View.GONE // Oculte o RecyclerView de pastas
    }

    fun showArtists(){
        val artists = musicServer.getArtists() // Chama o método para obter as pastas
        // Crie um adapter para mostrar as pastas no RecyclerView
        val artistAdapter = ArtistAdapter(artists) { artist ->
            //showSongsInArtist(folder) // Chama a função quando uma pasta é selecionada
            Toast.makeText(this, "Selected: $artist", Toast.LENGTH_SHORT).show()
            showSongsInArtist(artist)
        }
        artistsRecyclerView.adapter = artistAdapter // Defina o adapter no RecyclerView
        artistsRecyclerView.visibility = View.VISIBLE // Mostre o RecyclerView de pastas
        foldersRecyclerView.visibility = View.GONE // Mostre o RecyclerView de pastas
        audioFilesRecyclerView.visibility = View.GONE // Oculte o RecyclerView de músicas
        songsFolderRecyclerView.visibility = View.GONE
    }

    private fun showSongsInArtist(artistName: String) {
        val audioList = mutableListOf<AudioFile>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST
        )

        // Correct the selection to filter by artist
        val selection = "${MediaStore.Audio.Media.ARTIST} = ?"
        val selectionArgs = arrayOf(artistName) // Set artist name as the selection argument
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

        cursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)

            while (it.moveToNext()) {
                val path = it.getString(dataIndex)
                val title = it.getString(titleIndex)
                val songName = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))

                // Add songs to the audioList
                audioList.add(AudioFile(title, path))
            }
        }

        val songAdapter = SongsFolderAdapter(audioList) { selectedTitle, selectedFile ->
            val fileName = File(selectedFile).name
            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

            if (currentMode == ("Master")) {
                val serverIpAddress = sharedPreferences.getString("connectorIp", "192.168.1.65")
                val client = SocketClient(serverIpAddress, 12345) // Substitua com o IP do servidor
                client.sendCommand("play $fileName")
                Toast.makeText(this, "Play command sent to $serverIpAddress", Toast.LENGTH_SHORT).show()
            } else {
                musicServer.playSpecificMusic(fileName)
            }

            val mp3info = SongActivity.MusicUtils.getMP3Metadata(musicServer.getSongPathFromName(fileName), songImageView)
            SongActivity.MusicUtils.updateSongInfo(this, songTextView,fileName, musicServer.getSongTitleFromName(fileName), mp3info["album"], mp3info["artist"])
            buttonState = "Pause"
            SongActivity.MusicUtils.updatePlayButton(this, playButton, "Pause")
            setSongTextSize()
        }

        // Set the adapter to the RecyclerView
        songsFolderRecyclerView.adapter = songAdapter
        songsFolderRecyclerView.visibility = View.VISIBLE
        audioFilesRecyclerView.visibility = View.GONE
        foldersRecyclerView.visibility = View.GONE
        artistsRecyclerView.visibility = View.GONE
    }


    fun setSongTextSize(){
        val text = songTextView.text.toString()
        songTextView.textSize = 20f
        if(text.length>14)
            songTextView.textSize = 24f
        if(text.length>25)
            songTextView.textSize = 16f
        else if(text.length<=14)
            songTextView.textSize = 24f// For example, update a TextView

    }

    fun playSongFromMaster(songMaster: String){
        musicServer.playSpecificMusic(songMaster)
        buttonState = "Pause"
        SongActivity.MusicUtils.updatePlayButton(this, playButton,"Pause")
        val mp3info = SongActivity.MusicUtils.getMP3Metadata(musicServer.getSongPathFromName(songMaster), songImageView)
        SongActivity.MusicUtils.updateSongInfo(this, songTextView,songMaster, musicServer.getSongTitleFromName(songMaster), mp3info["album"], mp3info["artist"])
        //songAlbumArtist.text = "${mp3info["album"]} - ${mp3info["artist"]}"
    }

    override fun onMusicStopped(songName: String) {
        playSongFromMaster(musicServer.getNextSongName(songName))
    }

    override fun onNewMusicFromMaster(songName: String) {
        playSongFromMaster(songName)
    }


}
