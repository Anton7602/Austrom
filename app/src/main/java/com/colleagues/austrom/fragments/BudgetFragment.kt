package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.VerticalBarChartAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.PieChartDiagramView
import kotlin.math.absoluteValue

class BudgetFragment : Fragment(R.layout.fragment_budget) {
    private lateinit var pieChart: PieChartDiagramView
    private lateinit var barChart: RecyclerView
    private fun bindViews(view: View) {
        pieChart = view.findViewById(R.id.bud_chart_pch)
        barChart = view.findViewById(R.id.bud_bar_chart_rcv)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    override fun onStart() {
        super.onStart()
        val dbProvider = LocalDatabaseProvider(requireActivity())
        pieChart.setChartData(calculateTransactionsSums(dbProvider.getTransactionsOfActiveBudget()))
        setUpRecyclerView()
    }

    private fun calculateTransactionsSums(transactions: List<Transaction>) : List<Pair<Double, String>> {
        val transactionsByCategories = mutableMapOf<String, Double>()
        val dataSet = mutableListOf<Pair<Double, String>>()
        transactions.forEach { transaction ->
            if (transactionsByCategories.containsKey(transaction.categoryId)) {
                transactionsByCategories[transaction.categoryId] = transactionsByCategories[transaction.categoryId]!! +  transaction.amount.absoluteValue
            } else {
                transactionsByCategories[transaction.categoryId] = transaction.amount.absoluteValue
            }
        }
        transactionsByCategories.forEach { (categoryId, transactionsSum) ->
            if (AustromApplication.activeCategories.filter { l -> l.value.transactionType==TransactionType.EXPENSE }.containsKey(categoryId)) {
                dataSet.add(Pair(transactionsSum, AustromApplication.activeCategories[categoryId]!!.name))
            }
        }
        return dataSet.sortedByDescending { l->l.first }
    }

    private fun setUpRecyclerView() {
        barChart.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = VerticalBarChartAdapter(requireActivity(), calculateTransactionsSums(LocalDatabaseProvider(requireActivity()).getTransactionsOfUser(AustromApplication.appUser!!)))
        barChart.adapter = adapter
    }
}