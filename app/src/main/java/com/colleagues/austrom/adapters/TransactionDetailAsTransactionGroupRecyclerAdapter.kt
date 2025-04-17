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
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.views.MoneyFormatTextView


class TransactionDetailAsTransactionGroupRecyclerAdapter(private val groupedTransactionsDetailsMap: Map<String, Map<Transaction, List<TransactionDetail>>>, private val context: Context) : RecyclerView.Adapter<TransactionDetailAsTransactionGroupRecyclerAdapter.TransactionDetailAsTransactionGroupViewHolder>(){
    class TransactionDetailAsTransactionGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionGroupName: TextView = itemView.findViewById(R.id.trgritem_date_txt)
        val transactionHolderRecyclerView: RecyclerView = itemView.findViewById(R.id.trgritem_transactionholder_rcv)
        val transactionGroupSumHolder: MoneyFormatTextView = itemView.findViewById(R.id.trgritem_sumHolder_mfor)
    }
    init {
        //groupedTransactionsDetailsMap.forEach { group -> group.value.sortBy { transaction -> transaction.amount } }
    }
    private var returnClickedItem: (transactionDetail: TransactionDetail, index: Int)->Unit = { _, _ ->}
    fun setOnItemClickListener(l: ((TransactionDetail, Int)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionDetailAsTransactionGroupViewHolder { return TransactionDetailAsTransactionGroupViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_group, parent, false)) }
    override fun getItemCount(): Int { return groupedTransactionsDetailsMap.size }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TransactionDetailAsTransactionGroupViewHolder, position: Int) {
        val transactionGroupHeader = groupedTransactionsDetailsMap.keys.elementAt(position)
        holder.transactionGroupName.text = transactionGroupHeader
        holder.transactionHolderRecyclerView.layoutManager = LinearLayoutManager(context)
        var transactionsSum = 0.0
        groupedTransactionsDetailsMap.values.elementAt(position).forEach { transaction -> transactionsSum+=transaction.value.sumOf { transactionDetail -> transactionDetail.cost } }
        val moneyColor = if (transactionsSum>0) context.getColor(R.color.incomeGreen) else if (transactionsSum<0) context.getColor(R.color.expenseRed) else context.getColor(R.color.transferYellow)
        holder.transactionGroupSumHolder.setAmountColor(moneyColor)
        holder.transactionGroupSumHolder.setCurrencyColor(moneyColor)
        holder.transactionGroupSumHolder.setValue(transactionsSum)
        val adapter = TransactionDetailAsTransactionRecyclerAdapter(groupedTransactionsDetailsMap[transactionGroupHeader]!!, context)
        adapter.setOnItemClickListener { transactionDetail, _ -> returnClickedItem(transactionDetail, position) }
        holder.transactionHolderRecyclerView.adapter = adapter
    }
}
