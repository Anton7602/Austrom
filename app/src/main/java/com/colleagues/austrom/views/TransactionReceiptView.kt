package com.colleagues.austrom.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.TransactionDetailRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toDayOfWeekAndLongDateFormat
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType
import kotlin.math.absoluteValue

class TransactionReceiptView(context: Context, attrs: AttributeSet): CardView(context, attrs) {
    //region Binding
    private lateinit var sourceNameTextView: TextView
    private lateinit var targetNameTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var ownerTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var commentTextView: TextView
    private lateinit var transactionDetailsRecyclerView: RecyclerView
    private lateinit var headerLayout: LinearLayout
    private lateinit var bodyLayout: LinearLayout
    private lateinit var footerLayout: LinearLayout
    private lateinit var totalAmountTextView: MoneyFormatTextView
    private fun bindViews(view: View) {
        sourceNameTextView = view.findViewById(R.id.tranrecview_sourceName_txt)
        targetNameTextView = view.findViewById(R.id.tranrecview_targetName_txt)
        dateTextView = view.findViewById(R.id.tranrecview_date_txt)
        ownerTextView = view.findViewById(R.id.tranrecview_transactionOwner_txt)
        categoryTextView = view.findViewById(R.id.tranrecview_transactionCategory_txt)
        commentTextView = view.findViewById(R.id.tranrecview_comment_txt)
        transactionDetailsRecyclerView = view.findViewById(R.id.tranrecview_transactionDetails_rcv)
        totalAmountTextView = view.findViewById(R.id.tranrecview_totalAmount_monv)
        headerLayout = view.findViewById(R.id.tranrecview_header_lly)
        bodyLayout = view.findViewById(R.id.tranrecview_body_lly)
        footerLayout = view.findViewById(R.id.tranrecview_footer_lly)

    }
    //endregion
    private var transaction: Transaction? = null
    private var transactionDetails = mutableListOf<TransactionDetail>()


    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_transaction_receipt, this, true)
        bindViews(view)
    }

    fun fillInTransaction(newTransaction: Transaction) {
        transaction = newTransaction
        val dbProvider = LocalDatabaseProvider(context)
        transactionDetails = dbProvider.getTransactionDetailsOfTransaction(newTransaction).toMutableList()
        sourceNameTextView.text = if (newTransaction.transactionType()==TransactionType.INCOME) newTransaction.transactionName else  AustromApplication.activeAssets[newTransaction.assetId]?.assetName
        targetNameTextView.text = if (newTransaction.transactionType() == TransactionType.INCOME) AustromApplication.activeAssets[newTransaction.assetId]?.assetName else newTransaction.transactionName
        dateTextView.text = newTransaction.transactionDate.toDayOfWeekAndLongDateFormat()
        ownerTextView.text = AustromApplication.knownUsers[newTransaction.userId]?.username.startWithUppercase()
        categoryTextView.text = if (newTransaction.transactionType() == TransactionType.INCOME) AustromApplication.activeIncomeCategories[newTransaction.categoryId]?.name
        else if (newTransaction.transactionType() == TransactionType.EXPENSE) AustromApplication.activeExpenseCategories[newTransaction.categoryId]?.name
        else AustromApplication.activeTransferCategories.values.toList()[0].name
        commentTextView.visibility = if (newTransaction.comment.isNullOrEmpty()) View.GONE else View.VISIBLE
        commentTextView.text = newTransaction.comment

        bodyLayout.visibility = if (transactionDetails.isEmpty()) View.GONE else View.VISIBLE
        if (transactionDetails.isNotEmpty()) {
            transactionDetailsRecyclerView.layoutManager = LinearLayoutManager(context)
            transactionDetailsRecyclerView.adapter = TransactionDetailRecyclerAdapter(newTransaction, transactionDetails)
        }

        totalAmountTextView.setValue(newTransaction.amount.absoluteValue, AustromApplication.activeAssets[newTransaction.assetId]!!.currencyCode)
    }
}