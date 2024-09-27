package com.colleagues.austrom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.google.android.material.textfield.TextInputEditText

class AuthorizationActivity : AppCompatActivity() {
    private lateinit var logInButton: Button
    private lateinit var signUpButton: TextView
    private lateinit var forgotPasswordButton: TextView
    private lateinit var loginTextBox : TextInputEditText
    private lateinit var passwordTextBox : TextInputEditText
    private lateinit var sharedPreferences : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authorization)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()

        runQuickAuthorization()

        logInButton.setOnClickListener{
            val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
            var existingUser = dbProvider.getUserByUsername(loginTextBox.text.toString().lowercase())
            if (existingUser == null || existingUser.password!=passwordTextBox.text.toString()) {
                existingUser = dbProvider.getUserByEmail(loginTextBox.text.toString().lowercase())
                if (existingUser== null || existingUser.password!=passwordTextBox.text.toString()) {
                    Toast.makeText(this, "Username/Email or password is incorrect", Toast.LENGTH_LONG).show()
                } else {
                    AustromApplication.appUser = existingUser
                    val editor = sharedPreferences.edit()
                    editor.putString("appUserId", existingUser.userId)
                    editor.apply()
                    startActivity(Intent(this, MainActivity::class.java))
                }
            } else {
                AustromApplication.appUser = existingUser
                val editor = sharedPreferences.edit()
                editor.putString("appUserId", existingUser.userId)
                editor.apply()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        signUpButton.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))

        }

        forgotPasswordButton.setOnClickListener{
            startActivity(Intent(this, PasswordRecoveryActivity::class.java))
        }
    }

    private fun runQuickAuthorization() {
        val storedUserID = sharedPreferences.getString("appUserId", "")
        val storedPin = sharedPreferences.getString("appQuickPin", "")
        if (!storedUserID.isNullOrEmpty() && !storedPin.isNullOrEmpty()) {
            val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
            val existingUser = dbProvider.getUserByUserId(storedUserID)
            if (existingUser!= null) {
                startActivity(Intent(applicationContext, AuthorizationQuickActivity::class.java))
            }
        }
    }

    private fun bindViews() {
        logInButton = findViewById(R.id.auth_login_btn)
        signUpButton = findViewById(R.id.auth_signUp_btn)
        forgotPasswordButton = findViewById(R.id.auth_forgotPassword_btn)
        loginTextBox = findViewById(R.id.auth_login_txt)
        passwordTextBox = findViewById(R.id.auth_password_txt)
        sharedPreferences =  getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }
}