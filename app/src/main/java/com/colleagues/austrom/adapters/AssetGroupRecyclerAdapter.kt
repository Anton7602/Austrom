package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Asset

class AssetGroupRecyclerAdapter(private var assetTypes: Map<String, MutableList<Asset>>,
                                private var activity: AppCompatActivity) : RecyclerView.Adapter<AssetGroupRecyclerAdapter.AssetGroupViewHolder>() {
    class AssetGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assetTypeName: TextView = itemView.findViewById(R.id.assgritem_assettype_txt)
        val assetTypeSum: TextView= itemView.findViewById(R.id.assgritem_sumamount_txt)
        val assetTypeCurrencySymbol: TextView= itemView.findViewById(R.id.assgritem_currencysymbol_txt)
        val assetsListHolder: RecyclerView= itemView.findViewById(R.id.assgritem_assetholder_rcv)
        val dropdownButton: ImageButton = itemView.findViewById(R.id.assgritem_dropdown_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetGroupViewHolder {
        return AssetGroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_asset_group, parent, false))
    }

    override fun getItemCount(): Int {
        return assetTypes.size
    }

    override fun onBindViewHolder(holder: AssetGroupViewHolder, position: Int) {
        holder.assetTypeName.text = assetTypes.keys.elementAt(position)
        holder.assetTypeCurrencySymbol.text = AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol
        var sum = 0.0
        for (asset in assetTypes.values.elementAt(position)) {
            sum += if (asset.currencyCode.equals(AustromApplication.appUser?.baseCurrencyCode)) {
                asset.amount
            } else {
                asset.amount/AustromApplication.activeCurrencies[asset.currencyCode]!!.exchangeRate
            }
        }
        holder.assetTypeSum.text = sum.toMoneyFormat()
        holder.assetsListHolder.layoutManager = LinearLayoutManager(activity)
        holder.assetsListHolder.adapter = AssetRecyclerAdapter(assetTypes.values.elementAt(position), activity, null, true)
        holder.dropdownButton.setOnClickListener {
            if (holder.assetsListHolder.visibility == View.VISIBLE) {
                holder.assetsListHolder.visibility = View.GONE
                holder.dropdownButton.setImageResource(R.drawable.ic_navigation_down_temp)
            } else {
                holder.assetsListHolder.visibility = View.VISIBLE
                holder.dropdownButton.setImageResource(R.drawable.ic_navigation_up_temp)
            }
        }
    }
}