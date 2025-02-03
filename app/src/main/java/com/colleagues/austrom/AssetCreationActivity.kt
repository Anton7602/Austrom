package com.colleagues.austrom

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.CurrencySelectionDialogFragment
import com.colleagues.austrom.extensions.dpToPx
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType
import com.colleagues.austrom.models.Currency
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.absoluteValue

class AssetCreationActivity : AppCompatActivity() {
    //region Binding
    private lateinit var backButton: ImageButton
    private lateinit var addAssetButton: MaterialButton
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
    private lateinit var headerText: TextView
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
        headerText = findViewById(R.id.ascreat_header_txt)
    }
    //endregion
    //region Styling
    private fun adjustInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val softKeyboardHeightInset = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, if (softKeyboardHeightInset==0) systemBars.bottom else softKeyboardHeightInset)
            insets
        }
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars=AustromApplication.isApplicationThemeLight
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars=AustromApplication.isApplicationThemeLight
    }
    //endregion
    private var assetNameTextListener: TextWatcher? = null
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
            isCheckedIconVisible = false
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
        val presentedAssetTypes = intent.getIntegerArrayListExtra("ListOfAvailableAssetTypes")
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
        switchFieldVisibilities(assetType as AssetType)
    }

    private fun switchFieldVisibilities(assetType: AssetType) {
        selectedAssetType = assetType
        assetTypesHolder.children.forEach { child -> if (child is Chip) { child.isChecked = assetType.ordinal==child.tag; child.chipIcon=null }}
        bankTextLayout.visibility = if (assetType!=AssetType.CASH) View.VISIBLE else View.GONE
        percentNameTextLayout.visibility = if (assetType==AssetType.DEPOSIT) View.VISIBLE else View.GONE
        switchAssetAndLiabilityMode(assetType.isLiability)

    }

    private fun switchAssetAndLiabilityMode(isLiability: Boolean) {
        headerText.text = if (isLiability) getString(R.string.new_liability) else getString(R.string.new_asset)
        assetNameTextLayout.setHint(if (isLiability) R.string.liability_name else R.string.asset_name)
        addAssetButton.setText(if (isLiability) R.string.add_liability else R.string.add_asset)
        addAssetButton.setStrokeColorResource(if (isLiability) R.color.expenseRedBackground else R.color.incomeGreenBackground)
    }

    private fun addAsset() {
        if (isAssetValid()) {
            val amount = currentBalanceTextView.text.toString().parseToDouble() ?: return

            val newAsset = Asset(
                assetName = assetNameTextView.text.toString(),
                assetTypeId = selectedAssetType,
                currencyCode = selectedCurrency!!.code,
                amount = if (selectedAssetType.isLiability) -amount.absoluteValue else amount.absoluteValue
            )
            when (selectedAssetType) {
                AssetType.DEPOSIT -> newAsset.isPrivate //TODO("Update later")
                else -> {}
            }
            newAsset.add(LocalDatabaseProvider(this), FirebaseDatabaseProvider(this))
            this.finish()
        }
    }

    private fun isAssetValid(): Boolean {
        if (!isAssetNameValid()) return false
        if (!isAssetAmountValid()) return false
        return true
    }

    private fun isAssetNameValid(): Boolean {
        if (assetNameTextListener==null)
            assetNameTextListener = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {isAssetNameValid()}
                override fun afterTextChanged(s: Editable?) {}
            }
        val assetName = assetNameTextView.text.toString()
        if (assetName.isEmpty()) {
            assetNameTextLayout.error = getString(R.string.asset_s_title_cannot_be_empty)
            assetNameTextView.addTextChangedListener(assetNameTextListener)
            return false
        } else {
            assetNameTextLayout.error = null
            assetNameTextView.removeTextChangedListener(assetNameTextListener)
            return true
        }
    }

    private fun isAssetAmountValid(): Boolean {
        val amount = currentBalanceTextView.text.toString().parseToDouble()
        if (amount == null) {
            currentBalanceTextLayout.error = "Amount Value is invalid"

            return false
        }
        else {
            currentBalanceTextLayout.error = null
            return true
        }
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