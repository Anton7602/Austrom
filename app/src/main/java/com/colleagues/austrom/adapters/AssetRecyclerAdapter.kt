package com.colleagues.austrom.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.views.MoneyFormatTextView

class AssetRecyclerAdapter(private val assets: MutableList<Asset>, private val context: Context) : RecyclerView.Adapter<AssetRecyclerAdapter.AssetViewHolder>()  {
    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assetAmount: MoneyFormatTextView = itemView.findViewById(R.id.asitem_assetAmount_mtxt)
        val baseAssetAmount: MoneyFormatTextView = itemView.findViewById(R.id.asitem_baseCurrencyAmount_mtxt)
        val assetHolder: CardView = itemView.findViewById(R.id.asitem_assetHolder_crv)
        val assetName: TextView = itemView.findViewById(R.id.asitem_assetName_txt)
        val assetType: TextView = itemView.findViewById(R.id.asitem_assetType_txt)
        val assetTypeImg: ImageView = itemView.findViewById(R.id.asitem_assetType_img)
        val assetOwner: TextView = itemView.findViewById(R.id.asitem_owner_txt)
        val assetOwnerIcon: ImageView = itemView.findViewById(R.id.asitem_ownerIcon_img)
    }
    private var returnClickedItem: (Asset)->Unit = {}
    fun setOnItemClickListener(l: ((Asset)->Unit)) { returnClickedItem = l }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder { return AssetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_asset,parent,false)) }
    override fun getItemCount(): Int { return assets.size }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        val asset = assets.elementAt(position)
        holder.assetName.text = asset.assetName
        holder.assetType.text = asset.assetTypeId.toString()
        holder.assetAmount.setValue(asset.amount, AustromApplication.activeCurrencies[asset.currencyCode]?.symbol ?: throw Exception())
        if (asset.currencyCode!=AustromApplication.appUser?.baseCurrencyCode) {
            holder.baseAssetAmount.visibility = View.VISIBLE
            holder.baseAssetAmount.setValue((asset.amount/(AustromApplication.activeCurrencies[asset.currencyCode]?.exchangeRate ?: 1.0)), AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol ?: throw Exception())
        } else {
            holder.baseAssetAmount.visibility = View.GONE
        }
        if (AustromApplication.appUser?.activeBudgetId==null) {
            holder.assetOwnerIcon.visibility = View.GONE
            holder.assetOwner.visibility = View.GONE
        } else {
            val username = AustromApplication.knownUsers[asset.userId]?.username
            if (username!=null) {
                holder.assetOwner.text = username.startWithUppercase()
            }
        }
        holder.assetTypeImg.setImageResource(asset.assetTypeId.iconResourceId)
        holder.assetHolder.setOnClickListener { returnClickedItem(asset) }
    }
}