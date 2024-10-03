package com.colleagues.austrom

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.google.android.material.textfield.TextInputEditText
import java.time.format.DateTimeFormatter

class TransactionPropertiesActivity : AppCompatActivity() {
    private lateinit var transaction: Transaction
    private lateinit var backButton: ImageButton
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction_properties)
        adjustInsets()
        bindViews()
        retrieveTransactionFromIntent()
        setUpTransactionProperties()

        backButton.setOnClickListener {
            this.finish()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setUpTransactionProperties() {
        if (transaction.getTransactionType()!=TransactionType.TRANSFER) {
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
        when (transaction.getTransactionType()) {
            TransactionType.INCOME ->
            {
                sourceAmount.setTextColor(Color.rgb(0,100,0))
                sourceAmount.text = "+" + transaction.amount.toMoneyFormat()
                categoryImage.setImageResource((Category.defaultIncomeCategories
                    .find { it.name == transaction.categoryId })?.imgReference ?: R.drawable.placeholder_icon_background)
            }
            TransactionType.TRANSFER ->
            {
                sourceAmount.setTextColor(Color.RED)
                sourceAmount.text = "-" + transaction.amount.toMoneyFormat()
                targetAmount.setTextColor(Color.rgb(0,100,0))
                targetAmount.text = "+" + transaction.secondaryAmount?.toMoneyFormat()
                categoryImage.setImageResource((Category.defaultTransferCategories
                    .find { it.name == transaction.categoryId })?.imgReference ?: R.drawable.placeholder_icon_background)
            }
            TransactionType.EXPENSE ->
            {
                sourceAmount.setTextColor(Color.RED)
                sourceAmount.text = "-" + transaction.amount.toMoneyFormat()
                categoryImage.setImageResource((Category.defaultExpenseCategories
                    .find { it.name == transaction.categoryId })?.imgReference ?: R.drawable.placeholder_icon_background)
            }
        }
        //comment.text = transaction.comment.toString()
    }

    private fun retrieveTransactionFromIntent() {
        if (intent.getStringExtra("Transaction")!=null) {
            transaction = Transaction.parseFromString(intent.getStringExtra("Transaction")!!)
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
    }
}