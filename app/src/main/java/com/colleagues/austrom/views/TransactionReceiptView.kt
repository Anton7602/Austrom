package com.colleagues.austrom.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
    private lateinit var editedDetailLayout: LinearLayout
    private lateinit var editedDetailName: TextView
    private lateinit var editedDetailAmount: TextView
    private lateinit var editedDetailUnit: TextView
    private lateinit var editedDetailCost: MoneyFormatTextView
    private fun bindViews(view: View) {
        sourceNameTextView = view.findViewById(R.id.tranrecview_sourceName_txt)
        targetNameTextView = view.findViewById(R.id.tranrecview_targetName_txt)
        dateTextView = view.findViewById(R.id.tranrecview_date_txt)
        ownerTextView = view.findViewById(R.id.tranrecview_transactionOwner_txt)
        categoryTextView = view.findViewById(R.id.tranrecview_transactionCategory_txt)
        commentTextView = view.findViewById(R.id.tranrecview_comment_txt)
        transactionDetailsRecyclerView = view.findViewById(R.id.tranrecview_transactionDetails_rcv)
        totalAmountTextView = view.findViewById(R.id.tranrecview_totalAmount_monf)
        headerLayout = view.findViewById(R.id.tranrecview_header_lly)
        bodyLayout = view.findViewById(R.id.tranrecview_body_lly)
        footerLayout = view.findViewById(R.id.tranrecview_footer_lly)
        editedDetailLayout = view.findViewById(R.id.tranrecview_editedDetailHolder_lly)
        editedDetailName = view.findViewById(R.id.tranrecview_itemName_txt)
        editedDetailAmount = view.findViewById(R.id.tranrecview_quantity_txt)
        editedDetailUnit = view.findViewById(R.id.tranrecview_quantityType_txt)
        editedDetailCost = view.findViewById(R.id.tranrecview_cost_monf)
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

        if (transactionDetails.isNotEmpty()) {
            transactionDetailsRecyclerView.layoutManager = LinearLayoutManager(context)
            transactionDetailsRecyclerView.adapter = TransactionDetailRecyclerAdapter(newTransaction, transactionDetails, context)
        }

        val unallocatedBalance = newTransaction.amount.absoluteValue-transactionDetails.sumOf { it.cost }
        editedDetailLayout.visibility = if (unallocatedBalance==0.0) View.GONE else View.VISIBLE
        updateEditedTransactionDetail(resources.getString(R.string.unallocated_balance), 0.0, "", unallocatedBalance)
        totalAmountTextView.setValue(newTransaction.amount.absoluteValue, AustromApplication.activeAssets[newTransaction.assetId]!!.currencyCode)
    }

    fun updateEditedTransactionDetail(detailName: String, detailAmount: Double, detailUnit: String, detailCost: Double) {
        editedDetailName.text = detailName
        editedDetailAmount.text = detailAmount.toString()
        editedDetailUnit.text = detailUnit
        editedDetailAmount.visibility = if (detailAmount==0.0) View.GONE else View.VISIBLE
        editedDetailUnit.visibility = if (detailAmount==0.0) View.GONE else View.VISIBLE
        editedDetailCost.setValue(detailCost, AustromApplication.activeCurrencies[AustromApplication.activeAssets[transaction?.assetId]?.currencyCode]?.symbol ?: "USD" )
    }
}