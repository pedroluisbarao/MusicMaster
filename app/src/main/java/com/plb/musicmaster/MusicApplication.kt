package com.plb.musicmaster

import android.app.Application
import android.os.Build

class MusicApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtil.createChannel(this)
        }
    }
}