package com.colleagues.austrom

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.LinearLayout
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
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CategorySelectionDialogFragment
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.SelectorButtonView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
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
    private lateinit var transactionNameTxt: AutoCompleteTextView
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
    private lateinit var secondaryAmountHolder: LinearLayout
    private lateinit var secondaryAmountTxt: TextInputEditText
    private lateinit var secondaryCurrencyTxt: TextView
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
        secondaryAmountHolder = findViewById(R.id.trcreat_secondaryAmountHolder_lly)
        secondaryAmountTxt = findViewById(R.id.trcreat_amount_secondary_txt)
        secondaryCurrencyTxt = findViewById(R.id.trcreat_currencySymbol_secondary_txt)
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
    private var primarySelectedAsset: Asset? = null
    private var secondarySelectedAsset: Asset? = null
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
        setUpAutofill()
        readFromIntent()

        if (AustromApplication.activeAssets.size<2) { transferChip.visibility = View.GONE }

        backButton.setOnClickListener { finish() }
        incomeChip.setOnClickListener { switchChipSelection(incomeChip.id) }
        expenseChip.setOnClickListener { switchChipSelection(expenseChip.id) }
        transferChip.setOnClickListener { switchChipSelection(transferChip.id) }
        createTransactionButton.setStrokeColorResource((when (transactionType) {
            TransactionType.EXPENSE -> R.color.expenseRedBackground
            TransactionType.INCOME -> R.color.incomeGreenBackground
            TransactionType.TRANSFER -> R.color.transferYellowBackground
        }))
        sourceHolderLabel.text = (if (transactionType==TransactionType.INCOME) getString(R.string.payee) else getString(R.string.payer)).startWithUppercase()
        targetHolderLabel.text = getString(R.string.payee).startWithUppercase()

        dateSelector.setFieldValue(selectedDate.toDayOfWeekAndShortDateFormat())
        dateSelector.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.choose_transaction_date)).setSelection(
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

    private fun isInputValid(): Boolean {
        if (primarySelectedAsset==null) { Toast.makeText(this, getString(R.string.asset_isn_t_selected), Toast.LENGTH_LONG).show(); return false }
        if (transactionType==TransactionType.TRANSFER && secondarySelectedAsset==null) { Toast.makeText(this, getString(R.string.receiver_asset_isn_t_selected), Toast.LENGTH_LONG).show(); return false }
        val amount = amountTxt.text.toString().parseToDouble()?.absoluteValue
        if (amount==null || amount>Double.MAX_VALUE) {  amountTxt.error = getString(R.string.invalid_transaction_amount_provided); return false}
        if (amount == 0.0) {  amountTxt.error = getString(R.string.transaction_amount_cannot_be_zero); return false}
        if (transactionType==TransactionType.EXPENSE && amount>primarySelectedAsset!!.amount) { amountTxt.error = getString(R.string.transaction_amount_is_greater_than_assets_balance); return false }
        val name = transactionNameTxt.text.toString()
        if (name.isEmpty() && transactionType!=TransactionType.TRANSFER) { transactionNameTxt.error = getString(R.string.name_cannot_be_empty); return false }
        if (name.length>40) { transactionNameTxt.error = getString(R.string.transaction_name_is_too_long); return false }
        return true
    }

    private fun submitTransaction() {
        val dbProvider = LocalDatabaseProvider(this)
        if (!isInputValid()) return
        val amount = amountTxt.text.toString().parseToDouble()!!.absoluteValue
        val primaryTransaction = Transaction(
            assetId = primarySelectedAsset!!.assetId,
            amount = if (transactionType==TransactionType.INCOME) amount else -amount,
            categoryId = selectedCategory.categoryId,
            transactionDate = selectedDate,
            transactionName = if (transactionType==TransactionType.TRANSFER) secondarySelectedAsset!!.assetName else transactionNameTxt.text.toString()
        )
        if (transactionType==TransactionType.TRANSFER) {
            val secondaryTransaction =  Transaction(
                assetId = secondarySelectedAsset!!.assetId,
                amount = if (secondaryAmountHolder.visibility==View.VISIBLE) secondaryAmountTxt.text.toString().parseToDouble()?.absoluteValue ?: amount else amount,
                categoryId = selectedCategory.categoryId,
                transactionDate = selectedDate,
                transactionName = primarySelectedAsset!!.assetName
            )
            secondaryTransaction.linkTo(primaryTransaction)
            secondaryTransaction.submit(dbProvider, FirebaseDatabaseProvider(this))
        }
        primaryTransaction.submit(dbProvider, FirebaseDatabaseProvider(this))
        this.finish()
    }

    private fun readFromIntent() {
        val intentString = intent.getStringExtra("TransactionType")
        when (intentString) {
            "INCOME" -> switchChipSelection(R.id.trcreat_income_chp)
            "EXPENSE" -> switchChipSelection(R.id.trcreat_expense_chp)
            "TRANSFER" -> switchChipSelection(R.id.trcreat_transfer_chp)
            else -> switchChipSelection(R.id.trcreat_expense_chp)
        }
    }

    private fun launchCategorySelectionDialog() {
        if (transactionType!=TransactionType.TRANSFER) {
            val categorySelectionDialog = CategorySelectionDialogFragment(transactionType)
            categorySelectionDialog.setOnDialogResultListener{ category ->
                categorySelector.setFieldValue(category.name)
                selectedCategory = category
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

        transactionNameTil.hint = if (transactionType==TransactionType.EXPENSE) getString(R.string.payee).startWithUppercase() else getString(R.string.payer).startWithUppercase()
        sourceHolderLabel.text = (if (transactionType==TransactionType.INCOME) getString(R.string.payee).startWithUppercase() else getString(R.string.payer)).startWithUppercase()
        targetHolderLabel.text = getString(R.string.payee).startWithUppercase()

        transactionNameTxt.visibility = if (transactionType!=TransactionType.TRANSFER) View.VISIBLE else View.GONE
        categorySelector.visibility = if (transactionType!=TransactionType.TRANSFER) View.VISIBLE else View.GONE
        targetHolder.visibility = if (transactionType==TransactionType.TRANSFER) View.VISIBLE else View.GONE
        categorySelector.setFieldValue(when(transactionType) {
            TransactionType.EXPENSE -> AustromApplication.activeExpenseCategories.values.first().name
            TransactionType.INCOME -> AustromApplication.activeIncomeCategories.values.first().name
            TransactionType.TRANSFER -> AustromApplication.activeTransferCategories.values.first().name
        })
        selectedCategory = when(transactionType) {
            TransactionType.EXPENSE -> AustromApplication.activeExpenseCategories.values.first()
            TransactionType.INCOME -> AustromApplication.activeIncomeCategories.values.first()
            TransactionType.TRANSFER -> AustromApplication.activeTransferCategories.values.first()
        }

        createTransactionButton.setStrokeColorResource((when (transactionType) {
            TransactionType.EXPENSE -> R.color.expenseRedBackground
            TransactionType.INCOME -> R.color.incomeGreenBackground
            TransactionType.TRANSFER -> R.color.transferYellowBackground
        }))

        secondaryAmountHolder.visibility = if (transactionType==TransactionType.TRANSFER && primarySelectedAsset?.currencyCode!=secondarySelectedAsset?.currencyCode) View.VISIBLE else View.GONE
        setUpAutofill()
    }

    private fun setUpRecyclerViews() {
        if (primarySelectedAsset==null) primarySelectedAsset = AustromApplication.activeAssets[AustromApplication.appUser!!.primaryPaymentMethod] ?: AustromApplication.activeAssets.values.toList()[0]
        sourceHolderRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL,false)
        val adapterSource = AssetSquareRecyclerAdapter(AustromApplication.activeAssets.values.toMutableList(), this, primarySelectedAsset)
        adapterSource.setOnItemClickListener { asset ->
            primarySelectedAsset = asset
            setUpRecyclerViews()
        }
        currencyTxt.text = primarySelectedAsset?.currencyCode
        sourceHolderRecycler.adapter = adapterSource
        val assetList = AustromApplication.activeAssets.values.toMutableList()
        assetList.remove(primarySelectedAsset)
        if (transactionType==TransactionType.TRANSFER && (secondarySelectedAsset==null || secondarySelectedAsset?.assetId==primarySelectedAsset?.assetId)) secondarySelectedAsset = assetList[0]
        targetHolderRecycler.layoutManager = LinearLayoutManager(this, HORIZONTAL, true) // TODO("What if not enough assets for transfer?")
        val adapterTarget = AssetSquareRecyclerAdapter(assetList, this, secondarySelectedAsset)
        adapterTarget.setOnItemClickListener { asset ->
            secondarySelectedAsset = asset
            setUpRecyclerViews()
        }
        secondaryCurrencyTxt.text = secondarySelectedAsset?.currencyCode
        targetHolderRecycler.adapter = adapterTarget

        secondaryAmountHolder.visibility = if (transactionType==TransactionType.TRANSFER && primarySelectedAsset?.currencyCode!=secondarySelectedAsset?.currencyCode) View.VISIBLE else View.GONE
    }

    private fun setUpAutofill() {
        val localDBProvider = LocalDatabaseProvider(this)
        localDBProvider.getUniqueTransactionNamesAsync(transactionType).observe(this) { names ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, names)
            transactionNameTxt.setAdapter(adapter)
            transactionNameTxt.threshold = 2

            transactionNameTxt.setOnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                val categoryId = localDBProvider.getTransactionNameMostUsedCategory(selectedItem)
                when (transactionType) {
                    TransactionType.INCOME -> {
                        if (AustromApplication.activeIncomeCategories.containsKey(categoryId)) {
                            selectedCategory = AustromApplication.activeIncomeCategories[categoryId]!!
                            categorySelector.setFieldValue(selectedCategory.name)
                        }
                    }
                    TransactionType.EXPENSE -> {
                        if (AustromApplication.activeExpenseCategories.containsKey(categoryId)) {
                            selectedCategory = AustromApplication.activeExpenseCategories[categoryId]!!
                            categorySelector.setFieldValue(selectedCategory.name)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}