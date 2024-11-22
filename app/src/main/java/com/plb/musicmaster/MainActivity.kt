package com.plb.musicmaster

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plb.musicmaster.ui.theme.MusicMasterTheme
import android.media.MediaPlayer
import android.widget.EditText
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.browse.MediaBrowser
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.annotation.OptIn
import androidx.appcompat.widget.Toolbar
import androidx.compose.foundation.layout.Column
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.plb.musicmaster.AudioFilesAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.net.Uri

import android.os.IBinder
import androidx.media3.common.MediaItem
import timber.log.Timber
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column





class MainActivity : ComponentActivity(), ServiceConnection {

    //private lateinit var mediaPlayer: MediaPlayer
    private lateinit var moreButton: Button
    private lateinit var settingsButton: Button
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private lateinit var musicServer: MusicServer
    private lateinit var selectMusicTrack: Button
    private lateinit var modeRadioGroup: RadioGroup
    private lateinit var masterModeRadioButton: RadioButton
    private lateinit var slaveModeRadioButton: RadioButton
    private lateinit var mediaControlHelper: MediaControlHelper
    private lateinit var audioFilesRecyclerView: RecyclerView
    private lateinit var audioFilesAdapter: AudioFilesAdapter

    //var serverIpAddress: String = "192.168.1.65" // Valor padrão
    private var currentMode: String = "Master"
    private var mediaPlayer: MediaPlayer? = null

    //private val MORE_ACTIVITY_REQUEST_CODE = 1
    private val REQUEST_CODE_PERMISSION = 100

    companion object {
        private const val PICK_MUSIC_REQUEST = 1
    }

    //Gamado

    private lateinit var exoPlayer: ExoPlayer

    private var songRunnable: Runnable = Runnable {}
    private var songHandler: Handler = Handler(Looper.getMainLooper())

    private var currentMusicState = MusicState() // default MusicState

    private var mediaPlayerService: MediaPlayerService? = null
    private val songController = object : SongController {
        override fun play() {
            exoPlayer.play()
        }

        override fun pause() {
            exoPlayer.pause()
        }

        override fun next() {
            // TODO: Next
        }

        override fun previous() {
            // TODO: Previous
        }

        override fun stop() {
            exoPlayer.stop()
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ExoPlayer
        exoPlayer = ExoPlayer.Builder(this)
            .setLooper(Looper.getMainLooper())
            .build()

        // Copy asset to a temporary file
        val tempFile = File.createTempFile("temp_music", ".mp3", cacheDir).apply {
            deleteOnExit()
            outputStream().use { output ->
                assets.open("SWR_hymne.mp3").use { input ->
                    input.copyTo(output)
                }
            }
        }

        exoPlayer.apply {
            // Load music from assets
            addMediaItem(MediaItem.fromUri(Uri.fromFile(tempFile)))

            // Prepare audio
            prepare()

            // Init state
            currentMusicState = MusicState(
                isPlaying = exoPlayer.isPlaying,
                title = "No Celestial",
                artist = "LE SSERAFIM",
                album = "ANTIFRAGILE",
                albumArt = BitmapFactory.decodeStream(assets.open("eurocat.png")),
                duration = exoPlayer.duration
            )
        }

        songRunnable = Runnable {
            // Update state every 1 seconds
            currentMusicState = currentMusicState.copy(
                isPlaying = exoPlayer.isPlaying,
                currentDuration = exoPlayer.currentPosition,
                duration = exoPlayer.duration
            )

            startOrUpdateService()

            songHandler.postDelayed(songRunnable, 1000)
        }

        songHandler.post(songRunnable)

        // Bind service
        bindService(
            startOrUpdateService(),
            this,
            BIND_AUTO_CREATE
        )

        setContent {
            ForBloggingTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    MainScreen()
                }
            }
        }

        //setContentView(R.layout.activity_main)

        /*moreButton = findViewById(R.id.moreButton)
        settingsButton = findViewById(R.id.settingsButton)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)
        selectMusicTrack = findViewById(R.id.selectSampleButton)
        modeRadioGroup = findViewById(R.id.modeRadioGroup)
        masterModeRadioButton = findViewById(R.id.masterMode)
        slaveModeRadioButton = findViewById(R.id.slaveMode)

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val serverIpAddress = sharedPreferences.getString("connectorIp", "192.168.1.633")

        audioFilesRecyclerView = findViewById(R.id.audioFilesRecyclerView)
        audioFilesRecyclerView.layoutManager = LinearLayoutManager(this)

        checkStoragePermission()  // Solicita permissão e carrega arquivos de áudio





        //mediaPlayer = MediaPlayer.create(this, R.raw.sample)

        musicServer = MusicServer(this, 12345)
        musicServer.start()

        // Set default mode
        masterModeRadioButton.isChecked = true
        currentMode = "Master"

        modeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            currentMode = when (checkedId) {
                R.id.masterMode -> "Master"
                R.id.slaveMode -> "Slave"
                else -> "Master"
            }
            updateMode()
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

        playButton.setOnClickListener {
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



        }

        stopButton.setOnClickListener {
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




        }


        selectMusicTrack.setOnClickListener {
            openFileChooser()
        }*/


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
        val audioList = mutableListOf<String>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)

            while (it.moveToNext()) {
                val path = it.getString(dataIndex)
                val title = it.getString(titleIndex)
                audioList.add("$title\n$path")
            }
        }

        audioFilesAdapter = AudioFilesAdapter(audioList) { selectedFile ->
            Toast.makeText(this, "Selected: $selectedFile", Toast.LENGTH_SHORT).show()
            // Aqui você pode lidar com o arquivo selecionado, por exemplo, iniciar a reprodução
            if (currentMode == ("Master")) {
                val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
                val serverIpAddress = sharedPreferences.getString("connectorIp", "192.168.1.65")
                val client = SocketClient(serverIpAddress, 12345) // Substitua com o IP do servidor
                val fileName = File(selectedFile).name
                client.sendCommand("play $fileName")
                Toast.makeText(this, "Play command sent to $serverIpAddress", Toast.LENGTH_SHORT).show()
            }
        }
        audioFilesRecyclerView.adapter = audioFilesAdapter
    }


    private fun updateMode() {
        if (currentMode == "Master") {
            Toast.makeText(this, "Master mode activated", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Slave mode activated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_MUSIC_REQUEST)
    }

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

        when (requestCode) {
            PICK_MUSIC_REQUEST -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                    val selectedMusicUri = data.data
                    if (selectedMusicUri != null) {
                        musicServer.updateMusicUri(selectedMusicUri)
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

    //mais merda

    private fun startOrUpdateService(): Intent {
        // Start service
        val serviceIntent = Intent(this, MediaPlayerService::class.java).apply {
            putExtra("musicState", currentMusicState)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else startService(serviceIntent)

        return serviceIntent
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            unbindService(this)
        } catch (e: IllegalArgumentException) {
            Timber.e(e, "Service not registered")
        }
    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
        val binder = p1 as MediaPlayerService.MediaPlayerServiceBinder

        mediaPlayerService = binder.getService()
        mediaPlayerService!!.setSongController(songController)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        mediaPlayerService = null
    }

    @Composable
    fun MainScreen() {

        Column {
            Button(
                onClick = {
                    if (exoPlayer.isPlaying) songController.pause()
                    else songController.play()
                }
            ) {
                Text("Play/Pause")
            }
        }

    }


}
