package com.colleagues.austrom

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.models.Currency
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.shape.ShapeAppearanceModel

class AssetCreationActivity : AppCompatActivity() {
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
    private lateinit var assetTypesHolder: LinearLayout
    private lateinit var cardChip: Chip
    private lateinit var cashChip: Chip
    private lateinit var depositChip: Chip
    private lateinit var investmentChip: Chip
    private fun bindViews() {
        backButton = findViewById(R.id.ascreat_backButton_btn)
        addAssetButton = findViewById(R.id.ascreat_acceptButton_btn)
        currencyTextView = findViewById(R.id.ascreat_currencySymbol_txt)
        assetNameTextView = findViewById(R.id.ascreat_assetName_txt)
        assetNameTextLayout = findViewById(R.id.ascreat_assetName_til)
        currentBalanceTextView = findViewById(R.id.ascreat_currentAmount_txt)
        currentBalanceTextLayout = findViewById(R.id.ascreat_currentAmount_til)
        bankTextView = findViewById(R.id.ascreat_bank_txt)
        bankTextLayout = findViewById(R.id.ascreat_bank_til)
        percentNameTextView = findViewById(R.id.ascreat_percent_txt)
        percentNameTextLayout = findViewById(R.id.ascreat_percent_til)
        assetTypesHolder = findViewById(R.id.ascreat_assetTypesHolder_lly)
        cardChip = findViewById(R.id.ascreat_card_chp)
        cashChip = findViewById(R.id.ascreat_cash_chp)
        depositChip = findViewById(R.id.ascreat_deposit_chp)
        investmentChip = findViewById(R.id.ascreat_investment_chp)
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
    private var visibleAssetTypes: MutableList<AssetType> = mutableListOf()
    private var selectedCurrency: Currency? = null
    private var selectedAssetType: AssetType = AssetType.CARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_asset_creation)
        adjustInsets()
        bindViews()
        readDataFromIntent()

        selectedCurrency = AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode] ?: AustromApplication.activeCurrencies.values.toList()[0]


        switchFieldVisibilities(selectedAssetType)
        backButton.setOnClickListener { finish() }
        currencyTextView.setOnClickListener { launchCurrencyDialog() }
        addAssetButton.setOnClickListener { addAsset() }
        cardChip.setOnClickListener { switchFieldVisibilities(AssetType.CARD) }
        cashChip.setOnClickListener { switchFieldVisibilities(AssetType.CASH) }
        depositChip.setOnClickListener { switchFieldVisibilities(AssetType.DEPOSIT) }
        investmentChip.setOnClickListener { switchFieldVisibilities(AssetType.INVESTMENT) }

        AustromApplication.showKeyboard(this, assetNameTextView)
    }

    private fun createChipForAssetType(assetType: AssetType) {
        val chip = Chip(this, null, com.google.android.material.R.style.Widget_Material3_Chip_Suggestion_Elevated).apply {
            id = View.generateViewId()
            setChipBackgroundColorResource(if (assetType.isLiability) R.color.chip_expense_background_color else R.color.chip_income_background_color)
            setText(assetType.stringResourceId)
            isEnabled = true
            isCheckable = true
            isChecked = false
            chipIcon = null
            tag = assetType.ordinal
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(getColorStateList(R.color.chip_transaction_type_text_color))
            shapeAppearanceModel = ShapeAppearanceModel.builder().setAllCornerSizes(context.dpToPx(12)).build()
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(context.dpToPx(8).toInt(),0, context.dpToPx(8).toInt(),0)
            }
        }
        chip.setOnClickListener { switchFieldVisibilities(assetType) }
        assetTypesHolder.addView(chip)
    }

    private fun readDataFromIntent() {
        val presentedAssetTypes = intent.getIntegerArrayListExtra("listOfAvailableAssetType")
        if (presentedAssetTypes==null) { visibleAssetTypes=AssetType.entries.toMutableList() } else {
            visibleAssetTypes = mutableListOf()
            AssetType.entries.forEach { assetType ->
                if (presentedAssetTypes.contains(assetType.ordinal)) visibleAssetTypes.add(assetType)
            }
        }
        visibleAssetTypes.forEach { assetType -> createChipForAssetType(assetType)}

        var assetType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("AssetType", AssetType::class.java)
        } else {
            intent.getSerializableExtra("AssetType")
        }
        if (!visibleAssetTypes.contains(assetType)) {
            assetType=visibleAssetTypes[0]
        }
        when (assetType) {
            AssetType.CARD -> {switchFieldVisibilities(AssetType.CARD)}
            AssetType.CASH -> {switchFieldVisibilities(AssetType.CASH)}
            AssetType.DEPOSIT -> {switchFieldVisibilities(AssetType.DEPOSIT)}
            AssetType.INVESTMENT -> {switchFieldVisibilities(AssetType.INVESTMENT)}
            else -> {switchFieldVisibilities(AssetType.CARD)}
        }
    }

    private fun switchFieldVisibilities(assetType: AssetType) {
        selectedAssetType = assetType
        assetTypesHolder.children.forEach { child -> if (child is Chip) { child.isChecked = assetType.ordinal==child.tag; child.chipIcon=null }}
        cardChip.isChecked = assetType==AssetType.CARD
        cashChip.isChecked = assetType==AssetType.CASH
        depositChip.isChecked = assetType==AssetType.DEPOSIT
        investmentChip.isChecked = assetType==AssetType.INVESTMENT

        bankTextLayout.visibility = if (assetType!=AssetType.CASH) View.VISIBLE else View.GONE
        percentNameTextLayout.visibility = if (assetType==AssetType.DEPOSIT) View.VISIBLE else View.GONE
    }

    private fun addAsset() {
        if (isAssetValid()) {
            val dbProvider = LocalDatabaseProvider(this)
            val newAsset = Asset(
                assetName = assetNameTextView.text.toString(),
                assetTypeId = selectedAssetType,
                currencyCode = selectedCurrency!!.code,
                amount = currentBalanceTextView.text.toString().toDouble()
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