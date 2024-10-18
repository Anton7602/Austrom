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
import com.colleagues.austrom.extensions.startWithUppercase
import java.util.Locale

class LanguageRecyclerAdapter(private val languages: List<Locale>, private val activity: AppCompatActivity): RecyclerView.Adapter<LanguageRecyclerAdapter.LanguageViewHolder>() {
    class LanguageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var languageHolder: CardView = itemView.findViewById(R.id.lanit_languageHolder_crv)
        var languageName: TextView = itemView.findViewById(R.id.lanit_languageName_txt)
        var languageCode: TextView = itemView.findViewById(R.id.lanit_languageCode_txt)
        var selectionMarker: RadioButton = itemView.findViewById(R.id.lanit_selectionMarker)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false))
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.languageName.text = languages[position].displayLanguage.startWithUppercase()
        holder.languageCode.text = languages[position].language.uppercase()
        holder.selectionMarker.isChecked = languages[position]==activity.resources.configuration.locales.get(0)
        holder.languageHolder.setOnClickListener {
            switchLanguage(languages[position])
        }
        holder.selectionMarker.setOnClickListener {
            switchLanguage(languages[position])
        }
    }

    private fun switchLanguage(locale: Locale) {
        (activity.application as AustromApplication).setApplicationLanguage(locale.language)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale.language))
        } else  {
            activity.recreate()
        }
    }
}