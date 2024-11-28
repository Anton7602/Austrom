package com.colleagues.austrom.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.colleagues.austrom.R
import com.colleagues.austrom.adapters.CategoryArrayAdapter
import com.colleagues.austrom.database.LocalDatabaseProvider
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.colleagues.austrom.models.Category
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionType

class CategoryPullDialogFragment(private val deletedCategory: Category, private val transactionsToChange: List<Transaction>, private val parent: IDialogInitiator? = null) : DialogFragment() {
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private lateinit var messageLabel: TextView
    private lateinit var categoriesSpinner: Spinner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val i: LayoutInflater = requireActivity().layoutInflater
        val view = i.inflate(R.layout.dialog_fragment_category_pull, null)
        bindViews(view)
        setUpValues()
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireActivity()).setView(view)
        val transactionTransferDialog = adb.create()

        acceptButton.setOnClickListener {
            val dbProvider = LocalDatabaseProvider(requireActivity())
            transactionsToChange.forEach { transaction ->
                transaction.categoryId = (categoriesSpinner.selectedItem as Category).id
                dbProvider.updateTransaction(transaction)
            }
            parent?.receiveValue("Success", "TransactionTransfer")
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        return transactionTransferDialog
    }

    private fun setUpValues() {
        val dbProvider = LocalDatabaseProvider(requireActivity())
        messageLabel.text = requireActivity().getString(R.string.transaction_Transfer_message, transactionsToChange.size.toString())
        val categories = dbProvider.getCategories(deletedCategory.transactionType).toMutableList()
        categories.remove(deletedCategory)
        categoriesSpinner.adapter = CategoryArrayAdapter(requireActivity(), categories)
    }

    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.catpull_delete_btn)
        cancelButton = view.findViewById(R.id.catpull_cancel_btn)
        messageLabel = view.findViewById(R.id.catpull_message_txt)
        categoriesSpinner = view.findViewById(R.id.catpull_categories_rcv)
    }
}