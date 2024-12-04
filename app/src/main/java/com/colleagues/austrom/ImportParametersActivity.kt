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
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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



    @SuppressLint("NotifyDataSetChanged")
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
        exampleTransactionHolder.layoutManager = LinearLayoutManager(this)
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
        dateDynamicSpinner.onItemSelectedListener = selectionChangedListener
        categoryDynamicSpinner.onItemSelectedListener = selectionChangedListener
        commentDynamicSpinner.onItemSelectedListener = selectionChangedListener

        sourceStaticSpinner.onItemSelectedListener = selectionChangedListener
        targetStaticValue.onFocusChangeListener = valueChangedListener
        amountStaticValue.onFocusChangeListener = valueChangedListener
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

        categorySwitchMaterial.setOnClickListener {
            if (categorySwitchMaterial.isChecked) {
                categoryDynamicSpinner.visibility = View.VISIBLE
                categoryExpenseStaticLabel.visibility = View.GONE
                categoryExpenseStaticSpinner.visibility = View.GONE
                categoryIncomeStaticLabel.visibility = View.GONE
                categoryIncomeStaticSpinner.visibility = View.GONE
            } else {
                categoryDynamicSpinner.visibility = View.GONE
                categoryExpenseStaticLabel.visibility = View.VISIBLE
                categoryExpenseStaticSpinner.visibility = View.VISIBLE
                categoryIncomeStaticLabel.visibility = View.VISIBLE
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
                    val amount = (if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition).parseToDouble() else amountStaticValue.text.toString().parseToDouble()) ?: 0.0
                    var sourceName: String? = null
                    var sourceId: String? = null
                    var targetName: String? = null
                    var targetId: String? = null
                    var categoryId: String? = null
                    val transactionDate: LocalDate? = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition).parseToLocalDate() else dateStaticValue.toString().parseToLocalDate()
                    if (amount==0.0) {
                        //TODO("Something Should be Done Here")
                    }
                    if (amount>0) {
                        targetId = asset?.assetId
                        val categories = dbProvider.getCategories(TransactionType.INCOME)
                        targetName = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString()
                        sourceName = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
                        categoryId = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categoryIncomeStaticSpinner.selectedItem as Category).id
//                        categoryId = AustromApplication.getActiveIncomeCategories().find {
//                            it.name == if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryStaticSpinner.selectedItem.toString()
//                        }?.id.toString()
                    }
                    if (amount<0) {
                        sourceId = asset?.assetId
                        val categories = dbProvider.getCategories(TransactionType.EXPENSE)
                        sourceName = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem.toString()
                        targetName = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
                        categoryId = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else (categoryExpenseStaticSpinner.selectedItem as Category).id
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
                        amount = amount,
                        transactionDate = transactionDate,
                        comment =  if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()
                    ))
                }
                exampleTransactionHolder.adapter = TransactionExtendedRecyclerAdapter(importedTransactions, this, true)
                exampleTransactionHolder.adapter!!.notifyDataSetChanged()
//                val dbProvider = LocalDatabaseProvider(this)
//                for (transaction in importedTransactions) {
//                    val asset: Asset? =  if (transaction.sourceId!=null) AustromApplication.activeAssets[transaction.sourceId] else AustromApplication.activeAssets[transaction.targetId]
//                    if (asset!=null) {
//                        asset.amount = if (transaction.sourceId!=null) asset.amount - transaction.amount else asset.amount+transaction.amount
//                        dbProvider.writeNewTransaction(transaction)
//                        dbProvider.updateAsset(asset)
//                    }
//                }
//                this.finish()
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
//                sourceTxt.text = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem?.toString()
//                targetTxt.text = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
//                amountTxt.text = if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition) else amountStaticValue.text.toString()
//                currencyTxt.text = if (currencySwitchMaterial.isChecked) line?.get(currencyDynamicSpinner.selectedItemPosition) else currencyStaticSpinner.selectedItem?.toString()
//                categoryTxt.text = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryExpenseStaticSpinner.selectedItem?.toString()
//                dateTxt.text = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition) else dateStaticValue.toString()
//                commentTxt.text = if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()

                val sourceTxt = if (sourceSwitchMaterial.isChecked) line?.get(sourceDynamicSpinner.selectedItemPosition) else sourceStaticSpinner.selectedItem?.toString()
                val targetTxt = if (targetSwitchMaterial.isChecked) line?.get(targetDynamicSpinner.selectedItemPosition) else targetStaticValue.text.toString()
                val amountTxt = if (amountSwitchMaterial.isChecked) line?.get(amountDynamicSpinner.selectedItemPosition) else amountStaticValue.text.toString()
                val categoryTxt = if (categorySwitchMaterial.isChecked) line?.get(categoryDynamicSpinner.selectedItemPosition) else categoryExpenseStaticSpinner.selectedItem?.toString()
                val dateTxt = if (dateSwitchMaterial.isChecked) line?.get(dateDynamicSpinner.selectedItemPosition) else dateStaticValue.toString()
                val commentTxt = if (commentSwitchMaterial.isChecked) line?.get(commentDynamicSpinner.selectedItemPosition) else commentStaticValue.text.toString()


                exampleTransactionHolder.adapter = TransactionExtendedRecyclerAdapter(listOf(Transaction(
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
    }
}