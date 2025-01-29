package com.colleagues.austrom.adapters

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Guideline
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.views.MoneyFormatTextView

class VerticalBarChartAdapter(context: Context, private val dataSet: List<Pair<Double, String>>) : RecyclerView.Adapter<VerticalBarChartAdapter.VerticalBarChartViewHolder>() {
    class VerticalBarChartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryNameTextView: TextView = itemView.findViewById(R.id.vbaritem_groupTitle_txt)
        val moneyFormatTextView: MoneyFormatTextView = itemView.findViewById(R.id.vbaritem_groupAmount_monf)
        val percentageTextView: TextView = itemView.findViewById(R.id.vbaritem_percentValue_txt)
        val guideline: Guideline = itemView.findViewById(R.id.vbaritem_fillerGuideline_gul)
        val filler: CardView = itemView.findViewById(R.id.vbaritem_filling_cdv)
    }
    private var totalSum: Double = 0.0
    private var barChartColors: List<Int> = listOf(context.getColor(R.color.diagramColor1), context.getColor(R.color.diagramColor2), context.getColor(R.color.diagramColor3),
        context.getColor(R.color.diagramColor4), context.getColor(R.color.diagramColor5), context.getColor(R.color.diagramColor6), context.getColor(R.color.diagramColor7),
        context.getColor(R.color.diagramColor8), context.getColor(R.color.diagramColor9), context.getColor(R.color.diagramColor10))

    init {
        dataSet.forEach { dataEntry -> totalSum+=dataEntry.first }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalBarChartViewHolder {
        return VerticalBarChartViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_vertical_bar_chart, parent, false))
    }

    override fun onBindViewHolder(holder: VerticalBarChartViewHolder, position: Int) {
        val item = dataSet[position]
        holder.categoryNameTextView.text = item.second
        holder.moneyFormatTextView.setValue(item.first, AustromApplication.activeCurrencies[AustromApplication.appUser!!.baseCurrencyCode]!!)
        holder.percentageTextView.text = "${(item.first/totalSum*100).toMoneyFormat()}%"
        holder.guideline.setGuidelinePercent((item.first/totalSum).toFloat())
        holder.filler.setCardBackgroundColor(barChartColors[position % barChartColors.size])

        playAnimation(holder.guideline, 0f, (item.first/totalSum).toFloat())
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    private fun playAnimation(guideline: Guideline, startValue: Float, endValue: Float, duration: Long = 500) {
        val animator = ObjectAnimator.ofFloat(guideline, "guidelinePercent", startValue, endValue)
        animator.duration = duration
        animator.start()
    }
}