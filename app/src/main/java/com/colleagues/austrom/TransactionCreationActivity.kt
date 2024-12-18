package com.colleagues.austrom

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.colleagues.austrom.adapters.AssetSquareRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.SelectorButtonView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TransactionCreationActivity : AppCompatActivity(), IDialogInitiator {
    private lateinit var amountTxt: TextView
    private lateinit var currencyTxt: TextView
    private lateinit var transactionNameTil: TextInputLayout
    private lateinit var transactionNameTxt: TextView
    private lateinit var sourceHolderRecycler: RecyclerView
    private lateinit var targetHolderRecycler: RecyclerView
    private lateinit var sourceHolder: CardView
    private lateinit var targetHolder: CardView
    private lateinit var incomeChip: Chip
    private lateinit var expenseChip: Chip
    private lateinit var transferChip: Chip
    private lateinit var backButton: ImageButton
    private lateinit var createTransactionButton: MaterialButton
    private lateinit var categorySelector: SelectorButtonView
    private lateinit var dateSelector: SelectorButtonView

    private var transactionType: TransactionType = TransactionType.EXPENSE
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_creation)
        adjustInsets()
        bindViews()
        setUpRecyclerViews()

        backButton.setOnClickListener {
            finish()
        }

        incomeChip.setOnClickListener { switchChipSelection(incomeChip.id) }
        expenseChip.setOnClickListener { switchChipSelection(expenseChip.id) }
        transferChip.setOnClickListener { switchChipSelection(transferChip.id) }
        createTransactionButton.setStrokeColorResource((when (transactionType) {
            TransactionType.EXPENSE -> R.color.expenseRedBackground
            TransactionType.INCOME -> R.color.incomeGreenBackground
            TransactionType.TRANSFER -> R.color.transferYellowBackground
        }))

        dateSelector.setFieldValue(selectedDate.toDayOfWeekAndShortDateFormat())
        dateSelector.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Choose Transaction Date").setSelection(
                MaterialDatePicker.todayInUtcMilliseconds()).build()
            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDate = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
                dateSelector.setFieldValue(selectedDate.toDayOfWeekAndShortDateFormat())
            }
            datePicker.show(this.supportFragmentManager, "DatePicker Dialog")
        }
    }

    private fun switchChipSelection(chipId: Int) {
        incomeChip.isChecked = (chipId==R.id.trcreat_income_chp)
        expenseChip.isChecked = (chipId==R.id.trcreat_expense_chp)
        transferChip.isChecked = (chipId==R.id.trcreat_transfer_chp)

        transactionNameTil.hint = if (chipId==R.id.trcreat_expense_chp) getString(R.string.toAsset).startWithUppercase() else getString(R.string.fromAsset).startWithUppercase()
        transactionNameTxt.visibility = if (chipId==R.id.trcreat_transfer_chp) View.GONE else View.VISIBLE
        targetHolder.visibility = if (chipId==R.id.trcreat_transfer_chp) View.VISIBLE else View.GONE

        transactionType = when (chipId) {
            R.id.trcreat_income_chp -> TransactionType.INCOME
            R.id.trcreat_expense_chp -> TransactionType.EXPENSE
            R.id.trcreat_transfer_chp -> TransactionType.TRANSFER
            else -> {TransactionType.EXPENSE}
        }
        createTransactionButton.setStrokeColorResource((when (transactionType) {
            TransactionType.EXPENSE -> R.color.expenseRedBackground
            TransactionType.INCOME -> R.color.incomeGreenBackground
            TransactionType.TRANSFER -> R.color.transferYellowBackground
        }))
        createTransactionButton.setOnClickListener {
            val dbProvider = LocalDatabaseProvider(this)
            if ((sourceHolderRecycler.adapter as AssetSquareRecyclerAdapter).selectedAsset==null) return@setOnClickListener
            Transaction(
                assetId = (sourceHolderRecycler.adapter as AssetSquareRecyclerAdapter).selectedAsset!!.assetId,
                amount = amountTxt.text.toString().parseToDouble() ?: 0.0,
                categoryId = AustromApplication.activeExpenseCategories.values.first().categoryId, //TODO("CHANGE!!!")
                transactionDate = selectedDate,
                transactionName = transactionNameTxt.text.toString()
            ).submit(dbProvider)
            if (transactionType!=TransactionType.TRANSFER) return@setOnClickListener

        }
    }

    private fun setUpRecyclerViews() {
        sourceHolderRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL,false)
        sourceHolderRecycler.adapter = AssetSquareRecyclerAdapter(AustromApplication.activeAssets.values.toMutableList(), this, this)
        targetHolderRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL, true)
        targetHolderRecycler.adapter = AssetSquareRecyclerAdapter(AustromApplication.activeAssets.values.toMutableList(), this, this)
    }

    private fun bindViews() {
        sourceHolderRecycler = findViewById(R.id.trcreat_sourceHolder_rcv)
        targetHolderRecycler = findViewById(R.id.trcreat_targetHolder_rcv)
        sourceHolder = findViewById(R.id.trcreat_sourceHolder_crv)
        targetHolder = findViewById(R.id.trcreat_targetHolder_crv)
        incomeChip = findViewById(R.id.trcreat_income_chp)
        expenseChip = findViewById(R.id.trcreat_expense_chp)
        transferChip = findViewById(R.id.trcreat_transfer_chp)
        backButton = findViewById(R.id.trcreat_backButton_btn)
        transactionNameTil = findViewById(R.id.trcreat_transactionName_til)
        transactionNameTxt = findViewById(R.id.trcreat_transactionName_txt)
        amountTxt = findViewById(R.id.trcreat_amount_txt)
        currencyTxt = findViewById(R.id.trcreat_currencySymbol_txt)
        createTransactionButton = findViewById(R.id.trcreat_acceptButton_btn)
        categorySelector = findViewById(R.id.trcreat_categorySelector_sbv)
        dateSelector = findViewById(R.id.trcreat_dateSelector_sbv)
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

    override fun receiveValue(value: String, valueType: String) {
        TODO("Not yet implemented")
    }
}