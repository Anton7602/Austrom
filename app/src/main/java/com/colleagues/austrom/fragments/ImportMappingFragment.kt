package com.colleagues.austrom.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.TransactionExtendedRecyclerAdapter
import com.colleagues.austrom.dialogs.ImportMappingDialogFragment
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.parseToLocalDate
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.SelectorButtonView
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDate

class ImportMappingFragment(private val fileUri: Uri? = null) : Fragment(R.layout.fragment_import_mapping) {
    constructor(): this(null)
    fun setOnFragmentChangeRequestedListener(l: ((Fragment?)->Unit)) { changeFragment = l }
    private var changeFragment: (Fragment?) -> Unit = {}
    //region Binding
    private lateinit var assetSelector: SelectorButtonView
    private lateinit var nameSelector: SelectorButtonView
    private lateinit var amountSelector: SelectorButtonView
    private lateinit var dateSelector: SelectorButtonView
    private lateinit var categorySelector: SelectorButtonView
    private lateinit var commentSelector: SelectorButtonView
    private lateinit var demoTransactionRecycler: RecyclerView
    private lateinit var importButton: Button
    private fun bindViews(view: View) {
        assetSelector = view.findViewById(R.id.impmap_assetSelector_selb)
        nameSelector = view.findViewById(R.id.impmap_nameSelector_selb)
        amountSelector = view.findViewById(R.id.impmap_amountSelector_selb)
        dateSelector = view.findViewById(R.id.impmap_dateSelector_selb)
        categorySelector = view.findViewById(R.id.impmap_categorySelector_selb)
        commentSelector = view.findViewById(R.id.impmap_commentSelector_selb)
        demoTransactionRecycler = view.findViewById(R.id.impmap_demoTransactionHolder_rcv)
        importButton = view.findViewById(R.id.impmap_impotButton_btn)

    }
    //endregion
    private var csvSeparator: Char = ','
    private var charset = "windows-1251"
    private var csvColumnsList: List<String> = listOf()
    private var selectorsList: List<SelectorButtonView> =  listOf()
    private var fileMap: MutableMap<String, Int> = mutableMapOf("asset" to 1, "name" to 1, "amount" to 1, "date" to 1, "category" to 1, "comment" to 1)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    override fun onStart() {
        super.onStart()
        if (fileUri!=null)
        {
            detectSeparator(fileUri)
            readCsvSchema(fileUri)
            selectorsList = listOf(assetSelector, nameSelector, amountSelector, dateSelector, categorySelector, commentSelector)
            if (csvColumnsList.isNotEmpty()) {
                updateDemoTransaction(fileUri)
                selectorsList.forEach { selector -> selector.setOnClickListener{ launchMappingDialog(selector)}; selector.setFieldValue("*Mapped To File - ${csvColumnsList.first()}") }
            }
            importButton.setOnClickListener { changeFragment(TransactionApprovementFragment(readTransactionDataFromCSV(fileUri))) }
        }
    }

    private fun launchMappingDialog(clickedSelector: SelectorButtonView) {
        val dialog = when (clickedSelector.id) {
            assetSelector.id -> ImportMappingDialogFragment(csvColumnsList, AustromApplication.activeAssets.values.map {it.assetName}, "asset")
            nameSelector.id -> ImportMappingDialogFragment(csvColumnsList, listOf(), "name")
            amountSelector.id -> ImportMappingDialogFragment(csvColumnsList, listOf(), "amount")
            dateSelector.id -> ImportMappingDialogFragment(csvColumnsList, listOf(), "date")
            categorySelector.id -> ImportMappingDialogFragment(csvColumnsList, AustromApplication.activeCategories.values.filter { it.transactionType!=TransactionType.TRANSFER }.map { it.name }, "category")
            commentSelector.id -> ImportMappingDialogFragment(csvColumnsList, listOf(), "comment")
            else -> null
        }
        if (dialog==null) return
        dialog.setOnDialogResultListener { isFromFile, selectedValue, keyMap ->
            clickedSelector.setFieldValue(if (isFromFile) "*Mapped To File - $selectedValue" else selectedValue)
            fileMap[keyMap] = if (!isFromFile) 0 else csvColumnsList.indexOf(selectedValue)
            updateDemoTransaction(fileUri!!)
        }
        dialog.show(requireActivity().supportFragmentManager, "Value Selection Dialog")
    }

    private fun initializeCsvReader(uri: Uri): CSVReader? {
        val inputStream = requireActivity().contentResolver.openInputStream(uri) ?: return null
        val reader = BufferedReader(InputStreamReader(inputStream, Charset.forName(charset)))
        val csvParser = CSVParserBuilder().withSeparator(csvSeparator).build()
        return CSVReaderBuilder(reader).withCSVParser(csvParser).build()
    }

    private fun readCsvSchema(uri: Uri) {
        val csvReader = initializeCsvReader(uri) ?: return
        csvColumnsList  = csvReader.readNext().toList()
        csvReader.close()
    }

    private fun updateDemoTransaction(uri: Uri) {
        val csvReader = initializeCsvReader(uri) ?: return
        var line: Array<String>?
        csvReader.readNext().also { line = it }
        csvReader.readNext().also { line = it }
        if (line!=null) {
            val adapter = TransactionExtendedRecyclerAdapter(mutableListOf(readTransactionFromCSVLine(line!!)), requireActivity())
            demoTransactionRecycler.layoutManager = LinearLayoutManager(requireActivity())
            demoTransactionRecycler.adapter = adapter
        }
    }

    private fun readTransactionDataFromCSV(uri: Uri): MutableList<Transaction> {
        val csvReader = initializeCsvReader(uri) ?: return mutableListOf()
        var line: Array<String>?
        csvReader.readNext().also { line = it }
        val importedTransactions = mutableListOf<Transaction>()
        while (csvReader.readNext().also { line = it } != null) {
            importedTransactions.add(readTransactionFromCSVLine(line!!))
        }

        csvReader.close()
        return importedTransactions
    }

    private fun readTransactionFromCSVLine(line: Array<String>): Transaction {
        val assetTxt = if (fileMap["asset"]!=0) line[fileMap["asset"]!!] else assetSelector.getValue()
        val targetTxt = if (fileMap["name"]!=0) line[fileMap["name"]!!] else nameSelector.getValue()
        val amountTxt = if (fileMap["amount"]!=0) line[fileMap["amount"]!!] else amountSelector.getValue()
        val amount = amountTxt.parseToDouble() ?: 0.0
        val categoryTxt = if (fileMap["category"]!=0) line[fileMap["category"]!!] else categorySelector.getValue()
        val dateTxt = if (fileMap["date"]!=0) {line[fileMap["date"]!!]} else dateSelector.getValue()
        val commentTxt = if (fileMap["comment"]!=0) line[fileMap["comment"]!!] else commentSelector.getValue()

        return Transaction(
            assetId = AustromApplication.activeAssets.values.find { l -> l.assetName == assetTxt }?.assetId ?: assetTxt,
            transactionName = targetTxt.toString(),
            categoryId = if (amount>0) AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.EXPENSE }.find { l -> l.name == categoryTxt }?.categoryId ?: categoryTxt.toString() else
                AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.INCOME }.find { l -> l.name == categoryTxt }?.categoryId ?: categoryTxt.toString(),
            amount =amount,
            transactionDate = dateTxt.parseToLocalDate() ?: LocalDate.now(),
            comment =  commentTxt.toString()
        )
    }

    private fun detectSeparator(uri: Uri) {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
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
}