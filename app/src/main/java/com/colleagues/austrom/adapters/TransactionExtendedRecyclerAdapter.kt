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
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType

class TransactionExtendedRecyclerAdapter(private val transactions: List<Transaction>,
                                         private val activity: AppCompatActivity,
                                         private val isShowingActionButtons: Boolean = false)  : RecyclerView.Adapter<TransactionExtendedRecyclerAdapter.TransactionExtendedViewHolder>() {


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
        var isIssueEncountered: Boolean = false
        var categoryId: String? = null
        var assetId: String? = null
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
        var issueMessage = ""
        holder.issueMessage.text = "No Issues Found"
        holder.actionButtonsHolder.visibility = if (isShowingActionButtons) View.VISIBLE else View.GONE
        holder.transactionHolderBackground.visibility = if (isShowingActionButtons) View.VISIBLE else View.GONE
        holder.categoryName.text = transaction.categoryId
        holder.comment.text = transaction.comment
        holder.amount.text = transaction.amount.toMoneyFormat()
        holder.date.text = transaction.transactionDate.toString()
        val activeAsset = if (AustromApplication.activeAssets.values.find { l -> l.assetName==transaction.targetName } != null) {
            AustromApplication.activeAssets.values.find { l -> l.assetName==transaction.targetName }
        } else {
            AustromApplication.activeAssets.values.find { l -> l.assetName==transaction.sourceName }
        }
        holder.currencySymbol.text = if (activeAsset!=null) AustromApplication.activeCurrencies[activeAsset.currencyCode]?.symbol ?: "$" else "$"
        if (transaction.amount>0.0) {
            holder.primaryParticipant.text = transaction.sourceName
            holder.secondaryParticipant.text = transaction.targetName
            holder.amount.setTextColor(activity.getColor(R.color.incomeGreen))
            holder.currencySymbol.setTextColor(activity.getColor(R.color.incomeGreen))
            val incomeCategories = dbProvider.getCategories(TransactionType.INCOME)
            holder.categoryId = (incomeCategories.find { l -> l.name==transaction.categoryId })?.id
        } else if (transaction.amount<0.0) {
            holder.primaryParticipant.text = transaction.targetName
            holder.secondaryParticipant.text = transaction.sourceName
            holder.amount.setTextColor(activity.getColor(R.color.expenseRed))
            holder.currencySymbol.setTextColor(activity.getColor(R.color.expenseRed))
            val expenseCategories = dbProvider.getCategories(TransactionType.EXPENSE)
            holder.categoryId = (expenseCategories.find { l -> l.name==transaction.categoryId })?.id
        } else {
            holder.primaryParticipant.text = transaction.sourceName
            holder.secondaryParticipant.text = transaction.targetName
            issueMessage += activity.getString(R.string.transaction_amount_is_zero)
            holder.isIssueEncountered = true
        }

        if (activeAsset==null) {
            issueMessage += activity.getString(R.string.asset_does_not_match_any_active_asset)
            holder.isIssueEncountered = true
        } else {
            holder.assetId = activeAsset.assetId
        }

        if (holder.categoryId == null) {
            issueMessage += activity.getString(R.string.category_does_not_match_any_of_existing)
            holder.isIssueEncountered = true
        }
        
        if (!holder.isIssueEncountered) {
            holder.issueMessage.text = activity.getString(R.string.no_issues_encountered)
        } else {
            holder.issueMessage.text = issueMessage
        }

        holder.cancelButton.setOnClickListener {

        }

        holder.editButton.setOnClickListener {

        }

        holder.acceptButton.setOnClickListener {

        }
    }
}