package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.views.PieChartDiagramView
import java.util.Dictionary
import kotlin.math.absoluteValue

class BudgetFragment : Fragment(R.layout.fragment_budget) {
    private lateinit var budgetHolder: ConstraintLayout
    private lateinit var budgetChartHolder: FrameLayout
    private fun bindViews(view: View) {
        budgetHolder = view.findViewById(R.id.bud_holder_cly)
        budgetChartHolder = view.findViewById(R.id.bud_chartHolder_fly)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    override fun onStart() {
        super.onStart()
        val dbProvider = LocalDatabaseProvider(requireActivity())


        val pieChartView = PieChartDiagramView(requireActivity())
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        pieChartView.layoutParams = layoutParams

        budgetChartHolder.addView(pieChartView)
        //requireActivity().setContentView(pieChartView)
        pieChartView.setChartData(calculateTransactionsSums(dbProvider.getTransactionsOfUser(AustromApplication.appUser!!)))
        pieChartView.startAnimation()
    }

    private fun calculateTransactionsSums(transactions: List<Transaction>) : List<Pair<Int, String>> {
        val transactionsByCategories = mutableMapOf<String, Double>()
        val dataSet = mutableListOf<Pair<Int, String>>()
        transactions.forEach { transaction ->
            if (transactionsByCategories.containsKey(transaction.categoryId)) {
                transactionsByCategories[transaction.categoryId] = transactionsByCategories[transaction.categoryId]!! +  transaction.amount.absoluteValue
            } else {
                transactionsByCategories[transaction.categoryId] = transaction.amount.absoluteValue
            }
        }
        transactionsByCategories.forEach { (categoryId, transactionsSum) ->
            if (AustromApplication.activeExpenseCategories.containsKey(categoryId)) {
                dataSet.add(Pair(transactionsSum.toInt(), AustromApplication.activeExpenseCategories[categoryId]!!.name))
            }
        }
        return dataSet
    }
}