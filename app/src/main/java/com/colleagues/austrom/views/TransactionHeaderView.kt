package com.colleagues.austrom.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toMoneyFormat

class TransactionHeaderView (context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    //region Binding
    private lateinit var incomeSumTextView: TextView
    private lateinit var expenseSumTextView: TextView
    private lateinit var holderCardView: CardView
    private fun bindViews(view: View) {
        incomeSumTextView = view.findViewById(R.id.trlistheadview_incomeSum_txt)
        expenseSumTextView = view.findViewById(R.id.trlistheadview_expenseSum_txt)
        holderCardView = view.findViewById(R.id.trlistheadview_holder_crd)
    }
    //endregion
    private var incomeSum: Double = 0.0
    private var expenseSum: Double = 0.0
    private var currencySymbol: String = "$"

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_transaction_list_header, this, true)
        bindViews(view)

        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.TransactionHeaderView)
        incomeSum = attributes.getFloat(R.styleable.TransactionHeaderView_IncomeSum, 0f).toDouble()
        expenseSum = attributes.getFloat(R.styleable.TransactionHeaderView_ExpenseSum, 0f).toDouble()
        currencySymbol = attributes.getString(R.styleable.TransactionHeaderView_CurrencySymbol) ?: "$"

        incomeSumTextView.text = "${incomeSum.toMoneyFormat()} $currencySymbol"
        expenseSumTextView.text = "${expenseSum.toMoneyFormat()} $currencySymbol"
        holderCardView.setBackgroundResource(R.drawable.img_transaction_header_card_background)

        attributes.recycle()
    }

    fun setIncome(value: Double) {
        incomeSum = value
        incomeSumTextView.text = "${incomeSum.toMoneyFormat()} $currencySymbol"
    }

    fun setExpense(value: Double) {
        expenseSum = value
        expenseSumTextView.text = "${expenseSum.toMoneyFormat()} $currencySymbol"
    }

    fun setCurrencySymbol(value: String) {
        currencySymbol = value
        incomeSumTextView.text = "${incomeSum.toMoneyFormat()} $currencySymbol"
        expenseSumTextView.text = "${expenseSum.toMoneyFormat()} $currencySymbol"
    }
}