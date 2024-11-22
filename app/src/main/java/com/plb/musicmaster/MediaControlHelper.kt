package com.plb.musicmaster

import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast

class MediaControlHelper(private val context: Context) {

    private var mediaController: MediaController? = null
    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    fun setupMediaSession() {
        // Pegue a sessão de mídia ativa (Spotify, por exemplo)
        val activeSessions = mediaSessionManager.getActiveSessions(null)
        if (activeSessions.isNotEmpty()) {
            // Pegue o primeiro controle ativo de mídia
            mediaController = activeSessions[0]
        }
    }

    fun playMedia() {
        mediaController?.let { controller ->
            controller.transportControls.play()
        }
    }

    fun pauseMedia() {
        mediaController?.let { controller ->
            controller.transportControls.pause()
        }
    }

    fun stopMedia() {
        mediaController?.let { controller ->
            controller.transportControls.stop()
        }
    }
}
