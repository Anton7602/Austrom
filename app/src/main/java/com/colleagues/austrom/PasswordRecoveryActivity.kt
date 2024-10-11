package com.colleagues.austrom

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

class PasswordRecoveryActivity : AppCompatActivity() {
    private lateinit var recoverPasswordButton: TextView
    private lateinit var emailTextBox : TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password_recovery)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()

        recoverPasswordButton.setOnClickListener{
            Toast.makeText(this, getString(R.string.not_yet_implemented), Toast.LENGTH_LONG).show()
        }
    }

    private fun bindViews() {
        recoverPasswordButton = findViewById(R.id.pass_recover_btn)
        emailTextBox = findViewById(R.id.pass_email_txt)
    }
}