package com.colleagues.austrom

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        logInButton.setOnClickListener{
            val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider()
            val existingUser = dbProvider.getUserByUsername(loginTextBox.text.toString(), this)
            if (existingUser == null || existingUser.password!=passwordTextBox.text.toString()) {
                Toast.makeText(this, "Username or password is incorrect", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Welcome, ${existingUser.username}", Toast.LENGTH_LONG).show()
                (this.application as AustromApplication).appUser = existingUser
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

    private fun bindViews() {
        logInButton = findViewById(R.id.auth_login_btn)
        signUpButton = findViewById(R.id.auth_signUp_btn)
        forgotPasswordButton = findViewById(R.id.auth_forgotPassword_btn)
        loginTextBox = findViewById(R.id.auth_login_txt)
        passwordTextBox = findViewById(R.id.auth_password_txt)
    }
}