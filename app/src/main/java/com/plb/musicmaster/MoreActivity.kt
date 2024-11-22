package com.plb.musicmaster

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MoreActivity : AppCompatActivity() {

    private lateinit var showIpButton: Button
    private lateinit var saveIpButton: Button
    private lateinit var ipInput: EditText
    //var newIpAddress: String = "192.168.1.65" // Valor padrão

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)

        showIpButton = findViewById(R.id.showIpButton)
        saveIpButton = findViewById(R.id.saveIpButton)
        ipInput = findViewById(R.id.ipInput) // Initialize ipInput here

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true) // Show the back button

        // Handle the click action for the back button
        toolbar.setNavigationOnClickListener {
            finish() // Finish the current activity and go back to the previous activity (Home screen)
        }

        showIpButton.setOnClickListener {
            val ipAddress = getIpAddress()
            Toast.makeText(this, "IP Address: $ipAddress", Toast.LENGTH_LONG).show()
        }

        saveIpButton.setOnClickListener {
            val newIpAddress = ipInput.text.toString()
            if (newIpAddress.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("NEW_IP_ADDRESS", "192.168.1." + newIpAddress)
                setResult(Activity.RESULT_OK, resultIntent)
                Toast.makeText(this, "IP Address saved: 192.168.1.$newIpAddress", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a valid IP Address", Toast.LENGTH_SHORT).show()
            }
            val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("connectorIp", "192.168.1." + newIpAddress)
            editor.apply()
        }
    }

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
}
