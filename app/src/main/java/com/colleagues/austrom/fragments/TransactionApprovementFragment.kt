package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.TransactionExtendedRecyclerAdapter
import com.colleagues.austrom.database.FirebaseDatabaseProvider
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.views.TransactionHeaderView

class TransactionApprovementFragment(private val transactionsList: MutableList<Transaction>) : Fragment(R.layout.fragment_transaction_approvement) {
    constructor(): this(mutableListOf())
    fun setOnFragmentChangeRequestedListener(l: ((Fragment?)->Unit)) { changeFragment = l }
    private var changeFragment: (Fragment?) -> Unit = {}

    //region Binding
    private lateinit var transactionHolder: RecyclerView
    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.tranapprov_transactionExampleRecycler_rcv)
    }
    //endregion
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        transactionHolder.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = TransactionExtendedRecyclerAdapter(transactionsList, requireActivity(), true)
        adapter.setOnItemClickListenerAccept { transaction -> transaction.submit(LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity()))}
        adapter.setOnItemClickListenerEdit { transaction -> changeFragment(TransactionEditFragment(transaction)) }
        transactionHolder.adapter = adapter
    }
}