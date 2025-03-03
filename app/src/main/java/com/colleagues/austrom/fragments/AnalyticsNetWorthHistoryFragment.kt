package com.colleagues.austrom.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.AustromApplication.Companion.activeCategories
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.sidesheetdialogs.AnalyticsSelectionDialogFragment
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.DateControllerView
import com.colleagues.austrom.views.WeightedBarChartDiagramView
import java.time.LocalDate

class AnalyticsNetWorthHistoryFragment : Fragment(R.layout.fragment_analytics_net_worth_history) {
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var weightedBarChart: WeightedBarChartDiagramView
    private lateinit var testButton: ImageButton
    private lateinit var callNavDrawerButton: ImageButton
    private lateinit var dateController: DateControllerView
    private fun bindViews(view: View) {
        weightedBarChart = view.findViewById(R.id.nwhist_barChart_wbch)
        testButton = view.findViewById(R.id.nwhist_testSheetBtn_btn)
        callNavDrawerButton = view.findViewById(R.id.nwhist_navDrawer_btn)
        dateController = view.findViewById(R.id.nwhist_dateController_dcon)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
    }

    override fun onStart() {
        super.onStart()
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        setUpDateController()
        testButton.setOnSafeClickListener { launchAnalyticsSelectionDialog() }
        callNavDrawerButton.setOnSafeClickListener { requestNavigationDrawerOpen() }

        weightedBarChart.setData(localDBProvider.getTransactionsOfUser(AustromApplication.appUser!!), dateController.getSelectedDatesRange().first,
            dateController.getSelectedDatesRange().second, 800000.0)
    }

    private fun launchAnalyticsSelectionDialog() {
        val dialog = AnalyticsSelectionDialogFragment(requireActivity())
        dialog.setOnAnalyticsSelectedListener { selectedFragment -> (requireActivity() as MainActivity).switchFragment(selectedFragment) }
        dialog.show()
    }

    private fun setUpDateController() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        dateController.setDatesRangeChangedListener { dateRange ->
            localDBProvider.getTransactionsByTransactionFilterAsync(
                TransactionFilter(
                    activeCategories.values.map { l -> l.categoryId }.toMutableList(),
                    dateRange.first, dateRange.second)
            ).observe(viewLifecycleOwner) { transactionList ->
                weightedBarChart.setData(transactionList, dateController.getSelectedDatesRange().first,
                    dateController.getSelectedDatesRange().second, 800000.0)
            }
        }

        dateController.setPeriodTypeChangeRequestedListener {
            //launchPeriodTypeSelectionDialog()
            }
        dateController.setDate(LocalDate.now())
    }


}