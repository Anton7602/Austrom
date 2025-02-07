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
    //region Binding
    private lateinit var logInButton: Button
    private lateinit var signUpButton: TextView
    private lateinit var forgotPasswordButton: TextView
    private lateinit var loginTextBox : TextInputEditText
    private lateinit var passwordTextBox : TextInputEditText
    private fun bindViews() {
        logInButton = findViewById(R.id.auth_login_btn)
        signUpButton = findViewById(R.id.auth_signUp_btn)
        forgotPasswordButton = findViewById(R.id.auth_forgotPassword_btn)
        loginTextBox = findViewById(R.id.auth_login_txt)
        passwordTextBox = findViewById(R.id.auth_password_txt)
    }
    //endregion
    //region Localization
    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.attachBaseContext(newBase)
        } else  {
            super.attachBaseContext(AustromApplication.updateBaseContextLocale(newBase))
        }
    }
    // endregion
    //region Styling
    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val softKeyboardHeightInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, if (softKeyboardHeightInset==0) systemBars.bottom else softKeyboardHeightInset)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }
    // endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_authorization)
        adjustInsets()
        bindViews()
        //this.deleteDatabase("local_database")
        runQuickAuthorization()

        logInButton.setOnClickListener{ logInUser() }
        signUpButton.setOnClickListener{ startActivity(Intent(this, SignUpActivity::class.java)) }
        forgotPasswordButton.setOnClickListener{ startActivity(Intent(this, PasswordRecoveryActivity::class.java)) }
    }

    private fun launchMainActivity(user: User) {
        AustromApplication.appUser = user
        val intent = Intent(this, MainActivity::class.java)
        val rememberedUser = (application as AustromApplication).getRememberedUser()
        if (rememberedUser== null || rememberedUser!=user.userId) {
            (application as AustromApplication).setRememberedUser(user.userId)
            intent.putExtra("newUser", true)
        }
        startActivity(intent)
    }

    private fun logInUser() {
        val dbProvider: IRemoteDatabaseProvider = FirebaseDatabaseProvider(this)
        val encryptionManager = EncryptionManager()
        val existingUser = dbProvider.getUserByEmail(loginTextBox.text.toString().lowercase())
        if (existingUser== null  || !encryptionManager.isPasswordFitsHash(passwordTextBox.text.toString(),existingUser.password)) {
            Toast.makeText(this, "Email or password is incorrect", Toast.LENGTH_LONG).show()
        } else {
            existingUser.password = passwordTextBox.text.toString()
            if (existingUser.tokenId!=null) {
                existingUser.tokenId = encryptionManager.decrypt(existingUser.tokenId!!, encryptionManager.generateEncryptionKey(existingUser.password, existingUser.userId.toByteArray()))
            }
            val localProvider = LocalDatabaseProvider(this)
            if (localProvider.getUserByUserId(existingUser.userId)==null) {
                localProvider.writeNewUser(existingUser)
            }
            launchMainActivity(existingUser)
        }
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
}