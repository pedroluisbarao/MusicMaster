package com.plb.musicmaster

import android.os.Bundle

interface MusicServerListener {
    fun onMusicStopped(songName: String)
    fun onNewMusicFromMaster(songName: String)

}