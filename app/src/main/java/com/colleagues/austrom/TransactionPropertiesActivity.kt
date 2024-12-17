package com.colleagues.austrom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.TransactionDetailRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.DeletionConfirmationDialogFragment
import com.colleagues.austrom.dialogs.ImageSelectionDialogFragment
import com.colleagues.austrom.dialogs.TransactionDetailCreationDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.InvalidTransactionException
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.TransactionValidationType
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

class TransactionPropertiesActivity : AppCompatActivity(), IDialogInitiator {
    private lateinit var transaction: Transaction
    private lateinit var backButton: ImageButton
    private lateinit var deleteButton: Button
    private lateinit var fromLayout: LinearLayout
    private lateinit var toLayout: LinearLayout
    private lateinit var sourceText: TextView
    private lateinit var targetText: TextView
    private lateinit var sourceAmount: TextView
    private lateinit var targetAmount: TextView
    private lateinit var sourceCurrency: TextView
    private lateinit var targetCurrency: TextView
    private lateinit var ownerText: TextView
    private lateinit var dateText: TextView
    private lateinit var categoryText: TextView
    private lateinit var categoryImage: ImageView
    private lateinit var comment: TextInputEditText
    private lateinit var transactionDetailsRecyclerView: RecyclerView
    private lateinit var unallocatedSum: TextView
    private lateinit var unallocatedCurrency: TextView
    private lateinit var detailConstructorHolder: FragmentContainerView
    private lateinit var detailsLabel: TextView
    private lateinit var addPhoto: ImageView
    private var transactionDetails = listOf<TransactionDetail>()

    override fun attachBaseContext(newBase: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            super.attachBaseContext(newBase)
        } else  {
            super.attachBaseContext(AustromApplication.updateBaseContextLocale(newBase))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_properties)
        adjustInsets()
        bindViews()
        retrieveSelectedTransaction()
        setUpTransactionProperties()
        setUpRecyclerView()
        setUpFragment()

        backButton.setOnClickListener {
            this.finish()
        }

        deleteButton.setOnClickListener {
            DeletionConfirmationDialogFragment(this).show(supportFragmentManager, "Delete Confirmation Dialog" )
        }


        addPhoto.setOnClickListener {
            ImageSelectionDialogFragment(transaction, this).show(supportFragmentManager, "ImageSelectionDialog")
        }

        comment.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                transaction.comment = comment.text.toString()
                val dbProvider  = LocalDatabaseProvider(this)
                dbProvider.updateTransaction(transaction)
            }
        }
    }

    fun updateUnallocatedSum(addedValue: Double = 0.0): Double {
        var sum = transaction.amount
        for (detail in transactionDetails) {
            sum -= detail.cost
        }
        if (BigDecimal(sum).setScale(2, RoundingMode.HALF_DOWN)==BigDecimal(0).setScale(2, RoundingMode.HALF_DOWN)) {
            detailConstructorHolder.visibility = View.GONE
            detailsLabel.text = getString(R.string.total)
            unallocatedSum.text = transaction.amount.toMoneyFormat()
        } else {
            detailConstructorHolder.visibility = View.VISIBLE
            detailsLabel.text = getString(R.string.unallocated_balance)
            sum -= addedValue
            unallocatedSum.text = sum.toMoneyFormat()
            unallocatedSum.setTextColor(if (sum>=0) Color.BLACK else Color.RED)
        }
        return sum
    }

    fun addTransactionDetail(transactionDetail: TransactionDetail) {
        val dbProvider = LocalDatabaseProvider(this)
        dbProvider.writeNewTransactionDetail(transactionDetail)
        transactionDetails = dbProvider.getTransactionDetailsOfTransaction(transaction)
        updateUnallocatedSum()
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        transactionDetailsRecyclerView.layoutManager = LinearLayoutManager(this)
        transactionDetailsRecyclerView.adapter = TransactionDetailRecyclerAdapter(transaction, transactionDetails)
    }

    private fun setUpFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.trdet_transactionDetailHolder_frt, TransactionDetailCreationDialogFragment(this, transaction))
        fragmentTransaction.commit()
    }

    private fun setUpAttachedImage() {
        val imageFile = File(externalCacheDir, "${transaction.transactionId}.jpg")
        if (imageFile.exists()) {
            val paddingDp = (resources.displayMetrics.density).toInt()
            addPhoto.setPadding(paddingDp,paddingDp,paddingDp,paddingDp)
            addPhoto.setImageURI(null)
            addPhoto.setImageURI(FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile))
            addPhoto.setBackgroundColor(Color.TRANSPARENT)
        } else {
            addPhoto.setBackgroundResource(R.drawable.sh_add_image_button_background)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpTransactionProperties() {
        val dbProvider = LocalDatabaseProvider(this)
        if (transaction.transactionType()!=TransactionType.TRANSFER) {
            toLayout.visibility = View.GONE
        }
        ownerText.text = AustromApplication.knownUsers[transaction.userId]?.username.startWithUppercase()
        dateText.text = transaction.transactionDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        setUpAttachedImage()
        when (transaction.transactionType()) {
            TransactionType.INCOME ->
            {
                sourceText.text = transaction.transactionName
                targetText.text = AustromApplication.activeAssets[transaction.assetId]?.assetName ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_ASSET_INVALID)
                sourceCurrency.text = AustromApplication.activeCurrencies[AustromApplication.activeAssets[transaction.assetId]?.currencyCode]?.symbol
                targetCurrency.text = AustromApplication.activeCurrencies[AustromApplication.activeAssets[transaction.assetId]?.currencyCode]?.symbol
                sourceAmount.setTextColor(Color.rgb(0,100,0))
                sourceAmount.text = "+" + transaction.amount.toMoneyFormat()
                categoryText.text = AustromApplication.activeIncomeCategories[transaction.categoryId]?.name ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_CATEGORY_INVALID)
                categoryImage.setImageResource((AustromApplication.activeIncomeCategories.values.find { it.name == transaction.categoryId })?.imgReference?.resourceId ?: R.drawable.placeholder_icon_background)
            }
            TransactionType.TRANSFER ->
            {
                //TODO("Fix Transfer Transactions")
//                sourceAmount.setTextColor(Color.RED)
//                sourceAmount.text = "-" + transaction.amount.toMoneyFormat()
//                targetAmount.setTextColor(Color.rgb(0,100,0))
//                targetAmount.text = "+" + transaction.secondaryAmount?.toMoneyFormat()
//                categoryImage.setImageResource((AustromApplication.activeTransferCategories.values.find { it.name == transaction.categoryId })?.imgReference?.resourceId ?: R.drawable.placeholder_icon_background)
            }
            TransactionType.EXPENSE ->
            {
                sourceText.text = AustromApplication.activeAssets[transaction.assetId]?.assetName ?: throw InvalidTransactionException("", TransactionValidationType.UNKNOWN_ASSET_INVALID)
                targetText.text = transaction.transactionName
                sourceCurrency.text = AustromApplication.activeCurrencies[AustromApplication.activeAssets[transaction.assetId]?.currencyCode]?.symbol
                targetCurrency.text = AustromApplication.activeCurrencies[AustromApplication.activeAssets[transaction.assetId]?.currencyCode]?.symbol
                sourceAmount.setTextColor(Color.RED)
                sourceAmount.text = "-" + transaction.amount.toMoneyFormat()
                categoryText.text = AustromApplication.activeIncomeCategories[transaction.categoryId]?.name ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_CATEGORY_INVALID)
                categoryImage.setImageResource((AustromApplication.activeExpenseCategories.values.find { it.name == transaction.categoryId })?.imgReference?.resourceId ?: R.drawable.placeholder_icon_background)
            }
        }
        val provider = LocalDatabaseProvider(this)
        transactionDetails = provider.getTransactionDetailsOfTransaction(transaction)
        updateUnallocatedSum(0.0)
        unallocatedCurrency.text = AustromApplication.activeCurrencies[AustromApplication.activeAssets[transaction.assetId]?.currencyCode]?.symbol
        if (transaction.comment!=null) {
            comment.setText(transaction.comment.toString())
        }
    }

    private fun retrieveSelectedTransaction() {
        if (AustromApplication.selectedTransaction!=null) {
            transaction = AustromApplication.selectedTransaction!!
        } else {
            finish()
        }
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

    private fun bindViews() {
        backButton = findViewById(R.id.trdet_back_btn)
        deleteButton = findViewById(R.id.trdet_delete_btn)
        fromLayout = findViewById(R.id.trdet_fromAmount_lly)
        toLayout = findViewById(R.id.trdet_toAmount_lly)
        sourceText = findViewById(R.id.trdet_source_txt)
        targetText = findViewById(R.id.trdet_target_txt)
        sourceAmount = findViewById(R.id.trdet_primaryAmount_txt)
        targetAmount = findViewById(R.id.trdet_secondaryAmount_txt)
        sourceCurrency = findViewById(R.id.trdet_primaryCurrency_txt)
        targetCurrency = findViewById(R.id.trdet_secondaryCurrency_txt)
        ownerText = findViewById(R.id.trdet_owner_txt)
        dateText = findViewById(R.id.trdet_date_txt)
        categoryText = findViewById(R.id.trdet_category_txt)
        categoryImage = findViewById(R.id.trdet_category_img)
        comment = findViewById(R.id.trdet_comments_txt)
        transactionDetailsRecyclerView = findViewById(R.id.trdet_transactionDetails_rcv)
        unallocatedSum = findViewById((R.id.trdet_unallocatedSum_txt))
        unallocatedCurrency = findViewById((R.id.trdet_unallocatedCurrency_txt))
        detailConstructorHolder = findViewById(R.id.trdet_transactionDetailHolder_frt)
        detailsLabel = findViewById(R.id.trdet_detLabel_txt)
        addPhoto = findViewById(R.id.trdet_addPhoto_btn)
    }

    override fun receiveValue(value: String, valueType: String) {
        if (valueType=="DialogResult" && value=="true") {
            transaction.cancel(LocalDatabaseProvider(this))
            this.finish()
        }
        if (valueType=="ImageUpdate" && value=="true") {
            setUpAttachedImage()
        }
    }
}