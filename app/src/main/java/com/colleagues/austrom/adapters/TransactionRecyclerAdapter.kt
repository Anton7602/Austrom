package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionPropertiesActivity
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType

class TransactionRecyclerAdapter(private val transactions: List<Transaction>,
                                 private val activity: AppCompatActivity) : RecyclerView.Adapter<TransactionRecyclerAdapter.TransactionViewHolder>() {


    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tritem_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.tritem_categoryIcon_img)
        val amount: TextView = itemView.findViewById(R.id.tritem_amount_txt)
        val currencySymbol: TextView = itemView.findViewById(R.id.tritem_currencySymbol_txt)
        val primaryParticipant: TextView = itemView.findViewById(R.id.tritem_sourceName_txt)
        val secondaryParticipant: TextView = itemView.findViewById(R.id.tritem_targetName_txt)
        val transactionHolder: CardView = itemView.findViewById(R.id.tritem_transactionHolder_cdv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return TransactionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false))
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val dbProvider = LocalDatabaseProvider(activity)
        holder.transactionHolder.setBackgroundResource(R.drawable.img_transaction_card_background)
        val transaction = transactions[position]

        val source = AustromApplication.activeAssets[transaction.sourceId]
        val target = AustromApplication.activeAssets[transaction.targetId]
        val category : Category? =  if (transaction.categoryId!=null) dbProvider.getCategoryById(transaction.categoryId!!) else null
        holder.categoryName.text = category?.name
        when (transaction.transactionType()) {
            TransactionType.TRANSFER -> {
                holder.amount.text ="+" + if(transaction.secondaryAmount==null) transaction.amount.toMoneyFormat() else transaction.secondaryAmount?.toMoneyFormat()
                holder.amount.setTextColor(Color.rgb(0,100,0))
                holder.currencySymbol.text = AustromApplication.activeCurrencies[target?.currencyCode]?.symbol

            }
            TransactionType.EXPENSE -> {
                holder.amount.text ="-" + transaction.amount.toMoneyFormat()
                holder.amount.setTextColor(activity.getColor(R.color.expenseRed))
                holder.currencySymbol.setTextColor(activity.getColor(R.color.expenseRed))
                holder.currencySymbol.text = AustromApplication.activeCurrencies[source?.currencyCode]?.symbol
                holder.primaryParticipant.text = "${activity.getString(R.string.fromAsset)} ${transaction.sourceName}"
                holder.secondaryParticipant.text = transaction.targetName
            }
            TransactionType.INCOME -> {
                holder.amount.text = "+" + transaction.amount.toMoneyFormat()
                holder.amount.setTextColor(activity.getColor(R.color.incomeGreen))
                holder.currencySymbol.setTextColor(activity.getColor(R.color.incomeGreen))
                holder.currencySymbol.text = AustromApplication.activeCurrencies[target?.currencyCode]?.symbol
                holder.primaryParticipant.text = "${activity.getString(R.string.toAsset)} ${transaction.targetName}"
                holder.secondaryParticipant.text = transaction.sourceName
            }
        }
        if (category!=null) {
            holder.categoryImage.setImageResource(category.imgReference!!.resourceId)
        }

        holder.transactionHolder.setOnClickListener {
            AustromApplication.selectedTransaction = transaction
            activity.startActivity(Intent(activity, TransactionPropertiesActivity::class.java))
        }
    }
}

