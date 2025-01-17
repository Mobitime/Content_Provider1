package com.example.content_provider

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SendMessageActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_SEND_SMS = 100
    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)


        phoneNumber = intent.getStringExtra("CONTACT_PHONE") ?: run {
            Toast.makeText(this, "Номер телефона не указан", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Отправка сообщения"
            subtitle = phoneNumber
        }


        val tvPhoneNumber: TextView = findViewById(R.id.tvPhoneNumber)
        tvPhoneNumber.text = "Кому: $phoneNumber"


        val etMessage: EditText = findViewById(R.id.etMessage)


        val btnSendMessage: Button = findViewById(R.id.btnSendMessage)
        btnSendMessage.setOnClickListener {
            val message = etMessage.text.toString().trim()


            if (message.isEmpty()) {
                etMessage.error = "Введите сообщение"
                return@setOnClickListener
            }


            if (checkSmsPermission()) {
                sendSms(phoneNumber, message)
            } else {
                requestSmsPermission()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.send_message_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_exit -> {

                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS),
            PERMISSIONS_REQUEST_SEND_SMS
        )
    }


    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= 23) {
                this.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }

            smsManager.sendTextMessage(phoneNumber, null, message, null, null)

            Toast.makeText(applicationContext, "Сообщение отправлено", Toast.LENGTH_LONG).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext, 
                "Ошибка отправки: ${e.message}", 
                Toast.LENGTH_LONG
            ).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на отправку SMS получено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Отправка SMS запрещена", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
