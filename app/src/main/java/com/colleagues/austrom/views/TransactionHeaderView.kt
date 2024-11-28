package com.colleagues.austrom.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toMoneyFormat

class TransactionHeaderView (context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    private lateinit var incomeSumTextView: TextView
    private lateinit var expenseSumTextView: TextView

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_transaction_list_header, this, true)
        bindViews(view)

        // Obtain custom attributes
        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.TransactionHeaderView)
        val incomeSum = attributes.getFloat(R.styleable.TransactionHeaderView_IncomeSum, 0f).toDouble()
        val expenseSum = attributes.getFloat(R.styleable.TransactionHeaderView_ExpenseSum, 0f).toDouble()

        // Set attributes to views
        incomeSumTextView.text = incomeSum.toMoneyFormat() ?: "0.0 $"
        expenseSumTextView.text = expenseSum.toMoneyFormat() ?: "0.0 $"

        attributes.recycle()
    }

    private fun bindViews(view: View) {
        incomeSumTextView = view.findViewById(R.id.trlistheadview_incomeSum_txt)
        expenseSumTextView = view.findViewById(R.id.trlistheadview_expenseSum_txt)
    }

    fun setIncome(value: Double) {
        incomeSumTextView.text = value.toMoneyFormat()
    }

    fun setExpense(value: Double) {
        expenseSumTextView.text = value.toMoneyFormat()
    }
}