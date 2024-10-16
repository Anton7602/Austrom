package com.colleagues.austrom

import android.annotation.SuppressLint
import android.graphics.Color
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
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.adapters.TransactionDetailRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.IDatabaseProvider
import com.colleagues.austrom.dialogs.DeletionConfirmationDialogFragment
import com.colleagues.austrom.dialogs.ImageSelectionDialogFragment
import com.colleagues.austrom.dialogs.TransactionDetailCreationDialogFragment
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType
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
    private lateinit var transactionDetails: RecyclerView
    private lateinit var unallocatedSum: TextView
    private lateinit var unallocatedCurrency: TextView
    private lateinit var detailConstructorHolder: FragmentContainerView
    private lateinit var detailsLabel: TextView
    private lateinit var addPhoto: ImageView

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
                val dbProvider : IDatabaseProvider = FirebaseDatabaseProvider(this)
                dbProvider.updateTransaction(transaction)
            }
        }
    }

    fun updateUnallocatedSum(addedValue: Double) {
        var sum = transaction.amount
        for (detail in transaction.details) {
            sum -= detail.cost!!
        }
        if (BigDecimal(sum).setScale(2, RoundingMode.HALF_DOWN)==BigDecimal(0).setScale(2, RoundingMode.HALF_DOWN)) {
            detailConstructorHolder.visibility = View.GONE
            detailsLabel.text = getString(R.string.total)
            unallocatedSum.text = transaction.amount.toMoneyFormat()
        } else {
            detailConstructorHolder.visibility = View.VISIBLE
            detailsLabel.text = getString(R.string.unallocated_balance)
            unallocatedSum.text = sum.toMoneyFormat()
        }
    }

    fun addTransactionDetail(transactionDetail: TransactionDetail) {
        val dbProvider: IDatabaseProvider = FirebaseDatabaseProvider(this)
        transaction.details.add(0, transactionDetail)
        dbProvider.updateTransaction(transaction)
        updateUnallocatedSum(0.0)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        transactionDetails.layoutManager = LinearLayoutManager(this)
        transactionDetails.adapter = TransactionDetailRecyclerAdapter(transaction)
    }

    private fun setUpFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.trdet_transactionDetailHolder_frt, TransactionDetailCreationDialogFragment(this, transaction))
        fragmentTransaction.commit()
    }

    private fun setUpAttachedImage() {
        val imageFile = File(externalCacheDir, "${transaction.transactionId}.jpg")
        if (imageFile.exists()) {
            addPhoto.setImageURI(FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpTransactionProperties() {
        if (transaction.transactionType()!=TransactionType.TRANSFER) {
            toLayout.visibility = View.GONE
        }
        val source = AustromApplication.activeAssets[transaction.sourceId]
        val target = AustromApplication.activeAssets[transaction.targetId]
        sourceText.text = transaction.sourceName
        targetText.text = transaction.targetName
        sourceCurrency.text = AustromApplication.activeCurrencies[source?.currencyCode]?.symbol
        targetCurrency.text = AustromApplication.activeCurrencies[target?.currencyCode]?.symbol
        ownerText.text = AustromApplication.knownUsers[transaction.userId]?.username.startWithUppercase()
        dateText.text = transaction.transactionDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        categoryText.text = transaction.categoryId
        setUpAttachedImage()
        when (transaction.transactionType()) {
            TransactionType.INCOME ->
            {
                sourceAmount.setTextColor(Color.rgb(0,100,0))
                sourceAmount.text = "+" + transaction.amount.toMoneyFormat()
                categoryImage.setImageResource((AustromApplication.getActiveIncomeCategories()
                    .find { it.name == transaction.categoryId })?.imgReference?.resourceId ?: R.drawable.placeholder_icon_background)
            }
            TransactionType.TRANSFER ->
            {
                sourceAmount.setTextColor(Color.RED)
                sourceAmount.text = "-" + transaction.amount.toMoneyFormat()
                targetAmount.setTextColor(Color.rgb(0,100,0))
                targetAmount.text = "+" + transaction.secondaryAmount?.toMoneyFormat()
                categoryImage.setImageResource((AustromApplication.getActiveTransferCategories()
                    .find { it.name == transaction.categoryId })?.imgReference?.resourceId ?: R.drawable.placeholder_icon_background)
            }
            TransactionType.EXPENSE ->
            {
                sourceAmount.setTextColor(Color.RED)
                sourceAmount.text = "-" + transaction.amount.toMoneyFormat()
                categoryImage.setImageResource((AustromApplication.getActiveExpenseCategories()
                    .find { it.name == transaction.categoryId })?.imgReference?.resourceId ?: R.drawable.placeholder_icon_background)
            }
        }
        updateUnallocatedSum(0.0)
        unallocatedCurrency.text = AustromApplication.activeCurrencies[source?.currencyCode]?.symbol
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
        transactionDetails = findViewById(R.id.trdet_transactionDetails_rcv)
        unallocatedSum = findViewById((R.id.trdet_unallocatedSum_txt))
        unallocatedCurrency = findViewById((R.id.trdet_unallocatedCurrency_txt))
        detailConstructorHolder = findViewById(R.id.trdet_transactionDetailHolder_frt)
        detailsLabel = findViewById(R.id.trdet_detLabel_txt)
        addPhoto = findViewById(R.id.trdet_addPhoto_btn)
    }

    override fun receiveValue(value: String, valueType: String) {
        if (valueType=="DialogResult" && value=="true") {
            transaction.cancel(FirebaseDatabaseProvider(this))
            this.finish()
        }
        if (valueType=="ImageUpdate" && value=="true") {
            setUpAttachedImage()
        }
    }
}