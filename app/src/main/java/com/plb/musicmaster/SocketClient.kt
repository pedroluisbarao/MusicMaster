package com.plb.musicmaster

import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val serverIp: String?, private val port: Int) {

    fun sendCommand(command: String) {
        Thread {
            try {
                val socket = Socket(serverIp, port)
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(command)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
