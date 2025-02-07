package com.colleagues.austrom

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.colleagues.austrom.adapters.AssetSquareRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.toDayMonthYearFormat
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.SelectorButtonView
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class ExportActivity : AppCompatActivity() {
    //region Binding
    private lateinit var assetHolderRecycler: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var exportToCsvButton: Button
    private lateinit var dateSelector: SelectorButtonView
    private fun bindViews() {
        assetHolderRecycler = findViewById(R.id.exprt_assetHolder_rcv)
        backButton = findViewById(R.id.exprt_backButton_btn)
        exportToCsvButton = findViewById(R.id.exprt_export_btn)
        dateSelector = findViewById(R.id.exprt_dateRange_selb)
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
    //endregion
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
    //endregion
    private var selectedAsset: Asset? = null
    private var selectedDates: Pair<LocalDate, LocalDate> = Pair(LocalDate.now().minusMonths(1), LocalDate.now())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_export)
        adjustInsets()
        bindViews()
        setUpRecyclerViews()
        setUpDateSelector()

        backButton.setOnClickListener { this.finish() }
        exportToCsvButton.setOnClickListener { if (selectedAsset!=null) { exportToCSVFile(selectedAsset!!) } else { Toast.makeText(this, "No Asset Selected", Toast.LENGTH_SHORT).show()} }
        dateSelector.setOnClickListener { launchDateSelectionDialog() }

    }

    private fun launchDateSelectionDialog() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Date Range")
            .build()
        dateRangePicker.show(this.supportFragmentManager, "DATE_PICKER")
        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = Instant.ofEpochMilli(selection.first)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val endDate = Instant.ofEpochMilli(selection.second)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            selectedDates = Pair(startDate, endDate)
            setUpDateSelector()
        }
    }

    private fun setUpDateSelector() {
        dateSelector.setFieldValue("${selectedDates.first.toDayMonthYearFormat()}-${selectedDates.second.toDayMonthYearFormat()}")
    }

    private fun setUpRecyclerViews() {
        assetHolderRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL,false)
        val adapterSource = AssetSquareRecyclerAdapter(AustromApplication.activeAssets.values.toMutableList(), this, selectedAsset)
        adapterSource.setOnItemClickListener { asset ->
            selectedAsset = asset
            setUpRecyclerViews()
        }
        assetHolderRecycler.adapter = adapterSource
    }



    private fun exportToCSVFile(exportedAsset: Asset) {
        val fos = openFileOutput("${exportedAsset.assetName}_${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}.csv", MODE_PRIVATE)
        val csvBuilder = StringBuilder()
        val dbProvider = LocalDatabaseProvider(this)
        val exportedTransactions = dbProvider.getTransactionsOfAsset(exportedAsset).filter { transaction ->
            transaction.transactionDate>selectedDates.first && transaction.transactionDate<selectedDates.second }
        
        csvBuilder.append("Transaction Description,Date,Amount,Currency,Base Currency Amount,Base Currency,Asset,Category,Holder,Comment\n")
        exportedTransactions.forEach { transaction ->
            csvBuilder.append(transaction.toCSVFormat()).append("\n")
        }
        try {
            fos.write(csvBuilder.toString().toByteArray(Charset.forName("windows-1251")))
            fos.close()
            val csvFile = File(filesDir, "${exportedAsset.assetName}_${LocalDate.now().format(DateTimeFormatter.ISO_DATE)}.csv")
            val csvUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", csvFile)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/csv")
            shareIntent.putExtra(Intent.EXTRA_STREAM, csvUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Share CSV File"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}