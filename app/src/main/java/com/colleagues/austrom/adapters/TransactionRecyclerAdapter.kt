package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.TransactionPropertiesActivity
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType

class TransactionRecyclerAdapter(private val transactions: List<Transaction>,
                                 private val activity: AppCompatActivity) : RecyclerView.Adapter<TransactionRecyclerAdapter.TransactionViewHolder>() {


    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tritem_categoryName_txt)
        val categoryImage: ImageView = itemView.findViewById(R.id.tritem_categoryIcon_img)
        val transactionDate: TextView = itemView.findViewById(R.id.tritem_date_txt)
        val amount: TextView = itemView.findViewById(R.id.tritem_amount_txt)
        val currencySymbol: TextView = itemView.findViewById(R.id.tritem_currencySymbol_txt)
        val sourceName: TextView = itemView.findViewById(R.id.tritem_sourceName_txt)
        val targetName: TextView = itemView.findViewById(R.id.tritem_targetName_txt)
        val secondaryAmount: TextView = itemView.findViewById(R.id.tritem_secondaryamount_txt)
        val secondaryCurrency: TextView = itemView.findViewById(R.id.tritem_secondarycurrency_txt)
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
        val transaction = transactions[position]
        holder.categoryName.text = transaction.categoryId
        holder.sourceName.text = transaction.sourceName
        holder.targetName.text = transaction.targetName
        val source = AustromApplication.activeAssets[transaction.sourceId]
        val target = AustromApplication.activeAssets[transaction.targetId]
        var category : Category? = null
        when (transaction.transactionType()) {
            TransactionType.TRANSFER -> {
                holder.secondaryAmount.visibility = View.VISIBLE
                holder.secondaryAmount.text ="-" + transaction.amount.toMoneyFormat()
                holder.secondaryAmount.setTextColor(Color.RED)
                holder.secondaryCurrency.visibility = View.VISIBLE
                holder.secondaryCurrency.text = AustromApplication.activeCurrencies[source?.currencyCode]?.symbol
                holder.amount.text ="+" + transaction.secondaryAmount?.toMoneyFormat()
                holder.amount.setTextColor(Color.rgb(0,100,0))
                holder.currencySymbol.text = AustromApplication.activeCurrencies[target?.currencyCode]?.symbol
                category = Category.defaultTransferCategories.find { it.name == transaction.categoryId }
            }
            TransactionType.EXPENSE -> {
                holder.amount.text ="-" + transaction.amount.toMoneyFormat()
                holder.amount.setTextColor(Color.RED)
                holder.currencySymbol.text = AustromApplication.activeCurrencies[source?.currencyCode]?.symbol
                category = Category.defaultExpenseCategories.find { it.name == transaction.categoryId }
            }
            TransactionType.INCOME -> {
                holder.amount.text = "+" + transaction.amount.toMoneyFormat()
                holder.amount.setTextColor(Color.rgb(0,100,0))
                holder.currencySymbol.text = AustromApplication.activeCurrencies[target?.currencyCode]?.symbol
                category = Category.defaultIncomeCategories.find { it.name == transaction.categoryId }
            }
        }
        holder.transactionDate.text = transaction.transactionDate?.toDayOfWeekAndShortDateFormat()
        if (category!=null) {
            holder.categoryImage.setImageResource(category.imgReference!!)
        }

        holder.transactionHolder.setOnClickListener {
            AustromApplication.selectedTransaction = transaction
            activity.startActivity(Intent(activity, TransactionPropertiesActivity::class.java))
        }
    }
}

