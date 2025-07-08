package com.example.project180.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.project180.Model.ItemsModel
import com.example.project180.databinding.ViewholderBestSellerBinding

class BestSellerAdapter(
    private val items: MutableList<ItemsModel>,
    private val onItemClick: (ItemsModel) -> Unit
) : RecyclerView.Adapter<BestSellerAdapter.ViewHolder>() {

    private var context: Context? = null

    inner class ViewHolder(val binding: ViewholderBestSellerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemsModel) {
            binding.titleTxt.text = item.title
            binding.ratingTxt.text = "%.1f".format(item.rating)

            val price = item.price ?: 0.0
            val discount = item.discountedPrice ?: 0.0

            if (discount > 0 && discount < price) {
                // Afficher seulement le prix réduit
                binding.txtDiscountedPrice.text = "%.2f€".format(discount)
                binding.txtDiscountedPrice.visibility = View.VISIBLE
                binding.priceTxt.visibility = View.GONE
            } else {
                // Afficher le prix normal
                binding.priceTxt.text = "%.2f€".format(price)
                binding.priceTxt.visibility = View.VISIBLE
                binding.txtDiscountedPrice.visibility = View.GONE
                binding.priceTxt.paintFlags = 0 // Retirer le strikethrough si présent
            }

            val requestOption = RequestOptions().transform(CenterCrop())
            if (item.picUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.picUrl[0])
                    .apply(requestOption)
                    .into(binding.picBestSeller)
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ViewholderBestSellerBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ItemsModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}