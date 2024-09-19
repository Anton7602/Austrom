package com.colleagues.austrom

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.models.User
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {
    private lateinit var loginTextBox: TextInputEditText
    private lateinit var emailTextBox: TextInputEditText
    private lateinit var passwordTextBox: TextInputEditText
    private lateinit var repeatPasswordTextBox: TextInputEditText
    private lateinit var signUpButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()

        signUpButton.setOnClickListener{
            if (passwordTextBox.text.toString().isEmpty() || loginTextBox.text.toString().isEmpty()) {
                Toast.makeText(this, "Login and Password cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (passwordTextBox.text.toString() != repeatPasswordTextBox.text.toString()) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val provider : IDatabaseProvider = FirebaseDatabaseProvider(this)
            val existingUser = provider.getUserByUsername(loginTextBox.text.toString().lowercase())
            if (existingUser == null) {
                provider.createNewUser(
                    User(
                        null,
                        loginTextBox.text.toString().lowercase(),
                        emailTextBox.text.toString(),
                        passwordTextBox.text.toString()))
                Toast.makeText(this, "User successfully added", Toast.LENGTH_LONG).show()
                this.finish()
            } else {
                Toast.makeText(this, "User with provided username already exist in the system", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }
    }

    private fun bindViews() {
        signUpButton = findViewById(R.id.signUp_signUp_btn)
        loginTextBox = findViewById(R.id.signUp_login_txt)
        emailTextBox = findViewById(R.id.signUp_email_txt)
        passwordTextBox = findViewById(R.id.signUp_password_txt)
        repeatPasswordTextBox = findViewById(R.id.signUp_repeatPassword_txt)
    }
}