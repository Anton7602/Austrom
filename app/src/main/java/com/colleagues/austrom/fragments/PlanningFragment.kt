package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.PlanningGraphRecyclerAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType

class PlanningFragment : Fragment(R.layout.fragment_planning) {
    fun setOnNavigationDrawerOpenCalled(l: ()->Unit) { requestNavigationDrawerOpen = l }
    private var requestNavigationDrawerOpen: ()->Unit = {}
    //region Binding
    private lateinit var planHolderRecycler: RecyclerView
    private lateinit var callNavDrawerButton: ImageButton
    private fun bindViews(view: View) {
        planHolderRecycler = view.findViewById(R.id.plan_planHolder_rcv)
        callNavDrawerButton = view.findViewById(R.id.plan_navDrawer_btn)
    }
    //endregion

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerView()
        callNavDrawerButton.setOnClickListener { requestNavigationDrawerOpen() }
    }

    private fun setUpRecyclerView() {
        val localDBProvider = LocalDatabaseProvider(requireActivity())
        planHolderRecycler.layoutManager = LinearLayoutManager(requireActivity())
        val dataset = mutableListOf<Pair<String, List<Transaction>>>()
        AustromApplication.activeCategories.values.filter { l -> l.transactionType== TransactionType.EXPENSE }.forEach { category ->
            val transactions = localDBProvider.getTransactionOfCategory(category)
            dataset.add(Pair(category.name, transactions))
        }
        val adapter = PlanningGraphRecyclerAdapter(requireActivity(), dataset)
        planHolderRecycler.adapter = adapter
    }
}