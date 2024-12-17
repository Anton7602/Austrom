package com.colleagues.austrom

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IRemoteDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.managers.EncryptionManager
import com.colleagues.austrom.models.User
import com.google.android.material.textfield.TextInputEditText

class AuthorizationActivity : AppCompatActivity() {
    private lateinit var logInButton: Button
    private lateinit var signUpButton: TextView
    private lateinit var forgotPasswordButton: TextView
    private lateinit var loginTextBox : TextInputEditText
    private lateinit var passwordTextBox : TextInputEditText

    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.attachBaseContext(newBase)
        } else  {
            super.attachBaseContext(AustromApplication.updateBaseContextLocale(newBase))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authorization)
        adjustInsets()
        bindViews()
        //this.deleteDatabase("local_database")
        runQuickAuthorization()

        logInButton.setOnClickListener{
            val dbProvider: IRemoteDatabaseProvider = FirebaseDatabaseProvider(this)
            val encryptionManager = EncryptionManager()
            val existingUser = dbProvider.getUserByEmail(loginTextBox.text.toString().lowercase())
            if (existingUser== null  || !encryptionManager.isPasswordFitsHash(passwordTextBox.text.toString(),existingUser.password.toString())) {
                Toast.makeText(this, "Email or password is incorrect", Toast.LENGTH_LONG).show()
            } else {
                existingUser.password = passwordTextBox.text.toString()
                val localProvider = LocalDatabaseProvider(this)
                localProvider.writeNewUser(existingUser)
                launchMainActivity(existingUser)
            }
        }

        signUpButton.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        forgotPasswordButton.setOnClickListener{
            startActivity(Intent(this, PasswordRecoveryActivity::class.java))
        }
    }

    private fun launchMainActivity(user: User) {
        AustromApplication.appUser = user
        val intent = Intent(this, MainActivity::class.java)
        val rememberedUser = (application as AustromApplication).getRememberedUser()
        if (rememberedUser== null || rememberedUser!=user.userId) {
            (application as AustromApplication).setRememberedUser(user.userId!!)
            intent.putExtra("newUser", true)
        }
        startActivity(intent)
    }

    private fun runQuickAuthorization() {
        val storedUserID = (application as AustromApplication).getRememberedUser()
        val storedPin = (application as AustromApplication).getRememberedPin()
        if (!storedUserID.isNullOrEmpty() && !storedPin.isNullOrEmpty()) {
            val dbProvider = LocalDatabaseProvider(this)
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
    }

    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }
}