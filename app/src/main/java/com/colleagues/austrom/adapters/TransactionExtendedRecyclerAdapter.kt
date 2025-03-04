package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.TransactionValidationType

class TransactionExtendedRecyclerAdapter(private val transactions: MutableList<Transaction>, private val context: Context, private val isShowingActionButtons: Boolean = false)  : RecyclerView.Adapter<TransactionExtendedRecyclerAdapter.TransactionExtendedViewHolder>() {
    class TransactionExtendedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val primaryParticipant: TextView = itemView.findViewById(R.id.tritemext_targetName_txt)
        val amount: TextView = itemView.findViewById(R.id.tritemext_amount_txt)
        val currencySymbol: TextView = itemView.findViewById(R.id.tritemext_currencySymbol_txt)
        val secondaryParticipant: TextView = itemView.findViewById(R.id.tritemext_sourceName_txt)
        val categoryName: TextView = itemView.findViewById(R.id.tritemext_categoryName_txt)
        val comment: TextView = itemView.findViewById(R.id.tritemext_comment_txt)
        val date: TextView = itemView.findViewById(R.id.tritemext_date_txt)
        val transactionHolder: CardView = itemView.findViewById(R.id.tritemext_transactionHolder_cdv)
        val transactionHolderBackground: ImageView = itemView.findViewById(R.id.tritemext_transactionHolderBackground_img)
        val actionButtonsHolder: CardView = itemView.findViewById(R.id.tritemext_actionButtonsHolder_btn)
        val cancelButton: ImageButton = itemView.findViewById(R.id.tritemext_cancels_btn)
        val editButton: ImageButton = itemView.findViewById(R.id.tritemext_edit_btn)
        val acceptButton: ImageButton = itemView.findViewById(R.id.tritemext_accept_btn)
        val issueMessage: TextView = itemView.findViewById(R.id.tritemext_issueMessage_txt)
        var transaction: Transaction? = null
        var isIssueEncountered: Boolean = false
        var isWarningEncountered: Boolean = false

        fun switchDisplayMode(context: Context) {
            transactionHolderBackground.setImageResource(R.drawable.img_transaction_extended_ok)
            issueMessage.setTextColor(context.getColor(R.color.incomeGreen))
            if (isWarningEncountered) {
                transactionHolderBackground.setImageResource(R.drawable.img_transaction_extended_warning)
                issueMessage.setTextColor(context.getColor(R.color.transferYellow))
            }

            if (isIssueEncountered) {
                transactionHolderBackground.setImageResource(R.drawable.img_transaction_extended_error)
                issueMessage.setTextColor(context.getColor(R.color.expenseRed))
            }
        }
    }
    private var returnClickedItemAccept: (Transaction)->Unit = {}
    fun setOnItemClickListenerAccept(l: ((Transaction)->Unit)) { returnClickedItemAccept = l }
    private var returnClickedItemEdit: (Transaction)->Unit = {}
    fun setOnItemClickListenerEdit(l: ((Transaction)->Unit)) { returnClickedItemEdit = l }
    private var returnOperationResult: ()->Unit = {}
    fun setOnOperationResultListener(l: (()->Unit)) {returnOperationResult = l}
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionExtendedViewHolder { return TransactionExtendedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_extended, parent, false)) }
    override fun getItemCount(): Int { return transactions.size }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionExtendedViewHolder, position: Int) {
        val dbProvider = LocalDatabaseProvider(context)
        val transaction = transactions[position]
        val activeAsset = AustromApplication.activeAssets[transaction.assetId]
        var activeCategory: Category? = null

        holder.isIssueEncountered = false
        holder.isWarningEncountered = false
        holder.issueMessage.text = context.getString(R.string.no_issues_encountered)
        holder.actionButtonsHolder.visibility = if (isShowingActionButtons) View.VISIBLE else View.GONE
        holder.transactionHolderBackground.visibility = if (isShowingActionButtons) View.VISIBLE else View.GONE
        holder.comment.text = transaction.comment
        holder.amount.text = transaction.amount.toMoneyFormat()
        holder.date.text = transaction.transactionDate.toString()
        holder.currencySymbol.text = AustromApplication.activeCurrencies[activeAsset?.currencyCode]?.symbol ?: "$"
        holder.primaryParticipant.text = transaction.transactionName
        holder.secondaryParticipant.text = activeAsset?.assetName ?: transaction.assetId
        if (transaction.amount>0.0) {
            holder.amount.setTextColor(context.getColor(R.color.incomeGreen))
            holder.currencySymbol.setTextColor(context.getColor(R.color.incomeGreen))
            val incomeCategories = AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.INCOME }
            activeCategory = incomeCategories.find { l -> l.name.lowercase()==transaction.categoryId.lowercase() } ?: incomeCategories.find { l -> l.categoryId.lowercase()==transaction.categoryId.lowercase() }
        } else if (transaction.amount<0.0) {
            holder.amount.setTextColor(context.getColor(R.color.expenseRed))
            holder.currencySymbol.setTextColor(context.getColor(R.color.expenseRed))
            val expenseCategories = AustromApplication.activeCategories.values.filter { l -> l.transactionType==TransactionType.EXPENSE }
            activeCategory = expenseCategories.find { l -> l.name.lowercase()==transaction.categoryId.lowercase() } ?: expenseCategories.find { l -> l.categoryId.lowercase()==transaction.categoryId.lowercase() }
        } else {
            holder.issueMessage.text = context.getString(R.string.transaction_amount_is_zero)
            holder.isIssueEncountered = true
        }
        if (activeCategory!=null) {
            transaction.categoryId = activeCategory.categoryId
        }
        holder.categoryName.text = activeCategory?.name ?: transaction.categoryId

        holder.primaryParticipant.setTextColor(context.getColor(R.color.primaryTextColor))
        holder.categoryName.setTextColor(context.getColor(R.color.primaryTextColor))
        if (!transaction.isValid()) {
            holder.isIssueEncountered = true
            when (transaction.validate()) {
                TransactionValidationType.UNKNOWN_ASSET_INVALID -> holder.primaryParticipant.setTextColor(context.getColor(R.color.expenseRed))
                TransactionValidationType.UNKNOWN_CATEGORY_INVALID -> holder.categoryName.setTextColor(context.getColor(R.color.expenseRed))
                //TransactionValidationType.AMOUNT_INVALID -> holder.amount.setTextColor(context.getColor(R.color.expenseRed))
                else -> {}
            }
        }

        if (!holder.isIssueEncountered && transaction.isColliding(dbProvider)) {
            holder.isWarningEncountered = true
            holder.issueMessage.text = context.getString(R.string.in_this_date_transaction_of_this_exact_amount_already_exist_are_you_sure_it_isn_t_duplicate)
        }

        holder.switchDisplayMode(context)
        holder.cancelButton.setOnSafeClickListener { removeTransaction(transaction) }
        holder.editButton.setOnSafeClickListener { returnClickedItemEdit(transaction) }
        holder.acceptButton.setOnSafeClickListener { if (!holder.isIssueEncountered) returnClickedItemAccept(transaction) }
    }

    fun removeTransaction(transaction: Transaction) {
        val index = transactions.indexOf(transaction)
        transactions.remove(transaction)
        this.notifyItemRemoved(index)
    }

    private fun submitTransaction(transaction: Transaction, dbProvider: LocalDatabaseProvider) {
        if (transaction.isValid()) {
            transaction.submit(dbProvider)
            val index = transactions.indexOf(transaction)
            transactions.remove(transaction)
            this.notifyItemRemoved(index)
            //receiver?.receiveValue(transaction.transactionId, "Transaction Removed")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitAllValidTransactions() {
        val dbProvider = LocalDatabaseProvider(context)
        for (i in 0..<this.transactions.size) if (!transactions[i].isColliding(dbProvider)) transactions[i].submit(dbProvider)
        transactions.removeIf { transaction -> transaction.isValid() && !transaction.isColliding(dbProvider) }
        this.notifyDataSetChanged()
        //receiver?.receiveValue("", "All Valid Transactions Submitted")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitAllSuspiciousTransactions() {
        val dbProvider = LocalDatabaseProvider(context)
        for (i in 0..<this.transactions.size)  if (transactions[i].isColliding(dbProvider))  transactions[i].submit(dbProvider)
        transactions.removeIf { transaction -> transaction.isValid() && transaction.isColliding(dbProvider) }
        this.notifyDataSetChanged()
        //receiver?.receiveValue("", "All Suspicious Transactions Submitted")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAllInvalidTransaction() {
        transactions.removeIf { transaction -> !transaction.isValid() }
        this.notifyDataSetChanged()
        //receiver?.receiveValue("", "All Invalid Transactions Removed")
    }
}