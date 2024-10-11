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

class SettingsButtonView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    private lateinit var labelTextView: TextView
    private lateinit var valueTextView: TextView
    private lateinit var iconImageView: ImageView

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.view_setting_button, this, true)
        bindViews(view)

        // Obtain custom attributes
        val attributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingsButtonView)
        val labelText = attributes.getString(R.styleable.SettingsButtonView_primaryText)
        val valueText = attributes.getString(R.styleable.SettingsButtonView_secondaryText)
        val iconResId = attributes.getResourceId(R.styleable.SettingsButtonView_settingIcon, 0)

        // Set attributes to views
        labelTextView.text = labelText ?: "Unknown text"
        valueTextView.text = valueText ?: "Unknown text"
        if (iconResId != 0) {
            iconImageView.setImageResource(iconResId)
        }

        attributes.recycle()
    }

    private fun bindViews(view: View) {
        labelTextView = view.findViewById(R.id.setbtnview_primaryText_text)
        valueTextView = view.findViewById(R.id.setbtnview_secondaryText_txt)
        iconImageView = view.findViewById(R.id.setbtnview_icon_img)
    }

    fun setLabelText(text: String) {
        labelTextView.text = text
    }

    fun setValueText(text: String) {
        valueTextView.text = text
    }

    fun setIconResource(resId: Int) {
        iconImageView.setImageResource(resId)
    }
}

