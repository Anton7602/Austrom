package com.colleagues.austrom.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.colleagues.austrom.R
import com.google.android.material.textfield.TextInputLayout

class SelectorButtonView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    private lateinit var selectorTil: TextInputLayout
    private lateinit var selectorTxt: TextView

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_selector_button, this, true)
        bindViews(view)

        // Obtain custom attributes
        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SelectorButtonView)
        val fieldText = attributes.getString(R.styleable.SelectorButtonView_fieldName)
        val valueText = attributes.getString(R.styleable.SelectorButtonView_fieldValue)

        // Set attributes to views
        selectorTil.hint = fieldText ?: "Field"
        selectorTxt.text = valueText ?: "Value"

        attributes.recycle()
    }

    fun setFieldValue(value: String) {
        selectorTxt.text = value
    }

    private fun bindViews(view: View) {
        selectorTil = view.findViewById(R.id.selbtnview_selector_til)
        selectorTxt = view.findViewById(R.id.selbtnview_selector_txt)
    }
}