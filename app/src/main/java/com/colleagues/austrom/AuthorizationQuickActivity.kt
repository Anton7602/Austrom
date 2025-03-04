package com.colleagues.austrom

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.managers.BiometricPromptManager
import com.colleagues.austrom.models.User
import kotlinx.coroutines.launch
import java.time.LocalTime

class AuthorizationQuickActivity : AppCompatActivity() {
    //region Binding
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var button6: Button
    private lateinit var button7: Button
    private lateinit var button8: Button
    private lateinit var button9: Button
    private lateinit var button0: Button
    private lateinit var buttonRemove: ImageButton
    private lateinit var buttonBio: ImageButton
    private lateinit var pinDot1: ImageView
    private lateinit var pinDot2: ImageView
    private lateinit var pinDot3: ImageView
    private lateinit var pinDot4: ImageView
    private lateinit var username: TextView
    private lateinit var timeOfDay: TextView
    private lateinit var callToAction: TextView
    private fun bindViews() {
        button1 = findViewById(R.id.qauth_keyboard_1_btn)
        button2 = findViewById(R.id.qauth_keyboard_2_btn)
        button3 = findViewById(R.id.qauth_keyboard_3_btn)
        button4 = findViewById(R.id.qauth_keyboard_4_btn)
        button5 = findViewById(R.id.qauth_keyboard_5_btn)
        button6 = findViewById(R.id.qauth_keyboard_6_btn)
        button7 = findViewById(R.id.qauth_keyboard_7_btn)
        button8 = findViewById(R.id.qauth_keyboard_8_btn)
        button9 = findViewById(R.id.qauth_keyboard_9_btn)
        button0 = findViewById(R.id.qauth_keyboard_0_btn)
        buttonBio = findViewById(R.id.qauth_keyboard_bio_btn)
        buttonRemove = findViewById(R.id.qauth_keyboard_remove_btn)
        pinDot1 = findViewById(R.id.qauth_symbolFirst_img)
        pinDot2 = findViewById(R.id.qauth_symbolSecond_img)
        pinDot3 = findViewById(R.id.qauth_symbolThird_img)
        pinDot4 = findViewById(R.id.qauth_symbolFourth_img)
        username = findViewById(R.id.qauth_username_txt)
        timeOfDay = findViewById(R.id.qauth_timeOfDay_txt)
        callToAction = findViewById(R.id.qauth_callToAction_txt)
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setUpOrientationLimitations() { setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }
    // endregion
    private var promptResults: BiometricPromptManager.BiometricResult? = null
    private val promptManager by lazy{ BiometricPromptManager(this) }
    private lateinit var authorizingUser: User
    private lateinit var pin: String
    private var input = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setUpOrientationLimitations()
        setContentView(R.layout.activity_authorization_quick)
        adjustInsets()
        bindViews()
        initializeAuthorizingUser()
        initializeBiometricAuthentication()
        username.text = authorizingUser.username.startWithUppercase()
        timeOfDay.text = generateGreetings()

        val keyboardButtonClickListener = OnClickListener {buttonPressed ->  handleNewInput(buttonPressed) }
        button1.setOnClickListener(keyboardButtonClickListener)
        button2.setOnClickListener(keyboardButtonClickListener)
        button3.setOnClickListener(keyboardButtonClickListener)
        button4.setOnClickListener(keyboardButtonClickListener)
        button5.setOnClickListener(keyboardButtonClickListener)
        button6.setOnClickListener(keyboardButtonClickListener)
        button7.setOnClickListener(keyboardButtonClickListener)
        button8.setOnClickListener(keyboardButtonClickListener)
        button9.setOnClickListener(keyboardButtonClickListener)
        button0.setOnClickListener(keyboardButtonClickListener)
        buttonRemove.setOnClickListener(keyboardButtonClickListener)
        buttonBio.setOnClickListener(keyboardButtonClickListener)
    }

    private fun launchMainActivity() {
        AustromApplication.appUser = authorizingUser
        startActivity(Intent(applicationContext, MainActivity::class.java))
    }

    private fun handleNewInput(buttonPressed: View) {
        if (input.length==4) {
            input = ""
        }
        if (input.length<4) {
            input += when (buttonPressed.id) {
                R.id.qauth_keyboard_1_btn -> "1"
                R.id.qauth_keyboard_2_btn -> "2"
                R.id.qauth_keyboard_3_btn -> "3"
                R.id.qauth_keyboard_4_btn -> "4"
                R.id.qauth_keyboard_5_btn -> "5"
                R.id.qauth_keyboard_6_btn -> "6"
                R.id.qauth_keyboard_7_btn -> "7"
                R.id.qauth_keyboard_8_btn -> "8"
                R.id.qauth_keyboard_9_btn -> "9"
                R.id.qauth_keyboard_0_btn -> "0"
                else -> ""
            }
        }
        if (buttonPressed.id == R.id.qauth_keyboard_remove_btn && input.isNotEmpty()) { input = input.substring(0, input.length-1) }
        if (buttonPressed.id == R.id.qauth_keyboard_bio_btn) { initializeBiometricAuthentication()  }
        if (input.length==4) {
            if (input == pin) {
                pinDot1.setColorFilter(ContextCompat.getColor(this, R.color.incomeGreen))
                pinDot2.setColorFilter(ContextCompat.getColor(this, R.color.incomeGreen))
                pinDot3.setColorFilter(ContextCompat.getColor(this, R.color.incomeGreen))
                pinDot4.setColorFilter(ContextCompat.getColor(this, R.color.incomeGreen))
                launchMainActivity()
                this.finish()
            } else {
                pinDot1.setColorFilter(ContextCompat.getColor(this, R.color.expenseRed))
                pinDot2.setColorFilter(ContextCompat.getColor(this, R.color.expenseRed))
                pinDot3.setColorFilter(ContextCompat.getColor(this, R.color.expenseRed))
                pinDot4.setColorFilter(ContextCompat.getColor(this, R.color.expenseRed))
            }
        } else {
            pinDot1.setColorFilter(ContextCompat.getColor(this, if (input.isNotEmpty()) {R.color.blue} else {R.color.dark_grey}))
            pinDot2.setColorFilter(ContextCompat.getColor(this, if (input.length>1) {R.color.blue} else {R.color.dark_grey}))
            pinDot3.setColorFilter(ContextCompat.getColor(this, if (input.length>2) {R.color.blue} else {R.color.dark_grey}))
            pinDot4.setColorFilter(ContextCompat.getColor(this, if (input.length>3) {R.color.blue} else {R.color.dark_grey}))
        }
    }

    private fun generateGreetings(): String {
        val time = LocalTime.now()
        return if (time.hour>20) {
            getString(R.string.good_evening)
        } else if (time.hour>12) {
            getString(R.string.good_afternoon)
        } else if (time.hour>5) {
            getString(R.string.good_morning)
        } else {
            getString(R.string.good_night)
        }
    }

    private fun initializeAuthorizingUser() {
        val storedUserID = (application as AustromApplication).getRememberedUser()
        if (!storedUserID.isNullOrEmpty()) {
            val dbProvider = LocalDatabaseProvider(this)
            var existingUser = dbProvider.getUserByUserId(storedUserID)
            if (existingUser!= null) {
                authorizingUser = existingUser
            } else {
                val remoteDbProvider = FirebaseDatabaseProvider(this)
                existingUser = remoteDbProvider.getUserByUserId(storedUserID)
                if (existingUser!= null) {
                    authorizingUser = existingUser
                } else {
                    this.finish()
                }
            }
        }
        val storedPin = (application as AustromApplication).getRememberedPin()
        if (!storedPin.isNullOrEmpty()) {
            pin = storedPin
        } else {
            this.finish()
        }
    }

    private fun initializeBiometricAuthentication() {
        lifecycleScope.launch {
            promptManager.promptResults.collect { results ->
                promptResults = results
                when(promptResults) {
                    BiometricPromptManager.BiometricResult.AuthenticationError -> Toast.makeText(applicationContext, "Authentication Error", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.AuthenticationFailed -> Toast.makeText(applicationContext, "Authentication Failed", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.AuthenticationNotSet -> Toast.makeText(applicationContext, "Authentication Not Set", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                        launchMainActivity()
                        finish()
                    }
                    BiometricPromptManager.BiometricResult.FeatureUnavailable -> Toast.makeText(applicationContext, "Authentication Feature Unavailable", Toast.LENGTH_LONG).show()
                    BiometricPromptManager.BiometricResult.HardwareUnavailable -> Toast.makeText(applicationContext, "Authentication Hardware Unavailable", Toast.LENGTH_LONG).show()
                    else ->  Toast.makeText(applicationContext,  "??", Toast.LENGTH_LONG).show()
                }
                if (promptResults is BiometricPromptManager.BiometricResult.AuthenticationNotSet && Build.VERSION.SDK_INT>=30) {
                    val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
}