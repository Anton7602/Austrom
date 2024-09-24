package com.colleagues.austrom.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionGroupRecyclerAdapter(private val groupedTransactions: MutableMap<LocalDate, MutableList<Transaction>>,
                                      private val activity: Activity) : RecyclerView.Adapter<TransactionGroupRecyclerAdapter.TransactionGroupViewHolder>(){
    class TransactionGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionGroupName: TextView = itemView.findViewById(R.id.trgritem_date_txt)
        val transactionHolderRecyclerView: RecyclerView= itemView.findViewById(R.id.trgritem_transactionholder_rcv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionGroupViewHolder {
        return TransactionGroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_group, parent, false))
    }

    override fun getItemCount(): Int {
        return groupedTransactions.size
    }

    override fun onBindViewHolder(holder: TransactionGroupViewHolder, position: Int) {
        val transactionDate = groupedTransactions.keys.elementAt(position)
        holder.transactionGroupName.text = "${transactionDate.dayOfWeek} ${transactionDate.format(DateTimeFormatter.ofPattern("dd.MM"))}"
        holder.transactionHolderRecyclerView.layoutManager = LinearLayoutManager(activity)
        holder.transactionHolderRecyclerView.adapter = TransactionRecyclerAdapter(groupedTransactions.values.elementAt(position))
    }

}