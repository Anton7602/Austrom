package com.colleagues.austrom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.managers.BiometricPromptManager
import com.colleagues.austrom.models.User
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AuthorizationActivity : AppCompatActivity() {
    private lateinit var logInButton: Button
    private lateinit var signUpButton: TextView
    private lateinit var forgotPasswordButton: TextView
    private lateinit var loginTextBox : TextInputEditText
    private lateinit var passwordTextBox : TextInputEditText
    private lateinit var sharedPreferences : SharedPreferences
    private var promptResults: BiometricPromptManager.BiometricResult? = null
    private val promptManager by lazy{
        BiometricPromptManager(this)
    }

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
        tryAuthorizeExistingUserOnDevice()

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

    private fun tryAuthorizeExistingUserOnDevice() {
        val storedUserID = sharedPreferences.getString("appUserId", "")
        if (!storedUserID.isNullOrEmpty()) {
            val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
            val existingUser = dbProvider.getUserByUserId(storedUserID)
            if (existingUser!= null) {
                initializeBiometricAuthentication(existingUser)
            }
        }
    }

    private fun initializeBiometricAuthentication(user: User) {
        lifecycleScope.launch {
            promptManager.promptResults.collect { results ->
                promptResults = results
                when(promptResults) {
                    BiometricPromptManager.BiometricResult.AuthenticationError -> Toast.makeText(applicationContext, "Authentication Error", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.AuthenticationFailed -> Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.AuthenticationNotSet -> Toast.makeText(applicationContext, "Authentication Not Set", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                        AustromApplication.appUser = user
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    }
                    BiometricPromptManager.BiometricResult.FeatureUnavailable -> Toast.makeText(applicationContext, "Authentication Feature Unavailable", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.HardwareUnavailable -> Toast.makeText(applicationContext, "Authentication Hardware Unavailable", Toast.LENGTH_LONG).show()
                    else ->  Toast.makeText(applicationContext,  "??", Toast.LENGTH_LONG).show()
                    }
                if (promptResults is BiometricPromptManager.BiometricResult.AuthenticationNotSet && Build.VERSION.SDK_INT>=30) {
                    val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
                        if (result.resultCode == RESULT_OK) {
                            Toast.makeText(applicationContext,  result.data.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                    }
                    activityLauncher.launch(enrollIntent)
                }
            }
        }
        promptManager.showBiometricPrompt(this.getString(R.string.biometric_title), this.getString(R.string.biometric_prompt))
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