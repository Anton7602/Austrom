package com.colleagues.austrom

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.CategoryArrayAdapter
import com.colleagues.austrom.adapters.TransactionExtendedRecyclerAdapter
import com.colleagues.austrom.adapters.TransactionGroupRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.parseToLocalDate
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.switchmaterial.SwitchMaterial
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDate
import kotlin.math.absoluteValue

class ImportParametersActivity : AppCompatActivity() {
    private var fileUri : Uri? = null
    private var csvSeparator: Char = ','
    private var charset = "windows-1251"
    private lateinit var sourceDynamicSpinner: Spinner
    private lateinit var sourceStaticSpinner: Spinner
    private lateinit var sourceSwitchMaterial: SwitchMaterial
    private lateinit var targetDynamicSpinner: Spinner
    private lateinit var targetStaticValue: EditText
    private lateinit var targetSwitchMaterial: SwitchMaterial
    private lateinit var amountDynamicSpinner: Spinner
    private lateinit var amountStaticValue: EditText
    private lateinit var amountSwitchMaterial: SwitchMaterial
    private lateinit var dateDynamicSpinner: Spinner
    private lateinit var dateStaticValue: Spinner
    private lateinit var dateSwitchMaterial: SwitchMaterial
    private lateinit var categoryDynamicSpinner: Spinner
    private lateinit var categoryExpenseStaticSpinner: Spinner
    private lateinit var categoryExpenseStaticLabel: TextView
    private lateinit var categoryIncomeStaticSpinner: Spinner
    private lateinit var categoryIncomeStaticLabel: TextView
    private lateinit var categorySwitchMaterial: SwitchMaterial
    private lateinit var commentDynamicSpinner: Spinner
    private lateinit var commentStaticValue: EditText
    private lateinit var commentSwitchMaterial: SwitchMaterial
    private lateinit var importButton: Button
    private lateinit var exampleTransactionHolder: RecyclerView
    private lateinit var fieldMappingHolder: ScrollView
    private lateinit var importControlHolder: CardView
    private lateinit var importNumberOfTransactionsMessageView: TextView
    private lateinit var topLabel: TextView
    private lateinit var importAcceptedTransactions: Button
    private var importedTransactions: MutableList<Transaction> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_import_parameters)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bindViews()
        fileUri = intent.getParcelableExtra("FilePath") as Uri?
        if (fileUri!=null) {
            detectSeparator()
            setUpSpinnersFromCSV()
        } else {
            finish()
        }

        exampleTransactionHolder.layoutManager = LinearLayoutManager(this)
        val selectionChangedListener = object :  OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { updateExampleTransactionFromCSV() }
            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }
        val valueChangedListener = OnFocusChangeListener { _, _ -> updateExampleTransactionFromCSV()}
        sourceDynamicSpinner.onItemSelectedListener = selectionChangedListener
        targetDynamicSpinner.onItemSelectedListener = selectionChangedListener
        amountDynamicSpinner.onItemSelectedListener = selectionChangedListener
        dateDynamicSpinner.onItemSelectedListener = selectionChangedListener
        categoryDynamicSpinner.onItemSelectedListener = selectionChangedListener
        commentDynamicSpinner.onItemSelectedListener = selectionChangedListener

        sourceStaticSpinner.onItemSelectedListener = selectionChangedListener
        targetStaticValue.onFocusChangeListener = valueChangedListener
        amountStaticValue.onFocusChangeListener = valueChangedListener
        dateDynamicSpinner.onFocusChangeListener = valueChangedListener
        categoryExpenseStaticSpinner.onItemSelectedListener = selectionChangedListener
        commentStaticValue.onFocusChangeListener = valueChangedListener

        sourceSwitchMaterial.setOnClickListener {switchVisibilityOfSpinners(sourceSwitchMaterial, sourceDynamicSpinner, sourceStaticSpinner)}
        targetSwitchMaterial.setOnClickListener {switchVisibilityOfSpinners(targetSwitchMaterial, targetDynamicSpinner, null, targetStaticValue) }
        amountSwitchMaterial.setOnClickListener {switchVisibilityOfSpinners(amountSwitchMaterial, amountDynamicSpinner, null, amountStaticValue) }
        categorySwitchMaterial.setOnClickListener {
            switchVisibilityOfSpinners(categorySwitchMaterial, categoryDynamicSpinner, categoryExpenseStaticSpinner, null, categoryExpenseStaticLabel)
            switchVisibilityOfSpinners(categorySwitchMaterial, categoryDynamicSpinner, categoryIncomeStaticSpinner, null, categoryIncomeStaticLabel)
        }
        dateSwitchMaterial.setOnClickListener {switchVisibilityOfSpinners(dateSwitchMaterial, dateDynamicSpinner, dateStaticValue)}
        commentSwitchMaterial.setOnClickListener {switchVisibilityOfSpinners(commentSwitchMaterial, commentDynamicSpinner, null, commentStaticValue)}

        importButton.setOnClickListener {
            readTransactionDataFromCSV()
        }

        importAcceptedTransactions.setOnClickListener {
//            (exampleTransactionHolder.adapter as TransactionExtendedRecyclerAdapter).viewHolders.forEach { viewHolder ->
//                val transaction = viewHolder.transaction
//                if (transaction!=null && !viewHolder.isIssueEncountered) {
//                    val dbProvider = LocalDatabaseProvider(this)
//                    transaction.submit(dbProvider)
//                }
//            }
        }
    }

    private fun readTransactionDataFromCSV() {
        val inputStream = contentResolver.openInputStream(fileUri!!)
        inputStream?.let { stream ->
            val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
            val csvParser= CSVParserBuilder().withSeparator(csvSeparator).build()
            val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
            var line: Array<String>?
            csvReader.readNext().also { line = it }
            importedTransactions = mutableListOf()
            while (csvReader.readNext().also { line = it } != null) {
                val asset = AustromApplication.activeAssets.values.find { it.assetName == if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString() }
                val amount = (if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition).parseToDouble() else amountStaticValue.text.toString().parseToDouble()) ?: 0.0

                if (amount>0) {
                    val category = AustromApplication.activeIncomeCategories.values.find { l -> l.name == (if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categoryIncomeStaticSpinner.selectedItem as Category).name) }
                    importedTransactions.add(Transaction(
                        userId = AustromApplication.appUser!!.userId,
                        sourceName = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString(),
                        sourceId = null,
                        targetName = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString(),
                        targetId = asset?.assetId,
                        categoryId = category?.id
                            ?: if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categoryIncomeStaticSpinner.selectedItem as Category).name,
                        amount = amount,
                        transactionDate = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition).parseToLocalDate() else dateStaticValue.toString().parseToLocalDate(),
                        comment =  if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()
                    ))
                } else {
                    val category = AustromApplication.activeExpenseCategories.values.find { l -> l.name == (if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categoryIncomeStaticSpinner.selectedItem as Category).name) }
                    importedTransactions.add(Transaction(
                        userId = AustromApplication.appUser!!.userId,
                        sourceName = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString(),
                        sourceId = asset?.assetId,
                        targetName = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString(),
                        targetId = null,
                        categoryId = category?.id
                            ?: if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categoryIncomeStaticSpinner.selectedItem as Category).name,
                        amount = amount,
                        transactionDate = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition).parseToLocalDate() else dateStaticValue.toString().parseToLocalDate(),
                        comment =  if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()
                    ))
                }
            }
            exampleTransactionHolder.adapter = TransactionExtendedRecyclerAdapter(importedTransactions, this, true)
            exampleTransactionHolder.adapter!!.notifyItemRangeChanged(0, importedTransactions.size)
            switchLayoutsVisibilities()
        }
    }

    private fun updateExampleTransactionFromCSV() {
        val inputStream = contentResolver.openInputStream(fileUri!!)
        inputStream?.let { stream ->
            val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
            val csvParser= CSVParserBuilder().withSeparator(csvSeparator).build()
            val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
            var line: Array<String>?
            csvReader.readNext().also { line = it }
            csvReader.readNext().also { line = it }
            if (line != null) {
                val sourceTxt = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem?.toString()
                val targetTxt = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
                val amountTxt = if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition) else amountStaticValue.text.toString()
                val categoryTxt = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryExpenseStaticSpinner.selectedItem?.toString()
                val dateTxt = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition) else dateStaticValue.toString()
                val commentTxt = if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()

                exampleTransactionHolder.adapter = TransactionExtendedRecyclerAdapter(mutableListOf(Transaction(
                    sourceName = sourceTxt,
                    targetName = targetTxt,
                    amount = amountTxt.parseToDouble() ?: 0.0,
                    categoryId = categoryTxt,
                    transactionDate = dateTxt.parseToLocalDate(),
                    comment = commentTxt
                )), this)
                exampleTransactionHolder.adapter!!.notifyItemChanged(0)
            }
            reader.close()
        }
    }

    private fun setUpSpinnersFromCSV() {
        val inputStream = contentResolver.openInputStream(fileUri!!)
        inputStream?.let { stream ->
            val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
            val csvParser = CSVParserBuilder().withSeparator(csvSeparator).build()
            val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
            var line: Array<String>?
            csvReader.readNext().also { line = it }
            if (line != null) {
                val columnNames = line?.toMutableList()
                if (columnNames!=null) {
                    sourceDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    targetDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    amountDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    dateDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    categoryDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    commentDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)

                    val dbProvider = LocalDatabaseProvider(this)

                    sourceStaticSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        AustromApplication.activeAssets.map { it.value.assetName })
                    categoryExpenseStaticSpinner.adapter = CategoryArrayAdapter(this, dbProvider.getCategories(TransactionType.EXPENSE))
                    categoryIncomeStaticSpinner.adapter = CategoryArrayAdapter(this, dbProvider.getCategories(TransactionType.INCOME))
                }
            }
            csvReader.close()
        }
    }

    private fun detectSeparator() {
        val inputStream = contentResolver.openInputStream(fileUri!!)
        inputStream?.let { stream ->
            val reader = BufferedReader(InputStreamReader(stream, Charset.forName("UTF-8")))
            val separators = listOf(',', ';', '\t', '|')
            val separatorCounts = mutableMapOf<Char, Int>()

            repeat(5) {
                val line = reader.readLine() ?: return@repeat
                separators.forEach { separator ->
                    separatorCounts[separator] =
                        separatorCounts.getOrDefault(separator, 0) + line.count { it == separator }
                }
            }
            reader.close()
            csvSeparator = separatorCounts.maxByOrNull { it.value }?.key ?: ','
        }
    }

    private fun switchVisibilityOfSpinners(switch: SwitchMaterial,  dynamicSpinner: Spinner, staticSpinner: Spinner? = null, staticEditTextView: EditText? = null, staticLabel: TextView? = null) {
        if (switch.isChecked) {
            dynamicSpinner.visibility = View.VISIBLE
            staticSpinner?.visibility = View.GONE
            staticEditTextView?.visibility = View.GONE
            staticLabel?.visibility = View.GONE
        } else {
            dynamicSpinner.visibility = View.GONE
            staticSpinner?.visibility = View.VISIBLE
            staticEditTextView?.visibility = View.VISIBLE
            staticLabel?.visibility = View.VISIBLE
        }
        updateExampleTransactionFromCSV()
        switch.text =if (switch.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
    }

    private fun switchLayoutsVisibilities() {
        fieldMappingHolder.visibility = View.GONE
        importControlHolder.visibility = View.VISIBLE
        topLabel.text = getString(R.string.transactions_from_file)
        importNumberOfTransactionsMessageView.text = getString(R.string.transactions_are_read_from_file, importedTransactions.size)
    }

    private fun bindViews() {
        sourceDynamicSpinner = findViewById(R.id.imppar_source_dynamic_spn)
        targetDynamicSpinner = findViewById(R.id.imppar_target_dynamic_spn)
        amountDynamicSpinner = findViewById(R.id.imppar_amount_dynamic_spn)
        dateDynamicSpinner = findViewById(R.id.imppar_date_dynamic_spn)
        categoryDynamicSpinner = findViewById(R.id.imppar_category_dynamic_spn)
        commentDynamicSpinner = findViewById(R.id.imppar_comment_dynamic_spn)

        sourceStaticSpinner = findViewById(R.id.imppar_source_static_spn)
        targetStaticValue = findViewById(R.id.imppar_target_static_txt)
        amountStaticValue = findViewById(R.id.imppar_amount_static_txt)
        dateStaticValue = findViewById(R.id.imppar_date_static_spn)
        categoryExpenseStaticSpinner = findViewById(R.id.imppar_category_expense_static_spn2)
        categoryExpenseStaticLabel = findViewById(R.id.imppar_category_expense_label)
        categoryIncomeStaticSpinner = findViewById(R.id.imppar_category_income_static_spn)
        categoryIncomeStaticLabel = findViewById(R.id.imppar_category_income_label)
        commentStaticValue = findViewById(R.id.imppar_comment_static_txt)

        sourceSwitchMaterial = findViewById(R.id.imppar_sourceSwitch_sch)
        targetSwitchMaterial = findViewById(R.id.imppar_targetSwitch_sch)
        amountSwitchMaterial = findViewById(R.id.imppar_amountSwitch_sch)
        dateSwitchMaterial = findViewById(R.id.imppar_dateSwitch_sch)
        categorySwitchMaterial = findViewById(R.id.imppar_categorySwitch_sch)
        commentSwitchMaterial = findViewById(R.id.imppar_commentSwitch_sch)

        importButton = findViewById(R.id.imppar_accept_btn)
        exampleTransactionHolder = findViewById(R.id.imppar_transactionExampleRecycler_rcv)
        fieldMappingHolder = findViewById(R.id.imppar_filedMappingHolder_scv)
        importControlHolder = findViewById(R.id.imppar_importControlPanel_crv)
        importNumberOfTransactionsMessageView = findViewById(R.id.imppar_numberOfTransactions_txt)
        importAcceptedTransactions = findViewById(R.id.imppar_importAllTransactions_btn)
        topLabel = findViewById(R.id.imppar_topLabel_txt)
    }
}