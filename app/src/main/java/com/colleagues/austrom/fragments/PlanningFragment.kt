package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.AustromApplication.Companion.activeCategories
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.PlanningGraphRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.TextEditDialogFragment
import com.colleagues.austrom.extensions.parseToDouble
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Plan
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.DateControllerView
import java.time.LocalDate

class PlanningFragment : Fragment(R.layout.fragment_planning) {
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var planHolderRecycler: RecyclerView
    private lateinit var callNavDrawerButton: ImageButton
    private lateinit var dateController: DateControllerView
    private fun bindViews(view: View) {
        planHolderRecycler = view.findViewById(R.id.plan_planHolder_rcv)
        callNavDrawerButton = view.findViewById(R.id.plan_navDrawer_btn)
        dateController = view.findViewById(R.id.plan_dateController_datc)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpDateController()
        callNavDrawerButton.setOnClickListener { requestNavigationDrawerOpen() }
    }

    private fun setUpRecyclerView(dataSet: MutableList<Pair<Category, List<Transaction>>>) {
        planHolderRecycler.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = PlanningGraphRecyclerAdapter(requireActivity(), dataSet, dateController.getSelectedDatesRange().first, dateController.getSelectedPeriodType())
        adapter.setOnItemClickListener {category -> launchPlannedValueEditDialog(category) }
        planHolderRecycler.adapter = adapter
    }

    private fun launchPlannedValueEditDialog(category: Category) {
        val dialog = TextEditDialogFragment()
        dialog.setOnDialogResultListener { result ->
            val parsedResult = result.parseToDouble() ?: return@setOnDialogResultListener
            val localDBProvider = LocalDatabaseProvider(requireActivity())
            localDBProvider.insertPlan(Plan(
                planDate = dateController.getSelectedDatesRange().first,
                planType = dateController.getSelectedPeriodType(),
                planName = category.categoryId,
                planValue = parsedResult
            ))

        }
        dialog.show(requireActivity().supportFragmentManager, "Plan Edit Dialog")
    }

    private fun setUpDateController() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        dateController.setDatesRangeChangedListener { dateRange ->
            localDBProvider.getTransactionsByTransactionFilterAsync(
                TransactionFilter(activeCategories.values.filter { l -> l.transactionType == TransactionType.EXPENSE }
                        .map { l -> l.categoryId }.toMutableList(), dateRange.first, dateRange.second)
            ).observe(requireActivity()) { transactionList -> setUpRecyclerView(splitTransactionsByCategories(transactionList))}
        }
        dateController.setDate(LocalDate.now())
    }

    private fun splitTransactionsByCategories(transactionList: List<Transaction>): MutableList<Pair<Category, List<Transaction>>> {
        val dataSet = mutableListOf<Pair<Category, List<Transaction>>>()
        activeCategories.values.filter { category -> category.transactionType==TransactionType.EXPENSE }.forEach { category ->
            dataSet.add(Pair(category, transactionList.filter { transaction -> transaction.categoryId==category.categoryId }))
        }
        return dataSet
    }
}