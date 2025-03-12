package com.colleagues.austrom.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toDayOfWeekAndShortDateFormat
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import java.time.LocalDate


class TransactionDetailAsTransactionGroupRecyclerAdapter(private val groupedTransactionsDetailsMap: Map<LocalDate, Map<Transaction, List<TransactionDetail>>>, private val context: Context) : RecyclerView.Adapter<TransactionDetailAsTransactionGroupRecyclerAdapter.TransactionDetailAsTransactionGroupViewHolder>(){
    class TransactionDetailAsTransactionGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionGroupName: TextView = itemView.findViewById(R.id.trgritem_date_txt)
        val transactionHolderRecyclerView: RecyclerView = itemView.findViewById(R.id.trgritem_transactionholder_rcv)
    }
    private var returnClickedItem: (transactionDetail: TransactionDetail, index: Int)->Unit = { _, _ ->}
    fun setOnItemClickListener(l: ((TransactionDetail, Int)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailAsTransactionGroupViewHolder { return TransactionDetailAsTransactionGroupViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_group, parent, false)) }
    override fun getItemCount(): Int { return groupedTransactionsDetailsMap.size }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionDetailAsTransactionGroupViewHolder, position: Int) {
        val transactionDate = groupedTransactionsDetailsMap.keys.elementAt(position)
        holder.transactionGroupName.text = transactionDate.toDayOfWeekAndShortDateFormat()
        holder.transactionHolderRecyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = TransactionDetailAsTransactionRecyclerAdapter(groupedTransactionsDetailsMap[transactionDate]!!, context)
        adapter.setOnItemClickListener { transactionDetail, _ -> returnClickedItem(transactionDetail, position) }
        holder.transactionHolderRecyclerView.adapter = adapter
    }
}
