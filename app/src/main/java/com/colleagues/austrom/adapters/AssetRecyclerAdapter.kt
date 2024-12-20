package com.colleagues.austrom.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.extensions.startWithUppercase
import com.colleagues.austrom.extensions.toMoneyFormat
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.AssetType

class AssetRecyclerAdapter(private val assets: MutableList<Asset>, private val activity: AppCompatActivity) : RecyclerView.Adapter<AssetRecyclerAdapter.AssetViewHolder>()  {
    class AssetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val assetHolder: CardView = itemView.findViewById(R.id.asitem_assetHolder_crv)
        val assetName: TextView = itemView.findViewById(R.id.asitem_assetName_txt)
        val assetType: TextView = itemView.findViewById(R.id.asitem_assetType_txt)
        val assetAmount: TextView = itemView.findViewById(R.id.asitem_amount_txt)
        val currencySymbol: TextView = itemView.findViewById(R.id.asitem_currencySymbol_txt)
        val assetBaseAmount: TextView = itemView.findViewById(R.id.asitem_baseCurrencyAmount_txt)
        val assetBaseSymbol: TextView = itemView.findViewById(R.id.asitem_baseCurrencySymbol_txt)
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
        holder.assetAmount.text = asset.amount.toMoneyFormat()
        holder.currencySymbol.text = AustromApplication.activeCurrencies[asset.currencyCode]?.symbol
        if (asset.currencyCode!=AustromApplication.appUser?.baseCurrencyCode) {
            holder.assetBaseSymbol.text = AustromApplication.activeCurrencies[AustromApplication.appUser?.baseCurrencyCode]?.symbol
            holder.assetBaseAmount.text = (asset.amount/(AustromApplication.activeCurrencies[asset.currencyCode]?.exchangeRate ?: 0.0)).toMoneyFormat()
        } else {
            holder.assetBaseSymbol.visibility = View.GONE
            holder.assetBaseAmount.visibility = View.GONE
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
        holder.assetTypeImg.setImageResource(
            when(asset.assetTypeId) {
                AssetType.CASH -> R.drawable.ic_assettype_cash_temp
                AssetType.CARD -> R.drawable.ic_assettype_card_temp
                AssetType.INVESTMENT -> R.drawable.ic_placeholder_icon
                else -> R.drawable.ic_placeholder_icon
            }
        )

        holder.assetHolder.setOnClickListener {
            returnClickedItem(asset)
//            if (isAllowOpenProperties) {
//                AustromApplication.selectedAsset = asset
//                activity.startActivity(Intent(activity, AssetPropertiesActivity::class.java))
//            }
        }
    }
}