package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.ImportTransactionDialogFragment
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionValidationType

class TransactionExtendedRecyclerAdapter(private val transactions: MutableList<Transaction>,
                                         private val activity: AppCompatActivity,
                                         private val isShowingActionButtons: Boolean = false, private val receiver: IDialogInitiator? = null)  : RecyclerView.Adapter<TransactionExtendedRecyclerAdapter.TransactionExtendedViewHolder>(), IDialogInitiator {
    //var viewHolders: MutableList<TransactionExtendedViewHolder> = mutableListOf()

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

        fun switchDisplayMode(activity: AppCompatActivity) {
            transactionHolderBackground.setImageResource(R.drawable.img_transaction_extended_ok)
            issueMessage.setTextColor(activity.getColor(R.color.incomeGreen))
            if (isWarningEncountered) {
                transactionHolderBackground.setImageResource(R.drawable.img_transaction_extended_warning)
                issueMessage.setTextColor(activity.getColor(R.color.transferYellow))
            }

            if (isIssueEncountered) {
                transactionHolderBackground.setImageResource(R.drawable.img_transaction_extended_error)
                issueMessage.setTextColor(activity.getColor(R.color.expenseRed))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionExtendedViewHolder {
        return TransactionExtendedViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_extended, parent, false))
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionExtendedViewHolder, position: Int) {
        val dbProvider = LocalDatabaseProvider(activity)
        val transaction = transactions[position]
        val activeAsset = AustromApplication.activeAssets[transaction.assetId]
        var activeCategory: Category? = null

        holder.isIssueEncountered = false
        holder.isWarningEncountered = false
        holder.issueMessage.text = activity.getString(R.string.no_issues_encountered)
        holder.actionButtonsHolder.visibility = if (isShowingActionButtons) View.VISIBLE else View.GONE
        holder.transactionHolderBackground.visibility = if (isShowingActionButtons) View.VISIBLE else View.GONE
        holder.categoryName.text = transaction.categoryId
        holder.comment.text = transaction.comment
        holder.amount.text = transaction.amount.toMoneyFormat()
        holder.date.text = transaction.transactionDate.toString()
        holder.currencySymbol.text = AustromApplication.activeCurrencies[activeAsset?.currencyCode]?.symbol ?: "$"
        holder.primaryParticipant.text = transaction.transactionName
        holder.secondaryParticipant.text = activeAsset?.assetName ?: transaction.assetId
        if (transaction.amount>0.0) {
            holder.amount.setTextColor(activity.getColor(R.color.incomeGreen))
            holder.currencySymbol.setTextColor(activity.getColor(R.color.incomeGreen))
            activeCategory = (AustromApplication.activeIncomeCategories.values.find { l -> l.categoryId==transaction.categoryId })
        } else if (transaction.amount<0.0) {
            holder.amount.setTextColor(activity.getColor(R.color.expenseRed))
            holder.currencySymbol.setTextColor(activity.getColor(R.color.expenseRed))
            activeCategory = (AustromApplication.activeExpenseCategories.values.find { l -> l.categoryId==transaction.categoryId })
        } else {
            //TODO("Transfer Case")
            //holder.primaryParticipant.text = transaction.sourceName
            //holder.secondaryParticipant.text = transaction.targetName
            holder.issueMessage.text = activity.getString(R.string.transaction_amount_is_zero)
            holder.isIssueEncountered = true
        }
        holder.categoryName.text = activeCategory?.name ?: transaction.categoryId

        if (!transaction.isValid()) {
            holder.isIssueEncountered = true
            holder.issueMessage.text = when (transaction.validate()) {
                TransactionValidationType.VALID -> "Unidentified Issue"
                TransactionValidationType.UNKNOWN_ASSET_INVALID -> activity.getString(R.string.asset_does_not_match_any_active_asset)
                TransactionValidationType.UNKNOWN_CATEGORY_INVALID -> activity.getString(R.string.category_does_not_match_any_of_existing)
                TransactionValidationType.UNKNOWN_LINKED_TRANSACTION -> "Linked Transaction Issue"
                TransactionValidationType.AMOUNT_INVALID -> "Amount Issue"
            }
        }

        if (!holder.isIssueEncountered && transaction.isColliding(dbProvider)) {
            holder.isWarningEncountered = true
            holder.issueMessage.text = activity.getString(R.string.in_this_date_transaction_of_this_exact_amount_already_exist_are_you_sure_it_isn_t_duplicate)
        }

//        if (!holder.isIssueEncountered) {
//            holder.transaction = Transaction(
//                userId = AustromApplication.appUser!!.userId,
//                sourceId = if (transaction.amount<0) assetId else null,
//                sourceName = transaction.sourceName,
//                targetId = if (transaction.amount>0) assetId else null,
//                targetName = transaction.targetName,
//                amount = transaction.amount.absoluteValue,
//                categoryId = categoryId,
//                transactionDate = transaction.transactionDate,
//                comment = transaction.comment
//            )

//        }

        holder.switchDisplayMode(activity)
        holder.cancelButton.setOnClickListener {
            val index = transactions.indexOf(transaction)
            transactions.remove(transaction)
            this.notifyItemRemoved(index)
            receiver?.receiveValue(transaction.transactionId, "Transaction Imported")
        }
        holder.editButton.setOnClickListener {
            ImportTransactionDialogFragment(transaction, transactions, activity, this).show(activity.supportFragmentManager, "Import Linking Dialog" )
        }
        holder.acceptButton.setOnClickListener {
            submitTransaction(transaction, dbProvider)
        }
    }

    private fun submitTransaction(transaction: Transaction, dbProvider: LocalDatabaseProvider) {
        if (transaction.isValid()) {
            transaction.submit(dbProvider)
            val index = transactions.indexOf(transaction)
            transactions.remove(transaction)
            this.notifyItemRemoved(index)
            receiver?.receiveValue(transaction.transactionId, "Transaction Removed")
        }
    }

    fun submitAllValidTransactions() {
        val dbProvider = LocalDatabaseProvider(activity)
        transactions.forEach { transaction -> submitTransaction(transaction, dbProvider) }
    }

    override fun receiveValue(value: String, valueType: String) {
        this.notifyItemChanged(value.toInt())
        receiver?.receiveValue("", "Transaction Changed")
    }
}