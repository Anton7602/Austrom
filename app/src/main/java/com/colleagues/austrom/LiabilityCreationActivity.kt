package com.colleagues.austrom

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.models.Currency
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LiabilityCreationActivity : AppCompatActivity() {
    //region Binding
    private lateinit var backButton: ImageButton
    private lateinit var addAssetButton: Button
    private lateinit var currencyTextView: TextView
    private lateinit var assetNameTextView: TextInputEditText
    private lateinit var assetNameTextLayout: TextInputLayout
    private lateinit var currentBalanceTextView: TextInputEditText
    private lateinit var currentBalanceTextLayout: TextInputLayout
    private lateinit var bankTextView: TextInputEditText
    private lateinit var bankTextLayout: TextInputLayout
    private lateinit var percentNameTextView: TextInputEditText
    private lateinit var percentNameTextLayout: TextInputLayout
    private lateinit var cardChip: Chip
    private lateinit var loanChip: Chip
    private lateinit var mortageChip: Chip
    private fun bindViews() {
        backButton = findViewById(R.id.liabcreat_backButton_btn)
        addAssetButton = findViewById(R.id.liabcreat_acceptButton_btn)
        currencyTextView = findViewById(R.id.liabcreat_currencySymbol_txt)
        assetNameTextView = findViewById(R.id.liabcreat_assetName_txt)
        assetNameTextLayout = findViewById(R.id.liabcreat_assetName_til)
        currentBalanceTextView = findViewById(R.id.liabcreat_currentAmount_txt)
        currentBalanceTextLayout = findViewById(R.id.liabcreat_currentAmount_til)
        bankTextView = findViewById(R.id.liabcreat_bank_txt)
        bankTextLayout = findViewById(R.id.liabcreat_bank_til)
        percentNameTextView = findViewById(R.id.liabcreat_percent_txt)
        percentNameTextLayout = findViewById(R.id.liabcreat_percent_til)
        cardChip = findViewById(R.id.liabcreat_card_chp)
        loanChip = findViewById(R.id.liabcreat_loan_chp)
        mortageChip = findViewById(R.id.liabcreat_mortage_chp)
    }
    //endregion
    //region Styling
    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val softKeyboardHeightInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            //val bottomInset = insets.systemWindowInsetBottom
            //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, if (softKeyboardHeightInset==0) systemBars.bottom else softKeyboardHeightInset)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }
    //endregion
    private var selectedCurrency: Currency? = null
    private var selectedAssetType: AssetType = AssetType.CARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liability_creation)
        adjustInsets()
        bindViews()
        readDataFromIntent()

        selectedCurrency = AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode] ?: AustromApplication.activeCurrencies.values.toList()[0]

        switchFieldVisibilities(selectedAssetType)
        backButton.setOnClickListener { finish() }
        currencyTextView.setOnClickListener { launchCurrencyDialog() }
        addAssetButton.setOnClickListener { addLiability() }
        cardChip.setOnClickListener { switchFieldVisibilities(AssetType.CREDIT_CARD) }
        loanChip.setOnClickListener { switchFieldVisibilities(AssetType.LOAN) }
        mortageChip.setOnClickListener { switchFieldVisibilities(AssetType.MORTAGE) }

        AustromApplication.showKeyboard(this, assetNameTextView)
    }

    private fun readDataFromIntent() {
        val assetType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("AssetType", AssetType::class.java)
        } else {
            intent.getSerializableExtra("AssetType")
        }
        when (assetType) {
            AssetType.CREDIT_CARD -> {switchFieldVisibilities(AssetType.CREDIT_CARD)}
            AssetType.LOAN -> {switchFieldVisibilities(AssetType.LOAN)}
            AssetType.MORTAGE -> {switchFieldVisibilities(AssetType.MORTAGE)}
            else -> {switchFieldVisibilities(AssetType.CREDIT_CARD)}
        }
    }

    private fun switchFieldVisibilities(assetType: AssetType) {
        selectedAssetType = assetType
        cardChip.isChecked = assetType==AssetType.CREDIT_CARD
        loanChip.isChecked = assetType==AssetType.LOAN
        mortageChip.isChecked = assetType==AssetType.MORTAGE

        bankTextLayout.visibility = if (assetType!=AssetType.CASH) View.VISIBLE else View.GONE
        percentNameTextLayout.visibility = if (assetType==AssetType.DEPOSIT) View.VISIBLE else View.GONE
    }

    private fun addLiability() {
        if (isAssetValid()) {
            val dbProvider = LocalDatabaseProvider(this)
            val newAsset = Asset(
                assetName = assetNameTextView.text.toString(),
                assetTypeId = selectedAssetType,
                currencyCode = selectedCurrency!!.code,
                amount = -currentBalanceTextView.text.toString().toDouble()
            )
            when (selectedAssetType) {
                AssetType.DEPOSIT -> newAsset.isPrivate //TODO("Update later")
                else -> {}
            }
            newAsset.add(dbProvider)
            this.finish()
        }
    }

    private fun isAssetValid(): Boolean {
        return true
    }

    private fun launchCurrencyDialog() {
        AustromApplication.hideKeyboard(this)
        val dialog = CurrencySelectionDialogFragment(AustromApplication.activeCurrencies[currencyTextView.text.toString()])
        dialog.setOnDialogResultListener { currency ->
            selectedCurrency = currency
            currencyTextView.text = currency.code
        }
        dialog.show(supportFragmentManager, "Currency Selection Dialog")
    }
}