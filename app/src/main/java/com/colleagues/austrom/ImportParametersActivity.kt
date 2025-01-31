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
//    private lateinit var sourceDynamicSpinner: Spinner
//    private lateinit var sourceStaticSpinner: Spinner
//    private lateinit var sourceSwitchMaterial: SwitchMaterial
//    private lateinit var targetDynamicSpinner: Spinner
//    private lateinit var targetStaticValue: EditText
//    private lateinit var targetSwitchMaterial: SwitchMaterial
//    private lateinit var amountDynamicSpinner: Spinner
//    private lateinit var amountStaticValue: EditText
//    private lateinit var amountSwitchMaterial: SwitchMaterial
//    private lateinit var dateDynamicSpinner: Spinner
//    private lateinit var dateStaticValue: Spinner
//    private lateinit var dateSwitchMaterial: SwitchMaterial
//    private lateinit var categoryDynamicSpinner: Spinner
//    private lateinit var categoryExpenseStaticSpinner: Spinner
//    private lateinit var categoryExpenseStaticLabel: TextView
//    private lateinit var categoryIncomeStaticSpinner: Spinner
//    private lateinit var categoryIncomeStaticLabel: TextView
//    private lateinit var categorySwitchMaterial: SwitchMaterial
//    private lateinit var commentDynamicSpinner: Spinner
//    private lateinit var commentStaticValue: EditText
//    private lateinit var commentSwitchMaterial: SwitchMaterial
//    private lateinit var importButton: Button
//    private lateinit var exampleTransactionHolder: RecyclerView
//    private lateinit var fieldMappingHolder: ScrollView
//    private lateinit var importControlHolder: CardView
//    private lateinit var topLabel: TextView
//    private lateinit var invalidCounter: TextView
//    private lateinit var invalidRemoveButton: ImageButton
//    private lateinit var suspiciousCounter: TextView
//    private lateinit var suspiciousImportButton: ImageButton
//    private lateinit var validCounter: TextView
//    private lateinit var validImportButton: ImageButton
    private lateinit var fragmentHolder: FragmentContainerView
    private lateinit var backButton: ImageButton
    private var mappingFragment: Fragment? = null
    private var approvementFragment: Fragment? = null

    private fun bindViews() {
//        sourceDynamicSpinner = findViewById(R.id.imppar_source_dynamic_spn)
//        targetDynamicSpinner = findViewById(R.id.imppar_target_dynamic_spn)
//        amountDynamicSpinner = findViewById(R.id.imppar_amount_dynamic_spn)
//        dateDynamicSpinner = findViewById(R.id.imppar_date_dynamic_spn)
//        categoryDynamicSpinner = findViewById(R.id.imppar_category_dynamic_spn)
//        commentDynamicSpinner = findViewById(R.id.imppar_comment_dynamic_spn)
//
//        sourceStaticSpinner = findViewById(R.id.imppar_source_static_spn)
//        targetStaticValue = findViewById(R.id.imppar_target_static_txt)
//        amountStaticValue = findViewById(R.id.imppar_amount_static_txt)
//        dateStaticValue = findViewById(R.id.imppar_date_static_spn)
//        categoryExpenseStaticSpinner = findViewById(R.id.imppar_category_expense_static_spn2)
//        categoryExpenseStaticLabel = findViewById(R.id.imppar_category_expense_label)
//        categoryIncomeStaticSpinner = findViewById(R.id.imppar_category_income_static_spn)
//        categoryIncomeStaticLabel = findViewById(R.id.imppar_category_income_label)
//        commentStaticValue = findViewById(R.id.imppar_comment_static_txt)
//
//        sourceSwitchMaterial = findViewById(R.id.imppar_sourceSwitch_sch)
//        targetSwitchMaterial = findViewById(R.id.imppar_targetSwitch_sch)
//        amountSwitchMaterial = findViewById(R.id.imppar_amountSwitch_sch)
//        dateSwitchMaterial = findViewById(R.id.imppar_dateSwitch_sch)
//        categorySwitchMaterial = findViewById(R.id.imppar_categorySwitch_sch)
//        commentSwitchMaterial = findViewById(R.id.imppar_commentSwitch_sch)
//
//        importButton = findViewById(R.id.imppar_accept_btn)
//        exampleTransactionHolder = findViewById(R.id.imppar_transactionExampleRecycler_rcv)

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
            }
        }
    }

    private fun pickCsvFile() {
        filePickerLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        })
    }








//    private fun readTransactionDataFromCSV() {
//        val inputStream = contentResolver.openInputStream(fileUri!!)
//        inputStream?.let { stream ->
//            val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
//            val csvParser= CSVParserBuilder().withSeparator(csvSeparator).build()
//            val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
//            var line: Array<String>?
//            csvReader.readNext().also { line = it }
//            importedTransactions = mutableListOf()
//            while (csvReader.readNext().also { line = it } != null) {
//                val assetTxt = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem?.toString()
//                val targetTxt = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
//                val amountTxt = if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition) else amountStaticValue.text.toString()
//                val amount = amountTxt.parseToDouble() ?: 0.0
//                val categoryTxt = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else
//                    if (amount<0) categoryExpenseStaticSpinner.selectedItem?.toString() else categoryIncomeStaticSpinner.selectedItem?.toString()
//                val dateTxt = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition) else dateStaticValue.toString()
//                val commentTxt = if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()
//
//                importedTransactions.add(Transaction(
//                    assetId = AustromApplication.activeAssets.values.find { l -> l.assetName == assetTxt }?.assetId ?: assetTxt.toString(),
//                    transactionName = targetTxt.toString(),
//                    categoryId = if (amount>0) AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.EXPENSE }.find { l -> l.name == categoryTxt }?.categoryId ?: categoryTxt.toString() else
//                        AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.INCOME }.find { l -> l.name == categoryTxt }?.categoryId ?: categoryTxt.toString(),
//                    amount =amount,
//                    transactionDate = dateTxt.parseToLocalDate() ?: LocalDate.now(),
//                    comment =  commentTxt.toString()
//                ))
//            }
//            val adapter = TransactionExtendedRecyclerAdapter(importedTransactions, this, true)
//            exampleTransactionHolder.adapter = adapter
//            exampleTransactionHolder.adapter!!.notifyItemRangeChanged(0, importedTransactions.size)
//        }
//    }
//
//    private fun updateExampleTransactionFromCSV() {
//        val inputStream = contentResolver.openInputStream(fileUri!!)
//        inputStream?.let { stream ->
//            val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
//            val csvParser= CSVParserBuilder().withSeparator(csvSeparator).build()
//            val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
//            var line: Array<String>?
//            csvReader.readNext().also { line = it }
//            csvReader.readNext().also { line = it }
//            if (line != null) {
//                val assetTxt = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem?.toString()
//                val targetTxt = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
//                val amountTxt = if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition) else amountStaticValue.text.toString()
//                val categoryTxt = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryExpenseStaticSpinner.selectedItem?.toString()
//                val dateTxt = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition) else dateStaticValue.toString()
//                val commentTxt = if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()
//
//                exampleTransactionHolder.adapter = TransactionExtendedRecyclerAdapter(mutableListOf(Transaction(
//                    assetId = assetTxt.toString(),
//                    transactionName = targetTxt.toString(),
//                    amount = amountTxt.parseToDouble() ?: 0.0,
//                    categoryId = categoryTxt.toString(),
//                    transactionDate = dateTxt.parseToLocalDate() ?: LocalDate.now(),
//                    comment = commentTxt
//                )), this)
//                exampleTransactionHolder.adapter!!.notifyItemChanged(0)
//            }
//            reader.close()
//        }
//    }
//
//    private fun setUpSpinnersFromCSV() {
//        val inputStream = contentResolver.openInputStream(fileUri!!)
//        inputStream?.let { stream ->
//            val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
//            val csvParser = CSVParserBuilder().withSeparator(csvSeparator).build()
//            val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
//            var line: Array<String>?
//            csvReader.readNext().also { line = it }
//            if (line != null) {
//                val columnNames = line?.toMutableList()
//                if (columnNames!=null) {
//                    sourceDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
//                    targetDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
//                    amountDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
//                    dateDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
//                    categoryDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
//                    commentDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
//
//                    val dbProvider = LocalDatabaseProvider(this)
//
//                    sourceStaticSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
//                        AustromApplication.activeAssets.map { it.value.assetName })
//                    categoryExpenseStaticSpinner.adapter = CategoryArrayAdapter(this, dbProvider.getCategories(TransactionType.EXPENSE))
//                    categoryIncomeStaticSpinner.adapter = CategoryArrayAdapter(this, dbProvider.getCategories(TransactionType.INCOME))
//                }
//            }
//            csvReader.close()
//        }
//    }
//
//    private fun switchVisibilityOfSpinners(switch: SwitchMaterial,  dynamicSpinner: Spinner, staticSpinner: Spinner? = null, staticEditTextView: EditText? = null, staticLabel: TextView? = null) {
//        if (switch.isChecked) {
//            dynamicSpinner.visibility = View.VISIBLE
//            staticSpinner?.visibility = View.GONE
//            staticEditTextView?.visibility = View.GONE
//            staticLabel?.visibility = View.GONE
//        } else {
//            dynamicSpinner.visibility = View.GONE
//            staticSpinner?.visibility = View.VISIBLE
//            staticEditTextView?.visibility = View.VISIBLE
//            staticLabel?.visibility = View.VISIBLE
//        }
//        updateExampleTransactionFromCSV()
//        switch.text =if (switch.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
//    }
}