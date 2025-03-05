package com.colleagues.austrom.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.AustromApplication.Companion.activeCategories
import com.colleagues.austrom.MainActivity
import com.colleagues.austrom.R
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.dialogs.bottomsheetdialogs.PeriodTypeSelectionDialogFragment
import com.colleagues.austrom.dialogs.sidesheetdialogs.AnalyticsSelectionDialogFragment
import com.colleagues.austrom.extensions.setOnSafeClickListener
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionFilter
import com.colleagues.austrom.models.TransactionType
import com.colleagues.austrom.views.DateControllerView
import com.colleagues.austrom.views.MoneyFormatTextView
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
    private lateinit var finalSumMoneyFormat: MoneyFormatTextView
    private lateinit var changePerPeriodTextView: TextView
    private fun bindViews(view: View) {
        weightedBarChart = view.findViewById(R.id.nwhist_barChart_wbch)
        testButton = view.findViewById(R.id.nwhist_testSheetBtn_btn)
        callNavDrawerButton = view.findViewById(R.id.nwhist_navDrawer_btn)
        dateController = view.findViewById(R.id.nwhist_dateController_dcon)
        finalSumMoneyFormat = view.findViewById(R.id.nwhist_finalSum_monf)
        changePerPeriodTextView = view.findViewById(R.id.nwhist_moneyChange_txt)
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
        setUpBoxChart(LocalDatabaseProvider(requireActivity()).getTransactionsOfUser(AustromApplication.appUser!!))
    }

    @SuppressLint("SetTextI18n")
    private fun setUpBoxChart(transactionList: List<Transaction>) {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        val finalNetWorth = calculateFinalDateNetWorth(dateController.getSelectedDatesRange().second)
        val changeDuringPeriod = Transaction.getSumOfTransactions(localDBProvider.getTransactionBetweenDates(dateController.getSelectedDatesRange().first,
            dateController.getSelectedDatesRange().second))
        finalSumMoneyFormat.setValue(finalNetWorth)
        changePerPeriodTextView.setTextColor(if (changeDuringPeriod>0) requireContext().getColor(R.color.incomeGreenBackground) else (requireContext().getColor(R.color.expenseRedBackground)) )
        changePerPeriodTextView.text = "${if (changeDuringPeriod>0) "+" else ""} ${changeDuringPeriod.toMoneyFormat()} (${if (changeDuringPeriod>0) "+" else ""} ${"%.2f".format((changeDuringPeriod/(finalNetWorth-changeDuringPeriod))*100)} %)"
        weightedBarChart.setData(transactionList, dateController.getSelectedDatesRange().first, dateController.getSelectedDatesRange().second, finalNetWorth)
    }

    private fun launchAnalyticsSelectionDialog() {
        val dialog = AnalyticsSelectionDialogFragment(requireActivity())
        dialog.setOnAnalyticsSelectedListener { selectedFragment -> (requireActivity() as MainActivity).switchFragment(selectedFragment) }
        dialog.show()
    }

    private fun calculateFinalDateNetWorth(endDate: LocalDate): Double {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        val currentSum = Asset.getSumOfAssets(AustromApplication.activeAssets.values.toList())
        val sumOfTransactionsSince = Transaction.getSumOfTransactions(localDBProvider.getTransactionBetweenDates(endDate, LocalDate.now()))
        //finalSumMoneyFormat.setValue(currentSum-sumOfTransactionsSince)
        return currentSum-sumOfTransactionsSince
    }

    private fun calculateNetWorthChangeBetweenDates(startDate: LocalDate, endDate: LocalDate) {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        val transactionsWithinPeriod = localDBProvider.getTransactionBetweenDates(dateController.getSelectedDatesRange().first, dateController.getSelectedDatesRange().second)
    }

    private fun setUpDateController() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        dateController.setDatesRangeChangedListener { dateRange ->
            localDBProvider.getTransactionsByTransactionFilterAsync(
                TransactionFilter(
                    mutableListOf(), mutableListOf(),
                    dateRange.first, dateRange.second
                )
            ).observe(viewLifecycleOwner) { transactionList ->setUpBoxChart(transactionList) }
        }

        dateController.setPeriodTypeChangeRequestedListener { launchPeriodTypeSelectionDialog() }
        dateController.setDate(LocalDate.now())
    }

    private fun launchPeriodTypeSelectionDialog() {
        val dialog = PeriodTypeSelectionDialogFragment()
        dialog.setOnDialogResultListener { periodType -> dateController.setPeriodType(periodType); dialog.dismiss(); }
        dialog.show(requireActivity().supportFragmentManager, "Date Period Type Selection")
    }
}