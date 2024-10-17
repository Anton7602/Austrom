package com.colleagues.austrom.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.models.Language

class LanguageRecyclerAdapter(private val languages: List<Language>, private val activity: AppCompatActivity): RecyclerView.Adapter<LanguageRecyclerAdapter.LanguageViewHolder>() {
    class LanguageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var languageHolder: CardView = itemView.findViewById(R.id.lanit_languageHolder_crv)
        var languageName: TextView = itemView.findViewById(R.id.lanit_languageName_txt)
        var selectionMarker: RadioButton = itemView.findViewById(R.id.lanit_selectionMarker)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false))
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.languageName.text = languages[position].languageName
        holder.selectionMarker.isChecked = languages[position].languageCode==activity.resources.configuration.locales.get(0).language
        holder.languageHolder.setOnClickListener {
            switchLanguage(languages[position].languageCode)
        }
        holder.selectionMarker.setOnClickListener {
            switchLanguage(languages[position].languageCode)
        }
    }

    private fun switchLanguage(languageCode: String) {
        (activity.application as AustromApplication).setApplicationLanguage(languageCode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        } else  {
            activity.recreate()
        }
    }
}