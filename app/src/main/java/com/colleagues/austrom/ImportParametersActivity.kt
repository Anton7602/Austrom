package com.colleagues.austrom

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.switchmaterial.SwitchMaterial
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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
    private lateinit var currencyDynamicSpinner: Spinner
    private lateinit var currencyStaticSpinner: Spinner
    private lateinit var currencySwitchMaterial: SwitchMaterial
    private lateinit var dateDynamicSpinner: Spinner
    private lateinit var dateStaticValue: Spinner
    private lateinit var dateSwitchMaterial: SwitchMaterial
    private lateinit var categoryDynamicSpinner: Spinner
    private lateinit var categoryExpenseStaticSpinner: Spinner
    private lateinit var categoryIncomeStaticSpinner: Spinner
    private lateinit var categorySwitchMaterial: SwitchMaterial
    private lateinit var commentDynamicSpinner: Spinner
    private lateinit var commentStaticValue: EditText
    private lateinit var commentSwitchMaterial: SwitchMaterial
    private lateinit var sourceTxt: TextView
    private lateinit var targetTxt: TextView
    private lateinit var amountTxt: TextView
    private lateinit var currencyTxt: TextView
    private lateinit var categoryTxt: TextView
    private lateinit var dateTxt: TextView
    private lateinit var commentTxt: TextView
    private lateinit var importButton: Button


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
            setUpSpinnersFromCSV(fileUri!!)
        }

        val selectionChangedListener = object :  OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateExampleTransactionFromCSV(fileUri!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val valueChangedListener = OnFocusChangeListener { _, _ ->
            updateExampleTransactionFromCSV(fileUri!!)
        }


        sourceDynamicSpinner.onItemSelectedListener = selectionChangedListener
        targetDynamicSpinner.onItemSelectedListener = selectionChangedListener
        amountDynamicSpinner.onItemSelectedListener = selectionChangedListener
        currencyDynamicSpinner.onItemSelectedListener = selectionChangedListener
        dateDynamicSpinner.onItemSelectedListener = selectionChangedListener
        categoryDynamicSpinner.onItemSelectedListener = selectionChangedListener
        commentDynamicSpinner.onItemSelectedListener = selectionChangedListener

        sourceStaticSpinner.onItemSelectedListener = selectionChangedListener
        targetStaticValue.onFocusChangeListener = valueChangedListener
        amountStaticValue.onFocusChangeListener = valueChangedListener
        currencyStaticSpinner.onItemSelectedListener = selectionChangedListener
        dateDynamicSpinner.onFocusChangeListener = valueChangedListener
        categoryExpenseStaticSpinner.onItemSelectedListener = selectionChangedListener
        commentStaticValue.onFocusChangeListener = valueChangedListener

        sourceSwitchMaterial.setOnClickListener {
            if (sourceSwitchMaterial.isChecked) {
                sourceDynamicSpinner.visibility = View.VISIBLE
                sourceStaticSpinner.visibility = View.GONE
            } else {
                sourceDynamicSpinner.visibility = View.GONE
                sourceStaticSpinner.visibility = View.VISIBLE
            }
            updateExampleTransactionFromCSV(fileUri!!)
            sourceSwitchMaterial.text =if (sourceSwitchMaterial.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
        }

        targetSwitchMaterial.setOnClickListener {
            if (targetSwitchMaterial.isChecked) {
                targetDynamicSpinner.visibility = View.VISIBLE
                targetStaticValue.visibility = View.GONE
            } else {
                targetDynamicSpinner.visibility = View.GONE
                targetStaticValue.visibility = View.VISIBLE
            }
            updateExampleTransactionFromCSV(fileUri!!)
            targetSwitchMaterial.text =if (targetSwitchMaterial.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
        }

        amountSwitchMaterial.setOnClickListener {
            if (amountSwitchMaterial.isChecked) {
                amountDynamicSpinner.visibility = View.VISIBLE
                amountStaticValue.visibility = View.GONE
            } else {
                amountDynamicSpinner.visibility = View.GONE
                amountStaticValue.visibility = View.VISIBLE
            }
            updateExampleTransactionFromCSV(fileUri!!)
            amountSwitchMaterial.text =if (amountSwitchMaterial.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
        }

        currencySwitchMaterial.setOnClickListener {
            if (currencySwitchMaterial.isChecked) {
                currencyDynamicSpinner.visibility = View.VISIBLE
                currencyStaticSpinner.visibility = View.GONE
            } else {
                currencyDynamicSpinner.visibility = View.GONE
                currencyStaticSpinner.visibility = View.VISIBLE
            }
            updateExampleTransactionFromCSV(fileUri!!)
            currencySwitchMaterial.text =if (currencySwitchMaterial.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
        }

        categorySwitchMaterial.setOnClickListener {
            if (categorySwitchMaterial.isChecked) {
                categoryDynamicSpinner.visibility = View.VISIBLE
                categoryExpenseStaticSpinner.visibility = View.GONE
                categoryIncomeStaticSpinner.visibility = View.GONE
            } else {
                categoryDynamicSpinner.visibility = View.GONE
                categoryExpenseStaticSpinner.visibility = View.VISIBLE
                categoryIncomeStaticSpinner.visibility = View.VISIBLE
            }
            updateExampleTransactionFromCSV(fileUri!!)
            categorySwitchMaterial.text =if (categorySwitchMaterial.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
        }

        dateSwitchMaterial.setOnClickListener {
            if (dateSwitchMaterial.isChecked) {
                dateDynamicSpinner.visibility = View.VISIBLE
                dateStaticValue.visibility = View.GONE
            } else {
                dateDynamicSpinner.visibility = View.GONE
                dateStaticValue.visibility = View.VISIBLE
            }
            updateExampleTransactionFromCSV(fileUri!!)
            dateSwitchMaterial.text =if (dateSwitchMaterial.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
        }

        commentSwitchMaterial.setOnClickListener {
            if (commentSwitchMaterial.isChecked) {
                commentDynamicSpinner.visibility = View.VISIBLE
                commentStaticValue.visibility = View.GONE
            } else {
                commentDynamicSpinner.visibility = View.GONE
                commentStaticValue.visibility = View.VISIBLE
            }
            updateExampleTransactionFromCSV(fileUri!!)
            commentSwitchMaterial.text =if (commentSwitchMaterial.isChecked) getString(R.string.string_dynamic) else getString(R.string.string_static)
        }

        importButton.setOnClickListener {
            val inputStream = contentResolver.openInputStream(fileUri!!)
            inputStream?.let { stream ->
                val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
                val csvParser= CSVParserBuilder().withSeparator(csvSeparator).build()
                val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
                var line: Array<String>?
                csvReader.readNext().also { line = it }


                val importedTransactions = mutableListOf<Transaction>()
                while (csvReader.readNext().also { line = it } != null) {
                    val asset = AustromApplication.activeAssets.values.find {
                        it.assetName == if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString()
                    }
                    if (asset==null) {
                        //TODO("Something Should Be Done Here")
                    }
                    val dbProvider = LocalDatabaseProvider(this)
                    val amount = parseDouble(if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition)!! else amountStaticValue.text.toString()) ?: 0.0
                    var sourceName: String? = null
                    var sourceId: String? = null
                    var targetName: String? = null
                    var targetId: String? = null
                    var categoryId: String? = null
                    val transactionDate: LocalDate? = parseDate(if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition)!! else dateStaticValue.toString())
                    if (amount==0.0) {
                        //TODO("Something Should be Done Here")
                    }
                    if (amount>0) {
                        targetId = asset?.assetId
                        val categories = dbProvider.getCategories(TransactionType.INCOME)
                        targetName = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString()
                        sourceName = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
                        categoryId = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categories.find { l -> l.name == categoryIncomeStaticSpinner.selectedItem.toString() })?.id
//                        categoryId = AustromApplication.getActiveIncomeCategories().find {
//                            it.name == if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryStaticSpinner.selectedItem.toString()
//                        }?.id.toString()
                    }
                    if (amount<0) {
                        sourceId = asset?.assetId
                        val categories = dbProvider.getCategories(TransactionType.EXPENSE)
                        sourceName = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString()
                        targetName = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
                        categoryId = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categories.find { l -> l.name == categoryExpenseStaticSpinner.selectedItem.toString() })?.id
//                        categoryId = AustromApplication.getActiveExpenseCategories().find {
//                            it.name == if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryStaticSpinner.selectedItem.toString()
//                        }?.id.toString()
                    }
                    importedTransactions.add(Transaction(
                        userId = AustromApplication.appUser!!.userId,
                        sourceName = sourceName,
                        sourceId = sourceId,
                        targetName = targetName,
                        targetId = targetId,
                        categoryId = categoryId,
                        amount = amount.absoluteValue,
                        transactionDate = transactionDate,
                        comment =  if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()
                    ))
                }
                val dbProvider = LocalDatabaseProvider(this)
                for (transaction in importedTransactions) {
                    val asset: Asset? =  if (transaction.sourceId!=null) AustromApplication.activeAssets[transaction.sourceId] else AustromApplication.activeAssets[transaction.targetId]
                    if (asset!=null) {
                        asset.amount = if (transaction.sourceId!=null) asset.amount - transaction.amount else asset.amount+transaction.amount
                        dbProvider.writeNewTransaction(transaction)
                        dbProvider.updateAsset(asset)
                    }
                }
                this.finish()
            }
        }
    }

    private fun updateExampleTransactionFromCSV(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let { stream ->
            val reader = BufferedReader(InputStreamReader(stream, Charset.forName(charset)))
            val csvParser= CSVParserBuilder().withSeparator(csvSeparator).build()
            val csvReader = CSVReaderBuilder(reader).withCSVParser(csvParser).build()
            var line: Array<String>?
            csvReader.readNext().also { line = it }
            csvReader.readNext().also { line = it }
            if (line != null) {
                sourceTxt.text = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem?.toString()
                targetTxt.text = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
                amountTxt.text = if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition) else amountStaticValue.text.toString()
                currencyTxt.text = if (currencySwitchMaterial.isChecked) line?.get(currencyDynamicSpinner.selectedItemPosition) else currencyStaticSpinner.selectedItem?.toString()
                categoryTxt.text = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryExpenseStaticSpinner.selectedItem?.toString()
                dateTxt.text = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition) else dateStaticValue.toString()
                commentTxt.text = if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()
            }
            reader.close()
        }
    }

    private fun setUpSpinnersFromCSV(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
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
                    currencyDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    dateDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    categoryDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    commentDynamicSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)

                    val dbProvider = LocalDatabaseProvider(this)

                    sourceStaticSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        AustromApplication.activeAssets.map { it.value.assetName })
                    currencyStaticSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        AustromApplication.activeCurrencies.map { it.value.name })
                    categoryExpenseStaticSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        dbProvider.getCategories(TransactionType.EXPENSE).map { it.name!! })
                    categoryIncomeStaticSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        dbProvider.getCategories(TransactionType.INCOME).map { it.name!! })
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

    private fun parseDate(dateString: String): LocalDate? {
        val dateFormats = listOf(
            "dd.MM.yyyy HH:mm:ss",
            "dd.MM.yyyy",
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "MM/dd/yyyy",
            "yyyy/MM/dd",
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "MM-dd-yyyy",
            "yyyyMMdd"
        )

        for (format in dateFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(format)
                return LocalDate.parse(dateString, formatter)
            } catch (e: DateTimeParseException) {
            }
        }
        return null
    }

    fun parseDouble(dateString: String): Double? {
        val normalizedValue = dateString.replace(',', '.')
        return normalizedValue.toDoubleOrNull()
    }

    private fun bindViews() {
        sourceDynamicSpinner = findViewById(R.id.imppar_source_dynamic_spn)
        targetDynamicSpinner = findViewById(R.id.imppar_target_dynamic_spn)
        amountDynamicSpinner = findViewById(R.id.imppar_amount_dynamic_spn)
        currencyDynamicSpinner = findViewById(R.id.imppar_currency_dynamic_spn)
        dateDynamicSpinner = findViewById(R.id.imppar_date_dynamic_spn)
        categoryDynamicSpinner = findViewById(R.id.imppar_category_dynamic_spn)
        commentDynamicSpinner = findViewById(R.id.imppar_comment_dynamic_spn)

        sourceStaticSpinner = findViewById(R.id.imppar_source_static_spn)
        targetStaticValue = findViewById(R.id.imppar_target_static_txt)
        amountStaticValue = findViewById(R.id.imppar_amount_static_txt)
        currencyStaticSpinner = findViewById(R.id.imppar_currency_static_spn)
        dateStaticValue = findViewById(R.id.imppar_date_static_spn)
        categoryExpenseStaticSpinner = findViewById(R.id.imppar_category_expense_static_spn2)
        categoryIncomeStaticSpinner = findViewById(R.id.imppar_category_income_static_spn)
        commentStaticValue = findViewById(R.id.imppar_comment_static_txt)

        sourceSwitchMaterial = findViewById(R.id.imppar_sourceSwitch_sch)
        targetSwitchMaterial = findViewById(R.id.imppar_targetSwitch_sch)
        amountSwitchMaterial = findViewById(R.id.imppar_amountSwitch_sch)
        currencySwitchMaterial = findViewById(R.id.imppar_currencySwitch_sch)
        dateSwitchMaterial = findViewById(R.id.imppar_dateSwitch_sch)
        categorySwitchMaterial = findViewById(R.id.imppar_categorySwitch_sch)
        commentSwitchMaterial = findViewById(R.id.imppar_commentSwitch_sch)

        sourceTxt = findViewById(R.id.imppar_example_sourceName_txt)
        targetTxt = findViewById(R.id.imppar_example_targetName_txt)
        amountTxt = findViewById(R.id.imppar_example_amount_txt)
        currencyTxt = findViewById(R.id.imppar_example_currencySymbol_txt)
        categoryTxt = findViewById(R.id.imppar_example_category_txt)
        dateTxt = findViewById(R.id.imppar_example_date_txt)
        commentTxt = findViewById(R.id.imppar_example_comment_txt)

        importButton = findViewById(R.id.imppar_accept_btn)
    }
}