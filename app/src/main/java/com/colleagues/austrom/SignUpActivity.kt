package com.colleagues.austrom

import android.content.Intent
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
import com.google.android.material.textfield.TextInputLayout

class SignUpActivity : AppCompatActivity() {
    private lateinit var loginTextBox: TextInputEditText
    private lateinit var emailTextBox: TextInputEditText
    private lateinit var passwordTextBox: TextInputEditText
    private lateinit var repeatPasswordTextBox: TextInputEditText
    private lateinit var signUpButton: Button
    private lateinit var checkTextFields: MutableMap<String, Boolean>
    private lateinit var loginTextLayout: TextInputLayout
    private lateinit var emailTextLayout: TextInputLayout
    private lateinit var passwordTextLayout: TextInputLayout
    private lateinit var repeatPasswordTextLayout: TextInputLayout

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

        // booleans for all textBoxes checks are passed
        checkTextFields = mutableMapOf("login" to false, "email" to false, "password" to false, "repeatPassword" to false) // 25.09.24 MC
        signUpButton.isEnabled = false

        signUpButton.setOnClickListener{
            val provider : IDatabaseProvider = FirebaseDatabaseProvider(this)
            val existingUser = provider.getUserByUsername(loginTextBox.text.toString().lowercase())
            if (existingUser == null) {
                val newUser = User(
                    null,
                    loginTextBox.text.toString().lowercase(),
                    emailTextBox.text.toString(),
                    passwordTextBox.text.toString())
                provider.createNewUser(newUser)
                Toast.makeText(this, "User successfully added", Toast.LENGTH_LONG).show()
                AustromApplication.appUser = newUser
                startActivity(Intent(this, MainActivity::class.java))
                this.finish()
            } else {
                Toast.makeText(this, "User with provided username already exist in the system", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }

        loginTextBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkLogin()
        }
        loginTextBox.setOnKeyListener { _, keyCode, event ->
            when {
                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyCode == android.view.KeyEvent.KEYCODE_ENTER) && (event.action == android.view.KeyEvent.ACTION_DOWN)) -> {
                    checkLogin()
                    return@setOnKeyListener true
                }
                else -> false
            }
        }

        emailTextBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkEmail()
        }
        emailTextBox.setOnKeyListener { _, keyCode, event ->
            when {
                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyCode == android.view.KeyEvent.KEYCODE_ENTER) && (event.action == android.view.KeyEvent.ACTION_DOWN)) -> {
                    checkEmail()
                    return@setOnKeyListener true
                }
                else -> false
            }
        }

        passwordTextBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkPassword()
        }
        passwordTextBox.setOnKeyListener { _, keyCode, event ->
            when {
                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyCode == android.view.KeyEvent.KEYCODE_ENTER) && (event.action == android.view.KeyEvent.ACTION_DOWN)) -> {
                    checkPassword()
                    return@setOnKeyListener true
                }
                else -> false
            }
        }

        repeatPasswordTextBox.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus)
                checkRepeatPassword()
        }
        repeatPasswordTextBox.setOnKeyListener { _, keyCode, event ->
            when {
                //Check if it is the Enter-Key,      Check if the Enter Key was pressed down
                ((keyCode == android.view.KeyEvent.KEYCODE_ENTER) && (event.action == android.view.KeyEvent.ACTION_DOWN)) -> {
                    checkRepeatPassword()
                    return@setOnKeyListener true
                }
                else -> false
            }
        }
    }

    private fun bindViews() {
        signUpButton = findViewById(R.id.signUp_signUp_btn)
        loginTextBox = findViewById(R.id.signUp_login_txt)
        emailTextBox = findViewById(R.id.signUp_email_txt)
        passwordTextBox = findViewById(R.id.signUp_password_txt)
        repeatPasswordTextBox = findViewById(R.id.signUp_repeatPassword_txt)
        loginTextLayout = findViewById(R.id.signUp_login_til)
        emailTextLayout = findViewById(R.id.signUp_email_til)
        passwordTextLayout = findViewById(R.id.signUp_password_til)
        repeatPasswordTextLayout = findViewById(R.id.signUp_repeatPassword_til)
    }

    private fun checkLogin() {
        if (loginTextBox.text.toString().isEmpty()) {
            loginTextLayout.setError("Login cannot be empty")
            checkTextFields["login"] = false
            return
        }
        if (!loginTextBox.text.toString().matches("^[a-zA-Z0-9_-]{3,15}$".toRegex())) {
            if (!loginTextBox.text.toString().matches("^.{3,15}$".toRegex()))
                loginTextLayout.setError("The length of login must be of 3 to 16 characters")
            else
                loginTextLayout.setError("Login can contain only letters, numbers and symbols _ and -")
            checkTextFields["login"] = false
            return
        }
        acceptField("login")
    }

    private fun checkEmail() {
        if (emailTextBox.text.toString().isEmpty()) {
            emailTextLayout.setError("Email cannot be empty")
            checkTextFields["email"] = false
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTextBox.text.toString()).matches()) {
            emailTextLayout.setError("Email has a wrong format")
            checkTextFields["email"] = false
            return
        }
        acceptField("email")
    }

    private fun checkPassword() {
        if (passwordTextBox.text.toString().isEmpty()) {
            passwordTextLayout.setError("Password cannot be empty")
            checkTextFields["password"] = false
            return
        }
        if (!passwordTextBox.text.toString().matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$ %^&*-]).{8,}$".toRegex())) {
            if (!passwordTextBox.text.toString().matches("^.{8,}$".toRegex()))
                passwordTextLayout.setError("Password must be at least 8 characters long")
            else
                passwordTextLayout.setError("Password must contain at least one upper case, one lower case latin letter, one number and one of the following character: #?!@$%^&*-")
            checkTextFields["password"] = false
            return
        }
        acceptField("password")
    }

    private fun checkRepeatPassword() {
        if (repeatPasswordTextBox.text.toString().isEmpty()) {
            repeatPasswordTextLayout.setError("Repeat the password, please")
            checkTextFields["repeatPassword"] = false
            return
        }
        if (passwordTextBox.text.toString() != repeatPasswordTextBox.text.toString()) {
            repeatPasswordTextLayout.setError("Passwords do not match")
            checkTextFields["repeatPassword"] = false
            return
        }
        acceptField("repeatPassword")
    }

    private fun acceptField(fieldName: String) {
        checkTextFields[fieldName] = true
        repeatPasswordTextLayout.isErrorEnabled = false
        signUpButton.isEnabled = checkTextFields.values.all { it }
    }
}