package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication.Companion.activeCategories
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.VerticalBarChartAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.PeriodTypeSelectionDialogFragment
import com.colleagues.austrom.dialogs.sidesheetdialogs.AnalyticsSelectionDialogFragment
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.DateControllerView
import com.colleagues.austrom.views.PieChartDiagramView
import java.time.LocalDate
import kotlin.math.absoluteValue

class AnalyticsSplitByCategoryFragment : Fragment(R.layout.fragment_analytics_split_by_category) {
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var pieChart: PieChartDiagramView
    private lateinit var barChart: RecyclerView
    private lateinit var testButton: ImageButton
    private lateinit var callNavDrawerButton: ImageButton
    private lateinit var dateController: DateControllerView
    private fun bindViews(view: View) {
        pieChart = view.findViewById(R.id.bud_chart_pch)
        barChart = view.findViewById(R.id.bud_bar_chart_rcv)
        testButton = view.findViewById(R.id.bud_testSheetBtn_btn)
        callNavDrawerButton = view.findViewById(R.id.bud_navDrawer_btn)
        dateController = view.findViewById(R.id.bud_dateController_dcon)
    }
    //endregion


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    override fun onStart() {
        super.onStart()
        setUpDateController()
        testButton.setOnSafeClickListener { launchAnalyticsSelectionDialog() }
        callNavDrawerButton.setOnSafeClickListener { requestNavigationDrawerOpen() }
    }

    private fun launchAnalyticsSelectionDialog() {
        val dialog = AnalyticsSelectionDialogFragment(requireActivity())
        dialog.setOnAnalyticsSelectedListener { selectedFragment -> (requireActivity() as MainActivity).switchFragment(selectedFragment) }
        dialog.show()
    }

    private fun calculateTransactionsSums(transactions: List<Transaction>) : List<Pair<Double, String>> {
        val transactionsByCategories = mutableMapOf<String, Double>()
        val dataSet = mutableListOf<Pair<Double, String>>()
        transactions.forEach { transaction ->
            if (transactionsByCategories.containsKey(transaction.categoryId)) {
                transactionsByCategories[transaction.categoryId] = transactionsByCategories[transaction.categoryId]!! +  transaction.getAmountInBaseCurrency().absoluteValue
            } else {
                transactionsByCategories[transaction.categoryId] = transaction.getAmountInBaseCurrency().absoluteValue
            }
        }
        transactionsByCategories.forEach { (categoryId, transactionsSum) -> dataSet.add(Pair(transactionsSum, activeCategories[categoryId]!!.name)) }
        return dataSet.sortedByDescending { l->l.first }
    }

    private fun setUpRecyclerView(transactionsGroups: List<Pair<Double, String>>) {
        barChart.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = VerticalBarChartAdapter(requireActivity(), transactionsGroups)
        barChart.adapter = adapter
    }

    private fun setUpDateController() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        dateController.setDatesRangeChangedListener { dateRange ->
            localDBProvider.getTransactionsByTransactionFilterAsync(
                TransactionFilter(activeCategories.values.filter { l -> l.transactionType==TransactionType.EXPENSE }.map { l -> l.categoryId }.toMutableList(),
                    dateRange.first, dateRange.second)).observe(viewLifecycleOwner) { transactionList ->
                        val calculatedSumsOfTransactions = calculateTransactionsSums(transactionList)
                        pieChart.setChartData(calculatedSumsOfTransactions)
                        setUpRecyclerView(calculatedSumsOfTransactions)
            }
        }

        dateController.setPeriodTypeChangeRequestedListener { launchPeriodTypeSelectionDialog() }
        dateController.setDate(LocalDate.now())
    }

    private fun launchPeriodTypeSelectionDialog() {
        val dialog = PeriodTypeSelectionDialogFragment()
        dialog.setOnDialogResultListener { periodType -> dateController.setPeriodType(periodType) }
        dialog.show(requireActivity().supportFragmentManager, "Date Period Type Selection")
    }
}