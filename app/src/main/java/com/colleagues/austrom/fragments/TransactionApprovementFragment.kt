package com.colleagues.austrom.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
    fun setOnImportCompletedListener(l: (()->Unit)) { completeImport = l }
    private var completeImport: () -> Unit = {}

    //region Binding
    private lateinit var transactionHolder: RecyclerView
    private lateinit var massImportButton: CardView
    private lateinit var counterTxt: TextView
    private fun bindViews(view: View) {
        transactionHolder = view.findViewById(R.id.tranapprov_transactionExampleRecycler_rcv)
        massImportButton = view.findViewById(R.id.tranapprov_massApprove_crv)
        counterTxt = view.findViewById(R.id.tranapprov_counter_txt)
    }
    //endregion
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        if (transactionsList.isEmpty()) completeImport
        setUpRecyclerView()
        calculateValidTransaction()
        massImportButton.setOnClickListener { acceptValidTransactions() }
    }

    private fun acceptValidTransactions() {
        for (i in transactionsList.size - 1 downTo 0) {
            if (transactionsList[i].isValid()) {
                transactionsList[i].submit(LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity() as AppCompatActivity)
                )
                transactionsList.removeAt(i)
            }
        }
        if (transactionsList.isEmpty()) {
            completeImport
        } else {
            calculateValidTransaction()
            setUpRecyclerView()
        }
    }

    private fun calculateValidTransaction() {
        var counter = 0
        transactionsList.forEach { transaction -> if (transaction.isValid()) counter++ }
        counterTxt.text = counter.toString()
    }

    private fun setUpRecyclerView() {
        transactionHolder.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = TransactionExtendedRecyclerAdapter(transactionsList, requireActivity(), true)
        adapter.setOnItemClickListenerAccept { transaction -> transaction.submit(LocalDatabaseProvider(requireActivity()), FirebaseDatabaseProvider(requireActivity())); adapter.removeTransaction(transaction)}
        adapter.setOnItemClickListenerEdit { transaction -> changeFragment(TransactionEditFragment(transaction, transactionsList)) }
        transactionHolder.adapter = adapter
    }
}