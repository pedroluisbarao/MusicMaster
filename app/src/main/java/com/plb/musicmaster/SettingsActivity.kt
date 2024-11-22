package com.plb.musicmaster

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SettingsActivity: AppCompatActivity() {

    private lateinit var networkButton: Button
    //var newIpAddress: String = "192.168.1.65" // Valor padrão

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        networkButton = findViewById(R.id.networkButton)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true) // Show the back button

        // Handle the click action for the back button
        toolbar.setNavigationOnClickListener {
            finish() // Finish the current activity and go back to the previous activity (Home screen)
        }

        networkButton.setOnClickListener {
            val intent = Intent(this, NetworkActivity::class.java)
            startActivity(intent)
        }


    }
}
