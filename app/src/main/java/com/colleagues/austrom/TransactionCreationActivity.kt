package com.colleagues.austrom

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.colleagues.austrom.adapters.AssetSquareRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CategorySelectionDialogFragment
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.models.Category
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
import kotlin.math.absoluteValue

class TransactionCreationActivity : AppCompatActivity() {
    //region Binding
    private lateinit var amountTxt: TextView
    private lateinit var currencyTxt: TextView
    private lateinit var transactionNameTil: TextInputLayout
    private lateinit var transactionNameTxt: TextView
    private lateinit var sourceHolderRecycler: RecyclerView
    private lateinit var targetHolderRecycler: RecyclerView
    private lateinit var sourceHolderLabel: TextView
    private lateinit var targetHolderLabel: TextView
    private lateinit var sourceHolder: CardView
    private lateinit var targetHolder: CardView
    private lateinit var incomeChip: Chip
    private lateinit var expenseChip: Chip
    private lateinit var transferChip: Chip
    private lateinit var backButton: ImageButton
    private lateinit var createTransactionButton: MaterialButton
    private lateinit var categorySelector: SelectorButtonView
    private lateinit var dateSelector: SelectorButtonView
    private fun bindViews() {
        sourceHolderRecycler = findViewById(R.id.trcreat_sourceHolder_rcv)
        targetHolderRecycler = findViewById(R.id.trcreat_targetHolder_rcv)
        sourceHolder = findViewById(R.id.trcreat_sourceHolder_crv)
        targetHolder = findViewById(R.id.trcreat_targetHolder_crv)
        sourceHolderLabel = findViewById(R.id.trcreat_sourceHolderLabel_txt)
        targetHolderLabel = findViewById(R.id.trcreat_targetHolderLabel_txt)
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
    //endregion
    //region Localizing
    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.attachBaseContext(newBase)
        } else  {
            super.attachBaseContext(AustromApplication.updateBaseContextLocale(newBase))
        }
    }//endregion
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
    private var transactionType: TransactionType = TransactionType.EXPENSE
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedCategory: Category = AustromApplication.activeExpenseCategories.values.first()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_creation)
        adjustInsets()
        bindViews()
        setUpRecyclerViews()

        backButton.setOnClickListener { finish() }

        incomeChip.setOnClickListener { switchChipSelection(incomeChip.id) }
        expenseChip.setOnClickListener { switchChipSelection(expenseChip.id) }
        transferChip.setOnClickListener { switchChipSelection(transferChip.id) }
        createTransactionButton.setStrokeColorResource((when (transactionType) {
            TransactionType.EXPENSE -> R.color.expenseRedBackground
            TransactionType.INCOME -> R.color.incomeGreenBackground
            TransactionType.TRANSFER -> R.color.transferYellowBackground
        }))
        sourceHolderLabel.text = (if (transactionType==TransactionType.INCOME) getString(R.string.toAsset) else getString(R.string.fromAsset)).startWithUppercase()
        targetHolderLabel.text = getString(R.string.toAsset).startWithUppercase()

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

        categorySelector.setFieldValue(selectedCategory.name)
        categorySelector.setOnClickListener { launchCategorySelectionDialog() }
        createTransactionButton.setOnClickListener { submitTransaction() }
    }

    private fun submitTransaction() {
        val dbProvider = LocalDatabaseProvider(this)
        if ((sourceHolderRecycler.adapter as AssetSquareRecyclerAdapter).selectedAsset==null) return
        val amount = amountTxt.text.toString().parseToDouble()?.absoluteValue
        if (amount==null || amount==0.0) {
            Toast.makeText(this, "Invalid transaction amount provided", Toast.LENGTH_LONG).show()
            return
        }
        Transaction(
            assetId = (sourceHolderRecycler.adapter as AssetSquareRecyclerAdapter).selectedAsset!!.assetId,
            amount = if (transactionType==TransactionType.INCOME) amount else -amount,
            categoryId = selectedCategory.categoryId,
            transactionDate = selectedDate,
            transactionName = if (transactionType==TransactionType.TRANSFER) (targetHolderRecycler.adapter as AssetSquareRecyclerAdapter).selectedAsset!!.assetName else transactionNameTxt.text.toString()
        ).submit(dbProvider)
        if (transactionType!=TransactionType.TRANSFER) return
        Transaction(
            assetId = (targetHolderRecycler.adapter as AssetSquareRecyclerAdapter).selectedAsset!!.assetId,
            amount = amount,
            categoryId = selectedCategory.categoryId,
            transactionDate = selectedDate,
            transactionName = (sourceHolderRecycler.adapter as AssetSquareRecyclerAdapter).selectedAsset!!.assetName
        )
    }

    private fun launchCategorySelectionDialog() {
        if (transactionType!=TransactionType.TRANSFER) {
            val categorySelectionDialog = CategorySelectionDialogFragment(transactionType)
            categorySelectionDialog.setOnDialogResultListener{ category ->
                categorySelector.setFieldValue(category.name)
            }
            categorySelectionDialog.show(supportFragmentManager, "CategorySelectionDialog")
        }
    }

    private fun switchChipSelection(chipId: Int) {
        transactionType = when (chipId) {
            R.id.trcreat_income_chp -> TransactionType.INCOME
            R.id.trcreat_expense_chp -> TransactionType.EXPENSE
            R.id.trcreat_transfer_chp -> TransactionType.TRANSFER
            else -> {TransactionType.EXPENSE}
        }

        incomeChip.isChecked = (chipId==R.id.trcreat_income_chp)
        expenseChip.isChecked = (chipId==R.id.trcreat_expense_chp)
        transferChip.isChecked = (chipId==R.id.trcreat_transfer_chp)

        transactionNameTil.hint = if (transactionType==TransactionType.EXPENSE) getString(R.string.toAsset).startWithUppercase() else getString(R.string.fromAsset).startWithUppercase()
        transactionNameTxt.visibility = if (transactionType==TransactionType.TRANSFER) View.GONE else View.VISIBLE
        targetHolder.visibility = if (transactionType==TransactionType.TRANSFER) View.VISIBLE else View.GONE
        sourceHolderLabel.text = (if (transactionType==TransactionType.INCOME) getString(R.string.toAsset) else getString(R.string.fromAsset)).startWithUppercase()
        targetHolderLabel.text = getString(R.string.toAsset).startWithUppercase()

        categorySelector.setFieldValue(when(transactionType) {
            TransactionType.EXPENSE -> AustromApplication.activeExpenseCategories.values.first().name
            TransactionType.INCOME -> AustromApplication.activeIncomeCategories.values.first().name
            TransactionType.TRANSFER -> AustromApplication.activeTransferCategories.values.first().name
        })

        createTransactionButton.setStrokeColorResource((when (transactionType) {
            TransactionType.EXPENSE -> R.color.expenseRedBackground
            TransactionType.INCOME -> R.color.incomeGreenBackground
            TransactionType.TRANSFER -> R.color.transferYellowBackground
        }))
    }

    private fun setUpRecyclerViews() {
        sourceHolderRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL,false)
        val adapterSource = AssetSquareRecyclerAdapter(AustromApplication.activeAssets.values.toMutableList(), this)
        adapterSource.setOnItemClickListener { asset ->  }
        sourceHolderRecycler.adapter = adapterSource

        targetHolderRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL, true)
        val adapterTarget = AssetSquareRecyclerAdapter(AustromApplication.activeAssets.values.toMutableList(), this)
        adapterTarget.setOnItemClickListener { asset -> }
        targetHolderRecycler.adapter = adapterTarget
    }
}