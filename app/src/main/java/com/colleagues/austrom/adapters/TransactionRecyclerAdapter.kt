package com.colleagues.austrom.adapters

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
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import java.time.format.DateTimeFormatter

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

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.categoryName.text = transactions[position].categoryId
        holder.sourceName.text = transactions[position].sourceName
        val source = AustromApplication.activeAssets[transactions[position].sourceId]
        val target = AustromApplication.activeAssets[transactions[position].targetId]
        var category : Category? = null
        if (transactions[position].sourceId!=null && transactions[position].targetId!=null) {
            holder.secondaryAmount.visibility = View.VISIBLE
            holder.secondaryAmount.text ="-" + String.format("%.2f", transactions[position].amount)
            holder.secondaryAmount.setTextColor(Color.RED)
            holder.secondaryCurrency.visibility = View.VISIBLE
            holder.secondaryCurrency.text = AustromApplication.activeCurrencies[source?.currencyCode]?.symbol
            holder.amount.text ="+" + String.format("%.2f", transactions[position].secondaryAmount)
            holder.amount.setTextColor(Color.rgb(0,100,0))
            holder.currencySymbol.text = AustromApplication.activeCurrencies[target?.currencyCode]?.symbol
            category = Category.defaultTransferCategories.find { it.name == transactions[position].categoryId }
        } else if (transactions[position].sourceId!=null) {
            holder.amount.text ="-" + String.format("%.2f", transactions[position].amount)
            holder.amount.setTextColor(Color.RED)
            holder.currencySymbol.text = AustromApplication.activeCurrencies[source?.currencyCode]?.symbol
            category = Category.defaultExpenseCategories.find { it.name == transactions[position].categoryId }
        } else {
            holder.amount.text = "+" + String.format("%.2f", transactions[position].amount)
            holder.amount.setTextColor(Color.rgb(0,100,0))
            holder.currencySymbol.text = AustromApplication.activeCurrencies[target?.currencyCode]?.symbol
            category = Category.defaultIncomeCategories.find { it.name == transactions[position].categoryId }
        }
        holder.targetName.text = transactions[position].targetName
        holder.transactionDate.text =
            "${transactions[position].transactionDate?.dayOfWeek} ${transactions[position].transactionDate?.format(DateTimeFormatter.ofPattern("dd.MM"))}"
        if (category!=null) {
            holder.categoryImage.setImageResource(category.imgReference!!)
        }

        holder.transactionHolder.setOnClickListener {
            activity.startActivity(Intent(activity, TransactionPropertiesActivity::class.java))
        }
    }
}

