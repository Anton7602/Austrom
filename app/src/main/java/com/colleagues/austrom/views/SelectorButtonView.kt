package com.colleagues.austrom.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R

class SelectorButtonView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    //region Binding
    private lateinit var selectorFieldNameTextView: TextView
    private lateinit var selectorFieldValueTextView: TextView
    private fun bindViews(view: View) {
        selectorFieldNameTextView = view.findViewById(R.id.selbtnview_fieldName_txt)
        selectorFieldValueTextView = view.findViewById(R.id.selbtnview_fieldValue_txt)
    }
    //endregion

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_selector_button, this, true)
        bindViews(view)

        // Obtain custom attributes
        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SelectorButtonView)
        val fieldText = attributes.getString(R.styleable.SelectorButtonView_fieldName)
        val valueText = attributes.getString(R.styleable.SelectorButtonView_fieldValue)

        // Set attributes to views
        selectorFieldNameTextView.text = fieldText ?: "Field"
        selectorFieldValueTextView.text = valueText ?: "Value"

        attributes.recycle()
    }

    fun setFieldValue(value: String) {
        selectorFieldValueTextView.text = value
    }
}