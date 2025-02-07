package com.colleagues.austrom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.CategoryArrayAdapter
import com.colleagues.austrom.adapters.TransactionExtendedRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.parseToLocalDate
import com.colleagues.austrom.fragments.BalanceFragment
import com.colleagues.austrom.fragments.ImportMappingFragment
import com.colleagues.austrom.fragments.OpsFragment
import com.colleagues.austrom.fragments.TransactionApprovementFragment
import com.colleagues.austrom.fragments.TransactionEditFragment
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.switchmaterial.SwitchMaterial
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDate

class ImportParametersActivity : AppCompatActivity() {
    private lateinit var fragmentHolder: FragmentContainerView
    private lateinit var backButton: ImageButton

    private fun bindViews() {
        fragmentHolder = findViewById(R.id.impact_fragmentHolder_fcv)
        backButton = findViewById(R.id.impact_back_btn)
    }

    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }

    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_import_parameters)
        adjustInsets()
        bindViews()

        filePickerLauncher = initializeActivityForResult()
        pickCsvFile()

        onBackPressedDispatcher.addCallback(this) { handleReturn() }
        backButton.setOnClickListener { handleReturn() }
    }

    private fun handleReturn() {
        this.finish()
    }

    private fun switchFragment(fragment: Fragment) {
        val transition: FragmentTransaction = supportFragmentManager.beginTransaction()
        transition.replace(fragmentHolder.id, fragment)
        when(fragment) {
            is ImportMappingFragment -> fragment.setOnFragmentChangeRequestedListener{newFragment -> if (newFragment!=null) switchFragment(newFragment) else this.finish() }
            is TransactionApprovementFragment -> {
                fragment.setOnFragmentChangeRequestedListener { newFragment -> if (newFragment!=null) switchFragment(newFragment) else this.finish() }
                fragment.setOnImportCompletedListener { this.finish() }
            }
            is TransactionEditFragment -> fragment.setOnDialogResultListener{transaction, transactionList -> switchFragment(TransactionApprovementFragment(transactionList.toMutableList())) }
        }
        transition.commit()
    }

    private fun initializeActivityForResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    switchFragment(ImportMappingFragment(uri))
                }
            } else {
                this.finish()
            }
        }
    }

    private fun pickCsvFile() {
        filePickerLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        })
    }
}