package com.colleagues.austrom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.opencsv.CSVReader
import java.io.InputStreamReader

class ImportParametersActivity : AppCompatActivity() {
    private var fileUri : Uri? = null
    private lateinit var sourceSpinner: Spinner
    private lateinit var targetSpinner: Spinner
    private lateinit var amountSpinner: Spinner
    private lateinit var currencySpinner: Spinner
    private lateinit var dateSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var commentSpinner: Spinner
    private lateinit var sourceTxt: TextView
    private lateinit var targetTxt: TextView
    private lateinit var amountTxt: TextView
    private lateinit var currencyTxt: TextView


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
            setUpSpinnersFromCSV(fileUri!!)
        }

        val selectionChangedListener = object :  OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateExampleTransactionFromCSV(fileUri!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        sourceSpinner.onItemSelectedListener = selectionChangedListener
        targetSpinner.onItemSelectedListener = selectionChangedListener
        amountSpinner.onItemSelectedListener = selectionChangedListener
        currencySpinner.onItemSelectedListener = selectionChangedListener
        dateSpinner.onItemSelectedListener = selectionChangedListener
        categorySpinner.onItemSelectedListener = selectionChangedListener
        commentSpinner.onItemSelectedListener = selectionChangedListener
    }

    private fun updateExampleTransactionFromCSV(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let { stream ->
            val reader = InputStreamReader(stream)
            val csvReader = CSVReader(reader)
            var line: Array<String>?
            csvReader.readNext().also { line = it }
            csvReader.readNext().also { line = it }
            if (line != null) {
                sourceTxt.text = if (sourceSpinner.selectedItemPosition < line?.size!!) line?.get(sourceSpinner.selectedItemPosition) else ""
                targetTxt.text = if (targetSpinner.selectedItemPosition < line?.size!!) line?.get(targetSpinner.selectedItemPosition) else ""
                amountTxt.text = if (amountSpinner.selectedItemPosition < line?.size!!) line?.get(amountSpinner.selectedItemPosition) else ""
                currencyTxt.text = if (currencySpinner.selectedItemPosition < line?.size!!) line?.get(currencySpinner.selectedItemPosition) else ""
            }


//            while (csvReader.readNext().also { line = it } != null) {
//                val firstValue = line?.get(0) ?: continue
//                val parts = firstValue.split(';')
//
//                Log.d("CSV Value", firstValue)
//            }
        }
    }

    private fun setUpSpinnersFromCSV(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.let { stream ->
            val reader = InputStreamReader(stream)
            val csvReader = CSVReader(reader)
            var line: Array<String>?
            csvReader.readNext().also { line = it }
            if (line != null) {
                val columnNames = line?.toMutableList()
                columnNames?.add("* Not Present In Dataset *")
                if (columnNames!=null) {
                    sourceSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    targetSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    amountSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    currencySpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    dateSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    categorySpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                    commentSpinner.adapter = ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, columnNames)
                }
            }
            csvReader.close()
        }
    }

    private fun bindViews() {
        sourceSpinner = findViewById(R.id.imppar_source_spn)
        targetSpinner = findViewById(R.id.imppar_target_spn)
        amountSpinner = findViewById(R.id.imppar_amount_spn)
        currencySpinner = findViewById(R.id.imppar_currency_spn)
        dateSpinner = findViewById(R.id.imppar_date_spn)
        categorySpinner = findViewById(R.id.imppar_category_spn)
        commentSpinner = findViewById(R.id.imppar_comment_spn)
        sourceTxt = findViewById(R.id.tritem_sourceName_txt)
        targetTxt = findViewById(R.id.tritem_targetName_txt)
        amountTxt = findViewById(R.id.tritem_amount_txt)
        currencyTxt = findViewById(R.id.tritem_currencySymbol_txt)

    }
}