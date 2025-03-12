package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.DeletionConfirmationDialogFragment
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.InvalidTransactionException
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.models.TransactionValidationType
import kotlin.math.absoluteValue

class TransactionDetailAsTransactionRecyclerAdapter(private val transactionDetailsMap: Map<Transaction, List<TransactionDetail>>, private val context: Context) : RecyclerView.Adapter<TransactionDetailAsTransactionRecyclerAdapter.TransactionDetailAsTransactionViewHolder>() {
    class TransactionDetailAsTransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tritem_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.tritem_categoryIcon_img)
        val amount: TextView = itemView.findViewById(R.id.tritem_amount_txt)
        val currencySymbol: TextView = itemView.findViewById(R.id.tritem_currencySymbol_txt)
        val secondaryParticipant: TextView = itemView.findViewById(R.id.tritem_sourceName_txt)
        val primaryParticipant: TextView = itemView.findViewById(R.id.tritem_targetName_txt)
        val transactionHolder: CardView = itemView.findViewById(R.id.tritem_transactionHolder_cdv)
    }
    private var transactions: Map<String, Transaction> = mapOf()
    private var transactionDetails: List<TransactionDetail> = listOf()
    private var returnClickedItem: (transaction: TransactionDetail, index: Int)->Unit = { _, _ ->}
    fun setOnItemClickListener(l: ((TransactionDetail, Int)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailAsTransactionViewHolder { return TransactionDetailAsTransactionViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)) }
    override fun getItemCount(): Int { return transactionDetails.size }
    init {
        transactionDetails = transactionDetailsMap.values.flatten()
        transactions = transactionDetailsMap.keys.associateBy { it.transactionId }
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionDetailAsTransactionViewHolder, position: Int) {
        holder.transactionHolder.setBackgroundResource(R.drawable.img_transaction_card_background)
        val transactionDetail = transactionDetails[position]
        val transaction = transactions[transactionDetail.transactionId] ?: throw InvalidTransactionException(TransactionValidationType.UNKNOWN_LINKED_TRANSACTION)
        try {
            val asset = AustromApplication.activeAssets[transaction.assetId] ?: throw InvalidTransactionException("Asset used in transaction is not recognized", TransactionValidationType.UNKNOWN_ASSET_INVALID)
            val category: Category
            when (transaction.transactionType()) {
                TransactionType.TRANSFER -> {
                    category = AustromApplication.activeCategories[transaction.categoryId] ?: throw InvalidTransactionException("Category used in transaction is not recognized", TransactionValidationType.UNKNOWN_CATEGORY_INVALID)
                    holder.amount.text = if (transactionDetail.cost < 0) "-${transactionDetail.cost.absoluteValue.toMoneyFormat()}" else "+${transactionDetail.cost.absoluteValue.toMoneyFormat()}"
                    holder.amount.setTextColor(context.getColor(R.color.transferYellow))
                    holder.currencySymbol.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
                    holder.currencySymbol.setTextColor(context.getColor(R.color.transferYellow))
                    holder.primaryParticipant.text = transactionDetail.name
                    holder.secondaryParticipant.text = if (transaction.amount < 0) "${context.getString(R.string.from)} ${transaction.transactionName}" else "${context.getString(R.string.toAsset)} ${transaction.transactionName}"
                }

                TransactionType.EXPENSE -> {
                    category = AustromApplication.activeCategories[transaction.categoryId] ?: throw InvalidTransactionException("Category used in transaction is not recognized", TransactionValidationType.UNKNOWN_CATEGORY_INVALID)
                    holder.amount.text = (-transactionDetail.cost).toMoneyFormat()
                    holder.amount.setTextColor(context.getColor(R.color.expenseRed))
                    holder.currencySymbol.setTextColor(context.getColor(R.color.expenseRed))
                    holder.currencySymbol.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
                    holder.secondaryParticipant.text = "${context.getString(R.string.at)} ${transaction.transactionName}"
                    holder.primaryParticipant.text = transactionDetail.name
                }

                TransactionType.INCOME -> {
                    category = AustromApplication.activeCategories[transaction.categoryId] ?: throw InvalidTransactionException("Category used in transaction is not recognized", TransactionValidationType.UNKNOWN_CATEGORY_INVALID)
                    holder.amount.text = "+" + transactionDetail.cost.toMoneyFormat()
                    holder.amount.setTextColor(context.getColor(R.color.incomeGreen))
                    holder.currencySymbol.setTextColor(context.getColor(R.color.incomeGreen))
                    holder.currencySymbol.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
                    holder.secondaryParticipant.text = "${context.getString(R.string.from)} ${transaction.transactionName}"
                    holder.primaryParticipant.text = transactionDetail.name
                }
            }
            if (holder.secondaryParticipant.text.toString().length>20) holder.secondaryParticipant.text = holder.secondaryParticipant.text.toString().substring(0,20)+"."
            holder.categoryName.text = category.name
            holder.categoryImage.setImageResource(category.imgReference.resourceId)
        } catch (ex: InvalidTransactionException) {
            callErrorDialog(transaction)
        }
        holder.transactionHolder.setOnClickListener { returnClickedItem(transactionDetail, position) }
        animateItem(holder.itemView)
    }

    private fun animateItem(view: View) {
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun callErrorDialog(transaction: Transaction) {
        val dialog = DeletionConfirmationDialogFragment("There was error with displaying a transaction: ${transaction.transactionName}. Do you want to delete it from the system?", "Yes", "No")
        dialog.setOnDialogResultListener { isConfirmed -> if (isConfirmed) transaction.cancel( LocalDatabaseProvider(context)) }
        dialog.show((context  as AppCompatActivity).supportFragmentManager, "Deletion Confirmation Dialog")
    }
}
