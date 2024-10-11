package com.colleagues.austrom.adapters

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.R
import java.util.Locale

class LanguageRecyclerAdapter(private val languages: List<String>, private val activity: AppCompatActivity): RecyclerView.Adapter<LanguageRecyclerAdapter.LanguageViewHolder>() {
    class LanguageViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var languageHolder: CardView = itemView.findViewById(R.id.lanit_languageHolder_crv)
        var languageName: TextView = itemView.findViewById(R.id.lanit_languageName_txt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false))
    }

    override fun getItemCount(): Int {
        return languages.size
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.languageName.text = languages[position]
        holder.languageHolder.setOnClickListener {
            when (languages[position]) {
                "English" -> changeLocale("en")
                "Russian" -> changeLocale("ru")
            }

        }
    }

    private fun changeLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        activity.recreate()
    }
}