package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Guideline
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.colleagues.austrom.views.MoneyFormatTextView
import com.colleagues.austrom.views.PeriodType
import java.time.LocalDate
import kotlin.math.absoluteValue

class PlanningGraphRecyclerAdapter(private val context: Context, private val dataSet: List<Pair<Category, List<Transaction>>>,
                                   private val date: LocalDate, private val periodType: PeriodType) : RecyclerView.Adapter<PlanningGraphRecyclerAdapter.PlanningGraphViewHolder>() {
    private var returnClickedItem: (Category)->Unit = {}
    fun setOnItemClickListener(l: ((Category)->Unit)) { returnClickedItem = l }
    class PlanningGraphViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryNameTextView: TextView = view.findViewById(R.id.planit_categoryName_txt)
        val plannedExpenseTextView: MoneyFormatTextView = view.findViewById(R.id.planit_categoryPlanExpense_monf)
        val actualExpenseTextView: MoneyFormatTextView = view.findViewById(R.id.planit_factExpence_monf)
        val innerPercent: Guideline = view.findViewById(R.id.planit_fillPercentInner_gui)
        val outerPercent: Guideline = view.findViewById(R.id.planit_fillPercentOut_gui)
        val topItemsRecycler: RecyclerView = view.findViewById(R.id.planit_topExpensesHolder_rcv)
        val holder: CardView = view.findViewById(R.id.planit_holder_cdv)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanningGraphViewHolder { return PlanningGraphViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_planning, parent, false) ) }
    override fun getItemCount(): Int = dataSet.size

    override fun onBindViewHolder(holder: PlanningGraphViewHolder, position: Int) {
        val item = dataSet[position]
        holder.categoryNameTextView.text = item.first.name
        val sum = item.second.sumOf { l -> l.getAmountInBaseCurrency() }.absoluteValue
        val localDBProvider = LocalDatabaseProvider(context)
        val plannedValue = localDBProvider.getPlan(date,periodType,item.first)?.planValue ?: 10000.0
        holder.plannedExpenseTextView.setValue(plannedValue)
        holder.actualExpenseTextView.setValue(sum, "")
        var percent = sum.toFloat()/plannedValue.toFloat()
        if (percent>1f) percent=1f
        holder.innerPercent.setGuidelinePercent(percent)
        holder.outerPercent.setGuidelinePercent(innerPercentToOuterPercent(percent))

        holder.topItemsRecycler.layoutManager = LinearLayoutManager(context)
        if (item.second.isNotEmpty()) {
            val transactionsDetailsToShow = mutableListOf<TransactionDetail>()
            item.second.sortedBy { transaction -> transaction.amount }.take(3).forEach { transaction ->
                transactionsDetailsToShow.add(TransactionDetail(transaction.transactionId, transaction.transactionName, transaction.amount.absoluteValue))
            }
            holder.topItemsRecycler.adapter = TransactionDetailRecyclerAdapter(item.second.first(), transactionsDetailsToShow, context)
        } else {
            holder.topItemsRecycler.adapter = SelectorRecyclerAdapter(listOf())
        }

        holder.holder.setOnClickListener { returnClickedItem(item.first) }
    }

    private fun innerPercentToOuterPercent(innerPercentValue: Float): Float { return 0.07f+0.86f*innerPercentValue }
}